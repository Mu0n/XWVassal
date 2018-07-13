package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static mic.Util.*;

/**
 * Created by Mic on 12/02/2017.
 */
public class AutoSquadSpawn extends AbstractConfigurable {

    static final String yasbURL = "http://geordanr.github.io/xwing/";
    static final String voidURL = "http://xwing-builder.co.uk/build";
    static final String fabsURL = "http://x-wing.fabpsb.net/gindex.php";

    static final String modeListURL = "https://raw.githubusercontent.com/Mu0n/XWVassal-website/master/modeList.json";

    private boolean listHasHoundsTooth = false;
    private int houndsToothPilotSkill = 0;

    private VassalXWSPieceLoader slotLoader = new VassalXWSPieceLoader();
    private List<JButton> spawnButtons = Lists.newArrayList();
    MasterGameModeRouter mgmr = new MasterGameModeRouter();
    String altXwingDataString = "";

    private void spawnPiece(GamePiece piece, Point position, Map playerMap) {
        Command placeCommand = playerMap.placeOrMerge(piece, position);
        placeCommand.execute();
        GameModule.getGameModule().sendAndLog(placeCommand);
    }

    private Map getMap() {
        for (Map loopMap : GameModule.getGameModule().getComponentsOf(Map.class)) {
            if ("Contested Sector".equals(loopMap.getMapName())) {
                return loopMap;
            }
        }
        return null;
    }

    private void hackSpawnTCdemo1(Map playerMap)
    {
        Map theMap = playerMap;
        List<PieceSlot> allSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);
        PieceSlot mediumStemSlot = null;
        PieceSlot smallXwingSlot = null;
        PieceSlot smallSingleTurretStemSlot = null;
        PieceSlot stemDialSlot = null;
        PieceSlot cheapoCard = null;
        PieceSlot chargeToken = null;


        for(PieceSlot pieceSlot : allSlots )
        {
            String slotName = pieceSlot.getConfigureName();
            if(slotName.equals("ship -- medium 2e") && mediumStemSlot == null)
            {
                mediumStemSlot = pieceSlot;
                continue;
            }
            if(slotName.equals("Rebel Stem Dial") && stemDialSlot == null)
            {
                stemDialSlot = pieceSlot;
                continue;
            }
            if(slotName.equals("ship -- small 2e xwing") && smallXwingSlot == null)
            {
                smallXwingSlot = pieceSlot;
                continue;
            }
            if(slotName.equals("ship -- small 2e single turret") && smallSingleTurretStemSlot == null)
             {
                 smallSingleTurretStemSlot = pieceSlot;
                continue;
             }
             if(slotName.equals("Small TextBox") && cheapoCard == null){
                cheapoCard = pieceSlot;
                continue;
             }
            if(slotName.equals("2.0 Charge") && chargeToken == null){
                chargeToken = pieceSlot;
                continue;
            }
        }

        int pilotColPosX = 400;

        int upgradeSpacing = cheapoCard.getPiece().getShape().getBounds().width;

        GamePiece piece = mic.Util.newPiece(mediumStemSlot);
        piece.setProperty("Initiative",2);
        piece.setProperty("Shield Rating",3);
        piece.setProperty("Hull Rating",5);
        piece.setProperty("Pilot Name","Benthic Two Tubes");
        spawnPiece(piece, new Point(400,100), theMap);
        GamePiece dialPiece1 = mic.Util.newPiece(stemDialSlot);
        // execute the command
        List<String> aMoveList = Arrays.asList("0OR","1BG","1FG","1NG","2TW","2BG","2FG","2NG","2YW","3BW","3FW","3NW","4FW");
        StemDial.DialGenerateCommand myDialGen = new StemDial.DialGenerateCommand(aMoveList, "Benthic", dialPiece1, "Rebel Alliance");
        dialPiece1.setProperty("Pilot Name","2.0 Dial");
        dialPiece1.setProperty("Craft ID #","Benthic");
        myDialGen.execute();
        spawnPiece(dialPiece1, new Point(900,100), theMap);
        GamePiece pilotCard = mic.Util.newPiece(cheapoCard);
        pilotCard.setProperty("Line_1","Benthic Two Tubes");
        pilotCard.setProperty("xwstext", "Benthic Two Tubes - After you perform a [focus] action, you may transfer 1 of your focus tokens to a friendly ship at range 1-2.");
        spawnPiece(pilotCard, new Point(pilotColPosX,600), theMap);

        GamePiece upCard1 = mic.Util.newPiece(cheapoCard);
        upCard1.setProperty("Line_1","Pivot Wing");
        upCard1.setProperty("xwstext", "Pivot Wing - Open: Before you activate, you may flip this card. Closed: While you defend, roll 1 fewer defense die. After you execute a [0 Stop] maneuver, you may rotate your ship 90˚ or 180˚. Before you activate, you may flip this card.");
        spawnPiece(upCard1, new Point(pilotColPosX+upgradeSpacing,630), theMap);

        GamePiece upCard2 = mic.Util.newPiece(cheapoCard);
        upCard2.setProperty("Line_1","Perceptive Copilot");
        upCard2.setProperty("xwstext", "Perceptive Copilot - After you perform a [focus] action, gain 1 focus token.");
        spawnPiece(upCard2, new Point(pilotColPosX+2*upgradeSpacing,630), theMap);

        GamePiece upCard3 = mic.Util.newPiece(cheapoCard);
        upCard3.setProperty("Line_1","Advanced Sensors");
        upCard3.setProperty("xwstext", "Advanced Sensors - After you reveal your dial, you may perform 1 action. If you do, you cannot perform another action during your activation.");
        spawnPiece(upCard3, new Point(pilotColPosX+3*upgradeSpacing,630), theMap);

        GamePiece piece2 = mic.Util.newPiece(smallXwingSlot);
        piece2.setProperty("Initiative",2);
        piece2.setProperty("Shield Rating",2);
        piece2.setProperty("Hull Rating",4);
        piece2.setProperty("Pilot Name","Edrio");
        spawnPiece(piece2, new Point(700,100), theMap);
        GamePiece dialPiece2 = mic.Util.newPiece(stemDialSlot);
        // execute the command
        aMoveList = Arrays.asList("1BG","1FG","1NG","2TW","2BG","2FG","2NG","2YW","3ER","3TW","3BW","3FW","3NW","3YW","3RR","4FW","4KR");
        StemDial.DialGenerateCommand myDialGen2 = new StemDial.DialGenerateCommand(aMoveList, "Edrio", dialPiece2, "Rebel Alliance");
        dialPiece2.setProperty("Pilot Name","2.0 Dial");
        dialPiece2.setProperty("Craft ID #","Edrio");
        myDialGen2.execute();
        spawnPiece(dialPiece2, new Point(1100,100), theMap);
        GamePiece pilotCard2 = mic.Util.newPiece(cheapoCard);
        pilotCard2.setProperty("Line_1","Edrio Two Tubes");
        pilotCard2.setProperty("xwstext", "Edrio Two Tubes - Before you activate, if you are focused, you may perform an action.");
        spawnPiece(pilotCard2, new Point(pilotColPosX,700), theMap);
        upCard1 = mic.Util.newPiece(cheapoCard);
        upCard1.setProperty("Line_1","Servomotor S-Foils");
        upCard1.setProperty("xwstext", "Servomotor S-Foils - Open: Before you activate, you may flip this card. Closed: While you defend, roll 1 fewer defense die. After you execute a [0 5] maneuver, you may rotate your ship 90˚ or 180˚. Before you activate, you may flip this card.");
        spawnPiece(upCard1, new Point(pilotColPosX+upgradeSpacing,730), theMap);


        GamePiece piece3 = mic.Util.newPiece(smallXwingSlot);
        piece3.setProperty("Initiative",4);
        piece3.setProperty("Shield Rating",3);
        piece3.setProperty("Hull Rating",4);
        piece3.setProperty("Pilot Name","Jek Porkins");
        spawnPiece(piece3, new Point(400,400), theMap);
        GamePiece dialPiece3 = mic.Util.newPiece(stemDialSlot);
        // execute the command
        aMoveList = Arrays.asList("1BG","1FG","1NG","2TW","2BG","2FG","2NG","2YW","3ER","3TW","3BW","3FW","3NW","3YW","3RR","4FW","4KR");
        StemDial.DialGenerateCommand myDialGen3 = new StemDial.DialGenerateCommand(aMoveList, "Jek", dialPiece3, "Rebel Alliance");
        dialPiece3.setProperty("Pilot Name","2.0 Dial");
        dialPiece3.setProperty("Craft ID #","Jek");
        myDialGen3.execute();
        spawnPiece(dialPiece3, new Point(900,300), theMap);
        GamePiece pilotCard3 = mic.Util.newPiece(cheapoCard);
        pilotCard3.setProperty("Line_1","Jek Porkins");
        pilotCard3.setProperty("xwstext", "Jek Porkins - After you receive a stress token, you may roll 1 attack die to remove it. On a d result, suffer 1 d damage.");
        spawnPiece(pilotCard3, new Point(pilotColPosX,800), theMap);
        upCard1 = mic.Util.newPiece(cheapoCard);
        upCard1.setProperty("Line_1","Servomotor S-Foils");
        upCard1.setProperty("xwstext", "Servomotor S-Foils - Open: Before you activate, you may flip this card. Closed: While you defend, roll 1 fewer defense die. After you execute a [0 5] maneuver, you may rotate your ship 90˚ or 180˚. Before you activate, you may flip this card.");
        spawnPiece(upCard1, new Point(pilotColPosX+upgradeSpacing,830), theMap);
        upCard2 = mic.Util.newPiece(cheapoCard);
        upCard2.setProperty("Line_1","Proton Torpedoes");
        upCard2.setProperty("xwstext", "Proton Torpedoes - Attack (Lock): Spend 1 [Charge]. Change 1 [Hit] result to a [Crit] result.");
        spawnPiece(upCard2, new Point(pilotColPosX+2*upgradeSpacing,830), theMap);
        GamePiece chargePiece1 = mic.Util.newPiece(chargeToken);
        GamePiece chargePiece2 = mic.Util.newPiece(chargeToken);
        spawnPiece(chargePiece1, new Point(920,890), theMap);
        spawnPiece(chargePiece2, new Point(1000, 890), theMap);

        GamePiece piece4 = mic.Util.newPiece(smallSingleTurretStemSlot);
        piece4.setProperty("Initiative",2);
        piece4.setProperty("Shield Rating",3);
        piece4.setProperty("Hull Rating",4);
        piece4.setProperty("Pilot Name","Gray Sq.");
        spawnPiece(piece4, new Point(700,400), theMap);
        GamePiece dialPiece4 = mic.Util.newPiece(stemDialSlot);
        // execute the command
        aMoveList = Arrays.asList("1BG","1FG","1NG","2TW","2BW","2FG","2NW","2YW","3TR","3BW","3FW","3NW","3YR","4FR","4KR");
        StemDial.DialGenerateCommand myDialGen4 = new StemDial.DialGenerateCommand(aMoveList, "GSB", dialPiece4, "Rebel Alliance");
        dialPiece4.setProperty("Pilot Name","2.0 Dial");
        dialPiece4.setProperty("Craft ID #","GSB");
        myDialGen4.execute();
        spawnPiece(dialPiece4, new Point(1100,300), theMap);
        GamePiece pilotCard4 = mic.Util.newPiece(cheapoCard);
        pilotCard4.setProperty("Line_1","Gray Squadron Bomber");
        pilotCard4.setProperty("xwstext", "Gray Squadron Bomber - No pilot ability.");
        spawnPiece(pilotCard4, new Point(pilotColPosX,900), theMap);
        upCard1 = mic.Util.newPiece(cheapoCard);
        upCard1.setProperty("Line_1","Ion Cannon Turret");
        upCard1.setProperty("xwstext", "Ion Cannon Turret - Attack: If this attack hits, spend 1 [Hit] or [Crit] result to cause the defender to suffer 1 [Hit] damage. All remaining [Hit]/[Crit] results inflict ion tokens instead of damage.");
        spawnPiece(upCard1, new Point(pilotColPosX+upgradeSpacing,930), theMap);
        upCard2 = mic.Util.newPiece(cheapoCard);
        upCard2.setProperty("Line_1","Seismic Charges");
        upCard2.setProperty("xwstext", "Seismic Charges - Bomb Suring the System Phase, you may spend 1 [Charge] to drop a Seismic Charge with the [1 Forward] template.");
        spawnPiece(upCard2, new Point(pilotColPosX+2*upgradeSpacing,930), theMap);
        chargePiece1 = mic.Util.newPiece(chargeToken);
        chargePiece2 = mic.Util.newPiece(chargeToken);
        spawnPiece(chargePiece1, new Point(920,990), theMap);
        spawnPiece(chargePiece2, new Point(1000, 990), theMap);
    }


    private void hackSpawnTCdemo2(Map playerMap) {
        Map theMap = playerMap;
        List<PieceSlot> allSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);
        PieceSlot mediumStemSlot = null;
        PieceSlot smallTiefSlot = null;
        PieceSlot smallx1Slot = null;
        PieceSlot stemDialSlot = null;
        PieceSlot cheapoCard = null;
        PieceSlot chargeToken = null;


        for(PieceSlot pieceSlot : allSlots )
        {
            String slotName = pieceSlot.getConfigureName();
            if(slotName.equals("ship -- 2e Reaper") && mediumStemSlot == null)
            {
                mediumStemSlot = pieceSlot;
                continue;
            }
            if(slotName.equals("Imperial Stem Dial") && stemDialSlot == null)
            {
                stemDialSlot = pieceSlot;
                continue;
            }
            if(slotName.equals("ship -- small 2e tief") && smallTiefSlot == null)
            {
                smallTiefSlot = pieceSlot;
                continue;
            }

            if(slotName.equals("ship -- small 2e x1") && smallx1Slot == null)
            {
                smallx1Slot = pieceSlot;
                continue;
            }
            if(slotName.equals("Small TextBox") && cheapoCard == null){
                cheapoCard = pieceSlot;
                continue;
            }
            if(slotName.equals("2.0 Charge") && chargeToken == null){
                chargeToken = pieceSlot;
                continue;
            }
        }

        int pilotColPosX = 400;

        int upgradeSpacing = cheapoCard.getPiece().getShape().getBounds().width;

        GamePiece piece = mic.Util.newPiece(mediumStemSlot);
        piece.setProperty("Initiative",2);
        piece.setProperty("Shield Rating",3);
        piece.setProperty("Hull Rating",5);
        piece.setProperty("Pilot Name","Captain Feroph");
        spawnPiece(piece, new Point(400,100), theMap);
        GamePiece dialPiece1 = mic.Util.newPiece(stemDialSlot);
        // execute the command
        List<String> aMoveList = Arrays.asList("0OR","1LR","1TR","1BG","1FG","1NG","1YR","1PR","2TR","2BW","2FG","2NW","2YR","3BW","3FG","3NW");
        StemDial.DialGenerateCommand myDialGen = new StemDial.DialGenerateCommand(aMoveList, "Feroph", dialPiece1, "Galactic Empire");
        dialPiece1.setProperty("Pilot Name","2.0 Dial");
        dialPiece1.setProperty("Craft ID #","Feroph");
        myDialGen.execute();
        spawnPiece(dialPiece1, new Point(900,100), theMap);
        GamePiece pilotCard = mic.Util.newPiece(cheapoCard);
        pilotCard.setProperty("Line_1","Captain Feroph");
        pilotCard.setProperty("xwstext", "Captain Feroph - While you defend, if the attacker does not have any green tokens, you may change 1 of your f/blank results to an e result.");
        spawnPiece(pilotCard, new Point(pilotColPosX,600), theMap);

        GamePiece upCard1 = mic.Util.newPiece(cheapoCard);
        upCard1.setProperty("Line_1","Pivot Wing");
        upCard1.setProperty("xwstext", "Pivot Wing - Open: Before you activate, you may flip this card. Closed: While you defend, roll 1 fewer defense die. After you execute a [0 Stop] maneuver, you may rotate your ship 90˚ or 180˚. Before you activate, you may flip this card.");
        spawnPiece(upCard1, new Point(pilotColPosX+upgradeSpacing,630), theMap);

        GamePiece upCard2 = mic.Util.newPiece(cheapoCard);
        upCard2.setProperty("Line_1","Perceptive Copilot");
        upCard2.setProperty("xwstext", "Perceptive Copilot - After you perform a [focus] action, gain 1 focus token.");
        spawnPiece(upCard2, new Point(pilotColPosX+2*upgradeSpacing,630), theMap);

        GamePiece upCard3 = mic.Util.newPiece(cheapoCard);
        upCard3.setProperty("Line_1","Advanced Sensors");
        upCard3.setProperty("xwstext", "Advanced Sensors - After you reveal your dial, you may perform 1 action. If you do, you cannot perform another action during your activation.");
        spawnPiece(upCard3, new Point(pilotColPosX+3*upgradeSpacing,630), theMap);

        GamePiece piece2 = mic.Util.newPiece(smallTiefSlot);
        piece2.setProperty("Initiative",2);
        piece2.setProperty("Shield Rating",2);
        piece2.setProperty("Hull Rating",4);
        piece2.setProperty("Pilot Name","Edrio");
        spawnPiece(piece2, new Point(700,100), theMap);
        GamePiece dialPiece2 = mic.Util.newPiece(stemDialSlot);
        // execute the command
        aMoveList = Arrays.asList("1BG","1FG","1NG","2TW","2BG","2FG","2NG","2YW","3ER","3TW","3BW","3FW","3NW","3YW","3RR","4FW","4KR");
        StemDial.DialGenerateCommand myDialGen2 = new StemDial.DialGenerateCommand(aMoveList, "Edrio", dialPiece2, "Rebel Alliance");
        dialPiece2.setProperty("Pilot Name","2.0 Dial");
        dialPiece2.setProperty("Craft ID #","Edrio");
        myDialGen2.execute();
        spawnPiece(dialPiece2, new Point(1100,100), theMap);
        GamePiece pilotCard2 = mic.Util.newPiece(cheapoCard);
        pilotCard2.setProperty("Line_1","Edrio Two Tubes");
        pilotCard2.setProperty("xwstext", "Edrio Two Tubes - Before you activate, if you are focused, you may perform an action.");
        spawnPiece(pilotCard2, new Point(pilotColPosX,700), theMap);
        upCard1 = mic.Util.newPiece(cheapoCard);
        upCard1.setProperty("Line_1","Servomotor S-Foils");
        upCard1.setProperty("xwstext", "Servomotor S-Foils - Open: Before you activate, you may flip this card. Closed: While you defend, roll 1 fewer defense die. After you execute a [0 5] maneuver, you may rotate your ship 90˚ or 180˚. Before you activate, you may flip this card.");
        spawnPiece(upCard1, new Point(pilotColPosX+upgradeSpacing,730), theMap);


        GamePiece piece3 = mic.Util.newPiece(smallTiefSlot);
        piece3.setProperty("Initiative",4);
        piece3.setProperty("Shield Rating",3);
        piece3.setProperty("Hull Rating",4);
        piece3.setProperty("Pilot Name","Jek Porkins");
        spawnPiece(piece3, new Point(400,400), theMap);
        GamePiece dialPiece3 = mic.Util.newPiece(stemDialSlot);
        // execute the command
        aMoveList = Arrays.asList("1BG","1FG","1NG","2TW","2BG","2FG","2NG","2YW","3ER","3TW","3BW","3FW","3NW","3YW","3RR","4FW","4KR");
        StemDial.DialGenerateCommand myDialGen3 = new StemDial.DialGenerateCommand(aMoveList, "Jek", dialPiece3, "Rebel Alliance");
        dialPiece3.setProperty("Pilot Name","2.0 Dial");
        dialPiece3.setProperty("Craft ID #","Jek");
        myDialGen3.execute();
        spawnPiece(dialPiece3, new Point(900,300), theMap);
        GamePiece pilotCard3 = mic.Util.newPiece(cheapoCard);
        pilotCard3.setProperty("Line_1","Jek Porkins");
        pilotCard3.setProperty("xwstext", "Jek Porkins - After you receive a stress token, you may roll 1 attack die to remove it. On a d result, suffer 1 d damage.");
        spawnPiece(pilotCard3, new Point(pilotColPosX,800), theMap);
        upCard1 = mic.Util.newPiece(cheapoCard);
        upCard1.setProperty("Line_1","Servomotor S-Foils");
        upCard1.setProperty("xwstext", "Servomotor S-Foils - Open: Before you activate, you may flip this card. Closed: While you defend, roll 1 fewer defense die. After you execute a [0 5] maneuver, you may rotate your ship 90˚ or 180˚. Before you activate, you may flip this card.");
        spawnPiece(upCard1, new Point(pilotColPosX+upgradeSpacing,830), theMap);
        upCard2 = mic.Util.newPiece(cheapoCard);
        upCard2.setProperty("Line_1","Proton Torpedoes");
        upCard2.setProperty("xwstext", "Proton Torpedoes - Attack (Lock): Spend 1 [Charge]. Change 1 [Hit] result to a [Crit] result.");
        spawnPiece(upCard2, new Point(pilotColPosX+2*upgradeSpacing,830), theMap);
        GamePiece chargePiece1 = mic.Util.newPiece(chargeToken);
        GamePiece chargePiece2 = mic.Util.newPiece(chargeToken);
        spawnPiece(chargePiece1, new Point(920,890), theMap);
        spawnPiece(chargePiece2, new Point(1000, 890), theMap);
    }

    private void spawnForPlayer(int playerIndex) {
        listHasHoundsTooth = false;
        houndsToothPilotSkill = 0;

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

        //Panel which will include a Combo box for selecting the source of the xwing-data to use
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        JPanel sourcePanel = new JPanel();
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));
        sourcePanel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
        JPanel sourceInfoPanel = new JPanel();
        sourceInfoPanel.setLayout(new BoxLayout(sourceInfoPanel, BoxLayout.Y_AXIS));
        sourceInfoPanel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
        final JComboBox aComboBox = new JComboBox();

        mgmr.loadData();
        if(mgmr!=null)
        {
            for(MasterGameModeRouter.GameMode o : mgmr.getGameModes()){
                aComboBox.addItem(o.getName());
            }
        }
        else
        //if it can't access the list of sources on the web, make it base game by default
        {
            aComboBox.addItem("Base Game");
        }

        final JLabel sourceTextDescription = new JLabel("<html><body width=600><b>Description for <i>"
                + aComboBox.getSelectedItem().toString() + "</i></b>: "
                + mgmr.getGameMode(aComboBox.getSelectedItem().toString()).getDescription()
                + "</body></html>");
        sourceTextDescription.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        sourceInfoPanel.add(sourceTextDescription);

        JLabel sourceExplanationLabel = new JLabel("Select the game mode here (preliminary version for modes other than the base game):");


        aComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                sourceTextDescription.setText("<html><body width=600><b>Description for <i>"
                        + aComboBox.getSelectedItem().toString() + "</i></b>: "
                        + mgmr.getGameMode(aComboBox.getSelectedItem().toString()).getDescription()
                        + "</body></html>");
            }
        });

        sourcePanel.add(sourceExplanationLabel);
        sourcePanel.add(aComboBox);



        //make it editable further down the line once it's properly tested
        //aComboBox.setEditable(true);
        JPanel explanationPanel = new JPanel();
        explanationPanel.setLayout(new BoxLayout(explanationPanel, BoxLayout.Y_AXIS));
        explanationPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel autoSpawnExplanationLabel = new JLabel("Please paste (CTRL-V can be used to paste text copied with CTRL-C from a browser) ");
        JLabel autoSpawnExplanationLabel2 = new JLabel("a voidstate url or ID, YASB url, FABS url, or raw XWS JSON.");
        JLabel autoSpawnExplanationLabel3 = new JLabel("If the list uses new elements, a download delay may occur");
        SwingLink yasbLink = new SwingLink("Yet Another Squad Builder", yasbURL);
        SwingLink voidstateLink = new SwingLink("Voidstate Squadron Builder", voidURL);
        SwingLink fabsLink = new SwingLink("Fab's Squadron Builder", fabsURL);

        explanationPanel.add(autoSpawnExplanationLabel);
        explanationPanel.add(autoSpawnExplanationLabel2);
        explanationPanel.add(autoSpawnExplanationLabel3);
        explanationPanel.add(yasbLink);
        explanationPanel.add(voidstateLink);
        explanationPanel.add(fabsLink);


        rootPanel.add(sourcePanel);
        rootPanel.add(sourceInfoPanel);
        rootPanel.add(Box.createRigidArea(new Dimension(0,8)));
        rootPanel.add(new JSeparator());
        rootPanel.add(Box.createRigidArea(new Dimension(0,8)));
        rootPanel.add(explanationPanel);
        JFrame frame = new JFrame();
        frame.setPreferredSize(new Dimension(800,500));
        frame.add(rootPanel);


        String userInput = "";
        userInput = JOptionPane.showInputDialog(frame, rootPanel, "Squad AutoSpawn for player " + Integer.toString(playerInfo.getSide()), JOptionPane.PLAIN_MESSAGE);

        if(userInput==null){
            logToChat("Error - could not find anything in the input field of the autospawn dialog.");
            return;
        }
        if(userInput.isEmpty()) {
            logToChat("Error - could not find anything in the input field of the autospawn dialog.");
            return;
        }

        int whichEdition = 0;
        XWSList xwsList = new XWSList();
        XWSList2 xwsList2 = new XWSList2();

        if (userInput == null || userInput.length() == 0) {
            return;
        }
        userInput = userInput.trim();
        //first 2 cases - raw json, but must decide between 1e and 2e
        if (userInput.startsWith("{")) {
            if(isListFor2ndEdition(userInput) == false) {
                xwsList = loadListFromRawJson(userInput);
                whichEdition = 1;
            }
            else {
                //TODOSPAWN2E very complex forking here - MIGHT DITCH THIS
                xwsList2 = loadListFromRawJson2(userInput);
                whichEdition = 2;
            }
        }
        else if(userInput.equals("tcdemo1")){
            hackSpawnTCdemo1(playerMap);
        }
        else if(userInput.equals("tcdemo2")){
            hackSpawnTCdemo2(playerMap);
        }
        else {
            whichEdition = 1;
            try {xwsList = loadListFromUrl(userInput);}
            catch(Exception e){logToChat("Was not able to load the list");}
            return;
        }

        try {
            if(!"Base Game".equals(aComboBox.getSelectedItem().toString()))
            {
                logToChat("Attempting to load a squad in a mode that's not the base game");
                loadData("true".equals(mgmr.getGameMode(aComboBox.getSelectedItem().toString()).getWantFullControl())?true:false,
                        mgmr.getGameMode(aComboBox.getSelectedItem().toString()).getDispatchersURL());
            }else {
                if(whichEdition==1) loadData();
                else if(whichEdition==2) loadData2();
            }
            validateList(xwsList);
        }catch(XWSpawnException e)
        {
            // first replace the list with the "cleaned" list
            xwsList = e.getNewList();

            // now alert the user
            ArrayList<String> messages = e.getMessages();
            for(String message : messages)
            {
                mic.Util.logToChat(message);
            }

        }

        if (xwsList == null || xwsList.getPilots() == null || xwsList.getPilots().size() == 0) {
            return;
        }

        // If the list includes a yv666 with Hound's Tooth upgrade, add the nashtah pup ship
        xwsList = handleHoundsTooth(xwsList);

        VassalXWSListPieces pieces = slotLoader.loadListFromXWS(xwsList);

        Point startPosition = new Point(150, 150);
        Point dialstartPosition = new Point(300, 100);
        Point tokensStartPosition = new Point(300, 220);
        Point tlStartPosition = new Point(300, 290);
        int shipBaseY = 110;

        int fudgePilotUpgradeFrontier = -50;
        int totalPilotHeight = 0;
        int totalDialsWidth = 0;
        int totalTokenWidth = 0;
        int totalTLWidth = 0;

        List<GamePiece> shipBases = Lists.newArrayList();

        // flag - does this pilot have the Jabba The Hutt upgrade card assigned
        boolean squadHasJabba = false;

        // check to see if any pilot in the squad has Jabba the Hutt Crew equipped
        for (VassalXWSPilotPieces ship : pieces.getShips()) {
            for (VassalXWSPilotPieces.Upgrade tempUpgrade : ship.getUpgrades()) {

                if(tempUpgrade.getXwsName().equals("jabbathehutt"))
                {
                    squadHasJabba = true;
                    break;
                }

            }
        }

        List<Point> illicitLocations = Lists.newArrayList(); // list of coordinates to place illicit tokens
        int illicitYOffset = 50; // Y-Offset of where to place illicit tokens relative to the upgrade card
        PieceSlot illicitPieceSlot = null;

        for (VassalXWSPilotPieces ship : pieces.getShips())
        {

            // flag - does this pilot have the Extra Munitions upgrade card assigned
            boolean pilotHasExtraMunitions = false;

            // flag - does this pilot have the  Silos upgrade card assigned
            boolean pilotHasOrdnanceSilos = false;

            if(ship.getPilotData().getXws().equals("nashtahpuppilot")) //<- NULL HERE?
            {
                MasterPilotData.PilotData nashtahPilotData = ship.getPilotData();
                nashtahPilotData.setSkill(houndsToothPilotSkill);
                ship.setPilotData(nashtahPilotData);

            }

            // ======================================================
            // Generate the ship base pieces
            // ======================================================
            GamePiece shipPiece = GamePieceGenerator.generateShip(ship);

            shipBases.add(shipPiece);


            // ======================================================
            // Generate the Pilot Pieces
            // ======================================================
            GamePiece pilotPiece = GamePieceGenerator.generatePilot(ship);

            int pilotWidth = (int) pilotPiece.boundingBox().getWidth();
            int pilotHeight = (int) pilotPiece.boundingBox().getHeight();
            totalPilotHeight += pilotHeight;
            spawnPiece(pilotPiece, new Point(
                            (int) startPosition.getX(),
                            (int) startPosition.getY() + totalPilotHeight),
                    playerMap);


            // ======================================================
            // Generate the Dial
            // ======================================================
            GamePiece dialPiece = GamePieceGenerator.generateDial(ship);

            int dialWidth = (int) dialPiece.boundingBox().getWidth();
            spawnPiece(dialPiece, new Point((int) dialstartPosition.getX() + totalDialsWidth, (int) dialstartPosition.getY()), playerMap);
            totalDialsWidth += dialWidth;

            int totalUpgradeWidth = 0;

            //Check to see if this pilot has extra munitions or Ordnance Silos
            for (VassalXWSPilotPieces.Upgrade tempUpgrade : ship.getUpgrades()) {
               // GamePiece tempPiece = tempUpgrade.cloneGamePiece();
                if(tempUpgrade.getXwsName().equalsIgnoreCase("extramunitions")) {
                    pilotHasExtraMunitions = true;
                }else if(tempUpgrade.getXwsName().equalsIgnoreCase("ordnancesilos")) {
                    pilotHasOrdnanceSilos = true;
                }
            }

            List<Point> ordnanceLocations = Lists.newArrayList(); // list of coordinates to place ordnance tokens
            int ordnanceYOffset = 50; // Y-Offset of where to place ordnance tokens relative to the upgrade card


            // ======================================================
            //TODO Generate the Upgrades
            // ======================================================
            for (VassalXWSPilotPieces.Upgrade upgrade : ship.getUpgrades())
            {

                GamePiece upgradePiece = GamePieceGenerator.generateUpgrade(upgrade);

              //  GamePiece upgradePiece = upgrade.cloneGamePiece();


/*
                // if this is an unreleased upgrade, we have to set the name
                if(upgrade.getPieceSlot().getConfigureName().startsWith("Stem"))
                {
                    // we need to set the upgrade name
                    upgradePiece.setProperty("Upgrade Name",upgrade.getUpgradeData().getName());
                }else{
                    // we need to use a stem
                }
                */

                // if pilot has extra munitions, we will collect the positions of each card that can take it so we can add the tokens later
                if(pilotHasExtraMunitions)
                {
                    String slotName = upgrade.getUpgradeData().getSlot();
                    if(slotName.equals("Bomb") || slotName.equals("Missile") || slotName.equals("Torpedo"))
                    {
                        ordnanceLocations.add(new Point((int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier,
                                (int) startPosition.getY() + totalPilotHeight + ordnanceYOffset));
                    }

                }

                // if pilot has Ordnance Silos, we will collect the positions of each card that can take it so we can add the tokens later
                if(pilotHasOrdnanceSilos) {

                    String slotName = upgrade.getUpgradeData().getSlot();
                    if (slotName.equals("Bomb") && !upgrade.getXwsName().equals("ordnancesilos")) {
                        // add three ordnance token locations
                        ordnanceLocations.add(new Point((int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier,
                                (int) startPosition.getY() + totalPilotHeight + ordnanceYOffset));

                        ordnanceLocations.add(new Point((int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier + 5,
                                (int) startPosition.getY() + totalPilotHeight + ordnanceYOffset + 10));

                        ordnanceLocations.add(new Point((int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier + 10,
                                (int) startPosition.getY() + totalPilotHeight + ordnanceYOffset + 20));
                    }

                }

                // if squad has Jabba the Hutt, we will collect the positions of each card that can take illicit tokens
                // so we can add the tokens later
                if(squadHasJabba)
                {
                    String slotName = upgrade.getUpgradeData().getSlot();
                    if(slotName.equals("Illicit"))
                    {
                        // add the coordinates to the list of ordnance token locations
                        illicitLocations.add(new Point((int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier,
                                (int) startPosition.getY() + totalPilotHeight + illicitYOffset));
                    }

                }

                spawnPiece(upgradePiece, new Point(
                                (int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier,
                                (int) startPosition.getY() + totalPilotHeight),
                        playerMap);
                totalUpgradeWidth += upgradePiece.boundingBox().getWidth();
            } //loop to next upgrade


            // ======================================================
            //TODO Generate the Conditions
            // ======================================================
            for (VassalXWSPilotPieces.Condition condition: ship.getConditions()) {
                GamePiece conditionPiece = GamePieceGenerator.generateCondition(condition);
                /*
                GamePiece conditionPiece = newPiece(condition.getPieceSlot());
                if(condition.getPieceSlot().getConfigureName().startsWith("Stem"))
                {
                    // this is an unreleased condition.  Need to set the name
                    conditionPiece.setProperty("Upgrade Name",condition.getXwsName());
                }*/
                spawnPiece(conditionPiece, new Point(
                                (int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier,
                                (int) startPosition.getY() + totalPilotHeight),
                        playerMap);
                totalUpgradeWidth += conditionPiece.boundingBox().getWidth();


                // spawn the condition token
                GamePiece conditionTokenPiece = GamePieceGenerator.generateConditionToken(condition);
                spawnPiece(conditionTokenPiece, new Point(
                                (int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier,
                                (int) startPosition.getY() + totalPilotHeight),
                        playerMap);
                totalUpgradeWidth += conditionTokenPiece.boundingBox().getWidth();
            } //loop to next condition

            // ======================================================
            // Add all of the appropriate tokens
            // ======================================================
            for (GamePiece token : ship.getTokensForDisplay()) {
                PieceSlot pieceSlot = new PieceSlot(token);
                if ("Target Lock".equals(pieceSlot.getConfigureName())) {//if a target lock token, place elsewhere
                    spawnPiece(token, new Point(
                                    (int) tokensStartPosition.getX() + totalTLWidth,
                                    (int) tlStartPosition.getY()),
                            playerMap);
                    totalTLWidth += token.boundingBox().getWidth();
                }else if("Ordnance".equals(pieceSlot.getConfigureName()))
                {
                    // place the ordnance tokens
                    for(Point aPoint : ordnanceLocations)
                    {
                        GamePiece ordnanceToken = newPiece(pieceSlot);
                        spawnPiece(ordnanceToken, aPoint, playerMap);
                    }
                }else if("Illicit".equals(pieceSlot.getConfigureName()))
                {
                    // just store the illicit piece slot.
                    illicitPieceSlot = pieceSlot;
                } else {
                    spawnPiece(token, new Point(
                                    (int) tokensStartPosition.getX() + totalTokenWidth,
                                    (int) tokensStartPosition.getY()),
                            playerMap);
                    totalTokenWidth += token.boundingBox().getWidth();
                }
            }// loop to next token*/
        } //loop to next pilot

        // ======================================================
        // place necessary illicit tokens throughout the squad
        // ======================================================
        for(Point aPoint : illicitLocations)
        {
            GamePiece illicitToken = newPiece(illicitPieceSlot);
            spawnPiece(illicitToken, aPoint, playerMap);
        }
        UserInformer.informUser();

        int shipBaseX = (int) dialstartPosition.getX() + totalDialsWidth - 30;
        for (GamePiece piece : shipBases) {
            int halfBase = (int) (piece.getShape().getBounds2D().getWidth() / 2.0);
            spawnPiece(piece, new Point(shipBaseX + halfBase, shipBaseY), playerMap);
            shipBaseX += piece.getShape().getBounds2D().getWidth() + 10.0;
        }

        int obstacleX = (int) dialstartPosition.getX() + totalDialsWidth - 30;
        int obstacleStartY = shipBaseY + 200;
        for (GamePiece obstacle : pieces.getObstaclesForDisplay()) {
            int halfSize = (int) (obstacle.boundingBox().getWidth() / 2.0);
            spawnPiece(obstacle, new Point(obstacleX + halfSize, obstacleStartY), playerMap);
            obstacleX += obstacle.getShape().getBounds().getWidth();
        }

        String listName = xwsList.getName();
        logToChat("The '" + aComboBox.getSelectedItem().toString() + "' game mode was used to spawn a %s point list%s loaded from %s", pieces.getSquadPoints(),
                listName != null ? " '" + listName + "'" : "", xwsList.getXwsSource());

    }


    private void validateList2(XWSList2 list)
    {
        XWSList2 newList = null;
        for(XWSList2.XWSPilot2 pilot : list.getPilots())
        {
            String shipXws = pilot.getShip();
            String pilotXws = pilot.getXws();
            if(MasterShipData.getShipData(shipXws) == null)
            {
                
            }
        }
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
/*
    private void downloadNecessaryImages(List<VassalXWSPilotPieces> ships)
    {

        // send the command to download the images

        // loop through each ship/pilot to see what images are needed
        // collect every image, because we don't know what other clients might not have
        ArrayList pilotImageList = new ArrayList();
        for (VassalXWSPilotPieces ship : ships) {

            // get the pilotImageName
            MasterShipData.ShipData shipData = ship.getShipData();
            MasterPilotData.PilotData pilotData = ship.getPilotData();


            Canonicalizer.getCanonicalFactionName(pilotData.getFaction());
            shipData.getXws();
            pilotData.getXws();

            String pilotCardImage = "Pilot_" + Canonicalizer.getCanonicalFactionName(pilotData.getFaction()) + "_" + shipData.getXws() + "_" + pilotData.getXws() + ".jpg";
            pilotImageList.add(pilotCardImage);
        }

        OTAImageDownloader.ImageDownloadCommand myImageDownloader = new OTAImageDownloader.ImageDownloadCommand(pilotImageList);
        myImageDownloader.executeCommand();
    }
*//*
    private GamePiece generatePilot(VassalXWSPilotPieces ship)
    {

        GamePiece newPilot = mic.Util.newPiece(ship.getPilotCard());
        if (ship.getShipNumber() != null && ship.getShipNumber() > 0) {
            newPilot.setProperty("Pilot ID #", ship.getShipNumber());
        } else {
            newPilot.setProperty("Pilot ID #", "");
        }

        // this is a stem card = fill it in

        MasterShipData.ShipData shipData = ship.getShipData();
        MasterPilotData.PilotData pilotData = ship.getPilotData();
    //    newPilot.setProperty("Ship Type",shipData.getName());
    //    newPilot.setProperty("Pilot Name",pilotData.getName());

        StemPilot.PilotGenerateCommand myShipGen = new StemPilot.PilotGenerateCommand(pilotData.getXws(),newPilot,pilotData.getFaction(),shipData.getXws(),pilotData.getName(),shipData.getName());

        myShipGen.execute();

        return newPilot;
*/



/*

        // find the 2 slots for the stem ships

        List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

        PieceSlot smallShipSlot = null;
        PieceSlot largeShipSlot = null;

        for (PieceSlot pieceSlot : pieceSlots) {
            String slotName = pieceSlot.getConfigureName();
            if(slotName.startsWith("ship -- Nu Stem Small Ship")&& smallShipSlot == null)
            {
                smallShipSlot = pieceSlot;
                continue;
            } else if(slotName.startsWith("ship -- Nu Stem Large Ship")&& largeShipSlot == null) {
                largeShipSlot = pieceSlot;
                continue;
            }
        }

        // grab the correct ship for the size of the ship
        GamePiece newShip = null;
        if(shipData.getSize().contentEquals("small"))
        {
            newShip = mic.Util.newPiece(smallShipSlot);
        }else if(shipData.getSize().contentEquals("large"))
        {
            newShip = mic.Util.newPiece(largeShipSlot);
        }


        // execute the command
        StemShip.ShipGenerateCommand myShipGen = new StemShip.ShipGenerateCommand(ship.getShipData().getXws(), newShip, faction, shipData.getSize());

        myShipGen.execute();

        //TODO add stats
        //       dial.setProperty("ShipXwsId",ship.getShipData().getXws());
        //      dial.setProperty("Pilot Name", getDisplayShipName(ship.getPilotData(),shipData));
        //     dial.setProperty("Craft ID #", getDisplayPilotName(ship.getPilotData(),shipData,ship.getShipNumber()));
        return newShip;

    }*/
/*
    private GamePiece generateShip(VassalXWSPilotPieces ship)
    {

        MasterShipData.ShipData shipData = ship.getShipData();

        String faction = ship.getPilotData().getFaction();


        // find the 2 slots for the stem ships

        List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

        PieceSlot smallShipSlot = null;
        PieceSlot largeShipSlot = null;

        for (PieceSlot pieceSlot : pieceSlots) {
            String slotName = pieceSlot.getConfigureName();
            if(slotName.startsWith("ship -- Nu Stem Small Ship")&& smallShipSlot == null)
            {
                smallShipSlot = pieceSlot;
                continue;
            } else if(slotName.startsWith("ship -- Nu Stem Large Ship")&& largeShipSlot == null) {
                largeShipSlot = pieceSlot;
                continue;
            }
        }

        // grab the correct ship for the size of the ship
        GamePiece newShip = null;
        if(shipData.getSize().contentEquals("small"))
        {
            newShip = mic.Util.newPiece(smallShipSlot);
        }else if(shipData.getSize().contentEquals("large"))
        {
            newShip = mic.Util.newPiece(largeShipSlot);
        }


        // execute the command
        StemShip.ShipGenerateCommand myShipGen = new StemShip.ShipGenerateCommand(ship.getShipData().getXws(), newShip, faction, shipData.getSize());

        myShipGen.execute();

        //TODO add stats
        newShip.setProperty("Craft ID #", getDisplayPilotName(ship.getPilotData(),shipData,ship.getShipNumber())); //is actually the pilot name
        newShip.setProperty("Pilot Skill", Integer.toString(ship.getPilotData().getSkill()));

        newShip.setProperty("Pilot Name", getDisplayShipName(ship.getPilotData(),shipData)); //is actually the ship name
        newShip.setProperty("Attack Rating", Integer.toString(ship.getShipData().getAttack()));
        newShip.setProperty("Defense Rating", Integer.toString(ship.getShipData().getAgility()));
        newShip.setProperty("Hull Rating", Integer.toString(ship.getShipData().getHull()));
        newShip.setProperty("Shield Rating", Integer.toString(ship.getShipData().getShields()));
 //       dial.setProperty("ShipXwsId",ship.getShipData().getXws());
  //      dial.setProperty("Pilot Name", getDisplayShipName(ship.getPilotData(),shipData));
   //     dial.setProperty("Craft ID #", getDisplayPilotName(ship.getPilotData(),shipData,ship.getShipNumber()));
        return newShip;
    }*/
/*
    // generate an autogenerated dial
    private GamePiece generateDial(VassalXWSPilotPieces ship)
    {

        MasterShipData.ShipData shipData = ship.getShipData();

        String faction = ship.getPilotData().getFaction();

        PieceSlot rebelDialSlot = null;
        PieceSlot imperialDialSlot = null;
        PieceSlot scumDialSlot = null;

        // find the 3 slots for the auto-gen dials
        List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

        for (PieceSlot pieceSlot : pieceSlots) {
            String slotName = pieceSlot.getConfigureName();
            if (slotName.startsWith("Rebel Stem Dial") && rebelDialSlot == null) {
                rebelDialSlot = pieceSlot;
                continue;
            } else if (slotName.startsWith("Imperial Stem Dial") && imperialDialSlot == null) {
                imperialDialSlot = pieceSlot;
                continue;
            } else if (slotName.startsWith("Scum Stem Dial") && scumDialSlot == null) {
                scumDialSlot = pieceSlot;
                continue;
            }
        }

        // grab the correct dial for the faction
        GamePiece dial = null;
        if(faction.contentEquals("Rebel Alliance") || faction.contentEquals("Resistance")) {
            dial = mic.Util.newPiece(rebelDialSlot);
        }else if(faction.contentEquals("Galactic Empire") || faction.contentEquals("First Order")) {
            dial = mic.Util.newPiece(imperialDialSlot);
        }else if(faction.contentEquals("Scum and Villainy")) {
            dial = mic.Util.newPiece(scumDialSlot);
        }

        // execute the command
        StemDial.DialGenerateCommand myDialGen = new StemDial.DialGenerateCommand(ship.getShipData().getXws(), dial, faction);

        myDialGen.execute();

        dial.setProperty("ShipXwsId",ship.getShipData().getXws());
        dial.setProperty("Pilot Name", getDisplayShipName(ship.getPilotData(),shipData));
        dial.setProperty("Craft ID #", getDisplayPilotName(ship.getPilotData(),shipData,ship.getShipNumber()));
        return dial;
    }*/
/*
    private String getDisplayPilotName(MasterPilotData.PilotData pilotData, MasterShipData.ShipData shipData, Integer shipNumber )
    {
        String pilotName = "";
        if (pilotData != null) {
            pilotName = Acronymizer.acronymizer(
                    pilotData.getName(),
                    pilotData.isUnique(),
                    shipData.hasSmallBase());
        }

        if (shipNumber != null && shipNumber > 0) {
            pilotName += " " + shipNumber;
        }
        return pilotName;
    }
*/
/*
    private String getDisplayShipName(MasterPilotData.PilotData pilotData, MasterShipData.ShipData shipData) {
        String shipName = "";
        if (pilotData != null) {
            shipName = Acronymizer.acronymizer(
                    pilotData.getShip(),
                    pilotData.isUnique(),
                    shipData.hasSmallBase());
        }

        return shipName;
    }
*/

    public void addTo(Buildable parent) {
        loadData();

        for (int i = 1; i <= 8; i++) {
            final int playerId = i;

            JButton b = new JButton("Squad Spawn");
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

    private void loadData() {
        this.slotLoader.loadPieces();
        MasterPilotData.loadData();
        MasterUpgradeData.loadData();
        MasterShipData.loadData();
    }
    private void loadData2() {
        this.slotLoader.loadPieces();
        MasterPilotData.loadData2();
        MasterUpgradeData.loadData2();
        MasterShipData.loadData2();
    }

    private void loadData(Boolean wantFullControl, String altDispatcherString) {
        this.slotLoader.loadPieces();
        mic.Util.logToChat("inside AutoSquadSpawn.loadData()");
        MasterPilotData.loadData(wantFullControl, altDispatcherString);
        mic.Util.logToChat("PilotDataLoaded");
        MasterUpgradeData.loadData(wantFullControl, altDispatcherString);
        mic.Util.logToChat("UpgradeDataLoaded");
        MasterShipData.loadData(wantFullControl, altDispatcherString);
    }


    private XWSList loadListFromUrl(String userInput) {
        try {
            URL translatedURL = XWSUrlHelper.translate(userInput);

            if (translatedURL == null) {
                logToChat("Invalid list url detected, please try again");
                return null;
            }
            XWSList xwsList = loadRemoteJson(translatedURL, XWSList.class);

            xwsList.setXwsSource(userInput);
            return xwsList;

        } catch (Exception e) {
            logToChat("Unable to translate xws url '%s': %s", userInput, e.toString());
            return null;
        }
    }

    //this is a quick json read to see if it's a 2nd edition squad. if so, report true at once.
    private boolean isListFor2ndEdition(String userInput) {
        try {
            XWSList list = getMapper().readValue(userInput, XWSList.class);
            list.setXwsSource("JSON");

            if("second".equals(list.getFfgEdition())){
                return true;
            }
            return false;
        } catch (Exception e) {
            logToChat("Unable to load raw JSON list '%s': %s", userInput, e.toString());
            return false;
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
    private XWSList2 loadListFromRawJson2(String userInput){
        try{
            XWSList2 list2 = getMapper().readValue(userInput, XWSList2.class);
            list2.setXwsSource("JSON");
            return list2;
        }catch (Exception e) {
            logToChat("Unable to load raw JSON list '%s': %s", userInput, e.toString());
            return null;
        }
    }


    private XWSList handleHoundsTooth(XWSList list)
    {


        for (XWSList.XWSPilot pilot : list.getPilots())
        {
            if (pilot.getShip().equals("yv666"))
            {
                // check for the hounds tooth upgrade
                java.util.Map<String, List<String>> upgrades = pilot.getUpgrades();
                List titleList = upgrades.get("title");
                if(titleList != null)
                {

                    for(Object title : titleList)
                    {
                        if(((String)title).equals("houndstooth"))
                        {
                            // found the hounds tooth

                            listHasHoundsTooth = true;

                            houndsToothPilotSkill = MasterPilotData.getPilotData("yv666", pilot.getName(),list.getFaction()).getSkill();
                            break;
                        }
                    }
                }

            }
            if(listHasHoundsTooth)
            {
                break;
            }
        }

        if(listHasHoundsTooth)
        {
            // add the pup
            java.util.Map<String, List<String>> upgrades = Maps.newHashMap();
            java.util.Map<String, java.util.Map<String, String>> vendor = Maps.newHashMap();
            XWSList.XWSPilot pupPilot = new XWSList.XWSPilot("nashtahpuppilot","z95headhunter",upgrades,vendor,null);
            list.addPilot(pupPilot);
        }
        return list;
    }

    private XWSList loadListFromUserInput(String userInput) {
        if (userInput == null || userInput.length() == 0) {
            return null;
        }
        userInput = userInput.trim();
        if (userInput.startsWith("{")) {
            return loadListFromRawJson(userInput);
        }
        return loadListFromUrl(userInput); //if there's ever a web squad builder that has a backend thing that can prepare a community driven squad json, then TODOSPAWN2E deal with those cases in here
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
