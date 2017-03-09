package mic;

import static mic.Util.logToChat;
import static mic.Util.newPiece;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.*;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;

/**
 * Created by Mic on 12/02/2017.
 */
public class AutoSquadSpawn extends AbstractConfigurable {

    private JButton loadFromXwsUrlButton;
    private VassalXWSPieceLoader slotLoader = new VassalXWSPieceLoader();

    private void spawnPiece(GamePiece piece, Point position) {
        Command placeCommand = getMap().placeOrMerge(piece, position);
        placeCommand.execute();
        GameModule.getGameModule().sendAndLog(placeCommand);
    }

    private void loadFromXwsButtonPressed() {

        String url = null;

        try {
            url = JOptionPane.showInputDialog("Please paste a voidstate url or ID, YASB url, or FABS url");
        } catch (Exception e) {
            logToChat("Unable to process url, please try again");
        }
        if (url == null || url.length() == 0) {
            return;
        }

        URL translatedURL = null;
        try {
            translatedURL = XWSUrlHelper.translate(url);
            if (translatedURL == null) {
                logToChat("Invalid list url detected, please try again");
                return;
            }
        } catch (Exception e) {
            logToChat("Unable to translate xws url: \n" + e.toString());
            return;
        }

        XWSList xwsList = XWSFetcher.fetchFromUrl(translatedURL.toString());
        VassalXWSListPieces pieces = slotLoader.loadListFromXWS(xwsList);

        Point startPosition = new Point(500, 60);
        Point tokensStartPosition = new Point(500, 180);
        Point dialstartPosition = new Point(500, 80);
        Point tlStartPosition = new Point(500, 240);

        int fudgePilotUpgradeFrontier = -50;
        int totalPilotHeight = 0;
        int totalDialsWidth = 0;
        int totalTokenWidth = 0;
        int totalTLWidth = 0;

        for (VassalXWSPilotPieces ship : pieces.getShips()) {
            logToChat(String.format("Spawning pilot: %s", ship.getPilotCard().getConfigureName()));

            GamePiece pilotPiece = ship.clonePilotCard();
            int pilotWidth = (int) pilotPiece.boundingBox().getWidth();
            int pilotHeight = (int) pilotPiece.boundingBox().getHeight();
            totalPilotHeight += pilotHeight;
            spawnPiece(pilotPiece, new Point(
                    (int) startPosition.getX(),
                    (int) startPosition.getY() + totalPilotHeight));
            GamePiece shipPiece = ship.cloneShip();
            spawnPiece(shipPiece, new Point(
                    (int) startPosition.getX() - pilotWidth,
                    (int) startPosition.getY() + totalPilotHeight + 20));

            GamePiece dialPiece = ship.cloneDial();
            int dialWidth = (int) dialPiece.boundingBox().getWidth();
            spawnPiece(dialPiece, new Point(
                    (int) dialstartPosition.getX() + totalDialsWidth,
                    (int) dialstartPosition.getY()));
            totalDialsWidth += dialWidth;

            int totalUpgradeWidth = 0;
            for (VassalXWSPilotPieces.Upgrade upgrade : ship.getUpgrades()) {
                GamePiece upgradePiece = upgrade.cloneGamePiece();
                spawnPiece(upgradePiece, new Point(
                        (int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier,
                        (int) startPosition.getY() + totalPilotHeight));

                totalUpgradeWidth += upgradePiece.boundingBox().getWidth();
            } //loop to next upgrade

            for (PieceSlot conditionSlot : ship.getConditions()) {
                GamePiece conditionPiece = newPiece(conditionSlot);
                spawnPiece(conditionPiece, new Point(
                        (int) startPosition.getX() + pilotWidth + totalUpgradeWidth + fudgePilotUpgradeFrontier,
                        (int) startPosition.getY() + totalPilotHeight));

                totalUpgradeWidth += conditionPiece.boundingBox().getWidth();
            } //loop to next condition


            for (GamePiece token : ship.getTokensForDisplay()) {
                PieceSlot pieceSlot = new PieceSlot(token);
                if ("Target Lock".equals(pieceSlot.getConfigureName())) {//if a target lock token, place elsewhere
                    spawnPiece(token, new Point(
                            (int) tokensStartPosition.getX() + totalTLWidth,
                            (int) tlStartPosition.getY()));
                    totalTLWidth += token.boundingBox().getWidth();
                } else {
                    spawnPiece(token, new Point(
                            (int) tokensStartPosition.getX() + totalTokenWidth,
                            (int) tokensStartPosition.getY()));
                    totalTokenWidth += token.boundingBox().getWidth();
                }
            }// loop to next token*/
        } //loop to next pilot

        int totalObstacleWidth = (int) dialstartPosition.getX() + totalDialsWidth + 150;
        int obstacleStartY = (int) dialstartPosition.getY();
        for (GamePiece obstacle : pieces.getObstaclesForDisplay()) {
            spawnPiece(obstacle, new Point(totalObstacleWidth, obstacleStartY));
            totalObstacleWidth += obstacle.getShape().getBounds().getWidth();
        }

        String listName = xwsList.getName();
        logToChat(String.format("%s point list '%s' loaded from %s", pieces.getSquadPoints(),
                listName != null ? " " + listName : "",
                url));

    }

    public void removeFrom(Buildable parent) {
        final Map map = getMap();
        map.getToolBar().remove(loadFromXwsUrlButton);
    }


    public void addTo(Buildable parent) {
        final Map map = getMap();
        loadFromXwsUrlButton = new JButton("Squad Spawn");
        loadFromXwsUrlButton.setAlignmentY(0.0F);
        loadFromXwsUrlButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loadFromXwsButtonPressed();
            }
        });
        map.getToolBar().add(loadFromXwsUrlButton);
    }

    private Map getMap() {
        for (Map loopMap : GameModule.getGameModule().getComponentsOf(Map.class)) {
            if ("Contested Sector".equals(loopMap.getMapName())) {
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

    public VASSAL.build.module.documentation.HelpFile getHelpFile() {
        return null;
    }
    // </editor-fold>
}
