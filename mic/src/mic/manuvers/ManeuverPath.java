package mic.manuvers;

import java.util.List;

/**
 * Created by amatheny on 2/17/17.
 */
public interface ManeuverPath {
    List<PathPart> getPathParts(int numSegments, double baseOffset, boolean isLargeBase);

    double getPathLength();
}
