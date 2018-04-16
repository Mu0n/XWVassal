package mic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Created by Mic Juneau on 15/4/18
 * This contains the structure to load the master game mode router, essential to know where to look for xwing-data rules, custom images dispatched by the dispatcher, etc.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MasterGameModeRouter {
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
