package mic.ota;

import java.util.ArrayList;
import java.util.Iterator;

public class OTAContentsCheckerResults
{
    private ArrayList<OTAMasterPilots.OTAPilot> missingPilots;//
    private ArrayList<OTAMasterShips.OTAShip> missingShips;//
    private ArrayList<OTAMasterActions.OTAAction> missingActions;//
    private ArrayList<OTAMasterUpgrades.OTAUpgrade> missingUpgrades;//
    private ArrayList<OTAMasterConditions.OTACondition> missingConditions;//
    private ArrayList<OTAMasterDialHides.OTADialHide> missingDialHides;
    private ArrayList<OTADialMask> missingDialMasks;
    private ArrayList<OTAShipBase> missingShipBases;
    ArrayList<OTAMasterPilots.OTAPilot> pilotResults;
    ArrayList<OTAMasterShips.OTAShip> shipResults;
    ArrayList<OTAMasterActions.OTAAction> actionResults;
    ArrayList<OTAShipBase> shipBaseResults;
    ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeResults;
    ArrayList<OTAMasterConditions.OTACondition> conditionResults;
    ArrayList<OTAMasterDialHides.OTADialHide> dialHideResults;
    ArrayList<OTADialMask> dialMaskResults;

    public int getTotalWork()
    {
        int total = 0;
        total += missingPilots.size();
        total += missingShips.size();
        total += missingActions.size();
        total += missingUpgrades.size();
        total += missingConditions.size();
        total += missingDialHides.size();
        total += missingDialMasks.size();
        total += missingShipBases.size();
        return total;
    }

    public ArrayList<String> getMissingPilotImages()
    {
        ArrayList<String> images = new ArrayList<String>();
        Iterator<OTAMasterPilots.OTAPilot> iterator = missingPilots.iterator();
        OTAMasterPilots.OTAPilot pilot  = null;
        while(iterator.hasNext())
        {
            pilot = iterator.next();
            images.add(pilot.getImage());
        }
        return images;
    }

    public ArrayList<String> getMissingShipImages()
    {
        ArrayList<String> images = new ArrayList<String>();
        Iterator<OTAMasterShips.OTAShip> iterator = missingShips.iterator();
        OTAMasterShips.OTAShip ship  = null;
        while(iterator.hasNext())
        {
            ship = iterator.next();
            images.add(ship.getImage());
        }
        return images;
    }

    public ArrayList<String> getMissingUpgradeImages()
    {
        ArrayList<String> images = new ArrayList<String>();
        Iterator<OTAMasterUpgrades.OTAUpgrade> iterator = missingUpgrades.iterator();
        OTAMasterUpgrades.OTAUpgrade upgrade  = null;
        while(iterator.hasNext())
        {
            upgrade = iterator.next();
            images.add(upgrade.getImage());
        }
        return images;
    }

    public ArrayList<String> getMissingActionImages()
    {
        ArrayList<String> images = new ArrayList<String>();
        Iterator<OTAMasterActions.OTAAction> iterator = missingActions.iterator();
        OTAMasterActions.OTAAction action  = null;
        while(iterator.hasNext())
        {
            action = iterator.next();
            images.add(action.getImage());
        }
        return images;
    }

    public ArrayList<String> getMissingConditionImages()
    {
        ArrayList<String> images = new ArrayList<String>();
        Iterator<OTAMasterConditions.OTACondition> iterator = missingConditions.iterator();
        OTAMasterConditions.OTACondition condition  = null;
        while(iterator.hasNext())
        {
            condition = iterator.next();
            images.add(condition.getImage());
            images.add(condition.getTokenImage());
        }
        return images;
    }

    public ArrayList<String> getMissingDialHideImages()
    {
        ArrayList<String> images = new ArrayList<String>();
        Iterator<OTAMasterDialHides.OTADialHide> iterator = missingDialHides.iterator();
        OTAMasterDialHides.OTADialHide dialHide  = null;
        while(iterator.hasNext())
        {
            dialHide = iterator.next();
            images.add(dialHide.getImage());
        }
        return images;
    }

    public ArrayList<OTAMasterShips.OTAShip> getMissingShips() {
        return missingShips;
    }

    public void setMissingShips(ArrayList<OTAMasterShips.OTAShip> missingShips) {
        this.missingShips = missingShips;
    }

    public ArrayList<OTAMasterActions.OTAAction> getMissingActions() {
        return missingActions;
    }

    public void setMissingActions(ArrayList<OTAMasterActions.OTAAction> missingActions) {
        this.missingActions = missingActions;
    }

    public ArrayList<OTAMasterUpgrades.OTAUpgrade> getMissingUpgrades() {
        return missingUpgrades;
    }

    public void setMissingUpgrades(ArrayList<OTAMasterUpgrades.OTAUpgrade> missingUpgrades) {
        this.missingUpgrades = missingUpgrades;
    }

    public ArrayList<OTAMasterConditions.OTACondition> getMissingConditions() {
        return missingConditions;
    }

    public void setMissingConditions(ArrayList<OTAMasterConditions.OTACondition> missingConditions) {
        this.missingConditions = missingConditions;
    }

    public ArrayList<OTAMasterDialHides.OTADialHide> getMissingDialHides() {
        return missingDialHides;
    }

    public void setMissingDialHides(ArrayList<OTAMasterDialHides.OTADialHide> missingDialHides) {
        this.missingDialHides = missingDialHides;
    }

    public ArrayList<OTADialMask> getMissingDialMasks() {
        return missingDialMasks;
    }

    public void setMissingDialMasks(ArrayList<OTADialMask> missingDialMasks) {
        this.missingDialMasks = missingDialMasks;
    }

    public ArrayList<OTAShipBase> getMissingShipBases() {
        return missingShipBases;
    }

    public void setMissingShipBases(ArrayList<OTAShipBase> missingShipBases) {
        this.missingShipBases = missingShipBases;
    }

    public ArrayList<OTAMasterPilots.OTAPilot> getPilotResults() {
        return pilotResults;
    }

    public void setPilotResults(ArrayList<OTAMasterPilots.OTAPilot> pilotResults) {
        this.pilotResults = pilotResults;
    }

    public ArrayList<OTAMasterShips.OTAShip> getShipResults() {
        return shipResults;
    }

    public void setShipResults(ArrayList<OTAMasterShips.OTAShip> shipResults) {
        this.shipResults = shipResults;
    }

    public ArrayList<OTAMasterActions.OTAAction> getActionResults() {
        return actionResults;
    }

    public void setActionResults(ArrayList<OTAMasterActions.OTAAction> actionResults) {
        this.actionResults = actionResults;
    }

    public ArrayList<OTAShipBase> getShipBaseResults() {
        return shipBaseResults;
    }

    public void setShipBaseResults(ArrayList<OTAShipBase> shipBaseResults) {
        this.shipBaseResults = shipBaseResults;
    }

    public ArrayList<OTAMasterUpgrades.OTAUpgrade> getUpgradeResults() {
        return upgradeResults;
    }

    public void setUpgradeResults(ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeResults) {
        this.upgradeResults = upgradeResults;
    }

    public ArrayList<OTAMasterConditions.OTACondition> getConditionResults() {
        return conditionResults;
    }

    public void setConditionResults(ArrayList<OTAMasterConditions.OTACondition> conditionResults) {
        this.conditionResults = conditionResults;
    }

    public ArrayList<OTAMasterDialHides.OTADialHide> getDialHideResults() {
        return dialHideResults;
    }

    public void setDialHideResults(ArrayList<OTAMasterDialHides.OTADialHide> dialHideResults) {
        this.dialHideResults = dialHideResults;
    }

    public ArrayList<OTADialMask> getDialMaskResults() {
        return dialMaskResults;
    }

    public void setDialMaskResults(ArrayList<OTADialMask> dialMaskResults) {
        this.dialMaskResults = dialMaskResults;
    }



    public OTAContentsCheckerResults()
    {
        super();
    }

    public ArrayList<OTAMasterPilots.OTAPilot> getMissingPilots()
    {
        return missingPilots;
    }
    public void setMissingPilots(ArrayList<OTAMasterPilots.OTAPilot> missingPilots)
    {
        this.missingPilots = missingPilots;
    }


}
