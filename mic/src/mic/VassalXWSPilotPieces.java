package mic;

import VASSAL.build.widget.PieceSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amatheny on 2/8/17.
 */
public class VassalXWSPilotPieces {
    private PieceSlot pilotCard;
    private PieceSlot dial;
    private PieceSlot movementCard;
    private List<PieceSlot> upgrades = new ArrayList<PieceSlot>();

    public PieceSlot getPilotCard() {
        return pilotCard;
    }

    public void setPilotCard(PieceSlot pilotCard) {
        this.pilotCard = pilotCard;
    }

    public PieceSlot getDial() {
        return dial;
    }

    public void setDial(PieceSlot dial) {
        this.dial = dial;
    }

    public PieceSlot getMovementCard() {
        return movementCard;
    }

    public void setMovementCard(PieceSlot movementCard) {
        this.movementCard = movementCard;
    }

    public List<PieceSlot> getUpgrades() {
        return upgrades;
    }
}
