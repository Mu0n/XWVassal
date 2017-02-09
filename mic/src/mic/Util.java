package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by amatheny on 2/9/17.
 */
public class Util {
    public static <T> T loadRemoteJson(String url, Class<T> type) {
        try {
            InputStream inputStream = new BufferedInputStream(new URL(url).openStream());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(inputStream, type);
        } catch (Exception e) {
            System.out.println("Unhandled error parsing remote json: \n" + e.toString());
            logToChat("Unhandled error parsing remote json: \n" + e.toString());
            return null;
        }
    }


    public static void logToChat(String msg) {
        Command c = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), msg);
        c.execute();
        GameModule.getGameModule().sendAndLog(c);
    }
}
