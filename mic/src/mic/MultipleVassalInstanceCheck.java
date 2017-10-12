package mic;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.module.documentation.HelpFile;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static mic.Util.getCurrentPlayer;
import static mic.Util.logToChat;

/**
 * Created by amatheny on 10/11/17.
 */
public class MultipleVassalInstanceCheck extends AbstractConfigurable {
    private static Logger logger = LoggerFactory.getLogger(MultipleVassalInstanceCheck.class);

    private static boolean alreadyRunning = false;
    private static int CHECK_INTERVAL_SECONDS = 5 * 60;

    private List<String> getProcessNames() {
        List<String> ret = Lists.newArrayList();
        try {
            Process p = Runtime.getRuntime().exec(getProcessListCommand());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String process;
            while ((process = input.readLine()) != null) {
                ret.add(process);
            }
            input.close();
        } catch (Exception e) {
            logger.error("Error getting running processes", e);
        }
        return ret;
    }

    public void addTo(Buildable parent) {
        if (alreadyRunning) {
            return;
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Iterable<String> processNames = getProcessNames();
                Iterable<String> vassalProcesses = Iterables.filter(processNames, new Predicate<String>() {
                    public boolean apply(String input) {
                        return input != null && input.contains("VASSAL.launch.Player");
                    }
                });

                if (Iterables.size(vassalProcesses) > 1) {
                    mic.Util.XWPlayerInfo playerInfo = getCurrentPlayer();
                    logToChat("* %s is running multiple instances of Vassal. " +
                            " Please report on github if you think this is a bug https://github.com/Mu0n/XWVassal/issues", playerInfo.getName());
                }
            }
        }, 0, CHECK_INTERVAL_SECONDS * 1000);
    }

    private static String getProcessListCommand() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "wmic process";
        }
        if (os.contains("nix")) {
            return "ps -few";
        }
        if (os.contains("mac")) {
            return "ps -few";
        }
        throw new Exception("Unable to get process for operating system = " + os);
    }

    // <editor-fold desc="unused vassal hooks">
    public String[] getAttributeNames() { return new String[]{}; }
    public void setAttribute(String s, Object o) { /* */ }
    public String[] getAttributeDescriptions() { return new String[]{}; }
    public Class[] getAttributeTypes() { return new Class[]{}; }
    public String getAttributeValueString(String key) { return ""; }
    public Class[] getAllowableConfigureComponents() { return new Class[0]; }
    public HelpFile getHelpFile() { return null; }
    public void removeFrom(Buildable parent) { /* */ }
    // </editor-fold>
}
