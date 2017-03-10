package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.PlayerHand;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.gamepieceimage.TextItem;
import VASSAL.build.module.map.DrawPile;
import VASSAL.build.module.map.SetupStack;
import VASSAL.counters.Deck;
import VASSAL.counters.GamePiece;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import static mic.Util.getCurrentPlayer;
import static mic.Util.logToChat;

/**
 * Created by mjuneau on 2017-03-09.
 */
public class EpicDeckTrayToggle extends AbstractConfigurable {

    private List<JButton> toggleButtons =  Lists.newArrayList();
    private boolean[] isHiding = new boolean[8];

    private void EpicMaskToggle(int playerId) {
    //playerId goes from 1 to 8, boolean array goes from 0 to 7

        mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
        if (playerInfo.getSide() != playerId) return;

        if(isHiding[playerId-1]){
            ShowOnePlayerEpic(playerId);
            isHiding[playerId-1] = false;
        }
        else {
            HideOnePlayerEpic(playerId);
            isHiding[playerId-1] = true;
        }
}

    private void HideOnePlayerEpic(int playerId) {
        toggleButtons.get(playerId-1).setText("Activate Epic");
        String nbPlayer = Integer.toString(playerId);
        String pString = "Player " + nbPlayer;
        PlayerHand.getMapById(pString).getBoardByName(pString).setAttribute("image","player_hand_background_mask.png");

        repaintTrayOutlines(pString, Color.WHITE);
        repaintTrayCounters(pString, Color.WHITE);

        logToChat("Must find a way to force refresh the background image change to player_hand_background_mask.png");
        PlayerHand.getMapById(pString).getBoardByName(pString).getMap().repaint();
    }


    private void ShowOnePlayerEpic(int playerId) {
        toggleButtons.get(playerId-1).setText("Disable Epic");
        String nbPlayer = Integer.toString(playerId);
        String pString = "Player " + nbPlayer;
        PlayerHand.getMapById(pString).getBoardByName(pString).setAttribute("image","player_hand_background.jpg");

        repaintTrayOutlines(pString, Color.BLACK);
        repaintTrayCounters(pString, Color.BLACK);

        logToChat("Must find a way to force refresh the background image change to player_hand_background.jpg");
        PlayerHand.getMapById(pString).getBoardByName(pString).getMap().repaint();
    }

    private void repaintTrayCounters(String pString, Color color) {
        List<SetupStack> setupStacks = PlayerHand.getMapById(pString).getAllDescendantComponentsOf(SetupStack.class);
        Iterator<SetupStack> myIter = setupStacks.iterator();
        String dontTouchThis = "Std Dam Counter";
        while(myIter.hasNext()) {
            SetupStack ss = myIter.next();
            if(dontTouchThis.equals(ss.getConfigureName())) continue;
            if(!ss.getConfigureName().contains("Counter")) continue;

            //Must access gamepiece's Text Label decorator and modify its color
            logToChat("Must access " + ss.getConfigureName() + "'s text label and paint it " + color.toString());

        }
    }

    private void repaintTrayOutlines(String pString, Color color) {
        List<DrawPile> dps = PlayerHand.getMapById(pString).getAllDescendantComponentsOf(DrawPile.class);
        Iterator<DrawPile> myIter = dps.iterator();
        String dontTouchThis = pString + " Std Damage Deck Tray";
        while(myIter.hasNext()) {
            Deck deck = myIter.next().getDeck();
            if(dontTouchThis.equals(deck.getDeckName())) continue;
            deck.setOutlineColor(color);
        }
    }
    public void addTo(Buildable parent) {
        for (int i = 1; i <= 8; i++) {
            final int playerId = i;

            JButton b = new JButton("Disable Epic");
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
