package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Mic on 09/08/2017.
 */
public class EmptyTest extends Decorator implements EditablePiece {
    public static final String ID = "emptytest";

    public EmptyTest(){
        this(null);
    }

    public EmptyTest(GamePiece piece){
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
    public String myGetType() {
        return null;
    }
    @Override
    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }
    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        return null;
    }
    @Override
    public String getDescription() {
        return "emptytest";
    }
    @Override
    public void mySetType(String s) {

    }
    @Override
    public HelpFile getHelpFile() {
        return null;
    }
    @Override
    public void draw(Graphics graphics, int i, int i1, Component component, double v) {

    }
    @Override
    public Rectangle boundingBox() {
        return null;
    }
    @Override
    public Shape getShape() {
        return null;
    }
    @Override
    public String getName() {
        return null;
    }
}
