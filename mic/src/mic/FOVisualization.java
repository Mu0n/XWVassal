package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

import static mic.Util.*;

/**
 * Created by Mic on 05/03/2019.
 *
 * Used to be part of AutoRangeFinder as a static class, is now in its own file
 * This is used to create a Command in order to generate visuals for the user and all other players
 * A unique ID is generated upon creation and is used when it's time to shut the visual off with the
 * sister Command class, FOVisualizationClear
 */
public class FOVisualization extends Command {

    private final FiringOptionsVisuals fovContent;
    final private String pieceId;

    FOVisualization(FiringOptionsVisuals fovc, String senderPieceId) {
        this.fovContent = fovc;
        this.pieceId = senderPieceId;
    }

    private static GamePiece findPieceFromMicID(String thisId){
        Collection<GamePiece> pieces=  GameModule.getGameModule().getGameState().getAllPieces();
        for(GamePiece p : pieces){
            try{
                String checkedUpId = p.getProperty("micID").toString();
                if(checkedUpId.equals(thisId)) {
                    return p;
                }
            }catch(Exception e){
                continue;
            }
        }
        return null;
    }

    protected void executeCommand() {
        final Timer timer = new Timer();
        final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");
        final String copyOverId = this.pieceId;

        timer.schedule(new TimerTask() {

            int i=0;
            GamePiece p;
                           @Override
                           public void run() {
                               if(i==0){
                               map.addDrawComponent(fovContent);
                               map.repaint();
                               i++;
                               p = FOVisualization.findPieceFromMicID(copyOverId);
                               }
                               else{
                                   String isShowingLines = p.getProperty("isShowingLines").toString();
                                   if(isShowingLines.equals("1")){
                                       map.removeDrawComponent(fovContent);
                                       map.repaint();
                                       Command c = p.keyEvent(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.SHIFT_DOWN_MASK,false));
                                       if(c!=null) c.execute();
                                       timer.cancel();
                                   }
                               }
                           }
                       }, 0, 1000);
/*
        //if not already present, find the piece that should be tied to this command and set it to this; this will be needed for players who need to decode this command
        GamePiece p = pieceInCommand;

        AutoRangeFinder ARF2 = null;
        while(p instanceof Decorator){
            if(((Decorator) p).getOuter().myGetType().equals("auto-range-finder")){
                ARF2 = (AutoRangeFinder)((Decorator) p).getOuter();
                break;
            }
            p = ((Decorator) p).getInner();
        }
        //AutoRangeFinder ARF =(AutoRangeFinder)AutoRangeFinder.getDecorator(pieceInCommand,AutoRangeFinder.class);

        if(ARF2==null) logToChat("couldn't find the autorange decorator");
        else{
            if(ARF2.fovCommand==null) ARF2.populateFovCommand(this.fovContent);
        }
        */
    }
/*
  public static GamePiece getDecorator(GamePiece p, Class<?> type) {
    while (p instanceof Decorator) {
      if (type.isInstance(p)) {
        return p;
      }
      p = ((Decorator) p).piece;
    }
    return null;
  }
 */
    protected Command myUndoCommand() {
        return null;
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

                logger.info("Decoding AutorangeVisualization id=" + parts[0]);
                if (parts.length != 3) {
                    throw new IllegalStateException("Invalid command format " + command);
                }
                FiringOptionsVisuals visContent = new FiringOptionsVisuals();
                String pieceIdToSend = parts[0];

                String[] encodedLines = parts[1].equals(nullPart) ? new String[0] : parts[1].split(itemDelim);
                logger.info("Decoding {} lines", encodedLines.length);
                for (String base64Line : encodedLines) {
                    AutoRangeFinder.MicLine line = (AutoRangeFinder.MicLine) deserializeBase64Obj(base64Line);
                    visContent.addLine(line);
                }

                String[] encodedSwt = parts[2].equals(nullPart) ? new String[0] : parts[2].split(itemDelim);
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
            logger.info("Encoding autorange visualization micID=" + visualization.pieceId);
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

                return commandPrefix + Joiner.on(partDelim).useForNull(nullPart).join(visualization.pieceId, linesPart, swtPart);
            } catch (Exception e) {
                logger.error("Error encoding autorange visualization", e);
                return null;
            }
        }
    }


}

