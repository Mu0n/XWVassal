package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.counters.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.*;

import static mic.Util.logToChat;

/**
 * Created by Mic on 09/08/2017.
 *
 * This source file manages every mouse event so that the ships can be driven by a non-modal mouse interface with buttons
 */
public class MouseShipGUI extends AbstractConfigurable {
    public static final String ID = "MouseShipGUI";
    GamePiece activatedPiece;
    MouseShipGUIDrawable lastPopup;
    MouseListener ml;

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
        theMap.removeDrawComponent(lastPopup);
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
            public void mouseClicked(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
                Collection<GamePiece> shipPieces = new ArrayList<GamePiece>();
                GamePiece[] gpArray = theMap.getAllPieces();
                for (int i = 0; i < gpArray.length; i++)
                {
                    try{
                        if(gpArray[i].getState().contains("this_is_a_ship")){
                            shipPieces.add(gpArray[i]);
                        }
                    }catch(Exception ex){
                        continue;
                    }
                }
                if(shipPieces.size()>0){
                    for(GamePiece ship : shipPieces){

                        Shape theShape = getTransformedPieceShape(ship);

                        if(theShape.contains(e.getX(),e.getY()))
                        {
                            if(activatedPiece != ship ){
                                //gotta deactivate the last one before doing the new one
                                theMap.removeDrawComponent(lastPopup);
                            }else if (ship==activatedPiece && lastPopup!=null){
                                //clicking on a ship whose popup is already here, deal with buttons here
                                //TODO buttons
                                break;
                            }
                            //Go ahead and make this ship the active popup owner
                            final java.util.List<XWS2Pilots> allShips = XWS2Pilots.loadFromLocal();

                            //final solution to fetch a ship's info
                            String xwsStr = ship.getProperty("xws").toString();


                            XWS2Pilots.Pilot2e pilot = XWS2Pilots.getSpecificPilot(xwsStr, allShips);
                            XWS2Pilots pilotShip = XWS2Pilots.getSpecificShipFromPilotXWS2(xwsStr,allShips);
                            logToChat("Pilot name = " + pilot.getName() + " xws = " + pilot.getXWS()+ " who flies a " + pilotShip.getName());
                            logToChat("Hull Status: " + ship.getProperty("Hull Rating").toString() + "/" + pilotShip.getHull() + " Shield Rating: " + ship.getProperty("Shield Rating") + "/" + pilotShip.getShields());
                            logToChat("Attack Rating Front Arc: " + pilotShip.getFrontArc() + " Back Arc: " + pilotShip.getRearArc());
                            MouseShipGUIDrawable msgd = new MouseShipGUIDrawable( (int)ship.getPosition().getX() + ship.getShape().getBounds().width + 50,
                                    (int)ship.getPosition().getY() - 50,1000,150, theMap, pilotShip, pilot);
                            theMap.addDrawComponent(msgd);
                            theMap.repaint();

                            //save this ship and popup Drawable for future behavior
                            activatedPiece = ship;
                            lastPopup=msgd;
                            break;
                        }
                        else{ //clicked outside of a ship, deactivate the popup and remove the component
                            activatedPiece = null;
                            if(lastPopup!=null) {
                                theMap.removeDrawComponent(lastPopup);
                                lastPopup=null;
                            }
                        }
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {



            }

            public void mouseExited(MouseEvent e) {

            }
        };
        theMap.addLocalMouseListener(ml);
    }

    private static Shape getTransformedPieceShape(GamePiece piece) {
        Shape rawShape = piece.getShape();
        Shape transformed = AffineTransform
                .getTranslateInstance(piece.getPosition().getX(), piece.getPosition().getY())
                .createTransformedShape(rawShape);

        FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(piece), FreeRotator.class));
        double centerX = piece.getPosition().getX();
        double centerY = piece.getPosition().getY();
        transformed = AffineTransform
                .getRotateInstance(rotator.getAngleInRadians(), centerX, centerY)
                .createTransformedShape(transformed);

        return transformed;
    }

    static public Map getTheMainMap(){
        for (Map loopMap : GameModule.getGameModule().getComponentsOf(Map.class)) {
            if (("Contested Sector").equals(loopMap.getMapName())) {
                return loopMap;
            }
        }
        return null;
    }
}
