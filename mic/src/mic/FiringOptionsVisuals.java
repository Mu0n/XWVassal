package mic;

import VASSAL.build.module.Map;
import VASSAL.build.module.map.Drawable;
import com.google.common.collect.Lists;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.text.AttributedString;
import java.util.ArrayList;

/*Created by Mic on 03-08-2019
 Contains on the drawables. Driven by FOVisualization
 */
public class FiringOptionsVisuals implements Drawable, Serializable{
    private java.util.List<Shape> shapes;
    private java.util.List<AutoRangeFinder.ShapeWithText> shapesWithText;
    private java.util.List<AutoRangeFinder.MicLine> lines;

    public Color badLineColor = new Color(0, 121,255,110);
    public Color bestLineColor = new Color(246, 255, 41,255);
    public Color shipsObstaclesColor = new Color(255,99,71, 150);
    public Color arcLineColor = new Color(246, 255, 41,180);

    FiringOptionsVisuals(){
        shapes = Lists.newArrayList();
        shapesWithText = Lists.newArrayList();
        lines = Lists.newArrayList();
    }

    public void clear(){
        this.shapes = new ArrayList<Shape>();
        this.lines = new ArrayList<AutoRangeFinder.MicLine>();
        this.shapesWithText = new ArrayList<AutoRangeFinder.ShapeWithText>();
    }
    public void add(Shape bumpable) {
        this.shapes.add(bumpable);
    }
    public void addLine(AutoRangeFinder.MicLine line){
        this.lines.add(line);
    }
    public void addShapeWithText(AutoRangeFinder.ShapeWithText swt){ this.shapesWithText.add(swt); }
    public int getCount() {
        return lines.size() + shapesWithText.size();
    }

    public java.util.List<Shape> getShapes() {
        return this.shapes;
    }

    public java.util.List<AutoRangeFinder.ShapeWithText> getTextShapes() {
        return this.shapesWithText;
    }

    public java.util.List<AutoRangeFinder.MicLine> getMicLines() {
        return this.lines;
    }
    public void draw(Graphics graphics, Map map) {
        Graphics2D graphics2D = (Graphics2D) graphics;

        Object prevAntiAliasing = graphics2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double scale = map.getZoom();
        AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);

        graphics2D.setColor(shipsObstaclesColor);
        for (Shape shape : this.getShapes()) {
            graphics2D.fill(scaler.createTransformedShape(shape));

        }

        for(AutoRangeFinder.ShapeWithText SWT : this.getTextShapes()){
            graphics2D.setColor(badLineColor);
            graphics2D.fill(scaler.createTransformedShape(SWT.shape));

            drawText(SWT.rangeString, scale, SWT.x, SWT.y, graphics2D);
        }

        int colorNb = 100;
        for(AutoRangeFinder.MicLine line : this.getMicLines()){
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
}
