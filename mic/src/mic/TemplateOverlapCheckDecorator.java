package mic;

/**
 * Created by mjuneau on 2017-04-26.
 */


import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.widget.PieceSlot;
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
import javax.swing.Timer;
import java.awt.*;
import java.awt.List;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.*;

import static mic.Util.logToChatWithTime;

import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.NonRectangular;


public class TemplateOverlapCheckDecorator extends Decorator implements EditablePiece {
    public static final String ID = "TemplateOverlapCheck";
    static final int NBFLASHES = 5;
    static final int DELAYBETWEENFLASHES = 150;

    // Set to true to enable visualizations of collision objects.
    // They will be drawn after a collision resolution, select the colliding
    // ship and press x to remove it.
    private static boolean DRAW_COLLISIONS = true;

    private final FreeRotator testRotator;

    private FreeRotator myRotator = null;
    public CollisionVisualization previousCollisionVisualization = null;

    public TemplateOverlapCheckDecorator() {
        this(null);
    }

    public TemplateOverlapCheckDecorator(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
        previousCollisionVisualization = new CollisionVisualization();
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

    private PieceSlot findPieceSlotByID(String gpID) {
        for(PieceSlot ps : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)){
            if(gpID.equals(ps.getGpId())) return ps;
        }
        return null;
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {
        //Any keystroke made on a ship will remove the orange shades

        if(this.previousCollisionVisualization == null) {
            this.previousCollisionVisualization = new CollisionVisualization();
        }

            //check to see if 'c' was pressed
            if(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false).equals(stroke)) {
                java.util.List<BumpableWithShape> otherShipShapes = getShipsWithShapes();

                boolean isCollisionOccuring = findCollidingEntity(getBumpableCompareShape(this), otherShipShapes) != null ? true : false;
                //backtracking requested with a detected bumpable overlap, deal with it
                if (isCollisionOccuring) {
                    Command innerCommand = piece.keyEvent(stroke);

                    //paint the template orange and the culprits too

                    return bumpResolveCommand == null ? innerCommand : innerCommand.append(bumpResolveCommand);
                }
            }
            // 'c' keystroke has finished here, leave the method altogether

            return piece.keyEvent(stroke);


        // We know we're dealing with a maneuver keystroke
// TO DO include decloaks, barrel rolls
        if (stroke.isOnKeyRelease() == false) {


            if (this.previousCollisionVisualization != null && this.previousCollisionVisualization.getCount() > 0) {
                getMap().removeDrawComponent(this.previousCollisionVisualization);
                this.previousCollisionVisualization.shapes.clear();
            }
            // find the list of other bumpables
            java.util.List<BumpableWithShape> otherBumpableShapes = getBumpablesWithShapes();

            //safeguard old position and path

            this.prevPosition = getCurrentState();
            this.lastManeuver = path;

            //This PathPart list will be used everywhere: moving, bumping, out of boundsing
            //maybe fetch it for both 'c' behavior and movement
            final java.util.List<PathPart> parts = path.getTransformedPathParts(
                    this.getCurrentState().x,
                    this.getCurrentState().y,
                    this.getCurrentState().angle,
                    isLargeShip(this)
            );

            //this is the final ship position post-move
            PathPart part = parts.get(parts.size()-1);

            //Get the ship name string for announcements
            String yourShipName = getShipStringForReports(true);

            //Start the Command chain
            Command innerCommand = piece.keyEvent(stroke);

            innerCommand.append(buildTranslateCommand(part, path.getAdditionalAngleForShip()));
            logToChatWithTime("* --- " + yourShipName + " performs move: " + path.getFullName());

            //These lines fetch the Shape of the last movement template used
            FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(this), FreeRotator.class));
            Shape lastMoveShapeUsed = path.getTransformedTemplateShape(this.getPosition().getX(),
                    this.getPosition().getY(),
                    isLargeShip(this),
                    rotator);

            //Check for template shape overlap with mines, asteroids, debris
            checkTemplateOverlap(lastMoveShapeUsed, otherBumpableShapes);
            //Check for ship bumping other ships, mines, asteroids, debris
            announceBumpAndPaint(otherBumpableShapes);
            //Check if a ship becomes out of bounds
            checkIfOutOfBounds(yourShipName);

            //Add all the detected overlapping shapes to the map drawn components here
            if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getCount() > 0){

                final java.util.Timer timer = new java.util.Timer();
                timer.schedule(new TimerTask() {
                    int count = 1;
                    @Override
                    public void run() {
                        try{
                            previousCollisionVisualization.draw(getMap().getView().getGraphics(),getMap());
                            count++;
                            if(count == NBFLASHES * 2) {
                                getMap().removeDrawComponent(previousCollisionVisualization);
                                timer.cancel();
                            }
                        } catch (Exception e) {

                        }
                    }
                }, 0,DELAYBETWEENFLASHES);
            }

/*
            Executors.newCachedThreadPool().submit(new Runnable() {
                public void run() {
                    try {
                        for(int i=0; i < 8; i++) {
                            previousCollisionVisualization.draw(getMap().getView().getGraphics(),getMap());
                            getMap().getView().revalidate();
                            getMap().getView().repaint();
                            Thread.sleep(500);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    previousCollisionVisualization.shapes.clear();
                    getMap().removeDrawComponent(previousCollisionVisualization);
                }
            });
            */
        }
        //the maneuver has finished. return control of the event to vassal to do nothing
        return piece.keyEvent(stroke);
    }

    private void checkTemplateOverlap(Shape lastMoveShapeUsed, java.util.List<BumpableWithShape> otherBumpableShapes) {

        java.util.List<BumpableWithShape> collidingEntities = findCollidingEntities(lastMoveShapeUsed, otherBumpableShapes);
        CollisionVisualization cvFoundHere = new CollisionVisualization(lastMoveShapeUsed);

        int howManyBumped = 0;
        for (BumpableWithShape bumpedBumpable : collidingEntities) {
            if (DRAW_COLLISIONS) {
                String yourShipName = getShipStringForReports(true);
                if (bumpedBumpable.type.equals("Asteroid")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + "'s maneuver template and an asteroid.";
                    logToChatWithTime(bumpAlertString);
                    cvFoundHere.add(bumpedBumpable.shape);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Debris")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + "'s maneuver template and a debris cloud.";
                    logToChatWithTime(bumpAlertString);
                    cvFoundHere.add(bumpedBumpable.shape);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Mine")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + "'s maneuver template and a mine.";
                    logToChatWithTime(bumpAlertString);
                    cvFoundHere.add(bumpedBumpable.shape);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                }
            }
        }
        if (howManyBumped > 0) {
            this.previousCollisionVisualization.add(lastMoveShapeUsed);
        }
    }



    private void announceBumpAndPaint(java.util.List<BumpableWithShape> otherBumpableShapes) {
        Shape theShape = getBumpableCompareShape(this);

        java.util.List<BumpableWithShape> collidingEntities = findCollidingEntities(theShape, otherBumpableShapes);
        CollisionVisualization cvFoundHere = new CollisionVisualization(theShape);

        int howManyBumped = 0;
        for (BumpableWithShape bumpedBumpable : collidingEntities) {
            if (DRAW_COLLISIONS) {
                String yourShipName = getShipStringForReports(true);
                if (bumpedBumpable.type.equals("Ship")) {
                    String otherShipName = getShipStringForReports(false);
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and " + otherShipName + ". Resolve this by hitting the 'c' key.";
                    logToChatWithTime(bumpAlertString);
                    cvFoundHere.add(bumpedBumpable.shape);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Asteroid")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and an asteroid.";
                    logToChatWithTime(bumpAlertString);
                    cvFoundHere.add(bumpedBumpable.shape);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Debris")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and a debris cloud.";
                    logToChatWithTime(bumpAlertString);
                    cvFoundHere.add(bumpedBumpable.shape);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Mine")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and a mine.";
                    logToChatWithTime(bumpAlertString);
                    cvFoundHere.add(bumpedBumpable.shape);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                }
            }
        }
        if (howManyBumped > 0) {
            this.previousCollisionVisualization.add(theShape);
        }

    }

    /**
     * Iterate in reverse over path of last maneuver and return a command that
     * will move the ship to a non-overlapping position and rotation
     *
     * @return
     */

    /**
     * Returns the comparision shape of the first bumpable colliding with the provided ship.  Returns null if there
     * are no collisions
     *
     * @param myTestShape
     * @return
     */
    private BumpableWithShape findCollidingEntity(Shape myTestShape, java.util.List<BumpableWithShape> otherShapes) {
        java.util.List<BumpableWithShape> allCollidingEntities = findCollidingEntities(myTestShape, otherShapes);
        if (allCollidingEntities.size() > 0) {
            return allCollidingEntities.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns a list of all bumpables colliding with the provided ship.  Returns an empty list if there
     * are no collisions
     *
     * @param myTestShape
     * @return
     */
    private java.util.List<BumpableWithShape> findCollidingEntities(Shape myTestShape, java.util.List<BumpableWithShape> otherShapes) {
        java.util.List<BumpableWithShape> shapes = Lists.newLinkedList();
        for (BumpableWithShape otherBumpableShape : otherShapes) {
            if (shapesOverlap(myTestShape, otherBumpableShape.shape)) {
                shapes.add(otherBumpableShape);
            }
        }
        return shapes;
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
        return "Custom overlap detection (mic.TemplateOverlapCheckDecorator)";
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

    private java.util.List<BumpableWithShape> getShipsWithShapes() {
        java.util.List<BumpableWithShape> ships = Lists.newLinkedList();
        for (BumpableWithShape ship : getShipsOnMap()) {
            if (getId().equals(ship.bumpable.getId())) {
                continue;
            }
            ships.add(ship);
        }
        return ships;
    }

    private java.util.List<BumpableWithShape> getBumpablesWithShapes() {
        java.util.List<BumpableWithShape> bumpables = Lists.newLinkedList();
        for (BumpableWithShape bumpable : getBumpablesOnMap()) {
            if (getId().equals(bumpable.bumpable.getId())) {
                continue;
            }
            bumpables.add(bumpable);
        }
        return bumpables;
    }


    private java.util.List<BumpableWithShape> getBumpablesOnMap() {
        java.util.List<BumpableWithShape> bumpables = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("Asteroid")) {
                // comment out this line and the next three that add to bumpables if bumps other than with ships shouldn't be detected yet
                bumpables.add(new BumpableWithShape((Decorator)piece, getBumpableCompareShape((Decorator)piece), "Asteroid"));
            } else if (piece.getState().contains("Debris")) {
                bumpables.add(new BumpableWithShape((Decorator)piece, getBumpableCompareShape((Decorator)piece), "Debris"));
            } else if (piece.getState().contains("Bomb")) {
                bumpables.add(new BumpableWithShape((Decorator)piece, getBumpableCompareShape((Decorator)piece), "Mine"));
            }
        }
        return bumpables;
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
     * @param bumpable
     * @return
     */
    private Shape getRawShape(Decorator bumpable) {
        return Decorator.getDecorator(Decorator.getOutermost(bumpable), NonRectangular.class).getShape();
    }

    /**
     * Finds raw ship mask and translates and rotates it to the current position and heading
     * of the ship
     *
     * @param bumpable
     * @return Translated ship mask
     */
    private Shape getBumpableCompareShape(Decorator bumpable) {
        Shape rawShape = getRawShape(bumpable);
        double scaleFactor = 1.0f;


        Shape transformed = AffineTransform.getScaleInstance(scaleFactor, scaleFactor).createTransformedShape(rawShape);

        transformed = AffineTransform
                .getTranslateInstance(bumpable.getPosition().getX(), bumpable.getPosition().getY())
                .createTransformedShape(transformed);

        FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(bumpable), FreeRotator.class));
        double centerX = bumpable.getPosition().getX();
        double centerY = bumpable.getPosition().getY();
        transformed = AffineTransform
                .getRotateInstance(rotator.getAngleInRadians(), centerX, centerY)
                .createTransformedShape(transformed);

        return transformed;
    }

    private static class CollisionVisualization implements Drawable {

        private final java.util.List<Shape> shapes;
        private boolean tictoc = false;
        Color myO = new Color(255,99,71, 150);

        CollisionVisualization() {
            this.shapes = new ArrayList<Shape>();
        }
        CollisionVisualization(Shape shipShape) {
            this.shapes = new ArrayList<Shape>();
            this.shapes.add(shipShape);
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
            if(tictoc == false)
            {
                graphics2D.setColor(myO);
                AffineTransform scaler = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom());
                for (Shape shape : shapes) {
                    graphics2D.fill(scaler.createTransformedShape(shape));
                }
                tictoc = true;
            }
            else {
                map.getView().repaint();
                tictoc = false;
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

    public class BumpableWithShape {
        Shape shape;
        Decorator bumpable;
        String type;
        BumpableWithShape(Decorator bumpable, Shape shape, String type) {
            this.bumpable = bumpable;
            this.shape = shape;
            this.type = type;
        }
    }
}