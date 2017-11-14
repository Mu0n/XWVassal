package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.*;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mic.manuvers.ManeuverPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.io.*;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

import static mic.Util.*;
import static mic.Util.serializeToBase64;

/**
 * Created by Mic on 23/03/2017.
 */
class FluidAnim extends TimerTask {

    AutoRangeFinder myARF;
Integer savedOption = 0;

    FluidAnim ( AutoRangeFinder ARF, int whichOption )
    {
        this.myARF = ARF;
        this.savedOption = whichOption;
    }

    public void run() {
        final Timer timer = new Timer();
        final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");
        final AtomicInteger count = new AtomicInteger(0);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    myARF.justRunLines(savedOption);
                } catch (Exception e) {
                }
            }
        }, 0,8);
    }
}

public class AutoRangeFinder extends Decorator implements EditablePiece, MouseListener, MouseMotionListener {

    private static Boolean DEBUGMODE = false;
    private static Boolean MULTILINES = false;

    protected VASSAL.build.module.Map map;
    private static final int frontArcOption = 1;
    private static final int turretArcOption = 2;
    private static final int frontAuxArcOption = 3;
    private static final int backArcOption = 4;
    private static final int mobileSideArcOption = 5;

    public static final String ID = "auto-range-finder";

    private final FreeRotator testRotator;

    private ShipPositionState prevPosition = null;
    private ManeuverPaths lastManeuver = null;
    private FreeRotator myRotator = null;
    public FOVisualization fov = null;
    private static Map<String, Integer> keyStrokeToOptions = ImmutableMap.<String, Integer>builder()
            .put("CTRL SHIFT F", frontArcOption) //primary arc
            .put("CTRL SHIFT L", turretArcOption) //turret/TL
            .put("CTRL SHIFT N", frontAuxArcOption) //front pairs of aux arc (YV-666, Auzituck)
            .put("CTRL SHIFT V", backArcOption) //back aux arc
            .put("CTRL ALT SHIFT V", mobileSideArcOption) //mobile turret arc, must detect which one is selected on the ship
            .put("F5", 12)
            .put("F6", 13)
            .build();
    private static double RANGE1 = 282.5;
    private static double RANGE2 = 565;
    private static double RANGE3 = 847.5;
    int whichOption = -1; //which autorange option. See the Map above. -1 if none is selected.
    String bigAnnounce = "";
    public BumpableWithShape thisShip; //ship's full combined name string
    Point2D.Double A1, A2, A3, A4; //attacker ship's arc start points, case by case basis according to whichOption
    Point2D.Double E1, E2, E3, E4; //attacker ship's end points. Associated with the corresponding A's.
Boolean isThisTheOne = false;
    // Mouse stuff
    protected Point anchor;
    protected String anchorLocation = "";
    protected String lastLocation = "";
    protected Point lastAnchor = new Point();

    public AutoRangeFinder() {
        this(null);
    }

    public AutoRangeFinder(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
        this.fov = new FOVisualization();
       // launch();
        map = VASSAL.build.module.Map.getMapById("Map0");
        map.getView().addMouseMotionListener(this);

    }

    protected void launch() {
            map.pushMouseListener(this);
            anchor.move(0, 0);
    }

    @Override
    public void mySetState(String s) {

    }

    @Override
    public String myGetState() {
        return "";
    }

    @Override
    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }

    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        return null;
    }

    private int getKeystrokeToOptions(KeyStroke keyStroke) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToOptions.containsKey(hotKey)) {
            return keyStrokeToOptions.get(hotKey).intValue();
        }
        return -1;
    }


    public Command keyEvent(KeyStroke stroke) {

        ArrayList<RangeFindings> rfindings = new ArrayList<RangeFindings>(); //findings compiled here

        String hotKey = HotKeyConfigurer.getString(stroke);
        Command bigCommand = piece.keyEvent(stroke);

        // check to see if the this code needs to respond to the event

        //identify which autorange option was used by using the static Map defined above in the globals, store it in an int
        whichOption = getKeystrokeToOptions(stroke);
        if (whichOption != -1 && stroke.isOnKeyRelease() == false) {

            if(whichOption == 12) {
                MULTILINES = true;
            }
            if(whichOption == 13) {
                MULTILINES = false;
            }

            isThisTheOne = true;
            if(DEBUGMODE == true) {
                FluidAnim FA = new FluidAnim(this, whichOption);
                logToChat("whichOption = " + Integer.toString(whichOption));
                FA.run();
            }

            //if the firing options were already activated, remove the visuals and exit right away
            if (this.fov != null && this.fov.getCount() > 0) {
                bigCommand.append(clearVisu());
                this.fov.execute();
                return bigCommand;
            }
            thisShip = new BumpableWithShape(this, "Ship",
                    this.getInner().getProperty("Pilot Name").toString(), this.getInner().getProperty("Craft ID #").toString());

            thisShip.refreshSpecialPoints();
            //Prepare the start of the appropriate chat announcement string - which ship are we doing this from, which kind of autorange
            prepAnnouncementStart();

            //Loop over every other target ship
            List<BumpableWithShape> BWS = getOtherShipsOnMap();
            for (BumpableWithShape b : BWS) {
                //Preliminary check, eliminate attempt to calculate this if the target is overlapping the attacker, could lead to exception error
                if (shapesOverlap(thisShip.shape, b.shape)) continue;

                findAttackerBestPoints(b);
                //Do everything method here:
                figureOutAutoRange(b, rfindings);
            }

            //draw visuals and announce the results in the chat
            Command bigAnnounceCommand = makeBigAnnounceCommand(bigAnnounce, rfindings);
            bigCommand.append(bigAnnounceCommand);
            if(this.fov !=null && this.fov.getCount() > 0) {
                bigCommand.append(this.fov);
                this.fov.execute();
                //logToChatCommand("launch execute");
            }
        } else if (KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK, false).equals(stroke)) {
            if (this.fov != null && this.fov.getCount() > 0) {
                bigCommand.append(clearVisu());
                this.fov.execute();
            }
        }

        return bigCommand;
    }

    public void justRunLines(int savedOption){
        whichOption = savedOption;
        ArrayList<RangeFindings> rfindings = new ArrayList<RangeFindings>();
        //if the firing options were already activated, remove the visuals and exit right away
        if (this.fov != null && this.fov.getCount() > 0) {
            //logToChatCommand("toggle off");
            clearVisu();
            this.fov.execute();
            return;
        }
        thisShip = new BumpableWithShape(this, "Ship",
                this.getInner().getProperty("Pilot Name").toString(), this.getInner().getProperty("Craft ID #").toString());

        thisShip.refreshSpecialPoints();
        //Prepare the start of the appropriate chat announcement string - which ship are we doing this from, which kind of autorange
        prepAnnouncementStart();

        //Loop over every other target ship
        List<BumpableWithShape> BWS = getOtherShipsOnMap();
        for (BumpableWithShape b : BWS) {
            //Preliminary check, eliminate attempt to calculate this if the target is overlapping the attacker, could lead to exception error
            if (shapesOverlap(thisShip.shape, b.shape)) continue;
            findAttackerBestPoints(b);
            //Do everything method here:
            figureOutAutoRange(b, rfindings);
        }

        //draw visuals and announce the results in the chat
        Command bigAnnounceCommand = makeBigAnnounceCommand(bigAnnounce, rfindings);
        if(this.fov !=null && this.fov.getCount() > 0) {
            this.fov.execute();
            //logToChatCommand("launch execute");
        }
    }

    private FOVisualizationClear clearVisu() {
        getMap().removeDrawComponent(this.fov);
        this.fov.shapes.clear();
        this.fov.lines.clear();
        this.fov.shapesWithText.clear();
        return new FOVisualizationClear(this.fov.getId());
    }

    private void figureOutAutoRange(BumpableWithShape b, ArrayList<RangeFindings> rfindings) {
        Point2D.Double D1 = findClosestVertex(b, thisShip);
        Point2D.Double D2 = find2ndClosestVertex(b, thisShip);
        Point2D.Double D3 = find3rdClosestVertex(b, thisShip);

        MicLine bestLine = null;
        switch(whichOption){
            case turretArcOption:
                bestLine = findBestLine(D1, D2, 3);
                break;
            case frontArcOption:
            case backArcOption:
                bestLine = findBestLineInSimpleArcs(D1, D2, D3, 3);
                break;
            case frontAuxArcOption:
                bestLine = findBestLineInFrontAuxArcs(D1, D2, D3, 3);
                break;
            case mobileSideArcOption:
                bestLine = findBestLineInMobileArc(D1, D2, D3, 3);
                break;
        }

        if (bestLine == null) return;
        bestLine.isBestLine = true;

        //Prepare the end of the appropriate chat announcement string - targets with their range, obstruction notice if relevant, ordered per range
        String bShipName = b.pilotName + "(" + b.shipName + ")";
        RangeFindings found = new RangeFindings(bestLine.rangeLength, bShipName);


        //deal with the case where's there no chance of having multiple best lines first
        if ((!isTargetOutsideofRectangles(thisShip, b, true) && are90degreesAligned(thisShip, b) == false) ||
                isTargetOutsideofRectangles(thisShip, b, true)) {
            //TO DO: replace with a different range cap later, associated with the type of arc (for huge ships)
            if (bestLine.rangeLength > 3) return;

            //find if there's an obstruction
            List<BumpableWithShape> obstructions = getObstructionsOnMap();
            for (BumpableWithShape obstruction : obstructions) {
                if (isLine2DOverlapShape(bestLine.line, obstruction.shape)) {
                    bestLine.rangeString += " obstructed";
                    fov.add(obstruction.shape);
                    found.isObstructed = true;
                    break;
                }
            }

            rfindings.add(found);
            fov.addLine(bestLine);
        }
        else {
            //multiple lines case
            int quickDist = (int) Math.ceil(Math.sqrt(Math.pow(A1.getX() - D1.getX(), 2.0) + Math.pow(A1.getY() - D1.getY(), 2.0)) / 282.5);
            if (quickDist > 3) return;

            double wantedWidth = 0.0;
            if (whichOption == turretArcOption) wantedWidth = thisShip.getChassisWidth();
            else wantedWidth = thisShip.getChassisWidth() - thisShip.chassis.getCornerToFiringArc() * 2.0;

            Shape fromShip = findInBetweenRectangle(thisShip, b, wantedWidth, whichOption); //use only the sides you need
            Shape fromTarget = findInBetweenRectangle(b, thisShip, b.getChassisWidth(), turretArcOption); //use all 4 sides

            if (fromShip == null) return;
            Area a1 = new Area(fromShip);
            Area a2 = new Area(fromTarget);
            a1.intersect(a2);

            //TO DO:
            //Initial step: if an obstacle intersects this rectangle, get this rectangular shape and find its 2 lengthwise edges
            //case 1: the 2 lines intersect the SAME obstacle. Then, no chance of finding a non-obstructed line. Case closed
            //case 2: if the 2 lines are crossed by different obstacles, then ray-cast all the possible lines and check for an obstacle free line
            double extra = getExtraAngleDuringRectDetection(thisShip, b);
            ShapeWithText bestBand = new ShapeWithText(a1.getBounds2D(), thisShip.getAngleInRadians() + extra);
            rfindings.add(found);
            fov.addShapeWithText(bestBand);
        }
    }

    private MicLine findBestLineInMobileArc(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {
        MicLine best = null;
        return best;
    }

    private MicLine findBestLineInFrontAuxArcs(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {
        ArrayList<MicLine> lineList = new ArrayList<MicLine>(); //reserved for front arc lines
        ArrayList<MicLine> leftLineList = new ArrayList<MicLine>(); //reserved for left aux arc lines
        ArrayList<MicLine> rightLineList = new ArrayList<MicLine>(); //reserved for right aux arc lines
        ArrayList<MicLine> noAngleCheckList = new ArrayList<MicLine>(); //along the arc edge, so already angle valid

        Point2D.Double LC = thisShip.getVertices().get(0);
        Point2D.Double RC = thisShip.getVertices().get(1);

        //Prep segments along ships (attacker and defender) used to figure out some of the firing lines
        //Left Edge
        MicLine LE = new MicLine(A1, LC, false);
        //Left Front Edge
        MicLine LFE = new MicLine(LC, A2, false);
        //Right Edge
        MicLine RE = new MicLine(RC, A4, false);
        //Right Front Edge
        MicLine RFE = new MicLine(A3, RC, false);
        //Front arc Edge
        MicLine AA = new MicLine(A2, A3, false);
        //Closest Defender Edge
        MicLine DD = new MicLine(D1, D2, false);
        //2nd closest defender edge
        MicLine DD_2nd = new MicLine(D1, D3, false);


        MicLine lineToVet; //temp MicLine object used for debug testing

        //Left Corner Attacker to Closest Defender
        MicLine LCD1 = new MicLine(LC, D1, false);
        lineToVet = vetThisLine(LCD1, "LCD1", 0.1);
        if(lineToVet != null) leftLineList.add(lineToVet);

        //Left Corner Attacker to 2nd closest Defender
        MicLine LCD2 = new MicLine(LC, D2, false);
        lineToVet = vetThisLine(LCD2, "LCD2", 0.1);
        if(lineToVet != null) leftLineList.add(lineToVet);

        //Right Corner Attacker to Closest Defender
        MicLine RCD1 = new MicLine(RC, D1, false);
        lineToVet = vetThisLine(RCD1, "RCD1", 0.1);
        if(lineToVet != null) rightLineList.add(lineToVet);

        //Right Corner Attacker to 2nd closest Defender
        MicLine RCD2 = new MicLine(RC, D2, false);
        lineToVet = vetThisLine(RCD2, "RCD2", 0.1);
        if(lineToVet != null) rightLineList.add(lineToVet);


        //first arc edge
        MicLine A1DD_arc_restricted = createLineAxtoDD_along_arc_edge(A1, E1, DD);
        lineToVet = vetThisLine(A1DD_arc_restricted, "A1DD_ea", 0.5);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);
        //first arc edge to 2nd def edge
        MicLine A1DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A1, E1, DD_2nd);
        lineToVet = vetThisLine(A1DD_arc_restricted_2nd, "A1DD_2nd_ea", 0.7);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);

        //2nd arc edge
        MicLine A2DD_arc_restricted = createLineAxtoDD_along_arc_edge(A2, E2, DD);
        lineToVet = vetThisLine(A2DD_arc_restricted, "A2DD_ea", 0.5);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);
        //2nd arc edge to 2nd def edge
        MicLine A2DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A2, E2, DD_2nd);
        lineToVet = vetThisLine(A2DD_arc_restricted_2nd, "A2DD_2nd_ea", 0.7);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);

        //3rd arc edge
        MicLine A3DD_arc_restricted = createLineAxtoDD_along_arc_edge(A3, E3, DD);
        lineToVet = vetThisLine(A3DD_arc_restricted, "A3DD_ea", 0.5);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);
        //3rd arc edge to 2nd def edge
        MicLine A3DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A3, E3, DD_2nd);
        lineToVet = vetThisLine(A3DD_arc_restricted_2nd, "A3DD_2nd_ea", 0.7);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);

        //4th arc edge
        MicLine A4DD_arc_restricted = createLineAxtoDD_along_arc_edge(A4, E4, DD);
        lineToVet = vetThisLine(A4DD_arc_restricted, "A4DD_ea", 0.5);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);
        //4th arc edge to 2nd def edge
        MicLine A4DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A4, E4, DD_2nd);
        lineToVet = vetThisLine(A4DD_arc_restricted_2nd, "A4DD_2nd_ea", 0.7);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);


        //front arc lines
        MicLine A2D1 = new MicLine(A2, D1, false);
        lineToVet = vetThisLine(A2D1, "A2D1", 0.3);
        if(lineToVet != null) lineList.add(lineToVet);
        MicLine A2D2 = new MicLine(A2, D2, false);
        lineToVet = vetThisLine(A2D2, "A2D2", 0.5);
        if(lineToVet != null) lineList.add(lineToVet);

        MicLine A3D1 = new MicLine(A3, D1, false);
        lineToVet = vetThisLine(A3D1, "A3D1", 0.3);
        if(lineToVet != null) lineList.add(lineToVet);
        MicLine A3D2 = new MicLine(A3, D2, false);
        lineToVet = vetThisLine(A3D2, "A3D2", 0.5);
        if(lineToVet != null) lineList.add(lineToVet);

        //normal to defender's edges
        //Closest attacker's point to the defender's closest edge
        MicLine A1DD = createLinePtoAB(A1, DD, true);
        lineToVet = vetThisLine(A1DD, "A1DD", 0.2);
        if(lineToVet != null) lineList.add(lineToVet);

        MicLine A2DD = createLinePtoAB(A2, DD, true);
        lineToVet = vetThisLine(A2DD, "A2DD", 0.4);
        if(lineToVet != null) lineList.add(lineToVet);

        //Attacker's edge to defender's closest vertex
        MicLine AAD1 = createLinePtoAB(D1, AA, false);
        if(doesAAforInArcPassTest(AAD1, AA)== true && isRangeOk(AAD1, 1, rangeInt))
        {
            lineToVet = vetThisLine(AAD1, "AAD1", 0.8);
            if(lineToVet != null) noAngleCheckList.add(lineToVet);
        }

        MicLine LED1 = createLinePtoAB(D1, LE, false);
        if(doesAAforInArcPassTest(LED1, LE)== true && isRangeOk(LED1, 1, rangeInt))
        {
            lineToVet = vetThisLine(LED1, "LED1", 0.8);
            if(lineToVet != null) noAngleCheckList.add(lineToVet);
        }
        MicLine RED1 = createLinePtoAB(D1, RE, false);
        if(doesAAforInArcPassTest(RED1, RE)== true && isRangeOk(RED1, 1, rangeInt))
        {
            lineToVet = vetThisLine(RED1, "RED1", 0.8);
            if(lineToVet != null) noAngleCheckList.add(lineToVet);
        }


        ArrayList<MicLine> filteredList = new ArrayList<MicLine>();
        ArrayList<MicLine> deadList = new ArrayList<MicLine>();

        //Filter out shots that aren't inside the left aux arc
        for(MicLine l: leftLineList)
        {
            if(isEdgeInArc(l, A1, E1, A2, E2) == true) filteredList.add(l);
            else deadList.add(l);
        }
        //Same, right side
        for(MicLine l: rightLineList)
        {
            if(isEdgeInArc(l, A3, E3, A4, E4) == true) filteredList.add(l);
            else deadList.add(l);
        }
        //Same, front side
        for(MicLine l: lineList)
        {
            if(isEdgeInArc(l, A2, E2, A3, E3) == true) filteredList.add(l);
            else deadList.add(l);
        }
        //add the rest of the safe angle lines
        for(MicLine l: noAngleCheckList) {
            filteredList.add(l);
        }

        //ALLLINES: if all lines have to been added to the visuals, then, uncomment this section
        if(MULTILINES == true){
            for(MicLine everyline : filteredList) {
                fov.addLine(everyline);
            }
            for(MicLine l: deadList){
                l.markedAsDead = true;
                fov.addLine(l);
            }
        }
        //end of section


        //First criterium, find the best distance and make it the best Line
        double bestDist = rangeInt * 282.5;
        MicLine best = null;
        for (MicLine l : filteredList) {
            if (l.markedAsDead == false && Double.compare(bestDist, l.pixelLength) > 0) {
                bestDist = l.pixelLength;
                best = l;
            }
        }
        //nothing under the requested range was found, no best lines can be submitted
        if (best == null) {
            return null;
        }

        best.isArcLine = true;
        return best;
    }


    private MicLine vetThisLine(MicLine A1D1, String label, double v) {
        if(DEBUGMODE == false) return A1D1;
        return new MicLine(A1D1.first, A1D1.second, A1D1.markedAsDead, label, v);
    }


    private MicLine findBestLineInSimpleArcs(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {
        ArrayList<MicLine> lineList = new ArrayList<MicLine>();
        ArrayList<MicLine> noAngleCheckList = new ArrayList<MicLine>(); //along the arc edge, so already angle valid

        //Closest Attacker Edge
        MicLine AA = new MicLine(A1, A2, false);
        //Closest Defender Edge
        MicLine DD = new MicLine(D1, D2, false);
        //2nd closest defender edge
        MicLine DD_2nd = new MicLine(D1, D3, false);

        MicLine lineToVet;

        //Closest Attacker to Closest Defender
        MicLine A1D1 = new MicLine(A1, D1, false);
        lineToVet = vetThisLine(A1D1, "A1D1", 0.1);
        if(lineToVet != null) lineList.add(lineToVet);

        //Closest Defender to 2nd Closest Attacker
        MicLine A2D1 = new MicLine(A2, D1, false);
        lineToVet = vetThisLine(A2D1, "A2D1", 0.5);
        if(lineToVet != null) lineList.add(lineToVet);

        //Closest attacker's point to the defender's closest edge
        MicLine A1DD = createLinePtoAB(A1, DD, true);
        lineToVet = vetThisLine(A1DD, "A1DD", 0.2);
        if(lineToVet != null) lineList.add(lineToVet);

        MicLine A2DD = createLinePtoAB(A2, DD, true);
        lineToVet = vetThisLine(A2DD, "A2DD", 0.4);
        if(lineToVet != null) lineList.add(lineToVet);

        //Closest attacker's point to the defender's closest edge along the arc edge
        MicLine A1DD_arc_restricted = createLineAxtoDD_along_arc_edge(A1, E1, DD);
        lineToVet = vetThisLine(A1DD_arc_restricted, "A1DD_ea", 0.5);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);

        MicLine A2DD_arc_restricted = createLineAxtoDD_along_arc_edge(A2, E2, DD);
        lineToVet = vetThisLine(A2DD_arc_restricted, "A2DD_ea", 0.7);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);

        //Closest attacker's point to the defender's 2nd closest edge along the arc edge
        MicLine A1DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A1, E1, DD_2nd);
        lineToVet = vetThisLine(A1DD_arc_restricted_2nd, "A1DD_2nd_ea", 0.1);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);

        MicLine A2DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A2, E2, DD_2nd);
        lineToVet = vetThisLine(A2DD_arc_restricted_2nd, "A2DD_2nd_ea", 0.9);
        if(lineToVet != null) noAngleCheckList.add(lineToVet);

        //Attacker's edge to defender's closest vertex
        MicLine AAD1 = createLinePtoAB(D1, AA, false);
        if(doesAAforInArcPassTest(AAD1, AA)== true && isRangeOk(AAD1, 1, rangeInt))
        {
            lineToVet = vetThisLine(AAD1, "AAD1", 0.8);
            if(lineToVet != null) noAngleCheckList.add(lineToVet);
        }

        ArrayList<MicLine> filteredList = new ArrayList<MicLine>();
        ArrayList<MicLine> deadList = new ArrayList<MicLine>();
        //Filter out shots that aren't in-arc if the turret option is not chosen
        for(MicLine l: lineList)
        {
            if(isEdgeInArc(l, A1, E1, A2, E2) == true) filteredList.add(l);
            else deadList.add(l);
        }
        //Add already vetted lines
        for(MicLine l: noAngleCheckList){
            filteredList.add(l);
        }

        //ALLLINES: if all lines have to been added to the visuals, then, uncomment this section
        if(MULTILINES == true){
            for(MicLine everyline : lineList) {
                fov.addLine(everyline);
            }
            for(MicLine l: deadList){
                l.markedAsDead = true;
                fov.addLine(l);
            }
        }
        //end of section


        //First criterium, find the best distance and make it the best Line
        double bestDist = rangeInt * 282.5;
        MicLine best = null;
        for (MicLine l : filteredList) {
            if (l.markedAsDead == false && Double.compare(bestDist, l.pixelLength) > 0) {
                bestDist = l.pixelLength;
                best = l;
            }
        }
        //nothing under the requested range was found, no best lines can be submitted
        if (best == null) {
            return null;
        }

        best.isArcLine = true;
        return best;
    }

    private void prepAnnouncementEnd() {
    }

    private void findAttackerBestPoints(BumpableWithShape b) {
        if(whichOption == turretArcOption) {
            A1 = findClosestVertex(thisShip, b);
            A2 = find2ndClosestVertex(thisShip, b);
        }
        else if(whichOption == frontArcOption){
            // Assuming an upward facing ship,
            // firing arc edges used to restrict best lines if they are crossing (unused for turret/TL.
            // 0 and 2 for CCW primary arc (start and end)
            // 1 and 3 for CW primary arc
            // 4 and 6 for CCW aux arc, CCW edge
            // 5 and 7 for CCW aux arc, CW edge
            // 8 and 10 for CW aux arc, CCW edge
            // 9 and 11 for CW aux arc, CW edge

            A1 = thisShip.tPts.get(0);
            A2 = thisShip.tPts.get(1);

            E1 = thisShip.tPts.get(2); //associated with A1 as the edge A1E1
            E2 = thisShip.tPts.get(3); //associated with A2 as the edge A2E2
        }
        else if(whichOption == backArcOption){
            A1 = thisShip.tPts.get(4);
            A2 = thisShip.tPts.get(5);

            E1 = thisShip.tPts.get(6);
            E2 = thisShip.tPts.get(7);
        }
        else if(whichOption == frontAuxArcOption){
            A1 = thisShip.tPts.get(8);
            A2 = thisShip.tPts.get(0);

            E1 = thisShip.tPts.get(10);
            E2 = thisShip.tPts.get(2);

            A3 = thisShip.tPts.get(1);
            A4 = thisShip.tPts.get(9);

            E3 = thisShip.tPts.get(3);
            E4 = thisShip.tPts.get(11);
        }
        else if(whichOption == mobileSideArcOption){
            String sideCheck ="";
            try{
                sideCheck = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
            } catch (Exception e){
                logToChat("tried to find a mobile turret definition, couldn't.");
            }
            if(sideCheck.equals("4")) {
                //left side check
                A1 = thisShip.tPts.get(5);
                A2 = thisShip.tPts.get(0);

                E1 = thisShip.tPts.get(7);
                E2 = thisShip.tPts.get(2);
            }
            else if(sideCheck.equals("2")){
                //right side check
                A1 = thisShip.tPts.get(1);
                A2 = thisShip.tPts.get(4);

                E1 = thisShip.tPts.get(3);
                E2 = thisShip.tPts.get(6);
            }
            else if(sideCheck.equals("1")){
                //do like front arc
                A1 = thisShip.tPts.get(0);
                A2 = thisShip.tPts.get(1);

                E1 = thisShip.tPts.get(2); //associated with A1 as the edge A1E1
                E2 = thisShip.tPts.get(3); //associated with A2 as the edge A2E2
            }
            else if(sideCheck.equals("3")){
                //do like back aux
                A1 = thisShip.tPts.get(4);
                A2 = thisShip.tPts.get(5);

                E1 = thisShip.tPts.get(6);
                E2 = thisShip.tPts.get(7);
            }
        }
    }

    private void prepAnnouncementStart() {
        //prepares the initial part of the chat window string that announces the firing options
        bigAnnounce = "*** Firing Options ";

        switch (whichOption) {
            case frontArcOption:
                bigAnnounce += "for the primary arc - from ";
                break;
            case turretArcOption:
                bigAnnounce += "for Target Lock/Turrets - from ";
                break;
            case backArcOption:
                bigAnnounce += "for the backward auxiliary arc - from";
                break;
            case frontAuxArcOption:
                bigAnnounce += "for the front pair of auxiliary arcs - from";
                break;
        }

        String fullShipName = thisShip.pilotName + "(" + thisShip.shipName + ")";
        bigAnnounce += fullShipName + "\n";

    }

    private Command makeBigAnnounceCommand(String bigAnnounce, ArrayList<RangeFindings> rfindings) {
        String range1String = "";
        String range2String = "";
        String range3String = "";

        boolean hasR1 = false;
        boolean hasR2 = false;
        boolean hasR3 = false;

        for (RangeFindings rf : rfindings) {
            if (rf.range == 1) {
                hasR1 = true;
                range1String += rf.fullName + (rf.isObstructed ? " [obstructed] | " : " | ");
            }
            if (rf.range == 2) {
                hasR2 = true;
                range2String += rf.fullName + (rf.isObstructed ? " [obstructed] | " : " | ");
            }
            if (rf.range == 3) {
                hasR3 = true;
                range3String += rf.fullName + (rf.isObstructed ? " [obstructed] | " : " | ");
            }
        }

        String result = bigAnnounce + (hasR1 ? "*** Range 1: " + range1String + "\n" : "") +
                (hasR2 ? "*** Range 2: " + range2String + "\n" : "") +
                (hasR3 ? "*** Range 3: " + range3String + "\n" : "");
        if (hasR1 == false && hasR2 == false && hasR3 == false) result = "No ships in range.";

        return logToChatCommand(result);
    }


    private Boolean isRangeOk(MicLine theLine, int rangeMin, int rangeMax) {
        if(theLine == null) return false;
        double minPixelDist = rangeMin * 282.5 - 282.5;
        double maxPixelDist = rangeMax * 282.5;

        if(Double.compare(theLine.pixelLength, minPixelDist) >= 0 && Double.compare(theLine.pixelLength, maxPixelDist) <=0) return true;
        return false;
    }



    //Checks if the AAD1 line, from closest defender vertex to a point somewhere inside the arc, on the attacker's edge, is actually between the arc points instead of simply outside but still on that extended line
    private boolean doesAAforInArcPassTest(MicLine AAD1, MicLine AA) {
        double x1 = AA.first.x;
        double y1 = AA.first.y;
        double x2 = AA.second.x;
        double y2 = AA.second.y;
        //Get the midpoint inside the firing arc, on the attacker
        double xp = AAD1.first.x;
        double yp = AAD1.first.y;

        double dist1 = Math.sqrt(Math.pow(xp - x1, 2.0) + Math.pow(yp - y1, 2.0));
        double dist2 = Math.sqrt(Math.pow(xp - x2, 2.0) + Math.pow(yp - y2, 2.0));
        double distaa = Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
        //is the point used in D1AA between A1 and A2?
        if(Double.compare(distaa,dist1) > 0 && Double.compare(distaa,dist2) > 0) return true;
        return false;
    }


    //this finds the line that links the attacker ship to someplace on the line formed by the closest edge of the target, using
    //the closest and 2nd closest lines to the target's vertices.

    private MicLine findBestLine(Point2D.Double D1, Point2D.Double D2, int rangeInt) {
        ArrayList<MicLine> lineList = new ArrayList<MicLine>();

        //Closest Attacker Edge
        MicLine AA = new MicLine(A1, A2, false);
        //Closest Defender Edge
        MicLine DD = new MicLine(D1, D2, false);

        MicLine lineToVet;

        //Closest Attacker to Closest Defender
        MicLine A1D1 = new MicLine(A1, D1, false);
        lineToVet = vetThisLine(A1D1, "A1D1", 0.1);
        if(lineToVet != null) lineList.add(lineToVet);

        //Closest Attacker to 2nd Closest Defender
        MicLine A1D2 = new MicLine(A1, D2, false);
        lineToVet = vetThisLine(A1D2, "A1D2", 0.3);
        if(lineToVet != null) lineList.add(lineToVet);


        //Closest Defender to 2nd Closest Attacker
        MicLine A2D1 = new MicLine(A2, D1, false);
        lineToVet = vetThisLine(A2D1, "A2D1", 0.5);
        if(lineToVet != null) lineList.add(lineToVet);

        MicLine A1DD = createLinePtoAB(A1, DD, true);
        lineToVet = vetThisLine(A1DD, "A1DD", 0.2);
        if(lineToVet != null) lineList.add(lineToVet);


        MicLine A2DD = createLinePtoAB(A2, DD, true);
        lineToVet = vetThisLine(A2DD, "A2DD", 0.4);
        if(lineToVet != null) lineList.add(lineToVet);

        MicLine D1AA = createLinePtoAB(D1, AA, false);
        lineToVet = vetThisLine(D1AA, "D1AA", 0.6);
        if(lineToVet != null) lineList.add(lineToVet);

        //MULTILINES: if all lines have to been added to the visuals, then, uncomment this section
        if(MULTILINES == true){
            for(MicLine everyline : lineList) {
                fov.addLine(everyline);
            }
        }
        //end of section

        //First criterium, find the best distance and make it the best Line
        double bestDist = rangeInt*282.5;
        MicLine best = null;
        for(MicLine l : lineList){
            if(l.markedAsDead == false && Double.compare(bestDist,l.pixelLength) > 0) {
                bestDist = l.pixelLength;
                best = l;
            }
        }
        //nothing under the requested range was found, no best lines can be submitted
        if(best==null) return null;
        best.isBestLine = true;
        return best;
    }

    private MicLine findBestArcRestrictedLine(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {

        ArrayList<MicLine> lineList = new ArrayList<MicLine>();

        //Closest Attacker Edge
        MicLine AA = new MicLine(A1, A2, false);
        //Closest Defender Edge
        MicLine DD = new MicLine(D1, D2, false);
        //2nd closest defender edge
        MicLine DD_2nd = new MicLine(D1, D3, false);


        //Closest Attacker to Closest Defender
        MicLine A1D1 = new MicLine(A1, D1, false);
        if(A1D1 != null) {
            if(DEBUGMODE == false){
                lineList.add(A1D1);
            }
            else{
                MicLine A1D1copy = new MicLine(A1D1.first, A1D1.second, A1D1.markedAsDead, "A1D1", 0.1);
                lineList.add(A1D1copy);
            }
        }
        //Closest Defender to 2nd Closest Attacker
        MicLine A2D1 = new MicLine(A2, D1, false);
        if(A2D1 != null) {
            if (DEBUGMODE == false) {
                lineList.add(A2D1);
            } else {
                MicLine A2D1copy = new MicLine(A2D1.first, A2D1.second, A2D1.markedAsDead, "A2D1", 0.5);
                lineList.add(A2D1copy);
            }
        }

        //Closest attacker's point to the defender's closest edge
        MicLine A1DD = createLinePtoAB(A1, DD, true);
        if(A1DD != null) {
            if (DEBUGMODE == false) {
                lineList.add(A1DD);
            } else {
                MicLine A1DDcopy = new MicLine(A1DD.first, A1DD.second, A1DD.markedAsDead, "A1DD", 0.2);
                lineList.add(A1DDcopy);
            }
        }

        MicLine A2DD = createLinePtoAB(A2, DD, true);
        if(A2DD != null){
            if (DEBUGMODE == false) {
                lineList.add(A2DD);
            } else {
                MicLine A2DDcopy = new MicLine(A2DD.first, A2DD.second, A2DD.markedAsDead, "A2DD", 0.4);
                lineList.add(A2DDcopy);
            }
        }

        if(whichOption == frontAuxArcOption){
            MicLine A3D1 = new MicLine(A3, D1, false);
            MicLine A4D1 = new MicLine(A4, D1, false);

            if(A3D1 != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A3D1);
                } else {
                    MicLine A3D1copy = new MicLine(A3D1.first, A3D1.second, A3D1.markedAsDead, "A3D1", 0.5);
                    lineList.add(A3D1copy);
                }
            }
            if(A4D1 != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A4D1);
                } else {
                    MicLine A4D1copy = new MicLine(A4D1.first, A4D1.second, A4D1.markedAsDead, "A4D1", 0.5);
                    lineList.add(A4D1copy);
                }
            }
        }

        if(whichOption == frontArcOption || whichOption == backArcOption || whichOption == frontAuxArcOption) {
            //Closest attacker's point to the defender's closest edge along the arc edge
            MicLine A1DD_arc_restricted = createLineAxtoDD_along_arc_edge(A1, E1, DD);
            if(A1DD_arc_restricted != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A1DD_arc_restricted);
                } else {
                    MicLine A1DD_arc_restrictedcopy = new MicLine(A1DD_arc_restricted.first, A1DD_arc_restricted.second, A1DD_arc_restricted.markedAsDead, "A1DD_ea", 0.5);
                    lineList.add(A1DD_arc_restrictedcopy);
                }
            }

            MicLine A2DD_arc_restricted = createLineAxtoDD_along_arc_edge(A2, E2, DD);
            if(A2DD_arc_restricted != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A2DD_arc_restricted);
                } else {
                    MicLine A2DD_arc_restrictedcopy = new MicLine(A2DD_arc_restricted.first, A2DD_arc_restricted.second, A2DD_arc_restricted.markedAsDead, "A2DD_ea", 0.7);
                    lineList.add(A2DD_arc_restrictedcopy);
                }
            }

            //Closest attacker's point to the defender's 2nd closest edge along the arc edge
            MicLine A1DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A1, E1, DD_2nd);
            if(A1DD_arc_restricted_2nd != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A1DD_arc_restricted_2nd);
                } else {
                    MicLine A1DD_arc_restricted_2ndcopy = new MicLine(A1DD_arc_restricted_2nd.first, A1DD_arc_restricted_2nd.second, A1DD_arc_restricted_2nd.markedAsDead, "A1DD_2nd_ea", 0.1);
                    lineList.add(A1DD_arc_restricted_2ndcopy);
                }
            }

            MicLine A2DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A2, E2, DD_2nd);
            if(A2DD_arc_restricted_2nd != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A2DD_arc_restricted_2nd);
                } else {
                    MicLine A2DD_arc_restricted_2ndcopy = new MicLine(A2DD_arc_restricted_2nd.first, A2DD_arc_restricted_2nd.second, A2DD_arc_restricted_2nd.markedAsDead, "A2DD_2nd_ea", 0.9);
                    lineList.add(A2DD_arc_restricted_2ndcopy);
                }
            }
        }
        if(whichOption == frontAuxArcOption) {
            MicLine A3DD_arc_restricted = createLineAxtoDD_along_arc_edge(A3, E3, DD);
            if(A3DD_arc_restricted != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A3DD_arc_restricted);
                } else {
                    MicLine A3DD_arc_restricted_copy = new MicLine(A3DD_arc_restricted.first, A3DD_arc_restricted.second, A3DD_arc_restricted.markedAsDead, "A3DD_ea", 0.6);
                    lineList.add(A3DD_arc_restricted_copy);
                }
            }

            MicLine A4DD_arc_restricted = createLineAxtoDD_along_arc_edge(A4, E4, DD);
            if(A4DD_arc_restricted != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A4DD_arc_restricted);
                } else {
                    MicLine A4DD_arc_restricted_copy = new MicLine(A4DD_arc_restricted.first, A4DD_arc_restricted.second, A4DD_arc_restricted.markedAsDead, "A4DD_ea", 0.7);
                    lineList.add(A4DD_arc_restricted_copy);
                }
            }

            MicLine A3DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A3, E3, DD_2nd);
            if(A3DD_arc_restricted_2nd != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A3DD_arc_restricted_2nd);
                } else {
                    MicLine A3DD_arc_restricted_2nd_copy = new MicLine(A3DD_arc_restricted_2nd.first, A3DD_arc_restricted_2nd.second, A3DD_arc_restricted_2nd.markedAsDead, "A3DD_2nd_ea", 0.8);
                    lineList.add(A3DD_arc_restricted_2nd_copy);
                }
            }

            MicLine A4DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A4, E4, DD_2nd);
            if(A4DD_arc_restricted_2nd != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A4DD_arc_restricted_2nd);
                } else {
                    MicLine A4DD_arc_restricted_2nd_copy = new MicLine(A4DD_arc_restricted_2nd.first, A4DD_arc_restricted_2nd.second, A4DD_arc_restricted_2nd.markedAsDead, "A4DD_2nd_ea", 0.9);
                    lineList.add(A4DD_arc_restricted_2nd_copy);
                }
            }

        }
        //Attacker's edge to defender's closest vertex

        MicLine AAD1 = createLinePtoAB(D1, AA, false);
        if(doesAAforInArcPassTest(AAD1, AA)== true)
            if(isRangeOk(AAD1, 1, rangeInt)) {
                MicLine AAD1copy = new MicLine(AAD1.first, AAD1.second, AAD1.markedAsDead, "AAD1", 0.8);
                lineList.add(AAD1copy);
            }
        ArrayList<MicLine> filteredList = new ArrayList<MicLine>();
        //Filter out shots that aren't in-arc if the turret option is not chosen
       /* for(MicLine l: lineList)
        {
            if(isEdgeInArc(l) == true) filteredList.add(l);
            else l.markedAsDead = true;
        }
*/

        //ALLLINES: if all lines have to been added to the visuals, then, uncomment this section
        if(MULTILINES == true){
            for(MicLine everyline : lineList) {
                fov.addLine(everyline);
            }
        }
        //end of section


        //First criterium, find the best distance and make it the best Line
        double bestDist = rangeInt * 282.5;
        MicLine best = null;
        for (MicLine l : filteredList) {
            if (l.markedAsDead == false && Double.compare(bestDist, l.pixelLength) > 0) {
                bestDist = l.pixelLength;
                best = l;
            }
        }
        //nothing under the requested range was found, no best lines can be submitted
        if (best == null) {
            return null;
        }

        best.isArcLine = true;
        return best;
    }

    private boolean isEdgeInArc(MicLine theCandidateLine, Point2D.Double leftMostStart, Point2D.Double leftMostEnd, Point2D.Double rightMostStart, Point2D.Double  rightMostEnd)
    {
        double fudgefactor = 0.00001; //open up the arc just slightly to better allow along-the-arc firing lines
        double firstArcEdgePolarAngle = getEdgeAngle(leftMostStart, leftMostEnd) - fudgefactor; //edge most counter-clockwise
        double secondArcEdgePolarAngle = getEdgeAngle(rightMostStart, rightMostEnd) + fudgefactor; //edge most clockwise

        double bestLinePolarAngle = getEdgeAngle(theCandidateLine.first, theCandidateLine.second);

       if(Double.compare(bestLinePolarAngle, firstArcEdgePolarAngle) < 0 || Double.compare(bestLinePolarAngle, secondArcEdgePolarAngle) > 0)
            return false;
        return true;
    }

    private double getEdgeAngle(Point2D.Double start, Point2D.Double end) {
        double deltaX = end.x - start.x;
        double deltaY = end.y - start.y;

        //returns a polar angle in rad, keeps it positive for easy ordering (might not be necessary)
        if(whichOption == frontAuxArcOption) {//back aux arc, hits the transition between 0 and 360, breaking the test
            double angle = Math.atan2(deltaY, deltaX);
            if(Double.compare(angle, 0.0) < 0) angle += Math.PI*2.0;
            return angle;
        }
        return Math.atan2(deltaY, deltaX) + Math.PI;
    }

    private MicLine createLineAAtoD1(MicLine A1D1, MicLine AA, Point2D.Double D1)
    {
        //Find A1 again
        double x1 = AA.first.getX();
        double y1 = AA.first.getY();
        //Find A2 again
        double x2 = AA.second.getX();
        double y2 = AA.second.getY();
        //find D1 again
        double xp = A1D1.second.getX();
        double yp = A1D1.second.getY();
//getting the shortest distance in pixels to the line formed by both (x1,y1) and (x2,y2)
        double numerator = Math.abs((xp - x1) * (y2 - y1) - (yp - y1) * (x2 - x1));
        double denominator = Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
        double shortestdist = numerator / denominator;

        double segmentPartialDist = Math.sqrt(Math.pow(A1D1.pixelLength,2.0) - Math.pow(shortestdist,2.0));
        double segmentFullDist = AA.pixelLength;
        double gapx = (x2-x1)/segmentFullDist*segmentPartialDist;
        double gapy = (y2-y1)/segmentFullDist*segmentPartialDist;
        double vector_x = x1 + gapx;
        double vector_y = y1 + gapy;

        //returns AAD1 - closest defender vertex to an attacker's edge point satisfying the 90 degrees shortest distance requirement
        return new MicLine(new Point2D.Double(vector_x,vector_y), D1, false);
    }

    //P is the closest vertex; AB are the end points of the segments that is being used to draw a line
    //using an algorithm based on this: http://www.ahristov.com/tutorial/geometry-games/point-line-distance.html
    //isPOnAttacker == true makes P a point that is part of the attacker; if false, P is a point that's part of the defender
    //this is important in order to produce a MicLine that will be oriented from attacker to defender, in order to check for proper shooting angles
    //in-arc stuff has to throw lines which don't have the right angle
    //if the attacker segment is being used, A1D1 dotproduct A1A2 needs to be positive and the partialsegment has to be land on the attacker segment in order to be retained
    private MicLine createLinePtoAB(Point2D.Double P, MicLine AB, boolean isPOnAttacker) {
        Boolean markAsDead = false;

        double x1 = AB.first.getX();
        double y1 = AB.first.getY();
        double x2 = AB.second.getX();
        double y2 = AB.second.getY();
        double xp = P.getX();
        double yp = P.getY();

        //getting the shortest distance in pixels to the line formed by both (x1,y1) and (x2,y2)
        double numerator = Math.abs((xp - x1) * (y2 - y1) - (yp - y1) * (x2 - x1));
        double denominator = Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
        double shortestdist = numerator / denominator;

        MicLine AP = new MicLine(new Point2D.Double(AB.first.x, AB.first.y), P, false);

        double segmentPartialDist = Math.sqrt(Math.pow(AP.pixelLength,2.0) - Math.pow(shortestdist,2.0));
        double segmentFullDist = AB.pixelLength;

        //check if the partial distance has point outside AB
        //whichWay = 1: points inward the segment, -1: points outward, line should be marked as dead
        int whichWay = 1;
        if(Double.compare(micLineDotProduct(AB, AP),0)<0) {
            whichWay = -1;
            markAsDead = true;
        }


        //if it points inward, it might be too long for its segment, mark it as dead if so.
        if(whichWay==1 && Double.compare(segmentPartialDist,segmentFullDist)>0)  {
            markAsDead = true;
        }

        //compute the partial vector's components
        double partialX = whichWay*(x2-x1)/segmentFullDist*segmentPartialDist;
        double partialY = whichWay*(y2-y1)/segmentFullDist*segmentPartialDist;


        double vector_x = x1 + partialX;
        double vector_y = y1 + partialY;

        //returns A1DD - closest attacker point (vertex or in-arc edge) to an defender's edge point satisfying the 90 degrees shortest distance requirement
        if(isPOnAttacker == true) return new MicLine(P, new Point2D.Double(vector_x, vector_y), markAsDead);
        else return new MicLine(new Point2D.Double(vector_x, vector_y), P, markAsDead);
    }

    private MicLine createLineAxtoDD_along_arc_edge(Point2D.Double A, Point2D.Double E, MicLine DD) {
        //getting D1 again
        double x1 = DD.first.getX();
        double y1 = DD.first.getY();
        //getting D2 again
        double x2 = DD.second.getX();
        double y2 = DD.second.getY();
        //getting A1 again
        double xp = A.getX();
        double yp = A.getY();

        MicLine AE = new MicLine(A, E, false);
        Point2D.Double D3 = null;
        D3 = findSegmentCrossPoint(AE,DD);
        if(D3 != null) return new MicLine(A,D3, false);
        return null;
    }

    //Using this algorithm to detect if the two segments cross
    //https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
    private Point2D.Double findSegmentCrossPoint(MicLine A1E1, MicLine DD)
    {
        Point2D.Double intersectionPt = new Point2D.Double(-1.0,-1.0);
        double px = A1E1.first.x;
        double py = A1E1.first.y;

        double qx = DD.first.x;
        double qy = DD.first.y;
        
        double rx = A1E1.second.x - A1E1.first.x;
        double ry = A1E1.second.y - A1E1.first.y;

        double sx = DD.second.x - DD.first.x;
        double sy = DD.second.y - DD.first.y;

        MicLine qmpLine = new MicLine(new Point2D.Double(px, py), new Point2D.Double(qx, qy), false);

        Boolean checkColinearity_1 = Double.compare(micLineCrossProduct(A1E1, DD),0.0) == 0;
        Boolean checkColinearity_2 = Double.compare(micLineCrossProduct(qmpLine, A1E1), 0.0) == 0;

        if(checkColinearity_1 == true) {
            if(checkColinearity_2 == true) return null; //TO DO parallel and intersecting
            else return null; //parallel and non-intersecting
        }

        double t = micLineCrossProduct(qmpLine, DD) / micLineCrossProduct(A1E1, DD);
        double u = micLineCrossProduct(qmpLine, A1E1) / micLineCrossProduct(A1E1, DD);

        if(Double.compare(t,0.0) >= 0 && Double.compare(t,1.0) <= 0
                && Double.compare(u,0.0) >= 0 && Double.compare(u, 1.0) <= 0)
        {
            //logToChatCommand("t " + Double.toString(t) + " u " + Double.toString(u));
            intersectionPt.x = px + t*rx;
            intersectionPt.y = py + t*ry;
        }
        if(Double.compare(intersectionPt.x,-1.0) == 0) return null;
        return intersectionPt;
    }

    private double micLineCrossProduct(MicLine A, MicLine B)
    {
        double ax = A.second.x - A.first.x;
        double ay = A.second.y - A.first.y;

        double bx = B.second.x - B.first.x;
        double by = B.second.y - B.first.y;

        return ax * by  - ay * bx;
    }

    private double micLineDotProduct(MicLine A, MicLine B)
    {
        double ax = A.second.x - A.first.x;
        double ay = A.second.y - A.first.y;

        double bx = B.second.x - B.first.x;
        double by = B.second.y - B.first.y;

        return ax * bx + ay * by;
    }


    //FindClosestVertex: first argument is the ship for which all 4 vertices will be considered
    //second argument is the ship for which you get a vertex
    private Point2D.Double findClosestVertex(BumpableWithShape shipWithVertex, BumpableWithShape target) {
        ArrayList<Point2D.Double> vertices = shipWithVertex.getVertices();
        double min = Double.MAX_VALUE;
        Point2D.Double centerTarget = new Point2D.Double(target.bumpable.getPosition().getX(), target.bumpable.getPosition().getY());
        Point2D.Double closestVertex = null;
        for(Point2D.Double vertex : vertices) {
            double dist = nonSRPyth(vertex, centerTarget);
            if(min > dist) {
                min = dist;
                closestVertex = vertex;
            }
        }
        return closestVertex;
    }

    //FindClosestVertex: first argument is the ship for which all 4 vertices will be considered
    //second argument is the ship for which you get a vertex
    private Point2D.Double find2ndClosestVertex(BumpableWithShape shipWithVertex, BumpableWithShape target) {
        ArrayList<Point2D.Double> vertices = shipWithVertex.getVertices();
        double min = Double.MAX_VALUE;
        Point2D.Double centerTarget = new Point2D.Double(target.bumpable.getPosition().getX(), target.bumpable.getPosition().getY());
        Point2D.Double closestVertex = null;
        for(Point2D.Double vertex : vertices) {
            double dist = nonSRPyth(vertex, centerTarget);
            if(min > dist) {
                min = dist;
                closestVertex = vertex;
            }
        }
        Point2D.Double secondVertex = null;
        double min2 = Double.MAX_VALUE;
        for(Point2D.Double vertex : vertices) {
            if(vertex == closestVertex) continue;
            double dist = nonSRPyth(vertex, centerTarget);
            if(min2 > dist) {
                min2 = dist;
                secondVertex = vertex;
            }
        }
        return secondVertex;
    }

    //FindClosestVertex: first argument is the ship for which all 4 vertices will be considered
    //second argument is the ship for which you get a vertex
    private Point2D.Double find3rdClosestVertex(BumpableWithShape shipWithVertex, BumpableWithShape target) {
        ArrayList<Point2D.Double> vertices = shipWithVertex.getVertices();
        double min = Double.MAX_VALUE;
        Point2D.Double centerTarget = new Point2D.Double(target.bumpable.getPosition().getX(), target.bumpable.getPosition().getY());
        Point2D.Double closestVertex = null;
        for(Point2D.Double vertex : vertices) {
            double dist = nonSRPyth(vertex, centerTarget);
            if(min > dist) {
                min = dist;
                closestVertex = vertex;
            }
        }
        Point2D.Double secondVertex = null;
        double min2 = Double.MAX_VALUE;
        for(Point2D.Double vertex : vertices) {
            if(vertex == closestVertex) continue;
            double dist = nonSRPyth(vertex, centerTarget);
            if(min2 > dist) {
                min2 = dist;
                secondVertex = vertex;
            }
        }
        Point2D.Double thirdVertex = null;
        double min3 = Double.MAX_VALUE;
        for(Point2D.Double vertex : vertices) {
            if(vertex == secondVertex || vertex == closestVertex) continue;
            double dist = nonSRPyth(vertex, centerTarget);
            if(min3 > dist) {
                min3 = dist;
                thirdVertex = vertex;
            }
        }
        return thirdVertex;
    }

    private boolean isTargetOverlappingAttacker(BumpableWithShape b) {
        return false;
    }

    private Shape getRawShape(Decorator bumpable) {
        return Decorator.getDecorator(Decorator.getOutermost(bumpable), NonRectangular.class).getShape();
    }

    private boolean are90degreesAligned(BumpableWithShape thisShip, BumpableWithShape b) {
        int shipAngle = Math.abs((int)thisShip.getAngle());
        int bAngle =  Math.abs((int)b.getAngle());

        while(shipAngle > 89) shipAngle -= 90;
        while(bAngle > 89) bAngle -= 90;
        if(shipAngle == bAngle) return true;
        else return false;
    }


    private Boolean isTargetOutsideofRectangles(BumpableWithShape thisShip, BumpableWithShape targetBWS, boolean wantBoost) {
        Shape crossZone = findUnionOfRectangularExtensions(thisShip, wantBoost);
        return !shapesOverlap(crossZone, targetBWS.shape);
    }

    public  java.util.List<BumpableWithShape> getObstructionsOnMap() {
        List<BumpableWithShape> bumpables = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_an_asteroid")) {
                // comment out this line and the next three that add to bumpables if bumps other than with ships shouldn't be detected yet
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece, "Asteroid", "2".equals(testFlipString)));
            } else if (piece.getState().contains("this_is_a_debris")) {
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece,"Debris","2".equals(testFlipString)));
            }
        }
        return bumpables;
    }

    private Shape findInBetweenRectangle(BumpableWithShape atk, BumpableWithShape def, double wantedWidth, int chosenOption) {
        //whichAtkRect: 1 = front; 2 = left; 3 = right; 4 = back; 5 = all
        double chassisHeight = atk.getChassisHeight();
        double chassisWidth = atk.getChassisWidth();


        double centerX = atk.bumpable.getPosition().getX();
        double centerY = atk.bumpable.getPosition().getY();
        Shape testShape = null;
        if(chosenOption == frontArcOption) { //front only
            testShape = new Rectangle2D.Double(-wantedWidth/2.0, -RANGE3 - chassisHeight/2.0, wantedWidth, RANGE3);
        }
        if(chosenOption == backArcOption) { //back only
            testShape = new Rectangle2D.Double(-wantedWidth/2.0, chassisHeight/2.0, wantedWidth, RANGE3);
        }
        if(chosenOption == turretArcOption) { //all 4
            Shape front = new Rectangle2D.Double(-wantedWidth/2.0, -RANGE3 - chassisHeight/2.0, wantedWidth, RANGE3);
            Shape back = new Rectangle2D.Double(-wantedWidth/2.0, chassisHeight/2.0, wantedWidth, RANGE3);
            Shape left = new Rectangle2D.Double(-chassisWidth/2.0 - RANGE3, -chassisHeight/2.0, RANGE3, chassisHeight);
            Shape right = new Rectangle2D.Double(chassisWidth/2.0, -chassisHeight/2.0, RANGE3, chassisHeight);

            ArrayList<Shape> listShape = new ArrayList<Shape>();
            listShape.add(front);
            listShape.add(back);
            listShape.add(left);
            listShape.add(right);

            for(Shape s : listShape){
                Shape transformed = transformRectShapeForBestLines(atk, def, s, centerX, centerY);
                if(shapesOverlap(transformed, def.shape)) return transformed;
            }
        }
        if(chosenOption == frontAuxArcOption) { //auzituck YV-666

            Shape front = new Rectangle2D.Double(-wantedWidth/2.0, -RANGE3 - chassisHeight/2.0, wantedWidth, RANGE3);
            Shape left = new Rectangle2D.Double(-chassisWidth/2.0 - RANGE3, -chassisHeight/2.0, RANGE3, chassisHeight/2.0);
            Shape right = new Rectangle2D.Double(chassisWidth/2.0, -chassisHeight/2.0, RANGE3, chassisHeight/2.0);

            ArrayList<Shape> listShape = new ArrayList<Shape>();
            listShape.add(front);
            listShape.add(left);
            listShape.add(right);

            for(Shape s : listShape){
                Shape transformed = transformRectShapeForBestLines(atk, def, s, centerX, centerY);
                if(shapesOverlap(transformed, def.shape)) return transformed;
            }
        }
        //common to all options except turret/TL shots
        Shape tShape = transformRectShapeForBestLines(atk, def, testShape, centerX, centerY);
        if(shapesOverlap(tShape, def.shape)) return tShape;
        return null;
    }

    private Shape transformRectShapeForBestLines(BumpableWithShape atk, BumpableWithShape def, Shape toTransform, double x, double y) {
        Shape transformed = AffineTransform
                .getTranslateInstance(x, y)
                .createTransformedShape(toTransform);

        transformed = AffineTransform
                .getRotateInstance(atk.getAngleInRadians(), x, y)
                .createTransformedShape(transformed);
        return transformed;
    }


    private double getExtraAngleDuringRectDetection(BumpableWithShape atk, BumpableWithShape def){
        double workingWidth = atk.getChassisWidth();
        Shape front = new Rectangle2D.Double(-workingWidth/2.0, -RANGE3 - workingWidth/2.0, workingWidth, RANGE3);
        Shape back = new Rectangle2D.Double(-workingWidth/2.0, workingWidth/2.0, workingWidth, RANGE3);

        Shape left = new Rectangle2D.Double(-workingWidth/2.0 - RANGE3, -workingWidth/2.0, RANGE3, workingWidth);
        Shape right = new Rectangle2D.Double(workingWidth/2.0, -workingWidth/2.0, RANGE3, workingWidth);

        ArrayList<Shape> listShape = new ArrayList<Shape>();
        listShape.add(front);
        listShape.add(right);
        listShape.add(back);
        listShape.add(left);

        double centerX = atk.bumpable.getPosition().getX();
        double centerY = atk.bumpable.getPosition().getY();
        double extra = 90.0f;
        for(Shape s : listShape){

            Shape transformed = AffineTransform
                    .getTranslateInstance(centerX, centerY)
                    .createTransformedShape(s);

            transformed = AffineTransform
                    .getRotateInstance(atk.getAngleInRadians(), centerX, centerY)
                    .createTransformedShape(transformed);
            if(shapesOverlap(transformed, def.shape)) {
                return Math.PI*extra/180.0;
            }
            extra += 90.0;
        }
        return 0.0;
    }
    private Shape findUnionOfRectangularExtensions(BumpableWithShape b, boolean superLong) {
        Shape rawShape = BumpableWithShape.getRawShape(b.bumpable);
        double chassisWidth = b.getChassisWidth();
        double workingWidth = b.getChassisWidth();
        if(whichOption !=2) workingWidth = b.getChassisWidth() - b.getChassis().getCornerToFiringArc()*2.0;
        double workingHeight = b.getChassisHeight();
        double boost = 1.0f;
        if(superLong) boost = 30.0f;
        Shape frontBack = new Rectangle2D.Double(-workingWidth/2.0, -boost*RANGE3 - workingHeight/2.0, workingWidth, 2.0*RANGE3*boost + workingHeight);
        Shape front = new Rectangle2D.Double(-workingWidth/2.0, -boost*RANGE3 - workingHeight/2.0, workingWidth, RANGE3*boost);

        Shape leftRight = new Rectangle2D.Double(-chassisWidth/2.0 - boost*RANGE3, -workingHeight/2.0, 2.0*boost*RANGE3+chassisWidth, workingHeight);

        //half height reduced side rectangles for Auzituck/YV-666 arcs
        Shape leftRight_reduced = new Rectangle2D.Double(-chassisWidth/2.0 - boost*RANGE3, -workingHeight/2.0, 2.0*boost*RANGE3+chassisWidth, workingHeight/2.0);

        //by default, get everything for turret/TL shots
        Area zone = new Area(frontBack);
        zone.add(new Area(leftRight));

        if(whichOption == frontArcOption) zone = new Area(front);
        if(whichOption == frontAuxArcOption) zone = new Area(leftRight_reduced);

        zone.exclusiveOr(new Area(rawShape));


        double centerX = b.bumpable.getPosition().getX();
        double centerY = b.bumpable.getPosition().getY();

        Shape transformed = AffineTransform
                .getTranslateInstance(centerX, centerY)
                .createTransformedShape(zone);

        transformed = AffineTransform
                .getRotateInstance(b.getAngleInRadians(), centerX, centerY)
                .createTransformedShape(transformed);

        //fov.add(transformed);
        return transformed;
    }



    public void draw(Graphics graphics, int i, int i1, Component component, double v) {
        this.piece.draw(graphics, i, i1, component, v);
    }

    public Rectangle boundingBox() {
        return this.piece.boundingBox();
    }

    public Shape getShape() {
        return this.piece.getShape();
    }

    public String getName() {
        return this.piece.getName();
    }

    @Override
    public String myGetType() {
        return ID;
    }

    public String getDescription() {
        return "Custom auto-range finder (mic.AutoRangeFinder)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    private List<BumpableWithShape> getOtherShipsOnMap() {
        List<BumpableWithShape> ships = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_a_ship") && piece.getId() != this.piece.getId()) {
                ships.add(new BumpableWithShape((Decorator)piece, "Ship",
                        piece.getProperty("Pilot Name").toString(), piece.getProperty("Craft ID #").toString()));
            }
        }
        return ships;
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        if(DEBUGMODE == false) return;
        Point p = e.getPoint();
            anchor = p;
            anchorLocation = map.localizedLocationName(anchor);
            lastLocation = anchorLocation;
        }


    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {
        if(DEBUGMODE == false) return;
        if(isThisTheOne == false) return;

        Point p = e.getPoint();
        map.scrollAtEdge(p, 15);

        Point mapAnchor = lastAnchor;

        int fudge = this.piece.getShape().getBounds().width * 12;

        this.piece.setPosition(p);
        Rectangle r = new Rectangle(this.piece.getPosition().x-fudge,
                this.piece.getPosition().y-fudge,
                this.piece.getShape().getBounds().width+fudge*2,
                this.piece.getShape().getBounds().height+fudge*2);
        map.repaint(r);
    }

    public void mouseMoved(MouseEvent e) {

    }

    private static class FOVisualizationClear extends Command {

        private final String id;

        public FOVisualizationClear(String id) {
            this.id = id;
        }

        protected void executeCommand() {
            final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");
            FOVisualization component = AutorangeVisualizationEncoder.INSTANCE.getVisualizations().get(this.id);
            if (component != null) {
                map.removeDrawComponent(component);
                AutorangeVisualizationEncoder.INSTANCE.getVisualizations().remove(this.id);
            }
        }

        protected Command myUndoCommand() {
            return null;
        }
    }

    public static class FOVisualizationClearEncoder implements CommandEncoder {
        private static final String prefix = "FoVisClearId=";
        private static final Logger logger = LoggerFactory.getLogger(FOVisualizationClearEncoder.class);

        public Command decode(String command) {
            if (command == null || !command.contains(prefix)) {
                return null;
            }
            String id = command.substring(prefix.length());
            logger.info("Decoded clear visualization with id = {}", id);
            return new FOVisualizationClear(id);
        }

        public String encode(Command c) {
            if(!(c instanceof FOVisualizationClear)) {
                return null;
            }
            FOVisualizationClear visClear = (FOVisualizationClear) c;
            logger.info("Encoded clear visualization with id = {}", visClear.id);
            return prefix + visClear.id;
        }
    }

    private static class FOVisualization extends Command implements Drawable {

        private final List<Shape> shapes;
        private final List<ShapeWithText> shapesWithText;
        private final List<MicLine> lines;
        private final String id;

        public Color badLineColor = new Color(0, 121,255,110);
        public Color bestLineColor = new Color(0, 180, 200,255);
        public Color shipsObstaclesColor = new Color(255,99,71, 150);
        public Color arcLineColor = new Color(246, 255, 41,180);

        FOVisualization(String id) {
            this.id = id;
            this.shapes = new ArrayList<Shape>();
            this.lines = new ArrayList<MicLine>();
            this.shapesWithText = new ArrayList<ShapeWithText>();
        }

        FOVisualization() {
            this(UUID.randomUUID().toString());
        }

        public String getId() {
            return this.id;
        }

        public void add(Shape bumpable) {
            this.shapes.add(bumpable);
        }
        public void addLine(MicLine line){
            this.lines.add(line);
        }
        public void addShapeWithText(ShapeWithText swt){ this.shapesWithText.add(swt); }
        public int getCount() {
            return lines.size() + shapesWithText.size();
        }


        protected void executeCommand() {
            final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");
            int count = getCount();
            if (count > 0) {
                map.addDrawComponent(this);
            }
            else {
                map.removeDrawComponent(FOVisualization.this);
            }
        }

        protected Command myUndoCommand() {
            return null;
        }

        public void draw(Graphics graphics, VASSAL.build.module.Map map) {
            Graphics2D graphics2D = (Graphics2D) graphics;

            double scale = map.getZoom();
            AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);

            graphics2D.setColor(shipsObstaclesColor);
            for (Shape shape : shapes) {
                graphics2D.fill(scaler.createTransformedShape(shape));

            }

            for(ShapeWithText SWT : shapesWithText){
                graphics2D.setColor(badLineColor);
                graphics2D.fill(scaler.createTransformedShape(SWT.shape));
                graphics2D.setFont(new Font("Arial",0,42));
                graphics2D.setColor(bestLineColor);
                Shape textShape = getTextShape(graphics2D, SWT.rangeString, graphics2D.getFont(), true);
                textShape = AffineTransform.getTranslateInstance(SWT.x, SWT.y)
                        .createTransformedShape(textShape);
                textShape = AffineTransform.getScaleInstance(scale, scale)
                        .createTransformedShape(textShape);
                graphics2D.draw(textShape);
            }

            int colorNb = 100;
            for(MicLine line : lines){
                if(line.isBestLine == true) graphics2D.setColor(bestLineColor);
                else graphics2D.setColor(badLineColor);

                if(line.isArcLine == true) graphics2D.setColor(arcLineColor);

                /*ALLLines COlor Hijack*/
                if(line.markedAsDead == true) graphics2D.setColor(new Color(255,0,0,255));
                else {
                    Color gradiant = new Color(colorNb, colorNb, 255, 255);
                    colorNb += 20;
                    graphics2D.setColor(gradiant);
                }
                if(line.isBestLine == true && line.markedAsDead == false) graphics2D.setColor(new Color(200, 18, 194,255));
                  /*end*/


                Line2D.Double lineShape = new Line2D.Double(line.first, line.second);
                graphics2D.draw(scaler.createTransformedShape(lineShape));

                //create a shape in the form of the string that's needed
                graphics2D.setFont(new Font("Arial",0,42));
                Shape textShape = getTextShape(graphics2D, line.rangeString, graphics2D.getFont(), true);

                //transform the textShape into place, near the center of the line
                textShape = AffineTransform.getTranslateInstance(line.centerX, line.centerY)
                        .createTransformedShape(textShape);
                textShape = AffineTransform.getScaleInstance(scale, scale)
                        .createTransformedShape(textShape);
                graphics2D.draw(textShape);

            }
        }

        public static Shape getTextShape(Graphics2D g2d, String text, Font font, boolean ltr) {
            AttributedString attstring = new AttributedString(text);
            attstring.addAttribute(TextAttribute.FONT, font);
            attstring.addAttribute(TextAttribute.RUN_DIRECTION, ltr ? TextAttribute.RUN_DIRECTION_LTR : TextAttribute.RUN_DIRECTION_RTL);
            FontRenderContext frc = g2d.getFontRenderContext();
            TextLayout t = new TextLayout(attstring.getIterator(), frc);
            return t.getOutline(null);
        }

        public List<Shape> getShapes() {
            return this.shapes;
        }


        public List<ShapeWithText> getTextShapes() {
            return this.shapesWithText;
        }

        public List<MicLine> getMicLines() {
            return this.lines;
        }

        public boolean drawAboveCounters() {
            return true;
        }
    }

    public static class AutorangeVisualizationEncoder implements CommandEncoder {
        private static final Logger logger = LoggerFactory.getLogger(AutoRangeFinder.class);
        private static final String commandPrefix = "AutorangeVisualizationEncoder=";
        private static final String nullPart = "nullPart";
        private static final String partDelim = "!";
        private static final String itemDelim = "\t";

        public static AutorangeVisualizationEncoder INSTANCE = new AutorangeVisualizationEncoder();

        private Map<String, FOVisualization> visualizationsById = Maps.newHashMap();

        public Map<String, FOVisualization> getVisualizations() {
            return this.visualizationsById;
        }

        public Command decode(String command) {
            if (command == null || !command.contains(commandPrefix)) {
                return null;
            }

            logger.info("Decoding AutorangeVisualization");

            command = command.substring(commandPrefix.length());

            try {
                String[] parts = command.split(partDelim);
                if (parts.length != 3) {
                    throw new IllegalStateException("Invalid command format " + command);
                }
                FOVisualization visualization = new FOVisualization(parts[0]);

                String[] encodedLines = parts[1].equals(nullPart) ? new String[0] : parts[1].split(itemDelim);
                logger.info("Decoding {} lines", encodedLines.length);
                for (String base64Line : encodedLines) {
                    MicLine line = (MicLine) deserializeBase64Obj(base64Line);
                    visualization.addLine(line);
                }

                String[] encodedSwt = parts[2].equals(nullPart) ? new String[0] : parts[2].split(itemDelim);
                logger.info("Decoding {} shapesWithText", encodedLines.length);
                for (String base64Shape : encodedSwt) {
                    ShapeWithText swt = (ShapeWithText) deserializeBase64Obj(base64Shape);
                    visualization.addShapeWithText(swt);
                }

                this.visualizationsById.put(visualization.getId(), visualization);

                logger.info("Decoded AutorangeVisualization with {} shapes", visualization.getShapes().size());
                return visualization;
            } catch (Exception e) {
                logger.error("Error decoding AutorangeVisualization", e);
                return null;
            }
        }

        public String encode(Command c) {
            if (!(c instanceof FOVisualization)) {
                return null;
            }
            logger.info("Encoding autorange visualization");
            FOVisualization visualization = (FOVisualization) c;
            try {
                List<String> lines = Lists.newArrayList();
                logger.info("Encoding {} lines", visualization.getMicLines().size());
                for (MicLine line : visualization.getMicLines()) {
                    lines.add(serializeToBase64(line));
                }
                List<String> shapesWithText = Lists.newArrayList();
                logger.info("Encoding {} shapesWithText", visualization.getTextShapes().size());
                for(ShapeWithText swt : visualization.getTextShapes()) {
                    shapesWithText.add(serializeToBase64(swt));
                }
                String linesPart = lines.size() > 0 ? Joiner.on(itemDelim).join(lines) : null;
                String swtPart = shapesWithText.size() > 0 ? Joiner.on(itemDelim).join(shapesWithText) : null;
                return commandPrefix + Joiner.on(partDelim).useForNull(nullPart).join(visualization.getId(), linesPart, swtPart);
            } catch (Exception e) {
                logger.error("Error encoding autorange visualization", e);
                return null;
            }
        }
    }

    public static class ShapeWithText implements Serializable {
        public String rangeString = "Range ";
        public int rangeLength = 0;
        public Shape shape;
        public int x, y;
        //angle should be the calling detection angle for the multiple best line case, but add an extra angle to account for
        //if it's the right, left or bottom band required.
        public double angle = 0.0;

        ShapeWithText(Shape shape, double angle) {
            this.shape = shape;
            this.angle = angle;
            rangeLength = (int)Math.ceil(((double)calculateRange()/282.5));
            rangeString += Integer.toString(rangeLength);
            this.x = shape.getBounds().x;
            this.y = shape.getBounds().y;
        }

        private int calculateRange() {
            Shape temp = this.shape;
            double centerX = shape.getBounds().getCenterX();
            double centerY = shape.getBounds().getCenterY();

            //unrotate the shape so we can get the range through its width
            temp = AffineTransform
                    .getRotateInstance(-angle, centerX, centerY)
                    .createTransformedShape(temp);

            return (int)temp.getBounds().getWidth();
        }
    }
    public static class MicLine implements Serializable {
        public Boolean isBestLine = false;
        public Boolean isArcLine = false;
        public String rangeString = "Range ";
        public double pixelLength = 0.0f;
        public int rangeLength = 0;
        public int centerX, centerY;
        public Point2D.Double first, second;
        public Line2D.Double line = null;
        public Boolean markedAsDead = false;

        public MicLine() { /* To allow serizliation */ }

        //default constructor, lets the range be converted into a string
        public MicLine(Point2D.Double first, Point2D.Double second, Boolean markAsDead) {
            this.first = first;
            this.second = second;
            doRest("", 0.5);
            markedAsDead = markAsDead;
        }

        //for when you want to set a manual string and an arbitrary string position
        public MicLine(Point2D.Double first, Point2D.Double second, Boolean markAsDead, String label, double percentage) {
            this.first = first;
            this.second = second;
            doRest(label, percentage);
            markedAsDead = markAsDead;
        }

        void doRest(String label, double percentage) {
            pixelLength = Math.sqrt(nonSRPyth(first, second));
            rangeLength = (int) Math.ceil(pixelLength / 282.5);
            if("".equals(label)) rangeString += Integer.toString(rangeLength);
            else rangeString = label;
            line = new Line2D.Double(first, second);
            calculateCenter(percentage);
        }

        //get a % of the length
        void calculateCenter(double percentage){
            centerX = (int)(this.first.getX() + percentage*(this.second.getX()-this.first.getX()));
            centerY = (int)(this.first.getY() + percentage*(this.second.getY()-this.first.getY()));
        }
    }

    public static class RangeFindings {
        public int range=0;
        public String fullName="";
        public boolean isObstructed=false;

        public RangeFindings(){ /* To allow serizliation */ }
        public RangeFindings(int range, String fullName){
            this.fullName = fullName;
            this.range = range;
            this.isObstructed = false;
        }
    }
    private static class ShipPositionState {
        double x;
        double y;
        double angle;
    }
}
