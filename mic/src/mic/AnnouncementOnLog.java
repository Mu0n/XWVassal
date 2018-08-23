package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.properties.MutablePropertiesContainer;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.FileArchive;
import com.google.common.collect.Lists;
import mic.ota.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static mic.Util.logToChat;

/**
 * Created by Mic on 09/04/2017.
 * Make it so the URL pointed at is configurable in the vassal editor?
 */
public class AnnouncementOnLog extends AbstractConfigurable {

    // debug flag - setting this to false skips the onLoad download of OTA
    private static final boolean DEBUG_DO_DOWNLOAD = true;

    private static String defaultURL =        "https://raw.githubusercontent.com/Mu0n/XWVassal-website/master/VassalNews";
    private static String currentVersionURL = "https://raw.githubusercontent.com/Mu0n/XWVassal-website/master/currentVersion";
    private static String blogURL = "https://raw.githubusercontent.com/Mu0n/XWVassal-website/master/currentBlog";
    private static String vassalDownloadURL = "http://www.vassalengine.org/wiki/Module:Star_Wars:_X-Wing_Miniatures_Game";
    private static String githubDownloadURL = "https://github.com/Mu0n/XWVassal/releases";
    private static String guideURL = "http://xwvassal.info/guide";

    private JFrame updateCheckFrame;

    private static boolean checkComplete = false;

    private synchronized void AnnouncementOnLog() {

    }

    private boolean downloadXwingDataAndDispatcherJSONFiles()
    {
        boolean errorOccurredOnXWingData = false;

        ArrayList<String> jsonFilesToDownloadFromURL = new ArrayList<String>();
        jsonFilesToDownloadFromURL.add(MasterShipData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(OTAContentsChecker.OTA_DISPATCHER_SHIPS_JSON_URL);
        jsonFilesToDownloadFromURL.add(MasterPilotData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(OTAContentsChecker.OTA_PILOTS_JSON_URL);
        jsonFilesToDownloadFromURL.add(MasterUpgradeData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(OTAContentsChecker.OTA_DISPATCHER_UPGRADES_JSON_URL);
        jsonFilesToDownloadFromURL.add(MasterConditionData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(OTAContentsChecker.OTA_DISPATCHER_CONDITIONS_JSON_URL);



        //2nd edition. check the manifest's list of files to load and get them all

        //all the ShipPilots jsons
        XWS2Pilots.pilotsDataSources whereToGetPilots = mic.Util.loadRemoteJson(XWS2Pilots.remoteUrl, XWS2Pilots.pilotsDataSources.class);

        for(XWS2Pilots.OneFactionGroup oSDS : whereToGetPilots.getPilots()){
            jsonFilesToDownloadFromURL.add(XWS2Pilots.guidoRootUrl + oSDS.getShipUrlSuffixes());
        }
        try {
            XWOTAUtils.downloadJSONFilesFromGitHub(jsonFilesToDownloadFromURL);
        }catch(IOException e)
        {
            errorOccurredOnXWingData = true;
        }

        return errorOccurredOnXWingData;
    }
/*
    private void downloadContent()
    {

        // grab xwing-data: pilots, ships, upgrades, conditions
        // dispatcher: pilots, ships, upgrades, conditions
        // and save to the module
        boolean errorOccurredOnXWingData = downloadXwingDataAndDispatcherJSONFiles();

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
            XWOTAUtils.downloadAndSaveImagesFromOTA(imagesToDownload, OTAContentsChecker.OTA_RAW_BRANCH_URL);

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
                    imageToDownload.setImageDisplayType("Dial Mask");
                    MasterShipData.ShipData shipData = MasterShipData.getShipData(dialMask.getShipXws());
                    imageToDownload.setObjectName(shipData.getName());
                    imagesToDownload.add(imageToDownload);
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
                    imageToDownload.setImageType("ShipBase");
                    imageToDownload.setImageDisplayType("Ship Base");
                    MasterShipData.ShipData shipData = MasterShipData.getShipData(shipBase.getShipXws());
                    imageToDownload.setObjectName(shipData.getName());
                    imagesToDownload.add(imageToDownload);
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
                    action = "Downloaded Image for";

                    if(image.getImageDisplayType().equals("Ship Base"))
                    {
                        mic.Util.logToChat("Generated Ship Base image for Ship: "+image.getObjectName());
                    }else if(image.getImageDisplayType().equals("Dial Mask"))
                    {
                        mic.Util.logToChat("Generated Dial Mask image for Ship: "+image.getObjectName());
                    }else{
                        mic.Util.logToChat("Downloaded image for "+image.getImageDisplayType()+ ": " + image.getObjectName());
                    }

                }
            }

            imagesToDownload = null;
        }
    }
*/
    public void addTo(Buildable parent) {
        openAnnouncementWindow();
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


            if(isGreater == true) msg += "A new version is available!";
            else msg += "You have the latest version.";
/*
                msg += "You currently have version " + userVersion + " of the X-Wing Vassal module.<br>"
                    + "The latest version available for download is " + line + "<br><br>"
                        + "The Module is about to check for additional content and download it.<br>"
            + "Vassal may become unresponsive between a few seconds to a few minutes.<br>"
            + "Do you want to proceed?<br>"
                        + "You can choose to skip for now and perform this step by clicking on Contents Checker later.<br></html>";
*/
            JLabel versionLabel = new JLabel(msg);
            JLabel versionLabel2 = new JLabel("You currently have version " + userVersion + " of the X-Wing Vassal module.");
            JLabel versionLabel3 = new JLabel("The latest version available for download is " + line);
            JLabel checkLabel = new JLabel("The module is about to check for additional content.");
            JLabel checkLabel2 = new JLabel("The Content Checker button will flash red if it finds any.");
            JLabel checkLabel3 = new JLabel("Click on it to download the missing components.");
            SwingLink mainDownloadLink = new SwingLink("X-Wing Vassal download page", vassalDownloadURL);
            SwingLink altDownloadLink = new SwingLink("Alt download page on github", githubDownloadURL);
            SwingLink whatsNewLink = new SwingLink("What's new in v" + line, urlPatchString);
            SwingLink guideLink = new SwingLink("New? Need help? Go to the web guide", guideURL);
            SwingLink supportLink = new SwingLink("Support the X-Wing Vassal module", "http://xwvassal.info/supportmodule.html");
            SwingLink homeLink = new SwingLink("Home for the Vassal League", "http://xwvassal.info");

            JPanel panel = new JPanel();

            panel.setMinimumSize(new Dimension(1000,1600));

            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
            labelPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            labelPanel.setMinimumSize(new Dimension(700,400));
            labelPanel.add(versionLabel);
            labelPanel.add(versionLabel2);
            labelPanel.add(versionLabel3);

            labelPanel.add(Box.createRigidArea(new Dimension(0,8)));
            labelPanel.add(new JSeparator());
            labelPanel.add(Box.createRigidArea(new Dimension(0,8)));

            labelPanel.add(checkLabel);
            labelPanel.add(checkLabel2);
            labelPanel.add(checkLabel3);
            panel.add(labelPanel);

            DataArchive dataArchive = GameModule.getGameModule().getDataArchive();
            JPanel linkPanel = new JPanel();
            linkPanel.setLayout(new BoxLayout(linkPanel, BoxLayout.X_AXIS));

            JPanel homeLinkArea = new JPanel();
            homeLinkArea.setLayout(new BoxLayout(homeLinkArea, BoxLayout.Y_AXIS));
            homeLinkArea.setMinimumSize(new Dimension(275,400));
            homeLinkArea.setAlignmentY(Component.TOP_ALIGNMENT);
            JLabel homeIcon = new JLabel();

            BufferedImage img = null;
            InputStream is = dataArchive.getInputStream("images/Token_2e_charge.png");
            img = ImageIO.read(is);
            is.close();
            homeIcon.setIcon(new ImageIcon(img));

            homeLinkArea.add(homeIcon);
            homeLinkArea.add(homeLink);
            homeLinkArea.add(supportLink);

            JPanel downloadLinkArea = new JPanel();
            downloadLinkArea.setLayout(new BoxLayout(downloadLinkArea, BoxLayout.Y_AXIS));
            downloadLinkArea.setMinimumSize(new Dimension(275,400));
            downloadLinkArea.setAlignmentY(Component.TOP_ALIGNMENT);
            JLabel downloadIcon = new JLabel();

            BufferedImage img2 = null;
            InputStream is2 = dataArchive.getInputStream("images/Token_2e_reinforce_fore.png");
            img2 = ImageIO.read(is2);
            is2.close();
            downloadIcon.setIcon(new ImageIcon(img2));

            downloadLinkArea.add(downloadIcon);
            downloadLinkArea.add(mainDownloadLink);
            downloadLinkArea.add(altDownloadLink);

            JPanel guideLinkArea = new JPanel();
            guideLinkArea.setLayout(new BoxLayout(guideLinkArea, BoxLayout.Y_AXIS));
            guideLinkArea.setMinimumSize(new Dimension(275,400));
            guideLinkArea.setAlignmentY(Component.TOP_ALIGNMENT);
            JLabel guideIcon = new JLabel();

            BufferedImage img3 = null;
            InputStream is3 = dataArchive.getInputStream("images/Token_2e_focus.png");
            img3 = ImageIO.read(is3);
            is3.close();
            guideIcon.setIcon(new ImageIcon(img3));

            guideLinkArea.add(guideIcon);
            guideLinkArea.add(guideLink);
            guideLinkArea.add(whatsNewLink);



            linkPanel.add(homeLinkArea);
            linkPanel.add(Box.createHorizontalStrut(30));
            linkPanel.add(downloadLinkArea);
            linkPanel.add(Box.createHorizontalStrut(30));
            linkPanel.add(guideLinkArea);
            linkPanel.add(Box.createHorizontalStrut(30));

            panel.add(linkPanel);

            JFrame frame = new JFrame();
            frame.setAlwaysOnTop(true);

            int answer = JOptionPane.showOptionDialog(frame, panel, "Welcome to the X-Wing vassal module",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new String[] { "OK" }, "OK");

            frame.requestFocus();

            frame.setAlwaysOnTop(false);
            frame.dispose();

            if(answer==0) {
                downloadXwingDataAndDispatcherJSONFiles();
            }else{
                downloadXwingDataAndDispatcherJSONFiles();
            }

            /*
            JOptionPane optionPane = new JOptionPane();
                optionPane.setMessage(msg);
                optionPane.setOptionType(JOptionPane.YES_NO_OPTION);
                //optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                optionPane.add(panel);
                JDialog dialog = optionPane.createDialog(frame, "Welcome to the X-Wing vassal module");
                dialog.setPreferredSize(new Dimension(1200,800));


            frame.setVisible(true);
                frame.toFront();
                frame.repaint();
*/

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
