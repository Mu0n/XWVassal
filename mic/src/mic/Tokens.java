package mic;

import com.google.common.collect.Lists;

import java.util.List;

import static mic.Util.none;

/**
 * Created by amatheny on 2/11/17.
 */
public enum Tokens {
    shield,
    targetlock(
            Lists.newArrayList("Target Lock"),
            Lists.newArrayList("targetingcomputer"),
            none
    ),
    stress(
            none,
            Lists.newArrayList("zuckuss", "r2a2"),
            Lists.newArrayList("awing/tychocelchu")
    ),
    focus(
            Lists.newArrayList("Focus"),
            none,
            none
    ),
    evade(
            Lists.newArrayList("Evade"),
            Lists.newArrayList("r3astromech", "janors"),
            none
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
            Lists.newArrayList("burnoutslam"),
            none
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

    Tokens() { }

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
