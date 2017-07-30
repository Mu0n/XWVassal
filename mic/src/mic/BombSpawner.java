package mic;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.*;
import java.util.List;
import java.util.Timer;

import javax.swing.*;

import VASSAL.build.GameModule;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.MoveTracker;
import VASSAL.configure.HotKeyConfigurer;
import mic.manuvers.ManeuverPaths;
import mic.manuvers.PathPart;

import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static mic.Util.*;

/**
 * Created by Mu0n on 7/29/17.
 *
 * Manages the spawning of bombs on the bomb spawner by intercepting the shortcuts, which will be the only item using this custom java class
 * Phase II could include a spiffy GUI menu 
 */
enum BombToken {
    ConnerNet("Conner Net","Mine","666",0.0f, 0.0f),
    ProxMine("Proximity Mine","Mine","666",0.0f, 0.0f),
    ClusterMineCenter("Cluster Mine Center","Mine","666", 0.0f, 0.0f),
    ClusterMineLeft("Cluster Mine Left","Mine","666", 0.0f, 0.0f),
    ClusterMineRight("Cluster Mine Right", "Mine", "666", 0.0f, 0.0f),
    IonBombs("Ion Bobs", "Bomb", "666", 0.0f, 0.0f),
    SeismicCharge("Seismic Charge", "Bomb", "666", 0.0f, 0.0f),
    ProtonBomb("Proton Bomb", "Bomb", "666", 0.0f, 0.0f),
    ThermalDetonator("Thermal Detonator", "Bomb", "666", 0.0f, 0.0f),
    Bomblet("Bomblet", "Bomb", "666", 0.0f, 0.0f);

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
    Back1("Back 1", 0.0f, "521", 0.0f, 0.0f, -113.0f, 0.0f, -226.0f),
    Back2("Back 2", 0.0f, "521", 0.0f, 0.0f, -113.0f, 0.0f, -226.0f),
    Back3("Back 3", 0.0f, "521", 0.0f, 0.0f, -113.0f, 0.0f, -226.0f),
    LT1("Left Turn 1", 0.0f, "521", 0.0f, 0.0f, -113.0f, 0.0f, -226.0f),
    RT1("Right Turn 1", 0.0f, "521", 0.0f, 0.0f, -113.0f, 0.0f, -226.0f),
    LT3("Left Turn 3", 0.0f, "521", 0.0f, 0.0f, -113.0f, 0.0f, -226.0f),
    RT3("Right Turn 3", 0.0f, "521", 0.0f, 0.0f, -113.0f, 0.0f, -226.0f);

    private final String templateName;
    private final double additionalAngleForShip;
    private final String gpID;
    private final double templateAngle;
    private final double offsetX;
    private final double offsetY;
    private final double offsetXLarge;
    private final double offsetYLarge;

    BombManeuver(String templateName,  double additionalAngleForShip, String gpID, double templateAngle,
                double offsetX, double offsetY, double offsetXLarge, double offsetYLarge)
    {
        this.templateName = templateName;
        this.additionalAngleForShip = additionalAngleForShip;
        this.gpID = gpID;
        this.templateAngle = templateAngle;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetXLarge = offsetXLarge;
        this.offsetYLarge = offsetYLarge;
    }

    public String getTemplateName() { return this.templateName; }
    public double getAdditionalAngleForShip() { return this.additionalAngleForShip; }
    public String getTemplateGpID() { return this.gpID; }
    public double getTemplateAngle() { return this.templateAngle; }
    public double getOffsetX() { return this.offsetX; }
    public double getOffsetY() { return this.offsetY; }
    public double getOffsetXLarge() { return this.offsetXLarge; }
    public double getOffsetYLarge() { return this.offsetYLarge; }

}

public class BombSpawner extends Decorator implements EditablePiece {
    public static final String ID = "bombspawner";
    static final int NBFLASHES = 5; //use the same flash functionality if a mine is spawned on a ship
    static final int DELAYBETWEENFLASHES = 150;

    // Set to true to enable visualizations of collision objects.
    // They will be drawn after a collision resolution, select the colliding
    // ship and press x to remove it.
    private static boolean DRAW_COLLISIONS = true;

    private final FreeRotator testRotator;

    private ShipPositionState prevPosition = null;
    private ManeuverPaths lastManeuver = null;
    private FreeRotator myRotator = null;
    public CollisionVisualization previousCollisionVisualization = null;

    private static Map<String, BombManeuver> keyStrokeToManeuver = ImmutableMap.<String, BombManeuver>builder()
            .put("SHIFT 1", BombManeuver.Back1)
            .put("SHIFT 2", BombManeuver.Back2)
            .put("SHIFT 3", BombManeuver.Back3)
            .put("CTRL SHIFT 1", BombManeuver.LT1)
            .put("ALT SHIFT 1", BombManeuver.RT1)
            .put("CTRL SHIFT 3", BombManeuver.LT3)
            .put("ALT SHIFT 3", BombManeuver.RT3)
            .build();

    public BombSpawner() {
        this(null);
    }

    public BombSpawner(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
        previousCollisionVisualization = new CollisionVisualization();
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


    private Command spawnBomb(BombToken theBomb) {
        //STEP 1: Collision aide template, centered as in in the image file, centered on 0,0 (upper left corner)
        GamePiece piece = newPiece(findPieceSlotByID(theManeuv.getAide_gpID()));

        //Info Gathering: Position of the center of the ship, integers inside a Point
        double shipx = this.getPosition().getX();
        double shipy = this.getPosition().getY();
        Point shipPt = new Point((int) shipx, (int) shipy); // these are the center coordinates of the ship, namely, shipPt.x and shipPt.y

         //Info Gathering: offset vector (integers) that's used in local coordinates, right after a rotation found in lastManeuver.getTemplateAngle(), so that it's positioned behind nubs properly
        double x = isLargeShip(this) ? theManeuv.getAide_xLarge() : theManeuv.getAide_x();
        double y = isLargeShip(this) ? theManeuv.getAide_yLarge() : theManeuv.getAide_y();
        int posx =  (int)x;
        int posy =  (int)y;
        Point tOff = new Point(posx, posy); // these are the offsets in local space for the templates, if the ship's center is at 0,0 and pointing up


        //Info Gathering: gets the angle from ManeuverPaths which deals with degrees, local space with ship at 0,0, pointing up
        double tAngle = lastManeuver.getTemplateAngle();
        double sAngle = this.getRotator().getAngle();

        //STEP 2: rotate the collision aide with both the getTemplateAngle and the ship's final angle,
        FreeRotator fR = (FreeRotator)Decorator.getDecorator(piece, FreeRotator.class);
        fR.setAngle(sAngle - tAngle);

        //STEP 3: rotate a double version of tOff to get tOff_rotated
        double xWork = Math.cos(-Math.PI*sAngle/180.0f)*tOff.getX() - Math.sin(-Math.PI*sAngle/180.0f)*tOff.getY();
        double yWork = Math.sin(-Math.PI*sAngle/180.0f)*tOff.getX() + Math.cos(-Math.PI*sAngle/180.0f)*tOff.getY();
        Point tOff_rotated = new Point((int)xWork, (int)yWork);

        //STEP 4: translation into place
        Command placeCommand = getMap().placeOrMerge(piece, new Point(tOff_rotated.x + shipPt.x, tOff_rotated.y + shipPt.y));

        return placeCommand;
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {
        //Any keystroke made on a ship will remove the orange shades

        if(this.previousCollisionVisualization == null) {
            this.previousCollisionVisualization = new CollisionVisualization();
        }

        ManeuverPaths path = getKeystrokePath(stroke);
        // Is this a keystroke for a maneuver? Deal with the 'no' cases first
        if (path == null) {
            //check to see if 'c' was pressed
            if(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false).equals(stroke) && lastManeuver != null) {
                List<BumpableWithShape> otherShipShapes = getShipsWithShapes();



                // Whenever I want to resume template placement with java, this is where it happens
logToChat("with c: "+ lastManeuver.getFullName());
                    if(lastManeuver != null) {
                        Command placeCollisionAide = spawnRotatedPiece(lastManeuver);
                        placeCollisionAide.execute();
                        GameModule.getGameModule().sendAndLog(placeCollisionAide);
                    }


                boolean isCollisionOccuring = findCollidingEntity(BumpableWithShape.getBumpableCompareShape(this), otherShipShapes) != null ? true : false;
                //backtracking requested with a detected bumpable overlap, deal with it
                if (isCollisionOccuring) {
                    Command innerCommand = piece.keyEvent(stroke);
                    Command bumpResolveCommand = resolveBump(otherShipShapes);
                    return bumpResolveCommand == null ? innerCommand : innerCommand.append(bumpResolveCommand);
                }
            }
            // 'c' keystroke has finished here, leave the method altogether
            if(KeyStroke.getKeyStroke(KeyEvent.VK_8, ALT_DOWN_MASK, false).equals(stroke)){

            }

            return piece.keyEvent(stroke);
        }

        // We know we're dealing with a maneuver keystroke
// TO DO include decloaks, barrel rolls
        if (stroke.isOnKeyRelease() == false) {


            if (this.previousCollisionVisualization != null && this.previousCollisionVisualization.getCount() > 0) {
                getMap().removeDrawComponent(this.previousCollisionVisualization);
                this.previousCollisionVisualization.shapes.clear();
            }
            // find the list of other bumpables
            List<BumpableWithShape> otherBumpableShapes = getBumpablesWithShapes();

            //safeguard old position and path

            this.prevPosition = getCurrentState();
            this.lastManeuver = path;

            //Start the Command chain
            Command innerCommand = piece.keyEvent(stroke);

            //These lines fetch the Shape of the last movement template used
            FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(this), FreeRotator.class));
            Shape lastMoveShapeUsed = path.getTransformedTemplateShape(this.getPosition().getX(),
                    this.getPosition().getY(),
                    isLargeShip(this),
                    rotator);

return innerCommand;
        }
        //the maneuver has finished. return control of the event to vassal to do nothing
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
        return "Custom auto-bump resolution (mic.AutoBumpDecorator)";
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

    /**
     * Finds any maneuver paths related to the keystroke based on the map
     * keyStrokeToManeuver map
     *
     * @param keyStroke
     * @return
     */
    private ManeuverPaths getKeystrokePath(KeyStroke keyStroke) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToManeuver.containsKey(hotKey)) {
            return keyStrokeToManeuver.get(hotKey);
        }
        return null;
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
            if (piece.getState().contains("Ship")) {
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

    private static class CollisionVisualization implements Drawable {

        private final List<Shape> shapes;
        private boolean tictoc = false;
        Color myO = new Color(255,99,71, 150);

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

    private static class ShipPositionState {
        double x;
        double y;
        double angle;
    }
}
