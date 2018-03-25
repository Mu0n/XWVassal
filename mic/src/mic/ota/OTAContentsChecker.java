package mic.ota;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.FileArchive;
import mic.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Mic on 12/03/2018.
 */
public class OTAContentsChecker extends AbstractConfigurable {
    private JButton OKButton = new JButton();

    private ArrayList<String> missingPilots;
    private ArrayList<String> missingShips;
    private ArrayList<String> missingActions;
    private ArrayList<String> missingUpgrades;
    private ArrayList<String> missingConditions;
    private ArrayList<String> missingDialHides;
    private ArrayList<OTADialMask> missingDialMasks;
    private ArrayList<OTAShipBase> missingShipBases;

    private final String[] finalColumnNames = {"Type","Name", "Faction"};


    private JTable finalTable;

    private ModuleIntegrityChecker modIntChecker = null;


    private synchronized void ContentsCheckerWindow()
    {

        modIntChecker = new ModuleIntegrityChecker();


        ArrayList<OTAMasterPilots.OTAPilot> pilotResults = modIntChecker.checkPilots();
        ArrayList<OTAMasterShips.OTAShip> shipResults = modIntChecker.checkShips();
        ArrayList<OTAMasterActions.OTAAction> actionResults = modIntChecker.checkActions();
        ArrayList<OTAShipBase> shipBaseResults = modIntChecker.checkShipBases();
        ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeResults = modIntChecker.checkUpgrades();
        ArrayList<OTAMasterConditions.OTACondition> conditionResults = modIntChecker.checkConditions();
        ArrayList<OTAMasterDialHides.OTADialHide> dialHideResults = modIntChecker.checkDialHides();
        ArrayList<OTADialMask> dialMaskResults = modIntChecker.checkDialMasks();

        // store the missing pilots
        missingPilots = new ArrayList<String>();
        Iterator<OTAMasterPilots.OTAPilot> pilotIterator = pilotResults.iterator();
        OTAMasterPilots.OTAPilot pilot = null;
        while(pilotIterator.hasNext())
        {
            pilot = pilotIterator.next();
            if(!pilot.getStatus())
            {
                missingPilots.add(pilot.getImage());
            }
        }

        // store the missing upgrades
        missingUpgrades = new ArrayList<String>();
        Iterator<OTAMasterUpgrades.OTAUpgrade> upgradeIterator = upgradeResults.iterator();
        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        while(upgradeIterator.hasNext())
        {
            upgrade = upgradeIterator.next();
            if(!upgrade.getStatus())
            {
                missingUpgrades.add(upgrade.getImage());
            }
        }

        // store the missing conditions
        missingConditions = new ArrayList<String>();
        Iterator<OTAMasterConditions.OTACondition> conditionIterator = conditionResults.iterator();
        OTAMasterConditions.OTACondition condition = null;
        while(conditionIterator.hasNext())
        {
            condition = conditionIterator.next();
            if(!condition.getStatus())
            {
                missingConditions.add(condition.getImage());
            }
            if(!condition.getTokenStatus())
            {
                missingConditions.add(condition.getTokenImage());
            }
        }

        // store the missing ships
        missingShips = new ArrayList<String>();
        Iterator<OTAMasterShips.OTAShip> shipIterator = shipResults.iterator();
        OTAMasterShips.OTAShip ship = null;
        while(shipIterator.hasNext())
        {
            ship = shipIterator.next();
            if(!ship.getStatus())
            {
                missingShips.add(ship.getImage());
            }
        }

        // store the missing actions
        missingActions = new ArrayList<String>();
        Iterator<OTAMasterActions.OTAAction> actionIterator = actionResults.iterator();
        OTAMasterActions.OTAAction action = null;
        while(actionIterator.hasNext())
        {
            action = actionIterator.next();
            if(!action.getStatus())
            {
                missingActions.add(action.getImage());
            }
        }

        // store the missing dial hides
        missingDialHides = new ArrayList<String>();
        Iterator<OTAMasterDialHides.OTADialHide> dialHideIterator = dialHideResults.iterator();
        OTAMasterDialHides.OTADialHide dialHide = null;
        while(dialHideIterator.hasNext())
        {
            dialHide = dialHideIterator.next();
            if(!dialHide.getStatus())
            {
                missingDialHides.add(dialHide.getImage());
            }
        }

        // store the missing dial masks
        missingDialMasks = new ArrayList<OTADialMask>();
        Iterator<OTADialMask> dialMaskIterator = dialMaskResults.iterator();
        while(dialMaskIterator.hasNext())
        {
            OTADialMask dialMask = dialMaskIterator.next();
            if(!dialMask.getStatus())
            {
                missingDialMasks.add(dialMask);

            }
        }

        // store the missing ship bases
        missingShipBases = new ArrayList<OTAShipBase>();
        Iterator<OTAShipBase> shipBaseIterator = shipBaseResults.iterator();
        while(shipBaseIterator.hasNext())
        {
            OTAShipBase shipBase = shipBaseIterator.next();
            if(!shipBase.getStatus())
            {
                missingShipBases.add(shipBase);

            }
        }


        String msg = modIntChecker.getTestString();;
        JFrame frame = new JFrame();
        frame.setResizable(true);
        JPanel panel = new JPanel();
        JLabel spacer;

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JOptionPane optionPane = new JOptionPane();
        optionPane.setMessage(msg);
        optionPane.add(panel);
        JDialog dialog = optionPane.createDialog(frame, "Contents Checker");
        dialog.setSize(1000,1000);

        // new window here
        finalTable = buildFinalTable(pilotResults,shipResults,actionResults,shipBaseResults,upgradeResults,conditionResults,dialHideResults,dialMaskResults);

        if(finalTable.getModel().getRowCount() > 0) {
            JScrollPane finalPane = new JScrollPane(finalTable);
            panel.add(finalPane, BorderLayout.CENTER);
            JButton downloadAllButton = new JButton("Download");
            downloadAllButton.setAlignmentY(0.0F);
            downloadAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    downloadAll();
                }
            });
            panel.add(downloadAllButton);
        }else{
            String okMsg = "All content is up to date";
            optionPane.setMessage(okMsg);
            optionPane.add(panel);
            dialog.setSize(250,250);
        }

            dialog.setVisible(true);
            frame.toFront();
            frame.repaint();

    }


    private synchronized void downloadAll() {

        boolean needToSaveModule = false;

        // download pilots
        if(missingPilots.size() > 0) {
            XWOTAUtils.downloadAndSaveImagesFromOTA("pilots", missingPilots);
            needToSaveModule = true;
        }

        // download Upgrades
        if(missingUpgrades.size() > 0) {
            XWOTAUtils.downloadAndSaveImagesFromOTA("upgrades", missingUpgrades);
            needToSaveModule = true;
        }

        // download Conditions
        if(missingConditions.size() > 0) {
            XWOTAUtils.downloadAndSaveImagesFromOTA("conditions", missingConditions);
            needToSaveModule = true;
        }

        // download actions
        if(missingActions.size() > 0) {
            XWOTAUtils.downloadAndSaveImagesFromOTA("actions", missingActions);
            needToSaveModule = true;
        }

        // download dial hides
        if(missingDialHides.size() > 0) {
            XWOTAUtils.downloadAndSaveImagesFromOTA("dial", missingDialHides);
            needToSaveModule = true;
        }

        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();
        ArchiveWriter writer = new ArchiveWriter(fileArchive);


        // generate dial masks
        Iterator<OTADialMask> dialMaskIterator = missingDialMasks.iterator();
        OTADialMask dialMask = null;
        while(dialMaskIterator.hasNext())
        {
            dialMask = dialMaskIterator.next();

            XWOTAUtils.buildDialMaskImages(dialMask.getFaction(),dialMask.getShipXws(),dialMask.getDialHideImageName(),dialMask.getDialMaskImageName(),writer);
            needToSaveModule = true;
        }

        // generate ship bases
        Iterator<OTAShipBase> shipBaseIterator = missingShipBases.iterator();
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
                mic.Util.logToChat("Exception occurred saving module");
            }

            // refresh the table
            refreshFinalTable();
        }


    }


    private void refreshFinalTable()
    {

        modIntChecker = new ModuleIntegrityChecker();


        ArrayList<OTAMasterPilots.OTAPilot> pilotResults = modIntChecker.checkPilots();
        ArrayList<OTAMasterShips.OTAShip> shipResults = modIntChecker.checkShips();
        ArrayList<OTAMasterActions.OTAAction> actionResults = modIntChecker.checkActions();
        ArrayList<OTAShipBase> shipBaseResults = modIntChecker.checkShipBases();
        ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeResults = modIntChecker.checkUpgrades();
        ArrayList<OTAMasterConditions.OTACondition> conditionResults = modIntChecker.checkConditions();
        ArrayList<OTAMasterDialHides.OTADialHide> dialHideResults = modIntChecker.checkDialHides();
        ArrayList<OTADialMask> dialMaskResults = modIntChecker.checkDialMasks();

        ArrayList<String[]> tableResults = new ArrayList<String[]>();

        // fill in the ships
        OTAMasterShips.OTAShip ship = null;
        missingShips = new ArrayList<String>();
        for(int i=0;i<shipResults.size();i++)
        {

            ship = shipResults.get(i);
            if(!ship.getStatus())
            {
                String[] shipLine = new String[3];
                shipLine[0] = "Ship Image";
                shipLine[1] = MasterShipData.getShipData(ship.getXws()).getName();
                tableResults.add(shipLine);
                missingShips.add(ship.getImage());
            }
        }

        // fill in the ship bases
        missingShipBases = new ArrayList<OTAShipBase>();
        OTAShipBase shipBase = null;
        for(int i=0;i<shipBaseResults.size();i++)
        {

            shipBase = shipBaseResults.get(i);

            if(!shipBase.getStatus()) {
                String[] shipBaseLine = new String[3];
                shipBaseLine[0] = "Ship Base Image";
                shipBaseLine[1] = MasterShipData.getShipData(shipBase.getShipXws()).getName();
                tableResults.add(shipBaseLine);
                missingShipBases.add(shipBase);
            }

        }

        // fill in the dial hides
        missingDialHides = new ArrayList<String>();
        OTAMasterDialHides.OTADialHide dialHide = null;
        for(int i=0;i<dialHideResults.size();i++)
        {

            dialHide = dialHideResults.get(i);
            if(!dialHide.getStatus()) {
                String[] dialHideLine = new String[3];
                dialHideLine[0] = "Dial Hide Image";
                dialHideLine[1] = MasterShipData.getShipData(dialHide.getXws()).getName();
                tableResults.add(dialHideLine);
                missingDialHides.add(dialHide.getImage());
            }
        }

        // fill in the dial masks
        missingDialMasks = new ArrayList<OTADialMask>();
        OTADialMask dialMask = null;
        for(int i=0;i<dialMaskResults.size();i++)
        {

            dialMask = dialMaskResults.get(i);
            if(!dialMask.getStatus()) {
                String[] dialMaskLine = new String[3];
                dialMaskLine[0] = "Dial Hide Image";
                dialMaskLine[1] = MasterShipData.getShipData(dialMask.getShipXws()).getName();
                dialMaskLine[2] = dialMask.getFaction();
                tableResults.add(dialMaskLine);
                missingDialMasks.add(dialMask);
            }
        }

        // fill in the pilots
        missingPilots = new ArrayList<String>();
        OTAMasterPilots.OTAPilot pilot = null;
        for(int i=0;i<pilotResults.size();i++)
        {

            pilot = pilotResults.get(i);
            if(!pilot.getStatus()) {
                String[] pilotLine = new String[3];
                pilotLine[0] = "Pilot";
                pilotLine[1] = MasterPilotData.getPilotData(pilot.getShipXws(), pilot.getPilotXws(), pilot.getFaction()).getName();
                pilotLine[2] = pilot.getFaction();
                tableResults.add(pilotLine);
                missingPilots.add(pilot.getImage());
            }
        }

        // fill in the upgrades
        missingUpgrades = new ArrayList<String>();
        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        for(int i=0;i<upgradeResults.size();i++)
        {

            upgrade = upgradeResults.get(i);
            if(!upgrade.getStatus()) {
                String[] upgradeLine = new String[3];
                upgradeLine[0] = "Upgrade";
                upgradeLine[1] = MasterUpgradeData.getUpgradeData(upgrade.getXws()).getName();
                tableResults.add(upgradeLine);
                missingUpgrades.add(upgrade.getImage());
            }
        }

        // fill in the conditions/tokens
        missingConditions = new ArrayList<String>();
        OTAMasterConditions.OTACondition condition = null;
        for(int i=0;i<conditionResults.size();i++)
        {

            condition = conditionResults.get(i);

            if(!condition.getStatus()) {
                String[] conditionLine = new String[3];
                conditionLine[0] = "Condition";
                conditionLine[1] = MasterConditionData.getConditionData(condition.getXws()).getName();
                tableResults.add(conditionLine);
                missingConditions.add(condition.getImage());
            }

            if(!condition.getTokenStatus())
            {
                String[] conditionLine = new String[3];
                conditionLine[0] = "Condition Token";
                conditionLine[1] = MasterConditionData.getConditionData(condition.getXws()).getName();
                tableResults.add(conditionLine);
                missingConditions.add(condition.getTokenImage());
            }
        }

        // fill in the actions
        missingActions = new ArrayList<String>();
        OTAMasterActions.OTAAction action = null;
        for(int i=0;i<actionResults.size();i++)
        {
            action = actionResults.get(i);
            if(!action.getStatus()) {
                String[] actionLine = new String[3];
                actionLine[0] = "Ship Action Image";
                actionLine[1] = action.getName();
                tableResults.add(actionLine);
                missingActions.add(action.getImage());
            }
        }

        // convert the ArrayList<String[]> to a String[][]
        String[][] convertedTableResults = new String[tableResults.size()][3];
        String[] tableRow = null;
        for(int i=0; i<tableResults.size();i++)
        {
            tableRow = tableResults.get(i);
            convertedTableResults[i] = tableRow;
        }

        DefaultTableModel model = (DefaultTableModel) finalTable.getModel();
        model.setNumRows(tableResults.size());
        model.setDataVector(convertedTableResults,finalColumnNames);
        finalTable.getColumnModel().getColumn(0).setPreferredWidth(75);;
        finalTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        finalTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        model.fireTableDataChanged();

    }




    private JTable buildFinalTable(ArrayList<OTAMasterPilots.OTAPilot> pilotResults,ArrayList<OTAMasterShips.OTAShip> shipResults,ArrayList<OTAMasterActions.OTAAction> actionResults,
                                   ArrayList<OTAShipBase> shipBaseResults,ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeResults, ArrayList<OTAMasterConditions.OTACondition> conditionResults,
                                   ArrayList<OTAMasterDialHides.OTADialHide> dialHideResults,ArrayList<OTADialMask> dialMaskResults)
    {

        ArrayList<String[]> tableResults = new ArrayList<String[]>();
        // fill in the ships
        OTAMasterShips.OTAShip ship = null;
        missingShips = new ArrayList<String>();
        for(int i=0;i<shipResults.size();i++)
        {

            ship = shipResults.get(i);
            if(!ship.getStatus())
            {
                String[] shipLine = new String[3];
                shipLine[0] = "Ship Image";
                shipLine[1] = MasterShipData.getShipData(ship.getXws()).getName();
                tableResults.add(shipLine);
                missingShips.add(ship.getImage());
            }
        }

        // fill in the ship bases
        missingShipBases = new ArrayList<OTAShipBase>();
        OTAShipBase shipBase = null;
        for(int i=0;i<shipBaseResults.size();i++)
        {

            shipBase = shipBaseResults.get(i);

            if(!shipBase.getStatus()) {
                String[] shipBaseLine = new String[3];
                shipBaseLine[0] = "Ship Base Image";
                shipBaseLine[1] = MasterShipData.getShipData(shipBase.getShipXws()).getName();
                tableResults.add(shipBaseLine);
                missingShipBases.add(shipBase);
            }

        }

        // fill in the dial hides
        missingDialHides = new ArrayList<String>();
        OTAMasterDialHides.OTADialHide dialHide = null;
        for(int i=0;i<dialHideResults.size();i++)
        {

            dialHide = dialHideResults.get(i);
            if(!dialHide.getStatus()) {
                String[] dialHideLine = new String[3];
                dialHideLine[0] = "Dial Hide Image";
                dialHideLine[1] = MasterShipData.getShipData(dialHide.getXws()).getName();
                tableResults.add(dialHideLine);
                missingDialHides.add(dialHide.getImage());
            }
        }

        // fill in the dial masks
        missingDialMasks = new ArrayList<OTADialMask>();
        OTADialMask dialMask = null;
        for(int i=0;i<dialMaskResults.size();i++)
        {

            dialMask = dialMaskResults.get(i);
            if(!dialMask.getStatus()) {
                String[] dialMaskLine = new String[3];
                dialMaskLine[0] = "Dial Hide Image";
                dialMaskLine[1] = MasterShipData.getShipData(dialMask.getShipXws()).getName();
                dialMaskLine[2] = dialMask.getFaction();
                tableResults.add(dialMaskLine);
                missingDialMasks.add(dialMask);
            }
        }

        // fill in the pilots
        missingPilots = new ArrayList<String>();
        OTAMasterPilots.OTAPilot pilot = null;
        for(int i=0;i<pilotResults.size();i++)
        {

            pilot = pilotResults.get(i);
            if(!pilot.getStatus()) {
                String[] pilotLine = new String[3];
                pilotLine[0] = "Pilot";
                pilotLine[1] = MasterPilotData.getPilotData(pilot.getShipXws(), pilot.getPilotXws(), pilot.getFaction()).getName();
                pilotLine[2] = pilot.getFaction();
                tableResults.add(pilotLine);
                missingPilots.add(pilot.getImage());
            }
        }

        // fill in the upgrades
        missingUpgrades = new ArrayList<String>();
        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        for(int i=0;i<upgradeResults.size();i++)
        {

            upgrade = upgradeResults.get(i);
            if(!upgrade.getStatus()) {
                String[] upgradeLine = new String[3];
                upgradeLine[0] = "Upgrade";
                upgradeLine[1] = MasterUpgradeData.getUpgradeData(upgrade.getXws()).getName();
                tableResults.add(upgradeLine);
                missingUpgrades.add(upgrade.getImage());
            }
        }

        // fill in the conditions/tokens
        missingConditions = new ArrayList<String>();
        OTAMasterConditions.OTACondition condition = null;
        for(int i=0;i<conditionResults.size();i++)
        {

            condition = conditionResults.get(i);

            if(!condition.getStatus()) {
                String[] conditionLine = new String[3];
                conditionLine[0] = "Condition";
                conditionLine[1] = MasterConditionData.getConditionData(condition.getXws()).getName();
                tableResults.add(conditionLine);
                missingConditions.add(condition.getImage());
            }

            if(!condition.getTokenStatus())
            {
                String[] conditionLine = new String[3];
                conditionLine[0] = "Condition Token";
                conditionLine[1] = MasterConditionData.getConditionData(condition.getXws()).getName();
                tableResults.add(conditionLine);
                missingConditions.add(condition.getTokenImage());
            }
        }

        // fill in the actions
        missingActions = new ArrayList<String>();
        OTAMasterActions.OTAAction action = null;
        for(int i=0;i<actionResults.size();i++)
        {
            action = actionResults.get(i);
            if(!action.getStatus()) {
                String[] actionLine = new String[3];
                actionLine[0] = "Ship Action Image";
                actionLine[1] = action.getName();
                tableResults.add(actionLine);
                missingActions.add(action.getImage());
            }
        }

        // convert the ArrayList<String[]> to a String[][]
        String[][] convertedTableResults = new String[tableResults.size()][3];
        String[] tableRow = null;
        for(int i=0; i<tableResults.size();i++)
        {
            tableRow = tableResults.get(i);
            convertedTableResults[i] = tableRow;
        }

        finalTable = new JTable(convertedTableResults,finalColumnNames);
        DefaultTableModel model = new DefaultTableModel(tableResults.size(), finalColumnNames.length);
        model.setNumRows(tableResults.size());
        model.setDataVector(convertedTableResults,finalColumnNames);

        finalTable.setModel(model);
        finalTable.getColumnModel().getColumn(0).setPreferredWidth(75);;
        finalTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        finalTable.getColumnModel().getColumn(2).setPreferredWidth(150);


        finalTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return finalTable;


    }

    public void addTo(Buildable parent) {
        JButton b = new JButton("Content Checker");
        b.setAlignmentY(0.0F);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ContentsCheckerWindow();
            }
        });
        OKButton = b;
        GameModule.getGameModule().getToolBar().add(b);
    }

    public String getDescription() {
        return "Contents Checker (mic.ContentsChecker)";
    }

    @Override
    public String[] getAttributeDescriptions() {
        return new String[0];
    }

    @Override
    public Class<?>[] getAttributeTypes() {
        return new Class[0];
    }

    @Override
    public String[] getAttributeNames() {
        return new String[0];
    }

    @Override
    public void setAttribute(String key, Object value) {

    }

    @Override
    public String getAttributeValueString(String key) {
        return null;
    }


    public void removeFrom(Buildable parent) {
        GameModule.getGameModule().getToolBar().remove(OKButton);
    }
    @Override
    public HelpFile getHelpFile() {
        return null;
    }

    @Override
    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }


}
