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
 */
public class AutoSquadSpawn2e extends AbstractConfigurable {


    private List<JButton> spawnButtons = Lists.newArrayList();

    private void spawnPiece(GamePiece piece, Point position, Map playerMap) {
        Command placeCommand = playerMap.placeOrMerge(piece, position);
        placeCommand.execute();
        GameModule.getGameModule().sendAndLog(placeCommand);
    }
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
        final JPanel anUpgradePanel = new JPanel();
        anUpgradePanel.setLayout(new BoxLayout(anUpgradePanel, BoxLayout.X_AXIS));
        anUpgradePanel.add(Box.createRigidArea(new Dimension(25,0)));
        JComboBox upgradeTypes = new JComboBox();
        upgradeTypes.addItem("Select Upgrade Type.");
        JComboBox upgrades = new JComboBox();
        for(XWS2Upgrades ups : allUpgrades) {
            for(XWS2Upgrades.anUpgrade anUp : ups.getUpgrades())
            {
                upgrades.addItem(anUp.getName());
            }
        }
        JButton addUpg = new JButton("Add");
        addUpg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUpgradeEntry(upgradeWholeBoxForShipPanel, frame);
            }
        });
        anUpgradePanel.add(upgradeTypes);
        anUpgradePanel.add(upgrades);
        anUpgradePanel.add(addUpg);

        upgradeWholeBoxForShipPanel.add(anUpgradePanel);
        //anUpgradePanel.add(remUpg);

        JButton addShipButton = new JButton("Add Ship");
        addShipButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {
            cloneShipBehavior(false, empireShipComboList, empirePilotComboList, rootPanel, frame, allShips);
        }
        });
        JButton cloneShipButton = new JButton("Clone Ship");
        cloneShipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cloneShipBehavior(true, empireShipComboList, empirePilotComboList, rootPanel, frame, allShips);
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

        JButton validateButton = new JButton("Spawn List");
        validateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if("ok".equals(entryField.getText())){

                }
            }
        });

        rootPanel.add(validateButton);
        JButton cloneButton = new JButton("Clone Ship");
        cloneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cloneShipBehavior(true, empireShipComboList,empirePilotComboList, rootPanel, frame, allShips);
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

    private void addUpgradeEntry(final JPanel theUpgPanel, final JFrame frame) {
        final JPanel anUpgradePanel = new JPanel();
        anUpgradePanel.setLayout(new BoxLayout(anUpgradePanel, BoxLayout.X_AXIS));
        anUpgradePanel.add(Box.createRigidArea(new Dimension(25,0)));
        JComboBox upgradeTypes = new JComboBox();
        upgradeTypes.addItem("Select Upgrade Type.");
        JComboBox upgrades = new JComboBox();
        JButton remUpg = new JButton("[X]");
        remUpg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(Component c : theUpgPanel.getComponents())
                {
                    if((Component)anUpgradePanel == c) theUpgPanel.remove(c);
                }
                frame.setSize(frame.getWidth(), frame.getHeight() - anUpgradePanel.getHeight());
                frame.pack();
                theUpgPanel.invalidate();
                frame.invalidate();
            }
        });
        anUpgradePanel.add(upgradeTypes);
        anUpgradePanel.add(upgrades);
        anUpgradePanel.add(remUpg);

        theUpgPanel.add(anUpgradePanel);
        frame.setSize(new Dimension(frame.getWidth(), frame.getHeight() + anUpgradePanel.getHeight()));
        frame.validate();
        frame.invalidate();
        frame.pack();
    }

    private void cloneShipBehavior(final boolean wantCloning, final JComboBox toCopyShip, final JComboBox toCopyPilot, final JPanel rootPanel, final JFrame frame, final List<XWS2Pilots> allShips) {
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

        final JPanel anotherPanel = new JPanel();

        anotherPanel.add(empireShipComboList);
        anotherPanel.add(empirePilotComboList);

        final JButton cloneButton = new JButton("Clone Ship");
        final JButton removeButton = new JButton("Remove Ship");

        final JPanel anotherUpgradeWholeThing = new JPanel();

        cloneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cloneShipBehavior(true, empireShipComboList,empirePilotComboList, rootPanel, frame, allShips);
            }
        });
        removeButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {
            for(Component c : rootPanel.getComponents())
            {
                if((Component)anotherPanel == c) rootPanel.remove(c);
                if((Component)anotherUpgradeWholeThing == c) rootPanel.remove(c);
            }
            frame.setSize(frame.getWidth(), frame.getHeight() - anotherPanel.getHeight());
            frame.pack();
            rootPanel.invalidate();
            frame.invalidate();
        }
        });
        anotherPanel.add(cloneButton);
        anotherPanel.add(removeButton);
        anotherUpgradeWholeThing.setLayout(new BoxLayout(anotherUpgradeWholeThing, BoxLayout.Y_AXIS));
        JPanel firstUpgrade = new JPanel();
        firstUpgrade.setLayout(new BoxLayout(firstUpgrade, BoxLayout.X_AXIS));
        JComboBox newUTypes = new JComboBox();
        newUTypes.addItem("Select Upgrade Type.");
        JComboBox newUpgradeCombo = new JComboBox();
        JButton newUpgButton = new JButton("Add");
        newUpgButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUpgradeEntry(anotherUpgradeWholeThing, frame);
            }
        });
        firstUpgrade.add(newUTypes);
        firstUpgrade.add(newUpgradeCombo);
        firstUpgrade.add(newUpgButton);
        anotherUpgradeWholeThing.add(firstUpgrade);

        rootPanel.add(anotherPanel);
        rootPanel.add(anotherUpgradeWholeThing);
        rootPanel.setSize(new Dimension(rootPanel.getWidth(), rootPanel.getHeight() + anotherUpgradeWholeThing.getHeight()));
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
