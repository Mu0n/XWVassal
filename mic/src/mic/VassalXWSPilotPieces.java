package mic;

import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.GamePiece;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by amatheny on 2/8/17.
 */
public class VassalXWSPilotPieces {

    private static Map<String, String> actionLayers = ImmutableMap.<String, String>builder()
            .put("","1")
            .put("Focus","2")
            .put("Target Lock","3")
            .put("Boost","4")
            .put("Evade","5")
            .put("Barrel Roll","6")
            .put("Cloak","7")
            .put("SLAM","8")
            .put("Rotate Arc","9")
            .put("Reinforce","10")
            .put("Reload","11")
            .build();

    private PieceSlot pilotCard;
    private PieceSlot dial;
    private PieceSlot movementCard;
    private List<Upgrade> upgrades = new ArrayList<Upgrade>();
    private List<PieceSlot> conditions = new ArrayList<PieceSlot>();
    private PieceSlot movementStrip;
    private PieceSlot openDial;
    private MasterShipData.ShipData shipData;
    private MasterPilotData.PilotData pilotData;
    private Integer shipNumber = null;
    private Map<Tokens, PieceSlot> tokens = Maps.newHashMap();
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

    public List<PieceSlot> getConditions() {
        return this.conditions;
    }

    public PieceSlot getShip() {
        return ship;
    }

    public void setShip(PieceSlot ship) {
        this.ship = ship;
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

    public Map<Tokens, PieceSlot> getTokens() {
        return tokens;
    }

    public List<GamePiece> getTokensForDisplay() {
        List<GamePiece> tokenPieces = Lists.newArrayList();
        for (Tokens token : tokens.keySet()) {
            GamePiece piece = Util.newPiece(tokens.get(token));
            if (token == Tokens.targetlock && pilotData != null) {
                piece.setProperty("ID", getDisplayPilotName());
            }
            tokenPieces.add(piece);
        }
        return tokenPieces;
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

    public Integer getShipNumber()
    {
        if(shipNumber == null)
        {
            return null;
        }
        return shipNumber;
    }

    public void setPilotData(MasterPilotData.PilotData pilotData) {
        this.pilotData = pilotData;
    }

    public GamePiece clonePilotCard() {
        GamePiece piece = Util.newPiece(this.pilotCard);
        if (shipNumber != null && shipNumber > 0) {
            piece.setProperty("Pilot ID #", shipNumber);
        } else {
            piece.setProperty("Pilot ID #", "");
        }

        if(this.pilotCard.getConfigureName().startsWith("Stem"))
        {
            // this is a stem card = fill it in
            piece.setProperty("Ship Type",this.shipData.getName());
            piece.setProperty("Pilot Name",this.pilotData.getName());
        }
        return piece;
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

            MasterUpgradeData.UpgradeGrants doubleSideCardStats = DoubleSideCardPriorityPicker.getDoubleSideCardStats(upgrade.getXwsName());
            ArrayList<MasterUpgradeData.UpgradeGrants> grants = new ArrayList<MasterUpgradeData.UpgradeGrants>();
            if (doubleSideCardStats != null) {
                grants.add(doubleSideCardStats);
            } else {
                grants.addAll(upgrade.getUpgradeData().getGrants());
            }

            for (MasterUpgradeData.UpgradeGrants modifier : grants) {
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

            if (this.pilotData != null && this.pilotData.getShipOverrides() != null) {
                MasterPilotData.ShipOverrides shipOverrides = this.pilotData.getShipOverrides();
                agility = shipOverrides.getAgility();
                hull = shipOverrides.getHull();
                shields = shipOverrides.getShields();
                attack = shipOverrides.getAttack();
            }

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

        if(this.ship.getConfigureName().equals("ship -- Small Stem Ship") || this.ship.getConfigureName().equals("ship -- Large Stem Ship"))
        {
            // this is a stem ship, so we need to set the arcs and actions

            // find the faction
            // set the correct arc for the faction
            String factionInt = "1";
            if(this.pilotData.getFaction().equals("Rebel Alliance") || this.pilotData.getFaction().equals("Resistance"))
            {
                factionInt = "2";
            }else if(this.pilotData.getFaction().equals("Galactic Empire") || this.pilotData.getFaction().equals("First Order"))
            {
                factionInt = "3";
            }else if(this.pilotData.getFaction().equals("Scum & Villainy"))
            {
                factionInt = "4";
            }


            // arcs
            for(String arc : shipData.getFiringArcs())
            {
                Util.logToChat("Setting arc "+arc);
                if(arc.equals("Front"))
                {
                    Util.logToChat(arc + " setting was "+piece.getProperty("Front Arc"));
                    Util.logToChat("Setting to "+factionInt);
                    piece.setProperty("Front Arc",factionInt);
                    Util.logToChat(arc + " new setting is "+piece.getProperty("Front Arc"));
                }else if(arc.equals("Turret"))
                {
                    Util.logToChat(arc + " setting was "+piece.getProperty("Turret Arc"));
                    Util.logToChat("Setting to "+factionInt);
                    piece.setProperty("Turret Arc",factionInt);
                    Util.logToChat(arc + " new setting is "+piece.getProperty("Turret Arc"));
                }else if(arc.equals("Auxiliary Rear"))
                {
                    Util.logToChat(arc + " setting was "+piece.getProperty("Aux Rear Arc"));
                    Util.logToChat("Setting to "+factionInt);
                    piece.setProperty("Aux Rear Arc",factionInt);
                    Util.logToChat(arc + " new setting is "+piece.getProperty("Aux Rear Arc"));
                }else if(arc.equals("Auxiliary 180"))
                {
                    Util.logToChat(arc + " setting was "+piece.getProperty("Aux 180 Arc"));
                    Util.logToChat("Setting to "+factionInt);
                    piece.setProperty("Aux 180 Arc",factionInt);
                    Util.logToChat(arc + " new setting is "+piece.getProperty("Aux 180 Arc"));
                }else if(arc.equals("Mobile"))
                {
                    Util.logToChat(arc + " setting was "+piece.getProperty("Mobile Arc"));
                    Util.logToChat("Setting to "+factionInt);
                    piece.setProperty("Mobile Arc",factionInt);
                    Util.logToChat(arc + " new setting is "+piece.getProperty("Mobile Arc"));
                }else if(arc.equals("Bullseye"))
                {
                    Util.logToChat(arc + " setting was "+piece.getProperty("Bullseye Arc"));
                    Util.logToChat("Setting to "+factionInt);
                    piece.setProperty("Bullseye Arc",factionInt);
                    Util.logToChat(arc + " new setting is "+piece.getProperty("Bullseye Arc"));
                }


            }

            //actions
            int actionCounter = 0;
            for(String action : shipData.getActions())
            {
                actionCounter++;
                String layerNumber = actionLayers.get(action);
                if(layerNumber != null)
                {
                    switch(actionCounter){
                        case 1:
                            piece.setProperty("ActionLayer1",layerNumber);
                            break;
                        case 2:
                            piece.setProperty("ActionLayer2",layerNumber);
                            break;
                        case 3:
                            piece.setProperty("ActionLayer3",layerNumber);
                            break;
                        case 4:
                            piece.setProperty("ActionLayer4",layerNumber);
                            break;
                        case 5:
                            piece.setProperty("ActionLayer5",layerNumber);
                            break;
                    }
                }
            }
        }
        return piece;
    }

    private void setPilotShipName(GamePiece piece) {
        if (pilotData != null) {
            piece.setProperty("Pilot Name", getDisplayShipName());
        }
        piece.setProperty("Craft ID #", getDisplayPilotName());
    }

    private String getDisplayPilotName() {
        String pilotName = "";
        if (pilotData != null) {
            pilotName = Acronymizer.acronymizer(
                    this.pilotData.getName(),
                    this.pilotData.isUnique(),
                    this.shipData.hasSmallBase());
        }

        if (shipNumber != null && shipNumber > 0) {
            pilotName += " " + shipNumber;
        }
        return pilotName;
    }

    private String getDisplayShipName() {
        String shipName = "";
        if (pilotData != null) {
            shipName = Acronymizer.acronymizer(
                    this.pilotData.getShip(),
                    this.pilotData.isUnique(),
                    this.shipData.hasSmallBase());
        }

        return shipName;
    }

    public MasterShipData.ShipData getShipData() {
        return shipData;
    }

    public MasterPilotData.PilotData getPilotData() {
        return pilotData;
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
