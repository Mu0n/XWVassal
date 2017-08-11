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
    private static String githubDownloadURL = "https://github.com/Mu0n/XWVassal/releases";
    private static String guideURL = "http://xwvassal.info/guide";
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
        String msg ="";
        Boolean isGreater = false;
        try {
            URL url = new URL(currentVersionURL);
            URLConnection con = url.openConnection();
            con.setUseCaches(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            line = in.readLine();
            in.close();

                String[] onlineParts = line.split("\\.");
                String[] userParts = userVersion.split("\\.");
                int length = Math.max(userParts.length, onlineParts.length);
                for(int i = 0; i < length; i++) {
                    int userPart = i < userParts.length ?
                            Integer.parseInt(userParts[i]) : 0;
                    int onlinePart = i < onlineParts.length ?
                            Integer.parseInt(onlineParts[i]) : 0;
                    logToChat("user " + Integer.toString(userPart) + " online " + Integer.toString(onlinePart));
                    if(onlinePart > userPart) {
                        isGreater = true;
                        break;
                    } else if(userPart > onlinePart) break;
                }

                if(isGreater == true) msg += "A new version is available! ";
                else msg += "You have the latest version. ";

                msg += "You currently have version " + userVersion + " of the X-Wing Vassal module.\n"
                    + "The latest version available for download is " + line + "\n";

                SwingLink link = new SwingLink("X-Wing Vassal download page", vassalDownloadURL);
                SwingLink link2 = new SwingLink("Alt download page on github", githubDownloadURL);
                SwingLink link3 = new SwingLink("New? Need help? Go to the web guide", guideURL);

                JFrame frame = new JFrame();
                JPanel panel = new JPanel();
                JLabel spacer;

                panel.setMinimumSize(new Dimension(600,100));
                panel.add(link);
            panel.add(spacer = new JLabel(" "),"span, grow");
                panel.add(link2);
            panel.add(spacer = new JLabel(" "),"span, grow");
                panel.add(link3);

                JOptionPane optionPane = new JOptionPane();
                optionPane.setMessage(msg);
                //optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                optionPane.add(panel);
                JDialog dialog = optionPane.createDialog(frame, "Welcome to the X-Wing vassal module");

                dialog.setVisible(true);
                frame.toFront();
                frame.repaint();


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
