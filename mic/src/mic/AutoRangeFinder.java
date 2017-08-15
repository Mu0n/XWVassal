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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.Map;

import static mic.Util.logToChat;
import static mic.Util.logToChatCommand;

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
            .put("CTRL O", ManeuverPaths.Str1)
            .build();
private final double preventDoubleTap = 500;
private Date lastPressTime = new Date();

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


    public Command keyEvent(KeyStroke stroke) {
        if(this.fov == null) {
            this.fov = new FOVisualization();
        }

        Command bigCommand = logToChatCommand("");

        //Full Range Options CTRL-O
        if (KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK,false).equals(stroke)) {

            Date thisPressTime = new Date();
            if(thisPressTime.getTime() - lastPressTime.getTime() < preventDoubleTap)
            {
                logToChat("delta time is " + Double.toString(thisPressTime.getTime() - lastPressTime.getTime()));
                lastPressTime = thisPressTime;
                return null;
            }
            else lastPressTime = thisPressTime;

            if (this.fov != null && this.fov.getCount() > 0) {
                getMap().removeDrawComponent(this.fov);
                logToChat("components removed");
                this.fov.shapes.clear();
            }

            List<BumpableWithShape> BWS = getOtherShipsOnMap();
            for(BumpableWithShape b: BWS){
                fov.add(b.rectWithNoNubs);

                bigCommand = logToChatCommand("ship " + b.pilotName + " (" + b.shipName + ") " +
                " center is at " + Integer.toString(b.bumpable.getPosition().x) + "," + Integer.toString(b.bumpable.getPosition().y) +
                        "raw distance " + Double.toString(nonSRPyth(b.bumpable.getPosition(),this.getPosition()))
                );
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
                int j = 5;
                for(int i = 0 ; i <4 ; i++){
                    if(min > dist[i]) {
                        min = dist[i]; //min distance
                        j=i;//index of the min distance
                    }
                }
                bigCommand.append(logToChatCommand("minimum distance " + Double.toString(min) + " sqroot " + Math.sqrt(min) + " range " + (int)Math.ceil(Math.sqrt(min)/282.5)));
                TestAWT myLine = new TestAWT(this.getPosition().x, this.getPosition().y, (int)b.getVertices()[j], (int)b.getVertices()[j+1]);
                myLine.draw(getMap().getView().getGraphics(),getMap());
            }


            fov.draw(getMap().getView().getGraphics(),getMap());
            return bigCommand;
        }
        return null;
    }

    //non square rooted Pythagoreas theorem. don't need the real pixel distance, just numbers which can be compared and are proportional to the distance
private double nonSRPyth(Point first, Point second){
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

        Color myO = new Color(0,50,255, 150);

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
int x1, y1, x2, y2;
TestAWT(int x1, int y1, int x2, int y2){
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
}

        public void draw(Graphics graphics, VASSAL.build.module.Map map) {
            Graphics2D graphics2D = (Graphics2D) graphics;

            AffineTransform scaler = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom());
            graphics2D.setColor(new Color(10,255,200));
            graphics2D.drawLine(x1,y1,x2,y2);
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
