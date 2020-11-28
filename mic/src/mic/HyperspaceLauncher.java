package mic;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.List;

import static mic.Util.*;
import static mic.Util.logToChatCommand;

/**
 * Created by Mic on 2020-11-28.
 *
 * Equivalent to RemoteRelocation but for the Aces High Hyperspace Marker
 */

public class HyperspaceLauncher extends Decorator implements EditablePiece{

    public static final String ID = "HyperspaceLauncher";
    MouseListener ml;
    private final FreeRotator testRotator;
    private FreeRotator myRotator = null;
    public static float DOT_DIAMETER = 46.0f;
    public static float DOT_RADIUS_FOR_PROBE = 110.0f;
    public static float DOT_RADIUS_FOR_PROBE_BANKEXTRA = 90.0f;
    public static float DOT_BUZZ_RADIUS = 72.0f;

    java.util.List<RepositionChoiceVisual> rpcList = Lists.newArrayList();
    private static java.util.Map<Integer, ReloManeuverForProbe> optionToRelocate = ImmutableMap.<Integer, ReloManeuverForProbe>builder()
            //called by option 1
            .put(6 ,ReloManeuverForProbe.Left_1)
            .put(7 ,ReloManeuverForProbe.Fwd_1)
            .put(8 ,ReloManeuverForProbe.Right_1)
            //called by option 2
            .put(9 ,ReloManeuverForProbe.Left_2)
            .put(10,ReloManeuverForProbe.Fwd_2)
            .put(11,ReloManeuverForProbe.Right_2)
            //called by option 3
            .put(12,ReloManeuverForProbe.Left_3)
            .put(13,ReloManeuverForProbe.Fwd_3)
            .put(14,ReloManeuverForProbe.Right_3)
            //called by option -77, buzz swarm
            .put(-55, ReloManeuverForProbe.BuzzFront)
            .put(-66, ReloManeuverForProbe.BuzzBack)
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

    public Command tripleChoiceDispatcher(RepositionChoiceVisual rpc, Map theMap) {

        if(!isATripleChoiceAllowed()) return null;

        Command startIt = startTripleChoiceStopNewOnes();
        String contemplatingPlayerName = getCurrentPlayer().getName();

        java.util.List<ReloManeuverForProbe> relos = Lists.newArrayList();
        switch(rpc._option){
            case 1: //Probe Droid
                relos = Lists.newArrayList(ReloManeuverForProbe.Left_1,ReloManeuverForProbe.Fwd_1,ReloManeuverForProbe.Right_1);
                break;
            case 2: //Probe Droid
                relos = Lists.newArrayList(ReloManeuverForProbe.Left_2,ReloManeuverForProbe.Fwd_2,ReloManeuverForProbe.Right_2);
                break;
            case 3: //Probe Droid
                relos = Lists.newArrayList(ReloManeuverForProbe.Left_3,ReloManeuverForProbe.Fwd_3,ReloManeuverForProbe.Right_3);
                break;
            case 4: //Probe Droid
                relos = Lists.newArrayList(ReloManeuverForProbe.Left_4,ReloManeuverForProbe.Fwd_4,ReloManeuverForProbe.Right_4);
                break;
            case 5: //Probe Droid
                relos = Lists.newArrayList(ReloManeuverForProbe.Left_5,ReloManeuverForProbe.Fwd_5,ReloManeuverForProbe.Right_5);
                break;
            case -77: //Buzz Droid Swarm
                relos = Lists.newArrayList(ReloManeuverForProbe.BuzzFront, ReloManeuverForProbe.BuzzBack, ReloManeuverForProbe.BuzzBack);

                int nbOfRedDots = offerTripleChoices(relos,  theMap, rpc);
                logToChatWithoutUndo("Please click on a dot to relocate the remote. Click on empty space to cancel this.");
                if(startIt!=null) startIt.append(logToChatCommand("*-- " + contemplatingPlayerName + " is considering attachment positions for the Buzz Swarm Droids. There are " + (2-nbOfRedDots) + " valid positions."));
                else startIt = logToChatCommand("*-- " + contemplatingPlayerName + " is considering attachment positions for the Buzz Swarm Droids. There are " + (2-nbOfRedDots) + " valid positions.");
                if(nbOfRedDots==-1) return null; //something wrong happened
                return startIt;
        }
        offerTripleChoices(relos,  theMap, rpc);
        logToChatWithoutUndo("Please click on a dot to relocate the remote. Click on empty space to cancel this.");
        if(startIt!=null) startIt.append(logToChatCommand("*-- " + contemplatingPlayerName + " is considering 3 relocation positions for the DRK-1 Probe Droid."));
        else startIt = logToChatCommand("*-- " + contemplatingPlayerName + " is considering 3 relocation positions for the DRK-1 Probe Droid.");

        return startIt;
    }

    private Shape repositionedBuzzSwarmShape(ReloManeuverForProbe relo, GamePiece victimShip){
        boolean wantBack = false;
        if(relo == ReloManeuverForProbe.BuzzBack) wantBack = true;

        double globalShipAngle =  ((FreeRotator) Decorator.getDecorator(getOutermost(victimShip), FreeRotator.class)).getAngle();

        double off2x = victimShip.getPosition().x;
        double off2y = victimShip.getPosition().y;

        double off1x = 0.0f;
        double off1y = -116.0f;
        int sizeship = whichSizeShip((Decorator)Decorator.getOutermost(victimShip), true);
        if(sizeship==2) off1y = -145.5f;
        else if(sizeship==3) off1y = -173.0f;

        double off1x_rot = rotX(off1x, off1y, globalShipAngle + (wantBack?180.0f:0.0f));
        double off1y_rot = rotY(off1x, off1y, globalShipAngle + (wantBack?180.0f:0.0f));

        Shape shapeForBuzz = Decorator.getDecorator(Decorator.getOutermost(victimShip), NonRectangular.class).getShape();


        double roundedAngle = globalShipAngle + (wantBack?180.0f:0.0f);

        shapeForBuzz = AffineTransform.
                getTranslateInstance((int) off1x_rot + (int) off2x, (int) off1y_rot + (int) off2y).
                createTransformedShape(shapeForBuzz);
        shapeForBuzz = AffineTransform
                .getRotateInstance(Math.toRadians(roundedAngle), (int) off1x_rot + (int) off2x,(int) off1y_rot + (int) off2y)
                .createTransformedShape(shapeForBuzz);
        return shapeForBuzz;
    }


    private Command repositionBuzzSwarm(ReloManeuverForProbe relo, GamePiece victimShip){
        boolean wantBack = false;
        if(relo==ReloManeuverForProbe.BuzzBack) wantBack = true;

        double globalShipAngle =  ((FreeRotator) Decorator.getDecorator(getOutermost(victimShip), FreeRotator.class)).getAngle();

        double off2x = victimShip.getPosition().x;
        double off2y = victimShip.getPosition().y;

        double off1x = 0.0f;
        double off1y = -116.0f;

        int sizeship = whichSizeShip((Decorator)Decorator.getOutermost(victimShip), true);
        if(sizeship==2) off1y = -145.5f;
        else if(sizeship==3) off1y = -173.0f;

        double off1x_rot = rotX(off1x, off1y, globalShipAngle + (wantBack?180.0f:0.0f));
        double off1y_rot = rotY(off1x, off1y, globalShipAngle + (wantBack?180.0f:0.0f));

        Command bigCommand = null;
        //STEP 11: reposition the remote
        //Set the remote's final angle
        ChangeTracker changeTracker = new ChangeTracker(this);
        FreeRotator fRShip = (FreeRotator)Decorator.getDecorator(this.piece, FreeRotator.class);
        fRShip.setAngle(globalShipAngle + (wantBack?180.0f:0.0f));
        Command changeRotationCommand = changeTracker.getChangeCommand();
        if(bigCommand !=null) bigCommand.append(changeRotationCommand);
        else bigCommand = changeRotationCommand;
        //Remote's final translation command
        if(bigCommand != null) bigCommand.append(getMap().placeOrMerge(Decorator.getOutermost(this), new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y)));
        else bigCommand = getMap().placeOrMerge(Decorator.getOutermost(this), new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y));

        return bigCommand;

    }
    private Command repositionTheRemote(ReloManeuverForProbe relo) {

        //Info Gathering: angle and position of the remote
        double globalRemoteAngle = this.getRotator().getAngle(); //remote angle
        double pentaDirectionAngle = relo.getPentaDir();

        //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
        double off2x = this.getPosition().getX();
        double off2y = this.getPosition().getY();

        double off1x = relo.getRemoteX();
        double off1y = relo.getRemoteY();

        double off1x_rot = rotX(off1x, off1y, globalRemoteAngle+pentaDirectionAngle);
        double off1y_rot = rotY(off1x, off1y, globalRemoteAngle+pentaDirectionAngle);

        double templateTurnsRemoteAngle = relo.getRemoteAngle(); //get the extra angle caused by the template to the ship
        Command bigCommand = null;
        //STEP 11: reposition the remote
        //Set the remote's final angle
        ChangeTracker changeTracker = new ChangeTracker(this);
        FreeRotator fRShip = (FreeRotator)Decorator.getDecorator(this.piece, FreeRotator.class);
        fRShip.setAngle(globalRemoteAngle - templateTurnsRemoteAngle);
        Command changeRotationCommand = changeTracker.getChangeCommand();
        if(bigCommand !=null) bigCommand.append(changeRotationCommand);
        else bigCommand = changeRotationCommand;

        //Remote's final translation command
        if(bigCommand != null) bigCommand.append(getMap().placeOrMerge(Decorator.getOutermost(this), new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y)));
        else bigCommand = getMap().placeOrMerge(Decorator.getOutermost(this), new Point((int)off1x_rot + (int)off2x, (int)off1y_rot + (int)off2y));

        return bigCommand;
    }
    private java.util.List<BumpableWithShape> findCollidingEntities(Shape myTestShape, java.util.List<BumpableWithShape> otherShapes) {
        java.util.List<BumpableWithShape> shapes = Lists.newLinkedList();
        for (BumpableWithShape otherBumpableShape : otherShapes) {
            if (shapesOverlap(myTestShape, otherBumpableShape.shape)) {
                shapes.add(otherBumpableShape);
            }
        }
        return shapes;
    }

    private int offerTripleChoices(java.util.List<ReloManeuverForProbe> reloTemplates, VASSAL.build.module.Map theMap, RepositionChoiceVisual rpcInput) {
        //Getting into this function, repoShip is associated with the template used to reposition the ship. We also need the non-mapped final ship tentative position

        // STEP 0: gather ship angle and rotator
        double remoteAngle = this.getRotator().getAngle(); //remote angle
        //FreeRotator fR = (FreeRotator) Decorator.getDecorator(piece, FreeRotator.class);
        int nbOfRedDots = 0;

        if(rpcInput._option==-77) {//buzz droid swarm case
            boolean wantOverlapColor = false;

            java.util.List<GamePiece> excludedPieces = Lists.newArrayList();
            excludedPieces.add(this.piece); //must not detect an overlap with this buzzdroid
            excludedPieces.add(rpcInput.associatedTargetPiece); //must exclude the victim ship

            java.util.List<BumpableWithShape> objects = OverlapCheckManager.getBumpablesOnMap(true, excludedPieces);

            double globalShipAngle =  ((FreeRotator) Decorator.getDecorator(getOutermost(rpcInput.associatedTargetPiece), FreeRotator.class)).getAngle();
            //STEP 1:
            //Info Gathering: Offset 2 get the center global coordinates of the ship calling this op
            double off2x = rpcInput.associatedTargetPiece.getPosition().getX();
            double off2y = rpcInput.associatedTargetPiece.getPosition().getY();

            double off1x = 0.0f;
            float sizeFudge = whichSizeShip((Decorator) Decorator.getOutermost(rpcInput.associatedTargetPiece),true);
            double off1y = -DOT_BUZZ_RADIUS - (sizeFudge-1) * 35.0f;

            double off1x_rot_front = rotX(off1x, off1y, globalShipAngle);
            double off1y_rot_front = rotY(off1x, off1y, globalShipAngle);

            double off1x_rot_back = rotX(off1x, off1y, globalShipAngle + 180.0f);
            double off1y_rot_back = rotY(off1x, off1y, globalShipAngle + 180.0f);


            float diam = DOT_DIAMETER + (sizeFudge-1) * 20.0f;
            //deal with the front dot
            Shape dot = new Ellipse2D.Float(-diam / 2, -diam / 2, diam, diam);
            dot = AffineTransform.
                    getTranslateInstance((int) off1x_rot_front + (int) off2x, (int) off1y_rot_front + (int) off2y).
                    createTransformedShape(dot);

            //check if the front buzz swarm shape would overlap something
            Shape buzzInFrontShape = repositionedBuzzSwarmShape(ReloManeuverForProbe.BuzzFront,rpcInput.associatedTargetPiece);
            if (buzzInFrontShape != null) {
                java.util.List<BumpableWithShape> overlappingShips = findCollidingEntities(buzzInFrontShape, objects);
                if (overlappingShips.size() > 0) {
                    wantOverlapColor = true;
                    nbOfRedDots++;
                }
            }
            RepositionChoiceVisual rpc = new RepositionChoiceVisual(null, dot, wantOverlapColor, "", -55, rpcInput.associatedTargetPiece, null, 0);
            rpcList.add(rpc);

            //deal with the back dot
            wantOverlapColor = false;

            Shape dot2 = new Ellipse2D.Float(-diam / 2, -diam / 2, diam, diam);
            dot2 = AffineTransform.
                    getTranslateInstance((int) off1x_rot_back + (int) off2x, (int) off1y_rot_back + (int) off2y).
                    createTransformedShape(dot2);
            //check if the front buzz swarm shape would overlap something
            Shape buzzInBackShape = repositionedBuzzSwarmShape(ReloManeuverForProbe.BuzzBack,rpcInput.associatedTargetPiece);
            if (buzzInBackShape != null) {
                java.util.List<BumpableWithShape> overlappingShips = findCollidingEntities(buzzInBackShape, objects);
                if (overlappingShips.size() > 0) {
                    wantOverlapColor = true;
                    nbOfRedDots++;
                }
            }
            RepositionChoiceVisual rpc2 = new RepositionChoiceVisual(null, dot2, wantOverlapColor, "", -66, rpcInput.associatedTargetPiece, null, 0);
            rpcList.add(rpc2);
        }
        else { // probe droid case
            //for now, Probe Droid case, the only other case
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

                double off1x_rot_s_dot = rotX(offLx, offLy, remoteAngle + (rpcInput._option-1)*72.0);
                double off1y_rot_s_dot = rotY(offLx, offLy, remoteAngle + (rpcInput._option-1)*72.0);

                dot = AffineTransform.
                        getTranslateInstance((int) off1x_rot_s_dot + (int) off2x, (int) off1y_rot_s_dot + (int) off2y).
                        createTransformedShape(dot);

                //STEP 11: reposition the ship
                //Add visuals according to the selection of repositioning

                RepositionChoiceVisual rpc = new RepositionChoiceVisual(null, dot, false, "", 4 + 3*rpcInput._option + spreadDotAngleFactor, null, null, 0);
                rpcList.add(rpc);
                spreadDotAngleFactor++;
                //return bigCommand;
            } // end of loop around the 3 templates used in the repositions
        }


        //FINAL STEP: add the visuala to the map and the mouse listener


        for(RepositionChoiceVisual r : rpcList){
            theMap.addDrawComponent(r);
        }

        final Decorator remoteToRelocate = this;
        final VASSAL.build.module.Map finalMap = theMap;
        final int finalCase = rpcInput._option;

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
                        RR.newNonKeyEvent(theChosenOne);

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

        return nbOfRedDots;
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


    //There were too many hotkeys for the mouse GUI, so it uses names instead
    private ReloManeuverForProbe getNewSystemRelo(int choice) {
        if (optionToRelocate.containsKey(choice)) {
            return optionToRelocate.get(choice);
        }
        return null;
    }
    //used at the end of triple choice sequences. Lots less to deal with
    public Command newNonKeyEvent(RepositionChoiceVisual rpc){
        //Deal with ship repositioning, including overlap detection for the templates used, including the triple choice keystrokes that lead to a mouse GUI
        ReloManeuverForProbe reloRemote = getNewSystemRelo(rpc._option);

        //Ship reposition requested
        if(reloRemote != null) {
            if(reloRemote==ReloManeuverForProbe.BuzzBack || reloRemote==ReloManeuverForProbe.BuzzFront){
                Command repoCommand = repositionBuzzSwarm(reloRemote, rpc.associatedTargetPiece);

                if(repoCommand == null) return null; //somehow did not get a programmed reposition command
                else{
                    repoCommand.append(logToChatCommand("*** The Buzz Swarm Droid has attached to " + rpc.associatedTargetPiece.getProperty("Pilot Name").toString() + " to the " + reloRemote.getRepoName()));
                    repoCommand.execute();
                    GameModule.getGameModule().sendAndLog(repoCommand);
                    return null;
                }
            }
            //detect that the ship's final position overlaps a ship or obstacle
            Command repoCommand = repositionTheRemote(reloRemote);
            if(repoCommand == null) return null; //somehow did not get a programmed reposition command
            else{
                repoCommand.append(logToChatCommand("*** The DRK-1 Probe Droid has repositioned"));
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
        return "Custom hyperspace launcher (mic.HyperspaceLauncher)";
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

    public static HyperspaceLauncher findRemoteRelocationDecorator(GamePiece activatedPiece) {
        return (HyperspaceLauncher)HyperspaceLauncher.getDecorator(activatedPiece,HyperspaceLauncher.class);
    }
}
