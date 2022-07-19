package mic;

/**
 * Created by amatheny on 2/11/17.
 */
public enum Obstacles {
    coreasteroid0("Asteroid 5"),
    coreasteroid1("Asteroid 1"),
    coreasteroid2("Asteroid 3"),
    coreasteroid3("Asteroid 2"),
    coreasteroid4("Asteroid 6"),
    coreasteroid5("Asteroid 4"),
    yt2400debris0("Debris 5"),
    yt2400debris1("Debris 4"),
    yt2400debris2("Debris 6"),
    vt49decimatordebris0("Debris 1"),
    vt49decimatordebris1("Debris 2"),
    vt49decimatordebris2("Debris 3"),
    core2asteroid0("Asteroid 12"),
    core2asteroid1("Asteroid 11"),
    core2asteroid2("Asteroid 10"),
    core2asteroid3("Asteroid 8"),
    core2asteroid4("Asteroid 7"),
    core2asteroid5("Asteroid 9"),
    gascloud1("Gas Cloud 1"),
    gascloud2("Gas Cloud 2"),
    gascloud3("Gas Cloud 3"),
    gascloud4("Gas Cloud 4"),
    gascloud5("Gas Cloud 5"),
    gascloud6("Gas Cloud 6"),
    pomasteroid1("Pride of Mandalore Asteroid 1"),
    pomasteroid2("Pride of Mandalore Asteroid 2"),
    pomasteroid3("Pride of Mandalore Asteroid 3"),
    pomdebris1("Pride of Mandalore Debris 1"),
    pomdebris2("Pride of Mandalore Debris 2"),
    pomdebris3("Pride of Mandalore Debris 3");

    private final String vassalName;

    Obstacles(String vassalName) {
        this.vassalName = vassalName;
    }

    public static Obstacles forVassalName(String vassalName) {
        if (vassalName == null) {
            return null;
        }
        for (Obstacles obstacle : values()) {
            if (obstacle.vassalName.equals(vassalName)) {
                return obstacle;
            }
        }
        return null;
    }

    public static Obstacles forXwsName(String xwsObstacleName) {
        if (xwsObstacleName == null) {
            return null;
        }
        try {
            return Obstacles.valueOf(xwsObstacleName.trim().toLowerCase());
        } catch (Exception e) {
            return null;
        }
    }
}
