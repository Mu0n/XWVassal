package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;

/**
 * Created by amatheny on 2/23/17.
 */
public class XWCounterFactory extends BasicCommandEncoder {

    static {
        // This is only done here since this is something loaded early on
        // and I have no better idea of where it should go
        GameModule.getGameModule().addCommandEncoder(new MapVisualizations.CollsionVisualizationEncoder());
        GameModule.getGameModule().addCommandEncoder(new FOVisualization.FOVisualizationEncoder());
        GameModule.getGameModule().addCommandEncoder(new FOVisualizationClear.FOVisualizationClearEncoder());
        GameModule.getGameModule().addCommandEncoder(new StemDial.DialGenerateCommand.DialGeneratorEncoder());
        GameModule.getGameModule().addCommandEncoder(new DialRevealCommand.Dial2eRevealEncoder());
        GameModule.getGameModule().addCommandEncoder(new DialHideCommand.Dial2eHideEncoder());
        GameModule.getGameModule().addCommandEncoder(new DialRotateCommand.Dial2eRotateEncoder());
        GameModule.getGameModule().addCommandEncoder(new BroadcastEscrowSquadCommand.broadcastEscrowSquadCommandEncoder());
    }

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
        }else if (type.startsWith(RemoteRelocation.ID)) {
            piece = new RemoteRelocation(inner);
        }else if (type.startsWith(HyperspaceLauncher.ID)) {
            piece = new HyperspaceLauncher(inner);
        }else if (type.startsWith(EmptyTest.ID)) {
            piece = new EmptyTest(inner);
        }else if (type.startsWith(CritSpawner.ID)) {
            piece = new CritSpawner(inner);
        }else if (type.startsWith(StemDial.ID)) {
            piece = new StemDial(inner);
        }else if (type.startsWith(ReportCardText.ID)) {
            piece = new ReportCardText(inner);
        }else if (type.startsWith(ReportCardText2e.ID)) {
            piece = new ReportCardText2e(inner);
        }else if (type.startsWith(StemNuDial2e.ID)) {
            piece = new StemNuDial2e(inner);
        }else if (type.startsWith(AutoRangeForTokens.ID)) {
            piece = new AutoRangeForTokens(inner);
        }else {
            piece = super.createDecorator(type, inner);
        }
        return piece;
    }
}
