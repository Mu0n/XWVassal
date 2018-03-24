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

public class StemConditionToken extends Decorator implements EditablePiece {
    public static final String ID = "stemConditionToken";

    private static final String wipGenericTokenImage = "Cond-WIP_Token.png";


    public StemConditionToken(){
        this(null);
    }

    public StemConditionToken(GamePiece piece){
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
        return "Custom StemConditionToken (mic.StemConditionToken)";
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
    public static class TokenGenerateCommand extends Command {

        GamePiece piece;
        static String conditionXWS = "";


        TokenGenerateCommand(String conditionXWS, GamePiece piece) {
            this.piece = piece;
            this.conditionXWS = conditionXWS;

        }

        // construct the Condition Card piece
        protected void executeCommand() {

            // get the upgrade front image
            String image = "ConditionToken_" + conditionXWS + ".png";
            // check to see that the condition token image exists in the module.
            // if it doesn't then use a WIP image

            if (!XWOTAUtils.imageExistsInModule(image))
            {
                image = wipGenericTokenImage;
            }

            // now build the piece
            piece = buildImageLayer(piece, image);


        }



        private GamePiece buildImageLayer(GamePiece piece, String image) {


            String layerName = "Layer - Condition Token";


            StringBuilder sb = new StringBuilder();

            sb.append("emb2;Activate;2;;;2;;;2;;;;1;false;0;0;");
            sb.append(image);
            sb.append(";;false;Condition Token;;;false;;1;1;true;65,130;;");

            Embellishment emb = (Embellishment) Util.getEmbellishment(piece, layerName);
            emb.mySetType(sb.toString());

            return piece;

        }

        protected Command myUndoCommand() {
            return null;
        }

        //the following class is used to send the info to the other player whenever a upgrade generation command is issued, so it can be done locally on all machines playing/watching the game

        public static class TokenGeneratorEncoder implements CommandEncoder {
            private static final Logger logger = LoggerFactory.getLogger(StemPilot.class);
            private static final String commandPrefix = "ConditionTokenGeneratorEncoder=";

            public static StemConditionToken.TokenGenerateCommand.TokenGeneratorEncoder INSTANCE = new StemConditionToken.TokenGenerateCommand.TokenGeneratorEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding TokenGenerateCommand");

                command = command.substring(commandPrefix.length());
                try {
                    conditionXWS = command.toString();
                } catch (Exception e) {
                    logger.error("Error decoding TokenGenerateCommand", e);
                    return null;
                }
                return null;
            }

            public String encode(Command c) {
                if (!(c instanceof StemConditionToken.TokenGenerateCommand)) {
                    return null;
                }
                logger.info("Encoding TokenGenerateCommand");
                StemConditionToken.TokenGenerateCommand dialGenCommand = (StemConditionToken.TokenGenerateCommand) c;
                try {
                    return commandPrefix + conditionXWS;
                } catch (Exception e) {
                    logger.error("Error encoding TokenGenerateCommand", e);
                    return null;
                }
            }
        }
    }
}
