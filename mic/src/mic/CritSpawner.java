package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;

import javax.swing.*;
import java.awt.*;

import static mic.Util.newPiece;

/**
 * CritSpawner
 *
 * This class applies as a Trait to the Damage Cards
 * It allows you to spawn a crit token from the damage card.  It responds to "ALT CTRL SHIFT T".
 * There is a trigger action on the damage deck prototype that captures "CTRL-T" and sends "ALT CTRL SHIFT T".
 * Had to do this because if the user hit CTRL-T, it sent the command twice (I think once for key down, once for key up)
 *
 * If the damage card is NOT flipped, it simply spawns a normal Crit token.
 * If the damage card IS flipped, it spawns a Crit token with a text label containing the name of the Crit.
 *
 *  @author MrMurphM
 */
public class CritSpawner extends Decorator implements EditablePiece {

    public static final String ID = "crit-spawn;";

    public CritSpawner() {
        this(null);
    }

    public CritSpawner(GamePiece piece) {
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


    @Override
    public Command keyEvent(KeyStroke stroke) {

        String hotKey = HotKeyConfigurer.getString(stroke);

        // check to see if the this code needs to respond to the event
        if(hotKey.equals("ALT CTRL SHIFT T")){

            // get the text from the damage card
            GamePiece damageCard = getInner();

            // get the location of the damage card
            Point aPoint = damageCard.getPosition();

            // get a new crit token
            GamePiece critToken = newPiece(findPieceSlotByName("Crit"));

            // check to see if the damage card is flipped
            if(damageCard.getProperty("isFlipped") != null &&((String) damageCard.getProperty("isFlipped")).equals("1") ) {

                // If the card is flipped, change the text on the crit token
                critToken.setProperty("critID", damageCard.getLocalizedName());
            }

            // spawn the new token on the board
            spawnPiece(critToken, aPoint, damageCard.getMap());

        }

        return piece.keyEvent(stroke);
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
        return "Custom Crit Token Spawner (mic.CritSpawner)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    private void spawnPiece(GamePiece piece, Point position, Map playerMap) {
        Command placeCommand = playerMap.placeOrMerge(piece, position);
        placeCommand.execute();
        GameModule.getGameModule().sendAndLog(placeCommand);
    }

    private PieceSlot findPieceSlotByName(String name) {
        for(PieceSlot ps : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)){
            if(ps.getConfigureName().equals(name)) {
                return ps;
            }
        }
        return null;
    }

}
