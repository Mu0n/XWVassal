package mic.ota;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.FileArchive;
import com.google.common.collect.ImmutableMap;
import mic.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static mic.Util.logToChat;

public class OTAContentsChecker extends AbstractConfigurable {
    MasterGameModeRouter mgmr = new MasterGameModeRouter();
    static final int NBFLASHES = 60000;
    static final int DELAYBETWEENFLASHES = 700;

    // change this variable to a branch name to test, or master for deployment
    private static final String OTA_RAW_GITHUB_BRANCH = "master";
    // private static final String OTA_RAW_GITHUB_BRANCH = "dual-based-ships";

    private static final String OTA_RAW_GITHUB_ROOT = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/";
    private static final String OTA_RAW_GITHUB_ROOT_2E = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/";

    public static final String OTA_RAW_BRANCH_URL = OTA_RAW_GITHUB_ROOT + OTA_RAW_GITHUB_BRANCH + "/";
    public static final String OTA_RAW_BRANCH_URL_2E = OTA_RAW_GITHUB_ROOT_2E + OTA_RAW_GITHUB_BRANCH + "/";

    private static final String OTA_RAW_GITHUB_JSON_URL = OTA_RAW_BRANCH_URL + "json/";
    private static final String OTA_RAW_GITHUB_JSON_URL_2E = OTA_RAW_BRANCH_URL_2E + "json/";

    public static final String OTA_UPGRADES_JSON_URL = OTA_RAW_GITHUB_JSON_URL + "upgrade_images.json";
    public static final String OTA_SHIPS_JSON_URL = OTA_RAW_GITHUB_JSON_URL + "ship_images.json";
    public static final String OTA_PILOTS_JSON_URL = OTA_RAW_GITHUB_JSON_URL + "pilot_images.json";
    public static final String OTA_DIALHIDES_JSON_URL =  OTA_RAW_GITHUB_JSON_URL + "dial_images.json";
    public static final String OTA_CONDITIONS_JSON_URL =  OTA_RAW_GITHUB_JSON_URL + "condition_images.json";
    public static final String OTA_ACTIONS_JSON_URL =  OTA_RAW_GITHUB_JSON_URL + "action_images.json";

    public static final String OTA_UPGRADES_JSON_URL_2E = OTA_RAW_GITHUB_JSON_URL_2E + "upgrade_images.json";
    public static final String OTA_SHIPS_JSON_URL_2E = OTA_RAW_GITHUB_JSON_URL_2E + "ship_images.json";
    public static final String OTA_PILOTS_JSON_URL_2E = OTA_RAW_GITHUB_JSON_URL_2E + "pilot_images.json";
    public static final String OTA_CONDITIONS_JSON_URL_2E =  OTA_RAW_GITHUB_JSON_URL_2E + "condition_images.json";


    public static final String OTA_DISPATCHER_UPGRADES_JSON_URL = OTA_RAW_GITHUB_JSON_URL + "dispatcher_upgrades.json";
    public static final String OTA_DISPATCHER_PILOTS_JSON_URL = OTA_RAW_GITHUB_JSON_URL + "dispatcher_pilots.json";
    public static final String OTA_DISPATCHER_SHIPS_JSON_URL = OTA_RAW_GITHUB_JSON_URL + "dispatcher_ships.json";
    public static final String OTA_DISPATCHER_CONDITIONS_JSON_URL = OTA_RAW_GITHUB_JSON_URL + "dispatcher_conditions.json";


    public static final String OTA_DISPATCHER_UPGRADES_JSON_URL_2E = OTA_RAW_GITHUB_JSON_URL_2E + "dispatcher_upgrades.json";
    public static final String OTA_DISPATCHER_PILOTS_JSON_URL_2E = OTA_RAW_GITHUB_JSON_URL_2E + "dispatcher_pilots.json";
    public static final String OTA_DISPATCHER_SHIPS_JSON_URL_2E = OTA_RAW_GITHUB_JSON_URL_2E + "dispatcher_ships.json";
    public static final String OTA_DISPATCHER_CONDITIONS_JSON_URL_2E = OTA_RAW_GITHUB_JSON_URL_2E + "dispatcher_conditions.json";

    public static String manifest2eURL = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/data/manifest.json";
    public static String ota2BuildURL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/master/version";


    String aComboBoxChoice = "Base Game";
    String chosenURL = OTA_RAW_BRANCH_URL;

    private static Map<String,String> fullFactionNames = ImmutableMap.<String, String>builder()
            .put("galacticempire","Galactic Empire")
            .put("firstorder","First Order")
            .put("rebelalliance","Rebel Alliance")
            .put("resistance","Resistance")
            .put("scumandvillainy","Scum and Villainy")
            .put("galacticrepublic","Galactic Republic")
            .put("separatistarmy","Separatist Army")
            .build();

    private boolean debugMode = false;


    private JButton contentCheckerButton = new JButton();
    private ModuleIntegrityChecker modIntChecker = null;
    private ModuleIntegrityChecker_2e modIntChecker_2e = null;
    private OTAContentsCheckerResults results = null;
    private OTAContentsCheckerResults results2e = null;
    private final String[] finalColumnNames = {"Type","Name", "Variant"};
    private JTable finalTable;
    private JButton downloadButton;
    private JButton downloadButton2e;
    private JFrame frame;
    private JLabel jlabel;
    private boolean downloadAll = false;
    JTabbedPane myTabbedPane = new JTabbedPane();
    int missing1stEdContent = 0;
    int missing2ndEdContent = 0;
    boolean wantToBeNotified1st = false;

    boolean thinkingWeHaveNoNetAccess = false; //once it has trouble connecting, don't bother with more connections

    private boolean tictoc = false;
    private boolean tictoc1st = false;
    private boolean tictoc2nd = false;
    Color backupColor = Color.WHITE;
    Boolean killItIfYouHaveTo = false; //kills the blinking Contents Checker button, after an update
    Boolean stopBlink1stTab = false;
    Boolean stopBlink2ndTab = false;
    Boolean killItIfYouHaveTo1stTab = false; //kills the blinking "First Edition tab", after an update
    Boolean killItIfYouHaveTo2ndTab = false; //kills the blinking "Second Edition tab" after an update
    public static final String modeListURL = "https://raw.githubusercontent.com/Mu0n/XWVassal-website/master/modeList.json";

    //for 2nd edition, keep a global variable
    static List<XWS2Pilots> allShips;
    static XWS2Upgrades allUpgrades;
    static List<XWS2Upgrades.Condition> allConditions;

    public void activateBlinky(final Color whichColor)
    {
        contentCheckerButton.setForeground(Color.WHITE);
        contentCheckerButton.setBackground(whichColor);

            final java.util.Timer timer = new Timer();
            final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");
            this.tictoc = false;
            final AtomicInteger count = new AtomicInteger(0);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try{
                        if(killItIfYouHaveTo || killItIfYouHaveTo2ndTab){
                            timer.cancel();
                            contentCheckerButton.setBackground(backupColor);
                            contentCheckerButton.setForeground(Color.BLACK);
                            return;
                        }
                        if(count.getAndIncrement() >= NBFLASHES * 2) {
                            timer.cancel();
                            contentCheckerButton.setBackground(backupColor);
                            contentCheckerButton.setForeground(Color.BLACK);
                            return;
                        }
                        if(tictoc==true) {
                            contentCheckerButton.setBackground(whichColor);
                            contentCheckerButton.setForeground(Color.WHITE);
                            tictoc=false;
                        }
                        else{
                            tictoc=true;
                            contentCheckerButton.setBackground(backupColor);
                            contentCheckerButton.setForeground(Color.BLACK);
                        }
                    } catch (Exception e) {
                    }
                }
            }, 0,DELAYBETWEENFLASHES);
    }
    public void activateBlinky2ndTab()
    {
        myTabbedPane.setForegroundAt(0,Color.WHITE);
        myTabbedPane.setBackgroundAt(0,Color.BLACK);

        final java.util.Timer timer = new Timer();
        final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");
        this.tictoc2nd = false;
        final AtomicInteger count = new AtomicInteger(0);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    if(stopBlink2ndTab){
                        timer.cancel();
                        myTabbedPane.setBackgroundAt(0, backupColor);
                        myTabbedPane.setForegroundAt(0,Color.BLACK);
                        return;
                    }
                    if(count.getAndIncrement() >= NBFLASHES * 2) {
                        timer.cancel();
                        myTabbedPane.setBackgroundAt(0,backupColor);
                        myTabbedPane.setForegroundAt(0,Color.BLACK);
                        return;
                    }
                    if(tictoc2nd==true) {
                        myTabbedPane.setBackgroundAt(0,Color.BLACK);
                        myTabbedPane.setForegroundAt(0,Color.WHITE);
                        tictoc1st=false;
                    }
                    else{
                        tictoc2nd=true;
                        myTabbedPane.setBackgroundAt(0,backupColor);
                        myTabbedPane.setForegroundAt(0,Color.BLACK);
                    }
                } catch (Exception e) {
                }
            }
        }, 0,DELAYBETWEENFLASHES);
    }
    public void activateBlinky1stTab()
    {
        myTabbedPane.setForegroundAt(1,Color.WHITE);
        myTabbedPane.setBackgroundAt(1,Color.RED);

        final java.util.Timer timer = new Timer();
        final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");
        this.tictoc1st = false;
        final AtomicInteger count = new AtomicInteger(0);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    if(stopBlink1stTab){
                        timer.cancel();
                        myTabbedPane.setBackgroundAt(1, backupColor);
                        myTabbedPane.setForegroundAt(1,Color.BLACK);
                        return;
                    }
                    if(count.getAndIncrement() >= NBFLASHES * 2) {
                        timer.cancel();
                        myTabbedPane.setBackgroundAt(1,backupColor);
                        myTabbedPane.setForegroundAt(1,Color.BLACK);
                        return;
                    }
                    if(tictoc1st==true) {
                        myTabbedPane.setBackgroundAt(1,Color.RED);
                        myTabbedPane.setForegroundAt(1,Color.WHITE);
                        tictoc1st=false;
                    }
                    else{
                        tictoc1st=true;
                        myTabbedPane.setBackgroundAt(1,backupColor);
                        myTabbedPane.setForegroundAt(1,Color.BLACK);
                    }
                } catch (Exception e) {
                }
            }
        }, 0,DELAYBETWEENFLASHES);
    }
    public void addTo(Buildable parent) {


        JButton b = new JButton("Content Checker");
        b.setAlignmentY(0.0F);
        backupColor = b.getBackground();

        b.setOpaque(true);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ContentsCheckerWindow();
            }
        });
        contentCheckerButton = b;

        wantToBeNotified1st = false;
        if (!XWOTAUtils.fileExistsInModule("want1stednotifs.txt")) {
            String choice = "no";
            try {
                XWOTAUtils.addFileToModule("want1stednotifs.txt", choice.getBytes());
            } catch (Exception e) {
            }
            wantToBeNotified1st = false;
        } else {
            // read contents of want1stednotifs.txt
            String wantNotifStr = null;
            try {

                InputStream inputStream = GameModule.getGameModule().getDataArchive().getInputStream("want1stednotifs.txt");
                if (inputStream == null) {
                    logToChat("couldn't load /want1stednotifs.txt");
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
                    wantToBeNotified1st = true;
                }
                else wantToBeNotified1st = false;
                inputStream.close();
            } catch (Exception e) {
                System.out.println("Unhandled error reading want1stednotifs.txt: \n" + e.toString());
                logToChat("Unhandled error reading want1stednotifs.txt: \n" + e.toString());
            }
        }

        //The zipping process should happen only once when all the local files to be replaced are known. On slow hard disk, this could be unfortunate if it was done several times.
        checkPendingCheck(); //locally
        checkAndUpdateRemoteJsonsIfNewFound(); //remotely, will influence thinkingWeHaveNoNetAccess if the timeout is reached
        checkPendingOTA(); //locally
        if(thinkingWeHaveNoNetAccess == false) compareOTAversions(); //remotely only if the first net access worked, don't bother otherwise.

            //if the pending file doesn't exist, give it a chance to find a new manifest that requires a content check and create that pendingContentCheck.txt again
        //this file should be destroyed when a content check download or downloadAll is done

        allShips = XWS2Pilots.loadFromLocal();
        allUpgrades = XWS2Upgrades.loadFromLocal();
        allConditions = XWS2Upgrades.loadConditionsFromLocal();

        if (wantToBeNotified1st) missing1stEdContent = justFind1MissingContent();

        XWOTAUtils.checkOnlineOrder66();

        //TODO replace the whole content checker by veryfiying a change to Guido's manifest version vs my own local file
        //
        // if(XWOTAUtils.amIDoingOrder66() == false) missing2ndEdContent = justFind1MissingContent_2e();
        //
        if (missing1stEdContent > 0) {
            activateBlinky(Color.RED);
        }
        if (missing2ndEdContent > 0) {
            activateBlinky(Color.BLACK);
        }
        GameModule.getGameModule().getToolBar().add(b);
    }



    private void checkPendingOTA() {
        if(!XWOTAUtils.fileExistsInModule("pendingOTACheck.txt")){
            String choice = "no";
            try {
                XWOTAUtils.addFileToModule("pendingOTACheck.txt", choice.getBytes());
                if(debugMode) logToChat("otacheck line 337 adding pending OTA check");
            } catch (Exception e) {
            }
        } else{
            String pendingContentStr = null;
            try {

                InputStream inputStream = GameModule.getGameModule().getDataArchive().getInputStream("pendingOTACheck.txt");
                if (inputStream == null) {
                    logToChat("couldn't load /pendingOTACheck.txt");
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder contents = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    contents.append(line);
                }
                reader.close();

                pendingContentStr = contents.toString();
                if (pendingContentStr.equalsIgnoreCase("yes")) {
                    missing2ndEdContent = 1;
                    if(debugMode) logToChat("otacheck line 359 pending OTA detected as yes");
                }
                else {
                    if(debugMode) logToChat("otacheck line 362 pending OTA detected as no");
                }
                inputStream.close();
            } catch (Exception e) {
                System.out.println("Unhandled error reading pendingOTACheck.txt: \n" + e.toString());
                logToChat("Unhandled error reading pendingOTACheck.txt: \n" + e.toString());
            }
        }


    }

    private void checkPendingCheck(){
        //deal with hanging content checker update request if it wasn't dealt with before
        if (!XWOTAUtils.fileExistsInModule("pendingContentCheck.txt")) {
            String choice = "no";
            try {
                XWOTAUtils.addFileToModule("pendingContentCheck.txt", choice.getBytes());
                if(debugMode) logToChat("otacheck line 380 xwd2 check added to module");
            } catch (Exception e) {
            }
        } else {
            String pendingContentStr = null;
            try {

                InputStream inputStream = GameModule.getGameModule().getDataArchive().getInputStream("pendingContentCheck.txt");
                if (inputStream == null) {
                    logToChat("couldn't load /pendingContentCheck.txt");
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder contents = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    contents.append(line);
                }
                reader.close();

                pendingContentStr = contents.toString();
                if (pendingContentStr.equalsIgnoreCase("yes")) {
                    missing2ndEdContent = 1;
                    if(debugMode) logToChat("otacheck line 380 xwd2 pending check as yes");
                } else {
                    if(debugMode) logToChat("otacheck line 382 xwd2 pending check as no");
                }
                inputStream.close();
            } catch (Exception e) {
                System.out.println("Unhandled error reading pendingContentCheck.txt: \n" + e.toString());
                logToChat("Unhandled error reading pendingContentCheck.txt: \n" + e.toString());
            }
        }
    }

    private void removePendingCheck(){
        //unlikely to happen, but if somehow the file isn't present, make one and mark it as no pending check, since this method is called after a content checker download op.
        if (!XWOTAUtils.fileExistsInModule("pendingContentCheck.txt")) {
            String choice = "no";
            try {
                XWOTAUtils.addFileToModule("pendingContentCheck.txt", choice.getBytes());
                if(debugMode) logToChat("otacheck line 421xwd2 pending check as no after a download AND had to remake the file");
            } catch (Exception e) {
            }
        } else {
            String choice = "no";
            try {
                XWOTAUtils.addFileToModule("pendingContentCheck.txt", choice.getBytes());
                if(debugMode) logToChat("otacheck line 426 xwd2 pending check as no after a download");
            } catch (Exception e) {
            }
        }
    }

    private void removePendingOTA2Check(){
        //unlikely to happen, but if somehow the file isn't present, make one and mark it as no pending check, since this method is called after a content checker download op.
        if (!XWOTAUtils.fileExistsInModule("pendingOTACheck.txt")) {
            String choice = "no";
            try {
                XWOTAUtils.addFileToModule("pendingOTACheck.txt", choice.getBytes());
                if(debugMode) logToChat("otacheck line 421xwd2 pending check as no after a download AND had to remake the file");
            } catch (Exception e) {
            }
        } else {
            String choice = "no";
            try {
                XWOTAUtils.addFileToModule("pendingOTACheck.txt", choice.getBytes());
                if(debugMode) logToChat("otacheck line 426 xwd2 pending check as no after a download");
            } catch (Exception e) {
            }
        }
    }

    private void checkAndUpdateRemoteJsonsIfNewFound()  {
        if(XWOTAUtils.fileExitsOnTheNet(manifest2eURL))
        {
            XWS2Pilots.tripleVersion remoteVer = XWS2Pilots.checkRemoteManifestVersion();
            XWS2Pilots.tripleVersion localVer = XWS2Pilots.checkLocalManifestVersion();
            localVer.displayInChat("local");
            remoteVer.displayInChat("remote");


            if(remoteVer != null && localVer != null)
            {
                if(remoteVer.isNewerThan(localVer)) {
                    if((remoteVer.getMinor() > localVer.getMinor()) || (remoteVer.getMajor() > localVer.getMajor())) {
                        //Scenario B: the new online stuff requires a content checker flash! and the local files will be replaced
                        missing2ndEdContent=1;
                        String msg = "yes";
                        try {
                            XWOTAUtils.addFileToModule("pendingContentCheck.txt", msg.getBytes());
                        } catch (Exception e) {
                        }
                        downloadXwingDataAndDispatcherJSONFiles_2e();
                        logToChat("The local xwing-data2 is being updated");
                        XWS2Pilots.tripleVersion localVer2 = XWS2Pilots.checkLocalManifestVersion();
                        logToChat("new local version--");
                        localVer2.displayInChat("local");

                    }
                    else if(remoteVer.getPatch() > localVer.getPatch()){
                        //Scenario C: no content checker flash, but the local files will be replaced.
                        downloadXwingDataAndDispatcherJSONFiles_2e();
                        logToChat("The local xwing-data2 is being updated");

                    }
                    else{
                        logToChat("The local xwing-data2 is up to date.");
                    }
                }
            }
        }
        else {
            //SCENARIO A: can't connect to guido's xwing-data2 file online, will resort to purely local files for data.
            logToChat("Error: can't connect online to verify the module's integrity with xwing-data2 - will attempt to use local files instead.");
            thinkingWeHaveNoNetAccess = true;
            return;
        }
    }
    private void compareOTAversions() {

        if(XWOTAUtils.fileExitsOnTheNet(ota2BuildVersion.remoteUrl))
        {
            int remoteVer = ota2BuildVersion.checkRemoteBuildVersion();
            int localVer = ota2BuildVersion.checkLocalBuildVersion();
            logToChat("Over-the-air local version: " + localVer + " remote version: " + remoteVer);
            if(remoteVer != -1){
                if(localVer == 0 || localVer < remoteVer){ //needs a rebuild of ota2.zip
                    missing2ndEdContent=1;
                    String msg = "yes";
                    try{
                        String pathToUse = XWOTAUtils.getModulePath();
                        //if ota2.zip is here, delete it first, we know we want a newer one, might want to not do this in case there's
                        //no net connection though
                        if(ota2BuildVersion.checkExistenceOfLocalOTA2Zip() == true){
                            File file = new File(pathToUse + File.separator + ota2BuildVersion.ota2Zip);
                            file.delete();
                        }
                        //the try-catch problem triggers at this line, an IOException Error
                        FileOutputStream fos = new FileOutputStream(pathToUse + File.separator + ota2BuildVersion.ota2Zip);
                        ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(fos));

                        String content = "{\"version\":\"" + remoteVer + "\"}";

                        String fileName = "version";
                        ZipEntry zipEntry = new ZipEntry(fileName);
                        zipEntry.setSize(content.getBytes().length);
                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(content.getBytes());
                        zipOut.closeEntry();
                        zipOut.close();
                        fos.close();
                    } catch(Exception e)
                    {
                        logToChat("Error: can't create a local journal of the over-the-air content depot.");
                    }
                } else{ //do nothing
                    logToChat("The local over-the-air journal is up to date.");
                }
            }
            else{
                logToChat("Error: can't connect to the online over-the-air content depot.");
            }
        }
        /*
        {
            XWS2Pilots.tripleVersion remoteVer = XWS2Pilots.checkRemoteManifestVersion();
            XWS2Pilots.tripleVersion localVer = XWS2Pilots.checkLocalManifestVersion();
            localVer.displayInChat("local");
            remoteVer.displayInChat("remote");


            if(remoteVer != null && localVer != null)
            {
                if(remoteVer.isNewerThan(localVer)) {
                    if((remoteVer.getMinor() > localVer.getMinor()) || (remoteVer.getMajor() > localVer.getMajor())) {
                        //Scenario B: the new online stuff requires a content checker flash! and the local files will be replaced
                        missing2ndEdContent=1;
                        String msg = "yes";
                        try {
                            XWOTAUtils.addFileToModule("pendingContentCheck.txt", msg.getBytes());
                        } catch (Exception e) {
                        }
                        downloadXwingDataAndDispatcherJSONFiles_2e();
                        logToChat("The local xwing-data2 is being updated");
                        XWS2Pilots.tripleVersion localVer2 = XWS2Pilots.checkLocalManifestVersion();
                        logToChat("new local version--");
                        localVer2.displayInChat("local");

                    }
                    else if(remoteVer.getPatch() > localVer.getPatch()){
                        //Scenario C: no content checker flash, but the local files will be replaced.
                        downloadXwingDataAndDispatcherJSONFiles_2e();
                        logToChat("The local xwing-data2 is being updated");

                    }
                    else{
                        logToChat("The local xwing-data2 is up to date.");
                    }
                }
            }
        }
        else {
            //SCENARIO A: can't connect to guido's xwing-data2 file online, will resort to purely local files for data.
            logToChat("Error: can't connect online to verify the module's integrity with xwing-data2 - will attempt to use local files instead.");
            thinkingWeHaveNoNetAccess = true;
            return;

         */
    }


    private static XWS2Pilots.pilotsDataSources parseTheManifestForShipPilots(){
            return mic.Util.loadRemoteJson(XWS2Pilots.remoteUrl, XWS2Pilots.pilotsDataSources.class);
    }
    private static XWS2Upgrades.upgradesDataSources parseTheManifestForUpgrades(){
        return mic.Util.loadRemoteJson(XWS2Upgrades.remoteUrl, XWS2Upgrades.upgradesDataSources.class);
    }
    private static XWS2Upgrades.conditionsDataSources parseTheManifestForConditions(){
        return mic.Util.loadRemoteJson(XWS2Upgrades.remoteUrl, XWS2Upgrades.conditionsDataSources.class);
    }

    public static boolean downloadXwingDataAndDispatcherJSONFiles_2e() {
        boolean errorOccurredOnXWingData = false;
        //these have to be dumped in a /data subfolder, it will help prevent shipPilot json collisions, such as tielfighter
        //by putting them in /data/pilots/rebelalliance/
        ArrayList<String> jsonFilesToDownloadFromURL_2e = new ArrayList<String>();

        //logToChat("OTAContentChecker line 472 STEP adds the manifest from the remote URL to the list of files to zip up");
        jsonFilesToDownloadFromURL_2e.add(XWS2Pilots.remoteUrl);

        for(XWS2Pilots.OneFactionGroup oSDS : parseTheManifestForShipPilots().getPilots()){
            for(String suffix : oSDS.getShipUrlSuffixes()){
                jsonFilesToDownloadFromURL_2e.add(XWS2Pilots.guidoRootUrl + suffix);
            }
        }
        for(String suffix : parseTheManifestForUpgrades().getUrlEnds()){
            jsonFilesToDownloadFromURL_2e.add(XWS2Upgrades.guidoRootUrl + suffix);
        }

        jsonFilesToDownloadFromURL_2e.add(XWS2Upgrades.guidoRootUrl + parseTheManifestForConditions().getUrlEnd());

        try {
            XWOTAUtils.downloadJSONFilesFromGitHub(jsonFilesToDownloadFromURL_2e, true);
        }catch(IOException e)
        {
            logToChat("error download and integrating the jsons");
            errorOccurredOnXWingData = true;
        }
        return errorOccurredOnXWingData;
    }
    private static boolean downloadXwingDataAndDispatcherJSONFiles()
    {
        boolean errorOccurredOnXWingData = false;
        //these will be dumped in the root of the module
        ArrayList<String> jsonFilesToDownloadFromURL = new ArrayList<String>();

        jsonFilesToDownloadFromURL.add(MasterShipData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(OTAContentsChecker.OTA_DISPATCHER_SHIPS_JSON_URL);
        jsonFilesToDownloadFromURL.add(MasterPilotData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(OTAContentsChecker.OTA_PILOTS_JSON_URL);
        jsonFilesToDownloadFromURL.add(MasterUpgradeData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(OTAContentsChecker.OTA_DISPATCHER_UPGRADES_JSON_URL);
        jsonFilesToDownloadFromURL.add(MasterConditionData.REMOTE_URL);
        jsonFilesToDownloadFromURL.add(OTAContentsChecker.OTA_DISPATCHER_CONDITIONS_JSON_URL);

        try {
            XWOTAUtils.downloadJSONFilesFromGitHub(jsonFilesToDownloadFromURL, false);
        }catch(IOException e)
        {
            logToChat("error download and integrating the jsons");
            errorOccurredOnXWingData = true;
        }
        return errorOccurredOnXWingData;
    }

    public static int justFind1MissingContent()
    {

        // grab xwing-data: pilots, ships, upgrades, conditions
        // dispatcher: pilots, ships, upgrades, conditions
        // and save to the module
        boolean errorOccurredOnXWingData = downloadXwingDataAndDispatcherJSONFiles();

        if(errorOccurredOnXWingData)
        {
            mic.Util.logToChat("Unable to reach xwing-data1 server (1st edition). No update performed");
        }else {
            ModuleIntegrityChecker modIntCheck = new ModuleIntegrityChecker();

            // =============================================================
            // Pilots
            // =============================================================
            ArrayList<OTAMasterPilots.OTAPilot> pilots = modIntCheck.checkPilots(true);
            for (OTAMasterPilots.OTAPilot pilot : pilots) {
                if (!pilot.getStatus() && pilot.getStatusOTA()) {
                    return 1;
                }
            }
            pilots = null;
            // =============================================================
            // Actions
            // =============================================================
            ArrayList<OTAMasterActions.OTAAction> actions = modIntCheck.checkActions(true);
            for (OTAMasterActions.OTAAction action : actions) {
                if (!action.getStatus() && action.getStatusOTA()) {
                    return 1;
                }
            }
            actions = null;


            // =============================================================
            // Ships
            // =============================================================
            ArrayList<OTAMasterShips.OTAShip> ships = modIntCheck.checkShips(true);
            for (OTAMasterShips.OTAShip ship : ships) {
                if (!ship.getStatus() && ship.getStatusOTA()) {
                    return 1;
                }
            }
            ships = null;

            // =============================================================
            // Upgrades
            // =============================================================
            ArrayList<OTAMasterUpgrades.OTAUpgrade> upgrades = modIntCheck.checkUpgrades(true);
            for (OTAMasterUpgrades.OTAUpgrade upgrade : upgrades) {
                if (!upgrade.getStatus() && upgrade.getStatusOTA()) {
                    return 1;
                }
            }
            upgrades = null;

            // =============================================================
            // Conditions
            // =============================================================
            ArrayList<OTAMasterConditions.OTACondition> conditions = modIntCheck.checkConditions(true);
            for (OTAMasterConditions.OTACondition condition : conditions) {
                if (!condition.getStatus() && condition.getStatusOTA()) {
                    return 1;
                }

                if (!condition.getTokenStatus() && condition.getTokenStatusOTA()) {
                    return 1;
                }
            }
            conditions = null;

            // =============================================================
            // Dial Hides
            // =============================================================
            ArrayList<OTAMasterDialHides.OTADialHide> dialHides = modIntCheck.checkDialHides(true);
            for (OTAMasterDialHides.OTADialHide dialHide : dialHides) {
                if (!dialHide.getStatus() && dialHide.getStatusOTA()) {
                    return 1;
                }
            }
            dialHides = null;

            // =============================================================
            // Check Dial Masks
            // =============================================================
            ArrayList<OTADialMask> dialMasksToGenerate = new ArrayList<OTADialMask>();
            ArrayList<OTADialMask> dialMaskResults = modIntCheck.checkDialMasks(true);
            Iterator<OTADialMask> dialMaskIterator = dialMaskResults.iterator();
            OTADialMask missingDialMask = null;
            while (dialMaskIterator.hasNext()) {
                missingDialMask = dialMaskIterator.next();
                if (!missingDialMask.getStatus() && missingDialMask.getStatusOTA()) {
                    return 1;
                }
            }

            // =============================================================
            // Check Ship Bases
            // =============================================================
            ArrayList<OTAShipBase> shipBaseResults = modIntCheck.checkShipBases(true);
            Iterator<OTAShipBase> shipBaseIterator = shipBaseResults.iterator();
            OTAShipBase missingShipBase = null;
            while (shipBaseIterator.hasNext()) {
                missingShipBase = shipBaseIterator.next();
                if (!missingShipBase.getStatus() && missingShipBase.getStatusOTA()) {
                    return 1;
                }
            }
        }
        return 0;
    }

    /*
     * Build the contents checker window
     */

    private synchronized void ContentsCheckerWindow2ndEdTab(JPanel hostPanel) {
        results2e = checkAllResults2e();
        if(results2e.getTotalWork() == 0) {
            removePendingOTA2Check();
            removePendingCheck();
            killItIfYouHaveTo2ndTab = true;
        }
        finalTable = buildFinalTable2e(results2e);
        hostPanel.setLayout(new BoxLayout(hostPanel, BoxLayout.Y_AXIS));
        // create the label
        jlabel = new JLabel();

        // add the results table
        JScrollPane finalPane = new JScrollPane(finalTable);
        // ALL checkbox
        final JCheckBox allButton = new JCheckBox("Download all content");
        allButton.setSelected(false);
        allButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.DESELECTED)
                {
                    downloadAll = false;
                    refreshFinalTable2e();
                }else if(evt.getStateChange() == ItemEvent.SELECTED)
                {
                    downloadAll = true;
                    refreshFinalTable2e();
                }
            }
        });

        // download button
        downloadButton2e = new JButton("Download");
        downloadButton2e.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadButton2e.setEnabled(false);

                int answer =  JOptionPane.showConfirmDialog(null, "This might take several minutes. During the download, Vassal will be unresponsive. \nDo you want to continue?", "Do you want to proceed?", JOptionPane.YES_NO_OPTION);

                if(answer == JOptionPane.YES_OPTION)
                {
                    downloadAll2e(OTA_RAW_BRANCH_URL_2E);
                    allButton.setSelected(false);
                    killItIfYouHaveTo2ndTab = true; //gets rid of the blinky
                    stopBlink2ndTab = true; //gets rid of the tab blinky
                    refreshFinalTable2e();
                    removePendingCheck();
                    removePendingOTA2Check();
                }else{
                    downloadButton2e.setEnabled(true);
                    removePendingCheck();
                    removePendingOTA2Check();
                }
            }
        });

        // cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                frame.dispose();
            }
        });
        JPanel buttonSubPanel = new JPanel();
        buttonSubPanel.add(downloadButton2e);
        buttonSubPanel.add(allButton);
        buttonSubPanel.add(cancelButton);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        hostPanel.add(topPanel);
        hostPanel.add(jlabel);
        hostPanel.add(buttonSubPanel);
        hostPanel.add(finalPane);

        hostPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        //frame.add(panel, BorderLayout.PAGE_START);

        jlabel.setText("<html><body><br>Click the download button to download the following images</body></html>");
        if(finalTable.getModel().getRowCount() == 0)
        {
            jlabel.setText("All content is up to date");
            downloadButton2e.setEnabled(false);
            removePendingCheck();
            removePendingOTA2Check();
        }else{

            downloadButton2e.setEnabled(true);
        }
    }
    private synchronized void ContentsCheckerWindow1stEdTab(JPanel hostPanel)
    {
        results = checkAllResults();
        finalTable = buildFinalTable(results);

        hostPanel.setLayout(new BoxLayout(hostPanel, BoxLayout.Y_AXIS));

        // create the label
        jlabel = new JLabel();

        // add the results table
        JScrollPane finalPane = new JScrollPane(finalTable);

        // ALL checkbox
        final JCheckBox allButton = new JCheckBox("Download all content");
        allButton.setSelected(false);
        allButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.DESELECTED)
                {
                    downloadAll = false;
                    refreshFinalTable();
                }else if(evt.getStateChange() == ItemEvent.SELECTED)
                {
                    downloadAll = true;
                    refreshFinalTable();
                }
            }
        });

        // download button
        downloadButton = new JButton("Download");
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadButton.setEnabled(false);

                int answer =  JOptionPane.showConfirmDialog(null, "This might take several minutes. During the download, Vassal will be unresponsive. \nDo you want to continue?", "Do you want to proceed?", JOptionPane.YES_NO_OPTION);

                if(answer == JOptionPane.YES_OPTION)
                {
                    if("Base Game".equals(aComboBoxChoice)) downloadAll(OTA_RAW_BRANCH_URL);
                    else {
                        chosenURL = mgmr.getGameMode(aComboBoxChoice).getBaseDataURL();
                        downloadAll(chosenURL);
                    }
                    allButton.setSelected(false);
                    killItIfYouHaveTo = true; //gets rid of the blinky
                    stopBlink1stTab = true; //gets rid of the tab blinky
                    refreshFinalTable();
                }else{
                    downloadButton.setEnabled(true);
                }
            }
        });

        // cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                frame.dispose();
            }
        });

        final JComboBox aComboBox = new JComboBox();
        mgmr.loadData();
        if(mgmr!=null)
        {
            for(MasterGameModeRouter.GameMode o : mgmr.getGameModes()){
                aComboBox.addItem(o.getName());
            }
        }
        else
        //if it can't access the list of sources on the web, make it base game by default
        {
            aComboBox.addItem("Base Game");
        }
        final JLabel sourceTextDescription = new JLabel("<html><body width=400><b>Description for <i>"
                + aComboBox.getSelectedItem().toString() + "</i></b>: "
                + mgmr.getGameMode(aComboBox.getSelectedItem().toString()).getDescription()
                + "</body></html>");
        //sourceTextDescription.setAlignmentX(JLabel.RIGHT_ALIGNMENT);




        JLabel selectExplanationLabel = new JLabel("<html><body><br><br>Select the game mode here (only the base game can acquire new content for now):</body></html>");
        aComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                sourceTextDescription.setText("<html><body width=400><b>Description for <i>"
                        + aComboBox.getSelectedItem().toString() + "</i></b>: "
                        + mgmr.getGameMode(aComboBox.getSelectedItem().toString()).getDescription()
                        + "</body></html>");
            }
        });

        JPanel buttonSubPanel = new JPanel();
        buttonSubPanel.add(downloadButton);
        buttonSubPanel.add(allButton);
        buttonSubPanel.add(cancelButton);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(selectExplanationLabel);

        hostPanel.add(topPanel);
        hostPanel.add(aComboBox);
        hostPanel.add(sourceTextDescription);
        hostPanel.add(jlabel);
        hostPanel.add(buttonSubPanel);
        hostPanel.add(finalPane);

        hostPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        //frame.add(panel, BorderLayout.PAGE_START);

        jlabel.setText("<html><body><br>Click the download button to download the following images</body></html>");
        if(finalTable.getModel().getRowCount() == 0)
        {
            jlabel.setText("All content is up to date");
            downloadButton.setEnabled(false);
        }else{

            downloadButton.setEnabled(true);
        }
    }
    private synchronized void ContentsCheckerWindow()
    {
        // =============================================================
        // Common to both editions
        // =============================================================
        frame = new JFrame();
        frame.setResizable(true);

        // =============================================================
        // 1st Edition
        // =============================================================

        final JPanel panel = new JPanel();
        final JButton activate1stTab = new JButton("Check for missing content in 1st edition (Warning: may take minutes!)");
        activate1stTab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activate1stTab.setEnabled(false);
                ContentsCheckerWindow1stEdTab(panel);
            }
        });

        JLabel sourceExplanationLabel = new JLabel("<html><body>The Content Checker allows you to download card images, ship dials, ship bases as new content<br>" +
                "is previewed or released in the game. Please download this new content to ensure maximum compatibility with other players.<br><br>"+
                "By default, the first edition content is absent from the module. Click on the button below to get it back. You only need to do this operation once<br>(per x-wing vassal module version)</body></html>");

        JCheckBox prefChecbox1st = new JCheckBox("Checking this box: Notifications turned on for missing 1st edition content. Unchecking: Notifications turned off.");
        prefChecbox1st.setSelected(wantToBeNotified1st);
        prefChecbox1st.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String wantNotifStr="no";
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    wantNotifStr = "yes";
                }
                try {
                    XWOTAUtils.addFileToModule("want1stednotifs.txt", wantNotifStr.getBytes());

                } catch (Exception exc) {
                    System.out.println("Unhandled error writing want1stednotifs.txt: \n" + e.toString());
                    logToChat("Unhandled error writing want1stednotifs.txt: \n" + e.toString());
                }
            }
        });

        panel.add(sourceExplanationLabel);
        panel.add(prefChecbox1st);
        panel.add(activate1stTab);

        // =============================================================
        // 2nd Edition
        // =============================================================
        final JPanel secondEditionMainPanel = new JPanel();
        final JLabel sourceExplanationLabel2nd = new JLabel("<html><body>The Content Checker allows you to download card images, ship dials, ship bases as new content<br>" +
                "is previewed or released in the game.<br>"+
                "If you see the Content Checker flashing black, it means you need to verify if you have some files to download right here in this window.<br>" +
                "Click on the 'Check for missing content...' button to get a list of missing elements and then click download.<br>" +
                "Doing so will ensure maximum compatibility with other players.</body></html>");

        final JButton activate2ndTab = new JButton("Check for missing content in 2nd edition (Warning: may take minutes!)");
        activate2ndTab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activate2ndTab.setEnabled(false);
                ContentsCheckerWindow2ndEdTab(secondEditionMainPanel);
            }
        });

        secondEditionMainPanel.add(sourceExplanationLabel2nd);
        secondEditionMainPanel.add(activate2ndTab);
        secondEditionMainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
       // frame.add(secondEditionMainPanel, BorderLayout.PAGE_START);

        // =============================================================
        // Common to both editions
        // =============================================================
        if(myTabbedPane.getTabCount()==0){

            myTabbedPane.addTab("Second Edition",secondEditionMainPanel);
            myTabbedPane.addTab("First Edition",panel);
        }

        frame.setPreferredSize(new Dimension(800,900));
        frame.setTitle("Content Checker");
        panel.setOpaque(true); // content panes must be opaque
        frame.setContentPane(myTabbedPane);
        //frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.toFront();

        if(missing1stEdContent>0) activateBlinky1stTab();
        if(missing2ndEdContent>0) activateBlinky2ndTab();

    }

    //Download the missing content from the built CheckResults list (can be every content if the checkbox downloadAll was selected
    private void downloadAll2e(String branchURL)
    {

        boolean needToSaveModule = false;

        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();
        ArchiveWriter writer = new ArchiveWriter(fileArchive);

        // download pilots
        if(results2e.getMissingPilots().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("pilots", results2e.getMissingPilotImages(),writer, branchURL);
            needToSaveModule = true;
        }

        // download ships
        if(results2e.getMissingShips().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("ships", results2e.getMissingShipImages(),writer, branchURL);
            needToSaveModule = true;
        }

        // download Upgrades
        if(results2e.getMissingUpgrades().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("upgrades", results2e.getMissingUpgradeImages(),writer, branchURL);
            needToSaveModule = true;
        }

        // download Conditions
        if(results2e.getMissingConditions().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("conditions", results2e.getMissingConditionImages(),writer,branchURL);
            needToSaveModule = true;
        }

        if(needToSaveModule)
        {
            try {
                writer.save();
                needToSaveModule = false;
            } catch (IOException e) {
                logToChat("Exception occurred saving module");
            }
        }
/*
        // generate dial masks
        Iterator<OTADialMask> dialMaskIterator = results.getMissingDialMasks().iterator();
        OTADialMask dialMask = null;
        while(dialMaskIterator.hasNext())
        {
            dialMask = dialMaskIterator.next();

            XWOTAUtils.buildDialMaskImages(dialMask.getFaction(),dialMask.getShipXws(),dialMask.getDialHideImageName(),dialMask.getDialMaskImageName(),writer);
            needToSaveModule = true;
        }
*/
        // generate ship bases
        Iterator<OTAShipBase> shipBaseIterator = results2e.getMissingShipBases().iterator();
        OTAShipBase shipBase = null;
        while(shipBaseIterator.hasNext())
        {
            shipBase = shipBaseIterator.next();
            XWS2Pilots shipData = XWS2Pilots.getSpecificShipFromShipXWS(shipBase.getShipXws(), allShips);

            //TODO implement huge ships this
            if(!shipData.getSize().equals("huge")) {

                XWOTAUtils.buildBaseShipImage2e(shipBase.getFaction(), shipBase.getShipXws(),
                        Canonicalizer.getCleanedName(shipData.getSize()),
                        shipBase.getIdentifier(),shipBase.getshipImageName(), writer);
                needToSaveModule = true;
            }
        }

        if(needToSaveModule) {
            try {
                writer.save();
            } catch (IOException e) {
                logToChat("Exception occurred saving module");
            }
        }
    }
    private void downloadAll(String branchURL)
    {

        boolean needToSaveModule = false;

        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();
        ArchiveWriter writer = new ArchiveWriter(fileArchive);

        // download pilots
        if(results.getMissingPilots().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("pilots", results.getMissingPilotImages(),writer, branchURL);
            needToSaveModule = true;
        }

        // download ships
        if(results.getMissingShips().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("ships", results.getMissingShipImages(),writer, branchURL);
            needToSaveModule = true;
        }

        // download Upgrades
        if(results.getMissingUpgrades().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("upgrades", results.getMissingUpgradeImages(),writer, branchURL);
            needToSaveModule = true;
        }

        // download Conditions
        if(results.getMissingConditions().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("conditions", results.getMissingConditionImages(),writer,branchURL);
            needToSaveModule = true;
        }

        // download actions
        if(results.getMissingActions().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("actions", results.getMissingActionImages(),writer, branchURL);
            needToSaveModule = true;
        }

        // download dial hides
        if(results.getMissingDialHides().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("dial", results.getMissingDialHideImages(),writer, branchURL);
            needToSaveModule = true;
        }

        if(needToSaveModule)
        {
            try {
                writer.save();
                needToSaveModule = false;
            } catch (IOException e) {
                logToChat("Exception occurred saving module");
            }
        }


        // generate dial masks
        Iterator<OTADialMask> dialMaskIterator = results.getMissingDialMasks().iterator();
        OTADialMask dialMask = null;
        while(dialMaskIterator.hasNext())
        {
            dialMask = dialMaskIterator.next();

            XWOTAUtils.buildDialMaskImages(dialMask.getFaction(),dialMask.getShipXws(),dialMask.getDialHideImageName(),dialMask.getDialMaskImageName(),writer);
            needToSaveModule = true;
        }

        // generate ship bases
        Iterator<OTAShipBase> shipBaseIterator = results.getMissingShipBases().iterator();
        OTAShipBase shipBase = null;
        while(shipBaseIterator.hasNext())
        {
            shipBase = shipBaseIterator.next();

            MasterShipData.ShipData shipData = MasterShipData.getShipData(shipBase.getShipXws());
            java.util.List<String> arcs = shipData.getFiringArcs();

            java.util.List<String> actions = shipData.getActions();

            //TODO implement huge ships this
            if(!shipData.getSize().equals("huge")) {

                XWOTAUtils.buildBaseShipImage(shipBase.getFaction(), shipBase.getShipXws(), arcs, actions, shipData.getSize(),shipBase.getIdentifier(),shipBase.getshipImageName(), writer);
                needToSaveModule = true;
            }

        }

        if(needToSaveModule) {
            try {
                writer.save();
            } catch (IOException e) {
                logToChat("Exception occurred saving module");
            }
        }
    }

    //refresh the table results for the benefit of the user
    private void refreshFinalTable2e()
    {
        results2e = checkAllResults2e();
        String[][] convertedTableResults = buildTableResultsFromResults2e(results2e);

        DefaultTableModel model = (DefaultTableModel) finalTable.getModel();
        model.setNumRows(convertedTableResults.length);
        model.setDataVector(convertedTableResults,finalColumnNames);
        finalTable.getColumnModel().getColumn(0).setPreferredWidth(75);;
        finalTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        finalTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(finalTable.getModel());
        ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>(25);
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        finalTable.setRowSorter(sorter);

        model.fireTableDataChanged();

        if(finalTable.getModel().getRowCount() == 0)
        {
            jlabel.setText("Your content is up to date");
            downloadButton2e.setEnabled(false);
        }else{
            //jlabel.setText("Click the download button to download the following images");
            downloadButton2e.setEnabled(true);
        }
        // framefor1st.repaint();
    }
    private void refreshFinalTable()
    {
        results = checkAllResults();
        String[][] convertedTableResults = buildTableResultsFromResults(results);


        DefaultTableModel model = (DefaultTableModel) finalTable.getModel();
        model.setNumRows(convertedTableResults.length);
        model.setDataVector(convertedTableResults,finalColumnNames);
        finalTable.getColumnModel().getColumn(0).setPreferredWidth(75);;
        finalTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        finalTable.getColumnModel().getColumn(2).setPreferredWidth(150);


        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(finalTable.getModel());
        ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>(25);
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        finalTable.setRowSorter(sorter);


        model.fireTableDataChanged();


        if(finalTable.getModel().getRowCount() == 0)
        {
            jlabel.setText("Your content is up to date");
            downloadButton.setEnabled(false);


        }else{
            //jlabel.setText("Click the download button to download the following images");
            downloadButton.setEnabled(true);
        }
        // framefor1st.repaint();
    }

    private String[][] buildTableResultsFromResults2e(OTAContentsCheckerResults results)
    {
        ArrayList<String[]> tableResults = new ArrayList<String[]>();
        String[] tableRow = null;


        //ships
        OTAMasterShips.OTAShip ship = null;
        for(int i = 0; i<results.getMissingShips().size(); i++)
        {
            ship = results.getMissingShips().get(i);
            tableRow = new String[3];
            tableRow[0] = "Ship";
            tableRow[1] = XWS2Pilots.getSpecificShipFromShipXWS(ship.getXws(), allShips).getName();

            if(ship.getIdentifier().equalsIgnoreCase("Standard"))
            {
                tableRow[2] = "";
            }else {
                tableRow[2] = ship.getIdentifier();
            }
            tableResults.add(tableRow);
        }

        // bases
        OTAShipBase shipBase = null;
        for(int i = 0; i<results.getMissingShipBases().size();i++)
        {
            shipBase = results.getMissingShipBases().get(i);
            tableRow = new String[3];
            tableRow[0] = "Ship Base";
            tableRow[1] = XWS2Pilots.getSpecificShipFromShipXWS(shipBase.getShipXws(), allShips).getName();
            if(shipBase.getIdentifier().equalsIgnoreCase("Standard")) {
                tableRow[2] = fullFactionNames.get(shipBase.getFaction());
            }else{
                tableRow[2] = fullFactionNames.get(shipBase.getFaction()) + shipBase.getIdentifier();
            }
            tableResults.add(tableRow);
        }

 /*
        // dial hides
        OTAMasterDialHides.OTADialHide dialHide = null;
        for(int i=0;i<results.getMissingDialHides().size();i++)
        {
            dialHide = results.getMissingDialHides().get(i);
            tableRow = new String[3];
            tableRow[0] = "Dial Hide";
            tableRow[1] = MasterShipData.getShipData(dialHide.getXws()).getName();
            tableRow[2] = "";
            tableResults.add(tableRow);
        }

        // dial masks
        OTADialMask dialMask = null;
        for(int i=0;i<results.getMissingDialMasks().size();i++)
        {
            dialMask = results.getMissingDialMasks().get(i);
            tableRow = new String[3];
            tableRow[0] = "Dial Mask";
            tableRow[1] = MasterShipData.getShipData(dialMask.getShipXws()).getName();
            tableRow[2] = fullFactionNames.get(dialMask.getFaction());
            tableResults.add(tableRow);
        }
*/
        // pilots
        OTAMasterPilots.OTAPilot pilot = null;
        for(int i=0;i<results.getMissingPilots().size();i++)
        {
            pilot = results.getMissingPilots().get(i);
            tableRow = new String[3];
            tableRow[0] = "Pilot";
            tableRow[1] = (XWS2Pilots.getSpecificPilot(pilot.getPilotXws(), allShips)).getName();
            tableRow[2] = fullFactionNames.get(pilot.getFaction());
            tableResults.add(tableRow);
        }

        // upgrades
        OTAMasterUpgrades.OTAUpgrade upgrade = null;


        for(int i=0;i<results.getMissingUpgrades().size();i++)
        {
            upgrade = results.getMissingUpgrades().get(i);
            tableRow = new String[3];
            tableRow[0] = "Upgrade";

            XWS2Upgrades.OneUpgrade detectedUpgrade = XWS2Upgrades.getSpecificUpgrade(upgrade.getXws(), allUpgrades);
            if(detectedUpgrade != null)
            {
                tableRow[1] = detectedUpgrade.getName();
            }else{
                tableRow[1] = upgrade.getSlot()+" " +upgrade.getXws();
            }


            tableRow[2] = "";
            tableResults.add(tableRow);
        }

        // Conditions/Tokens
        OTAMasterConditions.OTACondition condition = null;
        for(int i=0;i<results.getMissingConditions().size();i++)
        {
            condition = results.getMissingConditions().get(i);
            tableRow = new String[3];
            tableRow[0] = "Condition";
            tableRow[1] = XWS2Upgrades.getSpecificConditionByXWS(condition.getXws(), allConditions).getName();
            tableRow[2] = "Card";
            tableResults.add(tableRow);

            tableRow = new String[3];
            tableRow[0] = "Condition";
            tableRow[1] = XWS2Upgrades.getSpecificConditionByXWS(condition.getXws(), allConditions).getName();
            tableRow[2] = "Token";
            tableResults.add(tableRow);
        }

        // convert the arrayList to an array
        String[][] convertedTableResults = convertTableArrayListToArray(tableResults);
        return convertedTableResults;
    }
    private String[][] buildTableResultsFromResults(OTAContentsCheckerResults results)
    {
        ArrayList<String[]> tableResults = new ArrayList<String[]>();
        String[] tableRow = null;

        //ships
        OTAMasterShips.OTAShip ship = null;
        for(int i = 0; i<results.getMissingShips().size(); i++)
        {
            ship = results.getMissingShips().get(i);
            tableRow = new String[3];
            tableRow[0] = "Ship";
            tableRow[1] = MasterShipData.getShipData(ship.getXws()).getName();

            if(ship.getIdentifier().equalsIgnoreCase("Standard"))
            {
                tableRow[2] = "";
            }else {
                tableRow[2] = ship.getIdentifier();
            }
            tableResults.add(tableRow);
        }

        // bases
        OTAShipBase shipBase = null;
        for(int i = 0; i<results.getMissingShipBases().size();i++)
        {
            shipBase = results.getMissingShipBases().get(i);
            tableRow = new String[3];
            tableRow[0] = "Ship Base";
            tableRow[1] = MasterShipData.getShipData(shipBase.getShipXws()).getName();
            if(shipBase.getIdentifier().equalsIgnoreCase("Standard")) {
                tableRow[2] = fullFactionNames.get(shipBase.getFaction());
            }else{
                tableRow[2] = fullFactionNames.get(shipBase.getFaction()) + shipBase.getIdentifier();
            }

            tableResults.add(tableRow);
        }

        // dial hides
        OTAMasterDialHides.OTADialHide dialHide = null;
        for(int i=0;i<results.getMissingDialHides().size();i++)
        {
            dialHide = results.getMissingDialHides().get(i);
            tableRow = new String[3];
            tableRow[0] = "Dial Hide";
            tableRow[1] = MasterShipData.getShipData(dialHide.getXws()).getName();
            tableRow[2] = "";
            tableResults.add(tableRow);
        }

        // dial masks
        OTADialMask dialMask = null;
        for(int i=0;i<results.getMissingDialMasks().size();i++)
        {
            dialMask = results.getMissingDialMasks().get(i);
            tableRow = new String[3];
            tableRow[0] = "Dial Mask";
            tableRow[1] = MasterShipData.getShipData(dialMask.getShipXws()).getName();
            tableRow[2] = fullFactionNames.get(dialMask.getFaction());
            tableResults.add(tableRow);
        }

        // pilots
        OTAMasterPilots.OTAPilot pilot = null;
        for(int i=0;i<results.getMissingPilots().size();i++)
        {
            pilot = results.getMissingPilots().get(i);
            tableRow = new String[3];
            tableRow[0] = "Pilot";
            tableRow[1] = MasterPilotData.getPilotData(pilot.getShipXws(), pilot.getPilotXws(), pilot.getFaction()).getName();
            tableRow[2] = fullFactionNames.get(pilot.getFaction());
            tableResults.add(tableRow);
        }

        // upgrades
        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        for(int i=0;i<results.getMissingUpgrades().size();i++)
        {
            upgrade = results.getMissingUpgrades().get(i);
            tableRow = new String[3];
            tableRow[0] = "Upgrade";

            if(MasterUpgradeData.getUpgradeData(upgrade.getXws()) != null)
            {
                tableRow[1] = MasterUpgradeData.getUpgradeData(upgrade.getXws()).getName();
            }else{
                tableRow[1] = upgrade.getSlot()+" " +upgrade.getXws();
            }


            tableRow[2] = "";
            tableResults.add(tableRow);
        }

        // Conditions/Tokens
        OTAMasterConditions.OTACondition condition = null;
        for(int i=0;i<results.getMissingConditions().size();i++)
        {
            condition = results.getMissingConditions().get(i);
            tableRow = new String[3];
            tableRow[0] = "Condition";
            tableRow[1] = MasterConditionData.getConditionData(condition.getXws()).getName();
            tableRow[2] = "Card";
            tableResults.add(tableRow);

            tableRow = new String[3];
            tableRow[0] = "Condition";
            tableRow[1] = MasterConditionData.getConditionData(condition.getXws()).getName();
            tableRow[2] = "Token";
            tableResults.add(tableRow);
        }

        // actions
        OTAMasterActions.OTAAction action = null;
        for(int i=0;i<results.getMissingActions().size();i++)
        {
            action = results.getMissingActions().get(i);
            tableRow = new String[3];
            tableRow[0] = "Action";
            tableRow[1] = action.getName();
            tableRow[2] = "";
            tableResults.add(tableRow);
        }

        // convert the arrayList to an array
        String[][] convertedTableResults = convertTableArrayListToArray(tableResults);
        return convertedTableResults;
    }

    private JTable buildFinalTable2e(OTAContentsCheckerResults results)
    {
        //{"Type","Name", "Variant"};

        String[][] convertedTableResults = buildTableResultsFromResults2e(results);

        // build the swing table
        finalTable = new JTable(convertedTableResults,finalColumnNames);
        DefaultTableModel model = new DefaultTableModel(convertedTableResults.length, finalColumnNames.length);
        model.setNumRows(convertedTableResults.length);
        model.setDataVector(convertedTableResults,finalColumnNames);

        finalTable.setModel(model);
        finalTable.getColumnModel().getColumn(0).setPreferredWidth(75);;
        finalTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        finalTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(finalTable.getModel());
        ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>(25);
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        finalTable.setRowSorter(sorter);

        finalTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return finalTable;

    }
    private JTable buildFinalTable(OTAContentsCheckerResults results)
    {
        //{"Type","Name", "Variant"};

        String[][] convertedTableResults = buildTableResultsFromResults(results);

        // build the swing table
        finalTable = new JTable(convertedTableResults,finalColumnNames);
        DefaultTableModel model = new DefaultTableModel(convertedTableResults.length, finalColumnNames.length);
        model.setNumRows(convertedTableResults.length);
        model.setDataVector(convertedTableResults,finalColumnNames);

        finalTable.setModel(model);
        finalTable.getColumnModel().getColumn(0).setPreferredWidth(75);;
        finalTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        finalTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(finalTable.getModel());
        ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>(25);
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        finalTable.setRowSorter(sorter);

        finalTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return finalTable;

    }

    private String[][] convertTableArrayListToArray(ArrayList<String[]> tableResults)
    {
        // convert the ArrayList<String[]> to a String[][]
        String[][] convertedTableResults = new String[tableResults.size()][3];
        String[] tableRow = null;
        for(int i=0; i<tableResults.size();i++)
        {
            tableRow = tableResults.get(i);
            convertedTableResults[i] = tableRow;
        }
        return convertedTableResults;
    }

    private OTAContentsCheckerResults checkAllResults2e() {
        results2e = new OTAContentsCheckerResults();

        // perform all checks
        modIntChecker_2e = new ModuleIntegrityChecker_2e();

        results2e.setPilotResults(modIntChecker_2e.checkPilots(false, allShips));
        results2e.setShipResults(modIntChecker_2e.checkShips(false, allShips));
        results2e.setUpgradeResults(modIntChecker_2e.checkUpgrades(false, allUpgrades));
        results2e.setConditionResults(modIntChecker_2e.checkConditions(false, allConditions));
        results2e.setShipBaseResults(modIntChecker_2e.checkShipBases(false, allShips));
        /*
        results2e.setActionResults(modIntChecker_2e.checkActions(false));
        results.setDialHideResults(modIntChecker_2e.checkDialHides(false));
        results.setDialMaskResults(modIntChecker_2e.checkDialMasks(false));
        */

        //These 3 are unused so they get empty ArrayLists.
        results2e.setMissingActions(new ArrayList<OTAMasterActions.OTAAction>());
        results2e.setMissingDialHides(new ArrayList<OTAMasterDialHides.OTADialHide>());
        results2e.setMissingDialMasks(new ArrayList<OTADialMask>());

        // determine which images are missing
        results2e.setMissingPilots(findMissingPilots(results2e.getPilotResults()));
        results2e.setMissingShips(findMissingShips(results2e.getShipResults()));
        results2e.setMissingUpgrades(findMissingUpgrades(results2e.getUpgradeResults()));
        results2e.setMissingConditions(findMissingConditions(results2e.getConditionResults()));
        results2e.setMissingShipBases(findMissingShipBases(results2e.getShipBaseResults()));

/*
        results.setMissingActions(findMissingActions(results.getActionResults()));
        results.setMissingConditions(findMissingConditions(results.getConditionResults()));
        results.setMissingDialHides(findMissingDialHides(results.getDialHideResults()));
        results.setMissingDialMasks(findMissingDialMasks(results.getDialMaskResults()));
*/
        return results2e;
    }
    private OTAContentsCheckerResults checkAllResults()
    {
        results = new OTAContentsCheckerResults();

        // perform all checks
        modIntChecker = new ModuleIntegrityChecker();
        results.setPilotResults(modIntChecker.checkPilots(false));
        results.setShipResults(modIntChecker.checkShips(false));
        results.setActionResults(modIntChecker.checkActions(false));
        results.setShipBaseResults(modIntChecker.checkShipBases(false));
        results.setUpgradeResults(modIntChecker.checkUpgrades(false));
        results.setConditionResults(modIntChecker.checkConditions(false));
        results.setDialHideResults(modIntChecker.checkDialHides(false));
        results.setDialMaskResults(modIntChecker.checkDialMasks(false));


        // determine which images are missing
        results.setMissingPilots(findMissingPilots(results.getPilotResults()));
        results.setMissingUpgrades(findMissingUpgrades(results.getUpgradeResults()));
        results.setMissingConditions(findMissingConditions(results.getConditionResults()));
        results.setMissingShips(findMissingShips(results.getShipResults()));
        results.setMissingActions(findMissingActions(results.getActionResults()));
        results.setMissingDialHides(findMissingDialHides(results.getDialHideResults()));
        results.setMissingDialMasks(findMissingDialMasks(results.getDialMaskResults()));
        results.setMissingShipBases(findMissingShipBases(results.getShipBaseResults()));


        return results;
    }

    private ArrayList<OTAMasterPilots.OTAPilot> findMissingPilots(ArrayList<OTAMasterPilots.OTAPilot> pilotResults)
    {
        ArrayList<OTAMasterPilots.OTAPilot> missing = new ArrayList<OTAMasterPilots.OTAPilot>();
        Iterator<OTAMasterPilots.OTAPilot> pilotIterator = pilotResults.iterator();
        OTAMasterPilots.OTAPilot pilot = null;
        while(pilotIterator.hasNext())
        {
            pilot = pilotIterator.next();
            if((!pilot.getStatus() && pilot.getStatusOTA()) || downloadAll)
            {
                missing.add(pilot);
            }
        }
        return missing;
    }

    private ArrayList<OTAMasterUpgrades.OTAUpgrade> findMissingUpgrades(ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeResults)
    {
        ArrayList<OTAMasterUpgrades.OTAUpgrade> missing = new ArrayList<OTAMasterUpgrades.OTAUpgrade>();
        Iterator<OTAMasterUpgrades.OTAUpgrade> upgradeIterator = upgradeResults.iterator();
        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        while(upgradeIterator.hasNext())
        {
            upgrade = upgradeIterator.next();
            if((!upgrade.getStatus() && upgrade.getStatusOTA()) || downloadAll)
            {
                missing.add(upgrade);
            }
        }
        return missing;
    }

    private ArrayList<OTAMasterConditions.OTACondition> findMissingConditions(ArrayList<OTAMasterConditions.OTACondition> conditionResults)
    {
        ArrayList<OTAMasterConditions.OTACondition> missing = new ArrayList<OTAMasterConditions.OTACondition>();
        Iterator<OTAMasterConditions.OTACondition> conditionIterator = conditionResults.iterator();
        OTAMasterConditions.OTACondition condition = null;
        while(conditionIterator.hasNext())
        {
            condition = conditionIterator.next();
            if((!condition.getStatus() && condition.getStatusOTA()) || (!condition.getTokenStatus() && condition.getTokenStatusOTA()) || downloadAll)
            {
                missing.add(condition);
            }

        }
        return missing;
    }

    private ArrayList<OTAMasterShips.OTAShip> findMissingShips(ArrayList<OTAMasterShips.OTAShip> shipResults)
    {
        ArrayList<OTAMasterShips.OTAShip> missing = new ArrayList<OTAMasterShips.OTAShip>();
        Iterator<OTAMasterShips.OTAShip> shipIterator = shipResults.iterator();
        OTAMasterShips.OTAShip ship = null;
        while(shipIterator.hasNext())
        {
            ship = shipIterator.next();
            if((!ship.getStatus() && ship.getStatusOTA()) || downloadAll)
            {
                missing.add(ship);
            }
        }
        return missing;
    }

    private ArrayList<OTAMasterActions.OTAAction> findMissingActions(ArrayList<OTAMasterActions.OTAAction> actionResults)
    {
        ArrayList<OTAMasterActions.OTAAction> missing = new ArrayList<OTAMasterActions.OTAAction>();
        Iterator<OTAMasterActions.OTAAction> actionIterator = actionResults.iterator();
        OTAMasterActions.OTAAction action = null;
        while(actionIterator.hasNext())
        {
            action = actionIterator.next();
            if(!action.getStatus() || downloadAll)
            {
                missing.add(action);
            }
        }
        return missing;
    }

    private ArrayList<OTAMasterDialHides.OTADialHide> findMissingDialHides(ArrayList<OTAMasterDialHides.OTADialHide> dialHideResults)
    {
        ArrayList<OTAMasterDialHides.OTADialHide> missing = new ArrayList<OTAMasterDialHides.OTADialHide>();
        Iterator<OTAMasterDialHides.OTADialHide> dialHideIterator = dialHideResults.iterator();
        OTAMasterDialHides.OTADialHide dialHide = null;
        while(dialHideIterator.hasNext())
        {
            dialHide = dialHideIterator.next();
            if(!dialHide.getStatus() || downloadAll)
            {
                missing.add(dialHide);
            }
        }
        return missing;
    }

    private ArrayList<OTADialMask> findMissingDialMasks(ArrayList<OTADialMask> dialMaskResults)
    {
        ArrayList<OTADialMask> missing = new ArrayList<OTADialMask>();
        Iterator<OTADialMask> dialMaskIterator = dialMaskResults.iterator();
        while(dialMaskIterator.hasNext())
        {
            OTADialMask dialMask = dialMaskIterator.next();
            if(!dialMask.getStatus() || downloadAll)
            {
                missing.add(dialMask);

            }
        }
        return missing;
    }

    private ArrayList<OTAShipBase> findMissingShipBases(ArrayList<OTAShipBase> shipBaseResults)
    {
        ArrayList<OTAShipBase> missing = new ArrayList<OTAShipBase>();
        Iterator<OTAShipBase> shipBaseIterator = shipBaseResults.iterator();
        while(shipBaseIterator.hasNext())
        {
            OTAShipBase shipBase = shipBaseIterator.next();
            if((!shipBase.getStatus() && shipBase.getStatusOTA()) || downloadAll)
            {
                missing.add(shipBase);

            }
        }
        return missing;
    }

    public String getDescription() {
        return "Contents Checker (mic.ota.OTAContentsChecker)";
    }

    @Override
    public HelpFile getHelpFile() {
        return null;
    }

    @Override
    public String getAttributeValueString(String key) {
        return null;
    }

    @Override
    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    @Override
    public Class<?>[] getAttributeTypes() {
        return new Class[0];
    }

    @Override
    public void removeFrom(Buildable parent) {
        GameModule.getGameModule().getToolBar().remove(contentCheckerButton);
    }

    @Override
    public void setAttribute(String key, Object value) {

    }

    @Override
    public String[] getAttributeDescriptions() {
        return new String[0];
    }

    @Override
    public String[] getAttributeNames() {
        return new String[0];
    }
}