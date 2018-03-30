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

public class OTAContentsChecker extends AbstractConfigurable {

    private JButton OKButton = new JButton();
    private ModuleIntegrityChecker modIntChecker = null;
    private OTAContentsCheckerResults results = null;
    private final String[] finalColumnNames = {"Type","Name", "Faction"};
    private JTable finalTable;
    private JProgressBar progressBar;
    private JButton downloadButton;
    private JOptionPane optionPane;
    private JFrame frame;
    public void addTo(Buildable parent)
    {

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



    /*
     * Build the contents checker window
     */
    private synchronized void ContentsCheckerWindow()
    {


        results = checkAllResults();


       // String msg = modIntChecker.getTestString();;
        frame = new JFrame();

        frame.setResizable(true);
        JPanel panel = new JPanel();
        JLabel spacer;

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

         optionPane = new JOptionPane();
         optionPane.setMessage("Click the download button to download the following images");
       // optionPane.setMessage(msg);
        optionPane.add(panel);
        JDialog dialog = optionPane.createDialog(frame, "Contents Checker");
        dialog.setSize(500,250);

        // new window here
        finalTable = buildFinalTable(results);

        JScrollPane finalPane = new JScrollPane(finalTable);
        panel.add(finalPane, BorderLayout.CENTER);
        downloadButton = new JButton("Download");
        downloadButton.setAlignmentY(0.0F);
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadAll();
            }
        });
        panel.add(downloadButton);


        if(finalTable.getModel().getRowCount() == 0)
        {
            //String okMsg = "All content is up to date";
         //   optionPane.setMessage("All content is up to date");
            optionPane.setMessage("All content is up to date");

            downloadButton.setEnabled(false);
        }
       // optionPane.add(panel);


        dialog.setVisible(true);
        frame.toFront();
        frame.repaint();

        /*
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                OTADownloadWindow.createAndShowGUI(results);
             //   new OTADownloadWindow(results);

            }
        });*/

 //       OTADownloadWindow.createAndShowGUI(results);
    //    new OTADownloadWindow(results);
       // showContentsCheckerWindow(results);

    }

    private void downloadAll()
    {
        boolean needToSaveModule = false;

        // download pilots
        if(results.getMissingPilots().size() > 0) {
            XWOTAUtils.downloadAndSaveImagesFromOTA("pilots", results.getMissingPilotImages());
            needToSaveModule = true;
        }

        // download ships
        if(results.getMissingShips().size() > 0) {
            XWOTAUtils.downloadAndSaveImagesFromOTA("ships", results.getMissingShipImages());
            needToSaveModule = true;
        }

        // download Upgrades
        if(results.getMissingUpgrades().size() > 0) {
            XWOTAUtils.downloadAndSaveImagesFromOTA("upgrades", results.getMissingUpgradeImages());
            needToSaveModule = true;
        }

        // download Conditions
        if(results.getMissingConditions().size() > 0) {
            XWOTAUtils.downloadAndSaveImagesFromOTA("conditions", results.getMissingConditionImages());
            needToSaveModule = true;
        }

        // download actions
        if(results.getMissingActions().size() > 0) {
            XWOTAUtils.downloadAndSaveImagesFromOTA("actions", results.getMissingActionImages());
            needToSaveModule = true;
        }

        // download dial hides
        if(results.getMissingDialHides().size() > 0) {
            XWOTAUtils.downloadAndSaveImagesFromOTA("dial", results.getMissingDialHideImages());
            needToSaveModule = true;
        }

        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();
        ArchiveWriter writer = new ArchiveWriter(fileArchive);


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
                mic.Util.logToChat("Exception occurred saving module");
            }

            // refresh the table
            refreshFinalTable();
        }



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
        model.fireTableDataChanged();


        if(finalTable.getModel().getRowCount() == 0)
        {
            optionPane.setMessage("All content is up to date");
            downloadButton.setEnabled(false);
            optionPane.repaint();
            frame.repaint();
        }

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
            tableRow[2] = "";
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
            tableRow[2] = shipBase.getFaction();
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
            tableRow[2] = dialMask.getFaction();
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
            tableRow[2] = pilot.getFaction();
            tableResults.add(tableRow);
        }

        // upgrades
        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        for(int i=0;i<results.getMissingUpgrades().size();i++)
        {
            upgrade = results.getMissingUpgrades().get(i);
            tableRow = new String[3];
            tableRow[0] = "Upgrade";
            tableRow[1] = MasterUpgradeData.getUpgradeData(upgrade.getXws()).getName();
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
            tableRow[2] = "";
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

    private JTable buildFinalTable(OTAContentsCheckerResults results)
    {
        //{"Type","Name", "Faction"};

        String[][] convertedTableResults = buildTableResultsFromResults(results);
                /*
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
            tableRow[2] = "";
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
            tableRow[2] = shipBase.getFaction();
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
            tableRow[2] = dialMask.getFaction();
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
            tableRow[2] = pilot.getFaction();
            tableResults.add(tableRow);
        }

        // upgrades
        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        for(int i=0;i<results.getMissingUpgrades().size();i++)
        {
            upgrade = results.getMissingUpgrades().get(i);
            tableRow = new String[3];
            tableRow[0] = "Upgrade";
            tableRow[1] = MasterUpgradeData.getUpgradeData(upgrade.getXws()).getName();
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
            tableRow[2] = "";
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
*/
        // build the swing table
        finalTable = new JTable(convertedTableResults,finalColumnNames);
        DefaultTableModel model = new DefaultTableModel(convertedTableResults.length, finalColumnNames.length);
        model.setNumRows(convertedTableResults.length);
        model.setDataVector(convertedTableResults,finalColumnNames);

        finalTable.setModel(model);
        finalTable.getColumnModel().getColumn(0).setPreferredWidth(75);;
        finalTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        finalTable.getColumnModel().getColumn(2).setPreferredWidth(150);


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





    private OTAContentsCheckerResults checkAllResults()
    {
        results = new OTAContentsCheckerResults();

        // perform all checks
        modIntChecker = new ModuleIntegrityChecker();
        results.setPilotResults(modIntChecker.checkPilots());
        results.setShipResults(modIntChecker.checkShips());
        results.setActionResults(modIntChecker.checkActions());
        results.setShipBaseResults(modIntChecker.checkShipBases());
        results.setUpgradeResults(modIntChecker.checkUpgrades());
        results.setConditionResults(modIntChecker.checkConditions());
        results.setDialHideResults(modIntChecker.checkDialHides());
        results.setDialMaskResults(modIntChecker.checkDialMasks());

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
            if(!pilot.getStatus())
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
            if(!upgrade.getStatus())
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
            if(!condition.getStatus() || !condition.getTokenStatus())
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
            if(!ship.getStatus())
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
            if(!action.getStatus())
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
            if(!dialHide.getStatus())
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
            if(!dialMask.getStatus())
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
            if(!shipBase.getStatus())
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
        GameModule.getGameModule().getToolBar().remove(OKButton);
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
