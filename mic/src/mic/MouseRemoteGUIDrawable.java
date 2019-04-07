package mic;

import VASSAL.build.module.Map;
import VASSAL.build.module.map.Drawable;
import VASSAL.counters.Decorator;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.GamePiece;
import com.google.common.collect.Lists;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.*;

import static VASSAL.counters.Decorator.getOutermost;
import static mic.Util.rotX;
import static mic.Util.rotY;

/**
 * Created by Mic on 2019-04-05.
 *
 * This class prepares the drawable so that the vassal engine knows when to draw stuff. No encoder is used since the UI is not shared to others
 */


public class MouseRemoteGUIDrawable extends MouseGUIDrawable implements Drawable  {
    GamePiece _remotePiece;
    Map _map;
    int _option;
    java.util.List<ShipReposition.repositionChoiceVisual> rpcList = Lists.newArrayList();
    public static float DOT_DIAMETER = 46.0f;

    Collection<MouseShipGUIDrawable.miElement> listOfInteractiveElements = Lists.newArrayList();
    double scale;

    public MouseRemoteGUIDrawable(GamePiece remotePiece, Map map, int option){
        _remotePiece = remotePiece;
        _map = map;
        _option = option;
        scale = _map.getZoom();

        //Define the top left coordinate of the popup outline
        ulX = remotePiece.getPosition().x;
        ulY = remotePiece.getPosition().y;

        //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
        double offx = _remotePiece.getPosition().getX();
        double offy = _remotePiece.getPosition().getY();
        // STEP 0: gather ship angle and rotator
        double shipAngle = ((FreeRotator) Decorator.getDecorator(getOutermost(_remotePiece), FreeRotator.class)).getAngle(); //remote angle
        //STEP 7: rotate the offset1 dependant within the spawner's local coordinates

        for(int i=0; i<5; i++){
            float diam = DOT_DIAMETER;
            Shape dot = new Ellipse2D.Float(-diam / 2, -diam / 2, diam, diam);

            double off2x = 0;
            double off2y = -80;

            double off2x_rot_dot = rotX(off2x, off2y, shipAngle + 72*i);
            double off2y_rot_dot = rotY(off2x, off2y, shipAngle + 72*i);

            dot = AffineTransform.
                    getTranslateInstance((int) offx + (int) off2x_rot_dot, (int) offy + (int) off2y_rot_dot).
                    createTransformedShape(dot);

            ShipReposition.repositionChoiceVisual rpc = new ShipReposition.repositionChoiceVisual(null, dot, false,"");
            rpcList.add(rpc);
        }
    }

    public void draw(Graphics g, Map map) {
        Graphics2D g2d = (Graphics2D) g;

        scale = _map.getZoom();

        AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);

        for(ShipReposition.repositionChoiceVisual rpc : rpcList){
            Shape tDot = scaler.createTransformedShape(rpc.theDot);
            g2d.setColor(rpc.dotColor);
            g2d.fill(tDot);
        }
    }

    public boolean drawAboveCounters() {
        return true;
    }
}
