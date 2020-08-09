package mic.manuvers;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Created by amatheny on 2/17/17.
 * Path calculations implemented by haslo on 2017-02-24
 */
public enum CurvedPaths implements ManeuverPath {
    LBk1(80 * 2.825, true, true, false, 0.95, false),
    RBk1(80 * 2.825, false, true, false, 0.95, false),
    LBk2(130 * 2.825, true, true, false, 0.98, false),
    RBk2(130 * 2.825, false, true, false, 0.98, false),
    LBk3(180 * 2.825, true, true, false, 1.0, false),
    RBk3(180 * 2.825, false, true, false, 1.0, false),

    RevLB1(80 * 2.825, true, true, true, 0.95, false),
    RevRB1(80 * 2.825, false, true, true, 0.95, false),

    LT1(35 * 2.825, true, false, false, 0.85, false),
    RT1(35 * 2.825, false, false, false, 0.85, false),
    LT2(62.5 * 2.825, true, false, false, 0.95, false),
    RT2(62.5 * 2.825, false, false, false, 0.95, false),
    LT3(90 * 2.825, true, false, false, 1.0, false),
    RT3(90 * 2.825, false, false, false, 1.0, false),

    SSLT1(35 * 2.825, true, false, false, 0.85, true),
    SSRT1(35 * 2.825, false, false, false, 0.85, true),
    SSLT2(62.5 * 2.825, true, false, false, 0.95, true),
    SSRT2(62.5 * 2.825, false, false, false, 0.95, true),
    SSLT3(90 * 2.825, true, false, false, 1.0, true),
    SSRT3(90 * 2.825, false, false, false, 1.0, true),

    SSLBk1(80 * 2.825, true, true, false, 0.95, true),
    SSRBk1(80 * 2.825, false, true, false, 0.95, true),
    SSLBk2(130 * 2.825, true, true, false, 0.98, true),
    SSRBk2(130 * 2.825, false, true, false, 0.98, true),
    SSLBk3(180 * 2.825, true, true, false, 1.0, true),
    SSRBk3(180 * 2.825, false, true, false, 1.0, true)
    ;

    private boolean bank, reverse, sideslip;
    boolean left;
    private double radius, approximationMultiplier;

    CurvedPaths(double radius, boolean left, boolean bank, boolean reverse, double approximationMultiplier, boolean sideslip) {
        this.radius = radius;
        this.left = left;
        this.bank = bank;
        this.reverse = reverse;
        this.approximationMultiplier = approximationMultiplier;
        this.sideslip = sideslip;
    }

    public double getFinalAngleOffset() {
        return this.bank ? 45 : 90;
    }

    private class Vector {
        public double x, y;

        Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // --------- //
    //  utility
    // --------- //

    /**
     * length of bank and turn arcs
     */
    public double getPathLength() {
        return 2 * Math.PI * this.radius / (this.bank ? 8.0 : 4.0);
    }

    /**
     * angle that a vector from origin to target has in the Vassal map space
     */
    private double calcAngle(Vector origin, Vector target) {
        if (Math.abs(target.x - origin.x) < 10e-6) {
            return (Math.PI / 2) * (target.y > origin.y ? 1 : -1);
        } else if (target.x > origin.x) {
            return Math.atan((target.y - origin.y) / (target.x - origin.x));
        } else {
            return Math.PI + Math.atan((target.y - origin.y) / (target.x - origin.x));
        }
    }

    /**
     * scaling function for the input parameters to the position calculation methods - improves approximation by
     * reducing the errors introduced by bases getting "shorter" through Pythagoras; version for the front edge of the base
     */
    private double frontPercentage(double percentage, int whichSizeShip) {
        // small bases need less adjustment because their error is smaller (they're smaller relative to the template)
        double adjustedApproximationMultiplier = 0.0;
        switch(whichSizeShip)
        {
            case 1:
                adjustedApproximationMultiplier = Math.sqrt(this.approximationMultiplier);
                break;
            case 2:
                adjustedApproximationMultiplier = Math.sqrt(this.approximationMultiplier);
                break;
            case 3:
                adjustedApproximationMultiplier = this.approximationMultiplier;
                break;
        }
        // front goes faster in the first half, slower in the second
        if (percentage < 0.5) {
            return (2 - adjustedApproximationMultiplier) * percentage;
        } else {
            return ((2 - adjustedApproximationMultiplier) * 0.5) + (adjustedApproximationMultiplier * (percentage - 0.5));
        }
    }

    /**
     * scaling function for the input parameters to the position calculation methods - improves approximation by
     * reducing the errors introduced by bases getting "shorter" through Pythagoras; version for the back edge of the base
     */
    private double backPercentage(double percentage, int whichSizeShip) {
        // small bases need less adjustment because their error is smaller (they're smaller relative to the template)
        double adjustedApproximationMultiplier = 0.0;
        switch(whichSizeShip)
        {
            case 1:
                adjustedApproximationMultiplier = Math.sqrt(this.approximationMultiplier);
                break;
            case 2:
                adjustedApproximationMultiplier = Math.sqrt(this.approximationMultiplier);
                break;
            case 3:
                adjustedApproximationMultiplier = this.approximationMultiplier;
                break;
        }
        // front goes slower in the first half, faster in the second
        if (percentage < 0.5) {
            return adjustedApproximationMultiplier * percentage;
        } else {
            return (adjustedApproximationMultiplier * 0.5) + ((2 - adjustedApproximationMultiplier) * (percentage - 0.5));
        }
    }

    // ------- //
    //  banks
    // ------- //

    /**
     * calculate the position and angle of a bit of path; uses getBankFrontPosition and getBankBackPosition
     *
     * @param percentage  at what percentage (from 0 to 1) of the entire path should the position be calculated?
     * @param baseLength  length of the base to calculate
     * @param arcLength   pre-computed length of the arc that the template's center forms
     * @param whichSizeShip small and large bases need different computation
     * @return a PathPart with x, y and angle (degrees)
     */
    private PathPart getBankPart(double percentage, double baseLength, double arcLength, int whichSizeShip) {
        double currentFrontDistance = frontPercentage(percentage, whichSizeShip) * (baseLength + arcLength);
        double currentBackDistance = backPercentage(percentage, whichSizeShip) * (baseLength + arcLength);
        Vector frontPosition = getBankFrontPosition(currentFrontDistance, baseLength, arcLength);
        Vector backPosition = getBankBackPosition(currentBackDistance, baseLength, arcLength);
        double angle = 270 + (this.reverse ? 180 : 0) - (calcAngle(backPosition, frontPosition) / (Math.PI * 2) * 360.0);
        // the position we want to return is in the center between front and back
        double x = (frontPosition.x + backPosition.x) / 2;
        double y = (frontPosition.y + backPosition.y) / 2;

        if (this.left) {
            return new PathPart(-x, y, -angle);
        } else {
            return new PathPart(x, y, angle);
        }
    }

    /**
     * actual computation of x and y values of a bit of path for banks, for the front of the base
     */
    private Vector getBankFrontPosition(double currentDistance, double baseLength, double arcLength) {
        if (currentDistance < arcLength) {
            // first bit, the arc of the bank template
            double alpha = currentDistance / arcLength * (Math.PI / 4.0);
            double x = this.radius - (Math.cos(alpha) * this.radius);
            double y = (Math.sin(alpha) * this.radius) + (baseLength / 2); // upwards

            if(sideslip) return new Vector(y, -x);
            return new Vector(x, this.reverse ? y : -y);
        } else {
            // straight bit (template extension) at a 45° angle after the arc
            double startX = this.radius - (Math.cos(Math.PI / 4.0) * this.radius);
            double startY = (Math.sin(Math.PI / 4.0) * this.radius) + (baseLength / 2); // upwards
            double x = startX + ((currentDistance - arcLength) / Math.sqrt(2));
            double y = startY + ((currentDistance - arcLength) / Math.sqrt(2)); // upwards

            if(sideslip) return new Vector(y, -x);
            return new Vector(x, this.reverse ? y : -y);
        }
    }

    /**
     * actual computation of x and y values of a bit of path for banks, for the back of the base
     */
    private Vector getBankBackPosition(double currentDistance, double baseLength, double arcLength) {
        if (currentDistance < baseLength) {
            // go through the base first, straight (template extension)
            double x = 0;
            double y = -(baseLength / 2) + currentDistance; // upwards


            if(sideslip) return new Vector(y, -x);
            return new Vector(x, this.reverse ? y : -y);
        } else {
            // second bit, the arc of the bank template
            double alpha = (currentDistance - baseLength) / arcLength * (Math.PI / 4.0);
            double x = this.radius - (Math.cos(alpha) * this.radius);
            double y = (baseLength / 2) + (Math.sin(alpha) * this.radius); // upwards


            if(sideslip) return new Vector(y, -x);
            return new Vector(x, this.reverse ? y : -y);
        }
    }

    // ------- //
    //  turns  //
    // ------- //

    /**
     * calculate the position and angle of a bit of path; uses getTurnFrontPosition and getTurnBackPosition
     *
     * @param percentage  at what percentage (from 0 to 1) of the entire path should the position be calculated?
     * @param baseLength  length of the base to calculate
     * @param arcLength   pre-computed length of the arc that the template's center forms
     * @param whichSizeShip small and large bases need different computation
     * @return a PathPart with x, y and angle (degrees)
     */
    private PathPart getTurnPart(double percentage, double baseLength, double arcLength, int whichSizeShip) {
        double currentFrontDistance = frontPercentage(percentage, whichSizeShip) * (baseLength + arcLength);
        double currentBackDistance = backPercentage(percentage, whichSizeShip) * (baseLength + arcLength);
        Vector frontPosition = getTurnFrontPosition(currentFrontDistance, baseLength, arcLength);
        Vector backPosition = getTurnBackPosition(currentBackDistance, baseLength, arcLength);
        // the position we want to return is in the center between front and back
        double angle = 270 + (this.reverse ? 180 : 0) - (calcAngle(backPosition, frontPosition) / (Math.PI * 2) * 360.0);
        double x = (frontPosition.x + backPosition.x) / 2;
        double y = (frontPosition.y + backPosition.y) / 2;
        if (this.left) {
            return new PathPart(-x, y, -angle);
        } else {
            return new PathPart(x, y, angle);
        }
    }

    /**
     * actual computation of x and y values of a bit of path for turns, for the front of the base
     */
    private Vector getTurnFrontPosition(double currentDistance, double baseLength, double arcLength) {
        if (currentDistance < arcLength) {
            // first bit, the arc of the turn template
            double alpha = currentDistance / arcLength * (Math.PI / 2.0);
            double x = this.radius - (Math.cos(alpha) * this.radius);
            double y = (Math.sin(alpha) * this.radius) + (baseLength / 2); // upwards
            return new Vector(x, this.reverse ? y : -y);
        } else {
            // straight bit (template extension) horizontally after the arc
            double x = (currentDistance - arcLength) + this.radius;
            double y = this.radius + (baseLength / 2); // upwards
            return new Vector(x, this.reverse ? y : -y);
        }
    }

    /**
     * actual computation of x and y values of a bit of path for turns, for the back of the base
     */
    private Vector getTurnBackPosition(double currentDistance, double baseLength, double arcLength) {
        if (currentDistance < baseLength) {
            // go through the base first, straight (template extension)
            double x = 0;
            double y = -(baseLength / 2) + currentDistance; // upwards
            return new Vector(x, this.reverse ? y : -y);
        } else {
            // second bit, the arc of the turn template
            double alpha = (currentDistance - baseLength) / arcLength * (Math.PI / 2.0);
            double x = this.radius - (Math.cos(alpha) * this.radius);
            double y = (baseLength / 2) + (Math.sin(alpha) * this.radius); // upwards
            return new Vector(x, this.reverse ? y : -y);
        }
    }

    // ---------------------------------------- //
    //  use banks and turns to calculate paths  //
    // ---------------------------------------- //

    /**
     * The method that this entire class is about
     */
    public List<PathPart> getPathParts(int numSegments, double baseOffset, int whichSizeShip) {
        // init
        List<PathPart> parts = Lists.newArrayList();
        double baseLength = 0.0;
        switch(whichSizeShip)
        {
            case 1: //small
                baseLength = 113.0;
                break;
            case 2: //med
                baseLength = 169.0;
                break;
            case 3: //large
                baseLength = 226.0;
                break;
        }
        double arcLength = getPathLength();
        // calculate
        for (int i = 1; i <= numSegments; i++) {
            double percentage = (double) i / (double) numSegments;
            if (this.bank) {
                parts.add(getBankPart(percentage, baseLength, arcLength, whichSizeShip));
            } else {
                parts.add(getTurnPart(percentage, baseLength, arcLength, whichSizeShip));
            }
        }
        // done
        return parts;
    }
}
