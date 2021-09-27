package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.properties.MutablePropertiesContainer;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.FileArchive;
import com.google.common.collect.Lists;
import mic.ota.*;
//import org.omg.CORBA.Environment;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static mic.Util.logToChat;

/**
 * Created by Mic on 09/04/2017.
 * Make it so the URL pointed at is configurable in the vassal editor?
 */
public class AnnouncementOnLog extends AbstractConfigurable {

    // debug flag - setting this to false skips the onLoad download of OTA
    private static final boolean DEBUG_DO_DOWNLOAD = true;

    private static String defaultURL =        "https://raw.githubusercontent.com/Mu0n/XWVassal-website/master/VassalNews";
    private static String currentVersionURL = "https://raw.githubusercontent.com/Mu0n/XWVassal-website/master/currentVersion";
    private static String blogURL = "https://raw.githubusercontent.com/Mu0n/XWVassal-website/master/currentBlog";
    private static String vassalDownloadURL = "http://www.vassalengine.org/wiki/Module:Star_Wars:_X-Wing_Miniatures_Game";
    private static String githubDownloadURL = "https://github.com/Mu0n/XWVassal/releases";
    private static String guideURL = "http://xwvassal.info/guide";

    private JFrame updateCheckFrame;

    private static boolean checkComplete = false;

    private synchronized void AnnouncementOnLog() {

    }

    public void addTo(Buildable parent) {
        openAnnouncementWindow();
    }

    private void openAnnouncementWindow()
    {
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
                //logToChat("user " + Integer.toString(userPart) + " online " + Integer.toString(onlinePart));
                if(onlinePart > userPart) {
                    isGreater = true;
                    break;
                } else if(userPart > onlinePart) break;
            }


            URL urlPatchNotes = new URL(blogURL);
            URLConnection con2 = urlPatchNotes.openConnection();
            con2.setUseCaches(false);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(urlPatchNotes.openStream()));
            String urlPatchString = in2.readLine();
            in2.close();


            if(isGreater == true) msg += "A new version is available!";
            else msg += "You have the latest version.";
/*
                msg += "You currently have version " + userVersion + " of the X-Wing Vassal module.<br>"
                    + "The latest version available for download is " + line + "<br><br>"
                        + "The Module is about to check for additional content and download it.<br>"
            + "Vassal may become unresponsive between a few seconds to a few minutes.<br>"
            + "Do you want to proceed?<br>"
                        + "You can choose to skip for now and perform this step by clicking on Contents Checker later.<br></html>";
*/
            JLabel versionLabel = new JLabel("You currently have version " + userVersion + " of the X-Wing Vassal module.");
            versionLabel.setFont(new Font("Serif", Font.PLAIN, 20));
            JLabel versionLabel2 = new JLabel(msg);
            JLabel versionLabel3 = new JLabel("The latest version available for download is " + line);
            JLabel disclaimLabel = new JLabel("This X-Wing Second Edition Vassal module is UNOFFICIAL.");
            JLabel disclaimLabel2 = new JLabel("It is not endorsed by Fantasy Flight Games and Asmodee.");
            JLabel checkLabel = new JLabel("The module is about to check for additional content.");
            JLabel checkLabel2 = new JLabel("The Content Checker button will flash black if it finds any.");
            JLabel checkLabel3 = new JLabel("Click on it to download the missing components.");
            SwingLink mainDownloadLink = new SwingLink("Download X-Wing Module here (github project page) ", githubDownloadURL);
            SwingLink altDownloadLink = new SwingLink("Alernate download mirror (vassalengine.org)", vassalDownloadURL);
            SwingLink whatsNewLink = new SwingLink("What's new in v" + line, urlPatchString);
            SwingLink guideLink = new SwingLink("New? Need help? Go to the web guide", guideURL);
            SwingLink supportLink = new SwingLink("Support the X-Wing Vassal module", "http://xwvassal.info/supportmodule.html");
            SwingLink homeLink = new SwingLink("X-Wing module website", "http://xwvassal.info");

            JPanel panel = new JPanel();

            panel.setMinimumSize(new Dimension(1000,1600));

            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
            labelPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            labelPanel.setMinimumSize(new Dimension(700,400));
            labelPanel.add(versionLabel);
            labelPanel.add(versionLabel2);
            labelPanel.add(versionLabel3);
            labelPanel.add(disclaimLabel);
            labelPanel.add(disclaimLabel2);

            labelPanel.add(Box.createRigidArea(new Dimension(0,8)));
            labelPanel.add(new JSeparator());
            labelPanel.add(Box.createRigidArea(new Dimension(0,8)));

            labelPanel.add(checkLabel);
            labelPanel.add(checkLabel2);
            labelPanel.add(checkLabel3);
            panel.add(labelPanel);

            DataArchive dataArchive = GameModule.getGameModule().getDataArchive();
            if(dataArchive==null) {
                JOptionPane.showMessageDialog(null, "Error: can't manipulate the module and extract content from it. Move the x-wing module file in another location.", "Access Error to the module file", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JPanel linkPanel = new JPanel();
            linkPanel.setLayout(new BoxLayout(linkPanel, BoxLayout.X_AXIS));

            JPanel homeLinkArea = new JPanel();
            homeLinkArea.setLayout(new BoxLayout(homeLinkArea, BoxLayout.Y_AXIS));
            homeLinkArea.setMinimumSize(new Dimension(275,400));
            homeLinkArea.setAlignmentY(Component.TOP_ALIGNMENT);
            JLabel homeIcon = new JLabel();

            BufferedImage img = null;
            InputStream is = dataArchive.getInputStream("images/Token_2e_force.png");
            img = ImageIO.read(is);
            is.close();
            homeIcon.setIcon(new ImageIcon(img));

            homeLinkArea.add(homeIcon);
            homeLinkArea.add(homeLink);
            homeLinkArea.add(supportLink);

            JPanel downloadLinkArea = new JPanel();
            downloadLinkArea.setLayout(new BoxLayout(downloadLinkArea, BoxLayout.Y_AXIS));
            downloadLinkArea.setMinimumSize(new Dimension(275,400));
            downloadLinkArea.setAlignmentY(Component.TOP_ALIGNMENT);
            JLabel downloadIcon = new JLabel();

            BufferedImage img2 = null;
            InputStream is2 = dataArchive.getInputStream("images/Token_2e_reinforce_fore.png");
            img2 = ImageIO.read(is2);
            is2.close();
            downloadIcon.setIcon(new ImageIcon(img2));

            downloadLinkArea.add(downloadIcon);
            downloadLinkArea.add(mainDownloadLink);
            downloadLinkArea.add(altDownloadLink);

            JPanel guideLinkArea = new JPanel();
            guideLinkArea.setLayout(new BoxLayout(guideLinkArea, BoxLayout.Y_AXIS));
            guideLinkArea.setMinimumSize(new Dimension(275,400));
            guideLinkArea.setAlignmentY(Component.TOP_ALIGNMENT);
            JLabel guideIcon = new JLabel();

            BufferedImage img3 = null;
            InputStream is3 = dataArchive.getInputStream("images/Token_2e_focus.png");
            img3 = ImageIO.read(is3);
            is3.close();
            guideIcon.setIcon(new ImageIcon(img3));

            guideLinkArea.add(guideIcon);
            guideLinkArea.add(guideLink);
            guideLinkArea.add(whatsNewLink);



            linkPanel.add(homeLinkArea);
            linkPanel.add(Box.createHorizontalStrut(30));
            linkPanel.add(downloadLinkArea);
            linkPanel.add(Box.createHorizontalStrut(30));
            linkPanel.add(guideLinkArea);
            linkPanel.add(Box.createHorizontalStrut(30));

            panel.add(linkPanel);

            JFrame frame = new JFrame();
            frame.setAlwaysOnTop(true);

            int answer = JOptionPane.showOptionDialog(frame, panel, "Welcome to the X-Wing vassal module",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new String[] { "OK" }, "OK");

            frame.requestFocus();

            frame.setAlwaysOnTop(false);
            frame.dispose();

            if(answer==0) {
                //the jsons used to be downloaded here and kept in local copies, but the addTo method of the Content Checker is doing that now
            }else{
            }

            /*
            JOptionPane optionPane = new JOptionPane();
                optionPane.setMessage(msg);
                optionPane.setOptionType(JOptionPane.YES_NO_OPTION);
                //optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                optionPane.add(panel);
                JDialog dialog = optionPane.createDialog(frame, "Welcome to the X-Wing vassal module");
                dialog.setPreferredSize(new Dimension(1200,800));


            frame.setVisible(true);
                frame.toFront();
                frame.repaint();
*/

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
