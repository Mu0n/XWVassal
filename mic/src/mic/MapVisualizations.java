package mic;

import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mic on 12/10/2017.
 *
 * This makes an array of shapes flash a few times. Used by all sorts of overlap management classes (AutoBumpDecorator, ShipReposition, etc.
 */
public class MapVisualizations extends Command implements Drawable {
    private static final Logger logger = LoggerFactory.getLogger(AutoBumpDecorator.class);
    static final int NBFLASHES = 6;
    static final int DELAYBETWEENFLASHES = 250;

    private final List<Shape> shapes;
    private boolean tictoc = false;
    Color bumpColor = new Color(255,99,71, 150);

    MapVisualizations() {
        this.shapes = new ArrayList<Shape>();
    }

    MapVisualizations(Shape shipShape) {
        this.shapes = new ArrayList<Shape>();
        this.shapes.add(shipShape);
    }

    protected void executeCommand() {
        final Timer timer = new Timer();
        final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");
        logger.info("Rendering CollisionVisualization command");
        this.tictoc = false;
        final AtomicInteger count = new AtomicInteger(0);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    if(count.getAndIncrement() >= NBFLASHES * 2) {
                        timer.cancel();
                        map.removeDrawComponent(MapVisualizations.this);
                        return;
                    }
                    draw(map.getView().getGraphics(), map);
                } catch (Exception e) {
                    logger.error("Error rendering collision visualization", e);
                }
            }
        }, 0,DELAYBETWEENFLASHES);
    }

    protected Command myUndoCommand() {
        return null;
    }

    public void add(Shape bumpable) {
        this.shapes.add(bumpable);
    }

    public List<Shape> getShapes() {
        return this.shapes;
    }

    public void draw(Graphics graphics, VASSAL.build.module.Map map) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        if(tictoc == false)
        {
            graphics2D.setColor(bumpColor);
            AffineTransform scaler = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom());
            for (Shape shape : shapes) {
                graphics2D.fill(scaler.createTransformedShape(shape));
            }
            tictoc = true;
        }
        else {
            map.getView().repaint();
            tictoc = false;
        }
    }

    public boolean drawAboveCounters() {
        return true;
    }





    public static class CollsionVisualizationEncoder implements CommandEncoder {
        private static final Logger logger = LoggerFactory.getLogger(AutoBumpDecorator.class);
        private static String commandPrefix = "CollisionVis=";

        public Command decode(String command) {
            if (command == null || !command.contains(commandPrefix)) {
                return null;
            }

            logger.info("Decoding CollisionVisualization");

            command = command.substring(commandPrefix.length());

            try {
                String[] newCommandStrs = command.split("\t");
                MapVisualizations visualization = new MapVisualizations();
                for (String bytesBase64Str : newCommandStrs) {
                    ByteArrayInputStream strIn = new ByteArrayInputStream(Base64.decodeBase64(bytesBase64Str));
                    ObjectInputStream in = new ObjectInputStream(strIn);
                    Shape shape = (Shape) in.readObject();
                    visualization.add(shape);
                    in.close();
                }
                logger.info("Decoded CollisionVisualization with {} shapes", visualization.getShapes().size());
                return visualization;
            } catch (Exception e) {
                logger.error("Error decoding CollisionVisualization", e);
                return null;
            }
        }

        public String encode(Command c) {
            if (!(c instanceof MapVisualizations)) {
                return null;
            }
            logger.info("Encoding CollisionVisualization");
            MapVisualizations visualization = (MapVisualizations) c;
            try {
                List<String> commandStrs = Lists.newArrayList();
                for (Shape shape : visualization.getShapes()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(bos);
                    out.writeObject(shape);
                    out.close();
                    byte[] bytes = bos.toByteArray();
                    String bytesBase64 = Base64.encodeBase64String(bytes);
                    commandStrs.add(bytesBase64);
                }
                return commandPrefix + Joiner.on('\t').join(commandStrs);
            } catch (Exception e) {
                logger.error("Error encoding CollisionVisualization", e);
                return null;
            }
        }
    }


}

