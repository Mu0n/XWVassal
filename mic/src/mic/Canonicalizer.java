package mic;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Created by amatheny on 2/11/17.
 */
public class Canonicalizer {

    private static String invalidCanonicalCharPattern = "[^a-zA-Z0-9]";

    static Map<String, String> pilotFixes = ImmutableMap.<String, String>builder()
            .put("raiderclasscorvettefore", "raiderclasscorvfore")
            .put("raiderclasscorvetteaft", "raiderclasscorvaft")
//            .put("nashtahpuppilot", "nashtahpup")
            .put("nashtahpup", "nashtahpuppilot")
            .put("sabinewrentf", "sabinewren")
            .put("hansolo2", "hansolo-swx57")
            .put("chewbacca2", "chewbacca-swx57")
            .put("poedameron2", "poedameron-swx57")
            .build();

    static Map<String, String> shipFixes = ImmutableMap.<String, String>builder()
            .put("sabinestiefighter", "tiefighter")
            .put("bwings", "bwing")
            .put("ywings", "ywing")
            .put("z95s", "z95headhunter")
            .put("ewings", "ewing")
            .put("xwings", "xwing")
            .put("awings", "awing")
            .put("t70xwings", "t70xwing")
            .put("kwings", "kwing")
            .put("specialforcestie", "tiesffighter")
            .put("tiefighters", "tiefighter")
            .put("tiephantoms", "tiephantom")
            .put("tiedefenders", "tiedefender")
            .put("tiebombers", "tiebomber")
            .put("tieinterceptors", "tieinterceptor")
            .put("raiderclasscorvetteaft", "raiderclasscorvaft")
            .put("lambdas", "lambdaclassshuttle")
            .put("tieadvancedprototype", "tieadvprototype")
            .put("firesprays", "firespray31")
            .build();

    static Map<String, String> upgradeFixes = ImmutableMap.<String, String>builder()
            .put("crew/r2d2", "r2d2-swx22")
            .put("title/milleniumfalcon", "millenniumfalcon")
            .put("title/milleniumfalcon2", "millenniumfalcon-swx57")
            .put("title/adaptativeailerons", "adaptiveailerons")
            .put("title/ghost2","ghost-swx72")
            .build();

    static Map<String, String> upgradeTypeFixes = ImmutableMap.<String, String>builder()
            .put("elitepilottalents", "ept")
            .put("droid", "amd")
            .put("modification", "mod")
            .put("salvagedastromechs", "samd")
            .put("bombs", "bomb")
            .put("titles", "title")
            .put("missiles", "missile")
            .put("teams", "team")
            .put("torpedoes", "torpedo")
            .put("turrets", "turret")
            .build();

    public static String getCanonicalShipName(String shipName) {
        shipName = getCleanedName(shipName);
        return shipFixes.containsKey(shipName) ? shipFixes.get(shipName) : shipName;
    }

    public static String getCanonicalPilotName(String pilotName) {
        pilotName = getCleanedName(pilotName);
        return pilotFixes.containsKey(pilotName) ? pilotFixes.get(pilotName) : pilotName;
    }

    public static String getCanonicalUpgradeName(String upgradeType, String upgradeName) {
        upgradeType = getCanonicalUpgradeTypeName(upgradeType);
        upgradeName = getCleanedName(upgradeName);
        String key = upgradeType + "/" + upgradeName;
        return upgradeFixes.containsKey(key) ? upgradeFixes.get(key) : upgradeName;
    }

    public static String getCanonicalUpgradeTypeName(String upgradeTypeName) {
        upgradeTypeName = getCleanedName(upgradeTypeName);
        return upgradeTypeFixes.containsKey(upgradeTypeName) ? upgradeTypeFixes.get(upgradeTypeName) : upgradeTypeName;
    }

    public static String getCleanedName(String name) {
        if (name == null) {
            return "";
        }
        name = name.replaceAll(invalidCanonicalCharPattern, "").toLowerCase();
        name = name.replaceAll("flippable", "");
        return name;
    }
}
