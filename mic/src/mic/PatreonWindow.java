package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.*;
import VASSAL.build.module.documentation.HelpFile;
import com.google.common.collect.Lists;

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
import java.util.*;

import static javax.imageio.ImageIO.setUseCache;
import static mic.Util.logToChat;

/**
 * Created by Mic on 22/10/2017.
 */
public class PatreonWindow extends AbstractConfigurable  {
    private JButton patreonButton = new JButton();
    private static String defaultURL =        "https://www.patreon.com/mu0n";
    private static String listofPatronsURL = "https://raw.githubusercontent.com/Mu0n/XWVassal/master/patrons";

    private synchronized void PatreonWindow() {
        String msg ="";
        try {

            URL urlPatchNotes = new URL(listofPatronsURL);
            URLConnection con = urlPatchNotes.openConnection();
            con.setUseCaches(false);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlPatchNotes.openStream()));
            String line;
            while ((line = in.readLine()) != null) {
                msg += line;
                msg += "\n";
            }
            in.close();

            JFrame frame = new JFrame();
            JPanel panel = new JPanel();
            JLabel spacer;
            panel.setMinimumSize(new Dimension(1000,700));

            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            SwingLink link = new SwingLink("Click here to visit the Patreon page", defaultURL);

            panel.add(link);

            JOptionPane optionPane = new JOptionPane();
            optionPane.setMessage(msg);
            //optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
            optionPane.add(panel);
            JDialog dialog = optionPane.createDialog(frame, "Patreon Thank You List");

            dialog.setVisible(true);
            frame.toFront();
            frame.repaint();
        }catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());

        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }


    }
    public void addTo(Buildable parent) {
        JButton b = new JButton("Support the module through Patreon");
        b.setAlignmentY(0.0F);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                PatreonWindow();
            }
        });
        patreonButton = b;
        GameModule.getGameModule().getToolBar().add(b);
    }


    public void removeFrom(Buildable parent) {
        GameModule.getGameModule().getToolBar().remove(patreonButton);
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

}
