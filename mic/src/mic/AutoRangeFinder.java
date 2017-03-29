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
import java.util.*;

import static mic.Util.logToChat;

/**
 * Created by Mic on 23/03/2017.
 */
public class AutoRangeFinder extends Decorator implements EditablePiece {
    public static final String ID = "auto-range-finder";

    private final FreeRotator testRotator;

    private ShipPositionState prevPosition = null;
    private ManeuverPaths lastManeuver = null;
    private FreeRotator myRotator = null;
    private CollisionVisualization collisionVisualization = null;


    public AutoRangeFinder() {
        this(null);
    }

    public AutoRangeFinder(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
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

    private boolean isAutobumpTrigger(KeyStroke stroke) {
        return KeyStroke.getAWTKeyStroke(KeyEvent.VK_C, 0, false).equals(stroke);
    }

    public Command keyEvent(KeyStroke stroke) {

        //Firing Arc command activated CTRL-F
        if (KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK,false).equals(stroke)) {
           logToChat("FORWARD ARC CTRL-F has been activated");
        }

        //Auxiliary firing Arc command activated CTRL-V
        if (KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK,false).equals(stroke)) {
            logToChat("BACKWARD AUXILIARY ARC CTRL-V has been activated");
        }

        //Left mobile firing Arc command activated CTRL-Shift-V
        if (KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK & KeyEvent.SHIFT_DOWN_MASK,false).equals(stroke)) {
            logToChat("LEFT MOBILE ARC CTRL-SHIFT-V has been activated");
        }

        //Right mobile firing Arc command activated ALT-Shift-V
        if (KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.SHIFT_DOWN_MASK & KeyEvent.ALT_DOWN_MASK,false).equals(stroke)) {
            logToChat("RIGHT MOBILE ARC ALT-SHIFT-F has been activated");
        }
        //180 Auxiliary firing Arc command activated CTRL-N
        if (KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK,false).equals(stroke)) {
            logToChat("180 DEGREES AUXILIARY ARC CTRL-N has been activated");
        }




        return piece.keyEvent(stroke);
    }

    /**
     * Iterate in reverse over path of last maneuver and return a command that
     * will move the ship to a non-overlapping position and rotation
     *
     * @return
     */
    private Command resolveBump(java.util.List<Shape> otherShipShapes) {
        if (this.lastManeuver == null || this.prevPosition == null) {
            return null;
        }

        Shape rawShape = getRawShape(this);
        final java.util.List<PathPart> parts = this.lastManeuver.getTransformedPathParts(
                this.prevPosition.x,
                this.prevPosition.y,
                this.prevPosition.angle,
                isLargeShip(this)
        );

        Shape lastBumpedShip = null;
        for (int i = parts.size() - 1; i >= 0; i--) {
            PathPart part = parts.get(i);
            Shape movedShape = AffineTransform
                    .getTranslateInstance(part.getX(), part.getY())
                    .createTransformedShape(rawShape);
            double roundedAngle = convertAngleToGameLimits(part.getAngle());
            movedShape = AffineTransform
                    .getRotateInstance(Math.toRadians(-roundedAngle), part.getX(), part.getY())
                    .createTransformedShape(movedShape);

            Shape bumpedShip = findCollidingShip(movedShape, otherShipShapes);
            if (bumpedShip == null) {
                return buildTranslateCommand(part);
            }
            lastBumpedShip = bumpedShip;
        }

        // Could not find a position that wasn't bumping, bring it back to where it was before
        return buildTranslateCommand(new PathPart(this.prevPosition.x, this.prevPosition.y, this.prevPosition.angle));
    }

    /**
     * Builds vassal command to transform the current ship to the given PathPart
     *
     * @param part
     * @return
     */
    private Command buildTranslateCommand(PathPart part) {
        // Copypasta from VASSAL.counters.Pivot
        ChangeTracker changeTracker = new ChangeTracker(this);
        getRotator().setAngle(part.getAngle());
        setProperty("Moved", Boolean.TRUE);
        Command result = changeTracker.getChangeCommand();

        GamePiece outermost = Decorator.getOutermost(this);
        MoveTracker moveTracker = new MoveTracker(outermost);
        Point point = new Point((int) Math.floor(part.getX() + 0.5), (int) Math.floor(part.getY() + 0.5));
        // ^^ There be dragons here ^^ - vassals gives positions as doubles but only lets them be set as ints :(
        this.getMap().placeOrMerge(outermost, point);
        result = result.append(moveTracker.getMoveCommand());
        MovementReporter reporter = new MovementReporter(result);

        Command reportCommand = reporter.getReportCommand();
        if (reportCommand != null) {
            reportCommand.execute();
            result = result.append(reportCommand);
        }

        result = result.append(reporter.markMovedPieces());

        return result;
    }

    /**
     * Returns the comparision shape of the first ship colliding with the provided ship.  Returns null if there
     * are no collisions
     *
     * @param myTestShape
     * @return
     */
    private Shape findCollidingShip(Shape myTestShape, java.util.List<Shape> otherShapes) {
        for (Shape otherShipShape : otherShapes) {
            if (shapesOverlap(myTestShape, otherShipShape)) {
                return otherShipShape;
            }
        }
        return null;
    }

    /**
     * Returns true if the two provided shapes areas have any intersection
     *
     * @param shape1
     * @param shape2
     * @return
     */
    private boolean shapesOverlap(Shape shape1, Shape shape2) {
        Area a1 = new Area(shape1);
        a1.intersect(new Area(shape2));
        return !a1.isEmpty();
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
        return "Custom auto-bump resolution (mic.AutoBumpDecorator)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    /**
     * Returns FreeRotator decorator associated with this instance
     *
     * @return
     */
    private FreeRotator getRotator() {
        if (this.myRotator == null) {
            this.myRotator = ((FreeRotator) Decorator.getDecorator(getOutermost(this), FreeRotator.class));
        }
        return this.myRotator;
    }

    /**
     * Returns a new ShipPositionState based on the current position and angle of this ship
     *
     * @return
     */
    private ShipPositionState getCurrentState() {
        ShipPositionState shipState = new ShipPositionState();
        shipState.x = getPosition().getX();
        shipState.y = getPosition().getY();
        shipState.angle = getRotator().getAngle();
        return shipState;
    }


    private java.util.List<Shape> getOtherShipShapes() {
        java.util.List<Shape> shapes = Lists.newLinkedList();
        for (Decorator ship : getShipsOnMap()) {
            if (getId().equals(ship.getId())) {
                continue;
            }
            shapes.add(getShipCompareShape(ship));
        }
        return shapes;
    }

    private java.util.List<Decorator> getShipsOnMap() {
        java.util.List<Decorator> ships = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("Ship")) {
                ships.add((Decorator) piece);
            }
        }
        return ships;
    }

    /**
     * Uses a FreeRotator unassociated with any game pieces with a 360 rotation limit
     * to convert the provided angle to the same angle the ship would be drawn at
     * by vassal
     *
     * @param angle
     * @return
     */
    private double convertAngleToGameLimits(double angle) {
        this.testRotator.setAngle(angle);
        return this.testRotator.getAngle();
    }

    /**
     * Finds non-rectangular mask layer of provided ship.  This is the shape with only the base
     * and nubs
     *
     * @param ship
     * @return
     */
    private Shape getRawShape(Decorator ship) {
        return Decorator.getDecorator(Decorator.getOutermost(ship), NonRectangular.class).getShape();
    }

    /**
     * Finds raw ship mask and translates and rotates it to the current position and heading
     * of the ship
     *
     * @param ship
     * @return Translated ship mask
     */
    private Shape getShipCompareShape(Decorator ship) {
        Shape rawShape = getRawShape(ship);
        double scaleFactor = isLargeShip(ship) ? 1.01d : 1.025d;
        Shape transformed = AffineTransform.getScaleInstance(scaleFactor, scaleFactor).createTransformedShape(rawShape);

        transformed = AffineTransform
                .getTranslateInstance(ship.getPosition().getX(), ship.getPosition().getY())
                .createTransformedShape(transformed);

        FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(ship), FreeRotator.class));
        double centerX = ship.getPosition().getX();
        double centerY = ship.getPosition().getY();
        transformed = AffineTransform
                .getRotateInstance(rotator.getAngleInRadians(), centerX, centerY)
                .createTransformedShape(transformed);

        return transformed;
    }

    private boolean isLargeShip(Decorator ship) {
        return getRawShape(ship).getBounds().getWidth() > 114;
    }

    private static class CollisionVisualization implements Drawable {

        private final Shape ship1;
        private final Shape ship2;

        CollisionVisualization(Shape ship1, Shape ship2) {
            this.ship1 = ship1;
            this.ship2 = ship2;
        }

        public void draw(Graphics graphics, VASSAL.build.module.Map map) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            graphics2D.setColor(Color.orange);

            AffineTransform scaler = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom());
            graphics2D.fill(scaler.createTransformedShape(ship1));
            if (ship2 != null) {
                graphics2D.fill(scaler.createTransformedShape(ship2));
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
