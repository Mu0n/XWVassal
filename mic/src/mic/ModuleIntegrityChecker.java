package mic;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mic on 03/11/2018.
 *
 * Might only be temporary. Checks:
 * -if every xwing-data pilot or upgrade has a correspondant local image
 * -if every xwing-data ship has a ship token prepared, as well as dial
 */

public class ModuleIntegrityChecker {
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
MasterPilotData allo =
    }

}
