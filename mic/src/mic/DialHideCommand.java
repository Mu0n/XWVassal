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
    private static final Logger logger = LoggerFactory.getLogger(DialHideCommand.class);

    public GamePiece pieceInCommand;
    public String pieceId;

    public DialHideCommand(GamePiece piece) {
        pieceInCommand = piece;
        pieceId = pieceInCommand.getId();
    }

    public DialHideCommand(String pieceIdPassed) {
        pieceId = pieceIdPassed;
    }

    protected void executeCommand() {
        
        if(pieceInCommand == null && pieceId != null){
            Collection<GamePiece> pieces = GameModule.getGameModule().getGameState().getAllPieces();
            for (GamePiece piece : pieces) {
                if(piece.getId().equals(pieceId)) {
                    pieceInCommand = piece;
                }
            }
        }

        String ownerStr = pieceInCommand.getProperty("owner").toString();
        int ownerInt = Integer.parseInt(ownerStr);


        Embellishment chosenMoveEmb = (Embellishment)Util.getEmbellishment(pieceInCommand,"Layer - Chosen Move");
        Embellishment chosenSpeedEmb = (Embellishment)Util.getEmbellishment(pieceInCommand, "Layer - Chosen Speed");
        Embellishment sideHideEmb = (Embellishment)Util.getEmbellishment(pieceInCommand,"Layer - Side Hide");

        chosenMoveEmb.setValue(0); //Hide the maneuver
        chosenSpeedEmb.setValue(0); //Hide the speed
        pieceInCommand.setProperty("isHidden", 1);
        sideHideEmb.setValue(1); //show the side slashed eye icon
        //Util.logToChat("STEP 4b - Hid the dial");
        final VASSAL.build.module.Map map = pieceInCommand.getMap();
        map.repaint();
    }

    protected Command myUndoCommand() {
        logger.info("DialHide Undo");
        return null;
    }

    //the following class is used to send the info to the other player whenever a dial generation command is issued, so it can be done locally on all machines playing/watching the game
    //only the ship XWS string is sent
    public static class Dial2eHideEncoder implements CommandEncoder {
        private static final Logger logger = LoggerFactory.getLogger(Dial2eHideEncoder.class);
        private static final String commandPrefix = "Dial2eHideEncoder=";

        public static DialHideCommand.Dial2eHideEncoder INSTANCE = new DialHideCommand.Dial2eHideEncoder();

        public Command decode(String command) {

            if (command == null || !command.contains(commandPrefix)) {
                return null;
            }
            String extractedId = command.substring(commandPrefix.length());
            try{
                Collection<GamePiece> pieces = GameModule.getGameModule().getGameState().getAllPieces();
                for (GamePiece piece : pieces) {
                    if(piece.getId().equals(extractedId)) {
                        return new DialHideCommand(piece);
                    }
                }
                return new DialHideCommand(extractedId);
            }catch(Exception e){
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
                return commandPrefix + dhc.pieceId;
            } catch(Exception e) {
                logger.error("Error encoding DialHideCommand", e);
                return null;
            }
        }
    }
}
