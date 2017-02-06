package VASSAL.build.widget;

import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.GamePiece;
import VASSAL.build.Buildable;
import VASSAL.build.Builder;
import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.build.GpIdSupport;
import VASSAL.build.Widget;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.documentation.HelpWindow;
import VASSAL.build.module.documentation.HelpWindowExtension;
import VASSAL.build.module.map.MenuDisplayer;
import VASSAL.build.module.map.PieceMover.AbstractDragHandler;
import VASSAL.build.widget.CardSlot;
import VASSAL.command.AddPiece;
import VASSAL.configure.Configurer;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.Decorator;
import VASSAL.counters.DragBuffer;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyBuffer;
import VASSAL.counters.PieceCloner;
import VASSAL.counters.PieceDefiner;
import VASSAL.counters.PlaceMarker;
import VASSAL.i18n.ComponentI18nData;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by Mic on 06/02/2017.
 * Sole purpose is to access getExpandedPiece() method of a PieceSlot
 */
public class PieceSlotHack extends PieceSlot {

    public GamePiece getExpandedPiece() {
        if(this.expanded == null) {
            GamePiece var1 = this.getPiece();
            if(var1 != null) {
                this.expanded = PieceCloner.getInstance().clonePiece(var1);
            }
        }

        return this.expanded;
    }
}
