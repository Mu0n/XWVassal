package mic;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.build.module.map.MovementReporter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.MoveTracker;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.NonRectangular;
import mic.manuvers.ManeuverPaths;
import mic.manuvers.PathPart;

import static mic.Util.logToChat;


/**
 * Created by amatheny on 2/14/17.
 */
public class AutoBumpDecorator extends Decorator implements EditablePiece {
    public static final String ID = "auto-bump;";

    // Set to true to enable visualizations of collision objects.
    // They will be drawn after a collision resolution, select the colliding
    // ship and press x to remove it.
    private static boolean DRAW_COLLISIONS = true;

    private final FreeRotator testRotator;

    private ShipPositionState prevPosition = null;
    private ManeuverPaths lastManeuver = null;
    private FreeRotator myRotator = null;
    private CollisionVisualization collisionVisualization = null;

    private static Map<String, ManeuverPaths> keyStrokeToManeuver = ImmutableMap.<String, ManeuverPaths>builder()
            .put("SHIFT 1", ManeuverPaths.Str1)
            .put("SHIFT 2", ManeuverPaths.Str2)
            .put("SHIFT 3", ManeuverPaths.Str3)
            .put("SHIFT 4", ManeuverPaths.Str4)
            .put("SHIFT 5", ManeuverPaths.Str5)
            .put("CTRL SHIFT 1", ManeuverPaths.LT1)
            .put("CTRL SHIFT 2", ManeuverPaths.LT2)
            .put("CTRL SHIFT 3", ManeuverPaths.LT3)
            .put("ALT SHIFT 1", ManeuverPaths.RT1)
            .put("ALT SHIFT 2", ManeuverPaths.RT2)
            .put("ALT SHIFT 3", ManeuverPaths.RT3)
            .put("CTRL 1", ManeuverPaths.LBk1)
            .put("CTRL 2", ManeuverPaths.LBk2)
            .put("CTRL 3", ManeuverPaths.LBk3)
            .put("ALT 1", ManeuverPaths.RBk1)
            .put("ALT 2", ManeuverPaths.RBk2)
            .put("ALT 3", ManeuverPaths.RBk3)
            .put("ALT CTRL 1", ManeuverPaths.K1)
            .put("ALT CTRL 2", ManeuverPaths.K2)
            .put("ALT CTRL 3", ManeuverPaths.K3)
            .put("ALT CTRL 4", ManeuverPaths.K4)
            .put("ALT CTRL 5", ManeuverPaths.K5)
            .put("CTRL 6", ManeuverPaths.RevLbk1)
            .put("SHIFT 6", ManeuverPaths.RevStr1)
            .put("ALT 6", ManeuverPaths.RevRbk1)
            .put("CTRL Q", ManeuverPaths.SloopL1)
            .put("CTRL W", ManeuverPaths.SloopL2)
            .put("CTRL E", ManeuverPaths.SloopL3)
            .put("ALT Q", ManeuverPaths.SloopR1)
            .put("ALT W", ManeuverPaths.SloopR2)
            .put("ALT E", ManeuverPaths.SloopR3)
            .put("CTRL SHIFT E", ManeuverPaths.SloopL3Turn)
            .put("ALT SHIFT E", ManeuverPaths.SloopR3Turn)
            .put("CTRL Y", ManeuverPaths.TrollL2)
            .put("CTRL T", ManeuverPaths.TrollL3)
            .put("ALT Y", ManeuverPaths.TrollR2)
            .put("ALT T", ManeuverPaths.TrollR3)
            .build();

    public AutoBumpDecorator() {
        this(null);
    }

    public AutoBumpDecorator(GamePiece piece) {
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

    @Override
    public Command keyEvent(KeyStroke stroke) {

        if (stroke.getKeyChar() == 'x' && this.collisionVisualization != null) {
            getMap().removeDrawComponent(this.collisionVisualization);
        }

        ManeuverPaths path = getKeystrokePath(stroke);
        // Is this a keystroke for a maneuver? Deal with the 'no' cases first
        if (path == null) {
            //check to see if you want to delete the piece, if so, then remove the collision orange graphic first
            if(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK,false).equals(stroke)
                    && DRAW_COLLISIONS
                    && this.collisionVisualization != null)
            {
                getMap().removeDrawComponent(this.collisionVisualization);
            }
            if(isAutobumpTrigger(stroke)) //check to see if 'c' was pressed
            {
                List<Shape> otherShipShapes = getOtherShipShapes();
                boolean isCollisionOccuring = findCollidingShip(getShipCompareShape(this), otherShipShapes) != null ? true : false;

                //backtracking requested with a detected ship overlap, deal with it
                if(isCollisionOccuring)
                {
                    Command innerCommand = piece.keyEvent(stroke);
                    Command bumpResolveCommand = resolveBump(otherShipShapes);
                    return bumpResolveCommand == null ? innerCommand : innerCommand.append(bumpResolveCommand);
                }
            }
            return piece.keyEvent(stroke); // not a maneuver, not collsion backtracking either, deal with keystroke as usual
        }

        // We know we're dealing with a maneuver keystroke
        if (stroke.isOnKeyRelease() == false) {

            if(this.collisionVisualization != null)
            {
                getMap().removeDrawComponent(this.collisionVisualization);
            }

            // find the list of other ships
            List<Shape> otherShipShapes = getOtherShipShapes();

            //safeguard old position and path before sending off the keystroke to the regular Vassal command

            this.prevPosition = getCurrentState();
            this.lastManeuver = path;

            announceBumpAndPaint(otherShipShapes,path);
        }
        return piece.keyEvent(stroke);
    }


    private void announceBumpAndPaint(List<Shape> otherShipShapes, ManeuverPaths path) {

        Shape rawShape = getRawShape(this);


        final List<PathPart> parts = path.getTransformedPathParts(
                this.getCurrentState().x,
                this.getCurrentState().y,
                this.getCurrentState().angle,
                isLargeShip(this)
        );

            PathPart part = parts.get(parts.size()-1);
            Shape movedShape = AffineTransform
                    .getTranslateInstance(part.getX(), part.getY())
                    .createTransformedShape(rawShape);
            double roundedAngle = convertAngleToGameLimits(part.getAngle());
            movedShape = AffineTransform
                    .getRotateInstance(Math.toRadians(-roundedAngle), part.getX(), part.getY())
                    .createTransformedShape(movedShape);

            Shape bumpedShip = findCollidingShip(movedShape, otherShipShapes);
            if (bumpedShip != null) {
                if (DRAW_COLLISIONS) {
                    String bumpAlertString = "Overlap detected with "
                            + this.getProperty("Craft ID #").toString()
                            + " (" + this.getProperty("Pilot Name").toString() + ") and "
                            + "TODO: find other bumped ship name. Resolve this by hitting the 'c' key.";

                    logToChat(bumpAlertString);
                    this.collisionVisualization = new CollisionVisualization(movedShape, bumpedShip);
                    getMap().addDrawComponent(this.collisionVisualization);
                }
            }
            else
            {
                if (this.collisionVisualization != null) {
                    getMap().removeDrawComponent(this.collisionVisualization);
                }
            }
    }
    /**
     * Iterate in reverse over path of last maneuver and return a command that
     * will move the ship to a non-overlapping position and rotation
     *
     * @return
     */
    private Command resolveBump(List<Shape> otherShipShapes) {
        if (this.lastManeuver == null || this.prevPosition == null) {
            return null;
        }
        Shape rawShape = getRawShape(this);
        final List<PathPart> parts = this.lastManeuver.getTransformedPathParts(
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
                if (DRAW_COLLISIONS) {
                    if (this.collisionVisualization != null) {
                        getMap().removeDrawComponent(this.collisionVisualization);
                    }
                }
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
    private Shape findCollidingShip(Shape myTestShape, List<Shape> otherShapes) {
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

    /**
     * Finds any maneuver paths related to the keystroke based on the map
     * keyStrokeToManeuver map
     *
     * @param keyStroke
     * @return
     */
    private ManeuverPaths getKeystrokePath(KeyStroke keyStroke) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToManeuver.containsKey(hotKey)) {
            return keyStrokeToManeuver.get(hotKey);
        }
        return null;
    }

    private List<Shape> getOtherShipShapes() {
        List<Shape> shapes = Lists.newLinkedList();
        for (Decorator ship : getShipsOnMap()) {
            if (getId().equals(ship.getId())) {
                continue;
            }
            shapes.add(getShipCompareShape(ship));
        }
        return shapes;
    }

    private List<Decorator> getShipsOnMap() {
        List<Decorator> ships = Lists.newArrayList();

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
            Color myO = new Color(255,99,71,150);
            graphics2D.setColor(myO);
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