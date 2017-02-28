package mic;

import VASSAL.build.GameModule;
import VASSAL.build.Widget;
import VASSAL.build.widget.ListWidget;
import VASSAL.build.widget.PieceSlot;
import VASSAL.build.widget.TabWidget;
import com.google.common.collect.*;

import java.util.*;

/**
 * Created by amatheny on 2/8/17.
 */
public class VassalXWSPieceLoader {

    Map<String, VassalXWSPilotPieces> pilotPiecesMap = null;
    Map<String, VassalXWSPilotPieces.Upgrade> upgradePiecesMap = null;
    Map<Tokens, PieceSlot> tokenPiecesMap = null;

    public VassalXWSListPieces loadListFromXWS(XWSList list) {
        if (pilotPiecesMap == null || upgradePiecesMap == null || tokenPiecesMap == null) {
            loadPieces();
        }

        VassalXWSListPieces pieces = new VassalXWSListPieces();

        Multiset<String> pilotCounts = HashMultiset.create();
        for (XWSList.XWSPilot pilot : list.getPilots()) {
            pilotCounts.add(pilot.getName());
        }

        Multiset<String> genericPilotsAdded = HashMultiset.create();

        for (XWSList.XWSPilot pilot : list.getPilots()) {
            String pilotKey = getPilotMapKey(list.getFaction(), pilot.getShip(), pilot.getName());
            VassalXWSPilotPieces barePieces = this.pilotPiecesMap.get(pilotKey);
            if (barePieces == null) {
                Util.logToChat("Could not find pilot: " + pilotKey);
                continue;
            }

            VassalXWSPilotPieces pilotPieces = new VassalXWSPilotPieces(barePieces);

            if (pilotPieces.getPilotData() != null) {
                List<PieceSlot> foundConditions = getConditionsForCard(pilotPieces.getPilotData().getConditions());
                pilotPieces.getConditions().addAll(foundConditions);
            }

            if (pilotCounts.count(pilot.getName()) > 1) {
                genericPilotsAdded.add(pilot.getName());
                pilotPieces.setShipNumber(genericPilotsAdded.count(pilot.getName()));
            }

            for(String upgradeType: pilot.getUpgrades().keySet()) {
                for(String upgradeName : pilot.getUpgrades().get(upgradeType)) {
                    String upgradeKey = getUpgradeMapKey(upgradeType, upgradeName);
                    VassalXWSPilotPieces.Upgrade upgrade = upgradePiecesMap.get(upgradeKey);
                    if (upgrade == null) {
                        Util.logToChat("Could not find upgrade: " + upgradeKey);
                        continue;
                    }

                    if (upgrade.getUpgradeData() != null) {
                        List<PieceSlot> foundConditions = getConditionsForCard(upgrade.getUpgradeData().getConditions());
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
        }

        return pieces;
    }

    private List<PieceSlot> getConditionsForCard(List<String> conditions) {
        List<PieceSlot> conditionSlots = Lists.newArrayList();
        for(String conditionName : conditions) {
            String canonicalConditionName = Canonicalizer.getCanonicalUpgradeName(
                    "conditions", conditionName);
            String mapKey = getUpgradeMapKey("conditions", canonicalConditionName);
            VassalXWSPilotPieces.Upgrade condition = this.upgradePiecesMap.get(mapKey);
            if (condition == null) {
                Util.logToChat("Unable to load condition: " + conditionName);
                continue;
            }
            conditionSlots.add(condition.getPieceSlot());
        }
        return conditionSlots;
    }

    private void loadPieces() {
        pilotPiecesMap = Maps.newHashMap();
        upgradePiecesMap = Maps.newHashMap();
        tokenPiecesMap = Maps.newHashMap();

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
            if (chitList.getConfigureName() != null && chitList.getConfigureName().equals("Tokens")) {
                loadTokens(chitList);
            }
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

            MasterPilotData.PilotData pilotData = MasterPilotData.getPilotData(shipName, pilotName);

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
        for(String upgradeType : masterUpgrades.keySet()) {
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
