package mic;

import VASSAL.counters.Decorator;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.NonRectangular;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static mic.Util.logToChat;

/**
 * Created by mjuneau on 2017-06-08.
 */

//TO DO great place to add nub to corner distance, blue line
enum chassisInfo{
    unknown("unknown", 0.0, 0.0, 0.0, 0.0, 0.0),
    small("small", 113.0, 113.0, 9.0, 8.3296, 40.45),
    large("large", 226.0, 226.0, 10.0, 11.1650, 42.025),
    hugeLittle("GR-75 size", 226.0, 551.0, 10.0, 11.1650, 40.45),
    hugeBig("CR90 size", 226.0, 635.0, 10.0, 11.1650, 40.45);

    private final String chassisName;
    private final double width;
    private final double height;
    private final double nubFudge;
    private final double cornerToFiringArc;
    private final double arcHalfAngle;

    chassisInfo(String chassisName, double width, double height, double nubFudge, double cornerToFiringArc, double arcHalfAngle) {
        this.chassisName = chassisName;
        this.width = width;
        this.height = height;
        this.nubFudge = nubFudge;
        this.cornerToFiringArc = cornerToFiringArc;
        this.arcHalfAngle = arcHalfAngle;
    }
    public String getChassisName() { return this.chassisName; }
    public double getWidth() { return this.width; }
    public double getHeight() { return this.height; }
    public double getNubFudge() { return this.nubFudge; }
    public double getCornerToFiringArc() { return this.cornerToFiringArc; }
    public double getArcHalfAngle() { return this.arcHalfAngle; }
}
public class BumpableWithShape {
    Shape shape;
    Shape rectWithNoNubs;
    Decorator bumpable;
    String type;
    String shipName = "";
    String pilotName = "";
    chassisInfo chassis = chassisInfo.unknown;
    int nubAdjust = 10;

    BumpableWithShape(Decorator bumpable, String type, boolean wantFlip) {
        this.bumpable = bumpable;
        this.shape = wantFlip ? getBumpableCompareShapeButFlip(bumpable) : getBumpableCompareShape(bumpable);
        this.type = type;
        this.chassis = figureOutChassis();
    }
    BumpableWithShape(Decorator bumpable, String type, String pilotName, String shipName) {
        this.bumpable = bumpable;
        this.shape = getBumpableCompareShape(bumpable);
        this.rectWithNoNubs = getRectWithNoNubs();
        this.type = type;
        this.pilotName = pilotName;
        this.shipName = shipName;
        this.chassis = figureOutChassis();
    }

    private chassisInfo figureOutChassis() {
        Shape rawShape = getRawShape(bumpable);
        double rawWidth = rawShape.getBounds().width;
        double rawHeight = rawShape.getBounds().height;

        chassisInfo result = chassisInfo.unknown;
        if(Double.compare(rawWidth,chassisInfo.small.getWidth())==0) {
            result= chassisInfo.small;
        }
        else if(Double.compare(rawWidth,chassisInfo.large.getWidth())==0
                && Double.compare(rawHeight,chassisInfo.large.getHeight()+chassis.large.getNubFudge())==0) {
            result= chassisInfo.large;
        }
        else if(Double.compare(rawWidth,chassisInfo.hugeLittle.getWidth())==0
                && Double.compare(rawHeight,chassisInfo.hugeLittle.getHeight()+chassis.hugeLittle.getNubFudge())==0) result= chassisInfo.hugeLittle;
        else if(Double.compare(rawWidth,chassisInfo.hugeBig.getWidth())==0
                && Double.compare(rawHeight,chassisInfo.hugeBig.getHeight()+chassis.hugeBig.getNubFudge())==0) result= chassisInfo.hugeBig;
        //logToChat("rawWidth " + Double.toString(rawWidth) + " rawHeight " + Double.toString(rawHeight) + " chassis " + result.getChassisName());
        return result;
    }

    public chassisInfo getChassis() {
        return chassis;
    }
    public double getChassisHeight(){
        return chassis.getHeight();
    }
    public double getChassisWidth(){
        return chassis.getWidth();
    }
    public ArrayList<Point2D.Double> getVertices(){
        double angle = getAngle();
        Point center = bumpable.getPosition();
        double halfWidth = getChassisWidth()/2.0;
        double halfHeight = getChassisHeight()/2.0;
        ArrayList<Point2D.Double> vertices = new ArrayList<Point2D.Double>();

        //top left
        vertices.add(new Point2D.Double(Util.rotX(-halfWidth, -halfHeight, angle) + center.getX(),
                Util.rotY(-halfWidth, -halfHeight, angle) + center.getY()));
        //top right
        vertices.add(new Point2D.Double(Util.rotX(halfWidth, -halfHeight, angle) + center.getX(),
                Util.rotY(halfWidth, -halfHeight, angle) + center.getY()));
        //bottom right
        vertices.add(new Point2D.Double(Util.rotX(halfWidth, halfHeight, angle) + center.getX(),
                Util.rotY(halfWidth, halfHeight, angle) + center.getY()));
        //bottom left
        vertices.add(new Point2D.Double(Util.rotX(-halfWidth, halfHeight, angle) + center.getX(),
                Util.rotY(-halfWidth, halfHeight, angle) + center.getY()));

        return vertices;
    }

    //gets the firing arc edges on a ship. First 2 are on the ship (left and right), last 2 are at the end of the arc edge (left and right)
    //Use these points to reject a best (shortest) autorange firing line if it crosses these edges; eg a ship that could be in turret range 1 but it out of arc
    public ArrayList<Point2D.Double> getFiringArcEdges(int whichOption, int maxRange){

        //whichOption: 1 = primary arc; 3 = front aux arcs; 4 = back aux arc; 5 = mobile side arc
        //common to any arc requested
        double pixelMaxRange = maxRange * 282.5f;
        double angle = getAngle();
        Point center = bumpable.getPosition();
        double halfsize = getChassisWidth()/2.0;
        ArrayList<Point2D.Double> firingArcEdgePoints = new ArrayList<Point2D.Double>();
        double firstStartX = 0.0, firstStartY = 0.0, secondStartX = 0.0, secondStartY = 0.0; //used to get from center to a ship's arc start point
        double firstEndX = 0.0, firstEndY = 0.0, secondEndX = 0.0, secondEndY = 0.0; //used to get the end of the arc lines

        double arcAngleInRad = Math.PI*chassis.getArcHalfAngle()/180.0; //half angle of the arc being used TO DO: get the proper mobile turret arc angle, front aux arc, etc or just ignore it inside the switch

        //specific to the arc type, in local ship coordinates assuming it's facing up. x axis goes to right, y axis goes to bottom
        switch(whichOption)
        {
            case 1: //primary arc
                firstStartX = -halfsize + chassis.getCornerToFiringArc();
                firstStartY = -halfsize;
                secondStartX = halfsize - chassis.getCornerToFiringArc();
                secondStartY = firstStartY;

                firstEndX = - Math.sin(arcAngleInRad)*pixelMaxRange;
                firstEndY = - Math.cos(arcAngleInRad)*pixelMaxRange;

                secondEndX = Math.sin(arcAngleInRad)*pixelMaxRange;
                secondEndY = firstEndY;
                break;
            case 4: //back aux arc
                firstStartX = -halfsize + chassis.getCornerToFiringArc();
                firstStartY = halfsize;
                secondStartX = halfsize - chassis.getCornerToFiringArc();
                secondStartY = halfsize;

                firstEndX = - Math.sin(arcAngleInRad)*pixelMaxRange;
                firstEndY = Math.cos(arcAngleInRad)*pixelMaxRange;

                secondEndX = Math.sin(arcAngleInRad)*pixelMaxRange;
                secondEndY = firstEndY;
                break;
            case 5: //mobile arc, ugh
                break;
            default:
                break;
        }

//back to common to any arc requested
        //calculate the rotated, translated coordinates of the firing arc start points
        firingArcEdgePoints.add(getATransformedPoint(firstStartX, firstStartY, 0.0, 0.0, angle, center.getX(), center.getY()));
        firingArcEdgePoints.add(getATransformedPoint(secondStartX, secondStartY, 0.0, 0.0, angle, center.getX(), center.getY()));

        //calculate the rotated, translated coordinates of the firing arc end points
        firingArcEdgePoints.add(getATransformedPoint(firstStartX, firstStartY, firstEndX, firstEndY, angle, center.getX(), center.getY()));
        firingArcEdgePoints.add(getATransformedPoint(secondStartX, secondStartY, secondEndX, secondEndY, angle, center.getX(), center.getY()));

//specific to the arc type
        if(whichOption == 2) //front aux arcs for Auzituck, Hound's Tooth
        {
            //TO DO, calculate extra 2 edges for the right arc
        }
        return firingArcEdgePoints;
    }

    Point2D.Double getATransformedPoint(double offX, double offY, double extraOffX, double extraOffY, double shipAngle, double centerX, double centerY)
    {
        //use OffX & OffY to get to the start of the arc. Use extraOffX & extraOffY to add the offset needed to get to the outlier end of an arc, if needed
        return new Point2D.Double(
                Util.rotX(offX + extraOffX, offY + extraOffY, shipAngle) + centerX,
                Util.rotY(offX + extraOffX, offY + extraOffY, shipAngle) + centerY);
    }

    public Shape getRectWithNoNubs() {
        Shape theSquare = new Rectangle2D.Double(0.0f, 0.0f, 0.0f, 0.0f);

        double halfWidth = getChassisWidth()/2.0;
        theSquare = new Rectangle2D.Double(-halfWidth, -halfWidth, 2.0*halfWidth, 2.0*halfWidth);

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

    public double getAngleInRadians(){
        FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(bumpable), FreeRotator.class));
        return rotator.getAngleInRadians();
    }

    public double getAngle(){
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
