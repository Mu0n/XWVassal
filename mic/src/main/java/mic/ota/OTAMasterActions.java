package mic.ota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import mic.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class OTAMasterActions extends ArrayList<OTAMasterActions.OTAAction> {

//    private static String REMOTE_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/action_images.json";

    private static Map<String,OTAMasterActions.OTAAction> loadedData = null;

    public static void flushData()
    {
        loadedData = null;
    }

    public Collection<OTAAction> getAllActions()
    {
        if(loadedData == null)
        {
            loadData();
        }
       // Object[] actions = loadedData.values().toArray();
        return loadedData.values();

    }

    public static OTAMasterActions.OTAAction getAction(String actionName) {
        if (loadedData == null) {
            loadData();
        }
        return loadedData.get(actionName);
    }

    private static void loadData() {

        // load from
        OTAMasterActions data = Util.loadRemoteJson(OTAContentsChecker.OTA_ACTIONS_JSON_URL, OTAMasterActions.class);
        loadedData = Maps.newHashMap();
        if (data == null) {
            Util.logToChat("Unable to load OTA Actions from the web");
        }else {


            for (OTAMasterActions.OTAAction action : data) {
                loadedData.put(action.getName(), action);
            }
        }

    }

    public static class OTAAction {

        @JsonProperty("name")
        private String name;


        @JsonProperty("image")
        private String image;

        private boolean status;
        private boolean statusOTA;

        public void setStatus(boolean status)
        {
            this.status = status;
        }

        public boolean getStatus()
        {
            return status;
        }
        public boolean getStatusOTA() { return statusOTA; }
        public String getName() {
            return name;
        }
        public String getImage() {
            return image;
        }


        public void setStatusOTA(boolean statusOTA) {
            this.statusOTA = statusOTA;
        }
    }
}
