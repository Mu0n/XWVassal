package mic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import mic.ota.OTAContentsChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Mic on 2018-07-27.
 */
public class sirjorj2econtent extends ArrayList<sirjorj2econtent.pilots> {

    private static Map<String, sirjorj2econtent.pilots> loadedData = null;

    public static void loadData()
    {
    }

  /*  public sirjorj2econtent.pilots[] getPilots()
    {
    }

    public sirjorj2econtent.pilots getPilots(String name)
    {

    }
    */

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class pilots {
        @JsonProperty("name")
        private String name;

        public String getName() {
            return name;
        }
        public void setName(String name)
        {
            this.name = name;
        }

    }
}
