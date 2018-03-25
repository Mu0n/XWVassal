package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import mic.ota.XWOTAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

import static mic.Util.logToChat;

/**
 * Created by Mic on 12/21/2017.
 *
 * Can be used to set a dial to a specific ship at autospawn-time
 */
public class StemDial extends Decorator implements EditablePiece {
    public static final String ID = "stemdial";

    // English names of the maneuvers
    // DialGen move / English Name
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

    // maneuver images for the dial
    // DialGen format / image name
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
/*
    // Dial Hide images (ship image that shows active player that the dial is hidden
    // xwsship / image
    private static Map<String, String> dialHideImages = ImmutableMap.<String, String>builder()
            .put("xwing","Dial_Hide_Rebel_X-Wing.png")
            .put("ywing","Dial_Hide_Rebel_Y-Wing.png")
            .put("awing","Dial_Hide_Rebel_A-Wing.png")
            .put("yt1300","Dial_Hide_Rebel_YT-1300.png")
            .put("tiefighter","Dial_Hide_Empire_TIE_Fighter.png")
            .put("tieadvanced","Dial_Hide_Empire_TIE_Advanced.png")
            .put("tieinterceptor","Dial_Hide_Empire_TIE_Interceptor.png")
            .put("firespray31","Dial_Hide_Empire_Firespray-31.png")
            .put("hwk290","Dial_Hide_Rebel_HWK-290.png")
            .put("lambdaclassshuttle","Dial_Hide_Empire_Lambda-Class_Shuttle.png")
            .put("bwing","Dial_Hide_Rebel_B-Wing.png")
            .put("tiebomber","Dial_Hide_Empire_TIE_Bomber.png")
            .put("gr75mediumtransport","Dial_Hide_Rebel_GR-75.png")
            .put("z95headhunter","Dial_Hide_Rebel_Z95.png")
            .put("tiedefender","Dial_Hide_Empire_TIE_Defender.png")
            .put("ewing","Dial_Hide_Rebel_E-Wing.png")
            .put("tiephantom","Dial_Hide_Empire_TIE_Phantom.png")
            // do we need both of these?
            .put("cr90corvettefore","Dial_Hide_Rebel_CR90.png")
            .put("cr90corvetteaft","Dial_Hide_Rebel_CR90.png")
            .put("yt2400","Dial_Hide_Rebel_YT-2400.png")
            .put("vt49decimator","Dial_Hide_Empire_VT-49.png")
            .put("starviper","Dial_Hide_Scum_StarViper.png")
            .put("m3ainterceptor","Dial_Hide_Scum_m3-a.png")
            .put("aggressor","Dial_Hide_Scum_Aggressor.png")
            // do we need both of these?
            .put("raiderclasscorvettefore","Dial_Hide_Imperial_Raider.png")
            .put("raiderclasscorvetteaft","Dial_Hide_Imperial_Raider.png")
            .put("yv666","Dial_Hide_Scum_YV-666.png")
            .put("kihraxzfighter","Dial_Hide_Scum_Kihraxz.png")
            .put("kwing","Dial_Hide_Rebel_K-Wing.png")
            .put("tiepunisher","Dial_Hide_Imperial_Tie_Punisher.png")
            .put("t70xwing","Dial_Hide_Rebel_T70_X-Wing.png")
            .put("tiefofighter","Dial_Hide_Empire_TIEFO_Fighter.png")
            .put("vcx100","Dial_Hide_Rebel_VCX-100.png")
            .put("attackshuttle","Dial_Hide_Rebel_Attack_Shuttle.png")
            .put("jumpmaster5000","Dial_Hide_Scum-JM5k.png")
            .put("g1astarfighter","Dial_Hide_Scum-G-1Ai.png")
            .put("tieadvprototype","Dial_Hide_Empire_TAP.png")
            .put("gozanticlasscruiser","Dial_Hide_Empire_Gozanti.png")
            .put("arc170","Dial_Hide_Rebel_ARC-170.png")
            .put("tiesffighter","Dial_Hide_Empire_SF_Fighter.png")
            .put("protectoratestarfighter","Ship_Protectorate_Starfighter.png")
            .put("lancerclasspursuitcraft","Dial_Hide_Scum_Lancer-Class.png")
            .put("upsilonclassshuttle","Dial_Hide_Empire_Upsilon.png")
            .put("quadjumper","Dial_Hide_Scum_Quadjumper.png")
            .put("tiestriker","Dial_Hide_Empire_TIE Striker.png")
            .put("uwing","Dial_Hide_Rebel_U-Wing.png")
            .put("croccruiser","Dial_Hide_Empire_CROC.png")
            .put("auzituckgunship","Dial_Hide_Rebel_Auzituck_Gunship.png")
            .put("tieaggressor","Dial_Hide_Empire_TIE_Aggressor.png")
            .put("scurrgh6bomber","Dial_Hide_Rebel_Scurrg.png")
            .put("alphaclassstarwing","Dial_Hide_Empire_Alpha_Class.png")
            .put("m12lkimogilafighter","Dial_Hide_Scum_kimogila.png")
            .put("sheathipedeclassshuttle","Dial_Hide_Rebel_Sheathipede.png")
            .put("tiesilencer","Dial_Hide_Empire_TIE-Silencer.png")
            .put("bsf17bomber","Dial_Hide_Rebel_BSF-17.png")
            .put("unreleased","Dial_Hide_WIP.png")
            .build();
*/
/*
    // Dial Back images - images that the opposing player sees when the dial is hidden
    // xwsship/faction / image
    private static Map<String, String> dialBackImages = ImmutableMap.<String, String>builder()
            .put("xwing/Rebel Alliance","Dial_Back_Rebel_X-Wing_n.png")
            .put("ywing/Rebel Alliance","Dial_Back_Rebel_Y-Wing_n.png")
            .put("ywing/Scum and Villainy","Dial_Back_Scum_Y-Wing_n.png")
            .put("awing/Rebel Alliance","Dial_Back_Rebel_A-Wing_n.png")
            .put("yt1300/Rebel Alliance","Dial_Back_Rebel_YT-1300_n.png")
            .put("yt1300/Resistance","Dial_Back_Rebel_YT-1300_n.png")
            .put("tiefighter/Galactic Empire","Dial_Back_Empire_TIE_Fighter_n.png")
            .put("tiefighter/Rebel Alliance","Dial_Back_Rebel_Sab_TIE_Fighter_n.png")
            .put("tieadvanced/Galactic Empire","Dial_Back_Empire_TIE_Advanced_n.png")
            .put("tieinterceptor/Galactic Empire","Dial_Back_Empire_TIE_Interceptor_n.png")
            .put("firespray31/Galactic Empire","Dial_Back_Empire_Firespray-31_n.png")
            .put("firespray31/Scum and Villainy","Dial_Back_Scum_FS-31_n.png")
            .put("hwk290/Rebel Alliance","Dial_Back_Rebel_HWK-290_n.png")
            .put("hwk290/Scum and Villainy","Dial_Back_Scum_HWK-290_n.png")
            .put("lambdaclassshuttle/Galactic Empire","Dial_Back_Empire_Lambda-Class_Shuttle_n.png")
            .put("bwing/Rebel Alliance","Dial_Back_Rebel_B-Wing_n.png")
            .put("tiebomber/Galactic Empire","Dial_Back_Empire_TIE_Bomber_n.png")
            .put("gr75mediumtransport/Rebel Alliance","Dial_Back_Rebel_GR-75.png")
            .put("z95headhunter/Rebel Alliance","Dial_Back_Rebel_Z95_n.png")
            .put("z95headhunter/Scum and Villainy","Dial_Back_Scum_Z95_n.png")
            .put("tiedefender/Galactic Empire","Dial_Back_Empire_TIE_Defender_n.png")
            .put("ewing/Rebel Alliance","Dial_Back_Rebel_E-Wing_n.png")
            .put("tiephantom/Galactic Empire","Dial_Back_Empire_TIE_Phantom_n.png")
            // do we need both of these?
            .put("cr90corvettefore/Rebel Alliance","Dial_Back_Rebel_CR90_n.png")
            .put("cr90corvetteaft/Rebel Alliance","Dial_Back_Rebel_CR90_n.png")
            .put("yt2400/Rebel Alliance","Dial_Back_Rebel_YT-2400_n.png")
            .put("vt49decimator/Galactic Empire","Dial_Back_Empire_VT-49_n.png")
            .put("starviper/Scum and Villainy","Dial_Back_Scum_StarViper_n.png")
            .put("m3ainterceptor/Scum and Villainy","Dial_Back_Scum_m3-a_n.png")
            .put("aggressor/Scum and Villainy","Dial_Back_Scum_Aggressor_n.png")
            // do we need both of these?
            .put("raiderclasscorvettefore/Galactic Empire","Dial_Back_Empire_Raider_n.png")
            .put("raiderclasscorvetteaft/Galactic Empire","Dial_Back_Empire_Raider_n.png")
            .put("yv666/Scum and Villainy","Dial_Back_Scum_YV-666_n.png")
            .put("kihraxzfighter/Scum and Villainy","Dial_Back_Scum_kihraxz_n.png")
            .put("kwing/Rebel Alliance","Dial_Back_Rebel_K-Wing_n.png")
            .put("tiepunisher/Galactic Empire","Dial_Back_Empire_TIE_Punisher_n.png")
            .put("t70xwing/Resistance","Dial_Back_Rebel_T70_X-Wing_n.png")
            .put("tiefofighter/First Order","Dial_Back_Empire_TIEFO_Fighter copy_n.png")
            .put("vcx100/Rebel Alliance","Dial_Back_Rebel_VCX-100_n.png")
            .put("attackshuttle/Rebel Alliance","Dial_Back_Rebel_Attack_Shuttle_n.png")
            .put("jumpmaster5000/Scum and Villainy","Dial_Back_Scum_JM5k_n.png")
            .put("g1astarfighter/Scum and Villainy","Dial_Back_Scum_G-1A_n.png")
            .put("tieadvprototype/Galactic Empire","Dial_Back_Empire_TAP_n.png")
            .put("gozanticlasscruiser/Galactic Empire","Dial_Back_Empire_Gozanti_n.png")
            .put("arc170/Rebel Alliance","Dial_Back_Rebel_ARC-170_n.png")
            .put("tiesffighter/First Order","Dial_Back_Empire_SF_Fighter_n.png")
            .put("protectoratestarfighter/Scum and Villainy","Dial_Back_Scum_Protectorate_Starfighter_n.png")
            .put("lancerclasspursuitcraft/Scum and Villainy","Dial_Back_Scum_Lancer-Class_n.png")
            .put("upsilonclassshuttle/First Order","Dial_Back_Empire_Upsilon_n.png")
            .put("quadjumper/Scum and Villainy","Dial_Back_Scum_Quadjumper_n.png")
            .put("tiestriker/Galactic Empire","Dial_Back_Empire_TIE Striker_n.png")
            .put("uwing/Rebel Alliance","Dial_Back_Rebel_U-Wing_n.png")
            .put("croccruiser/Scum and Villainy","Dial_Back_Scum_Croc_n.png")
            .put("auzituckgunship/Rebel Alliance","Dial_Back_Rebel_Auzituck_Gunship_n.png")
            .put("tieaggressor/Galactic Empire","Dial_Back_Empire_TIE_Aggressor_n.png")
            .put("scurrgh6bomber/Rebel Alliance","Dial_Back_Rebel_Scurrg_n.png")
            .put("scurrgh6bomber/Scum and Villainy","Dial_Back_Scum_Scurrg_n.png")
            .put("alphaclassstarwing/Galactic Empire","Dial_Back_Empire_Alpha_Class.png")
            .put("m12lkimogilafighter/Scum and Villainy","Dial_Back_Scum_kimogila.png")
            .put("sheathipedeclassshuttle/Rebel Alliance","Dial_Back_Rebel_Sheathipede.png")
            .put("tiesilencer/First Order","Dial_Back_Empire_TIE_Silencer.png")
            .put("bsf17bomber/Resistance","Dial_Back_Rebel_BSF-17.png")

            .put("unreleased/Resistance","Dial_Back_Rebel_WIP.png")
            .put("unreleased/Rebel Alliance","Dial_Back_Rebel_WIP.png")
            .put("unreleased/First Order","Dial_Back_Empire_WIP.png")
            .put("unreleased/Galactic Empire","Dial_Back_Empire_WIP.png")
            .put("unreleased/Scum and Villainy","Dial_Back_Scum_WIP.png")
            .build();
*/
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

        List<String> newMoveList;
        String shipName;
        String faction = "";

        DialGenerateCommand(String thisName, GamePiece piece, String thisFaction) {

            // fetch the maneuver array of arrays according to the xws name passed on from autospawn or other means
            xwsShipName = thisName;
            faction = thisFaction;
            MasterShipData.ShipData shipData = MasterShipData.getShipData(xwsShipName);
            newMoveList = shipData.getDialManeuvers();
            shipName = shipData.getName();
            this.piece = piece;

        }

        // construct the dial Layers trait (Embellishment class) layer by layer according to the previous Array of Arrays.
        protected void executeCommand() {

            // build the layers for the maneuvers on the dial
            buildManeuvers(piece, newMoveList);

            // build the dial back and dial hide images
            buildDialMask(piece,xwsShipName,faction);

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
            final String wipResistanceMask = "Dial_Back_Rebel_WIP.png";
            final String wipRebelMask = "Dial_Back_Rebel_WIP.png";
            final String wipFirstOrderMask = "Dial_Back_Empire_WIP.pngg";
            final String wipEmpireMask = "Dial_Back_Empire_WIP.png";
            final String wipScumMask = "Dial_Back_Scum_WIP.png";


            // first get the core faction name from the subfaction (i.e. Resistance => RebelAlliance
            String coreFactionName = null;
            if(faction.equalsIgnoreCase("Rebel Alliance") || faction.equalsIgnoreCase("Resistance"))
            {
                coreFactionName = "rebelalliance";
            }else if(faction.equalsIgnoreCase("Galactic Empire") || faction.equalsIgnoreCase("First Order"))
            {
                coreFactionName = "galacticempire";
            }else if(faction.equalsIgnoreCase("Scum and Villainy"))
            {
                coreFactionName = "scumandvillainy";
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


