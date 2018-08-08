package mic;

import VASSAL.build.GameModule;
import VASSAL.build.Widget;
import VASSAL.build.widget.ListWidget;
import VASSAL.build.widget.PieceSlot;
import VASSAL.build.widget.TabWidget;
import com.google.common.collect.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by mujuneau on 6/8/18.
 */
public class VassalXWSPieceLoader2e {
    private static final String STEM_UPGRADE_SLOT_NAME = "Stem Upgrade";
    private static Map<String, String> stemUpgradeSlotNames = ImmutableMap.<String, String>builder()
            .put("astromechdroid","Stem2e Upgrade Astromech Droid")
            .put("cannon","Stem2e Upgrade Cannon")
            .put("configuration","Stem2e Upgrade Configuration")
            .put("crew","Stem2e Upgrade Crew")
            .put("device","Stem2e Upgrade Device")
            .put("force","Stem2e Upgrade Force")
            .put("gunner","Stem2e Upgrade Gunner")
            .put("illicit","Stem2e Upgrade Illicit")
            .put("missile","Stem2e Upgrade Missile")
            .put("modification","Stem2e Upgrade Modification")
            .put("system","Stem2e Upgrade System")
            .put("talent","Stem2e Upgrade Talent")
            .put("tech","Stem2e Upgrade Tech")
            .put("title","Stem2e Upgrade Title")
            .put("torpedo","Stem2e Upgrade Torpedo")
            .put("turret","Stem2e Upgrade Turret")
            .build();

    private static List<String> obstacleTabNames = Lists.newArrayList(
            "Asteroids", "TFA_Asteroids", "Debris"
    );

    Map<String, VassalXWSPilotPieces2e> pilotPiecesMap = Maps.newHashMap();
    Map<String, VassalXWSPilotPieces2e.Upgrade> upgradePiecesMap = Maps.newHashMap();
    Map<Tokens2e, PieceSlot> tokenPiecesMap = Maps.newHashMap();
    Map<Obstacles, PieceSlot> obstaclesPiecesMap = Maps.newHashMap();
    // Map<String, VassalXWSPilotPieces2e.Condition> conditionPiecesMap = Maps.newHashMap();

    public VassalXWSListPieces2e loadListFromXWS(XWSList2e list, List<XWS2Pilots> allPilots, List<XWS2Upgrades> allUpgrades) {
        if (pilotPiecesMap.isEmpty()) {
            loadPieces(allPilots);
        }

        VassalXWSListPieces2e pieces = new VassalXWSListPieces2e();

        Multiset<String> pilotCounts = HashMultiset.create();
        for (XWSList2e.XWSPilot pilot : list.getPilots()) {
            pilotCounts.add(pilot.getName());
        }

        Multiset<String> genericPilotsAdded = HashMultiset.create();

        for (XWSList2e.XWSPilot pilot : list.getPilots())
        {
            String xwsShip = Canonicalizer.getCleanedName(pilot.getShip());
            XWS2Pilots shipData = XWS2Pilots.getSpecificShip(pilot.getXws2(), allPilots);
            XWS2Pilots.Pilot2e pilotData = XWS2Pilots.getSpecificPilot(pilot.getXws2(), allPilots);

            // generate the pilot card
            VassalXWSPilotPieces2e barePieces = new VassalXWSPilotPieces2e();
            barePieces.setPilotData(pilotData);
            barePieces.setShipData(shipData);

            // Add the pilot card
            // get the pilot card slot
            PieceSlot stemPilotSlot = null;


            // add the stem ship
            PieceSlot smallShipSlot = null;
            PieceSlot mediumShipSlot = null;
            PieceSlot largeShipSlot = null;

            // stem upgrade
            PieceSlot stemUpgradeSlot = null;
            PieceSlot stemConditionSlot = null;
            PieceSlot stemConditionTokenSlot = null;

            // ==================================================
            // Get the stem slots
            // ==================================================
            List<PieceSlot> stemPieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

            for (PieceSlot pieceSlot : stemPieceSlots) {
                String slotName = pieceSlot.getConfigureName();
                if (slotName.startsWith("Stem Pilot") && stemPilotSlot == null) {
                    stemPilotSlot = pieceSlot;
                    continue;

                } else if(slotName.startsWith("ship -- 2e Stem Small Ship")&& smallShipSlot == null)
                {
                    smallShipSlot = pieceSlot;
                    continue;
                }

            }

            // fill in the pilot cards
            barePieces.setPilotCard(stemPilotSlot);

            // fill in the ships
            if(shipData.getSize().equals("small"))
            {
                barePieces.setShip(smallShipSlot);
            }else if(shipData.getSize().equals("medium")) {
                barePieces.setShip(mediumShipSlot);
            }else if(shipData.getSize().equals("large")) {
                barePieces.setShip(largeShipSlot);
            }

            VassalXWSPilotPieces2e pilotPieces = new VassalXWSPilotPieces2e(barePieces);


            if (pilotCounts.count(pilot.getName()) > 1) {
                genericPilotsAdded.add(pilot.getName());
                pilotPieces.setShipNumber(genericPilotsAdded.count(pilot.getName()));
            }


            pieces.getShips().add(pilotPieces);

        }


        return pieces;
    }

    private List<VassalXWSPilotPieces2e.Condition> getConditionsForCard(List<String> conditions, PieceSlot stemConditionSlot)
    {
        List<VassalXWSPilotPieces2e.Condition> conditionSlots = Lists.newArrayList();
        for (String conditionName : conditions)
        {
           // String canonicalConditionName = Canonicalizer.getCanonicalUpgradeName("conditions", conditionName);
          //  String mapKey = getUpgradeMapKey("conditions", canonicalConditionName);
           // M
           // VassalXWSPilotPieces.Condition condition = this.conditionPiecesMap.get(mapKey);

            // MrMurphM
            MasterConditionData.ConditionData newConditionData = MasterConditionData.getConditionDataByName(conditionName);
            VassalXWSPilotPieces2e.Condition condition = new VassalXWSPilotPieces2e.Condition(stemConditionSlot, conditionName,  newConditionData.getName());

          //  MasterUpgradeData.UpgradeData newUpgradeData = MasterUpgradeData.getUpgradeData(conditionName);
            condition.setConditionData(newConditionData);
           // condition = new VassalXWSPilotPieces.Upgrade(conditionName, stemConditionSlot);

            conditionSlots.add(condition);
        }
        return conditionSlots;

        /*
        List<VassalXWSPilotPieces.Upgrade> conditionSlots = Lists.newArrayList();
        for (String conditionName : conditions) {
            String canonicalConditionName = Canonicalizer.getCanonicalUpgradeName(
                    "conditions", conditionName);
            String mapKey = getUpgradeMapKey("conditions", canonicalConditionName);
            VassalXWSPilotPieces.Upgrade condition = this.upgradePiecesMap.get(mapKey);
            if (condition == null)
            {
                Util.logToChat("Condition " + conditionName +" is not yet included in XWVassal.  Generating it.");

                // need to grab stem condition here
                String stemConditionSlotName = "Stem Condition WIP";
                List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

                for (PieceSlot pieceSlot : pieceSlots) {
                    String slotName = pieceSlot.getConfigureName();
                    if (slotName.equals(stemConditionSlotName))
                    {
                        // this is the correct slot
                        condition = new VassalXWSPilotPieces.Upgrade(conditionName, pieceSlot);

                        continue;
                    }
                }

            }
            conditionSlots.add(condition);
        }
        return conditionSlots;
        */
    }

    public void loadPieces(List<XWS2Pilots> allShips) {
        pilotPiecesMap = Maps.newHashMap();
        upgradePiecesMap = Maps.newHashMap();
        tokenPiecesMap = Maps.newHashMap();
        obstaclesPiecesMap = Maps.newHashMap();

        List<ListWidget> listWidgets = GameModule.getGameModule().getAllDescendantComponentsOf(ListWidget.class);
        for (ListWidget listWidget : listWidgets) {
            if (!(listWidget.getParent() instanceof TabWidget)) {
                continue;
            }
            ListParentType parentType = ListParentType.fromTab(listWidget.getParent());
            if (parentType == null) {
                continue;
            }
            switch (parentType) {
                case chits:
                    loadChits(listWidget);
                    break;
                case upgrades:
                    loadUpgrades(listWidget);
                    break;
                case imperial:
                case rebel:
                case scum:
                    loadPilots(listWidget, parentType, allShips);
                    break;
            }
        }
    }

    private void loadChits(ListWidget listWidget) {
        List<ListWidget> chitLists = listWidget.getAllDescendantComponentsOf(ListWidget.class);
        for (ListWidget chitList : chitLists) {
            if (chitList.getConfigureName() == null) {
                continue;
            }

            String name = chitList.getConfigureName().trim();

            if (name.equals("Tokens")) {
                loadTokens(chitList);
            } else if (obstacleTabNames.contains(name)) {
                loadObstacles(chitList);
            }
        }

    }

    private void loadObstacles(ListWidget chitList) {
        List<PieceSlot> tokenSlots = chitList.getAllDescendantComponentsOf(PieceSlot.class);
        for (PieceSlot tokenSlot : tokenSlots) {
            if (tokenSlot.getConfigureName() == null) {
                continue;
            }

            String obstacleName = tokenSlot.getConfigureName().trim();
            Obstacles obstacle = Obstacles.forVassalName(obstacleName);
            obstaclesPiecesMap.put(obstacle, tokenSlot);
        }
    }

    private void loadTokens(ListWidget listWidget) {
        List<PieceSlot> tokenSlots = listWidget.getAllDescendantComponentsOf(PieceSlot.class);
        for (PieceSlot tokenSlot : tokenSlots) {
            String tokenName = Canonicalizer.getCleanedName(tokenSlot.getConfigureName());
            Tokens2e token = null;
            try {
                token = Tokens2e.valueOf(tokenName);
            } catch (Exception e) {
                Util.logToChat("Couldn't find token: " + tokenName);
                continue;
            }

            tokenPiecesMap.put(token, tokenSlot);
        }
    }

    private void loadUpgrades(ListWidget listWidget) {
        String upgradeType = Canonicalizer.getCanonicalUpgradeTypeName(listWidget.getConfigureName());
        List<PieceSlot> upgrades = listWidget.getAllDescendantComponentsOf(PieceSlot.class);

        for (PieceSlot upgrade : upgrades) {
            String upgradeName = Canonicalizer.getCanonicalUpgradeName(upgradeType, upgrade.getConfigureName());

            String mapKey = getUpgradeMapKey(upgradeType, upgradeName);
            VassalXWSPilotPieces2e.Upgrade upgradePiece = new VassalXWSPilotPieces2e.Upgrade(upgradeName, upgrade);

            MasterUpgradeData.UpgradeData upgradeData = MasterUpgradeData.getUpgradeData(upgradeName);
            if (upgradeData != null) {
                upgradePiece.setUpgradeData(upgradeData);
            }

            upgradePiecesMap.put(mapKey, upgradePiece);
        }
    }



    private void loadPilots(ListWidget shipList, ListParentType faction, List<XWS2Pilots> allShips) {

        if (faction != ListParentType.rebel && faction != ListParentType.scum && faction != ListParentType.imperial) {
            return;
        }

        String shipName = Canonicalizer.getCanonicalShipName(shipList.getConfigureName());

        if (shipName.equals("gr75transport") || shipName.startsWith("gozanticlasscruiser") || shipName.equals("croccruiser")) {
            // TODO: Make GR75, Gozanti, and croc ship slot name start with 'ship --'
            return;
        }

        PieceSlot defaultShip = null;
        Map<String, PieceSlot> altArtShips = Maps.newHashMap();
        PieceSlot dial = null;
        PieceSlot movementCard = null;
        PieceSlot movementStrip = null;
        PieceSlot openDial = null;
        List<PieceSlot> slots = shipList.getAllDescendantComponentsOf(PieceSlot.class);
        List<PieceSlot> pilots = new LinkedList<PieceSlot>();
        for (PieceSlot slot : slots) {
            String slotName = slot == null ? "" : slot.getConfigureName();
            if (slotName.startsWith("=")) {
                continue;
            }
            if (slotName.startsWith("ship") && defaultShip == null) {
                defaultShip = slot;
                continue;
            }
            if (slotName.startsWith("ship") && defaultShip != null) {
                altArtShips.put(slotName, slot);
                continue;
            }
            if (slotName.startsWith("dial")) {
                dial = slot;
                continue;
            }
            if (slotName.startsWith("Ordered Open Dial")) {
                openDial = slot;
                continue;
            }
            if (slotName.startsWith("Ordered maneuver")) {
                movementStrip = slot;
                continue;
            }
            if (slotName.startsWith("movement")) {
                movementCard = slot;
                continue;
            }
            // Must be a pilot if all is well
            pilots.add(slot);
        }

        XWS2Pilots shipData = XWS2Pilots.getSpecificShip(shipName, allShips);

        for (PieceSlot pilot : pilots) {
            String pilotName = Canonicalizer.getCanonicalPilotName(pilot.getConfigureName());

            XWS2Pilots.Pilot2e pilotData = XWS2Pilots.getSpecificPilot(pilot.getConfigureName(), allShips);

            String mapKey = getPilotMapKey(faction.name(), shipName, pilotName);

            VassalXWSPilotPieces2e pilotPieces = new VassalXWSPilotPieces2e();
            pilotPieces.setShipData(shipData);
            pilotPieces.setDial(dial);
            pilotPieces.setShip(AltArtShipPicker.getAltArtShip(pilotName, altArtShips, defaultShip));
            pilotPieces.setMovementCard(movementCard);
            pilotPieces.setPilotCard(pilot);
            pilotPieces.setMovementStrip(movementStrip);
            pilotPieces.setOpenDial(openDial);
            pilotPieces.setPilotData(pilotData);
            pilotPiecesMap.put(mapKey, pilotPieces);
        }
    }

    private String getPilotMapKey(String faction, String shipName, String pilotName) {
        return String.format("%s/%s/%s", faction, shipName, pilotName);
    }

    private String getUpgradeMapKey(String upgradeType, String upgradeName) {
        return String.format("%s/%s", upgradeType, upgradeName);
    }

    public List<String> validateAgainstRemote(List<XWS2Pilots> allShips) {
        loadPieces(allShips);
        XWSMasterPilots masterPilots = XWSMasterPilots.loadFromRemote();

        List<String> missingKeys = Lists.newArrayList();

        for (XWSMasterPilots.FactionPilots factionPilots : Lists.newArrayList(masterPilots.rebel, masterPilots.scum, masterPilots.imperial)) {
            for (String shipName : factionPilots.ships.keySet()) {
                for (String pilotName : factionPilots.ships.get(shipName).pilots.keySet()) {
                    String pieceKey = getPilotMapKey(Canonicalizer.getCleanedName(factionPilots.name), shipName, pilotName);
                    if (!this.pilotPiecesMap.containsKey(pieceKey)) {
                        missingKeys.add(pieceKey);
                        Util.logToChat("Missing pilot: " + pieceKey);
                    }
                }
            }
        }

        Map<String, XWSMasterUpgrades.UpgradeType> masterUpgrades = XWSMasterUpgrades.loadFromRemote();
        for (String upgradeType : masterUpgrades.keySet()) {
            for (String upgradeName : masterUpgrades.get(upgradeType).upgrades.keySet()) {
                String pieceKey = getUpgradeMapKey(upgradeType, upgradeName);
                if (!upgradePiecesMap.containsKey(pieceKey)) {
                    missingKeys.add(pieceKey);
                    Util.logToChat("Missing upgrade: " + pieceKey);
                }
            }
        }

        return missingKeys;
    }

    private enum ListParentType {
        rebel("Rebel"),
        scum("Scum & Villainy"),
        imperial("Imperial"),
        upgrades("Upgrades"),
        chits("Chits");

        private String widgetName;

        ListParentType(String widgetName) {
            this.widgetName = widgetName;
        }

        public static ListParentType fromTab(Widget widget) {
            if (widget == null || widget.getConfigureName() == null) {
                return null;
            }
            for (ListParentType parent : values()) {
                if (widget.getConfigureName().contains(parent.widgetName)) {
                    return parent;
                }
            }
            return null;
        }
    }
}
