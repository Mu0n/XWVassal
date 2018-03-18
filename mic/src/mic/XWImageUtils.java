package mic;

import VASSAL.build.GameModule;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;
import VASSAL.tools.image.ImageUtils;
import VASSAL.tools.io.FileArchive;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XWImageUtils {
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
            {208,70},
            {208,90},
            {208,110},
            {208,130},
            {208,150}
    };

    private static String simplifyFactionName(String faction)
    {
        String newFaction = null;
        if(faction.equals("Rebel Alliance") || faction.equals("Resistance"))
        {
            newFaction = "rebelalliance";

        }else if(faction.equals("Galactic Empire") ||faction.equals("First Order"))
        {
            newFaction = "galacticempire";
        }else if(faction.equals("Scum & Villainy") || faction.equals("Scum and Villainy"))
        {
            newFaction = "scumandvillainy";
        }
        return newFaction;
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
        arcImagePrefixSB.append(faction);


        arcImagePrefixSB.append(".png");

        return arcImagePrefixSB.toString();
    }

    public static void buildBaseShipImage(String faction, String shipXWS, List<String> arcs, List<String> actions, String size, String identifier, String shipImageName, ArchiveWriter writer)
    {
        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();

        try {

            // build the base (cardbard of the ship base)
            BufferedImage newBaseImage = buildShipBase(size, dataArchive);

            // add the arcs to the image
            newBaseImage = addArcsToBaseShipImage(arcs, size, faction, newBaseImage, dataArchive);

            // add the ship to the image
            newBaseImage = addShipToBaseShipImage(shipXWS, newBaseImage, dataArchive, shipImageName);

            // add the actions to the image
            newBaseImage = addActionsToBaseShipImage(actions, size, newBaseImage, dataArchive);

            // save the newly created base image to the module
            saveBaseShipImageToModule(faction, shipXWS, identifier, newBaseImage, writer);


        }catch(IOException e)
        {
            Util.logToChat("Exception occurred generating base ship image for "+shipXWS);
        }


    }

    private static String determineAltShipBaseNameFromImage(String faction, String shipImageName){
        String coreName = shipImageName.replace("Ship_","").replace(".png","");
        return "Ship_Base_"+simplifyFactionName(faction)+"_"+coreName+".png";
    }


    private static void saveBaseShipImageToModule(String faction, String shipXWS, String identifier, BufferedImage baseImage, ArchiveWriter writer)
    {
        String targetBaseImageName = buildShipBaseImageName(faction,shipXWS,identifier);

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
            addImageToModule(targetBaseImageName, bytes, writer);

            tempFile.delete();
        }catch(IOException e)
        {

        }
    }

    private static void saveBaseShipImageToModule(String faction, String shipXWS, BufferedImage baseImage, String targetBaseImageName)
    {
    //    String targetBaseImageName = buildShipBaseImageName(faction,shipXWS);

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

    private static BufferedImage buildShipBase(String size, DataArchive dataArchive) throws IOException
    {

        final String smallBlackBase = "Ship_Generic_Starfield_Small.png";
        final String largeBlackBase = "Ship_Generic_Starfield_Large.png";
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

        InputStream is = dataArchive.getInputStream("images/"+cardboardBaseImageName);
        BufferedImage image = ImageUtils.getImage(cardboardBaseImageName,is);

        return image;
    }

    private static BufferedImage addArcsToBaseShipImage(List<String> arcs,String size, String faction, BufferedImage baseImage, DataArchive dataArchive) throws IOException
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
            InputStream is = dataArchive.getInputStream("images/" + arcImageName);
            BufferedImage arcImage = ImageUtils.getImage(arcImageName, is);//doesn't recognize SVG
             Graphics g = baseImage.getGraphics();
            g.drawImage(arcImage, 0, 0, null);
        }
        return baseImage;
    }

    private static BufferedImage addShipToBaseShipImage(String shipXWS, BufferedImage baseImage, DataArchive dataArchive) throws IOException
    {

        // add the ship
        // determine the ship image to use
        String shipImageName = "Ship_"+shipXWS+".png";


        InputStream is = dataArchive.getInputStream("images/" + shipImageName);

        BufferedImage shipImage = ImageUtils.getImage(shipImageName, is);

        Graphics g = baseImage.getGraphics();

        g.drawImage(shipImage, 0, 0, null);

        return baseImage;

    }

    private static BufferedImage addShipToBaseShipImage(String shipXWS, BufferedImage baseImage, DataArchive dataArchive, String shipImageName) throws IOException
    {
        InputStream is = dataArchive.getInputStream("images/" + shipImageName);
        BufferedImage shipImage = ImageUtils.getImage(shipImageName, is);
        Graphics g = baseImage.getGraphics();
        g.drawImage(shipImage, 0, 0, null);
        return baseImage;

    }

    private static BufferedImage addActionsToBaseShipImage(List<String> actions, String size, BufferedImage baseImage, DataArchive dataArchive) throws IOException
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

    public static String buildShipBaseImageName(String faction, String shipXWS, String identifier)
    {

        //Ship_Base_<faction lowercase no spaces>_<shipXWS>.png

        StringBuilder shipBaseImageSB = new StringBuilder();

        shipBaseImageSB.append("Ship_Base_");

        // find the faction
        shipBaseImageSB.append(faction);


        shipBaseImageSB.append("_");
        shipBaseImageSB.append(shipXWS);

        shipBaseImageSB.append("_");
        shipBaseImageSB.append(identifier);

        shipBaseImageSB.append(".png");

        return shipBaseImageSB.toString();
    }

    public static void downloadAndSaveImagesFromOTA(String imageType, ArrayList<String> imageNames)
    {

        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();
        ArchiveWriter writer = new ArchiveWriter(fileArchive);

        Iterator<String> i = imageNames.iterator();
        while(i.hasNext())
        {
            boolean imageFound = false;
            byte[] imageBytes = null;
            String imageName = i.next();

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
                    addImageToModule(imageName, imageBytes, writer);

                } catch (IOException e) {
                    Util.logToChat("IOException ocurred adding an image " + e.getMessage());
                }
            }

        }

        try {
            writer.save();
        }catch(IOException e)
        {
            Util.logToChat("IOException ocurred saving images " + e.getMessage());
        }

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
                Util.logToChat("IOException ocurred adding an image " + e.getMessage());
            }
        }

    }


    private static byte[] downloadFileFromOTA(String fileType, String fileName) throws IOException
    {
        // Util.logToChat("Downloading image: "+fileName);
        URL OTAImageURL = null;
        String url = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/" + fileType + "/" + fileName;
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

    private static void addImageToModule(String imageName,byte[] imageBytes,  ArchiveWriter writer) throws IOException
    {

        writer.addImage(imageName,imageBytes);
     //   writer.save();
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
}
