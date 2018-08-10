package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static mic.Util.logToChat;

/**
 * Created by Mic on 9/08/2018.
 *
 * Uses CTRL-Q and is used to report in the chatlog, the xwing-data fetched "text" field.
 */
public class ReportCardText2e extends Decorator implements EditablePiece {
    public static final String ID = "ReportCardText2";

    public ReportCardText2e(){
        this(null);
    }

    public ReportCardText2e(GamePiece piece){
        setInner(piece);
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {
        if (KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK,false).equals(stroke)) {
            logToChat("*-- Pilot Ability for " + this.getProperty("xws2").toString() + ": " + this.getProperty("pilotability"));
            logToChat("*-- Ship Ability for " + this.getProperty("xws2").toString() + ": " + this.getProperty("shipability"));
        }

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
        return "mic.reportCardText2";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
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
}
