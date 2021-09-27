package mic;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import VASSAL.build.GameModule;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mic.manuvers.ManeuverPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.MoveTracker;
import VASSAL.configure.HotKeyConfigurer;
import mic.manuvers.ManeuverPaths;
import mic.manuvers.PathPart;

import static mic.Util.*;

/**
 * Created by amatheny on 2/14/17.
 *
 * Second role: to completely intercept every maneuver shortcut and deal with movement AND autobump AND out of bound detection
 */
public class AutoBumpDecorator extends Decorator implements EditablePiece {

    public static float DOT_DIAMETER = 30.0f;
    public static float DOT_FUDGE = 40.0f;
    MouseListener ml;
    List<RepositionChoiceVisual> rpcList = Lists.newArrayList();

    private Shape shapeForTemplate;

    private static final Logger logger = LoggerFactory.getLogger(AutoBumpDecorator.class);
    public static final String ID = "auto-bump;";
    private final FreeRotator testRotator;

    private ShipPositionState prevPosition = null;
    private ManeuverPaths lastManeuver = null;
    private FreeRotator myRotator = null;
    //public CollisionVisualization previousCollisionVisualization = null;
    MapVisualizations previousCollisionVisualization = null;
    boolean lastTRWasNotCentered = false;
    double lastCenteredTRX;
    double lastCenteredTRY;


    private static Map<KeyStroke, ManeuverPaths> keyStrokeToManeuver = ImmutableMap.<KeyStroke, ManeuverPaths>builder()
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.Str1) //SHIFT 1
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.Str2) //SHIFT 2
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.Str3) //SHIFT 3
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.Str4) //SHIFT 4
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.Str5) //SHIFT 5
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.LT1) //CTRL SHIFT 1
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.LT2) //CTRL SHIFT 2
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.LT3) //CTRL SHIFT 3
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.RT1) //ALT SHIFT 1
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.RT2) //ALT SHIFT 2
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.RT3) //ALT SHIFT 3
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.LBk1) //CTRL 1
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.LBk2) //CTRL 2
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.LBk3) //CTRL 3
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.RBk1) //ALT 1
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.RBk2) //ALT 2
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.RBk3) //ALT 3
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK+KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.K1) //ALT CTRL 1
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK+KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.K2) //ALT CTRL 2
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK+KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.K3) //ALT CTRL 3
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_DOWN_MASK+KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.K4) //ALT CTRL 4
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.CTRL_DOWN_MASK+KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.K5) //ALT CTRL 5
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.RevLbk1) //CTRL 6
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.RevStr1) //SHIFT 6
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_7, KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.RevStr2) //SHIFT 7
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.RevRbk1) //ALT 6
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.SloopL1) //CTRL Q
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.SloopL2) //CTRL W
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.SloopL3) //CTRL E
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.SloopR1) //ALT Q
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.SloopR2) //ALT W - has a conflict with file open dialog
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.SloopR3) //ALT E
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.SloopL3Turn) //CTRL SHIFT E
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.ALT_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.SloopR3Turn) //ALT SHIFT E
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.TrollL1) //CTRL I
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.TrollL2) //CTRL Y
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.TrollL3) //CTRL T
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.TrollR1) //ALT I
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.TrollR2) //ALT Y
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.TrollR3) //ALT T


            .put(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.SideSlipL1Turn) //ALT K
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.SideSlipL2Turn) //CTRL K
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.SideSlipL3Turn) //SHIFT K

            .put(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.SideSlipR1Turn) //CTRL SHIFT K
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.SideSlipR2Turn) //ALT SHIFT K
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_DOWN_MASK+KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.SideSlipR3Turn) //ALT CTRL K

            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK,false), ManeuverPaths.SideSlipL1Bank) //ALT O
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.SideSlipL2Bank) //CTRL O
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.SideSlipL3Bank) //SHIFT O

            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0,false), ManeuverPaths.SideSlipR1Bank) //O
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), ManeuverPaths.SideSlipR2Bank) //ALT SHIFT O
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK+KeyEvent.CTRL_DOWN_MASK,false), ManeuverPaths.SideSlipR3Bank) //ALT CTRL O
            .build();

    public AutoBumpDecorator() {
        this(null);
    }

    public AutoBumpDecorator(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
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

    //this is similar to spawnRotatedPiece, but uses the real template instead of the collision aide template, triggered by 's' instead of 'c' on a selected ship
    private Command spawnRotatedPieceRealTemplate(ManeuverPaths theManeuv) {
        //STEP 1: Collision aide template, centered as in in the image file, centered on 0,0 (upper left corner)
        GamePiece piece = newPiece(findPieceSlotByID(theManeuv.getTemplateGpID()));

        //Info Gathering: Position of the center of the ship, integers inside a Point
        double shipx = this.getPosition().getX();
        double shipy = this.getPosition().getY();

        //use the centered tallon roll choice if it's this move being treated
        if(lastTRWasNotCentered &&
                (lastManeuver == ManeuverPaths.TrollL1  || lastManeuver == ManeuverPaths.TrollL2 || lastManeuver == ManeuverPaths.TrollL3
                        || lastManeuver == ManeuverPaths.TrollR1  || lastManeuver == ManeuverPaths.TrollR2 || lastManeuver == ManeuverPaths.TrollR3)){
            shipx = lastCenteredTRX;
            shipy = lastCenteredTRY;
        }


        Point shipPt = new Point((int) shipx, (int) shipy); // these are the center coordinates of the ship, namely, shipPt.x and shipPt.y

        //Info Gathering: offset vector (integers) that's used in local coordinates, right after a rotation found in lastManeuver.getTemplateAngle(), so that it's positioned behind nubs properly
        double x=0.0, y=0.0;
        if(whichSizeShip(this)==3){
            x = theManeuv.getOffsetXLarge();
            y = theManeuv.getOffsetYLarge();
        }
        else if(whichSizeShip(this)==2){
            x = theManeuv.getOffsetXMedium();
            y = theManeuv.getOffsetYMedium();
        }
        else{
            x = theManeuv.getOffsetX();
            y = theManeuv.getOffsetY();
        }
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

    private Command spawnRotatedPiece(ManeuverPaths theManeuv) {
        //STEP 1: Collision aide template, centered as in in the image file, centered on 0,0 (upper left corner)
        GamePiece piece = newPiece(findPieceSlotByID(theManeuv.getAide_gpID()));

        //Info Gathering: Position of the center of the ship, integers inside a Point
        double shipx = this.getPosition().getX();
        double shipy = this.getPosition().getY();

        //use the centered tallon roll choice if it's this move being treated
        if(lastTRWasNotCentered &&
                (lastManeuver == ManeuverPaths.TrollL1  || lastManeuver == ManeuverPaths.TrollL2 || lastManeuver == ManeuverPaths.TrollL3
                        || lastManeuver == ManeuverPaths.TrollR1  || lastManeuver == ManeuverPaths.TrollR2 || lastManeuver == ManeuverPaths.TrollR3)){
            shipx = lastCenteredTRX;
            shipy = lastCenteredTRY;
        }


        Point shipPt = new Point((int) shipx, (int) shipy); // these are the center coordinates of the ship, namely, shipPt.x and shipPt.y

         //Info Gathering: offset vector (integers) that's used in local coordinates, right after a rotation found in lastManeuver.getTemplateAngle(), so that it's positioned behind nubs properly
        double x=0.0, y=0.0;
        if(whichSizeShip(this)==3){
            x = theManeuv.getAide_xLarge();
            y = theManeuv.getAide_yLarge();
        }
        else if(whichSizeShip(this)==2){
            x = theManeuv.getAide_xMedium();
            y = theManeuv.getAide_yMedium();
        }
        else{
            x = theManeuv.getAide_x();
            y = theManeuv.getAide_y();
        }
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
        this.previousCollisionVisualization = new MapVisualizations();

        ManeuverPaths path = getKeystrokePath(stroke);
        // Is this a keystroke for a maneuver? Deal with the 'no' cases first
        if (path == null) {
            //check to see if 'c' was pressed
            if(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false).equals(stroke) && lastManeuver != null) {
                List<BumpableWithShape> otherShipShapes = getShipsWithShapes();

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

            //alternatively, check to see if 's' was pressed - spawn the real template instead of the collision aide template
            else if(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false).equals(stroke) && lastManeuver != null) {
                List<BumpableWithShape> otherShipShapes = getShipsWithShapes();

                if(lastManeuver != null) {
                    Command placeCollisionAide = spawnRotatedPieceRealTemplate(lastManeuver);
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
            // 's' keystroke has finished here, leave the method altogether
            return piece.keyEvent(stroke);
        }

        // We know we're dealing with a maneuver keystroke
        if (stroke.isOnKeyRelease() == false) {
            // find the list of other bumpables
            List<BumpableWithShape> otherBumpableShapes = getBumpablesWithShapes();

            String contemplatingPlayerName = getCurrentPlayer().getName();

            //safeguard old position and path
            this.prevPosition = getCurrentState();
            this.lastManeuver = path;


            //Get the ship name string for announcements
            String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());


            boolean sideslipBank = false, sideslipTurn = false, LeftOtherwiseRight = false;


            if(lastManeuver == ManeuverPaths.SideSlipL1Turn || lastManeuver == ManeuverPaths.SideSlipL2Turn || lastManeuver == ManeuverPaths.SideSlipL3Turn){
                sideslipTurn = true;
                LeftOtherwiseRight = true;
            }
            if(lastManeuver == ManeuverPaths.SideSlipR1Turn || lastManeuver == ManeuverPaths.SideSlipR2Turn || lastManeuver == ManeuverPaths.SideSlipR3Turn) {
                sideslipTurn = true;
                LeftOtherwiseRight = false;
            }

            if(lastManeuver == ManeuverPaths.SideSlipL1Bank || lastManeuver == ManeuverPaths.SideSlipL2Bank || lastManeuver == ManeuverPaths.SideSlipL3Bank) {
                sideslipBank = true;
                LeftOtherwiseRight = true;
            }
            if(lastManeuver == ManeuverPaths.SideSlipR1Bank || lastManeuver == ManeuverPaths.SideSlipR2Bank || lastManeuver == ManeuverPaths.SideSlipR3Bank) {
                sideslipBank = true;
                LeftOtherwiseRight = false;
            }

            //This PathPart list will be used everywhere: moving, bumping, out of boundsing
            //maybe fetch it for both 'c' behavior and movement
            final List<PathPart> parts = path.getTransformedPathParts(
                    this.getCurrentState().x,
                    this.getCurrentState().y,
                    this.getCurrentState().angle,
                    whichSizeShip(this), sideslipTurn, sideslipBank, LeftOtherwiseRight
            );

            //this is the final ship position post-move
            PathPart part = parts.get(parts.size()-1);

            //if the path is a troll:
            if(lastManeuver == ManeuverPaths.TrollL1  || lastManeuver == ManeuverPaths.TrollL2 || lastManeuver == ManeuverPaths.TrollL3
                    || lastManeuver == ManeuverPaths.TrollR1  || lastManeuver == ManeuverPaths.TrollR2 || lastManeuver == ManeuverPaths.TrollR3) {

                LeftOtherwiseRight = true;
                if(lastManeuver == ManeuverPaths.TrollR1  || lastManeuver == ManeuverPaths.TrollR2 || lastManeuver == ManeuverPaths.TrollR3) LeftOtherwiseRight = false;


                    ////modify that last element slightly in case there was a Troll or Sloop ordered
                PathPart lastPartAFAP = path.getTweakedPathPartForTroll(
                        this.getCurrentState().x,
                        this.getCurrentState().y,
                        this.getCurrentState().angle,
                        whichSizeShip(this), LeftOtherwiseRight, 1);
                PathPart lastPartABAP = path.getTweakedPathPartForTroll(
                        this.getCurrentState().x,
                        this.getCurrentState().y,
                        this.getCurrentState().angle,
                        whichSizeShip(this), LeftOtherwiseRight ,-1);;
                ////go to a triple choice mouse interface
                ////figure out valid positions, paint the dots in red if they're not, announce everything
                ////manage an early click-outside cancel

                List<PathPart> threePartChoices = Lists.newArrayList();
                threePartChoices.add(lastPartABAP);
                threePartChoices.add(part);
                threePartChoices.add(lastPartAFAP);

                lastCenteredTRX = part.getX();
                lastCenteredTRY = part.getY();

                int TRSpeed = 3;
                if(lastManeuver == ManeuverPaths.TrollR1  || lastManeuver == ManeuverPaths.TrollL1) TRSpeed = 1;
                if(lastManeuver == ManeuverPaths.TrollR2  || lastManeuver == ManeuverPaths.TrollL2) TRSpeed = 2;
                StringBuilder sb = new StringBuilder("*--- ");

                String trName = "Tallon Roll " + (LeftOtherwiseRight?"Left ":"Right ") + TRSpeed;
                int nbRedDots = offerTripleChoices(threePartChoices, true, getTheMainMap(), LeftOtherwiseRight, trName);

                sb.append(contemplatingPlayerName + " is considering a " + trName + " for " + yourShipName + ". There are " + (3-nbRedDots) + " unobstructed choice(s) out of 3. ");
                if(nbRedDots == 3) sb.append("The Tallon Roll must be completed even though there are no valid positions. Complete the move and if it lands on a ship, select the moving ship and hit 'c' to resolve the overlap.");

                logToChat(sb.toString());
                return null;
            }

            //Start the Command chain
            Command innerCommand = piece.keyEvent(stroke);
            innerCommand.append(buildTranslateCommand(part, path.getAdditionalAngleForShip()));
/* the code no longer reaches this spot
            //check for Tallon rolls and spawn the template
            if(lastManeuver == ManeuverPaths.TrollL1  || lastManeuver == ManeuverPaths.TrollL2 || lastManeuver == ManeuverPaths.TrollL3
            || lastManeuver == ManeuverPaths.TrollR1  || lastManeuver == ManeuverPaths.TrollR2 || lastManeuver == ManeuverPaths.TrollR3) {
                Command placeTrollTemplate = spawnRotatedPiece(lastManeuver);
                innerCommand.append(placeTrollTemplate);
            }
            */
            //These lines fetch the Shape of the last movement template used
            FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(this), FreeRotator.class));
            Shape lastMoveShapeUsed = path.getTransformedTemplateShape(this.getPosition().getX(),
                    this.getPosition().getY(),
                    whichSizeShip(this),
                    rotator);

            //don't check for collisions in windows other than the main map
            if(!"Contested Sector".equals(getMap().getMapName())) return innerCommand;

            innerCommand.append(logToChatWithTimeCommandNoExecute("* --- " + yourShipName + " performs move: " + path.getFullName()));

            //Check for template shape overlap with mines, asteroids, debris
            checkTemplateOverlap(lastMoveShapeUsed, otherBumpableShapes);
            //Check for ship bumping other ships, mines, asteroids, debris
            announceBumpAndPaint(otherBumpableShapes);
            //Check if a ship becomes out of bounds
            checkIfOutOfBounds(yourShipName);

            //Add all the detected overlapping shapes to the map drawn components here
            if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                innerCommand.append(this.previousCollisionVisualization);
                this.previousCollisionVisualization.execute();
            }
            return innerCommand;
        }
        //the maneuver has finished. return control of the event to vassal to do nothing
        return piece.keyEvent(stroke);
    }

    private void checkTemplateOverlap(Shape lastMoveShapeUsed, List<BumpableWithShape> otherBumpableShapes) {
        List<BumpableWithShape> collidingEntities = findCollidingEntities(lastMoveShapeUsed, otherBumpableShapes);
        MapVisualizations cvFoundHere = new MapVisualizations(lastMoveShapeUsed);

        int howManyBumped = 0;
        for (BumpableWithShape bumpedBumpable : collidingEntities) {
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
            } else if (bumpedBumpable.type.equals("GasCloud")) {
                String bumpAlertString = "* --- Overlap detected with " + yourShipName + "'s maneuver template and a gas cloud.";
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
            }else if (bumpedBumpable.type.equals("Remote")) {
                String bumpAlertString = "* --- Overlap detected with " + yourShipName + "'s maneuver template and a remote.";
                logToChatWithTime(bumpAlertString);
                cvFoundHere.add(bumpedBumpable.shape);
                this.previousCollisionVisualization.add(bumpedBumpable.shape);
                howManyBumped++;
            }
        }
        if (howManyBumped > 0) {
            this.previousCollisionVisualization.add(lastMoveShapeUsed);
        }
    }
    private boolean checkIfOutOfBounds(String yourShipName, Shape shapeForOutOfBounds, boolean andSayIt, boolean wantShapes) {
        Rectangle mapArea = new Rectangle(0,0,0,0);
        try{
            Board b = getMap().getBoards().iterator().next();
            mapArea = b.bounds();
            String name = b.getName();
        }catch(Exception e)
        {
            logToChat("Board name isn't formatted right, change to #'x#' Description");
        }
        //Shape theShape = BumpableWithShape.getBumpableCompareShape(this);

        if(shapeForOutOfBounds.getBounds().getMaxX() > mapArea.getBounds().getMaxX()  || // too far to the right
                shapeForOutOfBounds.getBounds().getMaxY() > mapArea.getBounds().getMaxY() || // too far to the bottom
                shapeForOutOfBounds.getBounds().getX() < mapArea.getBounds().getX() || //too far to the left
                shapeForOutOfBounds.getBounds().getY() < mapArea.getBounds().getY()) // too far to the top
        {

            if(andSayIt) logToChatWithTime("* -- " + yourShipName + " flew out of bounds");
            if(wantShapes) this.previousCollisionVisualization.add(shapeForOutOfBounds);
            return true;
        }

        return false;
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
            this.previousCollisionVisualization.add(theShape);
        }
    }

    private void announceBumpAndPaint(List<BumpableWithShape> otherBumpableShapes) {
        Shape theShape = BumpableWithShape.getBumpableCompareShape(this);

        List<BumpableWithShape> collidingEntities = findCollidingEntities(theShape, otherBumpableShapes);

        int howManyBumped = 0;
        for (BumpableWithShape bumpedBumpable : collidingEntities) {
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
            } else if (bumpedBumpable.type.equals("GasCloud")) {
                String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and a gas cloud.";
                logToChatWithTime(bumpAlertString);
                this.previousCollisionVisualization.add(bumpedBumpable.shape);
                howManyBumped++;
            }else if (bumpedBumpable.type.equals("Remote")) {
                String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and a remote.";
                logToChatWithTime(bumpAlertString);
                this.previousCollisionVisualization.add(bumpedBumpable.shape);
                howManyBumped++;
            }else if (bumpedBumpable.type.equals("Mine")) {
                String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and a mine.";
                logToChatWithTime(bumpAlertString);
                this.previousCollisionVisualization.add(bumpedBumpable.shape);
                howManyBumped++;
            }
        }
        if (howManyBumped > 0) {
            this.previousCollisionVisualization.add(theShape);
        }
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


        boolean sideslipBank = false, sideslipTurn = false, LeftOtherwiseRight = false;


        if(lastManeuver == ManeuverPaths.SideSlipL1Turn || lastManeuver == ManeuverPaths.SideSlipL2Turn || lastManeuver == ManeuverPaths.SideSlipL3Turn){
            sideslipTurn = true;
            LeftOtherwiseRight = true;
        }
        if(lastManeuver == ManeuverPaths.SideSlipR1Turn || lastManeuver == ManeuverPaths.SideSlipR2Turn || lastManeuver == ManeuverPaths.SideSlipR3Turn) {
            sideslipTurn = true;
            LeftOtherwiseRight = false;
        }

        if(lastManeuver == ManeuverPaths.SideSlipL1Bank || lastManeuver == ManeuverPaths.SideSlipL2Bank || lastManeuver == ManeuverPaths.SideSlipL3Bank) {
            sideslipBank = true;
            LeftOtherwiseRight = true;
        }
        if(lastManeuver == ManeuverPaths.SideSlipR1Bank || lastManeuver == ManeuverPaths.SideSlipR2Bank || lastManeuver == ManeuverPaths.SideSlipR3Bank) {
            sideslipBank = true;
            LeftOtherwiseRight = false;
        }

        final List<PathPart> parts = this.lastManeuver.getTransformedPathParts(
                this.prevPosition.x,
                this.prevPosition.y,
                this.prevPosition.angle,
                whichSizeShip(this), sideslipTurn, sideslipBank, LeftOtherwiseRight
        );

        for (int i = parts.size() - 1; i >= 0; i--) {
            PathPart part = parts.get(i);

            Shape movedShape = rawShape;
            double additionalAngleSideSlip = 0.0f;
            if(sideslipTurn && i != (parts.size() - 1)) {
                additionalAngleSideSlip = 90.0f;
                if(LeftOtherwiseRight)additionalAngleSideSlip = -90.0f;
            }
            if(sideslipBank && i != (parts.size() - 1)) {
                additionalAngleSideSlip = 90.0f;
                if(LeftOtherwiseRight)additionalAngleSideSlip = -90.0f;
            }


            movedShape = AffineTransform
                    .getTranslateInstance(part.getX(), part.getY())
                    .createTransformedShape(movedShape);
            double roundedAngle = convertAngleToGameLimits(part.getAngle());
            movedShape = AffineTransform
                    .getRotateInstance(Math.toRadians(-roundedAngle), part.getX(), part.getY())
                    .createTransformedShape(movedShape);

            BumpableWithShape bumpedBumpable = findCollidingEntity(movedShape, otherBumpableShapes);
            if (bumpedBumpable == null) {
                return buildTranslateCommand(part,additionalAngleSideSlip);
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
            if (Util.shapesOverlap(myTestShape, otherBumpableShape.shape)) {
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
        //old way
        // String hotKey = HotKeyConfigurer.encode(keyStroke);

        if (keyStrokeToManeuver.containsKey(keyStroke)) {
            return keyStrokeToManeuver.get(keyStroke);
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
        for (BumpableWithShape bumpable : OverlapCheckManager.getBumpablesOnMap(true,null)) {
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
            try{
                String bidon = null;
                bidon=piece.getProperty("dont_collide_with_this").toString();
                if(bidon != null)
                {
                    continue;
                }
            }
            catch(Exception e){}
            if (piece.getState().contains("this_is_a_ship")) {

                ships.add(new BumpableWithShape((Decorator)piece, "Ship",
                        piece.getProperty("Pilot Name").toString(), piece.getProperty("Craft ID #").toString(),
                        this.getInner().getState().contains("this_is_2pointoh")));
            }
        }
        return ships;
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


    //1 = small, 2 = medium, 3 = large
    private int whichSizeShip(Decorator ship) {
        if(BumpableWithShape.getRawShape(ship).getBounds().getWidth() > 224) return 3;
        if(BumpableWithShape.getRawShape(ship).getBounds().getWidth() > 167) return 2;
        return 1;
    }

    public Command tripleChoiceDispatcher(int which, String pilotName) {

        if (!isATripleChoiceAllowed()) return null;

        Command startIt = startTripleChoiceStopNewOnes();
        List<ManeuverPaths> maneuChoices = Lists.newArrayList();
        final VASSAL.build.module.Map theMap = Util.getTheMainMap();
        String contemplatingPlayerName = getCurrentPlayer().getName();

        StringBuilder sb = new StringBuilder("*--- ");
        switch(which){
            //Tallon Roll Left 1
            case 13:
                break;
            //Tallon Roll Left 2
            case 14:
                break;
            //Tallon Roll Left 3
            case 202:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for Tallon roll left 3 for " + pilotName);
                maneuChoices = Lists.newArrayList(ManeuverPaths.TrollL3, ManeuverPaths.TrollL3, ManeuverPaths.TrollL3);
                break;
            //Tallon Roll Right 1
            case 16:
                break;
            //Tallon Roll Right 2
            case 17:
                break;
            //Tallon Roll Right 3
            case 18:
                break;
            //Segnor's Loop Left 1
            //Segnor's Loop Left 2
            //Segnor's Loop Left 3
            //Segnor's Loop Right 1
            //Segnor's Loop Right 2
            //Segnor's Loop Right 3
        }
int nbOfRedDots = 0;
        //int nbOfRedDots = offerTripleChoices(maneuChoices, true, theMap);
        sb.append(". There are " + (3-nbOfRedDots) + " valid position"+(nbOfRedDots>1?"s":"")+" to pick from." );
        if(startIt!=null) startIt.append(logToChatCommand(sb.toString()));
        else startIt = logToChatCommand(sb.toString());
        if(nbOfRedDots==-1) return null;
        return startIt;

    }
    private void closeMouseListener(VASSAL.build.module.Map aMap, MouseListener aML){
        aMap.removeLocalMouseListener(aML);
    }

    private void removeVisuals(VASSAL.build.module.Map aMapm){
        for(RepositionChoiceVisual r : rpcList){
            aMapm.removeDrawComponent(r);
        }
        rpcList.clear();
    }

    private Shape repositionedTemplateShape(ManeuverPaths maneuChoice){
        //STEP 1: Collision reposition template, centered as in in the image file, centered on 0,0 (upper left corner)
        GamePiece templatePiece = newPiece(findPieceSlotByID(maneuChoice.getTemplateGpID()));

        shapeForTemplate = templatePiece.getShape();

        //Info Gathering: gets the angle from repoTemplate which deals with degrees, local space with ship at 0,0, pointing up
        double templateAngle = maneuChoice.getTemplateAngle(); //repo maneuver's angle
        double globalShipAngle = this.getRotator().getAngle(); //ship angle
        //STEP 2: rotate the reposition template with both angles
        FreeRotator fR = (FreeRotator)Decorator.getDecorator(templatePiece, FreeRotator.class);
        fR.setAngle(globalShipAngle + templateAngle);

        //Info Gathering: Offset 1, put to the side of the ship, local coords, adjusting for large base if it is found
        double off1x = maneuChoice.getOffsetX();
        double off1y = maneuChoice.getOffsetY();

        //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
        double off2x = this.getPosition().getX();
        double off2y = this.getPosition().getY();

        //STEP 3: rotate the offset1 dependant within the spawner's local coordinates
        double off1x_rot = rotX(off1x, off1y, globalShipAngle);
        double off1y_rot = rotY(off1x, off1y, globalShipAngle);

        //STEP 4: translation into place
        shapeForTemplate = AffineTransform.
                getTranslateInstance((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y).
                createTransformedShape(shapeForTemplate);
        double roundedAngle = convertAngleToGameLimits(globalShipAngle + templateAngle);
        shapeForTemplate = AffineTransform
                .getRotateInstance(Math.toRadians(-roundedAngle), (int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y)
                .createTransformedShape(shapeForTemplate);

        return shapeForTemplate;
    }

    private Shape repositionedShipShapeAfterTroll(PathPart part, boolean LeftOtherwiseRight){
        double globalShipAngle = this.getRotator().getAngle(); //ship angle
        double templateTurnsShipAngle = 180.0f; //template making the ship turn angle

        // spawn a copy of the ship without the actions
        //Shape shapeForOverlap2 = getCopyOfShapeWithoutActionsForOverlapCheck(this.piece,repoTemplate );
        Shape shapeForShip = Decorator.getDecorator(Decorator.getOutermost(this), NonRectangular.class).getShape();

        double roundedAngle = convertAngleToGameLimits(globalShipAngle - templateTurnsShipAngle);

        shapeForShip = AffineTransform
                .getRotateInstance(Math.toRadians(-roundedAngle), 0,0)
                .createTransformedShape(shapeForShip);

        //STEP 8: translation into place
        shapeForShip = AffineTransform.
                getTranslateInstance(part.getX(), part.getY()).
                createTransformedShape(shapeForShip);

        return shapeForShip;
    }



    private int offerTripleChoices(List<PathPart> trCoords, boolean is2pointOh, VASSAL.build.module.Map theMap, boolean LeftOtherwiseRight, String trName){
        //Getting into this function, repoShip is associated with the template used to reposition the ship. We also need the non-mapped final ship tentative position

        int size = whichSizeShip(this);
        removeVisuals(theMap);
        int wideningFudgeFactorBetweenDots = -1; //will go from -1 to 0 to 1 in the following loop

        // STEP 0: gather ship angle and rotator
        double shipAngle = this.getRotator().getAngle(); //ship angle
        //FreeRotator fR = (FreeRotator) Decorator.getDecorator(piece, FreeRotator.class);

        int nbOfRedDots = 0;

        List<GamePiece> skipItselfList = Lists.newArrayList();
        skipItselfList.add(this.piece);
        List<BumpableWithShape> shipsOrObstacles = OverlapCheckManager.getBumpablesOnMap(true, skipItselfList);

        int index = 1;
        for(PathPart trCoord : trCoords) { //loops over the list of potential repositions
            double off3x = 0.0f;
            double off3y = - wideningFudgeFactorBetweenDots * size * 1.2f  * DOT_FUDGE;
            double off3x_t_rot = rotX(off3x, off3y, shipAngle+(LeftOtherwiseRight?180.0f:-180.0f));
            double off3y_t_rot = rotY(off3x, off3y, shipAngle+(LeftOtherwiseRight?180.0f:-180.0f));

            //STEP 2: Gather info for ship's final wanted position
            // spawn a copy of the ship without the actions
            Shape shapeForShipOverlap = repositionedShipShapeAfterTroll(trCoord, LeftOtherwiseRight);

            float diam = DOT_DIAMETER + (size - 1) * DOT_DIAMETER * 0.666f;
            Shape dot = new Ellipse2D.Float(-diam / 2, -diam / 2, diam, diam);

            dot = AffineTransform.
                    getTranslateInstance((int) trCoord.getX()+ (int)off3x_t_rot, (int) trCoord.getY() + (int) off3y_t_rot).
                    createTransformedShape(dot);

            boolean wantOverlapColor = false;

            String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
                if (shapeForShipOverlap != null) {

                    List<BumpableWithShape> overlappingShipOrObstacles = findCollidingEntities(shapeForShipOverlap, shipsOrObstacles);

                    if (overlappingShipOrObstacles.size() > 0) {
                        wantOverlapColor = true;
                    }
                }

                // STEP 9.5: Check for movement out of bounds
                boolean outsideCheck = checkIfOutOfBounds(yourShipName, shapeForShipOverlap, false, false);
                if (outsideCheck) wantOverlapColor = true;


            //STEP 11: reposition the ship
            //Add visuals according to the selection of repositioning

            String extraName = " Centered.";
            switch(index){
                case 1:
                    extraName = " as backward as possible.";
                     break;
                case 2:
                    extraName = " centered.";
                    break;
                case 3:
                    extraName = " as forward as possible.";
                    break;
            }

            String tallonRollTitle = trName + extraName;
            RepositionChoiceVisual rpc = new RepositionChoiceVisual(shapeForShipOverlap, dot, wantOverlapColor,tallonRollTitle,0, null, trCoord, (LeftOtherwiseRight?90.0f:-90.0f));
            rpcList.add(rpc);

            //return bigCommand;
            wideningFudgeFactorBetweenDots++;
            if(wantOverlapColor == true) nbOfRedDots++;
            index++;
        } // end of loop around the 3 tallon roll choices

        //FINAL STEP: add the visuala to the map and the mouse listener
        for(RepositionChoiceVisual r : rpcList){
            theMap.addDrawComponent(r);
        }

        final Decorator shipToReposition = this;
        final VASSAL.build.module.Map finalMap = theMap;
        ml = new MouseListener() {
            int i=0;
            public void mousePressed(MouseEvent e) {
                if(e.isConsumed()) return;

                List<RepositionChoiceVisual> copiedList = Lists.newArrayList();
                for(RepositionChoiceVisual r : rpcList){
                    copiedList.add(r);
                }
                //When it gets the answer, gracefully close the mouse listenener and remove the visuals
                RepositionChoiceVisual theChosenOne = null;
                boolean slightMisclick = false;

                for(RepositionChoiceVisual r : copiedList){
                    if(r.theDot.contains(e.getX(),e.getY())){
                        theChosenOne = r;
                        break;
                    } else if(r.thePieceShape.contains(e.getX(), e.getY()))
                    {
                        slightMisclick = true; //in the ship area but not inside the dot, allow the whole thing to survive
                    }
                }
                try{
                    if(theChosenOne != null){
                        removeVisuals(finalMap);
                        Command endIt = stopTripleChoiceMakeNextReady();
                        if(endIt!=null) endIt.execute();
                        //Change this line to another function that can deal with strings instead
                        //shipToReposition.keyEvent(theChosenOne.getKeyStroke());
                        AutoBumpDecorator ABD = findAutoBumpDecorator(shipToReposition);
                        ABD.newNonKeyEvent(theChosenOne);

                        closeMouseListener(finalMap, ml);
                        return;
                    }
                }catch(Exception exce){
                    removeVisuals(finalMap);
                    logToChat("caught an exception while resolving ship maneuver");
                    closeMouseListener(finalMap, ml);
                    return;
                }

                if(slightMisclick) return; //misclick outside of a dot, but inside the ship shapes, do nothing, don't dismiss the GUI
                else{ //was not in any dot, any ship area, close the whole thing down
                    removeVisuals(finalMap);
                    Command stopItAll = stopTripleChoiceMakeNextReady();
                    String stoppingPlayerName = getCurrentPlayer().getName();
                    if(stopItAll!=null) stopItAll.execute();
                    logToChat("*-- " + stoppingPlayerName + " is cancelling the Tallon Roll.");

                    closeMouseListener(finalMap, ml);
                    return;
                }
            }

            public void mouseClicked(MouseEvent e) { }

            public void mouseReleased(MouseEvent e) { }

            public void mouseEntered(MouseEvent e) { }

            public void mouseExited(MouseEvent e) { }
        };
        theMap.addLocalMouseListenerFirst(ml);

        return nbOfRedDots;
    }


    //used at the end of triple choice sequence of the Tallon Roll. Lots less to deal with
    public Command newNonKeyEvent(RepositionChoiceVisual choice){
        previousCollisionVisualization = new MapVisualizations();

            //find out if the centered tallon roll was chosen in triplechoice and if so, flip a flag up
            if((int)choice.getPathPart().getX() == (int)lastCenteredTRX && (int)choice.getPathPart().getY() == (int)lastCenteredTRY) lastTRWasNotCentered = false;
            else lastTRWasNotCentered = true;
            Command repoCommand = buildTranslateCommand(choice.getPathPart(), choice.getTallonRollExtraAngle());


            //Command repoCommand = repositionTheShip(repoShip ,true); //only exists for 2.0 so is2pointohShip is true
            if(repoCommand == null) return null; //somehow did not get a programmed reposition command
            else{
                repoCommand.append(logToChatCommand("*** " + this.getProperty("Pilot Name").toString() +
                        " has moved" + " with " + choice.inStringForm));

                if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                    repoCommand.append(previousCollisionVisualization);
                }


                //These lines fetch the Shape of the last movement template used
                FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(this), FreeRotator.class));
                Shape lastMoveShapeUsed = this.lastManeuver.getTransformedTemplateShape(this.getPosition().getX(),
                        this.getPosition().getY(),
                        whichSizeShip(this),
                        rotator);

                //don't check for collisions in windows other than the main map
                if(!"Contested Sector".equals(getMap().getMapName())) return repoCommand;

                List<BumpableWithShape> otherBumpableShapes = getBumpablesWithShapes();
                //Check for template shape overlap with mines, asteroids, debris
                checkTemplateOverlap(lastMoveShapeUsed, otherBumpableShapes);
                //Check for ship bumping other ships, mines, asteroids, debris
                announceBumpAndPaint(otherBumpableShapes);
                //Check if a ship becomes out of bounds
                checkIfOutOfBounds( this.getProperty("Pilot Name").toString());

                //Add all the detected overlapping shapes to the map drawn components here
                if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                    repoCommand.append(this.previousCollisionVisualization);
                    this.previousCollisionVisualization.execute();
                }

                repoCommand.execute();
                GameModule.getGameModule().sendAndLog(repoCommand);
                return null;
            }
    }

    public static AutoBumpDecorator findAutoBumpDecorator(GamePiece activatedPiece) {
        return (AutoBumpDecorator)AutoBumpDecorator.getDecorator(activatedPiece,AutoBumpDecorator.class);
    }

    private static class ShipPositionState {
        double x;
        double y;
        double angle;
    }
}