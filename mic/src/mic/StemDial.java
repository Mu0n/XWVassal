package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

import static mic.Util.logToChat;
import static mic.Util.logToChatWithTime;

/**
 * Created by Mic on 12/21/2017.
 *
 * Can be used to set a dial to a specific ship at autospawn-time
 */
public class StemDial extends Decorator implements EditablePiece {
    public static final String ID = "stemdial";
    public String shipXWS = "xwing";

    public StemDial(){
        this(null);
    }

    public StemDial(GamePiece piece){
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

    @Override
    public Command keyEvent(KeyStroke stroke) {
        //check to see if 'x' was pressed
        if(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK, true).equals(stroke)) {
            MasterShipData.ShipData shipData = MasterShipData.getShipData(shipXWS);

            List<List<Integer>> moveList = shipData.getManeuvers();

            for(List<Integer> li : moveList) {
                for(Integer i : li) {
                    logToChatWithTime(i.toString());
                }
            }


        }

        return piece.keyEvent(stroke);
    }
    public String getDescription() {
        return "Custom StemDial (mic.StemDial)";
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
