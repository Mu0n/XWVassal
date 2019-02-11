package mic;
/**
 * Created by mjuneau on 2019-02-07.
 * Creates buttons in the player window and manages an escrow service for a 1v1 game
 * Mimicks behavior for the Vassal league where both lists are known only when both have been submitted
 */

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.launch.Player;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static mic.Util.getCurrentPlayer;
import static mic.Util.logToChat;
import static mic.Util.logToChatWithoutUndo;

public class EscrowSquads extends AbstractConfigurable {

    private List<JButton> escrowButtons = Lists.newArrayList();
    private static List<EscrowEntry> escrowEntries = Lists.newArrayList();
    static List<JLabel> escrowLabels = Lists.newArrayList(); //the labels that are shown in the frame
    final static JFrame frame = new JFrame();

    public static void escrowInstructionsPopup(){
        final JFrame frameInstr = new JFrame();
        frameInstr.setResizable(true);
        JLabel title = new JLabel("Escrow your squad - what, why, how?");
        title.setFont(new Font("Serif", Font.PLAIN, 20));

        JLabel instructions = new JLabel("<html><body>Step 1: each player goes to their respective player window and go into the 2nd edition Squad Spawn<br>"+
                "Step 2: they enter a list and carefully hit the 'Send to Escrow' button instead of the 'Spawn Squad...' button.<br>"+
                "Step 3: the players then open the Escrow Squad in their player window and select their opponent's player # and click 'Ready'<br>" +
                "Step 4: When the last player to click on the 'Ready' button has done so and a match is found, both lists will spawn in their respective windows.<br></body></html>");

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

    public static void insertEntry(String playerSide, String playerName, XWSList2e verifiedXWSSquad, String source, String squadPoints) {
        logToChat("ES line 67 number of Eentries " + escrowEntries.size());
        logToChat("ES line 67 number of Elabels " + escrowLabels.size());
        logToChat("ES line 67 inserting for playerSide:" + playerSide+":");
        logToChat("ES line 67 sending xwslist " + verifiedXWSSquad.toString());
        logToChat("ES line 67 source "+ source);
        for(EscrowEntry ee : escrowEntries){
            logToChat("ES lines 69 checking out this side:" + ee.playerSide+":");
            if(ee.playerSide.equals(playerSide)){ //found the entry, simply update the squad info, leave the side and name intact
                logToChat("ES line 74 found a playerSide match!");
                ee.playerSide = playerSide;
                ee.playerName = playerName;
                ee.xwsSquad = verifiedXWSSquad;
                ee.source = source;
                ee.points = squadPoints;
                refreshEL();
                return;
            }
        }
    }

    public static void clearOwnEntry() {
        String thisSide = "" + mic.Util.getCurrentPlayer().getSide();
        String thisName = mic.Util.getCurrentPlayer().getName();
        for(EscrowEntry ee : escrowEntries){
            if(ee.playerSide.equals(thisSide) && ee.playerName.equals(thisName)){ //found it!
                ee.clearSquad();
            }
        }
    }

    private static synchronized void populateEE(){
        if(escrowEntries==null) escrowEntries = Lists.newArrayList();
        if(escrowEntries.size()==0){
            for(int i=1;i<=8;i++){
                escrowEntries.add(new EscrowEntry("Player " + i, "(spot open)", null, "", ""));
            }
        }
    }
    private static synchronized void populateEL(){
        if(escrowLabels==null) escrowLabels = Lists.newArrayList();
        if(escrowLabels.size()==0){
            for(int i=1;i<=8;i++){
                escrowLabels.add(new JLabel("Player " + i + " - (spot open) - (no list yet)"));
            }
        }
    }

    private static synchronized void refreshEE(){
        if(escrowEntries==null || escrowEntries.size()==0) populateEE();
        PlayerRoster.PlayerInfo[] arrayOfPlayerInfo = mic.Util.getAllPlayerInfo();
        int currentSide = mic.Util.getCurrentPlayer().getSide();
        try{
            for(int i=0;i<8;i++){
                String sideString = arrayOfPlayerInfo[i].getSide();
                String justSideNo = sideString.split("Player ")[1];
                logToChat("ES line 122 - sideString:"+sideString + ": justSideNo:"+justSideNo+":");
            }
        }catch(Exception e){}

    }

    private static synchronized void refreshEL(){
        try{
            for(int i=0;i<8;i++){
                EscrowEntry ee = escrowEntries.get(i);
                JLabel jl = escrowLabels.get(i);
                jl.setText(ee.playerSide + " - " + ee.playerName + " - " + ee.xwsSquad + " - " + ee.source);
            }
        }catch(Exception e){}
    }

    public static synchronized  void refreshFrame(){
        frame.repaint();
    }
    public static synchronized  void disposeFrame(){
        frame.dispose();
    }
    /*
    private static synchronized void findPlayersAndRefreshFrame(){
        logToChat("ES line 117 entering refresh");
        for(int i=0; i<8; i++) {
            try {
                if(escrowEntries.get(i).xwsSquad!=null){
                    escrowLabels.get(i).setText(escrowEntries.get(i).playerSide + " - " +
                            escrowEntries.get(i).playerName + " - " +
                            escrowEntries.get(i).xwsSquad + " - " +
                            escrowEntries.get(i).source);
                }else {
                    EscrowEntry ee = escrowEntries.get(i);

                    logToChat("ES line 126 refreshing a name with no list");
                    escrowLabels.get(i).setText("Player " + escrowEntries.get(i).playerSide + " - " +
                            escrowEntries.get(i).playerName + " - no list escrowed yet");
                }

            } catch(Exception e){
                continue;
            }
        }
    }
*/
    public static synchronized void escrowPopup(int playerId) {
        //playerId is 1 to 8

        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        if (playerInfo.getSide() != playerId) {
            return;
        }
        refreshEE();
        refreshEL();
       // findPlayersAndRefreshFrame();



        frame.setResizable(true);

        final JPanel panel = new JPanel();


        //Populate the labels of the known players at this point

        JPanel playersAreaPanel = new JPanel();
        playersAreaPanel.setLayout(new BoxLayout(playersAreaPanel, BoxLayout.Y_AXIS));
        for(int i=0; i<8; i++){
            JPanel aPlayerSlot = new JPanel();
            aPlayerSlot.setLayout(new BoxLayout(aPlayerSlot, BoxLayout.X_AXIS));
            aPlayerSlot.add(escrowLabels.get(i));


            String sideCheck = mic.Util.getCurrentPlayer().getSide()+"";
            JButton ready = new JButton("Ready");
            try{
                if(sideCheck.equals(escrowEntries.get(i).playerSide)) {
                    aPlayerSlot.add(ready);
                }
            }catch(Exception e){

            }
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
            }
        });
        JButton spawnButton = new JButton("Spawn");
        spawnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                final List<XWS2Pilots> allShips = XWS2Pilots.loadFromLocal();
                final XWS2Upgrades allUpgrades = XWS2Upgrades.loadFromLocal();
                final List<XWS2Upgrades.Condition> allConditions = XWS2Upgrades.loadConditionsFromLocal();

                int theSide = mic.Util.getCurrentPlayer().getSide();
                for(int i=0;i<8;i++){
                    if(escrowEntries.get(i).playerSide.equals("Player " + theSide)) AutoSquadSpawn2e.DealWithXWSList(escrowEntries.get(i).xwsSquad, theSide, allShips, allUpgrades, allConditions);
                }

            }
        });
        controlButtonPanel.add(instrButton);
        controlButtonPanel.add(refreshButton);
        controlButtonPanel.add(spawnButton);
        panel.add(playersAreaPanel);
        panel.add(controlButtonPanel);
        frame.add(panel);
        frame.setPreferredSize(new Dimension(800,300));
        frame.setTitle("Escrow Squads");
        panel.setOpaque(true); // content panes must be opaque
        frame.pack();
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

        for (int i = 1; i <= 8; i++) {
            final int playerId = i;

            //Epic trays toggle buttons
            JButton b = new JButton("Escrow Squad");
            b.setAlignmentY(0.0F);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if(!frame.isDisplayable()) escrowPopup(playerId);

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

    public static class EscrowEntry{
        String playerSide="";
        String playerName="";
        XWSList2e xwsSquad;
        String source="";
        String points="";

        public EscrowEntry(String reqSide, String reqPlayerName, XWSList2e reqXWS, String reqSource, String reqPoints){
            playerSide = reqSide;
            playerName = reqPlayerName;
            xwsSquad = reqXWS;
            source = reqSource;
            points = reqPoints;
        }

        public void clearSquad(){
            xwsSquad = new XWSList2e();
            source="";
            points="";
        }
    }
}
