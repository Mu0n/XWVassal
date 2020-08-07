package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.Drawable;
import VASSAL.counters.Decorator;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.GamePiece;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.FileArchive;
import com.google.common.collect.Lists;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;

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
    public java.util.List<RepositionChoiceVisual> rpcList = Lists.newArrayList();
    public static float DOT_DIAMETER = 46.0f;

    double scale;

    public MouseRemoteGUIDrawable(GamePiece remotePiece, Map map, int option){
        _remotePiece = remotePiece;
        _map = map;
        _option = option;
        scale = _map.getZoom();

        //Info Gathering (for probe droid): Define the top left coordinate of the popup outline
        ulX = remotePiece.getPosition().x;
        ulY = remotePiece.getPosition().y;

        if(option == MouseShipGUI.probeDroidGUIOption){
            //Info Gathering (for probe droid): Offset 2 get the center global coordinates of the ship calling this op
            double offx = _remotePiece.getPosition().getX();
            double offy = _remotePiece.getPosition().getY();
            // Info Gathering (for probe droid): gather ship angle and rotator
            double shipAngle = ((FreeRotator) Decorator.getDecorator(getOutermost(_remotePiece), FreeRotator.class)).getAngle(); //remote angle
            //(Probe droid) Generate the 5 directional dots around the Probe Droid
            for(int i=1; i<=5; i++){
                float diam = DOT_DIAMETER;
                Shape dot = new Ellipse2D.Float(-diam / 2, -diam / 2, diam, diam);

                double off2x = 0;
                double off2y = -80;

                double off2x_rot_dot = rotX(off2x, off2y, shipAngle + 72*(i-1));
                double off2y_rot_dot = rotY(off2x, off2y, shipAngle + 72*(i-1));

                dot = AffineTransform.
                        getTranslateInstance((int) offx + (int) off2x_rot_dot, (int) offy + (int) off2y_rot_dot).
                        createTransformedShape(dot);

                RepositionChoiceVisual rpc = new RepositionChoiceVisual(null, dot, false,"",i, null, null, 0);
                rpcList.add(rpc);
            }
        }

        else if(option == MouseShipGUI.buzzSwarmGUIOption){

            GamePiece[] pieces = _map.getAllPieces();
            for (GamePiece piece : pieces) {
                try{
                    if(piece.getState().contains("this_is_a_ship")){
                        float diam = DOT_DIAMETER*4/3;
                        Shape dot = new Ellipse2D.Float(-diam / 2, -diam / 2, diam, diam);
                        double offx = piece.getPosition().getX();
                        double offy = piece.getPosition().getY();

                        dot = AffineTransform.
                                getTranslateInstance((int) offx , (int) offy).
                                createTransformedShape(dot);
                        RepositionChoiceVisual rpc = new RepositionChoiceVisual(null, dot, false, "", -77, piece, null, 0);
                        rpcList.add(rpc);
                    }

                }catch(Exception e){
                    continue;
                }
            }
        }

    }

    public void draw(Graphics g, Map map) {
        Graphics2D g2d = (Graphics2D) g;

        scale = _map.getZoom();
        //Vassal 3.x.x fix, uiScale can now change from the default value of 1 thanks to HiDPI monitors.
        final double os_scale = g2d.getDeviceConfiguration().getDefaultTransform().getScaleX();
        scale *= os_scale;
        //end fix

        AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);

        for(RepositionChoiceVisual rpc : rpcList){
            Shape tDot = scaler.createTransformedShape(rpc.theDot);
            g2d.setColor(rpc.dotColor);
            g2d.fill(tDot);
        }
    }

    public boolean drawAboveCounters() {
        return true;
    }

}
