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

public class DialRotateCommand extends Command {
     GamePiece pieceInCommand;
     String moveDef;
     boolean showEverything;
     String stateString;
     String moveSpeedLayerString;

    DialRotateCommand(GamePiece piece, String selectedMove, boolean wantShowEverything, String reqStateString, String reqMoveSpeedLayerString) {
        pieceInCommand = piece;
        moveDef = selectedMove;
        showEverything = wantShowEverything;
        stateString = reqStateString;
        moveSpeedLayerString = reqMoveSpeedLayerString;
    }

    protected void executeCommand() {
        pieceInCommand.setProperty("selectedMove", moveDef);
        String ownerStr = pieceInCommand.getProperty("owner").toString();
        int ownerInt = Integer.parseInt(ownerStr);

        if(showEverything == true || (ownerInt == Util.getCurrentPlayer().getSide())){
            Embellishment chosenMoveEmb = (Embellishment)Util.getEmbellishment(pieceInCommand,"Layer - Chosen Move");
            Embellishment chosenSpeedEmb = (Embellishment)Util.getEmbellishment(pieceInCommand, "Layer - Chosen Speed");

            chosenSpeedEmb.setValue(Integer.parseInt(moveSpeedLayerString));
            chosenMoveEmb.mySetType(stateString);
            chosenMoveEmb.setValue(1);

            final VASSAL.build.module.Map map = pieceInCommand.getMap();
            map.repaint();

        }
    }

    protected Command myUndoCommand() {
        return null;
    }

    //the following class is used to send the info to the other player whenever a dial generation command is issued, so it can be done locally on all machines playing/watching the game
    //only the ship XWS string is sent
    public static class Dial2eRotateEncoder implements CommandEncoder {
        private static final Logger logger = LoggerFactory.getLogger(StemNuDial2e.class);
        private static final String commandPrefix = "Dial2eRotateEncoder=";
        private static final String itemDelim = "\t";

        public static DialRotateCommand.Dial2eRotateEncoder INSTANCE = new DialRotateCommand.Dial2eRotateEncoder();

        public Command decode(String command){
            if(command == null || !command.contains(commandPrefix)) {
                return null;
            }

            command = command.substring(commandPrefix.length());
            String[] parts = command.split(itemDelim);

            try{
                Collection<GamePiece> pieces = GameModule.getGameModule().getGameState().getAllPieces();
                for (GamePiece piece : pieces) {
                    if(piece.getId().equals(parts[0])) {
                        return new DialRotateCommand(piece, parts[1], Boolean.parseBoolean(parts[2]), parts[3], parts[4]);
                    }
                }

            }catch(Exception e){
                logger.error("Error decoding Dial2eRotateEncoder", e);
                return null;
            }
            return null;
        }

        public String encode(Command c){
            if (!(c instanceof DialRotateCommand)) {
                return null;
            }
            try{
                DialRotateCommand drc = (DialRotateCommand)c;
                return commandPrefix + Joiner.on(itemDelim).join(drc.pieceInCommand.getId(), drc.moveDef, ""+drc.showEverything, drc.stateString, drc.moveSpeedLayerString);
            }catch(Exception e) {
                logger.error("Error encoding Dial2eRotateEncoder", e);
                return null;
            }
        }
    }

}
