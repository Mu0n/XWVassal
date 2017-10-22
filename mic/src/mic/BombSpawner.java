package mic;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.swing.*;

import VASSAL.build.GameModule;
import VASSAL.build.module.*;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.ChangeTracker;
import VASSAL.command.MoveTracker;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;
import VASSAL.configure.HotKeyConfigurer;
import static mic.Util.*;

/**
 * Created by Mu0n on 7/29/17.
 *
 * Manages the spawning of bombs on the bomb spawner by intercepting the shortcuts, which will be the only item using this custom java class
 * Phase II could include a spiffy GUI menu 
 */

/*The Offset x and y are calculated when everything is facing up and you're bringing the bomb, jpg centered at the mid point of the first
 edge of the bomb spawner, so that the bomb midnub point is brought to the mid point of the first edge of the bomb spawner
*/
enum BombToken {
    ConnerNet("Conner Net","Mine","6423", 0.0f, 110.0f),
    ProxMine("Proximity Mine","Mine","3666",0.0f, 92.0f),
    ClusterMineCenter("Cluster Mine","Mine","5774", 0.0f, 55.0f),
    ClusterMineLeft("Cluster Mine Left","Mine","5775", -113.0f, 58.5f),
    ClusterMineRight("Cluster Mine Right", "Mine", "5775", 113.0f, 58.5f),
    IonBombs("Ion Bombs", "Bomb", "5260", 0.0f, 40.0f),
    SeismicCharge("Seismic Charge", "Bomb", "3665", 0.0f, 40.0f),
    ProtonBomb("Proton Bomb", "Bomb", "1269", 0.0f, 40.0f),
    ThermalDetonator("Thermal Detonator", "Bomb", "8867", 0.0f, 40.0f),
    Bomblet("Bomblet", "Bomb", "11774", 0.0f, 40.0f);

    private final String bombName;
    private final String bombType;
    private final String gpID;
    private final double offsetX;
    private final double offsetY;

    BombToken(String bombName, String bombType, String gpID, double offsetX, double offsetY) {
        this.bombName = bombName;
        this.bombType = bombType;
        this.gpID = gpID;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    public String getBombName() { return this.bombName; }
    public String getBombType() { return this.bombType; }
    public String getBombGpID() { return this.gpID; }
    public double getOffsetX() { return this.offsetX; }
    public double getOffsetY() { return this.offsetY; }
}

enum BombManeuver {
    Back1("Back 1", "1", "524", 0.0f, 0.0f, 0.0f, 113.0f),
    Back2("Back 2", "2", "525", 0.0f, 0.0f, 0.0f, 226.0f),
    Back3("Back 3", "3", "526", 0.0f, 0.0f, 0.0f, 339.0f),
    LT1("Left Turn 1", "4", "521", 90.0f, 90.0f, -98.0f, 97.0f),
    RT1("Right Turn 1", "5", "521", 180.0f, -90.0f, 98.0f, 97.0f),
    LT3("Left Turn 3", "6", "523", 90.0f, 90.0f, -254.5f, 254.5f),
    RT3("Right Turn 3", "7", "523", 180.0f, -90.0f, 254.5f, 254.5f);

    private final String templateName;
    private final String gfxLayer;
    private final String gpID;
    private final double templateAngle;
    private final double bombAngle;
    private final double offsetX;
    private final double offsetY;

    BombManeuver(String templateName,  String gfxLayer, String gpID, double templateAngle, double bombAngle,
                double offsetX, double offsetY)
    {
        this.templateName = templateName;
        this.gfxLayer = gfxLayer;
        this.gpID = gpID;
        this.templateAngle = templateAngle;
        this.bombAngle = bombAngle;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public String getTemplateName() { return this.templateName; }
    public String getGFXLayer() { return this.gfxLayer; }
    public String getTemplateGpID() { return this.gpID; }
    public double getTemplateAngle() { return this.templateAngle; }
    public double getBombAngle() {return this.bombAngle; }
    public double getOffsetX() { return this.offsetX; }
    public double getOffsetY() { return this.offsetY; }
}

public class BombSpawner extends Decorator implements EditablePiece {
    public static final String ID = "bombspawner";
    static final int NBFLASHES = 5; //use the same flash functionality if a mine is spawned on a ship
    static final int DELAYBETWEENFLASHES = 150;

    // Set to true to enable visualizations of collision objects.
    // They will be drawn after a collision resolution, select the colliding
    // ship and press x to remove it.

    private final FreeRotator testRotator;

    private List<Shape> shapesForOverlap;
    private FreeRotator myRotator = null;
    public MapVisualizations previousCollisionVisualization = null;
    private Boolean processingOnlyOneBomb = false;

    private static Map<String, BombManeuver> keyStrokeToManeuver = ImmutableMap.<String, BombManeuver>builder()
            .put("SHIFT 1", BombManeuver.Back1)
            .put("SHIFT 2", BombManeuver.Back2)
            .put("SHIFT 3", BombManeuver.Back3)
            .put("CTRL SHIFT 1", BombManeuver.LT1)
            .put("ALT SHIFT 1", BombManeuver.RT1)
            .put("CTRL SHIFT 3", BombManeuver.LT3)
            .put("ALT SHIFT 3", BombManeuver.RT3)
            .build();

    private static Map<String, BombToken> keyStrokeToBomb = ImmutableMap.<String, BombToken>builder()
            .put("CTRL O", BombToken.ConnerNet)
            .put("CTRL M", BombToken.ProxMine)
            .put("CTRL L", BombToken.ClusterMineCenter)
            .put("CTRL I", BombToken.IonBombs)
            .put("CTRL S", BombToken.SeismicCharge)
            .put("CTRL P", BombToken.ProtonBomb)
            .put("CTRL H", BombToken.ThermalDetonator)
            .put("CTRL B", BombToken.Bomblet)
            .build();

    public BombSpawner() {
        this(null);
    }

    public BombSpawner(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
        shapesForOverlap = new ArrayList<Shape>();
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

    private PieceSlot findPieceSlotByID(String gpID) {
        for(PieceSlot ps : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)){
            if(gpID.equals(ps.getGpId())) return ps;
        }
        return null;
    }

    private boolean isLargeShip(Decorator ship) {
        return BumpableWithShape.getRawShape(ship).getBounds().getWidth() > 114;
    }

    private Command spawnBomb(BombToken theBomb, BombManeuver theManeu) {
        //STEP 1: Collision aide template, centered as in in the image file, centered on 0,0 (upper left corner)
        GamePiece piece = newPiece(findPieceSlotByID(theBomb.getBombGpID()));
        Shape transfShape = piece.getShape();

        //Info Gathering: gets the angle from ManeuverPaths which deals with degrees, local space with ship at 0,0, pointing up
        double tAngle;
        tAngle = theManeu.getBombAngle(); //t(he maneuver) angle for its dropped bomb
        double sAngle = this.getRotator().getAngle(); //s(pawner) angle
        //STEP 2: rotate the collision aide with both the getTemplateAngle and the ship's final angle,
        FreeRotator fR = (FreeRotator)Decorator.getDecorator(piece, FreeRotator.class);
        fR.setAngle(sAngle - tAngle);

        //STEP 3 -- Info Gathering: Offset 1, from mid bomb to mid start spawner nubs, constant
        double off1x = 0.0f;
        double off1y = -169.5f;

        //Info Gathering: Offset 2, from mid of start spawner nubs to end of spawner nubs, maneuver dependant
        double off2x = theManeu.getOffsetX();
        double off2y = theManeu.getOffsetY();

        //Info Gathering: Offset 3, bomb drop from mid point to center of bomb nubs, bomb dependant
        double off3x = theBomb.getOffsetX();
        double off3y = theBomb.getOffsetY();

        //Info Gathering: Offset 4 Position of the center of the bomb spawner, integers inside a Point
        double off4x = this.getPosition().getX();
        double off4y = this.getPosition().getY();

        //STEP 4: rotate the offsets that are dependant within the spawner's local coordinates
        double off1x_rot = rotX(off1x, off1y, sAngle);
        double off1y_rot = rotY(off1x, off1y, sAngle);

        double off2x_rot = rotX(off2x, off2y, sAngle);
        double off2y_rot = rotY(off2x, off2y, sAngle);


        double off3x_rot = rotX(off3x, off3y, sAngle - tAngle);
        double off3y_rot = rotY(off3x, off3y, sAngle - tAngle);
      //  logToChat(Double.toString(off3x) + " " + Double.toString(off3y) + " becomes " + Double.toString(off3x_rot) +  " " + Double.toString(off3y_rot));
        Point localTranslate = new Point((int)(off1x_rot + off2x_rot + off3x_rot), (int)(off1y_rot + off2y_rot + off3y_rot));

        //STEP 4: translation into place
        Command placeCommand = getMap().placeOrMerge(piece, new Point(localTranslate.x + (int)off4x, localTranslate.y + (int)off4y));

        transfShape = AffineTransform
                .getTranslateInstance(localTranslate.x + (int)off4x, localTranslate.y + (int)off4y)
                .createTransformedShape(transfShape);
        double roundedAngle = convertAngleToGameLimits(sAngle - tAngle);
        transfShape = AffineTransform
                .getRotateInstance(Math.toRadians(-roundedAngle), localTranslate.x + (int)off4x, localTranslate.y + (int)off4y)
                .createTransformedShape(transfShape);
        shapesForOverlap.add(transfShape);
        return placeCommand;
    }

    private double convertAngleToGameLimits(double angle) {
        this.testRotator.setAngle(angle);
        return this.testRotator.getAngle();
    }

    private BombManeuver getKeystrokeBombManeuver(KeyStroke keyStroke) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToManeuver.containsKey(hotKey)) {
            return keyStrokeToManeuver.get(hotKey);
        }
        return null;
    }

    private BombToken getKeystrokeBomb(KeyStroke keyStroke) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToBomb.containsKey(hotKey)) {
            return keyStrokeToBomb.get(hotKey);
        }
        return null;
    }

    private BombManeuver getBombManeuverFromProperty(String reportedProperty) {
        for(BombManeuver bm : BombManeuver.values()) {
            if(bm.getGFXLayer().equals(reportedProperty)) return bm;
        }
        return null;
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {
        //Any keystroke made on a ship will remove the orange shades
        previousCollisionVisualization = new MapVisualizations();

        ChangeTracker changeTracker = new ChangeTracker(this);
        final Command result = changeTracker.getChangeCommand();
        MoveTracker moveTracker = new MoveTracker(Decorator.getOutermost(this));
        result.append(moveTracker.getMoveCommand());

        BombManeuver bombDropTemplate = getKeystrokeBombManeuver(stroke);
        // Is this a keystroke for a maneuver? Deal with the 'no' cases first
        if (bombDropTemplate == null) {
            //check to see if a bomb was summoned
            BombToken droppedBomb = getKeystrokeBomb(stroke);
            if(droppedBomb != null && processingOnlyOneBomb == false){
                processingOnlyOneBomb = true;
                if("Mine".equals(droppedBomb.getBombType())) //deal with prox mine, conner net, etc which can trigger an overlap event
                {
                    List<BumpableWithShape> otherShipShapes = getShipsOnMap();

                    GamePiece thBS = getInner();
                    String selectedMove = thBS.getProperty("selectedMove").toString();

                    //prepare the drop command and get the shape ready for overlap detection
                    Command placeBombCommand = spawnBomb(droppedBomb, getBombManeuverFromProperty(selectedMove));
                    result.append(placeBombCommand);
                    if("Cluster Mine".equals(droppedBomb.getBombName())) {
                        //do the side ones too, their shapes are all added in the shapesForOverlap array inside the spawnBomb method
                        Command leftBomb = spawnBomb(BombToken.ClusterMineLeft, getBombManeuverFromProperty(selectedMove));
                        Command rightBomb = spawnBomb(BombToken.ClusterMineRight, getBombManeuverFromProperty(selectedMove));
                        result.append(leftBomb);
                        result.append(rightBomb);
                    }
                    boolean isCollisionOccuring = false;
                    for(Shape sh : shapesForOverlap ){
                        List<BumpableWithShape> overlappingShips = findCollidingEntities(sh, otherShipShapes);
                        if(overlappingShips.size() > 0) {
                            for(BumpableWithShape bws : overlappingShips)
                            {
                                previousCollisionVisualization.add(bws.shape);

                                result.append(logToChatCommand("*** Overlap detected with dropped " + droppedBomb.getBombName() + " and " + bws.shipName + " ("  + bws.pilotName + ")"));
                                isCollisionOccuring = true;
                            }
                            previousCollisionVisualization.add(sh);
                        }
                    }

                    // if a collision has been found, start painting the shapes and flash them with a timer, mark the bomb spawner for deletion after this has gone through.
                    if(isCollisionOccuring == true && this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                        result.append(previousCollisionVisualization);
                        previousCollisionVisualization.execute();

                        KeyStroke deleteyourself = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK, false);
                        Command goToHell = keyEvent(deleteyourself);
                        result.append(goToHell);

                        return result;
                    }
                    else { //mine was dropped, no collision found
                        KeyStroke deleteyourself = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK, false);
                        Command goToHell = keyEvent(deleteyourself);
                        result.append(goToHell);
                        return result;
                    }

                }
                //was not a mine, but still a bomb (seismic, ion, thermal, etc), so drop it and delete the bomb spawner right away
                else{
                    GamePiece thBS = getInner();
                    String selectedMove = thBS.getProperty("selectedMove").toString();
                    Command placeBombCommand = spawnBomb(droppedBomb, getBombManeuverFromProperty(selectedMove));

                    result.append(placeBombCommand);
                    KeyStroke deleteyourself = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK, false);
                    Command goToHell = keyEvent(deleteyourself);
                    result.append(goToHell);

                    return result;
                } // end of dealing with a non-mine drop
            } //end of dealing with the keystroke for any drop
        } // end of dealing with any keystroke, none found interesting
        return piece.keyEvent(stroke);
    }


    /**
     * Returns the comparision shape of the first bumpable colliding with the provided ship.  Returns null if there
     * are no collisions
     *
     * @param myTestShape
     * @return
     */
    private BumpableWithShape findCollidingEntity(Shape myTestShape, List<BumpableWithShape> otherShapes) {
        List<BumpableWithShape> allCollidingEntities = findCollidingEntities(myTestShape, otherShapes);
        if (allCollidingEntities.size() > 0) {
            return allCollidingEntities.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns a list of all bumpables colliding with the provided ship.  Returns an empty list if there
     * are no collisions
     *
     * @param myTestShape
     * @return
     */
    private List<BumpableWithShape> findCollidingEntities(Shape myTestShape, List<BumpableWithShape> otherShapes) {
        List<BumpableWithShape> shapes = Lists.newLinkedList();
        for (BumpableWithShape otherBumpableShape : otherShapes) {
            if (shapesOverlap(myTestShape, otherBumpableShape.shape)) {
                shapes.add(otherBumpableShape);
            }
        }
        return shapes;
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
        return "Custom bomb spawner (mic.BombSpawner)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    /**
     * Returns FreeRotator decorator associated with this instance
     *
     * @return
     */
    private FreeRotator getRotator() {
        if (this.myRotator == null) {
            this.myRotator = ((FreeRotator) Decorator.getDecorator(getOutermost(this), FreeRotator.class));
        }
        return this.myRotator;
    }

    /**
     * Returns a new ShipPositionState based on the current position and angle of this ship
     *
     * @return
     */
    private ShipPositionState getCurrentState() {
        ShipPositionState shipState = new ShipPositionState();
        shipState.x = getPosition().getX();
        shipState.y = getPosition().getY();
        shipState.angle = getRotator().getAngle();
        return shipState;
    }

    private List<BumpableWithShape> getShipsWithShapes() {
        List<BumpableWithShape> ships = Lists.newLinkedList();
        for (BumpableWithShape ship : getShipsOnMap()) {
            if (getId().equals(ship.bumpable.getId())) {
                continue;
            }
            ships.add(ship);
        }
        return ships;
    }

    private List<BumpableWithShape> getBumpablesWithShapes() {
        List<BumpableWithShape> bumpables = Lists.newLinkedList();
        for (BumpableWithShape bumpable : getBumpablesOnMap()) {
            if (getId().equals(bumpable.bumpable.getId())) {
                continue;
            }
            bumpables.add(bumpable);
        }
        return bumpables;
    }

    private List<BumpableWithShape> getShipsOnMap() {
        List<BumpableWithShape> ships = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_a_ship")) {
                ships.add(new BumpableWithShape((Decorator)piece, "Ship",
                        piece.getProperty("Pilot Name").toString(), piece.getProperty("Craft ID #").toString()));
            }
        }
        return ships;
    }

    private List<BumpableWithShape> getBumpablesOnMap() {
        List<BumpableWithShape> bumpables = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_a_ship")) {
                bumpables.add(new BumpableWithShape((Decorator)piece,"Ship",
                        piece.getProperty("Pilot Name").toString(), piece.getProperty("Craft ID #").toString()));
            } else if (piece.getState().contains("this_is_an_asteroid")) {
                // comment out this line and the next three that add to bumpables if bumps other than with ships shouldn't be detected yet
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece, "Asteroid", "2".equals(testFlipString)));
            } else if (piece.getState().contains("this_is_a_debris")) {
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece,"Debris","2".equals(testFlipString)));
            } else if (piece.getState().contains("this_is_a_bomb")) {
                bumpables.add(new BumpableWithShape((Decorator)piece, "Mine", false));
            }
        }
        return bumpables;
    }

   /* private static class FlashCommand extends Command implements Drawable {



        private final List<Shape> shapes;
        private boolean tictoc = false;
        Color bumpColor = new Color(215, 255, 0, 150);

        FlashCommand() {
            this.shapes = new ArrayList<Shape>();
        }
        FlashCommand(Shape shipShape) {
            this.shapes = new ArrayList<Shape>();
            this.shapes.add(shipShape);
        }

        protected void executeCommand() {
            draw(GameModule.getGameModule(), )
        }

        protected Command myUndoCommand() {
            return null;
        }

        public void add(Shape bumpable) {
            this.shapes.add(bumpable);
        }

        public int getCount() {
            int count = 0;
            Iterator<Shape> it = this.shapes.iterator();
            while(it.hasNext()) {
                count++;
                it.next();
            }
            return count;
        }

        public void draw(Graphics graphics, VASSAL.build.module.Map map) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            if(tictoc == false)
            {
                graphics2D.setColor(bumpColor);
                AffineTransform scaler = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom());
                for (Shape shape : shapes) {
                    graphics2D.fill(scaler.createTransformedShape(shape));
                }
                tictoc = true;
            }
            else {
                map.getView().repaint();
                tictoc = false;
            }
        }

        public boolean drawAboveCounters() {
            return true;
        }
    }*/

   /*
    private static class CollisionVisualization implements Drawable {

        private final List<Shape> shapes;
        private boolean tictoc = false;
        Color myO = new Color(215, 255, 0, 150);

        CollisionVisualization() {
            this.shapes = new ArrayList<Shape>();
        }
        CollisionVisualization(Shape shipShape) {
            this.shapes = new ArrayList<Shape>();
            this.shapes.add(shipShape);
        }

        public void add(Shape bumpable) {
            this.shapes.add(bumpable);
        }

        public int getCount() {
            int count = 0;
            Iterator<Shape> it = this.shapes.iterator();
            while(it.hasNext()) {
                count++;
                it.next();
            }
            return count;
        }

        public void draw(Graphics graphics, VASSAL.build.module.Map map) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            if(tictoc == false)
            {
                graphics2D.setColor(myO);
                AffineTransform scaler = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom());
                for (Shape shape : shapes) {
                    graphics2D.fill(scaler.createTransformedShape(shape));
                }
                tictoc = true;
            }
            else {
                map.getView().repaint();
                tictoc = false;
            }



        }

        public boolean drawAboveCounters() {
            return true;
        }
    }
*/
    private static class ShipPositionState {
        double x;
        double y;
        double angle;
    }
}
