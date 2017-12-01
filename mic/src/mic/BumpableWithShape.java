package mic;

import VASSAL.counters.Decorator;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.NonRectangular;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Created by mjuneau on 2017-06-08.
 */

//TO DO great place to add nub to corner distance, blue line
enum chassisInfo{
    unknown("unknown", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    small("small", 113.0, 113.0, 9.0, 8.3296, 40.45, 44.52),
    large("large", 226.0, 226.0, 10.0, 11.1650, 42.025, 0.0),
    hugeLittle("GR-75 size", 226.0, 551.0, 10.0, 11.1650, 40.45, 0.0),
    hugeBig("CR90 size", 226.0, 635.0, 10.0, 11.1650, 40.45, 0.0);

    private final String chassisName;
    private final double width;
    private final double height;
    private final double nubFudge;
    private final double cornerToFiringArc;
    private final double arcHalfAngle;
    private final double bullseyeWidth;

    chassisInfo(String chassisName, double width, double height, double nubFudge, double cornerToFiringArc, double arcHalfAngle, double bullseyeWidth) {
        this.chassisName = chassisName;
        this.width = width;
        this.height = height;
        this.nubFudge = nubFudge;
        this.cornerToFiringArc = cornerToFiringArc;
        this.arcHalfAngle = arcHalfAngle;
        this.bullseyeWidth = bullseyeWidth;
    }
    public String getChassisName() { return this.chassisName; }
    public double getWidth() { return this.width; }
    public double getHeight() { return this.height; }
    public double getNubFudge() { return this.nubFudge; }
    public double getCornerToFiringArc() { return this.cornerToFiringArc; }
    public double getArcHalfAngle() { return this.arcHalfAngle; }

    public double getBullsEyeWidth() {return this.bullseyeWidth;}
}
public class BumpableWithShape {
    Shape shape;
    Shape rectWithNoNubs;
    Decorator bumpable;
    String type;
    String shipName = "";
    String pilotName = "";
    chassisInfo chassis = chassisInfo.unknown;

    private Point2D.Double frontLeftArcBase = new Point2D.Double(0.0, 0.0); //used to get from center to a ship's arc start points
    private Point2D.Double frontRightArcBase = new Point2D.Double(0.0, 0.0);

    private Point2D.Double frontLeftArcEnd = new Point2D.Double(0.0, 0.0); //end of the edge-of-arc segment
    private Point2D.Double frontRightArcEnd = new Point2D.Double(0.0, 0.0);

    private Point2D.Double backLeftArcBase = new Point2D.Double(0.0, 0.0); // back arc start points
    private Point2D.Double backRightArcBase = new Point2D.Double(0.0, 0.0);

    private Point2D.Double backLeftArcEnd = new Point2D.Double(0.0, 0.0); // back arc end points
    private Point2D.Double backRightArcEnd = new Point2D.Double(0.0, 0.0);

    private Point2D.Double leftMidBase = new Point2D.Double(0.0, 0.0); //left mid point on the base
    private Point2D.Double rightMidBase = new Point2D.Double(0.0, 0.0); //right mid point on the base

    private Point2D.Double leftMidEnd = new Point2D.Double(0.0, 0.0); //end of mid segment
    private Point2D.Double rightMidEnd = new Point2D.Double(0.0, 0.0);

    private Point2D.Double upLeftVertex = new Point2D.Double(0.0, 0.0);
    private Point2D.Double upRightVertex = new Point2D.Double(0.0, 0.0);

    private Point2D.Double downLeftVertex = new Point2D.Double(0.0, 0.0);
    private Point2D.Double downRightVertex = new Point2D.Double(0.0, 0.0);


    private Point2D.Double leftBullseye = new Point2D.Double(0.0, 0.0);
    private Point2D.Double rightBullseye = new Point2D.Double(0.0, 0.0);


    private Point2D.Double leftFrontalEnd = new Point2D.Double(0.0, 0.0);//these last 4 are for finding a perpendicular intersection to the defender in the weird front aux arc and mobile case
    private Point2D.Double rightFrontalEnd = new Point2D.Double(0.0, 0.0);

    private Point2D.Double leftBackwardEnd = new Point2D.Double(0.0, 0.0);
    private Point2D.Double rightBackwardEnd = new Point2D.Double(0.0, 0.0);



    public ArrayList<Point2D.Double> tPts = new ArrayList<Point2D.Double>(); //array of transformed (rotated and translated) special points

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
        this.figureVertices();
        this.figureOutLocalPoints(3);
    }
    public ArrayList<Point2D.Double> getVertices(){
        figureVertices();
        ArrayList<Point2D.Double> list = new ArrayList<Point2D.Double>();
        list.add(upLeftVertex);
        list.add(upRightVertex);
        list.add(downRightVertex);
        list.add(downLeftVertex);
        return list;
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
    public void figureVertices(){
        double angle = getAngle();
        Point center = bumpable.getPosition();
        double halfWidth = getChassisWidth()/2.0;
        double halfHeight = getChassisHeight()/2.0;

        //top left
        upLeftVertex = new Point2D.Double(Util.rotX(-halfWidth, -halfHeight, angle) + center.getX(),
                Util.rotY(-halfWidth, -halfHeight, angle) + center.getY());
        //top right
        upRightVertex = new Point2D.Double(Util.rotX(halfWidth, -halfHeight, angle) + center.getX(),
                Util.rotY(halfWidth, -halfHeight, angle) + center.getY());
        //bottom right
        downRightVertex = new Point2D.Double(Util.rotX(halfWidth, halfHeight, angle) + center.getX(),
                Util.rotY(halfWidth, halfHeight, angle) + center.getY());
        //bottom left
        downLeftVertex = new Point2D.Double(Util.rotX(-halfWidth, halfHeight, angle) + center.getX(),
                Util.rotY(-halfWidth, halfHeight, angle) + center.getY());
    }

    //used to cut a triangle-shaped hole in the best blue band inside AutoRange, front aux or mobile turret autoranges, when the ship is parallel and close the front or back, rare usage method
    public Shape getFrontSubstractionTriangle(Boolean isLeft){
        //local coords first

        GeneralPath tri = new GeneralPath();
        double lastDirection = -1.0;

        double startX = frontLeftArcBase.x, startY = frontLeftArcBase.y;
        if(isLeft == false){
            startX = frontRightArcBase.x;
            startY = frontRightArcBase.y;
            lastDirection = 1.0;
        }
        double deltaY = getTriMaxVert();

        tri.moveTo(startX, startY);
        tri.lineTo(startX, startY - deltaY);
        tri.lineTo(startX + lastDirection * chassis.getCornerToFiringArc(),startY - deltaY);

        double centerX = bumpable.getPosition().getX();
        double centerY = bumpable.getPosition().getY();

        Shape transformed = AffineTransform
                .getTranslateInstance(centerX,centerY)
                .createTransformedShape(tri);

        transformed = AffineTransform
                .getRotateInstance(getAngleInRadians(), centerX, centerY)
                .createTransformedShape(transformed);
        return transformed;
    }
    public double getTriMaxVert(){
        return chassis.getCornerToFiringArc()/(Math.PI*Math.tan(chassis.getArcHalfAngle()/180.0));
    }


    public void figureOutLocalPoints(int maxRange){
        double pixelMaxRange = maxRange * 282.5f;
        double halfsize = getChassisWidth()/2.0;

        double arcAngleInRad = Math.PI*chassis.getArcHalfAngle()/180.0; //half angle of the arc being used TO DO: get the proper mobile turret arc angle, front aux arc, etc or just ignore it inside the switch

        frontLeftArcBase = new Point2D.Double(-halfsize + chassis.getCornerToFiringArc(), -halfsize);
        frontRightArcBase = new Point2D.Double(halfsize - chassis.getCornerToFiringArc(), -halfsize);

        frontLeftArcEnd = new Point2D.Double(- Math.sin(arcAngleInRad)*pixelMaxRange,- Math.cos(arcAngleInRad)*pixelMaxRange);
        frontRightArcEnd = new Point2D.Double(- frontLeftArcEnd.x, frontLeftArcEnd.y);

        backLeftArcBase = new Point2D.Double(-halfsize + chassis.getCornerToFiringArc(),halfsize);
        backRightArcBase = new Point2D.Double(halfsize - chassis.getCornerToFiringArc(),halfsize);

        backLeftArcEnd = new Point2D.Double(- Math.sin(arcAngleInRad)*pixelMaxRange,Math.cos(arcAngleInRad)*pixelMaxRange);
        backRightArcEnd= new Point2D.Double(- backLeftArcEnd.x, backLeftArcEnd.y);

        leftMidBase= new Point2D.Double(-halfsize, 0.0);
        rightMidBase= new Point2D.Double(halfsize, 0.0);

        leftMidEnd = new Point2D.Double(-halfsize - pixelMaxRange, 0.0);
        rightMidEnd = new Point2D.Double(halfsize + pixelMaxRange, 0.0);

        leftBullseye = new Point2D.Double(-chassis.getBullsEyeWidth()/2.0, -halfsize);
        rightBullseye = new Point2D.Double(chassis.getBullsEyeWidth()/2.0, -halfsize);

        leftFrontalEnd = new Point2D.Double(-halfsize + chassis.getCornerToFiringArc(), -halfsize  - pixelMaxRange);//these last 4 are for finding a perpendicular intersection to the defender in the weird front aux arc and mobile case
        rightFrontalEnd = new Point2D.Double(halfsize - chassis.getCornerToFiringArc(), -halfsize  - pixelMaxRange);

        leftBackwardEnd = new Point2D.Double(-halfsize + chassis.getCornerToFiringArc(), halfsize  + pixelMaxRange);
        rightBackwardEnd = new Point2D.Double(halfsize - chassis.getCornerToFiringArc(), halfsize  + pixelMaxRange);


    }

    public void refreshSpecialPoints(){
        figureOutSpecialPoints(3);
    }

    public void refreshSpecialPoints(int maxRange){
        figureOutSpecialPoints(maxRange);
    }

    //gets the firing arc edges on a ship. First 2 are on the ship (left and right), last 2 are at the end of the arc edge (left and right)
    //Use these points to reject a best (shortest) autorange firing line if it crosses these edges; eg a ship that could be in turret range 1 but it out of arc
    private void figureOutSpecialPoints(int maxRange){

        double pixelMaxRange = maxRange * 282.5f;
        Point center = bumpable.getPosition();
        double angle = getAngle();

        tPts.add(getATransformedPoint(frontLeftArcBase.x, frontLeftArcBase.y, 0.0, 0.0, angle, center.getX(), center.getY())); //0
        tPts.add(getATransformedPoint(frontRightArcBase.x, frontRightArcBase.y, 0.0, 0.0, angle, center.getX(), center.getY()));  //1

        tPts.add(getATransformedPoint(frontLeftArcBase.x, frontLeftArcBase.y, frontLeftArcEnd.x, frontLeftArcEnd.y, angle, center.getX(), center.getY())); //2
        tPts.add(getATransformedPoint(frontRightArcBase.x, frontRightArcBase.y, frontRightArcEnd.x, frontRightArcEnd.y, angle, center.getX(), center.getY())); //3

        tPts.add(getATransformedPoint(backRightArcBase.x, backRightArcBase.y, 0.0, 0.0, angle, center.getX(), center.getY())); //4
        tPts.add(getATransformedPoint(backLeftArcBase.x, backLeftArcBase.y, 0.0, 0.0, angle, center.getX(), center.getY()));  //5

        tPts.add(getATransformedPoint(backRightArcBase.x, backRightArcBase.y, backRightArcEnd.x, backRightArcEnd.y, angle, center.getX(), center.getY())); //6
        tPts.add(getATransformedPoint(backLeftArcBase.x, backLeftArcBase.y, backLeftArcEnd.x, backLeftArcEnd.y, angle, center.getX(), center.getY())); //7

        tPts.add(getATransformedPoint(leftMidBase.x, leftMidBase.y, 0.0, 0.0, angle, center.getX(), center.getY())); //8
        tPts.add(getATransformedPoint(rightMidBase.x, rightMidBase.y, 0.0, 0.0, angle, center.getX(), center.getY())); //9

        tPts.add(getATransformedPoint(leftMidEnd.x, leftMidEnd.y, 0.0, 0.0, angle, center.getX(), center.getY())); //10
        tPts.add(getATransformedPoint(rightMidEnd.x, rightMidEnd.y, 0.0, 0.0, angle, center.getX(), center.getY()));  //11

        tPts.add(getATransformedPoint(leftBullseye.x, leftBullseye.y, 0.0, 0.0, angle, center.getX(), center.getY())); //12
        tPts.add(getATransformedPoint(leftBullseye.x, leftBullseye.y, 0.0, -pixelMaxRange, angle, center.getX(), center.getY())); //13
        tPts.add(getATransformedPoint(rightBullseye.x, rightBullseye.y, 0.0, 0.0, angle, center.getX(), center.getY())); //14
        tPts.add(getATransformedPoint(rightBullseye.x, rightBullseye.y, 0.0, -pixelMaxRange, angle, center.getX(), center.getY())); //15

        tPts.add(getATransformedPoint(leftFrontalEnd.x, leftFrontalEnd.y, 0.0, 0.0, angle, center.getX(), center.getY())); //16
        tPts.add(getATransformedPoint(rightFrontalEnd.x, rightFrontalEnd.y, 0.0,  0.0, angle, center.getX(), center.getY())); //17
        tPts.add(getATransformedPoint(leftBackwardEnd.x, leftBackwardEnd.y, 0.0, 0.0, angle, center.getX(), center.getY())); //18
        tPts.add(getATransformedPoint(rightBackwardEnd.x, rightBackwardEnd.y, 0.0, 0.0, angle, center.getX(), center.getY())); //19
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
