package mic;

import VASSAL.build.module.*;
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
import java.awt.geom.*;
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

    List<TestAWT> myLines = new ArrayList<TestAWT>();
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
    for(TestAWT t: myLines) getMap().removeDrawComponent(t);
    logToChat("components removed");
    this.fov.shapes.clear();
    myLines.clear();
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
                    TestAWT toAdd = new TestAWT(this.getPosition().x, this.getPosition().y, (int)b.getVertices()[2*i], (int)b.getVertices()[2*i+1]);
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
                if(isTargetOutsideofRectangles(b) == true)
                {//easiest case first, the best line will go from attacker's corner to target's corner
                    logToChat("easiest case; corner to corner");
                    Point2D.Double fromAttacker = findClosestVertex(thisShip, b);
                    Point2D.Double toTarget = findClosestVertex(b, thisShip);
                    TestAWT bestLine = new TestAWT(fromAttacker, toTarget);
                    bestLine.isBestLine = true;
                    bigCommand.append(logToChatCommand("range " + (int)Math.ceil(Math.sqrt(nonSRPyth(fromAttacker,toTarget))/282.5)));

                    Line2D.Double bestLine2D = new Line2D.Double(fromAttacker,toTarget);

                    myLines.add(bestLine);
                }

                //verify that the target ship and attacker ship aren't of the same alignment, modulo 90 degrees
                else if(are90degreesAligned() == false) {
                    //second easiest case, the best line will be from a corner from the target to a point found on the attacker's edge
                    logToChat("mild case; particular point on attacker's edge to corner on target");
                }
                else { //toughest case, multiple lines are possible between attacker and target
                    logToChat("toughest case; multiple lines");
                }
                for(TestAWT t : myLines) {
                    getMap().addDrawComponent(t);
                }
            }
            getMap().addDrawComponent(fov);

            return bigCommand;
        }
        return piece.keyEvent(stroke);

    }

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

    //non square rooted Pythagoreas theorem. don't need the real pixel distance, just numbers which can be compared and are proportional to the distance
private double nonSRPyth(Point first, Point second){
        return Math.pow(first.getX()-second.getX(),2) + Math.pow(first.getY()-second.getY(),2);
}

    private double nonSRPyth(Point2D.Double first, Point2D.Double second){
        return Math.pow(first.getX()-second.getX(),2) + Math.pow(first.getY()-second.getY(),2);
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

        Color myO = new Color(0,50,255, 50);
        FOVisualization() {
            this.shapes = new ArrayList<Shape>();
        }
        FOVisualization(Shape ship) {
            this.shapes = new ArrayList<Shape>();
            this.shapes.add(ship);
        }

        public void add(Shape bumpable) {
            this.shapes.add(bumpable);
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

            AffineTransform scaler = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom());
            for (Shape shape : shapes) {
                graphics2D.fill(scaler.createTransformedShape(shape));
            }
        }

        public boolean drawAboveCounters() {
            return true;
        }
    }
    public static class TestAWT implements Drawable{
        public Color myO = new Color(30,30,255,255);
        public Color bestO = new Color(45,200,190,255);
        public Boolean isBestLine = false;
        public String range = "";
int x1, y1, x2, y2;
TestAWT(int x1, int y1, int x2, int y2){
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
}
TestAWT(Point2D.Double first, Point2D.Double second){
    this.x1 = (int)first.getX();
    this.y1 = (int)first.getY();
    this.x2 = (int)second.getX();
    this.y2 = (int)second.getY();
}

        public void draw(Graphics graphics, VASSAL.build.module.Map map) {
            Graphics2D graphics2D = (Graphics2D) graphics;

            if(isBestLine == true) graphics2D.setColor(bestO);
            else graphics2D.setColor(myO);
            graphics2D.drawLine(x1,y1,x2,y2);
            double centerX = 0.5*((double)x2 - (double)x1);
            double centerY = 0.5*((double)y2 - (double)y1);
            graphics2D.drawString(range,(int)centerX, (int)centerY);
        }

        public boolean drawAboveCounters() {
            return false;
        }
    }

    private static class ShipPositionState {
        double x;
        double y;
        double angle;
    }
}
