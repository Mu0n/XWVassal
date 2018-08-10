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
import java.util.Map;


public class StemPilot2e extends Decorator implements EditablePiece {

    private static Map<String, String> factionBacks = ImmutableMap.<String, String>builder()
            .put("Rebel Alliance","Pilot-Rebel_Back.jpg")
            .put("Resistance","Pilot-Resistance_Back.jpg")
            .put("Galactic Empire","Pilot-Imperial_Back.jpg")
            .put("First Order","Pilot-First_Order_Back.jpg")
            .put("Scum and Villainy","Pilot-S&V_Back.jpg")
            .build();

    private static Map<String, String> wipImages = ImmutableMap.<String, String>builder()
            .put("Rebel Alliance","Pilot2e_WIP_Rebel.jpg")
            .put("Resistance","Pilot2e_WIP_Resistance.jpg")
            .put("Galactic Empire","Pilot2e_WIP_Empire.jpg")
            .put("First Order","Pilot2e_WIP_First_Order.jpg")
            .put("Scum and Villainy","Pilot2e_WIP_Scum.jpg")
            .build();

    public static final String ID = "stemPilot";

    public StemPilot2e(){
        this(null);
    }

    public StemPilot2e(GamePiece piece){
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

//    @Override
//    public Command keyEvent(KeyStroke stroke) {
//        //check to see if 'x' was pressed
//        if(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK, true).equals(stroke)) {
//            logToChatWithTime("temporary trigger for Dial generation -will be eventually ported to autospawn\nPossibly to a right click menu as well with a dynamically fetched list of all ships??");
//            GamePiece piece = getInner();

//            // this is hardcoded - need to fix
//            DialGenerateCommand myDialGen = new DialGenerateCommand("attackshuttle", piece, "Rebel Alliance");
//            Command stringOCommands = piece.keyEvent(stroke);
//            stringOCommands.append(myDialGen);

//            myDialGen.execute();
//            return stringOCommands;
//        }

//        return piece.keyEvent(stroke);
//    }

    public String getDescription() {
        return "Custom StemShip (mic.StemPilot)";
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
    public static class PilotGenerateCommand extends Command {

        GamePiece piece;
        //        static String xwsPilotName = "";
        //       String shipName;
        static String faction = "";
        static String pilotName = "";
        static String pilotXWS2 = "";
        static String pilotXWSencoding = "";
        //static String stemPieceName = "";
        static String shipName = "";
        static String pilotAbilityText="";
        static String shipAbilityName="";
        static String shipAbilityText="";


        PilotGenerateCommand(GamePiece piece, VassalXWSPilotPieces2e ship)
        {
            pilotXWS2 = Canonicalizer.getCleanedName(ship.getShipData().getName());
            faction = ship.getShipData().getFaction();
            pilotName = ship.getPilotData().getName();
            shipName = ship.getShipData().getName();
            pilotAbilityText = ship.getPilotData().getAbility();
            shipAbilityName = ship.getPilotData().getShipAbility().getName();
            shipAbilityText = ship.getPilotData().getShipAbility().getText();

            String factionXWS = Canonicalizer.getCanonicalFactionName(faction);

            pilotXWSencoding = factionXWS+"_"+pilotXWS2+"_"+ship.getPilotData().getXWS2();

            this.piece = piece;

            if (ship.getShipNumber() != null && ship.getShipNumber() > 0) {
                this.piece.setProperty("Pilot ID #", ship.getShipNumber());
            } else {
                this.piece.setProperty("Pilot ID #", "");
            }

            this.piece.setProperty("Ship Type",shipName);
            this.piece.setProperty("Pilot Name",pilotName);
            this.piece.setProperty("xws2",pilotXWS2);
            this.piece.setProperty("pilotability",pilotAbilityText);
            this.piece.setProperty("shipability",shipAbilityName +": " + shipAbilityText);
        }

        // construct the Pilot Card piece
        protected void executeCommand()
        {
            String factionXWS = faction.toLowerCase().replaceAll(" ","");
            String pilotCardImage = "Pilot2e_"+pilotXWSencoding+".jpg";

            // check to see that the pilot card image exists in the module.
            // if it doesn't then use a WIP image
            boolean useWipImage = false;
            if(!XWOTAUtils.imageExistsInModule(pilotCardImage))
            {
                pilotCardImage = wipImages.get(faction);
                useWipImage = true;
            }

            piece = buildImageLayer(piece, pilotCardImage, pilotName, faction);

            // if we used a WIP image, we need to add the ship and pilot Name to the card

            if(useWipImage)
            {
                piece.setProperty("Ship Type",shipName);
                piece.setProperty("Pilot Name",pilotName);
            }

            piece.setProperty("xws2", pilotName);
            if(pilotName !=null) if(!pilotName.isEmpty()) {
                piece.setProperty("xws2", pilotName);
            }
            if(pilotAbilityText !=null) if(!pilotAbilityText.isEmpty()) {
                piece.setProperty("pilotability", pilotAbilityText);
            }
            if(shipAbilityText !=null) if(!shipAbilityText.isEmpty()) {
                piece.setProperty("shipability", shipAbilityName +": " + shipAbilityText);
            }
        }

        private GamePiece buildImageLayer(GamePiece piece, String pilotCardImage, String pilotName, String faction)
        {

            String backImage = factionBacks.get(faction);

            StringBuilder sb = new StringBuilder();

            sb.append("emb2;Activate;2;;Flip;2;;;2;;;;1;false;0;0;");
            sb.append(pilotCardImage);//Front image
            sb.append(",");
            sb.append(backImage);//Back image
            sb.append(";Destroyed,");
            sb.append(pilotName);//pilot name
            sb.append(";true;PilotImage;;;false;;1;1;true;65,130;70,130;");//LEAVE

            Embellishment emb = (Embellishment)Util.getEmbellishment(piece,"Layer - PilotImage");
            emb.mySetType(sb.toString());

            return piece;
        }

        protected Command myUndoCommand() {
            return null;
        }

        //the following class is used to send the info to the other player whenever a ship generation command is issued, so it can be done locally on all machines playing/watching the game
        //only the ship XWS string is sent
        public static class PilotGeneratorEncoder implements CommandEncoder {
            private static final Logger logger = LoggerFactory.getLogger(StemPilot2e.class);
            private static final String commandPrefix = "PilotGeneratorEncoder=";

            public static StemPilot2e.PilotGenerateCommand.PilotGeneratorEncoder INSTANCE = new StemPilot2e.PilotGenerateCommand.PilotGeneratorEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding PilotGenerateCommand");

                command = command.substring(commandPrefix.length());
                try {
                    pilotXWSencoding = command.toString();
                } catch (Exception e) {
                    logger.error("Error decoding PilotGenerateCommand", e);
                    return null;
                }
                return null;
            }

            public String encode(Command c) {
                if (!(c instanceof PilotGenerateCommand)) {
                    return null;
                }
                logger.info("Encoding PilotGenerateCommand");
                PilotGenerateCommand dialGenCommand = (PilotGenerateCommand) c;
                try {
                    return commandPrefix + pilotXWSencoding;
                } catch(Exception e) {
                    logger.error("Error encoding PilotGenerateCommand", e);
                    return null;
                }
            }
        }

    }
}

