package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class StemUpgrade  extends Decorator implements EditablePiece {
    public static final String ID = "stemUpgrade";

    private static final String wipGenericFrontImage = "Stem_Upgrade_WIP.png";
    private static final String wipGenericBackImage = "Stem_Upgrade_WIP_back.png";
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

    public StemUpgrade(){
        this(null);
    }

    public StemUpgrade(GamePiece piece){
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
        return "Custom StemUpgrade (mic.StemUpgrade)";
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
    public static class UpgradeGenerateCommand extends Command {

        GamePiece piece;

        static String upgradeXWS = "";

        String upgradeName = "";
        String upgradeType = "";

        UpgradeGenerateCommand(String upgradeXWS, GamePiece piece, String upgradeName, String upgradeType) {
            this.piece = piece;
            this.upgradeXWS = upgradeXWS;
            this.upgradeName = upgradeName;
            this.upgradeType = upgradeType.replaceAll(" ", "");

        }

        // construct the Upgrade Card piece
        protected void executeCommand() {

            // get the upgrade front image
            String frontImage = "Upgrade_" + upgradeType + "_" + upgradeXWS + ".jpg";

            // check to see that the upgrade card image exists in the module.
            // if it doesn't then use a WIP image
            boolean useWipImage = false;
            if (!XWImageUtils.imageExistsInModule(frontImage)) {
                frontImage = "Stem_Upgrade_"+upgradeType+".png";
                useWipImage = true;

                // if the slot specific wip image doesn't exist, use the generic upgrade wip image
                if (!XWImageUtils.imageExistsInModule(frontImage)) {
                    frontImage = wipGenericFrontImage;
                }
            }

            // get the slot specific back image
            String backImage = "Upgrade_"+upgradeType+"_back.jpg";

            // if the slot specific back image doesn't exist, use the generic upgrade wip back image
            if (!XWImageUtils.imageExistsInModule(backImage)) {

                backImage = wipGenericBackImage;
            }


            // now build the piece
            piece = buildImageLayer(piece, frontImage, backImage, upgradeName);

            // if we used a WIP image, we need to add the upgradeName

            if (useWipImage) {
                piece.setProperty("Upgrade Name", upgradeName);

            } else {
                piece.setProperty("Upgrade Name", "");
            }

        }


        private GamePiece buildImageLayer(GamePiece piece, String frontImage, String backImage, String upgradeName) {


            String layerName = "Layer - Upgrade Image";

            StringBuilder sb = new StringBuilder();

            sb.append("emb2;Activate;2;;Flip;2;;;2;;;;1;false;0;0;");
            sb.append(frontImage);//Front image
            sb.append(",");
            sb.append(backImage);//Back image
            sb.append(";Destroyed,");
            sb.append(upgradeName);//upgrade name
            sb.append(";true;UpgradeImage;;;false;;1;1;true;65,130;70,130;");//LEAVE

            Embellishment emb = (Embellishment) Util.getEmbellishment(piece, layerName);
            emb.mySetType(sb.toString());

            return piece;

        }

        protected Command myUndoCommand() {
            return null;
        }

        //the following class is used to send the info to the other player whenever a upgrade generation command is issued, so it can be done locally on all machines playing/watching the game

        public static class UpgradeGeneratorEncoder implements CommandEncoder {
            private static final Logger logger = LoggerFactory.getLogger(StemPilot.class);
            private static final String commandPrefix = "UpgradeGeneratorEncoder=";

            public static StemUpgrade.UpgradeGenerateCommand.UpgradeGeneratorEncoder INSTANCE = new StemUpgrade.UpgradeGenerateCommand.UpgradeGeneratorEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding UpgradeGenerateCommand");

                command = command.substring(commandPrefix.length());
                try {
                    upgradeXWS = command.toString();
                } catch (Exception e) {
                    logger.error("Error decoding UpgradeGenerateCommand", e);
                    return null;
                }
                return null;
            }

            public String encode(Command c) {
                if (!(c instanceof StemUpgrade.UpgradeGenerateCommand)) {
                    return null;
                }
                logger.info("Encoding UpgradeGenerateCommand");
                StemUpgrade.UpgradeGenerateCommand dialGenCommand = (StemUpgrade.UpgradeGenerateCommand) c;
                try {
                    return commandPrefix + upgradeXWS;
                } catch (Exception e) {
                    logger.error("Error encoding UpgradeGenerateCommand", e);
                    return null;
                }
            }
        }
    }
}
