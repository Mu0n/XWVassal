package mic;

import VASSAL.build.widget.PieceSlot;
import com.google.common.collect.ImmutableMap;
import mic.ota.XWOTAUtils;

import java.util.Map;

/**
 * Created by mjuneau on 28/08/18
 */
public class AltArtShipPicker2e {

    private static final String[] UWING_ART = {"attack","landing"};

    private static Map<String,String> newPilotAltArts = ImmutableMap.<String, String>builder()
            .put("blacksunace","blacksunace")
            .put("emonazzameen","emon")
            .put("krassistrelix","krassis")
            .put("greensquadronpilot","green")
            .put("mirandadoni","miranda")
            /*
            .put("prototypepilot","blue")
            .put("bluesquadronpilot","blue")
            .put("daggersquadronpilot","dagger")
            .put("knavesquadronpilot","knave")
            .put("kathscarlet","kath")
            .put("mandalorianmercenary","merc")
            .put("bountyhunter","bountyhunter")
            .put("grazthehunter","graz")
            .put("esegetuketu","esege")
            .put("guardiansquadronpilot","guardian")
            .put("laetinashera","laetin")
            .put("serissu","serissu")
            .put("poedameron","blackone")
            .put("poedameron-swx57","blackone")
            .put("gammasquadronveteran","veteran")
            .put("countessryad","veteran")
            .put("soontirfel","181st")
            .put("royalguardpilot","royalguard")
            .put("lukeskywalker","red5")
            .put("eadenvrill","eaden")
            .put("wildspacefringer","wildspacefringer")
            .put("graysquadronpilot","gray")
            .put("airencracken","airen")
            .put("banditsquadronpilot","bandit")
            .put("binayrepirate","binayre")
            .put("lieutenantblount","blount")
            .put("kaatoleeachos","kaato")
            .put("nashtahpuppilot","nashtah")
            .put("ndrusuhlak","ndru")
            .put("talasquadronpilot","tala").build();
            */
            .build();


    public static String getNewAltArtShip(String xwsPilot, String xwsShip, String faction)
    {

        String altArt = new String();
        boolean found = false;

        // first check for normal alt
        String normalAltArt = newPilotAltArts.get(xwsPilot);
        if(normalAltArt != null)
        {
            altArt = normalAltArt;
            found = true;
        }
/*
        // now check for faction/ship defaults
        if(!found)
        {
            String factionShip = XWOTAUtils.simplifyFactionName(faction)+"_"+xwsShip;
            String defaultArt = defaultShipArts.get(factionShip);
            if(defaultArt != null)
            {

                altArt = defaultArt;
                found = true;
            }
        }

*/
        // if we still haven't found anything, then there is no alt art
        if(!found)
        {

            altArt = "";
        }

        return altArt;
    }
}
