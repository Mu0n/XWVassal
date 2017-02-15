package mic;

import java.awt.*;

import javax.swing.*;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceEditor;
import VASSAL.counters.SimplePieceEditor;

/**
 * Created by amatheny on 2/14/17.
 */
public class AutoBumpDecorator extends Decorator implements EditablePiece {
    public static final String ID = "auto-bump";
    private final KeyCommand keyCommand;

    BaseSize baseSize = BaseSize.small;

    private enum BaseSize {
        large,
        small;
    }

    public AutoBumpDecorator() {
        this(null, null);
    }

    public AutoBumpDecorator(String type, GamePiece piece) {
        this.piece = piece;
        this.keyCommand = new KeyCommand("wut", KeyStroke.getKeyStroke('C'), Decorator.getOutermost(this));
    }

    @Override
    public void mySetState(String s) {
        if (s == null) {
            return;
        }
        String sizeStr = s.replace("baseSize=", "");
        try {
            this.baseSize = BaseSize.valueOf(sizeStr);
        } catch (Exception e) {
            throw new RuntimeException("Invalid base size, should be small or large");
        }
    }

    @Override
    public String myGetState() {
        return String.format("baseSize=%s", this.baseSize);
    }

    @Override
    public String myGetType() {
        return ID;
    }

    @Override
    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[]{keyCommand};
    }

    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        Util.logToChat("Key event! " + keyStroke.getKeyChar());
        if (keyStroke.equals(KeyStroke.getKeyStroke('C'))) {
            Util.logToChat("C was pressed, last move was " + Decorator.getOutermost(this).getProperty("LastMove"));
        }
        return new NullCommand();
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

    public String getDescription() {
        return "Auto-resolve bump on press of C";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    public PieceEditor getEditor() {
        return new SimplePieceEditor(this);
    }
}
