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
        } else {
            piece = super.createDecorator(type, inner);
        }
        return piece;
    }
}
