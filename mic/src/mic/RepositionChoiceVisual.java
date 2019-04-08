package mic;

import VASSAL.build.module.Map;
import VASSAL.build.module.map.Drawable;
import VASSAL.counters.GamePiece;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by Mic on 2019-04-07.
 *
 * This class maintains the dot visuals that are used in mouse GUIs (ships barrel rolling, remote relocating, etc)
 */


public class RepositionChoiceVisual implements Drawable {
    Shape thePieceShape;
    Shape theDot;
    Boolean mouseOvered = false;
    Color bumpColor = new Color(255,99,71, 60);
    Color validColor = new Color(0,255,130, 60);
    Color dotColor = new Color(255,255,200);
    Color mouseOverColor = new Color(255,255,130);
    Color dotOverlappedColor = new Color(255,0,0);
    boolean isOverlapped = false;
    KeyStroke theKey;
    String inStringForm;
    int _option;
    GamePiece associatedTargetPiece;

    public RepositionChoiceVisual(Shape translatedRotatedScaledShape, Shape centralDot, boolean wantOverlapColor, String choice, int option, GamePiece targetPiece){
        thePieceShape = translatedRotatedScaledShape;
        theDot = centralDot;
        isOverlapped = wantOverlapColor;
        inStringForm = choice;
        _option = option;
        associatedTargetPiece = targetPiece;
    }

    public void draw(Graphics g, VASSAL.build.module.Map map) {
        Graphics2D graphics2D = (Graphics2D) g;

        AffineTransform scaler = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom());

        graphics2D.setColor(validColor);
        if(thePieceShape!=null) graphics2D.fill(scaler.createTransformedShape(thePieceShape));
        if(isOverlapped) graphics2D.setColor(dotOverlappedColor);
        else graphics2D.setColor(dotColor);
        if(theDot!=null) graphics2D.fill(scaler.createTransformedShape(theDot));
        if(mouseOvered){
            graphics2D.setColor(mouseOverColor);
            graphics2D.draw(theDot);
        }
    }

    public AffineTransform getTransformForClick(double scale, double x, double y){
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(x, y);
        return affineTransform;
    }

    public AffineTransform getTransformForDraw(double scale, int x, int y){
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.scale(scale, scale);
        affineTransform.translate(x, y);
        return affineTransform;
    }

    public AffineTransform getTransformForDraw(double scale, double scale2, int x, int y){
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.scale(scale*scale2, scale*scale2);
        affineTransform.translate(x, y);
        return affineTransform;
    }

    public boolean drawAboveCounters() {
        return true;
    }

    public KeyStroke getKeyStroke() {
        return theKey;
    }

    public String getInStringForm() { return inStringForm; }

    public int getOption() { return _option; }

}
