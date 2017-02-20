package mic.manuvers;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by amatheny on 2/19/17.
 */
public class RealCurvedPathData {

    List<DataInstance> data = Lists.newArrayList();

    public static class DataInstance {
        CurvedPaths path;
        boolean bigBase;
        List<CurvedPathPart> parts = Lists.newArrayList();
    }

    public static class CurvedPathPart {
        double x;
        double y;
        double angle;
    }
}
