package mic;

import static mic.Util.getCurrentPlayer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;
import java.util.List;

import javax.swing.*;
import com.google.common.collect.Lists;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.counters.Deck;
import VASSAL.counters.GamePiece;

/**
 * Created by mjuneau on 2017-03-09.
 * epic trays are initially at y=-1000, can be toggled to y=75
 * epic tray counters are initially at y=-210, can be toggled to y=210
 *
 * Added functionality on 2018-04-05
 * Adds a combo box that will point to the source of autospawn to use
 */



public class EpicDeckTrayToggle extends AbstractConfigurable {

    private List<JButton> toggleButtons = Lists.newArrayList();
    private List<JComboBox> autoSpawnSourceComboBoxes = Lists.newArrayList();


    private synchronized void epicMaskToggle(int playerId) {
        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        if (playerInfo.getSide() != playerId) {
            return;
        }

        Map playerMap = getPlayerMap(playerId);
        Board board = playerMap.getBoardByName("Player " + playerId);

        for (GamePiece piece : playerMap.getAllPieces()) {
            if (piece instanceof Deck) {
                Deck deck = (Deck) piece;
                if (deck.getDeckName() != null && deck.getDeckName().contains("Huge")) {
                    if(deck.getPosition().getY() == -1000)
                    {
                        deck.setPosition(new Point((int)deck.getPosition().getX(),75));
                        toggleButtons.get(playerId - 1).setText("Disable Epic");
                        board.setAttribute("image", "player_hand_background.jpg");
                    }
                    else
                    {
                        deck.setPosition(new Point((int)deck.getPosition().getX(),-1000));
                        toggleButtons.get(playerId - 1).setText("Activate Epic");
                        board.setAttribute("image", "observer_hand_background.jpg");
                    }
                    continue;
                }
            } else if (piece instanceof VASSAL.counters.Stack) {
                if (piece.getName() != null && piece.getName().contains("/ 10)")) {
                    if(piece.getPosition().getY() == -210) piece.setPosition(new Point((int)piece.getPosition().getX(),210));
                    else piece.setPosition(new Point((int)piece.getPosition().getX(),-210));
                    continue;
                }
            }
        }

        playerMap.setBoards(Lists.newArrayList(board.copy()));


    }

    public void addTo(Buildable parent) {
        for (int i = 1; i <= 8; i++) {
            final int playerId = i;

            //Epic trays toggle buttons
            JButton b = new JButton("Activate Epic");
            b.setAlignmentY(0.0F);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    epicMaskToggle(playerId);
                }
            });
            toggleButtons.add(b);


            //Combo box for selecting the source of the xwing-data to use
            //if it can't access the list of sources on the web, make it base game by default
            String[] listOfXwingDataSources = {
                    "Base Game",
                    "X-Wing Supremacy"
            };

            JComboBox aComboBox = new JComboBox(listOfXwingDataSources);
            //make it editable further down the line once it's properly tested
            //aComboBox.setEditable(true);
            autoSpawnSourceComboBoxes.add(aComboBox);

            //Adding those elements to the player window toolbars
            Map playerMap = getPlayerMap(i);
            playerMap.getToolBar().add(b);
            playerMap.getToolBar().add(aComboBox);
        }
    }

    public void removeFrom(Buildable parent) {
        for (int i = 1; i <= 8; i++) {
            getPlayerMap(i).getToolBar().remove(toggleButtons.get(i - 1));
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
