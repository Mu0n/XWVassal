package mic.manuvers;

import java.awt.*;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Created by amatheny on 2/17/17.
 */
public enum StraightPaths implements ManeuverPath {
    Str1(40 * 2.825),
    Str2(80 * 2.825),
    Str3(120 * 2.825),
    Str4(160 * 2.825),
    Str5(200 * 2.825),
    Rev1(40 * 2.825, true);

    private boolean reverse;
    private double length;
    private static int TEMPLATEFATNESS = 57;

    StraightPaths(double length) {
        this(length, false);
    }

    StraightPaths(double length, boolean reverse) {
        this.length = length;
        this.reverse = reverse;
    }

    public static Shape getRawTemplateShape(int speed){
        return new Rectangle(TEMPLATEFATNESS, (int)(speed * 282.5f));
    }

    public double getPathLength() {
        return this.length;
    }

    public List<PathPart> getPathParts(int numSegments, double baseOffset, boolean isLargeBase) {
        List<PathPart> parts = Lists.newArrayList();

        double revModifier = this.reverse ? -1 : 1;

        //Extended straight back segment
        for (int i = 1; i <= numSegments; i++) {
            parts.add(new PathPart(
                    0,
                    (-baseOffset * (i / (double) numSegments) )* revModifier,
                    0.0
            ));
        }

        // Actual template
        for (int i = 1; i <= numSegments; i++) {
            parts.add(new PathPart(
                    0.0,
                    (-baseOffset - this.length * (i / (double) numSegments) ) * revModifier,
                    0.0
            ));
        }
//
        //Extended straight front segment
        for (int i = 1; i <= numSegments; i++) {
            parts.add(new PathPart(
                    0,
                    (-this.length - baseOffset - baseOffset * (i / (double) numSegments)) * revModifier,
                    0
            ));
        }

        return parts;
    }
}
