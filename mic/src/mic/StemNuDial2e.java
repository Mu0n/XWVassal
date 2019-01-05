package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static mic.Util.deserializeBase64Obj;
import static mic.Util.logToChat;
import static mic.Util.serializeToBase64;

/**
 * Created by Mic on 04/12/2018.
 *
 * this file is for all programmatic reactions to ',', '.' and 'ctrl-r' commands issued to this new style of dial
 * New style of dial:
 *
 * 1) looks like the open face 2nd edition dial instead of the 1st edition
 * 2) no more additional graphics due to the masked/hidden side - a simple eye-with-slash icon will be used to indicate the hidden mode of the dial (icon at center)
 * 3) when in reveal mode, the selected move (at the top) will be copied, larger, in the center and rotations can't be done at all
 * 4) when in hidden mode, dial rotation commands will only tweak the rotation of the faceplate for the owner of the dial, not for any other player
 * 5) The pilot name in text (white text over black background) above the dial; the ship name in text (same colors) under the dial, no more icon gfx to manage
 * 6) a player can't cheat anymore by swapping mask gfx by a transparent empty png
 * 7) the open face dial has to be kept in OTA2 - no mistakes are allowed because patches can't happen, unless a download all is forced in the content checker
 * 8) File name should be: D2e_'ship name from contracted manifest file names'.jpg
 * 9) Dial graphics should be generated by Mike's tool when he has time to implement it, otherwise generated by me in an outside program or from dialgen + photoshop
 * 10) new dial graphics added when a ship graphic is added in the best case scenario as info pours in from previews.
 */

public class StemNuDial2e extends Decorator implements EditablePiece, Serializable {
    public static final String ID = "StemNuDial2e";

    public StemNuDial2e()  {
        this(null);
    }

    public StemNuDial2e(GamePiece piece)
        {setInner(piece); }

    @Override
    public void mySetState(String newState) {

    }
    @Override
    public String myGetState() {
        return "";
    }
    @Override
    public String myGetType() {
        return ID;
    }
    @Override
    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }

    @Override
    public Command myKeyEvent(KeyStroke stroke) { return null; }

    @Override
    public Command keyEvent(KeyStroke stroke) {
        boolean hasSomethingHappened = false;
        String isHiddenPropCheck ="";
        Command result = piece.keyEvent(stroke);

        if (getOwnerOfThisDial() == Util.getCurrentPlayer().getSide()) {
            KeyStroke checkForCtrlRReleased = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, true);
            KeyStroke checkForCtrlRPressed = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, false);
            KeyStroke checkForCommaReleased = KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0, true);
            KeyStroke checkForPeriodReleased = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0, true);

            boolean goingLeft = checkForCommaReleased.equals(stroke);
            boolean goingRight = checkForPeriodReleased.equals(stroke);

            isHiddenPropCheck = piece.getProperty("isHidden").toString();

            if(checkForCtrlRReleased.equals(stroke)) {
                hasSomethingHappened = true;

                logToChat("isHidden property is " +isHiddenPropCheck);
                if(isHiddenPropCheck.equals("true")) { // about to reveal the dial
                                        // Fetch the string of movement from the dynamic property and chop it up in an array
                    String dialString = piece.getProperty("dialstring").toString();
                    String[] values = dialString.split(",");

                    // Fetch the saved move from the dynamic property of the dial piece
                    String savedMoveString = piece.getProperty("selectedMove").toString();
                    int savedMoveStringInt = Integer.parseInt(savedMoveString);
                    String moveCode = values[savedMoveStringInt-1];
                    Integer moveSpeedLayerToUse = getLayerFromMoveCode(moveCode);
                    String moveSpeedLayerString = moveSpeedLayerToUse.toString();
                    int rawSpeed = getRawSpeedFromMoveCode(moveCode);

                    // Make the speed appear (by correctly chosing among the 6 existing layers
                    //chosenSpeedEmb.setValue(moveSpeedLayerToUse);

                    //attempt to seed the move layer with the right image just like at spawn time
                    StringBuilder stateString = new StringBuilder();
                    StringBuilder moveNamesString = new StringBuilder();
                    stateString.append("emb2;Activate;2;;;2;;;2;;;;1;false;0;-24;,");

                    String moveImage;
                    String moveWithoutSpeed = getMoveCodeWithoutSpeed(moveCode);
                    String moveName = StemDial2e.maneuverNames.get(getMoveRaw(moveCode));
                    moveNamesString.append(moveName).append(" ").append(rawSpeed);

                    moveImage = StemDial2e.dialHeadingImages.get(moveWithoutSpeed);
                    stateString.append(moveImage);
                    // add in move names
                    stateString.append(";empty,"+moveNamesString);
                    stateString.append(";false;Chosen Move;;;false;;1;1;true;65,130");

                    Util.logToChat("Dial was hidden , about to reveal");
                    dialRevealCommand revealNow = new dialRevealCommand(piece, stateString.toString(), moveSpeedLayerString);
                    result.append(revealNow);
                    revealNow.execute();
                    //chosenMoveEmb.mySetType(stateString.toString());
                    //chosenMoveEmb.setValue(1);

                } else if(isHiddenPropCheck.equals("false")){ // about to hide the dial
                    Util.logToChat("Dial was revealed , about to hide");
                    dialHideCommand hideNow = new dialHideCommand(piece);
                    result.append(hideNow);
                    hideNow.execute();
                }
            }
            else if(goingLeft || goingRight){ //rotate left, move-- or rotate right, move++
                hasSomethingHappened = true;

// Fetch the string of movement from the dynamic property and chop it up in an array
                String dialString = piece.getProperty("dialstring").toString();
                String[] values = dialString.split(",");
                int nbOfMoves = values.length;

                // Fetch the saved move from the dynamic property of the dial piece
                String savedMoveString = piece.getProperty("selectedMove").toString();
                int savedMoveStringInt = Integer.parseInt(savedMoveString);

                if(goingLeft) {
                    // Decrement the movement index and reinject the saved move property
                    if (savedMoveStringInt == 1) savedMoveStringInt = nbOfMoves; //cycle around
                    else savedMoveStringInt--;
                    savedMoveString = "" + savedMoveStringInt;
                } else if(goingRight){
                    if(savedMoveStringInt == nbOfMoves) savedMoveStringInt = 1;
                    else savedMoveStringInt++;
                    savedMoveString = "" + savedMoveStringInt;
                }

                piece.setProperty("selectedMove", savedMoveString);

                // Fetch the new move based on the new lowered index
                String newMove = values[savedMoveStringInt-1]; //-1 because if a dial has moves from 1-15, the indices must be 0-14
                int newRawSpeed = getRawSpeedFromMoveCode(newMove);
                int newMoveSpeed = getLayerFromMoveCode(newMove);

                Util.logToChatWithoutUndo("decremented savedMove " + savedMoveString + " (" + savedMoveStringInt + ") = " + newMove + " at speed layer # " + newMoveSpeed);

                // Make the speed appear (by correctly chosing among the 6 existing layers
                Embellishment chosenSpeedEmb = (Embellishment)Util.getEmbellishment(piece, "Layer - Chosen Speed");


                //Prepare to modify the chosen move layer
                Embellishment chosenMoveEmb = (Embellishment)Util.getEmbellishment(piece,"Layer - Chosen Move");
                StringBuilder stateString = new StringBuilder();
                StringBuilder moveNamesString = new StringBuilder();
                stateString.append("emb2;Activate;2;;;2;;;2;;;;1;false;0;-24;,");
                String moveWithoutSpeed = getMoveCodeWithoutSpeed(newMove);
                String moveImage = StemDial2e.dialHeadingImages.get(moveWithoutSpeed);

                String moveName = StemDial2e.maneuverNames.get(getMoveRaw(newMove));
                moveNamesString.append(moveName).append(" ").append(newRawSpeed);
                // add in move names
                stateString.append(moveImage);
                stateString.append(";empty,"+moveNamesString);
                stateString.append(";false;Chosen Move;;;false;;1;1;true;65,130");

                Util.logToChatWithoutUndo("moveWOspeed " + moveWithoutSpeed + " moveImage " + moveImage);
                Util.logToChatWithoutUndo(stateString.toString());

                if(piece.getProperty("isHidden").equals("true")){ //encode only the modified selected move property
                    dialRotateCommand(piece, false, newMove);
                } else { //dial is revealed, show everything to all
                    dialRotateCommand(piece, true, stateString.toString(), newMoveSpeed);
                }
            }
        } else { // get scolded for not owning the dial that was manipulated
            Util.logToChatWithoutUndo("You (player " + Util.getCurrentPlayer().getSide() + ") are not the owner of this dial, player " + getOwnerOfThisDial() + " is.");
        }
        if(hasSomethingHappened) return result;
        return piece.keyEvent(stroke);
    }


    public int getRawSpeedFromMoveCode(String code){
        return Integer.parseInt(code.substring(0,1));
    }
    public int getLayerFromMoveCode(String code){
        return Integer.parseInt(code.substring(0,1)) + 1;
    }
    public String getMoveCodeWithoutSpeed(String code){
        return code.substring(1,3);
    }

    public String getMoveRaw(String code){
        return code.substring(1,2);
    }



    public int getOwnerOfThisDial(){
        GamePiece dialPiece = (GamePiece)this.piece;
        String ownerStr = dialPiece.getProperty("owner").toString();
        int ownerInt = Integer.parseInt(ownerStr);
        return ownerInt;
    }
    public String getDescription() {
        return "Custom StemNuDial (mic.StemNuDial2e)";
    }

    public void mySetType(String type) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    public void draw(Graphics g, int x, int y, Component obs, double zoom) {
        this.piece.draw(g, x, y, obs, zoom);
    }

    public Rectangle boundingBox() {
        return this.piece.boundingBox();
    }

    public Shape getShape() {
        return this.piece.getShape();
    }

    public String getName() {
        return this.piece.getName();
    }

    public static class dialRevealCommand extends Command {
        static GamePiece pieceInCommand;
        static String moveDef;
        static String speedLayer;

        dialRevealCommand(GamePiece piece, String requiredMoveDef, String requiredSpeedLayer){
            pieceInCommand = piece;
            moveDef = requiredMoveDef;
            speedLayer = requiredSpeedLayer;
        }

        protected void executeCommand() {
            Embellishment chosenMoveEmb = (Embellishment)Util.getEmbellishment(pieceInCommand,"Layer - Chosen Move");
            Embellishment chosenSpeedEmb = (Embellishment)Util.getEmbellishment(pieceInCommand, "Layer - Chosen Speed");
            chosenMoveEmb.mySetType(moveDef);
            chosenMoveEmb.setValue(1);
            chosenSpeedEmb.setValue(Integer.parseInt(speedLayer));
            pieceInCommand.setProperty("isHidden", "false");
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

            public static StemNuDial2e.dialRevealCommand.Dial2eRevealEncoder INSTANCE = new StemNuDial2e.dialRevealCommand.Dial2eRevealEncoder();

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
                            Util.logToChatWithoutUndo("DialReveal decoder moveDef=" + moveDef + " speedLayer=" + speedLayer);
                            return new dialRevealCommand(piece, parts[1], parts[2]);
                        }
                    }

                }catch(Exception e){

                    return null;
                }


                return null;
            }

            public String encode(Command c){
                if (!(c instanceof StemNuDial2e.dialRevealCommand)) {
                    return null;
                }
                try{
                    Util.logToChatWithoutUndo("DialReveal encoder moveDef=" + moveDef + " speedLayer=" + speedLayer);
                    return commandPrefix + Joiner.on(itemDelim).join(pieceInCommand.getId(), moveDef,speedLayer);
                }catch(Exception e) {
                    logger.error("Error encoding dialRevealCommand", e);
                    return null;
                }

            }
        }
    }
    public static class dialHideCommand extends Command {
        static GamePiece pieceInCommand;

        dialHideCommand(GamePiece piece) {
            pieceInCommand = piece;
        }

        protected void executeCommand() {
            Embellishment chosenMoveEmb = (Embellishment)Util.getEmbellishment(pieceInCommand,"Layer - Chosen Move");
            Embellishment chosenSpeedEmb = (Embellishment)Util.getEmbellishment(pieceInCommand, "Layer - Chosen Speed");
            chosenMoveEmb.setValue(0);
            chosenSpeedEmb.setValue(0);
            pieceInCommand.setProperty("isHidden", "true");
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

            public static StemNuDial2e.dialHideCommand.Dial2eHideEncoder INSTANCE = new StemNuDial2e.dialHideCommand.Dial2eHideEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding dialHideCommand");

                command = command.substring(commandPrefix.length());

                try{
                    Collection<GamePiece> pieces = GameModule.getGameModule().getGameState().getAllPieces();

                    for (GamePiece piece : pieces) {
                        if(piece.getId().equals(command)) {
                            Util.logToChat("DialHide encode id=" + command);
                            return new dialHideCommand(piece);
                        }
                    }
                }catch(Exception e){
                    return null;
                }


                return null;
            }

            public String encode(Command c) {
                if (!(c instanceof StemNuDial2e.dialHideCommand)) {
                    return null;
                }
                logger.info("Encoding dialHideCommand");
                StemNuDial2e.dialHideCommand dhc = (StemNuDial2e.dialHideCommand) c;
                try {
                    Util.logToChat("DialHide encode id=" + pieceInCommand.getId());
                    return commandPrefix + pieceInCommand.getId();
                } catch(Exception e) {
                    logger.error("Error encoding dialHideCommand", e);
                    return null;
                }
            }
        }
    }




    public static class dialRotateCommand extends Command {
        static GamePiece pieceInCommand;
        boolean goingPublic;
        String newMove;

        dialRotateCommand(GamePiece piece, boolean wantPublicShowing, String selectedMove) {
            pieceInCommand = piece;
            goingPublic = wantPublicShowing;
            newMove = selectedMove;
        }

        protected void executeCommand() {
            if(goingPublic == false){

            } else {

            }
            final VASSAL.build.module.Map map = pieceInCommand.getMap();
            map.repaint();
        }


        protected Command myUndoCommand() {
            return null;
        }

        //the following class is used to send the info to the other player whenever a dial generation command is issued, so it can be done locally on all machines playing/watching the game
        //only the ship XWS string is sent
        public static class Dial2eRotateEncoder implements CommandEncoder {
            private static final Logger logger = LoggerFactory.getLogger(StemNuDial2e.class);
            private static final String commandPrefix = "Dial2eHideEncoder=";

            public static StemNuDial2e.dialRotateCommand.Dial2eRotateEncoder INSTANCE = new StemNuDial2e.dialRotateCommand.Dial2eRotateEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding dialRotateCommand");

                command = command.substring(commandPrefix.length());

                try{
                    Collection<GamePiece> pieces = GameModule.getGameModule().getGameState().getAllPieces();

                    for (GamePiece piece : pieces) {
                        if(piece.getId().equals(command)) {
                            Util.logToChat("dialRotateCommand encode id=" + command);
                            return new dialRotateCommand(piece);
                        }
                    }
                }catch(Exception e){
                    return null;
                }


                return null;
            }

            public String encode(Command c) {
                if (!(c instanceof StemNuDial2e.dialRotateCommand)) {
                    return null;
                }
                logger.info("Encoding dialRotateCommand");
                StemNuDial2e.dialRotateCommand dhc = (StemNuDial2e.dialRotateCommand) c;
                try {
                    Util.logToChat("dialRotateCommand encode id=" + pieceInCommand.getId());
                    return commandPrefix + pieceInCommand.getId();
                } catch(Exception e) {
                    logger.error("Error encoding dialRotateCommand", e);
                    return null;
                }
            }
        }
    }




}
