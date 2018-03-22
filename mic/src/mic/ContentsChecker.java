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
public class ContentsChecker  extends AbstractConfigurable {
    private JButton OKButton = new JButton();

    private ArrayList<String> missingPilots;
    private ArrayList<String> missingShips;
    private ArrayList<String> missingActions;
    private ArrayList<String> missingUpgrades;
    private ArrayList<OTAShipBase> missingShipBases;
    private JTable pilotTable;
    private JTable shipTable;
    private JTable actionTable;
    private JTable shipBaseTable;
    private JTable upgradeTable;
    private final String[] pilotColumnNames = {"Faction","Ship","Pilot","Image","Status"};
    private final String[] shipColumnNames = {"XWS","Identifier","Image","Status"};
    private final String[] actionColumnNames = {"Name","Image","Status"};
    private final String[] shipBaseColumnNames = {"Name","XWS","Identifier","Faction","BaseImage","shipImage","Status"};
    private final String[] upgradeColumnNames = {"XWS","Slot","Image","Status"};

    private ModuleIntegrityChecker modIntChecker = null;

    private synchronized void downloadMissingPilots() {

        // download the pilots
        Iterator i = missingPilots.iterator();
        XWImageUtils.downloadAndSaveImagesFromOTA("pilots",missingPilots);


        // refresh the list
        ArrayList<OTAMasterPilots.OTAPilot> pilotResults = modIntChecker.checkPilots();
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

        // refresh the table
        refreshPilotTable();
    }

    private synchronized void downloadMissingUpgrades() {

        // download the upgrades
        Iterator<String> i = missingUpgrades.iterator();
        XWImageUtils.downloadAndSaveImagesFromOTA("upgrades",missingUpgrades);


        // refresh the list
        ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeResults = modIntChecker.checkUpgrades();
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

        // refresh the table
        refreshUpgradeTable();
    }

    private synchronized void downloadMissingActions() {

        // download the actions
        XWImageUtils.downloadAndSaveImagesFromOTA("actions",missingActions );


        // refresh the list
        ArrayList<OTAMasterActions.OTAAction> actionResults = modIntChecker.checkActions();
        missingActions = new ArrayList<String>();

        Iterator<OTAMasterActions.OTAAction> i = actionResults.iterator();
        OTAMasterActions.OTAAction action = null;
        while(i.hasNext())
        {
            action = i.next();
            if(!action.getStatus())
            {
                missingActions.add(action.getImage());
            }
        }

        // refresh the table
        refreshActionTable();
    }

    private synchronized void downloadMissingShips() {

        // download the ships
        XWImageUtils.downloadAndSaveImagesFromOTA("ships",missingShips);

        // refresh the list
        ArrayList<OTAMasterShips.OTAShip> shipResults = modIntChecker.checkShips();
        missingShips = new ArrayList<String>();

        Iterator<OTAMasterShips.OTAShip> i = shipResults.iterator();
        OTAMasterShips.OTAShip ship = null;
        while(i.hasNext())
        {
            ship = i.next();

            if(!ship.getStatus())
            {
                missingShips.add(ship.getImage());
            }
        }

        // refresh the table
        refreshShipTable();
    }


    private synchronized void createMissingShipBases()
    {

        Iterator<OTAShipBase> iter = missingShipBases.iterator();

        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();
        ArchiveWriter writer = new ArchiveWriter(fileArchive);
        OTAShipBase shipBase = null;
        while(iter.hasNext())
        {
            shipBase = iter.next();

            MasterShipData.ShipData shipData = MasterShipData.getShipData(shipBase.getShipXws());
            java.util.List<String> arcs = shipData.getFiringArcs();

            java.util.List<String> actions = shipData.getActions();

            //TODO implement huge ships this
            if(!shipData.getSize().equals("huge")) {

                XWImageUtils.buildBaseShipImage(shipBase.getFaction(), shipBase.getShipXws(), arcs, actions, shipData.getSize(),shipBase.getIdentifier(),shipBase.getshipImageName(), writer);
            }

        }
        try {
            writer.save();
        }catch(IOException e)
        {
            mic.Util.logToChat("Exception occurred saving module");
        }

        // refresh the list
        ArrayList<OTAShipBase> shipBaseResults = modIntChecker.checkShipBases();
        missingShipBases = new ArrayList<OTAShipBase>();
        Iterator<OTAShipBase> i = shipBaseResults.iterator();
        shipBase = null;
        while(i.hasNext())
        {

            shipBase = i.next();
            if(!shipBase.getStatus())
            {

                missingShipBases.add(shipBase);

            }
        }


        // refresh the table
        refreshShipBaseTable();


        shipBaseTable = buildShipBaseTable(shipBaseResults);

    }

    private void refreshShipBaseTable()
    {
        ArrayList<OTAShipBase> shipBaseResults = modIntChecker.checkShipBases();

        String[][] tableResults = new String[shipBaseResults.size()][7];
        OTAShipBase shipBase = null;
        for(int i=0;i<shipBaseResults.size();i++)
        {
            String[] shipBaseLine = new String[7];
            shipBase = shipBaseResults.get(i);

            shipBaseLine[0] = shipBase.getShipName();
            shipBaseLine[1] = shipBase.getShipXws();
            shipBaseLine[2] = shipBase.getIdentifier();
            shipBaseLine[3] = shipBase.getFaction();
            shipBaseLine[4] = shipBase.getShipBaseImageName();
            shipBaseLine[5] = shipBase.getshipImageName();
            shipBaseLine[6] = shipBase.getStatus() ? "Exists":"Not Found";

            tableResults[i] = shipBaseLine;
        }

        DefaultTableModel model = (DefaultTableModel) shipBaseTable.getModel();
        model.setNumRows(tableResults.length);
        model.setDataVector(tableResults,shipBaseColumnNames);
        shipBaseTable.getColumnModel().getColumn(0).setPreferredWidth(125);;
        shipBaseTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        shipBaseTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        shipBaseTable.getColumnModel().getColumn(3).setPreferredWidth(325);
        shipBaseTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        shipBaseTable.getColumnModel().getColumn(5).setPreferredWidth(75);
        shipBaseTable.getColumnModel().getColumn(6).setPreferredWidth(75);
        model.fireTableDataChanged();
    }

    private void refreshPilotTable()
    {
        ArrayList<OTAMasterPilots.OTAPilot> pilotResults = modIntChecker.checkPilots();
        String[][] tableResults = new String[pilotResults.size()][5];

        OTAMasterPilots.OTAPilot pilot = null;
        for(int i=0;i<pilotResults.size();i++)
        {
            String[] pilotLine = new String[5];
            pilot = pilotResults.get(i);
            pilotLine[0] = pilot.getFaction();
            pilotLine[1] = pilot.getShipXws();
            pilotLine[2] = pilot.getPilotXws();
            pilotLine[3] = pilot.getImage();
            pilotLine[4] = pilot.getStatus() ? "Exists":"Not Found";

            tableResults[i] = pilotLine;
        }


        DefaultTableModel model = (DefaultTableModel) pilotTable.getModel();

        model.setNumRows(pilotResults.size());
        model.setDataVector(tableResults,pilotColumnNames);
        pilotTable.getColumnModel().getColumn(0).setPreferredWidth(125);;
        pilotTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        pilotTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        pilotTable.getColumnModel().getColumn(3).setPreferredWidth(325);
        pilotTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        model.fireTableDataChanged();
    }

    private void refreshUpgradeTable()
    {
        ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeResults = modIntChecker.checkUpgrades();
        String[][] tableResults = new String[upgradeResults.size()][4];

        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        for(int i=0;i<upgradeResults.size();i++)
        {
            String[] upgradeLine = new String[4];
            upgrade = upgradeResults.get(i);

            upgradeLine[0] = upgrade.getXws();
            upgradeLine[1] = upgrade.getSlot();
            upgradeLine[2] = upgrade.getImage();
            upgradeLine[3] = upgrade.getStatus() ? "Exists":"Not Found";

            tableResults[i] = upgradeLine;

        }


        DefaultTableModel model = (DefaultTableModel) upgradeTable.getModel();

        model.setNumRows(upgradeResults.size());
        model.setDataVector(tableResults,upgradeColumnNames);
        upgradeTable.getColumnModel().getColumn(0).setPreferredWidth(100);;
        upgradeTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        upgradeTable.getColumnModel().getColumn(2).setPreferredWidth(325);
        upgradeTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        model.fireTableDataChanged();


    }


    private void refreshActionTable()
    {
        ArrayList<OTAMasterActions.OTAAction> actionResults = modIntChecker.checkActions();
        String[][] tableResults = new String[actionResults.size()][3];

        OTAMasterActions.OTAAction action = null;
        for(int i=0;i<actionResults.size();i++)
        {
            String[] actionLine = new String[3];
            action = actionResults.get(i);
            actionLine[0] = action.getName();
            actionLine[1] = action.getImage();
            actionLine[2] = action.getStatus() ? "Exists":"Not Found";


            tableResults[i] = actionLine;
        }


        DefaultTableModel model = (DefaultTableModel) actionTable.getModel();

        model.setNumRows(tableResults.length);
        model.setDataVector(tableResults,actionColumnNames);
        actionTable.getColumnModel().getColumn(0).setPreferredWidth(50);;
        actionTable.getColumnModel().getColumn(1).setPreferredWidth(325);
        actionTable.getColumnModel().getColumn(2).setPreferredWidth(75);
        model.fireTableDataChanged();
    }

    private void refreshShipTable()
    {
        ArrayList<OTAMasterShips.OTAShip> shipResults = modIntChecker.checkShips();

        String[][] tableResults = new String[shipResults.size()][4];
        OTAMasterShips.OTAShip ship = null;
          for(int i=0;i<shipResults.size();i++)
        {
            String[] shipLine = new String[7];
            ship = shipResults.get(i);
            shipLine[0] = ship.getXws();
            shipLine[1] = ship.getIdentifier();
            shipLine[2] = ship.getImage();
            shipLine[3] = ship.getStatus() ? "Exists":"Not Found";

            tableResults[i] = shipLine;
        }

        DefaultTableModel model = (DefaultTableModel) shipTable.getModel();

        model.setNumRows(tableResults.length);
        model.setDataVector(tableResults,shipColumnNames);
        shipTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        shipTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        shipTable.getColumnModel().getColumn(2).setPreferredWidth(325);
        shipTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        model.fireTableDataChanged();
    }


    private synchronized void ContentsCheckerWindow()
    {

        modIntChecker = new ModuleIntegrityChecker();

        ArrayList<OTAMasterPilots.OTAPilot> pilotResults = modIntChecker.checkPilots();
        ArrayList<OTAMasterShips.OTAShip> shipResults = modIntChecker.checkShips();
        ArrayList<OTAMasterActions.OTAAction> actionResults = modIntChecker.checkActions();
        ArrayList<OTAShipBase> shipBaseResults = modIntChecker.checkShipBases();
        ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeResults = modIntChecker.checkUpgrades();

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
            dialog.setSize(1000,750);

        pilotTable = buildPilotTable(pilotResults);
        shipTable = buildShipTable(shipResults);
        actionTable = buildActionTable(actionResults);
        shipBaseTable = buildShipBaseTable(shipBaseResults);
        upgradeTable = buildUpgradeTable(upgradeResults);

        JScrollPane pilotPane = new JScrollPane(pilotTable);
        JScrollPane shipPane = new JScrollPane(shipTable);
        JScrollPane actionPane = new JScrollPane(actionTable);
        JScrollPane shipBasePane = new JScrollPane(shipBaseTable);
        JScrollPane upgradePane = new JScrollPane(upgradeTable);

        // pilots
        panel.add(pilotPane, BorderLayout.CENTER);
        JButton downloadPilotButton = new JButton("Download Pilots");
        downloadPilotButton.setAlignmentY(0.0F);
        downloadPilotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadMissingPilots();
            }
        });
        panel.add(downloadPilotButton);

        // ships
        panel.add(shipPane, BorderLayout.CENTER);
        JButton downloadShipButton = new JButton("Download Ships");
        downloadShipButton.setAlignmentY(0.0F);
        downloadShipButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadMissingShips();
            }
        });
        panel.add(downloadShipButton);

        // actions
        panel.add(actionPane, BorderLayout.CENTER);
        JButton downloadActionButton = new JButton("Download Actions");
        downloadActionButton.setAlignmentY(0.0F);
        downloadActionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadMissingActions();
            }
        });
        panel.add(downloadActionButton);

        // upgrades
        panel.add(upgradePane, BorderLayout.CENTER);
        JButton downloadUpgradeButton = new JButton("Download Upgrades");
        downloadUpgradeButton.setAlignmentY(0.0F);
        downloadUpgradeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadMissingUpgrades();
            }
        });
        panel.add(downloadUpgradeButton);

        // ship bases
        panel.add(shipBasePane, BorderLayout.CENTER);
        JButton createShipBasesButton = new JButton("Create Ship Bases");
        createShipBasesButton.setAlignmentY(0.0F);
        createShipBasesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                createMissingShipBases();
            }
        });
        panel.add(createShipBasesButton);

            dialog.setVisible(true);
            frame.toFront();
            frame.repaint();
    }

    private JTable buildPilotTable(ArrayList<OTAMasterPilots.OTAPilot> pilotResults)
    {
        String[][] tableResults = new String[pilotResults.size()][5];

        OTAMasterPilots.OTAPilot pilot = null;
        for(int i=0;i<pilotResults.size();i++)
        {
            String[] pilotLine = new String[5];
            pilot = pilotResults.get(i);
            pilotLine[0] = pilot.getFaction();
            pilotLine[1] = pilot.getShipXws();
            pilotLine[2] = pilot.getPilotXws();
            pilotLine[3] = pilot.getImage();
            pilotLine[4] = pilot.getStatus() ? "Exists":"Not Found";

            tableResults[i] = pilotLine;
        }

        pilotTable = new JTable(tableResults,pilotColumnNames);
        DefaultTableModel model = new DefaultTableModel(pilotResults.size(), pilotColumnNames.length);
        model.setNumRows(pilotResults.size());
        model.setDataVector(tableResults,pilotColumnNames);

        pilotTable.setModel(model);
        pilotTable.getColumnModel().getColumn(0).setPreferredWidth(125);;
        pilotTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        pilotTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        pilotTable.getColumnModel().getColumn(3).setPreferredWidth(325);
        pilotTable.getColumnModel().getColumn(4).setPreferredWidth(75);

        pilotTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return pilotTable;
    }

    private JTable buildUpgradeTable(ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeResults)
    {
        String[][] tableResults = new String[upgradeResults.size()][4];

        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        for(int i=0;i<upgradeResults.size();i++)
        {
            String[] upgradeLine = new String[4];
            upgrade = upgradeResults.get(i);
            upgradeLine[0] = upgrade.getXws();
            upgradeLine[1] = upgrade.getSlot();
            upgradeLine[2] = upgrade.getImage();
            upgradeLine[3] = upgrade.getStatus() ? "Exists":"Not Found";

            tableResults[i] = upgradeLine;
        }

        upgradeTable = new JTable(tableResults,upgradeColumnNames);
        DefaultTableModel model = new DefaultTableModel(upgradeResults.size(), upgradeColumnNames.length);
        model.setNumRows(upgradeResults.size());
        model.setDataVector(tableResults,upgradeColumnNames);

        upgradeTable.setModel(model);
        upgradeTable.getColumnModel().getColumn(0).setPreferredWidth(100);;
        upgradeTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        upgradeTable.getColumnModel().getColumn(2).setPreferredWidth(325);
        upgradeTable.getColumnModel().getColumn(3).setPreferredWidth(75);

        upgradeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return upgradeTable;
    }

    private JTable buildShipBaseTable(ArrayList<OTAShipBase> shipBaseResults)
    {

        String[][] tableResults = new String[shipBaseResults.size()][7];

        OTAShipBase shipBase = null;
        for(int i=0;i<shipBaseResults.size();i++)
        {
            String[] shipBaseLine = new String[7];
            shipBase = shipBaseResults.get(i);

            shipBaseLine[0] = shipBase.getShipName();
            shipBaseLine[1] = shipBase.getShipXws();
            shipBaseLine[2] = shipBase.getIdentifier();
            shipBaseLine[3] = shipBase.getFaction();
            shipBaseLine[4] = shipBase.getShipBaseImageName();
            shipBaseLine[5] = shipBase.getshipImageName();
            shipBaseLine[6] = shipBase.getStatus() ? "Exists":"Not Found";

            tableResults[i] = shipBaseLine;
        }

        shipBaseTable = new JTable(tableResults,shipBaseColumnNames);
        DefaultTableModel model = new DefaultTableModel(tableResults.length, shipBaseColumnNames.length);
        model.setNumRows(tableResults.length);
        model.setDataVector(tableResults,shipBaseColumnNames);

        shipBaseTable.setModel(model);
        shipBaseTable.getColumnModel().getColumn(0).setPreferredWidth(125);;
        shipBaseTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        shipBaseTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        shipBaseTable.getColumnModel().getColumn(3).setPreferredWidth(325);
        shipBaseTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        shipBaseTable.getColumnModel().getColumn(5).setPreferredWidth(75);
        shipBaseTable.getColumnModel().getColumn(6).setPreferredWidth(75);

        shipBaseTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        model.fireTableDataChanged();
        return shipBaseTable;
    }

    private JTable buildShipTable(ArrayList<OTAMasterShips.OTAShip> shipResults)
    {
        String[][] tableResults = new String[shipResults.size()][4];

        OTAMasterShips.OTAShip ship = null;

        for(int i=0;i<shipResults.size();i++)
        {
            String[] shipLine = new String[5];
            ship = shipResults.get(i);
            shipLine[0] = ship.getXws();
            shipLine[1] = ship.getIdentifier();
            shipLine[2] = ship.getImage();
            shipLine[3] = ship.getStatus() ? "Exists":"Not Found";


            tableResults[i] = shipLine;
        }




        shipTable = new JTable(tableResults,shipColumnNames);
        DefaultTableModel model = new DefaultTableModel(tableResults.length, shipColumnNames.length);
        model.setNumRows(tableResults.length);
        model.setDataVector(tableResults,shipColumnNames);

        shipTable.setModel(model);
        shipTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        shipTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        shipTable.getColumnModel().getColumn(2).setPreferredWidth(325);
        shipTable.getColumnModel().getColumn(3).setPreferredWidth(75);

        shipTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return shipTable;
    }

    private JTable buildActionTable(ArrayList<OTAMasterActions.OTAAction> actionResults)
    {
        String[][] tableResults = new String[actionResults.size()][3];

        OTAMasterActions.OTAAction action = null;

        for(int i=0;i<actionResults.size();i++)
        {

            String[] actionLine = new String[3];
            action = actionResults.get(i);
            actionLine[0] = action.getName();
            actionLine[1] = action.getImage();
            actionLine[2] = action.getStatus() ? "Exists":"Not Found";



            tableResults[i] = actionLine;
        }



        actionTable = new JTable(tableResults,actionColumnNames);
        DefaultTableModel model = new DefaultTableModel(tableResults.length, actionColumnNames.length);
        model.setNumRows(tableResults.length);
        model.setDataVector(tableResults,actionColumnNames);

        actionTable.setModel(model);
        actionTable.getColumnModel().getColumn(0).setPreferredWidth(50);;
        actionTable.getColumnModel().getColumn(1).setPreferredWidth(325);
        actionTable.getColumnModel().getColumn(2).setPreferredWidth(75);

        actionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return actionTable;
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
