package mic;

/**
 * Created by Mic on 2020-04-13.
 * This main window button [Help] is meant to replace the How-To and Shortcuts and link to the vassal website
 */

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HelpWindow extends AbstractConfigurable {

    // debug flag - setting this to false skips the onLoad download of OTA
    private static final boolean DEBUG_DO_DOWNLOAD = true;

    private static String websiteVassal = "http://xwvassal.info";
    private static String guideURL = "http://xwvassal.info/guide";
    private JButton helpButton = new JButton();
    private JFrame updateCheckFrame;

    private static boolean checkComplete = false;
    private synchronized void HelpWindow() {
        JLabel titleLabel = new JLabel("How to get help - Click links to open in a browser");
        titleLabel.setFont(new Font("Serif", Font.PLAIN, 20));

        JLabel guideLabel = new JLabel("Guide for the X-Wing Vassal module: ");
        JLabel guideDesc = new JLabel("Contains step by step instructions on how to set up and play. Includes some videos.");

        JLabel websiteLabel = new JLabel("Website for the x-wing vassal module: ");
        JLabel websiteDesc = new JLabel("Contains links to ressources such as X-Wing Vassal League, Lady Luck, Video tutorials, Patch Notes, etc.");

        SwingLink guideDownloadLink = new SwingLink("http://xwvassal.info/guide ", guideURL);
        SwingLink websiteDownloadLink = new SwingLink("http://xwvassal.info", websiteVassal);
        JPanel panel = new JPanel();

        panel.setMinimumSize(new Dimension(900 ,1200));

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        labelPanel.setMinimumSize(new Dimension(700,400));

        labelPanel.add(titleLabel);
        labelPanel.add(Box.createRigidArea(new Dimension(0,8)));
        labelPanel.add(new JSeparator());
        labelPanel.add(Box.createRigidArea(new Dimension(0,8)));
        JPanel guideHoriz = new JPanel();
        guideHoriz.setLayout(new BoxLayout(guideHoriz, BoxLayout.X_AXIS));
        guideHoriz.add(guideLabel);
        guideHoriz.add(guideDownloadLink);

        labelPanel.add(guideHoriz);
        labelPanel.add(Box.createRigidArea(new Dimension(0,8)));
        labelPanel.add(guideDesc);


        labelPanel.add(Box.createRigidArea(new Dimension(0,12)));
        JPanel websiteHoriz = new JPanel();
        websiteHoriz.setLayout(new BoxLayout(guideHoriz, BoxLayout.X_AXIS));
        websiteHoriz.add(websiteLabel);
        websiteHoriz.add(websiteDownloadLink);
        labelPanel.add(websiteHoriz);
        labelPanel.add(Box.createRigidArea(new Dimension(0,8)));
        labelPanel.add(websiteDesc);


        panel.add(labelPanel);


        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);

        int answer = JOptionPane.showOptionDialog(frame, panel, "Help on the X-Wing Vassal Module",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[] { "OK" }, "OK");

        frame.requestFocus();

        frame.setAlwaysOnTop(false);
        frame.dispose();
    }
    public void addTo(Buildable parent) {

        JButton b = new JButton("Help");
        b.setAlignmentY(0.0F);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                HelpWindow();
            }
        });
        helpButton = b;
        GameModule.getGameModule().getToolBar().add(b);
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
        return guideURL;
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public HelpFile getHelpFile() {
        return null;
    }

}

