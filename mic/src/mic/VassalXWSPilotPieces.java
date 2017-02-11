package mic;

import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceCloner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amatheny on 2/8/17.
 */
public class VassalXWSPilotPieces {
    private PieceSlot pilotCard;
    private PieceSlot dial;
    private PieceSlot movementCard;
    private List<Upgrade> upgrades = new ArrayList<Upgrade>();
    private PieceSlot movementStrip;
    private PieceSlot openDial;
    private MasterShipData.ShipData shipData;
    private MasterPilotData.PilotData pilotData;

    public PieceSlot getShip() {
        return ship;
    }

    public void setShip(PieceSlot ship) {
        this.ship = ship;
    }

    private PieceSlot ship;

    public VassalXWSPilotPieces() {

    }

    public VassalXWSPilotPieces(VassalXWSPilotPieces pieces) {
        this.dial = pieces.dial;
        this.movementCard = pieces.movementCard;
        this.movementStrip = pieces.movementStrip;
        this.openDial = pieces.openDial;
        this.pilotCard = pieces.pilotCard;
        this.ship = pieces.ship;
        this.shipData = pieces.shipData;
        this.pilotData = pieces.pilotData;
    }

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

    public List<Upgrade> getUpgrades() {
        return upgrades;
    }

    public void setMovementStrip(PieceSlot movementStrip) {
        this.movementStrip = movementStrip;
    }

    public void setOpenDial(PieceSlot openDial) {
        this.openDial = openDial;
    }

    public PieceSlot getMovementStrip() {
        return movementStrip;
    }

    public PieceSlot getOpenDial() {
        return openDial;
    }

    public void setShipData(MasterShipData.ShipData shipData) {
        this.shipData = shipData;
    }

    public void setPilotData(MasterPilotData.PilotData pilotData) {
        this.pilotData = pilotData;
    }

    public GamePiece clonePilotCard() {
        return Util.newPiece(this.pilotCard);
    }

    public GamePiece cloneDial() {
        GamePiece piece = Util.newPiece(this.dial);


        if (this.pilotData != null) {
            piece.setProperty("Pilot Name", this.pilotData.getShip());
            piece.setProperty("Craft ID #", this.pilotData.getName());
        }

        return piece;
    }

    public GamePiece cloneShip() {
        GamePiece piece = Util.newPiece(this.ship);

        if (this.shipData != null) {
            int agility = this.shipData.getAgility();
            int hull = this.shipData.getHull();
            int shields = this.shipData.getShields();
            int attack = this.shipData.getAttack();

            if (containsUpgrade("stealthdevice")) {
                agility++;
            }

            if (containsUpgrade("hullupgrade")) {
                hull++;
            }

            if (containsUpgrade("heavyscykinterceptor")) {
                hull++;
            }

            if (containsUpgrade("shieldupgrade")) {
                shields++;
            }

            if (containsUpgrade("punishingone")) {
                attack++;
            }

            if (containsUpgrade("allianceoverhaul")) {
                attack++;
            }

            if (containsUpgrade("specialopstraining")) {
                attack++;
            }

            piece.setProperty("Defense Rating", agility);
            piece.setProperty("Hull Rating", hull);
            piece.setProperty("Attack Rating", attack);
            piece.setProperty("Shield Rating", shields);

            if (this.shipData.getEnergy() > 0) {
                piece.setProperty("Energy Rating", this.shipData.getEnergy());
            }
        }

        if (this.pilotData != null) {
            int ps = this.pilotData.getSkill();
            if (containsUpgrade("veteraninstincts")) {
                ps += 2;
            }
            piece.setProperty("Pilot Skill", ps);

            piece.setProperty("Pilot Name", this.pilotData.getShip());
            piece.setProperty("Craft ID #", this.pilotData.getName());
        }


        return piece;
    }

    private boolean containsUpgrade(final String upgradeName) {
        return Iterables.any(this.upgrades, new Predicate<Upgrade>() {
            public boolean apply(Upgrade input) {
                return upgradeName.equals(input.getXwsName());
            }
        });
    }


    public static class Upgrade {
        private String xwsName;
        private PieceSlot pieceSlot;

        public Upgrade(String xwsName, PieceSlot pieceSlot) {
            this.xwsName = xwsName;
            this.pieceSlot = pieceSlot;
        }

        public String getXwsName() {
            return xwsName;
        }

        public PieceSlot getPieceSlot() {
            return this.pieceSlot;
        }

        public GamePiece cloneGamePiece() {
            return Util.newPiece(pieceSlot);
        }
    }
}
