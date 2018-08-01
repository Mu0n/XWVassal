package mic;


import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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

//keepsake for this whole class' behavior inside the player window - they must be kept track so they can be removed safely later
    private List<JButton> spawnButtons = Lists.newArrayList();

    private void spawnPiece(GamePiece piece, Point position, Map playerMap) {
        Command placeCommand = playerMap.placeOrMerge(piece, position);
        placeCommand.execute();
        GameModule.getGameModule().sendAndLog(placeCommand);
    }

    //Main interface via a Java Swing JFrame. The complexity has outgrown an InputDialog - we now use ActionListener on the JComboBox and JButton to react to the user commands
    private void spawnForPlayer(int playerIndex) {

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
        final List<XWS2Pilots> allShips = XWS2Pilots.loadFromRemote();
        final List<XWS2Upgrades> allUpgrades = XWS2Upgrades.loadFromRemote();

        final JFrame frame = new JFrame();
        //Panel which will include a Combo box for selecting the source of the xwing-data to use
        final JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        JPanel sourcePanel = new JPanel();
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));
        sourcePanel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
        JPanel sourceInfoPanel = new JPanel();
        sourceInfoPanel.setLayout(new BoxLayout(sourceInfoPanel, BoxLayout.Y_AXIS));
        sourceInfoPanel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);

        JPanel oneShipPanel = new JPanel();

        JLabel sourceExplanationLabel = new JLabel("This is a rough preliminary version of the 2nd edition squad autospawn window.");
        final JTextField entryField = new JTextField("Enter a valid XWS2 squad here.");

        final JComboBox empireShipComboList = new JComboBox();
        empireShipComboList.setToolTipText("Select a ship.");
        empireShipComboList.addItem("Select a ship.");

        final JComboBox empirePilotComboList = new JComboBox();
        empirePilotComboList.setToolTipText("Select a pilot");
        for(XWS2Pilots ship : allShips)
        {
            empireShipComboList.addItem(ship.getName());
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
        final JPanel upgradeWholeBoxForShipPanel = new JPanel();
        upgradeWholeBoxForShipPanel.setLayout(new BoxLayout(upgradeWholeBoxForShipPanel, BoxLayout.Y_AXIS));
        addUpgradeEntry(true, upgradeWholeBoxForShipPanel, frame, allUpgrades);

        JButton addShipButton = new JButton("Add Ship");
        addShipButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {
            copyOrCloneShipButtonBehavior(false, empireShipComboList, empirePilotComboList, rootPanel, frame, allShips, allUpgrades);
        }
        });
        JButton cloneShipButton = new JButton("Clone Ship");
        cloneShipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyOrCloneShipButtonBehavior(true, empireShipComboList, empirePilotComboList, rootPanel, frame, allShips, allUpgrades);
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

        rootPanel.add(entryField);

        JButton createXWS2Button = new JButton("Export to XWS2");
        createXWS2Button.setToolTipText("XWS2 is a community-defined text format used by squad builders (web, apps, etc.) in order to exchange squads.");
        createXWS2Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateXWS2(rootPanel, entryField);
            }
        });
        JButton validateButton = new JButton("Spawn List");
        validateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if("ok".equals(entryField.getText())){

                }
            }
        });

        rootPanel.add(createXWS2Button);
        rootPanel.add(validateButton);
        JButton cloneButton = new JButton("Clone Ship");
        cloneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyOrCloneShipButtonBehavior(true, empireShipComboList,empirePilotComboList, rootPanel, frame, allShips, allUpgrades);
            }
        });

        oneShipPanel.add(empireShipComboList);
        oneShipPanel.add(empirePilotComboList);
        oneShipPanel.add(addShipButton);
        oneShipPanel.add(cloneButton);

        rootPanel.add(oneShipPanel);
        rootPanel.add(upgradeWholeBoxForShipPanel);

        frame.add(rootPanel);
        frame.setSize(900,500);
        frame.setTitle("2.0 Squad Autospawn for player " + Integer.toString(playerInfo.getSide()));
        frame.pack();
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
    }

    private void generateXWS2(JPanel rootPanel, JTextField entryField) {
        entryField.setText("yeah da button works.");
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


    //Reacts to both "Add Ship" and "Clone Ship" buttons
    private void copyOrCloneShipButtonBehavior(final boolean wantCloning, final JComboBox toCopyShip, final JComboBox toCopyPilot, final JPanel rootPanel, final JFrame frame, final List<XWS2Pilots> allShips, final List<XWS2Upgrades> allUpgrades) {
        if(toCopyShip.getSelectedItem().toString().equals("Select a ship.")) {
            JFrame warnFrame = new JFrame();
            JOptionPane.showMessageDialog(warnFrame, "Please select a ship and a pilot before cloning.");
            return;
        }
        final JComboBox empireShipComboList = new JComboBox();
        empireShipComboList.setToolTipText("Select a ship.");
        empireShipComboList.addItem("Select a ship.");
        for(XWS2Pilots ship : allShips)
        {
            empireShipComboList.addItem(ship.getName());
        }
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
                copyOrCloneShipButtonBehavior(true, empireShipComboList,empirePilotComboList, rootPanel, frame, allShips, allUpgrades);
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
