package mic.ota;

import VASSAL.build.GameModule;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;
import VASSAL.tools.image.ImageUtils;
import VASSAL.tools.io.FileArchive;
import mic.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static mic.Util.logToChat;

public class XWOTAUtils {

    public static final String XWD2DATAFILE = "xwd2.zip";
    private static final String SHIP_BASE_ARC_IMAGE_PREFIX = "SBA_";

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

    public static String simplifyFactionName(String faction)
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
        }else if(faction.equals("rebelalliance") || faction.equals("galacticempire") || faction.equals("scumandvillainy"))
        {
            newFaction = faction;
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
        arcImagePrefixSB.append(simplifyFactionName(faction));


        arcImagePrefixSB.append(".png");

        return arcImagePrefixSB.toString();
    }

    public static void buildBaseShipImage2e(String faction, String shipXWS, String size, String identifier, String shipImageName, ArchiveWriter writer)
    {
        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();

        try {
            BufferedImage newBaseImage = buildShipBase2e(size, dataArchive);

            //add the faction-decided arc color
            String arcImageName = findArcImageName(size, faction);
            newBaseImage = addShipToBaseShipImage2e(shipXWS, newBaseImage, dataArchive, arcImageName);
            newBaseImage = addShipToBaseShipImage2e(shipXWS, newBaseImage, dataArchive, arcImageName);


            //add the ship gfx of the ship
            newBaseImage = addShipToBaseShipImage2e(shipXWS, newBaseImage, dataArchive, shipImageName);


            saveBaseShipImageToModule(faction, shipXWS, identifier, newBaseImage, writer, 2);
        }catch(IOException e)
        {
            Util.logToChat("Exception occurred generating base ship image for "+shipXWS);
        }


    }

    private static String findArcImageName(String size, String faction) {
        String sb = "";
        sb+=SHIP_BASE_ARC_IMAGE_PREFIX + faction + "_" + size + ".png";
        return sb;
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
            saveBaseShipImageToModule(faction, shipXWS, identifier, newBaseImage, writer, 1);


        }catch(IOException e)
        {
            Util.logToChat("Exception occurred generating base ship image for "+shipXWS);
        }


    }


    public static void buildDialMaskImages(String faction, String shipXWS, String dialHideImageName , String dialMaskImageName, ArchiveWriter writer)
    {
        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        final String rebelDialImage = "DialBack_rebelalliance.png";
        final String imperialDialImage = "DialBack_galacticempire.png";
        final String scumDialImage = "DialBack_scumandvillainy.png";

        // get the correct image for the faction background
        String dialBackImage = null;
        if(faction.equals("rebelalliance"))
        {
            dialBackImage = rebelDialImage;
        }else if(faction.equals("galacticempire"))
        {
            dialBackImage = imperialDialImage;
        }else{
            dialBackImage = scumDialImage;
        }

        try {

            InputStream is = dataArchive.getInputStream("images/"+dialBackImage);
            BufferedImage image = ImageUtils.getImage(dialBackImage,is);

            // apply the ship hide image
            InputStream is2 = dataArchive.getInputStream("images/"+dialHideImageName);
            BufferedImage dialHideImage = ImageUtils.getImage(dialHideImageName, is2);
            Graphics g = image.getGraphics();
            g.drawImage(dialHideImage, 0, 0, null);

            // save it
            saveDialMaskImageToModule(dialMaskImageName, image, writer);


        }catch(IOException e)
        {
            Util.logToChat("Exception occurred generating dial mask image for "+faction+"/"+shipXWS);
        }


    }

    private static String determineAltShipBaseNameFromImage(String faction, String shipImageName){
        String coreName = shipImageName.replace("Ship_","").replace(".png","");
        return "Ship_Base_"+simplifyFactionName(faction)+"_"+coreName+".png";
    }


    private static void saveBaseShipImageToModule(String faction, String shipXWS, String identifier, BufferedImage baseImage, ArchiveWriter writer, int edition)
    {
        String targetBaseImageName = buildShipBaseImageName(faction,shipXWS,identifier, edition);

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

    private static void saveDialMaskImageToModule(String dialMaskImageName, BufferedImage dialMaskImage, ArchiveWriter writer)
    {

        File tempFile = null;
        try {
            tempFile = File.createTempFile("XWVassalMaskImage", "");

            ImageIO.write(dialMaskImage, "PNG", tempFile);
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
            addImageToModule(dialMaskImageName, bytes, writer);

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

    private static BufferedImage buildShipBase2e(String size, DataArchive dataArchive) throws IOException
    {
        final String smallBlackBase = "Ship2e_generic_small.png";
        final String mediumBlackBase = "Ship2e_generic_medium.png";
        final String largeBlackBase = "Ship2e_generic_large.png";
        String cardboardBaseImageName = null;

        // determine which background to use (size)
        if(size.equals("small"))
        {
            cardboardBaseImageName = smallBlackBase;
        }else if(size.equals("medium"))
        {
            cardboardBaseImageName = mediumBlackBase;
        }else if(size.equals("large"))
        {
            cardboardBaseImageName = largeBlackBase;
        }else if(size.equals("huge"))
        {
            //TODO replace eventually one day...huge is cut off from downloadall2e anyway in contentchecker
            cardboardBaseImageName = largeBlackBase;
        }
        InputStream is = dataArchive.getInputStream("images/"+cardboardBaseImageName);
        BufferedImage image = ImageUtils.getImage(cardboardBaseImageName,is);

        return image;
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

    private static BufferedImage addShipToBaseShipImage2e(String shipXWS, BufferedImage baseImage, DataArchive dataArchive, String imageName) throws IOException
    {
        InputStream is = dataArchive.getInputStream("images/" + imageName);

        BufferedImage imageToAdd = ImageUtils.getImage(imageName, is);

        Graphics g = baseImage.getGraphics();
        g.drawImage(imageToAdd, 0, 0, null);

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

    public static String buildShipBaseImageName(String faction, String shipXWS, String identifier, int edition)
    {

        //Ship_Base_<faction lowercase no spaces>_<shipXWS>.png

        StringBuilder shipBaseImageSB = new StringBuilder();

        if(edition == 1) shipBaseImageSB.append("Ship_Base_");
        else if(edition == 2) shipBaseImageSB.append("SB_2e_");

        // find the faction
        shipBaseImageSB.append(faction);


        shipBaseImageSB.append("_");
        shipBaseImageSB.append(shipXWS);

        shipBaseImageSB.append("_");
        shipBaseImageSB.append(identifier);

        shipBaseImageSB.append(".png");

        return shipBaseImageSB.toString();
    }


    public static String buildDialMaskImageName(String faction, String shipXWS)
    {

        //DialMask_<faction lowercase no spaces>_<shipXWS>.png

        StringBuilder shipBaseImageSB = new StringBuilder();

        shipBaseImageSB.append("DialMask_");

        // find the faction
        shipBaseImageSB.append(faction);


        shipBaseImageSB.append("_");
        shipBaseImageSB.append(shipXWS);


        shipBaseImageSB.append(".png");

        return shipBaseImageSB.toString();
    }

    public static void downloadJSONFilesFromGitHub(ArrayList<String> jsonFiles, boolean keepPrefix) throws IOException
    {
        String pathToUse = XWOTAUtils.getModulePath();
            FileOutputStream fos = new FileOutputStream(pathToUse + File.separator + XWD2DATAFILE);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            String fileName = null;
            byte[] fileContents = null;
            for (String jsonFile : jsonFiles)
            {
                if(keepPrefix == false) fileName = jsonFile.substring(jsonFile.lastIndexOf("/")+1,jsonFile.length());
                else fileName = jsonFile.substring(jsonFile.lastIndexOf("/data/"),jsonFile.length());
                fileContents = downloadFileFromOTA(jsonFile);

                File fileToZip = new File(fileName);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);
                int length;
                while ((length = fis.read(fileContents)) >= 0) {
                    zipOut.write(fileContents, 0, length);
                }
                zipOut.close();
                fis.close();
            }
            fos.close();
    }
    public static void downloadAndSaveImagesFromOTA( ArrayList<OTAImage> imagesToDownload, String branchURL)
    {

        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();
        ArchiveWriter writer = new ArchiveWriter(fileArchive);

        Iterator<OTAImage> i = imagesToDownload.iterator();
        OTAImage image = null;
        while(i.hasNext())
        {

            boolean imageFound = false;
            byte[] imageBytes = null;
            image = i.next();

            try {

                imageBytes = downloadFileFromOTA(image.getImageType(), image.getImageName(), branchURL);

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
                    addImageToModule(image.getImageName(), imageBytes, writer);

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

    public static void downloadImagesFromOTA(String imageType, ArrayList<String> imageNames, ArchiveWriter writer, String branchURL)
    {
        Iterator<String> i = imageNames.iterator();
        while(i.hasNext())
        {

            boolean imageFound = false;
            byte[] imageBytes = null;
            String imageName = i.next();

            try {

                imageBytes = downloadFileFromOTA(imageType, imageName, branchURL);

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

    }

    public static void downloadAndSaveImagesFromOTA(String imageType, ArrayList<String> imageNames, String branchURL)
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

                imageBytes = downloadFileFromOTA(imageType, imageName, branchURL);

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


    public static void downloadAndSaveImageFromOTA(String imageType, String imageName, String branchURL)
    {
        boolean imageFound = false;
        byte[] imageBytes = null;

        // download the image

        try {

            imageBytes = downloadFileFromOTA(imageType, imageName, branchURL);

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

    private static byte[] downloadFileFromOTA(String urlString) throws IOException
    {
        URL remoteURL = null;
        remoteURL = new URL(urlString);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        is = remoteURL.openStream();
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


    private static byte[] downloadFileFromOTA(String fileType, String fileName, String branchURL) throws IOException
    {
        URL OTAImageURL = null;
        //String url = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/" + fileType + "/" + fileName;
        String url = branchURL + fileType + "/" + fileName;
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

    public static boolean imageExistsInOTA(String fileType, String fileName, String branchURL)
    {

        String url = branchURL + fileType + "/" + fileName;

        HttpURLConnection httpUrlConn;
        try {
            httpUrlConn = (HttpURLConnection) new URL(url).openConnection();

            httpUrlConn.setRequestMethod("HEAD");

            // Set timeouts in milliseconds
            httpUrlConn.setConnectTimeout(30000);
            httpUrlConn.setReadTimeout(30000);

            return (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean fileExitsOnTheNet(String theURL)
    {

        HttpURLConnection httpUrlConn;
        try {
            httpUrlConn = (HttpURLConnection) new URL(theURL).openConnection();

            httpUrlConn.setRequestMethod("HEAD");

            // Set timeouts in milliseconds
            httpUrlConn.setConnectTimeout(30000);
            httpUrlConn.setReadTimeout(30000);

            return (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static void addFileToModule(String fileName,byte[] fileBytes, ArchiveWriter writer) throws IOException
    {
    //    GameModule gameModule = GameModule.getGameModule();
    //    DataArchive dataArchive = gameModule.getDataArchive();
    //    FileArchive fileArchive = dataArchive.getArchive();
     //   ArchiveWriter writer = new ArchiveWriter(fileArchive);
        Util.logToChat("addFileToModule used");

        writer.addImage(fileName,fileBytes);
        Util.logToChat("attempted to write " + fileName + " and I can find it? " + (writer.getArchive().contains(fileName)?"yes":"no"));
        //   writer.save();
    }



    public static void addFileToModule(String fileName, byte[] fileBytes) throws IOException
    {
            GameModule gameModule = GameModule.getGameModule();
            DataArchive dataArchive = gameModule.getDataArchive();
            FileArchive fileArchive = dataArchive.getArchive();
            ArchiveWriter writer = new ArchiveWriter(fileArchive);
            //addFileToModule(fileName, fileBytes,writer);
            writer.addFile(fileName,fileBytes);
            writer.save();
    }

    public static void addImageToModule(String imageName,byte[] imageBytes) throws IOException
    {
        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();
        ArchiveWriter writer = new ArchiveWriter(fileArchive);
        writer.addImage(imageName,imageBytes);
        writer.save();
    }

    public static void addImageToModule(String imageName,byte[] imageBytes,  ArchiveWriter writer) throws IOException
    {
        try{
            writer.removeImage(imageName);
        }catch(Exception e){}
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

    public static boolean fileExistsInModule(String fileName)
    {
        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();

        boolean found = false;
        try {
            found = fileArchive.contains(fileName);

        }catch(Exception e)
        {
            Util.logToChat("Exception searching for file in module");
        }

        return found;
    }

    public static boolean amIDoingOrder66(){

        boolean doingOrder66 = false;
        if (!XWOTAUtils.fileExistsInModule("cdr")) {
            doingOrder66 = false;
        } else {
            // read contents of cdr
            String wantNotifStr = null;
            try {

                InputStream inputStream = GameModule.getGameModule().getDataArchive().getInputStream("cdr");
                if (inputStream == null) {
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder contents = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    contents.append(line);
                }
                reader.close();

                wantNotifStr = contents.toString();
                if (wantNotifStr.equalsIgnoreCase("yes")) {
                    doingOrder66 = true;
                }
                else doingOrder66 = false;
                inputStream.close();
            } catch (Exception e) {
            }
        }
         return doingOrder66;
    }



    public static void checkOnlineOrder66(){
        String O66URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/master/oss";
        String line = "";
        try {
            URL url = new URL(O66URL);
            URLConnection con = url.openConnection();
            con.setUseCaches(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = in.readLine()) != null) {
                if(line.equals("1")) {
                    String choice = "yes";
                    XWOTAUtils.addFileToModule("cdr", choice.getBytes());
                }
                else if(line.equals("0")) {
                    String choice = "no";
                    XWOTAUtils.addFileToModule("cdr", choice.getBytes());
                }
            }
            in.close();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());

        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
    }

    public static boolean checkExistenceOfLocalXWD2Zip()
    {
        File dummyFile = new File(GameModule.getGameModule().getDataArchive().getName());
        String path = dummyFile.getPath();
        File theZip = new File( path.substring(0,path.lastIndexOf(File.separator)) + File.separator + XWOTAUtils.XWD2DATAFILE);
        return theZip.exists() && theZip.isFile();
    }

    public static String getModulePath() {
        File dummyFile = new File(GameModule.getGameModule().getDataArchive().getName());
        String path = dummyFile.getPath();
        return path.substring(0,path.lastIndexOf(File.separator));
    }
}
