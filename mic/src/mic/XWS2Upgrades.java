package mic;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
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
    //private static String remoteUrl = "https://gist.githubusercontent.com/guidokessels/0751793c9635eb67dcc2cad897e14b4b/raw/6ec0489ca527c49258297ead1945604bf3d3d046/xwing-data2-manifest.json";
    private static String remoteUrl = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/master/upgrades.json";

    @JsonProperty("name")
    private String name;

    @JsonProperty("limited")
    private int limited;

    @JsonProperty("sides")
    List<side> sides = Lists.newArrayList();

    public String getName() { return this.name;}
    public int getLimited() { return this.limited; }
    public List<side> getSides() { return this.sides; }

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
        public upgradesDataSources(List<XWS2Upgrades.oneUpgradeDataSource> upgrades){
            this.upgrades = upgrades;
        }
        @JsonProperty("upgrades")
        List<XWS2Upgrades.oneUpgradeDataSource> upgrades = Lists.newArrayList();

        public List<XWS2Upgrades.oneUpgradeDataSource> getUpgrades(){return this.upgrades;}
    }

    public static class oneUpgradeDataSource{
        public oneUpgradeDataSource() { super();}
        public oneUpgradeDataSource(String name, String url){
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

    public static List<XWS2Upgrades> loadFromRemote() {
        upgradesDataSources whereToGetUpgrades = Util.loadRemoteJson(remoteUrl, upgradesDataSources.class);

        List<XWS2Upgrades> allUpgrades = Lists.newArrayList();
        for(oneUpgradeDataSource oUDS : whereToGetUpgrades.getUpgrades()){
            try {
                List<XWS2Upgrades> aList = XWS2Upgrades.loadRemoteJsonArray(new URL(oUDS.getURL()));
                for(XWS2Upgrades anUp : aList){
                    allUpgrades.add(anUp);
                }

            }catch (Exception e){
                Util.logToChat(e.getMessage() + " on upgrade type " + oUDS.getName());
                continue;
            }
        }
        return allUpgrades;
    }

    private static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static List<XWS2Upgrades> loadRemoteJsonArray(URL url) {
        try {
            InputStream inputStream = new BufferedInputStream(url.openStream());
            return mapper.readValue(inputStream,  mapper.getTypeFactory().constructCollectionType(List.class, XWS2Upgrades.class));
        } catch (Exception e) {
            System.out.println("Unhandled error parsing remote json: \n" + e.toString());
            return null;
        }
    }
}
