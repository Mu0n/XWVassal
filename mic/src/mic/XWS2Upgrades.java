package mic;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mic on 2018-07-31.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class XWS2Upgrades {

    //private static String remoteUrl = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/data/manifest.json";
    public static String remoteUrl = "https://raw.githubusercontent.com/Mu0nHub/xwing-data2/master/data/manifest.json";
    //private static String guidoRootUrl = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/";
   // private static String guidoRootUrl = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/";
    public static String guidoRootUrl = "https://raw.githubusercontent.com/Mu0nHub/xwing-data2/master/";

    @JsonUnwrapped
    private List<OneUpgrade> upgrades = Lists.newArrayList();

    public XWS2Upgrades() {}

    public List<OneUpgrade> getUpgrades(){return upgrades;}
    public void add(OneUpgrade oneUp){ upgrades.add(oneUp);}
    public int getCount() { return this.upgrades.size(); }




    public static class Conditions {
        public Conditions() { super(); }
        private List<Condition> conditions = Lists.newArrayList();

        public List<Condition> getConditions() { return conditions; }

    }

    public static class Condition {
        public Condition() { super(); }
        public Condition(String name, String xws, String ability){
            this.name = name;
            this.xws = xws;
            this.ability = ability;
        }

        @JsonProperty("name")
        private String name;

        @JsonProperty("ability")
        private String ability;

        @JsonProperty("xws")
        private String xws;

        public String getName() { return this.name; }
        public String getAbility() { return this.ability; }
        public String getXws() { return this.xws; }
    }
//more like upgrade type

    public static class OneUpgrade {
        public OneUpgrade() { super(); }
        public OneUpgrade(String name, int limited, List<side> sides, String xws){
            this.name = name;
            this.limited = limited;
            this.sides = sides;
            this.xws = xws;
        }

        @JsonProperty("name")
        private String name;

        @JsonProperty("limited")
        private int limited;

        @JsonProperty("sides")
        List<side> sides = Lists.newArrayList();

        @JsonProperty("xws")
        private String xws;


        public String getName() { return this.name;}
        public int getLimited() { return this.limited; }
        public List<side> getSides() { return this.sides; }
        public String getXws() { return this.xws; }
    }

    public static XWS2Upgrades.Condition getSpecificConditionByXWS(String upXWS, List<XWS2Upgrades.Condition> allConditions){
        for(XWS2Upgrades.Condition oneCond : allConditions)
        {
            if(oneCond.getXws().equals(upXWS)) return oneCond;
        }
        return null;
    }

    public static class side {
        public side() { super(); }
        public side(String title, String type, String ability){
            this.title = title;
            this.type = type;
            this.ability = ability;
        }

        @JsonProperty("title")
        private String title;

        @JsonProperty("type")
        private String type;

        @JsonProperty("ability")
        private String ability;

        @JsonProperty("conditions")
        private List<String> conditions = Lists.newArrayList();

        @JsonProperty("slots")
        private List<String> slots = Lists.newArrayList();

        @JsonProperty("attack")
        private Attack attack;

        @JsonProperty("actions")
        private List<Action> actions = Lists.newArrayList();

        @JsonProperty("charges")
        private Charge charges;

        public String getTitle() { return this.title; }
        public String getType() { return this.type; }
        public String getAbility() { return this.ability; }
        public List<String> getSlots(){ return slots; }
        public List<String> getConditions() { return this.conditions; }
        public Attack getAttack() { return attack; }
        public List<Action> getActions() { return actions; }
        public Charge getCharges() { return charges; }
    }

    public static class Charge {
        public Charge() { super(); }

        @JsonProperty("value")
        private int value;

        @JsonProperty("recovers")
        private int recovers;

        public int getValue() { return value; }
        public int getRecovers() { return recovers; }

    }

    public static class Action{
        public Action() { super(); }

        @JsonProperty("type")
        private String type;

        @JsonProperty("difficulty")
        private String difficulty;

        public String getType() { return type; }
        public String getDifficulty() { return difficulty; }
    }
    public static class Attack{
        public Attack() { super(); }
        public Attack(String arc, int value, int minRange, int maxRange){
            this.arc = arc;
            this.value = value;
            this.minRange = minRange;
            this.maxRange = maxRange;
        }

        @JsonProperty("arc")
        private String arc;

        @JsonProperty("value")
        private int value;

        @JsonProperty("minRange")
        private int minRange;

        @JsonProperty("maxRange")
        private int maxRange;

        public String getArc() { return arc; }
        public int getValue() { return value; }
        public int getMinRange() { return minRange; }
        public int getMaxRange() { return maxRange; }

    }
    public static class upgradesDataSources{
        public upgradesDataSources() { super(); }
        public upgradesDataSources(List<String> urlEnd){
            this.urlEnds = urlEnd;
        }
        @JsonProperty("upgrades")
        List<String> urlEnds = Lists.newArrayList();

        public List<String> getUrlEnds(){return this.urlEnds;}
    }

    public static class conditionsDataSources{
        public conditionsDataSources() { super(); }
        public conditionsDataSources(String urlEnd){
            this.urlEnd = urlEnd;
        }
        @JsonProperty("conditions")
        String urlEnd;

        public String getUrlEnd(){return this.urlEnd;}
    }

    public static List<XWS2Upgrades.Condition> loadConditionsFromRemote() {
        conditionsDataSources whereToGetConditions = new conditionsDataSources();
        try{
            whereToGetConditions = Util.loadRemoteJson(remoteUrl, conditionsDataSources.class);
        }catch(Exception e){}
        List<XWS2Upgrades.Condition> conditionsRead = Lists.newArrayList();
        try{
            conditionsRead = XWS2Upgrades.loadRemoteJsonArrayOfConditions(new URL(guidoRootUrl+whereToGetConditions.getUrlEnd()));
        }
        catch(Exception e){}
        return conditionsRead;
    }



    public static XWS2Upgrades loadFromRemote() {

        upgradesDataSources whereToGetUpgrades = new upgradesDataSources();
        try{
            whereToGetUpgrades = Util.loadRemoteJson(remoteUrl, upgradesDataSources.class);
        }catch(Exception e){

        }

        XWS2Upgrades allUpgrades = new XWS2Upgrades();
        for(String urlEnd : whereToGetUpgrades.getUrlEnds()){

            List<XWS2Upgrades.OneUpgrade> upgradesListRead = Lists.newArrayList();
            try {
                upgradesListRead = XWS2Upgrades.loadRemoteJsonArrayOfOneUpgrades(new URL(guidoRootUrl+urlEnd));
                //XWS2Upgrades upgradesListRead = Util.loadRemoteJson(new URL(guidoRootUrl+urlEnd), XWS2Upgrades.class);
            }catch (Exception e){
                continue;
            }

            try{
                for(XWS2Upgrades.OneUpgrade oneUp : upgradesListRead){
                    allUpgrades.add(oneUp);
                }

            }catch (Exception e){
                continue;
            }
        }
        return allUpgrades;
    }

    private static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public static List<XWS2Upgrades.OneUpgrade> loadRemoteJsonArrayOfOneUpgrades(URL url) {
        try {
            InputStream inputStream = new BufferedInputStream(url.openStream());
            List<XWS2Upgrades.OneUpgrade> rawData = mapper.readValue(inputStream,  mapper.getTypeFactory().constructCollectionType(List.class, XWS2Upgrades.OneUpgrade.class));
            return rawData;
        } catch (Exception e) {
            System.out.println("Unhandled error parsing remote json: \n" + e.toString());
            return null;
        }
    }

    private static List<XWS2Upgrades.Condition> loadRemoteJsonArrayOfConditions(URL url) {
        try {
            InputStream inputStream = new BufferedInputStream(url.openStream());
            List<XWS2Upgrades.Condition> rawData = mapper.readValue(inputStream,  mapper.getTypeFactory().constructCollectionType(List.class, XWS2Upgrades.Condition.class));
            return rawData;
        } catch (Exception e) {
            System.out.println("Unhandled error parsing remote json: \n" + e.toString());
            return null;
        }
    }


    public static XWS2Upgrades.OneUpgrade getSpecificUpgrade(String searchedXWS2Name, XWS2Upgrades allUpgrades){
        for(XWS2Upgrades.OneUpgrade anUp : allUpgrades.upgrades)
        {

           String theXWS2String = anUp.getXws();
           if(theXWS2String.equals(searchedXWS2Name)) return anUp;

        }
        return null;
    }


}
