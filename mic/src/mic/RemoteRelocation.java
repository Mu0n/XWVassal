package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.rmi.Remote;
import java.util.List;

import static mic.Util.*;
import static mic.Util.getCurrentPlayer;
import static mic.Util.logToChat;

/**
 * Created by Mic on 2019-04-07.
 *
 * Equivalent to ShipReposition but for remotes
 */

enum ReloManeuverForProbe {
    //Remote Relocation data. Currently, the templateAngle would be the angle used to reorient the maneuver template from the Pieces window
    //it's unused since you don't need to check an overlap from a relocation template to anything
    Fwd_1("Forward 2 from 1st orientation", "525", 0.0f, 0.0f, -0.0f, 72.0f, 0.0f, -280.0f),
    Fwd_2("Forward 2 from 2nd orientation", "525", 0.0f, -0.0f, -0.0f, 72.0f, 0.0f, -280.0f),
    Fwd_3("Forward 2 from 3rd orientation", "525", 0.0f, -0.0f, 0.0f, 72.0f, 0.0f, -280.0f),
    Fwd_4("Forward 2 from 4th orientation", "525", 0.0f, 0.0f, 0.0f, 72.0f, 0.0f, -280.0f),
    Fwd_5("Forward 2 from 5th orientation", "525", 0.0f, 0.0f, -0.0f, 72.0f, 0.0f, -280.0f),
    Left_1("Bank 2 Left from 1st orientation", "519", 0.0f, -0.0f, -0.0f, 45.0f, 0.0f, 0.0f),
    Left_2("Bank 2 Left from 2nd orientation", "519", 0.0f, -0.0f, -0.0f, 45.0f, 0.0f, 0.0f),
    Left_3("Bank 2 Left from 3rd orientation", "519", 0.0f, -0.0f, 0.0f, 45.0f, 0.0f, 0.0f),
    Left_4("Bank 2 Left from 4th orientation", "519", 0.0f, 0.0f, 0.0f, 45.0f, 0.0f, 0.0f),
    Left_5("Bank 2 Left from 5th orientation", "519", 0.0f, 0.0f, -0.0f, 45.0f, 0.0f, 0.0f),
    Right_1("Bank 2 Right from 1st orientation", "519", 0.0f, 0.0f, -0.0f, -45.0f, 0.0f, 0.0f),
    Right_2("Bank 2 Right from 2nd orientation", "519", 0.0f, 0.0f, -0.0f, -45.0f, 0.0f, 0.0f),
    Right_3("Bank 2 Right from 3rd orientation", "519", 0.0f, 0.0f, 0.0f, -45.0f, 0.0f, 0.0f),
    Right_4("Bank 2 Right from 4th orientation", "519", 0.0f, 0.0f, 0.0f, -45.0f, 0.0f, 0.0f),
    Right_5("Bank 2 Right from 5th orientation", "519", 0.0f, 0.0f, -0.0f, -45.0f, 0.0f, 0.0f);

    private final String repoName;
    private final String gpID;
    private final double templateAngle;
    private final double offsetX;
    private final double offsetY;

    private final double remoteAngle;
    private final double remoteX;
    private final double remoteY;

    ReloManeuverForProbe(String repoName,  String gpID, double templateAngle,
                 double offsetX, double offsetY)
    {
        this.repoName = repoName;
        this.gpID = gpID;
        this.templateAngle = templateAngle;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.remoteAngle = 0.0f;
        this.remoteX = 0.0f;
        this.remoteY = 0.0f;
    }

    ReloManeuverForProbe(String repoName,  String gpID, double templateAngle,
                 double offsetX, double offsetY,
                 double angle, double x, double y)

    {
        this.repoName = repoName;
        this.gpID = gpID;
        this.templateAngle = templateAngle;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.remoteAngle = angle;
        this.remoteX = x;
        this.remoteY = y;

    }

    public String getRepoName() { return this.repoName; }
    public String getTemplateGpID() { return this.gpID; }
    public double getTemplateAngle() { return this.templateAngle; }
    public double getOffsetX() { return this.offsetX; }
    public double getOffsetY() { return this.offsetY; }

    public double getRemoteAngle() { return this.remoteAngle; }
    public double getRemoteX() { return this.remoteX; }
    public double getRemoteY() { return this.remoteY; }


}

public class RemoteRelocation extends Decorator implements EditablePiece {

    public static final String ID = "RemoteRelocation";
    MouseListener ml;
    private final FreeRotator testRotator;
    private FreeRotator myRotator = null;
    public static float DOT_DIAMETER = 46.0f;
    public static float DOT_RADIUS_FOR_PROBE = 170.0f;
    public static float DOT_RADIUS_FOR_PROBE_BANKEXTRA = 100.0f;

    List<RepositionChoiceVisual> rpcList = Lists.newArrayList();
    private static java.util.Map<Integer, ReloManeuverForProbe> optionToRelocate = ImmutableMap.<Integer, ReloManeuverForProbe>builder()
            .put(6 ,ReloManeuverForProbe.Left_1)
            .put(7 ,ReloManeuverForProbe.Fwd_1)
            .put(8 ,ReloManeuverForProbe.Right_1)
            .put(9 ,ReloManeuverForProbe.Left_2)
            .put(10,ReloManeuverForProbe.Fwd_2)
            .put(11,ReloManeuverForProbe.Right_2)
            .put(12,ReloManeuverForProbe.Left_3)
            .put(13,ReloManeuverForProbe.Fwd_3)
            .put(14,ReloManeuverForProbe.Right_3)
            .put(15,ReloManeuverForProbe.Left_4)
            .put(16,ReloManeuverForProbe.Fwd_4)
            .put(17,ReloManeuverForProbe.Right_4)
            .put(18,ReloManeuverForProbe.Left_5)
            .put(19,ReloManeuverForProbe.Fwd_5)
            .put(20,ReloManeuverForProbe.Right_5)
            .build();
    public RemoteRelocation() { this(null); }

    public RemoteRelocation(GamePiece piece){
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
    }
    private VASSAL.build.module.Map getPlayerMap(int playerIndex) {
        for (VASSAL.build.module.Map loopMap : GameModule.getGameModule().getComponentsOf(VASSAL.build.module.Map.class)) {
            if (("Player " + Integer.toString(playerIndex)).equals(loopMap.getMapName())) {
                return loopMap;
            }
        }
        return null;
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
    private boolean isATripleChoiceAllowed() {
        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        VASSAL.build.module.Map playerMap = getPlayerMap(playerInfo.getSide());
        Boolean ret = Boolean.parseBoolean(playerMap.getProperty("clickChoice").toString());
        if(ret) return false;
        else return true;
    }

    public Command tripleChoiceDispatcher(int which, Map theMap) {

        if(!isATripleChoiceAllowed()) return null;

        Command startIt = startTripleChoiceStopNewOnes();
        String contemplatingPlayerName = getCurrentPlayer().getName();

        List<ReloManeuverForProbe> relos = Lists.newArrayList();
        switch(which){
            case 1:
                relos = Lists.newArrayList(ReloManeuverForProbe.Left_1,ReloManeuverForProbe.Fwd_1,ReloManeuverForProbe.Right_1);
                break;
            case 2:
                relos = Lists.newArrayList(ReloManeuverForProbe.Left_2,ReloManeuverForProbe.Fwd_2,ReloManeuverForProbe.Right_2);
                break;
            case 3:
                relos = Lists.newArrayList(ReloManeuverForProbe.Left_3,ReloManeuverForProbe.Fwd_3,ReloManeuverForProbe.Right_3);
                break;
            case 4:
                relos = Lists.newArrayList(ReloManeuverForProbe.Left_4,ReloManeuverForProbe.Fwd_4,ReloManeuverForProbe.Right_4);
                break;
            case 5:
                relos = Lists.newArrayList(ReloManeuverForProbe.Left_5,ReloManeuverForProbe.Fwd_5,ReloManeuverForProbe.Right_5);
                break;
        }
        logToChatWithoutUndo("Please click on a dot to relocate the remote. Click on empty space to cancel this.");
        if(startIt!=null) startIt.append(logToChatCommand("*-- " + contemplatingPlayerName + " is considering 3 relocation positions for the DRK-1 Probe Droid."));
        else startIt = logToChatCommand("*-- " + contemplatingPlayerName + " is considering 3 relocation positions for the DRK-1 Probe Droid.");
        offerTripleChoices(relos,  theMap, which);
        return startIt;
    }

    private Command repositionTheRemote(ReloManeuverForProbe relo) {

        //Info Gathering: angle and position of the ship
        double globalRemoteAngle = this.getRotator().getAngle(); //remote angle

        //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
        double off2x = this.getPosition().getX();
        double off2y = this.getPosition().getY();

        double off1x = relo.getRemoteX();
        double off1y = relo.getRemoteY();

        double off1x_rot = rotX(off1x, off1y, globalRemoteAngle);
        double off1y_rot = rotY(off1x, off1y, globalRemoteAngle);

        double templateTurnsRemoteAngle = relo.getRemoteAngle(); //get the extra angle caused by the template to the ship
        Command bigCommand = null;
        //STEP 11: reposition the ship
        //Set the ship's final angle
        ChangeTracker changeTracker = new ChangeTracker(this);
        FreeRotator fRShip = (FreeRotator)Decorator.getDecorator(this.piece, FreeRotator.class);
        fRShip.setAngle(globalRemoteAngle + templateTurnsRemoteAngle);
        Command changeRotationCommand = changeTracker.getChangeCommand();
        if(bigCommand !=null) bigCommand.append(changeRotationCommand);
        else bigCommand = changeRotationCommand;

        //Ship's final translation command
        if(bigCommand != null) bigCommand.append(getMap().placeOrMerge(Decorator.getOutermost(this), new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y)));
        else bigCommand = getMap().placeOrMerge(Decorator.getOutermost(this), new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y));

        return bigCommand;
    }
    /*
    private Command repositionTheShip(RepoManeuver repoTemplate, boolean is2pointOh) {

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
        List<BumpableWithShape> shipsOrObstacles = getBumpablesOnMap(true);

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
     */


        private int offerTripleChoices(java.util.List<ReloManeuverForProbe> reloTemplates, VASSAL.build.module.Map theMap, int which) {
        //Getting into this function, repoShip is associated with the template used to reposition the ship. We also need the non-mapped final ship tentative position

        // STEP 0: gather ship angle and rotator
        double remoteAngle = this.getRotator().getAngle(); //remote angle
        //FreeRotator fR = (FreeRotator) Decorator.getDecorator(piece, FreeRotator.class);

            int spreadDotAngleFactor = -1;
        for(ReloManeuverForProbe reloTemplate : reloTemplates) { //loops over the list of potential repositions
            if(reloTemplate == null) {
                logToChat("--- Error: couldn't find the relocation data");
                return -1;
            }
            //STEP 1:
            //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
            double off2x = this.getPosition().getX();
            double off2y = this.getPosition().getY();

            float diam = DOT_DIAMETER;
            Shape dot = new Ellipse2D.Float(-diam / 2, -diam / 2, diam, diam);

            //Info Gathering: gets the angle from RepoManeuver which deals with degrees, local space with ship at 0,0, pointing up
            double templateTurnsRemoteAngle = reloTemplate.getRemoteAngle(); //repo maneuver's angle

            //Info Gathering: Offset 1, put to the side of the ship, local coords, get the final coords of the ship (and its dot)

            double off3x = 0.0f;
            double off3y = -DOT_RADIUS_FOR_PROBE_BANKEXTRA;

            double off3x_rot = rotX(off3x, off3y, -spreadDotAngleFactor*35.0f);
            double off3y_rot = rotY(off3x, off3y, -spreadDotAngleFactor*35.0f);

            double off1x_s = 0.0f;
            double off1y_s = -DOT_RADIUS_FOR_PROBE;

            double offLx = off1x_s + off3x_rot;
            double offLy = off1y_s + off3y_rot;


            //STEP 7: rotate the offset1 dependant within the spawner's local coordinates

            double off1x_rot_s_dot = rotX(offLx, offLy, remoteAngle + (which-1)*72.0);
            double off1y_rot_s_dot = rotY(offLx, offLy, remoteAngle + (which-1)*72.0);

            dot = AffineTransform.
                    getTranslateInstance((int) off1x_rot_s_dot + (int) off2x, (int) off1y_rot_s_dot + (int) off2y).
                    createTransformedShape(dot);

            //STEP 11: reposition the ship
            //Add visuals according to the selection of repositioning

            RepositionChoiceVisual rpc = new RepositionChoiceVisual(null, dot, false, "", which + 6 + spreadDotAngleFactor);
            rpcList.add(rpc);
            spreadDotAngleFactor++;
            //return bigCommand;
        } // end of loop around the 3 templates used in the repositions

        //FINAL STEP: add the visuala to the map and the mouse listener


        for(RepositionChoiceVisual r : rpcList){
            theMap.addDrawComponent(r);
        }

        final Decorator remoteToRelocate = this;
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

                for(RepositionChoiceVisual r : copiedList){
                    if(r.theDot.contains(e.getX(),e.getY())){
                        theChosenOne = r;
                        break;
                    }
                }
                try{
                    if(theChosenOne != null){
                        removeVisuals(finalMap);
                        Command endIt = stopTripleChoiceMakeNextReady();
                        if(endIt!=null) endIt.execute();
                        //Change this line to another function that can deal with strings instead
                        //shipToReposition.keyEvent(theChosenOne.getKeyStroke());
                        RemoteRelocation RR = findRemoteRelocationDecorator(remoteToRelocate);
                        RR.newNonKeyEvent(theChosenOne._option);

                        closeMouseListener(finalMap, ml);
                        return;
                    }
                }catch(Exception exce){
                    removeVisuals(finalMap);
                    logToChat("caught an exception while resolving remote relocation");
                    closeMouseListener(finalMap, ml);
                    return;
                }
                //was not in any dot, any ship area, close the whole thing down
                    removeVisuals(finalMap);
                    Command stopItAll = stopTripleChoiceMakeNextReady();
                    String stoppingPlayerName = getCurrentPlayer().getName();
                    if(stopItAll!=null) stopItAll.execute();
                    logToChat("*-- " + stoppingPlayerName + " is cancelling a relocation");

                    closeMouseListener(finalMap, ml);
                    return;

            }

            public void mouseClicked(MouseEvent e) {

            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {           }

            public void mouseExited(MouseEvent e) {            }
        };
        theMap.addLocalMouseListenerFirst(ml);

        return 1;
    }
    //There were too many hotkeys for the mouse GUI, so it uses names instead
    private ReloManeuverForProbe getNewSystemRelo(int choice) {
        if (optionToRelocate.containsKey(choice)) {
            return optionToRelocate.get(choice);
        }
        return null;
    }
    //used at the end of triple choice sequences. Lots less to deal with
    public Command newNonKeyEvent(int option){
        //Deal with ship repositioning, including overlap detection for the templates used, including the triple choice keystrokes that lead to a mouse GUI
        ReloManeuverForProbe reloRemote = getNewSystemRelo(option);
        //Ship reposition requested
        if(reloRemote != null) {
            //detect that the ship's final position overlaps a ship or obstacle
            Command repoCommand = repositionTheRemote(reloRemote);
            if(repoCommand == null) return null; //somehow did not get a programmed reposition command
            else{
                repoCommand.append(logToChatCommand("*** The DRK-1 Probe Droid has repositioned with option " + option));
                repoCommand.execute();
                GameModule.getGameModule().sendAndLog(repoCommand);
                return null;
            }
            //detect that the template used overlaps an obstacle
        } // end of dealing with ship repositions
        return null;
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
    private FreeRotator getRotator() {
        if (this.myRotator == null) {
            this.myRotator = ((FreeRotator) Decorator.getDecorator(getOutermost(this), FreeRotator.class));
        }
        return this.myRotator;
    }

    public void mySetState(String newState) {

    }

    public String myGetState() {
        return "";
    }

    public String myGetType() {
        return ID;
    }

    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }

    public Command myKeyEvent(KeyStroke stroke) {
        return null;
    }

    public String getDescription() {
        return "Custom remote relocation (mic.RemoteRelocation)";
    }

    public void mySetType(String type) {

    }

    public HelpFile getHelpFile() {
        return null;
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

    public static RemoteRelocation findRemoteRelocationDecorator(GamePiece activatedPiece) {
        return (RemoteRelocation)RemoteRelocation.getDecorator(activatedPiece,RemoteRelocation.class);
    }
}
