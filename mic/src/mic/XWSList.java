package mic;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
    private List<String> obstacles = Lists.newArrayList();

    @JsonProperty("pilots")
    private List<XWSPilot> pilots = Lists.newArrayList();

    @JsonProperty("vendor")
    private Map<String, Map<String, String>> vendor = Maps.newHashMap();


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
        private Map<String, List<String>> upgrades = Maps.newHashMap();

        @JsonProperty("vendor")
        private Map<String, Map<String, String>> vendor = Maps.newHashMap();

        @JsonProperty("points")
        private Integer points;

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

        public Integer getPoints() {
            return points;
        }
    }
}