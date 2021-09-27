package mic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import mic.ota.OTAContentsChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Mic Juneau on 15/4/18
 * This contains the structure to load the master game mode router, essential to know where to look for xwing-data rules, custom images dispatched by the dispatcher, etc.
 *
 */

public class MasterGameModeRouter extends ArrayList<MasterGameModeRouter.GameMode> {

    private static Map<String, GameMode> loadedData = null;

    public static void loadData()
    {
        MasterGameModeRouter data = Util.loadRemoteJson(OTAContentsChecker.modeListURL, MasterGameModeRouter.class);

        if (data == null) {
            // Util.logToChat("Unable to load the game mode list from the web, falling back to local copy");
            data = Util.loadClasspathJson("modeList.json", MasterGameModeRouter.class);
        }

        loadedData = Maps.newHashMap();

        for(GameMode gameMode : data)
        {
            loadedData.put(gameMode.getName(),gameMode);
        }
    }

    public GameMode[] getGameModes()
    {
        if(loadedData == null)
        {
            loadData();
        }
        GameMode[] gMA = Arrays.copyOf(loadedData.values().toArray(), loadedData.values().toArray().length, GameMode[].class);
        return gMA;
    }

    public GameMode getGameMode(String name)
    {
        if(loadedData == null)
        {
            loadData();
        }

        return loadedData.get(name);
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GameMode {
        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("basedataurl")
        private String basedataurl;

        @JsonProperty("dispatchers")
        private String dispatchers;

        @JsonProperty("wantFullControl")
        private Boolean wantFullControl;

        public String getName() {
            return name;
        }
        public void setName(String name)
        {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }
        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getBaseDataURL() {
            return basedataurl;
        }
        public void setBaseDataURL(String basedataurl)
        {
            this.basedataurl = basedataurl;
        }

        public String getDispatchersURL() {
            return dispatchers;
        }
        public void setDispatchersURL(String dispatchers)
        {
            this.dispatchers = dispatchers;
        }

        public Boolean getWantFullControl() {
            return wantFullControl;
        }
        public void setWantFullControl(Boolean wantFullControl)
        {
            this.wantFullControl = wantFullControl;
        }

    }
}