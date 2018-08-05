package mic;


import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;

import static mic.Util.*;


/**
 * Created by Mic on 2018-07-27.
 *
 * This creates a small Spawn 2.0 button in every player's window and can only be activated by the corresponding signed player, like always
 * New to this edition - a crude, data populated squad builder where you can add/clone ship+pilot combinations and add an arbitrarily large amount of upgrades for each, with buttons for deletion for the 2+ instances of each
 * (e.g. first ship+pilot does not have a delete button, but subsequent ship+pilots do. Same goes for upgrades, per ship+pilot)
 *
 * TO DO:
 * 1) make it generate a XWS2 squad json when the UI selections have been made in the comboboxes
 * 2) make it spawn a squad (starting with placeholder items first, next with cards)
 * 3) offer to save a XWS2 squad locally to disk for now, since the early 2.0 builders might not have squad saving management
 */
public class AutoSquadSpawn2e extends AbstractConfigurable {

    private static java.util.Map<String, String> xwingdata2ToYasb2 = ImmutableMap.<String, String>builder()
            .put("Rebel Alliance","rebelalliance")
            .put("Galactic Empire","galacticempire")
            .put("Scum and Villainy","scumandvillainy")
            .build();

//keepsake for this whole class' behavior inside the player window - they must be kept track so they can be removed safely later
    private List<JButton> spawnButtons = Lists.newArrayList();



    private void spawnPiece(GamePiece piece, Point position, Map playerMap) {
        Command placeCommand = playerMap.placeOrMerge(piece, position);
        placeCommand.execute();
        GameModule.getGameModule().sendAndLog(placeCommand);
    }

    //Main interface via a Java Swing JFrame. The complexity has outgrown an InputDialog - we now use ActionListener on the JComboBox and JButton to react to the user commands
    private void spawnForPlayer(final int playerIndex) {
        final List<String> factionsWanted = Lists.newArrayList();

        Map playerMap = getPlayerMap(playerIndex);
        if (playerMap == null) {
            logToChat("Unexpected error, couldn't find map for player side " + playerIndex);
            return;
        }

        XWPlayerInfo playerInfo = getCurrentPlayer();
        if (playerInfo.getSide() != playerIndex) {
            JOptionPane.showMessageDialog(playerMap.getView(), "Cannot spawn squads for other players");
            return;
        }

        final JFrame frame = new JFrame();
        final JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        JPanel sourceInfoPanel = new JPanel();
        sourceInfoPanel.setLayout(new BoxLayout(sourceInfoPanel, BoxLayout.Y_AXIS));
        sourceInfoPanel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
        JLabel sourceExplanationLabel = new JLabel("This is a rough preliminary version of the 2nd edition squad autospawn window.");

        sourceInfoPanel.add(sourceExplanationLabel);

        rootPanel.add(sourceInfoPanel);
        rootPanel.add(Box.createRigidArea(new Dimension(0,8)));
        rootPanel.add(new JSeparator());
        rootPanel.add(Box.createRigidArea(new Dimension(0,8)));

        final JCheckBox empireCheck = new JCheckBox("Empire");
        empireCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(empireCheck.isSelected()) factionsWanted.add("Galactic Empire");
                else factionsWanted.remove("Galactic Empire");
            }
        });

        final JCheckBox allianceCheck = new JCheckBox("Alliance");
        allianceCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(allianceCheck.isSelected()) factionsWanted.add("Rebel Alliance");
                else factionsWanted.remove("Rebel Alliance");
            }
        });

        final JCheckBox scumCheck = new JCheckBox("Scum and Villainy");
        scumCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(scumCheck.isSelected()) factionsWanted.add("Scum and Villainy");
                else factionsWanted.remove("Scum and Villainy");
            }
        });



        JButton builderButton = new JButton("Internal Squad Builder");
        builderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(factionsWanted.isEmpty()) {
                    JFrame warnFrame = new JFrame();
                    JOptionPane.showMessageDialog(warnFrame, "Check at least 1 faction before opening the internal builder");
                    return;
                }
                internalSquadBuilder(playerIndex, factionsWanted);
            }
        });

        JPanel builderPanel = new JPanel();
        builderPanel.setLayout(new BoxLayout(builderPanel,BoxLayout.X_AXIS));
        builderPanel.add(empireCheck);
        builderPanel.add(allianceCheck);
        builderPanel.add(scumCheck);
        builderPanel.add(builderButton);

        rootPanel.add(builderPanel);

        frame.add(rootPanel);
        frame.setSize(900,500);
        frame.setTitle("2.0 Squad Autospawn for player " + Integer.toString(playerIndex));
        frame.pack();
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();


    }

    private void internalSquadBuilder(int playerIndex, final List<String> factionsWanted){
        final List<XWS2Pilots> allShips = XWS2Pilots.loadFromRemote();
        final List<XWS2Upgrades> allUpgrades = XWS2Upgrades.loadFromRemote();

        final JFrame frame = new JFrame();
        //Panel which will include a Combo box for selecting the source of the xwing-data to use
        final JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        JPanel sourcePanel = new JPanel();
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));
        JPanel sourceInfoPanel = new JPanel();
        sourceInfoPanel.setLayout(new BoxLayout(sourceInfoPanel, BoxLayout.Y_AXIS));

        JPanel oneShipPanel = new JPanel();

        JLabel sourceExplanationLabel = new JLabel("You can build a list in this popup window. Its validity will not be checked. Illegal upgrades may be chosen and cross faction is possible.");
        final JTextArea entryArea = new JTextArea("Enter a valid XWS squad here.");
        entryArea.setPreferredSize(new Dimension(850,150));
        entryArea.setMaximumSize(new Dimension(850,150));
        entryArea.setLineWrap(true);
        entryArea.setAutoscrolls(true);

        final JComboBox ShipComboList = new JComboBox();
        ShipComboList.setToolTipText("Select a ship.");
        ShipComboList.addItem("Select a ship.");

        final JComboBox PilotComboList = new JComboBox();
        PilotComboList.setToolTipText("Select a pilot");
        populateShipComboBox(ShipComboList, factionsWanted, allShips);

        ShipComboList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                PilotComboList.removeAllItems();
                for(XWS2Pilots ship : allShips){
                    if(ship.getName().equals(ShipComboList.getSelectedItem()))
                    {
                        for(XWS2Pilots.Pilot2e pilot : ship.getPilots())
                        {
                            PilotComboList.addItem(pilot.getName());
                        }
                    }
                }
            }
        });
        final JPanel upgradeWholeBoxForShipPanel = new JPanel();
        upgradeWholeBoxForShipPanel.setLayout(new BoxLayout(upgradeWholeBoxForShipPanel, BoxLayout.Y_AXIS));
        addUpgradeEntry(true, upgradeWholeBoxForShipPanel, frame, allUpgrades);

        JButton addShipButton = new JButton("Add Ship");
        addShipButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {
            copyOrCloneShipButtonBehavior(factionsWanted, false, ShipComboList, PilotComboList, rootPanel, frame, allShips, allUpgrades);
        }
        });
        JButton cloneShipButton = new JButton("Clone Ship");
        cloneShipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyOrCloneShipButtonBehavior(factionsWanted, true, ShipComboList, PilotComboList, rootPanel, frame, allShips, allUpgrades);
            }
        });


        sourcePanel.add(sourceExplanationLabel);

        //make it editable further down the line once it's properly tested
        //aComboBox.setEditable(true);
        JPanel explanationPanel = new JPanel();
        explanationPanel.setLayout(new BoxLayout(explanationPanel, BoxLayout.Y_AXIS));
        explanationPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rootPanel.add(sourcePanel);
        rootPanel.add(sourceInfoPanel);
        rootPanel.add(Box.createRigidArea(new Dimension(0,8)));
        rootPanel.add(new JSeparator());
        rootPanel.add(Box.createRigidArea(new Dimension(0,8)));
        rootPanel.add(explanationPanel);

        rootPanel.add(entryArea);

        JButton createXWS2Button = new JButton("Export to XWS");
        createXWS2Button.setToolTipText("XWS is a community-defined text format used by squad builders (web, apps, etc.) in order to exchange squads.");
        createXWS2Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String theFaction = "mixed";
                if (factionsWanted.size() == 1 && xwingdata2ToYasb2.containsKey(factionsWanted.get(0).toString())) {
                    theFaction = xwingdata2ToYasb2.get(factionsWanted.get(0).toString());
                }
                generateXWS(rootPanel, entryArea, theFaction);
            }
        });
        JButton validateButton = new JButton("Spawn List");
        validateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                XWSList xwsList = new XWSList();
                xwsList = loadListFromRawJson(entryArea.getText());
                try{
                validateList(xwsList);
                } catch (Exception exc) {
                    logToChat("Unable to load raw JSON list '%s': %s", entryArea.getText(), exc.toString());
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        buttonPanel.add(createXWS2Button);
        buttonPanel.add(validateButton);

        rootPanel.add(buttonPanel);

        oneShipPanel.add(ShipComboList);
        oneShipPanel.add(PilotComboList);
        oneShipPanel.add(addShipButton);
        oneShipPanel.add(cloneShipButton);

        rootPanel.add(oneShipPanel);
        rootPanel.add(upgradeWholeBoxForShipPanel);

        frame.add(rootPanel);
        frame.setSize(900,500);
        frame.setTitle("2.0 Squad Autospawn for player " + Integer.toString(playerIndex));
        frame.pack();
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
    }

    private void generateXWS(JPanel rootPanel, JTextArea entryArea, String factionString) {
        String output = "{\"description\":\"\",\"faction\":\""+factionString+"\",\"name\":\"New Squadron\",\"pilots\":[{";

        List<ReadShipInfo> stuffToXWS = Lists.newArrayList();

        //start at rootpanel. The ship+pilot is going to be under a JPanel
        //their upgrades are going to be under a JPanel/JPanel
        ReadShipInfo currentShip = new ReadShipInfo();

        boolean aShipWasDetected = false;

        for(Component c : rootPanel.getComponents())
        {
            //a JPanel is found - will probably find a ship
            if(c.getClass().toString().equals(JPanel.class.toString())){
                int comboBoxSeenCount = 0;
                int upgradeSeenCount = 0;
                //check for a ship under rootPanel/JPanel hopefully oneShipPanel, or wholeUpgradePanel
                for(Component d : ((JPanel)c).getComponents()){
                    //found a combobox. might be ship or pilot.
                    if(d.getClass().toString().equals(JComboBox.class.toString()))
                    {
                        //the ship hasn't been selected, so get out of this rootPanel/JPanel loop
                        if(((JComboBox) d).getSelectedItem().equals("Select a ship.")) break;
                        else {
                            if(comboBoxSeenCount==0) {
                                if(aShipWasDetected){
                                    //looped to new ship
                                    stuffToXWS.add(currentShip);
                                    currentShip = new ReadShipInfo();
                                    aShipWasDetected = false;
                                }
                                comboBoxSeenCount = 1;
                                currentShip.setTypeName(Canonicalizer.getCleanedName((((JComboBox) d).getSelectedItem()).toString()));
                            }
                            else if(comboBoxSeenCount== 1)
                            {
                                comboBoxSeenCount = 2;
                                currentShip.setShipName(Canonicalizer.getCleanedName((((JComboBox) d).getSelectedItem()).toString()));
                                aShipWasDetected = true;
                                comboBoxSeenCount=0;
                            }
                        }
                    }
                    //getting to rootPanel/JPanela series of upgrades wholeUpgradePanel/anUpgradePanel
                    else if(d.getClass().toString().equals(JPanel.class.toString()))
                    {
                        String detectedType = "";
                        String detectedUpg = "";

                        for(Component e : ((JPanel) d).getComponents()){
                            if(e.getClass().toString().equals(JComboBox.class.toString()))
                            {
                                if(((JComboBox) e).getSelectedItem().equals("Select Upgrade Type.")) break;
                                else {
                                    if(upgradeSeenCount==0){
                                        detectedType = Canonicalizer.getCleanedName((((JComboBox) e).getSelectedItem()).toString());
                                        upgradeSeenCount=1;
                                    }else if(upgradeSeenCount==1){
                                        detectedUpg = Canonicalizer.getCleanedName((((JComboBox) e).getSelectedItem()).toString());
                                        upgradeSeenCount=2;
                                    }
                                    if(upgradeSeenCount==2){
                                        currentShip.addUpgrade(detectedType, detectedUpg);
                                        upgradeSeenCount=0;
                                    }
                                }
                            }
                        } //end of the for loop of a series of upgrades, maybe found some, maybe didn't.
                        //gets only 1 upgrade per ship HERE
                    } //end if of a series of upgrades, maybe found some, maybe didn't.
                    //makes too many ship entries HERE

                }//end of for loop
            } //end of rootPanel/JPanel (of a ship)
        }//end of rootPanel

//adds the last ship
        if(aShipWasDetected){
            //last ship to add at the end of everything
            stuffToXWS.add(currentShip);
        }

        for(int i=0; i< stuffToXWS.size(); i++){ //parse all ship/pilot entries
            String shipString ="\"ship\":\"" + stuffToXWS.get(i).getShipType() + "\",";
            String pilotString = "\"name\":\"" + stuffToXWS.get(i).getShipName() + "\",";
            String upgradesStartString = "\"upgrades\":{";
            output+= shipString + pilotString + upgradesStartString;

            for(int j=0; j<stuffToXWS.get(i).getUpgradeBins().size(); j++) //parse all upgrade types
            {
                String upgradeTypeString = "\""+ stuffToXWS.get(i).getUpgradeBins().get(j).getType() +"\":[";
                output += upgradeTypeString;
                for(int k=0; k<stuffToXWS.get(i).getUpgradeBins().get(j).getUpgrades().size(); k++){ // parse upgrades within a type
                    String upgradeString = "\""+stuffToXWS.get(i).getUpgradeBins().get(j).getUpgrades().get(k)+"\"";
                    output+=upgradeString;
                    if(k!=stuffToXWS.get(i).getUpgradeBins().get(j).getUpgrades().size()-1) output+=","; //not the last upgrade in an upg type
                }
                output+="]"; //marks the end of an upgrade type
                if(j!=stuffToXWS.get(i).getUpgradeBins().size()-1) output+=","; //not the last upgrade type in the upgrades
            }
            String upgradesEndString = "}"; //marks the end of upgrades
            output += upgradesEndString;
            output += "}"; //marks the end of a ship/pilot entry
            if(i!=stuffToXWS.size()-1) output+= ",{"; //not the last ship/pilot entry
        }
        output+="],\"vendor\":{\"yasb\":{\"builder\":\"(Yet Another) X-Wing Miniatures Squad Builder\",\"builder_url\":\"https://raithos.github.io/\",\"link\":\"https://raithos.github.io/?f=Galactic%20Empire&d=v4!s!168:-1,10,-1,-1,-1,-1:-1:-1:;217:116,-1:-1:-1:&sn=New%20Squadron&obs=\"}},\"version\":\"0.3.0\"}";

        entryArea.setText(output);
    }

    //Helper method that will populate the leftmost combobox for an upgrade - lists the types of upgrades (should be fairly stable)
    private void populateUpgradeTypes(JComboBox upgradeTypesComboBox, List<XWS2Upgrades> allUpgrades) {
        List<String> upgradeTypesSoFar = Lists.newArrayList();
        for(XWS2Upgrades up : allUpgrades)
        {
            for(XWS2Upgrades.side side : up.getSides())
            {
                if(upgradeTypesSoFar.contains(side.getType()) == false) upgradeTypesSoFar.add(side.getType());
            }
        }
        for(String s : upgradeTypesSoFar)
        {
            upgradeTypesComboBox.addItem(s);
        }
    }


    //helper method that adds a panel containing 2 new comboboxes (upgrade type + upgrade within that type), the [x] delete button. Should be used as well by the Add/clone Pilot method
    private void addUpgradeEntry(boolean wantAddButtonAsFirstUpgrade, final JPanel theUpgPanelHolderVert, final JFrame frame, final List<XWS2Upgrades> allUpgrades) {
        final JPanel anUpgradePanel = new JPanel();
        anUpgradePanel.setLayout(new BoxLayout(anUpgradePanel, BoxLayout.X_AXIS));
        anUpgradePanel.add(Box.createRigidArea(new Dimension(25,0)));
        final JComboBox upgradeTypesComboBox = new JComboBox();
        upgradeTypesComboBox.addItem("Select Upgrade Type.");
        populateUpgradeTypes(upgradeTypesComboBox, allUpgrades);

        final JComboBox upgradesComboBox = new JComboBox();

        upgradeTypesComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                upgradesComboBox.removeAllItems();
                for(XWS2Upgrades ups : allUpgrades){
                    if(upgradeTypesComboBox.getSelectedItem().equals(ups.getSides().get(0).getType())) upgradesComboBox.addItem(ups.getName());
                }
            }
        });

        JButton addUpg = new JButton("Add");
        addUpg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUpgradeEntry(false, theUpgPanelHolderVert, frame, allUpgrades);
            }
        });
        JButton remUpg = new JButton("[X]");
        remUpg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(Component c : theUpgPanelHolderVert.getComponents())
                {
                    if((Component)anUpgradePanel == c) theUpgPanelHolderVert.remove(c);
                }
                frame.setSize(frame.getWidth(), frame.getHeight() - anUpgradePanel.getHeight());
                frame.pack();
                theUpgPanelHolderVert.invalidate();
                frame.invalidate();
            }
        });
        anUpgradePanel.add(upgradeTypesComboBox);
        anUpgradePanel.add(upgradesComboBox);
        if(wantAddButtonAsFirstUpgrade == false) anUpgradePanel.add(remUpg);
        else anUpgradePanel.add(addUpg);

        theUpgPanelHolderVert.add(anUpgradePanel);
        frame.setSize(new Dimension(frame.getWidth(), frame.getHeight() + anUpgradePanel.getHeight()));
        frame.validate();
        frame.invalidate();
        frame.pack();
    }


    private void populateShipComboBox(JComboBox shipComboBox, final List<String> factionsWanted, List<XWS2Pilots> allShips)
    {
        for(XWS2Pilots ship : allShips)
        {
            //if(ship.getFaction().equals(factionsWanted)) shipComboBox.addItem(ship.getName());
            if(factionsWanted.contains(ship.getFaction())) shipComboBox.addItem(ship.getName());
        }
    }
    //Reacts to both "Add Ship" and "Clone Ship" buttons
    private void copyOrCloneShipButtonBehavior(final List<String> factionsWanted, final boolean wantCloning, final JComboBox toCopyShip, final JComboBox toCopyPilot, final JPanel rootPanel, final JFrame frame, final List<XWS2Pilots> allShips, final List<XWS2Upgrades> allUpgrades) {
        if(toCopyShip.getSelectedItem().toString().equals("Select a ship.")) {
            JFrame warnFrame = new JFrame();
            JOptionPane.showMessageDialog(warnFrame, "Please select a ship and a pilot before cloning.");
            return;
        }
        final JComboBox empireShipComboList = new JComboBox();
        empireShipComboList.setToolTipText("Select a ship.");
        empireShipComboList.addItem("Select a ship.");

        populateShipComboBox(empireShipComboList, factionsWanted, allShips);
        if(wantCloning==true){
            empireShipComboList.setSelectedItem(toCopyShip.getSelectedItem());
        }

        final JComboBox empirePilotComboList = new JComboBox();
        empirePilotComboList.setToolTipText("Select a pilot");
        empirePilotComboList.addItem("");
        for(XWS2Pilots ship : allShips)
        {
            if(ship.getName().equals(empireShipComboList.getSelectedItem()))
            {
                for(XWS2Pilots.Pilot2e pilot : ship.getPilots())
                {
                    empirePilotComboList.addItem(pilot.getName());
                }
            }
        }
        if(wantCloning==true){
            empirePilotComboList.setSelectedItem(toCopyPilot.getSelectedItem());
        }

        empireShipComboList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                empirePilotComboList.removeAllItems();
                for(XWS2Pilots ship : allShips){
                    if(ship.getName().equals(empireShipComboList.getSelectedItem()))
                    {
                        for(XWS2Pilots.Pilot2e pilot : ship.getPilots())
                        {
                            empirePilotComboList.addItem(pilot.getName());
                        }
                    }
                }

            }
        });


        final JPanel anotherShipPanel = new JPanel();

        anotherShipPanel.add(empireShipComboList);
        anotherShipPanel.add(empirePilotComboList);

        final JButton cloneButton = new JButton("Clone Ship");
        final JButton removeButton = new JButton("Remove Ship");


        final JPanel anotherUpgradeWholeThing = new JPanel();
        anotherUpgradeWholeThing.setLayout(new BoxLayout(anotherUpgradeWholeThing, BoxLayout.Y_AXIS));
        boolean wantFirstUpgradeEntryWithAddButton = true;
        addUpgradeEntry(wantFirstUpgradeEntryWithAddButton, anotherUpgradeWholeThing,frame,allUpgrades);


        cloneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyOrCloneShipButtonBehavior(factionsWanted,true, empireShipComboList,empirePilotComboList, rootPanel, frame, allShips, allUpgrades);
            }
        });
        removeButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {
            for(Component c : rootPanel.getComponents())
            {
                if((Component)anotherShipPanel == c) rootPanel.remove(c);
                if((Component)anotherUpgradeWholeThing == c) rootPanel.remove(c);
            }
            frame.setSize(frame.getWidth(), frame.getHeight() - anotherShipPanel.getHeight());
            frame.pack();
            rootPanel.invalidate();
            frame.invalidate();
        }
        });

        anotherShipPanel.add(cloneButton);
        anotherShipPanel.add(removeButton);

        rootPanel.add(anotherShipPanel);
        rootPanel.add(anotherUpgradeWholeThing);
        rootPanel.setSize(new Dimension(rootPanel.getWidth(), rootPanel.getHeight() + anotherUpgradeWholeThing.getHeight()));
        anotherUpgradeWholeThing.updateUI();
        rootPanel.updateUI();
        frame.setSize(new Dimension(frame.getWidth(), frame.getHeight() + anotherUpgradeWholeThing.getHeight()));
        frame.validate();
        frame.invalidate();
        frame.pack();
    }



    public static class ReadShipInfo{
        private String shipName="";
        private String shipType="";

        private List<ReadUpgradesInfo> upgradeBins = Lists.newArrayList();

        public ReadShipInfo(){}

        public String getShipName() { return shipName; }
        public String getShipType() { return shipType; }
        public List<ReadUpgradesInfo> getUpgradeBins() { return upgradeBins; }

        public void setShipName(String name) { this.shipName = name; }
        public void setTypeName(String type) { this.shipType = type; }
        public void addUpgrade(String type, String upgrade){
            boolean notFound = true;
            for(ReadUpgradesInfo bin : upgradeBins)
            {
                if(bin.getType().equals(type)){
                    bin.addUpgrade(upgrade);
                    notFound = false;
                }
            }
            if(notFound == true){
                upgradeBins.add(new ReadUpgradesInfo(type, upgrade));
            }
        }
    }

    public static class ReadUpgradesInfo{
        private String type="";
        private List<String> upgrades = Lists.newArrayList();

        public ReadUpgradesInfo(){}
        public ReadUpgradesInfo(String type, String upgrade){
            this.type = type;
            this.upgrades.add(upgrade);
        }

        public String getType(){ return type; }
        public List<String> getUpgrades() { return upgrades; }

        public void setType(String type) { this.type = type; }
        public void addUpgrade(String upgrade) {upgrades.add(upgrade); }
        public void removeUpgrade(String upgrade) { upgrades.remove(upgrade); }
    }


    private void validateList(XWSList list) throws XWSpawnException
    {
        boolean error = false;
        XWSpawnException exception = new XWSpawnException();
        XWSList newList = null;
        HashMap<String,String> skippedPilots = new HashMap<String,String>();
        for (XWSList.XWSPilot pilot : list.getPilots())
        {

            String shipXws = pilot.getShip();
            String pilotXws = pilot.getXws();

            // check the ship
            if(MasterShipData.getShipData(shipXws) == null)
            {

                // the ship is not valid
                error = true;
                exception.addMessage("Ship "+shipXws+" was not found.  Skipping.");
                // skippedPilots.put(pilot.getXws(),"X");
                skippedPilots.put(pilot.getName(),"X");

            }else if(MasterPilotData.getPilotData(shipXws,pilotXws,list.getFaction()) == null && MasterPilotData.getPilotData(shipXws,pilot.getName(),list.getFaction()) == null)
            {

                error = true;
                exception.addMessage("Pilot "+pilot.getName()+" was not found.  Skipping.");
                //  skippedPilots.put(pilot.getXws(),"X");
                skippedPilots.put(pilot.getName(),"X");
            }

        }

        if(error)
        {
            // create a new list, removing the pilots/ships that aren't valid
            newList = new XWSList();
            newList.setDescription(list.getDescription());
            newList.setFaction(list.getFaction());
            newList.setName(list.getName());
            newList.setObstacles(list.getObstacles());
            newList.setPoints(list.getPoints());
            newList.setVendor(list.getVendor());
            newList.setVersion(list.getVersion());
            newList.setXwsSource(list.getXwsSource());

            for (XWSList.XWSPilot pilot : list.getPilots())
            {


                if(skippedPilots.get(pilot.getName()) == null)
                {

                    newList.addPilot(pilot);
                }
            }
            exception.setNewList(newList);
            // throw the exception
            throw exception;
        }
    }

    private XWSList loadListFromRawJson(String userInput) {
        try {
            XWSList list = getMapper().readValue(userInput, XWSList.class);
            list.setXwsSource("JSON");
            return list;
        } catch (Exception e) {
            logToChat("Unable to load raw JSON list '%s': %s", userInput, e.toString());
            return null;
        }
    }

    public void addTo(Buildable parent) {

        for (int i = 1; i <= 8; i++) {
            final int playerId = i;

            JButton b = new JButton("2.0 Spawn");
            b.setAlignmentY(0.0F);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    spawnForPlayer(playerId);
                }
            });
            spawnButtons.add(b);

            getPlayerMap(i).getToolBar().add(b);
        }
    }

    public void removeFrom(Buildable parent) {
        for (int i = 1; i <= 8; i++) {
            getPlayerMap(i).getToolBar().remove(spawnButtons.get(i - 1));
        }
    }

    private Map getPlayerMap(int playerIndex) {
        for (Map loopMap : GameModule.getGameModule().getComponentsOf(Map.class)) {
            if (("Player " + Integer.toString(playerIndex)).equals(loopMap.getMapName())) {
                return loopMap;
            }
        }
        return null;
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
        return "";
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public HelpFile getHelpFile() {
        return null;
    }
    // </editor-fold>

}
