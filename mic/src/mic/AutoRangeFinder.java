package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.build.module.map.MovementReporter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.MoveTracker;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mic.manuvers.ManeuverPaths;
import mic.manuvers.PathPart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.RectangularShape;
import java.util.*;
import java.util.List;

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

        Command bigCommand;

        //Full Range Options CTRL-O
        if (KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK,false).equals(stroke)) {
            bigCommand = logToChatCommand("starting firing options");

            List<BumpableWithShape> BWS = getShipsOnMap();
            for(BumpableWithShape b: BWS){
                fov.add(b.rectWithNoNubs);

                bigCommand.append(logToChatCommand("ship #" + Integer.toString(fov.shapes.size()) + " detected of size "
                        + Double.toString(b.shape.getBounds().getWidth()) + " by "
                        + Double.toString(b.shape.getBounds().getHeight())));
            }
            fov.draw(getMap().getView().getGraphics(),getMap());
            return bigCommand;
        }
        return piece.keyEvent(stroke);
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

    private List<BumpableWithShape> getShipsOnMap() {
        List<BumpableWithShape> ships = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_a_ship")) {
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

    private static class ShipPositionState {
        double x;
        double y;
        double angle;
    }
}
