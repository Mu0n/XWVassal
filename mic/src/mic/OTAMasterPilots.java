package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class OTAMasterPilots extends ArrayList<OTAMasterPilots.OTAPilot> {

    private static String REMOTE_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/pilot_images.json";

    private static Map<String, OTAMasterPilots.OTAPilot> loadedData = null;

    public Collection<OTAPilot> getAllPilots()
    {
        if(loadedData == null)
        {
            loadData();
        }
       // Object[] actions = loadedData.values().toArray();
        return loadedData.values();

    }

    public static OTAMasterPilots.OTAPilot getPilot(String pilotxws, String faction, String shipxws) {
        if (loadedData == null) {
            loadData();
        }
        String pilotKey = faction +"_"+shipxws+"_"+pilotxws;
        return loadedData.get(pilotKey);
    }

    private static void loadData() {

        // load from
        OTAMasterPilots data = Util.loadRemoteJson(REMOTE_URL, OTAMasterPilots.class);
        loadedData = Maps.newHashMap();
        if (data == null) {
            Util.logToChat("Unable to load OTA pilots from the web");
        }else {

            // <faction>_<shipxws>_<pilotxws>
            for (OTAMasterPilots.OTAPilot pilot : data) {
                String pilotKey = pilot.getFaction() +"_"+pilot.getShipXws()+"_"+pilot.getPilotXws();
                loadedData.put(pilotKey, pilot);
            }
        }

    }

    public static class OTAPilot {

        @JsonProperty("shipxws")
        private String shipxws;

        @JsonProperty("pilotxws")
        private String pilotxws;

        @JsonProperty("faction")
        private String faction;

        @JsonProperty("image")
        private String image;


        public String getShipXws() {
            return shipxws;
        }
        public String getImage() {
            return image;
        }
        public String getPilotXws() { return pilotxws;}
        public String getFaction() {return faction;};


    }
}
