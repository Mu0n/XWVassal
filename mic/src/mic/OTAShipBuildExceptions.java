package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.*;

public class OTAShipBuildExceptions extends ArrayList<OTAShipBuildExceptions.ShipException> {
    private static String REMOTE_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/ship_build_exceptions.json";

    private static Map<String,ShipException> loadedData = null;

    public Object[] getAllShips()
    {
        if(loadedData == null)
        {
            loadData();
        }
        return loadedData.values().toArray();
    }

    public static OTAShipBuildExceptions.ShipException getShipException(String shipXwsId) {
        if (loadedData == null) {
            loadData();
        }
        return loadedData.get(shipXwsId);
    }

    public static OTAShipBuildExceptions.ShipException getShipExceptionsForShipName(String shipName)
    {
        if (loadedData == null) {
            loadData();
        }
        Collection data = loadedData.values();
        Iterator i = data.iterator();
        boolean found = false;
        OTAShipBuildExceptions.ShipException se = null;
        while(i.hasNext() && !found)
        {
            se = (OTAShipBuildExceptions.ShipException)i.next();
            if(se.getName().equalsIgnoreCase(shipName))
            {
                found = true;
            }
        }
        return se;
    }

    protected static void loadData() {

        // load from
        OTAShipBuildExceptions data = Util.loadRemoteJson(REMOTE_URL, OTAShipBuildExceptions.class);
        loadedData = Maps.newHashMap();
        if (data == null) {
            Util.logToChat("Unable to load OTA Exceptions for ships from the web");
         //   data = Util.loadClasspathJson("OTA_Ship_Build_Exceptions.json", OTAShipBuildExceptions.class);
        }else {


            for (OTAShipBuildExceptions.ShipException shipException : data) {
                loadedData.put(shipException.getXws(), shipException);
            }
        }

    }


    public static class ShipException {

        @JsonProperty("name")
        private String name;


        @JsonProperty("xws")
        private String xws;

        @JsonProperty("images")
        private List<String> images = Lists.newArrayList();


        public String getName() {
            return name;
        }
        public String getXws() {
            return xws;
        }

        public List<String> getImages() {
            return this.images;
        }


    }
}
