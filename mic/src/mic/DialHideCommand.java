package mic;

import VASSAL.build.GameModule;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class DialHideCommand extends Command {

    public GamePiece pieceInCommand;

    public DialHideCommand(GamePiece piece) {
        pieceInCommand = piece;
    }

    protected void executeCommand() {
        Embellishment chosenMoveEmb = (Embellishment)Util.getEmbellishment(pieceInCommand,"Layer - Chosen Move");
        Embellishment chosenSpeedEmb = (Embellishment)Util.getEmbellishment(pieceInCommand, "Layer - Chosen Speed");
        Embellishment centralHideEmb = (Embellishment)Util.getEmbellishment(pieceInCommand, "Layer - Central Hide");
        chosenMoveEmb.setValue(0); //Hide the maneuver
        chosenSpeedEmb.setValue(0); //Hide the speed
        centralHideEmb.setValue(1); //Show the central slashed icon
        pieceInCommand.setProperty("isHidden", 1);
        //Util.logToChat("STEP 4b - Hid the dial");
        final VASSAL.build.module.Map map = pieceInCommand.getMap();
        map.repaint();
    }

    protected Command myUndoCommand() {
        return null;
    }

    //the following class is used to send the info to the other player whenever a dial generation command is issued, so it can be done locally on all machines playing/watching the game
    //only the ship XWS string is sent
    public static class Dial2eHideEncoder implements CommandEncoder {
        private static final Logger logger = LoggerFactory.getLogger(StemNuDial2e.class);
        private static final String commandPrefix = "Dial2eHideEncoder=";

        public static DialHideCommand.Dial2eHideEncoder INSTANCE = new DialHideCommand.Dial2eHideEncoder();

        public Command decode(String command) {

            if (command == null || !command.contains(commandPrefix)) {
                return null;
            }
            logger.info("Decoding dialHideCommand");

            logger.info("Step 3b decoding - before chop " + command);
            String extractedId = command.substring(commandPrefix.length());
            logger.info("Step 3b decoding after chop " + extractedId);
            try{
                Collection<GamePiece> pieces = GameModule.getGameModule().getGameState().getAllPieces();
                logger.info("Step 3b prep - extractedId " + extractedId);
                for (GamePiece piece : pieces) {
                    if(piece.getId().equals(extractedId)) {

                        logger.info("Step 3b - Hide Encoder " + piece.getId());

                        return new DialHideCommand(piece);
                    }
                }
            }catch(Exception e){
                logger.info("Step 3b - exception error");
                return null;
            }


            return null;
        }

        public String encode(Command c) {
            if (!(c instanceof DialHideCommand)) {
                return null;
            }
            logger.info("Encoding dialHideCommand");

            //StemNuDial2e.dialHideCommand dhc = (StemNuDial2e.dialHideCommand) c;
            try {
                DialHideCommand dhc = (DialHideCommand) c;
                logger.info("Encoded dialHideCommand with id = {}", dhc.pieceInCommand.getId());
                return commandPrefix + dhc.pieceInCommand.getId();
            } catch(Exception e) {
                logger.error("Error encoding dialHideCommand", e);
                return null;
            }
        }
    }
}
