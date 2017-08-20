package mic;

import java.awt.*;
import java.awt.geom.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import VASSAL.counters.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;

/**
 * Created by amatheny on 2/9/17.
 */
public class Util {

    private static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T loadRemoteJson(String url, Class<T> type) {
        try {
            return loadRemoteJson(new URL(url), type);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static <T> T loadRemoteJson(URL url, Class<T> type) {
        try {
            InputStream inputStream = new BufferedInputStream(url.openStream());
            return mapper.readValue(inputStream, type);
        } catch (Exception e) {
            System.out.println("Unhandled error parsing remote json: \n" + e.toString());
            return null;
        }
    }

    public static <T> T loadClasspathJson(String filename, Class<T> type) {
        try {
            InputStream inputStream = GameModule.getGameModule().getDataArchive().getInputStream(filename);
            if (inputStream == null) {
                logToChat("couldn't load " + filename);
            }
            return mapper.readValue(inputStream, type);
        } catch (Exception e) {
            System.out.println("Unhandled error parsing classpath json: \n" + e.toString());
            logToChat("Unhandled error parsing classpath json: \n" + e.toString());
            return null;
        }
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static List<String> none = Lists.newArrayList();


    public static void logToChat(String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg, args);
        }
        Command c = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), msg);
        c.execute();
        GameModule.getGameModule().sendAndLog(c);
    }

    public static void logToChatWithoutUndo(String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg, args);
        }
        Command c = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), msg);
        c.execute();
        GameModule.getGameModule().sendAndLog(c);
    }

    public static void logToChatWithTime(String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg, args);
        }
        final Date currentTime = new Date();

        final SimpleDateFormat sdf =
                new SimpleDateFormat("MMM d, hh:mm:ss a z");

// Give it to me in GMT time.
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String theTime = sdf.format(currentTime);

        Command c = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "* (" + theTime + ")" + msg);
        c.execute();
        GameModule.getGameModule().sendAndLog(c);
    }
    public static Command logToChatWithTimeCommand(String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg, args);
        }
        final Date currentTime = new Date();

        final SimpleDateFormat sdf =
                new SimpleDateFormat("MMM d, hh:mm:ss a z");

// Give it to me in GMT time.
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String theTime = sdf.format(currentTime);

        Command c = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "* (" + theTime + ")" + msg);
        c.execute();
        return c;
    }
    public static Command logToChatCommand(String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg, args);
        }
        Command c = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), msg);
        c.execute();
        return c;
    }
    public static GamePiece newPiece(PieceSlot slot) {
        return PieceCloner.getInstance().clonePiece(slot.getPiece());
    }

    public static XWPlayerInfo getCurrentPlayer() {
        String name = GlobalOptions.getInstance().getPlayerId();
        String userId = GameModule.getGameModule().getUserId();
        String sideStr = PlayerRoster.getMySide();

        return new XWPlayerInfo(parsePlayerSide(sideStr), name, userId);
    }

    public static String getShipStringForReports(boolean isYours, String pilotName, String shipName)
    {

        String yourShipName = (isYours ? GlobalOptions.getInstance().getPlayerId() + "'s" : "another ship");

        if (!pilotName.equals("")) { yourShipName += " " + pilotName; }
        else yourShipName += " ship";
        if (!shipName.equals("")) { yourShipName += " (" + shipName + ")"; }
        else yourShipName += " ";

        return yourShipName;
    }

    private static int parsePlayerSide(String sideStr) {
        if (sideStr == null || sideStr.length() == 0) {
            return XWPlayerInfo.UNKNOWN;
        }

        if (sideStr.contains("observer")) {
            return XWPlayerInfo.OBSERVER_SIDE;
        }

        try {
            sideStr = sideStr.charAt(sideStr.length() - 1) + "";
            return Integer.parseInt(sideStr);
        } catch (Exception e) {
            return XWPlayerInfo.UNKNOWN;
        }
    }

    public static double rotX(double x, double y, double angle){
        return Math.cos(-Math.PI*angle/180.0f)*x - Math.sin(-Math.PI*angle/180.0f)*y;
    }
    public static double rotY(double x, double y, double angle){
        return Math.sin(-Math.PI*angle/180.0f)*x + Math.cos(-Math.PI*angle/180.0f)*y;
    }

    /**
     * Returns true if the two provided shapes areas have any intersection
     *
     * @param shape1
     * @param shape2
     * @return
     */
    public static boolean shapesOverlap(Shape shape1, Shape shape2) {
        Area a1 = new Area(shape1);
        a1.intersect(new Area(shape2));
        return !a1.isEmpty();
    }
    public static Shape getIntersectedShape(Shape shape1, Shape shape2){
        Area a1 = new Area(shape1);
        a1.intersect(new Area(shape2));
        return a1;
    }
    public static boolean isLine2DOverlapShape(Line2D.Double l, Shape s){
        double[] segment = new double[2];
        Point2D.Double p1 = new Point2D.Double();
        Point2D.Double p2 = new Point2D.Double();
        PathIterator pi = s.getPathIterator(null);

        pi.currentSegment(segment);
        p1 = new Point2D.Double(segment[0], segment[1]);
        pi.next();
        pi.currentSegment(segment);
        p2 = new Point2D.Double(segment[0], segment[1]);

        while(!pi.isDone()) {
            Line2D.Double l2 = new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            if(l.intersectsLine(l2)){
                return true;
            }
            else {
                p1 = new Point2D.Double(segment[0], segment[1]);

                pi.next();
                if(pi.isDone()) break;
                pi.currentSegment(segment);
                p2 = new Point2D.Double(segment[0], segment[1]);
            }
        }

        return false;
    }


    //non square rooted Pythagoreas theorem. don't need the real pixel distance, just numbers which can be compared and are proportional to the distance
    public static double nonSRPyth(Point first, Point second){
        return Math.pow(first.getX()-second.getX(),2) + Math.pow(first.getY()-second.getY(),2);
    }

    public static double nonSRPyth(Point2D.Double first, Point2D.Double second){
        return Math.pow(first.getX()-second.getX(),2) + Math.pow(first.getY()-second.getY(),2);
    }


    public static boolean isPointInShape(Point2D.Double pt, Shape shape) {
        Area a = new Area(shape);
        return a.contains(pt.getX(), pt.getY());
    }

    /**
     * Finds raw ship mask and translates and rotates it to the current position and heading
     * of the ship
     *
     * @param bumpable
     * @return Translated ship mask
     */
    public static Shape getBumpableCompareShape(Decorator bumpable) {
        Shape rawShape = getRawShape(bumpable);
        double scaleFactor = 1.0f;

        Shape transformed = AffineTransform.getScaleInstance(scaleFactor, scaleFactor).createTransformedShape(rawShape);

        transformed = AffineTransform
                .getTranslateInstance(bumpable.getPosition().getX(), bumpable.getPosition().getY())
                .createTransformedShape(transformed);
        FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(bumpable), FreeRotator.class));
        double centerX = bumpable.getPosition().getX();
        double centerY = bumpable.getPosition().getY();
        transformed = AffineTransform
                .getRotateInstance(rotator.getAngleInRadians(), centerX, centerY)
                .createTransformedShape(transformed);

        return transformed;
    }


    public static Shape getRawShape(Decorator bumpable) {
        return Decorator.getDecorator(Decorator.getOutermost(bumpable), NonRectangular.class).getShape();
    }

    public static class XWPlayerInfo {
        public static int OBSERVER_SIDE = 66;
        public static int UNKNOWN = -1;

        private int side;
        private String name;
        private String id;

        protected XWPlayerInfo(int side, String name, String id) {
            this.side = side;
            this.name = name;
            this.id = id;
        }

        public int getSide() {
            return side;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return String.format("side=%s, id=%s, name=%s", this.side, this.id, this.name);
        }
    }
}
