package mic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mic.ota.OTAContentsChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by amatheny on 2/11/17.
 */
public class MasterPilotData extends ArrayList<MasterPilotData.PilotData> {


    private static Map<String, String> factionConversion = ImmutableMap.<String, String>builder()
            .put("rebelalliance","rebel")
            .put("resistance","rebel")
            .put("galacticempire","imperial")
            .put("firstorder","imperial")
            .put("scumandvillainy","scum")
            .build();

    public static String REMOTE_URL = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/pilots.js";
    public static String REMOTE_URL_DIR = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/";
 //   public static String DISPATCHER_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/dispatcher_pilots.json";

    private static Map<String, PilotData> loadedData = null;

    public static PilotData getPilotData(String ship, String pilot, String faction) {
        if (loadedData == null) {
            loadData();
        }

        String xwsShip = Canonicalizer.getCanonicalShipName(ship);

        //MrMurphM - need to add in faction or pilots like Boba Fett will not work properly
        //String xwsFaction = Canonicalizer.getCanonicalFactionName(faction);
    //    String convFaction = factionConversion.get(xwsFaction);
     //   String pilotKey = convFaction+"/"+xwsShip+"/"+pilot;
        if(factionConversion.get(faction) != null)
        {
            faction = factionConversion.get(faction);
        }
        String pilotKey = faction+"/"+xwsShip+"/"+pilot;

        //return loadedData.get(ship + "/" + pilot);


        return loadedData.get(pilotKey);
    }


    protected static void loadData2()
    {
        loadFromXwingData2();
    }

    protected static void loadData()
    {

            // load data from xwing-data
            loadFromXwingData();

            // load data from dispatcher file
            MasterPilotData dispatcherData = loadFromDispatcher(REMOTE_URL_DIR);

            // dispatcher overrides xwing-data
            if (dispatcherData != null)
            {
                for (PilotData dispatcherPilot : dispatcherData)
                {
                    String xwsShip = Canonicalizer.getCanonicalShipName(dispatcherPilot.getShip());

                    //MrMurphM - need to add in faction or pilots like Boba Fett will not work properly
                    String xwsFaction = Canonicalizer.getCanonicalFactionName(dispatcherPilot.getFaction());
                    String convFaction = factionConversion.get(xwsFaction);


                    String pilotKey = convFaction+"/"+xwsShip+"/"+dispatcherPilot.getXws();


                    PilotData xwingDataPilot = loadedData.get(pilotKey);


                    // If there is no dispatcher version of this pilot, store the xwing-data version
                    if(dispatcherPilot == null)
                    {
                        loadedData.put(pilotKey, xwingDataPilot);

                    // if there is no xwing-data version of this pilot, store the dispatcher version
                    }else if(xwingDataPilot == null)
                    {
                        loadedData.put(pilotKey, dispatcherPilot);
                    // There are both xwing-data and dispatcher versions, so merge them, with dispatcher taking precedence
                    }else{
                        // do the merge.  Dispatcher overrides
                        PilotData mergedSPilot = mergePilots(xwingDataPilot,dispatcherPilot);
                        loadedData.put(pilotKey, mergedSPilot);
                    }

                }
            }


    }

    protected static void loadData(Boolean wantFullControl, String altDispatcherDataString)
    {            // load data from xwing-data
        if(wantFullControl == false) loadFromXwingData();

            // load data from dispatcher file
            MasterPilotData dispatcherData = loadFromDispatcher(altDispatcherDataString);

            // dispatcher overrides xwing-data
            if (dispatcherData != null)
            {
                for (PilotData dispatcherPilot : dispatcherData)
                {
                    String xwsShip = Canonicalizer.getCanonicalShipName(dispatcherPilot.getShip());

                    //MrMurphM - need to add in faction or pilots like Boba Fett will not work properly
                    String xwsFaction = Canonicalizer.getCanonicalFactionName(dispatcherPilot.getFaction());
                    String convFaction = factionConversion.get(xwsFaction);


                    String pilotKey = convFaction+"/"+xwsShip+"/"+dispatcherPilot.getXws();


                    PilotData xwingDataPilot = loadedData.get(pilotKey);


                    // If there is no dispatcher version of this pilot, store the xwing-data version
                    if(dispatcherPilot == null)
                    {
                        loadedData.put(pilotKey, xwingDataPilot);

                        // if there is no xwing-data version of this pilot, store the dispatcher version
                    }else if(xwingDataPilot == null)
                    {
                        loadedData.put(pilotKey, dispatcherPilot);
                        // There are both xwing-data and dispatcher versions, so merge them, with dispatcher taking precedence
                    }else{
                        // do the merge.  Dispatcher overrides
                        PilotData mergedSPilot = mergePilots(xwingDataPilot,dispatcherPilot);
                        loadedData.put(pilotKey, mergedSPilot);
                    }

                }
            }


    }

    private static String mergeProperties(String baseString, String overrideString)
    {
        return overrideString == null ? baseString : overrideString;
    }

    private static Boolean mergeProperties(Boolean baseBool, Boolean overrideBool)
    {
        return overrideBool == null ? baseBool : overrideBool;
    }

    private static List mergeProperties(List baseList, List overrideList)
    {
        return overrideList.size() == 0 ? baseList : overrideList;
    }

    private static ShipOverrides mergeProperties(ShipOverrides baseOverrides, ShipOverrides overrideOverrides)
    {
        return overrideOverrides == null ? baseOverrides : overrideOverrides;
    }

    private static PilotData mergePilots(PilotData basePilot, PilotData overridePilot)
    {
        PilotData mergedPilot = new PilotData();
        mergedPilot.setXws(basePilot.getXws());
        mergedPilot.setFaction(basePilot.getFaction());
        mergedPilot.setShip(basePilot.getShip());

        mergedPilot.setSkill(mergeProperties(basePilot.getSkillStr(),overridePilot.getSkillStr()));
        mergedPilot.setName(mergeProperties(basePilot.getName(),overridePilot.getName()));
        mergedPilot.setPoints(mergeProperties(basePilot.getPointsStr(),overridePilot.getPointsStr()));
        mergedPilot.setUnique(mergeProperties(basePilot.isUniqueBool(),overridePilot.isUniqueBool()));
        mergedPilot.setConditions(mergeProperties(basePilot.getConditions(),overridePilot.getConditions()));
        mergedPilot.setSlots(mergeProperties(basePilot.getSlots(),overridePilot.getSlots()));
        mergedPilot.setShipOverrides(mergeProperties(basePilot.getShipOverrides(),overridePilot.getShipOverrides()));
        mergedPilot.setText(mergeProperties(basePilot.getText(),overridePilot.getText()));

        return mergedPilot;
    }

    public Object[] getAllPilots()
    {
        if(loadedData == null)
        {
            loadData();
        }
        return loadedData.values().toArray();
    }

    private static void loadFromXwingData2()
    {
        // load from xwing-data
        MasterPilotData data = Util.loadRemoteJson(REMOTE_URL, MasterPilotData.class);
        if (data == null) {
            // Util.logToChat("Unable to load xwing-data for pilots from the web, falling back to local copy");
            data = Util.loadClasspathJson("pilots2.json", MasterPilotData.class);
        }

        loadedData = Maps.newHashMap();
        for(PilotData pilot : data) {
            String xwsShip = Canonicalizer.getCanonicalShipName(pilot.getShip());

            //MrMurphM - need to add in faction or pilots like Boba Fett will not work properly
            String xwsFaction = Canonicalizer.getCanonicalFactionName(pilot.getFaction());



            String convFaction = factionConversion.get(xwsFaction);


            String pilotKey = convFaction+"/"+xwsShip+"/"+pilot.getXws();



            loadedData.put(pilotKey,pilot);
            //loadedData.put(xwsShip + "/" + pilot.getXws(), pilot);
        }
    }
    private static void loadFromXwingData()
    {
        // load from xwing-data
        MasterPilotData data = Util.loadRemoteJson(REMOTE_URL, MasterPilotData.class);
        if (data == null) {
           // Util.logToChat("Unable to load xwing-data for pilots from the web, falling back to local copy");
            data = Util.loadClasspathJson("pilots.json", MasterPilotData.class);
        }

        loadedData = Maps.newHashMap();
        for(PilotData pilot : data) {
            String xwsShip = Canonicalizer.getCanonicalShipName(pilot.getShip());

            //MrMurphM - need to add in faction or pilots like Boba Fett will not work properly
            String xwsFaction = Canonicalizer.getCanonicalFactionName(pilot.getFaction());



            String convFaction = factionConversion.get(xwsFaction);


            String pilotKey = convFaction+"/"+xwsShip+"/"+pilot.getXws();



            loadedData.put(pilotKey,pilot);
            //loadedData.put(xwsShip + "/" + pilot.getXws(), pilot);
        }
    }

    private static void loadFromXwingData(String altXwingDataURL)
    {
        // load from xwing-data
        MasterPilotData data = Util.loadRemoteJson(altXwingDataURL + "pilots.json", MasterPilotData.class);
        if (data == null) {
            Util.logToChat("Unable to load xwing-data for pilots from the web, falling back to local copy");
            data = Util.loadClasspathJson("pilots.json", MasterPilotData.class);
        }

        loadedData = Maps.newHashMap();
        for(PilotData pilot : data) {
            String xwsShip = Canonicalizer.getCanonicalShipName(pilot.getShip());

            //MrMurphM - need to add in faction or pilots like Boba Fett will not work properly
            String xwsFaction = Canonicalizer.getCanonicalFactionName(pilot.getFaction());



            String convFaction = factionConversion.get(xwsFaction);


            String pilotKey = convFaction+"/"+xwsShip+"/"+pilot.getXws();



            loadedData.put(pilotKey,pilot);
            //loadedData.put(xwsShip + "/" + pilot.getXws(), pilot);
        }
    }

    private static MasterPilotData loadFromDispatcher()
    {
        // load from dispatch
        MasterPilotData data = Util.loadRemoteJson(OTAContentsChecker.OTA_DISPATCHER_PILOTS_JSON_URL, MasterPilotData.class);
        if (data == null) {
           // Util.logToChat("Unable to load dispatcher for ships from the web, falling back to local copy");
            data = Util.loadClasspathJson("dispatcher_pilots.json", MasterPilotData.class);
            if(data == null)
            {
                Util.logToChat("Unable to load dispatcher for pilots from the local copy.  Error in JSON format?");
            }
        }

        return data;
    }

    private static MasterPilotData loadFromDispatcher(String altDispatcherURL)
    {
        // load from dispatch
        MasterPilotData data = Util.loadRemoteJson(altDispatcherURL + "dispatcher_pilots.json", MasterPilotData.class);


        if (data == null) {
            // Util.logToChat("Unable to load dispatcher for ships from the web, falling back to local copy");
            data = Util.loadClasspathJson("dispatcher_pilots.json", MasterPilotData.class);
            if(data == null)
            {
                Util.logToChat("Unable to load dispatcher for pilots from the local copy.  Error in JSON format?");
            }
        }
        return data;
    }

    public static class PilotData {

        @JsonProperty("skill")
        private String skill;

        @JsonProperty("ship")
        private String ship;

        @JsonProperty("name")
        private String name;

        @JsonProperty("points")
        private String points;

        @JsonProperty("faction")
        private String faction;

        @JsonProperty("xws")
        private String xws;

        @JsonProperty("unique")
        private Boolean unique;

        @JsonProperty("conditions")
        private List<String> conditions = Lists.newArrayList();

        @JsonProperty("slots")
        private List<String> slots = Lists.newArrayList();

        @JsonProperty("ship_override")
        private ShipOverrides shipOverrides;

        @JsonProperty("text")
        private String text;

        public ShipOverrides getShipOverrides() {
            return shipOverrides;
        }

        private void setShipOverrides(ShipOverrides shipOverrides)
        {
            this.shipOverrides = shipOverrides;
        }

        public boolean isUnique() {
            return unique == null ? false : unique.booleanValue();
        //    return unique;
        }

        public Boolean isUniqueBool()
        {
            return unique;
        }

        private void setUnique(Boolean unique)
        {
            this.unique = unique;
        }

        private String getSkillStr()
        {
            return skill;
        }

        public int getSkill() {
            try {
                return Integer.parseInt(skill == null ? "0" : skill);
            } catch (Exception e) {
                return 0;
            }
        }

        @JsonIgnore
        public void setSkill(Integer skill)
        {
            this.skill = skill.toString();
        }

        @JsonIgnore
        private void setSkill(String skill)
        {
            this.skill = skill;
        }

        private String getPointsStr()
        {
            return points;
        }

        public int getPoints() {
            try {
                return Integer.parseInt(points == null ? "0" : points);
            } catch (Exception e) {
                return 0;
            }
        }

        private void setPoints(String points)
        {
            this.points = points;
        }

        public String getFaction(){
            return faction;
        }

        private void setFaction(String faction)
        {
            this.faction = faction;
        }

        public String getXws() {
            return xws;
        }

        private void setXws(String xws)
        {
            this.xws = xws;
        }

        public String getShip() {
            return ship;
        }

        private void setShip(String ship)
        {
            this.ship = ship;
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

        public List<String> getConditions() {
            return conditions;
        }

        private void setConditions(List<String> conditions)
        {
            this.conditions = conditions;
        }

        public List<String> getSlots() {
            return slots;
        }

        private void setSlots(List<String> slots)
        {
            this.slots = slots;
        }
    }

    public static class ShipOverrides {
        private int attack;
        private int agility;
        private int hull;
        private int shields;

        public int getAttack() {
            return attack;
        }

        public int getAgility() {
            return agility;
        }

        public int getHull() {
            return hull;
        }

        public int getShields() {
            return shields;
        }
    }
}
