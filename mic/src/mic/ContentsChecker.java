package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import sun.security.pkcs11.Secmod;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Mic on 12/03/2018.
 */
public class ContentsChecker  extends AbstractConfigurable {
    private JButton OKButton = new JButton();

    private synchronized void ContentsCheckerWindow() {
        ModuleIntegrityChecker mic = new ModuleIntegrityChecker();


        String msg = mic.getTestString();;
            JFrame frame = new JFrame();
            JPanel panel = new JPanel();
            JLabel spacer;
            panel.setMinimumSize(new Dimension(1000, 700));

            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JOptionPane optionPane = new JOptionPane();
            optionPane.setMessage(msg);
            //optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
            optionPane.add(panel);
            JDialog dialog = optionPane.createDialog(frame, "Contents Checker");

            dialog.setVisible(true);
            frame.toFront();
            frame.repaint();
    }

    public void addTo(Buildable parent) {
        JButton b = new JButton("OK");
        b.setAlignmentY(0.0F);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ContentsCheckerWindow();
            }
        });
        OKButton = b;
        GameModule.getGameModule().getToolBar().add(b);
    }

    @Override
    public String[] getAttributeDescriptions() {
        return new String[0];
    }

    @Override
    public Class<?>[] getAttributeTypes() {
        return new Class[0];
    }

    @Override
    public String[] getAttributeNames() {
        return new String[0];
    }

    @Override
    public void setAttribute(String key, Object value) {

    }

    @Override
    public String getAttributeValueString(String key) {
        return null;
    }


    public void removeFrom(Buildable parent) {
        GameModule.getGameModule().getToolBar().remove(OKButton);
    }
    @Override
    public HelpFile getHelpFile() {
        return null;
    }

    @Override
    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }


}
