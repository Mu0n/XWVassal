package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Created by amatheny on 2/9/17.
 */
public class XWSMasterPilots {

    private static String remoteUrl = "https://raw.githubusercontent.com/elistevens/xws-spec/master/dist/xws_pilots.json";

    @JsonProperty("rebel")
    FactionPilots rebel;

    @JsonProperty("scum")
    FactionPilots scum;

    @JsonProperty("imperial")
    FactionPilots imperial;

    public static class FactionPilots {
        @JsonProperty("name")
        String name;

        @JsonProperty("ships")
        Map<String, ShipPilots> ships;
    }

    public static class ShipPilots {
        @JsonProperty("name")
        String name;

        @JsonProperty("subfaction")
        String subfaction;

        @JsonProperty("multisection")
        boolean multisection;

        @JsonProperty("pilots")
        Map<String, Pilot> pilots;
    }

    public static class Pilot {
        @JsonProperty("name")
        String name;

        @JsonProperty("points")
        Integer points;
    }

    public static XWSMasterPilots loadFromRemote() {
        return Util.loadRemoteJson(remoteUrl, XWSMasterPilots.class);
    }
}
