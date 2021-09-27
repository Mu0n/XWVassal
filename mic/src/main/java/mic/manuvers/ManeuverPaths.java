package mic.manuvers;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.List;
import java.awt.geom.GeneralPath;

import VASSAL.build.GameModule;
import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.FreeRotator;
import com.google.common.collect.Lists;

import static mic.Util.logToChat;
import static mic.Util.logToChatWithoutUndo;

/**
 * Created by amatheny on 2/17/17.
 */


public enum ManeuverPaths {
    LBk1(CurvedPaths.LBk1, "Left Bank 1", 1, 0.0f, "517", 45.0f, -23.0f, 133.0f, -23.0f, 161.25f, -23.0f, 189.5f, "3653", -143.0f, 84.0f, -143.0f, 112.0f, -143.0f, 140.0f),
    LBk2(CurvedPaths.LBk2, "Left Bank 2", 2, 0.0f, "519", 45.0f, -43.6f, 183.0f, -43.6f, 211.25f, -43.6f, 239.5f, "3654", -163.0f, 133.0f, -163.0f, 161.0f, -163.0f, 189.0f),
    LBk3(CurvedPaths.LBk3, "Left Bank 3", 3, 0.0f, "520", 45.0f, -64.0f, 232.5f, -64.0f, 260.75f, -64.0f, 289.0f, "3655", -184.0f, 184.0f, -184.0f, 212.0f, -184.0f, 240.0f),

    RBk1(CurvedPaths.RBk1, "Right Bank 1", 1, 0.0f, "517", 180.0f, 29.0f, 147.0f, 29.0f, 175.25f, 29.0f, 203.5f, "3653", 149.0f, 98.0f, 149.0f, 126.0f, 149.0f, 154.0f),
    RBk2(CurvedPaths.RBk2, "Right Bank 2", 2, 0.0f, "519", 180.0f, 51.6f, 197.0f, 51.6f, 225.25f, 51.6f, 253.5f, "3654", 169.0f, 147.0f, 169.0f, 175.0f, 169.0f, 203.0f),
    RBk3(CurvedPaths.RBk3, "Right Bank 3", 3, 0.0f, "520", 180.0f, 72.0f, 246.5f, 72.0f, 274.75f, 72.0f, 303.0f, "3655", 191.0f, 198.0f, 191.0f, 226.0f, 191.0f, 254.0f),

    LT1(CurvedPaths.LT1, "Left Turn 1", 1, 0.0f, "521", 90.0f, -35.0f, 120.0f, -35.0f, 148.25f, -35.0f, 176.5f, "3656", -203.0f, -48.0f, -203.0f, -20.0f, -203.0f, 8.0f),
    LT2(CurvedPaths.LT2, "Left Turn 2", 2, 0.0f, "522", 90.0f, -74.0f, 159.0f, -74.0f, 187.25f, -74.0f, 215.5f, "3657", -244.0f, -8.0f, -244.0f, 20.0f, -244.0f, 48.0f),
    LT3(CurvedPaths.LT3, "Left Turn 3", 3, 0.0f, "523", 90.0f, -113.0f, 198.0f, -113.0f, 226.25f, -113.0f, 254.5f, "3658", -282.0f, 30.0f, -282.0f, 58.0f, -282.0f, 86.0f),

    RT1(CurvedPaths.RT1, "Right Turn 1", 1, 0.0f, "521", 180.0f, 35.0f, 120.0f, 35.0f, 148.25f, 35.0f, 176.5f, "3656", 203.0f, -48.0f, 205.0f, -20.0f, 203.0f, 8.0f),
    RT2(CurvedPaths.RT2, "Right Turn 2", 2, 0.0f, "522", 180.0f, 74.0f, 159.0f, 74.0f, 187.25f, 74.0f, 215.5f, "3657", 244.0f, -8.0f, 243.0f, 18.0f, 244.0f, 48.0f),
    RT3(CurvedPaths.RT3, "Right Turn 3", 3, 0.0f, "523", 180.0f, 113.0f, 198.0f, 113.0f, 226.25f, 113.0f, 254.5f, "3658", 282.0f, 30.0f, 283.0f, 58.0f, 282.0f, 86.0f),

    TrollR1(CurvedPaths.RT1, "Tallon Roll Right 1", 1, 270.0f, "521", 90.0f, 120.0f, -35.0f, 148.25f, -35.0f, 176.5f, -35.0f, "3656", -48.0f, -204.0f, -20.5f, -203.5f, 7.0f, -203.0f),
    TrollR2(CurvedPaths.RT2, "Tallon Roll Right 2", 2, 270.0f, "522", 90.0f, 159.5f, -75.0f, 187.5f, -75.0f, 215.5f, -75.0f, "3657", -11.0f, -243.0f, 17.5f, -244.0f, 46.0f, -245.0f),
    TrollR3(CurvedPaths.RT3, "Tallon Roll Right 3", 3, 270.0f, "523", 90.0f, 198.0f, -113.0f, 226.25f, -113.0f, 254.5f, -113.0f, "523", 198.0f, -113.0f, 226.25f, -113.0f, 254.5f, -113.0f),

    TrollL1(CurvedPaths.LT1, "Tallon Roll Left 1", 1, 90.0f, "521", 180.0f, -121.0f, -35.0f, -148.25f, -35.0f, -176.5f, -35.0f, "3656", 49.0f, -203.0f, 20.25f, -203.0f, -8.5f, -203.0f),
    TrollL2(CurvedPaths.LT2, "Tallon Roll Left 2", 2, 90.0f, "522", 180.0f, -159.0f, -75.0f, -187.25f, -75.0f, -215.5f, -75.0f, "3657", 10.0f, -243.0f, -18.5f, -244.0f, -47.0f, -245.0f),
    TrollL3(CurvedPaths.LT3, "Tallon Roll Left 3", 3, 90.0f, "523", 180.0f, -198.0f, -113.0f, -226.25f, -113.0f, -254.5f, -113.0f, "523", -198.0f, -113.0f, -226.25f, -113.0f, -254.5f, -113.0f),

    Str1(StraightPaths.Str1, "Forward 1", 1, 0.0f, "524", 0.0f, 0.0f, 113.0f, 0.0f, 141.25f, 0.0f, 169.5f, "528", 0.0f, 339.0f, 0.0f, 367.25f, 0.0f, 395.5f),
    Str2(StraightPaths.Str2, "Forward 2", 2, 0.0f, "525", 0.0f, 0.0f, 169.5f, 0.0f, 197.75f, 0.0f, 226.0f, "528", 0.0f, 339.0f, 0.0f, 367.25f, 0.0f, 395.5f),
    Str3(StraightPaths.Str3, "Forward 3", 3, 0.0f, "526", 0.0f, 0.0f, 226.0f, 0.0f, 254.25f, 0.0f, 282.5f, "528", 0.0f, 339.0f, 0.0f, 367.25f, 0.0f, 395.5f),
    Str4(StraightPaths.Str4, "Forward 4", 4, 0.0f, "527", 0.0f, 0.0f, 282.5f, 0.0f, 310.75f, 0.0f, 339.0f, "528", 0.0f, 339.0f, 0.0f, 367.25f, 0.0f, 395.5f),
    Str5(StraightPaths.Str5, "Forward 5", 5, 0.0f, "528", 0.0f, 0.0f, 339.0f, 0.0f, 367.25f, 0.0f, 395.5f, "528", 0.0f, 339.0f, 0.0f, 367.25f, 0.0f, 395.5f),

    K1(StraightPaths.Str1, "K-Turn 1", 1, 180.0f, "524", 0.0f, 0.0f, -113.0f, 0.0f, -141.25f, 0.0f, -169.5f, "528", 0.0f, -339.0f, 0.0f, -367.25f, 0.0f, -395.5f),
    K2(StraightPaths.Str2, "K-Turn 2", 2, 180.0f, "525", 0.0f, 0.0f, -169.5f, 0.0f, -197.75f, 0.0f, -226.0f, "528", 0.0f, -339.0f, 0.0f, -367.25f, 0.0f, -395.5f),
    K3(StraightPaths.Str3, "K-Turn 3", 3, 180.0f, "526", 0.0f, 0.0f, -226.0f, 0.0f, -254.25f, 0.0f, -282.5f, "528", 0.0f, -339.0f, 0.0f, -367.25f, 0.0f, -395.5f),
    K4(StraightPaths.Str4 ,"K-Turn 4", 4, 180.0f, "527", 0.0f, 0.0f, -282.5f, 0.0f, -310.75f, 0.0f, -339.0f, "528", 0.0f, -339.0f, 0.0f, -367.25f, 0.0f, -395.5f),
    K5(StraightPaths.Str5, "K-Turn 5", 5, 180.0f, "528", 0.0f, 0.0f, -339.0f, 0.0f, -367.25f, 0.0f, -395.5f, "528", 0.0f, -339.0f, 0.0f, -367.25f, 0.0f, -395.5f),

    RevStr1(StraightPaths.Rev1, "Reverse 1", 1, 0.0f, "524", 0.0f, 0.0f, -113.0f, 0.0f, -141.25f, 0.0f, -169.5f, "528", 0.0f, -339.0f, 0.0f, -367.25f, 0.0f, -395.5f),
    RevStr2(StraightPaths.Rev2, "Reverse 2", 1, 0.0f, "525", 0.0f, 0.0f, -169.5f, 0.0f, -197.75f, 0.0f, -226.0f, "528", 0.0f, -339.0f, 0.0f, -367.25f, 0.0f, -395.5f),

    RevLbk1(CurvedPaths.RevLB1, "Reverse Left Bank 1", 1, 0.0f, "517", 0.0f, -28.0f, -146.5f, -28.0f, -175.25f, -29.0f, -203.5f, "3653", -147.0f, -98.0f, -148.0f, -126.0f, -149.0f, -154.0f),
    RevRbk1(CurvedPaths.RevRB1, "Reverse Right Bank 1", 1, 0.0f, "517", 225.0f, 24.0f, -133.0f, 24.0f, -161.5f, 24.0f, -190.0f, "3653", 142.0f, -83.0f, 142.5f, -111.5f, 143.0f, -140.0f),


    SloopL1(CurvedPaths.LBk1, "Segnor's Loop Left 1", 1, 180.0f, "517", 225.0f, 23.0f, -133.0f, 23.0f, -161.25f, 23.0f, -189.5f, "3653", 143.0f, -83.0f, 143.0f, -111.0f, 144.0f, -139.0f),
    SloopL2(CurvedPaths.LBk2, "Segnor's Loop Left 2", 2, 180.0f, "519", 225.0f, 43.6f, -183.0f, 43.6f, -211.25f, 43.6f, -239.5f, "3654", 164.0f, -133.0f, 164.0f, -161.5f, 163.0f, -190.0f),
    SloopL3(CurvedPaths.LBk3, "Segnor's Loop Left 3", 3, 180.0f, "520", 225.0f, 64.0f, -232.5f, 64.0f, -260.75f, 64.0f, -289.0f, "3655", 184.0f, -183.0f, 184.0f, -211.5f, 184.0f, -240.0f),

    SloopR1(CurvedPaths.RBk1, "Segnor's Loop Right 1", 1, 180.0f, "517", 0.0f, -29.0f, -147.0f, -29.0f, -175.25f, -29.0f, -203.5f, "3653", -151.0f, -96.0f, -151.0f, -125.0f, -150.0f, -154.0f),
    SloopR2(CurvedPaths.RBk2, "Segnor's Loop Right 2", 2, 180.0f, "519", 0.0f, -51.6f, -197.0f, -51.6f, -225.25f, -51.6f, -253.5f, "3654", -171.0f, -145.0f, -171.0f, -174.5f, -169.0f, -204.0f),
    SloopR3(CurvedPaths.RBk3, "Segnor's Loop Right 3", 3, 180.0f, "520", 0.0f, -72.0f, -246.5f, -72.0f, -274.75f, -72.0f, -303.0f, "3655", -190.0f, -196.0f, -190.0f, -225.0f, -190.0f, -254.0f),

    SloopR3Turn(CurvedPaths.RT3, "Segnor's Loop Hard Right 3", 3, 180.0f, "523", 0.0f, -113.0f, -198.0f, -113.0f, -226.25f, -113.0f, -254.5f, "3658", -283.0f, -28.0f, -282.5f, -57.0f, -282.0f, -86.0f),
    SloopL3Turn(CurvedPaths.LT3, "Segnor's Loop Hard Left 3", 3, 180.0f, "523", 270.0f, 113.0f, -198.0f, 113.0f, -226.25f, 113.0f, -254.5f, "3658", 284.0f, -28.0f, 283.0f, -57.0f, 282.0f, -86.0f),

    SideSlipL1Turn(CurvedPaths.SSLT1, "Sideslip Hard Right 1", 1, 270.0f, "521", 90.0f, 120.0f, -36.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3656", -49.0f, -205.0f, 0.0f, -0.0f, 0.0f, -0.0f),
    SideSlipL2Turn(CurvedPaths.SSLT2, "Sideslip Hard Right 2", 2, 270.0f, "522", 90.0f, 159.0f, -74.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3657", -9.0f, -244.0f, 0.0f, -0.0f, 0.0f, -0.0f),
    SideSlipL3Turn(CurvedPaths.SSLT3, "Sideslip Hard Right 3", 3, 270.0f, "523", 90.0f, 198.0f, -113.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3658", 29.0f, -283.0f, 0.0f, -0.0f, 0.0f, -0.0f),

    SideSlipR1Turn(CurvedPaths.SSRT1, "Sideslip Hard Left 1", 1, 90.0f, "521", 180.0f, -119.0f, -35.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3656", 48.0f, -205.0f, 0.0f, -0.0f, 0.0f, -0.0f),
    SideSlipR2Turn(CurvedPaths.SSRT2, "Sideslip Hard Left 2", 2, 90.0f, "522", 180.0f, -159.0f, -75.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3657", 9.0f, -244.0f, 0.0f, -0.0f, 0.0f, -0.0f),
    SideSlipR3Turn(CurvedPaths.SSRT3, "Sideslip Hard Left 3", 3, 90.0f, "523", 180.0f, -198.0f, -113.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3658", -30.0f, -283.0f, 0.0f, -0.0f, 0.0f, -0.0f),


//the strings appear reversed here because of an early, false assumption into how sideslip worked
    SideSlipL1Bank(CurvedPaths.SSLBk1, "Sideslip Bank Right 1", 1, 270.0f, "517", 90.0f, 146.0f, -29.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3653", 97.0f, -149.0f, 0.0f, -0.0f, 0.0f, -0.0f),
    SideSlipL2Bank(CurvedPaths.SSLBk2, "Sideslip Bank Right 2", 2, 270.0f, "519", 90.0f, 196.0f, -50.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3654", 146.0f, -170.0f, 0.0f, -0.0f, 0.0f, -0.0f),
    SideSlipL3Bank(CurvedPaths.SSLBk3, "Sideslip Bank Right 3", 3, 270.0f, "520", 90.0f, 246.0f, -70.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3655", 196.0f, -190.0f, 0.0f, -0.0f, 0.0f, -0.0f),

    SideSlipR1Bank(CurvedPaths.SSRBk1, "Sideslip Bank Left 1", 1, 90.0f, "517", 135.0f, -133.0f, -22.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3653", -83.0f, -142.0f, 0.0f, -0.0f, 0.0f, -0.0f),
    SideSlipR2Bank(CurvedPaths.SSRBk2, "Sideslip Bank Left 2", 2, 90.0f, "519", 135.0f, -183.0f, -42.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3654", -134.0f, -163.0f, 0.0f, -0.0f, 0.0f, -0.0f),
    SideSlipR3Bank(CurvedPaths.SSRBk3, "Sideslip Bank Left 3", 3, 90.0f, "520", 135.0f, -233.0f, -64.0f, 0.0f, -0.0f, 0.0f, -0.0f, "3655", -184.0f, -184.0f, 0.0f, -0.0f, 0.0f, -0.0f)
    ;

    private final ManeuverPath path;
    private final String fullName;
    private final int speedInt;
    private final double additionalAngleForShip;
    private final double templateAngle;
    private final String gpID;
    private final double offsetX, offsetY;
    private final double offsetXMedium, offsetYMedium;
    private final double offsetXLarge, offsetYLarge;
    private final String aide_gpID;
    private final double aide_x, aide_y;
    private final double aide_xMedium, aide_yMedium;
    private final double aide_xLarge, aide_yLarge;

    final static double MMTOV = 2.825; // mm to vassal pixel size

    ManeuverPaths(ManeuverPath path, String fullName, int speedInt,
                  double additionalAngleForShip, String gpID, double templateAngle,
                  double offsetX, double offsetY,
                  double offsetXMedium, double offsetYMedium,
                  double offsetXLarge, double offsetYLarge,
                  String aide_gpID,
                  double aide_x, double aide_y,
                  double aide_xMedium, double aide_yMedium,
                  double aide_xLarge, double aide_yLarge) {
        this.path = path;
        this.fullName = fullName;
        this.speedInt = speedInt;
        this.additionalAngleForShip = additionalAngleForShip;
        this.gpID = gpID;
        this.templateAngle = templateAngle;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetXMedium = offsetXMedium;
        this.offsetYMedium = offsetYMedium;
        this.offsetXLarge = offsetXLarge;
        this.offsetYLarge = offsetYLarge;
        this.aide_gpID = aide_gpID;
        this.aide_x = aide_x;
        this.aide_y = aide_y;
        this.aide_xMedium = aide_xMedium;
        this.aide_yMedium = aide_yMedium;
        this.aide_xLarge = aide_xLarge;
        this.aide_yLarge = aide_yLarge;
    }


    public String getFullName() { return this.fullName; }
    public int getSpeedInt() { return this.speedInt; }
    public double getAdditionalAngleForShip() { return this.additionalAngleForShip; }
    public String getTemplateGpID() { return this.gpID; }
    public double getTemplateAngle() { return this.templateAngle; }
    public double getOffsetX() { return this.offsetX; }
    public double getOffsetY() { return this.offsetY; }
    public double getOffsetXMedium() { return this.offsetXMedium; }
    public double getOffsetYMedium() { return this.offsetYMedium; }
    public double getOffsetXLarge() { return this.offsetXLarge; }
    public double getOffsetYLarge() { return this.offsetYLarge; }
    public String getAide_gpID() { return this.aide_gpID; }
    public double getAide_x() { return this.aide_x; }
    public double getAide_y() { return this.aide_y; }
    public double getAide_xMedium() { return this.aide_xMedium; }
    public double getAide_yMedium() { return this.aide_yMedium; }
    public double getAide_xLarge() { return this.aide_xLarge; }
    public double getAide_yLarge() { return this.aide_yLarge; }

    public Shape getTransformedTemplateShape(double x, double y, int whichSizeShip, FreeRotator rotator){
        Shape rawShape = new Rectangle(0,0);
        Shape transformed = new Rectangle(0,0);
        int speed = getSpeedInt();

        for(PieceSlot ps : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)){
            if(getTemplateGpID().equals(ps.getGpId())) rawShape =  ps.getPiece().getShape();
        }
        transformed = AffineTransform
                .getRotateInstance(getTemplateAngle() * Math.PI/180.0f, 0.0f, 0.0f)
                .createTransformedShape(rawShape);

        double selectedOffsetX = 0.0, selectedOffsetY = 0.0;
        switch(whichSizeShip)
        {
            case 1: //small
                selectedOffsetX = offsetX;
                selectedOffsetY = offsetY;
            break;
            case 2: //medium
                selectedOffsetX = offsetXMedium;
                selectedOffsetY = offsetYMedium;
            break;
            case 3:
                selectedOffsetX = offsetXLarge;
                selectedOffsetY = offsetYLarge;
                break;
        }
        transformed = AffineTransform
                .getTranslateInstance(x + selectedOffsetX, y + selectedOffsetY)
                .createTransformedShape(transformed);
        transformed = AffineTransform
                .getRotateInstance(rotator.getAngleInRadians(), x, y)
                .createTransformedShape(transformed);

        return transformed;


    }

    private static int getNumPathSegments(ManeuverPath path) {
        return (int) Math.floor((path.getPathLength() / CurvedPaths.RT3.getPathLength()) * 500);
    }

    private List<PathPart> getTransformedPathPartsInternal(ManeuverPath workingPath, double x, double y, double angleDegrees, int whichSize, boolean sideslipTurn, boolean sideslipBank, boolean LeftOtherwiseRight) {
        double baseOffset = 56.5;
        if(whichSize==3) baseOffset = 113;
        else if(whichSize==2) baseOffset = 84.75;

        List<PathPart> rawParts = workingPath.getPathParts(getNumPathSegments(workingPath), baseOffset, whichSize);
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

    public PathPart getTweakedPathPartForTrollInternal(ManeuverPath workingPath, double x, double y, double angleDegrees, int whichSize, boolean LeftOtherwiseRight, int FrontCenterBack) {
        //Front = 1, Center = 0 (unused because you typically already have it as the last element of lastmaneuver in AutoBumpDecorator, Back = -1

        double baseOffset = 56.5;
        if(whichSize==3) baseOffset = 113;
        else if(whichSize==2) baseOffset = 84.75;

        List<PathPart> rawParts = workingPath.getPathParts(getNumPathSegments(workingPath), baseOffset, whichSize);
        PathPart transformed;

        PathPart rawPart = rawParts.get(rawParts.size()-1); //go directly at the end
        Path2D.Double testPath = new Path2D.Double();
        int yOffset  =(int)( FrontCenterBack * 56.5/2);

        testPath.moveTo(rawPart.getX(), rawPart.getY() + yOffset);

            testPath = (Path2D.Double) AffineTransform
                .getRotateInstance(-(angleDegrees) * (Math.PI / 180), 0, 0)
                .createTransformedShape(testPath);

        double angle = angleDegrees + rawPart.getAngle();
        if (angle > 0) {
            angle = angle - 360;
        }
        transformed = new PathPart(
                x + testPath.getCurrentPoint().getX(),
                y + testPath.getCurrentPoint().getY(),
                angle
        );

        return transformed;
    }

    public PathPart getTweakedPathPartForTroll(double x, double y, double angleDegrees, int whichSize, boolean LeftOtherwiseRight, int FrontCenterBack) {
        return getTweakedPathPartForTrollInternal(this.path, x, y, angleDegrees, whichSize, LeftOtherwiseRight, FrontCenterBack);
    }
    public List<PathPart> getTransformedPathParts(double x, double y, double angleDegrees, int whichSize, boolean sideslipTurn, boolean sideslipBank, boolean LeftOtherwiseRight) {
        return getTransformedPathPartsInternal(this.path, x, y, angleDegrees, whichSize, sideslipTurn, sideslipBank, LeftOtherwiseRight);
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