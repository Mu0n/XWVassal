package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by amatheny on 2/9/17.
 */
public class XWSMasterUpgrades extends HashMap<String, XWSMasterUpgrades.UpgradeType> {


    private static String remoteUrl = "https://github.com/elistevens/xws-spec/raw/master/dist/xws_upgrades.json";

    public static class UpgradeType {
        @JsonProperty("name")
        String name;

        @JsonProperty("upgrades")
        Map<String, Upgrade> upgrades;
    }

    public static class Upgrade {
        @JsonProperty("name")
        String name;

        @JsonProperty("points")
        Integer points;
    }

    public static XWSMasterUpgrades loadFromRemote() {
        return Util.loadRemoteJson(remoteUrl, XWSMasterUpgrades.class);
    }

    public static void main(String[] args) {
        loadFromRemote();
    }
}
