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
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

import static VASSAL.counters.Decorator.getOutermost;
import static mic.Util.*;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
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

    boolean wPressed = false;

    public static final int probeDroidGUIOption = 1;
    public static final int buzzSwarmGUIOption = 2;

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
        if(lastPopup instanceof MouseShipGUIDrawable) theMap.removeDrawComponent((MouseShipGUIDrawable)lastPopup);
        else if(lastPopup instanceof MouseRemoteGUIDrawable) theMap.removeDrawComponent((MouseRemoteGUIDrawable)lastPopup);
        theMap.removeLocalMouseListener(ml);
    }

    public HelpFile getHelpFile() {
        return null;
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public class IsKeyPressed {
        public boolean isWPressed() {
            synchronized (IsKeyPressed.class) {
                return wPressed;
            }
        }
    }

        public void addTo(Buildable parent) {
            final Map theMap = getTheMainMap();
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

                public boolean dispatchKeyEvent(KeyEvent ke) {
                    synchronized (IsKeyPressed.class) {

                        if(activatedPiece!=null){
                            switch (ke.getID()) {
                                case KeyEvent.KEY_PRESSED:
                                    if (ke.getKeyCode() == KeyEvent.VK_W) {
                                        wPressed = true;
                                        Iterator<MouseShipGUIElement> it = ((MouseShipGUIDrawable)lastPopup).guiElements.iterator();
                                        if(it.hasNext()){
                                            MouseShipGUIElement msge = it.next();
                                            msge.globalY--;
                                            logToChat("x: " + msge.globalX + " y: " + msge.globalY);
                                        }
                                    } //end if w code pressed
                                    if (ke.getKeyCode() == KeyEvent.VK_S) {
                                        wPressed = true;
                                        Iterator<MouseShipGUIElement> it = ((MouseShipGUIDrawable)lastPopup).guiElements.iterator();
                                        if(it.hasNext()) {
                                            MouseShipGUIElement msge = it.next();
                                            msge.globalY++;
                                            logToChat("x: " + msge.globalX + " y: " + msge.globalY);
                                        }
                                    }
                                    if (ke.getKeyCode() == KeyEvent.VK_A) {
                                        wPressed = true;
                                        Iterator<MouseShipGUIElement> it = ((MouseShipGUIDrawable)lastPopup).guiElements.iterator();
                                        if(it.hasNext()) {
                                            MouseShipGUIElement msge = it.next();
                                            msge.globalX--;
                                            logToChat("x: " + msge.globalX + " y: " + msge.globalY);
                                        }
                                    }
                                    if (ke.getKeyCode() == KeyEvent.VK_D) {
                                        wPressed = true;
                                        Iterator<MouseShipGUIElement> it = ((MouseShipGUIDrawable)lastPopup).guiElements.iterator();
                                        if(it.hasNext()) {
                                            MouseShipGUIElement msge = it.next();
                                            msge.globalX++;
                                            logToChat("x: " + msge.globalX + " y: " + msge.globalY);
                                        }
                                    }
                                    break;

                            } // end switch

                            theMap.repaint();
                        } //end if there's an activated piece

                        return false;
                    } //end synchronized method
                } //end dispatchKeyEvent
            }); //end key  focus manager

            ml = new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                }

                public void mousePressed(MouseEvent e) {
                    //restrict the initial activation to ctrl-clicks
                    if (e.isConsumed()) return;
                    if (e.isControlDown() == false && activatedPiece == null) return;

                    //Prep step of the GUI - will it activate?
                    if (e.isControlDown() == true && activatedPiece == null) {
                        Collection<GamePiece> shipPieces = new ArrayList<GamePiece>();
                        Collection<GamePiece> remotePieces = new ArrayList<GamePiece>();

                        GamePiece[] gpArray = theMap.getAllPieces();
                        // scan all game pieces, keep only the ones we're sure are ships
                        for (int i = 0; i < gpArray.length; i++) {
                            try {
                                if (gpArray[i].getState().contains("this_is_a_ship")) {
                                    shipPieces.add(gpArray[i]);
                                }
                            } catch (Exception ex) {
                                continue;
                            }
                        }
                        // scan all game pieces, keep only the ones we're sure are remotes
                        for (int i = 0; i < gpArray.length; i++) {
                            try {
                                if (gpArray[i].getState().contains("this_is_a_remote")) {
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
                        if (remotePieces.size() > 0) {
                            zerothStageRemoteGUI(e, theMap, remotePieces);
                        }
                    }// end of popping up the zeroth step

                    //There was already an activated piece, deal with the main GUI panel
                    else if (activatedPiece != null && lastPopup != null) {
                        if (lastPopup instanceof MouseShipGUIDrawable) {
                            firstStageShipGUI(e, theMap);
                        } else if (lastPopup instanceof MouseRemoteGUIDrawable) {
                            firstStageRemoteGUI(e, theMap);
                        }
                    } //end of non-ship clicks while a ship is activated
                } //end of mousePressed Event

                public void mouseReleased(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }
            };
            theMap.addLocalMouseListener(ml);
        }


        private boolean isPieceClicked(MouseEvent e, Map theMap, GamePiece candidate) {
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

        private void zerothStageRemoteGUI(MouseEvent e, Map theMap, Collection<GamePiece> remotePieces) {
            for (GamePiece remote : remotePieces) {
                if (isPieceClicked(e, theMap, remote)) {
                    if (activatedPiece != remote) {
                        //gotta deactivate the last one before doing the new one
                        if (lastPopup instanceof MouseShipGUIDrawable)
                            theMap.removeDrawComponent((MouseShipGUIDrawable) lastPopup);
                        if (lastPopup instanceof MouseRemoteGUIDrawable)
                            theMap.removeDrawComponent((MouseRemoteGUIDrawable) lastPopup);
                    } else if (remote == activatedPiece && lastPopup != null) {
                        //clicking on a ship whose popup is already here, deal with buttons here
                        //TODO buttons
                        return;
                    }
                    //Go ahead and make this remote the active popup owner

                    //final solution to fetch a ship's info
                    String remoteName = (Decorator.getInnermost(remote)).getName();
                    MouseRemoteGUIDrawable mrgd = null;

                    if (remoteNameToGUIOption.containsKey(remoteName)) {
                        mrgd = new MouseRemoteGUIDrawable(remote, theMap, remoteNameToGUIOption.get(remoteName));
                    } else {
                        logToChat("*-- This remote does not yet have a mouse GUI implemented");
                        continue;
                    }
                    if (mrgd == null) {
                        logToChat("*-- Error making a GUI for this remote: " + remoteName);
                        continue;
                    }
                    //MouseShipGUIDrawable msgd = new MouseShipGUIDrawable(ship, theMap, pilotShip, pilot);
                    if (mrgd != null) theMap.addDrawComponent(mrgd);
                    theMap.repaint();

                    logToChatWithoutUndo("*-- Welcome to the beta Mouse Graphical Interface. You got here by ctrl-left clicking on a remote. You can left-click on a dot to activate the next step of the relocation/attachment. You can click on an empty area to dismiss this.");

                    //save this ship and popup Drawable for future behavior
                    activatedPiece = remote;
                    lastPopup = mrgd;
                    e.consume();
                    return;
                }
            }
        }

        private void zerothStageShipGUI(MouseEvent e, Map theMap, Collection<GamePiece> shipPieces) {
            for (GamePiece ship : shipPieces) {
                if (isPieceClicked(e, theMap, ship)) {
                    if (activatedPiece != ship && lastPopup != null) {
                        //gotta deactivate the last one before doing the new one
                        if (lastPopup instanceof MouseShipGUIDrawable)
                            theMap.removeDrawComponent((MouseShipGUIDrawable) lastPopup);
                        if (lastPopup instanceof MouseRemoteGUIDrawable)
                            theMap.removeDrawComponent((MouseRemoteGUIDrawable) lastPopup);
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

        private void firstStageRemoteGUI(MouseEvent e, Map theMap) {
            for (RepositionChoiceVisual rpc : ((MouseRemoteGUIDrawable) lastPopup).rpcList) {
                if (rpc.theDot.contains(e.getX(), e.getY())) {
                    e.consume();

                    RemoteRelocation RL = RemoteRelocation.findRemoteRelocationDecorator(activatedPiece);
                    Command tripleChoiceCommand = RL.tripleChoiceDispatcher(rpc, theMap);

                    tripleChoiceCommand.execute();
                    GameModule.getGameModule().sendAndLog(tripleChoiceCommand);

                    removePopup(theMap, e);
                    return;
                }

            }
            //did not click on the dots
            logToChatWithoutUndo("You dismissed the GUI for the remote. Nothing was done. Ctrl-Click on the remote if you want to try again");
            removePopup(theMap, e);
        }

        private void firstStageShipGUI(MouseEvent e, Map theMap) {
            for (MouseShipGUIElement elem : ((MouseShipGUIDrawable) lastPopup).guiElements) {
                double scale = theMap.getZoom();
                AffineTransform af = elem.getTransformForClick(scale, lastPopup.ulX, lastPopup.ulY);
                // next line: old way, using the image to get a clickable bounds
                // Rectangle rawR = elem.image.getData().getBounds();
                Shape s = af.createTransformedShape(elem.getNonRect());


                if (s.contains(e.getX(), e.getY())) {
                    //First class of GUI-driven commands: Direct keystroke commands, keep the GUI up
                    if (elem.associatedKeyStroke != null) {
                        Command directKeyStroke = activatedPiece.keyEvent(elem.associatedKeyStroke);
                        //directKeyStroke.execute();
                        GameModule.getGameModule().sendAndLog(directKeyStroke);
                        e.consume();
                    }
                    //Second class of GUI-driven commands (ship repositions): Goes to a 2nd step with triple click choices:
                    else if (elem.whichTripleChoice > 0 && elem.whichTripleChoice < 200) {
                        logToChatWithoutUndo("Please click on a dot to reposition the ship. White dots = legal position. Red dots = illegal obstructed positions. Click on empty space to cancel this.");

                        e.consume();
                        ShipReposition SR = ShipReposition.findShipRepositionDecorator(activatedPiece);
                        Command tripleChoiceCommand = SR.tripleChoiceDispatcher(elem.whichTripleChoice, activatedPiece.getProperty("Pilot Name").toString());

                        tripleChoiceCommand.execute();
                        GameModule.getGameModule().sendAndLog(tripleChoiceCommand);

                        removePopup(theMap, e);
                    }
                    //keystroke is null and triplechoice is 0? must be a cosmetic non-interactive text or image
                    else if (elem.whichTripleChoice == 0) {
                        //do nothing
                        return;
                    }
                    //click on the close button
                    else if (elem.getTripleChoice() == -66) {
                        removePopup(theMap, e);
                        return;
                    } else {
                        logToChatWithoutUndo("Error: failed to execute a mouse GUI command.");
                        removePopup(theMap, e);
                    }
                    break;
                }//end of scanning a particular element (click was detected inside its shape)
            } //end of scanning all the mouse interface elements for clicks
        }

        private void removePopup(Map theMap, MouseEvent e) {
            activatedPiece = null;
            if (lastPopup != null) {
                if (lastPopup instanceof MouseShipGUIDrawable)
                    theMap.removeDrawComponent((MouseShipGUIDrawable) lastPopup);
                else if (lastPopup instanceof MouseRemoteGUIDrawable)
                    theMap.removeDrawComponent((MouseRemoteGUIDrawable) lastPopup);
                lastPopup = null;
                e.consume();
            }
        }

        private void removePopupButKeepActivatedPiece(Map theMap, MouseEvent e) {
            if (lastPopup != null) {
                if (lastPopup instanceof MouseShipGUIDrawable)
                    theMap.removeDrawComponent((MouseShipGUIDrawable) lastPopup);
                else if (lastPopup instanceof MouseRemoteGUIDrawable)
                    theMap.removeDrawComponent((MouseRemoteGUIDrawable) lastPopup);
                lastPopup = null;
                e.consume();
            }
        }

}
