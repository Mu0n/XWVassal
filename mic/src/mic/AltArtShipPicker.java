package mic;

import VASSAL.build.widget.PieceSlot;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by amatheny on 2/27/17.
 */
public class AltArtShipPicker {

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
