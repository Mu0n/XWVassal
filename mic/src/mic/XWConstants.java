package mic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TerTer on 2017-02-23.
 */
public class XWConstants {
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
}
