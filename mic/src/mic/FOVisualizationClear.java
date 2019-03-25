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

public class FOVisualizationClear extends Command {

    String pieceId;

    public FOVisualizationClear(String useThisPieceId) {
        pieceId = useThisPieceId;
    }

    protected synchronized void executeCommand() {
        GamePiece piece = FOVisualization.findPieceFromMicID(this.pieceId);
    }

    protected Command myUndoCommand() {
        return null;
    }

    public static class FOVisualizationClearEncoder implements CommandEncoder {
        private static final String commandPrefix = "FoVisClearId=";
        private static final Logger logger = LoggerFactory.getLogger(FOVisualizationClearEncoder.class);

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