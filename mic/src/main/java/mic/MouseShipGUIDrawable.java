package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.Drawable;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.counters.Decorator;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.GamePiece;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.FileArchive;
import com.google.common.collect.Lists;
//import javafx.scene.transform.Affine;
import mic.ota.OTAContentsChecker;
import mic.ota.OTAMasterShips;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.Key;
import java.text.AttributedString;
import java.util.Collection;

import java.util.List;

import static mic.Util.*;

/**
 * Created by Mic on 2019-01-17.
 *
 * This class prepares the drawable so that the vassal engine knows when to draw stuff. No encoder is used since the UI is not shared to others
 */
public class MouseShipGUIDrawable extends MouseGUIDrawable implements Drawable {
    private static final int MAXBUMPABLECOUNT = 1;
    private static final float SMALLSHIPGUIRADIUSBASE = 215 ;
    GamePiece _shipPiece;
    Map _map;
    XWS2Pilots _pilotShip;
    XWS2Pilots.Pilot2e _pilot;
    int smallGapX = 5;
    int padX = 20;
    int padY = 20;
    int cursorX = padX;
    int cursorY = padY;
    List<BumpableWithShape> drawThese = Lists.newArrayList();
    List<Shape> andThese = Lists.newArrayList();

    public Collection<MouseShipGUIElement> guiElements = Lists.newArrayList();

    //Collection<miElement> listOfInteractiveElements = Lists.newArrayList();
    double scale;
    public MouseEvent summoningEvent;


    //3rd iteration of the mouse ship GUI using the spiffy gfx from Jomblr
    public void MouseShipGUIJomblr(GamePiece shipPiece, Map map, XWS2Pilots pilotShip, XWS2Pilots.Pilot2e pilot){
        int smallShipTripleChoice = 0;
        boolean isSmall = false;
        //logToChat(pilotShip.toString());
        if(pilotShip.getSize().equals("Small")) isSmall = true;

        // *
        // * Movement/Slam buttons
        // *

        // * Extra page button
        MouseShipGUIElement EXTRA = new MouseShipGUIElement(0, "TOG_EXT", "Toggle_Extra_Active.png", "Toggle_Extra_Inactive.png", 50,398,
                null, -194);

        // * Moves page button
        MouseShipGUIElement MOVESLAM = new MouseShipGUIElement(0, "TOG_MOV", "Toggle_Move_Active.png", "Toggle_Move_Inactive.png", 382,398,
                null, -195);

        // * Straights
        MouseShipGUIElement STRAIGHT5 = new MouseShipGUIElement(5, "MI_MOVE", "mi_straight.png", 129,74,
                KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a straight 5");
        MouseShipGUIElement STRAIGHT4 = new MouseShipGUIElement(5, "MI_MOVE", "mi_straight.png", 129,139,
                KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a straight 4");
        MouseShipGUIElement STRAIGHT3 = new MouseShipGUIElement(5, "MI_MOVE", "mi_straight.png", 129,204,
                KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a straight 3");
        MouseShipGUIElement STRAIGHT2 = new MouseShipGUIElement(5, "MI_MOVE", "mi_straight.png", 129,269,
                KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a straight 2");
        MouseShipGUIElement STRAIGHT1 = new MouseShipGUIElement(5, "MI_MOVE", "mi_straight.png", 129,334,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.SHIFT_DOWN_MASK, false),0, " perform a straight 1");

        // * K-turns
        MouseShipGUIElement KTURN5 = new MouseShipGUIElement(5, "MI_MOVE", "mi_kturn.png", 340,74,
                KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.CTRL_DOWN_MASK + KeyEvent.ALT_DOWN_MASK, false), 0," perform a k-turn 5");
        MouseShipGUIElement KTURN4 = new MouseShipGUIElement(5, "MI_MOVE", "mi_kturn.png", 340,139,
                KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_DOWN_MASK + KeyEvent.ALT_DOWN_MASK, false), 0," perform a k-turn 4");
        MouseShipGUIElement KTURN3 = new MouseShipGUIElement(5, "MI_MOVE", "mi_kturn.png", 340,204,
                KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK + KeyEvent.ALT_DOWN_MASK, false), 0," perform a k-turn 3");
        MouseShipGUIElement KTURN2 = new MouseShipGUIElement(5, "MI_MOVE", "mi_kturn.png", 340,269,
                KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK + KeyEvent.ALT_DOWN_MASK, false), 0," perform a k-turn 2");
        MouseShipGUIElement KTURN1 = new MouseShipGUIElement(5, "MI_MOVE", "mi_kturn.png", 340,334,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK + KeyEvent.ALT_DOWN_MASK, false), 0, " perform a k-turn 1");

        // * Left Banks
        MouseShipGUIElement LEFTBANK3 = new MouseShipGUIElement(5, "MI_MOVE", "mi_bank_left.png", 76,9,
                KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a left bank 3");
        MouseShipGUIElement LEFTBANK2 = new MouseShipGUIElement(5, "MI_MOVE", "mi_bank_left.png", 76,74,
                KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a left bank 2");
        MouseShipGUIElement LEFTBANK1 = new MouseShipGUIElement(5, "MI_MOVE", "mi_bank_left.png", 76,139,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a left bank 1");

        // * Left Tallon Rolls
        MouseShipGUIElement LEFTTROLL3 = new MouseShipGUIElement(5, "MI_MOVE", "mi_tallon_left.png", 23,215,
                KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a left tallon roll 3");
        MouseShipGUIElement LEFTTROLL2 = new MouseShipGUIElement(5, "MI_MOVE", "mi_tallon_left.png", 23,280,
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a left tallon roll 2");
        MouseShipGUIElement LEFTTROLL1 = new MouseShipGUIElement(5, "MI_MOVE", "mi_tallon_left.png", 23,345,
                KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a left tallon roll 1");

        // * Left Segnor's Loops
        MouseShipGUIElement LEFTSLOOP3 = new MouseShipGUIElement(5, "MI_MOVE", "mi_sloop_left.png", 76,204,
                KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a left segnor's loop 3");
        MouseShipGUIElement LEFTSLOOP2 = new MouseShipGUIElement(5, "MI_MOVE", "mi_sloop_left.png", 76,269,
                KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a left segnor's loop 2");
        MouseShipGUIElement LEFTSLOOP1 = new MouseShipGUIElement(5, "MI_MOVE", "mi_sloop_left.png", 76,334,
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a left segnor's loop 1");

        // * Left Turns
        MouseShipGUIElement LEFTTURN3 = new MouseShipGUIElement(5, "MI_MOVE", "mi_turn_left.png", 23,20,
                KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a left turn 3");
        MouseShipGUIElement LEFTTURN2 = new MouseShipGUIElement(5, "MI_MOVE", "mi_turn_left.png", 23,85,
                KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK+ KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a left turn 2");
        MouseShipGUIElement LEFTTURN1 = new MouseShipGUIElement(5, "MI_MOVE", "mi_turn_left.png", 23,150,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK+ KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a left turn 1");

        // * Right Banks
        MouseShipGUIElement RIGHTBANK3 = new MouseShipGUIElement(5, "MI_MOVE", "mi_bank_right.png", 393,9,
                KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a right bank 3");
        MouseShipGUIElement RIGHTBANK2 = new MouseShipGUIElement(5, "MI_MOVE", "mi_bank_right.png", 393,74,
                KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a right bank 2");
        MouseShipGUIElement RIGHTBANK1 = new MouseShipGUIElement(5, "MI_MOVE", "mi_bank_right.png", 393,139,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a right bank 1");

        // * Right Tallon Rolls
        MouseShipGUIElement RIGHTTROLL3 = new MouseShipGUIElement(5, "MI_MOVE", "mi_tallon_right.png", 446,215,
                KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a right tallon roll 3");
        MouseShipGUIElement RIGHTTROLL2 = new MouseShipGUIElement(5, "MI_MOVE", "mi_tallon_right.png", 446,280,
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a right tallon roll 2");
        MouseShipGUIElement RIGHTTROLL1 = new MouseShipGUIElement(5, "MI_MOVE", "mi_tallon_right.png", 446,345,
                KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a right tallon roll 1");

        // * Right Segnor's Loops
        MouseShipGUIElement RIGHTSLOOP3 = new MouseShipGUIElement(5, "MI_MOVE", "mi_sloop_right.png", 393,204,
                KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a right segnor's loop 3");
        MouseShipGUIElement RIGHTSLOOP2 = new MouseShipGUIElement(5, "MI_MOVE", "mi_sloop_right.png", 393,269,
                KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a right segnor's loop 2");
        MouseShipGUIElement RIGHTSLOOP1 = new MouseShipGUIElement(5, "MI_MOVE", "mi_sloop_right.png", 393,334,
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a right segnor's loop 1");

        // * Right Turns
        MouseShipGUIElement RIGHTTURN3 = new MouseShipGUIElement(5, "MI_MOVE", "mi_turn_right.png", 446,20,
                KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a right turn 3");
        MouseShipGUIElement RIGHTTURN2 = new MouseShipGUIElement(5, "MI_MOVE", "mi_turn_right.png", 446,85,
                KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK+ KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a right turn 2");
        MouseShipGUIElement RIGHTTURN1 = new MouseShipGUIElement(5, "MI_MOVE", "mi_turn_right.png", 446,150,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK+ KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a right turn 1");


        // * Reverse
        MouseShipGUIElement REVERSE = new MouseShipGUIElement(5, "MI_MOVE", "mi_reverse.png", 235,20,
                KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a reverse 1");
        MouseShipGUIElement REVERSE2 = new MouseShipGUIElement(5, "MI_MOVE", "mi_reverse.png", 235,85,
                KeyStroke.getKeyStroke(KeyEvent.VK_7, KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a reverse 2");
        MouseShipGUIElement REVERSELEFT = new MouseShipGUIElement(5, "MI_MOVE", "mi_reverse_left.png", 182,20,
                KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a reverse left");
        MouseShipGUIElement REVERSERIGHT = new MouseShipGUIElement(5, "MI_MOVE", "mi_reverse_right.png", 288,20,
                KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a reverse right");



        // *
        // * Close button
        // *

        MouseShipGUIElement CLOSE = new MouseShipGUIElement(0, null, "mi_close.png", 228,154, null, -66, null);
        // *
        // * Misc icons
        // *

        //ship rotations
        MouseShipGUIElement SFOILS = new MouseShipGUIElement(4,"TOG_BR", "mi_sfoils.png", 218, 21,
                KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.ALT_DOWN_MASK, false), 0,null);
        MouseShipGUIElement ROT180 = new MouseShipGUIElement(4,"TOG_ARC", "mi_180.png", 217, 86,
                KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK, false), 0," flip around 180 degrees");

        MouseShipGUIElement CW1 = new MouseShipGUIElement(4,"TOG_TUR", "mi_cw_1.png", 321, 206,
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK, false), -999," rotate 1 degree clockwise");
        MouseShipGUIElement CW15 = new MouseShipGUIElement(4,"TOG_BR", "mi_cw_15.png", 397, 198,
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), -999," rotate 15 degrees clockwise");
        MouseShipGUIElement CW90 = new MouseShipGUIElement(4,"TOG_CON", "mi_cw_90.png", 407, 260,
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK, false), -999," rotate 90 degrees clockwise");

        MouseShipGUIElement CCW1 = new MouseShipGUIElement(4,"TOG_CON", "mi_ccw_1.png", 113, 206,
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK, false), -999," rotate 1 degree counter-clockwise");
        MouseShipGUIElement CCW15 = new MouseShipGUIElement(4,"TOG_BR", "mi_ccw_15.png", 38, 198,
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), -999," rotate 15 degrees counter-clockwise");
        MouseShipGUIElement CCW90 = new MouseShipGUIElement(4,"TOG_TUR", "mi_ccw_90.png", 29, 260,
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK, false), -999," rotate 90 degrees counter-clockwise");

        //1 pixel translations
        MouseShipGUIElement DISPLACEUP = new MouseShipGUIElement(4, "ARROW_UP", "mi_arrow_up.png", 407,29,
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, false), -999," move up 1 pixel");
        MouseShipGUIElement DISPLACEDOWN = new MouseShipGUIElement(4, "ARROW_DOWN", "mi_arrow_down.png", 406,97,
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, false), -999," move down 1 pixel");
        MouseShipGUIElement DISPLACELEFT = new MouseShipGUIElement(4, "ARROW_LEFT", "mi_arrow_left.png", 355,63,
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK, false), -999," move left 1 pixel");
        MouseShipGUIElement DISPLACERIGHT = new MouseShipGUIElement(4, "ARROW_RIGHT", "mi_arrow_right.png", 458,64,
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK, false), -999," move right 1 pixel");

        MouseShipGUIElement LAUNCH = new MouseShipGUIElement(4, "TOG_ARC", "mi_launch_device.png", 72,19,
                KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0," launch a device");
        MouseShipGUIElement DROP = new MouseShipGUIElement(4, "TOG_BR", "mi_drop_device.png", 73,86,
                KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK, false), 0," drop a device");

        MouseShipGUIElement CONPAGE = new MouseShipGUIElement(0,"TOG_CON", "Toggle_Conditions_Active.png", "Toggle_Conditions_Inactive.png",295, 397, null, -196);

        // *
        // * Turret icons
        // *
                MouseShipGUIElement ROTATE_LEFT = new MouseShipGUIElement(3,"TOG_TUR", "mi_rotate_left.png", 138, 92,
                KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), -999,"rotate the turret counter-clockwise");

                MouseShipGUIElement TURRET_ARC = new MouseShipGUIElement(3,"TOG_ARC", "mi_turret_arc.png", 217, 20,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_DOWN_MASK, false), 0, null);
                MouseShipGUIElement TURRET_123 = new MouseShipGUIElement(3,"TOG_BR", "mi_turret_123.png", 218, 88,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0, null);

                MouseShipGUIElement ROTATE_RIGHT = new MouseShipGUIElement(3,"TOG_CON", "mi_rotate_right.png", 296, 92,
                KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.ALT_DOWN_MASK, false), -999, " rotate the turret clockwise");
        // *
        // * Boost icons
        // *
        MouseShipGUIElement BOOST_LEFT = new MouseShipGUIElement(1,"BO_LEFT", "Boost_Left.png", 155, 64,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a boost bank left");
        MouseShipGUIElement BOOST_STR8 = new MouseShipGUIElement(1,"BO_STR8", "Boost_Front.png", 228, 54,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.SHIFT_DOWN_MASK, false), 0," perform a boost forward");
        MouseShipGUIElement BOOST_RIGHT = new MouseShipGUIElement(1,"BO_RIGHT", "Boost_Right.png", 295, 64,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a boost bank right");
        MouseShipGUIElement TURRETPAGE = new MouseShipGUIElement(0,"TOG_TUR", "Toggle_Turret_Active.png",
                "Toggle_Turret_Inactive.png",139, 397, null, -197);

        // *
        // * Barrel roll left straight
        // *
        //medium and large ships have a lot less barrel roll options so while these buttons still show up, they default to either BR left 1 or BR right 1, centered
        if(isSmall) smallShipTripleChoice = 3;
        else smallShipTripleChoice = 1;
         MouseShipGUIElement BR_L2 = new MouseShipGUIElement(1,"BR_L2", "Barrel_L_Side_2.png", 36, 226, null, smallShipTripleChoice, null);
         MouseShipGUIElement BR_L1 = new MouseShipGUIElement(1,"BR_L1", "Barrel_L_Side_1.png", 110, 230, null, 1, null);

        // *
        // * Barrel roll left banks
        // *
        if(isSmall) smallShipTripleChoice = 9;
        else smallShipTripleChoice = 1;
        MouseShipGUIElement BR_LF2 = new MouseShipGUIElement(1,"BR_LF2", "Barrel_L_Front_2.png", 64, 124, null, smallShipTripleChoice, null);
        if(isSmall) smallShipTripleChoice = 5;
        else smallShipTripleChoice = 1;
        MouseShipGUIElement BR_LF1 = new MouseShipGUIElement(1,"BR_LF1", "Barrel_L_Front_1.png", 120, 149, null, smallShipTripleChoice, null);
        if(isSmall) smallShipTripleChoice = 11;
        else smallShipTripleChoice = 1;
        MouseShipGUIElement BR_LB2 = new MouseShipGUIElement(1, "BR_LB2", "Barrel_L_Back_2.png", 63, 323, null, smallShipTripleChoice, null);
        if(isSmall) smallShipTripleChoice = 6;
        else smallShipTripleChoice = 1;
        MouseShipGUIElement BR_LB1 = new MouseShipGUIElement(1,"BR_LB1", "Barrel_L_Back_1.png", 117, 301, null, smallShipTripleChoice, null);

        // *
        // * center square base
        // *
         MouseShipGUIElement CENTER = new MouseShipGUIElement(0,null, "Base.png", 200, 200, null, -66, null);

        // *
        // * Barrel roll right banks
        // *
        if(isSmall) smallShipTripleChoice = 10;
        else smallShipTripleChoice = 2;
        MouseShipGUIElement BR_RF2 = new MouseShipGUIElement(1,"BR_RF2", "Barrel_R_Front_2.png", 377 , 123, null, smallShipTripleChoice, null);
        if(isSmall) smallShipTripleChoice = 7;
        else smallShipTripleChoice = 2;
        MouseShipGUIElement BR_RF1 = new MouseShipGUIElement(1,"BR_RF1", "Barrel_R_Front_1.png", 320, 149, null, smallShipTripleChoice, null);
        if(isSmall) smallShipTripleChoice = 12;
        else smallShipTripleChoice = 2;
        MouseShipGUIElement BR_RB2 = new MouseShipGUIElement(1,"BR_RB2", "Barrel_R_Back_2.png", 377, 321, null, smallShipTripleChoice, null);
        if(isSmall) smallShipTripleChoice = 8;
        else smallShipTripleChoice = 2;
        MouseShipGUIElement BR_RB1 = new MouseShipGUIElement(1,"BR_RB1", "Barrel_R_Back_1.png", 320, 301, null, smallShipTripleChoice, null);

        // *
        // * Barrel roll right straight
        // *
         MouseShipGUIElement BR_R1 = new MouseShipGUIElement(1,"BR_R1", "Barrel_R_Side_1.png",325, 230, null, 2, null);
         if(isSmall) smallShipTripleChoice = 4;
        else smallShipTripleChoice = 2;
         MouseShipGUIElement BR_R2 = new MouseShipGUIElement(1,"BR_R2", "Barrel_R_Side_2.png",404, 225, null, smallShipTripleChoice, null);

        MouseShipGUIElement ROLLPAGE = new MouseShipGUIElement(0,"TOG_BR", "Toggle_Barrel_Active.png", "Toggle_Barrel_Inactive.png",217, 395, null, -199);
        MouseShipGUIElement ARCPAGE = new MouseShipGUIElement(0,"TOG_ARC", "Toggle_Arc_Active.png", "Toggle_Arc_Inactive.png",217, 327, null, -198);


        // *
        // * SHIP STATS
        // *
        MouseShipGUIElement SHIELD_STAT = new MouseShipGUIElement(0,null, "mi_shield_stat.png", 48, 462, null, 0, null);
        MouseShipGUIElement SHIELD_UP = new MouseShipGUIElement(0,null, "mi_stat_up.png", 110,462,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK, false), 0, null);
        MouseShipGUIElement SHIELD_DOWN = new MouseShipGUIElement(0,null, "mi_stat_down.png", 172,462,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK + KeyEvent.CTRL_DOWN_MASK, false), 0, null);

        MouseShipGUIElement FORCE_STAT = new MouseShipGUIElement(0,null, "mi_force_stat.png", 280, 536, null, 0, null);
        MouseShipGUIElement FORCE_UP = new MouseShipGUIElement(0,null, "mi_stat_up.png", 342,536,
                KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.SHIFT_DOWN_MASK, false), 0, null);
        MouseShipGUIElement FORCE_DOWN = new MouseShipGUIElement(0,null, "mi_stat_down.png", 404,536,
                KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.SHIFT_DOWN_MASK + KeyEvent.CTRL_DOWN_MASK, false), 0, null);

        MouseShipGUIElement HULL_STAT = new MouseShipGUIElement(0,null, "mi_hull_stat.png", 48, 536, null, 0, null);
        MouseShipGUIElement HULL_UP = new MouseShipGUIElement(0,null, "mi_stat_up.png", 110,536,
                KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_DOWN_MASK, false), 0, null);
        MouseShipGUIElement HULL_DOWN = new MouseShipGUIElement(0,null, "mi_stat_down.png", 172,536,
                KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_DOWN_MASK + KeyEvent.CTRL_DOWN_MASK, false), 0, null);

        MouseShipGUIElement CHARGE_STAT = new MouseShipGUIElement(0,null, "mi_charge_stat.png", 280, 462, null, 0, null);
        MouseShipGUIElement CHARGE_UP = new MouseShipGUIElement(0,null, "mi_stat_up.png", 342,462,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_DOWN_MASK, false), 0, null);
        MouseShipGUIElement CHARGE_DOWN = new MouseShipGUIElement(0,null, "mi_stat_down.png", 404,462,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_DOWN_MASK + KeyEvent.CTRL_DOWN_MASK, false), 0, null);
// *
        // * ARC Toggles
        // *
        MouseShipGUIElement FRONT_ARC_TOGGLE = new MouseShipGUIElement(2, null, "mi_front_arc.png", 11, 132,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, false), 0, null);
        MouseShipGUIElement FRONT_ARC_123 = new MouseShipGUIElement(2, null, "mi_range_123.png", 104, 132,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0, null);

        MouseShipGUIElement BACK_ARC_TOGGLE = new MouseShipGUIElement(2, null, "mi_back_arc.png", 11, 199,
                KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK, false), 0, null);
        MouseShipGUIElement BACK_ARC_123 = new MouseShipGUIElement(2, null, "mi_range_123.png", 104, 199,
                KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0, null);

        MouseShipGUIElement LEFT_ARC_TOGGLE = new MouseShipGUIElement(2, null, "mi_left_arc.png", 11, 266,
                KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK, false), 0, null);
        MouseShipGUIElement LEFT_ARC_123 = new MouseShipGUIElement(2, null, "mi_range_123.png", 104, 266,
                KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0, null);

        MouseShipGUIElement RIGHT_ARC_TOGGLE = new MouseShipGUIElement(2, null, "mi_right_arc.png", 11, 333,
                KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.ALT_DOWN_MASK, false), 0, null);
        MouseShipGUIElement RIGHT_ARC_123 = new MouseShipGUIElement(2, null, "mi_range_123.png", 104, 333,
                KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0, null);

        MouseShipGUIElement TARGET_TOGGLE = new MouseShipGUIElement(2, null, "mi_target.png", 330, 132,
                KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK, false), 0, null);
        MouseShipGUIElement TARGET_123 = new MouseShipGUIElement(2, null, "mi_range_123.png", 423, 132,
                KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0, null);

        MouseShipGUIElement BULLSEYE_TOGGLE = new MouseShipGUIElement(2, null, "mi_bullseye.png", 330, 199,
                KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK, false), 0, null);
        MouseShipGUIElement BULLSEYE_123 = new MouseShipGUIElement(2, null, "mi_range_123.png", 423, 199,
                KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0, null);

        MouseShipGUIElement FORE_TOGGLE = new MouseShipGUIElement(2, null, "mi_fore_arc.png", 330, 266,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, false), 0, null);
        MouseShipGUIElement FORE_123 = new MouseShipGUIElement(2, null, "mi_range_123.png", 423, 266,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0, null);

        MouseShipGUIElement AFT_TOGGLE = new MouseShipGUIElement(2, null, "mi_aft_arc.png", 330, 333,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_DOWN_MASK, false), 0, null);
        MouseShipGUIElement AFT_123 = new MouseShipGUIElement(2, null, "mi_range_123.png", 423, 333,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), 0, null);

        // * Sideslip page, needs 12 buttons, all for the HMP Droid Gunship
        // *
        MouseShipGUIElement SS_LBF3 = new MouseShipGUIElement(6,"BR_LF3", "Barrel_L_Front_3.png", 12, 252,
                KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a sideslip right bank 3");
        MouseShipGUIElement SS_LBF2 = new MouseShipGUIElement(6,"BR_LF2", "Barrel_L_Front_2d.png", 64, 294,
                KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a sideslip right bank 2");
        MouseShipGUIElement SS_LBF1 = new MouseShipGUIElement(6,"BR_LF1", "Barrel_L_Front_1.png", 120, 319,
                KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a sideslip right bank 1");

        MouseShipGUIElement SS_RBF3 = new MouseShipGUIElement(6,"BR_RF3", "Barrel_R_Front_3.png", 420 , 251,
                KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK+KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a sideslip left bank 3");
        MouseShipGUIElement SS_RBF2 = new MouseShipGUIElement(6,"BR_RF2", "Barrel_R_Front_2d.png", 377 , 293,
                KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a sideslip left bank 2");
        MouseShipGUIElement SS_RBF1 = new MouseShipGUIElement(6,"BR_RF1", "Barrel_R_Front_1.png", 320, 319,
                KeyStroke.getKeyStroke(KeyEvent.VK_O, 0, false), 0, " perform a sideslip left bank 1");

        MouseShipGUIElement SS_LF3 = new MouseShipGUIElement(6,"BR_L3", "Barrel_L_Side_3.png", 37, 82,
                KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a sideslip right turn 3");
        MouseShipGUIElement SS_LF2 = new MouseShipGUIElement(6,"BR_L2", "Barrel_L_Side_2d.png", 36, 154,
                KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a sideslip right turn 2");
        MouseShipGUIElement SS_LF1 = new MouseShipGUIElement(6,"BR_L1", "Barrel_L_Side_1.png", 110, 158,
                KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_DOWN_MASK, false), 0, " perform a sideslip right turn 1");

        MouseShipGUIElement SS_LR3 = new MouseShipGUIElement(6,"BR_R3", "Barrel_R_Side_3.png", 394, 82,
                KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_DOWN_MASK+KeyEvent.CTRL_DOWN_MASK, false), 0, " perform a sideslip left turn 3");
        MouseShipGUIElement SS_LR2 = new MouseShipGUIElement(6,"BR_R2", "Barrel_R_Side_2d.png", 404, 153,
                KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a sideslip left turn 2");
        MouseShipGUIElement SS_LR1 = new MouseShipGUIElement(6,"BR_R1", "Barrel_R_Side_1.png", 325, 158,
                KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK, false), 0, " perform a sideslip left turn 1");



        if(isSmall){
            guiElements.add(SS_LBF1);
            guiElements.add(SS_LBF2);
            guiElements.add(SS_LBF3);
            guiElements.add(SS_RBF1);
            guiElements.add(SS_RBF2);
            guiElements.add(SS_RBF3);

            guiElements.add(SS_LF1);
            guiElements.add(SS_LF2);
            guiElements.add(SS_LF3);
            guiElements.add(SS_LR1);
            guiElements.add(SS_LR2);
            guiElements.add(SS_LR3);
        }
        guiElements.add(SFOILS);
        guiElements.add(CCW1);
        guiElements.add(CCW15);
        guiElements.add(CCW90);
        guiElements.add(ROT180);

        guiElements.add(DROP);
        guiElements.add(LAUNCH);
        guiElements.add(CW1);
        guiElements.add(CW15);
        guiElements.add(CW90);

        guiElements.add(DISPLACEUP);
        guiElements.add(DISPLACEDOWN);
        guiElements.add(DISPLACELEFT);
        guiElements.add(DISPLACERIGHT);

        guiElements.add(EXTRA);
        guiElements.add(LEFTTROLL3);
        guiElements.add(LEFTTROLL2);
        guiElements.add(LEFTTROLL1);
        guiElements.add(LEFTSLOOP3);
        guiElements.add(LEFTSLOOP2);
        guiElements.add(LEFTSLOOP1);
        guiElements.add(RIGHTTROLL3);
        guiElements.add(RIGHTTROLL2);
        guiElements.add(RIGHTTROLL1);
        guiElements.add(RIGHTSLOOP3);
        guiElements.add(RIGHTSLOOP2);
        guiElements.add(RIGHTSLOOP1);

        guiElements.add(LEFTBANK3);
        guiElements.add(LEFTBANK2);
        guiElements.add(LEFTBANK1);
        guiElements.add(RIGHTBANK3);
        guiElements.add(RIGHTBANK2);
        guiElements.add(RIGHTBANK1);
        guiElements.add(RIGHTTURN3);
        guiElements.add(RIGHTTURN2);
        guiElements.add(RIGHTTURN1);
        guiElements.add(REVERSELEFT);
        guiElements.add(REVERSE2);
        guiElements.add(REVERSE);
        guiElements.add(REVERSERIGHT);
        guiElements.add(LEFTTURN3);
        guiElements.add(LEFTTURN2);
        guiElements.add(LEFTTURN1);
        guiElements.add(MOVESLAM);
        guiElements.add(STRAIGHT1);
        guiElements.add(STRAIGHT2);
        guiElements.add(STRAIGHT3);
        guiElements.add(STRAIGHT4);
        guiElements.add(STRAIGHT5);
        guiElements.add(KTURN5);
        guiElements.add(KTURN4);
        guiElements.add(KTURN3);
        guiElements.add(KTURN2);
        guiElements.add(KTURN1);
        guiElements.add(CLOSE);
        guiElements.add(CONPAGE);
        guiElements.add(ROTATE_LEFT);
        guiElements.add(TURRET_123);
        guiElements.add(TURRET_ARC);
        guiElements.add(ROTATE_RIGHT);
        guiElements.add(TURRETPAGE);
        guiElements.add(BOOST_LEFT);
        guiElements.add(BOOST_STR8);
        guiElements.add(BOOST_RIGHT);
        guiElements.add(FORE_TOGGLE);
        guiElements.add(FORE_123);
        guiElements.add(AFT_TOGGLE);
        guiElements.add(AFT_123);
        guiElements.add(BACK_ARC_TOGGLE);
        guiElements.add(BACK_ARC_123);
        guiElements.add(LEFT_ARC_TOGGLE);
        guiElements.add(LEFT_ARC_123);
        guiElements.add(RIGHT_ARC_TOGGLE);
        guiElements.add(RIGHT_ARC_123);
        guiElements.add(BULLSEYE_TOGGLE);
        guiElements.add(BULLSEYE_123);
        guiElements.add(FRONT_ARC_TOGGLE);
        guiElements.add(FRONT_ARC_123);
        guiElements.add(TARGET_TOGGLE);
        guiElements.add(TARGET_123);

        guiElements.add(ARCPAGE);
        guiElements.add(ROLLPAGE);

        guiElements.add(FORCE_STAT);
        guiElements.add(FORCE_UP);
        guiElements.add(FORCE_DOWN);

        guiElements.add(HULL_STAT);
        guiElements.add(HULL_UP);
        guiElements.add(HULL_DOWN);

        guiElements.add(CHARGE_STAT);
        guiElements.add(CHARGE_UP);
        guiElements.add(CHARGE_DOWN);

        guiElements.add(SHIELD_STAT);
        guiElements.add(SHIELD_UP);
        guiElements.add(SHIELD_DOWN);
        guiElements.add(BR_RF1);
        guiElements.add(BR_RB1);
        guiElements.add(BR_RF2);
        guiElements.add(BR_RB2);

        guiElements.add(BR_LF2);
        guiElements.add(BR_LF1);
        guiElements.add(BR_LB2);
        guiElements.add(BR_LB1);
        guiElements.add(BR_L2);
        guiElements.add(BR_L1);
        guiElements.add(CENTER);
        guiElements.add(BR_R1);
        guiElements.add(BR_R2);

        // HARD CODED outline of the GUI - DONE AT EVERYONE'S PERIL
        totalWidth = 520;
        totalHeight = 610;

        figureOutBestTopLeftCorner();

    }

    //default ugly placeholder mouse ship GUI, will be phased out
    public MouseShipGUIDrawable(GamePiece shipPiece, Map map, XWS2Pilots pilotShip, XWS2Pilots.Pilot2e pilot){
        _shipPiece = shipPiece;
        _map = map;
        _pilotShip = pilotShip;
        _pilot = pilot;

        scale = _map.getZoom();

        //Define the top left coordinate of the popup outline, first attempt
        ulX = _shipPiece.getPosition().x + 150;
        ulY = Math.max(0,_shipPiece.getPosition().y - 150);

        MouseShipGUIJomblr(shipPiece, map, pilotShip, pilot);


        /*
        //Barrel Roll test
        miElement brIconLeft = new miElement("mi_barrelroll.png",  cursorX,  cursorY,
                null, 1);
        listOfInteractiveElements.add(brIconLeft);


        if(pilotShip.getSize().equals("Small")){

            miElement br2IconLeft = new miElement("mi_barrelroll2L.png",  cursorX,  cursorY + brIconLeft.image.getHeight()+padX,
                    null, 3);
            listOfInteractiveElements.add(br2IconLeft);

            miElement brLBFIconLeft = new miElement("mi_barrelroll_lb.png", cursorX,  cursorY + 2*brIconLeft.image.getHeight()+2*padX,
                    null, 5);
            listOfInteractiveElements.add(brLBFIconLeft);
            miElement brLBBIconLeft = new miElement("mi_barrelroll_lbb.png",  cursorX,  cursorY + 3*brIconLeft.image.getHeight()+3*padX,
                    null, 6);
            listOfInteractiveElements.add(brLBBIconLeft);

            miElement br2LBFIconLeft = new miElement("mi_barrelroll_lb.png", cursorX,  cursorY + 4*brIconLeft.image.getHeight()+4*padX,
                    null, 9);
            listOfInteractiveElements.add(br2LBFIconLeft);
            miElement br2LBBIconLeft = new miElement("mi_barrelroll_lbb.png",  cursorX,  cursorY + 5*brIconLeft.image.getHeight()+5*padX,
                    null, 11);
            listOfInteractiveElements.add(br2LBBIconLeft);

            cursorX += br2IconLeft.image.getWidth() + smallGapX;
        }
        else
            cursorX += brIconLeft.image.getWidth() + smallGapX;


        //add ship gfx, getShipImage deals with alt paint jobs and dual ships (just takes the first one it finds)
        int stateOfShipGfx = 0;
        try{
            int uLevel = Integer.parseInt(_shipPiece.getProperty("ULevel").toString());
            if(uLevel == 3 || uLevel == 4) stateOfShipGfx = 2;
            if(uLevel == 1 || uLevel == 2) stateOfShipGfx = 1;
        }catch(Exception e){

        }
        miElement shipGfx = new miElement(getShipImage(pilotShip, stateOfShipGfx),cursorX, cursorY, null,0);
        if(shipGfx!=null && shipGfx.image!=null) {
            listOfInteractiveElements.add(shipGfx);
            cursorX += shipGfx.image.getWidth() + smallGapX;
        }

        int tentativeTrollY = 0;
        if(pilotShip.getSize().equals("Small")) tentativeTrollY = cursorY + 4*brIconLeft.image.getHeight()+4*padX;
        else tentativeTrollY = cursorY + shipGfx.image.getHeight() + smallGapX;


        miElement brIconRight = new miElement("mi_barrelroll.png", cursorX,  cursorY,
                null, 2);
        listOfInteractiveElements.add(brIconRight);

        if(pilotShip.getSize().equals("Small")) {
            miElement br2IconRight = new miElement("mi_barrelroll2R.png",  cursorX,  cursorY + brIconRight.image.getHeight() + padX,
                    null, 4);
            listOfInteractiveElements.add(br2IconRight);

            miElement brRBFIconRight = new miElement("mi_barrelroll_rb.png", cursorX,  cursorY + 2*brIconRight.image.getHeight() + 2*padX,
                    null, 7);
            listOfInteractiveElements.add(brRBFIconRight);

            miElement brRBBIconRight = new miElement("mi_barrelroll_rbb.png",  cursorX,  cursorY + 3*brIconRight.image.getHeight() + 3*padX,
                    null, 8);
            listOfInteractiveElements.add(brRBBIconRight);

            miElement br2RBFIconRight = new miElement("mi_barrelroll_rb.png", cursorX,  cursorY + 4*brIconRight.image.getHeight() + 4*padX,
                    null, 10);
            listOfInteractiveElements.add(br2RBFIconRight);

            miElement br2RBBIconRight = new miElement("mi_barrelroll_rbb.png",  cursorX,  cursorY + 5*brIconRight.image.getHeight() + 5*padX,
                    null, 12);
            listOfInteractiveElements.add(br2RBBIconRight);
        }

        cursorX += brIconRight.image.getWidth() + padX;

        miElement hullGfx = new miElement("mi_hull.png",  cursorX+smallGapX, padY,
                null,0);
        miElement addHull = new miElement("mi_plus.png", cursorX+ smallGapX + hullGfx.image.getWidth(), padY+hullGfx.image.getHeight()/2-16,
                KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_DOWN_MASK, false),0);
        miElement removeHull = new miElement("mi_minus.png",  cursorX + 2*smallGapX + hullGfx.image.getWidth() + addHull.image.getWidth(),
                padY+hullGfx.image.getHeight()/2-16,
        KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_DOWN_MASK + KeyEvent.CTRL_DOWN_MASK, false),0);

        listOfInteractiveElements.add(hullGfx);
        listOfInteractiveElements.add(addHull);
        listOfInteractiveElements.add(removeHull);

        cursorY += hullGfx.image.getHeight() + smallGapX;

        miElement shieldGfx = new miElement("mi_shield.png",  cursorX+smallGapX, cursorY,
                null,0);
        miElement addShield = new miElement("mi_plus.png", cursorX+ smallGapX + hullGfx.image.getWidth(), cursorY+shieldGfx.image.getHeight()/2-16,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK, false),0);
        miElement removeShield = new miElement("mi_minus.png",  cursorX + 2*smallGapX + hullGfx.image.getWidth() + addHull.image.getWidth(),
                cursorY+shieldGfx.image.getHeight()/2-16,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK + KeyEvent.CTRL_DOWN_MASK, false),0);

        listOfInteractiveElements.add(shieldGfx);
        listOfInteractiveElements.add(addShield);
        listOfInteractiveElements.add(removeShield);


        cursorY += shieldGfx.image.getHeight() + smallGapX;

        miElement chargeGfx = new miElement("mi_charge.png", cursorX+smallGapX, cursorY,
                null,0);
        miElement addCharge = new miElement("mi_plus.png",  cursorX+ smallGapX + hullGfx.image.getWidth(), cursorY+chargeGfx.image.getHeight()/2-16,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_DOWN_MASK, false),0);
        miElement removeCharge = new miElement("mi_minus.png", cursorX + 2*smallGapX + hullGfx.image.getWidth() + addHull.image.getWidth(),
                cursorY+chargeGfx.image.getHeight()/2-16,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false),0);

        listOfInteractiveElements.add(chargeGfx);
        listOfInteractiveElements.add(addCharge);
        listOfInteractiveElements.add(removeCharge);

        cursorY += chargeGfx.image.getHeight() + smallGapX;

        miElement forceGfx = new miElement("mi_force.png", cursorX+smallGapX, cursorY,
                null,0);
        miElement addForce = new miElement("mi_plus.png",  cursorX+ smallGapX + hullGfx.image.getWidth(), cursorY+forceGfx.image.getHeight()/2-16,
                KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.SHIFT_DOWN_MASK, false),0);
        miElement removeForce = new miElement("mi_minus.png", cursorX + 2*smallGapX + hullGfx.image.getWidth() + addHull.image.getWidth(),
                cursorY+forceGfx.image.getHeight()/2-16,
                KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.SHIFT_DOWN_MASK + KeyEvent.CTRL_DOWN_MASK, false),0);

        listOfInteractiveElements.add(forceGfx);
        listOfInteractiveElements.add(addForce);
        listOfInteractiveElements.add(removeForce);

        cursorX += hullGfx.image.getWidth() + addHull.image.getWidth() + removeHull.image.getWidth();
        cursorY += forceGfx.image.getHeight() + smallGapX;

        miElement closeGfx = new miElement("mi_close.png",  cursorX + padX, 0, null, -66);
        listOfInteractiveElements.add(closeGfx);

        cursorX += closeGfx.image.getWidth();


        //adding the tallon roll buttons
        /*
        int tallonRollxCursor = padX;
        miElement trL3 = new miElement("mi_tallonleft.png", tallonRollxCursor, tentativeTrollY, KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK, false), 0);
        listOfInteractiveElements.add(trL3);
        tallonRollxCursor += trL3.image.getWidth() + 1;

        miElement trL2 = new miElement("mi_tallonleft.png", tallonRollxCursor, tentativeTrollY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK, false), 0);
        listOfInteractiveElements.add(trL2);
        tallonRollxCursor += trL2.image.getWidth() + 1;

        miElement trL1 = new miElement("mi_tallonleft.png", tallonRollxCursor, tentativeTrollY, KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK, false), 0);
        listOfInteractiveElements.add(trL1);
        tallonRollxCursor += trL1.image.getWidth() +padX;

        miElement trR1 = new miElement("mi_tallonright.png", tallonRollxCursor, tentativeTrollY, KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.ALT_DOWN_MASK, false), 0);
        listOfInteractiveElements.add(trR1);
        tallonRollxCursor += trR1.image.getWidth() + 1;

        miElement trR2 = new miElement("mi_tallonright.png", tallonRollxCursor, tentativeTrollY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.ALT_DOWN_MASK, false), 0);
        listOfInteractiveElements.add(trR2);
        tallonRollxCursor += trR2.image.getWidth() + 1;

        miElement trR3 = new miElement("mi_tallonright.png", tallonRollxCursor, tentativeTrollY, KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_DOWN_MASK, false), 0);
        listOfInteractiveElements.add(trR3);


        totalWidth = cursorX + padX;
        totalHeight = 6*brIconLeft.image.getHeight() + 2* padY;

        figureOutBestTopLeftCorner();
        */

    }
    private Shape refreshShape(){
        Rectangle outline = new Rectangle(totalWidth,totalHeight);
        scale = _map.getZoom();
        AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);
        scaler.translate(ulX, ulY);
        return scaler.createTransformedShape(outline);
    }
    private Shape getMapShape(){
        Rectangle mapArea = new Rectangle(0,0,0,0);
        try{
            Board b = _map.getBoards().iterator().next();
            mapArea = b.bounds();

            AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);
            return scaler.createTransformedShape(mapArea);
        }catch(Exception e)
        {
            return null;
        }
    }

    private void figureOutBestTopLeftCorner() {

        Shape wouldBeOutline = refreshShape();
        Shape mapArea = getMapShape();
        List<BumpableWithShape> bumpables = OverlapCheckManager.getBumpablesOnMap(true, null);
        drawThese= OverlapCheckManager.getBumpablesOnMap(true, null);
        boolean isSearching = true;
        int failSafe = 40;
        int iteration = 0;
        int oldUlX = ulX;
        int oldUlY = ulY;

        AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);

        double bestX = 200.0, bestY = 200.0; //worst case scenario
        int bestBump = 999; //default value, if no spots are found, this is gonna be it. should activate the GUI dead center or in a fixed position
        boolean breakoff=false;
        for(int i=1; i<8; i++){ //scan to the right of the ship
            float radius = SMALLSHIPGUIRADIUSBASE*getRadiusMultiplier(_pilotShip.getSize());
            radius *= i;
            for(int j=80; j>=-80; j=j-20){
                double x = rotX(radius, 0, -j);
                double y = rotY(radius, 0, -j);

                ulX = _shipPiece.getPosition().x + (int)x;
                ulY = Math.max(0,_shipPiece.getPosition().y - (int)y);

                wouldBeOutline = refreshShape();
                //andThese.add(wouldBeOutline);

                int bumps = isGreenToGo(wouldBeOutline,mapArea,bumpables);
                if(bumps < bestBump) {
                    bestBump = bumps;
                    bestX = ulX;
                    bestY = ulY;
                }
                if(bumps < MAXBUMPABLECOUNT) {
                    breakoff = true;
                    break;
                }
               // logToChat("keep the search on i:" + i + " j:" + j);
            }
            if(breakoff) break;
        } //end scan to the right of the ship
        if(breakoff == false){
            for(int i=1; i<8; i++){ //scan to the left of the ship
                float radius = SMALLSHIPGUIRADIUSBASE*getRadiusMultiplier(_pilotShip.getSize());
                radius*=i;
                for(int j= 80; j >= -80; j=j-20){
                    double x = rotX(radius, 0, -j);
                    double y = rotY(radius, 0, -j);

                    ulX = _shipPiece.getPosition().x - totalWidth - (int)x;
                    ulY = Math.max(0, _shipPiece.getPosition().y - (int) y);

                    wouldBeOutline = refreshShape();
                    //andThese.add(wouldBeOutline);

                    int bumps = isGreenToGo(wouldBeOutline,mapArea,bumpables);
                    if(bumps < bestBump) {
                        bestBump = bumps;
                        bestX = ulX;
                        bestY = ulY;
                    }
                    if(bumps < MAXBUMPABLECOUNT){
                        breakoff = true;
                        break;
                    }

                }
                if(breakoff) break;
            } //end scan to the left of the ship
        }
        //if(breakoff==false){logToChat("didn't find a proper location, using best x:" +bestX + " y:" + bestY + " with " + bestBump + " overlaps.");
        ulX = (int)bestX;
        ulY = (int)bestY;
        }

/*
        //try to the left
        while(isSearching){
            if(iteration >= failSafe) {
                ulX = oldUlX;
                ulY = oldUlY;
                break;
            }
            if(isGreenToGo(wouldBeOutline, mapArea, bumpables)) {
                logToChat("green to go");
                break;
            }
            logToChat("tweaking the position of the GUI iteration " + iteration);
            ulX += 125;
            wouldBeOutline = refreshShape();

            if(checkIfOutOfBoundsToTheRight(wouldBeOutline, mapArea)){
                ulX = 0;
                ulY += wouldBeOutline.getBounds().height;
                wouldBeOutline = refreshShape();
            }

            iteration++;
        }
        */



    /*
    shapeToCheck.getBounds().getMaxX() > mapArea.getBounds().getMaxX()  || // too far to the right
                shapeToCheck.getBounds().getMaxY() > mapArea.getBounds().getMaxY() || // too far to the bottom
                shapeToCheck.getBounds().getX() < mapArea.getBounds().getX() || //too far to the left
                shapeToCheck.getBounds().getY() < mapArea.getBounds().getY()) // too far to the top
     */
    private int isGreenToGo(Shape GUIOutline, Shape mapArea, List<BumpableWithShape> bumpables){

        //check if the gui outline, union with the map square is a bigger shape than the original map square
        //ie the mouse gui would go off the map and might have parts of it unseen by a player who has the map
        //set to zoom fit to height/width and not seeing any part outside the map, which shows up as red
        // in vassal - returns positive infinity to say something's the worst case possible
        if(Util.hasEnlargedUnion(GUIOutline, mapArea)==true) return Integer.MAX_VALUE;

        scale = _map.getZoom();
        AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);
        int bumpableCount = 0;
        for(BumpableWithShape bws : bumpables){
            Shape tShape = scaler.createTransformedShape(bws.shape);
            if(Util.shapesOverlap(GUIOutline, tShape)) bumpableCount++;
        }
        return bumpableCount;
    }

    private String getShipImage(XWS2Pilots pilotShip, int dualState) {

        StringBuilder sb = new StringBuilder();

        sb.append("S2e_");
        sb.append(pilotShip.getCleanedName());
        if(pilotShip.hasDualBase()){
            sb.append("_");
            if(dualState == 1) sb.append(pilotShip.getBaseImage1Identifier());
            if(dualState == 2) sb.append(pilotShip.getBaseImage2Identifier());
        }
        sb.append(".png");

       return sb.toString();
    }

    public float getRadiusMultiplier(String size){
        int i=1;
        if(size.equals("large") || size.equals("Large")) i=3;
        if(size.equals("medium") || size.equals("Medium")) i=2;

        return (i+1)/2;
    }
    public void draw(Graphics g, Map map) {
        Graphics2D g2d = (Graphics2D) g;

        Rectangle outline = new Rectangle(totalWidth,totalHeight);

        //Object prevAntiAliasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        scale = _map.getZoom();

        //Vassal 3.x.x fix, uiScale can now change from the default value of 1 thanks to HiDPI monitors.
        final double os_scale = g2d.getDeviceConfiguration().getDefaultTransform().getScaleX();
        scale *= os_scale;
        //end fix


        AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);


        //prepare the outline of the GUI

        scaler.translate(ulX,ulY);
        g2d.setPaint(Color.WHITE);

        //paint the big GUI outline
        //Shape transformedOutline = scaler.createTransformedShape(outline);
        //g2d.fill(transformedOutline);




        g2d.setPaint(new Color(0,0,255, 150));
        for(MouseShipGUIElement elem : guiElements){

            if(elem.getPage() == 0 || elem.getPage() == currentPage){
                scaler.translate(elem.globalX, elem.globalY);

                //prepare shadows for each GUI elements
           /*
            if(_pilotShip.getSize().equals("Large") || _pilotShip.getSize().equals("large")) af = elem.getTransformForDraw(scale, 0.5);
            else af = elem.getTransformForDraw(scale);
*/
                //af = elem.getTransformForDraw(scale);
                if(elem.getTripleChoice() <= -100 && elem.getTripleChoice() >= -199){ //toggle button

                    if(currentPage == (elem.getTripleChoice()+200)) g2d.drawImage(elem.image, scaler, new ImageObserver() {
                        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                            return false;
                        }
                    });
                    else g2d.drawImage(elem.imageSpent, scaler, new ImageObserver() {
                        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                            return false;
                        }
                    });
                } //end treatment of toggle button
                else g2d.drawImage(elem.image, scaler, new ImageObserver() {
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                        return false;
                    }
                });
                //undo the relative translation for this element to become ready for the next
                scaler.translate(-elem.globalX, -elem.globalY);


                // Rectangle rawR = elem.image.getData().getBounds();
                AffineTransform af = elem.getTransformForDraw(scale);
                af.translate(ulX, ulY);
                Shape s = null;
                if(elem.getNonRect()==null){
                    s = af.createTransformedShape(elem.image.getData().getBounds()); //use the image bounds
                } else {
                    //adjust for halfwidth half height
                    af.translate(elem.getNonRect().getBounds2D().getWidth()/2.0, elem.getNonRect().getBounds2D().getHeight()/2.0);
                    s = af.createTransformedShape(elem.getNonRect()); //use the non-rectangular if there's one

                }
//paint the shapes of the buttons here
                //g2d.fillRect(s.getBounds().x, s.getBounds().y, s.getBounds().width, s.getBounds().height);
            } //end drawing an element part of active page or page 0
        } //end drawing all the guielements

        //bring the translation back to what it was before the GUI
        scaler.translate(-ulX,-ulY);

        //fill something white. the main outline?

        g2d.setColor(Color.WHITE);
        //logToChat("amount of shapes to draw " + drawThese.size());

        //draw bumpables
        /*
        for(BumpableWithShape bws : drawThese){
            g2d.fill(scaler.createTransformedShape(bws.shape));
        }
        */

        // draw shapes of buttons? or outline test of the GUI
        /*
        g2d.setColor(new Color(0,255,0,60));
        for(Shape s : andThese){
            g2d.fill(s);
        }
*/

        /*  piece of code that can fetch the maneuver icons as seen on the dials
        try{
            int i = 0;
            for(String move : _pilotShip.getDial()){
                String imageNameToLoad = StemDial2e.dialHeadingImages.get(move.substring(1,3));

                InputStream inputstream = new BufferedInputStream(fileArchive.getInputStream("images/"+imageNameToLoad));
                image = ImageIO.read(inputstream);
                inputstream.close();

                AffineTransform translateNScale = new AffineTransform();
                translateNScale.scale(scale, scale);
                translateNScale.translate(60+ _x + i* 80,_y+50);
                i++;
                if(image!=null) g2d.drawImage(image, translateNScale, new ImageObserver() {
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                        return false;
                    }
                });
            }

        }catch(Exception e){}


        try{

            fileArchive.close();
            dataArchive.close();
        }catch(IOException ioe){
            Util.logToChat("can't close the xwd2 files " + ioe.getMessage());
        }

        drawText(_pilotShip.getDial().toString(),scale,_x + 30, _y + 50, g2d);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAntiAliasing);
        */

    }

    public boolean drawAboveCounters() {
        return true;
    }


    private static void drawText(String text, double scale, double x, double y, Graphics2D graphics2D) {
        AttributedString attstring = new AttributedString(text);
        attstring.addAttribute(TextAttribute.FONT, new Font("Arial", 0,32));
        attstring.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_LTR);
        FontRenderContext frc = graphics2D.getFontRenderContext();
        TextLayout t = new TextLayout(attstring.getIterator(), frc);
        Shape textShape = t.getOutline(null);

        textShape = AffineTransform.getTranslateInstance(x, y)
                .createTransformedShape(textShape);
        textShape = AffineTransform.getScaleInstance(scale, scale)
                .createTransformedShape(textShape);
        graphics2D.setColor(Color.white);
        graphics2D.fill(textShape);

        if (scale > 0.60) {
            // stroke makes it muddy at low scale
            graphics2D.setColor(Color.black);
            graphics2D.setStroke(new BasicStroke(0.8f));
            graphics2D.draw(textShape);
        }
    }
/*

    protected static class miElement {
        BufferedImage image;
        int x;
        int y;

        KeyStroke associatedKeyStroke;

        int whichTripleChoice = 0;


        public miElement(String fileName, int wantedX, int wantedY, KeyStroke wantedKeyStroke, int wantedTripleChoice){
            x = wantedX;
            y = wantedY;
            associatedKeyStroke = wantedKeyStroke;
            whichTripleChoice = wantedTripleChoice;


            //load the image
            try {
                GameModule gameModule = GameModule.getGameModule();
                DataArchive dataArchive = gameModule.getDataArchive();
                FileArchive fileArchive = dataArchive.getArchive();

                InputStream inputstream = new BufferedInputStream(fileArchive.getInputStream("images/" + fileName));
                image = ImageIO.read(inputstream);
                inputstream.close();
            }
            catch(Exception e){
                Util.logToChat("Failed to load GUI image " + fileName);
            }
        }
        public int getTripleChoice(){
            return whichTripleChoice;
        }
        public AffineTransform getTransformForClick(double scale, int abx, int aby){
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.translate(abx + x, aby + y);
            return affineTransform;
        }

        public AffineTransform getTransformForDraw(double scale){
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.scale(scale, scale);
            affineTransform.translate(x, y);
            return affineTransform;
        }

        public AffineTransform getTransformForDraw(double scale, double scale2){
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.scale(scale*scale2, scale*scale2);
            affineTransform.translate(x, y);
            return affineTransform;
        }

    }
    */
}
