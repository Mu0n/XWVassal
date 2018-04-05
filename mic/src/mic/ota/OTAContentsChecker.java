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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class OTAContentsChecker extends AbstractConfigurable {

    private static Map<String,String> fullFactionNames = ImmutableMap.<String, String>builder()
            .put("galacticempire","Galactic Empire")
            .put("firstorder","First Order")
            .put("rebelalliance","Rebel Alliance")
            .put("resistance","Resistance")
            .put("scumandvillainy","Scum and Villainy")
            .build();

    private JButton OKButton = new JButton();
    private ModuleIntegrityChecker modIntChecker = null;
    private OTAContentsCheckerResults results = null;
    private final String[] finalColumnNames = {"Type","Name", "Variant"};
    private JTable finalTable;
    private JButton downloadButton;
    private JFrame frame;
    private JLabel jlabel;
    private boolean downloadAll = false;

    static final String modeListURL = "https://raw.githubusercontent.com/Mu0n/XWVassal-website/master/modeList.json";


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
        finalTable = buildFinalTable(results);

        // create the frame
        frame = new JFrame();
        frame.setResizable(true);

        // create the panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // create the label
        jlabel = new JLabel();


        // add the results table
        JScrollPane finalPane = new JScrollPane(finalTable);

        // ALL checkbox
        final JCheckBox allButton = new JCheckBox("Download all content");
        allButton.setSelected(false);
        allButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                //TODO
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
        downloadButton.setAlignmentY(0.0F);
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadButton.setEnabled(false);

                int answer =  JOptionPane.showConfirmDialog(null, "This might take several minutes. During the download, Vassal will be unresponsive. \nDo you want to continue?", "Do you want to proceed?", JOptionPane.YES_NO_OPTION);

                if(answer == JOptionPane.YES_OPTION)
                {

                    downloadAll();
                    allButton.setSelected(false);
                }else{
                    downloadButton.setEnabled(true);
                }
                /*
                if(downloadAll)
                {

                    int answer =  JOptionPane.showConfirmDialog(null,
                            "This might take several minutes.  Do you want to continue?", "Do you want to proceed?", JOptionPane.YES_NO_OPTION);
                    if(answer == JOptionPane.YES_OPTION)
                    {

                        downloadAll();
                        allButton.setSelected(false);
                    }else{
                        downloadButton.setEnabled(true);
                    }
                }else{

                    downloadAll();


                }*/

            }
        });

        // cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                frame.dispose();
            }
        });
        downloadButton.setAlignmentY(0.0F);

        // game mode list
        JLabel sourceExplanationLabel = new JLabel("Select the game mode here:");

        //if it can't access the list of sources on the web, make it base game by default
        String[] listOfXwingDataSources = {
                "Base Game"
        };
        //TO DO fetch that list from the web just like in AutoSquadSpawn's dialog window
        JComboBox aComboBox = new JComboBox(listOfXwingDataSources);


        // add the components
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        panel.add(jlabel,c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        panel.add(finalPane,c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(downloadButton,c);

        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(allButton,c);

        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(cancelButton,c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        panel.add(sourceExplanationLabel);

        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 2;
        panel.add(aComboBox);

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        frame.add(panel, BorderLayout.PAGE_START);

        jlabel.setText("Click the download button to download the following images");
       if(finalTable.getModel().getRowCount() == 0)
       {
           jlabel.setText("All content is up to date");
            downloadButton.setEnabled(false);
        }else{

            downloadButton.setEnabled(true);
        }

        panel.setOpaque(true); // content panes must be opaque
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.toFront();


    }


    private void downloadAll()
    {

        boolean needToSaveModule = false;

        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();
        ArchiveWriter writer = new ArchiveWriter(fileArchive);

        // download pilots
        if(results.getMissingPilots().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("pilots", results.getMissingPilotImages(),writer);
            needToSaveModule = true;
        }

        // download ships
        if(results.getMissingShips().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("ships", results.getMissingShipImages(),writer);
            needToSaveModule = true;
        }

        // download Upgrades
        if(results.getMissingUpgrades().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("upgrades", results.getMissingUpgradeImages(),writer);
            needToSaveModule = true;
        }

        // download Conditions
        if(results.getMissingConditions().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("conditions", results.getMissingConditionImages(),writer);
            needToSaveModule = true;
        }

        // download actions
        if(results.getMissingActions().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("actions", results.getMissingActionImages(),writer);
            needToSaveModule = true;
        }

        // download dial hides
        if(results.getMissingDialHides().size() > 0) {
            XWOTAUtils.downloadImagesFromOTA("dial", results.getMissingDialHideImages(),writer);
            needToSaveModule = true;
        }

        if(needToSaveModule)
        {
            try {
                writer.save();
                needToSaveModule = false;
            } catch (IOException e) {
                mic.Util.logToChat("Exception occurred saving module");
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
       // frame.repaint();
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
            if(ship.getIdentifier().equalsIgnoreCase("Standard")) {
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
            if(!pilot.getStatus() || downloadAll)
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
            if(!upgrade.getStatus() || downloadAll)
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
            if(!condition.getStatus() || !condition.getTokenStatus() || downloadAll)
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
            if(!ship.getStatus() || downloadAll)
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
            if(!shipBase.getStatus() || downloadAll)
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
