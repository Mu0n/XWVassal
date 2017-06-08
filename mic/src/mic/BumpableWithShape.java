package mic;

import VASSAL.counters.Decorator;

import java.awt.*;

/**
 * Created by mjuneau on 2017-06-08.
 */
public class BumpableWithShape {
    Shape shape;
    Shape rectWithNoNubs;
    Decorator bumpable;
    String type;
    String shipName = "";
    String pilotName = "";

    BumpableWithShape(Decorator bumpable, Shape shape, String type) {
        this.bumpable = bumpable;
        this.shape = shape;
        this.type = type;
    }
    BumpableWithShape(Decorator bumpable, Shape shape, String type, String pilotName, String shipName, Shape rectWithNoNubs) {
        this.bumpable = bumpable;
        this.shape = shape;
        this.type = type;
        this.pilotName = pilotName;
        this.shipName = shipName;
        this.rectWithNoNubs = rectWithNoNubs;
    }
}
