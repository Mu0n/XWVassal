package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;
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
    private static Map<String, ManeuverPaths> keyStrokeToManeuver = ImmutableMap.<String, ManeuverPaths>builder()
            //.put("CTRL O", ManeuverPaths.Str1)
            .build();
    private static double RANGE1 = 282.5;
    private static double RANGE2 = 565;
    private static double RANGE3 = 847.5;


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

private void clearVisu()
{
    getMap().removeDrawComponent(this.fov);
    this.fov.shapes.clear();
    this.fov.lines.clear();
}
    public Command keyEvent(KeyStroke stroke) {

        if(this.fov == null) {
            this.fov = new FOVisualization();
        }

        //Full Range Options CTRL-O
        if (KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK,false).equals(stroke)) {
            Command bigCommand = piece.keyEvent(stroke);


            if (this.fov != null && this.fov.getCount() > 0) {
                clearVisu();
                return bigCommand;
            }
            BumpableWithShape thisShip = new BumpableWithShape(this, "Ship", false);
            List<BumpableWithShape> BWS = getOtherShipsOnMap();
            for(BumpableWithShape b: BWS){
                fov.add(b.rectWithNoNubs);
                /*
                double[] dist = new double[4];
                dist[0] = nonSRPyth(this.getPosition(), new Point((int)b.getVertices()[0],(int)b.getVertices()[1]));
                dist[1] = nonSRPyth(this.getPosition(), new Point((int)b.getVertices()[2],(int)b.getVertices()[3]));
                dist[2] = nonSRPyth(this.getPosition(), new Point((int)b.getVertices()[4],(int)b.getVertices()[5]));
                dist[3] = nonSRPyth(this.getPosition(), new Point((int)b.getVertices()[6],(int)b.getVertices()[7]));

bigCommand.append(logToChatCommand("dist1 " + Double.toString(dist[0]) +
        " dist2 " + Double.toString(dist[1]) +
        " dist3 " + Double.toString(dist[2]) +
        " dist4 " + Double.toString(dist[3])));


                double min = Double.MAX_VALUE;
                int bestIndex = -1;
                myLines.clear();
                for(int i = 0 ; i <4 ; i++){
                    micLine toAdd = new micLine(this.getPosition().x, this.getPosition().y, (int)b.getVertices()[2*i], (int)b.getVertices()[2*i+1]);
                    toAdd.range = "Range " + Double.toString((int)Math.ceil(Math.sqrt(nonSRPyth(this.getPosition(),new Point((int)b.getVertices()[2*i], (int)b.getVertices()[2*i+1])))/282.5f));
                myLines.add(toAdd);
                    if(min > dist[i]) {
                        min = dist[i]; //min distance
                        bestIndex = myLines.size()-1;
                    }

                }
                logToChat("best index" + Integer.toString(bestIndex));
                if(bestIndex != -1) myLines.get(bestIndex).isBestLine = true;
                bigCommand.append(logToChatCommand("minimum distance " + Double.toString(min) + " sqroot " + Math.sqrt(min) + " range " + (int)Math.ceil(Math.sqrt(min)/282.5)));
*/
                //Preliminary check, eliminate attempt to calculate this if the target is overlapping the attacker, could lead to exception errors
                if(isTargetOverlappingAttacker(b) == true) continue;

                //First check, verify that the target ship's vertices are not inside the projected rectangles from the attacker
                if(true)
                {//easiest case first, the best line will go from attacker's corner to target's corner
                    Point2D.Double A1 = findClosestVertex(thisShip, b);
                    Point2D.Double A2 = find2ndClosestVertex(thisShip, b);

                    Point2D.Double D1 = findClosestVertex(b, thisShip);
                    Point2D.Double D2 = find2ndClosestVertex(b, thisShip);

                    micLine bestLine = findBestLine(A1, A2, D1, D2);
                    bestLine.isBestLine = true;

                    //find if there's an obstruction
                    List<BumpableWithShape> obstructions = getObstructionsOnMap();
                    for(BumpableWithShape obstruction : obstructions){
                        if(isLine2DOverlapShape(bestLine.line, obstruction.shape))
                        {
                            bestLine.rangeString += " obstructed";
                            fov.add(obstruction.shape);
                            break;
                        }
                    }
                    fov.addLine(bestLine);
                }

                //verify that the target ship and attacker ship aren't of the same alignment, modulo 90 degrees
                else if(are90degreesAligned() == false) {
                    //second easiest case, the best line will be from a corner from the target to a point found on the attacker's edge
                    logToChat("mild case; particular point on attacker's edge to corner on target");
                }
                else { //toughest case, multiple lines are possible between attacker and target
                    logToChat("toughest case; multiple lines");
                }
            }
            getMap().addDrawComponent(fov);
            return bigCommand;
        }
        return piece.keyEvent(stroke);

    }

    //this finds the line that links the attacker ship to someplace on the line formed by the closest edge of the target, using
    //the closest and 2nd closest lines to the target's vertices.
    //using an algorithm based on this: http://www.ahristov.com/tutorial/geometry-games/point-line-distance.html
    private micLine findBestLine(Point2D.Double A1, Point2D.Double A2, Point2D.Double D1, Point2D.Double D2) {

        ArrayList<micLine> lineList = new ArrayList<micLine>();

        //Closest Attacker to Closest Defender
        micLine A1D1 = new micLine(A1, D1);
        //Closest Attacker to 2nd Closest Defender
        micLine A1D2 = new micLine(A1, D2);
        //Closest Defender to Closest Attacker
        micLine D1A1 = new micLine(D1, A1);
        //Closest Defender to 2nd Closest Attacker
        micLine D1A2 = new micLine(D1, A2);
        //Closest Attacker Edge
        micLine AA = new micLine(A1, A2);
        //Closest Defender Edge
        micLine DD = new micLine(D1, D2);

        lineList.add(A1D1);
        lineList.add(A1D2);
        lineList.add(D1A2);

        //Figure out A1 to DD, closest line according to the algorithm above
        //end of the closest vertex
        double x1 = A1D1.second.getX();
        double y1 = A1D1.second.getY();
        //end of the 2nd closest vertex
        double x2 = A1D2.second.getX();
        double y2 = A1D2.second.getY();
        //start point
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

        micLine A1DD = new micLine(A1, new Point2D.Double(vector_x, vector_y));
        lineList.add(A1DD);

        //Figure out D1 to AA, closest line according to the algorithm above
        x1 = A1D1.first.getX();
        y1 = A1D1.first.getY();
        //end of the 2nd closest vertex
        x2 = D1A2.second.getX();
        y2 = D1A2.second.getY();
        //start point
        xp = A1D1.second.getX();
        yp = A1D1.second.getY();
//getting the shortest distance in pixels to the line formed by both (x1,y1) and (x2,y2)
        numerator = Math.abs((xp - x1) * (y2 - y1) - (yp - y1) * (x2 - x1));
        denominator = Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
        shortestdist = numerator / denominator;

        segmentPartialDist = Math.sqrt(Math.pow(D1A1.pixelLength,2.0) - Math.pow(shortestdist,2.0));
        segmentFullDist = AA.pixelLength;
        gapx = (x2-x1)/segmentFullDist*segmentPartialDist;
        gapy = (y2-y1)/segmentFullDist*segmentPartialDist;
        vector_x = x1 + gapx;
        vector_y = y1 + gapy;

        micLine D1AA = new micLine(D1, new Point2D.Double(vector_x,vector_y));
        lineList.add(D1AA);

        //find the best distance and make it the best Line
        double bestDist = Double.MAX_VALUE;
        micLine best = A1D1;
        for(micLine l : lineList){
            if(Double.compare(bestDist,l.pixelLength) > 0) {
                bestDist = l.pixelLength;
                best = l;
            }
        }
        return best;

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

    private boolean are90degreesAligned() {
        return false;
    }

    private Boolean isTargetOutsideofRectangles(BumpableWithShape targetBWS) {
        Shape crossZone = findUnionOfRectangularExtensions();
        ArrayList<Point2D.Double> allFourCorners = targetBWS.getVertices();
        for(Point2D.Double vertex : allFourCorners){
            if(isPointInShape(vertex, crossZone)) return false;
        }
        return true;
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
    private Shape findUnionOfRectangularExtensions() {
        Shape rawShape = getRawShape(this);
        double workingWidth = 226.0f;
        if(rawShape.getBounds().getWidth() < 140.0f) workingWidth = 113.0f;

        Shape frontBack = new Rectangle2D.Double(-workingWidth/2.0, -RANGE3 - workingWidth/2.0, workingWidth, 2.0*RANGE3 + workingWidth);
        Shape leftRight = new Rectangle2D.Double(-workingWidth/2.0 - RANGE3, -workingWidth/2.0, 2.0*RANGE3+workingWidth, workingWidth);

        Area zone = new Area(frontBack);
        zone.add(new Area(leftRight));
        zone.exclusiveOr(new Area(rawShape));

        double centerX = this.getPosition().getX();
        double centerY = this.getPosition().getY();

        Shape transformed = AffineTransform
                .getTranslateInstance(centerX, centerY)
                .createTransformedShape(zone);
        FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(this), FreeRotator.class));

        transformed = AffineTransform
                .getRotateInstance(rotator.getAngleInRadians(), centerX, centerY)
                .createTransformedShape(transformed);

        fov.add(transformed);
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
        private final List<micLine> lines;

        public Color badLineColor = new Color(30,30,255,255);
        public Color bestLineColor = new Color(45,200,190,255);

        Color myO = new Color(0,50,255, 50);
        FOVisualization() {
            this.shapes = new ArrayList<Shape>();
            this.lines = new ArrayList<micLine>();
        }
        FOVisualization(Shape ship) {
            this.shapes = new ArrayList<Shape>();
            this.shapes.add(ship);
            this.lines = new ArrayList<micLine>();
        }

        public void add(Shape bumpable) {
            this.shapes.add(bumpable);
        }
        public void addLine(micLine line){
            this.lines.add(line);
        }

        public int getCount() {
            int count = 0;
            Iterator<Shape> it = this.shapes.iterator();
            while(it.hasNext()) {
                count++;
                it.next();
            }
            return count;
        }
        public void draw(Graphics graphics, VASSAL.build.module.Map map) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            graphics2D.setColor(myO);
            double scale = map.getZoom();
            AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);

            for (Shape shape : shapes) {
                graphics2D.fill(scaler.createTransformedShape(shape));

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

    private static class ShipPositionState {
        double x;
        double y;
        double angle;
    }
}
