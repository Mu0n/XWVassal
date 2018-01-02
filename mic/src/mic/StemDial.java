package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

import static mic.Util.logToChat;
import static mic.Util.logToChatWithTime;

/**
 * Created by Mic on 12/21/2017.
 *
 * Can be used to set a dial to a specific ship at autospawn-time
 */
public class StemDial extends Decorator implements EditablePiece {
    public static final String ID = "stemdial";



    private static Map<String, String> maneuverNames = ImmutableMap.<String, String>builder()
            .put("O","Stop")
            .put("A","Reverse Left Bank")
            .put("S","Reverse")
            .put("D","Reverse Right Bank")
            .put("E","Tallon Roll Left")
            .put("T","Hard Left")
            .put("B","Bank Left")
            .put("F","Forward")
            .put("N","Bank Right")
            .put("Y","Hard Right")
            .put("L","Segnor's Loop Left")
            .put("K","K-Turn")
            .put("P","Segnor's Loop Right")
            .put("R","Tallon Roll Right")
            .build();

    private static Map<String, String> dialManeuverImages = ImmutableMap.<String, String>builder()
            .put("0OG", "Move_0_G.png")
            .put("0OW", "Move_0_W.png")
            .put("0OR", "Move_0_R.png")
            .put("1AG", "Move_1_RLB1_G.png")
            .put("1AW", "Move_1_RLB1_W.png")
            .put("1AR", "Move_1_RLB1_R.png")
            .put("1SG", "Move_1_R_G.png")
            .put("1SW", "Move_1_R_W.png")
            .put("1SR", "Move_1_R_R.png")
            .put("1DG", "Move_1_RRB1_G.png")
            .put("1DW", "Move_1_RRB1_W.png")
            .put("1DR", "Move_1_RRB1_R.png")
            .put("1EG","Move_1_TR_R_G.png")
            .put("1EW","Move_1_TR_R_W.png")
            .put("1ER","Move_1_TR_R_R.png")
            .put("1TG", "Move_1_H-L_G.png")
            .put("1TW", "Move_1_H-L_W.png")
            .put("1TR", "Move_1_H-L_R.png")
            .put("1BG", "Move_1_G-L_G.png")
            .put("1BW", "Move_1_G-L_W.png")
            .put("1BR", "Move_1_G-L_R.png")
            .put("1FG", "Move_1_S_G.png")
            .put("1FW", "Move_1_S_W.png")
            .put("1FR", "Move_1_S_R.png")
            .put("1NG", "Move_1_G-R_G.png")
            .put("1NW", "Move_1_G-R_W.png")
            .put("1NR", "Move_1_G-R_R.png")
            .put("1YG", "Move_1_H-R_G.png")
            .put("1YW", "Move_1_H-R_W.png")
            .put("1YR", "Move_1_H-R_R.png")
            .put("1LG","Move_1_TR_R_G.png")
            .put("1LW","Move_1_TR_R_W.png")
            .put("1LR","Move_1_TR_R_R.png")
            .put("1KG","Move_1_U_G.png")
            .put("1KW","Move_1_U_W.png")
            .put("1KR","Move_1_U_R.png")
            .put("1PG","Move_1_SL_R_G.png")
            .put("1PW","Move_1_SL_R_W.png")
            .put("1PR","Move_1_SL_R_R.png")
            .put("1RG","Move_1_TR_L_G.png")
            .put("1RW","Move_1_TR_L_W.png")
            .put("1RR","Move_1_TR_L_R.png")
            .put("2EG", "Move_2_TR_R_G.png")
            .put("2EW", "Move_2_TR_R_W.png")
            .put("2ER", "Move_2_TR_R_R.png")
            .put("2TG", "Move_2_H-L_G.png")
            .put("2TW", "Move_2_H-L_W.png")
            .put("2TR", "Move_2_H-L_R.png")
            .put("2BG", "Move_2_G-L_G.png")
            .put("2BW", "Move_2_G-L_W.png")
            .put("2BR", "Move_2_G-L_R.png")
            .put("2FG", "Move_2_S_G.png")
            .put("2FW", "Move_2_S_W.png")
            .put("2FR", "Move_2_S_R.png")
            .put("2NG", "Move_2_G-R_G.png")
            .put("2NW", "Move_2_G-R_W.png")
            .put("2NR", "Move_2_G-R_R.png")
            .put("2YG", "Move_2_H-R_G.png")
            .put("2YW", "Move_2_H-R_W.png")
            .put("2YR", "Move_2_H-R_R.png")
            .put("2LG", "Move_2_SL_L_G.png")
            .put("2LW", "Move_2_SL_L_W.png")
            .put("2LR", "Move_2_SL_L_R.png")
            .put("2KG", "Move_2_U_G.png")
            .put("2KW", "Move_2_U_W.png")
            .put("2KR", "Move_2_U_R.png")
            .put("2PG", "Move_2_SL_R_G.png")
            .put("2PW", "Move_2_SL_R_W.png")
            .put("2PR", "Move_2_SL_R_R.png")
            .put("2RG", "Move_2_TR_L_G.png")
            .put("2RW", "Move_2_TR_L_W.png")
            .put("2RR", "Move_2_TR_L_R.png")
            .put("3EG", "Move_3_TR_R_G.png")
            .put("3EW", "Move_3_TR_R_W.png")
            .put("3ER", "Move_3_TR_R_R.png")
            .put("3TG", "Move_3_H-L_G.png")
            .put("3TW", "Move_3_H-L_W.png")
            .put("3TR", "Move_3_H-L_R.png")
            .put("3BG", "Move_3_G-L_G.png")
            .put("3BW", "Move_3_G-L_W.png")
            .put("3BR", "Move_3_G-L_R.png")
            .put("3FG", "Move_3_S_G.png")
            .put("3FW", "Move_3_S_W.png")
            .put("3FR", "Move_3_S_R.png")
            .put("3NG", "Move_3_G-R_G.png")
            .put("3NW", "Move_3_G-R_W.png")
            .put("3NR", "Move_3_G-R_R.png")
            .put("3YG", "Move_3_H-R_G.png")
            .put("3YW", "Move_3_H-R_W.png")
            .put("3YR", "Move_3_H-R_R.png")
            .put("3LG", "Move_3_SL_L_G.png")
            .put("3LW", "Move_3_SL_L_W.png")
            .put("3LR", "Move_3_SL_L_R.png")
            .put("3KG", "Move_3_U_G.png")
            .put("3KW", "Move_3_U_W.png")
            .put("3KR", "Move_3_U_R.png")
            .put("3PG", "Move_3_SL_R_G.png")
            .put("3PW", "Move_3_SL_R_W.png")
            .put("3PR", "Move_3_SL_R_R.png")
            .put("3RG", "Move_3_TR_L_G.png")
            .put("3RW", "Move_3_TR_L_W.png")
            .put("3RR", "Move_3_TR_L_R.png")
            .put("4FG", "Move_4_S_G.png")
            .put("4FW", "Move_4_S_W.png")
            .put("4FR", "Move_4_S_R.png")
            .put("4KG", "Move_4_U_G.png")
            .put("4KW", "Move_4_U_W.png")
            .put("4KR", "Move_4_U_R.png")
            .put("5FG", "Move_5_S_G.png")
            .put("5FW", "Move_5_S_W.png")
            .put("5FR", "Move_5_S_R.png")
            .put("5KG", "Move_5_U_G.png")
            .put("5KW", "Move_5_U_W.png")
            .put("5KR", "Move_5_U_R.png")
            .build();
    
    // xwsship/faction / image
  //  private static Map<String, String> dialHideImages = ImmutableMap.<String, String>builder()
 //           .put("xwing/Rebel Alliance","")
 //           .build();

    // xwsship/faction / image
  //  private static Map<String, String> dialBackImages = ImmutableMap.<String, String>builder()
  //          .build();

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

            //TODO this is hardcoded - need to fix
            DialGenerateCommand myDialGen = new DialGenerateCommand("attackshuttle", piece, "Rebel Alliance");
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
        //List<List<Integer>> moveList; //possibly changed for a higher level class so it stays current with future xws spec changes

        List<String> newMoveList;
        String shipName;
        String faction = "";
        DialGenerateCommand(String thisName, GamePiece piece, String thisFaction) {

            // fetch the maneuver array of arrays according to the xws name passed on from autospawn or other means
            xwsShipName = thisName;
            faction = thisFaction;
            MasterShipData.ShipData shipData = MasterShipData.getShipData(xwsShipName);
           // moveList = shipData.getManeuvers();
            newMoveList = shipData.getDialManeuvers();
            shipName = shipData.getName();
            this.piece = piece;

        }

            // construct the dial Layers trait (Embellishment class) layer by layer according to the previous Array of Arrays.
            protected void executeCommand() {

            //Now: take a hardcoded string (attack shuttle dial info taken from the build file, take note that the backslashes are doubled
            //Pasting in intellij automatically added them in the context of a string value
            //The key is to access the embellishment decorator associated with the piece and then use its mySetType method.
            //Later: fetch the string from a helper function that constructs the string bit by bit
            //hardcode some of them with a map because the order of moves is counterintuitive (ie the protectorate iirc).
            //Usually, red sloops or red trolls are on separate sides, but some dials group them together at the end of the moves of the same speed
            //ie sloop left, turn left, bank left, straight, bank right, turn right, sloop right can be found on some ships
            //turn left, bank left, straight, bank right, turn right, sloop left, sloop right can be found in other ships
            //the perfect order is also found on http://xwvassal.info/dialgen/dialgen.html. The online builders that use the xws spec do not carry this exact information

                logToChat("execute command = current ship xws name is: " + xwsShipName);

                // build the state string
                StringBuilder stateString = new StringBuilder();
                StringBuilder moveNamesString = new StringBuilder();

                // start the state string
                stateString.append("emb2;;2;;Right;2;;Left;2;;;;;false;0;-38;");

                // loop through the maneuvers from the xws-data
                int count = 0;
                String moveImage;
                for (String move : newMoveList)
                {


                    // look up the image for the maneuver
                    moveImage = (String)dialManeuverImages.get(move);
                    if(moveImage == null)
                    {
                        logToChat("Can't find image for move: " + move);
                    }else{

                        count++;
                        if(count != 1)
                        {
                            stateString.append(",");
                            moveNamesString.append(",");
                        }
                        // add the maneuver image to the dial
                        stateString.append(moveImage);



                        // build move names string
                        String speed = move.substring(0,1);
                        String moveCode = move.substring(1,2);
                        String moveName = maneuverNames.get(moveCode);
                        moveNamesString.append(moveName).append(" ").append(speed);


                    }
                }
                // add in move names
                stateString.append(";").append(moveNamesString.toString());

                // finish the state string
                stateString.append(";true;Move;;;false;;1;1;true;;46,0;44,0\\\\\\\\\tpiece;;;\t\\\tnull;\\\\\t\\\\\\\t1\\\\\\\\");
                //String dialString = "emb2;;2;;Right;2;;Left;2;;;;;false;0;-38;Move_1_H-L_R.png,Move_1_G-L_G.png,Move_1_S_G.png,Move_1_G-R_G.png,Move_1_H-R_R.png,Move_2_H-L_W.png,Move_2_G-L_W.png,Move_2_S_G.png,Move_2_G-R_W.png,Move_2_H-R_W.png,Move_3_H-L_R.png,Move_3_G-L_W.png,Move_3_S_W.png,Move_3_G-R_W.png,Move_3_H-R_R.png,Move_4_S_W.png,Move_4_U_R.png;Hard Left 1,Bank Left 1,Forward 1,Bank Right 1,Hard Right 1,Hard Left 2,Bank Left 2,Forward 2,Bank Right 2,Hard Right 2,Hard Left 3,Bank Left 3,Forward 3,Bank Right 3,Hard Right 3,Forward 4,K-Turn 4;true;Move;;;false;;1;1;true;;46,0;44,0\\\\\\\\\tpiece;;;Dial_Rebel_n.png;dial for Attack Shuttle/\t\\\tnull;\\\\\t\\\\\\\t1\\\\\\\\";

                Embellishment myEmb = (Embellishment)Decorator.getDecorator(piece,Embellishment.class);

                //myEmb.mySetType(dialString);
                myEmb.mySetType(stateString.toString());



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


