
package mic;

/**
 * Created by Mic on 2020-04-13.
 * This Events Calendar leads to the community driven Google Calendar, where signups and event dates are broadcasted from a curated
 * list of community organizers of tournaments and 1-day events.
 */

        import VASSAL.build.AbstractConfigurable;
        import VASSAL.build.Buildable;
        import VASSAL.build.GameModule;
        import VASSAL.build.module.documentation.HelpFile;
        import VASSAL.tools.DataArchive;

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

public class EventsCalendar extends AbstractConfigurable {

    private static String calendarURL = "https://calendar.google.com/calendar/embed?src=52h67q92ipbpbo5oin0a7mbn8k%40group.calendar.google.com&ctz=America%2FNew_York";
    private JButton eventsButton = new JButton();

    private synchronized void EventsCalendar() {
        try {
        JLabel titleLabel = new JLabel("Events on Vassal X-Wing - Click link to open in a browser");
        titleLabel.setFont(new Font("Serif", Font.PLAIN, 20));

        JLabel calendarDescLabel = new JLabel("Automatically updated Google Calendar with event links, signup dates and event dates. These are organized by active members of the X-Wing community.");
        JLabel calendarLinkLabel = new JLabel("Link to the Google Calendar: ");
        JLabel addExplanationLabel = new JLabel("You may import this Calendar to your Google account by clicking this button on the bottom right of that page. No need to visit the Calendar through here again!");
            JLabel addCalendarImage = new JLabel();
            DataArchive dataArchive = GameModule.getGameModule().getDataArchive();
            BufferedImage img = null;
            InputStream is = dataArchive.getInputStream("images/addtoCalendar.png");
            img = ImageIO.read(is);
            is.close();
            addCalendarImage.setIcon(new ImageIcon(img));
        SwingLink calendarLink = new SwingLink("Open Calendar in browser", calendarURL);

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

        labelPanel.add(calendarDescLabel);

        JPanel guideHoriz = new JPanel();
        guideHoriz.setLayout(new BoxLayout(guideHoriz, BoxLayout.X_AXIS));
        guideHoriz.add(calendarLinkLabel);
        guideHoriz.add(calendarLink);

        labelPanel.add(guideHoriz);
        labelPanel.add(Box.createRigidArea(new Dimension(0,8)));
        labelPanel.add(addExplanationLabel);
        labelPanel.add(addCalendarImage);


        panel.add(labelPanel);


        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);

        int answer = JOptionPane.showOptionDialog(frame, panel, "Vassal X-Wing Events in Community Calendar",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[] { "OK" }, "OK");

        frame.requestFocus();

        frame.setAlwaysOnTop(false);
        frame.dispose();
    } catch (MalformedURLException e) {
        System.out.println("Malformed URL: " + e.getMessage());

    } catch (IOException e) {
        System.out.println("I/O Error: " + e.getMessage());
    }
    }
    public void addTo(Buildable parent) {

        JButton b = new JButton("Events Calendar");
        b.setAlignmentY(0.0F);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                EventsCalendar();
            }
        });
        eventsButton = b;
        GameModule.getGameModule().getToolBar().add(b);
    }

    public void removeFrom(Buildable parent) {
        GameModule.getGameModule().getToolBar().remove(eventsButton);
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
        return calendarURL;
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public HelpFile getHelpFile() {
        return null;
    }

}

