package mic.manuvers;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Created by amatheny on 2/17/17.
 */
public enum CurvedPaths implements ManuverPath {
    LBk1(80 * 5.65, true, true),
    RBk1(80 * 5.65, false, true),
    LBk2(130 * 5.65, true, true),
    RBk2(130 * 5.65, false, true),
    LBk3(180 * 5.65, true, true),
    RBk3(180 * 5.65, false, true),

    LT1(35 * 5.65, true, false),
    RT1(35 * 5.65, false, false),
    LT2(62.5 * 5.65, true, false),
    RT2(62.5 * 5.65, false, false),
    LT3(90 * 5.65, true, false),
    RT3(90 * 5.65, false, false);

    private final boolean bank;
    private boolean left;
    private double radius;

    private static double QTR_PI = Math.PI / 4.0;
    private static double HALF_PI = Math.PI / 2.0;

    CurvedPaths(double radius, boolean left, boolean bank) {
        this.radius = radius;
        this.left = left;
        this.bank = bank;
    }

    private double getFinalAngleOffset() {
        return this.bank ? 45 : 90;
    }

    private double getPartMultipler() {
        return this.bank ? QTR_PI : HALF_PI;
    }

    public List<PathPart> getPathParts(int numSegments) {

        List<PathPart> parts = Lists.newArrayList();
        for (int i = 1; i <= numSegments; i++) {
            double arg = (getPartMultipler() * i) / (double) numSegments;

            double x = Math.sin(arg) * radius;
            double y = (-1 * Math.cos(arg) + 1) * radius;
            double angle = -(i / (double) numSegments) * getFinalAngleOffset();

            if (this.left) {
                angle = -angle;
                x = -x;
            }

            parts.add(new PathPart(x, y, angle));
        }

        return parts;
    }
}
