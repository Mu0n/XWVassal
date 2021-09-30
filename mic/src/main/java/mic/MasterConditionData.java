package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import mic.ota.OTAContentsChecker;

import java.util.ArrayList;
import java.util.Map;

public class MasterConditionData extends ArrayList<MasterConditionData.ConditionData> {
    public static String REMOTE_URL = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/conditions.js";
  //  public static String DISPATCHER_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/dispatcher_conditions.json";

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

            // dispatcher overrides xwing-data
            if (dispatcherData != null)
            {
                for (MasterConditionData.ConditionData dispatcherCondition : dispatcherData)
                {

                    MasterConditionData.ConditionData xwingDataCondition = loadedData.get(dispatcherCondition.getXws());

                    // If there is no dispatcher version of this condition, store the xwing-data version
                    if(dispatcherCondition == null)
                    {
                        loadedData.put(xwingDataCondition.getXws(), xwingDataCondition);

                        // if there is no xwing-data version of this condition, store the dispatcher version
                    }else if(xwingDataCondition == null)
                    {
                        loadedData.put(dispatcherCondition.getXws(), dispatcherCondition);
                        // There are both xwing-data and dispatcher versions, so merge them, with dispatcher taking precedence
                    }else{
                        // do the merge.  Dispatcher overrides
                        MasterConditionData.ConditionData mergedCondition = mergeConditions(xwingDataCondition,dispatcherCondition);
                        loadedData.put(mergedCondition.getXws(), mergedCondition);
                    }


                }
            }
        }

    }

    private static String mergeProperties(String baseString, String overrideString)
    {
        return overrideString == null ? baseString : overrideString;
    }

    private static MasterConditionData.ConditionData mergeConditions(MasterConditionData.ConditionData baseCondition, MasterConditionData.ConditionData overrideCondition) {

        MasterConditionData.ConditionData mergedCondition = new MasterConditionData.ConditionData();
        mergedCondition.setXws(baseCondition.getXws());

        mergedCondition.setName(mergeProperties(baseCondition.getName(),overrideCondition.getName()));

        return mergedCondition;
    }

    private static void loadFromXwingData()
    {
        // load from xwing-data
        MasterConditionData data = Util.loadRemoteJson(REMOTE_URL, MasterConditionData.class);
        if (data == null) {
          //  Util.logToChat("Unable to load xwing-data for conditions from the web, falling back to local copy");
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
        MasterConditionData data = Util.loadRemoteJson(OTAContentsChecker.OTA_DISPATCHER_CONDITIONS_JSON_URL, MasterConditionData.class);
        if (data == null) {
          //  Util.logToChat("Unable to load dispatcher for conditions from the web, falling back to local copy");
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

        private void setXws(String xws)
        {
            this.xws = xws;
        }

        public String getName() {
            return name;
        }

        private void setName(String name)
        {
            this.name = name;
        }

    }
}
