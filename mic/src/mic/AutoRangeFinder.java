package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mic.manuvers.ManeuverPaths;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.io.*;
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
    int tictoc = 0;

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
                    //myARF.justRunLines(savedOption);
                } catch (Exception e) {
                }
            }
        }, 0,(tictoc==0)?25:150);
        tictoc++;
        if(tictoc==1) tictoc=0;
    }
}

public class AutoRangeFinder extends Decorator implements EditablePiece {

    private static Boolean DEBUGMODE = false;
    private static Boolean MULTILINES = false;

    protected VASSAL.build.module.Map map;
    private static final int frontArcOption = 1;
    private static final int turretArcOption = 2;
    private static final int frontAuxArcOption = 3;
    private static final int backArcOption = 4;
    private static final int mobileSideArcOption = 5;
    private static final int bullseyeArcOption = 6;

    public static final String ID = "auto-range-finder";

    private final FreeRotator testRotator;

    private ShipPositionState prevPosition = null;
    private ManeuverPaths lastManeuver = null;
    private FreeRotator myRotator = null;
    private static Map<String, Integer> keyStrokeToOptions = ImmutableMap.<String, Integer>builder()
            .put("CTRL SHIFT F", frontArcOption) //primary arc
            .put("CTRL SHIFT L", turretArcOption) //turret/TL
            .put("CTRL SHIFT N", frontAuxArcOption) //front pairs of aux arc (YV-666, Auzituck)
            .put("CTRL SHIFT V", backArcOption) //back aux arc
            .put("ALT SHIFT F", mobileSideArcOption) //mobile turret arc, must detect which one is selected on the ship
            .put("CTRL SHIFT X", bullseyeArcOption) //bullseye arc
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
    Point2D.Double E2B, E3B; //alternative perpendicular to front arc in mobile turret+front aux case
    Point2D.Double E1B, E4B; //same for mobile turret, back side
    Point2D.Double bestACorner; //attacker's best corner. In use inside method that quickly calculates band lengths
    Boolean wantExtraBandsMorFA = false;
    int bestBandRange = 0;
    public FiringOptionsVisuals fov;
    public FOVisualization fovCommand;

    Boolean isThisTheOne = false;
    boolean twoPointOh = false;
    public AutoRangeFinder() {
        this(null);
    }
    public AutoRangeFinder(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
        this.fov = new FiringOptionsVisuals();
       // launch();
        map = VASSAL.build.module.Map.getMapById("Map0");
    }

    protected void launch() {
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

        String isShowingLines = (Decorator.getOutermost(this.piece)).getProperty("isShowingLines").toString();

        if (KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.SHIFT_DOWN_MASK,false).equals(stroke)){
            if (this.fov != null && this.fov.getCount() > 0 && fovCommand != null) {
                logToChatWithoutUndo("locally reacting to SHIFT-M");
                this.fov = new FiringOptionsVisuals();
                fovCommand = null;
                return null;
            }
        }

        // check to see if the this code needs to respond to the event
        //identify which autorange option was used by using the static Map defined above in the globals, store it in an int
        whichOption = getKeystrokeToOptions(stroke);
        if (whichOption != -1 && stroke.isOnKeyRelease() == false) {
            logToChat("isShowingLines " + isShowingLines);
            if(isShowingLines.equals("1") && fovCommand != null & this.fov !=null && this.fov.getCount() > 0) return piece.keyEvent(stroke); //not ready to deal with anything until the normal vassal editor trigger has worked and changed this to "0"
            else if(isShowingLines.equals("0") && this.fov !=null && this.fov.getCount() > 0) return piece.keyEvent(stroke); //the line garbage collector has not done its job yet, don't enter now.
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

            twoPointOh = this.getInner().getState().contains("this_is_2pointoh");

            thisShip = new BumpableWithShape(this, "Ship",
                    this.getInner().getProperty("Pilot Name").toString(), this.getInner().getProperty("Craft ID #").toString(),
                    twoPointOh);

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
            Command mBAC = makeBigAnnounceCommand(bigAnnounce, rfindings);

            logToChatWithoutUndo("about to enter. this.fov null? " + (this.fov==null?"yes":"no" + " count=" + this.fov.getCount() + " fovCommand null?" + (fovCommand==null?"yes":"no")));
            if(this.fov !=null && this.fov.getCount() > 0 && fovCommand == null && isShowingLines.equals("1")) {

                logToChatWithoutUndo("locally reacting to making a visual appear");

                //reading off the Piece's unique ID and sending it off in a FOVisualization command, which should make it appear for all
                String micID = this.piece.getProperty("micID").toString();
                fovCommand = new FOVisualization(this.fov, micID);
                fovCommand.append(mBAC);
                fovCommand.execute();
                GameModule.getGameModule().sendAndLog(fovCommand);
                return null;
            } // end of drawing visuals and announcing the results in the chatlog
            mBAC.execute();
            GameModule.getGameModule().sendAndLog(mBAC);
            return null; // for some reason, there were no visuals to do, so send that message and don't send these special keystrokes to others classes/decorators
        } //end of dealing with keystrokes that are linked to autorange lines
        else if (KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK, false).equals(stroke)) {
            if (this.fov != null && this.fov.getCount() > 0 && fovCommand!=null) {
                fovCommand = null;
                Command clearIt = this.piece.keyEvent(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.SHIFT_DOWN_MASK,false));;
                clearIt.execute();
                GameModule.getGameModule().sendAndLog(clearIt);
            }
            return piece.keyEvent(stroke); //send the CTRL-D to deal with the ship whether there were visuals to remove or not
        } //end of deleting a piece and remove its pending visuals if there are any

        return piece.keyEvent(stroke); //did not find anything worth react to, so send back the key for others to deal with it
    }

   /* public void justRunLines(int savedOption){
        whichOption = savedOption;
        ArrayList<RangeFindings> rfindings = new ArrayList<RangeFindings>();
        //if the firing options were already activated, remove the visuals and exit right away
        if (this.fov != null && this.fov.getCount() > 0) {
            //logToChatCommand("toggle off");
            clearVisu(map);
            this.fov.execute();
            return;
        }
        thisShip = new BumpableWithShape(this, "Ship",
                this.getInner().getProperty("Pilot Name").toString(), this.getInner().getProperty("Craft ID #").toString(),
                this.getInner().getState().contains("this_is_2pointoh"));

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
*/

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
                bestLine = findBestLineInSimpleArcs(D1, D2, D3, 3);
                break;
            case backArcOption:
                bestLine = findBestLineInSimpleArcs(D1, D2, D3, 3);
                break;
            case frontAuxArcOption:
                if(twoPointOh == false) bestLine = findBestLineInFrontAuxArcs(D1, D2, D3, 3);
                else findBestLineInFullFrontArc(D1, D2, D3, 3);;
                break;
            case mobileSideArcOption:
                bestLine = findBestLineInMobileArc(D1, D2, D3, 3);
                break;
            case bullseyeArcOption:
                bestLine = findBestLineInBullseye(D1, D2, D3, 3);
                break;
        }

        if (bestLine == null) return;
        bestLine.isBestLine = true;

        //Prepare the end of the appropriate chat announcement string - targets with their range, obstruction notice if relevant, ordered per range
        String bShipName = b.pilotName + "(" + b.shipName + ")";
        RangeFindings found = new RangeFindings(bestLine.rangeLength, bShipName);


        if(shouldWeDoBandScenario(b) == false){
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

        else { //multiple lines case; shouldWeDoBandScenario() == true
            //5th case: will be used in multiple bands, the triangles at the corners must be intersected with the rects of the defender
            //Boolean case5 = checkWeirdCase == true && trickyBandCase == true && are90degreesAligned(thisShip, b) == true && bestLineDoesntCrossArcEdge(bestLine);

            //step 1, make sure the distance is within acceptable ranges. Because of the multiple band
            //scenario that can happen with front aux and mobile turrets, this range should be checked in the line finding block above before this else
            //quickDist will NOT cut it because we aren't using A1 inside all cases of firing bands
            if(validBandRange(D1, D2, D3, 3) == false) return;

            //next, restrict the shape of the band that must be created. This could fork into multiple additions to shapes because of front aux arc and mobile arc
            //1st restriction: what length of attacker edge has to be used
            //2nd restriction: intersect what was kept on the attacker and intersect it with the "long cross" of rectangles emerging from the defender
            double wantedWidth = getBandWidth();

            Shape fromTarget = findInBetweenRectangle(b, thisShip, b.getChassisWidth(), turretArcOption); //use all 4 sides
            ArrayList<Shape> atkShapes = new ArrayList<Shape>();

            switch(whichOption){
                case frontArcOption:
                    Shape temp = findInBetweenRectangle(thisShip, b, wantedWidth, frontArcOption);
                    if(temp!=null)atkShapes.add(temp);
                    break;
                case turretArcOption:
                    Shape temp2 = findInBetweenRectangle(thisShip, b, wantedWidth, turretArcOption);
                    if(temp2!=null) atkShapes.add(temp2);
                    break;
                case backArcOption:
                    Shape temp3 = findInBetweenRectangle(thisShip, b, wantedWidth, backArcOption);
                    if(temp3!=null)atkShapes.add(temp3);
                    break;
                case bullseyeArcOption:
                    Shape temp4 = findInBetweenRectangle(thisShip, b, wantedWidth, bullseyeArcOption);
                    if(temp4!=null)atkShapes.add(temp4);
                    break;
                case frontAuxArcOption:
                    if(twoPointOh) {
                        Shape temp99 = findInBetweenRectangle(thisShip, b, wantedWidth, turretArcOption);
                        if(temp99!=null) atkShapes.add(temp99);
                        break;
                    }
                    Shape temp5 = findInBetweenRectangle(thisShip, b, wantedWidth, frontAuxArcOption);
                    if(temp5!=null)atkShapes.add(temp5);
                    if(wantExtraBandsMorFA){
                        //deal with triangle
                        Shape dualRects = findDualRects(thisShip);
                        Area filteredShape = new Area(dualRects);

                        MicLine DD = new MicLine(D1,D2,false);

                        Point2D.Double ArcIntersectsDD = findSegmentCrossPoint(new MicLine(A2,E2, false), DD, true);
                        if(ArcIntersectsDD == null || (int)ArcIntersectsDD.getX() == 0 && (int)ArcIntersectsDD.getY() == 0) {
                            //the defender ext rects will limit things anyway, let DualRects do its thing
                        }
                        else{
                            MicLine A2DD = createLinePtoAB(A2, DD, true);
                            Point2D.Double the4thPoint = new Point2D.Double(ArcIntersectsDD.x + (A2DD.first.x-A2DD.second.x), ArcIntersectsDD.y + + (A2DD.first.y-A2DD.second.y));
                            GeneralPath excessLeft = new GeneralPath();
                            excessLeft.moveTo(ArcIntersectsDD.x, ArcIntersectsDD.y);
                            excessLeft.lineTo(the4thPoint.x, the4thPoint.y);
                            excessLeft.lineTo(A2.x, A2.y);
                            excessLeft.lineTo(A2DD.second.x, A2DD.second.y);
                            excessLeft.closePath();

                            filteredShape.subtract(new Area(excessLeft));
                        }
                        Point2D.Double ArcIntersectsDDright = findSegmentCrossPoint(new MicLine(A3,E3, false), DD, false);
                        if(ArcIntersectsDDright == null || (int)ArcIntersectsDDright.getX() == 0 && (int)ArcIntersectsDDright.getY() == 0) {

                        }
                        else{
                            MicLine A3DD = createLinePtoAB(A3, DD, true);
                            Point2D.Double the4thPoint = new Point2D.Double(ArcIntersectsDDright.x + (A3DD.first.x-A3DD.second.x), ArcIntersectsDDright.y + + (A3DD.first.y-A3DD.second.y));
                            GeneralPath excessRight = new GeneralPath();
                            excessRight.moveTo(ArcIntersectsDDright.x, ArcIntersectsDDright.y);
                            excessRight.lineTo(the4thPoint.x, the4thPoint.y);
                            excessRight.lineTo(A3.x, A3.y);
                            excessRight.lineTo(A3DD.second.x, A3DD.second.y);
                            excessRight.closePath();

                            filteredShape.subtract(new Area(excessRight));
                        }

                        if(filteredShape!=null) if(filteredShape.getBounds2D().getWidth()!=0) atkShapes.add(filteredShape);

                    }
                    break;
                case mobileSideArcOption:
                    Shape temp6 = findInBetweenRectangle(thisShip, b, wantedWidth, frontArcOption);
                    if(temp6!=null) if(temp6.getBounds2D().getWidth()!=0) atkShapes.add(temp6);
                    Shape temp7 = findInBetweenRectangle(thisShip, b, wantedWidth, mobileSideArcOption);
                    if(temp7!=null) if(temp7.getBounds2D().getWidth()!=0)atkShapes.add(temp7);
                    if(wantExtraBandsMorFA){
                        //deal with triangle
                        Shape dualRects = findDualRects(thisShip);
                        Area protoFilteredShape = new Area(dualRects);
                        Area filteredShape=null;
                        MicLine DD = new MicLine(D1,D2,false);

                        switch(getMobileEdge()){
                            case 4:
                                Point2D.Double ArcIntersectsDD = findSegmentCrossPoint(new MicLine(A2,E2, false), DD, true);
                                if(ArcIntersectsDD == null || (int)ArcIntersectsDD.getX() == 0 && (int)ArcIntersectsDD.getY() == 0) {
                                    //the defender ext rects will limit things anyway, let DualRects do its thing
                                }
                                else{
                                    MicLine A2DD = createLinePtoAB(A2, DD, true);
                                    Point2D.Double the4thPoint = new Point2D.Double(ArcIntersectsDD.x + (A2DD.first.x-A2DD.second.x), ArcIntersectsDD.y + + (A2DD.first.y-A2DD.second.y));
                                    GeneralPath excessLeft = new GeneralPath();
                                    excessLeft.moveTo(ArcIntersectsDD.x, ArcIntersectsDD.y);
                                    excessLeft.lineTo(the4thPoint.x, the4thPoint.y);
                                    excessLeft.lineTo(A2.x, A2.y);
                                    excessLeft.lineTo(A2DD.second.x, A2DD.second.y);
                                    excessLeft.closePath();

                                    protoFilteredShape.subtract(new Area(excessLeft));
                                    double fullWidth = thisShip.getChassisWidth();
                                    Shape sideSelector = findInBetweenRectangle(thisShip, b, fullWidth, turretArcOption);
                                    filteredShape = new Area(sideSelector);
                                    filteredShape.intersect(protoFilteredShape);
                                }
                                Point2D.Double ArcIntersectsDD_back = findSegmentCrossPoint(new MicLine(A1,E1, false), DD, false);
                                if(ArcIntersectsDD_back == null || (int)ArcIntersectsDD_back.getX() == 0 && (int)ArcIntersectsDD_back.getY() == 0) {

                                }
                                else{
                                    MicLine A1DD = createLinePtoAB(A1, DD, true);
                                    Point2D.Double the4thPoint = new Point2D.Double(ArcIntersectsDD_back.x + (A1DD.first.x-A1DD.second.x), ArcIntersectsDD_back.y + + (A1DD.first.y-A1DD.second.y));
                                    GeneralPath excessRight = new GeneralPath();
                                    excessRight.moveTo(ArcIntersectsDD_back.x, ArcIntersectsDD_back.y);
                                    excessRight.lineTo(the4thPoint.x, the4thPoint.y);
                                    excessRight.lineTo(A1.x, A1.y);
                                    excessRight.lineTo(A1DD.second.x, A1DD.second.y);
                                    excessRight.closePath();

                                    protoFilteredShape.subtract(new Area(excessRight));
                                    double fullWidth = thisShip.getChassisWidth();
                                    Shape sideSelector = findInBetweenRectangle(thisShip, b, fullWidth, turretArcOption);
                                    filteredShape = new Area(sideSelector);
                                    filteredShape.intersect(protoFilteredShape);
                                }
                                break;
                            case 2:
                                Point2D.Double ArcIntersectsDDright = findSegmentCrossPoint(new MicLine(A3,E3, false), DD, false);
                                if(ArcIntersectsDDright == null || (int)ArcIntersectsDDright.getX() == 0 && (int)ArcIntersectsDDright.getY() == 0) {

                                }
                                else{
                                    MicLine A3DD = createLinePtoAB(A3, DD, true);
                                    Point2D.Double the4thPoint = new Point2D.Double(ArcIntersectsDDright.x + (A3DD.first.x-A3DD.second.x), ArcIntersectsDDright.y + + (A3DD.first.y-A3DD.second.y));
                                    GeneralPath excessRight = new GeneralPath();
                                    excessRight.moveTo(ArcIntersectsDDright.x, ArcIntersectsDDright.y);
                                    excessRight.lineTo(the4thPoint.x, the4thPoint.y);
                                    excessRight.lineTo(A3.x, A3.y);
                                    excessRight.lineTo(A3DD.second.x, A3DD.second.y);
                                    excessRight.closePath();

                                    protoFilteredShape.subtract(new Area(excessRight));
                                    double fullWidth = thisShip.getChassisWidth();
                                    Shape sideSelector = findInBetweenRectangle(thisShip, b, fullWidth, turretArcOption);
                                    filteredShape = new Area(sideSelector);
                                    filteredShape.intersect(protoFilteredShape);
                                }
                                Point2D.Double ArcIntersectsDDright_back = findSegmentCrossPoint(new MicLine(A4,E4, false), DD, false);
                                if(ArcIntersectsDDright_back == null || (int)ArcIntersectsDDright_back.getX() == 0 && (int)ArcIntersectsDDright_back.getY() == 0) {

                                }
                                else{
                                    MicLine A4DD = createLinePtoAB(A4, DD, true);
                                    Point2D.Double the4thPoint = new Point2D.Double(ArcIntersectsDDright_back.x + (A4DD.first.x-A4DD.second.x), ArcIntersectsDDright_back.y + + (A4DD.first.y-A4DD.second.y));
                                    GeneralPath excessRight = new GeneralPath();
                                    excessRight.moveTo(ArcIntersectsDDright_back.x, ArcIntersectsDDright_back.y);
                                    excessRight.lineTo(the4thPoint.x, the4thPoint.y);
                                    excessRight.lineTo(A4.x, A4.y);
                                    excessRight.lineTo(A4DD.second.x, A4DD.second.y);
                                    excessRight.closePath();

                                    protoFilteredShape.subtract(new Area(excessRight));
                                    double fullWidth = thisShip.getChassisWidth();
                                    Shape sideSelector = findInBetweenRectangle(thisShip, b, fullWidth, turretArcOption);
                                    filteredShape = new Area(sideSelector);
                                    filteredShape.intersect(protoFilteredShape);
                                }
                                break;
                        }

                        if(filteredShape!=null)  if(filteredShape.getBounds2D().getWidth()!=0)atkShapes.add(filteredShape);

                    }
                    break;

            }
//logToChat("nb of bands " + Integer.toString(atkShapes.size()));
            if(atkShapes.size() == 0) return;

            for(Shape s : atkShapes){
                if(fromTarget == null || s == null) continue;
                Area a1 = new Area(s);
                Area a2 = new Area(fromTarget);

                a1.intersect(a2);
                if(checkBandObstruction(a1, new MicLine(D1,D2,false))==true) {
                    found.isObstructed = true;
                }
                else found.isObstructed = false;

                
                double extra = getExtraAngleDuringRectDetection(thisShip, b);
                ShapeWithText bestBand = new ShapeWithText(new Path2D.Double(a1), thisShip.getAngleInRadians() + extra);
                if(found.isObstructed == true) {
                    fov.shapes.add(bestBand.shape);
                    bestBand.rangeString += " obstructed";
                }
                rfindings.add(found);
                fov.addShapeWithText(bestBand); 
            }

            //TO DO:
            //Initial step: if an obstacle intersects this rectangle, get this rectangular shape and find its 2 lengthwise edges
            //case 1: the 2 lines intersect the SAME obstacle. Then, no chance of finding a non-obstructed line. Case closed
            //case 2: if the 2 lines are crossed by different obstacles, then ray-cast all the possible lines and check for an obstacle free line

        }
    }

    //complicated method that sorts out which situation needs bands; gets hairy for mobile turret arcs
    private boolean shouldWeDoBandScenario(BumpableWithShape b) {
        int whichMobileSide = getMobileEdge();

        //deal with the cases where's there no chance of having multiple best lines first; check the rectangles extending from all 4 side of the attacker and check if you hit the defender

        //CASE 1: no in each other's full length extended cross shapes
        Boolean checkOutsideFullRects = isTargetOutsideofRectangles(thisShip, b, true, false);
        if(checkOutsideFullRects==true) return false; //no chance of bands here!
        //CERTIFICATION 1: from now on, the target is at least dipping into the full cross of the attacker

        //check if parallel, or modulo 90 degrees rotated
        //CASE 2: not parallel
        Boolean aligned = are90degreesAligned(thisShip, b);
        if(aligned == false) return false; //another case of no chance of bands here!
        //CERTIFICATION 2: from now on, the target is 90 degrees modulo aligned with the attacker

        //CASE 3: turret shots can be sorted easily now
        if(whichOption == turretArcOption) return true; //we want bands
        //CERTIFICATION 3: turretArcOption is no longer present



        //FORK IN THE ROAD, use specialized isTargetInsideofRectangles for mobile turret later and front aux arcs

        if(whichOption!=mobileSideArcOption && whichOption != frontAuxArcOption){
            //CASE 4: the target is inside of the arc bound rectangles not front aux arc nor mobile turret arcs, could be aligned or not, irrelevant
            //check if the ship is in front of the front edge of the arc
            Boolean checkInsideArcRects = isTargetInsideofRectangles(thisShip, b, true, true);
            if(checkInsideArcRects == false) return false; //another case of no chance of bands here!
            else return true; //otherwise request a line
            //CERTIFICATION 4: front, back, bullseye are no longer present at all;
            //mobile is still possible in all cases where it's in front of of the full width
            //same for front aux arc
        }




        //Getting line DDCC which will allow to settle things on allowing a secondary front-side band or not
        Point2D.Double D1 = findClosestVertex(b, thisShip);
        Point2D.Double D2 = find2ndClosestVertex(b, thisShip);
        Point2D.Double center = new Point2D.Double(thisShip.bumpable.getPosition().getX(), thisShip.bumpable.getPosition().getY());
        MicLine DD = new MicLine(D1, D2, false);
        Point2D.Double CC = findClosestVertex(thisShip, b);

        MicLine DDCC = createLinePtoAB(CC,DD, false);

        Boolean firingArcAllowsBand = (findSegmentCrossPoint(DDCC,new MicLine(center,E1,false),true)!=null ||
                findSegmentCrossPoint(DDCC,new MicLine(center,E2,false),true)!=null ||
                findSegmentCrossPoint(DDCC,new MicLine(center,E3,false),true)!=null ||
                findSegmentCrossPoint(DDCC,new MicLine(center,E4,false),true)!=null)
                ?false:true;

        //CASE 5: front aux arc can get a frontal band, or sideways band
        //deal with easy front aux arc case that will at least get a frontal band
        if(whichOption == frontAuxArcOption){
            Boolean checkInsideArcRects = isTargetInsideofRectangles(thisShip, b, true, true);
            if(checkInsideArcRects == true) return true;
        }
        //CERTIFICATION 5: front aux arc's only remaining case is if it's on frontal side, away from front arc, where it might have a band or not

        //CASE 6: we must check if the firing arc line prevents bands for front aux arc
        if(whichOption == frontAuxArcOption){
            Boolean checkInsideArcRects = isTargetInsideofRectangles(thisShip, b, true, true);

            //subcase 6a, line is blocked, but there might still be a frontal band allowed. decide on that limiting factor
            if(firingArcAllowsBand == false) {
                if(checkInsideArcRects == true) return true;
                else return false;
            }
            //subcase 6b: there's a side front band allowed, so for sure go to the band scenario and global-variable activate the boolean flag for 2ndary band
            else {
                wantExtraBandsMorFA = true;
                return true;
            }
        }
        //CERTIFICATION 6: front aux arc is completely dealt with

        //Completely deal with mobile turret cases that were not totally easy before the FORK above
        if(whichOption==mobileSideArcOption) { //should be moot because it's supposed to be the last surviving option
            Boolean checkFrontArcAlignment = isTargetInsideofFrontRectangle(thisShip, b, true);
            Boolean checkClassicCross = isTargetInsideofRectangles(thisShip, b, true, true);
            switch(whichMobileSide){
                case 1:
                    if(checkFrontArcAlignment==true) return true;
                    else return false;
                case 2:
                    Boolean leftCheck = isTargetInsideofLeftRectangle(thisShip, b, true);
                    if(leftCheck==true) {
                        logToChat("last issue?");
                        return false;
                    }
                    Boolean rightCheck = isTargetInsideofRightRectangle(thisShip, b, true);
                    if(rightCheck== true) return true;
                    Boolean rightFrontBackCheck = isTargetInsideofRightFrontBackRectangle(thisShip, b, true);
                    if(rightFrontBackCheck == true){
                        if(firingArcAllowsBand==true){
                            if(verifyThatCCIsOnSideThatAllowsBands(CC, 2)) {
                                wantExtraBandsMorFA = true;
                            }
                            return true;
                        }
                        if(checkClassicCross==true && verifyThatCCIsOnSideThatAllowsBands(CC, 2)) return true;
                        else return false;
                    }
                    if(checkClassicCross==true) return true;
                    break;
                case 3:
                    if(checkFrontArcAlignment==true) return true;
                    Boolean checkBackArcAlignment_3 = isTargetInsideofBackRectangle(thisShip, b, true);
                    if(checkBackArcAlignment_3==true) return true;
                    return false;
                case 4:
                    Boolean rightCheck_4 = isTargetInsideofRightRectangle(thisShip, b, true);
                    if(rightCheck_4== true) return false;
                    Boolean leftCheck_4 = isTargetInsideofLeftRectangle(thisShip, b, true);
                    if(leftCheck_4== true) return true;
                    Boolean leftFrontBackCheck = isTargetInsideofLeftFrontBackRectangle(thisShip, b, true);
                    if(leftFrontBackCheck == true){
                        if(firingArcAllowsBand==true){
                            if(verifyThatCCIsOnSideThatAllowsBands(CC, 4)) {
                                wantExtraBandsMorFA = true;
                            }
                            return true;
                        }
                        if(checkClassicCross==true  && verifyThatCCIsOnSideThatAllowsBands(CC, 4)) return true;
                        else return false;
                    }
                    if(checkClassicCross==true) return true;
                    break;
            }
        }
        return false; //must request a line because that corner is not in the side that's selected
    }



    private boolean verifyThatCCIsOnSideThatAllowsBands(Point2D.Double CC, int side) {
        ArrayList<Point2D.Double> vertices = thisShip.getVertices();
        if(side==2) {
            if(Math.abs(CC.x - vertices.get(1).x) < 0.01 && Math.abs(CC.y - vertices.get(1).y) < 0.01) return true;
            return false;
        }
        if(side==4){
            if(Math.abs(CC.x - vertices.get(0).x) < 0.01 && Math.abs(CC.y - vertices.get(0).y) < 0.01) return true;
            return false;
        }
        return false;
    }

    private Boolean checkBandObstruction(Area a1, MicLine DD) {
        //case 1, if no obstacles are found, just carry on
        Boolean passesCase1 = true;
        List<BumpableWithShape> obstructions = getObstructionsOnMap();

        List<BumpableWithShape> obstructionsFound = new ArrayList<BumpableWithShape>();

        for (BumpableWithShape obstruction : obstructions) {
            if (shapesOverlap(a1, obstruction.shape)) {
                passesCase1 = false;
                obstructionsFound.add(obstruction);
            }
        }
        if(passesCase1 == true) return false; //no obstacle found, carry on


        //case 2, check the lateral edges and see if they cross the SAME obstacle; if so, it's globally obstructed
        FlatteningPathIterator iter = new FlatteningPathIterator(a1.getPathIterator(new AffineTransform()), 1);
        ArrayList<Point2D.Double> points=new ArrayList<Point2D.Double>();
        double[] coords=new double[6];
        while (!iter.isDone()) {
            iter.currentSegment(coords);
            double x=coords[0];
            double y=coords[1];
            points.add(new Point2D.Double(x,y));
            iter.next();
        }
        if(points.size() <4){
            logToChat("bad rectangle");
            return false;
        }

        MicLine first = new MicLine(points.get(0), points.get(1), false);
        MicLine second = new MicLine(points.get(1), points.get(2), false);
        MicLine third = new MicLine(points.get(3), points.get(2), false);
        MicLine fourth = new MicLine(points.get(0), points.get(3), false);

        MicLine edge1 = first;
        MicLine edge2 = third;

        if(areMicLineParallel(first,DD)==true){
            edge1 = second;
            edge2 = fourth;
        }
        Boolean passesCase2 = true;
        for (BumpableWithShape obstruction : obstructions) {
            if (isLine2DOverlapShape(new Line2D.Double(edge1.first, edge1.second), obstruction.shape)) {
                if (isLine2DOverlapShape(new Line2D.Double(edge2.first, edge2.second), obstruction.shape)) {
                    fov.shapes.add(obstruction.shape);
                    passesCase2 = false;
                }
            }
        }
        if(passesCase2 == false) return true; //the same obstacle blocked the whole width of the rectangle, obstruction found

            //case 3, gotta ray cast from edge 1 to edge 2 and check all those lines. Assume it's false until you find a solid passthrough line
        Boolean passesCase3 = false;
        int count = (int)thisShip.chassis.getWidth();
        for (int i = 0; i < count; i++) {
            MicLine test = new MicLine(new Point2D.Double(edge1.first.x + (1.0/count)* i * (edge2.first.x - edge1.first.x),
                    edge1.first.y + (1.0/count)* i * (edge2.first.y - edge1.first.y)),
                    new Point2D.Double(edge1.second.x + (1.0/count)* i * (edge2.second.x - edge1.second.x),
                            edge1.second.y + (1.0/count)* i * (edge2.second.y - edge1.second.y)), false);


            for (BumpableWithShape obstruction : obstructionsFound) {
                if(isLine2DOverlapShape(new Line2D.Double(test.first, test.second), obstruction.shape)==false) { //finds a way for this rock, but check other rocks
                    passesCase3 = true;
                }
                else { //didn't find a way, check next line
                    passesCase3 = false;
                    break;
                }
            }
            if(passesCase3==true) return false; //found a way despite checking all rocks? then you have a non-obstructed shot
        }

         //no passthrough found, so it's blocked
        for(BumpableWithShape obstruction : obstructionsFound) fov.shapes.add(obstruction.shape);
        return true;

    }

    private boolean areMicLineParallel(MicLine border1, MicLine dd) {
        int angle1 = (int)((1/Math.PI)*180.0*getEdgeAngle(border1.first,border1.second));
        double angle2 = (int)((1/Math.PI)*180.0*getEdgeAngle(dd.first,dd.second));
        double angle3 = (int)((1/Math.PI)*180.0*getEdgeAngle(dd.second, dd.first));

        if(angle1 == angle2 || angle1 == angle3) return true;
        return false;
    }

    private boolean bestLineDoesntCrossArcEdge(MicLine bestLine) {
        return(Double.compare(bestLine.pixelLength, thisShip.getTriMaxVert()) < 0);
    }

    private double getBandWidth(){
        double wantedWidth = thisShip.getChassisWidth();
        switch(whichOption){
            case turretArcOption:
                break;
            case frontArcOption:
            case backArcOption:
            case frontAuxArcOption:
            case mobileSideArcOption:
                wantedWidth = thisShip.getChassisWidth() - thisShip.chassis.getCornerToFiringArc() * 2.0;
                break;
            case bullseyeArcOption:
                wantedWidth = thisShip.getChassis().getBullsEyeWidth();
                break;
        }
        return wantedWidth;
    }
    private boolean validBandRange(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int whichRange) {
        //when this method is reached, there are some guarantees as to how the attacker and defender are positioned
        //fact 1: they are parallel (not necesserily the same exact facing, but at worst at an integer multiple of 90 degrees rotated from each other
        //fact 2: they are inside each other's full ship width cross
        int foundRange=999;

        switch(whichOption){
            case turretArcOption:
            case frontArcOption:
            case backArcOption:
            case bullseyeArcOption:
                MicLine AA = new MicLine(A1, A2, false);
                MicLine D1AA = createLinePtoAB(D1, AA, false);
                if(D1AA != null) foundRange = D1AA.rangeLength;
                break;
            case frontAuxArcOption:
            case mobileSideArcOption:
                MicLine DD = new MicLine(D1, D2,false);
                MicLine A1DD = createLinePtoAB(bestACorner, DD, true);
                if(A1DD != null) foundRange = A1DD.rangeLength;
                break;
        }

        if(foundRange <= whichRange) return true;
        return false;
    }

    private int getMobileEdge() {
        //1: front 2: right, 3: back, 4: left
        String whichSideString ="";
        try {
            whichSideString = ((Decorator) piece).getDecorator(piece, piece.getClass()).getProperty("whichSide").toString();
        }catch(Exception e){

        }
        if("1".equals(whichSideString)) return 1;
        if("2".equals(whichSideString)) return 2;
        if("3".equals(whichSideString)) return 3;
        if("4".equals(whichSideString)) return 4;

        //2.0 additions for pulsar-type double turrets
        if("13".equals(whichSideString)) return 13;
        if("24".equals(whichSideString)) return 24;
        return 1;
    }

    private MicLine vetThisLine(MicLine A1D1, String label, double v) {
        if(A1D1 == null || MULTILINES == false) return A1D1;
        return new MicLine(A1D1.first, A1D1.second, A1D1.markedAsDead, label, v);
    }


    private MicLine findBestLineInMobileArc(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {
        ArrayList<MicLine> frontList = new ArrayList<MicLine>(); //reserved for front arc lines
        ArrayList<MicLine> rightList = new ArrayList<MicLine>(); //reserved for right side arc lines
        ArrayList<MicLine> backList = new ArrayList<MicLine>(); //reserved for back arc lines
        ArrayList<MicLine> leftList = new ArrayList<MicLine>(); //reserved for left side arc lines
        ArrayList<MicLine> noAngleCheckList = new ArrayList<MicLine>(); //reserved lines that are sure to be angle-ok

        Point2D.Double center = new Point2D.Double(thisShip.bumpable.getPosition().x, thisShip.bumpable.getPosition().y);

        Point2D.Double LCF = thisShip.getVertices().get(0); //left corner front
        Point2D.Double RCF = thisShip.getVertices().get(1); //right corner front
        Point2D.Double RCB = thisShip.getVertices().get(2); //right corner back
        Point2D.Double LCB = thisShip.getVertices().get(3); //left corner back

        //Prep segments along ships (attacker and defender) used to figure out some of the firing lines
        //Left Edge
        MicLine LE = new MicLine(LCB, LCF, false);
        //Left Front Edge
        MicLine LFE = new MicLine(LCF, A2, false);
        //Left Back Edge
        MicLine LBE = new MicLine(A1,LCB, false);
        //Right Edge
        MicLine RE = new MicLine(RCF, RCB, false);
        //Right Front Edge
        MicLine RFE = new MicLine(A3, RCF, false);
        //Right Back Edge
        MicLine RBE = new MicLine(A4, RCB, false);
        //Front arc Edge
        MicLine AAF = new MicLine(A2, A3, false);
        //Back arc Edge
        MicLine AAB = new MicLine(A1, A4, false);
        //Closest Defender Edge
        MicLine DD = new MicLine(D1, D2, false);
        //2nd closest defender edge
        MicLine DD_2nd = new MicLine(D1, D3, false);

        MicLine lineToVet;

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
        //////////////////

        //1st arc edge, back left
        MicLine A1DD_arc_restricted = createLineAxtoDD_along_arc_edge(A1, E1, DD);
        //1st arc edge, back left to 2nd def edge
        MicLine A1DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A1, E1, DD_2nd);
        //4th front arc edge, back right
        MicLine A4DD_arc_restricted = createLineAxtoDD_along_arc_edge(A4, E4, DD);
        //4th front arc edge, back right to 2nd def edge
        MicLine A4DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A4, E4, DD_2nd);


        //Lines from front arc extremes and front arc edge
        //front arc lines
        MicLine A2D1 = new MicLine(A2, D1, false);
        lineToVet = vetThisLine(A2D1, "A2D1", 0.3);
        if(lineToVet != null) frontList.add(lineToVet);
        MicLine A2D2 = new MicLine(A2, D2, false);
        lineToVet = vetThisLine(A2D2, "A2D2", 0.5);
        if(lineToVet != null) frontList.add(lineToVet);

        MicLine A3D1 = new MicLine(A3, D1, false);
        lineToVet = vetThisLine(A3D1, "A3D1", 0.3);
        if(lineToVet != null) frontList.add(lineToVet);

        MicLine A3D2 = new MicLine(A3, D2, false);
        lineToVet = vetThisLine(A3D2, "A3D2", 0.5);
        if(lineToVet != null) frontList.add(lineToVet);

        //other 2 lines from other arc edges
        MicLine A1D1 = new MicLine(A1, D1, false);
        MicLine A4D1 = new MicLine(A4, D1, false);

        //normal to defender's edges
        //Closest attacker's point to the defender's closest edge
        MicLine A2DD = createLinePtoAB(A2, DD, true);
        lineToVet = vetThisLine(A2DD, "A2DD", 0.2);
        if(lineToVet != null) frontList.add(lineToVet);

        MicLine A3DD = createLinePtoAB(A3, DD, true);
        lineToVet = vetThisLine(A3DD, "A3DD", 0.4);
        if(lineToVet != null) frontList.add(lineToVet);

        //normal to attacker's front part
        MicLine AAD1 = createLinePtoAB(D1, AAF, false);
        lineToVet = vetThisLine(AAD1, "AAD1", 0.4);
        if(lineToVet != null) frontList.add(lineToVet);
        ////////////////////////

        //corners
        MicLine LCFD1 = new MicLine(LCF, D1, false);
        MicLine RCFD1 = new MicLine(RCF, D1, false);
        MicLine LCBD1 = new MicLine(LCB, D1, false);
        MicLine RCBD1 = new MicLine(RCB, D1, false);

        //corners falling into defender's normal
        MicLine LCFDD = createLinePtoAB(LCF, DD, true);
        MicLine LCBDD = createLinePtoAB(LCB, DD, true);
        MicLine RCFDD = createLinePtoAB(RCF, DD, true);
        MicLine RCBDD = createLinePtoAB(RCB, DD, true);

        int mobileSide = getMobileEdge();
        switch(mobileSide){
            case 2:
                //along arc edges
                lineToVet = vetThisLine(A4DD_arc_restricted, "A4DD_ea", 0.5);
                if(lineToVet != null) noAngleCheckList.add(lineToVet);
                lineToVet = vetThisLine(A4DD_arc_restricted_2nd, "A4DD_2nd_ea", 0.7);
                if(lineToVet != null) noAngleCheckList.add(lineToVet);

                //from the arc extremities, free-style direction
                lineToVet = vetThisLine(A3D1, "A3D1_right", 0.2);
                if(lineToVet != null) rightList.add(lineToVet);
                lineToVet = vetThisLine(A4D1, "A4D1_right", 0.8);
                if(lineToVet != null) rightList.add(lineToVet);

                //from the corners
                if(findSegmentCrossPoint(RCFD1, new MicLine(center, E3, false), true)==null &&
                        findSegmentCrossPoint(RCFD1, new MicLine(center, E4, false), true)==null ){
                    lineToVet = vetThisLine(RCFD1, "RCFD1", 0.4);
                    if(lineToVet != null) noAngleCheckList.add(lineToVet);
                }
                if(findSegmentCrossPoint(RCBD1, new MicLine(center, E4, false), true)==null &&
                        findSegmentCrossPoint(RCBD1, new MicLine(center, E4, false), true)==null){
                    lineToVet = vetThisLine(RCBD1, "RCBD1", 0.4);
                    if(lineToVet != null) noAngleCheckList.add(lineToVet);
                }

                //from the corners, normal
                if(findSegmentCrossPoint(RCFDD, new MicLine(center, E3, false), true)==null &&
                        findSegmentCrossPoint(RCFDD, new MicLine(center, E4, false), true)==null){
                    lineToVet = vetThisLine(RCFDD, "RCFDD", 0.4);
                    if(lineToVet != null) noAngleCheckList.add(lineToVet);
                }
                if(findSegmentCrossPoint(RCBDD, new MicLine(center, E4, false), true)==null &&
                        findSegmentCrossPoint(RCBDD, new MicLine(center, E4, false), true)==null) {
                    lineToVet = vetThisLine(RCBDD, "RCBDD", 0.4);
                    if (lineToVet != null) noAngleCheckList.add(lineToVet);
                }
                //normal to attacker
                MicLine RED1 = createLinePtoAB(D1, RE, false);
                if(doesAAforInArcPassTest(RED1, RE)== true && isRangeOk(RED1, 1, rangeInt))
                {
                    lineToVet = vetThisLine(RED1, "RED1", 0.8);
                    if(lineToVet != null) rightList.add(lineToVet);
                }

                //small edges normal to attacker
                MicLine RFED1 = createLinePtoAB(D1, RFE, false);
                if(doesAAforInArcPassTest(RFED1, RFE)== true && isRangeOk(RFED1, 1, rangeInt) &&
                        findSegmentCrossPoint(RFED1, new MicLine(center, E3, false), true)==null &&
                        findSegmentCrossPoint(RFED1, new MicLine(center, E4, false), true)==null) {
                    lineToVet = vetThisLine(RFED1, "RFED1", 0.8);
                    if(lineToVet != null) noAngleCheckList.add(lineToVet);
                }

                MicLine RBED1 = createLinePtoAB(D1, RBE, false);
                if(doesAAforInArcPassTest(RBED1, RBE)== true && isRangeOk(RBED1, 1, rangeInt) &&
                        findSegmentCrossPoint(RBED1, new MicLine(center, E3, false), true)==null &&
                        findSegmentCrossPoint(RBED1, new MicLine(center, E4, false), true)==null) {
                    lineToVet = vetThisLine(RBED1, "RBED1", 0.8);
                    if(lineToVet != null) noAngleCheckList.add(lineToVet);
                }
                break;
            case 3:
                //along arc edges
                lineToVet = vetThisLine(A1DD_arc_restricted, "A1DD_ea", 0.5);
                if(lineToVet != null) noAngleCheckList.add(lineToVet);
                lineToVet = vetThisLine(A1DD_arc_restricted_2nd, "A1DD_2nd_ea", 0.7);
                if(lineToVet != null) noAngleCheckList.add(lineToVet);
                lineToVet = vetThisLine(A4DD_arc_restricted, "A4DD_ea", 0.5);
                if(lineToVet != null) noAngleCheckList.add(lineToVet);
                lineToVet = vetThisLine(A4DD_arc_restricted_2nd, "A4DD_2nd_ea", 0.7);
                if(lineToVet != null) noAngleCheckList.add(lineToVet);

                //from the arc extremities, free-style direction
                lineToVet = vetThisLine(A1D1, "A1D1_back", 0.2);
                if(lineToVet != null) backList.add(lineToVet);
                lineToVet = vetThisLine(A4D1, "A4D1_back", 0.8);
                if(lineToVet != null) backList.add(lineToVet);

                //normal to attacker
                MicLine BED1 = createLinePtoAB(D1, AAB, false);
                if(doesAAforInArcPassTest(BED1, AAB)== true && isRangeOk(BED1, 1, rangeInt))
                {
                    lineToVet = vetThisLine(BED1, "BED1", 0.8);
                    if(lineToVet != null) backList.add(lineToVet);
                }
                //normal to attacker's front part
                MicLine AABD1 = createLinePtoAB(D1, AAB, false);
                lineToVet = vetThisLine(AABD1, "AAD1", 0.4);
                if(lineToVet != null) backList.add(lineToVet);
                break;
            case 4:
                //along the arc edges
                lineToVet = vetThisLine(A1DD_arc_restricted, "A1DD_ea", 0.5);
                if(lineToVet != null) noAngleCheckList.add(lineToVet);
                lineToVet = vetThisLine(A1DD_arc_restricted_2nd, "A1DD_2nd_ea", 0.7);
                if(lineToVet != null) noAngleCheckList.add(lineToVet);

                //from the arc extremeties, free-style direction
                lineToVet = vetThisLine(A1D1, "A1D1_left", 0.2);
                if(lineToVet != null) leftList.add(lineToVet);
                lineToVet = vetThisLine(A2D1, "A2D1_left", 0.8);
                if(lineToVet != null) leftList.add(lineToVet);

                //from the corners
                if(findSegmentCrossPoint(LCFD1, new MicLine(center, E2, false), true)==null &&
                        findSegmentCrossPoint(LCFD1, new MicLine(center, E1, false), true)==null){
                    lineToVet = vetThisLine(LCFD1, "LCFD1", 0.4);
                    if(lineToVet != null) noAngleCheckList.add(lineToVet);
                }
                if(findSegmentCrossPoint(LCBD1, new MicLine(center, E1, false), true)==null &&
                        findSegmentCrossPoint(LCBD1, new MicLine(center, E2, false), true)==null){
                    lineToVet = vetThisLine(LCBD1, "LCBD1", 0.4);
                    if(lineToVet != null) noAngleCheckList.add(lineToVet);
                }

                //from the corners, normal
                if(findSegmentCrossPoint(LCFDD, new MicLine(center, E2, false), true)==null &&
                        findSegmentCrossPoint(LCFDD, new MicLine(center, E1, false), true)==null){
                    lineToVet = vetThisLine(LCFDD, "LCFDD", 0.4);
                    if(lineToVet != null) noAngleCheckList.add(lineToVet);
                }
                if(findSegmentCrossPoint(LCBDD, new MicLine(center, E1, false), true)==null &&
                        findSegmentCrossPoint(LCBDD, new MicLine(center, E2, false), true)==null){
                    lineToVet = vetThisLine(LCBDD, "LCBDD", 0.4);
                    if(lineToVet != null) noAngleCheckList.add(lineToVet);
                }

                //normal to attacker
                MicLine LED1 = createLinePtoAB(D1, LE, false);
                if(doesAAforInArcPassTest(LED1, LE)== true && isRangeOk(LED1, 1, rangeInt))
                {
                    lineToVet = vetThisLine(LED1, "LED1", 0.8);
                    if(lineToVet != null) leftList.add(lineToVet);
                }
                //small edges normal to attacker
                MicLine LFED1 = createLinePtoAB(D1, LFE, false);
                if(doesAAforInArcPassTest(LFED1, LFE)== true && isRangeOk(LFED1, 1, rangeInt) &&
                        findSegmentCrossPoint(LFED1, new MicLine(center, E1, false), true)==null &&
                        findSegmentCrossPoint(LFED1, new MicLine(center, E2, false), true)==null) {
                    lineToVet = vetThisLine(LFED1, "LFED1", 0.8);
                    if(lineToVet != null) noAngleCheckList.add(lineToVet);
                }

                MicLine LBED1 = createLinePtoAB(D1, LBE, false);
                if(doesAAforInArcPassTest(LBED1, LBE)== true && isRangeOk(LBED1, 1, rangeInt) &&
                        findSegmentCrossPoint(LBED1, new MicLine(center, E1, false), true)==null &&
                        findSegmentCrossPoint(LBED1, new MicLine(center, E2, false), true)==null) {
                    lineToVet = vetThisLine(LBED1, "LBED1", 0.8);
                    if(lineToVet != null) noAngleCheckList.add(lineToVet);
                }
                break;
        }
        
        ArrayList<MicLine> filteredList = new ArrayList<MicLine>();
        ArrayList<MicLine> deadList = new ArrayList<MicLine>();

        //logToChat("before filtering; front: " + Integer.toString(frontList.size()) + " right: " + Integer.toString(rightList.size()) + " back: " + Integer.toString(backList.size()) + " left: " + Integer.toString(leftList.size()));
        //Filter out shots that aren't inside the left aux arc
        for(MicLine l: leftList)
        {
            if(isEdgeInArc(l, A1, E1, A2, E2) == true) filteredList.add(l);
            else deadList.add(l);
        }
        //Same, right side
        for(MicLine l: rightList)
        {
            if(isEdgeInArc(l, A3, E3, A4, E4) == true) filteredList.add(l);
            else deadList.add(l);
        }
        //Same, front side
        for(MicLine l: frontList)
        {
            if(isEdgeInArc(l, A2, E2, A3, E3) == true) filteredList.add(l);
            else deadList.add(l);
        }
        //Same, back side
        for(MicLine l: backList)
        {
            if(isEdgeInArc(l, A4, E4, A1, E1) == true) filteredList.add(l);
            else deadList.add(l);
        }
        //add the rest of the safe angle lines
        for(MicLine l: noAngleCheckList) {
            filteredList.add(l);
        }

        //logToChat("after filtering: " + Integer.toString(filteredList.size()));

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

        //logToChat("found a best? " + Boolean.toString(best!=null?true:false));
        //nothing under the requested range was found, no best lines can be submitted
        if (best == null) {
            return null;
        }

        best.isArcLine = true;
        return best;
    }

    private MicLine findBestLineInFullFrontArc(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {
        ArrayList<MicLine> lineList = new ArrayList<MicLine>(); //only need one list for this one

        Point2D.Double LC = thisShip.getVertices().get(0);
        Point2D.Double RC = thisShip.getVertices().get(1);

        Point2D.Double center = new Point2D.Double(thisShip.bumpable.getPosition().x, thisShip.bumpable.getPosition().y);

        //Prep segments along ships (attacker and defender) used to figure out some of the firing lines
        //Left Edge
        MicLine LE = new MicLine(A1, LC, false);
        //Right Edge
        MicLine RE = new MicLine(RC, A2, false);
        //Front arc Edge
        MicLine AA = new MicLine(LC, RC, false);
        //Closest Defender Edge
        MicLine DD = new MicLine(D1, D2, false);
        //2nd closest defender edge
        MicLine DD_2nd = new MicLine(D1, D3, false);


        MicLine lineToVet; //temp MicLine object used for debug testing

        //Left Corner Attacker to Closest Defender
        MicLine LCD1 = new MicLine(LC, D1, false);
        if(findSegmentCrossPoint(LCD1, new MicLine(center, E1, false), true)==null &&
                findSegmentCrossPoint(LCD1, new MicLine(center, E2, false), true)==null){
            lineToVet = vetThisLine(LCD1, "LCD1", 0.1);
            if(lineToVet != null) lineList.add(lineToVet);
        }

        //Left Corner Attacker to 2nd closest Defender
        MicLine LCD2 = new MicLine(LC, D2, false);
        if(findSegmentCrossPoint(LCD2, new MicLine(center, E1, false), true)==null &&
                findSegmentCrossPoint(LCD2, new MicLine(center, E2, false), true)==null){
            lineToVet = vetThisLine(LCD2, "LCD2", 0.1);
            if(lineToVet != null) lineList.add(lineToVet);
        }
        //Right Corner Attacker to Closest Defender
        MicLine RCD1 = new MicLine(RC, D1, false);
        if(findSegmentCrossPoint(LCD2, new MicLine(center, E1, false), true)==null &&
                findSegmentCrossPoint(LCD2, new MicLine(center, E2, false), true)==null){
            lineToVet = vetThisLine(RCD1, "RCD1", 0.1);
            if(lineToVet != null) lineList.add(lineToVet);
        }

        //Right Corner Attacker to 2nd closest Defender
        MicLine RCD2 = new MicLine(RC, D2, false);
        if(findSegmentCrossPoint(LCD2, new MicLine(center, E1, false), true)==null &&
                findSegmentCrossPoint(LCD2, new MicLine(center, E2, false), true)==null){
            lineToVet = vetThisLine(RCD2, "RCD2", 0.1);
            if(lineToVet != null) lineList.add(lineToVet);
        }


        //first arc edge
        MicLine A1DD_arc_restricted = createLineAxtoDD_along_arc_edge(A1, E1, DD);
        lineToVet = vetThisLine(A1DD_arc_restricted, "A1DD_ea", 0.5);
        if(lineToVet != null) lineList.add(lineToVet);
        //first arc edge to 2nd def edge
        MicLine A1DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A1, E1, DD_2nd);
        lineToVet = vetThisLine(A1DD_arc_restricted_2nd, "A1DD_2nd_ea", 0.7);
        if(lineToVet != null) lineList.add(lineToVet);

        //2nd arc edge
        MicLine A2DD_arc_restricted = createLineAxtoDD_along_arc_edge(A2, E2, DD);
        lineToVet = vetThisLine(A2DD_arc_restricted, "A2DD_ea", 0.5);
        if(lineToVet != null) lineList.add(lineToVet);
        //2nd arc edge to 2nd def edge
        MicLine A2DD_arc_restricted_2nd = createLineAxtoDD_along_arc_edge(A2, E2, DD_2nd);
        lineToVet = vetThisLine(A2DD_arc_restricted_2nd, "A2DD_2nd_ea", 0.7);
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
            if(lineToVet != null) lineList.add(lineToVet);
        }

        //Attacker's left long edge to defender's closest vertex
        MicLine LED1 = createLinePtoAB(D1, LE, false);
        if(doesAAforInArcPassTest(LED1, LE)== true && isRangeOk(LED1, 1, rangeInt))
        {
            lineToVet = vetThisLine(LED1, "LED1", 0.8);
            if(lineToVet != null) lineList.add(lineToVet);
        }
        //same but right side
        MicLine RED1 = createLinePtoAB(D1, RE, false);
        if(doesAAforInArcPassTest(RED1, RE)== true && isRangeOk(RED1, 1, rangeInt))
        {
            lineToVet = vetThisLine(RED1, "RED1", 0.8);
            if(lineToVet != null) lineList.add(lineToVet);
        }

        //from the corners, normal
        MicLine LCDD = createLinePtoAB(LC, DD, false);
        if(findSegmentCrossPoint(LCDD, new MicLine(center, E1, false), true)==null){
            lineToVet = vetThisLine(LCDD, "LCDD", 0.4);
            if(lineToVet != null) lineList.add(lineToVet);
        }
        MicLine RCDD = createLinePtoAB(RC, DD, false);
        if(findSegmentCrossPoint(RCDD, new MicLine(center, E2, false), true)==null) {
            lineToVet = vetThisLine(RCDD, "RCDD", 0.4);
            if(lineToVet != null) lineList.add(lineToVet);
        }

        ArrayList<MicLine> filteredList = new ArrayList<MicLine>();
        ArrayList<MicLine> deadList = new ArrayList<MicLine>();

        for(MicLine l: lineList)
        {
            if(isEdgeInArcInwardTweak(l, A1, E1, A2, E2) == true) filteredList.add(l);
            else deadList.add(l);
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

    private MicLine findBestLineInFrontAuxArcs(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {
        ArrayList<MicLine> lineList = new ArrayList<MicLine>(); //reserved for front arc lines
        ArrayList<MicLine> leftLineList = new ArrayList<MicLine>(); //reserved for left aux arc lines
        ArrayList<MicLine> rightLineList = new ArrayList<MicLine>(); //reserved for right aux arc lines
        ArrayList<MicLine> noAngleCheckList = new ArrayList<MicLine>(); //along the arc edge, so already angle valid

        Point2D.Double LC = thisShip.getVertices().get(0);
        Point2D.Double RC = thisShip.getVertices().get(1);

        Point2D.Double center = new Point2D.Double(thisShip.bumpable.getPosition().x, thisShip.bumpable.getPosition().y);

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
        if(findSegmentCrossPoint(LCD1, new MicLine(center, E1, false), true)==null &&
                findSegmentCrossPoint(LCD1, new MicLine(center, E2, false), true)==null){
            lineToVet = vetThisLine(LCD1, "LCD1", 0.1);
            if(lineToVet != null) noAngleCheckList.add(lineToVet);
        }

        //Left Corner Attacker to 2nd closest Defender
        MicLine LCD2 = new MicLine(LC, D2, false);
        if(findSegmentCrossPoint(LCD2, new MicLine(center, E1, false), true)==null &&
                findSegmentCrossPoint(LCD2, new MicLine(center, E2, false), true)==null){
            lineToVet = vetThisLine(LCD2, "LCD2", 0.1);
            if(lineToVet != null) noAngleCheckList.add(lineToVet);
        }

        //Right Corner Attacker to Closest Defender
        MicLine RCD1 = new MicLine(RC, D1, false);
        if(findSegmentCrossPoint(RCD1, new MicLine(center, E3, false), true)==null &&
                findSegmentCrossPoint(RCD1, new MicLine(center, E4, false), true)==null){
            lineToVet = vetThisLine(RCD1, "RCD1", 0.1);
            if(lineToVet != null) noAngleCheckList.add(lineToVet);
        }

        //Right Corner Attacker to 2nd closest Defender
        MicLine RCD2 = new MicLine(RC, D2, false);
        if(findSegmentCrossPoint(RCD2, new MicLine(center, E3, false), true)==null &&
                findSegmentCrossPoint(RCD2, new MicLine(center, E4, false), true)==null){
            lineToVet = vetThisLine(RCD2, "RCD2", 0.1);
            if(lineToVet != null) noAngleCheckList.add(lineToVet);
        }


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

        MicLine A3DD = createLinePtoAB(A3, DD, true);
        lineToVet = vetThisLine(A3DD, "A3DD", 0.2);
        if(lineToVet != null) lineList.add(lineToVet);

        MicLine A4DD = createLinePtoAB(A4, DD, true);
        lineToVet = vetThisLine(A4DD, "A4DD", 0.4);
        if(lineToVet != null) lineList.add(lineToVet);


        //Attacker's edge to defender's closest vertex
        MicLine AAD1 = createLinePtoAB(D1, AA, false);
        if(doesAAforInArcPassTest(AAD1, AA)== true && isRangeOk(AAD1, 1, rangeInt))
        {
            lineToVet = vetThisLine(AAD1, "AAD1", 0.8);
            if(lineToVet != null) lineList.add(lineToVet);
        }

        //Attacker's left long edge to defender's closest vertex
        MicLine LED1 = createLinePtoAB(D1, LE, false);
        if(doesAAforInArcPassTest(LED1, LE)== true && isRangeOk(LED1, 1, rangeInt))
        {
            lineToVet = vetThisLine(LED1, "LED1", 0.8);
            if(lineToVet != null) leftLineList.add(lineToVet);
        }
        //same but right side
        MicLine RED1 = createLinePtoAB(D1, RE, false);
        if(doesAAforInArcPassTest(RED1, RE)== true && isRangeOk(RED1, 1, rangeInt))
        {
            lineToVet = vetThisLine(RED1, "RED1", 0.8);
            if(lineToVet != null) rightLineList.add(lineToVet);
        }

        //from the corners, normal
        MicLine LCDD = createLinePtoAB(LC, DD, false);
        if(findSegmentCrossPoint(LCDD, new MicLine(center, E2, false), true)==null &&
                findSegmentCrossPoint(LCDD, new MicLine(center, E1, false), true)==null){
            lineToVet = vetThisLine(LCDD, "LCDD", 0.4);
            if(lineToVet != null) noAngleCheckList.add(lineToVet);
        }
        MicLine RCDD = createLinePtoAB(RC, DD, false);
        if(findSegmentCrossPoint(RCDD, new MicLine(center, E3, false), true)==null &&
                findSegmentCrossPoint(RCDD, new MicLine(center, E4, false), true)==null){
            lineToVet = vetThisLine(RCDD, "RCDD", 0.4);
            if(lineToVet != null) noAngleCheckList.add(lineToVet);
        }

        //small edges normal to attacker
        MicLine LFED1 = createLinePtoAB(D1, LFE, false);
        if(doesAAforInArcPassTest(LFED1, LFE)== true && isRangeOk(LFED1, 1, rangeInt) &&
                findSegmentCrossPoint(LFED1, new MicLine(center, E1, false), true)==null &&
                findSegmentCrossPoint(LFED1, new MicLine(center, E2, false), true)==null) {
            lineToVet = vetThisLine(LFED1, "LFED1", 0.8);
            if(lineToVet != null) noAngleCheckList.add(lineToVet);
        }

        MicLine RFED1 = createLinePtoAB(D1, RFE, false);
        if(doesAAforInArcPassTest(RFED1, RFE)== true && isRangeOk(RFED1, 1, rangeInt) &&
                findSegmentCrossPoint(RFED1, new MicLine(center, E3, false), true)==null &&
                findSegmentCrossPoint(RFED1, new MicLine(center, E4, false), true)==null) {
            lineToVet = vetThisLine(RFED1, "RFED1", 0.8);
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


    private MicLine findBestLineInBullseye(Point2D.Double D1, Point2D.Double D2, Point2D.Double D3, int rangeInt) {
        ArrayList<MicLine> lineList = new ArrayList<MicLine>(); //along the arc edge, so already angle valid
        ArrayList<MicLine> noAngleCheckList = new ArrayList<MicLine>();

        //Closest Attacker Edge
        MicLine AA = new MicLine(A1, A2, false);
        //Closest Defender Edge
        MicLine DD = new MicLine(D1, D2, false);
        //2nd closest defender edge
        MicLine DD_2nd = new MicLine(D1, D3, false);

        MicLine lineToVet;
        //Attacker's edge to defender's closest vertex
        MicLine AAD1 = createLinePtoAB(D1, AA, false);
        if(doesAAforInArcPassTest(AAD1, AA)== true && isRangeOk(AAD1, 1, rangeInt))
        {
            lineToVet = vetThisLine(AAD1, "AAD1", 0.8);
            if(lineToVet != null) lineList.add(lineToVet);
        }

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


        ArrayList<MicLine> filteredList = new ArrayList<MicLine>();
        ArrayList<MicLine> deadList = new ArrayList<MicLine>();
        //Add already vetted lines
        for(MicLine l: noAngleCheckList){
            filteredList.add(l);
        }


        //Filter out shots that aren't in-arc if the turret option is not chosen
        for(MicLine l: lineList)
        {
            if(isEdgeInArc(l, A1, E1, A2, E2) == true) filteredList.add(l);
            else deadList.add(l);
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

        //Closest Attacker to Closest Defender
        MicLine A1D2 = new MicLine(A1, D2, false);
        lineToVet = vetThisLine(A1D2, "A1D2", 0.1);
        if(lineToVet != null) lineList.add(lineToVet);

        //Closest Defender to 2nd Closest Attacker
        MicLine A2D1 = new MicLine(A2, D1, false);
        lineToVet = vetThisLine(A2D1, "A2D1", 0.5);
        if(lineToVet != null) lineList.add(lineToVet);

        //Closest Defender to 2nd Closest Attacker
        MicLine A2D2 = new MicLine(A2, D2, false);
        lineToVet = vetThisLine(A2D2, "A2D2", 0.5);
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
            if(lineToVet != null) lineList.add(lineToVet);
        }

        //Attacker's edge to defender's 2nd closest vertex
        MicLine AAD2 = createLinePtoAB(D2, AA, false);

        if(doesAAforInArcPassTest(AAD2, AA)== true && isRangeOk(AAD2, 1, rangeInt))
        {
            lineToVet = vetThisLine(AAD2, "AAD2", 0.8);
            if(lineToVet != null) lineList.add(lineToVet);
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

    private void prepAnnouncementEnd() {
    }

    private void findAttackerBestPoints(BumpableWithShape b) {

        bestACorner = findClosestVertex(thisShip, b);

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
        else if(whichOption == frontAuxArcOption && twoPointOh == false){
            A1 = thisShip.tPts.get(8);
            A2 = thisShip.tPts.get(0);

            E1 = thisShip.tPts.get(10);
            E2 = thisShip.tPts.get(2);

            A3 = thisShip.tPts.get(1);
            A4 = thisShip.tPts.get(9);

            E3 = thisShip.tPts.get(3);
            E4 = thisShip.tPts.get(11);

            E2B = thisShip.tPts.get(16);
            E3B = thisShip.tPts.get(17);
        }
        else if(whichOption == frontAuxArcOption && twoPointOh == true){
            A1 = thisShip.tPts.get(8);
            A2 = thisShip.tPts.get(9);

            E1 = thisShip.tPts.get(10);
            E2 = thisShip.tPts.get(11);
        }
        /*
        else if(whichOption == backFullArcOption && twoPointOh == true){
            A1 = thisShip.tPts.get(8);
            A2 = thisShip.tPts.get(9);

            E1 = thisShip.tPts.get(10);
            E2 = thisShip.tPts.get(11);
        }
         */
        else if(whichOption == mobileSideArcOption && twoPointOh == false){
                //left side check
                A1 = thisShip.tPts.get(5);
                A2 = thisShip.tPts.get(0);

                E1 = thisShip.tPts.get(7);
                E2 = thisShip.tPts.get(2);

                //right side check
                A3 = thisShip.tPts.get(1);
                A4 = thisShip.tPts.get(4);

                E3 = thisShip.tPts.get(3);
                E4 = thisShip.tPts.get(6);
        }else if(whichOption == mobileSideArcOption && twoPointOh == true){
            //left side check
            A1 = thisShip.tPts.get(5);
            A2 = thisShip.tPts.get(0);

            E1 = thisShip.tPts.get(7);
            E2 = thisShip.tPts.get(2);

            //right side check
            A3 = thisShip.tPts.get(1);
            A4 = thisShip.tPts.get(4);

            E3 = thisShip.tPts.get(3);
            E4 = thisShip.tPts.get(6);
        }
        else if(whichOption == bullseyeArcOption){
            A1 = thisShip.tPts.get(12);
            E1 = thisShip.tPts.get(13);
            A2 = thisShip.tPts.get(14);
            E2 = thisShip.tPts.get(15);
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
                bigAnnounce += "for the backward auxiliary arc - from ";
                break;
            case frontAuxArcOption:
                if(twoPointOh == false) bigAnnounce += "for the front pair of auxiliary arcs - from ";
                else bigAnnounce += "for the full front arc - from ";
                break;
            case  bullseyeArcOption:
                bigAnnounce += "for the bullseye arc - from ";
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
        if(Double.compare(distaa,dist1) > 0 && Double.compare(distaa,dist2) > 0) {
            return true;
        }
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

        //2nd Closest Defender to 2nd Closest Attacker
        MicLine A2D2 = new MicLine(A2, D2, false);
        lineToVet = vetThisLine(A2D2, "A2D2", 0.5);
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

        MicLine D2AA = createLinePtoAB(D2, AA, false);
        lineToVet = vetThisLine(D2AA, "D2AA", 0.6);
        if(lineToVet != null) {
            lineList.add(lineToVet);
        }

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

    private boolean isEdgeInArc(MicLine theCandidateLine, Point2D.Double leftMostStart, Point2D.Double leftMostEnd, Point2D.Double rightMostStart, Point2D.Double  rightMostEnd)
    {
        double fudgefactor = 0.00001; //open up the arc just slightly to better allow along-the-arc firing lines
        double firstArcEdgePolarAngle = getEdgeAngle(leftMostStart, leftMostEnd) - fudgefactor; //edge most counter-clockwise
        double secondArcEdgePolarAngle = getEdgeAngle(rightMostStart, rightMostEnd) + fudgefactor; //edge most clockwise

        double firstAdjustment = 0.0;
        if(Double.compare(firstArcEdgePolarAngle, 0.0 + fudgefactor) > 0 && Double.compare(firstArcEdgePolarAngle, Math.PI + fudgefactor) < 0) firstAdjustment = -Math.PI;
        firstArcEdgePolarAngle += firstAdjustment;
        secondArcEdgePolarAngle += firstAdjustment;

        double bestLinePolarAngle = getEdgeAngle(theCandidateLine.first, theCandidateLine.second);
        bestLinePolarAngle += firstAdjustment;

        if(Double.compare(secondArcEdgePolarAngle, -Math.PI + fudgefactor) < 0) secondArcEdgePolarAngle += 2.0*Math.PI;
        if(Double.compare(bestLinePolarAngle, -Math.PI + fudgefactor) < 0 ) bestLinePolarAngle += 2.0*Math.PI;

        //logToChat("1: " + Double.toString(firstArcEdgePolarAngle) + " line: " + Double.toString(bestLinePolarAngle) + " 2: " + Double.toString(secondArcEdgePolarAngle));
        if(Double.compare(bestLinePolarAngle, firstArcEdgePolarAngle) < 0 || Double.compare(bestLinePolarAngle, secondArcEdgePolarAngle) > 0)
            return false;
        return true;
    }

    private boolean isEdgeInArcInwardTweak(MicLine theCandidateLine, Point2D.Double leftMostStart, Point2D.Double leftMostEnd, Point2D.Double rightMostStart, Point2D.Double  rightMostEnd)
    {
        double fudgefactor = 0.00001; //open up the arc just slightly to better allow along-the-arc firing lines
        double firstArcEdgePolarAngle = getEdgeAngle(leftMostStart, leftMostEnd) + fudgefactor; //edge most counter-clockwise
        double secondArcEdgePolarAngle = getEdgeAngle(rightMostStart, rightMostEnd) - fudgefactor; //edge most clockwise

        double firstAdjustment = 0.0;
        if(Double.compare(firstArcEdgePolarAngle, 0.0 + fudgefactor/2.0) > 0 && Double.compare(firstArcEdgePolarAngle, Math.PI + fudgefactor/2.0) < 0) firstAdjustment = -Math.PI;
        firstArcEdgePolarAngle += firstAdjustment;
        secondArcEdgePolarAngle += firstAdjustment;

        double bestLinePolarAngle = getEdgeAngle(theCandidateLine.first, theCandidateLine.second);
        bestLinePolarAngle += firstAdjustment;

        if(Double.compare(secondArcEdgePolarAngle, -Math.PI + fudgefactor) < 0) secondArcEdgePolarAngle += 2.0*Math.PI;
        if(Double.compare(bestLinePolarAngle, -Math.PI + fudgefactor) < 0 ) bestLinePolarAngle += 2.0*Math.PI;

        logToChat("1: " + Double.toString(firstArcEdgePolarAngle) + " line: " + Double.toString(bestLinePolarAngle) + " 2: " + Double.toString(secondArcEdgePolarAngle));
        if(Double.compare(bestLinePolarAngle, firstArcEdgePolarAngle) < 0 || Double.compare(bestLinePolarAngle, secondArcEdgePolarAngle) > 0)
            return false;
        return true;
    }

    private double getEdgeAngle(Point2D.Double start, Point2D.Double end) {
        double deltaX = end.x - start.x;
        double deltaY = end.y - start.y;
        if(Double.compare(deltaY, 0.0)== 0
                || (Double.compare(deltaY,0.0)>0 && Double.compare(deltaY, 0.0001)< 0)) deltaY += 0.0001; //avoids the 180,-180 divide

        return Math.atan2(deltaY, deltaX);
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
        D3 = findSegmentCrossPoint(AE,DD, true);
        if(D3 != null) return new MicLine(A,D3, false);
        return null;
    }

    private double getLengthOfAxtoDD(Point2D.Double A, Point2D.Double E, MicLine DD) {
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
        D3 = findSegmentCrossPoint(AE,DD, true);
        if(D3 != null) {
            MicLine temp = new MicLine(A,D3, false);
            return temp.pixelLength;
        }
        return 0.0;
    }

    private double getLengthOfLinePtoAB(Point2D.Double P, Point2D.Double A, Point2D.Double B) {
        Boolean markAsDead = false;

        MicLine AB = new MicLine(A,B, false);
        double x1 = A.getX();
        double y1 = A.getY();
        double x2 = B.getX();
        double y2 = B.getY();
        double xp = P.getX();
        double yp = P.getY();

        //getting the shortest distance in pixels to the line formed by both (x1,y1) and (x2,y2)
        double numerator = Math.abs((xp - x1) * (y2 - y1) - (yp - y1) * (x2 - x1));
        double denominator = Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
        double shortestdist = numerator / denominator;

        MicLine AP = new MicLine(new Point2D.Double(A.x, A.y), P, false);

        double segmentPartialDist = Math.sqrt(Math.pow(AP.pixelLength, 2.0) - Math.pow(shortestdist, 2.0));
        double segmentFullDist = AB.pixelLength;

        //check if the partial distance has point outside AB
        //whichWay = 1: points inward the segment, -1: points outward, line should be marked as dead
        int whichWay = 1;
        if (Double.compare(micLineDotProduct(AB, AP), 0) < 0) {
            whichWay = -1;
            markAsDead = true;
        }


        //if it points inward, it might be too long for its segment, mark it as dead if so.
        if (whichWay == 1 && Double.compare(segmentPartialDist, segmentFullDist) > 0) {
            markAsDead = true;
        }

        //compute the partial vector's components
        double partialX = whichWay * (x2 - x1) / segmentFullDist * segmentPartialDist;
        double partialY = whichWay * (y2 - y1) / segmentFullDist * segmentPartialDist;


        double vector_x = x1 + partialX;
        double vector_y = y1 + partialY;

        //returns A1DD - closest attacker point (vertex or in-arc edge) to an defender's edge point satisfying the 90 degrees shortest distance requirement
        MicLine temp = new MicLine(P, new Point2D.Double(vector_x, vector_y), markAsDead);
        return temp.pixelLength;
    }

    //Using this algorithm to detect if the two segments cross
    //https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
    //restrictOnDD = true wants it on the defender edge. if false, you don't care if it's out.
    private Point2D.Double findSegmentCrossPoint(MicLine A1E1, MicLine DD, Boolean restrictOnDD)
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
        if(Double.compare(intersectionPt.x,-1.0) == 0 && restrictOnDD == true) return null;
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


    private boolean are90degreesAligned(BumpableWithShape thisShip, BumpableWithShape b) {
        int shipAngle = Math.abs((int)thisShip.getAngle());
        int bAngle =  Math.abs((int)b.getAngle());

        while(shipAngle > 89) shipAngle -= 90;
        while(bAngle > 89) bAngle -= 90;
        if(shipAngle == bAngle) return true;
        else return false;
    }

    private Boolean isTargetOutsideofRectangles(BumpableWithShape thisShip, BumpableWithShape targetBWS, boolean wantBoost, boolean wantBoundByFrontAndBackArcs) {
        Shape crossZone = findUnionOfRectangularExtensions(thisShip, wantBoost, wantBoundByFrontAndBackArcs);
        return !shapesOverlap(crossZone, targetBWS.getRectWithNoNubs());
    }

    private Boolean isTargetInsideofRectangles(BumpableWithShape thisShip, BumpableWithShape targetBWS, boolean wantBoost, boolean wantBoundByFrontAndBackArcs) {

        Shape crossZone = findUnionOfRectangularExtensions(thisShip, wantBoost, wantBoundByFrontAndBackArcs);

        return shapesOverlap(crossZone, targetBWS.getRectWithNoNubs());
    }

    private Boolean isTargetInsideofFrontRectangle(BumpableWithShape thisShip, BumpableWithShape targetBWS, boolean wantBoost) {
        Shape crossZone = findFrontRectangularExtension(thisShip, wantBoost);

        return shapesOverlap(crossZone, targetBWS.getRectWithNoNubs());
    }

    private Boolean isTargetInsideofBackRectangle(BumpableWithShape thisShip, BumpableWithShape targetBWS, boolean wantBoost) {
        Shape crossZone = findBackRectangularExtension(thisShip, wantBoost);

        return shapesOverlap(crossZone, targetBWS.getRectWithNoNubs());
    }
    private Boolean isTargetInsideofRightRectangle(BumpableWithShape thisShip, BumpableWithShape targetBWS, boolean wantBoost) {
        Shape crossZone = findRightRectangularExtension(thisShip, wantBoost);

        return shapesOverlap(crossZone, targetBWS.getRectWithNoNubs());
    }
    private Boolean isTargetInsideofRightFrontBackRectangle(BumpableWithShape thisShip, BumpableWithShape targetBWS, boolean wantBoost) {
        Shape crossZone = findRightFrontBackRectangularExtension(thisShip, wantBoost);

        return shapesOverlap(crossZone, targetBWS.getRectWithNoNubs());
    }
    private Boolean isTargetInsideofLeftRectangle(BumpableWithShape thisShip, BumpableWithShape targetBWS, boolean wantBoost) {
        Shape crossZone = findLeftRectangularExtension(thisShip, wantBoost);

        return shapesOverlap(crossZone, targetBWS.getRectWithNoNubs());
    }
    private Boolean isTargetInsideofLeftFrontBackRectangle(BumpableWithShape thisShip, BumpableWithShape targetBWS, boolean wantBoost) {
        Shape crossZone = findLeftFrontBackRectangularExtension(thisShip, wantBoost);

        return shapesOverlap(crossZone, targetBWS.getRectWithNoNubs());
    }

    //only accessible if frontaux arc and turret shots anyway
    private Boolean isTargetInsideWeirdCase(BumpableWithShape thisShip, BumpableWithShape targetBWS){
        Shape rects = new Rectangle2D.Double(-9999.0,-9999.0,0.0,0.0);//ugly null avoidance

        if(whichOption == frontAuxArcOption) rects = findDualRects(thisShip);
        else if(whichOption == mobileSideArcOption){
            switch(getMobileEdge()){
                case 1:
                    rects = findDualRects(thisShip);
                    break;
                case 2:
                    rects = findSoloRect(thisShip, true);
                    break;
                case 3:
                    rects = findDualRects(thisShip);
                    break;
                case 4:
                    rects = findSoloRect(thisShip, false);
                    break;
            }
        }
        //fov.shapes.add(dualRects);
        return shapesOverlap(rects, targetBWS.getRectWithNoNubs());
    }

    //only used to find the corner to firing arc edges during mobile turret shots
    //only used if the mobile is facing right (then, grab front right, back right and front left rects)
    //or mobile is facing left (then, grab front left, back left and front right)
    private Shape findSoloRect(BumpableWithShape b, Boolean wantRightOtherwiseLeft){
        double workingWidth = b.getChassisWidth();
        double workingHeight = b.getChassisHeight();
        double cornerToArc = b.getChassis().getCornerToFiringArc();

        double boost = 30.0;

        Area zone = new Area(new Rectangle2D.Double(0.0,0.0,0.0,0.0));



        if(wantRightOtherwiseLeft==true){
            Shape right = new Rectangle2D.Double(workingWidth/2.0 - cornerToArc, -boost*RANGE3 - workingHeight/2.0, cornerToArc, RANGE3*boost);
            zone = new Area(right);
            Shape backRight = new Rectangle2D.Double(workingWidth / 2.0 - cornerToArc, workingHeight / 2.0, cornerToArc, RANGE3 * boost);
            zone.add(new Area(backRight));
        }
        else{
            Shape left = new Rectangle2D.Double(-workingWidth/2.0, -boost*RANGE3 - workingHeight/2.0, cornerToArc, RANGE3*boost);
            zone.add(new Area(left));
            Shape backLeft = new Rectangle2D.Double(-workingWidth / 2.0,   workingHeight / 2.0, cornerToArc, RANGE3 * boost);
            zone.add(new Area(backLeft));
        }

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

    private Shape findDualRects(BumpableWithShape b) {
        double workingWidth = b.getChassisWidth();
        double workingHeight = b.getChassisHeight();
        double cornerToArc = b.getChassis().getCornerToFiringArc();

        double boost = 30.0;

        Shape left = new Rectangle2D.Double(-workingWidth/2.0, -boost*RANGE3 - workingHeight/2.0, cornerToArc, RANGE3*boost);
        Shape right = new Rectangle2D.Double(workingWidth/2.0 - cornerToArc, -boost*RANGE3 - workingHeight/2.0, cornerToArc, RANGE3*boost);


        Area zone = new Area(left);
        zone.add(new Area(right));


        if(whichOption == mobileSideArcOption) {
            Shape backLeft = new Rectangle2D.Double(-workingWidth / 2.0,   workingHeight / 2.0, cornerToArc, RANGE3 * boost);
            Shape backRight = new Rectangle2D.Double(workingWidth / 2.0 - cornerToArc, workingHeight / 2.0, cornerToArc, RANGE3 * boost);
            zone.add(new Area(backLeft));
            zone.add(new Area(backRight));
        }



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
                bumpables.add(new BumpableWithShape((Decorator)piece, "Asteroid", "2".equals(testFlipString), false));
            } else if (piece.getState().contains("this_is_a_debris")) {
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece,"Debris","2".equals(testFlipString), false));
            }
        }
        return bumpables;
    }

    private Shape findInBetweenRectangle(BumpableWithShape atk, BumpableWithShape def, double wantedWidth, int chosenOption) {
        //this fishes out the rectangular shape of a multiple attack line scenario (band), bounded by arc lines in the case
        //of a front or back shot, including aux front arcs when the front has to be tested, or mobile turret shots when the front is tested.
        //testing out the triangles at the corners of front aux and mobile shots is done elsewhere, controlled in the else statement of the main keyEvent dealer method
        double chassisHeight = atk.getChassisHeight();
        double chassisWidth = atk.getChassisWidth();


        double centerX = atk.bumpable.getPosition().getX();
        double centerY = atk.bumpable.getPosition().getY();
        Shape testShape = null;
        if(chosenOption == frontArcOption || chosenOption == bullseyeArcOption) { //front only, includes the wantedWidth restricted M12-L bullseye rect
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
                if(shapesOverlap(transformed, def.getRectWithNoNubs())) return transformed;
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

            ArrayList<Shape> keptTransformedlistShape = new ArrayList<Shape>();
            for(Shape s : listShape){
                Shape transformed = transformRectShapeForBestLines(atk, def, s, centerX, centerY);
                if(shapesOverlap(transformed, def.getRectWithNoNubs())) keptTransformedlistShape.add(transformed);
            }
            Area fusion = new Area();
            for(Shape s : keptTransformedlistShape){
                fusion.add(new Area(s));
            }
            return fusion;
        }
        if(chosenOption == mobileSideArcOption) { //Lancer-Class and now many more in 2.0
            //preferably, if the mobile side is 1, this should not lead to a situation where you get a front band through normal ways and then a second one through here. filter out this situation before it happens
            int mobileSide = getMobileEdge();

            ArrayList<Shape> listShape = new ArrayList<Shape>();
            if(mobileSide == 2 || mobileSide == 6) listShape.add(new Rectangle2D.Double(chassisWidth/2.0, -chassisHeight/2.0, RANGE3, chassisHeight)); //right or rightleft
            if(mobileSide == 3 || mobileSide == 5) listShape.add(new Rectangle2D.Double(-wantedWidth/2.0, chassisHeight/2.0, wantedWidth, RANGE3)); //back or frontback
            if(mobileSide == 4 || mobileSide == 6) listShape.add(new Rectangle2D.Double(-chassisWidth/2.0 - RANGE3, -chassisHeight/2.0, RANGE3, chassisHeight)); //left or rightleft

            ArrayList<Shape> keptTransformedlistShape = new ArrayList<Shape>();
            for(Shape s : listShape){
                Shape transformed = transformRectShapeForBestLines(atk, def, s, centerX, centerY);
                if(shapesOverlap(transformed, def.getRectWithNoNubs())) keptTransformedlistShape.add(transformed);
            }
            Area fusion = new Area();
            for(Shape s : keptTransformedlistShape){
                fusion.add(new Area(s));
            }
            return fusion;
        }
        //common to all options except turret/TL shots
        Shape tShape = transformRectShapeForBestLines(atk, def, testShape, centerX, centerY);
        if(tShape !=null) if(shapesOverlap(tShape, def.getRectWithNoNubs())) return tShape;
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

    //dedicated for front facing mobile turret, no use checking other options
    private Shape findFrontRectangularExtension(BumpableWithShape b, boolean superLong) {
        Shape rawShape = BumpableWithShape.getRawShape(b.bumpable);
        double workingWidth = b.getChassisWidth() - b.getChassis().getCornerToFiringArc()*2.0;
        double workingHeight = b.getChassisHeight();

        double boost = 1.0f;
        if(superLong) boost = 30.0f;

        Shape front = new Rectangle2D.Double(-workingWidth/2.0, -boost*RANGE3 - workingHeight/2.0, workingWidth, RANGE3*boost);

        //by default, get everything for turret/TL shots
        Area zone = new Area(front);
        zone.subtract(new Area(rawShape));

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

    //dedicated for back facing mobile turret, no use checking other options
    private Shape findBackRectangularExtension(BumpableWithShape b, boolean superLong) {
        Shape rawShape = BumpableWithShape.getRawShape(b.bumpable);
        double workingWidth = b.getChassisWidth() - b.getChassis().getCornerToFiringArc()*2.0;
        double workingHeight = b.getChassisHeight();

        double boost = 1.0f;
        if(superLong) boost = 30.0f;

        Shape back = new Rectangle2D.Double(-workingWidth/2.0, workingHeight/2.0, workingWidth, boost*RANGE3);

        //by default, get everything for turret/TL shots
        Area zone = new Area(back);
        zone.subtract(new Area(rawShape));

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
    //dedicated for right facing mobile turret, no use checking other options
    private Shape findRightRectangularExtension(BumpableWithShape b, boolean superLong) {
        Shape rawShape = BumpableWithShape.getRawShape(b.bumpable);
        double chassisWidth = b.getChassisWidth();
        double workingWidth = b.getChassisWidth() - b.getChassis().getCornerToFiringArc()*2.0;
        double workingHeight = b.getChassisHeight();

        double boost = 1.0f;
        if(superLong) boost = 30.0f;

        Shape right = new Rectangle2D.Double(chassisWidth/2.0, -workingHeight/2.0, boost*RANGE3, workingHeight);

        //by default, get everything for turret/TL shots
        Area zone = new Area(right);

        zone.subtract(new Area(rawShape));

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
    //dedicated for right facing mobile turret, no use checking other options
    private Shape findRightFrontBackRectangularExtension(BumpableWithShape b, boolean superLong) {
        Shape rawShape = BumpableWithShape.getRawShape(b.bumpable);
        double chassisWidth = b.getChassisWidth();
        double workingWidth = b.getChassisWidth() - b.getChassis().getCornerToFiringArc()*2.0;
        double workingHeight = b.getChassisHeight();

        double boost = 1.0f;
        if(superLong) boost = 30.0f;

        Shape rightFront = new Rectangle2D.Double(workingWidth/2.0,-workingHeight/2.0-boost*RANGE3, (chassisWidth - workingWidth)/2.0,boost*RANGE3);
        Shape rightBack = new Rectangle2D.Double(workingWidth/2.0,workingHeight/2.0, (chassisWidth - workingWidth)/2.0,boost*RANGE3);

        //by default, get everything for turret/TL shots
        Area zone = new Area(rightFront);
        zone.add(new Area(rightBack));

        zone.subtract(new Area(rawShape));

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
    private Shape findLeftFrontBackRectangularExtension(BumpableWithShape b, boolean superLong) {
        Shape rawShape = BumpableWithShape.getRawShape(b.bumpable);
        double chassisWidth = b.getChassisWidth();
        double workingWidth = b.getChassisWidth() - b.getChassis().getCornerToFiringArc()*2.0;
        double workingHeight = b.getChassisHeight();

        double boost = 1.0f;
        if(superLong) boost = 30.0f;

        Shape leftFront = new Rectangle2D.Double(-chassisWidth/2.0,-workingHeight/2.0-boost*RANGE3, (chassisWidth - workingWidth)/2.0,boost*RANGE3);
        Shape leftBack = new Rectangle2D.Double(-chassisWidth/2.0,workingHeight/2.0, (chassisWidth - workingWidth)/2.0,boost*RANGE3);

        //by default, get everything for turret/TL shots
        Area zone = new Area(leftFront);
        zone.add(new Area(leftBack));

        zone.subtract(new Area(rawShape));

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
    //dedicated for left facing mobile turret, no use checking other options
    private Shape findLeftRectangularExtension(BumpableWithShape b, boolean superLong) {
        Shape rawShape = BumpableWithShape.getRawShape(b.bumpable);
        double chassisWidth = b.getChassisWidth();
        double workingWidth = b.getChassisWidth() - b.getChassis().getCornerToFiringArc()*2.0;
        double workingHeight = b.getChassisHeight();

        double boost = 1.0f;
        if(superLong) boost = 30.0f;

        Shape left = new Rectangle2D.Double(-chassisWidth/2.0 - boost*RANGE3, -workingHeight/2.0, boost*RANGE3, workingHeight);

        //by default, get everything for turret/TL shots
        Area zone = new Area(left);

        zone.subtract(new Area(rawShape));

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
    private Shape findUnionOfRectangularExtensions(BumpableWithShape b, boolean superLong, boolean wantBoundByFrontAndBackArcs) {
        Shape rawShape = BumpableWithShape.getRawShape(b.bumpable);
        double chassisWidth = b.getChassisWidth();
        double workingWidth = b.getChassisWidth();
        double workingHeight = b.getChassisHeight();

        int whichMobileSide = getMobileEdge();

        //restrict the width of the check only if you're dealing with front or back arcs. Front aux arcs might need the full width of the front, same for mobile turrets

        //only modify the width of the rectangles in the front and back if requested
        if(wantBoundByFrontAndBackArcs == true){
            if(whichOption == frontArcOption || whichOption == backArcOption || whichOption == mobileSideArcOption) workingWidth = b.getChassisWidth() - b.getChassis().getCornerToFiringArc()*2.0;
            if(whichOption == bullseyeArcOption) workingWidth = b.chassis.getBullsEyeWidth();
        }


        double boost = 1.0f;
        if(superLong) boost = 30.0f;
        Shape frontBack = new Rectangle2D.Double(-workingWidth/2.0, -boost*RANGE3 - workingHeight/2.0, workingWidth, 2.0*RANGE3*boost + workingHeight);
        Shape front = new Rectangle2D.Double(-workingWidth/2.0, -boost*RANGE3 - workingHeight/2.0, workingWidth, RANGE3*boost);
        Shape back = new Rectangle2D.Double(-workingWidth/2.0, workingHeight/2.0, workingWidth, boost*RANGE3);
        Shape leftRight = new Rectangle2D.Double(-chassisWidth/2.0 - boost*RANGE3, -workingHeight/2.0, 2.0*boost*RANGE3+chassisWidth, workingHeight);
        Shape right = new Rectangle2D.Double(chassisWidth/2.0, -workingHeight/2.0, boost*RANGE3, workingHeight);
        Shape left = new Rectangle2D.Double(-chassisWidth/2.0 - boost*RANGE3, -workingHeight/2.0, boost*RANGE3, workingHeight);

        //half height reduced side rectangles for Auzituck/YV-666 arcs
        Shape leftRight_reduced = new Rectangle2D.Double(-chassisWidth/2.0 - boost*RANGE3, -workingHeight/2.0, 2.0*boost*RANGE3+chassisWidth, workingHeight/2.0);

        //by default, get everything for turret/TL shots
        Area zone = new Area();
        switch(whichOption){
            case frontArcOption:
                zone = new Area(front);
                break;
            case backArcOption:
                zone = new Area(back);
                break;
            case frontAuxArcOption:
                zone = new Area(front);
                zone.add(new Area(leftRight_reduced));
                break;
            case mobileSideArcOption:
                zone = new Area(frontBack);
                zone.add(new Area(leftRight));
               /* if(whichMobileSide==2) zone.add(new Area(right));
                if(whichMobileSide==3) zone.add(new Area(back));
                if(whichMobileSide==4) zone.add(new Area(left));*/
                break;
            case bullseyeArcOption:
                zone = new Area(front);
                break;
            case turretArcOption:
                zone = new Area(frontBack);
                zone.add(new Area(leftRight));
                break;
        }

        zone.subtract(new Area(rawShape));

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
                        piece.getProperty("Pilot Name").toString(), piece.getProperty("Craft ID #").toString(),
                        piece.getState().contains("this_is_2pointoh")));
            }
        }
        return ships;
    }

    public static class ShapeWithText implements Serializable {
        public String rangeString = "Range ";
        public int rangeLength = 0;
        public Shape shape;
        public double x;
        public double y;
        //angle should be the calling detection angle for the multiple best line case, but add an extra angle to account for
        //if it's the right, left or bottom band required.
        public double angle = 0.0;

        ShapeWithText(Shape shape, double angle) {
            this.shape = shape;
            this.angle = angle;
            rangeLength = (int)Math.ceil(((double)calculateRange()/282.5));
            rangeString += Integer.toString(rangeLength);
            this.x = shape.getBounds().getCenterX();
            this.y = shape.getBounds().getCenterY();
        }

        private int calculateRange() {
            //unrotate the shape so we can get the range through its width
            Shape temp = AffineTransform
                    .getRotateInstance(-angle, this.x, this.y)
                    .createTransformedShape(this.shape);

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
