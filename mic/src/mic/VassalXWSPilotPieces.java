package mic;

import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.GamePiece;
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
    private Integer shipNumber = null;

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

    public void setShipNumber(Integer number) {
        this.shipNumber = number;
    }

    public void setPilotData(MasterPilotData.PilotData pilotData) {
        this.pilotData = pilotData;
    }

    public GamePiece clonePilotCard() {
        return Util.newPiece(this.pilotCard);
    }

    public GamePiece cloneDial() {
        GamePiece piece = Util.newPiece(this.dial);

        setPilotShipName(piece);

        return piece;
    }

    public GamePiece cloneShip() {
        GamePiece piece = Util.newPiece(this.ship);

        int skillModifier = 0;
        int attackModifier = 0;
        int agilityModifier = 0;
        int energyModifier = 0;
        int shieldsModifier = 0;
        int hullModifier = 0;

        for (Upgrade upgrade : this.upgrades) {
            for(MasterUpgradeData.UpgradeGrants modifier : upgrade.getUpgradeData().getGrants()) {
                if (modifier.isStatsModifier()) {
                    String name = modifier.getName();
                    int value = modifier.getValue();

                    if (name.equals("attack")) attackModifier += value;
                    else if (name.equals("agility")) agilityModifier += value;
                    else if (name.equals("hull")) hullModifier += value;
                    else if (name.equals("shields")) shieldsModifier += value;
                    else if (name.equals("skill")) skillModifier += value;
                    else if (name.equals("energy")) energyModifier += value;
                }
            }
        }

        if (this.shipData != null) {
            int agility = this.shipData.getAgility();
            int hull = this.shipData.getHull();
            int shields = this.shipData.getShields();
            int attack = this.shipData.getAttack();

            piece.setProperty("Defense Rating", agility + agilityModifier);
            piece.setProperty("Hull Rating", hull + hullModifier);
            piece.setProperty("Attack Rating", attack + attackModifier);
            piece.setProperty("Shield Rating", shields + shieldsModifier);

            if (this.shipData.getEnergy() > 0) {
                int energy = this.shipData.getEnergy();
                piece.setProperty("Energy Rating", energy + energyModifier);
            }
        }

        if (this.pilotData != null) {
            int ps = this.pilotData.getSkill() + skillModifier;
            piece.setProperty("Pilot Skill", ps);
        }

        setPilotShipName(piece);

        return piece;
    }

    private void setPilotShipName(GamePiece piece) {
        String pilotName = "";
        if (pilotData != null) {
            pilotName = this.pilotData.getName();
            piece.setProperty("Pilot Name", this.pilotData.getShip());
        }

        if (shipNumber != null && shipNumber > 0) {
            pilotName += " " + shipNumber;
        }

        piece.setProperty("Craft ID #", pilotName);

    }

    public static class Upgrade {
        private String xwsName;
        private PieceSlot pieceSlot;
        private MasterUpgradeData.UpgradeData upgradeData;

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

        public MasterUpgradeData.UpgradeData getUpgradeData() {
            return upgradeData;
        }

        public void setUpgradeData(MasterUpgradeData.UpgradeData upgradeData) {
            this.upgradeData = upgradeData;
        }

        public GamePiece cloneGamePiece() {
            return Util.newPiece(pieceSlot);
        }
    }
}
