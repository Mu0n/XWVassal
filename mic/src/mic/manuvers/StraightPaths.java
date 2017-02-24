package mic.manuvers;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by amatheny on 2/17/17.
 */
public enum StraightPaths implements ManeuverPath {
    Str1(40 * 2.825),
    Str2(80 * 2.825),
    Str3(120 * 2.825),
    Str4(160 * 2.825),
    Str5(200 * 2.825);

    private double length;

    StraightPaths(double length) {
        this.length = length;
    }

    public double getFinalAngleOffset() {
        return 0;
    }

    public List<PathPart> getPathParts(int numSegments, double baseOffset, boolean isLargeBase) {
        List<PathPart> parts = Lists.newArrayList();

        //Extended straight back segment
        for (int i = 1; i <=numSegments; i++) {
            double angle = 0.0;
            double x = 0.0;
            double y = -baseOffset * ( i / (double) numSegments);

            parts.add(new PathPart(x, y, angle));
        }

        // Actual template
        for (int i = 1; i <= numSegments; i++) {

            double y = -this.length * (i / (double) numSegments);
            double x = 0;
            double angle = 0;

            parts.add(new PathPart(x, y, angle));
        }
//
        //Extended straight front segment
        for (int i = 1; i <=numSegments; i++) {
            parts.add(new PathPart(0, -this.length - baseOffset - baseOffset * (i / (double) numSegments), 0));
        }

        return parts;
    }
}
