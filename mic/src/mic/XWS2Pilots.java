package mic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XWS2Pilots {
    private static String remoteUrl = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/data/pilots/galactic-empire/alpha-class-star-wing.json";

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

    public static XWS2Pilots loadFromRemote() {
        return Util.loadRemoteJson(remoteUrl, XWS2Pilots.class);
    }


}

