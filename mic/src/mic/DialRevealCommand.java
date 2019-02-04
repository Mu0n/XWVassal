package mic;

import VASSAL.build.GameModule;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Properties;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class DialRevealCommand extends Command {

    GamePiece pieceInCommand;
    String moveDef;
    String speedLayer;
    String revealerName ="";

    DialRevealCommand(GamePiece piece, String requiredMoveDef, String requiredSpeedLayer, String revealerNamePassed){
        pieceInCommand = piece;
        moveDef = requiredMoveDef;
        speedLayer = requiredSpeedLayer;
        revealerName = revealerNamePassed;
    }

    protected void executeCommand() {
        Embellishment chosenMoveEmb = (Embellishment) Util.getEmbellishment(pieceInCommand, "Layer - Chosen Move");
        Embellishment chosenSpeedEmb = (Embellishment) Util.getEmbellishment(pieceInCommand, "Layer - Chosen Speed");
        Embellishment sideHideEmb = (Embellishment) Util.getEmbellishment(pieceInCommand, "Layer - Side Hide");
        Embellishment centralHideEmb = (Embellishment) Util.getEmbellishment(pieceInCommand, "Layer - Central Hide");
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



        if(pieceInCommand.getMap().equals(mapNameCheck)) Util.logToChatCommand("* - "+ revealerName + " reveals the dial for "
                + craftIDCheck + " (" + pilotNameCheck + ") = "+ chosenMoveCheck + "*");



        //Util.logToChat("STEP 4a - Revealed the dial with " + chosenMoveEmb.getProperty("Chosen Move_Name"));
        final VASSAL.build.module.Map map = pieceInCommand.getMap();
        map.repaint();
    }
    protected Command myUndoCommand() {
        return null;
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
                for (GamePiece piece : pieces) {
                    if(piece.getId().equals(parts[0])) {
                        return new DialRevealCommand(piece, parts[1], parts[2],parts[3]);
                    }
                }
            }catch(Exception e){
                logger.info("Error decoding DialRevealCommand - exception error");
                return null;
            }
            return null;
        }

        public String encode(Command c){
            if (!(c instanceof DialRevealCommand)) {
                return null;
            }
            try{
                DialRevealCommand drc = (DialRevealCommand) c;

                return commandPrefix + Joiner.on(itemDelim).join(drc.pieceInCommand.getId(), drc.moveDef, drc.speedLayer, drc.revealerName);
            }catch(Exception e) {
                logger.error("Error encoding DialRevealCommand", e);
                return null;
            }

        }
    }
}