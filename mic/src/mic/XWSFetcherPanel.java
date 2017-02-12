package mic;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class XWSFetcherPanel implements HyperlinkListener {

    private JFrame frm = new JFrame();
    private JTextPane guide = new JTextPane();
    private JPanel builders = new JPanel();
    private JPanel rawXws = new JPanel();
    private JTabbedPane tabbedPane = new JTabbedPane();
    private XWSList xwsList;

    public XWSFetcherPanel() throws IOException, BadLocationException {
        tabbedPane.addTab("via Link", builders);
        tabbedPane.addTab("via Raw XWS", rawXws);
        tabbedPane.addTab("How To", guide);

        frm.add(tabbedPane, BorderLayout.CENTER);

        //guide stuff
        guide.setPreferredSize(new Dimension(480, 200));

        guide.setContentType("text/html");
        guide.setEditable(false);
        HTMLDocument doc = (HTMLDocument) guide.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit) guide.getEditorKit();
        String guideHTML = "<html><br>" +
                "<h2>Howto</h2>" +
                "Welcome to the X-Wing Vassal XWS Importer! XWS is a format invented by the X-Wing software " +
                "development community to share X-Wing lists. You can import your list by either providing a link " +
                "to the list from one of the on-line builders, or by providing the raw XWS code (which the " +
                "builders can also provide.<br><br>" +
                "<a href=\"https://geordanr.github.io/xwing/\">Yet Another Squad Builder</a><br>" +
                "<a href=\"xwing-builder.co.uk\">voidstate</a><br>" +
                "<a href=\"x-wing.fabpsb.net\">Fabs</a><br>" +
                "</html>";

        editorKit.insertHTML(doc, doc.getLength(), guideHTML, 0, 0, null);
        guide.addHyperlinkListener(this);
        Border raisedBorder = BorderFactory.createRaisedBevelBorder();
        guide.setBorder(raisedBorder);

        //builder JLabel
        builders.setLayout(new BorderLayout());
        final JTextField xwsUrl = new JTextField();
        xwsUrl.setText("Url of your squad in YASB, Voidstate, or FABS");
        builders.add(xwsUrl, BorderLayout.NORTH);
        JLabel xwsFetchInfoLabel = new JLabel();
        xwsFetchInfoLabel.setText("Fetch Status");
        builders.add(xwsFetchInfoLabel, BorderLayout.LINE_START);
        final JTextArea fetchResultArea = new JTextArea();
        fetchResultArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(fetchResultArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        builders.add(scroll, BorderLayout.CENTER);
        xwsUrl.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String url = xwsUrl.getText();
                    try {
                        URL xwsUrl = XWSUrlHelper.translate(url);
                        xwsList = XWSFetcher.fetchFromUrl(xwsUrl.toString());
                        if (xwsList != null) {
                            fetchResultArea.setText("SUCCESS!");
                        } else {
                            fetchResultArea.setText(String.format("XWS Fetch for URL\n%s\ndid not return a valid result, please check the url?", xwsUrl.toString()));
                        }
                    } catch (Exception exception) {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        exception.printStackTrace(pw);
                        fetchResultArea.setText("XWS fetch failed, reason:\n" + sw.toString()); // stack trace as a string
                    }
                }
            }

        });

        frm.setLocation(150, 100);
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // EDIT
        frm.setResizable(false);
        frm.pack();
        frm.setVisible(true);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    XWSFetcherPanel fS = new XWSFetcherPanel();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            openWebpage(e.getURL());
        }

    }
}