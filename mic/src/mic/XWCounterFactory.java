package mic;

import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;

/**
 * Created by amatheny on 2/23/17.
 */
public class XWCounterFactory extends BasicCommandEncoder {
    public Decorator createDecorator(String type, GamePiece inner) {
        Decorator piece;
        if (type.startsWith(AutoBumpDecorator.ID)) {
            piece = new AutoBumpDecorator(inner);
        } else if (type.startsWith(TemplateOverlapCheckDecorator.ID)) {
            piece = new TemplateOverlapCheckDecorator(inner);
        } else if (type.startsWith(AutoRangeFinder.ID)) {
            piece = new AutoRangeFinder(inner);
        } else if (type.startsWith(DialStack.ID)) {
            piece = new DialStack(inner);
        } else if (type.startsWith(BombSpawner.ID)) {
            piece = new BombSpawner(inner);
        }else if (type.startsWith(ShipReposition.ID)) {
            piece = new ShipReposition(inner);
        }else if (type.startsWith(EmptyTest.ID)) {
            piece = new EmptyTest(inner);
        }else {
            piece = super.createDecorator(type, inner);
        }
        return piece;
    }
}
