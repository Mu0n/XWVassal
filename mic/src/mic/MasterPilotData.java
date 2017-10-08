package mic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by amatheny on 2/11/17.
 */
public class MasterPilotData extends ArrayList<MasterPilotData.PilotData> {

    private static String REMOTE_URL = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/pilots.js";

    private static Map<String, PilotData> loadedData = null;

    public static PilotData getPilotData(String ship, String pilot) {
        if (loadedData == null) {
            loadData();
        }
        return loadedData.get(ship + "/" + pilot);
    }

    protected static void loadData() {
        MasterPilotData data = Util.loadRemoteJson(REMOTE_URL, MasterPilotData.class);
        if (data == null) {
            Util.logToChat("Unable to load xwing-data for pilots from the web, falling back to local copy");
            data = Util.loadClasspathJson("pilots.json", MasterPilotData.class);
        }

        loadedData = Maps.newHashMap();
        for(PilotData pilot : data) {
            String xwsShip = Canonicalizer.getCanonicalShipName(pilot.getShip());
            loadedData.put(xwsShip + "/" + pilot.getXws(), pilot);
        }
    }

    public static class PilotData {

        @JsonProperty("skill")
        private String skill = "0";

        @JsonProperty("ship")
        private String ship;

        @JsonProperty("name")
        private String name;

        @JsonProperty("points")
        private String points = "0";

        @JsonProperty("xws")
        private String xws;

        @JsonProperty("unique")
        private boolean unique;

        @JsonProperty("conditions")
        private List<String> conditions = Lists.newArrayList();

        @JsonProperty("ship_override")
        private ShipOverrides shipOverrides;

        public ShipOverrides getShipOverrides() {
            return shipOverrides;
        }

        public boolean isUnique() {
            return unique;
        }

        public int getSkill() {
            try {
                return Integer.parseInt(skill);
            } catch (Exception e) {
                return 0;
            }
        }

        @JsonIgnore
        public void setSkill(Integer skill)
        {
            this.skill = skill.toString();
        }
        
        public int getPoints() {
            try {
                return Integer.parseInt(points);
            } catch (Exception e) {
                return 0;
            }
        }

        public String getXws() {
            return xws;
        }

        public String getShip() {
            return ship;
        }

        public String getName() {
            return name;
        }

        public List<String> getConditions() {
            return conditions;
        }
    }

    public static class ShipOverrides {
        private int attack;
        private int agility;
        private int hull;
        private int shields;

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
    }
}
