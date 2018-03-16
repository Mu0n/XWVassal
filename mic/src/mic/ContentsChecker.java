package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Mic on 12/03/2018.
 */
public class ContentsChecker  extends AbstractConfigurable {
    private JButton OKButton = new JButton();

    private ArrayList<String> missingPilots;
  //  private ArrayList<String> missingArcs;
    private ArrayList<String> missingShips;
    private ArrayList<String> missingActions;
    private ArrayList<String[]> missingShipBases;
    private JTable pilotTable;
  //  private JTable arcTable;
    private JTable shipTable;
    private JTable actionTable;
    private JTable shipBaseTable;
    private final String[] pilotColumnNames = {"Faction","Ship","Pilot","Image","Status"};
 //   private final String[] arcColumnNames = {"Size","Faction","Arc","Image","Status"};
    private final String[] shipColumnNames = {"Name","XWS","Image","Status"};
    private final String[] actionColumnNames = {"Name","Image","Status"};
    private final String[] shipBaseColumnNames = {"Name","XWS","Size","Faction","Image","Status"};
    private ModuleIntegrityChecker modIntChecker = null;

    private synchronized void downloadMissingPilots() {

        // download the pilots
        Iterator i = missingPilots.iterator();
        while(i.hasNext()) {
            String pilotImage = (String)i.next();
            XWImageUtils.downloadAndSaveImageFromOTA("pilots",pilotImage );
        }

        // refresh the list
        String[][] pilotResults = modIntChecker.checkPilots();
        missingPilots = new ArrayList<String>();
        for(int j=0;j<pilotResults.length;j++)
        {
            if(pilotResults[j][4].equals("Not Found")) {
                missingPilots.add(pilotResults[j][3]);
            }
        }

        // refresh the table
        refreshPilotTable(pilotResults);
    }
/*
    private synchronized void downloadMissingArcs() {

        // download the arcs
        Iterator i = missingArcs.iterator();
        while(i.hasNext()) {
            String arcImage = (String)i.next();
            XWImageUtils.downloadAndSaveImageFromOTA("base_firing_arcs",arcImage );
        }

        // refresh the list
        String[][] arcResults = modIntChecker.checkArcs();
        missingArcs = new ArrayList<String>();
        for(int j=0;j<arcResults.length;j++)
        {
            if(arcResults[j][4].equals("Not Found")) {
                missingArcs.add(arcResults[j][3]);
            }
        }

        // refresh the table
        refreshArcTable(arcResults);
    }
*/
    private synchronized void downloadMissingActions() {

        // download the actions
        Iterator i = missingActions.iterator();
        while(i.hasNext()) {
            String actionImage = (String)i.next();
            XWImageUtils.downloadAndSaveImageFromOTA("actions",actionImage );
        }

        // refresh the list
        String[][] actionResults = modIntChecker.checkActions();
        missingActions = new ArrayList<String>();
        for(int j=0;j<actionResults.length;j++)
        {
            if(actionResults[j][2].equals("Not Found")) {
                missingActions.add(actionResults[j][1]);
            }
        }

        // refresh the table
        refreshActionTable(actionResults);
    }

    private synchronized void downloadMissingShips() {

        // download the ships
        Iterator i = missingShips.iterator();

        while(i.hasNext())
        {
          //  String shipXWS = (String)i.next();
          //  MasterShipData.ShipData shipData = MasterShipData.getShipData(shipXWS);
            String shipImage = (String)i.next();
            XWImageUtils.downloadAndSaveImageFromOTA("ships",shipImage);


        }

        // refresh the list
        String[][] shipResults = modIntChecker.checkShips();
        missingShips = new ArrayList<String>();
        for(int j=0;j<shipResults.length;j++)
        {
            if(shipResults[j][3].equals("Not Found")) {
                missingShips.add(shipResults[j][2]);
            }
        }

        // refresh the table
        refreshShipTable(shipResults);
    }


    private synchronized void createMissingShipBases()
    {

        Iterator iter = missingShipBases.iterator();

        while(iter.hasNext())
        {
            String[] missingBase = (String[])iter.next();

            String missingFaction = missingBase[0];
            String missingXWS = missingBase[1];


            //String shipBaseImage = (String)i.next();

            MasterShipData.ShipData shipData = MasterShipData.getShipData(missingXWS);
            java.util.List<String> arcs = shipData.getFiringArcs();

            java.util.List<String> actions = shipData.getActions();

            //TODO fix this
            if(!shipData.getSize().equals("huge")) {

                XWImageUtils.buildBaseShipImage(missingFaction, missingXWS, arcs, actions, shipData.getSize());

            }

        }

        // refresh the list
        String[][] shipBaseResults = modIntChecker.checkShipBases();
        missingShipBases = new ArrayList<String[]>();
        for(int i=0;i<shipBaseResults.length;i++)
        {
            if(shipBaseResults[i][4].equals("Not Found")) {
                String[] missingShipFaction = {shipBaseResults[i][3],shipBaseResults[i][1]};
                missingShipBases.add(missingShipFaction);

            }
        }

        // refresh the table
        refreshShipBaseTable(shipBaseResults);

    }

    private void refreshPilotTable(String[][] pilotResults)
    {

        DefaultTableModel model = (DefaultTableModel) pilotTable.getModel();

        model.setNumRows(pilotResults.length);
        model.setDataVector(pilotResults,pilotColumnNames);
        pilotTable.getColumnModel().getColumn(0).setPreferredWidth(125);;
        pilotTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        pilotTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        pilotTable.getColumnModel().getColumn(3).setPreferredWidth(325);
        pilotTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        model.fireTableDataChanged();
    }
/*
    private void refreshArcTable(String[][] arcResults)
    {

        DefaultTableModel model = (DefaultTableModel) arcTable.getModel();

        model.setNumRows(arcResults.length);
        model.setDataVector(arcResults,arcColumnNames);
        arcTable.getColumnModel().getColumn(0).setPreferredWidth(50);;
        arcTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        arcTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        arcTable.getColumnModel().getColumn(3).setPreferredWidth(325);
        arcTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        model.fireTableDataChanged();
    }*/

    private void refreshActionTable(String[][] actionResults)
    {

        DefaultTableModel model = (DefaultTableModel) actionTable.getModel();

        model.setNumRows(actionResults.length);
        model.setDataVector(actionResults,actionColumnNames);
        actionTable.getColumnModel().getColumn(0).setPreferredWidth(50);;
        actionTable.getColumnModel().getColumn(1).setPreferredWidth(325);
        actionTable.getColumnModel().getColumn(2).setPreferredWidth(75);
        model.fireTableDataChanged();
    }

    private void refreshShipTable(String[][] shipResults)
    {

        DefaultTableModel model = (DefaultTableModel) shipTable.getModel();

        model.setNumRows(shipResults.length);
        model.setDataVector(shipResults,shipColumnNames);
        shipTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        shipTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        shipTable.getColumnModel().getColumn(2).setPreferredWidth(325);
        shipTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        model.fireTableDataChanged();
    }

    private void refreshShipBaseTable(String[][] shipBaseResults)
    {

        DefaultTableModel model = (DefaultTableModel) shipBaseTable.getModel();

        model.setNumRows(shipBaseResults.length);
        model.setDataVector(shipBaseResults,shipBaseColumnNames);
        shipBaseTable.getColumnModel().getColumn(0).setPreferredWidth(125);;
        shipBaseTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        shipBaseTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        shipBaseTable.getColumnModel().getColumn(3).setPreferredWidth(325);
        shipBaseTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        model.fireTableDataChanged();
    }


    private synchronized void ContentsCheckerWindow()
    {

        modIntChecker = new ModuleIntegrityChecker();

        String[][] pilotResults = modIntChecker.checkPilots();
     //   String[][] arcResults = modIntChecker.checkArcs();
        String[][] shipResults = modIntChecker.checkShips();
        String[][] actionResults = modIntChecker.checkActions();
        String[][] shipBaseResults = modIntChecker.checkShipBases();

        // store the missing pilots
        missingPilots = new ArrayList<String>();
        for(int i=0;i<pilotResults.length;i++)
        {
            if(pilotResults[i][4].equals("Not Found")) {
                missingPilots.add(pilotResults[i][3]);
            }
        }
/*
        // store the missing arcs
        missingArcs = new ArrayList<String>();
        for(int i=0;i<arcResults.length;i++)
        {
            if(arcResults[i][4].equals("Not Found")) {
                missingArcs.add(arcResults[i][3]);
            }
        }
*/
        // store the missing ships
        missingShips = new ArrayList<String>();
        for(int i=0;i<shipResults.length;i++)
        {
            if(shipResults[i][3].equals("Not Found")) {
                missingShips.add(shipResults[i][2]);

            }
        }

        // store the missing actions
        missingActions = new ArrayList<String>();
        for(int i=0;i<actionResults.length;i++)
        {
            if(actionResults[i][2].equals("Not Found")) {
                missingActions.add(actionResults[i][1]);

            }
        }

        // store the missing ship bases
        missingShipBases = new ArrayList<String[]>();
        for(int i=0;i<shipBaseResults.length;i++)
        {
            if(shipBaseResults[i][5].equals("Not Found")) {
                String[] missingShipFaction = {shipBaseResults[i][3],shipBaseResults[i][1]};
                missingShipBases.add(missingShipFaction);

            }
        }

        String msg = modIntChecker.getTestString();;
            JFrame frame = new JFrame();
          //  frame.setSize(1000,1000);
            frame.setResizable(true);
            JPanel panel = new JPanel();
            JLabel spacer;
          //  panel.setMinimumSize(new Dimension(5000, 3500));

            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JOptionPane optionPane = new JOptionPane();
            optionPane.setMessage(msg);
            //optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
            optionPane.add(panel);
            JDialog dialog = optionPane.createDialog(frame, "Contents Checker");
            dialog.setSize(1000,750);

        pilotTable = buildPilotTable(pilotResults);
      //  arcTable = buildArcTable(arcResults);
        shipTable = buildShipTable(shipResults);
        actionTable = buildActionTable(actionResults);
        shipBaseTable = buildShipBaseTable(shipBaseResults);

        JScrollPane pilotPane = new JScrollPane(pilotTable);
    //    JScrollPane arcPane = new JScrollPane(arcTable);
        JScrollPane shipPane = new JScrollPane(shipTable);
        JScrollPane actionPane = new JScrollPane(actionTable);
        JScrollPane shipBasePane = new JScrollPane(shipBaseTable);

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
/*
        // arcs
        panel.add(arcPane, BorderLayout.CENTER);
        JButton downloadArcButton = new JButton("Download Arcs");
        downloadArcButton.setAlignmentY(0.0F);
        downloadArcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadMissingArcs();
            }
        });
        panel.add(downloadArcButton);
*/
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

    private JTable buildPilotTable(String[][] pilotResults)
    {
        pilotTable = new JTable(pilotResults,pilotColumnNames);
        DefaultTableModel model = new DefaultTableModel(pilotResults.length, pilotColumnNames.length);
        model.setNumRows(pilotResults.length);
        model.setDataVector(pilotResults,pilotColumnNames);

        pilotTable.setModel(model);
        pilotTable.getColumnModel().getColumn(0).setPreferredWidth(125);;
        pilotTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        pilotTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        pilotTable.getColumnModel().getColumn(3).setPreferredWidth(325);
        pilotTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        // table.setSize(300,300);
        // Turn off JTable's auto resize so that JScrollPane will show a horizontal
        // scroll bar.
        pilotTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return pilotTable;
    }

    private JTable buildShipBaseTable(String[][] shipBaseResults)
    {
        shipBaseTable = new JTable(shipBaseResults,shipBaseColumnNames);
        DefaultTableModel model = new DefaultTableModel(shipBaseResults.length, shipBaseColumnNames.length);
        model.setNumRows(shipBaseResults.length);
        model.setDataVector(shipBaseResults,shipBaseColumnNames);

        shipBaseTable.setModel(model);
        shipBaseTable.getColumnModel().getColumn(0).setPreferredWidth(125);;
        shipBaseTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        shipBaseTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        shipBaseTable.getColumnModel().getColumn(3).setPreferredWidth(325);
        shipBaseTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        // table.setSize(300,300);
        // Turn off JTable's auto resize so that JScrollPane will show a horizontal
        // scroll bar.
        shipBaseTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return shipBaseTable;
    }

    private JTable buildShipTable(String[][] shipResults)
    {
        shipTable = new JTable(shipResults,shipColumnNames);
        DefaultTableModel model = new DefaultTableModel(shipResults.length, shipColumnNames.length);
        model.setNumRows(shipResults.length);
        model.setDataVector(shipResults,shipColumnNames);

        shipTable.setModel(model);
       // shipTable.getColumnModel().getColumn(0).setPreferredWidth(50);;
        shipTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        shipTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        shipTable.getColumnModel().getColumn(2).setPreferredWidth(325);
        shipTable.getColumnModel().getColumn(3).setPreferredWidth(75);

        shipTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return shipTable;
    }
/*
    private JTable buildArcTable(String[][] arcResults)
    {
        arcTable = new JTable(arcResults,arcColumnNames);
        DefaultTableModel model = new DefaultTableModel(arcResults.length, arcColumnNames.length);
        model.setNumRows(arcResults.length);
        model.setDataVector(arcResults,arcColumnNames);

        arcTable.setModel(model);
        arcTable.getColumnModel().getColumn(0).setPreferredWidth(50);;
        arcTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        arcTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        arcTable.getColumnModel().getColumn(3).setPreferredWidth(325);
        arcTable.getColumnModel().getColumn(4).setPreferredWidth(75);

        arcTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return arcTable;
    }
*/
    private JTable buildActionTable(String[][] actionResults)
    {
        actionTable = new JTable(actionResults,actionColumnNames);
        DefaultTableModel model = new DefaultTableModel(actionResults.length, actionColumnNames.length);
        model.setNumRows(actionResults.length);
        model.setDataVector(actionResults,actionColumnNames);

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
