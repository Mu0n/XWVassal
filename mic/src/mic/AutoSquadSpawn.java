package mic;

import static mic.Util.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import javax.swing.*;

import com.google.common.collect.Lists;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;

/**
 * Created by Mic on 12/02/2017.
 */
public class AutoSquadSpawn extends AbstractConfigurable {

    private VassalXWSPieceLoader slotLoader = new VassalXWSPieceLoader();
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

        String userInput = JOptionPane.showInputDialog("Please paste (CTRL-V can be used to paste text copied with CTRL-C from a browser) a voidstate url or ID, YASB url, FABS url, or raw XWS JSON");
        XWSList xwsList = loadListFromUserInput(userInput);
        if (xwsList == null) {
            return;
        }

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
        for (VassalXWSPilotPieces ship : pieces.getShips()) {
            logToChat("Spawning pilot: %s", ship.getPilotCard().getConfigureName());
            shipBases.add(ship.cloneShip());

            GamePiece pilotPiece = ship.clonePilotCard();
            int pilotWidth = (int) pilotPiece.boundingBox().getWidth();
            int pilotHeight = (int) pilotPiece.boundingBox().getHeight();
            totalPilotHeight += pilotHeight;
            spawnPiece(pilotPiece, new Point(
                            (int) startPosition.getX(),
                            (int) startPosition.getY() + totalPilotHeight),
                    playerMap);

            GamePiece dialPiece = ship.cloneDial();
            int dialWidth = (int) dialPiece.boundingBox().getWidth();
            spawnPiece(dialPiece, new Point(
                            (int) dialstartPosition.getX() + totalDialsWidth,
                            (int) dialstartPosition.getY()),
                    playerMap);
            totalDialsWidth += dialWidth;

            int totalUpgradeWidth = 0;
            for (VassalXWSPilotPieces.Upgrade upgrade : ship.getUpgrades()) {
                GamePiece upgradePiece = upgrade.cloneGamePiece();
                spawnPiece(upgradePiece, new Point(
                                (int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier,
                                (int) startPosition.getY() + totalPilotHeight),
                        playerMap);
                totalUpgradeWidth += upgradePiece.boundingBox().getWidth();
            } //loop to next upgrade


            for (PieceSlot conditionSlot : ship.getConditions()) {
                GamePiece conditionPiece = newPiece(conditionSlot);
                spawnPiece(conditionPiece, new Point(
                                (int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier,
                                (int) startPosition.getY() + totalPilotHeight),
                        playerMap);
                totalUpgradeWidth += conditionPiece.boundingBox().getWidth();
            } //loop to next condition

            for (GamePiece token : ship.getTokensForDisplay()) {
                PieceSlot pieceSlot = new PieceSlot(token);
                if ("Target Lock".equals(pieceSlot.getConfigureName())) {//if a target lock token, place elsewhere
                    spawnPiece(token, new Point(
                                    (int) tokensStartPosition.getX() + totalTLWidth,
                                    (int) tlStartPosition.getY()),
                            playerMap);
                    totalTLWidth += token.boundingBox().getWidth();
                } else {
                    spawnPiece(token, new Point(
                                    (int) tokensStartPosition.getX() + totalTokenWidth,
                                    (int) tokensStartPosition.getY()),
                            playerMap);
                    totalTokenWidth += token.boundingBox().getWidth();
                }
            }// loop to next token*/
        } //loop to next pilot
        UserInformer.informUser();

        int shipBaseX = (int) dialstartPosition.getX() + totalDialsWidth - 30;
        for (GamePiece piece : shipBases) {
            int halfBase = (int) (piece.boundingBox().getWidth() / 2.0);
            spawnPiece(piece, new Point(shipBaseX + halfBase, shipBaseY), playerMap);
            shipBaseX += piece.boundingBox().getWidth();
        }

        int obstacleX = (int) dialstartPosition.getX() + totalDialsWidth - 30;
        int obstacleStartY = shipBaseY + 200;
        for (GamePiece obstacle : pieces.getObstaclesForDisplay()) {
            int halfSize = (int) (obstacle.boundingBox().getWidth() / 2.0);
            spawnPiece(obstacle, new Point(obstacleX + halfSize, obstacleStartY), playerMap);
            obstacleX += obstacle.getShape().getBounds().getWidth();
        }

        String listName = xwsList.getName();
        logToChat("%s point list%s loaded from %s", pieces.getSquadPoints(),
                listName != null ? " '" + listName + "'" : "", xwsList.getXwsSource());
    }

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

    private XWSList loadListFromUserInput(String userInput) {
        if (userInput == null || userInput.length() == 0) {
            return null;
        }
        userInput = userInput.trim();
        if (userInput.startsWith("{")) {
            return loadListFromRawJson(userInput);
        }
        return loadListFromUrl(userInput);
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
