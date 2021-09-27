package mic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TerTer on 2017-02-24.
 */
public class Acronymizer {
    private static String THE = "The";
    private static String COLONEL = "Colonel";
    private static String LIEUTENANT = "Lieutenant";

    public static List<String> SHIP_NAME_PREFIXES = new ArrayList<String>();
    static
    {
        SHIP_NAME_PREFIXES.add(THE);
        SHIP_NAME_PREFIXES.add(COLONEL);
        SHIP_NAME_PREFIXES.add(LIEUTENANT);
    }

    public static String acronymizer(String cardName, boolean isUnique, boolean shortName) {
        int maxLength = shortName ? 8 : 14;
        String name = cardName.replace("\"", "");
        String[] words = name.split(" ");

        if (name.length() <= maxLength) return name;
        if (words.length == 1) {
            return name.substring(0, maxLength);
        } else if (isUnique) {
            if (SHIP_NAME_PREFIXES.contains(words[0])) {
                if (words[1].length() <= maxLength) {
                    return words[1];
                } else {
                    return words[1].substring(0, maxLength);
                }
            } else {
                return words[0];
            }
        }

        String firstLetters = "";
        for (String s: words){
            firstLetters += s.charAt(0);
        }
        return firstLetters;
    }
}
