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

    public ArrayList<OTAMasterUpgrades.OTAUpgrade> checkUpgrades(boolean onlyDetectOne)
    {
        // get list of upgrades from OTAMasterUpgrades
        OTAMasterUpgrades omu = new OTAMasterUpgrades();
        omu.flushData();
        Collection<OTAMasterUpgrades.OTAUpgrade> upgrades = omu.getAllUpgrades(1);


        ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeList = new ArrayList<OTAMasterUpgrades.OTAUpgrade>();
        Iterator<OTAMasterUpgrades.OTAUpgrade> i = upgrades.iterator();

        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        while(i.hasNext())
        {
            upgrade = (OTAMasterUpgrades.OTAUpgrade)i.next();
            //if(MasterUpgradeData.getUpgradeData(upgrade.getXws()) != null && XWOTAUtils.imageExistsInOTA("upgrade",upgrade.getImage())) {
            if(MasterUpgradeData.getUpgradeData(upgrade.getXws()) != null) {

                boolean exists = XWOTAUtils.imageExistsInModule(upgrade.getImage());
                upgrade.setStatus(exists);

                boolean existsInOTA = XWOTAUtils.imageExistsInOTA("upgrades",upgrade.getImage(),OTAContentsChecker.OTA_RAW_BRANCH_URL);
                upgrade.setStatusOTA(existsInOTA);

                // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
                boolean gonnaDL = false;
                if(exists == false && existsInOTA) gonnaDL = true;

                if(upgrade.getStatus() || (!upgrade.getStatus() && existsInOTA)) {
                    upgradeList.add(upgrade);
                    if(onlyDetectOne && gonnaDL) return upgradeList;
                }
            }

        }

        return upgradeList;
    }

    public ArrayList<OTAMasterConditions.OTACondition> checkConditions(boolean onlyDetectOne)
    {
        // get list of conditions from OTAMasterUpgrades
        OTAMasterConditions omc = new OTAMasterConditions();
        omc.flushData();
        Collection<OTAMasterConditions.OTACondition> conditions = omc.getAllConditions(1);


        ArrayList<OTAMasterConditions.OTACondition> conditionList = new ArrayList<OTAMasterConditions.OTACondition>();
        Iterator<OTAMasterConditions.OTACondition> i = conditions.iterator();

        OTAMasterConditions.OTACondition condition = null;
        while(i.hasNext())
        {
            condition = (OTAMasterConditions.OTACondition)i.next();

            if(MasterConditionData.getConditionData(condition.getXws()) != null ) {


                boolean conditionExists = XWOTAUtils.imageExistsInModule(condition.getImage());
                boolean conditionTokenExists = XWOTAUtils.imageExistsInModule(condition.getTokenImage());

                condition.setStatus(conditionExists);
                condition.setTokenStatus(conditionTokenExists);

                boolean conditionExistsInOTA = XWOTAUtils.imageExistsInOTA("conditions",condition.getImage(),OTAContentsChecker.OTA_RAW_BRANCH_URL);
                condition.setStatusOTA(conditionExistsInOTA);
                boolean conditionTokenExistsInOTA = XWOTAUtils.imageExistsInOTA("conditions", condition.getTokenImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL);
                condition.setTokenStatusOTA(conditionTokenExists);

                condition.setStatus(conditionExists);
                condition.setTokenStatus(conditionTokenExists);

                // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
                boolean gonnaDL = false;
                if((conditionExists == false && conditionExistsInOTA) || (conditionTokenExists == false && conditionTokenExistsInOTA)) gonnaDL = true;

                if(
                        (conditionExists ||  (!conditionExists && conditionExistsInOTA)) ||
                                (conditionTokenExists || (!conditionTokenExists && conditionTokenExistsInOTA))
                        )
                {
                    conditionList.add(condition);
                    if(onlyDetectOne && gonnaDL) return conditionList;
                }
            }

        }

        return conditionList;
    }

    public ArrayList<OTAMasterDialHides.OTADialHide> checkDialHides(boolean onlyDetectOne)
    {

        OTAMasterDialHides omdh = new OTAMasterDialHides();
        omdh.flushData();
        Collection<OTAMasterDialHides.OTADialHide> dialHides = omdh.getAllDialHides();


        ArrayList<OTAMasterDialHides.OTADialHide> dialHideList = new ArrayList<OTAMasterDialHides.OTADialHide>();
        Iterator<OTAMasterDialHides.OTADialHide> i = dialHides.iterator();

        OTAMasterDialHides.OTADialHide dialHide = null;
        while(i.hasNext())
        {
            dialHide = i.next();

            // check to be sure this ship exists in xwing-data or dispatcher and the image exists in OTA
            // if it doesn't, just skip this entry from OTA
            if(MasterShipData.getShipData(dialHide.getXws()) != null)
            {
                boolean exists = XWOTAUtils.imageExistsInModule(dialHide.getImage());
                dialHide.setStatus(exists);
                boolean existsInOTA = XWOTAUtils.imageExistsInOTA("dial",dialHide.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL);
                dialHide.setStatusOTA(existsInOTA);

                // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
                boolean gonnaDL = false;
                if(exists == false && existsInOTA) gonnaDL = true;

                if(exists || gonnaDL) {
                    dialHideList.add(dialHide);

                    //speeds up the parsing right away if at least 1 object is detected as needing a download
                    if(onlyDetectOne && gonnaDL) return dialHideList;
                }

            }
        }

        return dialHideList;
    }



public ArrayList<OTAMasterPilots.OTAPilot> checkPilots(boolean onlyDetectOne)
{
    // get list of pilots from OTAMasterPilots
    OTAMasterPilots omp = new OTAMasterPilots();
    omp.flushData();
    Collection<OTAMasterPilots.OTAPilot> pilots = omp.getAllPilotImagesFromOTA(1);

    ArrayList<OTAMasterPilots.OTAPilot> pilotListToReturn = new ArrayList<OTAMasterPilots.OTAPilot>();
    Iterator<OTAMasterPilots.OTAPilot> i = pilots.iterator();

    while(i.hasNext())
    {
        OTAMasterPilots.OTAPilot pilot = (OTAMasterPilots.OTAPilot)i.next();

        if(MasterPilotData.getPilotData(pilot.getShipXws(),pilot.getPilotXws(),pilot.getFaction()) != null) {

            boolean exists = XWOTAUtils.imageExistsInModule(pilot.getImage());
            pilot.setStatus(exists);
            boolean existsInOTA = XWOTAUtils.imageExistsInOTA("pilots",pilot.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL);
            pilot.setStatusOTA(existsInOTA);

            // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
            boolean gonnaDL = false;
            if(exists == false && existsInOTA) gonnaDL = true;

            if(exists || gonnaDL) {
                pilotListToReturn.add(pilot);
                if(onlyDetectOne && gonnaDL) return pilotListToReturn;
            }

        }
    }
    return pilotListToReturn;
}

    public ArrayList<OTAMasterShips.OTAShip> checkShips(boolean onlyDetectOne)
    {
        // get list of ships from OTAMasterShips
        OTAMasterShips oms = new OTAMasterShips();
        oms.flushData();
        Collection<OTAMasterShips.OTAShip> ships = oms.getAllShips(1);


        ArrayList<OTAMasterShips.OTAShip> shipList = new ArrayList<OTAMasterShips.OTAShip>();
        Iterator<OTAMasterShips.OTAShip> i = ships.iterator();

        while(i.hasNext())
        {
            OTAMasterShips.OTAShip ship = (OTAMasterShips.OTAShip)i.next();
            if(MasterShipData.getShipData(ship.getXws()) != null)
            {
                boolean exists = XWOTAUtils.imageExistsInModule(ship.getImage());
                ship.setStatus(exists);
                boolean existsInOTA = XWOTAUtils.imageExistsInOTA("ships",ship.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL);
                ship.setStatusOTA(existsInOTA);

                // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
                boolean gonnaDL = false;
                if(exists == false && existsInOTA) gonnaDL = true;

                if(exists || gonnaDL) {
                    shipList.add(ship);
                    if(onlyDetectOne && gonnaDL) return shipList;
                }
            }
        }

        return shipList;

    }

    public ArrayList<OTAShipBase> checkShipBases(boolean onlyDetectOne)
    {
        OTAMasterShips oms = new OTAMasterShips();
        oms.flushData();
        Collection<OTAMasterShips.OTAShip> ships = oms.getAllShips(1);

          MasterShipData msd = new MasterShipData();
          msd.loadData();

        ArrayList<OTAShipBase> shipList = new ArrayList<OTAShipBase>();
        Iterator<OTAMasterShips.OTAShip> i1 = ships.iterator();

        while(i1.hasNext())
        {
            OTAMasterShips.OTAShip ship = (OTAMasterShips.OTAShip)i1.next();

            if(MasterShipData.getShipData(ship.getXws()) != null)
            {
                // check to see which factions to generate this ship base for
                List<String> factions = ship.getFactions();

                Iterator<String> i2 = factions.iterator();
                while (i2.hasNext()) {

                    String factionName = i2.next();

                    String shipBaseImageName = XWOTAUtils.buildShipBaseImageName(factionName, ship.getXws(), ship.getIdentifier(), 1);


                    OTAShipBase shipBase = new OTAShipBase();
                    shipBase.setFaction(factionName);
                    shipBase.setIdentifier(ship.getIdentifier());
                    shipBase.setShipBaseImageName(shipBaseImageName);
                    shipBase.setshipImageName(ship.getImage());
                    shipBase.setShipName(msd.getShipData(ship.getXws()).getName());
                    shipBase.setShipXws(ship.getXws());

                    boolean exists = XWOTAUtils.imageExistsInModule(shipBaseImageName);
                    shipBase.setStatus(exists);
                    boolean existsInOTA = XWOTAUtils.imageExistsInOTA("ships",ship.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL);
                    shipBase.setStatusOTA(existsInOTA);

                    // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
                    //the ship base can't exist in OTA, instead, we check the isolated ship gfx a few lines above
                    boolean gonnaDL = false;
                    if(exists == false && existsInOTA) gonnaDL = true;

                    if(exists || gonnaDL) {
                        shipList.add(shipBase);
                        if(onlyDetectOne && gonnaDL) return shipList;
                    }

                }
            }
        }
        return shipList;
    }

    public ArrayList<OTADialMask> checkDialMasks(boolean onlyDetectOne)
    {
        OTAMasterDialHides omdh = new OTAMasterDialHides();
        omdh.flushData();
        Collection<OTAMasterDialHides.OTADialHide> dialHides = omdh.getAllDialHides();

        MasterShipData msd = new MasterShipData();
        msd.loadData();

        ArrayList<OTADialMask> dialMaskList = new ArrayList<OTADialMask>();
        Iterator<OTAMasterDialHides.OTADialHide> dialHideIterator = dialHides.iterator();

        while(dialHideIterator.hasNext())
        {
            OTAMasterDialHides.OTADialHide dialHide = dialHideIterator.next();

            // check to be sure this ship exists in xwing-data or dispatcher
            // if it doesn't, just skip this entry from OTA
            if(MasterShipData.getShipData(dialHide.getXws()) != null)
            {
                // check to see which factions to generate this dial mask
                List<String> factions = dialHide.getFactions();

                Iterator<String> factionIterator = factions.iterator();
                while (factionIterator.hasNext()) {

                    String factionName = factionIterator.next();

                    String dialMaskImageName = XWOTAUtils.buildDialMaskImageName(factionName, dialHide.getXws());


                    OTADialMask dialMask = new OTADialMask();
                    dialMask.setFaction(factionName);
                    dialMask.setDialHideImageName(dialHide.getImage());
                    dialMask.setDialMaskImageName(dialMaskImageName);
                    dialMask.setShipName(MasterShipData.getShipData(dialHide.getXws()).getName());
                    dialMask.setShipXws(dialHide.getXws());

                    boolean exists = XWOTAUtils.imageExistsInModule(dialMaskImageName);
                    dialMask.setStatus(exists);
                    boolean existsInOTA = XWOTAUtils.imageExistsInOTA("dial",dialHide.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL);
                    dialMask.setStatusOTA(existsInOTA);

                    // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
                    //the ship base can't exist in OTA, instead, we check the isolated ship gfx a few lines above
                    boolean gonnaDL = false;
                    if(exists == false && existsInOTA) gonnaDL = true;

                    if(exists || gonnaDL) {
                        dialMaskList.add(dialMask);
                        if(onlyDetectOne && gonnaDL) return dialMaskList;
                    }



                }
            }


        }

        return dialMaskList;

    }

    public ArrayList<OTAMasterActions.OTAAction> checkActions(boolean onlyDetectOne)
    {

        boolean addToList = false;
        // get list of Actions from OTAMasterActions
        OTAMasterActions oma = new OTAMasterActions();
        oma.flushData();
        Collection<OTAMasterActions.OTAAction> actions = oma.getAllActions();

        ArrayList<OTAMasterActions.OTAAction> actionList = new ArrayList<OTAMasterActions.OTAAction>();
        Iterator<OTAMasterActions.OTAAction> i = actions.iterator();
        OTAMasterActions.OTAAction action = null;
        while(i.hasNext())
        {

            action = i.next();
            action.setStatus(XWOTAUtils.imageExistsInModule(action.getImage()));

            boolean exists = XWOTAUtils.imageExistsInModule(action.getImage());
            action.setStatus(exists);
            boolean existsInOTA = XWOTAUtils.imageExistsInOTA("actions",action.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL);
            action.setStatusOTA(existsInOTA);

            // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
            boolean gonnaDL = false;
            if(exists == false && existsInOTA) gonnaDL = true;

            if(exists || gonnaDL) {
                actionList.add(action);
                if(onlyDetectOne && gonnaDL) return actionList;
            }
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
