package mic.ota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mic.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OTAMasterDialPlates extends ArrayList<OTAMasterDialPlates.OTADialPlate> {
  //  private static String REMOTE_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/master/json/dialPlates_images.json";

    private static Map<String, OTAMasterDialPlates.OTADialPlate> loadedData = null;
    public static void flushData()
    {
        loadedData = null;
    }
    public Collection<OTAMasterDialPlates.OTADialPlate> getAllDialPlates()
    {
        if(loadedData == null)
        {
            loadData();
        }
        // Object[] actions = loadedData.values().toArray();
        return loadedData.values();

    }

    public static OTAMasterDialPlates.OTADialPlate getDialPlate(String xws)
    {
        if (loadedData == null) {
            loadData();
        }
        return loadedData.get(xws);
    }

    private static void loadData() {

        // load from
        OTAMasterDialPlates data = Util.loadRemoteJson(OTAContentsChecker.OTA_DIALPLATES_JSON_URL_2E, OTAMasterDialPlates.class);
        loadedData = Maps.newHashMap();
        if (data == null) {
            Util.logToChat("Unable to load OTA dial plate from the web");
        }else {

            // <faction>_<shipxws>_<pilotxws>
            for (OTAMasterDialPlates.OTADialPlate dialPlate : data) {
                loadedData.put(dialPlate.getXws(), dialPlate);
            }
        }

    }

    public static class OTADialPlate {
        @JsonProperty("xws")
        private String xws;

        @JsonProperty("image")
        private String image;

        private boolean status;
        private boolean statusOTA;

        public String getXws() {
            return xws;
        }
        public String getImage() {
            return image;
        }
        public boolean getStatus()
        {
            return status;
        }
        public void setXws(String xws) {
            this.xws = xws;
        }
        public void setImage(String image) {
            this.image = image;
        }
        public void setStatus(boolean status)
        {
            this.status = status;
        }
        public boolean getStatusOTA() { return this.statusOTA; }

        public void setStatusOTA(boolean statusOTA) {
            this.statusOTA = statusOTA;
        }
    }

}
