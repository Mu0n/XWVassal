package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.Drawable;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.FileArchive;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.text.AttributedString;

/**
 * Created by Mic on 2019-01-17.
 *
 * This class prepares the drawable so that the vassal engine knows when to draw stuff. No encoder is used since the UI is not shared to others
 */
public class MouseShipGUIDrawable implements Drawable {
    int _x;
    int _y;
    int _width;
    int _height;
    Map _map;
    XWS2Pilots _pilotShip;
    XWS2Pilots.Pilot2e _pilot;

    public MouseShipGUIDrawable(int x, int y, int width, int height, Map map, XWS2Pilots pilotShip, XWS2Pilots.Pilot2e pilot){
        _x=x;
        _y=y;
        _width=width;
        _height=height;
        _map = map;
        _pilotShip = pilotShip;
        _pilot = pilot;
    }

    public void draw(Graphics g, Map map) {
        Graphics2D g2d = (Graphics2D) g;
        Rectangle outline = new Rectangle(_x,_y,_width,_height);

        Object prevAntiAliasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        double scale = map.getZoom();
        AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);




        g2d.setPaint(Color.RED);
        g2d.draw(scaler.createTransformedShape(outline));

        GameModule gameModule = GameModule.getGameModule();
        DataArchive dataArchive = gameModule.getDataArchive();
        FileArchive fileArchive = dataArchive.getArchive();
        Image image;
        try{
            int i = 0;
            for(String move : _pilotShip.getDial()){
                String imageNameToLoad = StemDial2e.dialHeadingImages.get(move.substring(1,3));

                InputStream inputstream = new BufferedInputStream(fileArchive.getInputStream("images/"+imageNameToLoad));
                image = ImageIO.read(inputstream);

                AffineTransform translateNScale = new AffineTransform();
                translateNScale.scale(scale, scale);
                translateNScale.translate(60+ _x + i* 80,_y+50);
                i++;
                if(image!=null) g2d.drawImage(image, translateNScale, new ImageObserver() {
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                        return false;
                    }
                });
            }


        }catch(Exception e){}

        drawText(_pilotShip.getDial().toString(),scale,_x + 30, _y + 50, g2d);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAntiAliasing);
        g2d.dispose();
    }

    public boolean drawAboveCounters() {
        return true;
    }


    private static void drawText(String text, double scale, double x, double y, Graphics2D graphics2D) {
        AttributedString attstring = new AttributedString(text);
        attstring.addAttribute(TextAttribute.FONT, new Font("Arial", 0,32));
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
}
