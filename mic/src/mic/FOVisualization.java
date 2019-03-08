package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.GamePiece;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.text.AttributedString;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static mic.Util.deserializeBase64Obj;
import static mic.Util.logToChat;
import static mic.Util.serializeToBase64;

/**
 * Created by Mic on 05/03/2019.
 *
 * Used to be part of AutoRangeFinder as a static class, is now in its own file
 * This is used to create a Command in order to generate visuals for the user and all other players
 * A unique ID is generated upon creation and is used when it's time to shut the visual off with the
 * sister Command class, FOVisualizationClear
 */
public class FOVisualization extends Command implements Drawable, Serializable {

    private final AutoRangeFinder.FOVContent fovContent;
    private GamePiece pieceInCommand;
    private String pieceId;

    public Color badLineColor = new Color(0, 121,255,110);
    public Color bestLineColor = new Color(246, 255, 41,255);
    public Color shipsObstaclesColor = new Color(255,99,71, 150);
    public Color arcLineColor = new Color(246, 255, 41,180);

    //point of entry for the player initiating the keystroke for autorange
    FOVisualization(AutoRangeFinder.FOVContent fovc, GamePiece senderPiece) {
        this.fovContent = fovc;
        pieceInCommand = senderPiece;
        pieceId = pieceInCommand.getProperty("micID").toString(); //gets the random UUID from the ship that was saved during spawning
    }

    //point of entry for other players who have to decode this command
    FOVisualization(AutoRangeFinder.FOVContent fovc, String senderPieceId) {
        this.fovContent = fovc;
        pieceId = senderPieceId;

        List<GamePiece> pieces = GameModule.getGameModule().getAllDescendantComponentsOf(GamePiece.class);
        for(GamePiece p : pieces){
            try{
                String checkedUpId = p.getProperty("micID").toString();
                if(checkedUpId.equals(senderPieceId)) {
                    pieceInCommand = p;
                    break;
                }
            }catch(Exception e){
                continue;
            }
        }
    }


    public String getId() {
        return this.fovContent.getId();
    }

    public int getCount() {
        return fovContent.getMicLines().size() + fovContent.getTextShapes().size();
    }

    protected void executeCommand() {
        logToChat("executing show lines");
        final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");

        map.addDrawComponent(this);
        map.repaint();

        //if not already present, find the piece that should be tied to this command and set it to this; this will be needed for players who need to decode this command
        AutoRangeFinder ARF =(AutoRangeFinder)AutoRangeFinder.getDecorator(pieceInCommand,AutoRangeFinder.class);
        if(ARF==null) logToChat("couldn't find the autorange decorator");
        else{
            if(ARF.fovCommand==null) ARF.populateFovCommand(this);
        }
    }

    protected Command myUndoCommand() {
        return null;
    }

    public void draw(Graphics graphics, VASSAL.build.module.Map map) {
        Graphics2D graphics2D = (Graphics2D) graphics;

        Object prevAntiAliasing = graphics2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double scale = map.getZoom();
        AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);

        graphics2D.setColor(shipsObstaclesColor);
        for (Shape shape : fovContent.getShapes()) {
            graphics2D.fill(scaler.createTransformedShape(shape));

        }

        for(AutoRangeFinder.ShapeWithText SWT : fovContent.getTextShapes()){
            graphics2D.setColor(badLineColor);
            graphics2D.fill(scaler.createTransformedShape(SWT.shape));

            drawText(SWT.rangeString, scale, SWT.x, SWT.y, graphics2D);
        }

        int colorNb = 100;
        for(AutoRangeFinder.MicLine line : fovContent.getMicLines()){
            if(line.isBestLine == true) graphics2D.setColor(bestLineColor);
            else graphics2D.setColor(badLineColor);

            if(line.isArcLine == true) graphics2D.setColor(arcLineColor);

            /*ALLLines COlor Hijack*/
            if(line.markedAsDead == true) graphics2D.setColor(new Color(255,0,0,255));
            else {
                Color gradiant = new Color(colorNb, colorNb, 255, 255);
                if(colorNb < 250) colorNb += 5;
                graphics2D.setColor(gradiant);
            }
            if(line.isBestLine == true && line.markedAsDead == false) graphics2D.setColor(arcLineColor);
            /*end*/


            Line2D.Double lineShape = new Line2D.Double(line.first, line.second);
            graphics2D.draw(scaler.createTransformedShape(lineShape));

            drawText(line.rangeString, scale, line.centerX, line.centerY, graphics2D);
        }
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAntiAliasing);
        map.repaint(true);
    }

    private static void drawText(String text, double scale, double x, double y, Graphics2D graphics2D) {
        AttributedString attstring = new AttributedString(text);
        attstring.addAttribute(TextAttribute.FONT, new Font("Arial", 0,55));
        attstring.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_LTR);
        FontRenderContext frc = graphics2D.getFontRenderContext();
        TextLayout t = new TextLayout(attstring.getIterator(), frc);
        Shape textShape = t.getOutline(null);

        textShape = AffineTransform.getTranslateInstance(x, y)
                .createTransformedShape(textShape);
        textShape = AffineTransform.getScaleInstance(scale, scale)
                .createTransformedShape(textShape);
        graphics2D.setColor(Color.white);
        graphics2D.fill(textShape);

        if (scale > 0.60) {
            // stroke makes it muddy at low scale
            graphics2D.setColor(Color.black);
            graphics2D.setStroke(new BasicStroke(0.8f));
            graphics2D.draw(textShape);
        }
    }

    public boolean drawAboveCounters() {
        return true;
    }

    public static class AutorangeVisualizationEncoder implements CommandEncoder {
        private static final Logger logger = LoggerFactory.getLogger(AutoRangeFinder.class);
        private static final String commandPrefix = "AutorangeVisualizationEncoder=";
        private static final String nullPart = "nullPart";
        private static final String partDelim = "!";
        private static final String itemDelim = "\t";

        public static AutorangeVisualizationEncoder INSTANCE = new FOVisualization.AutorangeVisualizationEncoder();

        public Command decode(String command) {
            if (command == null || !command.contains(commandPrefix)) {
                return null;
            }


            command = command.substring(commandPrefix.length());

            try {
                String[] parts = command.split(partDelim);

                logger.info("Decoding AutorangeVisualization id=" + parts[0] + " micId=" + parts[1]);
                if (parts.length != 4) {
                    throw new IllegalStateException("Invalid command format " + command);
                }
                AutoRangeFinder.FOVContent visContent = new AutoRangeFinder.FOVContent(parts[0]);
                String pieceIdToSend = parts[1];

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

                logger.info("Decoded AutorangeVisualization with {} shapes", visContent.getShapes().size());
                return new FOVisualization(visContent, pieceIdToSend);
            } catch (Exception e) {
                logger.error("Error decoding AutorangeVisualization", e);
                return null;
            }
        }

        public String encode(Command c) {
            if (!(c instanceof FOVisualization)) {
                return null;
            }
            FOVisualization visualization = (FOVisualization) c;
            logger.info("Encoding autorange visualization id=" + visualization.getId() + " micID=" + visualization.pieceId);
            try {
                java.util.List<String> lines = Lists.newArrayList();
                logger.info("Encoding {} lines", visualization.fovContent.getMicLines().size());
                for (AutoRangeFinder.MicLine line : visualization.fovContent.getMicLines()) {
                    lines.add(serializeToBase64(line));
                }
                List<String> shapesWithText = Lists.newArrayList();
                logger.info("Encoding {} shapesWithText", visualization.fovContent.getTextShapes().size());
                for(AutoRangeFinder.ShapeWithText swt : visualization.fovContent.getTextShapes()) {
                    shapesWithText.add(serializeToBase64(swt));
                }
                String linesPart = lines.size() > 0 ? Joiner.on(itemDelim).join(lines) : null;
                String swtPart = shapesWithText.size() > 0 ? Joiner.on(itemDelim).join(shapesWithText) : null;

                return commandPrefix + Joiner.on(partDelim).useForNull(nullPart).join(visualization.getId(), visualization.pieceId, linesPart, swtPart);
            } catch (Exception e) {
                logger.error("Error encoding autorange visualization", e);
                return null;
            }
        }
    }


}

