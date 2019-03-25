package mic;

import VASSAL.build.GameModule;
import VASSAL.build.Widget;
import VASSAL.build.widget.ListWidget;
import VASSAL.build.widget.PieceSlot;
import VASSAL.build.widget.TabWidget;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by mujuneau on 6/8/18.
 */
public class VassalXWSPieceLoader2e {

    private static List<String> obstacleTabNames = Lists.newArrayList(
            "Asteroids", "TFA_Asteroids", "Debris", "Gas_Clouds"
    );


    Map<String, PieceSlot> tokenPiecesMap = Maps.newHashMap();
    Map<Obstacles, PieceSlot> obstaclesPiecesMap = Maps.newHashMap();
    // Map<String, VassalXWSPilotPieces2e.Condition> conditionPiecesMap = Maps.newHashMap();

    public VassalXWSListPieces2e loadListFromXWS(XWSList2e list, List<XWS2Pilots> allPilots, XWS2Upgrades allUpgrades, List<XWS2Upgrades.Condition> allConditions) {

        if (tokenPiecesMap.isEmpty()|| obstaclesPiecesMap.isEmpty()) {
            loadPieces();
        }

        //the following object is the full structure shebang that'll get returned at the end
        VassalXWSListPieces2e pieces = new VassalXWSListPieces2e();

        //The following is used to get duplicate names for pilots, in order to know if it has to add a number to dials, e.g. ESP 1, ESP 2, etc.
        Multiset<String> pilotCounts = HashMultiset.create();
        for (XWSList2e.XWSPilot xwsList2ePilot : list.getPilots()) {
            pilotCounts.add(xwsList2ePilot.getXws());
        }
        //The following is used to keep track of multiples of generic pilot
        Multiset<String> genericPilotsAdded = HashMultiset.create();

        for (XWSList2e.XWSPilot pilot : list.getPilots())
        {
            //Using a unique pilot xws2 tag in order to search its specific ship data (which includes the faction and dial moves)
            // and pilot data (which includes ship abiltity
            String xwsPilotToSearch = pilot.getXws();

            XWS2Pilots shipData = XWS2Pilots.getSpecificShipFromPilotXWS2(xwsPilotToSearch, allPilots);
            XWS2Pilots.Pilot2e pilotData = XWS2Pilots.getSpecificPilot(xwsPilotToSearch, allPilots);

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
                if (slotName.startsWith("Stem2e Pilot") && stemPilotSlot == null) {
                    stemPilotSlot = pieceSlot;
                    continue;
                } else if(slotName.startsWith("ship -- Stem2e Small Ship")&& smallShipSlot == null) {
                    smallShipSlot = pieceSlot;
                    continue;
                } else if(slotName.startsWith("ship -- Stem2e Medium Ship")&& mediumShipSlot == null) {
                    mediumShipSlot = pieceSlot;
                    continue;
                }else if(slotName.startsWith("ship -- Stem2e Large Ship")&& largeShipSlot == null) {
                    largeShipSlot = pieceSlot;
                    continue;
                }else if(slotName.equals("Stem2e Upgrade") && stemUpgradeSlot == null) {
                    stemUpgradeSlot = pieceSlot;
                    continue;
                }else if(slotName.equals("Stem2e Condition") && stemConditionSlot == null) {
                    stemConditionSlot = pieceSlot;
                    continue;
                }else if(slotName.equals("Stem2e Condition Token") && stemConditionTokenSlot == null) {
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
            }else if(shipData.getSize().equals("medium")) {
                barePieces.setShip(mediumShipSlot);
            }else if(shipData.getSize().equals("large")) {
                barePieces.setShip(largeShipSlot);
            }

            VassalXWSPilotPieces2e pilotPieces = new VassalXWSPilotPieces2e(barePieces);

            //check if the current pilot is associated with conditions
            if(pilotData.getConditions()!=null && !pilotData.getConditions().isEmpty())
            {
                List<VassalXWSPilotPieces2e.Condition> foundConditions = getConditionsForCard(pilotData.getConditions(),stemConditionSlot, allConditions);
                pilotPieces.getConditions().addAll(foundConditions);
            }
            if (pilotCounts.count(pilot.getXws()) > 1) {
                genericPilotsAdded.add(pilot.getXws());
                pilotPieces.setShipNumber(genericPilotsAdded.count(pilot.getXws()));
            }

            // ==================================================
            // Upgrades
            // ==================================================
            for (String upgradeType : pilot.getUpgrades().keySet())
            {
                if(Canonicalizer.getCleanedName(upgradeType).equals("hardpoint")) continue;
                for (String upgradeName : pilot.getUpgrades().get(upgradeType))
                {
                    VassalXWSPilotPieces2e.Upgrade upgrade = new VassalXWSPilotPieces2e.Upgrade(upgradeName, stemUpgradeSlot);
                    XWS2Upgrades.OneUpgrade newUpData = allUpgrades.getSpecificUpgrade(upgradeName, allUpgrades);

                    if(upgrade==null || newUpData == null)
                    {
                        Util.logToChat("Autospawn 2.0 doesn't know what to do with this upgrade " + upgradeName + " of type " + upgradeType);
                        continue;
                    }
                    upgrade.setUpgradeData(newUpData);

                        if(newUpData.getSides().get(0).getConditions()!=null && !newUpData.getSides().get(0).getConditions().isEmpty())
                        {
                            List<VassalXWSPilotPieces2e.Condition> foundConditions = getConditionsForCard(newUpData.getSides().get(0).getConditions(),stemConditionSlot, allConditions);
                            pilotPieces.getConditions().addAll(foundConditions);
                        }

                    pilotPieces.getUpgrades().add(upgrade);
                }
            }

            List<String> tokens = TokensFromWeb.loadForPilot(pilotPieces);

            for (String token : tokens) {
                PieceSlot tokenSlot = tokenPiecesMap.get(token);
                if (tokenSlot != null) {
                    pilotPieces.getTokens().put(token, tokenSlot);
                }
            }
            pieces.getShips().add(pilotPieces);



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

    private List<VassalXWSPilotPieces2e.Condition> getConditionsForCard(List<String> conditions, PieceSlot stemConditionSlot, List<XWS2Upgrades.Condition> allConditions)
    {
        List<VassalXWSPilotPieces2e.Condition> conditionSlots = Lists.newArrayList();
        for (String conditionName : conditions)
        {
           // String canonicalConditionName = Canonicalizer.getCanonicalUpgradeName("conditions", conditionName);
          //  String mapKey = getUpgradeMapKey("conditions", canonicalConditionName);
           // M
           // VassalXWSPilotPieces.Condition condition = this.conditionPiecesMap.get(mapKey);

            XWS2Upgrades.Condition newConditionData = XWS2Upgrades.getSpecificConditionByXWS(conditionName, allConditions);
            VassalXWSPilotPieces2e.Condition condition = new VassalXWSPilotPieces2e.Condition(stemConditionSlot, conditionName,  newConditionData.getName());
            condition.setConditionData(newConditionData);
            conditionSlots.add(condition);
        }
        return conditionSlots;
    }

    public void loadPieces() {
        obstaclesPiecesMap = Maps.newHashMap();
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

                //this first case only used for rocks anymore
                case chits:
                    loadChits(listWidget);
                    break;
                case secondeditiontokens:
                case remotes:
                    load2eTokens(listWidget);
                    break;
            }
        }
    }

    private void load2eTokens(ListWidget listWidget) {
        List<PieceSlot> tokenSlots = listWidget.getAllDescendantComponentsOf(PieceSlot.class);
        for (PieceSlot tokenSlot : tokenSlots) {
            String tokenName = Canonicalizer.getCleanedName(tokenSlot.getConfigureName());
            String token = null;
            try {
                token = tokenName;
            } catch (Exception e) {
                Util.logToChat("Couldn't find token: " + tokenName);
                continue;
            }
            tokenPiecesMap.put(token, tokenSlot);
        }
    }

    private void loadChits(ListWidget listWidget) {
        List<ListWidget> chitLists = listWidget.getAllDescendantComponentsOf(ListWidget.class);
        for (ListWidget chitList : chitLists) {
            if (chitList.getConfigureName() == null) {
                continue;
            }
            String name = chitList.getConfigureName().trim();
            if (obstacleTabNames.contains(name)) {
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

    private String getUpgradeMapKey(String upgradeType, String upgradeName) {
        return String.format("%s/%s", upgradeType, upgradeName);
    }

    private enum ListParentType {
        chits("Chits"),
        secondeditiontokens("SecondEdition"),
        debris("Debris"),
        remotes("Remotes");

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
