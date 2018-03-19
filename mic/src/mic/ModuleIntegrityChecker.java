package mic;


import mic.ota.OTAMasterActions;
import mic.ota.OTAMasterPilots;
import mic.ota.OTAMasterShips;
import mic.ota.OTAShipBase;

import java.util.*;

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
        boolean exists = XWImageUtils.imageExistsInModule(pilot.getImage());
        if(exists)
        {
            //"Faction","Ship","Pilot","Image","Status"
            pilot.setStatus(true);
          //  String[] pilotLine = {pilot.getFaction(),pilot.getShipXws(),pilot.getPilotXws(),pilot.getImage(),"Exists"};
          //  pilotList.add(pilotLine);
        }else{
            pilot.setStatus(false);
            String[] pilotLine = {pilot.getFaction(),pilot.getShipXws(),pilot.getPilotXws(),pilot.getImage(),"Not Found"};
         //   pilotList.add(pilotLine);
        }
        pilotList.add(pilot);

    }
/*
    // now we need to convert the Array<String[]> to String[][]
    Object[] tempResults = pilotList.toArray();
    String[][] pilotResults = new String[tempResults.length][5];

    for(int j=0;j<tempResults.length;j++)
    {
        pilotResults[j] = (String[])tempResults[j];
    }
*/
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
            boolean exists = XWImageUtils.imageExistsInModule(ship.getImage());
            if(exists)
            {
                ship.setStatus(true);
                //{"XWS","Identifier","Image","Status"};
             //   String[] shipLine = {ship.getXws(),ship.getIdentifier(),ship.getImage(),"Exists"};

            //    shipList.add(shipLine);
            }else{
                ship.setStatus(false);
              //  String[] shipLine = {ship.getXws(),ship.getIdentifier(),ship.getImage(),"Not Found"};
             //   shipList.add(shipLine);
            }
            shipList.add(ship);
        }
/*
        // now we need to convert the Array<String[]> to String[][]
        Object[] tempResults = shipList.toArray();
        String[][] shipResults = new String[tempResults.length][4];

        for(int j=0;j<tempResults.length;j++)
        {
            shipResults[j] = (String[])tempResults[j];
        }
*/
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
                boolean exists = XWImageUtils.imageExistsInModule(shipBaseImageName);

                OTAShipBase shipBase = new OTAShipBase();
                shipBase.setFaction(factionName);
                shipBase.setIdentifier(ship.getIdentifier());
                shipBase.setShipBaseImageName(shipBaseImageName);
                shipBase.setshipImageName(ship.getImage());
                shipBase.setShipName(msd.getShipData(ship.getXws()).getName());
                shipBase.setShipXws(ship.getXws());
                if(exists) {
                    shipBase.setStatus(true);
                }else{
                    shipBase.setStatus(false);
                }
                shipList.add(shipBase);

            }


        }

        return shipList;

    }

    public String[][] checkActions()
    {

        // get list of Actions from OTAMasterActions
        OTAMasterActions oma = new OTAMasterActions();
        Collection<OTAMasterActions.OTAAction> actions = oma.getAllActions();

        ArrayList<String[]> actionList = new ArrayList<String[]>();
        Iterator<OTAMasterActions.OTAAction> i = actions.iterator();
        while(i.hasNext())
        {

            OTAMasterActions.OTAAction action = (OTAMasterActions.OTAAction)i.next();
            boolean exists = XWImageUtils.imageExistsInModule(action.getImage());
            if(exists)
            {
                String[] actionLine = {action.getName(),action.getImage(),"Exists"};
                actionList.add(actionLine);
            }else{
                String[] actionLine = {action.getName(),action.getImage(),"Not Found"};
                actionList.add(actionLine);
            }


        }

        // now we need to convert the Array<String[]> to String[][]
        Object[] tempResults = actionList.toArray();
        String[][] actionResults = new String[tempResults.length][5];

        for(int j=0;j<tempResults.length;j++)
        {
            actionResults[j] = (String[])tempResults[j];
        }

        return actionResults;
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
