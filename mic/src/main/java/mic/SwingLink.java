package mic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

/**
 * Created by Mic on 31/05/2017.
 */
public class SwingLink extends JLabel {
    private static final long serialVersionUID = 8273875024682878518L;
    private String text;
    private URI uri;

    public SwingLink(String text, URI uri){
        super();
        setup(text,uri);
    }

    public SwingLink(String text, String uri){
        super();
        setup(text,URI.create(uri));
    }

    public void setup(String t, URI u){
        text = t;
        uri = u;
        setText(text);
        setToolTipText(uri.toString());
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                open(uri);
            }
            public void mouseEntered(MouseEvent e) {
                setText(text,false);
            }
            public void mouseExited(MouseEvent e) {
                setText(text,true);
            }
        });
    }

    @Override
    public void setText(String text){
        setText(text,true);
    }

    public void setText(String text, boolean ul){
        String link = ul ? "<u>"+text+"</u>" : text;
        super.setText("<html><span style=\"color: #000099;\">"+
                link+"</span></html>");
        this.text = text;
    }

    public String getRawText(){
        return text;
    }

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to launch the link, your computer is likely misconfigured.",
                        "Cannot Launch Link",JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Java is not able to launch links on your computer.",
                    "Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
        }
    }
}
