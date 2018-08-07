package mic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XWS2Pilots {
    private static String remoteUrl = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/master/ships.json";

    @JsonProperty("name")
    private String name;

    @JsonProperty("dial")
    private List<String> dial = Lists.newArrayList();

    @JsonProperty("size")
    private String size;

    @JsonProperty("faction")
    private String faction;

    @JsonProperty("stats")
    private List<Stat2e> stats = Lists.newArrayList();

    @JsonProperty("pilots")
    private List<Pilot2e> pilots = Lists.newArrayList();

    public String getName() {return this.name;}
    public List<String> getDial() {return this.dial;}
    public String getSize() { return this.size;}
    public String getFaction() {return this.faction;}
    public List<Stat2e> getStats() {return this.stats;}
    public List<Pilot2e> getPilots() { return this.pilots;}

    public int getHull(){
        for(Stat2e stat : stats)
        {
            if(stat.getType().equals("hull")) return stat.getValue();
        }
        return 0;
    }
    public int getAgility(){
        for(Stat2e stat : stats)
        {
            if(stat.getType().equals("agility")) return stat.getValue();
        }
        return 0;
    }
    public int getShields(){
        for(Stat2e stat : stats)
        {
            if(stat.getType().equals("shields")) return stat.getValue();
        }
        return 0;
    }
    public int getCharge(){
        for(Stat2e stat : stats)
        {
            if(stat.getType().equals("charge")) return stat.getValue();
        }
        return 0;
    }
    public int getForce(){
        for(Stat2e stat : stats)
        {
            if(stat.getType().equals("force")) return stat.getValue();
        }
        return 0;
    }
    public int getFrontArc(){
        for(Stat2e stat : stats)
        {
            if(stat.getArc().equals("Front Arc") && stat.getType().equals("attack")) return stat.getValue();
        }
        return 0;
    }
    public int getRearArc(){
        for(Stat2e stat : stats)
        {
            if(stat.getArc().equals("Rear Arc") && stat.getType().equals("attack")) return stat.getValue();
        }
        return 0;
    }
    public boolean hasSmallBase() {
        if(size.equals("small")) return true;
        return false;
    }

    public boolean containsPilot(String pilotName){
        for(Pilot2e pilot : getPilots())
        {
            Util.logToChat("scanning pilot " + pilot.getName());
            if(pilot.getName().equals(pilotName)) return true;
        }
        return false;
    }

    public boolean containsCleanedPilot(String cleanedPilotName){
        for(Pilot2e pilot : getPilots())
        {
            Util.logToChat("scanning pilot " + pilot.getName());
            if(Canonicalizer.getCleanedName(pilot.getName()).equals(cleanedPilotName)) return true;
        }
        return false;
    }

    public static class Stat2e{
        public Stat2e() { super(); }

        public Stat2e(String type, String arc, int value){
            this.type = type;
            this.arc = arc;
            this.value = value;
        }

        public Stat2e(String type, int value){
            this.type = type;
            this.value = value;
        }

        @JsonProperty("type")
        private String type;

        @JsonProperty("arc")
        private String arc = "";

        @JsonProperty("value")
        private int value;

        public String getType(){
            return this.type;
        }

        public String getArc(){
            return this.arc;
        }

        public int getValue(){
            return this.value;
        }


    }

    public static class Pilot2e{
        public Pilot2e() { super(); }

        public Pilot2e(String name, String caption, int initiative, int limited, String ability){
            this.name = name;
            this.caption = caption;
            this.initiative = initiative;
            this.limited = limited;
            this.ability = ability;
        }

        @JsonProperty("name")
        private String name;

        @JsonProperty("caption")
        private String caption;

        @JsonProperty("initiative")
        private int initiative;

        @JsonProperty("limited")
        private int limited;

        @JsonProperty("ability")
        private String ability;

        @JsonProperty("xws2")
        private String xws2;

        @JsonProperty("conditions")
        private List<String> conditions = Lists.newArrayList();

        @JsonProperty("actions")
        private List<String> actions = Lists.newArrayList();

        @JsonProperty("shipability")
        private ShipAbility shipAbility = new ShipAbility();

        public String getName(){return this.name;}
        public String getCaption() {return this.caption;}
        public int getInitiative() { return this.initiative;}
        public int getLimited() { return this.limited;}
        public String getAbility() { return this.ability; }
        public String getXWS2() { return this.xws2; }
        public List<String> getConditions() { return this.conditions; }
        public List<String> getActions() { return this.actions; }
        public ShipAbility getShipAbility() { return this.shipAbility; }

        public boolean isUnique() { if(limited>0) return true;
        return false;}
    }

    public static class ShipAbility{
        public ShipAbility() {super(); }
        public ShipAbility(String name, String text){
            this.name = name;
            this.text = text;
        }

        @JsonProperty("name")
        private String name;

        @JsonProperty("text")
        private String text;

        public String getName(){ return this.name;}
        public String getText(){ return this.text; }
    }

    public static class pilotsDataSources{
        public pilotsDataSources() { super(); }
        public pilotsDataSources(List<oneShipDataSource> ships){
            this.ships = ships;
        }
        @JsonProperty("ships")
        List<oneShipDataSource> ships = Lists.newArrayList();

        public List<oneShipDataSource> getShips(){return this.ships;}
    }

    public static class oneShipDataSource{
        public oneShipDataSource() { super();}
        public oneShipDataSource(String name, String url){
            this.name = name;
            this.url = url;
        }
        @JsonProperty("name")
        private String name;

        @JsonProperty("url")
        private String url;

        public String getName(){return this.name;}
        public String getURL(){return this.url;}
    }
    public static List<XWS2Pilots> loadFromRemote() {
        pilotsDataSources whereToGetPilots = Util.loadRemoteJson(remoteUrl, pilotsDataSources.class);

        List<XWS2Pilots> allPilots = Lists.newArrayList();
        for(oneShipDataSource oSDS : whereToGetPilots.getShips()){
            try {
                allPilots.add(Util.loadRemoteJson(oSDS.getURL(), XWS2Pilots.class));
            }catch (Exception e){
                Util.logToChat(e.getMessage() + " on ship " + oSDS.getName());
                continue;
            }
        }
        return allPilots;
    }

    public static XWS2Pilots getSpecificShip(String searchedXWS2Name, List<XWS2Pilots> allShips){
        for(XWS2Pilots aShip : allShips)
        {
            for(XWS2Pilots.Pilot2e aPilot : aShip.getPilots())
            {
             String foundXWS2="";
                try{

                Util.logToChat("xws2=" + aPilot.getXWS2());
                    foundXWS2 =  aPilot.getXWS2();
            }catch(Exception e){}

                if(foundXWS2.equals(Canonicalizer.getCleanedName(searchedXWS2Name))) return aShip;
            }
        }
        return null;
    }

    public static XWS2Pilots.Pilot2e getSpecificPilot(String searchedXWS2Name, List<XWS2Pilots> allShips){
        for(XWS2Pilots aShip : allShips)
        {
            for(XWS2Pilots.Pilot2e aPilot : aShip.getPilots())
            {
                if(aPilot.getXWS2().equals(Canonicalizer.getCleanedName(searchedXWS2Name))) return aPilot;
            }
        }
        return null;
    }
}
