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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

import static mic.Util.*;

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

    private static final Boolean DEBUGMODE = false;
    private static final Boolean MULTILINES = true;

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

        ArrayList<rangeFindings> rfindings = new ArrayList<rangeFindings>(); //findings compiled here

        String hotKey = HotKeyConfigurer.getString(stroke);

        // check to see if the this code needs to respond to the event

        //identify which autorange option was used by using the static Map defined above in the globals, store it in an int
        whichOption = getKeystrokeToOptions(stroke);
        if (whichOption != -1 && stroke.isOnKeyRelease() == false) {

            isThisTheOne = true;
            if(DEBUGMODE == true) {
                FluidAnim FA = new FluidAnim(this, whichOption);
                logToChat("whichOption = " + Integer.toString(whichOption));
                FA.run();
            }

            Command bigCommand = piece.keyEvent(stroke);
            //if the firing options were already activated, remove the visuals and exit right away
            if (this.fov != null && this.fov.getCount() > 0) {
                //logToChatCommand("toggle off");
                clearVisu();
                bigCommand.append(this.fov);
                this.fov.execute();
                return bigCommand;
            }
            thisShip = new BumpableWithShape(this, "Ship",
                    this.getInner().getProperty("Pilot Name").toString(), this.getInner().getProperty("Craft ID #").toString());

            thisShip.refreshSpecialPoints();
            findAttackerBestPoints(thisShip);

            //Prepare the start of the appropriate chat announcement string - which ship are we doing this from, which kind of autorange
            prepAnnouncementStart();

            //Loop over every other target ship
            List<BumpableWithShape> BWS = getOtherShipsOnMap();
            for (BumpableWithShape b : BWS) {
                //Preliminary check, eliminate attempt to calculate this if the target is overlapping the attacker, could lead to exception error
                if (shapesOverlap(thisShip.shape, b.shape)) continue;

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
            return bigCommand;
        } else if (KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK, false).equals(stroke)) {
            if (this.fov != null && this.fov.getCount() > 0) {
                clearVisu();
                Command goToHell = piece.keyEvent(stroke);
                goToHell.append(this.fov);
                this.fov.execute();
                return goToHell;
            }
        }

        return piece.keyEvent(stroke);
    }

public void justRunLines(int savedOption){
whichOption = savedOption;
        ArrayList<rangeFindings> rfindings = new ArrayList<rangeFindings>();
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
        findAttackerBestPoints(thisShip);
        //Prepare the start of the appropriate chat announcement string - which ship are we doing this from, which kind of autorange
        prepAnnouncementStart();

        //Loop over every other target ship
        List<BumpableWithShape> BWS = getOtherShipsOnMap();
        for (BumpableWithShape b : BWS) {
            //Preliminary check, eliminate attempt to calculate this if the target is overlapping the attacker, could lead to exception error
            if (shapesOverlap(thisShip.shape, b.shape)) continue;

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

    private void clearVisu() {
        getMap().removeDrawComponent(this.fov);
        this.fov.shapes.clear();
        this.fov.lines.clear();
        this.fov.shapesWithText.clear();
    }

    private void figureOutAutoRange(BumpableWithShape b, ArrayList<rangeFindings> rfindings) {
        Point2D.Double D1 = findClosestVertex(b, thisShip);
        Point2D.Double D2 = find2ndClosestVertex(b, thisShip);
        Point2D.Double D3 = find3rdClosestVertex(b, thisShip);

        micLine bestLine = null;
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
        rangeFindings found = new rangeFindings(bestLine.rangeLength, bShipName);


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
            } else { //multiple lines case
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
                ShapeWithText bestBand = new ShapeWithText(a1, thisShip.getAngleInRadians() + extra);
                rfindings.add(found);
                fov.addShapeWithText(bestBand);
            }

    }

    private micLine findBestLineInMobileArc(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {
    micLine best = null;
    return best;

    }

    private micLine findBestLineInFrontAuxArcs(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {
        micLine best = null;
        return best;
    }

    private micLine findBestLineInSimpleArcs(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {
        ArrayList<micLine> lineList = new ArrayList<micLine>();

        //Closest Attacker Edge
        micLine AA = new micLine(A1, A2, false);
        //Closest Defender Edge
        micLine DD = new micLine(D1, D2, false);
        //2nd closest defender edge
        micLine DD_2nd = new micLine(D1, D3, false);


        //Closest Attacker to Closest Defender
        micLine A1D1 = new micLine(A1, D1, false);
        if(A1D1 != null) {
            if(DEBUGMODE == false){
                lineList.add(A1D1);
            }
            else{
                micLine A1D1copy = new micLine(A1D1.first, A1D1.second, A1D1.markedAsDead, "A1D1", 0.1);
                lineList.add(A1D1copy);
            }
        }
        //Closest Defender to 2nd Closest Attacker
        micLine A2D1 = new micLine(A2, D1, false);
        if(A2D1 != null) {
            if (DEBUGMODE == false) {
                lineList.add(A2D1);
            } else {
                micLine A2D1copy = new micLine(A2D1.first, A2D1.second, A2D1.markedAsDead, "A2D1", 0.5);
                lineList.add(A2D1copy);
            }
        }

        //Closest attacker's point to the defender's closest edge
        micLine A1DD = createLinePtoAB(A1, DD, true);
        if(A1DD != null) {
            if (DEBUGMODE == false) {
                lineList.add(A1DD);
            } else {
                micLine A1DDcopy = new micLine(A1DD.first, A1DD.second, A1DD.markedAsDead, "A1DD", 0.2);
                lineList.add(A1DDcopy);
            }
        }

        micLine A2DD = createLinePtoAB(A2, DD, true);
        if(A2DD != null){
            if (DEBUGMODE == false) {
                lineList.add(A2DD);
            } else {
                micLine A2DDcopy = new micLine(A2DD.first, A2DD.second, A2DD.markedAsDead, "A2DD", 0.4);
                lineList.add(A2DDcopy);
            }
        }

            //Closest attacker's point to the defender's closest edge along the arc edge
            micLine A1DD_arc_restricted = createLineAxtoDD_along_arc_edge(A1, E1, DD);
            if(A1DD_arc_restricted != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A1DD_arc_restricted);
                } else {
                    micLine A1DD_arc_restrictedcopy = new micLine(A1DD_arc_restricted.first, A1DD_arc_restricted.second, A1DD_arc_restricted.markedAsDead, "A1DD_ea", 0.5);
                    lineList.add(A1DD_arc_restrictedcopy);
                }
            }

            micLine A2DD_arc_restricted = createLineAxtoDD_along_arc_edge(A2, E2, DD);
            if(A2DD_arc_restricted != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A2DD_arc_restricted);
                } else {
                    micLine A2DD_arc_restrictedcopy = new micLine(A2DD_arc_restricted.first, A2DD_arc_restricted.second, A2DD_arc_restricted.markedAsDead, "A2DD_ea", 0.7);
                    lineList.add(A2DD_arc_restrictedcopy);
                }
            }

            //Closest attacker's point to the defender's 2nd closest edge along the arc edge
            micLine A1DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A1, E1, DD_2nd);
            if(A1DD_arc_restricted_2nd != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A1DD_arc_restricted_2nd);
                } else {
                    micLine A1DD_arc_restricted_2ndcopy = new micLine(A1DD_arc_restricted_2nd.first, A1DD_arc_restricted_2nd.second, A1DD_arc_restricted_2nd.markedAsDead, "A1DD_2nd_ea", 0.1);
                    lineList.add(A1DD_arc_restricted_2ndcopy);
                }
            }

            micLine A2DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A2, E2, DD_2nd);
            if(A2DD_arc_restricted_2nd != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A2DD_arc_restricted_2nd);
                } else {
                    micLine A2DD_arc_restricted_2ndcopy = new micLine(A2DD_arc_restricted_2nd.first, A2DD_arc_restricted_2nd.second, A2DD_arc_restricted_2nd.markedAsDead, "A2DD_2nd_ea", 0.9);
                    lineList.add(A2DD_arc_restricted_2ndcopy);
                }
            }

        //Attacker's edge to defender's closest vertex
        micLine AAD1 = createLinePtoAB(D1, AA, false);
        if(doesAAforInArcPassTest(AAD1, AA)== true)
            if(isRangeOk(AAD1, 1, rangeInt)) {
                micLine AAD1copy = new micLine(AAD1.first, AAD1.second, AAD1.markedAsDead, "AAD1", 0.8);
                lineList.add(AAD1copy);
            }
        ArrayList<micLine> filteredList = new ArrayList<micLine>();
        //Filter out shots that aren't in-arc if the turret option is not chosen
       /* for(micLine l: lineList)
        {
            if(isEdgeInArc(l) == true) filteredList.add(l);
            else l.markedAsDead = true;
        }
*/

        //ALLLINES: if all lines have to been added to the visuals, then, uncomment this section
        if(MULTILINES == true){
            for(micLine everyline : lineList) {
                fov.addLine(everyline);
            }
        }
        //end of section


        //First criterium, find the best distance and make it the best Line
        double bestDist = rangeInt * 282.5;
        micLine best = null;
        for (micLine l : filteredList) {
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

            A1 = b.tPts.get(0);
            A2 = b.tPts.get(1);

            E1 = b.tPts.get(2); //associated with A1 as the edge A1E1
            E2 = b.tPts.get(3); //associated with A2 as the edge A2E2
        }
        else if(whichOption == backArcOption){
            A1 = b.tPts.get(4);
            A2 = b.tPts.get(5);

            E1 = b.tPts.get(6);
            E2 = b.tPts.get(7);
        }
        else if(whichOption == frontAuxArcOption){
            A1 = b.tPts.get(8);
            A2 = b.tPts.get(0);

            E1 = b.tPts.get(10);
            E2 = b.tPts.get(2);

            A3 = b.tPts.get(1);
            A4 = b.tPts.get(9);

            E3 = b.tPts.get(3);
            E4 = b.tPts.get(11);
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
                A1 = b.tPts.get(5);
                A2 = b.tPts.get(0);

                E1 = b.tPts.get(7);
                E2 = b.tPts.get(2);
            }
            else if(sideCheck.equals("2")){
                //right side check
                A1 = b.tPts.get(1);
                A2 = b.tPts.get(4);

                E1 = b.tPts.get(3);
                E2 = b.tPts.get(6);
            }
            else if(sideCheck.equals("1")){
                //do like front arc
                A1 = b.tPts.get(0);
                A2 = b.tPts.get(1);

                E1 = b.tPts.get(2); //associated with A1 as the edge A1E1
                E2 = b.tPts.get(3); //associated with A2 as the edge A2E2
            }
            else if(sideCheck.equals("3")){
                //do like back aux
                A1 = b.tPts.get(4);
                A2 = b.tPts.get(5);

                E1 = b.tPts.get(6);
                E2 = b.tPts.get(7);
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

    private Command makeBigAnnounceCommand(String bigAnnounce, ArrayList<rangeFindings> rfindings) {
        String range1String = "";
        String range2String = "";
        String range3String = "";

        boolean hasR1 = false;
        boolean hasR2 = false;
        boolean hasR3 = false;

        for (rangeFindings rf : rfindings) {
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


    private Boolean isRangeOk(micLine theLine, int rangeMin, int rangeMax)
    {
        if(theLine == null) return false;
        double minPixelDist = rangeMin * 282.5 - 282.5;
        double maxPixelDist = rangeMax * 282.5;

        if(Double.compare(theLine.pixelLength, minPixelDist) >= 0 && Double.compare(theLine.pixelLength, maxPixelDist) <=0) return true;
        return false;
    }



    //Checks if the AAD1 line, from closest defender vertex to a point somewhere inside the arc, on the attacker's edge, is actually between the arc points instead of simply outside but still on that extended line
    private boolean doesAAforInArcPassTest(micLine AAD1, micLine AA) {
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

    private micLine findBestLine(Point2D.Double D1, Point2D.Double D2, int rangeInt) {
        ArrayList<micLine> lineList = new ArrayList<micLine>();

        //Closest Attacker Edge
        micLine AA = new micLine(A1, A2, false);
        //Closest Defender Edge
        micLine DD = new micLine(D1, D2, false);

        //Closest Attacker to Closest Defender
        micLine A1D1 = new micLine(A1, D1, false);
        if(A1D1 != null) {
            if(DEBUGMODE == false){
                lineList.add(A1D1);
            }
            else{
                micLine A1D1copy = new micLine(A1D1.first, A1D1.second, A1D1.markedAsDead, "A1D1", 0.1);
                lineList.add(A1D1copy);
            }
        }

        //Closest Attacker to 2nd Closest Defender
        micLine A1D2 = new micLine(A1, D2, false);
        if(A1D2 != null) {
            if (DEBUGMODE == false) {
                lineList.add(A1D2);
            } else {
                micLine A1D2copy = new micLine(A1D2.first, A1D2.second, A1D2.markedAsDead, "A1D2", 0.3);
                lineList.add(A1D2copy);
            }
        }

        //Closest Defender to 2nd Closest Attacker
        micLine A2D1 = new micLine(A2, D1, false);
        if(A2D1 != null) {
            if (DEBUGMODE == false) {
                lineList.add(A2D1);
            } else {
                micLine A2D1copy = new micLine(A2D1.first, A2D1.second, A2D1.markedAsDead, "A2D1", 0.5);
                lineList.add(A2D1copy);
            }
        }

        micLine A1DD = createLinePtoAB(A1, DD, true);
        if(A1DD != null) {
            if(DEBUGMODE == false) {
                lineList.add(A1DD);
            }
            else{
                micLine A1DDcopy = new micLine(A1DD.first, A1DD.second, A1DD.markedAsDead, "A1DD", 0.2);
                lineList.add(A1DDcopy);
            }
        }
        micLine A2DD = createLinePtoAB(A2, DD, true);
        if(A2DD != null) {
            if(DEBUGMODE == false) {
                lineList.add(A2DD);
            }
            else {
                micLine A2DDcopy = new micLine(A2DD.first, A2DD.second, A2DD.markedAsDead, "A2DD", 0.4);
                lineList.add(A2DDcopy);
            }
        }

        micLine D1AA = createLinePtoAB(D1, AA, false);
        if(D1AA != null){
            if(DEBUGMODE == false) {
                lineList.add(D1AA);
            }
            else {
                micLine D1AAcopy = new micLine(D1AA.first, D1AA.second, D1AA.markedAsDead, "D1AA", 0.6);
                lineList.add(D1AAcopy);
            }
        }

        //MULTILINES: if all lines have to been added to the visuals, then, uncomment this section
        if(MULTILINES == true){
            for(micLine everyline : lineList) {
                fov.addLine(everyline);
            }
        }
        //end of section

        //First criterium, find the best distance and make it the best Line
        double bestDist = rangeInt*282.5;
        micLine best = null;
        for(micLine l : lineList){
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

    private micLine findBestArcRestrictedLine(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {

        ArrayList<micLine> lineList = new ArrayList<micLine>();

        //Closest Attacker Edge
        micLine AA = new micLine(A1, A2, false);
        //Closest Defender Edge
        micLine DD = new micLine(D1, D2, false);
        //2nd closest defender edge
        micLine DD_2nd = new micLine(D1, D3, false);


        //Closest Attacker to Closest Defender
        micLine A1D1 = new micLine(A1, D1, false);
        if(A1D1 != null) {
            if(DEBUGMODE == false){
                lineList.add(A1D1);
            }
            else{
                micLine A1D1copy = new micLine(A1D1.first, A1D1.second, A1D1.markedAsDead, "A1D1", 0.1);
                lineList.add(A1D1copy);
            }
        }
        //Closest Defender to 2nd Closest Attacker
        micLine A2D1 = new micLine(A2, D1, false);
        if(A2D1 != null) {
            if (DEBUGMODE == false) {
                lineList.add(A2D1);
            } else {
                micLine A2D1copy = new micLine(A2D1.first, A2D1.second, A2D1.markedAsDead, "A2D1", 0.5);
                lineList.add(A2D1copy);
            }
        }

        //Closest attacker's point to the defender's closest edge
        micLine A1DD = createLinePtoAB(A1, DD, true);
        if(A1DD != null) {
            if (DEBUGMODE == false) {
                lineList.add(A1DD);
            } else {
                micLine A1DDcopy = new micLine(A1DD.first, A1DD.second, A1DD.markedAsDead, "A1DD", 0.2);
                lineList.add(A1DDcopy);
            }
        }

        micLine A2DD = createLinePtoAB(A2, DD, true);
        if(A2DD != null){
            if (DEBUGMODE == false) {
                lineList.add(A2DD);
            } else {
                micLine A2DDcopy = new micLine(A2DD.first, A2DD.second, A2DD.markedAsDead, "A2DD", 0.4);
                lineList.add(A2DDcopy);
            }
        }

        if(whichOption == frontAuxArcOption){
            micLine A3D1 = new micLine(A3, D1, false);
            micLine A4D1 = new micLine(A4, D1, false);

            if(A3D1 != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A3D1);
                } else {
                    micLine A3D1copy = new micLine(A3D1.first, A3D1.second, A3D1.markedAsDead, "A3D1", 0.5);
                    lineList.add(A3D1copy);
                }
            }
            if(A4D1 != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A4D1);
                } else {
                    micLine A4D1copy = new micLine(A4D1.first, A4D1.second, A4D1.markedAsDead, "A4D1", 0.5);
                    lineList.add(A4D1copy);
                }
            }
        }

        if(whichOption == frontArcOption || whichOption == backArcOption || whichOption == frontAuxArcOption) {
            //Closest attacker's point to the defender's closest edge along the arc edge
            micLine A1DD_arc_restricted = createLineAxtoDD_along_arc_edge(A1, E1, DD);
            if(A1DD_arc_restricted != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A1DD_arc_restricted);
                } else {
                    micLine A1DD_arc_restrictedcopy = new micLine(A1DD_arc_restricted.first, A1DD_arc_restricted.second, A1DD_arc_restricted.markedAsDead, "A1DD_ea", 0.5);
                    lineList.add(A1DD_arc_restrictedcopy);
                }
            }

            micLine A2DD_arc_restricted = createLineAxtoDD_along_arc_edge(A2, E2, DD);
            if(A2DD_arc_restricted != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A2DD_arc_restricted);
                } else {
                    micLine A2DD_arc_restrictedcopy = new micLine(A2DD_arc_restricted.first, A2DD_arc_restricted.second, A2DD_arc_restricted.markedAsDead, "A2DD_ea", 0.7);
                    lineList.add(A2DD_arc_restrictedcopy);
                }
            }

            //Closest attacker's point to the defender's 2nd closest edge along the arc edge
            micLine A1DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A1, E1, DD_2nd);
            if(A1DD_arc_restricted_2nd != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A1DD_arc_restricted_2nd);
                } else {
                    micLine A1DD_arc_restricted_2ndcopy = new micLine(A1DD_arc_restricted_2nd.first, A1DD_arc_restricted_2nd.second, A1DD_arc_restricted_2nd.markedAsDead, "A1DD_2nd_ea", 0.1);
                    lineList.add(A1DD_arc_restricted_2ndcopy);
                }
            }

            micLine A2DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A2, E2, DD_2nd);
            if(A2DD_arc_restricted_2nd != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A2DD_arc_restricted_2nd);
                } else {
                    micLine A2DD_arc_restricted_2ndcopy = new micLine(A2DD_arc_restricted_2nd.first, A2DD_arc_restricted_2nd.second, A2DD_arc_restricted_2nd.markedAsDead, "A2DD_2nd_ea", 0.9);
                    lineList.add(A2DD_arc_restricted_2ndcopy);
                }
            }
        }
        if(whichOption == frontAuxArcOption) {
            micLine A3DD_arc_restricted = createLineAxtoDD_along_arc_edge(A3, E3, DD);
            if(A3DD_arc_restricted != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A3DD_arc_restricted);
                } else {
                    micLine A3DD_arc_restricted_copy = new micLine(A3DD_arc_restricted.first, A3DD_arc_restricted.second, A3DD_arc_restricted.markedAsDead, "A3DD_ea", 0.6);
                    lineList.add(A3DD_arc_restricted_copy);
                }
            }

            micLine A4DD_arc_restricted = createLineAxtoDD_along_arc_edge(A4, E4, DD);
            if(A4DD_arc_restricted != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A4DD_arc_restricted);
                } else {
                    micLine A4DD_arc_restricted_copy = new micLine(A4DD_arc_restricted.first, A4DD_arc_restricted.second, A4DD_arc_restricted.markedAsDead, "A4DD_ea", 0.7);
                    lineList.add(A4DD_arc_restricted_copy);
                }
            }

            micLine A3DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A3, E3, DD_2nd);
            if(A3DD_arc_restricted_2nd != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A3DD_arc_restricted_2nd);
                } else {
                    micLine A3DD_arc_restricted_2nd_copy = new micLine(A3DD_arc_restricted_2nd.first, A3DD_arc_restricted_2nd.second, A3DD_arc_restricted_2nd.markedAsDead, "A3DD_2nd_ea", 0.8);
                    lineList.add(A3DD_arc_restricted_2nd_copy);
                }
            }

            micLine A4DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A4, E4, DD_2nd);
            if(A4DD_arc_restricted_2nd != null) {
                if (DEBUGMODE == false) {
                    lineList.add(A4DD_arc_restricted_2nd);
                } else {
                    micLine A4DD_arc_restricted_2nd_copy = new micLine(A4DD_arc_restricted_2nd.first, A4DD_arc_restricted_2nd.second, A4DD_arc_restricted_2nd.markedAsDead, "A4DD_2nd_ea", 0.9);
                    lineList.add(A4DD_arc_restricted_2nd_copy);
                }
            }

        }
        //Attacker's edge to defender's closest vertex

        micLine AAD1 = createLinePtoAB(D1, AA, false);
        if(doesAAforInArcPassTest(AAD1, AA)== true)
            if(isRangeOk(AAD1, 1, rangeInt)) {
                micLine AAD1copy = new micLine(AAD1.first, AAD1.second, AAD1.markedAsDead, "AAD1", 0.8);
                lineList.add(AAD1copy);
            }
        ArrayList<micLine> filteredList = new ArrayList<micLine>();
        //Filter out shots that aren't in-arc if the turret option is not chosen
       /* for(micLine l: lineList)
        {
            if(isEdgeInArc(l) == true) filteredList.add(l);
            else l.markedAsDead = true;
        }
*/

        //ALLLINES: if all lines have to been added to the visuals, then, uncomment this section
        if(MULTILINES == true){
            for(micLine everyline : lineList) {
                fov.addLine(everyline);
            }
        }
        //end of section


        //First criterium, find the best distance and make it the best Line
        double bestDist = rangeInt * 282.5;
        micLine best = null;
        for (micLine l : filteredList) {
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

    private boolean isEdgeInArc(micLine theCandidateLine)
    {
        double fudgefactor = 0.00001; //open up the arc just slightly to better allow along-the-arc firing lines
        double firstArcEdgePolarAngle = getEdgeAngle(A1, E1) - fudgefactor; //edge most counter-clockwise
        double secondArcEdgePolarAngle = getEdgeAngle(A2, E2) + fudgefactor; //edge most clockwise

        double bestLinePolarAngle = getEdgeAngle(theCandidateLine.first, theCandidateLine.second);

        //logToChatCommand("arc1: " + Double.toString(firstArcEdgePolarAngle) + " arc2: " + Double.toString(secondArcEdgePolarAngle) + " checkedLine: "+ Double.toString(bestLinePolarAngle));
        if(whichOption == frontAuxArcOption) {
            double thirdArcEdgePolarAngle = getEdgeAngle(A3, E3) - fudgefactor;
            double fourthArcEdgePolarAngle = getEdgeAngle(A4, E4) + fudgefactor;
            if(Double.compare(bestLinePolarAngle, thirdArcEdgePolarAngle) < 0 || Double.compare(bestLinePolarAngle, fourthArcEdgePolarAngle) > 0) return false;
        }
        if (Double.compare(bestLinePolarAngle, firstArcEdgePolarAngle) < 0 || Double.compare(bestLinePolarAngle, secondArcEdgePolarAngle) > 0)
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
    private micLine createLineAAtoD1(micLine A1D1, micLine AA, Point2D.Double D1)
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
        return new micLine(new Point2D.Double(vector_x,vector_y), D1, false);
    }

    //P is the closest vertex; AB are the end points of the segments that is being used to draw a line
    //using an algorithm based on this: http://www.ahristov.com/tutorial/geometry-games/point-line-distance.html
    //isPOnAttacker == true makes P a point that is part of the attacker; if false, P is a point that's part of the defender
    //this is important in order to produce a micLine that will be oriented from attacker to defender, in order to check for proper shooting angles
    //in-arc stuff has to throw lines which don't have the right angle
    //if the attacker segment is being used, A1D1 dotproduct A1A2 needs to be positive and the partialsegment has to be land on the attacker segment in order to be retained
    private micLine createLinePtoAB(Point2D.Double P, micLine AB, boolean isPOnAttacker) {
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

        micLine AP = new micLine(new Point2D.Double(AB.first.x, AB.first.y), P, false);

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
        if(isPOnAttacker == true) return new micLine(P, new Point2D.Double(vector_x, vector_y), markAsDead);
        else return new micLine(new Point2D.Double(vector_x, vector_y), P, markAsDead);
    }

    private micLine createLineAxtoDD_along_arc_edge(Point2D.Double A, Point2D.Double E, micLine DD) {
        //getting D1 again
        double x1 = DD.first.getX();
        double y1 = DD.first.getY();
        //getting D2 again
        double x2 = DD.second.getX();
        double y2 = DD.second.getY();
        //getting A1 again
        double xp = A.getX();
        double yp = A.getY();

        micLine AE = new micLine(A, E, false);
        Point2D.Double D3 = null;
        D3 = findSegmentCrossPoint(AE,DD);
        if(D3 != null) return new micLine(A,D3, false);
        return null;
    }

    //Using this algorithm to detect if the two segments cross
    //https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
    private Point2D.Double findSegmentCrossPoint(micLine A1E1, micLine DD)
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

        micLine qmpLine = new micLine(new Point2D.Double(px, py), new Point2D.Double(qx, qy), false);

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

    private double micLineCrossProduct(micLine A, micLine B)
    {
        double ax = A.second.x - A.first.x;
        double ay = A.second.y - A.first.y;

        double bx = B.second.x - B.first.x;
        double by = B.second.y - B.first.y;

        return ax * by  - ay * bx;
    }

    private double micLineDotProduct(micLine A, micLine B)
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
        java.util.List<BumpableWithShape> bumpables = Lists.newArrayList();

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

    private static class FOVisualization extends Command implements Drawable {

        private final List<Shape> shapes;
        private final List<ShapeWithText> shapesWithText;
        private final List<micLine> lines;

        public Color badLineColor = new Color(0, 121,255,110);
        public Color bestLineColor = new Color(0, 180, 200,255);
        public Color shipsObstaclesColor = new Color(255,99,71, 150);
        public Color arcLineColor = new Color(246, 255, 41,180);
        Color myO = new Color(0,50,255, 50);
        FOVisualization() {
            this.shapes = new ArrayList<Shape>();
            this.lines = new ArrayList<micLine>();
            this.shapesWithText = new ArrayList<ShapeWithText>();
        }
        FOVisualization(Shape ship) {
            this.shapes = new ArrayList<Shape>();
            this.shapes.add(ship);
            this.lines = new ArrayList<micLine>();
            this.shapesWithText = new ArrayList<ShapeWithText>();
        }

        public void add(Shape bumpable) {
            this.shapes.add(bumpable);
        }
        public void addLine(micLine line){
            this.lines.add(line);
        }
        public void addShapeWithText(ShapeWithText swt){ this.shapesWithText.add(swt); }
        public int getCount() {
            int count = 0;
            Iterator<micLine> it = this.lines.iterator();
            while(it.hasNext()){
                count++;
                it.next();
            }
            return count;
        }


        protected void executeCommand() {
            final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");
            int count = getCount();
            if(count > 0) {

                map.addDrawComponent(this);
                draw(map.getView().getGraphics(), map);
                //logToChat("executing with " + Integer.toString(count) + " stuff to draw.");
            }
            else {
                map.removeDrawComponent(FOVisualization.this);
                //logToChat("removing components with " + Integer.toString(count) + " stuff to draw.");
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
            for(micLine line : lines){
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

        public List<micLine> getMicLines() {
            return this.lines;
        }

        public boolean drawAboveCounters() {
            return true;
        }
    }

    public static class AutorangeVisualizationEncoder implements CommandEncoder {
        private static final Logger logger = LoggerFactory.getLogger(AutoBumpDecorator.class);
        private static String commandPrefix = "CollisionVis=";

        public Command decode(String command) {
            if (command == null || !command.contains(commandPrefix)) {
                return null;
            }

            logger.info("Decoding CollisionVisualization");

            command = command.substring(commandPrefix.length());

            try {
                String[] newCommandStrs = command.split("\t");
                FOVisualization visualization = new FOVisualization();
                for (String bytesBase64Str : newCommandStrs) {
                    ByteArrayInputStream strIn = new ByteArrayInputStream(org.apache.commons.codec.binary.Base64.decodeBase64(bytesBase64Str));
                    ObjectInputStream in = new ObjectInputStream(strIn);
                    micLine line = (micLine) in.readObject();
                    visualization.addLine(line);
                    in.close();
                }
                logger.info("Decoded CollisionVisualization with {} shapes", visualization.getShapes().size());
                return visualization;
            } catch (Exception e) {
                logger.error("Error decoding CollisionVisualization", e);
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
                List<String> commandStrs = Lists.newArrayList();
                for (micLine line : visualization.getMicLines()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(bos);
                    out.writeObject(line);
                    out.close();
                    byte[] bytes = bos.toByteArray();
                    String bytesBase64 = org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
                    commandStrs.add(bytesBase64);
                }
                return commandPrefix + Joiner.on('\t').join(commandStrs);
            } catch (Exception e) {
                logger.error("Error encoding autorange visualization", e);
                return null;
            }
        }
    }

    public static class ShapeWithText {
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
    public static class micLine {
        public Boolean isBestLine = false;
        public Boolean isArcLine = false;
        public String rangeString = "Range ";
        public double pixelLength = 0.0f;
        public int rangeLength = 0;
        public int centerX, centerY;
        public Point2D.Double first, second;
        public Line2D.Double line = null;
        public Boolean markedAsDead = false;

        micLine(int x1, int y1, int x2, int y2) {
            this.first = new Point2D.Double(x1, y1);
            this.second = new Point2D.Double(x2, y2);
            doRest("", 0.5);
        }

        //default constructor, lets the range be converted into a string
        micLine(Point2D.Double first, Point2D.Double second, Boolean markAsDead) {
            this.first = first;
            this.second = second;
            doRest("", 0.5);
            markedAsDead = markAsDead;
        }

        //for when you want to set a manual string and an arbitrary string position
        micLine(Point2D.Double first, Point2D.Double second, Boolean markAsDead, String label, double percentage) {
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

    public static class rangeFindings {
        public int range=0;
        public String fullName="";
        public boolean isObstructed=false;

        rangeFindings(){}
        rangeFindings(int range, String fullName){
            this.fullName = fullName;
            this.range = range;
            this.isObstructed = false;
        }
        rangeFindings(int range, String fullName, boolean isObstructed){
            this.fullName = fullName;
            this.range = range;
            this.isObstructed = isObstructed;
        }
    }
    private static class ShipPositionState {
        double x;
        double y;
        double angle;
    }
}
