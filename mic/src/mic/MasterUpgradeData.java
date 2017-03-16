package mic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by guido on 11/02/17.
 */
public class MasterUpgradeData extends ArrayList<MasterUpgradeData.UpgradeData> {

    private static String REMOTE_URL = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/upgrades.js";

    private static Map<String, UpgradeData> loadedData = null;

    public static UpgradeData getUpgradeData(String upgradeXwsId) {
        if (loadedData == null) {
            loadData();
        }
        return loadedData.get(upgradeXwsId);
    }

    protected static void loadData() {
        MasterUpgradeData data = Util.loadRemoteJson(REMOTE_URL, MasterUpgradeData.class);
        if (data == null) {
            Util.logToChat("Unable to load xwing-data for upgrades from the web, falling back to local copy");
            data = Util.loadClasspathJson("upgrades.json", MasterUpgradeData.class);
        }

        loadedData = Maps.newHashMap();
        for(UpgradeData upgrade : data) {
            loadedData.put(upgrade.getXws(), upgrade);
        }
    }

    public static class UpgradeData {

        @JsonProperty("name")
        private String name;

        @JsonProperty("points")
        private Integer points = 0;

        @JsonProperty("xws")
        private String xws;

        @JsonProperty("grants")
        private ArrayList<UpgradeGrants> grants = Lists.newArrayList();

        @JsonProperty("conditions")
        private List<String> conditions = Lists.newArrayList();

        @JsonProperty("slot")
        private String slot;

        public List<String> getConditions() {
            return conditions;
        }

        public ArrayList<UpgradeGrants> getGrants() {
            return grants;
        }

        public String getXws() {
            return xws;
        }

        public String getName() {
            return name;
        }

        public String getSlot() {
            return slot;
        }

        public int getPoints() {
            return points;
        }
    }

    public static class UpgradeGrants {

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private String type;

        @JsonProperty("value")
        private int value;

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public int getValue() {
            return value;
        }

        public boolean isStatsModifier() {
            return type.equals("stats");
        }
    };
}
