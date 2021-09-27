package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.command.Command;

/**
 * Created by mjuneau in 2019-02-08.
 * Allows a squad XWS to be sent to the escrow service managed in the EscrowSquads class. When it receives that data,
 * the popup frame managed by it will be able to be properly populated with the list of active players, who has sent
 * a squad to escrow, who has marked its status as ready and which opponent they want.
 */

import VASSAL.command.CommandEncoder;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static mic.Util.*;

/*
  Created by mjuneau in 2019-02-08
  Allows to insert an escrowed squad into everyone's EscrowSquads' object
 */
public class BroadcastEscrowSquadCommand extends Command {
    EscrowSquads.EscrowEntry entry;
    boolean isReady;

    /*
    0: player.getSide()
    1: pre-validated XWS string
    2: source ("ffg", "yasb2", "internal", "xws")
    3: squad points
     */
    public BroadcastEscrowSquadCommand(EscrowSquads.EscrowEntry reqEntry, boolean wantReady){
        entry = reqEntry;
        isReady = wantReady;
    }

    protected void executeCommand() {
        EscrowSquads.insertEntry(entry.playerSide, entry.playerName, entry.xwsSquad, entry.source, entry.points, entry.isReady, entry.moduleVersion);
    }

    protected Command myUndoCommand() {
        return null;
    }

    public static class broadcastEscrowSquadCommandEncoder implements CommandEncoder {

        private static final Logger logger = LoggerFactory.getLogger(AutoSquadSpawn2e.class);
        private static final String commandPrefix = "broadcastEscrowSquadEncoder=";
        private static final String itemDelim = "\t";

        public static BroadcastEscrowSquadCommand.broadcastEscrowSquadCommandEncoder INSTANCE = new BroadcastEscrowSquadCommand.broadcastEscrowSquadCommandEncoder();

        public Command decode(String command) {
            if(command == null || !command.contains(commandPrefix)) {
                return null;
            }
            command = command.substring(commandPrefix.length());
            String[] parts = command.split(itemDelim);
            try{
                /*
                0: player.getSide()
                1: player name
                2: pre-validated XWS string
                3: source ("ffg", "yasb2", "internal", "xws")
                4: squad points
                5: boolean to say if ready
                6: module version
                 */
                XWSList2e xwsList = (XWSList2e) deserializeBase64Obj(parts[2]);
                Boolean readyDecoded = Boolean.parseBoolean(parts[5]);
                EscrowSquads.EscrowEntry retEntry = new EscrowSquads.EscrowEntry(parts[0], parts[1], xwsList, parts[3], parts[4],readyDecoded, parts[6]);
                return new BroadcastEscrowSquadCommand(retEntry, readyDecoded);
            }catch(Exception e){
                logger.info("Error decoding BroadcastEscrowSquadCommand - exception error");
                return null;
            }
        }

        public String encode(Command c) {
            if (!(c instanceof BroadcastEscrowSquadCommand)) {
                return null;
            }
            try{
                BroadcastEscrowSquadCommand beq = (BroadcastEscrowSquadCommand) c;
                Serializable serList = (Serializable) beq.entry.xwsSquad;
                String detectedVersion = GameModule.getGameModule().getGameVersion();
                return commandPrefix + Joiner.on(itemDelim).join(beq.entry.playerSide, beq.entry.playerName, serializeToBase64(serList), beq.entry.source, beq.entry.points, ""+beq.isReady, detectedVersion);
            }catch(Exception e) {
                logger.error("Error encoding BroadcastEscrowSquadCommand", e);
                return null;
            }
        }
    }
}
