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

    private static double QTR_PI = Math.PI / 4.0;
    private static double HALF_PI = Math.PI / 2.0;

    CurvedPaths(double radius, boolean left, boolean bank) {
        this.radius = radius;
        this.left = left;
        this.bank = bank;
    }

    public double getFinalAngleOffset() {
        return this.bank ? 45 : 90;
    }

    private double getPartMultipler() {
        return this.bank ? QTR_PI : HALF_PI;
    }

    public List<PathPart> getPathParts(int numSegments, double baseOffset) {
        List<PathPart> parts = Lists.newArrayList();
        //Extended straight back segment

        for (int i = 1; i <=numSegments; i++) {
            double angle = 0.0;
            double x = 0.0;
            double y = -baseOffset * ( i / (double) numSegments);

            parts.add(new PathPart(x, y, angle));
        }

        //Curved part
        for (int i = 1; i <= numSegments; i++) {
            double arg = (getPartMultipler() * i) / (double) numSegments;

            double y = -baseOffset - Math.sin(arg) * radius;
            double x = (-Math.cos(arg) + 1) * radius;
            double angle = -(i / (double) numSegments) * getFinalAngleOffset();

            if (this.left) {
                angle = -angle;
                x = -x;
            }

            parts.add(new PathPart(x, y, angle));
        }
        //Extended straight front segment
        for (int i = 1; i <=numSegments; i++) {

            double angle = -getFinalAngleOffset();
            double x = (-Math.cos(getPartMultipler()) + 1) * radius + Math.sin(getPartMultipler()) * baseOffset * ( i / numSegments);
            double y = -baseOffset - Math.sin(getPartMultipler()) * radius - Math.cos(getPartMultipler()) * baseOffset * (i/numSegments);

            if (this.left) {
                angle = -angle;
                x = -x;
            }

            parts.add(new PathPart(x, y, angle));
        }

        return parts;
    }
}
