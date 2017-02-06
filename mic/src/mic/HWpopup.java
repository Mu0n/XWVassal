package mic;

import VASSAL.build.*;
import VASSAL.build.module.*;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.widget.PieceSlot;
import VASSAL.build.widget.PieceSlotHack;
import VASSAL.command.*;
import VASSAL.counters.GamePiece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;



/**
 * HWpopup index class for the VASSAL tutorial
 */
public class HWpopup extends AbstractConfigurable implements CommandEncoder,
        GameComponent {

    private int index = 0;
    private int minChange = 0;
    private int maxChange = 0;
    private Random rand = new Random();
    private JButton addButton; // Adds an increment to the tension counter
    private JButton showButton; // Shows the current total tension in mah brain

    public void addToIndex(int change) {
        index += change;
    }

    public int getIndex() {
        return index;
    }

    public int newIncrement() {
        return (int) (rand.nextFloat() * (maxChange - minChange + 1))
                + minChange;
    }

    private void incrementButtonPressed() {
       //
        //
        //PieceSlot slot = findSlot("0");

        //GamePiece piece = slot.getExpandedPiece();

        /*PieceSlot slot = findSlot("0");
        PieceSlotHack slot2 = (PieceSlotHack) slot;*/
        //GamePiece piece = slot2.getExpandedPiece();

        /*JOptionPane.showMessageDialog(null, slot2.toString(), "Feedback",
                JOptionPane.ERROR_MESSAGE);*/

        GamePiece pieces[] = Map.activeMap.getPieces();
        pieces[0].setPosition(new Point(100,100));

        //Point pos = new Point (100, 100);
        /*Map myMap = Map.activeMap;

        JOptionPane.showMessageDialog(null, myMap.toString(), "myMap",
                JOptionPane.ERROR_MESSAGE);*/
        //Command place = myMap.placeOrMerge(piece, pos);

    }

    private PieceSlot findSlot(String myGpId) {
        PieceSlot mySlot = null;
        for (PieceSlot slot : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)) {
            if (slot.getGpId().equals(myGpId)) {
                mySlot = slot;
                break;
            }
        }
        return mySlot;
    }

    public static final String MIN = "min";
    public static final String MAX = "max";

    public void setAttribute(String key, Object value) {
        if (MIN.equals(key)) {
            if (value instanceof String) {
                minChange = Integer.parseInt((String) value);
            } else if (value instanceof Integer) {
                minChange = ((Integer) value).intValue();
            }
        } else if (MAX.equals(key)) {
            if (value instanceof String) {
                maxChange = Integer.parseInt((String) value);
            } else if (value instanceof Integer) {
                maxChange = ((Integer) value).intValue();
            }
        }
    }

    public String[] getAttributeNames() {
        return new String[]{MIN, MAX};
    }

    public String[] getAttributeDescriptions() {
        return new String[]{"Minimum increment", "Maximum increment"};
    }

    public Class[] getAttributeTypes() {
        return new Class[]{Integer.class, Integer.class};
    }

    public String getAttributeValueString(String key) {
        if (MIN.equals(key)) {
            return "" + minChange;
        } else if (MAX.equals(key)) {
            return "" + maxChange;
        } else {
            return null;
        }
    }

    public void addTo(Buildable parent) {
        GameModule mod = (GameModule) parent;

        mod.addCommandEncoder(this);
        mod.getGameState().addGameComponent(this);

        addButton = new JButton("Hello World");
        addButton.setAlignmentY(0.0F);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                incrementButtonPressed();
            }
        });
        mod.getToolBar().add(addButton);

    }

    public void removeFrom(Buildable parent) {
        GameModule mod = (GameModule) parent;

        mod.removeCommandEncoder(this);
        mod.getGameState().removeGameComponent(this);

        mod.getToolBar().remove(addButton);
    }

    public VASSAL.build.module.documentation.HelpFile getHelpFile() {
        return null;
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public void setup(boolean gameStarting) {
        if (!gameStarting) {
            index = 0;
        }
    }

    public Command getRestoreCommand() {
        return new Incr(this, index);
    }
    public static final String COMMAND_PREFIX = "TENSION:";

    public String encode(Command c) {
        if (c instanceof Incr) {
            return COMMAND_PREFIX + ((Incr) c).getChange();
        } else {
            return null;
        }
    }

    public Command decode(String s) {
        if (s.startsWith(COMMAND_PREFIX)) {
            return new Incr(this,
                    Integer.parseInt(s.substring(COMMAND_PREFIX.length())));
        } else {
            return null;
        }
    }

    public static class Incr extends Command {

        private HWpopup target;
        private int change;

        public Incr(HWpopup target, int change) {
            this.target = target;
            this.change = change;
        }

        protected void executeCommand() {
            target.addToIndex(change);
        }

        protected Command myUndoCommand() {
            return new Incr(target, -change);
        }

        public int getChange() {
            return change;
        }
    }
}
