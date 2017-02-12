package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceCloner;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by amatheny on 2/9/17.
 */
public class Util {

    private static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T loadRemoteJson(String url, Class<T> type) {
        try {
            InputStream inputStream = new BufferedInputStream(new URL(url).openStream());
            return mapper.readValue(inputStream, type);
        } catch (Exception e) {
            System.out.println("Unhandled error parsing remote json: \n" + e.toString());
            logToChat("Unhandled error parsing remote json: \n" + e.toString());
            return null;
        }
    }

    public static <T> T loadClasspathJson(String filename, Class<T> type) {
        try {
            InputStream inputStream = GameModule.getGameModule().getDataArchive().getInputStream(filename);
            if (inputStream == null) {
                logToChat("couldn't load " + filename);
            }
            return mapper.readValue(inputStream, type);
        } catch (Exception e) {
            System.out.println("Unhandled error parsing classpath json: \n" + e.toString());
            logToChat("Unhandled error parsing classpath json: \n" + e.toString());
            return null;
        }
    }


    public static void logToChat(String msg) {
        Command c = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), msg);
        c.execute();
        GameModule.getGameModule().sendAndLog(c);
    }

    public static GamePiece newPiece(PieceSlot slot) {
        return PieceCloner.getInstance().clonePiece(slot.getPiece());
    }
}
