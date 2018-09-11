package mic;

import com.google.common.collect.Lists;

import java.util.List;

import static mic.Util.none;

/**
 * Created by mjuneau on 7/8/18.
 */
public enum Tokens2e {
    shield2e(
            none,
            Lists.newArrayList("gonk"),
            none
    ),
    lock(
            Lists.newArrayList("Lock"),
            Lists.newArrayList("targetingcomputer", "firecontrolsystem"),
            none
    ),
    stress2e(
            Lists.newArrayList("Focus"),
            Lists.newArrayList("zuckuss", "r3a2","elusiveness","overclockedr4"),
            Lists.newArrayList("tychocelchu","zetaleader")
    ),
    focus2e(
            Lists.newArrayList("Focus"),
            none,
            none
    ),
    calculate2e(
            Lists.newArrayList("Calculate"),
            Lists.newArrayList("000","ig88d","c3po"),
            Lists.newArrayList("aggressorassaultfighter","")
    ),
    evade2e(
            Lists.newArrayList("Evade"),
            Lists.newArrayList("r3astromech", "janors", "millenniumfalcon","coolhand"),
            Lists.newArrayList("redace")
    ),
    ion2e(
            none,
            Lists.newArrayList("pulsedrayshield","leebo","feedbackarray"),
            none
    ),
    energy,
    reinforce2e(
            Lists.<String>newArrayList("Reinforce"),
            none,
            none
    ),
    cloak(
            Lists.newArrayList("Cloak"),
            Lists.newArrayList("cloakingdevice"),
            none
    ),
    idtoken,
    disarm2e(
            Lists.newArrayList("SLAM", "Reload"),
            Lists.newArrayList("burnoutslam", "arccaster"),
            Lists.newArrayList("corranhorn", "quinnjast")
    ),
    tractor2e(
            none,
            Lists.newArrayList("tractorbeam", "spacetugtractorarray", "shadowcaster"),
            Lists.newArrayList("lancerclasspursuitcraft/ketsuonyo", "unkarplutt")
    ),
    hull,
    hugeshipforestats,
    hugeshipaftstats,
    oldstylereinforce,
    jam2e(
            none,
            Lists.newArrayList("jammingbeam"),
            none

    );

    private List<String> actions = Lists.newArrayList();
    private List<String> upgrades = Lists.newArrayList();
    private List<String> pilots = Lists.newArrayList();

    Tokens2e() {
    }

    Tokens2e(List<String> actions, List<String> upgrades, List<String> pilots) {
        this.actions = actions;
        this.upgrades = upgrades;
        this.pilots = pilots;
    }

    public static List<Tokens2e> loadForPilot(VassalXWSPilotPieces2e pilot) {
        List<Tokens2e> tokens = Lists.newArrayList();
        for (Tokens2e token : values()) {
            if (pilot.getShipData() != null) {
                boolean passThroughShipActions = false;
                if(pilot.getPilotData().getShipActions().size() > 0)
                    for(XWS2Pilots.PilotAction shipAction : pilot.getPilotData().getShipActions()){
                        if (token.actions.contains(shipAction.getType())) {
                            tokens.add(token);
                        }
                        passThroughShipActions = true;
                }
                if(passThroughShipActions == false)
                    for (XWS2Pilots.PilotAction action : pilot.getShipData().getActions()) {
                    if (token.actions.contains(action.getType())) {
                        tokens.add(token);
                    }
                }

                if (pilot.getPilotData() != null) {
                    String shipPilot = pilot.getPilotData().getXWS();
                    if (token.pilots.contains(shipPilot)) {
                        tokens.add(token);
                    }
                }

            }

            for (VassalXWSPilotPieces2e.Upgrade upgrade : pilot.getUpgrades()) {
                if (token.upgrades.contains(upgrade.getXwsName())) {
                    tokens.add(token);
                }
            }
        }

        return tokens;
    }
}
