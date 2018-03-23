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
 * Created by amatheny on 2/8/17.
 */
public class VassalXWSPieceLoader {
    private static final String STEM_UPGRADE_SLOT_NAME = "Stem Upgrade";
    private static Map<String, String> stemUpgradeSlotNames = ImmutableMap.<String, String>builder()
            .put("turret","Stem Upgrade Turret")
            .put("torpedo","Stem Upgrade Torpedo")
            .put("amd","Stem Upgrade Astromech")
            .put("ept","Stem Upgrade Elite")
            .put("missile","Stem Upgrade Missile")
            .put("crew","Stem Upgrade Crew")
            .put("cannon","Stem Upgrade Cannon")
            .put("bomb","Stem Upgrade Bomb")
            .put("illicit","Stem Upgrade Illicit")
            .put("mod","Stem Upgrade Modification")
            .put("samd","Stem Upgrade Salvaged Astromech")
            .put("system","Stem Upgrade System")
            .put("tech","Stem Upgrade Tech")
            .put("title","Stem Upgrade Title")
            .build();

    private static List<String> obstacleTabNames = Lists.newArrayList(
            "Asteroids", "New Asteroids", "Debris"
    );

    Map<String, VassalXWSPilotPieces> pilotPiecesMap = Maps.newHashMap();
    Map<String, VassalXWSPilotPieces.Upgrade> upgradePiecesMap = Maps.newHashMap();
    Map<Tokens, PieceSlot> tokenPiecesMap = Maps.newHashMap();
    Map<Obstacles, PieceSlot> obstaclesPiecesMap = Maps.newHashMap();

    public VassalXWSListPieces loadListFromXWS(XWSList list) {
        if (pilotPiecesMap.isEmpty() || upgradePiecesMap.isEmpty()
                || tokenPiecesMap.isEmpty()|| obstaclesPiecesMap.isEmpty()) {
            loadPieces();
        }

        VassalXWSListPieces pieces = new VassalXWSListPieces();

        Multiset<String> pilotCounts = HashMultiset.create();
        for (XWSList.XWSPilot pilot : list.getPilots()) {
            pilotCounts.add(pilot.getName());
        }

        Multiset<String> genericPilotsAdded = HashMultiset.create();

        for (XWSList.XWSPilot pilot : list.getPilots())
        {

            String xwsShip = Canonicalizer.getCanonicalShipName(pilot.getShip());
            MasterShipData.ShipData shipData = MasterShipData.getShipData(pilot.getShip());


            MasterPilotData.PilotData pilotData = MasterPilotData.getPilotData(xwsShip,pilot.getName(),list.getFaction() );


            // generate the pilot card
            VassalXWSPilotPieces barePieces = new VassalXWSPilotPieces();
            barePieces.setPilotData(pilotData);
            barePieces.setShipData(shipData);

            // Add the pilot card
            // get the pilot card slot
            PieceSlot stemPilotSlot = null;


            // add the stem ship
            PieceSlot smallShipSlot = null;
            PieceSlot largeShipSlot = null;

            // stem upgrade
            PieceSlot stemUpgradeSlot = null;
            PieceSlot stemConditionSlot = null;
            PieceSlot stemConditionTokenSlot = null;

            // ==================================================
            // Get the stem slots
            // ==================================================
            List<PieceSlot> stempPieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

            for (PieceSlot pieceSlot : stempPieceSlots) {
                String slotName = pieceSlot.getConfigureName();
                if (slotName.startsWith("Stem Pilot") && stemPilotSlot == null) {
                    stemPilotSlot = pieceSlot;
                    continue;

                } else if(slotName.startsWith("ship -- Nu Stem Small Ship")&& smallShipSlot == null)
                {
                    smallShipSlot = pieceSlot;
                    continue;
                } else if(slotName.startsWith("ship -- Nu Stem Large Ship")&& largeShipSlot == null) {
                    largeShipSlot = pieceSlot;
                    continue;
                }else if(slotName.equals("Stem Upgrade") && stemUpgradeSlot == null)
                {
                    stemUpgradeSlot = pieceSlot;
                    continue;
                }else if(slotName.equals("Stem Condition") && stemConditionSlot == null)
                {
                    stemConditionSlot = pieceSlot;
                    continue;
                }else if(slotName.equals("Stem Condition Token") && stemConditionTokenSlot == null)
                {
                    stemConditionTokenSlot = pieceSlot;
                    continue;
                }

            }

            // fill in the pilot cards
            barePieces.setPilotCard(stemPilotSlot);

            // fill in the ships
            if(shipData.getSize().equals("small"))
            {
                barePieces.setShip(smallShipSlot);
            }else if(shipData.getSize().equals("large")) {
                barePieces.setShip(largeShipSlot);
            }

            VassalXWSPilotPieces pilotPieces = new VassalXWSPilotPieces(barePieces);

            if (pilotPieces.getPilotData() != null) {
                List<VassalXWSPilotPieces.Upgrade> foundConditions = getConditionsForCard(pilotPieces.getPilotData().getConditions(),stemConditionSlot);
                pilotPieces.getConditions().addAll(foundConditions);
            }

            if (pilotCounts.count(pilot.getName()) > 1) {
                genericPilotsAdded.add(pilot.getName());
                pilotPieces.setShipNumber(genericPilotsAdded.count(pilot.getName()));
            }

            // ==================================================
            // Upgrades
            // ==================================================
            for (String upgradeType : pilot.getUpgrades().keySet())
            {
                for (String upgradeName : pilot.getUpgrades().get(upgradeType))
                {
                    String upgradeKey = getUpgradeMapKey(upgradeType, upgradeName);
                    VassalXWSPilotPieces.Upgrade upgrade = upgradePiecesMap.get(upgradeKey);

                    upgrade = new VassalXWSPilotPieces.Upgrade(upgradeName, stemUpgradeSlot);
                    MasterUpgradeData.UpgradeData newUpgradeData = MasterUpgradeData.getUpgradeData(upgradeName);
                    upgrade.setUpgradeData(newUpgradeData);



                    /*
                    if (upgrade == null)
                    {
                        Util.logToChat("Upgrade was null");
                        // Upgrade wasn't found.  Check xws-data and dispatcher
                        MasterUpgradeData.UpgradeData newUpgradeData = MasterUpgradeData.getUpgradeData(upgradeName);

                        if(newUpgradeData != null)
                        {
                           // Util.logToChat("Upgrade "+upgradeName + " is not yet included in XWVassal.  Generating it.");
                            //String slotName = stemUpgradeSlotNames.get(upgradeType);

                           // if(slotName == null)
                          //  {
                          //      // this might be a new upgrade type, so use the WIP version
                          //      slotName = "Stem Upgrade WIP";
                          //  }

                         //   List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

                          //  for (PieceSlot pieceSlot : pieceSlots) {
                          //      String stemUpgradeSlotName = pieceSlot.getConfigureName();
                          //      if (slotName.equals(stemUpgradeSlotName)) {

                             //       // this is the correct slot
                                    upgrade = new VassalXWSPilotPieces.Upgrade(upgradeName, stemUpgradeSlot);
                                    upgrade.setUpgradeData(newUpgradeData);

                                    continue;
                             //   }
                         //   }

                        }else {
                            Util.logToChat("Could not find upgrade: " + upgradeName);
                            continue;
                        }
                    }*/

                    if (upgrade.getUpgradeData() != null) {
                        List<VassalXWSPilotPieces.Upgrade> foundConditions = getConditionsForCard(upgrade.getUpgradeData().getConditions(),stemConditionSlot);
                        pilotPieces.getConditions().addAll(foundConditions);
                    }

                    pilotPieces.getUpgrades().add(upgrade);
                }
            }

            List<Tokens> tokens = Tokens.loadForPilot(pilotPieces);
            for (Tokens token : tokens) {
                PieceSlot tokenSlot = tokenPiecesMap.get(token);
                if (tokenSlot != null) {
                    pilotPieces.getTokens().put(token, tokenSlot);
                }
            }

            pieces.getShips().add(pilotPieces);

            // find the stem pilot card for the correct faction
            // set the stats
            // set the image



           // String pilotKey = getPilotMapKey(list.getFaction(), pilot.getShip(), pilot.getName());
           // VassalXWSPilotPieces barePieces = this.pilotPiecesMap.get(pilotKey);
 /*
            if (barePieces == null) {

                // Pilot wasn't found in the pallet.  Need to check to see if it exists in xws-data or the dispatcher
                String xwsShip = Canonicalizer.getCanonicalShipName(pilot.getShip());
                MasterShipData.ShipData shipData = MasterShipData.getShipData(pilot.getShip());
                MasterPilotData.PilotData pilotData = MasterPilotData.getPilotData(xwsShip,pilot.getName() );

                if(pilotData != null && shipData != null)
                {

                    Util.logToChat("Ship "+pilot.getShip() + " is not yet included in XWVassal.  Generating it.");
                    Util.logToChat("Pilot "+xwsShip + "/" + pilot.getName() + " is not yet included in XWVassal.  Generating it.");
                    // The pilot and ship did exist in either XWS-data or dispatcher
                    // these will now be generated

                    // generate the pilot card
                    barePieces = new VassalXWSPilotPieces();
                    barePieces.setPilotData(pilotData);
                    barePieces.setShipData(shipData);

                    // get the pilot card slot
                    PieceSlot rebelCardSlot = null;
                    PieceSlot resistanceCardSlot = null;
                    PieceSlot empireCardSlot = null;
                    PieceSlot firstOrderCardSlot = null;
                    PieceSlot scumOrderCardSlot = null;

                    // add the stem ship
                    PieceSlot smallShipSlot = null;
                    PieceSlot largeShipSlot = null;


                    // Add the stem pilot card
                    List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

                    for (PieceSlot pieceSlot : pieceSlots) {
                        String slotName = pieceSlot.getConfigureName();
                        if (slotName.startsWith("Stem Rebel Pilot") && rebelCardSlot == null) {
                            rebelCardSlot = pieceSlot;
                            continue;
                        } else if (slotName.startsWith("Stem Emperial Pilot") && empireCardSlot == null) {
                            empireCardSlot = pieceSlot;
                            continue;
                        } else if (slotName.startsWith("Stem S&V Pilot") && scumOrderCardSlot == null) {
                            scumOrderCardSlot = pieceSlot;
                            continue;
                        } else if (slotName.startsWith("Stem First Order Pilot") && firstOrderCardSlot == null) {
                            firstOrderCardSlot = pieceSlot;
                            continue;
                        } else if (slotName.startsWith("Stem Resistance Pilot") && resistanceCardSlot == null) {
                            resistanceCardSlot = pieceSlot;
                            continue;
                        } else if(slotName.startsWith("ship -- Nu Stem Small Ship")&& smallShipSlot == null)
                        {
                            smallShipSlot = pieceSlot;
                            continue;
                        } else if(slotName.startsWith("ship -- Nu Stem Large Ship")&& largeShipSlot == null) {
                            largeShipSlot = pieceSlot;
                            continue;
                        }

                    }

                    // fill in the pilot cards
                    if (pilotData.getFaction().equals("Rebel Alliance"))
                    {
                        barePieces.setPilotCard(rebelCardSlot);
                    } else if (pilotData.getFaction().equals("Resistance"))
                    {
                        barePieces.setPilotCard(resistanceCardSlot);
                    }else if(pilotData.getFaction().equals("Galactic Empire"))
                    {
                        barePieces.setPilotCard(empireCardSlot);
                    }else if(pilotData.getFaction().equals("First Order"))
                    {
                        barePieces.setPilotCard(firstOrderCardSlot);
                    } else if(pilotData.getFaction().equals("Scum & Villainy"))
                    {
                        barePieces.setPilotCard(scumOrderCardSlot);
                    }

                    // fill in the ships

                    if(shipData.getSize().equals("small"))
                    {
                        barePieces.setShip(smallShipSlot);
                    }else if(shipData.getSize().equals("large")) {
                        barePieces.setShip(largeShipSlot);
                    }


                }else{

                    if(pilotData == null)
                    {
                        Util.logToChat("Could not find pilot: " + xwsShip + "/" + pilot.getName());
                    }

                    if(shipData == null)
                    {
                        Util.logToChat("Could not find ship: " + pilot.getShip());
                    }

                    continue;
                }

            }

           // VassalXWSPilotPieces pilotPieces = new VassalXWSPilotPieces(barePieces);

            if (pilotPieces.getPilotData() != null) {
                List<VassalXWSPilotPieces.Upgrade> foundConditions = getConditionsForCard(pilotPieces.getPilotData().getConditions());
                pilotPieces.getConditions().addAll(foundConditions);
            }

            if (pilotCounts.count(pilot.getName()) > 1) {
                genericPilotsAdded.add(pilot.getName());
                pilotPieces.setShipNumber(genericPilotsAdded.count(pilot.getName()));
            }

            for (String upgradeType : pilot.getUpgrades().keySet())
            {
                for (String upgradeName : pilot.getUpgrades().get(upgradeType)) {
                    String upgradeKey = getUpgradeMapKey(upgradeType, upgradeName);
                    VassalXWSPilotPieces.Upgrade upgrade = upgradePiecesMap.get(upgradeKey);
                    if (upgrade == null)
                    {

                        // Upgrade wasn't found in the pallet.  Check xws-data and dispatcher
                        MasterUpgradeData.UpgradeData newUpgradeData = MasterUpgradeData.getUpgradeData(upgradeName);

                        if(newUpgradeData != null)
                        {
                            Util.logToChat("Upgrade "+upgradeName + " is not yet included in XWVassal.  Generating it.");
                            String slotName = stemUpgradeSlotNames.get(upgradeType);

                            if(slotName == null)
                            {
                                // this might be a new upgrade type, so use the WIP version
                                slotName = "Stem Upgrade WIP";
                            }

                            List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

                            for (PieceSlot pieceSlot : pieceSlots) {
                                String stemUpgradeSlotName = pieceSlot.getConfigureName();
                                if (slotName.equals(stemUpgradeSlotName)) {

                                    // this is the correct slot
                                    upgrade = new VassalXWSPilotPieces.Upgrade(upgradeName, pieceSlot);
                                    upgrade.setUpgradeData(newUpgradeData);

                                    continue;
                                }
                            }

                        }else {
                            Util.logToChat("Could not find upgrade: " + upgradeName);
                            continue;
                        }
                    }

                    if (upgrade.getUpgradeData() != null) {
                        List<VassalXWSPilotPieces.Upgrade> foundConditions = getConditionsForCard(upgrade.getUpgradeData().getConditions());
                        pilotPieces.getConditions().addAll(foundConditions);
                    }

                    pilotPieces.getUpgrades().add(upgrade);
                }
            }

            List<Tokens> tokens = Tokens.loadForPilot(pilotPieces);
            for (Tokens token : tokens) {
                PieceSlot tokenSlot = tokenPiecesMap.get(token);
                if (tokenSlot != null) {
                    pilotPieces.getTokens().put(token, tokenSlot);
                }
            }

            pieces.getShips().add(pilotPieces);
            */
        }

        for (String xwsObstacleName : list.getObstacles()) {
            Obstacles obstacle = Obstacles.forXwsName(xwsObstacleName);
            if (!obstaclesPiecesMap.containsKey(obstacle)) {
                Util.logToChat("Unable to find vassal obstacle for xws obstacle '" + xwsObstacleName + "'");
                continue;
            }
            pieces.getObstacles().add(obstaclesPiecesMap.get(obstacle));
        }

        return pieces;
    }

    private List<VassalXWSPilotPieces.Upgrade> getConditionsForCard(List<String> conditions, PieceSlot stemConditionSlot)
    {
        List<VassalXWSPilotPieces.Upgrade> conditionSlots = Lists.newArrayList();
        for (String conditionName : conditions)
        {
            String canonicalConditionName = Canonicalizer.getCanonicalUpgradeName(
                    "conditions", conditionName);
            String mapKey = getUpgradeMapKey("conditions", canonicalConditionName);
            VassalXWSPilotPieces.Upgrade condition = this.upgradePiecesMap.get(mapKey);
            condition = new VassalXWSPilotPieces.Upgrade(conditionName, stemConditionSlot);
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

    public void loadPieces() {
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
                    loadPilots(listWidget, parentType);
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
            Tokens token = null;
            try {
                token = Tokens.valueOf(tokenName);
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
            VassalXWSPilotPieces.Upgrade upgradePiece = new VassalXWSPilotPieces.Upgrade(upgradeName, upgrade);

            MasterUpgradeData.UpgradeData upgradeData = MasterUpgradeData.getUpgradeData(upgradeName);
            if (upgradeData != null) {
                upgradePiece.setUpgradeData(upgradeData);
            }

            upgradePiecesMap.put(mapKey, upgradePiece);
        }
    }

    private void loadPilots(ListWidget shipList, ListParentType faction) {

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

        MasterShipData.ShipData shipData = MasterShipData.getShipData(shipName);

        for (PieceSlot pilot : pilots) {
            String pilotName = Canonicalizer.getCanonicalPilotName(pilot.getConfigureName());

            MasterPilotData.PilotData pilotData = MasterPilotData.getPilotData(shipName, pilotName, faction.name());

            String mapKey = getPilotMapKey(faction.name(), shipName, pilotName);

            VassalXWSPilotPieces pilotPieces = new VassalXWSPilotPieces();
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

    public List<String> validateAgainstRemote() {
        loadPieces();
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
