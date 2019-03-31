
package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mic.Util.*;
import static mic.Util.rotY;

/**
 * Created by Mic on 23/03/2017.
 */

enum ImagesUsedForRanges {
    ProbeDroid("DRK-1 Probe Droid","Probe-range1","Probe-range2","Probe-range");

    private final String pieceName;
    private final String r1Img;
    private final String r2Img;
    private final String r3Img;

    ImagesUsedForRanges(String pName, String r1, String r2, String r3){
        pieceName = pName;
        r1Img = r1;
        r2Img = r2;
        r3Img = r3;
    }

    public String getPieceName(){ return pieceName; }
    public String getR1Img(){ return r1Img; }
    public String getR2Img(){ return r2Img; }
    public String getR3Img(){ return r3Img; }
}

public class AutoRangeForTokens extends Decorator implements EditablePiece {

    protected VASSAL.build.module.Map map;
    private static final int findObstaclesShips = 1;
    private static final int findShips = 2;
    private static final int findObstacles = 3;

    String bigAnnounce = "";
    private final FreeRotator testRotator;

    int whichOption = -1; //which autorange option. See the Map above. -1 if none is selected.

    public static final String ID = "auto-range-finder-for-obstacles";

    private static Map<String, Integer> keyStrokeToOptions = ImmutableMap.<String, Integer>builder()
            .put("CTRL SHIFT L", findObstaclesShips) //find obstacles and ships
            .put("CTRL SHIFT S", findShips) //find only ships
            .put("CTRL SHIFT O", findObstacles) //find only obstacles
            .build();

    public AutoRangeForTokens() {
        this(null);
    }
    public AutoRangeForTokens(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
        // launch();
        map = VASSAL.build.module.Map.getMapById("Map0");
    }

    public void mySetState(String newState) {

    }

    public String myGetState() {
        return "";
    }

    public String myGetType() {
        return ID;
    }

    private int getKeystrokeToOptions(KeyStroke keyStroke) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToOptions.containsKey(hotKey)) {
            return keyStrokeToOptions.get(hotKey).intValue();
        }
        return -1;
    }

    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }

    public Command myKeyEvent(KeyStroke stroke) {
        return null;
    }

    public Command keyEvent(KeyStroke stroke) {

        ArrayList<AutoRangeFinder.RangeFindings> rfindings = new ArrayList<AutoRangeFinder.RangeFindings>(); //findings compiled here

        // check to see if the this code needs to respond to the event
        //identify which autorange option was used by using the static Map defined above in the globals, store it in an int
        whichOption = getKeystrokeToOptions(stroke);

        //we have a valid keystroke option, let's deal with it
        if(whichOption != -1){
            prepAnnouncementStart();
            Shape shapeOfPieceItself = this.piece.getShape();

            //add the ships to the detection
            if(whichOption == findShips || whichOption == findObstaclesShips) {
                //Prepare the start of the appropriate chat announcement string - which ship are we doing this from, which kind of autorange


                //Loop over every other target ship
                List<BumpableWithShape> BWS = getOtherShipsOnMap();
                for (BumpableWithShape b : BWS) {
                    //Preliminary check, eliminate attempt to calculate this if the target is overlapping the attacker, could lead to exception error
                    if (shapesOverlap(shapeOfPieceItself, b.shape)) rfindings.add(new AutoRangeFinder.RangeFindings(0, "a ship"));
                }
            }

            //add the obstacles to the detection
            if(whichOption == findObstacles || whichOption == findObstaclesShips) {
            }

            Command resultCommand = makeBigAnnounceCommand(bigAnnounce, rfindings);
            if(resultCommand!=null) {
                resultCommand.execute();
                GameModule.getGameModule().sendAndLog(resultCommand);
                return null;
            }
        }

        return piece.keyEvent(stroke); //did not find anything worth react to, so send back the key for others to deal with it
    }
    private Shape repositionedOriginShape(ImagesUsedForRanges iufr){
        Shape finalShape = Util.getImageShape(iufr.getR1Img() + ".png");

        //Info Gathering: gets the angle from repoTemplate which deals with degrees, local space with ship at 0,0, pointing up
        //double globalShipAngle = this.getRotator().getAngle(); //ship angle
        //STEP 2: rotate the reposition template with both angles
        //FreeRotator fR = (FreeRotator)Decorator.getDecorator(templatePiece, FreeRotator.class);
        //fR.setAngle(globalShipAngle + templateAngle);

        //Info Gathering: Offset 1, put to the side of the ship, local coords, adjusting for large base if it is found
        //double off1x = repoTemplate.getOffsetX();
        //double off1y = repoTemplate.getOffsetY();

        //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
        //double off2x = this.getPosition().getX();
        //double off2y = this.getPosition().getY();

        //STEP 3: rotate the offset1 dependant within the spawner's local coordinates
        //double off1x_rot = rotX(off1x, off1y, globalShipAngle);
        //double off1y_rot = rotY(off1x, off1y, globalShipAngle);

        //STEP 4: translation into place
        //shapeForTemplate = AffineTransform.
        //        getTranslateInstance((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y).
        //        createTransformedShape(shapeForTemplate);
        //double roundedAngle = convertAngleToGameLimits(globalShipAngle + templateAngle);
        //shapeForTemplate = AffineTransform
        //        .getRotateInstance(Math.toRadians(-roundedAngle), (int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y)
        //        .createTransformedShape(shapeForTemplate);

        return finalShape;
    }
    private void prepAnnouncementStart() {
        //prepares the initial part of the chat window string that announces the firing options
        //bigAnnounce = "*** Calculating ranges from " + this.piece.getName() + " to ";
        bigAnnounce = "*** Calculating ranges to ";

        switch (whichOption) {
            case findObstaclesShips:
                bigAnnounce += "ships and obstacles ";
                break;
            case findShips:
                bigAnnounce += "ships ";
                break;
            case findObstacles:
                bigAnnounce += "obstacles ";
                break;
        }
    }
    private Command makeBigAnnounceCommand(String bigAnnounce, ArrayList<AutoRangeFinder.RangeFindings> rfindings) {
        String range1String = "";
        String range2String = "";
        String range3String = "";
        String range0String = "";

        boolean hasR1 = false;
        boolean hasR2 = false;
        boolean hasR3 = false;
        boolean hasR0 = false;

        for (AutoRangeFinder.RangeFindings rf : rfindings) {
            if (rf.range == 0) {
                hasR0 = true;
                range0String += rf.fullName;
            }
            if (rf.range == 1) {
                hasR1 = true;
                range1String += rf.fullName;
            }
            if (rf.range == 2) {
                hasR2 = true;
                range2String += rf.fullName;
            }
            if (rf.range == 3) {
                hasR3 = true;
                range3String += rf.fullName;
            }
        }

        String result = bigAnnounce + (hasR0 ? "*** Range 0: " + range0String + "\n" : "") +
                (hasR1 ? "*** Range 1: " + range1String + "\n" : "") +
                (hasR2 ? "*** Range 2: " + range2String + "\n" : "") +
                (hasR3 ? "*** Range 3: " + range3String + "\n" : "");
        if (hasR0 == false && hasR1 == false && hasR2 == false && hasR3 == false) result = "Nothing in range.";

        return logToChatCommand(result);
    }
    private List<BumpableWithShape> getOtherShipsOnMap() {
        List<BumpableWithShape> ships = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_a_ship") && piece.getId() != this.piece.getId()) {
                ships.add(new BumpableWithShape((Decorator)piece, "Ship",
                        piece.getProperty("Pilot Name").toString(), piece.getProperty("Craft ID #").toString(),
                        piece.getState().contains("this_is_2pointoh")));
            }
        }
        return ships;
    }


    public String getDescription() {
        return "Custom range detection for tokens (mic.AutoRangeForTokens)";
    }

    public void mySetType(String type) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    public void draw(Graphics g, int x, int y, Component obs, double zoom) {

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
}
