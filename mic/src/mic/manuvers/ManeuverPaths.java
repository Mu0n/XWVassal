package mic.manuvers;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.List;

import VASSAL.counters.Decorator;
import VASSAL.counters.FreeRotator;
import com.google.common.collect.Lists;

/**
 * Created by amatheny on 2/17/17.
 */
public enum ManeuverPaths {
    LBk1(CurvedPaths.LBk1, "Left Bank 1", 1, 0.0f),
    LBk2(CurvedPaths.LBk2, "Left Bank 2", 2, 0.0f),
    LBk3(CurvedPaths.LBk3, "Left Bank 3", 3, 0.0f),

    RBk1(CurvedPaths.RBk1, "Right Bank 1", 1, 0.0f),
    RBk2(CurvedPaths.RBk2, "Right Bank 2", 2, 0.0f),
    RBk3(CurvedPaths.RBk3, "Right Bank 3", 3, 0.0f),


    LT1(CurvedPaths.LT1, "Left Turn 1", 1, 0.0f),
    LT2(CurvedPaths.LT2, "Left Turn 2", 2, 0.0f),
    LT3(CurvedPaths.LT3, "Left Turn 3", 3, 0.0f),

    RT1(CurvedPaths.RT1, "Right Turn 1", 1, 0.0f),
    RT2(CurvedPaths.RT2, "Right Turn 2", 2, 0.0f),
    RT3(CurvedPaths.RT3, "Right Turn 3", 3, 0.0f),

    Str1(StraightPaths.Str1, "Forward 1", 1, 0.0f),
    Str2(StraightPaths.Str2, "Forward 2", 2, 0.0f),
    Str3(StraightPaths.Str3, "Forward 3", 3, 0.0f),
    Str4(StraightPaths.Str4, "Forward 4", 4, 0.0f),
    Str5(StraightPaths.Str5, "Forward 5", 5, 0.0f),

    K1(StraightPaths.Str1, "K-Turn 1", 1, 180.0f),
    K2(StraightPaths.Str2, "K-Turn 2", 2, 180.0f),
    K3(StraightPaths.Str3, "K-Turn 3", 3, 180.0f),
    K4(StraightPaths.Str4 ,"K-Turn 4", 4, 180.0f),
    K5(StraightPaths.Str5, "K-Turn 5", 5, 180.0f),

    RevStr1(StraightPaths.Rev1, "Reverse 1", 1, 0.0f),
    RevLbk1(CurvedPaths.RevLB1, "Reverse Left Bank 1", 1, 0.0f),
    RevRbk1(CurvedPaths.RevRB1, "Reverse Right Bank 1", 1, 0.0f),

    SloopL1(CurvedPaths.LBk1, "Segnor's Loop Left 1", 1, 180.0f),
    SloopL2(CurvedPaths.LBk2, "Segnor's Loop Left 2", 2, 180.0f),
    SloopL3(CurvedPaths.LBk3, "Segnor's Loop Left 3", 3, 180.0f),

    SloopR1(CurvedPaths.RBk1, "Segnor's Loop Right 1", 1, 180.0f),
    SloopR2(CurvedPaths.RBk2, "Segnor's Loop Right 2", 2, 180.0f),
    SloopR3(CurvedPaths.RBk3, "Segnor's Loop Right 3", 3, 180.0f),

    SloopL3Turn(CurvedPaths.LT3, "Segnor's Loop Hard Left 3", 3, 180.0f),
    SloopR3Turn(CurvedPaths.RT3, "Segnor's Loop Hard Right 3", 3, 180.0f),

    TrollL2(CurvedPaths.LT2, "Tallon Roll Left 2", 2, 90.0f),
    TrollL3(CurvedPaths.LT3, "Tallon Roll Left 3", 2, 90.0f),
    TrollR2(CurvedPaths.RT2, "Tallon Roll Right 2", 2, 270.0f),
    TrollR3(CurvedPaths.RT3, "Tallon Roll Right 3", 2, 270.0f);

    private final ManeuverPath path;
    private final String fullName;
    private final int speedInt;
    private final double additionalAngleForMove;

    ManeuverPaths(ManeuverPath path, String fullName, int speedInt, double additionalAngleForMove) {
        this.path = path;
        this.fullName = fullName;
        this.speedInt = speedInt;
        this.additionalAngleForMove = additionalAngleForMove;
    }

public double getPathLength(){ return this.getPathLength(); }
public String getFullName() { return this.fullName; }
public int getSpeedInt() { return this.speedInt; }
public double getAdditionalAngleForMove() { return this.additionalAngleForMove; }

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
