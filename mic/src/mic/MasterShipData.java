package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mic.ota.OTAContentsChecker;

import java.util.*;

/**
 * Created by amatheny on 2/11/17.
 */
public class MasterShipData extends ArrayList<MasterShipData.ShipData> {

    public static String REMOTE_URL = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/ships.js";
  //  public static String DISPATCHER_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/dispatcher_ships.json";
    public static Map<String, ShipData> loadedData = null;

    public Object[] getAllShips()
    {
        if(loadedData == null)
        {
            loadData();
        }
        return loadedData.values().toArray();
    }

    public static ShipData getShipDataForShipName(String shipName)
    {
        if (loadedData == null) {
            loadData();
        }
        Collection data = loadedData.values();
        Iterator i = data.iterator();
        boolean found = false;
        ShipData sd = null;
        while(i.hasNext() && !found)
        {
            sd = (ShipData)i.next();
            if(sd.getName().equalsIgnoreCase(shipName))
            {
                found = true;
            }
        }
        return sd;
    }

    public static ShipData getShipData(String shipXwsId) {
        if (loadedData == null) {
            loadData();
        }
        return loadedData.get(shipXwsId);
    }

    protected static void loadData2() {
// load data from xwing-data
        loadFromXwingData2();
    }

        protected static void loadData() {

        // load data from xwing-data
        loadFromXwingData();

        // load data from dispatcher file
        MasterShipData dispatcherData = loadFromDispatcher();

        // dispatcher overrides xwing-data
        if(dispatcherData != null)
        {
            for (ShipData dispatcherShip : dispatcherData)
            {
                ShipData xwingDataShip = loadedData.get(dispatcherShip.getXws());
                // If there is no dispatcher version of this ship, store the xwing-data version
                if(dispatcherShip == null)
                {
                    loadedData.put(xwingDataShip.getXws(), xwingDataShip);

                // if there is no xwing-data version of this ship, store the dispatcher version
                }else if(xwingDataShip == null)
                {
                    loadedData.put(dispatcherShip.getXws(), dispatcherShip);
                // There are both xwing-data and dispatcher versions, so merge them, with dispatcher taking precedence
                }else{
                    // do the merge.  Dispatcher overrides
                    ShipData mergedShip = mergeShips(xwingDataShip,dispatcherShip);
                    loadedData.put(mergedShip.getXws(), mergedShip);
                }
            }
        }
    }

    protected static void loadData(Boolean wantFullControl, String altDispatcherString) {

        // load data from xwing-data
        if(wantFullControl==false) loadFromXwingData();

        // load data from dispatcher file
        MasterShipData dispatcherData = loadFromDispatcher(altDispatcherString);

        // dispatcher overrides xwing-data
        if(dispatcherData != null)
        {
            for (ShipData dispatcherShip : dispatcherData)
            {
                ShipData xwingDataShip = loadedData.get(dispatcherShip.getXws());
                // If there is no dispatcher version of this ship, store the xwing-data version
                if(dispatcherShip == null)
                {
                    loadedData.put(xwingDataShip.getXws(), xwingDataShip);

                    // if there is no xwing-data version of this ship, store the dispatcher version
                }else if(xwingDataShip == null)
                {
                    loadedData.put(dispatcherShip.getXws(), dispatcherShip);
                    // There are both xwing-data and dispatcher versions, so merge them, with dispatcher taking precedence
                }else{
                    // do the merge.  Dispatcher overrides
                    ShipData mergedShip = mergeShips(xwingDataShip,dispatcherShip);
                    loadedData.put(mergedShip.getXws(), mergedShip);
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

    private static List mergeProperties(List baseList, List overrideList)
    {
        return overrideList.size() == 0 ? baseList : overrideList;
    }

    private static Boolean mergeProperties(Boolean baseBool, Boolean overrideBool)
    {
        return overrideBool == null ? baseBool : overrideBool;
    }

    private static ShipData mergeShips(ShipData baseShip, ShipData overrideShip)
    {
        ShipData mergedShip = new ShipData();
        mergedShip.setXws(baseShip.getXws());

        mergedShip.setName(mergeProperties(baseShip.getName(),overrideShip.getName()));
        mergedShip.setAttack(mergeProperties(baseShip.getAttack(),overrideShip.getAttack()));
        mergedShip.setAgility(mergeProperties(baseShip.getAgility(),overrideShip.getAgility()));
        mergedShip.setHull(mergeProperties(baseShip.getHull(),overrideShip.getHull()));
        mergedShip.setShields(mergeProperties(baseShip.getShields(),overrideShip.getShields()));
        mergedShip.setEnergy(mergeProperties(baseShip.getEnergy(),overrideShip.getEnergy()));
        mergedShip.setActions(mergeProperties(baseShip.getActions(),overrideShip.getActions()));
        mergedShip.setFactions(mergeProperties(baseShip.getFactions(),overrideShip.getFactions()));
        mergedShip.setDialManeuvers(mergeProperties(baseShip.getDialManeuvers(),overrideShip.getDialManeuvers()));
        mergedShip.setManeuvers(mergeProperties(baseShip.getManeuvers(),overrideShip.getManeuvers()));
        mergedShip.setFiringArcs(mergeProperties(baseShip.getFiringArcs(),overrideShip.getFiringArcs()));
        mergedShip.setSize(mergeProperties(baseShip.getSize(),overrideShip.getSize()));
        mergedShip.setDualBase(mergeProperties(baseShip.hasDualBase(),overrideShip.hasDualBase()));
        mergedShip.setDualBaseToggleMenuText(mergeProperties(baseShip.getDualBaseToggleMenuText(),overrideShip.getDualBaseToggleMenuText()));
        mergedShip.setBaseImage1(mergeProperties(baseShip.getBaseImage1Identifier(),overrideShip.getBaseImage1Identifier()));
        mergedShip.setBaseImage2(mergeProperties(baseShip.getBaseImage2Identifier(),overrideShip.getBaseImage2Identifier()));
        mergedShip.setBaseReport1Identifier(mergeProperties(baseShip.getBaseReport1Identifier(),overrideShip.getBaseReport1Identifier()));
        mergedShip.setBaseReport2Identifier(mergeProperties(baseShip.getBaseReport2Identifier(),overrideShip.getBaseReport2Identifier()));
        return mergedShip;
    }
    private static void loadFromXwingData2() {
        // load from xwing-data
        MasterShipData data = Util.loadRemoteJson(REMOTE_URL, MasterShipData.class);
        if (data == null) {
            // Util.logToChat("Unable to load xwing-data for ships from the web, falling back to local copy");
            data = Util.loadClasspathJson("ships2.json", MasterShipData.class);
        }

        loadedData = Maps.newHashMap();
        for(ShipData ship : data) {
            loadedData.put(ship.getXws(), ship);
        }
    }

    private static void loadFromXwingData()
    {
        // load from xwing-data
        MasterShipData data = Util.loadRemoteJson(REMOTE_URL, MasterShipData.class);
        if (data == null) {
           // Util.logToChat("Unable to load xwing-data for ships from the web, falling back to local copy");
            data = Util.loadClasspathJson("ships.json", MasterShipData.class);
        }

        loadedData = Maps.newHashMap();
        for(ShipData ship : data) {
            loadedData.put(ship.getXws(), ship);
        }
    }
    private static void loadFromXwingData(String altXwingDataURL)
    {
        // load from xwing-data
        MasterShipData data = Util.loadRemoteJson(altXwingDataURL + "ships.json", MasterShipData.class);
        if (data == null) {
            // Util.logToChat("Unable to load xwing-data for ships from the web, falling back to local copy");
            data = Util.loadClasspathJson("ships.json", MasterShipData.class);
        }

        loadedData = Maps.newHashMap();
        for(ShipData ship : data) {
            loadedData.put(ship.getXws(), ship);
        }
    }

    private static MasterShipData loadFromDispatcher()
    {
        // load from dispatch
        MasterShipData data = Util.loadRemoteJson(OTAContentsChecker.OTA_DISPATCHER_SHIPS_JSON_URL, MasterShipData.class);
        if (data == null) {
           // Util.logToChat("Unable to load dispatcher for ships from the web, falling back to local copy");
            data = Util.loadClasspathJson("dispatcher_ships.json", MasterShipData.class);
            if(data == null)
            {
                Util.logToChat("Unable to load dispatcher for ships from the local copy.  Error in JSON format?");
            }
        }

        return data;
    }

    private static MasterShipData loadFromDispatcher(String altDispatcherString)
    {
        // load from dispatch
        MasterShipData data = Util.loadRemoteJson(altDispatcherString + "dispatcher_ships.json", MasterShipData.class);
        if (data == null) {
            // Util.logToChat("Unable to load dispatcher for ships from the web, falling back to local copy");
            data = Util.loadClasspathJson("dispatcher_ships.json", MasterShipData.class);
            if(data == null)
            {
                Util.logToChat("Unable to load dispatcher for ships from the local copy.  Error in JSON format?");
            }
        }

        return data;
    }
    public static class ShipData {

        @JsonProperty("name")
        private String name;

        @JsonProperty("attack")
        private Integer attack;

        @JsonProperty("agility")
        private Integer agility;

        @JsonProperty("hull")
        private Integer hull;

        @JsonProperty("shields")
        private Integer shields;

        @JsonProperty("energy")
        private Integer energy;

        @JsonProperty("xws")
        private String xws;

        @JsonProperty("actions")
        private List<String> actions = Lists.newArrayList();

        @JsonProperty("faction")
        private List<String> factions = Lists.newArrayList();

        @JsonProperty("dial")
        private List<String> dialManeuvers = Lists.newArrayList();

        @JsonProperty("maneuvers")
        private List<List<Integer>> maneuvers = Lists.newArrayList();

        @JsonProperty("firing_arcs")
        private List<String> firingArcs = Lists.newArrayList();

        @JsonProperty("size")
        private String size;

        @JsonProperty("has_dual_base")
        private Boolean hasDualBase;

        @JsonProperty("dual_base_toggle_menu_text")
        private String dualBaseToggleMenuText;


        @JsonProperty("dual_base_image_1_identifier")
        private String baseImage1Identifier;

        @JsonProperty("dual_base_image_2_identifier")
        private String baseImage2Identifier;

        @JsonProperty("dual_base_report_1_identifier")
        private String baseReport1Identifier;

        @JsonProperty("dual_base_report_2_identifier")
        private String baseReport2Identifier;

        public String getBaseImage1Identifier()
        {
            return baseImage1Identifier;
        }

        public String getBaseImage2Identifier()
        {
            return baseImage2Identifier;
        }

        public void setBaseImage1(String baseImage1Identifier)
        {
            this.baseImage1Identifier = baseImage1Identifier;
        }

        public void setBaseImage2(String baseImage2Identifier)
        {
            this.baseImage2Identifier = baseImage2Identifier;
        }

        public String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        public Integer getAttack() {
            return attack;
        }

        private void setAttack(Integer attack)
        {
            this.attack = attack;
        }

        public Integer getAgility() {
            return agility;
        }

        private void setAgility(Integer agility)
        {
            this.agility = agility;
        }

        public Integer getHull() {
            return hull;
        }

        private void setHull(Integer hull)
        {
            this.hull = hull;
        }

        public Integer getShields() {
            return shields;
        }

        private void setShields(Integer shields)
        {
            this.shields = shields;
        }

        public int getEnergy() {
            return energy == null ? 0 : energy.intValue();

        }

        public Integer getEntergyInt()
        {
            return energy;
        }

        private void setEnergy(Integer energy)
        {
            this.energy = energy;
        }

        public String getXws() {
            return xws;
        }

        private void setXws(String xws)
        {
            this.xws = xws;
        }

        public String getSize()
        {
            return size;
        }

        private void setSize(String size)
        {
            this.size = size;
        }

        public List<String> getActions() {
            return this.actions;
        }

        private void setActions(List<String> actions)
        {
            this.actions = actions;
        }

        public List<String> getFactions() {
            return this.factions;
        }

        private void setFactions(List<String> factions)
        {
            this.factions = factions;
        }

        public List<String> getFiringArcs() {
            return this.firingArcs;
        }

        private void setFiringArcs(List<String> firingArcs)
        {
            this.firingArcs = firingArcs;
        }

        public List<String> getDialManeuvers()
        {

            return this.dialManeuvers;
        }

        private void setDialManeuvers(List<String> dialManeuvers)
        {
            this.dialManeuvers = dialManeuvers;
        }
        public List<List<Integer>> getManeuvers() { return maneuvers; }

        private void setManeuvers(List<List<Integer>> maneuvers)
        {
            this.maneuvers = maneuvers;
        }

        public boolean hasSmallBase() {
            return "small".equals(this.size);
        }

        public boolean hasLargeBase() {
            return "large".equals(this.size);
        }

        public boolean hasHugeBase() {
            return "huge".equals(this.size);
        }

        public Boolean hasDualBase()
        {
            return this.hasDualBase;
        }

        private void setDualBase(Boolean hasDualBase)
        {
            this.hasDualBase = hasDualBase;
        }

        public String getDualBaseToggleMenuText()
        {
            return this.dualBaseToggleMenuText;
        }

        private void setDualBaseToggleMenuText(String dualBaseToggleMenuText)
        {
            this.dualBaseToggleMenuText = dualBaseToggleMenuText;
        }

        public String getBaseReport1Identifier()
        {
            return baseReport1Identifier;
        }

        public void setBaseReport1Identifier(String baseReport1Identifier)
        {
            this.baseReport1Identifier = baseReport1Identifier;
        }

        public String getBaseReport2Identifier()
        {
            return baseReport2Identifier;
        }

        public void setBaseReport2Identifier(String baseReport2Identifier)
        {
            this.baseReport2Identifier = baseReport2Identifier;
        }

    }
}
