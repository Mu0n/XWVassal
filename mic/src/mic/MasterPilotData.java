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
public class MasterPilotData extends ArrayList<MasterPilotData.PilotData> {

    private static Map<String, PilotData> loadedData = null;

    public static PilotData getPilotData(String ship, String pilot) {
        if ( loadedData == null) {
            loadData();
        }
        return loadedData.get(ship + "/" + pilot);
    }

    private static void loadData() {

        loadedData = Maps.newHashMap();
        MasterPilotData data = Util.loadClasspathJson("pilots.json", MasterPilotData.class);

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

        @JsonProperty("xws")
        private String xws;

        @JsonProperty("unique")
        private boolean unique;

        @JsonProperty("conditions")
        private List<String> conditions = Lists.newArrayList();

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
}
