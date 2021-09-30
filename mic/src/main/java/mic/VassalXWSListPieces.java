package mic;

import java.util.List;

import com.google.common.collect.Lists;

import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.GamePiece;

/**
 * Created by amatheny on 2/8/17.
 */
public class VassalXWSListPieces {
    private List<VassalXWSPilotPieces> ships = Lists.newArrayList();
    private List<PieceSlot> obstacles = Lists.newArrayList();

    public List<VassalXWSPilotPieces> getShips() {
        return ships;
    }

    public void setShips(List<VassalXWSPilotPieces> ships) {
        this.ships = ships;
    }

    public List<PieceSlot> getObstacles() {
        return obstacles;
    }

    public void setObstacles(List<PieceSlot> obstacles) {
        this.obstacles = obstacles;
    }

    public List<GamePiece> getObstaclesForDisplay() {
        List<GamePiece> pieces = Lists.newArrayList();
        for (PieceSlot slot : getObstacles()) {
            pieces.add(Util.newPiece(slot));
        }
        return pieces;
    }

    public int getSquadPoints() {
        if (ships.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (VassalXWSPilotPieces ship : ships) {
            if (ship.getPilotData() == null) {
                Util.logToChat("Unable to calculate points for " + ship.getPilotCard().getConfigureName());
                continue;
            }
            total += ship.getPilotData().getPoints();
            int systemCost = 0;
            int eliteCost = 0;
            boolean isTIEx1Here = false;
            boolean isVaksaiHere = false;
            boolean isRenegadeRefitHere = false;
            int vaksaiAccumulatedRebate = 0;
            for (VassalXWSPilotPieces.Upgrade upgrade : ship.getUpgrades()) {
                if (upgrade.getUpgradeData() == null) {
                    Util.logToChat("Unable to calculate points for " + upgrade.getXwsName());
                    continue;
                }
                total += upgrade.getUpgradeData().getPoints();
                if(upgrade.getUpgradeData().getPoints() > 0) vaksaiAccumulatedRebate++;
                if ("tiex1".equals(upgrade.getXwsName())) {
                    isTIEx1Here = true;
                }
                if("vaksai".equals(upgrade.getXwsName())) {
                    isVaksaiHere = true;
                }
                if("renegaderefit".equals(upgrade.getXwsName())){
                    isRenegadeRefitHere = true;
                }
                if ("System".equals(upgrade.getUpgradeData().getSlot()))
                    systemCost += upgrade.getUpgradeData().getPoints();
                if("Elite".equals(upgrade.getUpgradeData().getSlot()))
                    eliteCost += upgrade.getUpgradeData().getPoints();
            }
            if (isTIEx1Here) {
                total -= Math.min(4, systemCost);
            }
            if(isVaksaiHere) {
                total -= vaksaiAccumulatedRebate;
            }
            if(isRenegadeRefitHere){
                total -= Math.min(1, eliteCost);
            }
        }
        return total;
    }
}
