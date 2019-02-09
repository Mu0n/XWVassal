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

public class EscrowSquads extends AbstractConfigurable {

    private List<JButton> escrowButtons = Lists.newArrayList();
    private static List<EscrowEntry> escrowEntries = Lists.newArrayList();
    List<JLabel> escrowLabels = Lists.newArrayList(); //the labels that are shown in the frame


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

    public static void insertEntry(String playerSide, String playerName, String verifiedXWSSquad, String source, String squadPoints) {
        for(EscrowEntry ee : escrowEntries){
            if(ee.playerSide.equals(playerSide) && ee.playerName.equals(playerName)){ //found the entry, simply update the squad info, leave the side and name intact
                ee.xwsSquad = verifiedXWSSquad;
                ee.source = source;
                ee.points = squadPoints;
                return;
            }
        }
        escrowEntries.add(new EscrowEntry(playerSide, playerName, verifiedXWSSquad, source, squadPoints)); //this will happen for late joiners not already in the list
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

    private synchronized void findPlayersAndPopulateFrameAtTheStart(){
        PlayerRoster.PlayerInfo[] arrayOfPlayerInfo = mic.Util.getAllPlayerInfo();
        for(int i=0; i<arrayOfPlayerInfo.length; i++){
            escrowLabels.add(new JLabel(arrayOfPlayerInfo[i].getSide() + " " + arrayOfPlayerInfo[i].playerName + " - no list escrowed yet."));
        }
    }
    private synchronized void escrowPopup(int playerId) {
        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        if (playerInfo.getSide() != playerId) {
            return;
        }

        final JFrame frame = new JFrame();
        frame.setResizable(true);

        final JPanel panel = new JPanel();


        //Populate the labels of the known players at this point

        JPanel playersAreaPanel = new JPanel();
        playersAreaPanel.setLayout(new BoxLayout(playersAreaPanel, BoxLayout.Y_AXIS));
        for(JLabel jl : playerLabelList){
            JPanel aPlayerSlot = new JPanel();
            aPlayerSlot.setLayout(new BoxLayout(aPlayerSlot, BoxLayout.X_AXIS));

            JLabel readyLabel = new JLabel("not ready");
            aPlayerSlot.add(jl);
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
        controlButtonPanel.add(instrButton);
        panel.add(playersAreaPanel);
        panel.add(controlButtonPanel);
        frame.add(panel);
        frame.setPreferredSize(new Dimension(800,900));
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
        for (int i = 1; i <= 8; i++) {
            final int playerId = i;

            //Epic trays toggle buttons
            JButton b = new JButton("Escrow Squad");
            b.setAlignmentY(0.0F);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    escrowPopup(playerId);
                }
            });
            escrowButtons.add(b);

            //Adding those elements to the player window toolbars
            Map playerMap = getPlayerMap(i);
            playerMap.getToolBar().add(b);

            escrowedSquadXWS.add("");
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
        String xwsSquad="";
        String source="";
        String points="";

        public EscrowEntry(String reqSide, String reqPlayerName, String reqXWS, String reqSource, String reqPoints){
            playerSide = reqSide;
            playerName = reqPlayerName;
            xwsSquad = reqXWS;
            source = reqSource;
            points = reqPoints;
        }

        public void clearSquad(){
            xwsSquad ="";
            source="";
            points="";
        }
    }
}
