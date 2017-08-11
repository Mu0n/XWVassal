package mic;

/**
 * Created by mjuneau on 2017-04-26.
 */


import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.*;

import static mic.Util.logToChat;
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
        this.testRotator = getRotator();
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
            java.util.List<BumpableWithShape> otherBumpableShapes = getBumpablesWithShapes();

            boolean isCollisionOccuring = findCollidingEntity(getBumpableCompareShape(this), otherBumpableShapes) != null ? true : false;
            //backtracking requested with a detected bumpable overlap, deal with it
            if (isCollisionOccuring) {
                Command innerCommand = piece.keyEvent(stroke);

                //paint the template orange and the culprits too
                announceBumpAndPaint(otherBumpableShapes);

                //Add all the detected overlapping shapes to the map drawn components here
                if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getCount() > 0){

                    final java.util.Timer timer = new java.util.Timer();
                    timer.schedule(new TimerTask() {
                        int count = 0;
                        @Override
                        public void run() {
                            try{
                                previousCollisionVisualization.draw(getMap().getView().getGraphics(),getMap());
                                count++;
                                if(count == NBFLASHES * 2) {
                                    getMap().removeDrawComponent(previousCollisionVisualization);
                                    previousCollisionVisualization.shapes.clear();
                                    timer.cancel();
                                }
                            } catch (Exception e) {

                            }
                        }
                    }, 0,DELAYBETWEENFLASHES);
                }



                return innerCommand;
            }
        }

        return piece.keyEvent(stroke);
    }

    private void announceBumpAndPaint(java.util.List<BumpableWithShape> otherBumpableShapes) {
        Shape theShape = getBumpableCompareShape(this);

        java.util.List<BumpableWithShape> collidingEntities = findCollidingEntities(theShape, otherBumpableShapes);

        int howManyBumped = 0;
        for (BumpableWithShape bumpedBumpable : collidingEntities) {
            if (DRAW_COLLISIONS) {
                if (bumpedBumpable.type.equals("Asteroid")) {
                    String bumpAlertString = "* --- Overlap detected with your template and an asteroid.";
                    logToChatWithTime(bumpAlertString);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Debris")) {
                    String bumpAlertString = "* --- Overlap detected with your template and a debris cloud.";
                    logToChatWithTime(bumpAlertString);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Mine")) {
                    String bumpAlertString = "* --- Overlap detected with your template and a mine.";
                    logToChatWithTime(bumpAlertString);
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
            } else if (piece.getState().contains("this_is_a_bomb")) {
                bumpables.add(new BumpableWithShape((Decorator)piece, "Mine", false));
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
}