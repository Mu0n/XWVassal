package mic;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by amatheny on 2/9/17.
 */
public class NameFixes {

    static Map<String, String> pilotFixes = ImmutableMap.<String, String>builder()
            .put("raiderclasscorvettefore", "raiderclasscorvfore")
            .put("raiderclasscorvetteaft", "raiderclasscorvaft")
            .put("nashtahpuppilot", "nashtahpup")
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
            .put("ept/adaptabilityflippable", "adaptability")
            .put("title/milleniumfalcon", "millenniumfalcon")
            .put("title/milleniumfalcon2", "millenniumfalcon-swx57")
            .put("title/adaptativeailerons", "adaptiveailerons")
            .put("title/pivotwingflippable", "pivotwing")
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

    public static String fixShipName(String shipName) {
        return shipFixes.containsKey(shipName) ? shipFixes.get(shipName) : shipName;
    }

    public static String fixPilotName(String pilotName) {
        return pilotFixes.containsKey(pilotName) ? pilotFixes.get(pilotName) : pilotName;
    }

    public static String fixUpgradeName(String upgradeType, String upgradeName) {
        String key = upgradeType + "/" + upgradeName;
        return upgradeFixes.containsKey(key) ? upgradeFixes.get(key) : upgradeName;
    }

    public static String fixUpgradeTypeName(String upgradeTypeName) {
        return upgradeTypeFixes.containsKey(upgradeTypeName) ? upgradeTypeFixes.get(upgradeTypeName) : upgradeTypeName;
    }
}
