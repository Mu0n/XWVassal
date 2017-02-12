package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by amatheny on 2/11/17.
 */
public class MasterPilotData extends ArrayList<MasterPilotData.PilotData> {

    private static Map<String, PilotData> loadedData = null;

    public static Map<String, PilotData> getPilotDataByXWSId() {
        if (loadedData != null) {
            return loadedData;
        }

        loadedData = Maps.newHashMap();
        MasterPilotData data = Util.loadClasspathJson("pilots.json", MasterPilotData.class);

        for(PilotData pilot : data) {
            loadedData.put(pilot.getXws(), pilot);
        }
        return loadedData;
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
    }
}
