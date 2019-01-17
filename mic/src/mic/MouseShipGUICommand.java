package mic;

import VASSAL.build.module.Map;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by Mic on 2019-01-17.
 *
 * This class prepares the Commands (draw and remove) so that the vassal engine knows when to draw stuff. No encoder is used since the UI is not shared to others
 */
public class MouseShipGUICommand extends Command implements Drawable {
    int _x;
    int _y;
    int _width;
    int _height;
    Map _map;



    public MouseShipGUICommand(int x, int y, int width, int height, Map map){
        _x=x;
        _y=y;
        _width=width;
        _height=height;
        _map = map;

    }
    protected void executeCommand() {
        _map.addDrawComponent(this);
        _map.repaint();
    }

    protected Command myUndoCommand() {
        return null;
    }

    public void draw(Graphics g, Map map) {
        Graphics2D g2d = (Graphics2D) map.getView().getGraphics();
        Rectangle outline = new Rectangle(_x,_y,_width,_height);
        AffineTransform scaler = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom());

        g2d.setColor(Color.RED);
        g2d.fill(scaler.createTransformedShape(outline));
    }

    public boolean drawAboveCounters() {
        return false;
    }
}
