package mic;

import VASSAL.build.GameModule;
import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.GamePiece;

import java.util.Iterator;
import java.util.List;

/*
 * This class dynamically generates GamePieces during AutoSquadSpawn
 */
public class GamePieceGenerator
{
    private static final String SMALL_STEM_SHIP_SLOT_NAME = "ship -- Nu Stem Small Ship";
    private static final String LARGE_STEM_SHIP_SLOT_NAME = "ship -- Nu Stem Large Ship";
    private static final String SHIP_BASE_SIZE_SMALL = "small";
    private static final String SHIP_BASE_SIZE_LARGE = "large";

    // generate a ship GamePiece
    public static GamePiece generateShip(VassalXWSPilotPieces ship)
    {
        MasterPilotData.PilotData pilotData = ship.getPilotData();
        // get the master data for the ship
        MasterShipData.ShipData shipData = ship.getShipData();
        String faction = ship.getPilotData().getFaction();

        // generate the piece from the stem ships
        GamePiece newShip = null;
        if(shipData.getSize().contentEquals(SHIP_BASE_SIZE_SMALL))
        {
            newShip = mic.Util.newPiece(getPieceSlotByName(SMALL_STEM_SHIP_SLOT_NAME));
        }else if(shipData.getSize().contentEquals(SHIP_BASE_SIZE_LARGE))
        {
            newShip = mic.Util.newPiece(getPieceSlotByName(LARGE_STEM_SHIP_SLOT_NAME));
        }

        // execute the command to build the ship piece
        StemShip.ShipGenerateCommand myShipGen = new StemShip.ShipGenerateCommand(ship.getShipData().getXws(), newShip, faction, pilotData.getXws());

        myShipGen.execute();

        // add the stats to the piece
        newShip.setProperty("Craft ID #", getDisplayPilotName(ship.getPilotData(),shipData,ship.getShipNumber())); //is actually the pilot name
        newShip.setProperty("Pilot Skill", Integer.toString(ship.getPilotData().getSkill()));

        newShip.setProperty("Pilot Name", getDisplayShipName(ship.getPilotData(),shipData)); //is actually the ship name
        newShip.setProperty("Attack Rating", Integer.toString(ship.getShipData().getAttack()));
        newShip.setProperty("Defense Rating", Integer.toString(ship.getShipData().getAgility()));
        newShip.setProperty("Hull Rating", Integer.toString(ship.getShipData().getHull()));
        newShip.setProperty("Shield Rating", Integer.toString(ship.getShipData().getShields()));

        return newShip;
    }

    private static PieceSlot getPieceSlotByName(String name)
    {

        List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);
        PieceSlot targetPieceSlot = null;
        boolean found = false;

        PieceSlot pieceSlot = null;
        Iterator<PieceSlot> slotIterator = pieceSlots.iterator();
        while(slotIterator.hasNext() && !found)
        {
            pieceSlot = slotIterator.next();

            if (pieceSlot.getConfigureName().startsWith(name)) {
                targetPieceSlot = pieceSlot;
                found = true;
            }
        }
        return targetPieceSlot;
    }

    public static GamePiece generateDial(VassalXWSPilotPieces ship)
    {

        MasterShipData.ShipData shipData = ship.getShipData();

        String faction = ship.getPilotData().getFaction();

        PieceSlot rebelDialSlot = null;
        PieceSlot imperialDialSlot = null;
        PieceSlot scumDialSlot = null;

        // find the 3 slots for the auto-gen dials
        List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

        for (PieceSlot pieceSlot : pieceSlots) {
            String slotName = pieceSlot.getConfigureName();
            if (slotName.startsWith("Rebel Stem Dial") && rebelDialSlot == null) {
                rebelDialSlot = pieceSlot;
                continue;
            } else if (slotName.startsWith("Imperial Stem Dial") && imperialDialSlot == null) {
                imperialDialSlot = pieceSlot;
                continue;
            } else if (slotName.startsWith("Scum Stem Dial") && scumDialSlot == null) {
                scumDialSlot = pieceSlot;
                continue;
            }
        }

        // grab the correct dial for the faction
        GamePiece dial = null;
        if(faction.contentEquals("Rebel Alliance") || faction.contentEquals("Resistance")) {
            dial = mic.Util.newPiece(rebelDialSlot);
        }else if(faction.contentEquals("Galactic Empire") || faction.contentEquals("First Order")) {
            dial = mic.Util.newPiece(imperialDialSlot);
        }else if(faction.contentEquals("Scum and Villainy")) {
            dial = mic.Util.newPiece(scumDialSlot);
        }

        // execute the command
        StemDial.DialGenerateCommand myDialGen = new StemDial.DialGenerateCommand(ship.getShipData().getXws(), dial, faction);

        myDialGen.execute();

        dial.setProperty("ShipXwsId",ship.getShipData().getXws());
        dial.setProperty("Pilot Name", getDisplayShipName(ship.getPilotData(),shipData));
        dial.setProperty("Craft ID #", getDisplayPilotName(ship.getPilotData(),shipData,ship.getShipNumber()));
        return dial;
    }

    public static GamePiece generateUpgrade(VassalXWSPilotPieces.Upgrade upgrade)
    {

        GamePiece newUpgrade = mic.Util.newPiece(upgrade.getPieceSlot());
        StemUpgrade.UpgradeGenerateCommand myUpgradeGen = new StemUpgrade.UpgradeGenerateCommand(upgrade.getUpgradeData().getXws(), newUpgrade, upgrade.getUpgradeData().getName(), upgrade.getUpgradeData().getSlot());

        myUpgradeGen.execute();

        return newUpgrade;
    }

    public static GamePiece generateCondition(VassalXWSPilotPieces.Condition condition)
    {

        GamePiece newCondition = mic.Util.newPiece(condition.getPieceSlot());

        // get the pieceslot for the StemConditionToken
        List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);
        PieceSlot stemConditionTokenPieceSlot = null;
        for (PieceSlot pieceSlot : pieceSlots)
        {
            String slotName = pieceSlot.getConfigureName();
            if(slotName.equals("Stem Condition Token")) {

                stemConditionTokenPieceSlot = pieceSlot;
                break;
            }

        }


        // get a copy of the stem token game piece
        GamePiece conditionTokenPiece = mic.Util.newPiece(stemConditionTokenPieceSlot);

  //      if(conditionTokenPiece != null)
 //       {
  //          Util.logToChat("Token piece ID: "+conditionTokenPiece.getId());
   //     }else{
   //         Util.logToChat("conditionTokenPiece is null");
  //      }


        // build the token
        // generate a new ID
        int randomInt = (int) Math.ceil(Math.random() * 10000);
        conditionTokenPiece.setId("XWVassalConditionToken"+String.valueOf(randomInt));
   //     Util.logToChat("Token piece ID inside StemConditionToken: "+conditionTokenPiece.getId());
     //   StemConditionToken.TokenGenerateCommand myTokenGen = new StemConditionToken.TokenGenerateCommand(condition.getConditionData().getXws(), conditionTokenPiece);

 //       myTokenGen.execute();

        // build the condition card
        StemCondition.ConditionGenerateCommand myConditionGen = new StemCondition.ConditionGenerateCommand(condition.getConditionData().getXws(), newCondition, condition.getConditionData().getName(), conditionTokenPiece);

        myConditionGen.execute();

        return newCondition;
    }

    public static GamePiece generatePilot(VassalXWSPilotPieces ship) {

        GamePiece newPilot = mic.Util.newPiece(ship.getPilotCard());
        if (ship.getShipNumber() != null && ship.getShipNumber() > 0) {
            newPilot.setProperty("Pilot ID #", ship.getShipNumber());
        } else {
            newPilot.setProperty("Pilot ID #", "");
        }

        // this is a stem card = fill it in

        MasterShipData.ShipData shipData = ship.getShipData();
        MasterPilotData.PilotData pilotData = ship.getPilotData();
        //    newPilot.setProperty("Ship Type",shipData.getName());
        //    newPilot.setProperty("Pilot Name",pilotData.getName());

        StemPilot.PilotGenerateCommand myShipGen = new StemPilot.PilotGenerateCommand(pilotData.getXws(), newPilot, pilotData.getFaction(), shipData.getXws(), pilotData.getName(), shipData.getName());

        myShipGen.execute();

        return newPilot;
    }

    private static String getDisplayPilotName(MasterPilotData.PilotData pilotData, MasterShipData.ShipData shipData, Integer shipNumber )
    {
        String pilotName = "";
        if (pilotData != null) {
            pilotName = Acronymizer.acronymizer(
                    pilotData.getName(),
                    pilotData.isUnique(),
                    shipData.hasSmallBase());
        }

        if (shipNumber != null && shipNumber > 0) {
            pilotName += " " + shipNumber;
        }
        return pilotName;
    }

    private static String getDisplayShipName(MasterPilotData.PilotData pilotData, MasterShipData.ShipData shipData) {
        String shipName = "";
        if (pilotData != null) {
            shipName = Acronymizer.acronymizer(
                    pilotData.getShip(),
                    pilotData.isUnique(),
                    shipData.hasSmallBase());
        }

        return shipName;
    }
}
