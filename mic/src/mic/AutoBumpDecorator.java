package mic;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

import javax.swing.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import mic.manuvers.ManeuverPaths;

/**
 * Created by amatheny on 2/14/17.
 */
public class AutoBumpDecorator extends Decorator implements EditablePiece {
    public static final String ID = "auto-bump;";

    private MyState state = new MyState();
    private String type;

    private Map<String, ManeuverPaths> keyStrokeToManeuver = ImmutableMap.<String, ManeuverPaths>builder()
            .put("SHIFT 1", ManeuverPaths.Str1)
            .put("SHIFT 2", ManeuverPaths.Str2)
            .put("SHIFT 3", ManeuverPaths.Str3)
            .put("SHIFT 4", ManeuverPaths.Str4)
            .put("SHIFT 5", ManeuverPaths.Str5)
            .put("CTRL SHIFT 1", ManeuverPaths.LT1)
            .put("CTRL SHIFT 2", ManeuverPaths.LT2)
            .put("CTRL SHIFT 3", ManeuverPaths.LT3)
            .put("ALT SHIFT 1", ManeuverPaths.RT1)
            .put("ALT SHIFT 2", ManeuverPaths.RT2)
            .put("ALT SHIFT 3", ManeuverPaths.RT3)
            .put("CTRL 1", ManeuverPaths.LBk1)
            .put("CTRL 2", ManeuverPaths.LBk2)
            .put("CTRL 3", ManeuverPaths.LBk3)
            .put("ALT 1", ManeuverPaths.RBk1)
            .put("ALT 2", ManeuverPaths.RBk1)
            .put("ALT 3", ManeuverPaths.RBk1)
            .put("ALT CTRL 1", ManeuverPaths.K1)
            .put("ALT CTRL 2", ManeuverPaths.K2)
            .put("ALT CTRL 3", ManeuverPaths.K3)
            .put("ALT CTRL 4", ManeuverPaths.K4)
            .put("ALT CTRL 5", ManeuverPaths.K5)
            .put("CTRL 6", ManeuverPaths.RevLbk1)
            .put("SHIFT 6", ManeuverPaths.RevStr1)
            .put("ALT 6", ManeuverPaths.RevRbk1)
            .put("CTRL Q", ManeuverPaths.SloopL1)
            .put("CTRL W", ManeuverPaths.SloopL2)
            .put("CTRL E", ManeuverPaths.SloopL3)
            .put("ALT Q", ManeuverPaths.SloopR1)
            .put("ALT W", ManeuverPaths.SloopR2)
            .put("ALT E", ManeuverPaths.SloopR3)
            .put("CTRL SHIFT E", ManeuverPaths.SloopL3Turn)
            .put("ALT ALT E", ManeuverPaths.SloopR3Turn)
            .put("CTRL Y", ManeuverPaths.TrollL2)
            .put("CTRL T", ManeuverPaths.TrollL3)
            .put("ALT Y", ManeuverPaths.TrollR2)
            .put("ALT T", ManeuverPaths.TrollR3)
            .build();

    public AutoBumpDecorator() {
        this(null);
    }

    public AutoBumpDecorator(GamePiece piece) {
        setInner(piece);
        this.piece = piece;
    }

    @Override
    public void mySetState(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return;
        }
        this.state = MyState.fromJson(s);
    }

    @Override
    public String myGetState() {
        return this.state == null ? "" : this.state.toJson();
    }

    @Override
    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {
        ManeuverPaths path = getKeystrokePath(stroke);
        if (path == null) {
            // Ignore key event
            return piece.keyEvent(stroke);
        }
        Util.logToChat(String.format("Exectued %s via keystroke", path.name()));
        Util.logToChat(String.format("Current pos = (%s,%s) %s deg", getPosition().getX(), getPosition().getY(), getAngle()));
        return piece.keyEvent(stroke);
    }

    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        return new NullCommand();
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

    @Override
    public String myGetType() {
        return ID;
    }

    public String getDescription() {
        return "Custom auto-bump resolution (mic.AutoBumpDecorator)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    private double getAngle() {
        return ((FreeRotator) Decorator.getDecorator(getOutermost(this), FreeRotator.class)).getAngle();
    }

    private MyState getCurrentState() {
        MyState shipState = new MyState();
        shipState.prevX = getPosition().getX();
        shipState.prevY = getPosition().getY();
        shipState.prevAngle = getAngle();
        return shipState;
    }

    private ManeuverPaths getKeystrokePath(KeyStroke keyStroke) {
        String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToManeuver.containsKey(hotKey)) {
            return keyStrokeToManeuver.get(hotKey);
        }
        return null;
    }

    private static class MyState {
        @JsonProperty("prevX")
        double prevX;

        @JsonProperty("prevY")
        double prevY;

        @JsonProperty("prevAngle")
        double prevAngle;

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MyState)) {
                return false;
            }
            MyState that = (MyState) obj;
            return this.prevX == that.prevX && this.prevY == that.prevY && this.prevAngle == that.prevAngle;
        }

        public String toJson() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (Exception e) {
                return "";
            }
        }

        public static MyState fromJson(String jsonStr) {
            try {
                return new ObjectMapper().readValue(jsonStr, MyState.class);
            } catch (IOException e) {
                return new MyState();
            }
        }
    }
}
