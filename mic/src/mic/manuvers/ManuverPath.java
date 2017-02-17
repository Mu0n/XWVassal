package mic.manuvers;

import java.util.List;

/**
 * Created by amatheny on 2/17/17.
 */
public interface ManuverPath {
    List<PathPart> getPathParts(int numSegments);
}
