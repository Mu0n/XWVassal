package mic;

import VASSAL.build.module.Map;
import VASSAL.build.module.map.Drawable;
import VASSAL.counters.GamePiece;
import com.google.common.collect.Lists;

import java.awt.*;
import java.util.Collection;

/**
 * Created by Mic on 2019-04-05.
 *
 * This class prepares the drawable so that the vassal engine knows when to draw stuff. No encoder is used since the UI is not shared to others
 */


public class MouseRemoteGUIDrawable extends MouseGUIDrawable implements Drawable  {
    GamePiece _shipPiece;
    Map _map;
    int _option;

    Collection<MouseShipGUIDrawable.miElement> listOfInteractiveElements = Lists.newArrayList();
    double scale;

    public MouseRemoteGUIDrawable(GamePiece remotePiece, Map map, int option){
        _shipPiece = remotePiece;
        _map = map;
        _option = option;
    }

    public void draw(Graphics g, Map map) {
        Graphics2D g2d = (Graphics2D) g;

    }

    public boolean drawAboveCounters() {
        return false;
    }
}
