package mic;

import VASSAL.tools.DataArchive;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import mic.ota.XWOTAUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Created by Mic on 2018-07-31.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class XWS2Upgrades {

    public static String remoteUrl = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/data/manifest.json";
    //public static String remoteUrl = "https://raw.githubusercontent.com/Mu0nHub/xwing-data2/master/data/manifest.json";
    //private static String guidoRootUrl = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/";
    public static String guidoRootUrl = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/";
    //public static String guidoRootUrl = "https://raw.githubusercontent.com/Mu0nHub/xwing-data2/master/";

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
            this.grants = Lists.newArrayList();
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

        @JsonProperty("grants")
        private List<grant> grants = Lists.newArrayList();

        @JsonProperty("attack")
        private Attack attack;

        @JsonProperty("actions")
        private List<Action> actions = Lists.newArrayList();

        @JsonProperty("charges")
        private Charge charges;

        @JsonProperty("force")
        private Force force;

        public String getTitle() { return this.title; }
        public String getType() { return this.type; }
        public String getAbility() { return this.ability; }
        public List<String> getSlots(){ return slots; }
        public List<grant> getGrants() { return grants; }
        public List<String> getConditions() { return this.conditions; }
        public Attack getAttack() { return attack; }
        public List<Action> getActions() { return actions; }
        public Charge getCharges() { return charges; }
        public Force getForce() { return force; }
    }

    public static class grant {
        public grant() { super(); }

        @JsonProperty("type")
        private String type;

        @JsonProperty("value")
        private JsonNode value;

        @JsonProperty("amount")
        private int amount;

        public String getType() { return type; }

        public Map<String,String> getValue() {
            Map valueMap = new HashMap<String,String>();
            if(value.isObject())
            {
                Iterator i = value.fieldNames();
                while(i.hasNext())
                {
                    String nodeKey = (String)i.next();
                    String nodeValue = value.get(nodeKey).textValue();
                    valueMap.put(nodeKey,nodeValue);

                }
            }else {
                valueMap.put("value",value.textValue());
            }

            return valueMap;
        }

        public int getAmount() { return amount; }
    }

    public static class Force {
        public Force() { super(); }

        @JsonProperty("value")
        private int value = 0;

        @JsonProperty("recovers")
        private int recovers = 0;

        public int getValue() { return value; }

        public int getRecovers() { return recovers; }
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
        catch(Exception e){
        }
        return conditionsRead;
    }


    public static List<XWS2Upgrades.Condition> loadConditionsFromLocal() {
        String pathToUse = XWOTAUtils.getModulePath();
        conditionsDataSources whereToGetConditions = new conditionsDataSources();
        try {
            //Load the manifest in the local xwd2.zip
            DataArchive dataArchive = new DataArchive(pathToUse + File.separator + XWOTAUtils.XWD2DATAFILE);
            InputStream inputStream = dataArchive.getInputStream("manifest.json");

            whereToGetConditions = Util.loadClasspathJsonInDepot("manifest.json", conditionsDataSources.class, inputStream);

            inputStream.close();
            dataArchive.close();
        }catch(Exception e){
            Util.logToChat("XWS2Upgrades line 343 - couldn't load the manifest for conditions");
        }

        List<XWS2Upgrades.Condition> conditionsRead = Lists.newArrayList();
        try{

            DataArchive dataArchive = new DataArchive(pathToUse + File.separator + XWOTAUtils.XWD2DATAFILE);

            String suffixWithoutDataRoot = whereToGetConditions.getUrlEnd().split("data/")[1];
            InputStream is = dataArchive.getInputStream(suffixWithoutDataRoot);
            conditionsRead = XWS2Upgrades.loadLocalJsonArrayOfConditions(is);
            is.close();
            dataArchive.close();

        }catch(Exception e){
            Util.logToChat("XWS2Upgrades line 323 - couldn't load the manifest for conditions");

        }
        return conditionsRead;
    }


    public static XWS2Upgrades loadFromLocal() {
        String pathToUse = XWOTAUtils.getModulePath();

        upgradesDataSources whereToGetUpgrades = new upgradesDataSources();
        try{
            //Load the manifest in the local xwd2.zip
            DataArchive dataArchive = new DataArchive(pathToUse + File.separator + XWOTAUtils.XWD2DATAFILE);
            InputStream inputStream = dataArchive.getInputStream("manifest.json");

            whereToGetUpgrades = Util.loadClasspathJsonInDepot("manifest.json", upgradesDataSources.class, inputStream);

            inputStream.close();
            dataArchive.close();
        }catch(Exception e){
            Util.logToChat("XWS2Upgrades line 343 - couldn't load the manifest for upgrades");
        }

        XWS2Upgrades allUpgrades = new XWS2Upgrades();
        try {
        DataArchive dataArchive = new DataArchive(pathToUse + File.separator + XWOTAUtils.XWD2DATAFILE);

        for(String urlEnd : whereToGetUpgrades.getUrlEnds()) {
            List<XWS2Upgrades.OneUpgrade> upgradesListRead = Lists.newArrayList();

            String suffixWithoutDataRoot = urlEnd.split("data/")[1];

            InputStream is = dataArchive.getInputStream(suffixWithoutDataRoot);
            upgradesListRead = XWS2Upgrades.loadLocalJsonArrayOfOneUpgrades(is);

            //XWS2Upgrades upgradesListRead = Util.loadRemoteJson(new URL(guidoRootUrl+urlEnd), XWS2Upgrades.class);

            for (XWS2Upgrades.OneUpgrade oneUp : upgradesListRead) {
                allUpgrades.add(oneUp);
            }

            is.close();
        }
        dataArchive.close();
        }catch (Exception e){
        }

        return allUpgrades;
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


    public static List<XWS2Upgrades.OneUpgrade> loadLocalJsonArrayOfOneUpgrades(InputStream is) {
        try {
            List<XWS2Upgrades.OneUpgrade> rawData = mapper.readValue(is,  mapper.getTypeFactory().constructCollectionType(List.class, XWS2Upgrades.OneUpgrade.class));
            return rawData;
        } catch (Exception e) {
            System.out.println("Unhandled error parsing local json: \n" + e.toString());
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

    private static List<XWS2Upgrades.Condition> loadLocalJsonArrayOfConditions(InputStream is) {
        try {
            InputStream inputStream = new BufferedInputStream(is);
            List<XWS2Upgrades.Condition> rawData = mapper.readValue(inputStream,  mapper.getTypeFactory().constructCollectionType(List.class, XWS2Upgrades.Condition.class));
            return rawData;
        } catch (Exception e) {
            System.out.println("Unhandled error parsing local json: \n" + e.toString());
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
