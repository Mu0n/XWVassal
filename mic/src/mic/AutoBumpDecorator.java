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
 * Created by amatheny on 2/14/17.
 *
 * Second role: to completely intercept every maneuver shortcut and deal with movement AND autobump AND out of bound detection
 */
public class AutoBumpDecorator extends Decorator implements EditablePiece {
    public static final String ID = "auto-bump;";
    static final int NBFLASHES = 5;
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

    private static Map<String, ManeuverPaths> keyStrokeToManeuver = ImmutableMap.<String, ManeuverPaths>builder()
            .put("SHIFT 1", ManeuverPaths.Str1)
            .put("SHIFT 2", ManeuverPaths.Str2)
            .put("SHIFT 3", ManeuverPaths.Str3)
            .put("SHIFT 4", ManeuverPaths.Str4)
            .put("SHIFT 5", ManeuverPaths.Str5)
            .put("CTRL SHIFT 1", ManeuverPaths.LT1)
            .put("CTRL SHIFT 2", ManeuverPaths.LT2)
            .put("CTRL SHIFT 3", ManeuverPaths.LT3)
            .put("ALT SHIFT 1", ManeuverPaths.RT1)
            .put("ALT SHIFT 2", ManeuverPaths.RT2)
            .put("ALT SHIFT 3", ManeuverPaths.RT3)
            .put("CTRL 1", ManeuverPaths.LBk1)
            .put("CTRL 2", ManeuverPaths.LBk2)
            .put("CTRL 3", ManeuverPaths.LBk3)
            .put("ALT 1", ManeuverPaths.RBk1)
            .put("ALT 2", ManeuverPaths.RBk2)
            .put("ALT 3", ManeuverPaths.RBk3)
            .put("ALT CTRL 1", ManeuverPaths.K1)
            .put("ALT CTRL 2", ManeuverPaths.K2)
            .put("ALT CTRL 3", ManeuverPaths.K3)
            .put("ALT CTRL 4", ManeuverPaths.K4)
            .put("ALT CTRL 5", ManeuverPaths.K5)
            .put("CTRL 6", ManeuverPaths.RevLbk1)
            .put("SHIFT 6", ManeuverPaths.RevStr1)
            .put("ALT 6", ManeuverPaths.RevRbk1)
            .put("CTRL Q", ManeuverPaths.SloopL1)
            .put("CTRL W", ManeuverPaths.SloopL2)
            .put("CTRL E", ManeuverPaths.SloopL3)
            .put("ALT Q", ManeuverPaths.SloopR1)
            .put("ALT W", ManeuverPaths.SloopR2)
            .put("ALT E", ManeuverPaths.SloopR3)
            .put("CTRL SHIFT E", ManeuverPaths.SloopL3Turn)
            .put("ALT SHIFT E", ManeuverPaths.SloopR3Turn)
            .put("CTRL Y", ManeuverPaths.TrollL2)
            .put("CTRL T", ManeuverPaths.TrollL3)
            .put("ALT Y", ManeuverPaths.TrollR2)
            .put("ALT T", ManeuverPaths.TrollR3)
            .build();

    public AutoBumpDecorator() {
        this(null);
    }

    public AutoBumpDecorator(GamePiece piece) {
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


    private Command spawnRotatedPiece(ManeuverPaths theManeuv) {
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
            return piece.keyEvent(stroke);
        }

        // We know we're dealing with a maneuver keystroke
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



            //This PathPart list will be used everywhere: moving, bumping, out of boundsing
            //maybe fetch it for both 'c' behavior and movement
            final List<PathPart> parts = path.getTransformedPathParts(
                    this.getCurrentState().x,
                    this.getCurrentState().y,
                    this.getCurrentState().angle,
                    isLargeShip(this)
            );

            //this is the final ship position post-move
            PathPart part = parts.get(parts.size()-1);

            //Get the ship name string for announcements
            String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
            //Start the Command chain
            Command innerCommand = piece.keyEvent(stroke);

            innerCommand.append(buildTranslateCommand(part, path.getAdditionalAngleForShip()));

            //check for Tallon rolls and spawn the template
            if(lastManeuver == ManeuverPaths.TrollL2 || lastManeuver == ManeuverPaths.TrollL3 || lastManeuver == ManeuverPaths.TrollR2 || lastManeuver == ManeuverPaths.TrollR3) {
                Command placeTrollTemplate = spawnRotatedPiece(lastManeuver);
                innerCommand.append(placeTrollTemplate);

            }


            //These lines fetch the Shape of the last movement template used
            FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(this), FreeRotator.class));
            Shape lastMoveShapeUsed = path.getTransformedTemplateShape(this.getPosition().getX(),
                    this.getPosition().getY(),
                    isLargeShip(this),
                    rotator);

            //don't check for collisions in windows other than the main map
            if(!"Contested Sector".equals(getMap().getMapName())) return innerCommand;

            innerCommand.append(logToChatWithTimeCommand("* --- " + yourShipName + " performs move: " + path.getFullName()));

            //Check for template shape overlap with mines, asteroids, debris
            checkTemplateOverlap(lastMoveShapeUsed, otherBumpableShapes);
            //Check for ship bumping other ships, mines, asteroids, debris
            announceBumpAndPaint(otherBumpableShapes);
            //Check if a ship becomes out of bounds
            checkIfOutOfBounds(yourShipName);

            //Add all the detected overlapping shapes to the map drawn components here
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

return innerCommand;
        }
        //the maneuver has finished. return control of the event to vassal to do nothing
        return piece.keyEvent(stroke);
    }

    private void checkTemplateOverlap(Shape lastMoveShapeUsed, List<BumpableWithShape> otherBumpableShapes) {

        List<BumpableWithShape> collidingEntities = findCollidingEntities(lastMoveShapeUsed, otherBumpableShapes);
        CollisionVisualization cvFoundHere = new CollisionVisualization(lastMoveShapeUsed);

        int howManyBumped = 0;
        for (BumpableWithShape bumpedBumpable : collidingEntities) {
            if (DRAW_COLLISIONS) {
                String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
                if (bumpedBumpable.type.equals("Asteroid")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + "'s maneuver template and an asteroid.";
                    logToChatWithTime(bumpAlertString);
                    cvFoundHere.add(bumpedBumpable.shape);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Debris")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + "'s maneuver template and a debris cloud.";
                    logToChatWithTime(bumpAlertString);
                    cvFoundHere.add(bumpedBumpable.shape);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Mine")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + "'s maneuver template and a mine.";
                    logToChatWithTime(bumpAlertString);
                    cvFoundHere.add(bumpedBumpable.shape);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                }
            }
        }
        if (howManyBumped > 0) {
            this.previousCollisionVisualization.add(lastMoveShapeUsed);
        }
    }

    private void checkIfOutOfBounds(String yourShipName) {
        Rectangle mapArea = new Rectangle(0,0,0,0);
        try{
            Board b = getMap().getBoards().iterator().next();
            mapArea = b.bounds();
            String name = b.getName();
        }catch(Exception e)
        {
            logToChat("Board name isn't formatted right, change to #'x#' Description");
        }
        Shape theShape = BumpableWithShape.getBumpableCompareShape(this);

        if(theShape.getBounds().getMaxX() > mapArea.getBounds().getMaxX()  || // too far to the right
                theShape.getBounds().getMaxY() > mapArea.getBounds().getMaxY() || // too far to the bottom
                theShape.getBounds().getX() < mapArea.getBounds().getX() || //too far to the left
                theShape.getBounds().getY() < mapArea.getBounds().getY()) // too far to the top
        {

            logToChatWithTime("* -- " + yourShipName + " flew out of bounds");
            CollisionVisualization collisionVisualization = new CollisionVisualization(theShape);
            this.previousCollisionVisualization.add(theShape);
        }
    }

    private void announceBumpAndPaint(List<BumpableWithShape> otherBumpableShapes) {
        Shape theShape = BumpableWithShape.getBumpableCompareShape(this);

        List<BumpableWithShape> collidingEntities = findCollidingEntities(theShape, otherBumpableShapes);

        int howManyBumped = 0;
        for (BumpableWithShape bumpedBumpable : collidingEntities) {
            if (DRAW_COLLISIONS) {
                String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
                if (bumpedBumpable.type.equals("Ship")) {
                    String otherShipName = getShipStringForReports(false, bumpedBumpable.pilotName, bumpedBumpable.shipName);
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and " + otherShipName + ". Resolve this by hitting the 'c' key.";
                    logToChatWithTime(bumpAlertString);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Asteroid")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and an asteroid.";
                    logToChatWithTime(bumpAlertString);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Debris")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and a debris cloud.";
                    logToChatWithTime(bumpAlertString);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                } else if (bumpedBumpable.type.equals("Mine")) {
                    String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and a mine.";
                    logToChatWithTime(bumpAlertString);
                    this.previousCollisionVisualization.add(bumpedBumpable.shape);
                    howManyBumped++;
                }
            }
        }
        if (howManyBumped > 0) {
            this.previousCollisionVisualization.add(theShape);
        }

    }

    private String getShipStringForReports(boolean isYours, String pilotName, String shipName)
    {

        String yourShipName = (isYours ? GlobalOptions.getInstance().getPlayerId() + "'s" : "another ship");

        if (!pilotName.equals("")) { yourShipName += " " + pilotName; }
        else yourShipName += " ship";
        if (!shipName.equals("")) { yourShipName += " (" + shipName + ")"; }
        else yourShipName += " ";

        return yourShipName;
    }
    /**
     * Iterate in reverse over path of last maneuver and return a command that
     * will move the ship to a non-overlapping position and rotation
     *
     * @return
     */
    private Command resolveBump(List<BumpableWithShape> otherBumpableShapes) {
        if (this.lastManeuver == null || this.prevPosition == null) {
            return null;
        }
        Shape rawShape = BumpableWithShape.getRawShape(this);
        final List<PathPart> parts = this.lastManeuver.getTransformedPathParts(
                this.prevPosition.x,
                this.prevPosition.y,
                this.prevPosition.angle,
                isLargeShip(this)
        );



        for (int i = parts.size() - 1; i >= 0; i--) {
            PathPart part = parts.get(i);
            Shape movedShape = AffineTransform
                    .getTranslateInstance(part.getX(), part.getY())
                    .createTransformedShape(rawShape);
            double roundedAngle = convertAngleToGameLimits(part.getAngle());
            movedShape = AffineTransform
                    .getRotateInstance(Math.toRadians(-roundedAngle), part.getX(), part.getY())
                    .createTransformedShape(movedShape);

            BumpableWithShape bumpedBumpable = findCollidingEntity(movedShape, otherBumpableShapes);
            if (bumpedBumpable == null) {
                if (DRAW_COLLISIONS) {
                    if (this.previousCollisionVisualization != null) {
                        getMap().removeDrawComponent(this.previousCollisionVisualization);
                    }
                }
                return buildTranslateCommand(part,0.0f);
            }
        }

        // Could not find a position that wasn't bumping, bring it back to where it was before
        return buildTranslateCommand(new PathPart(this.prevPosition.x, this.prevPosition.y, this.prevPosition.angle), 0.0f);
    }

    /**
     * Builds vassal command to transform the current ship to the given PathPart
     *
     * @param part
     * @return
     */
    private Command buildTranslateCommand(PathPart part, double additionalAngle) {
        // Copypasta from VASSAL.counters.Pivot
        ChangeTracker changeTracker = new ChangeTracker(this);
        getRotator().setAngle(part.getAngle() + additionalAngle);

        setProperty("Moved", Boolean.TRUE);

        Command result = changeTracker.getChangeCommand();

        GamePiece outermost = Decorator.getOutermost(this);
        MoveTracker moveTracker = new MoveTracker(outermost);
        Point point = new Point((int) Math.floor(part.getX() + 0.5), (int) Math.floor(part.getY() + 0.5));
        // ^^ There be dragons here ^^ - vassals gives positions as doubles but only lets them be set as ints :(
        this.getMap().placeOrMerge(outermost, point);
        result = result.append(moveTracker.getMoveCommand());
/*
        MovementReporter reporter = new MovementReporter(result);

        Command reportCommand = reporter.getReportCommand();
        if (reportCommand != null) {
            reportCommand.execute();
            result = result.append(reportCommand);
        }

        result = result.append(reporter.markMovedPieces());
*/
        return result;
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

    /**
     * Uses a FreeRotator unassociated with any game pieces with a 360 rotation limit
     * to convert the provided angle to the same angle the ship would be drawn at
     * by vassal
     *
     * @param angle
     * @return
     */
    private double convertAngleToGameLimits(double angle) {
        this.testRotator.setAngle(angle);
        return this.testRotator.getAngle();
    }


    private boolean isLargeShip(Decorator ship) {
        return BumpableWithShape.getRawShape(ship).getBounds().getWidth() > 114;
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