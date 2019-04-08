package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.*;
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
    //Section for when you only want to place a template on the side of the start position of a repositioning
    //small normal BR
    Fwd_1("Forward 2 from 1st orientation", "525", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Fwd_2("Forward 2 from 2nd orientation", "525", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Fwd_3("Forward 2 from 3rd orientation", "525", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Fwd_4("Forward 2 from 4th orientation", "525", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Fwd_5("Forward 2 from 5th orientation", "525", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Left_1("Bank 2 Left from 1st orientation", "519", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Left_2("Bank 2 Left from 2nd orientation", "519", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Left_3("Bank 2 Left from 3rd orientation", "519", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Left_4("Bank 2 Left from 4th orientation", "519", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Left_5("Bank 2 Left from 5th orientation", "519", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Right_1("Bank 2 Right from 1st orientation", "519", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Right_2("Bank 2 Right from 2nd orientation", "519", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Right_3("Bank 2 Right from 3rd orientation", "519", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Right_4("Bank 2 Right from 4th orientation", "519", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f),
    Right_5("Bank 2 Right from 5th orientation", "519", -90.0f, -169.5f, 0.0f, 0.0f, -339.0f, -28.25f);

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
    List<RepositionChoiceVisual> rpcList = Lists.newArrayList();

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

        private int offerTripleChoices(java.util.List<ReloManeuverForProbe> reloTemplates, VASSAL.build.module.Map theMap, int which) {
        //Getting into this function, repoShip is associated with the template used to reposition the ship. We also need the non-mapped final ship tentative position

        // STEP 0: gather ship angle and rotator
        double remoteAngle = this.getRotator().getAngle(); //remote angle
        //FreeRotator fR = (FreeRotator) Decorator.getDecorator(piece, FreeRotator.class);

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

            double off1x_s = reloTemplate.getRemoteX();
            double off1y_s = reloTemplate.getRemoteY();

            //STEP 7: rotate the offset1 dependant within the spawner's local coordinates

            double off1x_rot_s_dot = rotX(off1x_s, off1y_s, remoteAngle);
            double off1y_rot_s_dot = rotY(off1x_s, off1y_s, remoteAngle);

            dot = AffineTransform.
                    getTranslateInstance((int) off1x_rot_s_dot + (int) off2x, (int) off1y_rot_s_dot + (int) off2y).
                    createTransformedShape(dot);

            //STEP 11: reposition the ship
            //Add visuals according to the selection of repositioning

            RepositionChoiceVisual rpc = new RepositionChoiceVisual(null, dot, false, "", which + 5);
            rpcList.add(rpc);

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
                       // RemoteRelocation RR = findShipRepositionDecorator(shipToReposition);
                     //   RR.newNonKeyEvent(theChosenOne._option);

                        closeMouseListener(finalMap, ml);
                        return;
                    }
                }catch(Exception exce){
                    removeVisuals(finalMap);
                    logToChat("caught an exception while resolving remote relocation");
                    closeMouseListener(finalMap, ml);
                    return;
                }

                if(slightMisclick) return; //misclick outside of a dot, but inside the ship shapes, do nothing, don't dismiss the GUI
                else{ //was not in any dot, any ship area, close the whole thing down
                    removeVisuals(finalMap);
                    Command stopItAll = stopTripleChoiceMakeNextReady();
                    String stoppingPlayerName = getCurrentPlayer().getName();
                    if(stopItAll!=null) stopItAll.execute();
                    logToChat("*-- " + stoppingPlayerName + " is cancelling a relocation");

                    closeMouseListener(finalMap, ml);
                    return;
                }
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
        return this.piece.getShape();;
    }

    public String getName() {
        return this.piece.getName();
    }

    public static RemoteRelocation findRemoteRelocationDecorator(GamePiece activatedPiece) {
        return (RemoteRelocation)RemoteRelocation.getDecorator(activatedPiece,RemoteRelocation.class);
    }
}
