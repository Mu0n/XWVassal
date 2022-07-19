package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import mic.ota.XWOTAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static mic.Util.logToChat;

/**
 * Created by Mic on 7/08/2018.
 *
 * Can be used to set a dial to a specific ship at autospawn-time
 *
 *  * Improved by Mic on 04/12/2018.
 *  *
 *  * New style of dial:
 *  *
 *  * 1) looks like the open face 2nd edition dial instead of the 1st edition
 *  * 2) no more additional graphics due to the masked/hidden side - a simple eye-with-slash icon will be used to indicate the hidden mode of the dial (icon at center)
 *  * 3) when in reveal mode, the selected move (at the top) will be copied, larger, in the center and rotations can't be done at all
 *  * 4) when in hidden mode, dial rotation commands will only tweak the rotation of the faceplate for the owner of the dial, not for any other player
 *  * 5) The pilot name in text (white text over black background) above the dial; the ship name in text (same colors) under the dial, no more icon gfx to manage
 *  * 6) a player can't cheat anymore by swapping mask gfx by a transparent empty png
 *  * 7) the open face dial has to be kept in OTA2 - no mistakes are allowed because patches can't happen, unless a download all is forced in the content checker
 *  * 8) File name should be: D2e_'ship name from contracted manifest file names'.jpg
 *  * 9) Dial graphics should be generated by Mike's tool when he has time to implement it, otherwise generated by me in an outside program or from dialgen + photoshop
 *  * 10) new dial graphics added when a ship graphic is added in the best case scenario as info pours in from previews.
 */
public class StemDial2e extends Decorator implements EditablePiece {
    public static final String ID = "stemdial2e";

    // English names of the maneuvers
    // DialGen move / English Name
    public static Map<String, String> maneuverNames = ImmutableMap.<String, String>builder()
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

    // maneuver images for the dial
    // DialGen format / image name
    private static Map<String, String> dialManeuverImages = ImmutableMap.<String, String>builder()
            .put("0OB", "Move_0_B.png")
            .put("0OW", "Move_0_W.png")
            .put("0OR", "Move_0_R.png")
            .put("1AB", "Move_1_RLB1_B.png")
            .put("1AW", "Move_1_RLB1_W.png")
            .put("1AR", "Move_1_RLB1_R.png")
            .put("1SB", "Move_1_R_B.png")
            .put("1SW", "Move_1_R_W.png")
            .put("1SR", "Move_1_R_R.png")
            .put("1DB", "Move_1_RRB1_B.png")
            .put("1DW", "Move_1_RRB1_W.png")
            .put("1DR", "Move_1_RRB1_R.png")
            .put("1EB","Move_1_TR_R_B.png")
            .put("1EW","Move_1_TR_R_W.png")
            .put("1ER","Move_1_TR_R_R.png")
            .put("1TB", "Move_1_H-L_B.png")
            .put("1TW", "Move_1_H-L_W.png")
            .put("1TR", "Move_1_H-L_R.png")
            .put("1BB", "Move_1_G-L_B.png")
            .put("1BW", "Move_1_G-L_W.png")
            .put("1BR", "Move_1_G-L_R.png")
            .put("1FB", "Move_1_S_B.png")
            .put("1FW", "Move_1_S_W.png")
            .put("1FR", "Move_1_S_R.png")
            .put("1NB", "Move_1_G-R_B.png")
            .put("1NW", "Move_1_G-R_W.png")
            .put("1NR", "Move_1_G-R_R.png")
            .put("1YB", "Move_1_H-R_B.png")
            .put("1YW", "Move_1_H-R_W.png")
            .put("1YR", "Move_1_H-R_R.png")
            .put("1LB","Move_1_TR_R_B.png")
            .put("1LW","Move_1_TR_R_W.png")
            .put("1LR","Move_1_SL_L_R.png")
            .put("1KB","Move_1_U_B.png")
            .put("1KW","Move_1_U_W.png")
            .put("1KR","Move_1_U_R.png")
            .put("1PB","Move_1_SL_R_B.png")
            .put("1PW","Move_1_SL_R_W.png")
            .put("1PR","Move_1_SL_R_R.png")
            .put("1RB","Move_1_TR_L_B.png")
            .put("1RW","Move_1_TR_L_W.png")
            .put("1RR","Move_1_TR_L_R.png")
            .put("2SB", "Move_2_R_B.png")
            .put("2SW", "Move_2_R_W.png")
            .put("2SR", "Move_2_R_R.png")
            .put("2EB", "Move_2_TR_R_B.png")
            .put("2EW", "Move_2_TR_R_W.png")
            .put("2ER", "Move_2_TR_R_R.png")
            .put("2EP", "Move_2_TR_R_P.png")
            .put("2TB", "Move_2_H-L_B.png")
            .put("2TW", "Move_2_H-L_W.png")
            .put("2TR", "Move_2_H-L_R.png")
            .put("2BB", "Move_2_G-L_B.png")
            .put("2BW", "Move_2_G-L_W.png")
            .put("2BR", "Move_2_G-L_R.png")
            .put("2FB", "Move_2_S_B.png")
            .put("2FW", "Move_2_S_W.png")
            .put("2FR", "Move_2_S_R.png")
            .put("2NB", "Move_2_G-R_B.png")
            .put("2NW", "Move_2_G-R_W.png")
            .put("2NR", "Move_2_G-R_R.png")
            .put("2YB", "Move_2_H-R_B.png")
            .put("2YW", "Move_2_H-R_W.png")
            .put("2YR", "Move_2_H-R_R.png")
            .put("2LB", "Move_2_SL_L_B.png")
            .put("2LW", "Move_2_SL_L_W.png")
            .put("2LR", "Move_2_SL_L_R.png")
            .put("2KB", "Move_2_U_B.png")
            .put("2KW", "Move_2_U_W.png")
            .put("2KR", "Move_2_U_R.png")
            .put("2PB", "Move_2_SL_R_B.png")
            .put("2PW", "Move_2_SL_R_W.png")
            .put("2PR", "Move_2_SL_R_R.png")
            .put("2RB", "Move_2_TR_L_B.png")
            .put("2RW", "Move_2_TR_L_W.png")
            .put("2RR", "Move_2_TR_L_R.png")
            .put("2RP", "Move_2_TR_L_P.png")
            .put("3EB", "Move_3_TR_R_B.png")
            .put("3EW", "Move_3_TR_R_W.png")
            .put("3ER", "Move_3_TR_R_R.png")
            .put("3TB", "Move_3_H-L_B.png")
            .put("3TW", "Move_3_H-L_W.png")
            .put("3TR", "Move_3_H-L_R.png")
            .put("3BB", "Move_3_G-L_B.png")
            .put("3BW", "Move_3_G-L_W.png")
            .put("3BR", "Move_3_G-L_R.png")
            .put("3FB", "Move_3_S_B.png")
            .put("3FW", "Move_3_S_W.png")
            .put("3FR", "Move_3_S_R.png")
            .put("3NB", "Move_3_G-R_B.png")
            .put("3NW", "Move_3_G-R_W.png")
            .put("3NR", "Move_3_G-R_R.png")
            .put("3YB", "Move_3_H-R_B.png")
            .put("3YW", "Move_3_H-R_W.png")
            .put("3YR", "Move_3_H-R_R.png")
            .put("3LB", "Move_3_SL_L_B.png")
            .put("3LW", "Move_3_SL_L_W.png")
            .put("3LR", "Move_3_SL_L_R.png")
            .put("3KB", "Move_3_U_B.png")
            .put("3KW", "Move_3_U_W.png")
            .put("3KR", "Move_3_U_R.png")
            .put("3PB", "Move_3_SL_R_B.png")
            .put("3PW", "Move_3_SL_R_W.png")
            .put("3PR", "Move_3_SL_R_R.png")
            .put("3RB", "Move_3_TR_L_B.png")
            .put("3RW", "Move_3_TR_L_W.png")
            .put("3RR", "Move_3_TR_L_R.png")
            .put("4FB", "Move_4_S_B.png")
            .put("4FW", "Move_4_S_W.png")
            .put("4FR", "Move_4_S_R.png")
            .put("4KB", "Move_4_U_B.png")
            .put("4KW", "Move_4_U_W.png")
            .put("4KR", "Move_4_U_R.png")
            .put("5FB", "Move_5_S_B.png")
            .put("5FW", "Move_5_S_W.png")
            .put("5FR", "Move_5_S_R.png")
            .put("5KB", "Move_5_U_B.png")
            .put("5KW", "Move_5_U_W.png")
            .put("5KR", "Move_5_U_R.png")
            .build();

    public static Map<String, String> dialHeadingImages = ImmutableMap.<String, String>builder()
            .put("OB", "mOB.png")
            .put("OW", "mOW.png")
            .put("OR", "mOR.png")
            .put("AB", "mAB.png")
            .put("AW", "mAW.png")
            .put("AR", "mAR.png")
            .put("SB", "mSB.png")
            .put("SW", "mSW.png")
            .put("SR", "mSR.png")
            .put("DB", "mDB.png")
            .put("DW", "mDW.png")
            .put("DR", "mDR.png")
            .put("EB","mEB.png")
            .put("EW","mEW.png")
            .put("ER","mER.png")
            .put("EP","mEP.png")
            .put("TB", "mTB.png")
            .put("TW", "mTW.png")
            .put("TR", "mTR.png")
            .put("BB", "mBB.png")
            .put("BW", "mBW.png")
            .put("BR", "mBR.png")
            .put("FB", "mFB.png")
            .put("FW", "mFW.png")
            .put("FR", "mFR.png")
            .put("NB", "mNB.png")
            .put("NW", "mNW.png")
            .put("NR", "mNR.png")
            .put("YB", "mYB.png")
            .put("YW", "mYW.png")
            .put("YR", "mYR.png")
            .put("LB","mLB.png")
            .put("LW","mLW.png")
            .put("LR","mLR.png")
            .put("KB","mKB.png")
            .put("KW","mKW.png")
            .put("KR","mKR.png")
            .put("PB","mPB.png")
            .put("PW","mPW.png")
            .put("PR","mPR.png")
            .put("RB","mRB.png")
            .put("RW","mRW.png")
            .put("RR","mRR.png")
            .put("RP","mRP.png")
            .build();

    public StemDial2e(){
        this(null);
    }

    public StemDial2e(GamePiece piece){
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
        String pieceId;
        GamePiece piece;
        static String xwsShipName = "";
        List<String> newMoveList;
        String shipName;
        String faction = "";
        String whoOwns;
        String associatedShipID = "";

//this is the point of entry for encoding the info and sending it to someone else, some stuff that's not a String have to be re-found, such
        // as the GamePiece object, the list of moves
        DialGenerateCommand(String reqPieceId, String aShipName, String thisFaction, String owner, String targettingThisShipID) {
            // hybrid approach, only send strings, strict minimum so anyone can receive this data and generate the dial on their own
                            /* parts
                0: shipXWSName
                1: faction
                2: who owns it (just the number of the player in string form from 1 to 8)
                3: the unique UUID of the ship piece that's associated to it
                 */
            //fetch the unspecified-yet dial piece
            pieceId = reqPieceId;
            //find its GamePiece presence in the running module
            Collection<GamePiece> allPieces = GameModule.getGameModule().getAllDescendantComponentsOf(GamePiece.class);
            for(GamePiece gp : allPieces) {
            if(gp.getId().equals(pieceId)) piece = gp;
            }
            //transfer over the xws name
            xwsShipName = Canonicalizer.getCleanedName(aShipName);
            //find the list of moves based on the xws name
            List<XWS2Pilots> allShips = XWS2Pilots.loadFromLocal();
            XWS2Pilots theShip = XWS2Pilots.getSpecificShipFromPilotXWS2(xwsShipName, allShips);
            newMoveList = theShip.getDial();

            //untreated ship name
            shipName = aShipName;

            //faction
            faction = thisFaction;

            //owner number in string format
            whoOwns = owner;

            //associated ship id
            associatedShipID = targettingThisShipID;
        }

        //this is the point of entry for the player who generates a dial
        DialGenerateCommand(List<String> aMoveList, String aShipName, GamePiece piece, String thisFaction, String owner, String targettingThisShipID) {
            // more direcct approach where the move list and the ship name are dictated directly without a master list fetch
            faction = thisFaction;
            newMoveList = aMoveList;
            shipName = aShipName;
            this.piece = piece;
            whoOwns = owner;
            xwsShipName = Canonicalizer.getCleanedName(shipName);
            associatedShipID = targettingThisShipID;
        }



        public String getOwner(){
            return whoOwns;
        }

        // construct the dial Layers trait (Embellishment class) layer by layer according to the previous Array of Arrays.
        protected void executeCommand() {
            // build the type string
            StringBuilder stateString = new StringBuilder();
            StringBuilder moveNamesString = new StringBuilder();
            StringBuilder basicPieceString = new StringBuilder();
            StringBuilder factionRingString = new StringBuilder();

            // start the state string
            stateString.append("emb2;Activate;2;;;2;;;2;;;;1;false;0;-24;,");
            basicPieceString.append("emb2;;2;;;2;;;2;;;;;false;0;0;");

            String moveImage;
            String move = newMoveList.get(0);
            String moveWithoutSpeed = move.substring(1);
            moveImage = dialHeadingImages.get(moveWithoutSpeed);

            String speed = move.substring(0,1);
            String moveCode = move.substring(1,2);
            String moveName = maneuverNames.get(moveCode);

            basicPieceString.append("D2e_" + xwsShipName + ".png");
            basicPieceString.append(";;false;Front Plate;;;false;;1;1;true;;;");

            moveNamesString.append(moveName).append(" ").append(speed);
            // add in move names
            stateString.append(moveImage);
            stateString.append(";empty,"+moveNamesString);

            // finish the type string
            stateString.append(";false;Chosen Move;;;false;;1;1;true;65,130");
            Embellishment chosenMoveEmb = (Embellishment)Util.getEmbellishment(piece,"Layer - Chosen Move");
            Embellishment chosenSpeedEmb = (Embellishment)Util.getEmbellishment(piece, "Layer - Chosen Speed");
            Embellishment fullDialEmb = (Embellishment)Util.getEmbellishment(piece, "Layer - Front Plate");
            Embellishment ringEmb = (Embellishment)Util.getEmbellishment(piece, "Layer - Faction Ring");

            factionRingString.append("emb2;;2;;;2;;;2;;;;;false;0;0;DialSelect_");
            factionRingString.append(faction);
            factionRingString.append(".png;;false;Faction Ring;;;false;;1;1;true;;;");
            chosenSpeedEmb.setValue(Integer.parseInt(speed)+1);

            String injectDialString = "";
            int count=0;
            for(String moveItem : newMoveList) {
                count++;
                injectDialString+= moveItem;
                if(count!=newMoveList.size()) injectDialString+=",";
            }
            piece.setProperty("dialstring",injectDialString);
            piece.setProperty("shipID", associatedShipID);

            // chosen move embellishment looks like: emb2;Activate;2;;;2;;;2;;;;1;false;0;-24;,mFW.png;empty,move;false;Chosen Move;;;false;;1;1;true;65,130;;
            // chosen speed embell looks like:       emb2;;2;;;2;;;2;;;;1;false;0;24;kimb5.png,,kimb0.png,kimb1.png,kimb2.png,kimb3.png,kimb4.png;5,empty,0,1,2,3,4;false;Chosen Speed;;;false;;1;1;true;;;
            // basic piece face plate looks like: emb2;;2;;;2;;;2;;;;;false;0;0;D2e_arc170starfighter.png;;false;Front Plate;;;false;;1;1;true;;;
            // faction ring looks like: emb2;;2;;;2;;;2;;;;;false;0;-6;DialSelect_rebelalliance.png;;false;Faction Ring;;;false;;1;1;true;;;

            chosenMoveEmb.mySetType(stateString.toString());
            chosenMoveEmb.setValue(1);

            fullDialEmb.mySetType(basicPieceString.toString());
            ringEmb.mySetType(factionRingString.toString());
        }

        // build the maneuvers layer
        private void buildManeuvers(GamePiece piece, List moveList)
        {
            // build the type string
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

            // finish the type string
            stateString.append(";true;Move;;;false;;1;1;true;;46,0;44,0");

            Embellishment myEmb = (Embellishment)Util.getEmbellishment(piece,"Layer - Move");
            //Embellishment myEmb = (Embellishment)Decorator.getDecorator(piece,Embellishment.class);

            myEmb.mySetType(stateString.toString());

        }




        private void buildDialMask(GamePiece piece, String xwsShipName, String faction)
        {
            final String wipResistanceMask = "Dial_Back_Resistance_WIP.png";
            final String wipRebelMask = "Dial_Back_Rebel_WIP.png";
            final String wipFirstOrderMask = "Dial_Back_FirstOrder_WIP.png";
            final String wipEmpireMask = "Dial_Back_Empire_WIP.png";
            final String wipScumMask = "Dial_Back_Scum_WIP.png";
            final String wipRepublicMask = "Dial_Back_Republic_WIP.png";
            final String wipCISMask = "Dial_Back_CIS_WIP.png";


            // first get the core faction name from the subfaction (i.e. Resistance => RebelAlliance
            String coreFactionName = null;
            if(faction.equalsIgnoreCase("Rebel Alliance"))
            {
                coreFactionName = "rebelalliance";
            }else if(faction.equalsIgnoreCase("Resistance")){
                coreFactionName = "resistance";
            } else if(faction.equalsIgnoreCase("Galactic Empire")){
                coreFactionName = "galacticempire";
            }else if(faction.equalsIgnoreCase("First Order")) {
                coreFactionName = "firstorder";
            } else if(faction.equalsIgnoreCase("Scum and Villainy")){
                coreFactionName = "scumandvillainy";
            }else if(faction.equalsIgnoreCase("Galactic Republic")){
                coreFactionName = "galacticrepublic";
            }else if(faction.equalsIgnoreCase("CIS")){
                coreFactionName = "cis";
            }

            // get the back image
            // String dialBackImage = dialBackImages.get(xwsShipName+"/"+faction);
            String dialMaskImageName = "DialMask_"+coreFactionName+"_"+xwsShipName+".png";

            // if we don't have the image (unreleased ship), use a WIP image
            if(!XWOTAUtils.imageExistsInModule(dialMaskImageName))
            {
                if(faction.equalsIgnoreCase("Resistance"))
                {
                    dialMaskImageName = wipResistanceMask;
                }else if(faction.equalsIgnoreCase("Rebel Alliance"))
                {
                    dialMaskImageName = wipRebelMask;
                }else if(faction.equalsIgnoreCase("First Order"))
                {
                    dialMaskImageName = wipFirstOrderMask;
                }else if(faction.equalsIgnoreCase("Galactic Empire"))
                {
                    dialMaskImageName = wipEmpireMask;
                }else if(faction.equalsIgnoreCase("Scum and Villainy"))
                {
                    dialMaskImageName = wipScumMask;
                }else if(faction.equalsIgnoreCase("Galactic Republic"))
                {
                    dialMaskImageName = wipRepublicMask;
                }else if(faction.equalsIgnoreCase("CIS"))
                {
                    dialMaskImageName = wipCISMask;
                }
            }


            // get the dial hide image
            // String dialHideImage = dialHideImages.get(xwsShipName);
            String dialHideImageName = "DialHide_"+xwsShipName+".png";

            // if we don't have the image (unreleased ship), use a WIP image
            if(!XWOTAUtils.imageExistsInModule(dialHideImageName))
            {
                dialHideImageName = "Dial_Hide_WIP.png";
            }

            // build the string
            StringBuilder sb = new StringBuilder();
            sb.append("obs;82,130;");
            sb.append(dialMaskImageName);
            sb.append(";Reveal;G");
            sb.append(dialHideImageName);
            sb.append(";;player:;Peek");

            Obscurable myObs = (Obscurable)Decorator.getDecorator(piece,Obscurable.class);
            myObs.mySetType(sb.toString());

        }

        protected Command myUndoCommand() {
            return null;
        }

        //the following class is used to send the info to the other player whenever a dial generation command is issued, so it can be done locally on all machines playing/watching the game
        //only the ship XWS string is sent
        public static class DialGeneratorEncoder implements CommandEncoder {
            private static final Logger logger = LoggerFactory.getLogger(StemDial2e.class);
            private static final String commandPrefix = "DialGeneratorEncoder=";
            private static final String itemDelim = "\t";

            public static StemDial2e.DialGenerateCommand.DialGeneratorEncoder INSTANCE = new StemDial2e.DialGenerateCommand.DialGeneratorEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding DialGenerateCommand");

                command = command.substring(commandPrefix.length());
                String[] parts = command.split(itemDelim);
                /* parts
                0: pieceId of a stem dial piece not specified yet but generated and sent to everyone after it's made from a PieceSlot
                1: shipXWSName
                2: faction
                3: who owns it (just the number of the player in string form from 1 to 8)
                4: the unique UUID of the ship piece that's associated to it
                 */
                try {
                    return new DialGenerateCommand(parts[0], parts[1], parts[2], parts[3], parts[4]);
                } catch (Exception e) {
                    logger.error("Error decoding DialGenerateCommand", e);
                    return null;
                }
            }

            public String encode(Command c) {
                if (!(c instanceof DialGenerateCommand)) {
                    return null;
                }
                logger.info("Encoding DialGenerateCommand");
                DialGenerateCommand dgc = (DialGenerateCommand) c;
                try {
                    return commandPrefix + Joiner.on(itemDelim).join(dgc.pieceId, dgc.xwsShipName, dgc.faction, dgc.whoOwns, dgc.associatedShipID);
                } catch(Exception e) {
                    logger.error("Error encoding DialGenerateCommand", e);
                    return null;
                }
            }
        }

    }




}
