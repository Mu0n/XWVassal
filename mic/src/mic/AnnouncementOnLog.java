package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.FileArchive;
import mic.ota.*;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import static mic.Util.logToChat;

/**
 * Created by Mic on 09/04/2017.
 * Make it so the URL pointed at is configurable in the vassal editor?
 */
public class AnnouncementOnLog extends AbstractConfigurable {

    private static String defaultURL =        "https://raw.githubusercontent.com/Mu0n/XWVassal/master/VassalNews";
    private static String currentVersionURL = "https://raw.githubusercontent.com/Mu0n/XWVassal/master/currentVersion";
    private static String blogURL = "https://raw.githubusercontent.com/Mu0n/XWVassal/master/currentBlog";
    private static String vassalDownloadURL = "http://www.vassalengine.org/wiki/Module:Star_Wars:_X-Wing_Miniatures_Game";
    private static String githubDownloadURL = "https://github.com/Mu0n/XWVassal/releases";
    private static String guideURL = "http://xwvassal.info/guide";

    private JFrame updateCheckFrame;

    private static boolean checkComplete = false;

    private synchronized void AnnouncementOnLog() {

    }

    private void downloadContent()
    {
        boolean errorOccurredOnXWingData = false;

        // grab xwing-data: pilots, ships, upgrades, conditions
        // dispatcher: pilots, ships, upgrades, conditions
        // and save to the module
        ArrayList<String> jsonFilesToDownloadFromURL = new ArrayList<String>();
        jsonFilesToDownloadFromURL.add(MasterShipData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(MasterShipData.DISPATCHER_URL);
        jsonFilesToDownloadFromURL.add(MasterPilotData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(MasterPilotData.DISPATCHER_URL);
        jsonFilesToDownloadFromURL.add(MasterUpgradeData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(MasterUpgradeData.DISPATCHER_URL);
        jsonFilesToDownloadFromURL.add(MasterConditionData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(MasterConditionData.DISPATCHER_URL);
        try {
            XWOTAUtils.downloadJSONFilesFromGitHub(jsonFilesToDownloadFromURL);
            mic.Util.logToChat("Core XWing data updated");
        }catch(IOException e)
        {
            errorOccurredOnXWingData = true;
        }

        if(errorOccurredOnXWingData)
        {
            mic.Util.logToChat("Unable to reach XWing-Data server. No update performed");
        }else {

            // check OTA for updates
            ArrayList<OTAImage> imagesToDownload = new ArrayList<OTAImage>();

            OTAImage imageToDownload = null;
            ModuleIntegrityChecker modIntCheck = new ModuleIntegrityChecker();

            // =============================================================
            // Pilots
            // =============================================================
            ArrayList<OTAMasterPilots.OTAPilot> pilots = modIntCheck.checkPilots();
            for (OTAMasterPilots.OTAPilot pilot : pilots) {
                if (!pilot.getStatus()) {
                    imageToDownload = new OTAImage();
                    imageToDownload.setImageName(pilot.getImage());
                    imageToDownload.setImageType("pilots");
                    imageToDownload.setImageDisplayType("Pilot");
                    imageToDownload.setObjectName(MasterPilotData.getPilotData(pilot.getShipXws(), pilot.getPilotXws(), pilot.getFaction()).getName());
                    imagesToDownload.add(imageToDownload);
                    // mic.Util.logToChat("Downloaded "+imageToDownload.getImageType()+" "+imageToDownload.getObjectName());
                }
            }
            pilots = null;

            // =============================================================
            // Ships
            // =============================================================
            ArrayList<OTAMasterShips.OTAShip> ships = modIntCheck.checkShips();
            for (OTAMasterShips.OTAShip ship : ships) {
                if (!ship.getStatus()) {
                    imageToDownload = new OTAImage();
                    imageToDownload.setImageName(ship.getImage());
                    imageToDownload.setImageType("ships");
                    imageToDownload.setImageDisplayType("Ship");
                    imageToDownload.setObjectName(MasterShipData.getShipData(ship.getXws()).getName());
                    imagesToDownload.add(imageToDownload);
                    //mic.Util.logToChat("Downloaded "+imageToDownload.getImageType()+" "+imageToDownload.getObjectName());
                }
            }
            ships = null;

            // =============================================================
            // Upgrades
            // =============================================================
            ArrayList<OTAMasterUpgrades.OTAUpgrade> upgrades = modIntCheck.checkUpgrades();
            for (OTAMasterUpgrades.OTAUpgrade upgrade : upgrades) {
                if (!upgrade.getStatus()) {
                    imageToDownload = new OTAImage();
                    imageToDownload.setImageName(upgrade.getImage());
                    imageToDownload.setImageType("upgrades");
                    imageToDownload.setImageDisplayType("Upgrade");
                    imageToDownload.setObjectName(MasterUpgradeData.getUpgradeData(upgrade.getXws()).getName());
                    imagesToDownload.add(imageToDownload);
                    // mic.Util.logToChat("Downloaded "+imageToDownload.getImageType()+" "+imageToDownload.getObjectName());
                }
            }
            upgrades = null;

            // =============================================================
            // Conditions
            // =============================================================
            ArrayList<OTAMasterConditions.OTACondition> conditions = modIntCheck.checkConditions();
            for (OTAMasterConditions.OTACondition condition : conditions) {
                if (!condition.getStatus()) {
                    imageToDownload = new OTAImage();
                    imageToDownload.setImageName(condition.getImage());
                    imageToDownload.setImageType("conditions");
                    imageToDownload.setImageDisplayType("Condition");
                    imageToDownload.setObjectName(MasterConditionData.getConditionData(condition.getXws()).getName());
                    imagesToDownload.add(imageToDownload);
                    //mic.Util.logToChat("Downloaded "+imageToDownload.getImageType()+" "+imageToDownload.getObjectName());
                }

                if (!condition.getTokenStatus()) {
                    imageToDownload = new OTAImage();
                    imageToDownload.setImageName(condition.getTokenImage());
                    imageToDownload.setImageType("conditions");
                    imageToDownload.setImageDisplayType("Condition Token");
                    imageToDownload.setObjectName(MasterConditionData.getConditionData(condition.getXws()).getName());
                    imagesToDownload.add(imageToDownload);
                }
            }
            conditions = null;

            // =============================================================
            // Dial Hides
            // =============================================================
            ArrayList<OTAMasterDialHides.OTADialHide> dialHides = modIntCheck.checkDialHides();
            for (OTAMasterDialHides.OTADialHide dialHide : dialHides) {
                if (!dialHide.getStatus()) {
                    imageToDownload = new OTAImage();
                    imageToDownload.setImageName(dialHide.getImage());
                    imageToDownload.setImageType("dial");
                    imageToDownload.setImageDisplayType("Dial Hide Ship Image");
                    imageToDownload.setObjectName(MasterShipData.getShipData(dialHide.getXws()).getName());
                    imagesToDownload.add(imageToDownload);
                    //mic.Util.logToChat("Downloaded "+imageToDownload.getImageType()+" "+imageToDownload.getObjectName());
                }
            }
            dialHides = null;

            // =============================================================
            // Check Dial Masks
            // =============================================================
            ArrayList<OTADialMask> dialMasksToGenerate = new ArrayList<OTADialMask>();
            ArrayList<OTADialMask> dialMaskResults = modIntCheck.checkDialMasks();
            Iterator<OTADialMask> dialMaskIterator = dialMaskResults.iterator();
            OTADialMask missingDialMask = null;
            while (dialMaskIterator.hasNext()) {
                missingDialMask = dialMaskIterator.next();
                if (!missingDialMask.getStatus()) {
                    dialMasksToGenerate.add(missingDialMask);
                }
            }

            // =============================================================
            // Check Ship Bases
            // =============================================================
            ArrayList<OTAShipBase> shipBasesToGenerate = new ArrayList<OTAShipBase>();
            ArrayList<OTAShipBase> shipBaseResults = modIntCheck.checkShipBases();
            Iterator<OTAShipBase> shipBaseIterator = shipBaseResults.iterator();
            OTAShipBase missingShipBase = null;
            while (shipBaseIterator.hasNext()) {
                missingShipBase = shipBaseIterator.next();
                if (!missingShipBase.getStatus()) {
                    shipBasesToGenerate.add(missingShipBase);
                }
            }

            // TODO great time for a progress bar here


            // =============================================================
            // Download the missing images
            // =============================================================
            XWOTAUtils.downloadAndSaveImagesFromOTA(imagesToDownload);

            // =============================================================
            // Generate the missing Dial Masks
            // =============================================================
            OTADialMask dialMask = null;
            GameModule gameModule = GameModule.getGameModule();
            DataArchive dataArchive = gameModule.getDataArchive();
            FileArchive fileArchive = dataArchive.getArchive();
            ArchiveWriter writer = new ArchiveWriter(fileArchive);
            dialMaskIterator = dialMasksToGenerate.iterator();
            while (dialMaskIterator.hasNext()) {
                dialMask = dialMaskIterator.next();
                if (!dialMask.getStatus()) {
                    imageToDownload = new OTAImage();
                    imageToDownload.setImageName("");
                    imageToDownload.setImageType("dial");
                    imageToDownload.setImageDisplayType("Dial Hide Ship Image");
                    MasterShipData.ShipData shipData = MasterShipData.getShipData(dialMask.getShipXws());
                    imageToDownload.setObjectName(shipData.getName());

                    XWOTAUtils.buildDialMaskImages(dialMask.getFaction(), dialMask.getShipXws(), dialMask.getDialHideImageName(), dialMask.getDialMaskImageName(), writer);

                }
            }
    //        try {
     //           writer.save();
     //       } catch (IOException e) {
     //           mic.Util.logToChat("Exception occurred saving module");
     //       }
            dialMaskResults = null;

            dialMasksToGenerate = null;


            // =============================================================
            // Generate the missing ship bases
            // =============================================================
            OTAShipBase shipBase = null;
   //         GameModule gameModule = GameModule.getGameModule();
     //       DataArchive dataArchive = gameModule.getDataArchive();
     //       FileArchive fileArchive = dataArchive.getArchive();
    //        ArchiveWriter writer = new ArchiveWriter(fileArchive);
            shipBaseIterator = shipBasesToGenerate.iterator();
            while (shipBaseIterator.hasNext()) {
                shipBase = shipBaseIterator.next();
                if (!shipBase.getStatus()) {
                    imageToDownload = new OTAImage();
                    imageToDownload.setImageName("");
                    imageToDownload.setImageType("Ship Base");
                    imageToDownload.setImageDisplayType("ShipBase");
                    MasterShipData.ShipData shipData = MasterShipData.getShipData(shipBase.getShipXws());
                    imageToDownload.setObjectName(shipData.getName());
                    java.util.List<String> arcs = shipData.getFiringArcs();

                    java.util.List<String> actions = shipData.getActions();

                    //TODO implement huge ships this
                    if (!shipData.getSize().equals("huge")) {

                        XWOTAUtils.buildBaseShipImage(shipBase.getFaction(), shipBase.getShipXws(), arcs, actions, shipData.getSize(), shipBase.getIdentifier(), shipBase.getshipImageName(), writer);
                    }
                    //mic.Util.logToChat("Generated "+imageToDownload.getImageType()+" "+imageToDownload.getObjectName());

                }
            }

            // now save the module
            try {
                writer.save();
            } catch (IOException e) {
                mic.Util.logToChat("Exception occurred saving module");
            }
            shipBaseResults = null;

            shipBasesToGenerate = null;

            // =============================================================
            // Generate a manifest
            // =============================================================
            if (imagesToDownload.size() == 0) {
                mic.Util.logToChat("All content is up to date");
            } else {
                Iterator<OTAImage> imageIterator = imagesToDownload.iterator();
                OTAImage image = null;
                String action = null;
                while (imageIterator.hasNext()) {
                    image = imageIterator.next();
                    action = "Downloaded";
                    if (image.getImageType().equals("Ship Base") || image.getImageType().equals("dial")) {
                        action = "Generated";
                    }
                    mic.Util.logToChat(action + " " + image.getImageDisplayType() + " " + image.getObjectName());
                }
            }

            imagesToDownload = null;
        }
    }

    public void addTo(Buildable parent) {

        // only do this once.  for some reason, it's happening twice

        if(!checkComplete) {
            // first, popup a window telling the user that a check for new content will occur
            updateCheckFrame = new JFrame();
            JPanel panel = new JPanel();
            JLabel spacer;

            panel.setMinimumSize(new Dimension(600, 100));

            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            // add panel content here
            String msg = "About to verify new content. Download delay may occur. Please click OK";
            /*
            panel.add(link);
            panel.add(link2);
            panel.add(link4);
            panel.add(link3);
            panel.add(link6);
            panel.add(link5);*/

            JOptionPane optionPane = new JOptionPane();
            optionPane.setMessage(msg);
            //optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
            optionPane.add(panel);

            JDialog dialog = optionPane.createDialog(updateCheckFrame, "UpdateCheck");

            dialog.setVisible(true);
            updateCheckFrame.toFront();
            updateCheckFrame.repaint();

            // then, after OK, download the xwing-data json, dispatcher json, & any OTA updates and save to the module
            downloadContent();
            //mic.Util.logToChat("Download occurred");
            updateCheckFrame.setVisible(false);
            checkComplete = true;


            // log the manifest of what was updated to the chat window

            openAnnouncementWindow();
        }


    }

    private void openAnnouncementWindow()
    {
        checkForUpdate();

        try {
            URL url = new URL(defaultURL);
            URLConnection con = url.openConnection();
            con.setUseCaches(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;

            while ((line = in.readLine()) != null) {
                logToChat("* " + line);
            }
            in.close();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());

        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
    }

    private void checkForUpdate() {
        String userVersion = GameModule.getGameModule().getGameVersion();
        String msg ="";
        Boolean isGreater = false;
        try {
            URL url = new URL(currentVersionURL);
            URLConnection con = url.openConnection();
            con.setUseCaches(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            line = in.readLine();
            in.close();

                String[] onlineParts = line.split("\\.");
                String[] userParts = userVersion.split("\\.");
                int length = Math.max(userParts.length, onlineParts.length);
                for(int i = 0; i < length; i++) {
                    int userPart = i < userParts.length ?
                            Integer.parseInt(userParts[i]) : 0;
                    int onlinePart = i < onlineParts.length ?
                            Integer.parseInt(onlineParts[i]) : 0;
                    //logToChat("user " + Integer.toString(userPart) + " online " + Integer.toString(onlinePart));
                    if(onlinePart > userPart) {
                        isGreater = true;
                        break;
                    } else if(userPart > onlinePart) break;
                }


                URL urlPatchNotes = new URL(blogURL);
                URLConnection con2 = urlPatchNotes.openConnection();
                con2.setUseCaches(false);
                BufferedReader in2 = new BufferedReader(new InputStreamReader(urlPatchNotes.openStream()));
                String urlPatchString = in2.readLine();
                in2.close();


                if(isGreater == true) msg += "A new version is available! ";
                else msg += "You have the latest version. ";

                msg += "You currently have version " + userVersion + " of the X-Wing Vassal module.\n"
                    + "The latest version available for download is " + line + "\n";

                SwingLink link = new SwingLink("X-Wing Vassal download page", vassalDownloadURL);
                SwingLink link2 = new SwingLink("Alt download page on github", githubDownloadURL);
            SwingLink link4 = new SwingLink("What's new in v" + line, urlPatchString);
                SwingLink link3 = new SwingLink("New? Need help? Go to the web guide", guideURL);
            SwingLink link6 = new SwingLink("Support the X-Wing Vassal module", "http://xwvassal.info/supportmodule.html");
            SwingLink link5 = new SwingLink("Home for the Vassal League", "http://xwvassal.info");
                JFrame frame = new JFrame();
                JPanel panel = new JPanel();
                JLabel spacer;

                panel.setMinimumSize(new Dimension(600,100));

            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(link);
            panel.add(link2);
            panel.add(link4);
            panel.add(link3);
            panel.add(link6);
            panel.add(link5);

                JOptionPane optionPane = new JOptionPane();
                optionPane.setMessage(msg);
                //optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                optionPane.add(panel);
                JDialog dialog = optionPane.createDialog(frame, "Welcome to the X-Wing vassal module");

                dialog.setVisible(true);
                frame.toFront();
                frame.repaint();


        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());

        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
    }

    public void removeFrom(Buildable parent) {
    }

    // <editor-fold desc="unused vassal hooks">
    @Override
    public String[] getAttributeNames() {
        return new String[]{};
    }

    @Override
    public void setAttribute(String s, Object o) {
        // No-op
    }

    @Override
    public String[] getAttributeDescriptions() {
        return new String[]{};
    }

    @Override
    public Class[] getAttributeTypes() {
        return new Class[]{};
    }

    @Override
    public String getAttributeValueString(String key) {
        return defaultURL;
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public HelpFile getHelpFile() {
        return null;
    }


    public class JLabelLink extends JFrame {

        private static final String LABEL_TEXT = "For further information visit:";
        private static final String A_VALID_LINK = "http://stackoverflow.com";
        private static final String A_HREF = "<a href=\"";
        private static final String HREF_CLOSED = "\">";
        private static final String HREF_END = "</a>";
        private static final String HTML = "<html>";
        private static final String HTML_END = "</html>";

        // </editor-fold>
    }
}
