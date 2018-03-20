package mic;

import VASSAL.build.widget.PieceSlot;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by amatheny on 2/27/17.
 */
public class AltArtShipPicker {

    private static final String[] UWING_ART = {"attack","landing"};

    private static Map<String,String> newPilotAltArts = ImmutableMap.<String, String>builder()
            .put("prototypepilot","blue")
            .put("greensquadronpilot","green")
            .put("bluesquadronpilot","blue")
            .put("daggersquadronpilot","dagger")
            .put("knavesquadronpilot","knave")
            .put("emonazzameen","emon")
            .put("kathscarlet","kath")
            .put("mandalorianmercenary","merc")
            .put("bountyhunter","bountyhunter")
            .put("krassistrelix","krassis")
            .put("blacksunace","blacksunace")
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

    private static Map<String,String> defaultShipArts = ImmutableMap.<String, String>builder()
            .put("rebelalliance_awing","red")
            .put("rebelalliance_tiefighter","sabinestie")
            .put("rebelalliance_ywing","gold")
            .put("scumandvillainy_ywing","scum")
            .put("scumandvillainy_z95headhunter","blacksun")
            .put("rebelalliance_z95headhunter","rebel").build();

    private static Map<String,String[]> dualBaseShips = ImmutableMap.<String, String[]>builder()
            .put("uwing",UWING_ART).build();



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


    public static String[] getNewAltArtShip(String xwsPilot, String xwsShip, String faction)
    {
        Util.logToChat(xwsShip+" "+faction+" "+xwsPilot);
        String[] altArt = new String[2];
        boolean found = false;

        // first check for normal alt
        String normalAltArt = newPilotAltArts.get(xwsPilot);
        if(normalAltArt != null)
        {
            Util.logToChat("Found normal alt art");
            altArt[0] = normalAltArt;
            found = true;
        }

        // now check for faction/ship defaults
        if(!found)
        {
            Util.logToChat("checking faction/ship");
            String factionShip = XWImageUtils.simplifyFactionName(faction)+"_"+xwsShip;
            String defaultArt = defaultShipArts.get(factionShip);
            if(defaultArt != null)
            {

                Util.logToChat("Found faction/ship alt art");
                altArt[0] = defaultArt;
                found = true;
            }
        }

        // finally check for dual based ships (like the U-Wing)
        if(!found)
        {
            Util.logToChat("checking dual base");
            String[] dualBase = dualBaseShips.get(xwsShip);
            if(dualBase != null && dualBase[0] != null)
            {
                Util.logToChat("Found dual base alt art");
                altArt = dualBase;
                found = true;
            }
        }

        // if we still haven't found anything, then there is no alt art
        if(!found)
        {
            Util.logToChat("found nothing");
            altArt[0] = "";
        }

        return altArt;
    }

    public static PieceSlot getAltArtShip(String pilotName, Map<String, PieceSlot> altArtShips, PieceSlot defaultShip) {
        if (pilotName == null || altArtShips == null || altArtShips.size() == 0 || !pilotAltArts.containsKey(pilotName)) {
            return defaultShip;
        }
        String pattern = pilotAltArts.get(pilotName);
        if (pattern == null) {
            return defaultShip;
        }
        for(String altArtName: altArtShips.keySet()) {
            if (altArtName != null && altArtName.toLowerCase().contains(pattern.toLowerCase())) {
                return altArtShips.get(altArtName);
            }
        }
        return defaultShip;
    }
}
