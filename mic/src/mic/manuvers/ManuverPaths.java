package mic.manuvers;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Created by amatheny on 2/17/17.
 */
public enum ManuverPaths {
    LBk1(CurvedPaths.LBk1),
    RBk1(CurvedPaths.RBk1),
    LT1(CurvedPaths.LT1),
    RT1(CurvedPaths.RT1);

    private static int NUM_PATH_SEGMENTS = 100;

    private final ManuverPath path;

    ManuverPaths(ManuverPath path) {
        this.path = path;
    }

    public List<PathPart> getTransformedPathParts(double x, double y, double angleDegrees) {
        List<PathPart> rawParts = this.path.getPathParts(NUM_PATH_SEGMENTS);
        List<PathPart> transformed = Lists.newArrayList();

        Path2D.Double path = new Path2D.Double();
        path.moveTo(0, 0);

        for (PathPart rawPart : rawParts) {
            Path2D.Double testPath = new Path2D.Double();
            testPath.moveTo(rawPart.getX(), rawPart.getY());
            testPath = (Path2D.Double) AffineTransform
                    .getRotateInstance(-angleDegrees * 180 / Math.PI, 0, 0)
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

            path.lineTo(rawPart.getX(), rawPart.getY());
        }

//        path = (Path2D.Double) AffineTransform.getRotateInstance(angleDegrees * 180 / Math.PI, 0, 0).createTransformedShape(path);
//
//        PathIterator iterator = path.getPathIterator(null);
//        iterator.next();
//        int i = 0;
//        double[] coords = new double[6];
//        while (!iterator.isDone()) {
//            PathPart rawPart = rawParts.get(i);
//            double angle = angleDegrees + rawPart.getAngle();
//            if (angle > 0) {
//                angle = angle - 360;
//            }
//
//            iterator.currentSegment(coords);
//
//            transformed.add(new PathPart(
//                    x + coords[0],
//                    y + coords[1],
//                    angle
//            ));
//            iterator.next();
//        }
        return transformed;
    }

    public static void main(String[] args) {
        List<PathPart> parts = ManuverPaths.RT1.getTransformedPathParts(0, 0, 0);
//        List<PathPart> parts = CurvedPaths.LBk1.getPathParts(100);
        for (PathPart part : parts) {
            System.out.println(String.format("%s\t %s", part.getX(), part.getY(), part.getAngle()));
        }
    }
}
