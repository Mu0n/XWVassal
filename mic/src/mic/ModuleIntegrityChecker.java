package mic;


import mic.ota.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Mic on 03/11/2018.
 *
 * Might only be temporary. Checks:
 * -if every xwing-data pilot or upgrade has a correspondant local image
 * -if every xwing-data ship has a ship token prepared, as well as dial
 */

public class ModuleIntegrityChecker {
 //   private static String REMOTE_URL = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/pilots.js";

    public String testString = "";
/*
    private ArrayList<String> pilotsRebelAlliance, pilotsRebelAllianceMissing,
            pilotsResistance, pilotsResistanceMissing,
            pilotsGalacticEmpire, pilotsGalacticEmpireMissing,
            pilotsFirstOrder, pilotsFirstOrderMissing,
            pilotsScumAndVillainy, getPilotsScumAndVillainyMissing,
            upgrades, upgradesMissing,
            dialsRebels, dialsEmpire, dialsScum;

    public ModuleIntegrityChecker() {
        pilotsRebelAlliance = new ArrayList<String>();
        pilotsRebelAllianceMissing = new ArrayList<String>();
        pilotsResistance = new ArrayList<String>();
        pilotsResistanceMissing = new ArrayList<String>();
        pilotsGalacticEmpire = new ArrayList<String>();
        pilotsGalacticEmpireMissing = new ArrayList<String>();
        pilotsFirstOrder = new ArrayList<String>();
        pilotsFirstOrderMissing = new ArrayList<String>();
        pilotsScumAndVillainy = new ArrayList<String>();
        getPilotsScumAndVillainyMissing = new ArrayList<String>();
        upgrades = new ArrayList<String>();
        upgradesMissing = new ArrayList<String>();
        dialsRebels = new ArrayList<String>();
        dialsEmpire = new ArrayList<String>();
        dialsScum = new ArrayList<String>();

        checkAll();
    }
    */

    public ArrayList<OTAMasterUpgrades.OTAUpgrade> checkUpgrades()
    {
        // get list of upgrades from OTAMasterUpgrades
        OTAMasterUpgrades omu = new OTAMasterUpgrades();
        Collection<OTAMasterUpgrades.OTAUpgrade> upgrades = omu.getAllUpgrades();


        ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeList = new ArrayList<OTAMasterUpgrades.OTAUpgrade>();
        Iterator<OTAMasterUpgrades.OTAUpgrade> i = upgrades.iterator();

        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        while(i.hasNext())
        {
            upgrade = (OTAMasterUpgrades.OTAUpgrade)i.next();

            upgrade.setStatus(XWImageUtils.imageExistsInModule(upgrade.getImage()));

            upgradeList.add(upgrade);

        }

        return upgradeList;
    }

    public ArrayList<OTAMasterConditions.OTACondition> checkConditions()
    {
        // get list of conditions from OTAMasterUpgrades
        OTAMasterConditions omc = new OTAMasterConditions();
        Collection<OTAMasterConditions.OTACondition> conditions = omc.getAllConditions();


        ArrayList<OTAMasterConditions.OTACondition> conditionList = new ArrayList<OTAMasterConditions.OTACondition>();
        Iterator<OTAMasterConditions.OTACondition> i = conditions.iterator();

        OTAMasterConditions.OTACondition condition = null;
        while(i.hasNext())
        {
            condition = (OTAMasterConditions.OTACondition)i.next();

            condition.setStatus(XWImageUtils.imageExistsInModule(condition.getImage()));
            condition.setTokenStatus(XWImageUtils.imageExistsInModule(condition.getTokenImage()));

            conditionList.add(condition);

        }

        return conditionList;
    }

public ArrayList<OTAMasterPilots.OTAPilot> checkPilots()
{
    // get list of pilots from OTAMasterPilots
    OTAMasterPilots omp = new OTAMasterPilots();
    Collection<OTAMasterPilots.OTAPilot> pilots = omp.getAllPilots();


    ArrayList<OTAMasterPilots.OTAPilot> pilotList = new ArrayList<OTAMasterPilots.OTAPilot>();
    Iterator<OTAMasterPilots.OTAPilot> i = pilots.iterator();

    while(i.hasNext())
    {
        OTAMasterPilots.OTAPilot pilot = (OTAMasterPilots.OTAPilot)i.next();

        pilot.setStatus(XWImageUtils.imageExistsInModule(pilot.getImage()));

        pilotList.add(pilot);

    }

    return pilotList;
}

    public ArrayList<OTAMasterShips.OTAShip> checkShips()
    {
        // get list of ships from OTAMasterShips
        OTAMasterShips oms = new OTAMasterShips();
        Collection<OTAMasterShips.OTAShip> ships = oms.getAllShips();


        ArrayList<OTAMasterShips.OTAShip> shipList = new ArrayList<OTAMasterShips.OTAShip>();
        Iterator<OTAMasterShips.OTAShip> i = ships.iterator();

        while(i.hasNext())
        {
            OTAMasterShips.OTAShip ship = (OTAMasterShips.OTAShip)i.next();
            ship.setStatus( XWImageUtils.imageExistsInModule(ship.getImage()));
            boolean exists = XWImageUtils.imageExistsInModule(ship.getImage());

            shipList.add(ship);
        }

        return shipList;

    }

    public ArrayList<OTAShipBase> checkShipBases()
    {
        OTAMasterShips oms = new OTAMasterShips();
        Collection<OTAMasterShips.OTAShip> ships = oms.getAllShips();

          MasterShipData msd = new MasterShipData();
          msd.loadData();

        ArrayList<OTAShipBase> shipList = new ArrayList<OTAShipBase>();
        Iterator<OTAMasterShips.OTAShip> i1 = ships.iterator();

        while(i1.hasNext())
        {
            OTAMasterShips.OTAShip ship = (OTAMasterShips.OTAShip)i1.next();

            // check to see which factions to generate this ship base for
            List<String> factions = ship.getFactions();

            Iterator<String> i2 = factions.iterator();
            while(i2.hasNext())
            {

                String factionName = i2.next();

                String shipBaseImageName = XWImageUtils.buildShipBaseImageName(factionName,ship.getXws(),ship.getIdentifier());


                OTAShipBase shipBase = new OTAShipBase();
                shipBase.setFaction(factionName);
                shipBase.setIdentifier(ship.getIdentifier());
                shipBase.setShipBaseImageName(shipBaseImageName);
                shipBase.setshipImageName(ship.getImage());
                shipBase.setShipName(msd.getShipData(ship.getXws()).getName());
                shipBase.setShipXws(ship.getXws());
                shipBase.setStatus(XWImageUtils.imageExistsInModule(shipBaseImageName));

                shipList.add(shipBase);

            }


        }

        return shipList;

    }

    public ArrayList<OTAMasterActions.OTAAction> checkActions()
    {

        // get list of Actions from OTAMasterActions
        OTAMasterActions oma = new OTAMasterActions();
        Collection<OTAMasterActions.OTAAction> actions = oma.getAllActions();

        ArrayList<OTAMasterActions.OTAAction> actionList = new ArrayList<OTAMasterActions.OTAAction>();
        Iterator<OTAMasterActions.OTAAction> i = actions.iterator();
        OTAMasterActions.OTAAction action = null;
        while(i.hasNext())
        {

            action = i.next();

            action.setStatus(XWImageUtils.imageExistsInModule(action.getImage()));

            actionList.add(action);

        }



        return actionList;
    }

    public void checkAll(){

        MasterPilotData mpd = new MasterPilotData();
        mpd.loadData();

        for(MasterPilotData.PilotData pd : mpd){
            testString+=pd.getFaction();
        }
    }

    public String getTestString(){
        return testString;
    }

    public class PilotResults {
        private String faction;
        private String ship;
        private String pilot;
        private String image;
        private boolean existsLocally;

        public PilotResults()
        {
            super();
        }

        public void setFaction(String faction)
        {
            this.faction = faction;
        }
        public void setShip(String ship)
        {
            this.ship = ship;
        }
        public void setPilot(String pilot)
        {
            this.pilot = pilot;
        }
        public void setImage(String image)
        {
            this.image = image;
        }
        public void setExistsLocally(boolean existsLocally)
        {
            this.existsLocally = existsLocally;
        }
        public String getFaction()
        {
            return faction;
        }
        public String getShip()
        {
            return ship;
        }
        public String getPilot()
        {
            return pilot;
        }
        public String getImage()
        {
            return image;
        }
        public boolean existsLocally()
        {
            return existsLocally;
        }

    }
}
