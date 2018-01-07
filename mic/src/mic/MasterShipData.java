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

    //TODO change this URL
    private static String DISPATCHER_URL = "https://raw.githubusercontent.com/mrmurphm/XWVassal/new-dial/mic/swxwmg.vmod-unpacked/dispatcher_ships.json";

    private static Map<String, ShipData> loadedData = null;

    public static ShipData getShipData(String shipXwsId) {
        if (loadedData == null) {
            loadData();
        }
        return loadedData.get(shipXwsId);
    }

    protected static void loadData() {

        // load data from xwing-data
        loadFromXwingData();

        // load data from dispatcher file
        MasterShipData dispatcherData = loadFromDispatcher();

        // add in any ships from dispatcher that aren't in xwing-data
        if(dispatcherData != null) {
            for (ShipData ship : dispatcherData) {
                if (loadedData.get(ship.getXws()) == null) {
//                    Util.logToChat("Adding ship " + ship.getXws() + " from dispatcher file");
                    loadedData.put(ship.getXws(), ship);
                }
            }
        }
    }

    private static void loadFromXwingData()
    {
        // load from xwing-data
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

    private static MasterShipData loadFromDispatcher()
    {
        // load from dispatch
        MasterShipData data = Util.loadRemoteJson(DISPATCHER_URL, MasterShipData.class);
        if (data == null) {
            Util.logToChat("Unable to load dispatcher for ships from the web, falling back to local copy");
            data = Util.loadClasspathJson("dispatcher_ships.json", MasterShipData.class);
            if(data == null)
            {
                Util.logToChat("Unable to load dispatcher for ships from the local copy.  Error in JSON format?");
            }
        }

        return data;
    }


    public static class ShipData {

        @JsonProperty("name")
        private String name;

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

        @JsonProperty("dial")
        private List<String> dialManeuvers = Lists.newArrayList();

        @JsonProperty("maneuvers")
        private List<List<Integer>> maneuvers = Lists.newArrayList();

        @JsonProperty("firing_arcs")
        private List<String> firingArcs = Lists.newArrayList();

        @JsonProperty("size")
        private String size;

        public String getName() {
            return name;
        }
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

        public String getSize()
        {
            return size;
        }

        public List<String> getActions() {
            return this.actions;
        }

        public List<String> getFiringArcs() {
            return this.firingArcs;
        }

        public List<String> getDialManeuvers()
        {
            return this.dialManeuvers;
        }

        private void setDialManeuvers(List<String> dialManeuvers)
        {
            this.dialManeuvers = dialManeuvers;
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
