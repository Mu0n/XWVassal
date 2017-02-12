package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by amatheny on 2/11/17.
 */
public class MasterShipData extends ArrayList<MasterShipData.ShipData> {

    private static Map<String, ShipData> loadedData = null;

    public static Map<String, ShipData> getShipDataByXWSId() {
        if (loadedData != null) {
            return loadedData;
        }

        loadedData = Maps.newHashMap();
        MasterShipData data = Util.loadClasspathJson("ships.json", MasterShipData.class);

        for(ShipData ship : data) {
            loadedData.put(ship.getXws(), ship);
        }
        return loadedData;
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
    }
}
