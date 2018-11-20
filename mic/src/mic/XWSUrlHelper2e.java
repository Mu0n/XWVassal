package mic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by Mu0n on 19/11/18.
 */
public class XWSUrlHelper2e {
    static private Pattern yasb2Pat = Pattern.compile(".*?raithos.*");
    static private Pattern officialPat = Pattern.compile(".*?fantasyflightgames.*");

    public static boolean isYASB2(String url) {
        Matcher m = yasb2Pat.matcher(url);
        return m.matches();
    }

    //example
    //https://raithos.github.io/?f=Rebel%20Alliance&d=v5!s!76:-1,-1,-1,-1,-1:&sn=Unnamed%20Squadron&obs=
    private static String yasb2XWSRoot = "https://yasb2-xws.herokuapp.com/?";

    private static URL translateYasb2(String url) throws MalformedURLException {
        URL originalUrl = new URL(url);
        String queryString = originalUrl.getQuery();
        return new URL(yasb2XWSRoot + queryString);
    }

    public static boolean isOfficial(String url) {
        Matcher m = officialPat.matcher(url);
        return m.matches();
    }

    //example
    //
    private static String officialXWSRoot = "http://sb2xws.herokuapp.com/translate/";

    private static URL translateOfficial(String url) throws MalformedURLException {
        URL originalUrl = new URL(url);
        String queryString = originalUrl.toString();

        String pat = ".com/\\w+\\-\\w+\\/([\\w-]*)";
        Pattern pattern = Pattern.compile(pat);

        Matcher mat = pattern.matcher(queryString);

        if(mat.find()){
            return new URL(officialXWSRoot + mat.group(1));
        }  else  return null;
    }

    public static URL translate(String url) throws MalformedURLException {
        if (isYASB2(url)) {
            Util.logToChat("YASB2 list detected.");
            return translateYasb2(url);
        } else if (isOfficial(url)) {

            Util.logToChat("Official builder list detected.");
            return translateOfficial(url);
        } else {
            return null; //throw exception?
        }

    }
}