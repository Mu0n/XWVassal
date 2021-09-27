package mic;

import VASSAL.build.GameModule;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by mjuneau in 2019-01.
 * Allows a dial hide command to be sent to other players for the new style of dial spawned with StemDial2e. This command is normally called
 * by StemNuDial2e (a decorator found in that Game Piece)
 */
public class DialHideCommand extends Command {

    private GamePiece pieceInCommand;
    private final String pieceId;

    /**
     * used for when you want to trigger the undo of an hide
     */
    private final Boolean undoVersion;


    public DialHideCommand(GamePiece piece, Boolean doItNormally) {
        pieceInCommand = piece;
        pieceId = pieceInCommand.getId();
        undoVersion = doItNormally;
    }

    public DialHideCommand(String pieceIdPassed, Boolean doItNormally) {
        pieceId = pieceIdPassed;
        undoVersion = doItNormally;
    }


    protected void executeCommand() {

        if (pieceInCommand == null && pieceId != null) {
            Collection<GamePiece> pieces = GameModule.getGameModule().getGameState().getAllPieces();
            for (GamePiece piece : pieces) {
                if (piece.getId().equals(pieceId)) {
                    pieceInCommand = piece;
                }
            }
        }

        if (pieceInCommand == null) {
            return;
        }

        String ownerStr = pieceInCommand.getProperty("owner").toString();
        int ownerInt = Integer.parseInt(ownerStr);

        Embellishment chosenMoveEmb = (Embellishment)Util.getEmbellishment(pieceInCommand,"Layer - Chosen Move");
        Embellishment chosenSpeedEmb = (Embellishment)Util.getEmbellishment(pieceInCommand, "Layer - Chosen Speed");
        Embellishment sideHideEmb = (Embellishment) Util.getEmbellishment(pieceInCommand, "Layer - Side Hide");
        Embellishment centralHideEmb = (Embellishment) Util.getEmbellishment(pieceInCommand, "Layer - Central Hide");

        if (undoVersion) {//do the hide normally
            if (Util.getCurrentPlayer().getSide() == ownerInt) {
                sideHideEmb.setValue(1);
                centralHideEmb.setValue(0);
            } else {
                chosenMoveEmb.setValue(0); //Hide the maneuver
                chosenSpeedEmb.setValue(0); //Hide the speed
                sideHideEmb.setValue(0);
                centralHideEmb.setValue(1);
            }
            pieceInCommand.setProperty("isHidden", 1);
        } else {//reverse the hide
            chosenMoveEmb.setValue(1);

            //
            //lots of nonsense just to get back the correct speed layer
            //
            String dialString = pieceInCommand.getProperty("dialstring").toString();
            String[] values = dialString.split(",");
            int nbOfMoves = values.length;
            // Fetch the saved move from the dynamic property of the dial piece
            String savedMoveString = pieceInCommand.getProperty("selectedMove").toString();
            int savedMoveStringInt = Integer.parseInt(savedMoveString);
            String moveCode = values[savedMoveStringInt-1];
            int moveSpeedLayerToUse = Integer.parseInt(moveCode.substring(0,1)) + 1;

            chosenSpeedEmb.setValue(moveSpeedLayerToUse);
            //
            //end of nonsense
            //

            sideHideEmb.setValue(0);
            centralHideEmb.setValue(0);
            pieceInCommand.setProperty("isHidden", 0);
        }

        final VASSAL.build.module.Map map = pieceInCommand.getMap();
        map.repaint();
    }

    protected Command myUndoCommand() {
        return new DialHideCommand(pieceId, false);
    }

    /**
     * the following class is used to send the info to the other player whenever a dial generation command is issued, so it can be done locally on all machines playing/watching the game
     * only the ship XWS string is sent
     */
    public static class Dial2eHideEncoder implements CommandEncoder {
        private static final Logger logger = LoggerFactory.getLogger(StemNuDial2e.class);
        private static final String commandPrefix = "Dial2eHideEncoder=";
        private static final String itemDelim = "\t";

        public static DialHideCommand.Dial2eHideEncoder INSTANCE = new DialHideCommand.Dial2eHideEncoder();

        public Command decode(String command) {

            if (command == null || !command.contains(commandPrefix)) {
                return null;
            }
            command = command.substring(commandPrefix.length());
            String[] parts = command.split(itemDelim);
            try {
                Collection<GamePiece> pieces = GameModule.getGameModule().getGameState().getAllPieces();
                Boolean doItNorm = Boolean.parseBoolean(parts[1]);
                for (GamePiece piece : pieces) {
                    if(piece.getId().equals(parts[0])) {
                        return new DialHideCommand(piece, doItNorm);
                    }
                }
                return new DialHideCommand(parts[0], doItNorm);
            } catch(Exception e) {
                logger.info("Error decoding DialHideCommand - exception error");
                return null;
            }
        }

        public String encode(Command c) {
            if (!(c instanceof DialHideCommand)) {
                return null;
            }
            try {
                DialHideCommand dhc = (DialHideCommand) c;
                return commandPrefix + Joiner.on(itemDelim).join(dhc.pieceId, dhc.undoVersion.toString());
            } catch(Exception e) {
                logger.error("Error encoding DialHideCommand", e);
                return null;
            }
        }
    }
}
