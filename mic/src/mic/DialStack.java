package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mic.manuvers.ManeuverPaths;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static mic.Util.logToChat;

/**
 * Created by Mic on 24/06/2017.
 */
public class DialStack  extends Decorator implements EditablePiece {
    public static final String ID = "dial-stack";

    public DialStack() {
        this(null);
    }

    public DialStack(GamePiece piece) {
        setInner(piece);
    }
    @Override
    public void mySetState(String s) {

    }

    @Override
    public String myGetState() {
        return "";
    }

    @Override
    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }

    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        return null;
    }


    public Command keyEvent(KeyStroke stroke) {
        /*if(this.fov == null) {
            this.fov = new FOVisualization();
        }*/

        //Full Range Options CTRL-O

        if (KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK,false).equals(stroke)) {
            //don't bother doing anything if it isn't happening on the map
            if(!"Contested Sector".equals(getMap().getMapName())) return piece.keyEvent(stroke);
            List<BumpableWithShape> BWS = getOtherDials();
            for(BumpableWithShape b: BWS){
                if(shapesOverlap(getBumpableCompareShape(this),b.shape)) logToChat("(((POTENTIAL CHEAT ATTEMPT WARNING))) The dial for "
                        + piece.getProperty("Pilot Name").toString() + "(" + piece.getProperty("Craft ID #").toString()
                + ") is overlapping the dial for " + b.pilotName + "(" + b.shipName + ")");
            }
        }
        return piece.keyEvent(stroke);
    }

    public static Shape getBumpableCompareShape(Decorator bumpable) {
        Shape rawShape = BumpableWithShape.getRawShape(bumpable);
        Shape transformed = AffineTransform
                .getTranslateInstance(bumpable.getPosition().getX(), bumpable.getPosition().getY())
                .createTransformedShape(rawShape);

        FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(bumpable), FreeRotator.class));
        double centerX = bumpable.getPosition().getX();
        double centerY = bumpable.getPosition().getY();
        transformed = AffineTransform
                .getRotateInstance(rotator.getAngleInRadians(), centerX, centerY)
                .createTransformedShape(transformed);

        return transformed;
    }


    /**
     * Returns true if the two provided shapes areas have any intersection
     *
     * @param shape1
     * @param shape2
     * @return
     */
    private boolean shapesOverlap(Shape shape1, Shape shape2) {
        Area a1 = new Area(shape1);
        a1.intersect(new Area(shape2));
        return !a1.isEmpty();
    }

    public void draw(Graphics graphics, int i, int i1, Component component, double v) {
        this.piece.draw(graphics, i, i1, component, v);
    }

    public Rectangle boundingBox() {
        return this.piece.boundingBox();
    }

    public Shape getShape() {
        return this.piece.getShape();
    }

    public String getName() {
        return this.piece.getName();
    }

    @Override
    public String myGetType() {
        return ID;
    }

    public String getDescription() {
        return "Custom dial stack (mic.DialStack)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    private List<BumpableWithShape> getDialsOnMap() {
        List<BumpableWithShape> ships = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("Dials")) {
                ships.add(new BumpableWithShape((Decorator)piece, "Dial",
                        piece.getProperty("Pilot Name").toString(), piece.getProperty("Craft ID #").toString()));
            }
        }
        return ships;
    }

    private List<BumpableWithShape> getOtherDials() {
        List<BumpableWithShape> bumpables = Lists.newLinkedList();
        for (BumpableWithShape bumpable : getDialsOnMap()) {
            if (getId().equals(bumpable.bumpable.getId())) {
                continue;
            }
            bumpables.add(bumpable);
        }
        return bumpables;
    }

    }
