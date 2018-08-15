package mic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by Mic on 2018-07-31.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class XWS2Upgrades {

    private static String remoteUrl = "https://raw.githubusercontent.com/Mu0nHub/xwing-data2/master/data/manifest.json";
    //private static String guidoRootUrl = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/";
    private static String guidoRootUrl = "https://raw.githubusercontent.com/Mu0nHub/xwing-data2/master/";

    @JsonUnwrapped
    private List<OneUpgrade> upgrades = Lists.newArrayList();

    public XWS2Upgrades() {}

    public List<OneUpgrade> getUpgrades(){return upgrades;}
    public void add(OneUpgrade oneUp){ upgrades.add(oneUp);}
    public int getCount() { return this.upgrades.size(); }

//more like upgrade type

    public class OneUpgrade{
        public OneUpgrade() { super(); }
        public OneUpgrade(String name, int limited, List<side> sides){
            this.name = name;
            this.limited = limited;
            this.sides = sides;
        }

        @JsonProperty("name")
        private String name;

        @JsonProperty("limited")
        private int limited;

        @JsonProperty("sides")
        List<side> sides = Lists.newArrayList();

        public String getName() { return this.name;}
        public int getLimited() { return this.limited; }
        public List<side> getSides() { return this.sides; }
    }

    public static class side{
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

        public String getTitle() { return this.title; }
        public String getType() { return this.type; }
        public String getAbility() { return this.ability; }



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


    public static List<XWS2Upgrades.OneUpgrade> loadFromRemote() {

        upgradesDataSources whereToGetUpgrades = new upgradesDataSources();
        try{
            whereToGetUpgrades = Util.loadRemoteJson(remoteUrl, upgradesDataSources.class);
        }catch(Exception e){

        }

        List<XWS2Upgrades.OneUpgrade> allUpgrades = Lists.newArrayList();
        for(String urlEnd : whereToGetUpgrades.getUrlEnds()){
            try {
                Util.logToChat("reading url end " + urlEnd);
                List<XWS2Upgrades.OneUpgrade> upgradesListRead = XWS2Upgrades.loadRemoteJsonArrayOfOneUpgrades(new URL(guidoRootUrl+urlEnd));

                Util.logToChat("upgradesListRead size " + Integer.toString(upgradesListRead.size()));
                for(XWS2Upgrades.OneUpgrade oneUp : upgradesListRead){
                    Util.logToChat("reading upgrade " + oneUp.getName());
                    allUpgrades.add(oneUp);
                }

            }catch (Exception e){
                Util.logToChat(e.getMessage() + " on upgrade type " + urlEnd);
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
            return mapper.readValue(inputStream,  mapper.getTypeFactory().constructCollectionType(List.class, XWS2Upgrades.OneUpgrade.class));
        } catch (Exception e) {
            System.out.println("Unhandled error parsing remote json: \n" + e.toString());
            return null;
        }
    }
}
