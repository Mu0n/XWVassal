package mic.manuvers;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Created by amatheny on 2/17/17.
 */
public enum ManeuverPaths {
    LBk1(CurvedPaths.LBk1, CurvedPaths.RBk1),
    LBk2(CurvedPaths.LBk2, CurvedPaths.RBk2),
    LBk3(CurvedPaths.LBk3, CurvedPaths.RBk3),

    RBk1(CurvedPaths.RBk1, CurvedPaths.LBk1),
    RBk2(CurvedPaths.RBk2, CurvedPaths.LBk2),
    RBk3(CurvedPaths.RBk3, CurvedPaths.LBk3),

    LT1(CurvedPaths.LT1, CurvedPaths.RT1),
    LT2(CurvedPaths.LT2, CurvedPaths.RT2),
    LT3(CurvedPaths.LT3, CurvedPaths.RT3),

    RT1(CurvedPaths.RT1, CurvedPaths.LT1),
    RT2(CurvedPaths.RT2, CurvedPaths.LT2),
    RT3(CurvedPaths.RT3, CurvedPaths.LT3),

    Str1(StraightPaths.Str1, StraightPaths.Str1),
    Str2(StraightPaths.Str2, StraightPaths.Str2),
    Str3(StraightPaths.Str3, StraightPaths.Str3),
    Str4(StraightPaths.Str4, StraightPaths.Str4),
    Str5(StraightPaths.Str5, StraightPaths.Str5);

    private static int NUM_PATH_SEGMENTS = 60;

    private final ManeuverPath path;
    private final ManeuverPath inversePath;

    ManeuverPaths(ManeuverPath path, ManeuverPath inversePath) {
        this.path = path;
        this.inversePath = inversePath;
    }

    private List<PathPart> getTransformedPathPartsInternal(ManeuverPath workingPath, double x, double y, double angleDegrees, boolean isLargeBase) {
        double baseOffset = isLargeBase ? 113 : 56.5;
        List<PathPart> rawParts = workingPath.getPathParts(NUM_PATH_SEGMENTS, baseOffset);
        List<PathPart> transformed = Lists.newArrayList();

        for (PathPart rawPart : rawParts) {
            Path2D.Double testPath = new Path2D.Double();
            testPath.moveTo(rawPart.getX(), rawPart.getY());
            testPath = (Path2D.Double) AffineTransform
                    .getRotateInstance(-angleDegrees * (Math.PI / 180), 0, 0)
                    .createTransformedShape(testPath);

            double angle = angleDegrees + rawPart.getAngle();
            if (angle > 0) {
                angle = angle - 360;
            }
            transformed.add(new PathPart(
                    x + testPath.getCurrentPoint().getX(),
                    y + testPath.getCurrentPoint().getY(),
                    angle
            ));
        }

        return transformed;
    }

    public List<PathPart> getTransformedPathParts(double x, double y, double angleDegrees, boolean isLargeBase) {
        return getTransformedPathPartsInternal(this.path, x, y, angleDegrees, isLargeBase);
    }

    public List<PathPart> getTransformedInversePathParts(double x, double y, double angleDegrees, boolean isLargeBase) {
        return getTransformedPathPartsInternal(this.inversePath, x, y, angleDegrees, isLargeBase);
    }

    public static ManeuverPaths fromLastMove(String lastMove) {
        try {
            return ManeuverPaths.valueOf(lastMove);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        List<PathPart> parts = ManeuverPaths.Str1.getTransformedPathParts(0, 0, 0, false);
//        List<PathPart> parts = CurvedPaths.LBk1.getPathParts(100);
        for (PathPart part : parts) {
            System.out.println(String.format("%s\t %s", part.getX(), part.getY(), part.getAngle()));
        }
    }
}
