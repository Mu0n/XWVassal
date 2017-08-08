package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.BufferedReader;

import java.net.URL;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Random;

import static javax.imageio.ImageIO.setUseCache;
import static mic.Util.logToChat;

/**
 * Created by Mic on 09/04/2017.
 * Make it so the URL pointed at is configurable in the vassal editor?
 */
public class AnnouncementOnLog extends AbstractConfigurable {

    private static String defaultURL =        "https://raw.githubusercontent.com/Mu0n/XWVassal/master/VassalNews";
    private static String currentVersionURL = "https://raw.githubusercontent.com/Mu0n/XWVassal/master/currentVersion";
    private static String vassalDownloadURL = "http://www.vassalengine.org/wiki/Module:Star_Wars:_X-Wing_Miniatures_Game";

    private synchronized void AnnouncementOnLog() {

    }

    public void addTo(Buildable parent) {

        checkForUpdate();

        try {
            URL url = new URL(defaultURL);
            URLConnection con = url.openConnection();
            con.setUseCaches(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;

            while ((line = in.readLine()) != null) {
                logToChat("* " + line);
            }
            in.close();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());

        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }

    }

    private void checkForUpdate() {
        String userVersion = GameModule.getGameModule().getGameVersion();

        try {
            URL url = new URL(currentVersionURL);
            URLConnection con = url.openConnection();
            con.setUseCaches(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            line = in.readLine();
            in.close();
            if (!userVersion.equals(line)) {
                String msg = "You currently have version " + userVersion + " of the X-Wing Vassal module.\n"
                    + "A new version " + line + " is available!\n";

                SwingLink link = new SwingLink("X-Wing Vassal download page", vassalDownloadURL);

                JFrame frame = new JFrame();
                JPanel panel = new JPanel();
                panel.setMinimumSize(new Dimension(600,100));
                panel.add(link);

                JOptionPane optionPane = new JOptionPane();
                optionPane.setMessage(msg);
                //optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                optionPane.add(panel);
                JDialog dialog = optionPane.createDialog(frame, "New module version");

                dialog.setVisible(true);


            }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());

        } catch (IOException e) {
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


    public class JLabelLink extends JFrame {

        private static final String LABEL_TEXT = "For further information visit:";
        private static final String A_VALID_LINK = "http://stackoverflow.com";
        private static final String A_HREF = "<a href=\"";
        private static final String HREF_CLOSED = "\">";
        private static final String HREF_END = "</a>";
        private static final String HTML = "<html>";
        private static final String HTML_END = "</html>";

        // </editor-fold>
    }
}
