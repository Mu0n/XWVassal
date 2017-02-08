package mic;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by amatheny on 2/8/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class XWSList {
    @JsonProperty("name")
    private String name;

    @JsonProperty("faction")
    private String faction;

    @JsonProperty("points")
    private Integer points;

    @JsonProperty("version")
    private String version;

    @JsonProperty("description")
    private String description;

    @JsonProperty("obstacles")
    private List<String> obstacles;

    @JsonProperty("pilots")
    private List<XWSPilot> pilots;

    @JsonProperty("vendor")
    private Map<String, Map<String, String>> vendor;

    public String getName() {
        return name;
    }

    public String getFaction() {
        return faction;
    }

    public Integer getPoints() {
        return points;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getObstacles() {
        return obstacles;
    }

    public List<XWSPilot> getPilots() {
        return pilots;
    }

    public Map<String, Map<String, String>> getVendor() {
        return vendor;
    }

    public static class XWSPilot {
        @JsonProperty("multisection_id")
        private Integer multisectionId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("ship")
        private String ship;

        @JsonProperty("upgrades")
        private Map<String, List<String>> upgrades;

        @JsonProperty("vendor")
        private Map<String, Map<String, String>> vendor;

        public Integer getMultisectionId() {
            return multisectionId;
        }

        public String getName() {
            return name;
        }

        public String getShip() {
            return ship;
        }

        public Map<String, List<String>> getUpgrades() {
            return upgrades;
        }

        public Map<String, Map<String, String>> getVendor() {
            return vendor;
        }
    }
}