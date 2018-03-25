package mic.ota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import mic.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class OTAMasterDialHides  extends ArrayList<OTAMasterDialHides.OTADialHide> {
    private static String REMOTE_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/dial_images.json";

    private static Map<String, OTAMasterDialHides.OTADialHide> loadedData = null;

    public Collection<OTAMasterDialHides.OTADialHide> getAllDialHides()
    {
        if(loadedData == null)
        {
            loadData();
        }
        // Object[] actions = loadedData.values().toArray();
        return loadedData.values();

    }

    public static OTAMasterDialHides.OTADialHide getDialHide(String xws)
    {
        if (loadedData == null) {
            loadData();
        }
        return loadedData.get(xws);
    }

    private static void loadData() {

        // load from
        OTAMasterDialHides data = Util.loadRemoteJson(REMOTE_URL, OTAMasterDialHides.class);
        loadedData = Maps.newHashMap();
        if (data == null) {
            Util.logToChat("Unable to load OTA dial from the web");
        }else {

            // <faction>_<shipxws>_<pilotxws>
            for (OTAMasterDialHides.OTADialHide dialHide : data) {
                loadedData.put(dialHide.getXws(), dialHide);
            }
        }

    }

    public static class OTADialHide {


        @JsonProperty("xws")
        private String xws;

        @JsonProperty("image")
        private String image;

        private boolean status;

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
    }

}
