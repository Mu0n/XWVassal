package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class StemCondition extends Decorator implements EditablePiece {
    public static final String ID = "stemCondition";

    private static final String wipGenericFrontImage = "Cond-WIP.png";
    private static final String genericBackImage = "Condition_back.png";
    private static final String wipGenericTokenImage = "Cond-WIP_Token.png";
/*
    private static Map<String, String> upgradeBackImages = ImmutableMap.<String, String>builder()
            .put("Tech","Upgrade_Tech_back.jpg")
            .put("SalvagedAstromech","Upgrade_SalvagedAstromech_back.jpg")
            .put("Illicit","Upgrade_Illicit_back.jpg")
            .put("Hardpoint","Upgrade_Hardpoint_back.jpg")
            .put("Team","Upgrade_Team_back.jpg")
            .put("","Upgrade_Cargo_back.jpg")
            .put("","Upgrade_Torpedo_back.jpg")
            .put("","Upgrade_Turret_back.jpg")
            .put("","Upgrade_System_back.jpg")
            .put("","Upgrade_Title_back.jpg")
            .put("","Upgrade_Modification_back.jpg")
            .put("","Upgrade_Astromech_back.jpg")
            .put("","Upgrade_Missile_back.jpg")
            .put("","Upgrade_Elite_back.jpg")
            .put("","Upgrade_Cannon_back.jpg")
            .put("","Upgrade_Crew_back.jpg")
            .put("","Upgrade_Bomb_back.jpg")
            .build();*/
/*
    private static Map<String, String> wipFrontImages = ImmutableMap.<String, String>builder()
            .put("","Stem_Upgrade_Missile.png")
            .put("","Stem_Upgrade_Astromech.png")
            .put("","Stem_Upgrade_Bomb.png")
            .put("","Stem_Upgrade_Cannon.png")
            .put("","Stem_Upgrade_Crew.png")
            .put("","Stem_Upgrade_Elite.png")
            .put("","Stem_Upgrade_Illicit.png")
            .put("","Stem_Upgrade_Modification.png")
            .put("","Stem_Upgrade_Salvaged_Astromech.png")
            .put("","Stem_Upgrade_System.png")
            .put("","Stem_Upgrade_Tech.png")
            .put("","Stem_Upgrade_Title.png")
            .put("","Stem_Upgrade_Torpedo.png")
            .put("","Stem_Upgrade_Turret.png")
            .build();*/

    public StemCondition(){
        this(null);
    }

    public StemCondition(GamePiece piece){
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
        return "Custom StemCondition (mic.StemCondition)";
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
    public static class ConditionGenerateCommand extends Command {

        GamePiece piece;

        static String conditionXWS = "";

        String conditionName = "";


        ConditionGenerateCommand(String conditionXWS, GamePiece piece, String conditionName) {
            this.piece = piece;
            this.conditionXWS = conditionXWS;
            this.conditionName = conditionName;
        }

        // construct the Condition Card piece
        protected void executeCommand() {

            // get the upgrade front image
            String frontImage = "Condition_" + conditionXWS + ".jpg";

            // check to see that the condition card image exists in the module.
            // if it doesn't then use a WIP image
            boolean useWipImage = false;
            if (!XWImageUtils.imageExistsInModule(frontImage))
            {
                frontImage = wipGenericFrontImage;
                useWipImage = true;

            }

            // get the back image
            String backImage = genericBackImage;


            // now build the piece
            piece = buildImageLayer(piece, frontImage, backImage, conditionName);

            // if we used a WIP image, we need to add the upgradeName

            if (useWipImage) {
                piece.setProperty("Upgrade Name", conditionName);

            } else {
                piece.setProperty("Upgrade Name", "");
            }

        }


        private GamePiece buildImageLayer(GamePiece piece, String frontImage, String backImage, String conditionName) {


            String layerName = "Layer - Condition Image";

            StringBuilder sb = new StringBuilder();

            sb.append("emb2;Activate;2;;Flip;2;;;2;;;;1;false;0;0;");
            sb.append(frontImage);//Front image
            sb.append(",");
            sb.append(backImage);//Back image
            sb.append(";Destroyed,");
            sb.append(conditionName);//upgrade name
            sb.append(";true;UpgradeImage;;;false;;1;1;true;65,130;70,130;");//LEAVE

            Embellishment emb = (Embellishment) Util.getEmbellishment(piece, layerName);
            emb.mySetType(sb.toString());

            return piece;

        }

        protected Command myUndoCommand() {
            return null;
        }

        //the following class is used to send the info to the other player whenever a upgrade generation command is issued, so it can be done locally on all machines playing/watching the game

        public static class ConditionGeneratorEncoder implements CommandEncoder {
            private static final Logger logger = LoggerFactory.getLogger(StemPilot.class);
            private static final String commandPrefix = "ConditionGeneratorEncoder=";

            public static StemCondition.ConditionGenerateCommand.ConditionGeneratorEncoder INSTANCE = new StemCondition.ConditionGenerateCommand.ConditionGeneratorEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding ConditionGenerateCommand");

                command = command.substring(commandPrefix.length());
                try {
                    conditionXWS = command.toString();
                } catch (Exception e) {
                    logger.error("Error decoding ConditionGenerateCommand", e);
                    return null;
                }
                return null;
            }

            public String encode(Command c) {
                if (!(c instanceof StemCondition.ConditionGenerateCommand)) {
                    return null;
                }
                logger.info("Encoding UpgradeGenerateCommand");
                StemCondition.ConditionGenerateCommand dialGenCommand = (StemCondition.ConditionGenerateCommand) c;
                try {
                    return commandPrefix + conditionXWS;
                } catch (Exception e) {
                    logger.error("Error encoding ConditionGenerateCommand", e);
                    return null;
                }
            }
        }
    }
}
