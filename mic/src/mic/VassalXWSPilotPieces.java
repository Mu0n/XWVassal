package mic;

import VASSAL.build.widget.PieceSlot;
import VASSAL.build.module.PrototypeDefinition;
import VASSAL.build.module.PrototypesContainer;
import VASSAL.counters.GamePiece;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mic.Util.logToChat;

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



    private static Map<String, String> cardboardFiringArcPrototypes = ImmutableMap.<String, String>builder()
            .put("small/rebel/Front","Stem Small Rebel Cardboard Front Arc")
            .put("small/rebel/Turret","Stem Small Rebel Cardboard Turret Arc")
            .put("small/rebel/Auxiliary Rear","Stem Small Rebel Cardboard Aux Rear Arc")
            .put("small/rebel/Auxiliary 180","Stem Small Rebel Cardboard Aux 180 Arc")
            .put("small/rebel/Mobile","Stem Small Rebel Cardboard Mobile Arc")
            .put("small/rebel/Bullseye","Stem Small Rebel Cardboard Bullseye Arc")
            .put("small/imperial/Front","Stem Small Imperial Cardboard Front Arc")
            .put("small/imperial/Turret","Stem Small Imperial Cardboard Turret Arc")
            .put("small/imperial/Auxiliary Rear","Stem Small Imperial Cardboard Aux Rear Arc")
            .put("small/imperial/Auxiliary 180","Stem Small Imperial Cardboard Aux 180 Arc")
            .put("small/imperial/Mobile","Stem Small Imperial Cardboard Mobile Arc")
            .put("small/imperial/Bullseye","Stem Small Imperial Cardboard Bullseye Arc")
            .put("small/scum/Front","Stem Small Scum Cardboard Front Arc")
            .put("small/scum/Turret","Stem Small Scum Cardboard Turret Arc")
            .put("small/scum/Auxiliary Rear","Stem Small Scum Cardboard Aux Rear Arc")
            .put("small/scum/Auxiliary 180","Stem Small Scum Cardboard Aux 180 Arc")
            .put("small/scum/Mobile","Stem Small Scum Cardboard Mobile Arc")
            .put("small/scum/Bullseye","Stem Small Scum Cardboard Bullseye Arc")
            .put("large/rebel/Front","Stem Large Rebel Cardboard Front Arc")
            .put("large/rebel/Turret","Stem Large Rebel Cardboard Turret Arc")
            .put("large/rebel/Auxiliary Rear","Stem Large Rebel Cardboard Aux Rear Arc")
            .put("large/rebel/Auxiliary 180","Stem Large Rebel Cardboard Aux 180 Arc")
            .put("large/rebel/Mobile","Stem Large Rebel Cardboard Mobile Arc")
            .put("large/rebel/Bullseye","Stem Large Rebel Cardboard Bullseye Arc")
            .put("large/imperial/Front","Stem Large Imperial Cardboard Front Arc")
            .put("large/imperial/Turret","Stem Large Imperial Cardboard Turret Arc")
            .put("large/imperial/Auxiliary Rear","Stem Large Imperial Cardboard Aux Rear Arc")
            .put("large/imperial/Auxiliary 180","Stem Large Imperial Cardboard Aux 180 Arc")
            .put("large/imperial/Mobile","Stem Large Imperial Cardboard Mobile Arc")
            .put("large/imperial/Bullseye","Stem Large Imperial Cardboard Bullseye Arc")
            .put("large/scum/Front","Stem Large Scum Cardboard Front Arc")
            .put("large/scum/Turret","Stem Large Scum Cardboard Turret Arc")
            .put("large/scum/Auxiliary Rear","Stem Large Scum Cardboard Aux Rear Arc")
            .put("large/scum/Auxiliary 180","Stem Large Scum Cardboard Aux 180 Arc")
            .put("large/scum/Mobile","Stem Large Scum Cardboard Mobile Arc")
            .put("large/scum/Bullseye","Stem Large Scum Cardboard Bullseye Arc")
            .build();



    private PieceSlot pilotCard;
    private PieceSlot dial;
    private PieceSlot movementCard;
    private List<Upgrade> upgrades = new ArrayList<Upgrade>();
    private List<Condition> conditions = new ArrayList<Condition>();
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

    public List<Condition> getConditions() {
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
/*
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

        if(this.ship.getConfigureName().equals("ship -- Nu Stem Small Ship") ||
                this.ship.getConfigureName().equals("ship -- Nu Stem Large Ship"))
        {
            // this is a stem ship, so we need to configure it

      //      piece = configureStemShip(this.ship.getConfigureName(), this.pilotData.getFaction(), shipData, piece);


        }
        return piece;
    }
*/
    private GamePiece configureStemShip(String slotName, String faction, MasterShipData.ShipData shipData, GamePiece piece)
    {
        StringBuilder prototypePrefixSB = new StringBuilder();
        // find the size
        if(slotName.equals("ship -- Nu Stem Small Ship"))
        {
            prototypePrefixSB.append("small");
        }else if(slotName.equals("ship -- Nu Stem Large Ship"))
        {
            prototypePrefixSB.append("large");
        }

        prototypePrefixSB.append("/");

        // find the faction
        if(faction.equals("Rebel Alliance") || faction.equals("Resistance"))
        {
            prototypePrefixSB.append("rebel");
        }else if(faction.equals("Galactic Empire") ||faction.equals("First Order"))
        {
            prototypePrefixSB.append("imperial");
        }else if(faction.equals("Scum & Villainy"))
        {
            prototypePrefixSB.append("scum");
        }

        prototypePrefixSB.append("/");

        // TODO set the Cardboard Arcs
        for(String arc : shipData.getFiringArcs())
        {
            String prototypeName = cardboardFiringArcPrototypes.get(prototypePrefixSB.toString() + arc);


            // add the prototype
            piece = addPrototypeToPiece(piece, prototypeName);


        }


        // TODO set the actual Firing Arcs

        //TODO Add the cardboard actions
        /*
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

        }*/

        //TODO add the actions
        //TODO add the ship layer
        //TODO add the rotate & pivot

        return piece;

    }

    private GamePiece addPrototypeToPiece(GamePiece piece, String prototypeName)
    {
        logToChat("Adding prototype "+prototypeName+ " to piece");
        PrototypeDefinition protoDef = PrototypesContainer.getPrototype(prototypeName);
        if(protoDef == null)
        {
            logToChat("protoDef is null");
        }
        protoDef.setPiece(piece);
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

    public static class Condition {
        private String xws;
        private String name;
        private PieceSlot pieceSlot;
        private MasterConditionData.ConditionData conditionData;

        public Condition(PieceSlot pieceSlot, String xws, String name)
        {
            this.xws = xws;
            this.pieceSlot = pieceSlot;
            this.name = name;
        }

        public String getXws()
        {
            return this.xws;
        }
        public PieceSlot getPieceSlot()
        {
            return this.pieceSlot;
        }
        public MasterConditionData.ConditionData getConditionData()
        {
            return this.conditionData;
        }

        public void setConditionData(MasterConditionData.ConditionData conditionData)
        {
            this.conditionData = conditionData;
        }
        public GamePiece cloneGamePiece() {
            return Util.newPiece(pieceSlot);
        }
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
