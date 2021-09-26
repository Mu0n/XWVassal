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
    Bomblet("Bomblet", "Bomb", "11774", 0.0f, 40.0f),
    BuzzDroidSwarm("Buzz Droid Swarm", "Remote", "13069", 0.0f, 53.0f),
    DRK1ProbeDroid("DRK-1 Probe Droid", "Remote", "13068", 0.0f, 62.0f),
    CargoDebris("Cargo", "Debris", "12871", 0.0f, 82.5f),
    SpareParts("Spare Parts", "Debris", "13071", 0.0f, -43.5f),
    ElectroProton("Electro-Proton Bomb","Bomb","13116", 0.0f, 40.0f),
    Concussion("Concussion Bomb", "Bomb", "13161", 0.0f, 40.0f),
    ElectroChaffCloud("ElectroChaffCloud", "Debris","13169",0.0f, 40.0f);

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
    Back1("Back 1", "1", "524", 0.0f, 0.0f, 0.0f, 0.0f),
    Back2("Back 2", "2", "525", 0.0f, 0.0f, 0.0f, 113.0f),
    Back3("Back 3", "3", "526", 0.0f, 0.0f, 0.0f, 226.0f),
    Back4("Back 4", "4", "527", 0.0f, 0.0f, 0.0f, 339.0f),
    Back5("Back 5", "5", "528", 0.0f, 0.0f, 0.0f, 452.0f),
    LT1("Left Turn 1", "6", "521", 90.0f, 90.0f, -98.0f, -16.0f),
    RT1("Right Turn 1", "7", "521", 180.0f, -90.0f, 98.0f, -16.0f),
    LT2("Left Turn 2", "8", "522", 90.0f, 90.0f, -176.25f, 62.75f),
    RT2("Right Turn 2", "9", "522", 180.0f, -90.0f, 176.25f, 62.75f),
    LT3("Left Turn 3", "10", "523", 90.0f, 90.0f, -254.5f, 141.5f),
    RT3("Right Turn 3", "11", "523", 180.0f, -90.0f, 254.5f, 141.5f),
    LB1("Left Bank 1", "12", "517", 45.0f, 45.0f, -65.0f, 48.0f),
    RB1("Right Bank 1", "13", "517", 180.0f, -45.0f, 65.0f, 48.0f),
    LB2("Left Bank 2", "14", "519", 45.0f, 45.0f, -107.0f, 148.0f),
    RB2("Right Bank 2", "15", "519", 180.0f, -45.0f, 107.0f, 148.0f),
    LB3("Left Bank 3", "16", "520", 45.0f, 45.0f, -149.0f, 248.0f),
    RB3("Right Bank 3", "17", "520", 180.0f, -45.0f, 149.0f, 248.0f);

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

    private static Map<KeyStroke, BombManeuver> keyStrokeToManeuver = ImmutableMap.<KeyStroke, BombManeuver>builder()
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.SHIFT_DOWN_MASK, false), BombManeuver.Back1)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.SHIFT_DOWN_MASK, false), BombManeuver.Back2)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.SHIFT_DOWN_MASK, false), BombManeuver.Back3)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.SHIFT_DOWN_MASK, false), BombManeuver.Back4)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.SHIFT_DOWN_MASK, false), BombManeuver.Back5)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK, false), BombManeuver.LT1)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK, false), BombManeuver.RT1)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK, false), BombManeuver.LT2)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK, false), BombManeuver.RT2)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK, false), BombManeuver.LT3)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK, false), BombManeuver.RT3)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK, false), BombManeuver.LB1)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, false), BombManeuver.RB1)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK, false), BombManeuver.LB2)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, false), BombManeuver.RB2)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK, false), BombManeuver.LB3)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK, false), BombManeuver.RB3)
            .build();

    private static Map<KeyStroke, BombToken> keyStrokeToBomb = ImmutableMap.<KeyStroke, BombToken>builder()
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK, false), BombToken.ConnerNet)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK, false), BombToken.ProxMine)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK, false), BombToken.ClusterMineCenter)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK, false), BombToken.IonBombs)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK, false), BombToken.SeismicCharge)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK, false), BombToken.ProtonBomb)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK, false), BombToken.ThermalDetonator)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK, false), BombToken.Bomblet)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK, false), BombToken.BuzzDroidSwarm)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, false), BombToken.DRK1ProbeDroid)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK, false), BombToken.CargoDebris)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK, false), BombToken.SpareParts)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK, false), BombToken.ElectroProton)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, false), BombToken.Concussion)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK, false), BombToken.ElectroChaffCloud)
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
        //old way
        // String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToManeuver.containsKey(keyStroke)) {
            return keyStrokeToManeuver.get(keyStroke);
        }
        return null;
    }

    private BombToken getKeystrokeBomb(KeyStroke keyStroke) {
        //OLD WAY
        // String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToBomb.containsKey(keyStroke)) {
            return keyStrokeToBomb.get(keyStroke);
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

        BombManeuver bombDropTemplate = getKeystrokeBombManeuver(stroke);
        // Is this a keystroke for a maneuver? Deal with the 'no' cases first
        if (bombDropTemplate == null) {
            //check to see if a bomb was summoned
            BombToken droppedBomb = getKeystrokeBomb(stroke);
            if(droppedBomb != null && processingOnlyOneBomb == false){
                processingOnlyOneBomb = true;
                if("Mine".equals(droppedBomb.getBombType())  || "Remote".equals(droppedBomb.getBombType()) || "Debris".equals(droppedBomb.getBombType()) ) //deal with prox mine, conner net, etc which can trigger an overlap event
                {
                    List<BumpableWithShape> otherShipShapes = OverlapCheckManager.getShipsOnMap(null);

                    GamePiece thBS = getInner();
                    String selectedMove = thBS.getProperty("selectedMove").toString();

                    //prepare the drop command and get the shape ready for overlap detection
                    Command result = spawnBomb(droppedBomb, getBombManeuverFromProperty(selectedMove));
                    if("Cluster Mine".equals(droppedBomb.getBombName())) {
                        //do the side ones too, their shapes are all added in the shapesForOverlap array inside the spawnBomb method
                        Command leftBomb = spawnBomb(BombToken.ClusterMineLeft, getBombManeuverFromProperty(selectedMove));
                        Command rightBomb = spawnBomb(BombToken.ClusterMineRight, getBombManeuverFromProperty(selectedMove));
                        result.append(leftBomb);
                        result.append(rightBomb);
                    } //end of dealing with extras for the cluster mine
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

                        KeyStroke deleteyourself = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK, false);
                        Command goToHell = keyEvent(deleteyourself);
                        result.append(goToHell);

                        result.execute();
                        GameModule.getGameModule().sendAndLog(result);
                        return null;
                    }
                    else { //mine was dropped, no collision found
                        KeyStroke deleteyourself = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK, false);
                        Command goToHell = keyEvent(deleteyourself);
                        result.append(goToHell);

                        result.execute();
                        GameModule.getGameModule().sendAndLog(result);
                        return null;
                    }

                }
                //was not a mine, but still a bomb (seismic, ion, thermal, etc), so drop it and delete the bomb spawner right away
                else{
                    GamePiece thBS = getInner();
                    String selectedMove = thBS.getProperty("selectedMove").toString();
                    Command result = spawnBomb(droppedBomb, getBombManeuverFromProperty(selectedMove));

                    KeyStroke deleteyourself = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK, false);
                    Command goToHell = keyEvent(deleteyourself);
                    result.append(goToHell);

                    result.execute();
                    GameModule.getGameModule().sendAndLog(result);
                    return null;
                } // end of dealing with a non-mine drop
            } //end of dealing with the keystroke for any drop
        } // end of dealing with any keystroke, none found interesting
        return piece.keyEvent(stroke); //let it deal with maneuver changes with vassal editor triggers IIRC?

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
}
