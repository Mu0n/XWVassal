package mic;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.*;
import java.util.List;
import java.util.Timer;

import javax.swing.*;

import VASSAL.build.GameModule;
import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.Command;
import VASSAL.configure.HotKeyConfigurer;
import static mic.Util.*;

/**
 * Created by Mic on 07/08/2017.
 * This class must deal with Barell Rolls (straight 1, bank 1, straight 2) as well as decloaks (straight 2, bank 2)
 * It has to place templates mid way to the sides and detect an overlap with an obstacle right away
 *
 * Long term: offer a mouse driven slide and click interface with valid position checking
 */

enum RepoManeuver {
    BR1_Left_Mid("Place Mid BR Left", "524", -113.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    BR1_Right_Mid("Place Mid BR Right", "524", 113.0f, 0.0f, 0.0f, 0.0f, 0.0f),

    BR2_Left_Mid("Place Mid BR2/Decloak Left", "525", -226.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    BR2_Right_Mid("Place Mid BR2/Decloak Right", "521", 226.0f, 0.0f, 0.0f, 0.0f, 0.0f),

    BR_Bk1_Left_Fwd_Mid("Place Mid BR Bank 1 Left Fwd", "521", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    BR_Bk1_Left_Bwd_Mid("Place Mid BR Bank 1 Left Bwd", "521", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),

    BR_Bk2_Left_Fwd_Mid("Place Mid Echo Decloak Left Fwd", "523", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    BR_Bk2_Left_Bwd_Mid("Place Mid Echo Decloak Left Bwd", "523", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),

    BR_Bk1_Right_Fwd_Mid("Place Mid BR Bank 1 Right Fwd", "521", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    BR_Bk1_Right_Bwd_Mid("Place Mid BR Bank 1 Right Bwd", "521", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),

    BR_Bk2_Right_Fwd_Mid("Place Mid Echo Decloak Right Fwd", "523", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    BR_Bk2_Right_Bwd_Mid("Place Mid Echo Decloak Right Bwd", "523", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),

    BR1_Left_AFAP("Place Mid BR Left", "524", -113.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    BR1_Left_ABAP("Place Mid BR Left", "524", -113.0f, 0.0f, 0.0f, 0.0f, 0.0f),

    BR2_Left_AFAP("Place Mid BR Left", "524", -113.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    BR2_LefT_ABAP("Place Mid BR Left", "524", -113.0f, 0.0f, 0.0f, 0.0f, 0.0f),

    BR1_Right_AFAP("Place Mid BR Left", "524", -113.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    BR1_Right_ABAP("Place Mid BR Left", "524", -113.0f, 0.0f, 0.0f, 0.0f, 0.0f),

    BR2_Right_AFAP("Place Mid BR Left", "524", -113.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    BR2_Right_ABAP("Place Mid BR Left", "524", -113.0f, 0.0f, 0.0f, 0.0f, 0.0f);

    private final String repoName;
    private final String gpID;
    private final double templateAngle;
    private final double offsetX;
    private final double offsetY;
    private final double offsetX_large;
    private final double offsetY_large;

    RepoManeuver(String repoName,  String gpID, double templateAngle,
                 double offsetX, double offsetY,
                 double offsetX_large, double offsetY_large)
    {
        this.repoName = repoName;
        this.gpID = gpID;
        this.templateAngle = templateAngle;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetX_large = offsetX_large;
        this.offsetY_large = offsetY_large;
    }

    public String getRepoName() { return this.repoName; }
    public String getTemplateGpID() { return this.gpID; }
    public double getTemplateAngle() { return this.templateAngle; }
    public double getOffsetX() { return this.offsetX; }
    public double getOffsetY() { return this.offsetY; }
    public double getOffsetX_large() { return this.offsetX_large; }
    public double getOffsetY_large() { return this.offsetY_large; }
}

public class ShipReposition extends Decorator implements EditablePiece {
    public static final String ID = "ShipReposition";
    private FreeRotator myRotator = null;
    public CollisionVisualization previousCollisionVisualization = null;
    private final FreeRotator testRotator;
    private Shape shapeForOverlap;
    static final int NBFLASHES = 5; //use the same flash functionality if a mine is spawned on a ship
    static final int DELAYBETWEENFLASHES = 150;

    private static Map<String, RepoManeuver> keyStrokeToDropTemplate = ImmutableMap.<String, RepoManeuver>builder()
            .put("CTRL R", RepoManeuver.BR1_Left_Mid)
            .put("ALT R", RepoManeuver.BR1_Right_Mid)
            .build();

    private static Map<String, RepoManeuver> keyStrokeToRepositionShip = ImmutableMap.<String, RepoManeuver>builder()
            .put("CTRL 8", RepoManeuver.BR1_Left_AFAP)
            .put("ALT 8", RepoManeuver.BR1_Right_AFAP)
            .build();

    public ShipReposition() {
        this(null);
    }

    public ShipReposition(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
        previousCollisionVisualization = new CollisionVisualization();
    }

    private Command spawnRepoTemplate(RepoManeuver theManeu) {
        //STEP 1: Collision reposition template, centered as in in the image file, centered on 0,0 (upper left corner)
        GamePiece piece = newPiece(findPieceSlotByID(theManeu.getTemplateGpID()));
        shapeForOverlap = piece.getShape();

        //Info Gathering: gets the angle from ManeuverPaths which deals with degrees, local space with ship at 0,0, pointing up
        double tAngle;
        tAngle = theManeu.getTemplateAngle(); //repo maneuver's angle
        double sAngle = this.getRotator().getAngle(); //ship angle
        //STEP 2: rotate the reposition template with both angles
        FreeRotator fR = (FreeRotator)Decorator.getDecorator(piece, FreeRotator.class);
        fR.setAngle(sAngle - tAngle);

        //Info Gathering: Offset 1, from mid of start spawner nubs to end of spawner nubs, maneuver dependant
        double off1x = isLargeShip(this) ? theManeu.getOffsetX() : theManeu.getOffsetX_large();
        double off1y = isLargeShip(this) ? theManeu.getOffsetY() : theManeu.getOffsetY_large();

        //Info Gathering: Offset 2 Position of the center of the bomb spawner, integers inside a Point
        double off2x = this.getPosition().getX();
        double off2y = this.getPosition().getY();

        //STEP 4: rotate the offset dependant within the spawner's local coordinates
        double off1x_rot = rotX(off1x, off1y, sAngle);
        double off1y_rot = rotY(off1x, off1y, sAngle);

        double off3x_rot = rotX(off2x, off2y, sAngle - tAngle);
        double off3y_rot = rotY(off2x, off2y, sAngle - tAngle);

        //STEP 4: translation into place
        Command placeCommand = getMap().placeOrMerge(piece, new Point((int)off1x + (int)off2x, (int)off1y + (int)off2y));

        shapeForOverlap = AffineTransform.
                getTranslateInstance((int)off1x + (int)off2x, (int)off1y + (int)off2y).
                createTransformedShape(shapeForOverlap);
        double roundedAngle = convertAngleToGameLimits(sAngle - tAngle);
        shapeForOverlap = AffineTransform
                .getRotateInstance(Math.toRadians(-roundedAngle), (int)off1x + (int)off2x, (int)off1y + (int)off2y)
                .createTransformedShape(shapeForOverlap);
        return placeCommand;
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {
        //Any keystroke made on a ship will remove the orange shades

        RepoManeuver repoTemplateDrop = getKeystrokeTemplateDrop(stroke);
        // Template drop requested
        if (repoTemplateDrop != null) {
            spawnRepoTemplate(repoTemplateDrop);
            List<BumpableWithShape> obstacles = getBumpablesOnMap();

            boolean isCollisionOccuring = false;
            if(shapeForOverlap != null){
                List<BumpableWithShape> overlappingObstacles = findCollidingEntities(shapeForOverlap, obstacles);
                if(overlappingObstacles.size() > 0) {
                    for(BumpableWithShape bws : overlappingObstacles)
                    {
                        previousCollisionVisualization.add(bws.shape);
                        logToChat("*** Warning: reposition template currently overlaps an obstacle");
                    }
                    previousCollisionVisualization.add(shapeForOverlap);
                }
            }
        }
        RepoManeuver repoShip = getKeystrokeRepoManeuver(stroke);
        //Ship reposition requested
        if(repoShip != null) {

        }
        // if a collision has been found, start painting the shapes and flash them with a timer, mark the bomb spawner for deletion after this has gone through.
        if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getCount() > 0){

            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                int count = 0;
                @Override
                public void run() {
                    try{
                        previousCollisionVisualization.draw(getMap().getView().getGraphics(),getMap());
                        count++;
                        if(count == NBFLASHES * 2) {
                            getMap().removeDrawComponent(previousCollisionVisualization);
                            timer.cancel();
                        }
                    } catch (Exception e) {
                    }
                }
            }, 0,DELAYBETWEENFLASHES);
        }

        return piece.keyEvent(stroke);
    }
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
    private List<BumpableWithShape> getBumpablesOnMap() {
        List<BumpableWithShape> bumpables = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_an_asteroid")) {
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
            }
        }
        return bumpables;
    }
    private boolean isLargeShip(Decorator ship) {
        return BumpableWithShape.getRawShape(ship).getBounds().getWidth() > 114;
    }

    private PieceSlot findPieceSlotByID(String gpID) {
        for(PieceSlot ps : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)){
            if(gpID.equals(ps.getGpId())) return ps;
        }
        return null;
    }
    double rotX(double x, double y, double angle){
        return Math.cos(-Math.PI*angle/180.0f)*x - Math.sin(-Math.PI*angle/180.0f)*y;
    }
    double rotY(double x, double y, double angle){
        return Math.sin(-Math.PI*angle/180.0f)*x + Math.cos(-Math.PI*angle/180.0f)*y;
    }
    private double convertAngleToGameLimits(double angle) {
        this.testRotator.setAngle(angle);
        return this.testRotator.getAngle();
    }
    private RepoManeuver getKeystrokeTemplateDrop(KeyStroke keyStroke) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToDropTemplate.containsKey(hotKey)) {
            return keyStrokeToDropTemplate.get(hotKey);
        }
        return null;
    }
    private RepoManeuver getKeystrokeRepoManeuver(KeyStroke keyStroke) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
          if (keyStrokeToRepositionShip.containsKey(hotKey)) {
            return keyStrokeToRepositionShip.get(hotKey);
        }
        return null;
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
        return "Custom ship reposition (mic.ShipReposition)";
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

    public void draw(Graphics graphics, int i, int i1, Component component, double v) {
        this.piece.draw(graphics, i, i1, component, v);
    }

    public Rectangle boundingBox() {
        return null;
    }

    public Shape getShape() {
        return null;
    }

    public String getName() {
        return null;
    }


    private static class CollisionVisualization implements Drawable {

        private final List<Shape> shapes;
        private boolean tictoc = false;
        Color myO = new Color(0, 255, 38, 150);

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
