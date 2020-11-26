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
import static mic.Util.shapesOverlap;

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
    public MapVisualizations previousCollisionVisualization = null;


    public TemplateOverlapCheckDecorator() {
        this(null);
    }

    public TemplateOverlapCheckDecorator(GamePiece piece) {
        setInner(piece);
        this.testRotator = getRotator();
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

    @Override
    public Command keyEvent(KeyStroke stroke) {
        //Any keystroke made on a ship will remove the orange shades
        previousCollisionVisualization = new MapVisualizations();

        //check to see if 'c' was pressed - should be for obstacle collision (asteroid, debris and gas cloud)
        //since this'll be often used with collision aides spawned after hitting 'c' after a ship has overlapped a final destination
        //this has been modified in November 2020 to only pick the real template shape, deduced from the collision aide (which is the same template, but extended with straight 3's on both ends

        if(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false).equals(stroke)) {
            java.util.List<BumpableWithShape> otherBumpableShapes = getBumpablesWithShapes();

            boolean isCollisionOccuring = findCollidingEntity(getBumpableCompareShape(this), otherBumpableShapes) != null ? true : false;
            //backtracking requested with a detected bumpable overlap, deal with it
            if (isCollisionOccuring) {
                Command innerCommand = piece.keyEvent(stroke);

                //paint the template orange and the culprits too
                announceBumpAndPaint(otherBumpableShapes);

                //Add all the detected overlapping shapes to the map drawn components here
                if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                    innerCommand.append(previousCollisionVisualization);
                    previousCollisionVisualization.execute();
                }
                return innerCommand;
            }
            else  logToChatWithTime("This template does not overlap with an obstacle.");
        }

        //check to see if 's' was pressed - should be for ships (ie Starbird Slash) for cross-a-ship abilities
        if(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false).equals(stroke)) {

            java.util.List<BumpableWithShape> otherBumpableShapes = getBumpablesWithShapesShipEdition();

            boolean isCollisionOccuring = findCollidingEntity(getBumpableCompareShape(this), otherBumpableShapes) != null ? true : false;
            //backtracking requested with a detected bumpable overlap, deal with it
            if (isCollisionOccuring) {
                Command innerCommand = piece.keyEvent(stroke);

                //paint the template orange and the culprits too
                announceBumpAndPaint(otherBumpableShapes);

                //Add all the detected overlapping shapes to the map drawn components here
                if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                    innerCommand.append(previousCollisionVisualization);
                    previousCollisionVisualization.execute();
                }
                return innerCommand;
            }
            else  logToChatWithTime("This template does not overlap with an obstacle.");
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
                }else if (bumpedBumpable.type.equals("GasCloud")) {
                    String bumpAlertString = "* --- Overlap detected with your template and a gas cloud.";
                    logToChatWithTime(bumpAlertString);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                }else if (bumpedBumpable.type.equals("Remote")) {
                    String bumpAlertString = "* --- Overlap detected with your template and a remote.";
                    logToChatWithTime(bumpAlertString);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                }else if (bumpedBumpable.type.equals("Ship")) {
                    String bumpAlertString = "* --- Overlap detected with your template and " + bumpedBumpable.shipName + ".";
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
        for (BumpableWithShape bumpable : OverlapCheckManager.getBumpablesOnMap(false, null)) {
            if (getId().equals(bumpable.bumpable.getId())) {
                continue;
            }
            bumpables.add(bumpable);
        }
        return bumpables;
    }

    private java.util.List<BumpableWithShape> getBumpablesWithShapesShipEdition() {
        java.util.List<BumpableWithShape> bumpables = Lists.newLinkedList();
        for (BumpableWithShape bumpable : OverlapCheckManager.getBumpablesOnMap(true, null)) {
            if (getId().equals(bumpable.bumpable.getId())) {
                continue;
            }
            bumpables.add(bumpable);
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

}