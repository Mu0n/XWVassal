package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.GamePiece;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;

import static mic.Util.deserializeBase64Obj;
import static mic.Util.logToChat;
import static mic.Util.serializeToBase64;

/**
 * Created by Mic on 05/03/2019.
 *
 * Used to be part of AutoRangeFinder as a static class, is now in its own file
 * This is used to create a Command in order to clear visuals that were sent to the map's draw component with FOVisualization
 */

public class FOVisualizationClear extends Command implements Drawable {

    private GamePiece pieceInCommand;
    String pieceId;

    //point of entry for the initiator of the keystroke that will remove the visuals
    public FOVisualizationClear(GamePiece useThisPiece) {
        pieceInCommand = useThisPiece;
        try{
            pieceId = useThisPiece.getProperty("micID").toString();
        }catch(Exception e){

        }
        logToChat("entering FOVClear micID="+pieceId);
    }

    //point of entry for players decoding this command
    public FOVisualizationClear(String useThisPieceId) {

        logToChat("decoding FOVClear micID="+pieceId);
        pieceId = useThisPieceId;
    }

    protected void executeCommand() {
        logToChat("executing clear lines micID=" + pieceId);
            Collection<GamePiece> pieces = GameModule.getGameModule().getGameState().getAllPieces();
            for (GamePiece piece : pieces) {
                try{
                    String testId = piece.getProperty("micID").toString();
                    logToChat("micID="+testId + " equal to this? " + pieceId);
                    if(testId.equals(pieceId)) {
                        pieceInCommand = piece;
                        logToChat("found the right piece!");
                        break;
                    }
                }catch(Exception e) {
                    continue;
                }
            }

       Command c = pieceInCommand.keyEvent(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.SHIFT_DOWN_MASK,false));
        c.execute();
    }

    protected Command myUndoCommand() {
        return null;
    }

    public void draw(Graphics g, Map map) {

    }
    public boolean drawAboveCounters() {
        return false;
    }

    public static class FOVisualizationClearEncoder implements CommandEncoder {
        private static final String commandPrefix = "FoVisClearId=";
        private static final Logger logger = LoggerFactory.getLogger(FOVisualizationClearEncoder.class);
        private static final String partDelim = "!";
        private static final String nullPart = "nullPart";
        private static final String itemDelim = "\t";

        public Command decode(String command) {
            if (command == null || !command.contains(commandPrefix)) {
                return null;
            }
            String id = command.substring(commandPrefix.length());

            logger.info("Decoding autorange visualization clearing micID=" + id);
            return new FOVisualizationClear(id);
        }

        public String encode(Command c) {
            if(!(c instanceof FOVisualizationClear)) {
                return null;
            }
            FOVisualizationClear fovcl = (FOVisualizationClear) c;
            logger.info("Encoding autorange visualization clearing micID=" + fovcl.pieceId);
            try {

                return commandPrefix + fovcl.pieceId;
            } catch (Exception e) {
                logger.error("Error encoding autorange visualization clear", e);
                return null;
            }
        }
    }
}