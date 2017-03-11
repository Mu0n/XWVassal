package mic;

import static mic.Util.none;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Created by amatheny on 2/11/17.
 */
public enum Tokens {
    shield(
            none,
            Lists.newArrayList("gonk"),
            none
    ),
    targetlock(
            Lists.newArrayList("Target Lock"),
            Lists.newArrayList("targetingcomputer", "firecontrolsystem"),
            none
    ),
    stress(
            none,
            Lists.newArrayList("zuckuss", "r3a2"),
            Lists.newArrayList("awing/tychocelchu")
    ),
    focus(
            Lists.newArrayList("Focus"),
            none,
            none
    ),
    evade(
            Lists.newArrayList("Evade"),
            Lists.newArrayList("r3astromech", "janors", "millenniumfalcon"),
            Lists.newArrayList("t70xwing/redace")
    ),
    crit,
    ion,
    energy,
    reinforce,
    cloak(
            Lists.newArrayList("Cloak"),
            Lists.newArrayList("cloakingdevice"),
            none
    ),
    idtoken,
    weaponsdisabled(
            Lists.newArrayList("SLAM"),
            Lists.newArrayList("burnoutslam", "arccaster"),
            Lists.newArrayList("ewing/corranhorn", "m3ainterceptor/quinnjast")
    ),
    initiative,
    ordnance(
            none,
            Lists.newArrayList("extramunitions"),
            none
    ),
    tractorbeam(
            none,
            Lists.newArrayList("tractorbeam", "spacetugtractorarray", "shadowcaster"),
            Lists.newArrayList("lancerclasspursuitcraft/ketsuonyo", "quadjumper/unkarplutt")
    ),
    hull,
    hugeshipforestats,
    hugeshipaftstats;

    private List<String> actions = Lists.newArrayList();
    private List<String> upgrades = Lists.newArrayList();
    private List<String> pilots = Lists.newArrayList();

    Tokens() {
    }

    Tokens(List<String> actions, List<String> upgrades, List<String> pilots) {
        this.actions = actions;
        this.upgrades = upgrades;
        this.pilots = pilots;
    }

    public static List<Tokens> loadForPilot(VassalXWSPilotPieces pilot) {
        List<Tokens> tokens = Lists.newArrayList();
        for (Tokens token : values()) {
            if (pilot.getShipData() != null) {
                for (String action : pilot.getShipData().getActions()) {
                    if (token.actions.contains(action)) {
                        tokens.add(token);
                    }
                }

                if (pilot.getPilotData() != null) {
                    String shipPilot = pilot.getShipData().getXws() + "/" + pilot.getPilotData().getXws();
                    if (token.pilots.contains(shipPilot)) {
                        tokens.add(token);
                    }
                }
            }

            for (VassalXWSPilotPieces.Upgrade upgrade : pilot.getUpgrades()) {
                if (token.upgrades.contains(upgrade.getXwsName())) {
                    tokens.add(token);
                }
            }
        }

        return tokens;
    }
}
