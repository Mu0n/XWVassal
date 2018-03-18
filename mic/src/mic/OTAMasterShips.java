package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OTAMasterShips extends ArrayList<OTAMasterShips.OTAShip> {

    private static String REMOTE_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/ship_images.json";

    private static Map<String, OTAMasterShips.OTAShip> loadedData = null;

    public Collection<OTAShip> getAllShips()
    {
        if(loadedData == null)
        {
            loadData();
        }
       // Object[] actions = loadedData.values().toArray();
        return loadedData.values();

    }

    public static OTAMasterShips.OTAShip getShip(String shipxws, String identifier) {
        if (loadedData == null) {
            loadData();
        }
        String shipKey = shipxws+"_"+identifier;
        return loadedData.get(shipKey);
    }

    private static void loadData() {

        // load from
        OTAMasterShips data = Util.loadRemoteJson(REMOTE_URL, OTAMasterShips.class);
        loadedData = Maps.newHashMap();
        if (data == null) {
            Util.logToChat("Unable to load OTA pilots from the web");
        }else {

            // <faction>_<shipxws>_<pilotxws>
            for (OTAMasterShips.OTAShip ship : data) {
                String shipKey = ship.getXws()+"_"+ship.getIdentifier();
                loadedData.put(shipKey, ship);
            }
        }

    }

    public static class OTAShip {

        @JsonProperty("xws")
        private String xws;

        @JsonProperty("identifier")
        private String identifier;

        @JsonProperty("image")
        private String image;

        @JsonProperty("faction")
        private List<String> factions = Lists.newArrayList();

        public String getXws() {
            return xws;
        }
        public String getImage() {
            return image;
        }
        public String getIdentifier() {return identifier;}
        public List<String> getFactions() {
            return this.factions;
        }

    }
}
