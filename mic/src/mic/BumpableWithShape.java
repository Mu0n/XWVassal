package mic;

import VASSAL.counters.Decorator;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.NonRectangular;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Created by mjuneau on 2017-06-08.
 */
public class BumpableWithShape {
    Shape shape;
    Shape rectWithNoNubs;
    Decorator bumpable;
    String type;
    String shipName = "";
    String pilotName = "";

    BumpableWithShape(Decorator bumpable, String type, boolean wantFlip) {
        this.bumpable = bumpable;
        this.shape = wantFlip ? getBumpableCompareShapeButFlip(bumpable) : getBumpableCompareShape(bumpable);
        this.type = type;
    }
    BumpableWithShape(Decorator bumpable, String type, String pilotName, String shipName) {
        this.bumpable = bumpable;
        this.shape = getBumpableCompareShape(bumpable);
        this.rectWithNoNubs = getRectWithNoNubs();
        this.type = type;
        this.pilotName = pilotName;
        this.shipName = shipName;
    }

public double[] getVertices(){
        double angle = getAngleInRadians();
        Point center = bumpable.getPosition();
        double halfsize = 56.5;
        double[] vertices = new double[8];

        if(rectWithNoNubs.getBounds().getWidth() > 140) halfsize = 113.0;
        //top left
        vertices[0] = Util.rotX(-halfsize, -halfsize, angle) + center.getX();
        vertices[1] = Util.rotY(-halfsize, -halfsize, angle) + center.getY();
        //top right
        vertices[2] = Util.rotX(halfsize, -halfsize, angle) + center.getX();
        vertices[3] = Util.rotY(halfsize, -halfsize, angle) + center.getY();
        //bottom right
        vertices[4] = Util.rotX(halfsize, halfsize, angle) + center.getX();
        vertices[5] = Util.rotY(halfsize, halfsize, angle) + center.getY();
        //bottom left
        vertices[6] = Util.rotX(-halfsize, halfsize, angle) + center.getX();
        vertices[7] = Util.rotY(-halfsize, halfsize, angle) + center.getY();
        return vertices;
}

    public Shape getRectWithNoNubs() {
        Shape rawShape = getRawShape(bumpable);
        Shape theSquare = new Rectangle2D.Double(0.0f, 0.0f, 0.0f, 0.0f);
        //small
        if(rawShape.getBounds().height < 140) {
            theSquare = new Rectangle2D.Double(-56.5f, -56.5f, 113.0f, 113.0f);
        }
        //large
        else if (rawShape.getBounds().height < 250) {
            theSquare = new Rectangle2D.Double(-113.0f, -113.0f, 226.0f, 226.0f);
        }

        //GR-75 huge, Gozanti, C-ROC
else if(rawShape.getBounds().height < 570){

        }
        //CR90, Raider

        else return null;

        if(theSquare.getBounds().getWidth() != 0.0f) {
            double centerX = bumpable.getPosition().getX();
            double centerY = bumpable.getPosition().getY();

            Shape transformed = AffineTransform
                    .getTranslateInstance(centerX,centerY)
                    .createTransformedShape(theSquare);

        transformed = AffineTransform
                .getRotateInstance(getAngleInRadians(), centerX, centerY)
                .createTransformedShape(transformed);

        return transformed;
        }

        return null;

    }

    private double getAngleInRadians(){
        FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(bumpable), FreeRotator.class));
        return rotator.getAngleInRadians();
    }

    private double getAngle(){
        FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(bumpable), FreeRotator.class));
        return rotator.getAngle();
    }
    /**
     * Finds non-rectangular mask layer of provided ship.  This is the shape with only the base
     * and nubs
     *
     * @param bumpable
     * @return
     */
    public static Shape getRawShape(Decorator bumpable) {
        return Decorator.getDecorator(Decorator.getOutermost(bumpable), NonRectangular.class).getShape();
    }

    /**
     * Finds raw ship mask and translates and rotates it to the current position and heading
     * of the ship
     *
     * @param bumpable
     * @return Translated ship mask
     */
    public static Shape getBumpableCompareShape(Decorator bumpable) {
        Shape rawShape = getRawShape(bumpable);
        Shape transformed = AffineTransform
                .getTranslateInstance(bumpable.getPosition().getX(), bumpable.getPosition().getY())
                .createTransformedShape(rawShape);

        FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(bumpable), FreeRotator.class));
        double centerX = bumpable.getPosition().getX();
        double centerY = bumpable.getPosition().getY();
        transformed = AffineTransform
                .getRotateInstance(rotator.getAngleInRadians(), centerX, centerY)
                .createTransformedShape(transformed);

        return transformed;
    }
    public static Shape getBumpableCompareShapeButFlip(Decorator bumpable) {
        Shape rawShape = getRawShape(bumpable);
        Shape transformed = AffineTransform.getScaleInstance(-1, 1).createTransformedShape(rawShape);
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
