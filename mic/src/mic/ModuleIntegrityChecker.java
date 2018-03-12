package mic;


import java.util.ArrayList;
import java.util.List;

import static mic.Util.logToChat;

/**
 * Created by Mic on 03/11/2018.
 *
 * Might only be temporary. Checks:
 * -if every xwing-data pilot or upgrade has a correspondant local image
 * -if every xwing-data ship has a ship token prepared, as well as dial
 */

public class ModuleIntegrityChecker {
    private static String REMOTE_URL = "https://raw.githubusercontent.com/guidokessels/xwing-data/master/data/pilots.js";

    public String testString ="";

    private ArrayList<String> pilotsRebelAlliance, pilotsRebelAllianceMissing,
                        pilotsResistance, pilotsResistanceMissing,
                        pilotsGalacticEmpire, pilotsGalacticEmpireMissing,
                        pilotsFirstOrder, pilotsFirstOrderMissing,
                        pilotsScumAndVillainy, getPilotsScumAndVillainyMissing,
                        upgrades, upgradesMissing,
                        dialsRebels, dialsEmpire, dialsScum;

    public ModuleIntegrityChecker(){
        pilotsRebelAlliance = new ArrayList<String>();
                pilotsRebelAllianceMissing  = new ArrayList<String>();
                pilotsResistance  = new ArrayList<String>();
                pilotsResistanceMissing  = new ArrayList<String>();
                pilotsGalacticEmpire  = new ArrayList<String>();
                pilotsGalacticEmpireMissing  = new ArrayList<String>();
                pilotsFirstOrder  = new ArrayList<String>();
                pilotsFirstOrderMissing = new ArrayList<String>();
                pilotsScumAndVillainy  = new ArrayList<String>();
                getPilotsScumAndVillainyMissing  = new ArrayList<String>();
                upgrades  = new ArrayList<String>();
                upgradesMissing  = new ArrayList<String>();
                dialsRebels  = new ArrayList<String>();
                dialsEmpire  = new ArrayList<String>();
                dialsScum = new ArrayList<String>();

                checkAll();
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

}
