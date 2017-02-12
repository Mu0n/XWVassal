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
    static private Pattern voidstatePat = Pattern.compile(".*?xwing-builder\\.co\\.uk\\/build\\/\\d+");
    static private Pattern fabsPat = Pattern.compile(".*?fabpsb.*");

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

    public static boolean isVoidstate(String url) {
        Matcher m = voidstatePat.matcher(url);
        return m.matches();
    }


    //example
    //http://xwing-builder.co.uk/build/649288
    //should turn into
    //http://xwing-builder.co.uk/xws/649288?raw=1
    private static URL translateVoidstate(String url) throws MalformedURLException {
        String xwsUrl = url.replace("/build/", "/xws/");
        return new URL(xwsUrl + "?raw=1");
    }



    public static URL translate(String url) throws MalformedURLException {
        if ( isYASB(url ) ) {
            return translateYasb(url);
        }
        else if ( isVoidstate(url)) {
            return translateVoidstate(url);
        }
        else if ( isFabs(url)) {
            return translateFabs(url);
        }
        else {
            return null; //throw exception?
        }

    }


    public static void main(String...args){
        try {
            System.out.println(XWSUrlHelper.translate("http://xwing-builder.co.uk/build/649288"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}

