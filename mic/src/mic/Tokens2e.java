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
            Lists.newArrayList("Target Lock"),
            Lists.newArrayList("targetingcomputer", "firecontrolsystem"),
            none
    ),
    stress2e(
            Lists.newArrayList("Focus"),
            Lists.newArrayList("zuckuss", "r3a2","elusiveness","overclockedr4","pushthelimit"),
            Lists.newArrayList("awing/tychocelchu","tiefofighter/zetaleader")
    ),
    focus2e(
            Lists.newArrayList("Focus"),
            none,
            none
    ),
    calculate2e(
            Lists.newArrayList("Calculate"),
            none,
            none
    ),
    evade2e(
            Lists.newArrayList("Evade"),
            Lists.newArrayList("r3astromech", "janors", "millenniumfalcon","coolhand"),
            Lists.newArrayList("t70xwing/redace")
    ),
    crit(
            Lists.newArrayList("Focus","Calculate"),
            none,
            none
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
    charge2e(
            none,
            none,
            Lists.newArrayList("hwk290/roarkgarnet", "tiefofighter/epsilonace")
    ),
    force(
            none,
            Lists.newArrayList("jabbathehutt"),
            none
            ),
    cloak(
            Lists.newArrayList("Cloak"),
            Lists.newArrayList("cloakingdevice"),
            none
    ),
    idtoken,
    disarm2e(
            Lists.newArrayList("SLAM"),
            Lists.newArrayList("burnoutslam", "arccaster"),
            Lists.newArrayList("ewing/corranhorn", "m3ainterceptor/quinnjast")
    ),
    tractor2e(
            none,
            Lists.newArrayList("tractorbeam", "spacetugtractorarray", "shadowcaster"),
            Lists.newArrayList("lancerclasspursuitcraft/ketsuonyo", "quadjumper/unkarplutt")
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
