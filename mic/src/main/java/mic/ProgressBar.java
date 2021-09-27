package mic;

import javax.swing.*;

public class ProgressBar extends JPanel {
    static final int MY_MINIMUM = 0;

    static final int MY_MAXIMUM = 100;

    private   JFrame frame ;
    private  JProgressBar pbar;

    public ProgressBar() {
        // initialize Progress Bar
        pbar = new JProgressBar();
        pbar.setMinimum(MY_MINIMUM);
        pbar.setMaximum(MY_MAXIMUM);
        pbar.setStringPainted(true);
        // add to JPanel
        add(pbar);
    }

    public  void updateBar(int newValue, String text) {

        pbar.setValue(newValue);
        pbar.setString(text);
        pbar.repaint();
    }

    public  void closeFrame(  )
    {
        frame.setVisible(false);
    }

    public  void openFrame()
    {

        frame = new JFrame("Downloading Images");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(this);
        frame.pack();
        frame.setSize(700,200);
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);


    }
}
