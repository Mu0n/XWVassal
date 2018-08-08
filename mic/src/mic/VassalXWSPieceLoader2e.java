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


    Map<Tokens2e, PieceSlot> tokenPiecesMap = Maps.newHashMap();
    Map<Obstacles, PieceSlot> obstaclesPiecesMap = Maps.newHashMap();
    // Map<String, VassalXWSPilotPieces2e.Condition> conditionPiecesMap = Maps.newHashMap();

    public VassalXWSListPieces2e loadListFromXWS(XWSList2e list, List<XWS2Pilots> allPilots, List<XWS2Upgrades> allUpgrades) {

        VassalXWSListPieces2e pieces = new VassalXWSListPieces2e();
        Multiset<String> pilotCounts = HashMultiset.create();

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


    private void loadPilots(ListWidget shipList, ListParentType faction, List<XWS2Pilots> allShips) {

        Util.logToChat("PieceLoader2e line 293  attempting to loadPilots");
        if (faction != ListParentType.rebel && faction != ListParentType.scum && faction != ListParentType.imperial) {
            return;
        }

        String shipName = Canonicalizer.getCanonicalShipName(shipList.getConfigureName());
Util.logToChat("line 300 pieceloader2e shipName " + shipName);
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

        XWS2Pilots shipData = null;
        for(XWS2Pilots sh : allShips)
        {
            if(shipName.equals(Canonicalizer.getCleanedName(sh.getName()))) shipData = sh;
        }
        if(shipData == null) return;
        //Util.logToChat("pieceloader2e line 344  shipData " + ((shipData==null)?"is null":"is not null"));

        //cycles through all the pilot PieceSlots of the wanted pilots List and wishes to gather the loaded pilotData from xwing-data2 for that specific ship/pilot key
        for (PieceSlot pilot : pilots) {


            Util.logToChat("pieceloader2e line 347 pilot parse " + ((pilot==null)?"is null":"is not null"));

            String pilotName = Canonicalizer.getCanonicalPilotName(pilot.getConfigureName());

Util.logToChat("pieceloader2e line 347 right after getting getConfigureName for a pilot");
            //get the right pilotData from the same ship, but make sure to verify it's the right faction too!
            XWS2Pilots.Pilot2e pilotData = null;
            for(XWS2Pilots xws2pilot : allShips){
                Util.logToChat("pieceloader2e line 362 xws2.getFaction " + xws2pilot.getFaction() + " shipData.getFaction " + shipData.getFaction());
                if(xws2pilot.getFaction()!=shipData.getFaction()) continue;
                pilotData = XWS2Pilots.getSpecificPilot(pilot.getConfigureName(), allShips);
            }
            if(pilotData == null) {
                Util.logToChat("couldn't find the right pilot data");
                return;
            }
            Util.logToChat("pieceloader2e line 349 pilotConfigName = " + pilot.getConfigureName() + " pilotName " + pilotName + " pilotData Name " + pilotData.getName());
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
        }
    }

    private String getPilotMapKey(String faction, String shipName, String pilotName) {
        return String.format("%s/%s/%s", faction, shipName, pilotName);
    }

    private String getUpgradeMapKey(String upgradeType, String upgradeName) {
        return String.format("%s/%s", upgradeType, upgradeName);
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
