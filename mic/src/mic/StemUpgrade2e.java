package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import mic.ota.XWOTAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class StemUpgrade2e extends Decorator implements EditablePiece {
    public static final String ID = "stemUpgrade";

    private static final String wipGenericFrontImage = "Stem2e_Upgrade_WIP.jpg";
    private static final String wipGenericBackImage = "Stem2e_Upgrade_WIP.jpg";

    public StemUpgrade2e(){
        this(null);
    }

    public StemUpgrade2e(GamePiece piece){
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

        private GamePiece piece;

        private static String upgradeXWS = "";

        private String upgradeName = "";
        private String upgradeType = "";
        private boolean isDualSided = false;
        private String upgradeText = "";
        private String pilotAbilityText = "";

        UpgradeGenerateCommand(GamePiece piece, VassalXWSPilotPieces2e.Upgrade upgrade, boolean isDualSided) {
            this.piece = piece;
            this.upgradeXWS = upgrade.getUpgradeData().getXws();
            this.upgradeName = upgrade.getUpgradeData().getName();
            this.upgradeType = upgrade.getUpgradeData().getSides().get(0).getType();
            this.isDualSided = isDualSided;
            this.upgradeText = upgrade.getUpgradeData().getSides().get(0).getAbility();
            this.pilotAbilityText = upgrade.getUpgradeData().getSides().get(0).getAbility();
        }

        // construct the Upgrade Card piece
        protected void executeCommand() {

            // get the upgrade front image
            // String frontImage = "Upgrade_" + upgradeType + "_" + upgradeXWS + ".jpg";
            String frontImage = "U2e_" + upgradeXWS + ".jpg";
            // check to see that the upgrade card image exists in the module.
            // if it doesn't then use a WIP image
            boolean useWipImage = false;
            if(XWOTAUtils.amIDoingOrder66()){
                frontImage = "U2e_o66.jpg";
                useWipImage = true;
            }
            else if (!XWOTAUtils.imageExistsInModule(frontImage)) {
                frontImage = wipGenericFrontImage;
                useWipImage = true;
            }

            String backImage = null;
            // Check to see if the card is dual sided
            if(isDualSided)
            {
                if(XWOTAUtils.amIDoingOrder66()) backImage = "U2e_o66.jpg";
                else backImage = "U2e_" + upgradeXWS + "_back.jpg";
            }else {
                // get the slot specific back image
                if(XWOTAUtils.amIDoingOrder66()) backImage = "U2e_o66.jpg";
                else backImage = "Stem2e_Upgrade_WIP.jpg";
            }

            // if the slot specific back image doesn't exist, use the generic upgrade wip back image
            if (!XWOTAUtils.imageExistsInModule(backImage)) {
                if(XWOTAUtils.amIDoingOrder66()) backImage = "U2e_o66.jpg";
                else backImage = wipGenericBackImage;
            }


            // now build the piece
            piece = buildImageLayer(piece, frontImage, backImage, upgradeName);

            // if we used a WIP image, we need to add the upgradeName

            if (useWipImage) {
                piece.setProperty("Upgrade Name", upgradeName);

            } else {
                piece.setProperty("Upgrade Name", "");
            }

            piece.setProperty("xwstag", upgradeName);
            if(upgradeText !=null) if(!upgradeText.isEmpty()) {
                piece.setProperty("xwstext", upgradeText);
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

            public static StemUpgrade2e.UpgradeGenerateCommand.UpgradeGeneratorEncoder INSTANCE = new StemUpgrade2e.UpgradeGenerateCommand.UpgradeGeneratorEncoder();

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
                if (!(c instanceof StemUpgrade2e.UpgradeGenerateCommand)) {
                    return null;
                }
                logger.info("Encoding UpgradeGenerateCommand");
                StemUpgrade2e.UpgradeGenerateCommand dialGenCommand = (StemUpgrade2e.UpgradeGenerateCommand) c;
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
