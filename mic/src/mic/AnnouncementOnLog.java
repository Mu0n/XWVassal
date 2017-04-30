package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.module.documentation.HelpFile;

import java.io.BufferedReader;

import java.net.URL;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import static mic.Util.logToChat;

/**
 * Created by Mic on 09/04/2017.
 * Make it so the URL pointed at is configurable in the vassal editor?
 */
public class AnnouncementOnLog extends AbstractConfigurable {

    private static String defaultURL = "https://raw.githubusercontent.com/Mu0n/HWpopup/AnnouncementOnLog/VassalNews";
    private synchronized void AnnouncementOnLog() {

    }

    public void addTo(Buildable parent) {

try {
    URL url = new URL(defaultURL);

    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

    String line;

    while ((line = in.readLine()) != null) {
        logToChat("* " + line);
    }
    in.close();
        }
        catch (MalformedURLException e) {
     System.out.println("Malformed URL: " + e.getMessage());

        }
        catch (IOException e) {
         System.out.println("I/O Error: " + e.getMessage());
        }

    }

    public void removeFrom(Buildable parent) {
    }

    // <editor-fold desc="unused vassal hooks">
    @Override
    public String[] getAttributeNames() {
        return new String[]{};
    }

    @Override
    public void setAttribute(String s, Object o) {
        // No-op
    }

    @Override
    public String[] getAttributeDescriptions() {
        return new String[]{};
    }

    @Override
    public Class[] getAttributeTypes() {
        return new Class[]{};
    }

    @Override
    public String getAttributeValueString(String key) {
        return defaultURL;
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public HelpFile getHelpFile() {
        return null;
    }
    // </editor-fold>
}
