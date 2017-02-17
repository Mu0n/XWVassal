package mic.manuvers;

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
    public static double scale = 5.65;

    private final ManuverPath path;

    ManuverPaths(ManuverPath path) {
        this.path = path;
    }

    public List<PathPart> getTransformedPathParts(double x, double y, double angleDegrees) {
        List<PathPart> rawParts = this.path.getPathParts(NUM_PATH_SEGMENTS);
        List<PathPart> transformed = Lists.newArrayList();
        for (PathPart rawPart : rawParts) {
            int angle = (int) Math.floor(angleDegrees + rawPart.getAngle());
            if (angle > 0) {
                angle = angle - 360;
            }

            double rotatedPartX = rawPart.getX() * Math.cos(rawPart.getAngle())
                    - rawPart.getY() * Math.sin(rawPart.getAngle());
            double rotatedPartY = rawPart.getY() * Math.cos(rawPart.getAngle())
                    + rawPart.getX() * Math.sin(rawPart.getAngle());

            transformed.add(new PathPart(
                    rotatedPartX + x,
                    rotatedPartY + y,
                    angle
            ));
        }
        return transformed;
    }
}
