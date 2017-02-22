package mic.manuvers;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Created by amatheny on 2/17/17.
 */
public enum CurvedPaths implements ManeuverPath {
    LBk1(80 * 2.825, true, true),
    RBk1(80 * 2.825, false, true),
    LBk2(130 * 2.825, true, true),
    RBk2(130 * 2.825, false, true),
    LBk3(180 * 2.825, true, true),
    RBk3(180 * 2.825, false, true),

    LT1(35 * 2.825, true, false),
    RT1(35 * 2.825, false, false),
    LT2(62.5 * 2.825, true, false),
    RT2(62.5 * 2.825, false, false),
    LT3(90 * 2.825, true, false),
    RT3(90 * 2.825, false, false);

    private final boolean bank;
    boolean left;
    private double radius;

    CurvedPaths(double radius, boolean left, boolean bank) {
        this.radius = radius;
        this.left = left;
        this.bank = bank;
    }
    public double getFinalAngleOffset() {
        return this.bank ? 45 : 90;
    }
    private class Vector {
        public double x, y;
        Vector(double x, double y) { this.x = x; this.y = y; }
    }

    // ------- //
    //  banks
    // ------- //

    private PathPart getBankPart(double percentage, double baseLength, double arcLength) {
        double currentDistance = percentage * (baseLength + arcLength);
        Vector frontPosition = getBankFrontPosition(currentDistance, baseLength, arcLength);
        Vector backPosition = getBankBackPosition(currentDistance, baseLength, arcLength);
        double angle = 270 - (calcAngle(backPosition, frontPosition) / (Math.PI * 2) * 360.0);
        double x = (frontPosition.x + backPosition.x) / 2;
        double y = (frontPosition.y + backPosition.y) / 2;
        if (this.left) {
            return new PathPart(-x, y, -angle);
        } else {
            return new PathPart(x, y, angle);
        }
    }
    private Vector getBankFrontPosition(double currentDistance, double baseLength, double arcLength) {
        if (currentDistance < arcLength) {
            double alpha = currentDistance / arcLength * (Math.PI / 4.0);
            double x = this.radius - (Math.cos(alpha) * this.radius);
            double y = (Math.sin(alpha) * this.radius) + (baseLength / 2); // upwards
            return new Vector(x, -y);
        } else {
            double startX = this.radius - (Math.cos(Math.PI / 4.0) * this.radius);
            double startY = (Math.sin(Math.PI / 4.0) * this.radius) + (baseLength / 2); // upwards
            double x = startX + ((currentDistance - arcLength) / Math.sqrt(2));
            double y = startY + ((currentDistance - arcLength) / Math.sqrt(2)); // upwards
            return new Vector(x, -y);
        }
    }
    private Vector getBankBackPosition(double currentDistance, double baseLength, double arcLength) {
        if (currentDistance < baseLength) {
            double x = 0;
            double y = -(baseLength / 2) + currentDistance; // upwards
            return new Vector(x, -y);
        } else {
            double alpha = (currentDistance - baseLength) / arcLength * (Math.PI / 4.0);
            double x = this.radius - (Math.cos(alpha) * this.radius);
            double y = (baseLength / 2) + (Math.sin(alpha) * this.radius); // upwards
            return new Vector(x, -y);
        }
    }

    // ------- //
    //  turns  //
    // ------- //

    private PathPart getTurnPart(double percentage, double baseLength, double arcLength) {
        double currentDistance = percentage * (baseLength + arcLength);
        Vector frontPosition = getTurnFrontPosition(currentDistance, baseLength, arcLength);
        Vector backPosition = getTurnBackPosition(currentDistance, baseLength, arcLength);
        double angle = 270 - (calcAngle(backPosition, frontPosition) / (Math.PI * 2) * 360.0);
        double x = (frontPosition.x + backPosition.x) / 2;
        double y = (frontPosition.y + backPosition.y) / 2;
        if (this.left) {
            return new PathPart(-x, y, -angle);
        } else {
            return new PathPart(x, y, angle);
        }
    }
    private Vector getTurnFrontPosition(double currentDistance, double baseLength, double arcLength) {
        if (currentDistance < arcLength) {
            double alpha = currentDistance / arcLength * (Math.PI / 2.0);
            double x = this.radius - (Math.cos(alpha) * this.radius);
            double y = (Math.sin(alpha) * this.radius) + (baseLength / 2); // upwards
            return new Vector(x, -y);
        } else {
            double x = (currentDistance - arcLength) + this.radius;
            double y = this.radius + (baseLength / 2); // upwards
            return new Vector(x, -y);
        }
    }
    private Vector getTurnBackPosition(double currentDistance, double baseLength, double arcLength) {
        if (currentDistance < baseLength) {
            double x = 0;
            double y = -(baseLength / 2) + currentDistance; // upwards
            return new Vector(x, -y);
        } else {
            double alpha = (currentDistance - baseLength) / arcLength * (Math.PI / 2.0);
            double x = this.radius - (Math.cos(alpha) * this.radius);
            double y = (baseLength / 2) + (Math.sin(alpha) * this.radius); // upwards
            return new Vector(x, -y);
        }
    }

    // ---------------------------------------- //
    //  use banks and turns to calculate paths  //
    // ---------------------------------------- //

    private double calcArcLength() {
        return 2 * Math.PI * this.radius / (this.bank ? 8.0 : 4.0);
    }
    private double calcAngle(Vector origin, Vector target) {
        if (target.x > origin.x) {
            return Math.atan((target.y - origin.y) / (target.x - origin.x));
        } else {
            return Math.PI + Math.atan((target.y - origin.y) / (target.x - origin.x));
        }
    }

    public List<PathPart> getPathParts(int numSegments, double baseOffset, boolean isLargeBase) {
        // init
        List<PathPart> parts = Lists.newArrayList();
        double baseLength = isLargeBase ? 2.0 * 113.0 : 113.0;
        double arcLength = calcArcLength();
        // calculate
        for (int i = 0; i < numSegments; i++) {
            double percentage = (double)i / (double)numSegments;
            if (this.bank) {
                parts.add(getBankPart(percentage, baseLength, arcLength));
            } else {
                parts.add(getTurnPart(percentage, baseLength, arcLength));
            }
        }
        // done
        return parts;
    }
}
