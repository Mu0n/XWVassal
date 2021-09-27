package mic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mic.ota.OTAContentsChecker;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Mic Juneau on 15/4/18
 * This contains the structure to load the master game mode router, essential to know where to look for xwing-data rules, custom images dispatched by the dispatcher, etc.
 *
 */

public class Suggested2eBuilders extends ArrayList<Suggested2eBuilders.Builder> {

    private static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static String BUILDERS_REFERENCE_LIST = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/master/builders.json";
    private static List<Builder> suggestions = null;

    public void loadData() {
        if(suggestions == null){
            try{
                InputStream inputStream = new BufferedInputStream((new URL(BUILDERS_REFERENCE_LIST)).openStream());
                this.suggestions = mapper.readValue(inputStream, mapper.getTypeFactory().constructCollectionType(List.class, Suggested2eBuilders.Builder.class));

            }catch(Exception e){
                System.out.println("Unhandled error parsing remote json: \n" + e.toString());
            }
        }
    }

    public List<Builder> getSuggestions() { return suggestions; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {

    public Builder(){ super(); }

    @JsonProperty("name")
    private String name;
    @JsonProperty("url")
    private String url;
    @JsonProperty("description")
    private String description;

    public String getName() { return this.name; }
    public String getURL() { return this.url; }
    public String getDescription() { return this.description; }

    }
}
