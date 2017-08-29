package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by Mic on 09/08/2017.
 *
 * This source file manages every mouse event so that the ships can be driven by a non-modal mouse interface with buttons
 */
public class MouseShipGUI extends Decorator implements EditablePiece {
    public static final String ID = "MouseShipGUI";
    private MouseEvent mEvent;
    private MouseListener mListener;


    public MouseShipGUI(){
        this(null);
    }
/*
    public void addDragSource(Component var1) {
        var1.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent var1) {
                DragBuffer.this.lastRelease = null;
                DragBuffer.this.dropTarget = null;
                DragBuffer.this.dropHandler = null;
            }

            public void mouseReleased(MouseEvent var1) {
                var1.getComponent().setCursor((Cursor)null);
                Component var2 = (Component)var1.getSource();
                if(DragBuffer.this.dropTarget == null) {
                    var1.translatePoint(var2.getLocationOnScreen().x, var2.getLocationOnScreen().y);
                    DragBuffer.this.lastRelease = var1;
                } else {
                    var1.translatePoint(var2.getLocationOnScreen().x, var2.getLocationOnScreen().y);
                    var1.translatePoint(-DragBuffer.this.dropTarget.getLocationOnScreen().x, -DragBuffer.this.dropTarget.getLocationOnScreen().y);
                    DragBuffer.this.dropHandler.mouseReleased(var1);
                }

            }
        });
    }
*/

    public MouseShipGUI(GamePiece piece){
        setInner(piece);
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {
        return piece.keyEvent(stroke);
    }

    @Override
    public void mySetState(String s) {

    }
    @Override
    public String myGetState() {
        return "";
    }
    @Override
    public String myGetType() {
        return ID;
    }
    @Override
    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }
    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        return null;
    }

    public String getDescription() {
        return "MouseShipGUI";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    public void draw(Graphics graphics, int i, int i1, Component component, double v) {

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
}
