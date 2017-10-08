package mic;

import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mic.manuvers.ManeuverPaths;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
import java.util.Map;

import static mic.Util.*;

/**
 * Created by Mic on 23/03/2017.
 */


public class AutoRangeFinder extends Decorator implements EditablePiece {
    public static final String ID = "auto-range-finder";

    private final FreeRotator testRotator;

    private ShipPositionState prevPosition = null;
    private ManeuverPaths lastManeuver = null;
    private FreeRotator myRotator = null;
    private FOVisualization fov = null;
    private static Map<String, Integer> keyStrokeToOptions = ImmutableMap.<String, Integer>builder()
            .put("CTRL SHIFT F", 1) //primary arc
            .put("CTRL SHIFT L", 2) //turret/TL
            //.put("CTRL SHIFT N", 3) //front pairs of aux arc (YV-666, Auzituck)
            //.put("CTRL SHIFT V", 4) //back aux arc
            //.put("ALT SHIFT V", 5) //mobile turret arc, must detect which one is selected on the ship
            .build();
    private static double RANGE1 = 282.5;
    private static double RANGE2 = 565;
    private static double RANGE3 = 847.5;
    int whichOption = -1; //which autorange option. See the Map above. -1 if none is selected.
    String bigAnnounce = "";
    BumpableWithShape thisShip; //ship's full combined name string
    ArrayList<rangeFindings> rfindings = new ArrayList<rangeFindings>(); //findings compiled here
    Point2D.Double A1, A2; //attacker ship's best points
    ArrayList<Point2D.Double> edges = new ArrayList<Point2D.Double>();
    // Assuming an upward facing ship,
    // firing arc edges used to restrict best lines if they are crossing (unused for turret/TL. element 1&3 (leftmost) and element 2&4 (rightmost).
    // 5&7, rightmost front aux arc for Auzituck/hound's tooth, left edge. 6&8 rightmost front aux arc for Auz/Hound's, right edge


    public AutoRangeFinder() {
        this(null);
    }

    public AutoRangeFinder(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
        fov = new FOVisualization();
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

    private void clearVisu() {
        getMap().removeDrawComponent(this.fov);
        this.fov.shapes.clear();
        this.fov.lines.clear();
        this.fov.shapesWithText.clear();
    }

    private int getKeystrokeToOptions(KeyStroke keyStroke) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToOptions.containsKey(hotKey)) {
            return keyStrokeToOptions.get(hotKey).intValue();
        }
        return -1;
    }

    public Command keyEvent(KeyStroke stroke) {
        if (this.fov == null) {
            this.fov = new FOVisualization();
        }
        //identify which autorange option was used by using the static Map defined above in the globals, store it in an int
        whichOption = getKeystrokeToOptions(stroke);
        if (whichOption != -1 && stroke.isOnKeyRelease() == false) {
            Command bigCommand = piece.keyEvent(stroke);
            //if the firing options were already activated, remove the visuals and exit right away
            if (this.fov != null && this.fov.getCount() > 0) {
                clearVisu();
                return bigCommand;
            }
            thisShip = new BumpableWithShape(this, "Ship",
                    this.getInner().getProperty("Pilot Name").toString(), this.getInner().getProperty("Craft ID #").toString());

            //Prepare the start of the appropriate chat announcement string - which ship are we doing this from, which kind of autorange
            prepAnnouncementStart();

            //Loop over every other target ship
            List<BumpableWithShape> BWS = getOtherShipsOnMap();
            for (BumpableWithShape b : BWS) {
                //Preliminary check, eliminate attempt to calculate this if the target is overlapping the attacker, could lead to exception error
                if (shapesOverlap(thisShip.shape, b.shape)) continue;

                //Do everything method here:
                figureOutAutoRange(b);
            }

            //draw visuals and announce the results in the chat
            getMap().addDrawComponent(fov);
            Command bigAnnounceCommand = makeBigAnnounceCommand(bigAnnounce, rfindings);
            bigCommand.append(bigAnnounceCommand);
            return bigCommand;
        } else if (KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK, false).equals(stroke)) {
            if (this.fov != null && this.fov.getCount() > 0) clearVisu();
        }
        return piece.keyEvent(stroke);
    }


    private void figureOutAutoRange(BumpableWithShape b) {
        //Figure out which best 2 points will be used on the attacker, A1 and A2

        if (whichOption != 2)
            edges = thisShip.getFiringArcEdges(whichOption,3); //TO DO: take into account huge ships eventually; not needed if you're checking turret/TL
        findAttackerBestPoints(b);

        Point2D.Double D1 = findClosestVertex(b, thisShip);
        Point2D.Double D2 = find2ndClosestVertex(b, thisShip);

        //Extremity of arc edges, used for angle checks. They are only used if an in-arc check will be performed
        Point2D.Double E1 = null;
        Point2D.Double E2 = null;
        if (whichOption != 2)
        {
            E1 = edges.get(2); //associated with A1 as the edge A1E1
            E2 = edges.get(3); //associated with A2 as the edge A2E2
        }

        //TO DO inside findBestLine: special case, D1 and D2 might not be a vertex on the target, but instead an intermediate point on some edge of the ship.
        micLine bestLine = null;
        if(whichOption == 2) bestLine = findBestLine(A1, A2, D1, D2, 3);
        else bestLine = findBestArcRestrictedLine(A1, A2, D1, D2, E1, E2, 3);
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
            if(whichOption == 2) wantedWidth = thisShip.getChassisWidth();
            else wantedWidth = thisShip.getChassisWidth() - thisShip.chassis.getCornerToFiringArc()*2.0;

            Shape fromShip = findInBetweenRectangle(thisShip, b, wantedWidth);
            Shape fromTarget = findInBetweenRectangle(b, thisShip, b.getChassisWidth());

            if(fromShip == null) return;
            Area a1 = new Area(fromShip);
            Area a2 = new Area(fromTarget);
            a1.intersect(a2);

            double extra = getExtraAngleDuringRectDetection(thisShip, b);
            ShapeWithText bestBand = new ShapeWithText(a1, thisShip.getAngleInRadians() + extra);
            rfindings.add(found);
            fov.addShapeWithText(bestBand);
        }

    }

    private void prepAnnouncementEnd() {
    }

    private void findAttackerBestPoints(BumpableWithShape b) {

        switch (whichOption) {
            case 1:
            case 3:
                A1 = edges.get(0);
                A2 = edges.get(1);
                break;
            case 2:
                A1 = findClosestVertex(thisShip, b);
                A2 = find2ndClosestVertex(thisShip, b);
                break;
            default:
                break;
        }
    }

    private void prepAnnouncementStart() {
        //prepares the initial part of the chat window string that announces the firing options
        bigAnnounce = "*** Firing Options ";

        switch (whichOption) {
            case 1:
                bigAnnounce += "for the primary arc - from ";
                break;
            case 2:
                bigAnnounce += "for Target Lock/Turrets - from ";
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



    private micLine findBestArcRestrictedLine(Point2D.Double A1, Point2D.Double A2, Point2D.Double D1, Point2D.Double D2, Point2D.Double E1, Point2D.Double E2, int rangeInt) {
        ArrayList<micLine> lineList = new ArrayList<micLine>();

        //Closest Attacker to Closest Defender
        micLine A1D1 = new micLine(A1, D1);
        //Closest Attacker to 2nd Closest Defender
        micLine A1D2 = new micLine(A1, D2);
        //Closest Defender to Closest Attacker
        micLine D1A1 = new micLine(D1, A1);
        //Closest Defender to 2nd Closest Attacker
        micLine A2D1 = new micLine(A2, D1);
        //Closest Defender to 2nd Closest Attacker
        micLine D1A2 = new micLine(A2, D1);
        //Closest Attacker Edge
        micLine AA = new micLine(A1, A2);
        //Closest Defender Edge
        micLine DD = new micLine(D1, D2);

        lineList.add(A1D1);
        lineList.add(A1D2);
        lineList.add(A2D1);

        //Closest attacker's point to the defender's closest vertex
        micLine A1DD = createLineA1toDD(A1D1, A1D2, DD);
        lineList.add(A1DD);

        micLine A1DD_arc_restricted = createLineA1toDD(A1D1, A1D2, DD);
        micLine A2DD_arc_restricted = createLineA1toDD(A2D1, A2D1, DD);

        //Attacker's edge to defender's closest vertex
        micLine D1AA = createLineAAtoD1(A1D1, D1A2, AA, D1);
        if(doesAAforInArcPassTest(D1AA, AA)== true)lineList.add(D1AA);


        ArrayList<micLine> filteredList = new ArrayList<micLine>();
        //Filter out shots that aren't in-arc if the turret option is not chosen
        for(micLine l: lineList)
        {
            if(isEdgeInArc(A1, A2, l, E1, E2) == true) filteredList.add(l);
        }

        ArrayList<micLine> usedList = new ArrayList<micLine>();

        if(whichOption == 2) usedList = (ArrayList<micLine>)lineList.clone();
        else  usedList = (ArrayList<micLine>) filteredList.clone();

        //First criterium, find the best distance and make it the best Line
        double bestDist = rangeInt * 282.5;
        micLine best = null;
        for (micLine l : usedList) {
            if (Double.compare(bestDist, l.pixelLength) > 0) {
                bestDist = l.pixelLength;
                best = l;
            }
        }
        //nothing under the requested range was found, no best lines can be submitted
        if (best == null) {
            //logToChatCommand("did not find a best line");
            return null;
        }

        return best;
    }

    private boolean doesAAforInArcPassTest(micLine D1AA, micLine AA) {
        double x1 = AA.first.x;
        double y1 = AA.first.y;
        double x2 = AA.second.x;
        double y2 = AA.second.y;
        //Get the midpoint inside the firing arc, on the attacker
        double xp = D1AA.second.x;
        double yp = D1AA.second.y;

        double dist1 = Math.sqrt(Math.pow(xp - x1, 2.0) + Math.pow(yp - y1, 2.0));
        double dist2 = Math.sqrt(Math.pow(xp - x2, 2.0) + Math.pow(yp - y2, 2.0));
        double distaa = Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
//is the point used in D1AA between A1 and A2?
        if(Double.compare(distaa,dist1) > 0 && Double.compare(distaa,dist2) > 0) return true;
        return false;
    }


    //this finds the line that links the attacker ship to someplace on the line formed by the closest edge of the target, using
    //the closest and 2nd closest lines to the target's vertices.
    //using an algorithm based on this: http://www.ahristov.com/tutorial/geometry-games/point-line-distance.html
    private micLine findBestLine(Point2D.Double A1, Point2D.Double A2, Point2D.Double D1, Point2D.Double D2, int rangeInt) {

        ArrayList<micLine> lineList = new ArrayList<micLine>();

        //Closest Attacker to Closest Defender
        micLine A1D1 = new micLine(A1, D1);
        //Closest Attacker to 2nd Closest Defender
        micLine A1D2 = new micLine(A1, D2);
        //Closest Defender to 2nd Closest Attacker
        micLine A2D1 = new micLine(A2, D1);
        //Closest Attacker Edge
        micLine AA = new micLine(A1, A2);
        //Closest Defender Edge
        micLine DD = new micLine(D1, D2);

        lineList.add(A1D1);
        lineList.add(A1D2);
        lineList.add(A2D1);

        micLine A1DD = createLineA1toDD(A1D1, A1D2, DD);
        lineList.add(A1DD);

        micLine D1AA = createLineAAtoD1(A1D1, A2D1, AA, D1);
        lineList.add(D1AA);

        //First criterium, find the best distance and make it the best Line
        double bestDist = rangeInt*282.5;
        micLine best = null;
        for(micLine l : lineList){
            if(Double.compare(bestDist,l.pixelLength) > 0) {
                bestDist = l.pixelLength;
                best = l;
            }
        }
        //nothing under the requested range was found, no best lines can be submitted
        if(best==null) return null;

        return best;
    }

    private boolean isEdgeInArc(Point2D.Double A1, Point2D.Double A2, micLine theCandidateLine, Point2D.Double E1, Point2D.Double E2)
    {
        double firstArcEdgePolarAngle = getEdgeAngle(A1, E1);
        double secondArcEdgePolarAngle = getEdgeAngle(A2, E2);
        double bestLinePolarAngle = getEdgeAngle(theCandidateLine.first, theCandidateLine.second);
        logToChatCommand("arc edge left at " + Double.toString(firstArcEdgePolarAngle) + " bestline at " + Double.toString(bestLinePolarAngle) + " arc edge right at " + Double.toString(secondArcEdgePolarAngle));
        if (bestLinePolarAngle < firstArcEdgePolarAngle || bestLinePolarAngle > secondArcEdgePolarAngle)
            return false;
        return true;
    }

    private double getEdgeAngle(Point2D.Double start, Point2D.Double end) {
        double deltaX = end.x - start.x;
        double deltaY = end.y - start.y;

        //returns a polar angle in rad, keeps it positive for easy ordering (might not be necessary)
        return Math.atan2(deltaY, deltaX) + Math.PI;
    }

    private micLine createLineA1toDD(micLine A1D1, micLine A1D2, micLine DD) {
        //getting D1 again
        double x1 = A1D1.second.getX();
        double y1 = A1D1.second.getY();
        //getting D2 again
        double x2 = A1D2.second.getX();
        double y2 = A1D2.second.getY();
        //getting A1 again
        double xp = A1D1.first.getX();
        double yp = A1D1.first.getY();

        //getting the shortest distance in pixels to the line formed by both (x1,y1) and (x2,y2)
        double numerator = Math.abs((xp - x1) * (y2 - y1) - (yp - y1) * (x2 - x1));
        double denominator = Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
        double shortestdist = numerator / denominator;

        double segmentPartialDist = Math.sqrt(Math.pow(A1D1.pixelLength,2.0) - Math.pow(shortestdist,2.0));
        double segmentFullDist = DD.pixelLength;
        double gapx = (x2-x1)/segmentFullDist*segmentPartialDist;
        double gapy = (y2-y1)/segmentFullDist*segmentPartialDist;


        double vector_x = x1 + gapx;
        double vector_y = y1 + gapy;

        //returns A1DD - closest attacker point (vertex or in-arc edge) to an defender's edge point satisfying the 90 degrees shortest distance requirement
        return new micLine(A1, new Point2D.Double(vector_x, vector_y));
    }

    private micLine createLineA1toDD_arc_restricted(micLine A1D1, micLine A1D2, micLine DD, Point2D.Double E1, Point2D.Double E2) {
        //getting D1 again
        double x1 = A1D1.second.getX();
        double y1 = A1D1.second.getY();
        //getting D2 again
        double x2 = A1D2.second.getX();
        double y2 = A1D2.second.getY();
        //getting A1 again
        double xp = A1D1.first.getX();
        double yp = A1D1.first.getY();

        micLine A1E1 = new micLine(A1, E1);
        micLine A2E2 = new micLine(A2, E2);

        if(areMicLinesCrossing(A1E1, DD) == true);
        else if(areMicLinesCrossing(A2E2, DD) == true);

        //getting the shortest distance in pixels to the line formed by both (x1,y1) and (x2,y2)
        double numerator = Math.abs((xp - x1) * (y2 - y1) - (yp - y1) * (x2 - x1));
        double denominator = Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
        double shortestdist = numerator / denominator;

        double segmentPartialDist = Math.sqrt(Math.pow(A1D1.pixelLength,2.0) - Math.pow(shortestdist,2.0));
        double segmentFullDist = DD.pixelLength;
        double gapx = (x2-x1)/segmentFullDist*segmentPartialDist;
        double gapy = (y2-y1)/segmentFullDist*segmentPartialDist;


        double vector_x = x1 + gapx;
        double vector_y = y1 + gapy;

        //returns A1DD - closest attacker point (vertex or in-arc edge) to an defender's edge point satisfying the 90 degrees shortest distance requirement
        return new micLine(A1, new Point2D.Double(vector_x, vector_y));
    }

    //Using this algorithm to detect if the two segments cross
    //https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
    private boolean areMicLinesCrossing(micLine A1E1, micLine DD) {
        micLine p = A1E1;

        micLine q = DD;
    }

    private Point2D.Double findSegmentCrossPoint(micLine A1E1, micLine DD)
    {
        Point2D.Double intersectionPt = null;
        double px = A1E1.first.x;
        double py = A1E1.first.y;

        double qx = DD.first.x;
        double qy = DD.first.y;
        

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


    private micLine createLineAAtoD1(micLine A1D1, micLine A2D1, micLine AA, Point2D.Double D1)
    {
        //Find A1 again
        double x1 = A1D1.first.getX();
        double y1 = A1D1.first.getY();
        //Find A2 again
        double x2 = A2D1.first.getX();
        double y2 = A2D1.first.getY();
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
        return new micLine(new Point2D.Double(vector_x,vector_y), D1);
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

    private Shape findInBetweenRectangle(BumpableWithShape atk, BumpableWithShape def, double wantedWidth) {
        double chassisHeight = atk.getChassisHeight();
        double chassisWidth = atk.getChassisWidth();
        Shape front = new Rectangle2D.Double(-wantedWidth/2.0, -RANGE3 - chassisHeight/2.0, wantedWidth, RANGE3);
        Shape back = new Rectangle2D.Double(-wantedWidth/2.0, chassisHeight/2.0, wantedWidth, RANGE3);

        Shape left = new Rectangle2D.Double(-chassisWidth/2.0 - RANGE3, -chassisHeight/2.0, RANGE3, chassisHeight);
        Shape right = new Rectangle2D.Double(chassisWidth/2.0, -chassisHeight/2.0, RANGE3, chassisHeight);

        ArrayList<Shape> listShape = new ArrayList<Shape>();
        listShape.add(front);
        listShape.add(back);
        listShape.add(left);
        listShape.add(right);

        double centerX = atk.bumpable.getPosition().getX();
        double centerY = atk.bumpable.getPosition().getY();

        for(Shape s : listShape){

            Shape transformed = AffineTransform
                    .getTranslateInstance(centerX, centerY)
                    .createTransformedShape(s);

            transformed = AffineTransform
                    .getRotateInstance(atk.getAngleInRadians(), centerX, centerY)
                    .createTransformedShape(transformed);
            if(shapesOverlap(transformed, def.shape)) return transformed;
        }
        return null;
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

        Shape leftRight = new Rectangle2D.Double(-chassisWidth/2.0 - boost*RANGE3, -workingHeight/2.0, 2.0*boost*RANGE3+chassisWidth, workingWidth);

        Area zone = new Area(frontBack);
        zone.add(new Area(leftRight));
        zone.exclusiveOr(new Area(rawShape));

        if(whichOption!=2) zone = new Area(front);

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

    private static class FOVisualization implements Drawable {

        private final List<Shape> shapes;
        private final List<ShapeWithText> shapesWithText;
        private final List<micLine> lines;

        public Color badLineColor = new Color(30,30,255,110);
        public Color bestLineColor = new Color(45,200,190,255);
        public Color shipsObstaclesColor = new Color(255,99,71, 150);

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
            Iterator<Shape> it = this.shapes.iterator();
            while(it.hasNext()) {
                count++;
                it.next();
            }
            Iterator<ShapeWithText> it2 = this.shapesWithText.iterator();
            while(it2.hasNext()){
                count++;
                it2.next();
            }
            Iterator<micLine> it3 = this.lines.iterator();
            while(it3.hasNext()){
                count++;
                it3.next();
            }
            return count;
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
            for(micLine line : lines){
                if(line.isBestLine == true) graphics2D.setColor(bestLineColor);
                else graphics2D.setColor(badLineColor);

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

        public boolean drawAboveCounters() {
            return true;
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
        public String rangeString = "Range ";
        public double pixelLength = 0.0f;
        public int rangeLength = 0;
        public int centerX, centerY;
        public Point2D.Double first, second;
        public Line2D.Double line = null;

    micLine(int x1, int y1, int x2, int y2){
        this.first = new Point2D.Double(x1, y1);
        this.second = new Point2D.Double(x2, y2);
        doRest();
        }
    micLine(Point2D.Double first, Point2D.Double second){
        this.first = first;
        this.second = second;
        doRest();
    }
    void doRest(){
        pixelLength = Math.sqrt(nonSRPyth(first, second));
        rangeLength = (int)Math.ceil(pixelLength/282.5);
        rangeString += Integer.toString(rangeLength);
        line = new Line2D.Double(first, second);
        calculateCenter();
    }

    void calculateCenter(){
        centerX = (int)(this.first.getX() + 0.5*(this.second.getX()-this.first.getX()));
        centerY = (int)(this.first.getY() + 0.5*(this.second.getY()-this.first.getY()));
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
