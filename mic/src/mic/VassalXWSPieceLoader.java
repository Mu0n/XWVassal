package mic;

import VASSAL.build.GameModule;
import VASSAL.build.Widget;
import VASSAL.build.widget.ListWidget;
import VASSAL.build.widget.PieceSlot;
import VASSAL.build.widget.TabWidget;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import java.util.*;

/**
 * Created by amatheny on 2/8/17.
 */
public class VassalXWSPieceLoader {

    Map<String, VassalXWSPilotPieces> pilotPiecesMap = null;
    Map<String, VassalXWSPilotPieces.Upgrade> upgradePiecesMap = null;

    public VassalXWSListPieces loadListFromXWS(XWSList list) {
        if (pilotPiecesMap == null || upgradePiecesMap == null) {
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
                    pilotPieces.getUpgrades().add(upgrade);
                }
            }
            pieces.getShips().add(pilotPieces);
        }

        return pieces;
    }

    private void loadPieces() {
        pilotPiecesMap = new HashMap<String, VassalXWSPilotPieces>();
        upgradePiecesMap = new HashMap<String, VassalXWSPilotPieces.Upgrade>();

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
                    // TODO: implement loading of obstacles/tokens/etc
                    continue;
                case upgrades:
                    loadUpgrades(listWidget);
                case imperial:
                case rebel:
                case scum:
                    loadPilots(listWidget, parentType);
                    break;
            }
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

        PieceSlot ship = null;
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
            if (slotName.startsWith("ship")) {
                // TODO: figure out how to determine which ship model to load
                ship = slot;
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
            pilotPieces.setShip(ship);
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
