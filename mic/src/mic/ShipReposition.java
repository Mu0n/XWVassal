package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Map;
import static mic.Util.*;
import static mic.Util.getBumpableCompareShape;

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

    //
    //Section for 2.0 style barrel roll. AFAP and ABAP will be limited by the back and front edges and will spawn the template for habit building
    //
    //
    BR1_Left_AFAP_2E("BR Left as Forward as Possible", "524", -90.0f, -113.0f, 0.0f, 0.0f, -226.0f, -28.25f),
    BR1_Left_2E("BR Left", "524", -90.0f, -113.0f, 0.0f, 0.0f, -226.0f, 0.0f),
    BR1_Left_ABAP_2E("BR Left as Backward as Possible", "524", -90.0f, -113.0f, 0.0f, 0.0f, -226.0f, 28.25f),

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

    //small StarViper BR
    BR1_Left_BankF_AFAP_2E("BR Bank Left Forward, as Forward as Possible", "517", -135.0f, -132.0f, -21.0f, 45.0f, -237.5f, -124.0f),
    BR1_Left_BankF_2E("BR Bank Left Forward, centered", "517", -135.0f, -132.0f, -21.0f, 45.0f, -257.5f, -104.0f),
    BR1_Left_BankF_ABAP_2E("BR Bank Left Forward, as Forward as Possible", "517", -135.0f, -132.0f, -21.0f, 45.0f, -277.5f, -84.0f),

    BR1_Left_BankB_AFAP_2E("BR Bank Left Backward, as Forward as Possible", "517", 90.0f, -146.0f, 29.0f, -45.0f, -277.5f, 87.0f),
    BR1_Left_BankB_2E("BR Bank Left Backward, centered", "517", 90.0f, -146.0f, 29.0f, -45.0f, -257.5f, 107.0f),
    BR1_Left_BankB_ABAP_2E("BR Bank Left Backward, as Forward as Possible", "517", 90.0f, -146.0f, 29.0f, -45.0f, -237.5f, 127.0f),

    BR1_Right_BankF_AFAP_2E("BR Bank Right Forward, as Forward as Possible", "517", -90.0f, 147.0f, -28.0f, -45.0f, 237.5f, -124.0f),
    BR1_Right_BankF_2E("BR Bank Right Forward, centered", "517", -90.0f, 147.0f, -28.0f, -45.0f, 257.5f, -104.0f),
    BR1_Right_BankF_ABAP_2E("BR Bank Right Forward, as Forward as Possible", "517", -90.0f, 147.0f, -28.0f, -45.0f, 277.5f, -84.0f),

    BR1_Right_BankB_AFAP_2E("BR Bank Right Forward, as Forward as Possible", "517", 45.0f, 134.0f, 22.0f, 45.0f, 277.5f, 87.0f),
    BR1_Right_BankB_2E("BR Bank Right Forward, centered", "517", 45.0f, 134.0f, 22.0f, 45.0f, 257.5f, 107.0f),
    BR1_Right_BankB_ABAP_2E("BR Bank Right Forward, as Forward as Possible", "517", 45.0f, 134.0f, 22.0f, 45.0f, 237.5f, 127.0f),

    //Echo BR
    BR2_Left_BankF_AFAP_2E("BR2 Bank Left Forward, as Forward as Possible", "519", -135.0f, -132.0f, -21.0f, 45.0f, -237.5f, -124.0f),
    BR2_Left_BankF_2E("BR2 Bank Left Forward, centered", "519", -135.0f, -132.0f, -21.0f, 45.0f, -257.5f, -104.0f),
    BR2_Left_BankF_ABAP_2E("BR2 Bank Left Forward, as Forward as Possible", "519", -135.0f, -132.0f, -21.0f, 45.0f, -277.5f, -84.0f),

    BR2_Left_BankB_AFAP_2E("BR2 Bank Left Backward, as Forward as Possible", "519", 90.0f, -146.0f, 29.0f, -45.0f, -277.5f, 87.0f),
    BR2_Left_BankB_2E("BR2 Bank Left Backward, centered", "519", 90.0f, -146.0f, 29.0f, -45.0f, -257.5f, 107.0f),
    BR2_Left_BankB_ABAP_2E("BR2 Bank Left Backward, as Forward as Possible", "519", 90.0f, -146.0f, 29.0f, -45.0f, -237.5f, 127.0f),

    BR2_Right_BankF_AFAP_2E("BR2 Bank Right Forward, as Forward as Possible", "519", -90.0f, 147.0f, -28.0f, -45.0f, 237.5f, -124.0f),
    BR2_Right_BankF_2E("BR2 Bank Right Forward, centered", "519", -90.0f, 147.0f, -28.0f, -45.0f, 257.5f, -104.0f),
    BR2_Right_BankF_ABAP_2E("BR2 Bank Right Forward, as Forward as Possible", "519", -90.0f, 147.0f, -28.0f, -45.0f, 277.5f, -84.0f),

    BR2_Right_BankB_AFAP_2E("BR2 Bank Right Forward, as Forward as Possible", "519", 45.0f, 134.0f, 22.0f, 45.0f, 277.5f, 87.0f),
    BR2_Right_BankB_2E("BR2 Bank Right Forward, centered", "519", 45.0f, 134.0f, 22.0f, 45.0f, 257.5f, 107.0f),
    BR2_Right_BankB_ABAP_2E("BR2 Bank Right Forward, as Forward as Possible", "519", 45.0f, 134.0f, 22.0f, 45.0f, 237.5f, 127.0f),

    //medium normal BR
    BR1_Left_AFAP_Medium_2E("BR Left as Forward as Possible", "524", 0.0f, -113.75f, 0.0f, 0.0f, -227.5f, -56.5f),
    BR1_Left_Medium_2E("BR Left", "524", 0.0f, -113.75f, 0.0f, 0.0f, -227.5f, 0.0f),
    BR1_Left_ABAP_Medium_2E("BR Left as Backward as Possible", "524", 0.0f, -113.75f, 0.0f, 0.0f, -227.5f, 56.5f),

    BR1_Right_AFAP_Medium_2E("BR Right as Forward as Possible", "524", 0.0f, 113.75f, 0.0f, 0.0f, 227.5f, -56.5f),
    BR1_Right_Medium_2E("BR Right", "524", 0.0f, 113.75f, 0.0f, 0.0f, 227.5f, 0.0f),
    BR1_Right_ABAP_Medium_2E("BR Right as Backward as Possible", "524", 0.0f, 113.75f, 0.0f, 0.0f, 227.5f, 56.5f),

    //medium decloak using a standard
    BRD_Left_AFAP_Medium_2E("Decloak Left as Forward as Possible", "524",0.0f, -113.75f, 0.0f, 0.0f, -227.5f, -56.5f),
    BRD_Left_Medium_2E("Decloak Left", "524", 0.0f, -113.75f, 0.0f, 0.0f, -227.5f, 0.0f),
    BRD_Left_ABAP_Medium_2E("Decloak Left as Backward as Possible", "524", 0.0f, -113.75f, 0.0f, 0.0f, -227.5f, 56.5f),

    BRD_Right_AFAP_Medium_2E("Decloak Right as Forward as Possible", "524", 0.0f, 113.75f, 0.0f, 0.0f, 227.5f, -56.5f),
    BRD_Right_Medium_2E("Decloak Right", "524", 0.0f, 113.75f, 0.0f, 0.0f, 227.5f, 0.0f),
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
    BRD_Left_Large_2E("BR Left as Forward as Possible", "524", -90.0f, -169.5f, 0.0f, 0.0f, -283.0f, 0.0f),
    BRD_Left_ABAP_Large_2E("BR Left as Backward as Possible", "524", 0.0f, -141.25f, 0.0f, 0.0f, -283.0f, 56.5f),

    BRD_Right_AFAP_Large_2E("BR Right as Forward as Possible", "524", 0.0f, 141.25f, 0.0f, 0.0f, 283.0f, -56.5f),
    BRD_Right_Large_2E("BR Right", "524", -90.0f, 169.5f, 0.0f, 0.0f, 283.0f, 0.0f),
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
    private Shape shapeForTemplate;
    static final int NBFLASHES = 5; //use the same flash functionality if a mine is spawned on a ship
    static final int DELAYBETWEENFLASHES = 150;

    MouseListener ml;
    Boolean dealingWithClickChoicesDuringReposition = false;
    List<RepositionChoiceVisual> rpcList = Lists.newArrayList();

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

    //Latest 2.0: Better map for the multitudes of triple choices for repositioning in 2E
    private static Map<String, RepoManeuver> newNameToRepoManeuver = ImmutableMap.<String, RepoManeuver>builder()
            .put("Left Barrel Roll as Forward as Possible", RepoManeuver.BR1_Left_AFAP_2E)
            .put("Left Barrel Roll, centered", RepoManeuver.BR1_Left_2E)
            .put("Left Barrel Roll as Backward as Possible", RepoManeuver.BR1_Left_ABAP_2E)

            .put("Right Barrel Roll as Forward as Possible", RepoManeuver.BR1_Right_AFAP_2E)
            .put("Right Barrel Roll, centered", RepoManeuver.BR1_Right_2E)
            .put("Right Barrel Roll as Backward as Possible", RepoManeuver.BR1_Right_ABAP_2E)

            .put("Left Straight Decloak/BR2 as Forward as Possible", RepoManeuver.BR2_Left_AFAP_2E)
            .put("Left Straight Decloak/BR2, centered", RepoManeuver.BR2_Left_2E)
            .put("Left Straight Decloak/BR2 as Backward as Possible", RepoManeuver.BR2_Left_ABAP_2E)

            .put("Right Straight Decloak/BR2 as Forward as Possible", RepoManeuver.BR2_Right_AFAP_2E)
            .put("Right Straight Decloak/BR2, centered", RepoManeuver.BR2_Right_2E)
            .put("Right Straight Decloak/BR2 as Backward as Possible", RepoManeuver.BR2_Right_ABAP_2E)

            .put("Left, Forward Bank, as Forward as Possible", RepoManeuver.BR1_Left_BankF_AFAP_2E)
            .put("Left, Forward Bank, centered", RepoManeuver.BR1_Left_BankF_2E)
            .put("Left, Forward Bank, as Backward as Possible", RepoManeuver.BR1_Left_BankF_ABAP_2E)
            .put("Left, Backward Bank, as Forward as Possible", RepoManeuver.BR1_Left_BankB_AFAP_2E)
            .put("Left, Backward Bank, centered", RepoManeuver.BR1_Left_BankB_2E)
            .put("Left, Backward Bank, as Backward as Possible", RepoManeuver.BR1_Left_BankB_ABAP_2E)

            .put("Right, Forward Bank, as Forward as Possible", RepoManeuver.BR1_Right_BankF_AFAP_2E)
            .put("Right, Forward Bank, centered", RepoManeuver.BR1_Right_BankF_2E)
            .put("Right, Forward Bank, as Backward as Possible", RepoManeuver.BR1_Right_BankF_ABAP_2E)
            .put("Right, Backward Bank, as Forward as Possible", RepoManeuver.BR1_Right_BankB_AFAP_2E)
            .put("Right, Backward Bank, centered", RepoManeuver.BR1_Right_BankB_2E)
            .put("Right, Backward Bank, as Backward as Possible", RepoManeuver.BR1_Right_BankB_ABAP_2E)

            .build();
    //Latest 2.0: Get back the String (reverse map)
    private static Map<RepoManeuver, String> repoShipToString = ImmutableMap.<RepoManeuver, String>builder()
            //Barrel Roll Left
            .put(RepoManeuver.BR1_Left_AFAP_2E, "Left Barrel Roll as Forward as Possible")
            .put(RepoManeuver.BR1_Left_2E, "Left Barrel Roll, centered")
            .put(RepoManeuver.BR1_Left_ABAP_2E, "Left Barrel Roll as Backward as Possible")
            .put(RepoManeuver.BR1_Left_AFAP_Medium_2E, "Left Barrel Roll as Forward as Possible")
            .put(RepoManeuver.BR1_Left_Medium_2E, "Left Barrel Roll, centered")
            .put(RepoManeuver.BR1_Left_ABAP_Medium_2E, "Left Barrel Roll as Backward as Possible")
            .put(RepoManeuver.BR1_Left_AFAP_Large_2E, "Left Barrel Roll as Forward as Possible")
            .put(RepoManeuver.BR1_Left_Large_2E, "Left Barrel Roll, centered")
            .put(RepoManeuver.BR1_Left_ABAP_Large_2E, "Left Barrel Roll as Backward as Possible")
            //Barrel Roll Right
            .put(RepoManeuver.BR1_Right_AFAP_2E, "Right Barrel Roll as Forward as Possible")
            .put(RepoManeuver.BR1_Right_2E, "Right Barrel Roll, centered")
            .put(RepoManeuver.BR1_Right_ABAP_2E, "Right Barrel Roll as Backward as Possible")
            .put(RepoManeuver.BR1_Right_AFAP_Medium_2E, "Right Barrel Roll as Forward as Possible")
            .put(RepoManeuver.BR1_Right_Medium_2E, "Right Barrel Roll, centered")
            .put(RepoManeuver.BR1_Right_ABAP_Medium_2E, "Right Barrel Roll as Backward as Possible")
            .put(RepoManeuver.BR1_Right_AFAP_Large_2E, "Right Barrel Roll as Forward as Possible")
            .put(RepoManeuver.BR1_Right_Large_2E, "Right Barrel Roll, centered")
            .put(RepoManeuver.BR1_Right_ABAP_Large_2E, "Right Barrel Roll as Backward as Possible")
            //Barrel Roll 2 Left / Decloak
            .put(RepoManeuver.BR2_Left_AFAP_2E, "Left Straight Decloak/BR2 as Forward as Possible")
            .put(RepoManeuver.BR2_Left_2E, "Left Straight Decloak/BR2, centered")
            .put(RepoManeuver.BR2_Left_ABAP_2E, "Left Straight Decloak/BR2 as Backward as Possible")
            //Barrel Roll 2 Right / Decloak
            .put(RepoManeuver.BR2_Right_AFAP_2E, "Right Straight Decloak/BR2 as Forward as Possible")
            .put(RepoManeuver.BR2_Right_2E, "Right Straight Decloak/BR2, centered")
            .put(RepoManeuver.BR2_Right_ABAP_2E, "Right Straight Decloak/BR2 as Backward as Possible")
            //Barrel Roll Bank Left (StarViper style)
            .put(RepoManeuver.BR1_Left_BankF_AFAP_2E, "Left, Forward Bank, as Forward as Possible")
            .put(RepoManeuver.BR1_Left_BankF_2E, "Left, Forward Bank, centered")
            .put(RepoManeuver.BR1_Left_BankF_ABAP_2E, "Left, Forward Bank, as Backward as Possible")
            .put(RepoManeuver.BR1_Left_BankB_AFAP_2E, "Left, Backward Bank, as Forward as Possible")
            .put(RepoManeuver.BR1_Left_BankB_2E, "Left, Backward Bank, centered")
            .put(RepoManeuver.BR1_Left_BankB_ABAP_2E, "Left, Backward Bank, as Backward as Possible")

            //Barrel Roll Bank Right (StarViper style)
            .put(RepoManeuver.BR1_Right_BankF_AFAP_2E, "Right, Forward Bank, as Forward as Possible")
            .put(RepoManeuver.BR1_Right_BankF_2E, "Right, Forward Bank, centered")
            .put(RepoManeuver.BR1_Right_BankF_ABAP_2E, "Right, Forward Bank, as Backward as Possible")
            .put(RepoManeuver.BR1_Right_BankB_AFAP_2E, "Right, Backward Bank, as Forward as Possible")
            .put(RepoManeuver.BR1_Right_BankB_2E, "Right, Backward Bank, centered")
            .put(RepoManeuver.BR1_Right_BankB_ABAP_2E, "Right, Backward Bank, as Backward as Possible")
            .build();




    //Names of the reposition
    private static Map<String, String> keyStrokeToName_2e = ImmutableMap.<String, String>builder()
            .put("CTRL 8", "Left Barrel Roll as Forward as Possible")
            .put("CTRL R", "Left Barrel Roll, centered")
            .put("CTRL SHIFT 8", "Left Barrel Roll as Backward as Possible")
            .put("ALT 8", "Right Barrel Roll as Forward as Possible")
            .put("ALT R", "Right Barrel Roll, centered")
            .put("ALT SHIFT 8", "Right Barrel Roll as Backward as Possible")
            .put("CTRL 9", "Left Straight Decloak as Forward as Possible")
            .put("J", "Left Straight Decloak, centered")
            .put("CTRL SHIFT 9", "Left Straight Decloak as Backward as Possible")
            .put("ALT 9", "Right Straight Decloak as Forward as Possible")
            .put("K", "Right Straight Decloak, centered")
            .put("ALT SHIFT 9", "Right Straight Decloak as Backward as Possible")
            .put("ALT J", "Left, Forward Bank, centered")
            .build();


    //Get back a Keystroke object
    private static Map<RepoManeuver, KeyStroke> repoShipToKeyStroke_2e = ImmutableMap.<RepoManeuver, KeyStroke>builder()
            //Barrel Roll Left
            .put(RepoManeuver.BR1_Left_AFAP_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Left_2E, KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Left_ABAP_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Left_AFAP_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Left_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Left_ABAP_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Left_AFAP_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Left_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Left_ABAP_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))

            //Barrel Roll Right
            .put(RepoManeuver.BR1_Right_AFAP_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.ALT_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Right_2E, KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.ALT_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Right_ABAP_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Right_AFAP_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.ALT_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Right_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.ALT_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Right_ABAP_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Right_AFAP_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.ALT_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Right_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.ALT_DOWN_MASK, false))
            .put(RepoManeuver.BR1_Right_ABAP_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))

            //Barrel Roll Left or straight decloak
            .put(RepoManeuver.BR2_Left_AFAP_2E, KeyStroke.getKeyStroke(KeyEvent.VK_9,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BR2_Left_2E, KeyStroke.getKeyStroke(KeyEvent.VK_J,0, false))
            .put(RepoManeuver.BR2_Left_ABAP_2E, KeyStroke.getKeyStroke(KeyEvent.VK_9,KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Left_AFAP_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Left_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Left_ABAP_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Left_AFAP_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Left_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.CTRL_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Left_ABAP_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))

            //Barrel Roll Right or straight decloak
            .put(RepoManeuver.BR2_Right_AFAP_2E, KeyStroke.getKeyStroke(KeyEvent.VK_9,KeyEvent.ALT_DOWN_MASK, false))
            .put(RepoManeuver.BR2_Right_2E, KeyStroke.getKeyStroke(KeyEvent.VK_K,0, false))
            .put(RepoManeuver.BR2_Right_ABAP_2E, KeyStroke.getKeyStroke(KeyEvent.VK_9,KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Right_AFAP_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.ALT_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Right_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.ALT_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Right_ABAP_Medium_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Right_AFAP_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.ALT_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Right_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.ALT_DOWN_MASK, false))
            .put(RepoManeuver.BRD_Right_ABAP_Large_2E, KeyStroke.getKeyStroke(KeyEvent.VK_8,KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false))

            //Barrel Roll Bank Left Forward
            .put(RepoManeuver.BR1_Left_BankF_2E, KeyStroke.getKeyStroke(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK, false))
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
        shapeForTemplate = piece.getShape();

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

        shapeForTemplate = AffineTransform.
                getTranslateInstance((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y).
                createTransformedShape(shapeForTemplate);
        double roundedAngle = convertAngleToGameLimits(sAngle - tAngle);
        shapeForTemplate = AffineTransform
                .getRotateInstance(Math.toRadians(-roundedAngle), (int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y)
                .createTransformedShape(shapeForTemplate);

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
        else if(size == 3 && is2pointOh == true) {
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

    private int offerTripleChoices(List<RepoManeuver> repoTemplates, boolean is2pointOh, VASSAL.build.module.Map theMap) {
        //Getting into this function, repoShip is associated with the template used to reposition the ship. We also need the non-mapped final ship tentative position

        int size = whichSizeShip(this, is2pointOh);
        removeVisuals(theMap);
        int wideningFudgeFactorBetweenDots = -1; //will go from -1 to 0 to 1 in the following loop

        // STEP 0: gather ship angle and rotator
        double shipAngle = this.getRotator().getAngle(); //ship angle
        //FreeRotator fR = (FreeRotator) Decorator.getDecorator(piece, FreeRotator.class);

        int nbOfRedDots = 0;

        List<BumpableWithShape> obstacles = OverlapCheckManager.getBumpablesOnMap(false, null);
        Boolean condemnAllDots = false;
        shapeForTemplate = repositionedTemplateShape(repoTemplates.get(0)); //using the first of the list will do, since all 3 use the same template information
        if(shapeForTemplate != null) {
            List<BumpableWithShape> overlappingObstacles = findCollidingEntities(shapeForTemplate, obstacles);
            if (overlappingObstacles.size() > 0) {
                condemnAllDots = true;
            }
        }

        for(RepoManeuver repoTemplate : repoTemplates) { //loops over the list of potential repositions
//Prep step, check if it's a medium ship, and only deal with regular barrel rolls, because it's all they can do anyway, rerouting to the correct RepoManeuver
            //logToChat("repo name before change: " + repoTemplate.name());
            repoTemplate = swapToRepoManeuverIfMedOrLarge(repoTemplate, size, is2pointOh);
            //logToChat("repo name after change: " + repoTemplate.name());

            if(repoTemplate == null) {
                logToChat("--- Error: this size " + size + " ship can't perform this reposition.");
                return -1;
            }
            //STEP 1:
            //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
            double off2x = this.getPosition().getX();
            double off2y = this.getPosition().getY();

            //STEP 2: Gather info for ship's final wanted position
            // spawn a copy of the ship without the actions
            Shape shapeForShipOverlap = repositionedShape(repoTemplate);

            float diam = DOT_DIAMETER + (size - 1) * DOT_FUDGE * 0.666f;
            Shape dot = new Ellipse2D.Float(-diam / 2, -diam / 2, diam, diam);

            //Info Gathering: gets the angle from RepoManeuver which deals with degrees, local space with ship at 0,0, pointing up
            double templateTurnsShipAngle = repoTemplate.getShipAngle(); //repo maneuver's angle

            //Info Gathering: Offset 1, put to the side of the ship, local coords, get the final coords of the ship (and its dot)
            double off1x_s = repoTemplate.getShipX();
            double off1y_s = repoTemplate.getShipY();
            double off3x = 0.0f;
            double off3y = wideningFudgeFactorBetweenDots * size * 0.666f * DOT_FUDGE;
            double off3x_t_rot = rotX(off3x, off3y, -templateTurnsShipAngle);
            double off3y_t_rot = rotY(off3x, off3y, -templateTurnsShipAngle);
            double off1x_s_dot = off1x_s + off3x_t_rot;
            double off1y_s_dot = off1y_s + off3y_t_rot;

            //STEP 7: rotate the offset1 dependant within the spawner's local coordinates

            double off1x_rot_s_dot = rotX(off1x_s_dot, off1y_s_dot, shipAngle);
            double off1y_rot_s_dot = rotY(off1x_s_dot, off1y_s_dot, shipAngle);

            dot = AffineTransform.
                    getTranslateInstance((int) off1x_rot_s_dot + (int) off2x, (int) off1y_rot_s_dot + (int) off2y).
                    createTransformedShape(dot);



            //STEP 9: Check for overlap with obstacles and ships with the final ship position
            List<BumpableWithShape> shipsOrObstacles = OverlapCheckManager.getBumpablesOnMap(true, null);
            boolean wantOverlapColor = false;

            if(condemnAllDots==true){
                wantOverlapColor = true;
            }
            else{ //this dot might still be white, or maybe not.
                String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
                if (shapeForShipOverlap != null) {

                    List<BumpableWithShape> overlappingShipOrObstacles = findCollidingEntities(shapeForShipOverlap, shipsOrObstacles);

                    if (overlappingShipOrObstacles.size() > 0) {
                        for (BumpableWithShape bws : overlappingShipOrObstacles) {
                            wantOverlapColor = true;
                        }
                    }
                }

                // STEP 9.5: Check for movement out of bounds
                boolean outsideCheck = checkIfOutOfBounds(yourShipName, shapeForShipOverlap, false, false);
                if (outsideCheck) wantOverlapColor = true;
            }

            //STEP 11: reposition the ship
            //Add visuals according to the selection of repositioning

            RepositionChoiceVisual rpc = new RepositionChoiceVisual(shapeForShipOverlap, dot, wantOverlapColor,repoShipToString.get(repoTemplate),0, null);
            rpcList.add(rpc);

            //return bigCommand;
            wideningFudgeFactorBetweenDots++;
            if(wantOverlapColor == true) nbOfRedDots++;
        } // end of loop around the 3 templates used in the repositions

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
                        ShipReposition SR = findShipRepositionDecorator(shipToReposition);
                        SR.newNonKeyEvent(theChosenOne.inStringForm);

                        closeMouseListener(finalMap, ml);
                        return;
                    }
                }catch(Exception exce){
                    removeVisuals(finalMap);
                    logToChat("caught an exception while resolving ship reposition");
                    closeMouseListener(finalMap, ml);
                    return;
                }

                if(slightMisclick) return; //misclick outside of a dot, but inside the ship shapes, do nothing, don't dismiss the GUI
                else{ //was not in any dot, any ship area, close the whole thing down
                    removeVisuals(finalMap);
                    Command stopItAll = stopTripleChoiceMakeNextReady();
                    String stoppingPlayerName = getCurrentPlayer().getName();
                    if(stopItAll!=null) stopItAll.execute();
                    logToChat("*-- " + stoppingPlayerName + " is cancelling a reposition");

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



    private Command repositionTheShip(RepoManeuver repoTemplate, boolean is2pointOh) {
        //Getting into this function, repoShip is associated with the template used to reposition the ship. We also need the non-mapped final ship tentative position
        Boolean spawnTemplate = false;

        int size = whichSizeShip(this, is2pointOh);
        //Prep step, check if it's a medium ship, and only deal with regular barrel rolls, because it's all they can do anyway, rerouting to the correct RepoManeuver
        repoTemplate = swapToRepoManeuverIfMedOrLarge(repoTemplate, size, is2pointOh);
        if(repoTemplate == null) return null;

        //Info Gathering: angle and position of the ship
        double globalShipAngle = this.getRotator().getAngle(); //ship angle
        double templateAngle = repoTemplate.getTemplateAngle(); //repo maneuver's angle

        double off2x = this.getPosition().getX();
        double off2y = this.getPosition().getY();
        //Info Gathering: Offset 1, put to the side of the ship, local coords, adjusting for large base if it is found
        double off1x = repoTemplate.getOffsetX();
        double off1y = repoTemplate.getOffsetY();
        //STEP 3: rotate the offset1 dependant within the spawner's local coordinates
        double off1x_rot = rotX(off1x, off1y, globalShipAngle);
        double off1y_rot = rotY(off1x, off1y, globalShipAngle);
        //Info Gathering: gets the angle from repoTemplate which deals with degrees, local space with ship at 0,0, pointing up
        double templateTurnsShipAngle = repoTemplate.getShipAngle(); //get the extra angle caused by the template to the ship
        GamePiece templatePiece = newPiece(findPieceSlotByID(repoTemplate.getTemplateGpID()));

        shapeForTemplate = repositionedTemplateShape(repoTemplate);
        FreeRotator fR = (FreeRotator)Decorator.getDecorator(templatePiece, FreeRotator.class);
        fR.setAngle(globalShipAngle + templateAngle);
        //STEP 5: Check for overlap with an obstacle, if so, spawn it so the player sees it
        Command bigCommand = null;

        List<BumpableWithShape> obstacles = OverlapCheckManager.getBumpablesOnMap(false, null);

        if(shapeForTemplate != null) {
            List<BumpableWithShape> overlappingObstacles = findCollidingEntities(shapeForTemplate, obstacles);
            if (overlappingObstacles.size() > 0) {
                for (BumpableWithShape bws : overlappingObstacles) {
                    previousCollisionVisualization.add(bws.shape);
                    String overlapWarn = "*** Warning: reposition template currently overlaps an obstacle.";
                    if(bigCommand != null) bigCommand.append(logToChatCommand(overlapWarn));
                    else bigCommand = logToChatCommand(overlapWarn);
                }
                previousCollisionVisualization.add(shapeForTemplate);
                spawnTemplate = true; //we'll want the template
            }
        }
        //STEP 6: Gather info for ship's final wanted position

        // spawn a copy of the ship without the actions
        //Shape shapeForOverlap2 = getCopyOfShapeWithoutActionsForOverlapCheck(this.piece,repoTemplate );
        Shape shapeForShip = repositionedShape(repoTemplate);

        //Info Gathering: Offset 1, put to the side of the ship, local coords, adjusting for large base if it is found
        double off1x_s = repoTemplate.getShipX();
        double off1y_s = repoTemplate.getShipY();

        //STEP 7: rotate the offset1 dependant within the spawner's local coordinates
        double off1x_rot_s = rotX(off1x_s, off1y_s, globalShipAngle);
        double off1y_rot_s = rotY(off1x_s, off1y_s, globalShipAngle);


        //STEP 9: Check for overlap with obstacles and ships with the final ship position
        List<BumpableWithShape> shipsOrObstacles = OverlapCheckManager.getBumpablesOnMap(true, null);

        String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
        if(shapeForShip != null){

            List<BumpableWithShape> overlappingShipOrObstacles = findCollidingEntities(shapeForShip, shipsOrObstacles);

            if(overlappingShipOrObstacles.size() > 0) {
                for(BumpableWithShape bws : overlappingShipOrObstacles)
                {
                    previousCollisionVisualization.add(bws.shape);

                    String overlapOnFinalWarn = "*** Warning: " + yourShipName + "'s final reposition location currently overlaps a Ship or Obstacle.";
                    if(bigCommand !=null) bigCommand.append(logToChatCommand(overlapOnFinalWarn));
                    else bigCommand = logToChatCommand(overlapOnFinalWarn);
                }
                previousCollisionVisualization.add(shapeForShip);
                spawnTemplate = true; //we'll want the template
            }
        }

        // STEP 9.5: Check for movement out of bounds
        checkIfOutOfBounds(yourShipName, shapeForShip, true, true);

        //STEP 10: optional if there's any kind of overlap, produce both the template and initial ship position
        if(spawnTemplate == true) {
            //the template is needed, in case of any kind of overlap
            if(bigCommand !=null) bigCommand.append(getMap().placeOrMerge(templatePiece, new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y)));
            else bigCommand = getMap().placeOrMerge(templatePiece, new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y));
            //clone the initial position
            GamePiece myClone = PieceCloner.getInstance().clonePiece(Decorator.getOutermost(this));

            bigCommand.append(getMap().placeOrMerge(myClone, new Point((int)off2x, (int)off2y)));
        }

        //STEP 11: reposition the ship
        //Set the ship's final angle
        ChangeTracker changeTracker = new ChangeTracker(this);
        FreeRotator fRShip = (FreeRotator)Decorator.getDecorator(this.piece, FreeRotator.class);
        fRShip.setAngle(globalShipAngle - templateTurnsShipAngle);
        Command changeRotationCommand = changeTracker.getChangeCommand();
        if(bigCommand !=null) bigCommand.append(changeRotationCommand);
        else bigCommand = changeRotationCommand;

        //Ship's final translation command
        if(bigCommand != null) bigCommand.append(getMap().placeOrMerge(Decorator.getOutermost(this), new Point((int)off1x_rot_s + (int)off2x, (int)off1y_rot_s + (int)off2y)));
        else bigCommand = getMap().placeOrMerge(Decorator.getOutermost(this), new Point((int)off1x_rot_s + (int)off2x, (int)off1y_rot_s + (int)off2y));

        return bigCommand;
    }

    private Shape repositionedTemplateShape(RepoManeuver repoTemplate){
        //STEP 1: Collision reposition template, centered as in in the image file, centered on 0,0 (upper left corner)
        GamePiece templatePiece = newPiece(findPieceSlotByID(repoTemplate.getTemplateGpID()));

        shapeForTemplate = templatePiece.getShape();

        //Info Gathering: gets the angle from repoTemplate which deals with degrees, local space with ship at 0,0, pointing up
        double templateAngle = repoTemplate.getTemplateAngle(); //repo maneuver's angle
        double globalShipAngle = this.getRotator().getAngle(); //ship angle
        //STEP 2: rotate the reposition template with both angles
        FreeRotator fR = (FreeRotator)Decorator.getDecorator(templatePiece, FreeRotator.class);
        fR.setAngle(globalShipAngle + templateAngle);

        //Info Gathering: Offset 1, put to the side of the ship, local coords, adjusting for large base if it is found
        double off1x = repoTemplate.getOffsetX();
        double off1y = repoTemplate.getOffsetY();

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
    private Shape repositionedShape(RepoManeuver repoTemplate){
        double globalShipAngle = this.getRotator().getAngle(); //ship angle
        double templateTurnsShipAngle = repoTemplate.getShipAngle(); //template making the ship turn angle
        //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
        double off2x = this.getPosition().getX();
        double off2y = this.getPosition().getY();

        // spawn a copy of the ship without the actions
        //Shape shapeForOverlap2 = getCopyOfShapeWithoutActionsForOverlapCheck(this.piece,repoTemplate );
        Shape shapeForShip = Decorator.getDecorator(Decorator.getOutermost(this), NonRectangular.class).getShape();

        //Info Gathering: Offset 1, put to the side of the ship, local coords, adjusting for large base if it is found
        double off1x_s = repoTemplate.getShipX();
        double off1y_s = repoTemplate.getShipY();

        //STEP 7: rotate the offset1 dependant within the spawner's local coordinates
        double off1x_rot_s = rotX(off1x_s, off1y_s, globalShipAngle);
        double off1y_rot_s = rotY(off1x_s, off1y_s, globalShipAngle);

        double roundedAngle = convertAngleToGameLimits(globalShipAngle - templateTurnsShipAngle);

        //STEP 8: translation into place
        shapeForShip = AffineTransform.
                getTranslateInstance((int) off1x_rot_s + (int) off2x, (int) off1y_rot_s + (int) off2y).
                createTransformedShape(shapeForShip);
        shapeForShip = AffineTransform
                .getRotateInstance(Math.toRadians(-roundedAngle), (int) off1x_rot_s + (int) off2x,(int) off1y_rot_s + (int) off2y)
                .createTransformedShape(shapeForShip);

        return shapeForShip;
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


    private void closeMouseListener(VASSAL.build.module.Map aMap, MouseListener aML){
        aMap.removeLocalMouseListener(aML);
    }

    private void removeVisuals(VASSAL.build.module.Map aMapm){
        for(RepositionChoiceVisual r : rpcList){
            aMapm.removeDrawComponent(r);
        }
        rpcList.clear();
    }

    public Command tripleChoiceDispatcher(int which, String pilotName){

        if(!isATripleChoiceAllowed()) return null;

        Command startIt = startTripleChoiceStopNewOnes();
        List<RepoManeuver> repoChoices = Lists.newArrayList();
        final VASSAL.build.module.Map theMap = Util.getTheMainMap();
        String contemplatingPlayerName = getCurrentPlayer().getName();

        StringBuilder sb = new StringBuilder("*--- ");
        switch(which){
            //barrel roll left 1
            case 1:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll left for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR1_Left_AFAP_2E, RepoManeuver.BR1_Left_2E, RepoManeuver.BR1_Left_ABAP_2E);
                break;
            //barrel roll right 1
            case 2:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll right for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR1_Right_AFAP_2E, RepoManeuver.BR1_Right_2E, RepoManeuver.BR1_Right_ABAP_2E);
                break;
            //barrel roll left 2 / decloak
            case 3:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll 2 or decloak left for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR2_Left_AFAP_2E, RepoManeuver.BR2_Left_2E, RepoManeuver.BR2_Left_ABAP_2E);
                break;
            //barrel roll right 2 / decloak
            case 4:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll 2 or decloak right for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR2_Right_AFAP_2E, RepoManeuver.BR2_Right_2E, RepoManeuver.BR2_Right_ABAP_2E);
                break;
            //SV barrel roll left, forward
            case 5:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll left with forward bank for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR1_Left_BankF_AFAP_2E, RepoManeuver.BR1_Left_BankF_2E, RepoManeuver.BR1_Left_BankF_ABAP_2E);
                break;
            //SV barrel roll left, backward
            case 6:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll left with backward bank for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR1_Left_BankB_AFAP_2E, RepoManeuver.BR1_Left_BankB_2E, RepoManeuver.BR1_Left_BankB_ABAP_2E);
                break;
            //SV barrel roll right, forward
            case 7:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll right with forward bank for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR1_Right_BankF_AFAP_2E, RepoManeuver.BR1_Right_BankF_2E, RepoManeuver.BR1_Right_BankF_ABAP_2E);
                break;
            //SV barrel roll right, backward
            case 8:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll right with backward bank for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR1_Right_BankB_AFAP_2E, RepoManeuver.BR1_Right_BankB_2E, RepoManeuver.BR1_Right_BankB_ABAP_2E);
                break;
            //echo decloak left, forward
            case 9:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll left with forward bank 2 (Echo style decloak) for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR2_Left_BankF_AFAP_2E, RepoManeuver.BR2_Left_BankF_2E, RepoManeuver.BR2_Left_BankF_ABAP_2E);
                break;
            //echo decloak right, forward
            case 10:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll right with forward bank 2 (Echo style decloak) for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR2_Right_BankF_AFAP_2E, RepoManeuver.BR2_Right_BankF_2E, RepoManeuver.BR2_Right_BankF_ABAP_2E);
                break;
            //echo decloak left, backward
            case 11:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll left with backward bank 2 (Echo style decloak) for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR2_Left_BankB_AFAP_2E, RepoManeuver.BR2_Left_BankB_2E, RepoManeuver.BR2_Left_BankB_ABAP_2E);
                break;
            //echo decloak right, backward
            case 12:
                sb.append(contemplatingPlayerName + " is contemplating 3 choices for barrel roll right with backward bank 2 (Echo style decloak) for " + pilotName);
                repoChoices = Lists.newArrayList(RepoManeuver.BR2_Right_BankB_AFAP_2E, RepoManeuver.BR2_Right_BankB_2E, RepoManeuver.BR2_Right_BankB_ABAP_2E);
                break;
            //Tallon Roll Left 1
            case 13:
                break;
            //Tallon Roll Left 2
            case 14:
                break;
            //Tallon Roll Left 3
            case 15:
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

        int nbOfRedDots = offerTripleChoices(repoChoices, true, theMap);
        sb.append(". There are " + (3-nbOfRedDots) + " valid position"+(nbOfRedDots>1?"s":"")+" to pick from." );
        if(startIt!=null) startIt.append(logToChatCommand(sb.toString()));
        else startIt = logToChatCommand(sb.toString());
        if(nbOfRedDots==-1) return null;
        return startIt;
    }
    //used at the end of triple choice sequences. Lots less to deal with
    public Command newNonKeyEvent(String choice){
        previousCollisionVisualization = new MapVisualizations();
        //Deal with ship repositioning, including overlap detection for the templates used, including the triple choice keystrokes that lead to a mouse GUI
        RepoManeuver repoShip = getNewSystemRepoManeuver(choice);
        //Ship reposition requested
        if(repoShip != null) {
            //detect that the ship's final position overlaps a ship or obstacle
            Command repoCommand = repositionTheShip(repoShip ,true); //only exists for 2.0 so is2pointohShip is true
            if(repoCommand == null) return null; //somehow did not get a programmed reposition command
            else{
                repoCommand.append(logToChatCommand("*** " + this.getProperty("Pilot Name").toString() +
                        " has repositioned" + " with " + choice));

                if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                    repoCommand.append(previousCollisionVisualization);
                }
                repoCommand.execute();
                GameModule.getGameModule().sendAndLog(repoCommand);
                return null;
            }
            //detect that the template used overlaps an obstacle
        } // end of dealing with ship repositions
        return null;
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {
        //Test for 2.0 ship
        boolean is2pointohShip = this.getInner().getState().contains("this_is_2pointoh");
        //Any keystroke made on a ship will remove the orange shades
        previousCollisionVisualization = new MapVisualizations();

        //Deal with ALT-C, detect if there's something under the ship creating an overlap
        if (KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK,false).equals(stroke)){
            List<BumpableWithShape> BWS = OverlapCheckManager.getBumpablesOnMap(true, null);
            Shape shipShape = getBumpableCompareShape(this);
            List<BumpableWithShape> overlappingObstacles = findCollidingEntities(shipShape, BWS);
            if(overlappingObstacles.size() > 0) {
                Command result = null;
                for(BumpableWithShape bws : overlappingObstacles)
                {
                    previousCollisionVisualization.add(bws.shape);
                    String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
                    if(result==null) result = logToChatCommand("*** Warning: " + yourShipName + " overlaps a " + bws.type + ".");
                    else result.append(logToChatCommand("*** Warning: " + yourShipName + " overlaps a " + bws.type + "."));
                }
                previousCollisionVisualization.add(shipShape);
                if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                    if(result==null) result = previousCollisionVisualization;
                    else result.append(previousCollisionVisualization);
                }
                if(result!=null) {
                    result.execute();
                    GameModule.getGameModule().sendAndLog(result);
                }
                return null;
            }
            else {
                String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
                logToChatWithTime(yourShipName + " does not overlap with an obstacle, ship or mine.");
                return null;
            }
        } //End of dealing with ALT-C

        //Deal with a dropping of a maneuver. only exists for 1.0 now
        RepoManeuver repoTemplateDrop = getKeystrokeTemplateDrop(stroke);
        if (is2pointohShip == false && repoTemplateDrop != null && stroke.isOnKeyRelease() == false) {
            Command result = spawnRepoTemplate(repoTemplateDrop);

            List<BumpableWithShape> obstacles = OverlapCheckManager.getBumpablesOnMap(false, null);

            if(shapeForTemplate != null){
                List<BumpableWithShape> overlappingObstacles = findCollidingEntities(shapeForTemplate, obstacles);
                if(overlappingObstacles.size() > 0) {
                    for(BumpableWithShape bws : overlappingObstacles)
                    {
                        previousCollisionVisualization.add(bws.shape);
                        result.append(logToChatCommand("*** Warning: reposition template currently overlaps an obstacle"));
                    }
                    previousCollisionVisualization.add(shapeForTemplate);
                }
            }
            if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                result.append(previousCollisionVisualization);
            }
            result.execute();
            GameModule.getGameModule().sendAndLog(result);
            return null;
        } // End of dropping a maneuver template


        //Deal with ship repositioning, including overlap detection for the templates used, including the triple choice keystrokes that lead to a mouse GUI
        RepoManeuver repoShip = getKeystrokeRepoManeuver(stroke, is2pointohShip);
        //Ship reposition requested
        if(repoShip != null  && stroke.isOnKeyRelease() == false) {
            //detect that the ship's final position overlaps a ship or obstacle
            Command repoCommand = repositionTheShip(repoShip, is2pointohShip);
            if(repoCommand == null) return piece.keyEvent(stroke); //somehow did not get a programmed reposition command
            else{
                repoCommand.append(logToChatCommand("*** " + this.getProperty("Pilot Name").toString() +
                        " has repositioned" + (is2pointohShip?" with " + getRepositionNameMappedToKeyStroke(stroke, true):"")));

                if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                    repoCommand.append(previousCollisionVisualization);
                }
                repoCommand.execute();
                GameModule.getGameModule().sendAndLog(repoCommand);
                return null;
            }
            //detect that the template used overlaps an obstacle

        } // end of dealing with ship repositions

        //no wanted key event was detected, send it to other objects
        return piece.keyEvent(stroke);
    }

    private boolean isATripleChoiceAllowed() {
        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        VASSAL.build.module.Map playerMap = getPlayerMap(playerInfo.getSide());
        Boolean ret = Boolean.parseBoolean(playerMap.getProperty("clickChoice").toString());
        if(ret) return false;
        else return true;
    }

    private Command stopTripleChoiceMakeNextReady() {
        Command result = null;
        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        VASSAL.build.module.Map playerMap = getPlayerMap(playerInfo.getSide());
        GamePiece[] pieces = playerMap.getAllPieces();
        for(GamePiece p : pieces){
            if(p.getName().equals("clickChoiceController")) {
                result = p.keyEvent(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK, false));
                return result;
            }
        }
        return result;
    }

    private Command startTripleChoiceStopNewOnes() {
        Command result = null;
        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        VASSAL.build.module.Map playerMap = getPlayerMap(playerInfo.getSide());
        GamePiece[] pieces = playerMap.getAllPieces();
        for(GamePiece p : pieces){
            if(p.getName().equals("clickChoiceController")){
                result = p.keyEvent(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK, false));
                return result;
            }
        }
        return result;
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

    private RepoManeuver getKeystrokeTemplateDrop(KeyStroke keyStroke) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
            if (keyStrokeToDropTemplate.containsKey(hotKey)) {
                return keyStrokeToDropTemplate.get(hotKey);
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

    //There were too many hotkeys for the mouse GUI, so it uses names instead
    private RepoManeuver getNewSystemRepoManeuver(String name) {
        if (newNameToRepoManeuver.containsKey(name)) {
            return newNameToRepoManeuver.get(name);
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


    public static ShipReposition findShipRepositionDecorator(GamePiece activatedPiece) {
        return (ShipReposition)ShipReposition.getDecorator(activatedPiece,ShipReposition.class);
    }
}