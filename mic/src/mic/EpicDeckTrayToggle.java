package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created by mjuneau on 2017-03-09.
 */
public class EpicDeckTrayToggle extends AbstractConfigurable {

    private List<JButton> toggleButtons =  Lists.newArrayList();
    private boolean isHiding = true;

    private void EpicMaskToggle(int playerId) {
    //load that piece from a hidden deck
        PieceSlot mask = new PieceSlot();

        GameModule mod = GameModule.getGameModule();
        for(PieceSlot slot: GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)) {
            if(slot.getGpId().equals("15")) {
                Point position = new Point(0,0);
                GamePiece maskPiece = slot.getPiece();
                Map map = getPlayerMap(playerId);
                maskPiece.setMap(map);
                maskPiece.setPosition(position);
                Command place = map.placeOrMerge(maskPiece,position);
                place.execute();
                mod.sendAndLog(place);
                break;
            }
        }
}

    public void addTo(Buildable parent) {
        for (int i = 1; i <= 8; i++) {
            final int playerId = i;

            JButton b = new JButton("Activate Epic");
            b.setAlignmentY(0.0F);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    EpicMaskToggle(playerId);
                }
            });
            toggleButtons.add(b);

            getPlayerMap(i).getToolBar().add(b);
        }
    }
    public void removeFrom(Buildable parent) {
        for (int i = 1; i <= 8; i++) {
            getPlayerMap(i).getToolBar().remove(toggleButtons.get(i - 1));
        }
    }
    private Map getPlayerMap(int playerIndex) {
        for (Map loopMap : GameModule.getGameModule().getComponentsOf(Map.class)) {
            if (("Player " + Integer.toString(playerIndex)).equals(loopMap.getMapName())) {
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
