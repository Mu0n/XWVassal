package mic.ota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import mic.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class OTAMasterConditions extends ArrayList<OTAMasterConditions.OTACondition>
{

 //   private static String REMOTE_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/condition_images.json";

    private static Map<String, OTAMasterConditions.OTACondition> loadedData = null;

    public static void flushData()
    {
        loadedData = null;
    }
    public Collection<OTAMasterConditions.OTACondition> getAllConditions(int edition)
    {
        if(loadedData == null)
        {
            loadData(edition);
        }

        return loadedData.values();

    }

    private static void loadData(int edition) {

        // load from
        OTAMasterConditions data = new OTAMasterConditions();
        if(edition == 1) data = Util.loadRemoteJson(OTAContentsChecker.OTA_CONDITIONS_JSON_URL, OTAMasterConditions.class);
        else if(edition == 2) data = Util.loadRemoteJson(OTAContentsChecker.OTA_CONDITIONS_JSON_URL_2E, OTAMasterConditions.class);
        loadedData = Maps.newHashMap();
        if (data == null) {
            Util.logToChat("Unable to load OTA conditions from the web");
        }else {


            for (OTAMasterConditions.OTACondition condition : data)
            {
                // String pilotKey = pilot.getFaction() +"_"+pilot.getShipXws()+"_"+pilot.getPilotXws();
                loadedData.put(condition.getImage(), condition);
            }
        }

    }

    public static class OTACondition {

        @JsonProperty("xws")
        private String xws;

        @JsonProperty("image")
        private String image;

        @JsonProperty("tokenimage")
        private String tokenImage;

        private boolean status;
        private boolean tokenStatus;

        public String getXws() {
            return xws;
        }
        public String getImage() {
            return image;
        }

        public String getTokenImage()
        {
            return tokenImage;
        }

        public boolean getStatus()
        {
            return status;
        }

        public boolean getTokenStatus()
        {
            return tokenStatus;
        }

        public void setXws(String xws) {
            this.xws = xws;
        }
        public void setImage(String image) {
            this.image = image;
        }
        public void setTokenImage(String tokenImage)
        {
            this.tokenImage = tokenImage;
        }

        public void setStatus(boolean status)
        {
            this.status = status;
        }
        public void setTokenStatus(boolean tokenStatus)
        {
            this.tokenStatus = tokenStatus;
        }
    }
}
