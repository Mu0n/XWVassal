package mic;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import javax.swing.*;


import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.Map;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.GamePiece;

import static mic.Util.logToChat;
import static mic.Util.newPiece;


/**
 * HWpopup index class for the VASSAL tutorial
 */
public class HWpopup extends AbstractConfigurable implements CommandEncoder,
        GameComponent {

    private int index = 0;
    private int minChange = 0;
    private int maxChange = 0;
    private Random rand = new Random();
    private JButton experimentalButton; // Used to perform new and aggressive tests
    private JButton helloWorldButton; // Button that pops a java alert, displays a simple message
    private JButton readTextButton; // Button that reads a text file inside the module, could be used for XWS spec
    private JButton printPilotPiecesButton; // Button that prints loaded pilot Pieces
    private JButton printUpgradePiecesButton; // Button that prints loaded pilot Pieces
    private JButton findMissingPiecesButton;
    private JButton loadFromXwsUrlButton;
    private VassalXWSPieceLoader slotLoader = new VassalXWSPieceLoader();

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

    private void helloWorldButtonPressed() {
        //Put simple alert/debug messages to be displayed here in a basic Java popup alert
        try {
            GamePiece pieces[] = Map.activeMap.getPieces();
            JOptionPane.showMessageDialog(null, "Hello World, there are " + Integer.toString(pieces.length) + " pieces on the board right now.", "Feedback",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Hello World, there are no pieces on the board right now.", "Feedback",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    private void readTextButtonPressed() {
                /*
        We will need to read a text file into java. quick test done here
        */
        String everything = "";
        try {
            int i = 0;
            char c;
            InputStream is = GameModule.getGameModule().getDataArchive().getInputStream("file.txt");

            while ((i = is.read()) != -1) {
                // converts integer to character
                c = (char) i;
                // append char to everything ???
                everything += c;
            }

            // releases system resources associated with this stream
            if (is != null)
                is.close();

            JTextArea msg = new JTextArea(everything);

            msg.setLineWrap(true);
            msg.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(msg);
            scrollPane.setPreferredSize(new Dimension(800, 450));
            JOptionPane.showMessageDialog(null, scrollPane, "XWS example", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, System.getProperty(e.toString()), "Feedback",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void experimentalButtonPressed() {
        //
        /* This is the failed piece of code to fetch the collection of PieceSlot (shown as the Pieces window
        in the x-wing module. Dude who helped me in the forums didn't realize he made me use a protected method
        */
        //PieceSlot slot = findSlot("0");
        //GamePiece piece = slot.getExpandedPiece();

        // This is java's way to do a simple "Hello World" popup alert. Modify at will for debug and tests
        /*JOptionPane.showMessageDialog(null,"Hello World",  "Feedback",
                JOptionPane.ERROR_MESSAGE);*/
        /*
        Force an already present piece to move around to a specific location on the map
        Usefulness: to make sure a loaded squad won't stack a bunch of cards/ships in the same place
        GamePiece pieces[] = Map.activeMap.getPieces();
        pieces[0].setPosition(new Point(100, 100));
        */
        String listOfPieceSlots = "";
        PieceSlot ps = new PieceSlot();
        GameModule mod = GameModule.getGameModule();
        Point pos = new Point(250, 200);
        int foundCount = 0;

        int counter = 1;
        for (PieceSlot slot : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)) {
            listOfPieceSlots += "#:" + Integer.toString(counter) + " gpuId:" + slot.getGpId() + " " + slot.getConfigureName() + "\n";
            if (slot.getGpId().equals("15") || slot.getGpId().equals("219") || slot.getGpId().equals("218")) {


                GamePiece gp = slot.getPiece();
                if (!slot.getGpId().equals("15")) pos.setLocation(pos.getX() + gp.boundingBox().getWidth(), pos.getY());

                foundCount++;

                List<Map> mapList = Map.getMapList();
                gp.setMap(mapList.get(0));
                gp.setPosition(pos);


                Command place = mapList.get(0).placeOrMerge(gp, pos);
                place.execute();
                mod.sendAndLog(place);

            }
            counter++;
        }

        Command c = new
                Chatter.DisplayText(mod.getChatter(), listOfPieceSlots);
        c.execute();
        mod.sendAndLog(c);

    }


    //method given to me in the vassal/module design forum, unfortunately it uses a protected method in the class
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

    private void printUpgradePiecesButtonPressed() {
        this.slotLoader.loadListFromXWS(null); // only used to populate maps
        for (String mapKey : this.slotLoader.upgradePiecesMap.keySet()) {
            VassalXWSPilotPieces.Upgrade upgrade = this.slotLoader.upgradePiecesMap.get(mapKey);
            logToChat(String.format("upgrade: key=%s, gpid=%s, name=%s", mapKey, upgrade.getPieceSlot().getGpId(), upgrade.getPieceSlot().getConfigureName()));
        }
    }

    private void spawnPiece(GamePiece piece, Point position) {
      Command placeCommand = Map.getMapById("Map0").placeOrMerge(piece, position);
      placeCommand.execute();
      GameModule.getGameModule().sendAndLog(placeCommand);
    }

    private void loadFromXwsButtonPressed() {

        String url = JOptionPane.showInputDialog("Please paste in the voidstate, YASB, or FABS list URL");
        if (url == null || url.length() == 0) {
            return;
        }

        URL translatedURL = null;
        try {
            translatedURL = XWSUrlHelper.translate(url);
        } catch (MalformedURLException e) {
            logToChat("unable to translate xws url: \n" + e.toString());
            return;
        }

        GameModule mod = GameModule.getGameModule();
        Point startPosition = new Point(500,50);
        int fudgePilotUpgradeFrontier = -50;
        int totalPilotHeight = 0;

        VassalXWSListPieces pieces = slotLoader.loadListFromXWS(XWSFetcher.fetchFromUrl(translatedURL.toString()));
        for (VassalXWSPilotPieces ship : pieces.getShips()) {
            logToChat(String.format("pilot: %s, gpid=%s", ship.getPilotCard().getConfigureName(), ship.getPilotCard().getGpId()));

            GamePiece pilotPiece = ship.clonePilotCard();
            int pilotWidth = (int)pilotPiece.boundingBox().getWidth();
            int pilotHeight = (int)pilotPiece.boundingBox().getHeight();
            totalPilotHeight += pilotHeight;
            spawnPiece(pilotPiece, new Point(
                (int)startPosition.getX(),
                (int)startPosition.getY()+totalPilotHeight));

            GamePiece shipPiece = ship.cloneShip();
            int shipWidth = (int)shipPiece.boundingBox().getWidth();
            int shipHeight = (int)shipPiece.boundingBox().getHeight();
            spawnPiece(shipPiece, new Point(
                (int)startPosition.getX()-pilotWidth,
                (int)startPosition.getY()+totalPilotHeight+20));

            GamePiece dialPiece = ship.cloneDial();
            int dialWidth = (int)dialPiece.boundingBox().getWidth();
            int dialHeight = (int)dialPiece.boundingBox().getHeight();
            spawnPiece(dialPiece, new Point(
                (int)startPosition.getX()-pilotWidth,
                (int)startPosition.getY()+totalPilotHeight-dialHeight));

            int totalUpgradeWidth = 0;
            for (VassalXWSPilotPieces.Upgrade upgrade : ship.getUpgrades()) {
                GamePiece upgradePiece = upgrade.cloneGamePiece();
                spawnPiece(upgradePiece, new Point(
                    (int)startPosition.getX()+pilotWidth+totalUpgradeWidth+fudgePilotUpgradeFrontier,
                    (int)startPosition.getY()+totalPilotHeight));

                totalUpgradeWidth += upgradePiece.boundingBox().getWidth();
            } //loop to next upgrade

            int totalChitWidth = 0;
            for (GamePiece token : ship.getTokensForDisplay()) {
                spawnPiece(token, new Point(
                        (int)startPosition.getX()+pilotWidth+totalUpgradeWidth+totalChitWidth+fudgePilotUpgradeFrontier,
                        (int)startPosition.getY()+totalPilotHeight));
                totalChitWidth += token.boundingBox().getWidth();
            }
        } //loop to next pilot

    }


    /*
    private void loadFromXwsButtonPressed() {

        String url = JOptionPane.showInputDialog("XWS Url");
        if (url == null || url.length() == 0) {
            return;
        }

        VassalXWSListPieces pieces = slotLoader.loadListFromXWS(XWSFetcher.fetchFromUrl(url));

        for (VassalXWSPilotPieces ship : pieces.getShips()) {
            logToChat(String.format("pilot: %s, gpid=%s", ship.getPilotCard().getConfigureName(), ship.getPilotCard().getGpId()));
            for (PieceSlot upgrade : ship.getUpgrades()) {
                logToChat(String.format("\t %s, gpid=%s", upgrade.getConfigureName(), upgrade.getGpId()));
            }
        }
    }
*/

    private void printPilotPiecesButtonPressed() {

        this.slotLoader.loadListFromXWS(null); // only used to populate maps

        logToChat(String.format("Loaded %d pilots", this.slotLoader.pilotPiecesMap.size()));
        for (String mapKey : this.slotLoader.pilotPiecesMap.keySet()) {
            VassalXWSPilotPieces pilot = this.slotLoader.pilotPiecesMap.get(mapKey);
            logToChat("key=" + mapKey);
            logToChat(String.format("\tship: gpid=%s, name=%s", pilot.getShip().getGpId(), pilot.getShip().getConfigureName()));
            logToChat(String.format("\tdial: gpid=%s, name=%s",  pilot.getDial().getGpId(), pilot.getDial().getConfigureName()));
            logToChat(String.format("\tmove: gpid=%s, name=%s",  pilot.getMovementCard().getGpId(), pilot.getMovementCard().getConfigureName()));
            logToChat(String.format("\tcard: gpid=%s, name=%s",  pilot.getPilotCard().getGpId(), pilot.getPilotCard().getConfigureName()));
            logToChat(String.format("\topen: gpid=%s, name=%s",  pilot.getOpenDial().getGpId(), pilot.getOpenDial().getConfigureName()));
            logToChat(String.format("\tstrip: gpid=%s, name=%s",  pilot.getMovementStrip().getGpId(), pilot.getMovementStrip().getConfigureName()));
        }
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

        experimentalButton = new JButton("Test Button");
        experimentalButton.setAlignmentY(0.0F);
        experimentalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                experimentalButtonPressed();
            }
        });
        mod.getToolBar().add(experimentalButton);

        helloWorldButton = new JButton("Alert popup");
        helloWorldButton.setAlignmentY(0.0F);
        helloWorldButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helloWorldButtonPressed();
            }
        });
        mod.getToolBar().add(helloWorldButton);

        readTextButton = new JButton("Read text spec");
        readTextButton.setAlignmentY(0.0F);
        readTextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                readTextButtonPressed();
            }
        });
        mod.getToolBar().add(readTextButton);

        printPilotPiecesButton = new JButton("Print pilot pieces");
        printPilotPiecesButton.setAlignmentY(0.0F);
        printPilotPiecesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                printPilotPiecesButtonPressed();
            }
        });
        mod.getToolBar().add(printPilotPiecesButton);

        printUpgradePiecesButton = new JButton("Print upgrade pieces");
        printUpgradePiecesButton.setAlignmentY(0.0F);
        printUpgradePiecesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                printUpgradePiecesButtonPressed();
            }
        });
        mod.getToolBar().add(printUpgradePiecesButton);


        findMissingPiecesButton = new JButton("Find missing pieces");
        findMissingPiecesButton.setAlignmentY(0.0F);
        findMissingPiecesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                slotLoader.validateAgainstRemote();
            }
        });
        mod.getToolBar().add(findMissingPiecesButton);

        loadFromXwsUrlButton = new JButton("Load from XWS");
        loadFromXwsUrlButton.setAlignmentY(0.0F);
        loadFromXwsUrlButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loadFromXwsButtonPressed();
            }
        });
        mod.getToolBar().add(loadFromXwsUrlButton);

    }

    public void removeFrom(Buildable parent) {
        GameModule mod = (GameModule) parent;

        mod.removeCommandEncoder(this);
        mod.getGameState().removeGameComponent(this);

        mod.getToolBar().remove(experimentalButton);
        mod.getToolBar().remove(helloWorldButton);
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
