package mic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by Mic on 2018-07-31.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class XWS2Upgrades {
    private static String remoteUrl = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/master/upgrades.json";


    @JsonProperty("upgrades")
    private List<anUpgrade> upgrades = Lists.newArrayList();

    public List<anUpgrade> getUpgrades() { return this.upgrades; }


    public static class anUpgrade{
        public anUpgrade() { super(); }
        public anUpgrade(String name){
            this.name = name;
        }
        @JsonProperty("name")
        private String name;

        public String getName() { return this.name;}
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
                allUpgrades.add(Util.loadRemoteJson(oUDS.getURL(), XWS2Upgrades.class));
            }catch (Exception e){
                Util.logToChat(e.getMessage() + " on upgrade type " + oUDS.getName());
                continue;
            }
        }
        return allUpgrades;
    }
}
