package mic.manuvers;

/**
 * Created by amatheny on 2/17/17.
 */
public class PathPart {
    double x;
    double y;
    double angle;

    public PathPart(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAngle() {
        return angle;
    }
}
