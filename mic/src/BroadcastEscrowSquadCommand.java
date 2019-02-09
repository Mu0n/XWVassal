package mic;

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

import java.util.Collection;

public class BroadcastEscrowSquadCommand extends Command {
    EscrowSquads.EscrowEntry entry;

    /*
    0: player.getSide()
    1: pre-validated XWS string
    2: source ("ffg", "yasb2", "internal", "xws")
    3: squad points
     */
    public BroadcastEscrowSquadCommand(EscrowSquads.EscrowEntry reqEntry){
        entry = reqEntry;
    }

    protected void executeCommand() {
        EscrowSquads.insertEntry(entry.playerSide, entry.playerName, entry.xwsSquad, entry.source, entry.points);
    }

    protected Command myUndoCommand() {
        return new BroadcastEscrowSquadCommand(new EscrowSquads.EscrowEntry(entry.playerSide, entry.playerName,"", "", ""));
    }

    public static class broadcastEscrowSquadCommandEncoder implements CommandEncoder {

        private static final Logger logger = LoggerFactory.getLogger(EscrowSquads.class);
        private static final String commandPrefix = "broadcastEscrowSquadEncoder=";
        private static final String itemDelim = "\t";

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
                 */
                EscrowSquads.EscrowEntry retEntry = new EscrowSquads.EscrowEntry(parts[0], parts[1], parts[2], parts[3], parts[4]);
                return new BroadcastEscrowSquadCommand(retEntry);
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

                return commandPrefix + Joiner.on(itemDelim).join(beq.entry.playerSide, beq.entry.playerName, beq.entry.xwsSquad, beq.entry.source, beq.entry.points);
            }catch(Exception e) {
                logger.error("Error encoding BroadcastEscrowSquadCommand", e);
                return null;
            }
        }
    }
}
