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
            /*
            .put("prototypepilot","blue")
            .put("greensquadronpilot","green")
            .put("bluesquadronpilot","blue")
            .put("daggersquadronpilot","dagger")
            .put("knavesquadronpilot","knave")
            .put("kathscarlet","kath")
            .put("mandalorianmercenary","merc")
            .put("bountyhunter","bountyhunter")
            .put("krassistrelix","krassis")
            .put("grazthehunter","graz")
            .put("esegetuketu","esege")
            .put("guardiansquadronpilot","guardian")
            .put("mirandadoni","miranda")
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

    private static Map<String,String> defaultShipArts = ImmutableMap.<String, String>builder()
            .put("rebelalliance_awing","red")
            .put("rebelalliance_tiefighter","sabinestie")
            .put("rebelalliance_ywing","gold")
            .put("scumandvillainy_ywing","scum")
            .put("scumandvillainy_z95headhunter","blacksun")
            .put("rebelalliance_z95headhunter","rebel").build();

   // private static Map<String,String[]> dualBaseShips = ImmutableMap.<String, String[]>builder()
   //         .put("uwing",UWING_ART).build();



    private static Map<String, String> pilotAltArts = ImmutableMap.<String, String>builder()
            .put("greensquadronpilot", "Green Squadron")
            .put("prototypepilot", "Prototype")
            .put("bluesquadronpilot", "B-Wing Blue")
            .put("daggersquadronpilot", "B-Wing Dagger")
            .put("knavesquadronpilot", "Knave")
            .put("guardiansquadronpilot", "Guardian")
            .put("esegetuketu", "Esege")
            .put("miranda", "Miranda")
            .put("lukeskywalker", "Luke")
            .put("graysquadronpilot", "Gray")
            .put("poedameron", "Black One")
            .put("poedameron-swx57", "Black One")
            .put("wildspacefringer", "Fringer")
            .put("eadenvrill", "Eaden")
            .put("banditsquadronpilot", "Bandit")
            .put("talasquadronpilot", "Tala")
            .put("airencracken", "Cracken")
            .put("lieutenantblount", "Blount")
            .put("bountyhunter", "Bounty")
            .put("krassistrelix", "Krassis")
            .put("kathscarlet", "Kath")
            .put("countessryad", "Veteran")
            .put("tomaxbren", "Veteran")
            .put("gammasquadronpilot", "Veteran")
            .put("soontirfel", "181st")
            .put("royalguardpilot", "Royal")
            .put("emonazzameen", "Emon")
            .put("mandalorianmercenary", "Mandalorian")
            .put("grazthehunter", "Graz")
            .put("blacksunace", "Black Sun")
            .put("laetinashera", "Laetin")
            .put("serissu", "Serissu")
            .put("binayrepirate", "Binayre")
            .put("blacksunsoldier", "Black Sun")
            .put("kaatoleeachos", "Leeachos")
            .put("ndrusuhlak", "Suhlak")
            .put("nashtahpuppilot", "Nashtah")
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


        // if we still haven't found anything, then there is no alt art
        if(!found)
        {

            altArt = "";
        }

        return altArt;
    }
}
