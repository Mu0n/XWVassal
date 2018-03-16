package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.counters.*;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;
import VASSAL.tools.image.ImageUtils;
import VASSAL.tools.io.FileArchive;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by amatheny on 2/9/17.
 */
public class Util {

    private static String[] actionOrder = {
            "cloak",
        "rotatearc",
        "reload",
        "reinforce",
        "coordinate",
        "slam",
        "evade",
        "boost",
        "barrelroll",
        "targetlock",
        "focus"
};


    private static final int[][] SMALL_SHIP_ACTION_COORD = {
            {95,20},
            {95,40},
            {95,60},
            {95,80},
            {95,100}
    };

    private static final int[][] LARGE_SHIP_ACTION_COORD = {
            {0,50},
            {0,50},
            {0,50},
            {0,50},
            {0,50}
    };

    private static final int[] LARGE_SHIP_ACTION_1_COORD = {0,0};
    private static final int[] LARGE_SHIP_ACTION_2_COORD = {0,0};
    private static final int[] LARGE_SHIP_ACTION_3_COORD = {0,0};
    private static final int[] LARGE_SHIP_ACTION_4_COORD = {0,0};
    private static final int[] LARGE_SHIP_ACTION_5_COORD = {0,0};

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


    public static boolean imageExistsInModule(String imageName)
    {
        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();

        boolean found = false;
        try {

            found = fileArchive.contains("images/" + imageName);

        }catch(Exception e)
        {
            Util.logToChat("Exception searching for image in module");
        }

        return found;

    }



    public static String buildFiringArcImageName(String size, String faction, String arc)
    {

        //Firing_Arc_<xws firing_arc, lower case, remove spaces>_<xws size>_<xws faction, lower case, remove spaces>.svg
        //Note: "Resistance" needs to be "rebelalliance" and "First Order" needs to be "galacticempire"

        StringBuilder arcImagePrefixSB = new StringBuilder();

        arcImagePrefixSB.append("Firing_Arc_");

        String arcName = arc.toLowerCase().replaceAll(" ","");
        arcImagePrefixSB.append(arcName);
        arcImagePrefixSB.append("_");

        arcImagePrefixSB.append(size.toLowerCase());
        arcImagePrefixSB.append("_");

        // find the faction
        if(faction.equals("Rebel Alliance") || faction.equals("Resistance"))
        {
            arcImagePrefixSB.append("rebelalliance");
        }else if(faction.equals("Galactic Empire") ||faction.equals("First Order"))
        {
            arcImagePrefixSB.append("galacticempire");
        }else if(faction.equals("Scum & Villainy") || faction.equals("Scum and Villainy"))
        {
            arcImagePrefixSB.append("scumandvillainy");
        }

        //arcImagePrefixSB.append(".svg");
        arcImagePrefixSB.append(".png");

        return arcImagePrefixSB.toString();
    }

    public static void buildBaseShipImage(String faction,String shipXWS, List<String> arcs, List<String> actions, String size)
    {
        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();

        // build the base (cardbard of the ship base)
        BufferedImage newBaseImage = buildShipBase(size, dataArchive);

        // add the arcs to the image
        newBaseImage = addArcsToBaseShipImage(arcs, size, faction, newBaseImage, dataArchive);

        // add the ship to the image
        newBaseImage = addShipToBaseShipImage(shipXWS, newBaseImage, dataArchive);

        // add the actions to the image
        newBaseImage = addActionsToBaseShipImage(actions, size, newBaseImage, dataArchive);

        // save the newly created base image to the module
        saveBaseShipImageToModule(faction, shipXWS, newBaseImage);


    }

    private static void saveBaseShipImageToModule(String faction, String shipXWS, BufferedImage baseImage)
    {
        String targetBaseImageName = buildShipBaseImageName(faction,shipXWS);

        File tempFile = null;
        try {
            tempFile = File.createTempFile("XWVassalBaseImage", "");

            ImageIO.write(baseImage, "PNG", tempFile);
        }catch(IOException e)
        {

        }
        try {
            FileInputStream fis = new FileInputStream(tempFile);
            byte[] byteChunk = new byte[4096];
            int n;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((n = fis.read(byteChunk)) > 0) {
                baos.write(byteChunk, 0, n);
            }
            if (fis != null) {
                fis.close();
            }
            byte[] bytes = baos.toByteArray();
            baos.close();
            addImageToModule(targetBaseImageName, bytes);

            tempFile.delete();
        }catch(IOException e)
        {

        }
    }

    private static BufferedImage buildShipBase(String size, DataArchive dataArchive)
    {

        final String smallBlackBase = "Ship_Generic_Starfield_Small.png";
        final String largeBlackBase = "Ship_Generic_Starfield_Lmall.png";
        String cardboardBaseImageName = null;


        // determine which background to use (size)
        if(size.equals("small"))
        {
            cardboardBaseImageName = smallBlackBase;
        }else if(size.equals("huge"))
        {

        }else if(size.equals("large"))
        {
            cardboardBaseImageName = largeBlackBase;
        }

        BufferedImage image = null;
        try{
            InputStream is = dataArchive.getInputStream("images/"+cardboardBaseImageName);
            image = ImageUtils.getImage(cardboardBaseImageName,is);

        }catch(IOException e)
        {

        }

        return image;
    }

    private static BufferedImage addArcsToBaseShipImage(List<String> arcs,String size, String faction, BufferedImage baseImage, DataArchive dataArchive)
    {

        List<String> arcImageNames = new ArrayList<String>();
        // determine which arcs to use
        for(String arc : arcs)
        {
            String arcImage = buildFiringArcImageName(size,faction,arc);
            arcImageNames.add(arcImage);
        }
        // add the arcs
        for(String arcImageName : arcImageNames)
        {
            try {
                InputStream is = dataArchive.getInputStream("images/" + arcImageName);

                BufferedImage arcImage = ImageUtils.getImage(arcImageName, is);//doesn't recognize SVG

                Graphics g = baseImage.getGraphics();

                g.drawImage(arcImage, 0, 0, null);
            }catch(IOException e)
            {
                Util.logToChat("Exception getting image: images/" + arcImageName);
                Util.logToChat(e.toString());
            }
        }
        return baseImage;
    }

    private static BufferedImage addShipToBaseShipImage(String shipXWS, BufferedImage baseImage, DataArchive dataArchive)
    {

        // add the ship
        // determine the ship image to use
        String shipImageName = "Ship_"+shipXWS+".png";
        try{

            InputStream is = dataArchive.getInputStream("images/" + shipImageName);

            BufferedImage shipImage = ImageUtils.getImage(shipImageName, is);

            Graphics g = baseImage.getGraphics();

            g.drawImage(shipImage, 0, 0, null);
        }catch(IOException e)
        {
            Util.logToChat("Exception occurred getting ship image "+shipImageName);
        }
        return baseImage;

    }

    private static BufferedImage addActionsToBaseShipImage(List<String> actions, String size, BufferedImage baseImage, DataArchive dataArchive)
    {

        List<String> actionImageNames = new ArrayList<String>();
        //sort the action order
        actions = sortActions(actions);

        for(String action : actions)
        {
            String actionImage = "Action_"+action+".png";
            actionImageNames.add(actionImage);
        }
        int numActions = actionImageNames.size();
        int actionNum = 0;
        // add the actions
        for(String actionImageName : actionImageNames)
        {

            try {
                InputStream is = dataArchive.getInputStream("images/" + actionImageName);
                BufferedImage actionImage = ImageUtils.getImage(actionImageName, is);

                Graphics g = baseImage.getGraphics();
                // g.drawImage(combined, 0, 0, null);


                // need to place the images properly

                int actionX = 0;
                int actionY = 0;
                if(size.equals("small"))
                {
                    actionX = SMALL_SHIP_ACTION_COORD[actionNum][0];
                    actionY = SMALL_SHIP_ACTION_COORD[actionNum][1];



                }else if(size.equals("large"))
                {
                    actionX = LARGE_SHIP_ACTION_COORD[actionNum][0];
                    actionY = LARGE_SHIP_ACTION_COORD[actionNum][1];
                }

                actionNum++;


                g.drawImage(actionImage, actionX, actionY, null);
            }catch(IOException e)
            {

            }
        }
        return baseImage;
    }

    private static String simplifyActionName(String action)
    {
        return action.toLowerCase().replaceAll(" ","");
    }

    private static List<String> sortActions(List<String> actions)
    {
        List<String> sortedActions = new ArrayList<String>();

        // loop through each action in order
        for(int i=0;i<actionOrder.length;i++)
        {
            //see if this ship has that action
            boolean found = false;

            for(int j=0;j<actions.size() && !found ;j++)
            {
                if(actionOrder[i].equals(simplifyActionName(actions.get(j))))
                {
                    found = true;
                    break;
                }
            }

            if(found)
            {
                sortedActions.add(actionOrder[i]);
            }
        }


        return sortedActions;
    }

    public static String buildShipBaseImageName(String faction, String shipXWS)
    {

        //Ship_Base_<faction lowercase no spaces>_<shipXWS>.png

        StringBuilder shipBaseImageSB = new StringBuilder();

        shipBaseImageSB.append("Ship_Base_");


        // find the faction
        if(faction.equals("Rebel Alliance") || faction.equals("Resistance"))
        {
            shipBaseImageSB.append("rebelalliance");
        }else if(faction.equals("Galactic Empire") ||faction.equals("First Order"))
        {
            shipBaseImageSB.append("galacticempire");
        }else if(faction.equals("Scum & Villainy") || faction.equals("Scum and Villainy"))
        {
            shipBaseImageSB.append("scumandvillainy");
        }

        shipBaseImageSB.append("_");
        shipBaseImageSB.append(shipXWS);

        shipBaseImageSB.append(".png");



        return shipBaseImageSB.toString();
    }

    public static void downloadAndSaveImageFromOTA(String imageType, String imageName)
    {
        boolean imageFound = false;
        byte[] imageBytes = null;

            // download the image

        try {

            imageBytes = downloadFileFromOTA(imageType, imageName);

            if(imageBytes != null)
            {
                imageFound = true;
            }
        }catch(IOException e)
        {
            // OTA doesn't have the image
            imageFound = false;
        }


        if(imageFound)
        {
            try {
                // add the image to the module
                addImageToModule(imageName, imageBytes);

            } catch (IOException e) {
                logToChat("IOException ocurred adding an image " + e.getMessage());
            }
        }

    }

    private static byte[] downloadFileFromOTA(String imageType, String fileName) throws IOException
    {
       // Util.logToChat("Downloading image: "+fileName);
        URL OTAImageURL = null;
        String url = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/" + imageType + "/" + fileName;
        OTAImageURL = new URL(url);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        is = OTAImageURL.openStream();



        byte[] byteChunk = new byte[4096];
        int n;

        while ((n = is.read(byteChunk)) > 0) {
            baos.write(byteChunk, 0, n);
        }
        if (is != null) {
            is.close();
        }
        byte[] bytes = baos.toByteArray();
        baos.close();
        return bytes;
    }


    private static void addImageToModule(String imageName,byte[] imageBytes) throws IOException
    {
        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();
        ArchiveWriter writer = new ArchiveWriter(fileArchive);
        writer.addImage(imageName,imageBytes);
        writer.save();
    }
}
