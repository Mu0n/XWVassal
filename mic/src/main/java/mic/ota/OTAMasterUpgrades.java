package mic.ota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import mic.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class OTAMasterUpgrades  extends ArrayList<OTAMasterUpgrades.OTAUpgrade> {

  //  private static String REMOTE_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/upgrade_images.json";

    private static Map<String, OTAMasterUpgrades.OTAUpgrade> loadedData = null;

    public static void flushData()
    {
        loadedData = null;
    }

    public Collection<OTAMasterUpgrades.OTAUpgrade> getAllUpgrades(int edition)
    {
        if(loadedData == null)
        {
            loadData(edition);
        }

        return loadedData.values();

    }

    private static void loadData(int edition) {

        // load from
        OTAMasterUpgrades data = new OTAMasterUpgrades();
        if(edition == 1) data = Util.loadRemoteJson(OTAContentsChecker.OTA_UPGRADES_JSON_URL, OTAMasterUpgrades.class);
        else if(edition == 2) data = Util.loadRemoteJson(OTAContentsChecker.OTA_UPGRADES_JSON_URL_2E, OTAMasterUpgrades.class);
        loadedData = Maps.newHashMap();
        if (data == null) {
            Util.logToChat("Unable to load OTA upgrades from the web");
        }else {


            for (OTAMasterUpgrades.OTAUpgrade upgrade : data)
            {
               // String pilotKey = pilot.getFaction() +"_"+pilot.getShipXws()+"_"+pilot.getPilotXws();
                loadedData.put(upgrade.getImage(), upgrade);
            }
        }

    }

    public static class OTAUpgrade {


        @JsonProperty("xws")
        private String xws;

        @JsonProperty("slot")
        private String slot;

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
        public String getSlot()
        {
            return slot;
        }

        public boolean getStatus()
        {
            return status;
        }
        public boolean getStatusOTA() { return statusOTA; }

        public void setXws(String xws) {
            this.xws = xws;
        }
        public void setImage(String image) {
            this.image = image;
        }
        public void setPilotXws(String slot)
        {
            this.slot = slot;
        }

        public void setStatus(boolean status)
        {
            this.status = status;
        }
        public void setStatusOTA(boolean statusOTA){
            this.statusOTA = statusOTA;
        }
    }
}
