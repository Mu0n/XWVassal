package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;

import static mic.Util.logToChat;

/**
 * Created by Mic on 09/08/2017.
 *
 * This source file manages every mouse event so that the ships can be driven by a non-modal mouse interface with buttons
 */
public class MouseShipGUI extends AbstractConfigurable  {
    public static final String ID = "MouseShipGUI";

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

    }

    public HelpFile getHelpFile() {
        return null;
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public void addTo(Buildable parent) {
        final Map theMap = getTheMainMap();
        MouseListener ml = new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                Collection<GamePiece> shipPieces = new ArrayList<GamePiece>();
                GamePiece[] gpArray = theMap.getAllPieces();
                logToChat(gpArray.length + " pieces detected");
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
                logToChat(shipPieces.size() + " of these are ships");
                if(shipPieces.size()>0){
                    logToChat(e.getClickCount() + "th click and you clicked here " + e.getX() + "," + e.getY());
                }
            }

            public void mousePressed(MouseEvent e) {

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


    private Map getTheMainMap(){
        for (Map loopMap : GameModule.getGameModule().getComponentsOf(Map.class)) {
            if (("Contested Sector").equals(loopMap.getMapName())) {
                return loopMap;
            }
        }
        return null;
    }
}
