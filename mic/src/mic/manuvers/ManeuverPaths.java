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
public double getPathLength(){
        return this.getPathLength();
}
    public String getFullName() {
        switch(this)
        {
            case LBk1:
                return "Left Bank 1";
            case LBk2:
                return "Left Bank 2";
            case LBk3:
                return "Left Bank 3";
            case RBk1:
                return "Right Bank 1";
            case RBk2:
                return "Right Bank 2";
            case RBk3:
                return "Right Bank 3";

            case LT1:
                return "Left Turn 1";
            case LT2:
                return "Left Turn 2";
            case LT3:
                return "Left Turn 3";
            case RT1:
                return "Right Turn 1";
            case RT2:
                return "Right Turn 2";
            case RT3:
                return "Right Turn 3";

            case Str1:
                return "Forward 1";
            case Str2:
                return "Forward 2";
            case Str3:
                return "Forward 3";
            case Str4:
                return "Forward 4";
            case Str5:
                return "Forward 5";

            case K1:
                return "K-Turn 1";
            case K2:
                return "K-Turn 2";
            case K3:
                return "K-Turn 3";
            case K4:
                return "K-Turn 4";
            case K5:
                return "K-Turn 5";

            case RevStr1:
                return "Reverse 1";
            case RevLbk1:
                return "Reverse Left Bank 1";
            case RevRbk1:
                return "Reverse Right Bank 1";

            case SloopL1:
                return "Segnor's Loop Left 1";
            case SloopL2:
                return "Segnor's Loop Left 2";
            case SloopL3:
                return "Segnor's Loop Left 3";

            case SloopR1:
                return "Segnor's Loop Right 1";
            case SloopR2:
                return "Segnor's Loop Right 2";
            case SloopR3:
                return "Segnor's Loop Right 3";

            case SloopL3Turn:
                return "Segnor's Loop Hard Left 3";
            case SloopR3Turn:
                return "Segnor's Loop Hard Right 3";

            case TrollL2:
                return "Tallon Roll Left 2";
            case TrollL3:
                return "Tallon Roll Left 3";
            case TrollR2:
                return "Tallon Roll Right 2";
            case TrollR3:
                return "Tallon Roll Right 3";

            default:
                return "Unknown Move";
        }
    }

    public int getSpeedInt() {
        switch(this)
        {
            case LBk1:
            case RBk1:
            case LT1:
            case RT1:
            case Str1:
            case K1:
            case RevStr1:
            case RevLbk1:
            case RevRbk1:
            case SloopL1:
            case SloopR1:
                return 1;
            case LBk2:
            case RBk2:
            case LT2:
            case RT2:
            case Str2:
            case K2:
            case SloopL2:
            case SloopR2:
            case TrollL2:
            case TrollR2:
                return 2;
            case LBk3:
            case RBk3:
            case LT3:
            case RT3:
            case Str3:
            case K3:
            case SloopL3:
            case SloopR3:
            case SloopL3Turn:
            case SloopR3Turn:
            case TrollL3:
            case TrollR3:
                return 3;
            case Str4:
            case K4:
                return 4;
            case Str5:
            case K5:
                return 5;
            default:
                return 1;
        }
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
