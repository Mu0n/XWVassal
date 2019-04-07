package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.*;
import VASSAL.counters.Stack;
import com.google.common.collect.ImmutableMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

import static mic.Util.*;

/**
 * Created by Mic on 09/08/2017.
 *
 * This source file manages every mouse event so that the ships can be driven by a non-modal mouse interface with buttons
 */
public class MouseShipGUI extends AbstractConfigurable {
    public static final String ID = "MouseShipGUI";
    GamePiece activatedPiece; //ship piece whose popup is active
    boolean secondStagePopup = false; //makes it easier to differ between first stage and 2nd stage
    MouseGUIDrawable lastPopup; //active popup drawable component with info on images, clickable areas, etc
    MouseListener ml;


    private static final int probeDroidGUIOption = 1;
    private static final int buzzSwarmGUIOption = 2;

    private static java.util.Map<String, Integer> remoteNameToGUIOption = ImmutableMap.<String, Integer>builder()
            .put("DRK-1 Probe Droid", probeDroidGUIOption)
            .put("Buzz Droid Swarm", buzzSwarmGUIOption)
            .build();

    public String[] getAttributeNames() {
        return new String[0];
    }

    public void setAttribute(String key, Object value) {

    }

    public String getAttributeValueString(String key) {
        return null;
    }

    public String[] getAttributeDescriptions() {
        return new String[0];
    }

    public Class<?>[] getAttributeTypes() {
        return new Class[0];
    }


    public void removeFrom(Buildable parent) {
        Map theMap = getTheMainMap();
        if(lastPopup.getClass().isInstance(MouseShipGUIDrawable.class)) theMap.removeDrawComponent((MouseShipGUIDrawable)lastPopup);
        else if(lastPopup.getClass().isInstance(MouseRemoteGUIDrawable.class)) theMap.removeDrawComponent((MouseRemoteGUIDrawable)lastPopup);
        theMap.removeLocalMouseListener(ml);
    }

    public HelpFile getHelpFile() {
        return null;
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public void addTo(Buildable parent) {
        final Map theMap = getTheMainMap();
        ml = new MouseListener() {
            public void mouseClicked(MouseEvent e) {}

            public void mousePressed(MouseEvent e) {
                //restrict the initial activation to ctrl-clicks
                if(e.isConsumed()) return;
                if(e.isControlDown() == false && activatedPiece == null) return;

                //Prep step of the GUI - will it activate?
                if(e.isControlDown() == true && activatedPiece == null) {
                    Collection<GamePiece> shipPieces = new ArrayList<GamePiece>();
                    Collection<GamePiece> remotePieces = new ArrayList<GamePiece>();

                    GamePiece[] gpArray = theMap.getAllPieces();
                    // scan all game pieces, keep only the ones we're sure are ships or remotes
                    for (int i = 0; i < gpArray.length; i++) {
                        try {
                            if (gpArray[i].getState().contains("this_is_a_ship")) {
                                shipPieces.add(gpArray[i]);
                            }
                            else if(gpArray[i].getState().contains("this_is_a_remote")){
                                remotePieces.add(gpArray[i]);
                            }
                        } catch (Exception ex) {
                            continue;
                        }
                    }
                    //scan which ship was clicked, keep it under activatedPiece
                    if (shipPieces.size() > 0) {
                        zerothStageShipGUI(e, theMap, shipPieces);
                    }//end of having ships on the map

                    //scan which remote was clicked, keep it under activatedPiece - AS it stands, this could override a ship if both objects in the the same clicked spot
                    if (remotePieces.size() > 0){
                        zerothStageRemoteGUI(e, theMap, remotePieces);
                    }
                }// end of popping up the zeroth step

                //There was already an activated piece, deal with the main GUI panel
                else if(activatedPiece!=null && lastPopup != null){
                    if(lastPopup.getClass().isInstance(MouseShipGUIDrawable.class)) firstStageShipGUI(e, theMap);
                    else if(lastPopup.getClass().isInstance(MouseRemoteGUIDrawable.class)) firstStageRemoteGUI(e, theMap);
                } //end of non-ship clicks while a ship is activated
            } //end of mousePressed Event
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        };
        theMap.addLocalMouseListener(ml);
    }

   private void firstStageRemoteGUI(MouseEvent e, Map theMap){
        logToChat("Reached the remote first stage");
        removePopup(theMap, e);
   }

    private boolean isPieceClicked(MouseEvent e, Map theMap, GamePiece candidate){
        Shape theShape = getTransformedPieceShape(candidate);
        Shape popupShape = new Rectangle(0, 0, 0, 0);
        if (lastPopup != null) {
            //figure out the shape of the active popup and allows clicks in it
            popupShape = new Rectangle(lastPopup.ulX, lastPopup.ulY, lastPopup.totalWidth, lastPopup.totalHeight);
            popupShape = getTransformedShape(popupShape, candidate);
        }
        if (theShape.contains(e.getX(), e.getY()) || popupShape.contains(e.getX(), e.getY())) return true;
        return false;
    }

    private void zerothStageRemoteGUI(MouseEvent e, Map theMap, Collection<GamePiece> remotePieces){
        for(GamePiece remote : remotePieces){
            if(isPieceClicked(e, theMap, remote)){
                if(activatedPiece != remote){
                    //gotta deactivate the last one before doing the new one
                    if(lastPopup.getClass().isInstance(MouseShipGUIDrawable.class)) theMap.removeDrawComponent((MouseShipGUIDrawable)lastPopup);
                    if(lastPopup.getClass().isInstance(MouseRemoteGUIDrawable.class)) theMap.removeDrawComponent((MouseRemoteGUIDrawable)lastPopup);
                } else if(remote == activatedPiece && lastPopup != null){
                    //clicking on a ship whose popup is already here, deal with buttons here
                    //TODO buttons
                    return;
                }
                //Go ahead and make this remote the active popup owner

                //final solution to fetch a ship's info
                String remoteName = (Decorator.getInnermost(remote)).getName();
                MouseRemoteGUIDrawable mrgd = null;

                if(remoteNameToGUIOption.containsKey(remoteName)){
                    mrgd = new MouseRemoteGUIDrawable(remote, theMap, remoteNameToGUIOption.get(remoteName));
                }else {
                    logToChat("*-- This remote does not yet have a mouse GUI implemented");
                    continue;
                }
                if(mrgd == null) {
                    logToChat("*-- Error making a GUI for this remote: " + remoteName);
                    continue;
                }
                logToChat("mouseShipGUI line 148 name: " + remoteName);
                //MouseShipGUIDrawable msgd = new MouseShipGUIDrawable(ship, theMap, pilotShip, pilot);
                //theMap.addDrawComponent(msgd);
                //theMap.repaint();

                logToChatWithoutUndo("*-- Welcome to the beta Mouse Graphical Interface. You got here by ctrl-left clicking on a remote. You can left-click on the dots to select the direction of the relocation. Click on the red X to close the popup");

                //save this ship and popup Drawable for future behavior
                activatedPiece = remote;
                lastPopup = mrgd;
                e.consume();
                return;
            }
        }
    }

    private void zerothStageShipGUI(MouseEvent e, Map theMap, Collection<GamePiece> shipPieces){
        for (GamePiece ship : shipPieces) {
            if(isPieceClicked(e, theMap, ship)) {
                if (activatedPiece != ship && lastPopup!=null) {
                    //gotta deactivate the last one before doing the new one
                    if(lastPopup.getClass().isInstance(MouseShipGUIDrawable.class)) theMap.removeDrawComponent((MouseShipGUIDrawable)lastPopup);
                    if(lastPopup.getClass().isInstance(MouseRemoteGUIDrawable.class)) theMap.removeDrawComponent((MouseRemoteGUIDrawable)lastPopup);
                } else if (ship == activatedPiece && lastPopup != null) {
                    //clicking on a ship whose popup is already here, deal with buttons here
                    //TODO buttons
                    return;
                }
                //Go ahead and make this ship the active popup owner
                final java.util.List<XWS2Pilots> allShips = XWS2Pilots.loadFromLocal();

                //final solution to fetch a ship's info
                String xwsStr = ship.getProperty("xws").toString();

                XWS2Pilots.Pilot2e pilot = XWS2Pilots.getSpecificPilot(xwsStr, allShips);
                XWS2Pilots pilotShip = XWS2Pilots.getSpecificShipFromPilotXWS2(xwsStr, allShips);
                            /*logToChat("Pilot name = " + pilot.getName() + " xws = " + pilot.getXWS()+ " who flies a " + pilotShip.getName());
                            logToChat("Hull Status: " + ship.getProperty("Hull Rating").toString() + "/" + pilotShip.getHull() + " Shield Rating: " + ship.getProperty("Shield Rating") + "/" + pilotShip.getShields());
                            logToChat("Attack Rating Front Arc: " + pilotShip.getFrontArc() + " Back Arc: " + pilotShip.getRearArc());*/
                MouseShipGUIDrawable msgd = new MouseShipGUIDrawable(ship, theMap, pilotShip, pilot);
                theMap.addDrawComponent(msgd);
                theMap.repaint();
                logToChatWithoutUndo("*-- Welcome to the beta Mouse Graphical Interface. You got here by ctrl-left clicking on a ship. You can left-click on the icons to perform \"things\" on the ship. Click on the red X to close the popup");

                //save this ship and popup Drawable for future behavior
                activatedPiece = ship;
                lastPopup = msgd;
                e.consume();
                return;
            } //end of ship clicks
        }//end of checking each ship
    }
    private void firstStageShipGUI(MouseEvent e, Map theMap)
    {
        for(MouseShipGUIDrawable.miElement elem : ((MouseShipGUIDrawable)lastPopup).listOfInteractiveElements){
            double scale = theMap.getZoom();
            AffineTransform af = elem.getTransformForClick(scale);
            Rectangle rawR = elem.image.getData().getBounds();
            Shape s = af.createTransformedShape(rawR);

            if(s.contains(e.getX(), e.getY())){
                //First class of GUI-driven commands: Direct keystroke commands, keep the GUI up
                if(elem.associatedKeyStroke!=null) {
                    Command directKeyStroke = activatedPiece.keyEvent(elem.associatedKeyStroke);
                    //directKeyStroke.execute();
                    GameModule.getGameModule().sendAndLog(directKeyStroke);
                    e.consume();
                }
                //Second class of GUI-driven commands: Goes to a 2nd step with triple click choices:
                else if(elem.whichTripleChoice > 0) {
                    logToChatWithoutUndo("Please click on a dot to reposition the ship. White dots = legal position. Red dots = illegal obstructed positions. Click on empty space to cancel this.");

                    e.consume();
                    ShipReposition SR = ShipReposition.findShipRepositionDecorator(activatedPiece);
                    Command tripleChoiceCommand = SR.tripleChoiceDispatcher(elem.whichTripleChoice, activatedPiece.getProperty("Pilot Name").toString());

                    tripleChoiceCommand.execute();
                    GameModule.getGameModule().sendAndLog(tripleChoiceCommand);

                    removePopup(theMap, e);
                }
                //keystroke is null and triplechoice is 0? must be a cosmetic non-interactive text or image
                else if(elem.whichTripleChoice == 0){
                    //do nothing
                    return;
                }
                //click on the close button
                else if(elem.getTripleChoice()== -66){
                    removePopup(theMap, e);
                    return;
                }
                else{
                    logToChatWithoutUndo("Error: failed to execute a mouse GUI command.");
                    removePopup(theMap, e);
                }
                break;
            }//end of scanning a particular element (click was detected inside its shape)
        } //end of scanning all the mouse interface elements for clicks
    }
   private void removePopup(Map theMap, MouseEvent e){
       activatedPiece=null;
       if(lastPopup!=null) {

           logToChat("test line 277 lastpopup is ship bool: " + lastPopup.getClass().isInstance(MouseShipGUIDrawable.class));
           if(lastPopup.getClass().isInstance(MouseShipGUIDrawable.class)) theMap.removeDrawComponent((MouseShipGUIDrawable)lastPopup);
           else if(lastPopup.getClass().isInstance(MouseRemoteGUIDrawable.class)) theMap.removeDrawComponent((MouseRemoteGUIDrawable)lastPopup);
           lastPopup=null;
           e.consume();
       }
   }

    private static VASSAL.build.module.Map getPlayerMap(int playerIndex) {
        for (VASSAL.build.module.Map loopMap : GameModule.getGameModule().getComponentsOf(VASSAL.build.module.Map.class)) {
            if (("Player " + Integer.toString(playerIndex)).equals(loopMap.getMapName())) {
                return loopMap;
            }
        }
        return null;
    }


}
