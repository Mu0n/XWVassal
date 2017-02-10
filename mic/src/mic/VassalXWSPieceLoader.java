package mic;

import VASSAL.build.GameModule;
import VASSAL.build.Widget;
import VASSAL.build.widget.ListWidget;
import VASSAL.build.widget.PieceSlot;
import VASSAL.build.widget.TabWidget;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.util.*;

/**
 * Created by amatheny on 2/8/17.
 */
public class VassalXWSPieceLoader {

    private static String invalidCanonicalCharPattern = "[^a-zA-Z0-9]";
    Map<String, VassalXWSPilotPieces> pilotPiecesMap = null;
    Map<String, PieceSlot> upgradePiecesMap = null;

    public VassalXWSListPieces loadListFromXWS(XWSList list) {
        if (pilotPiecesMap == null || upgradePiecesMap == null) {
            loadPieces();
        }

        VassalXWSListPieces pieces = new VassalXWSListPieces();

        //has to check that at least 1 pilot is in the list
        if (!list.getPilots().isEmpty())
        {
            for (XWSList.XWSPilot pilot : list.getPilots()) {
                String pilotKey = getPilotMapKey(list.getFaction(), pilot.getShip(), pilot.getName());
                VassalXWSPilotPieces barePieces = this.pilotPiecesMap.get(pilotKey);
                if (barePieces == null) {
                    Util.logToChat("Could not find pilot: " + pilotKey);
                    continue;
                }

                VassalXWSPilotPieces pilotPieces = new VassalXWSPilotPieces(barePieces);

                //has to check that at least 1 upgrade is tied to the pilot
                try
                {
                    for(String upgradeType: pilot.getUpgrades().keySet()) {
                        for(String upgradeName : pilot.getUpgrades().get(upgradeType)) {
                            String upgradeKey = getUpgradeMapKey(upgradeType, upgradeName);
                            PieceSlot upgrade = upgradePiecesMap.get(upgradeKey);
                            if (upgrade == null) {
                                Util.logToChat("Could not find upgrade: " + upgradeKey);
                                continue;
                            }
                            pilotPieces.getUpgrades().add(upgrade);
                        }
                    }
                } catch(Exception e) {

                }
                pieces.getShips().add(pilotPieces);
            }
        }

        return pieces;
    }

    private void loadPieces() {
        pilotPiecesMap = new HashMap<String, VassalXWSPilotPieces>();
        upgradePiecesMap = new HashMap<String, PieceSlot>();

        List<ListWidget> listWidgets = GameModule.getGameModule().getAllDescendantComponentsOf(ListWidget.class);
        for (ListWidget listWidget : listWidgets) {
            if (!(listWidget.getParent() instanceof TabWidget)) {
                continue;
            }
            ListParentType parentType = ListParentType.fromTab(listWidget.getParent());
            if (parentType == null) {
                Util.logToChat("Skipping tab widget: " + listWidget.getParent().getConfigureName());
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
        String upgradeType = getCleanedName(listWidget.getConfigureName());
        upgradeType = NameFixes.fixUpgradeTypeName(upgradeType);
        List<PieceSlot> upgrades = listWidget.getAllDescendantComponentsOf(PieceSlot.class);
        for (PieceSlot upgrade : upgrades) {
            String upgradeName = getCleanedName(upgrade.getConfigureName());
            upgradeName = NameFixes.fixUpgradeName(upgradeType, upgradeName);
            String mapKey = getUpgradeMapKey(upgradeType, upgradeName);
            upgradePiecesMap.put(mapKey, upgrade);
        }
    }

    private void loadPilots(ListWidget shipList, ListParentType faction) {
        if (faction != ListParentType.rebel && faction != ListParentType.scum && faction != ListParentType.imperial) {
            return;
        }

        String shipName = getCleanedName(shipList.getConfigureName());
        shipName = NameFixes.fixShipName(shipName);

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

        for (PieceSlot pilot : pilots) {
            String pilotName = getCleanedName(pilot.getConfigureName());
            pilotName = NameFixes.fixPilotName(pilotName);
            String mapKey = getPilotMapKey(faction.name(), shipName, pilotName);
            VassalXWSPilotPieces pilotPieces = new VassalXWSPilotPieces();
            pilotPieces.setDial(dial);
            pilotPieces.setShip(ship);
            pilotPieces.setMovementCard(movementCard);
            pilotPieces.setPilotCard(pilot);
            pilotPieces.setMovementStrip(movementStrip);
            pilotPieces.setOpenDial(openDial);
            pilotPiecesMap.put(mapKey, pilotPieces);
        }
    }

    private String getPilotMapKey(String faction, String shipName, String pilotName) {
        return String.format("%s/%s/%s", faction, shipName, pilotName);
    }

    private String getUpgradeMapKey(String upgradeType, String upgradeName) {
        return String.format("%s/%s", upgradeType, upgradeName);
    }


    private String getCleanedName(String name) {
        if (name == null) {
            return "";
        }
        return name.replaceAll(invalidCanonicalCharPattern, "").toLowerCase();
    }

    public List<String> validateAgainstRemote() {
        loadPieces();
        XWSMasterPilots masterPilots = XWSMasterPilots.loadFromRemote();

        List<String> missingKeys = Lists.newArrayList();

        for (XWSMasterPilots.FactionPilots factionPilots : Lists.newArrayList(masterPilots.rebel, masterPilots.scum, masterPilots.imperial)) {
            for (String shipName : factionPilots.ships.keySet()) {
                for (String pilotName : factionPilots.ships.get(shipName).pilots.keySet()) {
                    String pieceKey = getPilotMapKey(getCleanedName(factionPilots.name), shipName, pilotName);
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
