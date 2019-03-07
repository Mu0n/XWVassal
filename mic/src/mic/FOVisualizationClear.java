package mic;

import VASSAL.build.module.Map;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

import static mic.Util.deserializeBase64Obj;
import static mic.Util.serializeToBase64;

/**
 * Created by Mic on 05/03/2019.
 *
 * Used to be part of AutoRangeFinder as a static class, is now in its own file
 * This is used to create a Command in order to clear visuals that were sent to the map's draw component with FOVisualization
 */

public class FOVisualizationClear extends Command implements Drawable {

    private AutoRangeFinder.FOVContent fovContent;
    private FOVisualization originalFovCommand;


    public FOVisualizationClear(FOVisualization fovCommand, AutoRangeFinder.FOVContent fovc) {
        this.originalFovCommand = fovCommand;
        this.fovContent = fovc;
    }

    protected void executeCommand() {
        final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");
        if (fovContent != null) {
            map.removeDrawComponent(originalFovCommand);
        }
    }

    protected Command myUndoCommand() {
        return new FOVisualization(fovContent);
    }

    public void draw(Graphics g, Map map) {

    }
    public String getId() {
        return this.fovContent.getId();
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
            try {
                String[] parts = command.split(partDelim);

                logger.info("Decoded clear visualization with id = {}", parts[0]);
                if (parts.length != 4) {
                    throw new IllegalStateException("Invalid command format " + command);
                }
                AutoRangeFinder.FOVContent visContent = new AutoRangeFinder.FOVContent(parts[0]);

                FOVisualization decodedFovCommand = (FOVisualization) deserializeBase64Obj(parts[1]);

                String[] encodedLines = parts[2].equals(nullPart) ? new String[0] : parts[2].split(itemDelim);
                logger.info("Decoding {} lines", encodedLines.length);
                for (String base64Line : encodedLines) {
                    AutoRangeFinder.MicLine line = (AutoRangeFinder.MicLine) deserializeBase64Obj(base64Line);
                    visContent.addLine(line);
                }

                String[] encodedSwt = parts[3].equals(nullPart) ? new String[0] : parts[3].split(itemDelim);
                logger.info("Decoding {} shapesWithText", encodedLines.length);
                for (String base64Shape : encodedSwt) {
                    AutoRangeFinder.ShapeWithText swt = (AutoRangeFinder.ShapeWithText) deserializeBase64Obj(base64Shape);
                    visContent.addShapeWithText(swt);
                }

                logger.info("Decoded AutorangeVisualization clear with {} shapes", visContent.getShapes().size());
                return new FOVisualizationClear(decodedFovCommand, visContent);
            } catch (Exception e) {
                logger.error("Error decoding AutorangeVisualization clear", e);
                return null;
            }
        }

        public String encode(Command c) {
            if(!(c instanceof FOVisualizationClear)) {
                return null;
            }
            logger.info("Encoding autorange visualization clearing");
            FOVisualizationClear visualization = (FOVisualizationClear) c;
            try {
                java.util.List<String> lines = Lists.newArrayList();
                logger.info("Encoding {} lines to be cleared", visualization.fovContent.getMicLines().size());
                for (AutoRangeFinder.MicLine line : visualization.fovContent.getMicLines()) {
                    lines.add(serializeToBase64(line));
                }
                List<String> shapesWithText = Lists.newArrayList();
                logger.info("Encoding {} shapesWithText to be cleared", visualization.fovContent.getTextShapes().size());
                for(AutoRangeFinder.ShapeWithText swt : visualization.fovContent.getTextShapes()) {
                    shapesWithText.add(serializeToBase64(swt));
                }
                String linesPart = lines.size() > 0 ? Joiner.on(itemDelim).join(lines) : null;
                String swtPart = shapesWithText.size() > 0 ? Joiner.on(itemDelim).join(shapesWithText) : null;

                String fovCommandString = serializeToBase64(visualization.originalFovCommand);
                return commandPrefix + Joiner.on(partDelim).useForNull(nullPart).join(visualization.getId(), fovCommandString, linesPart, swtPart);
            } catch (Exception e) {
                logger.error("Error encoding autorange visualization clear", e);
                return null;
            }
        }
    }
}