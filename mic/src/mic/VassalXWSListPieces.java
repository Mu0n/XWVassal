package mic;

import VASSAL.build.widget.PieceSlot;

import java.util.List;

/**
 * Created by amatheny on 2/8/17.
 */
public class VassalXWSListPieces {
    private List<VassalXWSPilotPieces> ships;
    private List<PieceSlot> obstacles;

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
}
