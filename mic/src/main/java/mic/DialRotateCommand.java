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
 * Allows a dial selection rotation to be sent to other players for the new style of dial spawned with StemDial2e. This command is normally called
 * by StemNuDial2e (a decorator found in that Game Piece)
 */
public class DialRotateCommand extends Command {
    private GamePiece pieceInCommand;
    private final String pieceId;

    /**
     * will +1 a move, otherwise -1 a move
     */
    private final Boolean goingUpAMove;
    private final boolean showEverything;

    /**
     * used for when you want to trigger the undo of a rotate
     */
    private final Boolean undoVersion;

    DialRotateCommand(GamePiece piece, Boolean wantGoUpAMove, boolean wantShowEverything, Boolean doItNormally) {
        pieceInCommand = piece;
        pieceId = piece.getId();
        goingUpAMove = wantGoUpAMove;
        showEverything = wantShowEverything;
        undoVersion = doItNormally;
    }

    DialRotateCommand(String pieceIdPassed, Boolean wantGoUpAMove, boolean wantShowEverything, Boolean doItNormally) {
        pieceId = pieceIdPassed;
        goingUpAMove = wantGoUpAMove;
        showEverything = wantShowEverything;
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

        //Construct the next build string
        StringBuilder stateString = new StringBuilder();

        String moveDef = "";
        String moveSpeedLayerString = "";
        if (undoVersion) { //do the hide normally
            stateString.append(buildStateString(goingUpAMove?1:-1));
            //Get the movement heading layer
            moveDef = getNewMoveDefFromScratch(goingUpAMove?1:-1);
            //get the speed layer to show
            moveSpeedLayerString = getLayerFromScratch(goingUpAMove?1:-1);
        } else { //reverse the hide
            stateString.append(buildStateString(!goingUpAMove?1:-1));
            //Get the movement heading layer
            moveDef = getNewMoveDefFromScratch(!goingUpAMove?1:-1);
            //get the speed layer to show
            moveSpeedLayerString = getLayerFromScratch(!goingUpAMove?1:-1);
        }

        pieceInCommand.setProperty("selectedMove", moveDef);
        String ownerStr = pieceInCommand.getProperty("owner").toString();
        int ownerInt = Integer.parseInt(ownerStr);

        Embellishment chosenMoveEmb = (Embellishment)Util.getEmbellishment(pieceInCommand,"Layer - Chosen Move");
        Embellishment chosenSpeedEmb = (Embellishment)Util.getEmbellishment(pieceInCommand, "Layer - Chosen Speed");

        if (Util.getCurrentPlayer().getSide()==ownerInt || showEverything) {
            chosenMoveEmb.mySetType(stateString.toString());
            chosenMoveEmb.setValue(1);
            chosenSpeedEmb.setValue(Integer.parseInt(moveSpeedLayerString));
        } else {
            //do nothing!
        }
        final VASSAL.build.module.Map map = pieceInCommand.getMap();
        map.repaint();
    }

    protected Command myUndoCommand() {
        return new DialRotateCommand(pieceId, goingUpAMove, showEverything, false);
    }

    public String getLayerFromScratch(int moveModification){
        String dialString = pieceInCommand.getProperty("dialstring").toString();
        String[] values = dialString.split(",");
        int nbOfMoves = values.length;

        // Fetch the saved move from the dynamic property of the dial piece
        String savedMoveString = pieceInCommand.getProperty("selectedMove").toString();
        int savedMoveStringInt = Integer.parseInt(savedMoveString);

        if (moveModification == 1) { //if you want to shift the selected move 1 up.
            if(savedMoveStringInt == nbOfMoves) savedMoveStringInt = 1; //loop
            else savedMoveStringInt++;
        } else if (moveModification == -1) //if you want to shift the selected move 1 down
        {
            if (savedMoveStringInt == 1) savedMoveStringInt = nbOfMoves; //loop
            else savedMoveStringInt--;
        }

        String moveCode = values[savedMoveStringInt-1];
        int moveSpeedLayerToUse = getLayerFromMoveCode(moveCode);
        String moveSpeedLayerString = Integer.toString(moveSpeedLayerToUse);

        return moveSpeedLayerString;
    }

    public int getLayerFromMoveCode(String code){
        return Integer.parseInt(code.substring(0,1)) + 1;
    }

    private String getNewMoveDefFromScratch(int moveMod) {
        String dialString = pieceInCommand.getProperty("dialstring").toString();
        String[] values = dialString.split(",");
        int nbOfMoves = values.length;

        // Fetch the saved move from the dynamic property of the dial piece
        String savedMoveString = pieceInCommand.getProperty("selectedMove").toString();
        int savedMoveStringInt = Integer.parseInt(savedMoveString);

        if (moveMod == 1) { //if you want to shift the selected move 1 up.
            if (savedMoveStringInt == nbOfMoves) savedMoveStringInt = 1; //loop
            else savedMoveStringInt++;
        } else if(moveMod == -1) //if you want to shift the selected move 1 down
        {
            if (savedMoveStringInt == 1) savedMoveStringInt = nbOfMoves; //loop
            else savedMoveStringInt--;
        }

        return ""+savedMoveStringInt;
    }

    public String buildStateString(int moveModification) {
        /*
         * dialString, like: 1BW,1FB,1NW,2TW,2BB,2FB,2NB,2YW,3LR,3TW,3BW,3FW,3NW,3YW,3PR,4FR
         * values, like: [1BW,1FB,1NW,2TW,2BB,2FB,2NB,2YW,3LR,3TW,3BW,3FW,3NW,3YW,3PR,4FR]
         * nbOfMoves, like: 15
         *
         * saveMoveString, like: "4" (out of 15)
         * savedMoveStringInt, like: 4
         *
         * access the move in values by using savedMoveStringInt - 1
         * newMove, like: "1TR"
         * newRawSpeed, like 1
         * newMoveSpeed, like 3 (layer 0 = empty, layer 1 = '0', layer 2 = '1', layer 6 = '5')
         * moveWithoutSpeed, like: "TR"
         * moveImage, like: "1TR.png"
         *
         * stateString, like: "emb2...." which is fed moveImage, moveName
         * moveName, like: "Hard Left 1"
         */

        // Fetch the string of movement from the dynamic property and chop it up in an array
        String dialString = pieceInCommand.getProperty("dialstring").toString();
        String[] values = dialString.split(",");
        int nbOfMoves = values.length;

        // Fetch the saved move from the dynamic property of the dial piece
        String savedMoveString = pieceInCommand.getProperty("selectedMove").toString();
        int savedMoveStringInt = Integer.parseInt(savedMoveString);

        if(moveModification == 1){ //if you want to shift the selected move 1 up.
            if(savedMoveStringInt == nbOfMoves) savedMoveStringInt = 1; //loop
            else savedMoveStringInt++;
        } else if(moveModification == -1) //if you want to shift the selected move 1 down
        {
            if (savedMoveStringInt == 1) savedMoveStringInt = nbOfMoves; //loop
            else savedMoveStringInt--;
        }

        String moveCode = values[savedMoveStringInt-1];
        int rawSpeed = getRawSpeedFromMoveCode(moveCode);

        //attempt to seed the move layer with the right image just like at spawn time
        StringBuilder stateString = new StringBuilder();
        StringBuilder moveNamesString = new StringBuilder();
        stateString.append("emb2;Activate;2;;;2;;;2;;;;1;false;0;-24;,");

        String moveImage;
        String moveWithoutSpeed = getMoveCodeWithoutSpeed(moveCode);
        String moveName = StemDial2e.maneuverNames.get(getMoveRaw(moveCode));
        moveNamesString.append(moveName).append(" ").append(rawSpeed);

        moveImage = StemDial2e.dialHeadingImages.get(moveWithoutSpeed);
        stateString
            .append(moveImage)
        // add in move names
            .append(";empty,"+moveNamesString)
            .append(";false;Chosen Move;;;false;;1;1;true;65,130");

        return stateString.toString();
    }

    public int getRawSpeedFromMoveCode(String code){
        return Integer.parseInt(code.substring(0,1));
    }

    public String getMoveCodeWithoutSpeed(String code){
        return code.substring(1,3);
    }

    public String getMoveRaw(String code){
        return code.substring(1,2);
    }

    /**
     * the following class is used to send the info to the other player whenever a dial generation command is issued, so it can be done locally on all machines playing/watching the game
     * only the ship XWS string is sent
     */
    public static class Dial2eRotateEncoder implements CommandEncoder {
        private static final Logger logger = LoggerFactory.getLogger(StemNuDial2e.class);
        private static final String commandPrefix = "Dial2eRotateEncoder=";
        private static final String itemDelim = "\t";

        public static DialRotateCommand.Dial2eRotateEncoder INSTANCE = new DialRotateCommand.Dial2eRotateEncoder();

        public Command decode(String command){
            if (command == null || !command.contains(commandPrefix)) {
                return null;
            }

            command = command.substring(commandPrefix.length());
            String[] parts = command.split(itemDelim);

            try {
                Collection<GamePiece> pieces = GameModule.getGameModule().getGameState().getAllPieces();
                Boolean doGoUp = Boolean.parseBoolean(parts[1]);
                Boolean doItNorm = Boolean.parseBoolean(parts[3]);
                for (GamePiece piece : pieces) {
                    if (piece.getId().equals(parts[0])) {
                        return new DialRotateCommand(piece, doGoUp, Boolean.parseBoolean(parts[2]), doItNorm);
                    }
                }
                return new DialRotateCommand(parts[0], doGoUp, Boolean.parseBoolean(parts[2]), doItNorm);
            } catch(Exception e) {
                logger.error("Error decoding Dial2eRotateEncoder", e);
                return null;
            }
        }

        public String encode(Command c){
            if (!(c instanceof DialRotateCommand)) {
                return null;
            }
            try {
                DialRotateCommand drc = (DialRotateCommand)c;
                return commandPrefix + Joiner.on(itemDelim).join(drc.pieceId, drc.goingUpAMove.toString(), ""+drc.showEverything, drc.undoVersion.toString());
            } catch(Exception e) {
                logger.error("Error encoding Dial2eRotateEncoder", e);
                return null;
            }
        }
    }

}