package mic;


import java.util.ArrayList;

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
        if(Util.imageExistsInModule(pilotCardImage))
        {
            pilots[i][4] = "Exists";
        }else{
            pilots[i][4] = "Not Found";
        }

    }

    return pilots;
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
