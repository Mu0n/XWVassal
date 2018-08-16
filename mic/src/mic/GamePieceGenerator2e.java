package mic;

import VASSAL.build.GameModule;
import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.GamePiece;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static mic.Util.logToChat;

/*
 * created by mjuneau on 6/8/18
 * This class dynamically generates GamePieces during AutoSquadSpawn
 */
public class GamePieceGenerator2e
{
    private static final String SMALL_STEM_SHIP_SLOT_NAME = "ship -- Stem2e Small Ship";
    private static final String MEDIUM_STEM_SHIP_SLOT_NAME = "ship -- Stem2e Medium Ship";
    private static final String LARGE_STEM_SHIP_SLOT_NAME = "ship -- Stem2e Large Ship";

    private static final String SMALL_STEM_SHIP_SINGLE_TURRET_SLOT_NAME = "ship -- Stem2e Small Single Turret Ship";
    private static final String MEDIUM_STEM_SHIP_SINGLE_TURRET_SLOT_NAME = "ship -- Stem2e Medium Single Turret Ship";
    private static final String LARGE_STEM_SHIP_SINGLE_TURRET_SLOT_NAME = "ship -- Stem2e Large Single Turret Ship";

    private static final String MEDIUM_STEM_SHIP_DOUBLE_TURRET_SLOT_NAME = "ship -- Stem2e Medium Double Turret Ship";
    private static final String LARGE_STEM_SHIP_DOUBLE_TURRET_SLOT_NAME = "ship -- Stem2e Large Double Turret Ship";

    private static final String SHIP_BASE_SIZE_SMALL = "Small";
    private static final String SHIP_BASE_SIZE_MEDIUM = "Medium";
    private static final String SHIP_BASE_SIZE_LARGE = "Large";

    // generate a ship GamePiece
    public static GamePiece generateShip(VassalXWSPilotPieces2e ship)
    {
                // generate the piece from the stem ships
        GamePiece newShip = null;

        //single and double turret arc have to be checked first because they are more involved pieces
       boolean shipContainsSingleTurret = containsSingleTurret(ship);
       boolean shipContainsDoubleTurret = containsDoubleTurret(ship);

       if(ship.getShipData().getSize().contentEquals(SHIP_BASE_SIZE_SMALL))
       {
           if(shipContainsSingleTurret) newShip = Util.newPiece(getPieceSlotByName(SMALL_STEM_SHIP_SINGLE_TURRET_SLOT_NAME));
           else newShip = Util.newPiece(getPieceSlotByName(SMALL_STEM_SHIP_SLOT_NAME));
        }
        else if(ship.getShipData().getSize().contentEquals(SHIP_BASE_SIZE_MEDIUM))
        {
            if(shipContainsSingleTurret) newShip = Util.newPiece(getPieceSlotByName(MEDIUM_STEM_SHIP_SINGLE_TURRET_SLOT_NAME));
            else if(shipContainsDoubleTurret) newShip = Util.newPiece(getPieceSlotByName(MEDIUM_STEM_SHIP_DOUBLE_TURRET_SLOT_NAME));
            else newShip = Util.newPiece(getPieceSlotByName(MEDIUM_STEM_SHIP_SLOT_NAME));
        }
        else if(ship.getShipData().getSize().contentEquals(SHIP_BASE_SIZE_LARGE))
        {
            if(shipContainsSingleTurret) newShip = Util.newPiece(getPieceSlotByName(LARGE_STEM_SHIP_SINGLE_TURRET_SLOT_NAME));
            else if(shipContainsDoubleTurret) newShip = Util.newPiece(getPieceSlotByName(LARGE_STEM_SHIP_DOUBLE_TURRET_SLOT_NAME));
            else newShip = Util.newPiece(getPieceSlotByName(LARGE_STEM_SHIP_SLOT_NAME));
        }

        // determine if the ship needs bomb drop
        //boolean needsBombCapability = determineIfShipNeedsBombCapability(ship, allShips);

        // execute the command to build the ship piece
        /*
        ShipGenerateCommand(String shipXws,   GamePiece piece, String faction, String xwsPilot,
        boolean needsBombCapability, Boolean hasDualBase,
            String dualBaseToggleMenuText, String base1ReportIdentifier, String base2ReportIdentifier) {
        */
        StemShip2e.ShipGenerateCommand myShipGen = new StemShip2e.ShipGenerateCommand(
                ship, ship.getShipData().getName(),
                newShip, ship.getShipData().getFaction(), ship.getPilotData().getXWS(),true,
                false, "","","");
        myShipGen.execute();

        // add the stats to the piece
        newShip = setShipProperties(newShip,ship);
        return newShip;
    }

    public static GamePiece setShipProperties(GamePiece piece,VassalXWSPilotPieces2e ship ) {
        //GamePiece piece = Util.newPiece(this.ship);
        int initiativeModifier = 0;
        int chargeModifier = 0;
        int shieldsModifier = 0;
        int hullModifier = 0;
        int forceModifier = 0;
/*
        for (VassalXWSPilotPieces2e.Upgrade upgrade : upgrades) {

            MasterUpgradeData.UpgradeGrants doubleSideCardStats = DoubleSideCardPriorityPicker.getDoubleSideCardStats(upgrade.getXwsName());
            ArrayList<MasterUpgradeData.UpgradeGrants> grants = new ArrayList<MasterUpgradeData.UpgradeGrants>();
            if(grants!=null)
            {
                if (doubleSideCardStats != null) {
                    grants.add(doubleSideCardStats);
                } else {
                    ArrayList<MasterUpgradeData.UpgradeGrants> newGrants = new ArrayList<MasterUpgradeData.UpgradeGrants>();
                    try{
                        newGrants = upgrade.getUpgradeData().getGrants();
                    }catch(Exception e){

                    }
                    if(newGrants !=null) grants.addAll(newGrants);
                }
            }


            for (MasterUpgradeData.UpgradeGrants modifier : grants) {
                if (modifier.isStatsModifier()) {
                    String name = modifier.getName();
                    int value = modifier.getValue();

                    if (name.equals("hull")) hullModifier += value;
                    else if (name.equals("shields")) shieldsModifier += value;
                    else if (name.equals("initiative")) initiativeModifier += value;
                    else if (name.equals("force")) forceModifier += value;
                    else if (name.equals("charge")) chargeModifier += value;
                }
            }
        }*/

        if (ship.getShipData() != null)
        {
            int hull = ship.getShipData().getHull();
            int shields = ship.getShipData().getShields();
            int initiative = ship.getPilotData().getInitiative();

            //TO DO overrides, ugh
            /*
            if (pilotData != null && pilotData.getShipOverrides() != null)
            {
                MasterPilotData.ShipOverrides shipOverrides = pilotData.getShipOverrides();
                hull = shipOverrides.getHull();
                shields = shipOverrides.getShields();
            }
            */
            piece.setProperty("Initiative", initiative + initiativeModifier);
            piece.setProperty("Hull Rating", hull + hullModifier);
            piece.setProperty("Shield Rating", shields + shieldsModifier);


            if (ship.getPilotData().getChargeData().getValue() > 0) {
                int charge = ship.getPilotData().getChargeData().getValue();
                piece.setProperty("Charge Rating", charge + chargeModifier);
            }
            if (ship.getPilotData().getForceData().getValue() > 0) {
                int force = ship.getPilotData().getForceData().getValue();
                piece.setProperty("Force Rating", force + forceModifier);
            }
        }

        if (ship.getShipData().getName() != null) {
            piece.setProperty("Pilot Name", getDisplayPilotName(ship, ship.getShipNumber()));
        }

        return piece;
    }



    private static boolean containsSingleTurret(VassalXWSPilotPieces2e ship){
        boolean foundSingleTurret = false;
        for(XWS2Pilots.Stat2e stat : ship.getShipData().getStats()){
            if(stat.getArc().equals("Single Turret Arc")) foundSingleTurret = true;
        }
        //TODO add code for single turret granted from upgrade card
        return foundSingleTurret;
    }
    private static boolean containsDoubleTurret(VassalXWSPilotPieces2e ship) {
        boolean foundDoubleTurret = false;
        for(XWS2Pilots.Stat2e stat : ship.getShipData().getStats()){
            if(stat.getArc().equals("Double Turret Arc")) foundDoubleTurret = true;
        }
        //TODO add code for single turret granted from upgrade card
        return foundDoubleTurret;
    }

    //TO DO not sure where the bomb information will be polled from
    private static boolean determineIfShipNeedsBombCapability(VassalXWSPilotPieces2e ship, List<XWS2Pilots> allPilots)
    {
        boolean needsBomb = false;
        // if the pilot has a bomb slot
        /*
        MasterPilotData.PilotData pilotData = ship.getPilotData();
        List<String> slots = pilotData.getSlots();
        Iterator<String> slotIterator = slots.iterator();
        String slotName = null;
        while(slotIterator.hasNext() && !needsBomb)
        {
            slotName = slotIterator.next();
            if(slotName.equalsIgnoreCase("Bomb"))
            {
                needsBomb = true;
            }
        }

        // if an upgrade has a grant of bomb slot
        if(!needsBomb)
        {
            List<VassalXWSPilotPieces2e.Upgrade> upgrades = ship.getUpgrades();
            Iterator<VassalXWSPilotPieces2e.Upgrade> upgradeIterator = upgrades.iterator();
            VassalXWSPilotPieces2e.Upgrade upgrade = null;
            Iterator<MasterUpgradeData.UpgradeGrants> grantIterator = null;
            while(upgradeIterator.hasNext() && !needsBomb)
            {
                upgrade = upgradeIterator.next();
                ArrayList<MasterUpgradeData.UpgradeGrants> upgradeGrants;
                try {
                    upgradeGrants = upgrade.getUpgradeData().getGrants();
                }catch(Exception e){
                    logToChat("found the grants null exception.");
                    return false;
                }
                grantIterator = upgradeGrants.iterator();
                MasterUpgradeData.UpgradeGrants grant = null;
                while(grantIterator.hasNext() && !needsBomb)
                {
                    grant = grantIterator.next();
                    if(grant.getType().equalsIgnoreCase("slot") && grant.getName().equalsIgnoreCase("Bomb"))
                    {
                        needsBomb = true;
                    }
                }
            }
        }
*/
        return needsBomb;
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

    public static GamePiece generateDial(VassalXWSPilotPieces2e ship)
    {
        PieceSlot rebelDialSlot = null;
        PieceSlot imperialDialSlot = null;
        PieceSlot scumDialSlot = null;
        PieceSlot firstOrderDialSlot = null;
        PieceSlot resistanceDialSlot = null;
        PieceSlot cisDialSlot = null;
        PieceSlot republicDialSlot = null;

        // find the 3 slots for the auto-gen dials
        List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

        for (PieceSlot pieceSlot : pieceSlots) {
            String slotName = pieceSlot.getConfigureName();
            if (slotName.startsWith("Rebel Stem2e Dial") && rebelDialSlot == null) {
                rebelDialSlot = pieceSlot;
                continue;
            } else if (slotName.startsWith("Imperial Stem2e Dial") && imperialDialSlot == null) {
                imperialDialSlot = pieceSlot;
                continue;
            } else if (slotName.startsWith("Scum Stem2e Dial") && scumDialSlot == null) {
                scumDialSlot = pieceSlot;
                continue;
            } else if (slotName.startsWith("Resistance Stem2e Dial") && resistanceDialSlot == null) {
                resistanceDialSlot = pieceSlot;
                continue;
            } else if (slotName.startsWith("FirstOrder Stem2e Dial") && firstOrderDialSlot == null) {
                firstOrderDialSlot = pieceSlot;
                continue;
            }else if (slotName.startsWith("Republic Stem2e Dial") && republicDialSlot == null) {
                republicDialSlot = pieceSlot;
                continue;
            } else if (slotName.startsWith("CIS Stem2e Dial") && cisDialSlot == null) {
                cisDialSlot = pieceSlot;
                continue;
            }
        }

        String faction = ship.getShipData().getFaction();
        // grab the correct dial for the faction
        GamePiece dial = null;
        if(faction.contentEquals("Rebel Alliance")) {
            dial = Util.newPiece(rebelDialSlot);
        }else if(faction.contentEquals("Resistance")){
            dial = Util.newPiece(resistanceDialSlot);
        } if(faction.contentEquals("Galactic Empire")) {
            dial = Util.newPiece(imperialDialSlot);
        }else if(faction.contentEquals("First Order")){
        dial = Util.newPiece(firstOrderDialSlot);
        }else if(faction.contentEquals("Scum and Villainy")) {
            dial = Util.newPiece(scumDialSlot);
        }else if(faction.contentEquals("Galactic Republic")){
        dial = Util.newPiece(republicDialSlot);
    }else if(faction.contentEquals("CIS")) {
        dial = Util.newPiece(cisDialSlot);
    }


        // execute the command
        StemDial2e.DialGenerateCommand myDialGen = new StemDial2e.DialGenerateCommand(ship.getShipData().getDial(), ship.getShipData().getName(), dial, faction);

        myDialGen.execute();

        //is this even needed
        //dial.setProperty("ShipXwsId",Canonicalizer.getCleanedName(shipTag));
        dial.setProperty("Pilot Name", getDisplayShipName(ship));
        dial.setProperty("Craft ID #", getDisplayPilotName(ship, ship.getShipNumber()));

        return dial;
    }

    public static GamePiece generateUpgrade(VassalXWSPilotPieces2e.Upgrade upgrade)
    {

        GamePiece newUpgrade = Util.newPiece(upgrade.getPieceSlot());
        boolean isDualSided = (upgrade.getUpgradeData().sides.size() == 2);
        StemUpgrade2e.UpgradeGenerateCommand myUpgradeGen = new StemUpgrade2e.UpgradeGenerateCommand(newUpgrade, upgrade, isDualSided);

        myUpgradeGen.execute();

        return newUpgrade;
    }

    public static GamePiece generateCondition(VassalXWSPilotPieces2e.Condition condition)
    {

        GamePiece newCondition = Util.newPiece(condition.getPieceSlot());

        // build the condition card
        StemCondition.ConditionGenerateCommand myConditionGen = new StemCondition.ConditionGenerateCommand(condition.getConditionData().getXws(), newCondition, condition.getConditionData().getName());
        myConditionGen.execute();

        return newCondition;
    }

    public static GamePiece generateConditionToken(VassalXWSPilotPieces2e.Condition condition)
    {
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
        GamePiece conditionTokenPiece = Util.newPiece(stemConditionTokenPieceSlot);



        // build the condition card
        StemConditionToken.TokenGenerateCommand myTokenGen = new StemConditionToken.TokenGenerateCommand(condition.getConditionData().getXws(), conditionTokenPiece);
        myTokenGen.execute();

        return conditionTokenPiece;
    }

    public static GamePiece generatePilot(VassalXWSPilotPieces2e ship) {

        GamePiece newPilot = Util.newPiece(ship.getPilotCard());
        /*
        if (ship.getShipNumber() != null && ship.getShipNumber() > 0) {
            newPilot.setProperty("Pilot ID #", ship.getShipNumber());
        } else {
            newPilot.setProperty("Pilot ID #", "");
        }

        // this is a stem card = fill it in
        newPilot.setProperty("Ship Type",ship.getShipData().getName());
        newPilot.setProperty("Pilot Name",ship.getPilotData().getName());
*/
        StemPilot2e.PilotGenerateCommand myShipGen = new StemPilot2e.PilotGenerateCommand(newPilot, ship);

        myShipGen.execute();

        return newPilot;
    }

    private static String getDisplayPilotName(VassalXWSPilotPieces2e ship, Integer shipNumber )
    {
        String pilotName = "";

        if (ship.getPilotData() != null) {
            pilotName = Acronymizer.acronymizer(
                    ship.getPilotData().getName(),
                    ship.getPilotData().isUnique(),
                    ship.getShipData().hasSmallBase());
        }

        if (shipNumber != null && shipNumber > 0) {
            pilotName += " " + shipNumber;
        }
        return pilotName;
    }

    private static String getDisplayShipName(VassalXWSPilotPieces2e ship) {
        String shipName = "";

        if (ship.getPilotData() != null) {
            shipName = Acronymizer.acronymizer(
                    ship.getShipData().getName(),
                    ship.getPilotData().isUnique(),
                    ship.getShipData().hasSmallBase());
        }

        return shipName;
    }
}
