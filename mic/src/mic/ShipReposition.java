package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.MoveTracker;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import javafx.scene.transform.Affine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static mic.Util.*;
import static mic.Util.getBumpableCompareShape;

//import VASSAL.command.ChangeTracker;

/**
 * Created by Mic on 07/08/2017.
 * This class must deal with Barell Rolls (straight 1, bank 1, straight 2) as well as decloaks (straight 2, bank 2)
 * It has to place templates mid way to the sides and detect an overlap with an obstacle right away
 *
 * Long term: offer a mouse driven slide and click interface with valid position checking
 */


enum RepoManeuver {
    //Section for when you only want to place a template on the side of the start position of a repositioning
    //small normal BR
    BR1_Left_Mid("Place Mid BR Left", "524", -90.0f, -113.0f, 0.0f),
    BR1_Right_Mid("Place Mid BR Right", "524", -90.0f, 113.0f, 0.0f),
    //small decloak/zeta ace
    BR2_Left_Mid("Place Mid BR2/Decloak Left", "525", -90.0f, -169.5f, 0.0f),
    BR2_Right_Mid("Place Mid BR2/Decloak Right", "525", -90.0f, 169.5f, 0.0f),
    //large normal BR
    BR1_Left_Mid_Large("Place Mid BR Left", "524", 0.0f, -141.5f, 0.0f),
    BR1_Right_Mid_Large("Place Mid BR Right", "524", 0.0f, 141.5f, 0.0f),
    //small Lorrir/SVmk2 BR
    BR_Bk1_Left_Fwd_Mid("Place Mid BR Bank 1 Left Fwd", "517", 135.0f, -133.0f, -22.0f),
    BR_Bk1_Left_Bwd_Mid("Place Mid BR Bank 1 Left Bwd", "517", -90.0f, -146.0f, 30.0f),
    BR_Bk1_Right_Fwd_Mid("Place Mid BR Bank 1 Right Fwd", "517", 90.0f, 147.0f, -28.0f),
    BR_Bk1_Right_Bwd_Mid("Place Mid BR Bank 1 Right Bwd", "517", -45.0f, 134.0f, 25.0f),
    //small echo decloaks
    BR_Bk2_Left_Fwd_Mid("Place Mid Echo Decloak Left Fwd", "519", 135.0f, -183.0f, -45.0f),
    BR_Bk2_Left_Bwd_Mid("Place Mid Echo Decloak Left Bwd", "519", -90.0f, -196.0f, 50.0f),
    BR_Bk2_Right_Fwd_Mid("Place Mid Echo Decloak Right Fwd", "519", 90.0f, 196.0f, -49.0f),
    BR_Bk2_Right_Bwd_Mid("Place Mid Echo Decloak Right Bwd", "519", -45.0f, 183.0f, 43.0f),

    //Section for when you want to move the whole ship to extreme forward or backward positions, code must both fetch the final position and the template used in case of an overlap event
    //small normal BR
    BR1_Left_AFAP("BR Left as Forward as Possible", "524", -90.0f, -113.0f, -28.25f, 0.0f, -226.0f, -56.5f),
    BR1_Left_ABAP("BR Left as Backward as Possible", "524", -90.0f, -113.0f, 28.25f, 0.0f, -226.0f, 56.5f),
    BR1_Right_AFAP("BR Right as Forward as Possible", "524", -90.0f, 113.0f, -28.25f, 0.0f, 226.0f, -56.5f),
    BR1_Right_ABAP("BR Right as Backward as Possible", "524", -90.0f, 113.0f, 28.25f, 0.0f, 226.0f, 56.5f),
    //small Zeta Ace BR
    BR2_Left_AFAP("BR2 Left as Forward as Possible", "525", -90.0f, -169.5f, -28.25f, 0.0f, -339.0f, -56.5f),
    BR2_Left_ABAP("BR2 Left as Backward as Possible", "525", -90.0f, -169.5f, 28.25f, 0.0f, -339.0f, 56.5f),
    BR2_Right_AFAP("BR2 Right as Forward as Possible", "525", -90.0f, 169.5f, -28.25f, 0.0f, 339.0f, -56.5f),
    BR2_Right_ABAP("BR2 Right as Backward as Possible", "525", -90.0f, 169.5f, 28.25f, 0.0f, 339.0f, 56.5f),
    //large normal BR
    BR1_Left_AFAP_Large("BR Left as Forward as Possible", "524", 0.0f, -141.25f, -56.5f, 0.0f, -283.0f, -113.0f),
    BR1_Left_ABAP_Large("BR Left as Backward as Possible", "524", 0.0f, -141.25f, 56.5f, 0.0f, -283.0f, 113.0f),
    BR1_Right_AFAP_Large("BR Right as Forward as Possible", "524", 0.0f, 141.25f, -56.5f, 0.0f, 283.0f, -113.0f),
    BR1_Right_ABAP_Large("BR Right as Backward as Possible", "524", 0.0f, 141.25f, 56.5f, 0.0f, 283.0f, 113.0f),

    //Section for 2.0 style barrel roll. AFAP and ABAP will be limited by the back and front edges and will spawn the template for habit building
    BR1_Left_AFAP_2E("BR Left as Forward as Possible", "524", -90.0f, -113.0f, 0.0f, 0.0f, -226.0f, -28.25f),
    BR1_Left_2E("BR Left", "524", -90.0f, -113.0f, 0.0f, 0.0f, -226.0f, 0.0f),
    BR1_Left_ABAP_2E("BR Left as Backward as Possible", "524", -90.0f, -113.0f, 0.0f, 0.0f, -226.0f, 28.25f),

    BR1_Left_TripleChoices("","", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),

    BR1_Right_AFAP_2E("BR Right as Forward as Possible", "524", -90.0f, 113.0f, 0.0f, 0.0f, 226.0f, -28.25f),
    BR1_Right_2E("BR Right", "524", -90.0f, 113.0f, 0.0f, 0.0f, 226.0f, 0.0f),
    BR1_Right_ABAP_2E("BR Right as Backward as Possible", "524", -90.0f, 113.0f, 0.0f, 0.0f, 226.0f, 28.25f),
    //small Zeta Ace BR
    BR2_Left_AFAP_2E("BR2 Left as Forward as Possible", "525", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    BR2_Left_2E("BR2 Left", "525", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, 0.0f),
    BR2_Left_ABAP_2E("BR2 Left as Backward as Possible", "525", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, 28.25f),

    BR2_Right_AFAP_2E("BR2 Right as Forward as Possible", "525", -90.0f, 169.5f, 0.0f, 0.0f, 339.0f, -28.25f),
    BR2_Right_2E("BR2 Right", "525", -90.0f, 169.5f, 0.0f, 0.0f, 339.0f, 0.0f),
    BR2_Right_ABAP_2E("BR2 Right as Backward as Possible", "525", -90.0f, 169.5f, 0.0f, 0.0f, 339.0f, 28.25f),
    //medium normal BR
    BR1_Left_AFAP_Medium_2E("BR Left as Forward as Possible", "524", 0.0f, -113.75f, 0.0f, 0.0f, -227.5f, -56.5f),
    BR1_Left_Medium_2E("BR Left", "524", 0.0f, -113.75f, 0.0f, 0.0f, -227.5f, 0.0f),
    BR1_Left_ABAP_Medium_2E("BR Left as Backward as Possible", "524", 0.0f, -113.75f, 0.0f, 0.0f, -227.5f, 56.5f),

    BR1_Right_AFAP_Medium_2E("BR Right as Forward as Possible", "524", 0.0f, 113.75f, 0.0f, 0.0f, 227.5f, -56.5f),
    BR1_Right_Medium_2E("BR Right", "524", 0.0f, 113.75f, 0.0f, 0.0f, 227.5f, 0.0f),
    BR1_Right_ABAP_Medium_2E("BR Right as Backward as Possible", "524", 0.0f, 113.75f, 0.0f, 0.0f, 227.5f, 56.5f),
    //medium decloak using a standard long barrel roll 1
    BRD_Left_AFAP_Medium_2E("Decloak Left as Forward as Possible", "524",0.0f, -113.75f, 0.0f, 0.0f, -227.5f, -56.5f),
    BRD_Left_Medium_2E("Decloak Left", "524", -90.0f, -142.0f, 0.0f, 0.0f, -284.0f, 0.0f),
    BRD_Left_ABAP_Medium_2E("Decloak Left as Backward as Possible", "524", 0.0f, -113.75f, 0.0f, 0.0f, -227.5f, 56.5f),

    BRD_Right_AFAP_Medium_2E("Decloak Right as Forward as Possible", "524", 0.0f, 113.75f, 0.0f, 0.0f, 227.5f, -56.5f),
    BRD_Right_Medium_2E("Decloak Right", "524", -90.0f, 142.0f, 0.0f, 0.0f, 284.0f, 0.0f),
    BRD_Right_ABAP_Medium_2E("Decloak Right as Backward as Possible", "524", 0.0f, 113.75f, 0.0f, 0.0f, 227.5f, 56.5f),
    //large normal BR
    BR1_Left_AFAP_Large_2E("BR Left as Forward as Possible", "524", 0.0f, -141.25f, 0.0f, 0.0f, -283.0f, -56.5f),
    BR1_Left_Large_2E("BR Left as Forward as Possible", "524", 0.0f, -141.25f, 0.0f, 0.0f, -283.0f, 0.0f),
    BR1_Left_ABAP_Large_2E("BR Left as Backward as Possible", "524", 0.0f, -141.25f, 0.0f, 0.0f, -283.0f, 56.5f),

    BR1_Right_AFAP_Large_2E("BR Right as Forward as Possible", "524", 0.0f, 141.25f, 0.0f, 0.0f, 283.0f, -56.5f),
    BR1_Right_Large_2E("BR Right", "524", 0.0f, 141.25f, 0.0f, 0.0f, 283.0f, 0.0f),
    BR1_Right_ABAP_Large_2E("BR Right as Backward as Possible", "524", 0.0f, 141.25f, 0.0f, 0.0f, 283.0f, 56.5f),
    //large decloak using a standard long barrel roll 1
    BRD_Left_AFAP_Large_2E("BR Left as Forward as Possible", "524", 0.0f, -141.25f, 0.0f, 0.0f, -283.0f, -56.5f),
    BRD_Left_Large_2E("BR Left as Forward as Possible", "524", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, 0.0f),
    BRD_Left_ABAP_Large_2E("BR Left as Backward as Possible", "524", 0.0f, -141.25f, 0.0f, 0.0f, -283.0f, 56.5f),

    BRD_Right_AFAP_Large_2E("BR Right as Forward as Possible", "524", 0.0f, 141.25f, 0.0f, 0.0f, 283.0f, -56.5f),
    BRD_Right_Large_2E("BR Right", "524", -90.0f, 169.5f, 0.0f, 0.0f, 339.0f, 0.0f),
    BRD_Right_ABAP_Large_2E("BR Right as Backward as Possible", "524", 0.0f, 141.25f, 0.0f, 0.0f, 283.0f, 56.5f)
    ;

    private final String repoName;
    private final String gpID;
    private final double templateAngle;
    private final double offsetX;
    private final double offsetY;

    private final double shipAngle;
    private final double shipX;
    private final double shipY;

    RepoManeuver(String repoName,  String gpID, double templateAngle,
                 double offsetX, double offsetY)
    {
        this.repoName = repoName;
        this.gpID = gpID;
        this.templateAngle = templateAngle;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.shipAngle = 0.0f;
        this.shipX = 0.0f;
        this.shipY = 0.0f;
    }

    RepoManeuver(String repoName,  String gpID, double templateAngle,
                 double offsetX, double offsetY,
                 double shipAngle, double shipX, double shipY)

    {
        this.repoName = repoName;
        this.gpID = gpID;
        this.templateAngle = templateAngle;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.shipAngle = shipAngle;
        this.shipX = shipX;
        this.shipY = shipY;

    }

    public String getRepoName() { return this.repoName; }
    public String getTemplateGpID() { return this.gpID; }
    public double getTemplateAngle() { return this.templateAngle; }
    public double getOffsetX() { return this.offsetX; }
    public double getOffsetY() { return this.offsetY; }

    public double getShipAngle() { return this.shipAngle; }
    public double getShipX() { return this.shipX; }
    public double getShipY() { return this.shipY; }
}

public class ShipReposition extends Decorator implements EditablePiece {
    private static final Logger logger = LoggerFactory.getLogger(ShipReposition.class);

    public static float DOT_DIAMETER = 30.0f;
    public static float DOT_FUDGE = 30.0f;

    public static final String ID = "ShipReposition";
    private FreeRotator myRotator = null;
    public MapVisualizations previousCollisionVisualization = null;

    private final FreeRotator testRotator;
    private Shape shapeForOverlap;
    static final int NBFLASHES = 5; //use the same flash functionality if a mine is spawned on a ship
    static final int DELAYBETWEENFLASHES = 150;

    MouseListener ml;
    Boolean dealingWithClickChoicesDuringReposition = false;
    List<repositionChoiceVisual> rpcList = Lists.newArrayList();

    private static Map<String, RepoManeuver> keyStrokeToDropTemplate = ImmutableMap.<String, RepoManeuver>builder()
            .put("CTRL R", RepoManeuver.BR1_Left_Mid)
            .put("ALT R", RepoManeuver.BR1_Right_Mid)
            .put("J", RepoManeuver.BR2_Left_Mid)
            .put("K", RepoManeuver.BR2_Right_Mid)
            .put("CTRL J", RepoManeuver.BR_Bk2_Left_Fwd_Mid)
            .put("CTRL K", RepoManeuver.BR_Bk2_Right_Fwd_Mid)
            .put("CTRL SHIFT J", RepoManeuver.BR_Bk2_Left_Bwd_Mid)
            .put("CTRL SHIFT K", RepoManeuver.BR_Bk2_Right_Bwd_Mid)
            .put("ALT J", RepoManeuver.BR_Bk1_Left_Fwd_Mid)
            .put("ALT SHIFT J", RepoManeuver.BR_Bk1_Left_Bwd_Mid)
            .put("ALT K", RepoManeuver.BR_Bk1_Right_Fwd_Mid)
            .put("ALT SHIFT K", RepoManeuver.BR_Bk1_Right_Bwd_Mid)
            .build();

    //same stuff, but for 2nd edition. Echo style and SV Mk.2 style present.
    private static Map<String, RepoManeuver> keyStrokeToDropTemplate2e = ImmutableMap.<String, RepoManeuver>builder()
            .put("CTRL J", RepoManeuver.BR_Bk2_Left_Fwd_Mid)
            .put("CTRL K", RepoManeuver.BR_Bk2_Right_Fwd_Mid)
            .put("CTRL SHIFT J", RepoManeuver.BR_Bk2_Left_Bwd_Mid)
            .put("CTRL SHIFT K", RepoManeuver.BR_Bk2_Right_Bwd_Mid)
            .put("ALT J", RepoManeuver.BR_Bk1_Left_Fwd_Mid)
            .put("ALT SHIFT J", RepoManeuver.BR_Bk1_Left_Bwd_Mid)
            .put("ALT K", RepoManeuver.BR_Bk1_Right_Fwd_Mid)
            .put("ALT SHIFT K", RepoManeuver.BR_Bk1_Right_Bwd_Mid)
            .build();

    private static Map<String, RepoManeuver> keyStrokeToRepositionShip = ImmutableMap.<String, RepoManeuver>builder()
            .put("CTRL 8", RepoManeuver.BR1_Left_AFAP)
            .put("CTRL SHIFT 8", RepoManeuver.BR1_Left_ABAP)
            .put("ALT 8", RepoManeuver.BR1_Right_AFAP)
            .put("ALT SHIFT 8", RepoManeuver.BR1_Right_ABAP)
            .put("CTRL 9", RepoManeuver.BR2_Left_AFAP)
            .put("CTRL SHIFT 9", RepoManeuver.BR2_Left_ABAP)
            .put("ALT 9", RepoManeuver.BR2_Right_AFAP)
            .put("ALT SHIFT 9", RepoManeuver.BR2_Right_ABAP)
            .build();

    //Map for 2e keystrokes
    private static Map<String, RepoManeuver> keyStrokeToRepositionShip_2e = ImmutableMap.<String, RepoManeuver>builder()
            .put("CTRL 8", RepoManeuver.BR1_Left_AFAP_2E)
            .put("CTRL R", RepoManeuver.BR1_Left_2E)
            .put("CTRL SHIFT 8", RepoManeuver.BR1_Left_ABAP_2E)
            .put("ALT CTRL SHIFT R", RepoManeuver.BR1_Left_TripleChoices)
            .put("ALT 8", RepoManeuver.BR1_Right_AFAP_2E)
            .put("ALT R", RepoManeuver.BR1_Right_2E)
            .put("ALT SHIFT 8", RepoManeuver.BR1_Right_ABAP_2E)
            .put("CTRL 9", RepoManeuver.BR2_Left_AFAP_2E)
            .put("J", RepoManeuver.BR2_Left_2E)
            .put("CTRL SHIFT 9", RepoManeuver.BR2_Left_ABAP_2E)
            .put("ALT 9", RepoManeuver.BR2_Right_AFAP_2E)
            .put("K", RepoManeuver.BR2_Right_2E)
            .put("ALT SHIFT 9", RepoManeuver.BR2_Right_ABAP_2E)
            .build();

    //Names of the reposition
    private static Map<String, String> keyStrokeToName_2e = ImmutableMap.<String, String>builder()
            .put("CTRL 8", "Left Barrel Roll as Forward as Possible")
            .put("CTRL R", "Left Barrel Roll, centered")
            .put("CTRL SHIFT 8", "Left Barrel Roll as Backward as Possible")
            .put("ALT 8", "Right Barrel Roll as Forward as Possible")
            .put("ALT R", "Right Barrel Roll, centered")
            .put("ALT SHIFT 8", "Left Barrel Roll as Backward as Possible")
            .put("CTRL 9", "Left Straight Decloak as Forward as Possible")
            .put("J", "Left Straight Decloak, centered")
            .put("CTRL SHIFT 9", "Left Straight Decloak as Backward as Possible")
            .put("ALT 9", "Right Straight Decloak as Forward as Possible")
            .put("K", "Right Straight Decloak, centered")
            .put("ALT SHIFT 9", "Right Straight Decloak as Backward as Possible")
            .build();

    //Get back a Keystroke object
    private static Map<RepoManeuver, KeyStroke> repoShipToKeyStroke_2e = ImmutableMap.<RepoManeuver, KeyStroke>builder()
            .put(RepoManeuver.BR1_Left_AFAP_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Left_2E, KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Left_ABAP_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))
            .build();


    public ShipReposition() {
        this(null);
    }

    public ShipReposition(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
    }

    private Command spawnRepoTemplate(RepoManeuver theManeu) {
        //Prep step, check if it's a large ship, and only deal with regular barrel rolls, because it's all they can do anyway, rerouting to the correct RepoManeuver
        int size = whichSizeShip(this, false);

        if(size==3)
        {
            switch(theManeu){
                case BR1_Left_Mid:
                    theManeu = RepoManeuver.BR1_Left_Mid_Large;
                    break;
                case BR1_Right_Mid:
                    theManeu = RepoManeuver.BR1_Right_Mid_Large;
                    break;
                default:
                    return null;
            }
        }

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

        //Info Gathering: Offset 1, put to the side of the ship, local coords, adjusting for large base if it is found
        double off1x = theManeu.getOffsetX();
        double off1y = theManeu.getOffsetY();

        //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
        double off2x = this.getPosition().getX();
        double off2y = this.getPosition().getY();

        //STEP 3: rotate the offset1 dependant within the spawner's local coordinates
        double off1x_rot = rotX(off1x, off1y, sAngle);
        double off1y_rot = rotY(off1x, off1y, sAngle);

        //STEP 4: translation into place
        Command placeCommand = getMap().placeOrMerge(piece, new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y));

        shapeForOverlap = AffineTransform.
                getTranslateInstance((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y).
                createTransformedShape(shapeForOverlap);
        double roundedAngle = convertAngleToGameLimits(sAngle - tAngle);
        shapeForOverlap = AffineTransform
                .getRotateInstance(Math.toRadians(-roundedAngle), (int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y)
                .createTransformedShape(shapeForOverlap);

        return placeCommand;
    }

    private RepoManeuver swapToRepoManeuverIfMedOrLarge(RepoManeuver repoTemplate, int size, boolean is2pointOh){
        if(size == 2){
            switch(repoTemplate){
                case BR1_Left_AFAP_2E:
                    repoTemplate = RepoManeuver.BR1_Left_AFAP_Medium_2E;
                    break;
                case BR1_Left_2E:
                    repoTemplate = RepoManeuver.BR1_Left_Medium_2E;
                    break;
                case BR1_Left_ABAP_2E:
                    repoTemplate = RepoManeuver.BR1_Left_ABAP_Medium_2E;
                    break;
                case BR1_Right_AFAP_2E:
                    repoTemplate = RepoManeuver.BR1_Right_AFAP_Medium_2E;
                    break;
                case BR1_Right_2E:
                    repoTemplate = RepoManeuver.BR1_Right_Medium_2E;
                    break;
                case BR1_Right_ABAP_2E:
                    repoTemplate = RepoManeuver.BR1_Right_ABAP_Medium_2E;
                    break;
                case BR2_Right_AFAP_2E:
                    repoTemplate = RepoManeuver.BRD_Right_AFAP_Medium_2E;
                    break;
                case BR2_Right_2E:
                    repoTemplate = RepoManeuver.BRD_Right_Medium_2E;
                    break;
                case BR2_Right_ABAP_2E:
                    repoTemplate = RepoManeuver.BRD_Right_ABAP_Medium_2E;
                    break;
                case BR2_Left_AFAP_2E:
                    repoTemplate = RepoManeuver.BRD_Left_AFAP_Medium_2E;
                    break;
                case BR2_Left_2E:
                    repoTemplate = RepoManeuver.BRD_Left_Medium_2E;
                    break;
                case BR2_Left_ABAP_2E:
                    repoTemplate = RepoManeuver.BRD_Left_ABAP_Medium_2E;
                    break;
                default:
                    return null;
            }
        } //end of dealing with medium
        //Prep step, check if it's a large ship, and only deal with regular barrel rolls, because it's all they can do anyway, rerouting to the correct RepoManeuver
        if(size == 3 && is2pointOh == true) {
            switch (repoTemplate) {
                case BR1_Left_AFAP_2E:
                    repoTemplate = RepoManeuver.BR1_Left_AFAP_Large_2E;
                    break;
                case BR1_Left_2E:
                    repoTemplate = RepoManeuver.BR1_Left_Large_2E;
                    break;
                case BR1_Left_ABAP_2E:
                    repoTemplate = RepoManeuver.BR1_Left_ABAP_Large_2E;
                    break;
                case BR1_Right_AFAP_2E:
                    repoTemplate = RepoManeuver.BR1_Right_AFAP_Large_2E;
                    break;
                case BR1_Right_2E:
                    repoTemplate = RepoManeuver.BR1_Right_Large_2E;
                    break;
                case BR1_Right_ABAP_2E:
                    repoTemplate = RepoManeuver.BR1_Right_ABAP_Large_2E;
                    break;
                case BR2_Right_AFAP_2E:
                    repoTemplate = RepoManeuver.BRD_Right_AFAP_Large_2E;
                    break;
                case BR2_Right_2E:
                    repoTemplate = RepoManeuver.BRD_Right_Large_2E;
                    break;
                case BR2_Right_ABAP_2E:
                    repoTemplate = RepoManeuver.BRD_Right_ABAP_Large_2E;
                    break;
                case BR2_Left_AFAP_2E:
                    repoTemplate = RepoManeuver.BRD_Left_AFAP_Large_2E;
                    break;
                case BR2_Left_2E:
                    repoTemplate =  RepoManeuver.BRD_Left_Large_2E;
                    break;
                case BR2_Left_ABAP_2E:
                    repoTemplate = RepoManeuver.BRD_Left_ABAP_Large_2E;
                    break;
                default:
                    return null;
            }
        } //end of dealing with 2.0 large
        if(size == 3 && is2pointOh == false)
        {
            switch(repoTemplate){
                case BR1_Left_AFAP:
                    repoTemplate = RepoManeuver.BR1_Left_AFAP_Large;
                    break;
                case BR1_Left_ABAP:
                    repoTemplate = RepoManeuver.BR1_Left_ABAP_Large;
                    break;
                case BR1_Right_AFAP:
                    repoTemplate = RepoManeuver.BR1_Right_AFAP_Large;
                    break;
                case BR1_Right_ABAP:
                    repoTemplate = RepoManeuver.BR1_Right_ABAP_Large;
                    break;
                default:
                    return null;
            }
        } //end of dealing with 1.0 large
        return repoTemplate;
    }

    private void offerTripleChoices(List<RepoManeuver> repoTemplates, boolean is2pointOh, VASSAL.build.module.Map theMap) {
        //Getting into this function, repoShip is associated with the template used to reposition the ship. We also need the non-mapped final ship tentative position
        Boolean spawnTemplate = false;

        int size = whichSizeShip(this, is2pointOh);

        removeVisuals(theMap);

        int wideningFudgeFactorBetweenDots = -1; //will go from -1 to 0 to 1 in the following loop
        for(RepoManeuver repoTemplate : repoTemplates) {
//Prep step, check if it's a medium ship, and only deal with regular barrel rolls, because it's all they can do anyway, rerouting to the correct RepoManeuver
            repoTemplate = swapToRepoManeuverIfMedOrLarge(repoTemplate, size, is2pointOh);

            //STEP 1: Collision reposition template, centered as in in the image file, centered on 0,0 (upper left corner)
            GamePiece piece = newPiece(findPieceSlotByID(repoTemplate.getTemplateGpID()));
            shapeForOverlap = piece.getShape();

            //Info Gathering: gets the angle from repoTemplate which deals with degrees, local space with ship at 0,0, pointing up
            double tAngle;
            tAngle = repoTemplate.getTemplateAngle(); //repo maneuver's angle
            double sAngle = this.getRotator().getAngle(); //ship angle

            //STEP 2: rotate the reposition template with both angles
            FreeRotator fR = (FreeRotator) Decorator.getDecorator(piece, FreeRotator.class);
            fR.setAngle(sAngle - tAngle);

            //Info Gathering: Offset 1, put to the side of the ship, local coords, adjusting for large base if it is found
            double off1x = repoTemplate.getOffsetX();
            double off1y = repoTemplate.getOffsetY();


            //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
            double off2x = this.getPosition().getX();
            double off2y = this.getPosition().getY();

            //STEP 3: rotate the offset1 dependant within the spawner's local coordinates
            double off1x_rot = rotX(off1x, off1y, sAngle);
            double off1y_rot = rotY(off1x, off1y, sAngle);

            //STEP 4: translation into place
            shapeForOverlap = AffineTransform.
                    getTranslateInstance((int) off1x_rot + (int) off2x, (int) off1y_rot + (int) off2y).
                    createTransformedShape(shapeForOverlap);
            double roundedAngle = convertAngleToGameLimits(sAngle - tAngle);
            shapeForOverlap = AffineTransform
                    .getRotateInstance(Math.toRadians(-roundedAngle), (int) off1x_rot + (int) off2x, (int) off1y_rot + (int) off2y)
                    .createTransformedShape(shapeForOverlap);

            //STEP 5: Check for overlap with an obstacle, if so, spawn it so the player sees it
            //Command bigCommand = null;

            List<BumpableWithShape> obstacles = getBumpablesOnMap(false);

            if (shapeForOverlap != null) {
                List<BumpableWithShape> overlappingObstacles = findCollidingEntities(shapeForOverlap, obstacles);
                if (overlappingObstacles.size() > 0) {
                    for (BumpableWithShape bws : overlappingObstacles) {
                        previousCollisionVisualization.add(bws.shape);
                        String overlapWarn = "*** Warning: reposition template currently overlaps an obstacle. You can attempt to move it into a legal position and check if it still overlaps with 'c'.";
                        //if(bigCommand != null) bigCommand.append(logToChatCommand(overlapWarn));
                        //else bigCommand = logToChatCommand(overlapWarn);
                    }
                    previousCollisionVisualization.add(shapeForOverlap);
                    spawnTemplate = true; //we'll want the template
                }
            }
            //STEP 6: Gather info for ship's final wanted position

            // spawn a copy of the ship without the actions
            //Shape shapeForOverlap2 = getCopyOfShapeWithoutActionsForOverlapCheck(this.piece,repoTemplate );
            Shape shapeForOverlap2 = Decorator.getDecorator(Decorator.getOutermost(this), NonRectangular.class).getShape();

            float diam = DOT_DIAMETER + (size - 1) * DOT_FUDGE * 0.666f;
            Shape dot = new Ellipse2D.Float(-diam / 2, -diam / 2, diam, diam);

            //Info Gathering: gets the angle from RepoManeuver which deals with degrees, local space with ship at 0,0, pointing up
            double tAngle2;
            tAngle2 = repoTemplate.getShipAngle(); //repo maneuver's angle

            //Info Gathering: Offset 1, put to the side of the ship, local coords, adjusting for large base if it is found
            double off1x_s = repoTemplate.getShipX();
            double off1y_s = repoTemplate.getShipY();
            double off1y_s_dot = off1y_s + wideningFudgeFactorBetweenDots * size * 0.666f * DOT_FUDGE;

            //STEP 7: rotate the offset1 dependant within the spawner's local coordinates
            double off1x_rot_s = rotX(off1x_s, off1y_s, sAngle);
            double off1y_rot_s = rotY(off1x_s, off1y_s, sAngle);

            double off1x_rot_s_dot = rotX(off1x_s, off1y_s_dot, sAngle);
            double off1y_rot_s_dot = rotY(off1x_s, off1y_s_dot, sAngle);

            //STEP 8: translation into place
            shapeForOverlap2 = AffineTransform.
                    getTranslateInstance((int) off1x_rot_s + (int) off2x, (int) off1y_rot_s + (int) off2y).
                    createTransformedShape(shapeForOverlap2);
            double roundedAngle2 = convertAngleToGameLimits(tAngle2);
            shapeForOverlap2 = AffineTransform
                    .getRotateInstance(Math.toRadians(-roundedAngle2), (int) off1x_rot_s + (int) off2x, (int) off1y_rot_s + (int) off2y)
                    .createTransformedShape(shapeForOverlap2);

            dot = AffineTransform.
                    getTranslateInstance((int) off1x_rot_s_dot + (int) off2x, (int) off1y_rot_s_dot + (int) off2y).
                    createTransformedShape(dot);
            dot = AffineTransform
                    .getRotateInstance(Math.toRadians(-roundedAngle2), (int) off1x_rot_s_dot + (int) off2x, (int) off1y_rot_s_dot + (int) off2y)
                    .createTransformedShape(dot);

            //STEP 9: Check for overlap with obstacles and ships with the final ship position
            List<BumpableWithShape> shipsOrObstacles = getBumpablesOnMap(true);
            boolean wantOverlapColor = false;

            String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
            if (shapeForOverlap2 != null) {

                List<BumpableWithShape> overlappingShipOrObstacles = findCollidingEntities(shapeForOverlap2, shipsOrObstacles);

                if (overlappingShipOrObstacles.size() > 0) {
                    for (BumpableWithShape bws : overlappingShipOrObstacles) {
                        //TODO add repositionthing in red
                        wantOverlapColor = true;

                        //if(bigCommand !=null) bigCommand.append(logToChatCommand(overlapOnFinalWarn));
                        //else bigCommand = logToChatCommand(overlapOnFinalWarn);
                    }
                    //TODO add repositionthing in red

                }
            }

            // STEP 9.5: Check for movement out of bounds
            boolean outsideCheck = checkIfOutOfBounds(yourShipName, shapeForOverlap2);
            if (outsideCheck) wantOverlapColor = true;

            //STEP 10: optional if there's any kind of overlap, produce both the template and initial ship position
            if (spawnTemplate == true) {
                //the template is needed, in case of any kind of overlap
                //if(bigCommand !=null) bigCommand.append(getMap().placeOrMerge(piece, new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y)));
                //else bigCommand = getMap().placeOrMerge(piece, new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y));
                //clone the initial position
                //GamePiece myClone = PieceCloner.getInstance().clonePiece(Decorator.getOutermost(this));
                //bigCommand.append(getMap().placeOrMerge(myClone, new Point((int)off2x, (int)off2y)));
            }
            //STEP 11: reposition the ship
            //Add visuals according to the selection of repositioning

            repositionChoiceVisual rpc = new repositionChoiceVisual(shapeForOverlap2, dot, wantOverlapColor,repoShipToKeyStroke_2e.get(repoTemplate));
            rpcList.add(rpc);

            //return bigCommand;
            wideningFudgeFactorBetweenDots++;
        } // end of loop around the 3 templates

        //FINAL STEP: add the visuala to the map and the mouse listener
        for(repositionChoiceVisual r : rpcList){
            theMap.addDrawComponent(r);
        }
        final VASSAL.build.module.Map finalMap = theMap;
        ml = new MouseListener() {
            int i=0;
            public void mouseClicked(MouseEvent e) {
                if(e.isControlDown()) return;

                List<repositionChoiceVisual> copiedList = Lists.newArrayList();
                for(repositionChoiceVisual r : rpcList){
                    copiedList.add(r);
                }
                //When it gets the answer, gracefully close the mouse listenener and remove the visuals
                repositionChoiceVisual theChosenOne = null;
                boolean slightMisclick = false;

                for(repositionChoiceVisual r : copiedList){
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
                        //TODO move the ship according to the choice or send shortcut, I dunno
                        removeVisuals(finalMap);
                        stopTripleChoiceMakeNextReady();
                        Command doKey = piece.keyEvent(theChosenOne.getKeyStroke());
                        doKey.execute();
                        GameModule.getGameModule().sendAndLog(doKey);
                        closeMouseListener(finalMap, ml);
                    }
                }catch(Exception exce){

                }
                if(slightMisclick) return;
                else{ //was not in any dot, any ship area, close the whole thing down
                    removeVisuals(finalMap);
                    stopTripleChoiceMakeNextReady();
                    logToChat("Cancel the barrel roll");
                    closeMouseListener(finalMap, ml);
                    return;
                }
            }

            public void mousePressed(MouseEvent e) {

            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {
                for(repositionChoiceVisual rpc : rpcList){
                    if(rpc.theDot.contains(e.getPoint())){
                        rpc.setMouseOvered();
                        logToChat("$*/*$(*%($/%*($%*/*($%*/");
                    }
                }
            }

            public void mouseExited(MouseEvent e) {
                for(repositionChoiceVisual rpc : rpcList){
                    if(!rpc.theDot.contains(e.getPoint())){
                        rpc.unsetMouseOvered();
                    }
                }
            }
        };
        theMap.addLocalMouseListenerFirst(ml);
    }

    public static String getKeyStrokeFromRepoManeuver(RepoManeuver rp){
        for (Map.Entry<String, RepoManeuver> entry : keyStrokeToRepositionShip_2e.entrySet()) {
            if (entry.getValue()==rp) return entry.getKey();
        }
        return null;
    }


    private Command repositionTheShip(RepoManeuver repoTemplate, boolean is2pointOh) {
        //Getting into this function, repoShip is associated with the template used to reposition the ship. We also need the non-mapped final ship tentative position
        Boolean spawnTemplate = false;

        int size = whichSizeShip(this, is2pointOh);
        //Prep step, check if it's a medium ship, and only deal with regular barrel rolls, because it's all they can do anyway, rerouting to the correct RepoManeuver
        if(size == 2){
            switch(repoTemplate){
                case BR1_Left_AFAP_2E:
                    repoTemplate = RepoManeuver.BR1_Left_AFAP_Medium_2E;
                    break;
                case BR1_Left_2E:
                    repoTemplate = RepoManeuver.BR1_Left_Medium_2E;
                    break;
                case BR1_Left_ABAP_2E:
                    repoTemplate = RepoManeuver.BR1_Left_ABAP_Medium_2E;
                    break;
                case BR1_Right_AFAP_2E:
                    repoTemplate = RepoManeuver.BR1_Right_AFAP_Medium_2E;
                    break;
                case BR1_Right_2E:
                    repoTemplate = RepoManeuver.BR1_Right_Medium_2E;
                    break;
                case BR1_Right_ABAP_2E:
                    repoTemplate = RepoManeuver.BR1_Right_ABAP_Medium_2E;
                    break;
                case BR2_Right_AFAP_2E:
                    repoTemplate = RepoManeuver.BRD_Right_AFAP_Medium_2E;
                    break;
                case BR2_Right_2E:
                    repoTemplate = RepoManeuver.BRD_Right_Medium_2E;
                    break;
                case BR2_Right_ABAP_2E:
                    repoTemplate = RepoManeuver.BRD_Right_ABAP_Medium_2E;
                    break;
                case BR2_Left_AFAP_2E:
                    repoTemplate = RepoManeuver.BRD_Left_AFAP_Medium_2E;
                    break;
                case BR2_Left_2E:
                    repoTemplate = RepoManeuver.BRD_Left_Medium_2E;
                    break;
                case BR2_Left_ABAP_2E:
                    repoTemplate = RepoManeuver.BRD_Left_ABAP_Medium_2E;
                    break;
                default:
                    return null;
            }
        }

        //Prep step, check if it's a large ship, and only deal with regular barrel rolls, because it's all they can do anyway, rerouting to the correct RepoManeuver
        if(size == 3 && is2pointOh == true) {
            switch (repoTemplate) {
                case BR1_Left_AFAP_2E:
                    repoTemplate = RepoManeuver.BR1_Left_AFAP_Large_2E;
                    break;
                case BR1_Left_2E:
                    repoTemplate = RepoManeuver.BR1_Left_Large_2E;
                    break;
                case BR1_Left_ABAP_2E:
                    repoTemplate = RepoManeuver.BR1_Left_ABAP_Large_2E;
                    break;
                case BR1_Right_AFAP_2E:
                    repoTemplate = RepoManeuver.BR1_Right_AFAP_Large_2E;
                    break;
                case BR1_Right_2E:
                    repoTemplate = RepoManeuver.BR1_Right_Large_2E;
                    break;
                case BR1_Right_ABAP_2E:
                    repoTemplate = RepoManeuver.BR1_Right_ABAP_Large_2E;
                    break;
                case BR2_Right_AFAP_2E:
                    repoTemplate = RepoManeuver.BRD_Right_AFAP_Large_2E;
                    break;
                case BR2_Right_2E:
                    repoTemplate = RepoManeuver.BRD_Right_Large_2E;
                    break;
                case BR2_Right_ABAP_2E:
                    repoTemplate = RepoManeuver.BRD_Right_ABAP_Large_2E;
                    break;
                case BR2_Left_AFAP_2E:
                    repoTemplate = RepoManeuver.BRD_Left_AFAP_Large_2E;
                    break;
                case BR2_Left_2E:
                    repoTemplate =  RepoManeuver.BRD_Left_Large_2E;
                    break;
                case BR2_Left_ABAP_2E:
                    repoTemplate = RepoManeuver.BRD_Left_ABAP_Large_2E;
                    break;
                default:
                    return null;
            }
        }

        if(size == 3 && is2pointOh == false)
        {
            switch(repoTemplate){
                case BR1_Left_AFAP:
                    repoTemplate = RepoManeuver.BR1_Left_AFAP_Large;
                    break;
                case BR1_Left_ABAP:
                    repoTemplate = RepoManeuver.BR1_Left_ABAP_Large;
                    break;
                case BR1_Right_AFAP:
                    repoTemplate = RepoManeuver.BR1_Right_AFAP_Large;
                    break;
                case BR1_Right_ABAP:
                    repoTemplate = RepoManeuver.BR1_Right_ABAP_Large;
                    break;
                default:
                    return null;
            }
        }

        //STEP 1: Collision reposition template, centered as in in the image file, centered on 0,0 (upper left corner)
        GamePiece piece = newPiece(findPieceSlotByID(repoTemplate.getTemplateGpID()));
        shapeForOverlap = piece.getShape();

        //Info Gathering: gets the angle from repoTemplate which deals with degrees, local space with ship at 0,0, pointing up
        double tAngle;
        tAngle = repoTemplate.getTemplateAngle(); //repo maneuver's angle
        double sAngle = this.getRotator().getAngle(); //ship angle
        //STEP 2: rotate the reposition template with both angles
        FreeRotator fR = (FreeRotator)Decorator.getDecorator(piece, FreeRotator.class);
        fR.setAngle(sAngle - tAngle);

        //Info Gathering: Offset 1, put to the side of the ship, local coords, adjusting for large base if it is found
        double off1x = repoTemplate.getOffsetX();
        double off1y = repoTemplate.getOffsetY();

        //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
        double off2x = this.getPosition().getX();
        double off2y = this.getPosition().getY();

        //STEP 3: rotate the offset1 dependant within the spawner's local coordinates
        double off1x_rot = rotX(off1x, off1y, sAngle);
        double off1y_rot = rotY(off1x, off1y, sAngle);

        //STEP 4: translation into place
        shapeForOverlap = AffineTransform.
                getTranslateInstance((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y).
                createTransformedShape(shapeForOverlap);
        double roundedAngle = convertAngleToGameLimits(sAngle - tAngle);
        shapeForOverlap = AffineTransform
                .getRotateInstance(Math.toRadians(-roundedAngle), (int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y)
                .createTransformedShape(shapeForOverlap);

        //STEP 5: Check for overlap with an obstacle, if so, spawn it so the player sees it
        Command bigCommand = null;

        List<BumpableWithShape> obstacles = getBumpablesOnMap(false);

        if(shapeForOverlap != null) {
            List<BumpableWithShape> overlappingObstacles = findCollidingEntities(shapeForOverlap, obstacles);
            if (overlappingObstacles.size() > 0) {
                for (BumpableWithShape bws : overlappingObstacles) {
                    previousCollisionVisualization.add(bws.shape);
                    String overlapWarn = "*** Warning: reposition template currently overlaps an obstacle. You can attempt to move it into a legal position and check if it still overlaps with 'c'.";
                    if(bigCommand != null) bigCommand.append(logToChatCommand(overlapWarn));
                    else bigCommand = logToChatCommand(overlapWarn);
                }
                previousCollisionVisualization.add(shapeForOverlap);
                spawnTemplate = true; //we'll want the template
            }
        }
        //STEP 6: Gather info for ship's final wanted position

        // spawn a copy of the ship without the actions
        //Shape shapeForOverlap2 = getCopyOfShapeWithoutActionsForOverlapCheck(this.piece,repoTemplate );
        Shape shapeForOverlap2 = Decorator.getDecorator(Decorator.getOutermost(this), NonRectangular.class).getShape();

        //Info Gathering: gets the angle from RepoManeuver which deals with degrees, local space with ship at 0,0, pointing up
        double tAngle2;
        tAngle2 = repoTemplate.getShipAngle(); //repo maneuver's angle

        //Info Gathering: Offset 1, put to the side of the ship, local coords, adjusting for large base if it is found
        double off1x_s = repoTemplate.getShipX();
        double off1y_s = repoTemplate.getShipY();

        //STEP 7: rotate the offset1 dependant within the spawner's local coordinates
        double off1x_rot_s = rotX(off1x_s, off1y_s, sAngle);
        double off1y_rot_s = rotY(off1x_s, off1y_s, sAngle);

        //STEP 8: translation into place
        shapeForOverlap2 = AffineTransform.
                getTranslateInstance((int)off1x_rot_s + (int)off2x, (int)off1y_rot_s + (int)off2y).
                createTransformedShape(shapeForOverlap2);
        double roundedAngle2 = convertAngleToGameLimits(tAngle2);
        shapeForOverlap2 = AffineTransform
                .getRotateInstance(Math.toRadians(-roundedAngle2), (int)off1x_rot_s + (int)off2x, (int)off1y_rot_s + (int)off2y)
                .createTransformedShape(shapeForOverlap2);

        //STEP 9: Check for overlap with obstacles and ships with the final ship position
        List<BumpableWithShape> shipsOrObstacles = getBumpablesOnMap(true);

        String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
        if(shapeForOverlap2 != null){

            List<BumpableWithShape> overlappingShipOrObstacles = findCollidingEntities(shapeForOverlap2, shipsOrObstacles);

            if(overlappingShipOrObstacles.size() > 0) {
                for(BumpableWithShape bws : overlappingShipOrObstacles)
                {
                    previousCollisionVisualization.add(bws.shape);

                    String overlapOnFinalWarn = "*** Warning: " + yourShipName + "'s final reposition location currently overlaps a Ship or Obstacle. You can attempt to move it into a legal position and check if it still overlaps with 'alt-c'.";
                    if(bigCommand !=null) bigCommand.append(logToChatCommand(overlapOnFinalWarn));
                    else bigCommand = logToChatCommand(overlapOnFinalWarn);
                }
                previousCollisionVisualization.add(shapeForOverlap2);
                spawnTemplate = true; //we'll want the template
            }
        }

        // STEP 9.5: Check for movement out of bounds
        checkIfOutOfBounds(yourShipName, shapeForOverlap2);

        //STEP 10: optional if there's any kind of overlap, produce both the template and initial ship position
        if(spawnTemplate == true) {
            //the template is needed, in case of any kind of overlap
            if(bigCommand !=null) bigCommand.append(getMap().placeOrMerge(piece, new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y)));
            else bigCommand = getMap().placeOrMerge(piece, new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y));
            //clone the initial position
            GamePiece myClone = PieceCloner.getInstance().clonePiece(Decorator.getOutermost(this));
            bigCommand.append(getMap().placeOrMerge(myClone, new Point((int)off2x, (int)off2y)));
        }
        //STEP 11: reposition the ship
        if(bigCommand != null) bigCommand.append(getMap().placeOrMerge(Decorator.getOutermost(this), new Point((int)off1x_rot_s + (int)off2x, (int)off1y_rot_s + (int)off2y)));
        else bigCommand = getMap().placeOrMerge(Decorator.getOutermost(this), new Point((int)off1x_rot_s + (int)off2x, (int)off1y_rot_s + (int)off2y));
        //check if the templates is needed as well, in case of any kind of overlap

        return bigCommand;
    }

    private boolean checkIfOutOfBounds(String yourShipName, Shape shapeForOutOfBounds) {
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

            logToChatWithTime("* -- " + yourShipName + " flew out of bounds");
            this.previousCollisionVisualization.add(shapeForOutOfBounds);
            return true;
        }

        return false;
    }

    private Shape getCopyOfShapeWithoutActionsForOverlapCheck(GamePiece oldPiece,RepoManeuver repoTemplate ) {
        // Copy the old piece, but don't set the State
        GamePiece newPiece = GameModule.getGameModule().createPiece(oldPiece.getType());
        VASSAL.build.module.Map var3 = oldPiece.getMap();
        this.piece.setMap((VASSAL.build.module.Map) null);
        // manually set the same position of the old piece
        newPiece.setPosition(oldPiece.getPosition());
        oldPiece.setMap(var3);

        // now set the angle
        double templateAngle;
        templateAngle = repoTemplate.getTemplateAngle(); //repo maneuver's angle
        double shipAngle = this.getRotator().getAngle(); //ship angle
        FreeRotator rotater = (FreeRotator) Decorator.getDecorator(newPiece, FreeRotator.class);
        rotater.setAngle(shipAngle);
        return newPiece.getShape();
    }


    private void closeMouseListener(VASSAL.build.module.Map aMap, MouseListener aML){
        aMap.removeLocalMouseListener(aML);
    }

    private void removeVisuals(VASSAL.build.module.Map aMapm){
        for(repositionChoiceVisual r : rpcList){
            aMapm.removeDrawComponent(r);
        }
        rpcList.clear();
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {


        Boolean hasSomethingHappened  = false;

        //Test for 2.0 ship
        boolean is2pointohShip = this.getInner().getState().contains("this_is_2pointoh");
        //Any keystroke made on a ship will remove the orange shades
        previousCollisionVisualization = new MapVisualizations();

        ChangeTracker changeTracker = new ChangeTracker(this);
        Command result = changeTracker.getChangeCommand();
        MoveTracker moveTracker = new MoveTracker(Decorator.getOutermost(this));
        result.append(moveTracker.getMoveCommand());

        if (KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK,false).equals(stroke)){
            List<BumpableWithShape> BWS = getBumpablesOnMap(true);
            Shape shipShape = getBumpableCompareShape(this);
            List<BumpableWithShape> overlappingObstacles = findCollidingEntities(shipShape, BWS);
            if(overlappingObstacles.size() > 0) {
                hasSomethingHappened = true;
                for(BumpableWithShape bws : overlappingObstacles)
                {
                    previousCollisionVisualization.add(bws.shape);
                    String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
                    logToChat("*** Warning: " + yourShipName + " overlaps a " + bws.type + ".");
                }
                previousCollisionVisualization.add(shipShape);
            }
            else {
                String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
                logToChatWithTime(yourShipName + " does not overlap with an obstacle, ship or mine.");
            }
        }


        RepoManeuver repoTemplateDrop = getKeystrokeTemplateDrop(stroke, is2pointohShip);
        if (repoTemplateDrop != null && stroke.isOnKeyRelease() == false) {
            hasSomethingHappened = true;
            Command tempCommand = spawnRepoTemplate(repoTemplateDrop);
            result.append(tempCommand);

            List<BumpableWithShape> obstacles = getBumpablesOnMap(false);


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
            result.append(piece.keyEvent(stroke));
        }


        RepoManeuver repoShip = getKeystrokeRepoManeuver(stroke, is2pointohShip);
        //Ship reposition requested
        if(repoShip != null  && stroke.isOnKeyRelease() == false) {
            hasSomethingHappened = true;

            //TODO (2.0 only) intercept-detect the side of the barrel roll or decloak and start the 2nd step of waiting for a position choice:
            // if no spot is detected, offer the blinking red (visuals are for the owner only)
            // if 1-3 viable spots are detected, paint them blue, put clickable dots over legal ones AND illegal ones (ie tractored)
            // offer an out of this to cancel the visuals and the mouselistener checking for clicks
            //resume the resolution of a BR or DC and if an illegal position is chosen, spawn the template that was used for it

            if(repoShip.equals(RepoManeuver.BR1_Left_TripleChoices) && isATripleChoiceAllowed()){
                startTripleChoiceStopNewOnes();
                logToChat("Offering 3 choices for barrel roll left");
                final VASSAL.build.module.Map theMap = MouseShipGUI.getTheMainMap();

                List<RepoManeuver> barrelLeft = Lists.newArrayList( RepoManeuver.BR1_Left_AFAP_2E, RepoManeuver.BR1_Left_2E, RepoManeuver.BR1_Left_ABAP_2E);
                offerTripleChoices(barrelLeft, true, theMap);
                return null;
            }

            //detect that the ship's final position overlaps a ship or obstacle
            Command repoCommand = repositionTheShip(repoShip, is2pointohShip);
            if(repoCommand == null) return piece.keyEvent(stroke);
            else{
                result.append(repoCommand);
                result.append(new Chatter.DisplayText(GameModule.getGameModule().getChatter(),"*** " + this.getProperty("Pilot Name").toString() +
                        " has repositioned" + (is2pointohShip?" with " + getRepositionNameMappedToKeyStroke(stroke, true):"")));
                //result.append(piece.keyEvent(stroke));
                if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                    result.append(previousCollisionVisualization);
                    previousCollisionVisualization.execute();

                }
                return result;
            }
            //detect that the template used overlaps an obstacle

        }
        if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
            result.append(previousCollisionVisualization);
            previousCollisionVisualization.execute();
            return result;
        }
        if(hasSomethingHappened == true) return result;
        return piece.keyEvent(stroke);
    }

    private boolean isATripleChoiceAllowed() {
        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        VASSAL.build.module.Map playerMap = getPlayerMap(playerInfo.getSide());
        Boolean ret = Boolean.parseBoolean(playerMap.getProperty("clickChoice").toString());
        logToChat("checking clickChoice: " + playerMap.getProperty("clickChoice").toString());
        if(ret) return false;
        else return true;
    }

    private void stopTripleChoiceMakeNextReady() {
        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        VASSAL.build.module.Map playerMap = getPlayerMap(playerInfo.getSide());
        GamePiece[] pieces = playerMap.getAllPieces();
        for(GamePiece p : pieces){
            logToChat("piece " + p.getName());
            if(p.getName().equals("clickChoiceController")) {
                Command stopIt = p.keyEvent(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK, false));
                stopIt.execute();
                GameModule.getGameModule().sendAndLog(stopIt);
            }
        }
    }

    private void startTripleChoiceStopNewOnes() {
        logToChat("start Triple Choice sequence");
        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        VASSAL.build.module.Map playerMap = getPlayerMap(playerInfo.getSide());
        GamePiece[] pieces = playerMap.getAllPieces();
        for(GamePiece p : pieces){
            if(p.getName().equals("clickChoiceController")){
                Command startIt = p.keyEvent(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK, false));
                startIt.append(logToChatCommand("Stopping the barrel roll choices"));

                startIt.execute();
                GameModule.getGameModule().sendAndLog(startIt);
            }
        }
    }

    private VASSAL.build.module.Map getPlayerMap(int playerIndex) {
        for (VASSAL.build.module.Map loopMap : GameModule.getGameModule().getComponentsOf(VASSAL.build.module.Map.class)) {
            if (("Player " + Integer.toString(playerIndex)).equals(loopMap.getMapName())) {
                return loopMap;
            }
        }
        return null;
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

    private List<BumpableWithShape> getBumpablesOnMap(Boolean wantShipsToo) {

        List<BumpableWithShape> bumpables = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_an_asteroid")) {
                // comment out this line and the next three that add to bumpables if bumps other than with ships shouldn't be detected yet
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece, "Asteroid", "2".equals(testFlipString), false));
            } else if (piece.getState().contains("this_is_a_debris")) {
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece,"Debris","2".equals(testFlipString), false));
            } else if (piece.getState().contains("this_is_a_bomb")) {
                bumpables.add(new BumpableWithShape((Decorator)piece, "Mine", false, false));
            } else if(wantShipsToo == true && piece.getState().contains("this_is_a_ship")){
                //MrMurphM
                //    GamePiece newPiece = PieceCloner.getInstance().clonePiece(piece);
                //    newPiece.setPosition(piece.getPosition());
                //END
                BumpableWithShape tentativeBumpable = new BumpableWithShape((Decorator)piece, "Ship",false,
                        this.getInner().getState().contains("this_is_2pointoh"));
                if (getId().equals(tentativeBumpable.bumpable.getId())) {
                    continue;
                }
                bumpables.add(tentativeBumpable);

            }
        }
        return bumpables;
    }

    public String getId() {
        return this.piece.getId();
    }

    //1=small,2=medium,3=large
    private int whichSizeShip(Decorator ship, boolean is2pointoh) {
        BumpableWithShape test = new BumpableWithShape(ship, "Ship", "notimportant", "notimportant", is2pointoh);
        String chassisNameResult = test.chassis.getChassisName();
        if(chassisNameResult.equals("small")) return 1;
        if(chassisNameResult.equals("medium")) return 2;
        if(chassisNameResult.equals("large")) return 3;

        return 1; //default size
    }

    private PieceSlot findPieceSlotByID(String gpID) {
        for(PieceSlot ps : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)){
            if(gpID.equals(ps.getGpId())) return ps;
        }
        return null;
    }
    private double convertAngleToGameLimits(double angle) {
        this.testRotator.setAngle(angle);
        return this.testRotator.getAngle();
    }

    private RepoManeuver getKeystrokeTemplateDrop(KeyStroke keyStroke, boolean is2pointohShip) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);

        if(is2pointohShip == false){
            if (keyStrokeToDropTemplate.containsKey(hotKey)) {
                return keyStrokeToDropTemplate.get(hotKey);
            }
        }
        else {
            if (keyStrokeToDropTemplate2e.containsKey(hotKey)) {
                return keyStrokeToDropTemplate2e.get(hotKey);
            }
        }
        return null;
    }

    private String getRepositionNameMappedToKeyStroke(KeyStroke keyStroke, boolean is2pointohShip) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);

        if(is2pointohShip == false){
            //TODO want this for 1st edition? then add a map for the reposition names. But they're already present in the vassal traits for the pieces
            return "";
        }
        else {
            if (keyStrokeToName_2e.containsKey(hotKey)) {
                return keyStrokeToName_2e.get(hotKey);
            }
        }
        return null;
    }

    //updated to take into account 2.0 reposition hotkeys with a separate map
    private RepoManeuver getKeystrokeRepoManeuver(KeyStroke keyStroke, boolean is2pointohShip) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
        if(is2pointohShip==false){
            if (keyStrokeToRepositionShip.containsKey(hotKey)) {

                return keyStrokeToRepositionShip.get(hotKey);
            }
        }
        else {
            if (keyStrokeToRepositionShip_2e.containsKey(hotKey)) {

                return keyStrokeToRepositionShip_2e.get(hotKey);
            }
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

        return this.piece.boundingBox();
    }

    public Shape getShape() {
        return this.piece.getShape();
    }

    public String getName() {
        return this.piece.getName();
    }

    private static class repositionChoiceVisual implements Drawable {
        Shape thePieceShape;
        Shape theDot;
        Boolean mouseOvered = false;
        Color bumpColor = new Color(255,99,71, 60);
        Color validColor = new Color(0,255,130, 60);
        Color dotColor = new Color(255,255,200);
        Color mouseOverColor = new Color(255,255,130);
        Color dotOverlappedColor = new Color(255,0,0);
        boolean isOverlapped = false;
        KeyStroke theKey;

        public repositionChoiceVisual(Shape translatedRotatedScaledShape, Shape centralDot, boolean wantOverlapColor, KeyStroke wantKey){
            thePieceShape = translatedRotatedScaledShape;
            theDot = centralDot;
            isOverlapped = wantOverlapColor;
            theKey = wantKey;
        }

        public void setMouseOvered(){
            mouseOvered = true;
        }
        public void unsetMouseOvered(){
            mouseOvered = false;
        }
        public void draw(Graphics g, VASSAL.build.module.Map map) {
            Graphics2D graphics2D = (Graphics2D) g;

            AffineTransform scaler = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom());

            graphics2D.setColor(validColor);
            graphics2D.fill(scaler.createTransformedShape(thePieceShape));
            if(isOverlapped) graphics2D.setColor(dotOverlappedColor);
            else graphics2D.setColor(dotColor);
            graphics2D.fill(scaler.createTransformedShape(theDot));
            if(mouseOvered){
                graphics2D.setColor(mouseOverColor);
                graphics2D.draw(theDot);
            }
        }

        public boolean drawAboveCounters() {
            return false;
        }

        public KeyStroke getKeyStroke() {
            return theKey;
        }
    }
/*
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
    */
}