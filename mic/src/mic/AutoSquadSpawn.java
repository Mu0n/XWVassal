package mic;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import javax.swing.*;


import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.Map;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.GamePiece;

import static mic.Util.logToChat;
import static mic.Util.newPiece;

/**
 * Created by Mic on 12/02/2017.
 */
public class AutoSquadSpawn extends AbstractConfigurable implements CommandEncoder,
        GameComponent {

    private int index = 0;
    private int minChange = 0;
    private int maxChange = 0;
    private Random rand = new Random();
    private JButton loadFromXwsUrlButton;
    private VassalXWSPieceLoader slotLoader = new VassalXWSPieceLoader();

    public void addToIndex(int change) {
        index += change;
    }

    public int getIndex() {
        return index;
    }

    public int newIncrement() {
        return (int) (rand.nextFloat() * (maxChange - minChange + 1))
                + minChange;
    }


    private void spawnPiece(GamePiece piece, Point position) {
        Command placeCommand = Map.getMapById("Map0").placeOrMerge(piece, position);
        placeCommand.execute();
        GameModule.getGameModule().sendAndLog(placeCommand);
    }

    private void loadFromXwsButtonPressed() {

        String url = null;

        try {
            url = JOptionPane.showInputDialog("Please paste a voidstate url or ID, YASB url, or FABS url");
        }
        catch ( Exception e ) {
            logToChat("Unable to process url, please try again");
        }
        if (url == null || url.length() == 0) {
            return;
        }

        URL translatedURL = null;
        try {
            translatedURL = XWSUrlHelper.translate(url);
            if ( translatedURL == null ) {
                logToChat("Invalid list url detected, please try again");
                return;
            }
        } catch (Exception e) {
            logToChat("Unable to translate xws url: \n" + e.toString());
            return;
        }

        XWSList xwsList = XWSFetcher.fetchFromUrl(translatedURL.toString());
        VassalXWSListPieces pieces = slotLoader.loadListFromXWS(xwsList);

        Point startPosition = new Point(500,50);
        int fudgePilotUpgradeFrontier = -50;
        int totalPilotHeight = 0;

        for (VassalXWSPilotPieces ship : pieces.getShips()) {
            logToChat(String.format("Spawning pilot: %s", ship.getPilotCard().getConfigureName()));

            GamePiece pilotPiece = ship.clonePilotCard();
            int pilotWidth = (int)pilotPiece.boundingBox().getWidth();
            int pilotHeight = (int)pilotPiece.boundingBox().getHeight();
            totalPilotHeight += pilotHeight;
            spawnPiece(pilotPiece, new Point(
                    (int)startPosition.getX(),
                    (int)startPosition.getY()+totalPilotHeight));

            GamePiece shipPiece = ship.cloneShip();
            int shipWidth = (int)shipPiece.boundingBox().getWidth();
            int shipHeight = (int)shipPiece.boundingBox().getHeight();
            spawnPiece(shipPiece, new Point(
                    (int)startPosition.getX()-pilotWidth,
                    (int)startPosition.getY()+totalPilotHeight+20));

            GamePiece dialPiece = ship.cloneDial();
            int dialWidth = (int)dialPiece.boundingBox().getWidth();
            int dialHeight = (int)dialPiece.boundingBox().getHeight();
            spawnPiece(dialPiece, new Point(
                    (int)startPosition.getX()-pilotWidth,
                    (int)startPosition.getY()+totalPilotHeight-dialHeight));

            int totalUpgradeWidth = 0;
            for (VassalXWSPilotPieces.Upgrade upgrade : ship.getUpgrades()) {
                GamePiece upgradePiece = upgrade.cloneGamePiece();
                spawnPiece(upgradePiece, new Point(
                        (int)startPosition.getX()+pilotWidth+totalUpgradeWidth+fudgePilotUpgradeFrontier,
                        (int)startPosition.getY()+totalPilotHeight));

                totalUpgradeWidth += upgradePiece.boundingBox().getWidth();
            } //loop to next upgrade

            for (PieceSlot conditionSlot: ship.getConditions()) {
                GamePiece conditionPiece = newPiece(conditionSlot);
                spawnPiece(conditionPiece, new Point(
                        (int)startPosition.getX()+pilotWidth+totalUpgradeWidth+fudgePilotUpgradeFrontier,
                        (int)startPosition.getY()+totalPilotHeight));

                totalUpgradeWidth += conditionPiece.boundingBox().getWidth();
            } //loop to next condition

            int totalChitWidth = 0;
            for (GamePiece token : ship.getTokensForDisplay()) {
                spawnPiece(token, new Point(
                        (int)startPosition.getX()+pilotWidth+totalUpgradeWidth+totalChitWidth+fudgePilotUpgradeFrontier,
                        (int)startPosition.getY()+totalPilotHeight));
                totalChitWidth += token.boundingBox().getWidth();
            }// loop to next token
        } //loop to next pilot

        String listName = xwsList.getName();
        logToChat(String.format("List%s loaded from %s",
                                listName != null ? " " + listName : "",
                                url));

    }

    public static final String MIN = "min";
    public static final String MAX = "max";

    public void setAttribute(String key, Object value) {
        if (MIN.equals(key)) {
            if (value instanceof String) {
                minChange = Integer.parseInt((String) value);
            } else if (value instanceof Integer) {
                minChange = ((Integer) value).intValue();
            }
        } else if (MAX.equals(key)) {
            if (value instanceof String) {
                maxChange = Integer.parseInt((String) value);
            } else if (value instanceof Integer) {
                maxChange = ((Integer) value).intValue();
            }
        }
    }

    public String[] getAttributeNames() {
        return new String[]{MIN, MAX};
    }

    public String[] getAttributeDescriptions() {
        return new String[]{"Minimum increment", "Maximum increment"};
    }

    public Class[] getAttributeTypes() {
        return new Class[]{Integer.class, Integer.class};
    }

    public String getAttributeValueString(String key) {
        if (MIN.equals(key)) {
            return "" + minChange;
        } else if (MAX.equals(key)) {
            return "" + maxChange;
        } else {
            return null;
        }
    }

    public void addTo(Buildable parent) {
        GameModule mod = (GameModule) parent;

        mod.addCommandEncoder(this);
        mod.getGameState().addGameComponent(this);

        loadFromXwsUrlButton = new JButton("Squad AutoSpawn from web");
        loadFromXwsUrlButton.setAlignmentY(0.0F);
        loadFromXwsUrlButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loadFromXwsButtonPressed();
            }
        });
        mod.getToolBar().add(loadFromXwsUrlButton);

    }

    public void removeFrom(Buildable parent) {
        GameModule mod = (GameModule) parent;

        mod.removeCommandEncoder(this);
        mod.getGameState().removeGameComponent(this);

        mod.getToolBar().remove(loadFromXwsUrlButton);
    }

    public VASSAL.build.module.documentation.HelpFile getHelpFile() {
        return null;
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public void setup(boolean gameStarting) {
        if (!gameStarting) {
            index = 0;
        }
    }

    public Command getRestoreCommand() {
        return new Incr2(this, index);
    }

    public static final String COMMAND_PREFIX = "TENSION:";

    public String encode(Command c) {
        if (c instanceof Incr2) {
            return COMMAND_PREFIX + ((Incr2) c).getChange();
        } else {
            return null;
        }
    }

    public Command decode(String s) {
        if (s.startsWith(COMMAND_PREFIX)) {
            return new Incr2(this,
                    Integer.parseInt(s.substring(COMMAND_PREFIX.length())));
        } else {
            return null;
        }
    }

    public static class Incr2 extends Command {

        private AutoSquadSpawn target;
        private int change;

        public Incr2(AutoSquadSpawn target, int change) {
            this.target = target;
            this.change = change;
        }

        protected void executeCommand() {
            target.addToIndex(change);
        }

        protected Command myUndoCommand() {
            return new Incr2(target, -change);
        }

        public int getChange() {
            return change;
        }
    }
}
