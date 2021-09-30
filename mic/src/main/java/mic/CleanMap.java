package mic;

import static mic.Util.getCurrentPlayer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;
import java.util.List;

import javax.swing.*;

import VASSAL.counters.Decorator;
import com.google.common.collect.Lists;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.counters.Deck;
import VASSAL.counters.GamePiece;

/**
 * Created by mjuneau on 2017-04-09.
 * It's not included inside the module with the vassal editor
 * It doesn't seem to work as is to accomplish its goal of cleaning all orange shapes on the map at once
 * Not sure we even need it
 * Orange shapes are currently cleaned with the following events:
 * Maneuver is performed on any ship
 * 'c' key is hit if a bump has to be resolved
 * CTRL-D is performed on a bumped ship
 */



public class CleanMap extends AbstractConfigurable {

    JButton b = new JButton("Clean Map");

    private synchronized void cleanMap() {
        for (AutoBumpDecorator a : getMap().getComponentsOf(AutoBumpDecorator.class)) {
            if (a.previousCollisionVisualization != null) {
                getMap().removeDrawComponent(a.previousCollisionVisualization);
            }
        }
    }

    public void addTo(Buildable parent) {
            b.setAlignmentY(0.0F);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    cleanMap();
                }
            });
            getMap().getToolBar().add(b);
    }

    public void removeFrom(Buildable parent) {
        getMap().getToolBar().remove(b);
    }

    private Map getMap() {
        for (Map loopMap : GameModule.getGameModule().getComponentsOf(Map.class)) {
            if ("Contested Sector".equals(loopMap.getMapName())) {
                return loopMap;
            }
        }
        return null;
    }

    // <editor-fold desc="unused vassal hooks">
    @Override
    public String[] getAttributeNames() {
        return new String[]{};
    }

    @Override
    public void setAttribute(String s, Object o) {
        // No-op
    }

    @Override
    public String[] getAttributeDescriptions() {
        return new String[]{};
    }

    @Override
    public Class[] getAttributeTypes() {
        return new Class[]{};
    }

    @Override
    public String getAttributeValueString(String key) {
        return "";
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public HelpFile getHelpFile() {
        return null;
    }
    // </editor-fold>
}
