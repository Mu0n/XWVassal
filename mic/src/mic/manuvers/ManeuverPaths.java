package mic.manuvers;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Created by amatheny on 2/17/17.
 */
public enum ManeuverPaths {
    LBk1(CurvedPaths.LBk1),
    LBk2(CurvedPaths.LBk2),
    LBk3(CurvedPaths.LBk3),

    RBk1(CurvedPaths.RBk1),
    RBk2(CurvedPaths.RBk2),
    RBk3(CurvedPaths.RBk3),


    LT1(CurvedPaths.LT1),
    LT2(CurvedPaths.LT2),
    LT3(CurvedPaths.LT3),

    RT1(CurvedPaths.RT1),
    RT2(CurvedPaths.RT2),
    RT3(CurvedPaths.RT3),

    Str1(StraightPaths.Str1),
    Str2(StraightPaths.Str2),
    Str3(StraightPaths.Str3),
    Str4(StraightPaths.Str4),
    Str5(StraightPaths.Str5),

    K1(StraightPaths.Str1),
    K2(StraightPaths.Str2),
    K3(StraightPaths.Str3),
    K4(StraightPaths.Str4),
    K5(StraightPaths.Str5),

    RevStr1(StraightPaths.Rev1),
    RevLbk1(CurvedPaths.RevLB1),
    RevRbk1(CurvedPaths.RevRB1),

    SloopL1(CurvedPaths.LBk1),
    SloopL2(CurvedPaths.LBk2),
    SloopL3(CurvedPaths.LBk3),

    SloopR1(CurvedPaths.RBk1),
    SloopR2(CurvedPaths.RBk2),
    SloopR3(CurvedPaths.RBk3),

    SloopL3Turn(CurvedPaths.LT3),
    SloopR3Turn(CurvedPaths.RT3),

    TrollL2(CurvedPaths.LT2),
    TrollL3(CurvedPaths.LT3),
    TrollR2(CurvedPaths.RT2),
    TrollR3(CurvedPaths.RT3);

    private final ManeuverPath path;

    ManeuverPaths(ManeuverPath path) {
        this.path = path;
    }

    private static int getNumPathSegments(ManeuverPath path) {
        return (int) Math.floor((path.getPathLength() / CurvedPaths.RT3.getPathLength()) * 500);
    }

    private List<PathPart> getTransformedPathPartsInternal(ManeuverPath workingPath, double x, double y, double angleDegrees, boolean isLargeBase) {
        double baseOffset = isLargeBase ? 113 : 56.5;
        List<PathPart> rawParts = workingPath.getPathParts(getNumPathSegments(workingPath), baseOffset, isLargeBase);
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

    public static Path2D.Double toPath2D(List<PathPart> parts, double zoom) {
        Path2D.Double templatePath = new Path2D.Double();

        templatePath.moveTo(parts.get(0).getX(), parts.get(0).getY());
        for (int i = 1; i < parts.size(); i++) {
            templatePath.lineTo(parts.get(i).getX(), parts.get(i).getY());

        }
        return (Path2D.Double) AffineTransform.getScaleInstance(zoom, zoom)
                .createTransformedShape(templatePath);
    }
}
