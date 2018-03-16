package mic;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Mic on 03/11/2018.
 *
 * Might only be temporary. Checks:
 * -if every xwing-data pilot or upgrade has a correspondant local image
 * -if every xwing-data ship has a ship token prepared, as well as dial
 */

public class ModuleIntegrityChecker {
    private static String REMOTE_URL = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/pilots.js";

    public String testString = "";

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
/*
    public ArrayList<PilotResults> checkPilots()
    {
        MasterPilotData mpd = new MasterPilotData();
        mpd.loadData();

        MasterShipData msd = new MasterShipData();
        msd.loadData();

        PilotResults pr = null;
        ArrayList<PilotResults> pilots = new ArrayList<PilotResults>();
        MasterShipData.ShipData sd = null;
        for(MasterPilotData.PilotData pd : mpd)
        {
            pr = new PilotResults();
            pr.setFaction(pd.getFaction());
            pr.setPilot(pd.getName());
            pr.setShip(pd.getShip());

            sd = MasterShipData.getShipDataForShipName(pd.getShip());
            String pilotCardImage = "Pilot_" + Canonicalizer.getCanonicalFactionName(pd.getFaction()) + "_" + sd.getXws() + "_" + pd.getXws() + ".jpg";

            pr.setImage(pilotCardImage);
            pr.setExistsLocally(Util.imageExistsInModule(pilotCardImage));
            pilots.add(pr);
        }

        return pilots;
    }
    */
public String[][] checkPilots()
{
    MasterPilotData mpd = new MasterPilotData();
    mpd.loadData();

    MasterShipData msd = new MasterShipData();
    msd.loadData();

    PilotResults pr = null;
    Object[] allPilots = mpd.getAllPilots();
    String[][] pilots = new String[allPilots.length][5];

    MasterShipData.ShipData sd = null;
    for(int i=0;i<allPilots.length;i++)
    {
        pilots[i][0] = ((MasterPilotData.PilotData)allPilots[i]).getFaction();
        pilots[i][1] = ((MasterPilotData.PilotData)allPilots[i]).getShip();
        pilots[i][2] = ((MasterPilotData.PilotData)allPilots[i]).getName();


        sd = MasterShipData.getShipDataForShipName(((MasterPilotData.PilotData)allPilots[i]).getShip());
      //  String pilotCardImage = "Pilot_" + Canonicalizer.getCanonicalFactionName(((MasterPilotData.PilotData)allPilots[i]).getShip()) + "_" + sd.getXws() + "_" + ((MasterPilotData.PilotData)allPilots[i]).getXws() + ".jpg";

        String pilotCardImage = "Pilot_" + Canonicalizer.getCanonicalFactionName(
                ((MasterPilotData.PilotData)allPilots[i]).getFaction()) +
                "_" +
                sd.getXws() +
                "_" +
                ((MasterPilotData.PilotData)allPilots[i]).getXws() +
                ".jpg";

        pilots[i][3] = pilotCardImage;
        if(XWImageUtils.imageExistsInModule(pilotCardImage))
        {
            pilots[i][4] = "Exists";
        }else{
            pilots[i][4] = "Not Found";
        }

    }

    return pilots;
}

    public String[][] checkArcs()
    {

        MasterShipData msd = new MasterShipData();
        msd.loadData();

        Object[] allShips = msd.getAllShips();

        // first get a list of all possible combinations of faction/arc from the json
        HashMap possibleArcs = new HashMap();
        ArrayList<String[]> arcCombinations = new ArrayList<String[]>();
        for(int i=0;i<allShips.length;i++) {
            String shipSize = ((MasterShipData.ShipData) allShips[i]).getSize();
            for (int j = 0; j < ((MasterShipData.ShipData) allShips[i]).getFactions().size(); j++) {
                String factionName = ((MasterShipData.ShipData) allShips[i]).getFactions().get(j);
                for (int k = 0; k < ((MasterShipData.ShipData) allShips[i]).getFiringArcs().size(); k++) {
                    String arcName = ((MasterShipData.ShipData) allShips[i]).getFiringArcs().get(k);

                    String imageName = XWImageUtils.buildFiringArcImageName(shipSize,factionName,arcName);

                    if(possibleArcs.get(imageName)==null)
                    {
                        // add it
                        possibleArcs.put(imageName, "");

                        // check for existence
                        boolean exists = XWImageUtils.imageExistsInModule(imageName);

                        // add it to the array
                        if(exists) {
                            String[] arc = {shipSize, factionName, arcName, imageName, "Exists"};
                            arcCombinations.add(arc);
                        }else{
                            String[] arc = {shipSize, factionName, arcName, imageName, "Not Found"};
                            arcCombinations.add(arc);
                        }
                    }
                }
            }
        }

        // now we need to convert the Array<String[]> to String[][]
        Object[] tempResults = arcCombinations.toArray();
        String[][] arcResults = new String[tempResults.length][5];

        for(int i=0;i<tempResults.length;i++)
        {
            arcResults[i] = (String[])tempResults[i];
        }

        return arcResults;
    }

    public String[][] checkShips()
    {

        MasterShipData msd = new MasterShipData();
        msd.loadData();

        OTAShipBuildExceptions sbe = new OTAShipBuildExceptions();
        sbe.loadData();

        Object[] allShips = msd.getAllShips();


        // first get a list of all possible ships
        HashMap possibleShips = new HashMap();
        ArrayList<String[]> shipList = new ArrayList<String[]>();
        for(int i=0;i<allShips.length;i++)
        {
            String shipName = ((MasterShipData.ShipData)allShips[i]).getName();
            String shipXWS = ((MasterShipData.ShipData)allShips[i]).getXws();
            if(possibleShips.get(shipXWS)==null)
            {
                String imageName = "Ship_"+shipXWS+".png";
                possibleShips.put(shipXWS,imageName);

                boolean exists = XWImageUtils.imageExistsInModule(imageName);

                // add it to the array
                if(exists)
                {
                    String[] ship = {shipName,shipXWS,imageName,"Exists"};
                    shipList.add(ship);
                }else{
                    String[] ship = {shipName,shipXWS,imageName,"Not Found"};
                    shipList.add(ship);
                }

            }
        }

        // now check the OTA exceptions
        Object[] allShipExceptions = sbe.getAllShips();
        for(int i=0;i<allShipExceptions.length;i++)
        {
            String shipName = ((OTAShipBuildExceptions.ShipException)allShipExceptions[i]).getName();
            String shipXWS = ((OTAShipBuildExceptions.ShipException)allShipExceptions[i]).getXws();
            List<String> shipImages = ((OTAShipBuildExceptions.ShipException)allShipExceptions[i]).getImages();

            for(String altShipImage : shipImages)
            {
                if (possibleShips.get(shipXWS+"_"+altShipImage) == null)
                {
                    String imageName = altShipImage;
                    possibleShips.put(shipXWS+"_"+altShipImage, imageName);

                    boolean exists = XWImageUtils.imageExistsInModule(imageName);

                    // add it to the array
                    if (exists) {
                        String[] ship = {shipName, shipXWS, imageName, "Exists"};
                        shipList.add(ship);
                    } else {
                        String[] ship = {shipName, shipXWS, imageName, "Not Found"};
                        shipList.add(ship);
                    }

                }
            }
        }

        // now we need to convert the Array<String[]> to String[][]
        Object[] tempResults = shipList.toArray();
        String[][] shipResults = new String[tempResults.length][5];

        for(int i=0;i<tempResults.length;i++)
        {
            shipResults[i] = (String[])tempResults[i];
        }

        return shipResults;
    }

    public String[][] checkShipBases()
    {

        MasterShipData msd = new MasterShipData();
        msd.loadData();

        Object[] allShips = msd.getAllShips();

        HashMap possibleShipFactions = new HashMap();
        ArrayList<String[]> shipList = new ArrayList<String[]>();
        for(int i=0;i<allShips.length;i++)
        {
            String shipSize = ((MasterShipData.ShipData) allShips[i]).getSize();
            String shipName = ((MasterShipData.ShipData) allShips[i]).getName();
            String shipXWS = ((MasterShipData.ShipData) allShips[i]).getXws();

            for (int j = 0; j < ((MasterShipData.ShipData) allShips[i]).getFactions().size(); j++)
            {
                String factionName = ((MasterShipData.ShipData) allShips[i]).getFactions().get(j);

                String imageName = XWImageUtils.buildShipBaseImageName(factionName,shipXWS);

                if(possibleShipFactions.get(imageName)==null)
                {
                    // add it
                    possibleShipFactions.put(imageName, "");

                    // check for existence
                    boolean exists = XWImageUtils.imageExistsInModule(imageName);

                    // add it to the array
                    if(exists) {
                        String[] ship = {shipName, shipXWS, shipSize, factionName, imageName, "Exists"};
                        shipList.add(ship);
                    }else{
                        String[] ship = {shipName, shipXWS, shipSize, factionName, imageName,"Not Found"};
                        shipList.add(ship);
                    }
                }

            }
        }

        // now we need to convert the Array<String[]> to String[][]
        Object[] tempResults = shipList.toArray();
        String[][] shipBaseResults = new String[tempResults.length][5];

        for(int i=0;i<tempResults.length;i++)
        {
            shipBaseResults[i] = (String[])tempResults[i];
        }

        return shipBaseResults;
    }

    public String[][] checkActions()
    {

        MasterShipData msd = new MasterShipData();
        msd.loadData();

        Object[] allShips = msd.getAllShips();

        // first get a list of all possible ships
        HashMap possibleActions = new HashMap();
        ArrayList<String[]> actionList = new ArrayList<String[]>();
        for(int i=0;i<allShips.length;i++)
        {
            for(int j=0;j<((MasterShipData.ShipData)allShips[i]).getActions().size();j++)
            {
                String actionName = ((MasterShipData.ShipData)allShips[i]).getActions().get(j);
                if(possibleActions.get(actionName)==null)
                {
                    String imageName = "Action_"+actionName.toLowerCase().replaceAll(" ","")+".png";
                    possibleActions.put(actionName,imageName);
                    boolean exists = XWImageUtils.imageExistsInModule(imageName);
                    if(exists)
                    {
                        String[] action = {actionName,imageName,"Exists"};
                        actionList.add(action);
                    }else{
                        String[] action = {actionName,imageName,"Not Found"};
                        actionList.add(action);
                    }
                }
            }

        }

        // now we need to convert the Array<String[]> to String[][]
        Object[] tempResults = actionList.toArray();
        String[][] actionResults = new String[tempResults.length][5];

        for(int i=0;i<tempResults.length;i++)
        {
            actionResults[i] = (String[])tempResults[i];
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
