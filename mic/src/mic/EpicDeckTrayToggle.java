package mic;

import static mic.Util.getCurrentPlayer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

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
 */
public class EpicDeckTrayToggle extends AbstractConfigurable {

    private List<JButton> toggleButtons = Lists.newArrayList();
    private Multimap<Integer, GamePiece> removedPlayerEpicPieces = HashMultimap.create();

    private synchronized void epicMaskToggle(int playerId) {
        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        if (playerInfo.getSide() != playerId) {
            return;
        }

        if (removedPlayerEpicPieces.get(playerId).size() > 0) {
            showOnePlayerEpic(playerId);
        } else {
            hideOnePlayerEpic(playerId);
        }
    }

    private void hideOnePlayerEpic(int playerId) {
        toggleButtons.get(playerId - 1).setText("Activate Epic");

        Map playerMap = getPlayerMap(playerId);
        for (GamePiece piece : playerMap.getAllPieces()) {
            if (piece instanceof Deck) {
                Deck deck = (Deck) piece;
                if (deck.getDeckName() != null && deck.getDeckName().contains("Huge")) {
                    removedPlayerEpicPieces.put(playerId, piece);
                    continue;
                }
            } else if (piece instanceof VASSAL.counters.Stack) {
                if (piece.getName() != null && piece.getName().contains("/ 10)")) {
                    removedPlayerEpicPieces.put(playerId, piece);
                    continue;
                }
            }
        }
        Board board = playerMap.getBoardByName("Player " + playerId);
        board.setAttribute("image", "observer_hand_background.jpg");
        playerMap.setBoards(Lists.newArrayList(board.copy()));
        for (GamePiece piece : removedPlayerEpicPieces.get(playerId)) {
            playerMap.removePiece(piece);
        }
    }

    private void showOnePlayerEpic(int playerId) {
        Map playerMap = getPlayerMap(playerId);
        Board board = playerMap.getBoardByName("Player " + playerId);
        board.setAttribute("image", "player_hand_background.jpg");
        playerMap.setBoards(Lists.newArrayList(board.copy()));
        for (GamePiece piece : removedPlayerEpicPieces.get(playerId)) {
            playerMap.addPiece(piece);
        }
        removedPlayerEpicPieces.get(playerId).clear();
        toggleButtons.get(playerId - 1).setText("Disable Epic");
    }


    public void addTo(Buildable parent) {
        for (int i = 1; i <= 8; i++) {
            final int playerId = i;

            JButton b = new JButton("Disable Epic");
            b.setAlignmentY(0.0F);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    epicMaskToggle(playerId);
                }
            });
            toggleButtons.add(b);

            Map playerMap = getPlayerMap(i);
            playerMap.getToolBar().add(b);
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
