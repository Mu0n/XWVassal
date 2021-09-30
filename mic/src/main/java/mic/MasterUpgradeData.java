package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mic.ota.OTAContentsChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mic.Util.logToChat;

/**
 * Created by guido on 11/02/17.
 */
public class MasterUpgradeData extends ArrayList<MasterUpgradeData.UpgradeData> {

    public static String REMOTE_URL = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/upgrades.js";
    //public static String DISPATCHER_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/dispatcher_upgrades.json";



    private static Map<String, UpgradeData> loadedData = null;


    public static UpgradeData getUpgradeData(String upgradeXwsId) {
        if (loadedData == null) {
            loadData();
        }
        return loadedData.get(upgradeXwsId);
    }



    protected static void loadData(Boolean wantFullControl, String altDispatcherString) {
            // load data from xwing-data
            if(wantFullControl==false) loadFromXwingData();

            // load data from dispatcher file
            MasterUpgradeData dispatcherData = loadFromDispatcher(altDispatcherString);

            // dispatcher overrides xwing-data
            if (dispatcherData != null)
            {
                for (UpgradeData dispatcherUpgrade : dispatcherData)
                {
                    UpgradeData xwingDataUpgrade = loadedData.get(dispatcherUpgrade.getXws());

                    // If there is no dispatcher version of this upgrade, store the xwing-data version
                    if(dispatcherUpgrade == null)
                    {
                        loadedData.put(xwingDataUpgrade.getXws(), xwingDataUpgrade);

                        // if there is no xwing-data version of this upgrade, store the dispatcher version
                    }else if(xwingDataUpgrade == null)
                    {
                        loadedData.put(dispatcherUpgrade.getXws(), dispatcherUpgrade);
                        // There are both xwing-data and dispatcher versions, so merge them, with dispatcher taking precedence
                    }else{
                        // do the merge.  Dispatcher overrides
                        UpgradeData mergedUpgrade = mergeUpgrades(xwingDataUpgrade,dispatcherUpgrade);
                        loadedData.put(mergedUpgrade.getXws(), mergedUpgrade);
                    }
                }
            }
    }

    protected static void loadData2(){
        if(loadedData == null) {
            // load data from xwing-data
            loadFromXwingData2();
        }
    }
    protected static void loadData() {

        if(loadedData == null) {
            // load data from xwing-data
            loadFromXwingData();

            // load data from dispatcher file
            MasterUpgradeData dispatcherData = loadFromDispatcher();

            // dispatcher overrides xwing-data
            if (dispatcherData != null)
            {
                for (UpgradeData dispatcherUpgrade : dispatcherData)
                {
                    UpgradeData xwingDataUpgrade = loadedData.get(dispatcherUpgrade.getXws());

                    // If there is no dispatcher version of this upgrade, store the xwing-data version
                    if(dispatcherUpgrade == null)
                    {
                        loadedData.put(xwingDataUpgrade.getXws(), xwingDataUpgrade);

                        // if there is no xwing-data version of this upgrade, store the dispatcher version
                    }else if(xwingDataUpgrade == null)
                    {
                        loadedData.put(dispatcherUpgrade.getXws(), dispatcherUpgrade);
                        // There are both xwing-data and dispatcher versions, so merge them, with dispatcher taking precedence
                    }else{
                        // do the merge.  Dispatcher overrides
                        UpgradeData mergedUpgrade = mergeUpgrades(xwingDataUpgrade,dispatcherUpgrade);
                        loadedData.put(mergedUpgrade.getXws(), mergedUpgrade);
                    }
                }
            }
        }

    }

    private static String mergeProperties(String baseString, String overrideString)
    {
        return overrideString == null ? baseString : overrideString;
    }

    private static Integer mergeProperties(Integer baseInt, Integer overrideInt)
    {
        return overrideInt == null ? baseInt : overrideInt;
    }

    private static ArrayList mergeProperties(ArrayList baseList, ArrayList overrideList)
    {
        return overrideList.size() == 0 ? baseList : overrideList;
    }

    private static List mergeProperties(List baseList, List overrideList)
    {
        return overrideList.size() == 0 ? baseList : overrideList;
    }

    private static UpgradeData mergeUpgrades(UpgradeData baseUpgrade, UpgradeData overrideUpgrade)
    {
        UpgradeData mergedUpgrade = new UpgradeData();
        mergedUpgrade.setXws(baseUpgrade.getXws());
        mergedUpgrade.setId(baseUpgrade.getId());

        mergedUpgrade.setName(mergeProperties(baseUpgrade.getName(),overrideUpgrade.getName()));
        mergedUpgrade.setPoints(mergeProperties(baseUpgrade.getPointsInteger(),overrideUpgrade.getPointsInteger()));
        mergedUpgrade.setGrants(mergeProperties(baseUpgrade.getGrants(),overrideUpgrade.getGrants()));
        mergedUpgrade.setConditions(mergeProperties(baseUpgrade.getConditions(),overrideUpgrade.getConditions()));
        mergedUpgrade.setSlot(mergeProperties(baseUpgrade.getSlot(),overrideUpgrade.getSlot()));
        mergedUpgrade.setDualCard(mergeProperties(baseUpgrade.getDualCard(),overrideUpgrade.getDualCard()));
        mergedUpgrade.setText(mergeProperties(baseUpgrade.getText(),overrideUpgrade.getText()));

        return mergedUpgrade;
    }
    private static void loadFromXwingData2()
    {
        MasterUpgradeData data = Util.loadRemoteJson(REMOTE_URL, MasterUpgradeData.class);
        if (data == null) {
            //  Util.logToChat("Unable to load xwing-data for upgrades from the web, falling back to local copy");
            data = Util.loadClasspathJson("upgrades2.json", MasterUpgradeData.class);
        }

        loadedData = Maps.newHashMap();

        for(UpgradeData upgrade : data) {
            if(loadedData.get(upgrade.getXws()) == null)
            {
                loadedData.put(upgrade.getXws(), upgrade);
            }else{
                MasterUpgradeData.UpgradeData oldUpgrade = loadedData.get(upgrade.getXws());
                oldUpgrade.setText2("// " + upgrade.getName() + ": " + upgrade.getText());
                loadedData.put(upgrade.getXws(), oldUpgrade);
            }
        }
    }
    private static void loadFromXwingData()
    {
        MasterUpgradeData data = Util.loadRemoteJson(REMOTE_URL, MasterUpgradeData.class);
        if (data == null) {
            //  Util.logToChat("Unable to load xwing-data for upgrades from the web, falling back to local copy");
            data = Util.loadClasspathJson("upgrades.json", MasterUpgradeData.class);
        }

        loadedData = Maps.newHashMap();

        for(UpgradeData upgrade : data) {
            if(loadedData.get(upgrade.getXws()) == null)
            {
                loadedData.put(upgrade.getXws(), upgrade);
            }else{
                MasterUpgradeData.UpgradeData oldUpgrade = loadedData.get(upgrade.getXws());
                oldUpgrade.setText2("// " + upgrade.getName() + ": " + upgrade.getText());
                loadedData.put(upgrade.getXws(), oldUpgrade);
            }
        }
    }
    private static void loadFromXwingData(String altXwingDataString)
    {
        //DELETEME
        Util.logToChat("XWINGDATA loading from "+altXwingDataString+ "upgrades.json");


        MasterUpgradeData data = Util.loadRemoteJson(altXwingDataString + "upgrades.json", MasterUpgradeData.class);

        //DELETEME
        for(UpgradeData data2 : data)
        {
            Util.logToChat(data2.getXws());
        }


        if (data == null) {
            //  Util.logToChat("Unable to load xwing-data for upgrades from the web, falling back to local copy");
            data = Util.loadClasspathJson("upgrades.json", MasterUpgradeData.class);
        }

        loadedData = Maps.newHashMap();

        for(UpgradeData upgrade : data) {
            if(loadedData.get(upgrade.getXws()) == null)
            {
                loadedData.put(upgrade.getXws(), upgrade);
            }else{
                MasterUpgradeData.UpgradeData oldUpgrade = loadedData.get(upgrade.getXws());
                oldUpgrade.setText2("// " + upgrade.getName() + ": " + upgrade.getText());
                loadedData.put(upgrade.getXws(), oldUpgrade);
            }
        }
    }

    private static MasterUpgradeData loadFromDispatcher()
    {
        // load from dispatch
        MasterUpgradeData data = Util.loadRemoteJson(OTAContentsChecker.OTA_DISPATCHER_UPGRADES_JSON_URL, MasterUpgradeData.class);
        if (data == null) {
            // Util.logToChat("Unable to load dispatcher for upgrades from the web, falling back to local copy");
            data = Util.loadClasspathJson("dispatcher_upgrades.json", MasterUpgradeData.class);
            if(data == null)
            {
                logToChat("Unable to load dispatcher for upgrades from the local copy.  Error in JSON format?");
            }
        }

        return data;
    }

    private static MasterUpgradeData loadFromDispatcher(String altDispatcherString)
    {
        // DELETEME
        Util.logToChat("DISPATCHER loading from "+altDispatcherString+ "dispatcher_upgrades.json");

        // load from dispatch
        MasterUpgradeData data = Util.loadRemoteJson(altDispatcherString + "dispatcher_upgrades.json", MasterUpgradeData.class);

        //DELETEME
        if(data != null) {
            for (UpgradeData data2 : data) {
                Util.logToChat(data2.getXws());
            }
        }
        if (data == null) {
            // Util.logToChat("Unable to load dispatcher for upgrades from the web, falling back to local copy");
            data = Util.loadClasspathJson("dispatcher_upgrades.json", MasterUpgradeData.class);
            if(data == null)
            {
                logToChat("Unable to load dispatcher for upgrades from the local copy.  Error in JSON format?");
            }
        }

        return data;
    }

    public static class UpgradeData {

        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("points")
        private Integer points;

        @JsonProperty("xws")
        private String xws;

        @JsonProperty("grants")
        private ArrayList<UpgradeGrants> grants = Lists.newArrayList();

        @JsonProperty("conditions")
        private List<String> conditions = Lists.newArrayList();

        @JsonProperty("slot")
        private String slot;

        @JsonProperty("dualCard")
        private Integer dualCard;

        @JsonProperty("text")
        private String text;


        public List<String> getConditions() {
            return conditions;
        }

        private void setConditions(List<String> conditions)
        {
            this.conditions = conditions;
        }


        public ArrayList<UpgradeGrants> getGrants() {
            return grants;
        }

        private void setGrants(ArrayList<UpgradeGrants> grants)
        {
            this.grants = grants;
        }

        public Integer getId() { return id; }

        private void setId(Integer id)
        {
            this.id = id;
        }

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

        public String getText() {
            return text;
        }

        private void setText(String text)
        {
            this.text = text;
        }

        public String getSlot() {
            return slot;
        }

        private void setSlot(String slot)
        {
            this.slot = slot;
        }

        public int getPoints() {

            return points == null ? 0 : points;
        }

        public Integer getPointsInteger()
        {
            return points;
        }

        private void setPoints(Integer points)
        {
            this.points = points;
        }

        public Integer getDualCard()
        {
            return dualCard;
        }

        private void setDualCard(Integer dualCard)
        {
            this.dualCard = dualCard;
        }

        public void setText2(String text) {
            this.text += text;
        }
    }

    public static class UpgradeGrants {

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private String type;

        @JsonProperty("value")
        private int value;

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public int getValue() {
            return value;
        }

        public boolean isStatsModifier() {
            return type.equals("stats");
        }
    };
}