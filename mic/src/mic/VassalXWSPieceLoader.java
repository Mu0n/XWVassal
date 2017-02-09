package mic;

import VASSAL.build.GameModule;
import VASSAL.build.Widget;
import VASSAL.build.widget.ListWidget;
import VASSAL.build.widget.PieceSlot;
import VASSAL.build.widget.TabWidget;

import java.util.*;

/**
 * Created by amatheny on 2/8/17.
 */
public class VassalXWSPieceLoader {

    private static String invalidCanonicalCharPattern = "[^a-zA-Z0-9]";
    Map<String, PilotPieces> pilotPieces = null;
    Map<String, PieceSlot> upgradePieces = null;

    public VassalXWSListPieces loadListFromXWS(XWSList list) {
        if (pilotPieces == null || upgradePieces == null) {
            loadPieces();
        }
        return null;
    }

    private void loadPieces() {
        pilotPieces = new HashMap<String, PilotPieces>();
        upgradePieces = new HashMap<String, PieceSlot>();

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
        String upgradeType = getCanonicalName(listWidget.getConfigureName());
        upgradeType = handleUpgradeTypeExceptions(upgradeType);
        List<PieceSlot> upgrades = listWidget.getAllDescendantComponentsOf(PieceSlot.class);
        for (PieceSlot upgrade : upgrades) {
            String upgradeName = getCanonicalName(upgrade.getConfigureName());
            upgradeName = handleUpgradeNameExceptions(upgradeName);
            String mapKey = getUpgradeMapKey(upgradeType, upgradeName);
            upgradePieces.put(mapKey, upgrade);
        }
    }

    private void loadPilots(ListWidget shipList, ListParentType faction) {
        if (faction != ListParentType.rebel && faction != ListParentType.scum && faction != ListParentType.imperial) {
            return;
        }

        String shipName = getCanonicalName(shipList.getConfigureName());
        shipName = handleShipNameExceptions(shipName);

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
            String pilotName = getCanonicalName(pilot.getConfigureName());
            pilotName = handlePilotNameExceptions(pilotName);
            String mapKey = getPilotMapKey(faction.name(), shipName, pilotName);
            pilotPieces.put(mapKey, new PilotPieces(dial, movementCard, movementStrip, openDial, ship, pilot));
        }
    }

    private String getPilotMapKey(String faction, String shipName, String pilotName) {
        return String.format("%s/%s/%s", faction, shipName, pilotName);
    }

    private String getUpgradeMapKey(String upgradeType, String upgradeName) {
        return String.format("%s/%s", upgradeType, upgradeName);
    }

    private String handleShipNameExceptions(String name) {
        //TODO: Handle cases where ship names end in s (i.e. B-Wings, E-Wings, etc) or change them in module
        return name;
    }

    private String handlePilotNameExceptions(String name) {
        //TODO: Handle duplicate pilots in same ship
        return name;
    }


    private String handleUpgradeNameExceptions(String name) {
        //TODO: Handle duplicate upgrade names ie R2D2
        return name;
    }

    private String handleUpgradeTypeExceptions(String name) {
        //TODO: Switch from names like bombs to bomb etc.
        return name;
    }


    private String getCanonicalName(String name) {
        if (name == null) {
            return "";
        }
        return name.replaceAll(invalidCanonicalCharPattern, "").toLowerCase();
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
            if (widget == null) {
                return null;
            }
            for (ListParentType parent : values()) {
                if (parent.widgetName.equals(widget.getConfigureName())) {
                    return parent;
                }
            }
            return null;
        }
    }

    class PilotPieces {
        PieceSlot ship;
        PieceSlot dial;
        PieceSlot movement;
        PieceSlot pilot;
        PieceSlot openDial;
        PieceSlot movementStrip;

        public PilotPieces(PieceSlot dial, PieceSlot movement, PieceSlot movementStrip, PieceSlot openDial, PieceSlot ship, PieceSlot pilot) {
            this.dial = dial;
            this.movement = movement;
            this.ship = ship;
            this.pilot = pilot;
            this.movementStrip = movementStrip;
            this.openDial = openDial;
        }
    }
}
