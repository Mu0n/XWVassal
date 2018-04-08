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



    protected static void loadData()
    {

        if(loadedData == null) {

            // load data from xwing-data
            loadFromXwingData();

            // load data from dispatcher file
            MasterPilotData dispatcherData = loadFromDispatcher();

            // add in any pilots from dispatcher that aren't in xwing-data
            if (dispatcherData != null)
            {
                for (PilotData pilot : dispatcherData)
                {
                    String xwsShip = Canonicalizer.getCanonicalShipName(pilot.getShip());

                    //MrMurphM - need to add in faction or pilots like Boba Fett will not work properly
                    String xwsFaction = Canonicalizer.getCanonicalFactionName(pilot.getFaction());
                    String convFaction = factionConversion.get(xwsFaction);


                    String pilotKey = convFaction+"/"+xwsShip+"/"+pilot.getXws();

                    if(loadedData.get(pilotKey) == null)
                    {


                        loadedData.put(pilotKey, pilot);
                    }

                  //  if (loadedData.get(xwsShip + "/" + pilot.getXws()) == null) {
                  //      loadedData.put(xwsShip + "/" + pilot.getXws(), pilot);
                  //  }
                }
            }
        }

    }


    public Object[] getAllPilots()
    {
        if(loadedData == null)
        {
            loadData();
        }
        return loadedData.values().toArray();
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

    public static class PilotData {

        @JsonProperty("skill")
        private String skill = "0";

        @JsonProperty("ship")
        private String ship;

        @JsonProperty("name")
        private String name;

        @JsonProperty("points")
        private String points = "0";

        @JsonProperty("faction")
        private String faction;

        @JsonProperty("xws")
        private String xws;

        @JsonProperty("unique")
        private boolean unique;

        @JsonProperty("conditions")
        private List<String> conditions = Lists.newArrayList();

        @JsonProperty("slots")
        private List<String> slots = Lists.newArrayList();

        @JsonProperty("ship_override")
        private ShipOverrides shipOverrides;

        public ShipOverrides getShipOverrides() {
            return shipOverrides;
        }

        public boolean isUnique() {
            return unique;
        }

        public int getSkill() {
            try {
                return Integer.parseInt(skill);
            } catch (Exception e) {
                return 0;
            }
        }

        @JsonIgnore
        public void setSkill(Integer skill)
        {
            this.skill = skill.toString();
        }
        
        public int getPoints() {
            try {
                return Integer.parseInt(points);
            } catch (Exception e) {
                return 0;
            }
        }

        public String getFaction(){
            return faction;
        }

        public String getXws() {
            return xws;
        }

        public String getShip() {
            return ship;
        }

        public String getName() {
            return name;
        }

        public List<String> getConditions() {
            return conditions;
        }
        public List<String> getSlots() {
            return slots;
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
