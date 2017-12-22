package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by amatheny on 2/11/17.
 */
public class MasterShipData extends ArrayList<MasterShipData.ShipData> {

    private static String REMOTE_URL = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/ships.js";

    private static Map<String, ShipData> loadedData = null;

    public static ShipData getShipData(String shipXwsId) {
        if (loadedData == null) {
            loadData();
        }
        return loadedData.get(shipXwsId);
    }

    protected static void loadData() {
        MasterShipData data = Util.loadRemoteJson(REMOTE_URL, MasterShipData.class);
        if (data == null) {
            Util.logToChat("Unable to load xwing-data for ships from the web, falling back to local copy");
            data = Util.loadClasspathJson("ships.json", MasterShipData.class);
        }

        loadedData = Maps.newHashMap();
        for(ShipData ship : data) {
            loadedData.put(ship.getXws(), ship);
        }
    }

    public static class ShipData {

        @JsonProperty("attack")
        private int attack = 0;

        @JsonProperty("agility")
        private int agility = 0;

        @JsonProperty("hull")
        private int hull = 0;

        @JsonProperty("shields")
        private int shields = 0;

        @JsonProperty("energy")
        private int energy = 0;

        @JsonProperty("xws")
        private String xws;

        @JsonProperty("actions")
        private List<String> actions = Lists.newArrayList();

        @JsonProperty("maneuvers")
        private List<List<Integer>> maneuvers = Lists.newArrayList();

        @JsonProperty("size")
        private String size;

        public int getAttack() {
            return attack;
        }

        public int getAgility() {
            return agility;
        }

        public int getHull() {
            return hull;
        }

        public int getShields() {
            return shields;
        }

        public int getEnergy() {
            return energy;
        }

        public String getXws() {
            return xws;
        }

        public List<String> getActions() {
            return this.actions;
        }

        public List<List<Integer>> getManeuvers() { return maneuvers; }

        public boolean hasSmallBase() {
            return "small".equals(this.size);
        }

        public boolean hasLargeBase() {
            return "large".equals(this.size);
        }

        public boolean hasHugeBase() {
            return "huge".equals(this.size);
        }
    }
}
