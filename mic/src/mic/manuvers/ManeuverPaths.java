package mic.manuvers;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
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
        List<PathPart> rawParts = workingPath.getPathParts(NUM_PATH_SEGMENTS, baseOffset, isLargeBase);
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

    private static RealCurvedPathData.DataInstance calculateRealPaths(CurvedPaths curvedPath, boolean bigBase) {

        double baseLength = bigBase ? 2*113 : 113;

        RealCurvedPathData.DataInstance data = new RealCurvedPathData.DataInstance();

        List<Point.Double> points = Lists.newArrayList();

        int segments = 100;
        List<PathPart> parts = curvedPath.getPathParts(segments, baseLength, bigBase);
        int minPathIndex = segments / 2;
        while(minPathIndex < segments*2.5) {
            PathPart partA = parts.get(minPathIndex);
            Point.Double pointA = new Point2D.Double(partA.getX(), partA.getY());
            PathPart closest = partA;
            double closestDist = Double.MAX_VALUE;
            int closestIndex = minPathIndex;
            for(int i = minPathIndex + 1; i < parts.size(); i++) {
                PathPart partB = parts.get(i);
                Point.Double pointB = new Point2D.Double(partB.getX(), partB.getY());
                double distanceDiff = Math.abs(baseLength - Math.abs(pointB.distance(pointA)));
                if (distanceDiff < closestDist) {
                    closest = partB;
                    closestDist = distanceDiff;
                    closestIndex = i;
                }
            }
            minPathIndex++;
            Point.Double centerOfLine = new Point2D.Double(
                    pointA.getX() + closest.getX() - pointA.getX() / 2.0,
                    pointA.getY() + closest.getY() - pointA.getY() / 2.0);
            points.add(centerOfLine);

            System.out.println(String.format("%s\t%s\t%s\t%s", centerOfLine.x, centerOfLine.y, pointA.distance(closest.getX(), closest.getY()), closestIndex));
        }

        data.parts.add(new RealCurvedPathData.CurvedPathPart());
        data.parts.get(0).angle = 0;
        data.parts.get(0).x = points.get(0).getX();
        data.parts.get(0).y = points.get(0).getY();

        for(int i = 1; i < points.size(); i++) {
            data.parts.add(new RealCurvedPathData.CurvedPathPart());
            data.parts.get(i).x = points.get(i).getX();
            data.parts.get(i).y = points.get(i).getY();

            if (i + 1 >= points.size()) {
                if (curvedPath.left) {
                    data.parts.get(i).angle = -(360 - curvedPath.getFinalAngleOffset());
                } else {
                    data.parts.get(i).angle = -curvedPath.getFinalAngleOffset();
                }
                continue;
            }

            double dx = points.get(i - 1).getX() - points.get(i + 1).getX();
            double dy = points.get(i + 1).getY() - points.get(i - 1).getY();

            double rads = Math.atan2(dy, dx);

            rads += Math.PI / 2;

            // WHY IS THIS SO WRONG?
            data.parts.get(i).angle = Math.toDegrees(rads);

            if (data.parts.get(i).angle > 0) {
                data.parts.get(i).angle = data.parts.get(i).angle - 360;
            }
        }

        return data;
    }

    public static void main(String[] args) {

        RealCurvedPathData.DataInstance dataInstance = calculateRealPaths(CurvedPaths.RT1, false);
        for (RealCurvedPathData.CurvedPathPart part : dataInstance.parts) {
            System.out.println(String.format("%s\t%s\t%s", part.x, part.y, part.angle));
        }

//        for(CurvedPaths curve : CurvedPaths.values()) {
//            for (Boolean isBigBase : Lists.newArrayList(true, false)) {
//
//            }
//        }
    }
}
