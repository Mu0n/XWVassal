package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

/**
 * Created by Mic on 09/04/2017.
 * Make it so the URL pointed at is configurable in the vassal editor?
 */
public class AnnouncementOnLog extends AbstractConfigurable {

    private static String defaultURL = "https://raw.githubusercontent.com/Mu0n/HWpopup/master/README.md";
    private synchronized void AnnouncementOnLog() {

    }

    public void addTo(Buildable parent) {

//TO DO import some text from a web address and display it
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
