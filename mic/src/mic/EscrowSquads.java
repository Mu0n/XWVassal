package mic;
/**
 * Created by mjuneau on 2019-02-07.
 * Creates buttons in the player window and manages an escrow service for a 1v1 game
 * Mimicks behavior for the Vassal league where both lists are known only when both have been submitted
 */

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.Map;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.launch.Player;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.List;

import static mic.Util.getCurrentPlayer;
import static mic.Util.logToChat;
import static mic.Util.logToChatWithoutUndo;

public class EscrowSquads extends AbstractConfigurable implements GameComponent {

    private List<JButton> escrowButtons = Lists.newArrayList();
    private static List<EscrowEntry> escrowEntries = Lists.newArrayList();
    static List<JLabel> escrowLabels = Lists.newArrayList(); //the labels that are shown in the frame
    final static JFrame frame = new JFrame();
    static JButton spawnButton;

    public static void escrowInstructionsPopup(){
        final JFrame frameInstr = new JFrame();
        frameInstr.setResizable(true);
        JLabel title = new JLabel("Escrow your squad - what, why, how?");
        title.setFont(new Font("Serif", Font.PLAIN, 20));

        JLabel instructions = new JLabel("<html><body>Step 1: each player goes to their respective player window and go into the 2nd edition Squad Spawn<br>"+
                "Step 2: they enter a list and carefully hit the 'Send to Escrow' button instead of the 'Spawn Squad...' button.<br>"+
                "Step 3: the players then open the 'Escrow Squad' in their player window and click 'Set'<br>" +
                "Step 4: When at least 2 players are 'Ready for Escrow', a player can click on the 'Spawn' button, all readied lists will spawn in their respective windows.<br>" +
                "<br>"+
                "IMPORTANT: both players must have a simultaneous active connection in the game room for this whole process to work<br>"+
                "Do not send a squad to escrow before your opponent is present.<br>"+
                "If someone joins late or loses connection, then 'Resend Own Squad' will help restore that person's escrow data.</body></html>");

        JButton gotItButton = new JButton("Got it!");
        gotItButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frameInstr.dispose();
            }
        });
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));


        mainPanel.add(title);
        mainPanel.add(instructions);
        mainPanel.add(gotItButton);

        frameInstr.add(mainPanel);
        frameInstr.setTitle("Escrow Squad Instructions");
        frameInstr.pack();
        frameInstr.setVisible(true);
        frameInstr.toFront();
    }

    public static List<EscrowEntry> getEscrowEntries(){ return escrowEntries; }

    public static void insertEntry(String playerSide, String playerName, XWSList2e verifiedXWSSquad, String source, String squadPoints, Boolean ready) {
        for(EscrowEntry ee : escrowEntries){
            if(ee.playerSide.equals(playerSide)){ //found the entry, simply update the squad info, leave the side and name intact
                ee.playerSide = playerSide;
                ee.playerName = playerName;
                ee.xwsSquad = verifiedXWSSquad;
                ee.source = source;
                ee.points = squadPoints;
                ee.isReady = ready;
                refreshEE();
                refreshEL();
                return;
            }
        }
    }

    public static void clearOwnEntry() {
        String thisSide = "Player " + mic.Util.getCurrentPlayer().getSide();
        String thisName = mic.Util.getCurrentPlayer().getName();
        for(EscrowEntry ee : escrowEntries){
            if(ee.playerSide.equals(thisSide) && ee.playerName.equals(thisName)){ //found it!
                EscrowEntry clearedEE = new EscrowEntry(ee.playerSide, ee.playerName, null, "", "", false);
                BroadcastEscrowSquadCommand besq = new BroadcastEscrowSquadCommand(clearedEE, clearedEE.isReady);
                besq.execute();
                GameModule.getGameModule().sendAndLog(besq);
                logToChat(thisSide + " (" + ee.playerName + ") has cleared a squad for Escrow.");
                refreshEL();
            }
        }
    }
    public static void resendOwnEntry() {
        String thisSide = "Player " + mic.Util.getCurrentPlayer().getSide();
        String thisName = mic.Util.getCurrentPlayer().getName();
        for(EscrowEntry ee : escrowEntries){
            if(ee.playerSide.equals(thisSide) && ee.playerName.equals(thisName)){ //found it!
                BroadcastEscrowSquadCommand besq = new BroadcastEscrowSquadCommand(ee, ee.isReady);
                besq.execute();
                GameModule.getGameModule().sendAndLog(besq);
                logToChat(thisSide + " (" + ee.playerName + ") has resent a squad for Escrow.");
            }
        }
    }

    private static synchronized void populateEE(){ //gets called in the addTo as the module is loading up
        if(escrowEntries==null) escrowEntries = Lists.newArrayList();

        PlayerRoster.PlayerInfo[] arrayOfPI = mic.Util.getAllPlayerInfo();
        if(escrowEntries.size()==0){
            for(int i=1;i<=8;i++){
                String nameToUse = "(spot open)";
                try{
                    String detectedName = arrayOfPI[i-1].playerName;
                    nameToUse = detectedName;
                }catch(Exception e){}
                escrowEntries.add(new EscrowEntry("Player " + i, nameToUse, null, "", "", false));
            }
        }
    }
    private static synchronized void populateEL(){ //it's assumed EE is populated before EL
        if(escrowLabels==null) escrowLabels = Lists.newArrayList();
        if(escrowLabels.size()==0){
            for(int i=1;i<=8;i++){
                escrowLabels.add(new JLabel(escrowEntries.get(i-1).playerSide + " - "+ escrowEntries.get(i-1).playerName + " - null"));
            }
        }
    }

    private static synchronized void refreshEL(){ //this gets called every time an escrowEntry is redone so that the labels are updated
        try{
            for(int i=1;i<=8;i++){
                EscrowEntry ee = escrowEntries.get(i-1);
                JLabel jl = escrowLabels.get(i-1);
                jl.setText(ee.playerSide + " - " + ee.playerName + " - " + (ee.xwsSquad==null?"(no squad entered)":"Verified Squad Present!")
                        + " - " + ee.source + " || " + (ee.isReady?" READY for Escrow!":" NOT READY for escrow."));
            }
        }catch(Exception e){}
    }
    private static synchronized void refreshEE(){ //this has to be called after addTo (otherwise no players are present yet) when the popup is made visible
//only use is to make the player names visible asap they are available to fetch
        PlayerRoster.PlayerInfo[] arrayOfPI = mic.Util.getAllPlayerInfo();
        //PlayerRoster will only occupy a 2-element array if there are only 2 players. the trick is to still find and match the Player side in order to see escrowentries right

        List<Integer> donePlayers = Lists.newArrayList();

        for(int i=0; i< arrayOfPI.length;i++) {
            try {
                String detectedName = arrayOfPI[i].playerName;
                String detectedSide = arrayOfPI[i].getSide();
                String justSideNumberString = detectedSide.split("Player ")[1];
                Integer justSideNumber = Integer.parseInt(justSideNumberString);

                EscrowEntry ee = escrowEntries.get(justSideNumber - 1);
                ee.playerName = detectedName;
                donePlayers.add(justSideNumber);
            } catch (Exception e) {
            }
        }
        for(int i=0; i<8;i++){
            if(donePlayers.contains(i+1)) continue;
            EscrowEntry ee = escrowEntries.get(i);
            ee.playerName = "(spot open)";
        }
    }

    public static synchronized void escrowPopup() {
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        //Populate the labels of the known players at this point
        JPanel playersAreaPanel = new JPanel();
        playersAreaPanel.setLayout(new BoxLayout(playersAreaPanel, BoxLayout.Y_AXIS));
        for(int i=0; i<8; i++){
            JPanel aPlayerSlot = new JPanel();
            aPlayerSlot.setLayout(new BoxLayout(aPlayerSlot, BoxLayout.X_AXIS));
            aPlayerSlot.add(escrowLabels.get(i));
            final int index = i;

            JButton ready = new JButton("Set");
            ready.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    EscrowEntry ee = escrowEntries.get(index);

                    String thisSide = "Player " + mic.Util.getCurrentPlayer().getSide();
                    String sideCheckString = "Player " + (index+1);

                    if(sideCheckString.equals(thisSide)){
                        if(ee.xwsSquad!=null){
                            if(ee.isReady == false) ee.isReady = true;
                            else ee.isReady= false;
                            BroadcastEscrowSquadCommand besq = new BroadcastEscrowSquadCommand(ee, ee.isReady);
                            besq.execute();
                            GameModule.getGameModule().sendAndLog(besq);
                            logToChat(thisSide + " (" + ee.playerName + ") has Set " + (ee.isReady?"Ready":"Not Ready") + " for Escrow Spawning.");
                            revisitSpawnReadiness();
                        }else{
                            JOptionPane.showMessageDialog(frame, "Send a squad to escrow first! Go to the 2.0 Squad Spawn in your player window.");
                        }
                    }else{
                        JOptionPane.showMessageDialog(frame, "You can only mark yourself ready for escrow for your own side!");
                    }
                   refreshEL();
                }
            });
            aPlayerSlot.add(ready);

            playersAreaPanel.add(aPlayerSlot);
        }

        JPanel controlButtonPanel = new JPanel();
        controlButtonPanel.setLayout(new BoxLayout(controlButtonPanel, BoxLayout.X_AXIS));
        JButton instrButton = new JButton("What is Escrow?");
        instrButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                escrowInstructionsPopup();
            }
        });
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshEE();
                refreshEL();
                revisitSpawnReadiness();
            }
        });
        spawnButton = new JButton("Spawn");
        spawnButton.setEnabled(false);
        spawnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                final List<XWS2Pilots> allShips = XWS2Pilots.loadFromLocal();
                final XWS2Upgrades allUpgrades = XWS2Upgrades.loadFromLocal();
                final List<XWS2Upgrades.Condition> allConditions = XWS2Upgrades.loadConditionsFromLocal();

                List<Integer> spawningPlayers = Lists.newArrayList();
                for(int i=0;i<8;i++){
                    if(escrowEntries.get(i).isReady && escrowEntries.get(i).xwsSquad !=null)
                        AutoSquadSpawn2e.DealWithXWSList(escrowEntries.get(i).xwsSquad, i+1, allShips, allUpgrades, allConditions);
                    spawningPlayers.add(i);
                }
                //After the lists spawn final, go into a final state:
                //Make the labels simpler
                //make the Set button inactive
                //transform the players who just spawned's set button to a button that sends the XWS to sirjorj or another service

                //turn readiness off
                for(Integer j : spawningPlayers){
                    escrowEntries.get(j).isReady = false;
                    BroadcastEscrowSquadCommand besq = new BroadcastEscrowSquadCommand(escrowEntries.get(j), escrowEntries.get(j).isReady);
                    besq.execute();
                    GameModule.getGameModule().sendAndLog(besq);
                }
                spawnButton.setEnabled(false);
                revisitSpawnReadiness();
                refreshEL();
            }
        });
        JButton clearButton = new JButton("Clear own Squad");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearOwnEntry();
            }
        });
        JButton resendSquad = new JButton("Resend own Squad");
        resendSquad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resendOwnEntry();
            }
        });

        controlButtonPanel.add(instrButton);
        controlButtonPanel.add(resendSquad);
        controlButtonPanel.add(refreshButton);
        controlButtonPanel.add(spawnButton);
        controlButtonPanel.add(clearButton);

        JLabel instructions = new JLabel("<html><body>IMPORTANT: both players must have a simultaneous active connection in the game room for this whole process to work<br>" +
                "Do not send a squad to escrow before your opponent is present.<br>" +
                "If someone joins late or loses connection, then 'Resend Own Squad' will help restore that person's escrow data.</body></html>");
        instructions.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panel.add(playersAreaPanel);
        panel.add(controlButtonPanel);
        panel.add(instructions);
        frame.add(panel);
        frame.setPreferredSize(new Dimension(800,380));
        frame.setTitle("Escrow Squads");
        panel.setOpaque(true); // content panes must be opaque
        frame.pack();
        frame.setVisible(false);
    }

    private static void revisitSpawnReadiness() {
        int readyCount = 0;
        for(int i=0;i<8;i++){
            if(escrowEntries.get(i).isReady) readyCount++;
        }
        if(readyCount>1) spawnButton.setEnabled(true);
    }

    public static void showPopup(){
        refreshEE();
        refreshEL();
        frame.setVisible(true);
        frame.toFront();
    }

    public String[] getAttributeDescriptions() {
        return new String[0];
    }

    public Class<?>[] getAttributeTypes() {
        return new Class[0];
    }

    public String[] getAttributeNames() {
        return new String[0];
    }

    public void setAttribute(String key, Object value) {

    }

    public String getAttributeValueString(String key) {
        return null;
    }

    public void removeFrom(Buildable parent) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public void addTo(Buildable parent) {
        populateEE();
        populateEL();
//create the popup for the first time, don't show it by default
        if(frame.getContentPane().getComponents().length == 0) {
            escrowPopup();
        }

        for (int i = 1; i <= 8; i++) {
            final int playerId = i;

            //Epic trays toggle buttons
            JButton b = new JButton("Escrow Squad");
            b.setAlignmentY(0.0F);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    showPopup();
                }
            });
            escrowButtons.add(b);

            //Adding those elements to the player window toolbars
            Map playerMap = getPlayerMap(i);
            playerMap.getToolBar().add(b);
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

    public void setup(boolean gameStarting) {

    }

    public Command getRestoreCommand() {
        Command bigCommandChain = null;

        for(int i=0; i<8; i++){
            try{
                BroadcastEscrowSquadCommand besq = new BroadcastEscrowSquadCommand(escrowEntries.get(i), escrowEntries.get(i).isReady);
                bigCommandChain.append(besq);
            }catch(Exception e){
                logToChat("EGC line 40 couldn't access escrowEntries as Game Component");
            }
        }
        return bigCommandChain;
    }

    public static class EscrowEntry{
        String playerSide="";
        String playerName="";
        XWSList2e xwsSquad;
        String source="";
        String points="";
        Boolean isReady=false;

        public EscrowEntry(String reqSide, String reqPlayerName, XWSList2e reqXWS, String reqSource, String reqPoints, Boolean reqReady){
            playerSide = reqSide;
            playerName = reqPlayerName;
            xwsSquad = reqXWS;
            source = reqSource;
            points = reqPoints;
            isReady = reqReady;
        }
    }
}
