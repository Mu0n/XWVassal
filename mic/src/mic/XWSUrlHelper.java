package mic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lyle on 2/11/17.
 */
public class XWSUrlHelper {
    static private Pattern yasbPat = Pattern.compile(".*?geordanr.*");
    static private Pattern voidstatePat = Pattern.compile(".*?xwing-builder\\.co\\.uk.*");
    static private Pattern fabsPat = Pattern.compile(".*?fabpsb.*");
    static private Pattern metaWingPat = Pattern.compile(".*?meta-wing\\.com/squadrons/(\\d+)\\.json");

    public static boolean isYASB(String url) {
        Matcher m = yasbPat.matcher(url);
        return m.matches();
    }

    //example
    //https://geordanr.github.io/xwing/?f=Rebel%20Alliance&d=v4!s!139:135,126,-1,17,166,137,138:-1:16:U.-1;9:135,-1,-1,-1:-1:-1:;9:135,-1,-1,-1:-1:-1:&sn=Mirandayy
    private static String yasbXWSRoot =  "https://yasb-xws.herokuapp.com/?";
    private static URL translateYasb(String url) throws MalformedURLException {
        URL originalUrl = new URL(url);
        String queryString = originalUrl.getQuery();
        return new URL(yasbXWSRoot + queryString);
    }

    public static boolean isFabs(String url) {
        Matcher m = fabsPat.matcher(url);
        return m.matches();
    }

    //example
    //http://x-wing.fabpsb.net/permalink.php?sq=r4a3n1r5a4m1n1r6a4n1
    private static URL translateFabs(String url) throws MalformedURLException {
        return new URL(url + "&xws=1");
    }

    public static boolean isMetaWing(String url) {
        Matcher m = metaWingPat.matcher(url);
        return m.matches();
    }

    public static boolean isVoidstate(String url) {
        Matcher m1 = voidstatePat.matcher(url);
        Matcher m2 = Pattern.compile("^\\d+$").matcher(url);
        return m1.matches() || m2.matches();
    }

    //example
    //http://xwing-builder.co.uk/build/649288
    //should turn into
    //http://xwing-builder.co.uk/xws/649288?raw=1
    private static URL translateVoidstate(String url) throws MalformedURLException {

        Pattern p = Pattern.compile("/?(\\d+)/?");
        Matcher m = p.matcher(url);
        if (m.find()) {
            String voidstateId = m.group(1);
            return new URL("http://xwing-builder.co.uk/xws/" + voidstateId + "?raw=1");
        }
        else {
            throw new MalformedURLException("Received voidstate url without a voidstate id: " + url );
        }
    }

    private static URL translateMetaWing(String url) throws MalformedURLException {
        return new URL(url);
    }

    public static URL translate(String url) throws MalformedURLException {
        if ( isYASB(url)) {
            return translateYasb(url);
        }
        else if ( isVoidstate(url)) {
            return translateVoidstate(url);
        }
        else if ( isFabs(url)) {
            return translateFabs(url);
        }
        else if ( isMetaWing(url)) {
            return translateMetaWing(url);
        }
        else {
            return null; //throw exception?
        }

    }


    public static void main(String...args){
        try {
            System.out.println(XWSUrlHelper.translate("http://xwing-builder.co.uk/view/648536/rac-n-jax#"));
            System.out.println(XWSUrlHelper.translate("648536"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}

