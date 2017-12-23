package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

import static mic.Util.logToChat;
import static mic.Util.logToChatWithTime;

/**
 * Created by Mic on 12/21/2017.
 *
 * Can be used to set a dial to a specific ship at autospawn-time
 */
public class StemDial extends Decorator implements EditablePiece {
    public static final String ID = "stemdial";
    public String shipXWS = "xwing";

    public StemDial(){
        this(null);
    }

    public StemDial(GamePiece piece){
        setInner(piece);
    }

    @Override
    public void mySetState(String s) {

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
    public Command myKeyEvent(KeyStroke keyStroke) {
        return null;
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {
        //check to see if 'x' was pressed
        if(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK, true).equals(stroke)) {
            logToChatWithTime("temporary trigger for Dial generation -will be eventually ported to autospawn\nPossibly to a right click menu as well with a dynamically fetched list of all ships??");
            GamePiece piece = getInner();

            DialGenerateCommand myDialGen = new DialGenerateCommand("xwing", piece);
            Command stringOCommands = piece.keyEvent(stroke);
            stringOCommands.append(myDialGen);

            myDialGen.execute();
            return stringOCommands;
        }

        return piece.keyEvent(stroke);
    }
    public String getDescription() {
        return "Custom StemDial (mic.StemDial)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    public void draw(Graphics graphics, int i, int i1, Component component, double v) {
        this.piece.draw(graphics, i, i1, component, v);
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


    //this is the command that takes a ship xws name, fetches the maneuver info and constructs the dial layer by layer
    public static class DialGenerateCommand extends Command {
        GamePiece piece;
        static String xwsShipName = "";
        List<List<Integer>> moveList; //possibly changed for a higher level class so it stays current with future xws spec changes

        DialGenerateCommand(String thisName, GamePiece piece) {

            // fetch the maneuver array of arrays according to the xws name passed on from autospawn or other means
            xwsShipName = thisName;
            MasterShipData.ShipData shipData = MasterShipData.getShipData(xwsShipName);
            moveList = shipData.getManeuvers();
            this.piece = piece;

        }

            // construct the dial Layers trait (Embellishment class) layer by layer according to the previous Array of Arrays.
            protected void executeCommand() {

            //Fetch the existing Embellishment and pass it through a customLayers instance? Not clear here
            //customLayers myCustomLayers = new customLayers();
            //
            logToChat("execute command = current ship xws name is: " + xwsShipName);
                String dialString = "emb2;;2;;Right;2;;Left;2;;;;;false;0;-38;Move_1_H-L_R.png,Move_1_G-L_G.png,Move_1_S_G.png,Move_1_G-R_G.png,Move_1_H-R_R.png,Move_2_H-L_W.png,Move_2_G-L_W.png,Move_2_S_G.png,Move_2_G-R_W.png,Move_2_H-R_W.png,Move_3_H-L_R.png,Move_3_G-L_W.png,Move_3_S_W.png,Move_3_G-R_W.png,Move_3_H-R_R.png,Move_4_S_W.png,Move_4_U_R.png;Hard Left 1,Bank Left 1,Forward 1,Bank Right 1,Hard Right 1,Hard Left 2,Bank Left 2,Forward 2,Bank Right 2,Hard Right 2,Hard Left 3,Bank Left 3,Forward 3,Bank Right 3,Hard Right 3,Forward 4,K-Turn 4;true;Move;;;false;;1;1;true;;46,0;44,0\\\\\\\\\tpiece;;;Dial_Rebel_n.png;dial for Attack Shuttle/\t\\\tnull;\\\\\t\\\\\\\t1\\\\\\\\";
                Embellishment myEmb = new Embellishment(dialString, piece);
                piece.setProperty(myEmb, dialString);
        }

        protected Command myUndoCommand() {
            return null;
        }




        //the following class is used to send the info to the other player whenever a dial generation command is issued, so it can be done locally on all machines playing/watching the game
        //only the ship XWS string is sent
        public static class DialGeneratorEncoder implements CommandEncoder {
            private static final Logger logger = LoggerFactory.getLogger(StemDial.class);
            private static final String commandPrefix = "DialGeneratorEncoder=";

            public static StemDial.DialGenerateCommand.DialGeneratorEncoder INSTANCE = new StemDial.DialGenerateCommand.DialGeneratorEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding DialGenerateCommand");

                command = command.substring(commandPrefix.length());
                try {
                    xwsShipName = command.toString();
                } catch (Exception e) {
                    logger.error("Error decoding DialGenerateCommand", e);
                    return null;
                }
                return null;
            }

            public String encode(Command c) {
                if (!(c instanceof DialGenerateCommand)) {
                    return null;
                }
                logger.info("Encoding DialGenerateCommand");
                DialGenerateCommand dialGenCommand = (DialGenerateCommand) c;
                try {
                    return commandPrefix + xwsShipName;
                } catch(Exception e) {
                    logger.error("Error encoding DialGenerateCommand", e);
                    return null;
                }
            }
        }

    }

}


