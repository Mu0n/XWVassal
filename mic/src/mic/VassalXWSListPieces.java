package mic;

import java.util.List;

import com.google.common.collect.Lists;

import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.GamePiece;

import static mic.Util.logToChat;

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
        int total = 0;
        boolean isTIEx1Here = false;
        int systemCost = 0;

        if(!ships.isEmpty()) {
            for(VassalXWSPilotPieces ship : ships){
                total += ship.getPilotData().getPoints();
                for(VassalXWSPilotPieces.Upgrade upgrade : ship.getUpgrades()) {
                    total += upgrade.getUpgradeData().getPoints();
                    if("tiex1".equals(upgrade.getXwsName())) isTIEx1Here = true;
                    if("System".equals(upgrade.getUpgradeData().getSlot())) systemCost += upgrade.getUpgradeData().getPoints();
                }
                if(isTIEx1Here) total -= Math.min(4,systemCost);
                isTIEx1Here = false;
                systemCost = 0;
            }
        }
        return total;
    }
}
