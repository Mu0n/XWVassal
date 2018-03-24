package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Map;

public class MasterConditionData extends ArrayList<MasterConditionData.ConditionData> {
    public static String REMOTE_URL = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/conditions.js";
    public static String DISPATCHER_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/dispatcher_conditions.json";

    private static Map<String, MasterConditionData.ConditionData> loadedDataByName = null;
    private static Map<String, MasterConditionData.ConditionData> loadedData = null;
    public static MasterConditionData.ConditionData getConditionDataByName(String conditionName) {
        if (loadedDataByName == null) {
            loadData();
        }


        return loadedDataByName.get(conditionName);
    }

    public static MasterConditionData.ConditionData getConditionData(String conditionXws) {
        if (loadedData == null) {
            loadData();
        }


        return loadedData.get(conditionXws);
    }

    protected static void loadData()
    {

        if(loadedDataByName == null) {

            // load data from xwing-data
            loadFromXwingData();

            // load data from dispatcher file
            MasterConditionData dispatcherData = loadFromDispatcher();

            // add in any pilots from dispatcher that aren't in xwing-data
            if (dispatcherData != null)
            {
                for (MasterConditionData.ConditionData condition : dispatcherData)
                {

                    if(loadedDataByName.get(condition.getName()) == null)
                    {
                        loadedDataByName.put(condition.getName(), condition);
                    }
                    if(loadedData.get(condition.getXws()) == null)
                    {
                        loadedData.put(condition.getXws(), condition);
                    }

                }
            }
        }

    }

    private static void loadFromXwingData()
    {
        // load from xwing-data
        MasterConditionData data = Util.loadRemoteJson(REMOTE_URL, MasterConditionData.class);
        if (data == null) {
            Util.logToChat("Unable to load xwing-data for conditions from the web, falling back to local copy");
            data = Util.loadClasspathJson("conditions.json", MasterConditionData.class);
        }

        loadedDataByName = Maps.newHashMap();
        loadedData = Maps.newHashMap();
        for(MasterConditionData.ConditionData condition : data)
        {

            loadedDataByName.put(condition.getName(),condition);
            loadedData.put(condition.getXws(),condition);
            //loadedData.put(xwsShip + "/" + pilot.getXws(), pilot);
        }
    }

    private static MasterConditionData loadFromDispatcher()
    {
        // load from dispatch
        MasterConditionData data = Util.loadRemoteJson(DISPATCHER_URL, MasterConditionData.class);
        if (data == null) {
            Util.logToChat("Unable to load dispatcher for conditions from the web, falling back to local copy");
            data = Util.loadClasspathJson("dispatcher_conditions.json", MasterConditionData.class);
            if(data == null)
            {
                Util.logToChat("Unable to load dispatcher for conditions from the local copy.  Error in JSON format?");
            }
        }

        return data;
    }

    public static class ConditionData {

        @JsonProperty("name")
        private String name;

        @JsonProperty("xws")
        private String xws;

        public String getXws() {
            return xws;
        }

        public String getName() {
            return name;
        }

    }
}
