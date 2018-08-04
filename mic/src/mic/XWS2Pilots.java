package mic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XWS2Pilots {
    //private static String remoteUrl = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/master/ships.json";
    private static String remoteUrl = "https://gist.githubusercontent.com/guidokessels/0751793c9635eb67dcc2cad897e14b4b/raw/6ec0489ca527c49258297ead1945604bf3d3d046/xwing-data2-manifest.json";
    private static String rootURL = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/";

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

        public String getName(){return this.name;}
        public String getCaption() {return this.caption;}
        public int getInitiative() { return this.initiative;}
        public int getLimited() { return this.limited;}
        public String getAbility() { return this.ability; }

    }

    public static class manifestSource{
        public manifestSource() { super(); }
        public manifestSource(List<oneFactionDataSource> oneFaction){
            this.pilots = oneFaction;
        }
        @JsonProperty("pilots")
        List<oneFactionDataSource> pilots = Lists.newArrayList();

        public List<oneFactionDataSource> getOneFaction(){return this.pilots;}
    }

    public static class oneFactionDataSource{
        public oneFactionDataSource() { super(); }
        public oneFactionDataSource(List<oneShipDataSource> ships) {this.ships = ships;}
        @JsonProperty("")
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


}

