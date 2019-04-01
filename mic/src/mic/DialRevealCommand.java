package mic;
/**
 * Created by mjuneau in 2019-01.
 * Allows a dial reveal command to be sent to other players for the new style of dial spawned with StemDial2e. This command is normally called
 * by StemNuDial2e (a decorator found in that Game Piece)
 */
import VASSAL.build.GameModule;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class DialRevealCommand extends Command {

    GamePiece pieceInCommand;
    String pieceId;
    String moveDef;
    String speedLayer;
    String revealerName ="";
    Boolean undoVersion; //used for when you want to trigger the undo of a reveal

    DialRevealCommand(GamePiece piece, String requiredMoveDef, String requiredSpeedLayer, String revealerNamePassed, Boolean doItNormally){
        pieceInCommand = piece;
        pieceId = piece.getId();
        moveDef = requiredMoveDef;
        speedLayer = requiredSpeedLayer;
        revealerName = revealerNamePassed;
        undoVersion = doItNormally;
    }

    DialRevealCommand(String pieceIdPassed, String requiredMoveDef, String requiredSpeedLayer, String revealerNamePassed, Boolean doItNormally){
        pieceId = pieceIdPassed;
        moveDef = requiredMoveDef;
        speedLayer = requiredSpeedLayer;
        revealerName = revealerNamePassed;
        undoVersion = doItNormally;
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

        Embellishment chosenMoveEmb = (Embellishment) Util.getEmbellishment(pieceInCommand, "Layer - Chosen Move");
        Embellishment chosenSpeedEmb = (Embellishment) Util.getEmbellishment(pieceInCommand, "Layer - Chosen Speed");
        Embellishment sideHideEmb = (Embellishment) Util.getEmbellishment(pieceInCommand, "Layer - Side Hide");
        Embellishment centralHideEmb = (Embellishment) Util.getEmbellishment(pieceInCommand, "Layer - Central Hide");

        if(undoVersion == true){ //do the reveal normally
            chosenMoveEmb.mySetType(moveDef);
            chosenMoveEmb.setValue(1); // use the layer that shows the move
            sideHideEmb.setValue(0); //hide the small slashed eye icon
            centralHideEmb.setValue(0); //hide the central slashed eye icon
            chosenSpeedEmb.setValue(Integer.parseInt(speedLayer)); //use the right speed layer
            pieceInCommand.setProperty("isHidden", 0);

            VASSAL.build.module.Map mapNameCheck = VASSAL.build.module.Map.getMapById("Map0");
            String craftIDCheck = pieceInCommand.getProperty("Craft ID #").toString();
            String pilotNameCheck = pieceInCommand.getProperty("Pilot Name").toString();
            String chosenMoveCheck = chosenMoveEmb.getProperty("Chosen Move_Name").toString();

            if(pieceInCommand.getMap().equals(mapNameCheck) && revealerName.equals(Util.getCurrentPlayer().getName())) Util.logToChat("* - "+ revealerName + " reveals the dial for "
                    + craftIDCheck + " (" + pilotNameCheck + ") = "+ chosenMoveCheck + "*");
        }else { //reverse the reveal

            String ownerStr = pieceInCommand.getProperty("owner").toString();
            int ownerInt = Integer.parseInt(ownerStr);

            if(Util.getCurrentPlayer().getSide() == ownerInt){
                sideHideEmb.setValue(1);
            }else{
                centralHideEmb.setValue(1);
                chosenMoveEmb.setValue(0); // use the layer that shows the move
                chosenSpeedEmb.setValue(0); //use the right speed layer
            }
            pieceInCommand.setProperty("isHidden", 1);

        }


        final VASSAL.build.module.Map map = pieceInCommand.getMap();
        map.repaint();
    }
    protected Command myUndoCommand() {
        return new DialRevealCommand(pieceId, moveDef, speedLayer, revealerName, false);
    }

    public static class Dial2eRevealEncoder implements CommandEncoder {
        private static final Logger logger = LoggerFactory.getLogger(StemNuDial2e.class);
        private static final String commandPrefix = "Dial2eRevealEncoder=";
        private static final String itemDelim = "\t";

        public static DialRevealCommand.Dial2eRevealEncoder INSTANCE = new DialRevealCommand.Dial2eRevealEncoder();

        public Command decode(String command){
            if(command == null || !command.contains(commandPrefix)) {
                return null;
            }

            command = command.substring(commandPrefix.length());
            String[] parts = command.split(itemDelim);

            try{
                Collection<GamePiece> pieces = GameModule.getGameModule().getGameState().getAllPieces();
                Boolean doItNorm = Boolean.parseBoolean(parts[4]);
                for (GamePiece piece : pieces) {
                    if(piece.getId().equals(parts[0])) {
                        return new DialRevealCommand(piece, parts[1], parts[2],parts[3], doItNorm);
                    }
                }
                return new DialRevealCommand(parts[0], parts[1], parts[2],parts[3], doItNorm);
            }catch(Exception e){
                logger.info("Error decoding DialRevealCommand - exception error");
                return null;
            }
        }

        public String encode(Command c){
            if (!(c instanceof DialRevealCommand)) {
                return null;
            }
            try{
                DialRevealCommand drc = (DialRevealCommand) c;

                return commandPrefix + Joiner.on(itemDelim).join(drc.pieceId, drc.moveDef, drc.speedLayer, drc.revealerName, drc.undoVersion.toString());
            }catch(Exception e) {
                logger.error("Error encoding DialRevealCommand", e);
                return null;
            }

        }
    }
}