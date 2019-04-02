package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.counters.*;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.FileArchive;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import mic.ota.XWOTAUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
    public static <T> T loadClasspathJsonInSideZip(String sideZipName, String filename, Class<T> type) {
        try {
            String pathToUse = XWOTAUtils.getModulePath();
            DataArchive dataArchive = new DataArchive(pathToUse + File.separator + sideZipName);
            InputStream inputStream = dataArchive.getInputStream(filename);
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

    public static <T> T loadClasspathJsonInDepot(String filename, Class<T> type, InputStream is) {
        try {
            if (is == null) {
                logToChat("couldn't load " + filename);
            }
            return mapper.readValue(is, type);
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
        //GameModule.getGameModule().sendAndLog(c);
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

    public static Command logToChatWithTimeCommandNoExecute(String msg, Object... args) {
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
        return c;
    }
    public static Command logToChatCommand(String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg, args);
        }
        Command c = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), msg);
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

    public static PlayerRoster.PlayerInfo[] getAllPlayerInfo() {
            List<PlayerRoster> listOfPR = GameModule.getGameModule().getAllDescendantComponentsOf(PlayerRoster.class);
            return listOfPR.get(0).getPlayers();
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


    public static Shape getCenteredDot(Decorator bumpable, int which, Shape finalShipShape) {
        float diameter = 50.0f;
        float gapFromSide = 6.0f;

        double scaleFactor = 1.0f;
        double x=0.0f;
        double y=0.0f;
        switch(which){
            case 1:
                x = gapFromSide;
                y = gapFromSide;
                break;
            case 2:
                x = bumpable.getShape().getBounds().getMaxX() - gapFromSide;
                y = bumpable.getShape().getBounds().getCenterY();
                break;
            case 3:
                x = gapFromSide;
                y = bumpable.getShape().getBounds().getMaxY() - gapFromSide;
                break;
        }
        Shape dot = new Ellipse2D.Float(-diameter/2, -diameter/2, diameter, diameter);

        Shape transformed = AffineTransform
                .getTranslateInstance(bumpable.getPosition().x + x, bumpable.getPosition().y + y)
                .createTransformedShape(dot);
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

    public static Shape getImageShape(String imageFileName) {
        //load the image
        try {
            BufferedImage image;
            GameModule gameModule = GameModule.getGameModule();
            DataArchive dataArchive = gameModule.getDataArchive();
            FileArchive fileArchive = dataArchive.getArchive();

            InputStream inputstream = new BufferedInputStream(fileArchive.getInputStream("images/" + imageFileName));
            image = ImageIO.read(inputstream);
            inputstream.close();

            final int w = image.getWidth();
            final int h = image.getHeight();
            final int[] pixels = image.getRGB(0, 0, w, h, new int[w*h], 0, w);

            Area outline = new Area();
            for (int y = 0; y < h; ++y) {
                int left = -1;
                for (int x = 0; x < w; ++x) {
                    if (((pixels[x + y * w] >>> 24) & 0xff) > 0) {
                        if (left < 0) {
                            left = x;
                        }
                    } else if (left > -1) {
                        outline.add(new Area(new Rectangle(left, y, x - left, 1)));
                        left = -1;
                    }
                }

                if (left > -1) {
                    outline.add(new Area(new Rectangle(left, y, w - left, 1)));
                }
            }
            Shape returnShape = AffineTransform.getTranslateInstance(-w / 2, -h / 2)
                    .createTransformedShape(outline);

            return returnShape;
        }
        catch(Exception e){
            Util.logToChat("Failed to load image's Shape with " + imageFileName);
            return null;
        }
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

    public static String serializeToBase64(Serializable obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(obj);
        out.close();
        byte[] bytes = bos.toByteArray();
        return org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
    }

    public static Object deserializeBase64Obj(String base64) throws Exception {
        ByteArrayInputStream strIn = new ByteArrayInputStream(org.apache.commons.codec.binary.Base64.decodeBase64(base64));
        ObjectInputStream in = new ObjectInputStream(strIn);
        Object obj = in.readObject();
        in.close();
        return obj;
    }
/*
    public static void logTriggerActionNames(GamePiece p)
    {
        Class<?> type = TriggerAction.class;
        while (p instanceof Decorator) {

            if (type.isInstance(p) ) {
                mic.Util.logToChat(((TriggerAction) p).getDescription());
            }
            p = ((Decorator) p).getInner();
        }

    }*/

    public static GamePiece getTriggerAction(GamePiece p, String name)
    {
        Class<?> type = TriggerAction.class;
        while (p instanceof Decorator) {

            if (type.isInstance(p) && ((TriggerAction) p).getDescription().equals(name)) {
                return p;
            }
            p = ((Decorator) p).getInner();
        }
        return null;
    }

    public static GamePiece getEmbellishment(GamePiece p, String name) {

        Class<?> type = Embellishment.class;
        while (p instanceof Decorator) {

            if (type.isInstance(p) && ((Embellishment) p).getDescription().equals(name)) {
                return p;
            }
            p = ((Decorator) p).getInner();
        }
        return null;
    }

    public static GamePiece getPlaceMarkerTrait(GamePiece p, String name)
    {
        Class<?> type = PlaceMarker.class;
        while (p instanceof Decorator) {

            if (type.isInstance(p) && ((PlaceMarker) p).getDescription().equals(name)) {
                return p;
            }
            p = ((Decorator) p).getInner();
        }
        return null;
    }






}
