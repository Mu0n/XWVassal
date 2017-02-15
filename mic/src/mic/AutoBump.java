package mic;

import static mic.Util.logToChat;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.*;

import com.google.common.collect.Lists;

import VASSAL.build.AbstractBuildable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.counters.Decorator;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.NonRectangular;
import VASSAL.counters.PieceFinder;

/**
 * Created by amatheny on 2/14/17.
 */
public class AutoBump extends AbstractBuildable {

    @Override
    public String[] getAttributeNames() {
        return new String[0];
    }

    @Override
    public void setAttribute(String s, Object o) {
    }

    @Override
    public String getAttributeValueString(String s) {
        return "";
    }

    public void addTo(Buildable parent) {

        JButton mapDebugButton = new JButton("Debug map");
        mapDebugButton.setAlignmentY(0.0F);
        mapDebugButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mapDebug();
            }
        });

        final Map map = getMap();
        map.getToolBar().add(mapDebugButton);
        final AtomicReference<Decorator> selectedShip = new AtomicReference<Decorator>();
        map.addLocalMouseListenerFirst(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                GamePiece selected = map.findPiece(e.getPoint(), new PieceFinder() {
                    public GamePiece select(Map map, GamePiece gamePiece, Point point) {
                        if (gamePiece.getState().contains("Ship")) {
                            ShipCompareShape compareShape = getShipCompareShape((Decorator) gamePiece);
                            if (compareShape.compareShape.contains(point)) {
                                return gamePiece;
                            }
                        }
                        return null;
                    }
                });
                if (selected != null) {
                    logToChat("Selected " + name(selected));
                    selectedShip.set((Decorator) selected);

                    for (String propName : ((Decorator) selected).getPropertyNames()) {
                        logToChat(propName + " : " + selected.getProperty(propName));
                    }
                }
            }

            public void mousePressed(MouseEvent e) {

            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });
        map.getView().addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {

            }

            public void keyPressed(KeyEvent e) {

            }

            public void keyReleased(KeyEvent e) {
                Decorator movedShip = selectedShip.get();
                if ((e.getKeyChar() != 'c' && e.getKeyChar() != 'C') || movedShip == null) {
                    return;
                }

                String lastMove = (String) movedShip.getProperty("LastMove");
                if (lastMove == null || lastMove.length() == 0) {
                    return;
                }

                ShipCompareShape movedCompareShape = getShipCompareShape(movedShip);

                for (GamePiece ship : getShipsOnMap(map)) {
                    if (ship.equals(movedShip)) {
                        continue;
                    }
                    ShipCompareShape testCompareShape = getShipCompareShape((Decorator) ship);
                    if (checkBump(movedCompareShape.compareShape, testCompareShape.compareShape, true, map)) {
                        logToChat(String.format("Bump between %s and %s", name(movedShip), name(ship)));
                    }
                }
            }
        });
    }

    public static <T extends Decorator> List<T> getDecorators(GamePiece piece, Class<T> decoratorClass) {
        List<T> matches = Lists.newArrayList();
        piece = Decorator.getOutermost(piece);
        while (piece instanceof Decorator) {
            if (decoratorClass.isInstance(piece)) {
                matches.add((T) piece);
            }
            piece = ((Decorator) piece).getInner();
        }
        return matches;
    }

    private Map getMap() {
        for (Map loopMap : GameModule.getGameModule().getComponentsOf(Map.class)) {
            if ("Contested Sector".equals(loopMap.getMapName())) {
                return loopMap;
            }
        }
        return null;
    }

    private List<Decorator> getShipsOnMap(Map map) {
        List<Decorator> ships = Lists.newArrayList();

        GamePiece[] pieces = map.getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("Ship")) {
                ships.add((Decorator) piece);
            }
        }
        return ships;
    }

    public void mapDebug() {
        Map map = getMap();
        List<ShipCompareShape> shipAreas = Lists.newArrayList();
        for (GamePiece ship : getShipsOnMap(map)) {
            shipAreas.add(getShipCompareShape((Decorator) ship));
        }

        boolean foundBump = false;
        for (int i = 0; i < shipAreas.size(); i++) {
            ShipCompareShape iShip = shipAreas.get(i);
            for (int j = i + 1; j < shipAreas.size(); j++) {
                ShipCompareShape jShip = shipAreas.get(j);
                Area iArea = new Area(iShip.compareShape);
                iArea.intersect(new Area(jShip.compareShape));
                if (!iArea.isEmpty()) {
                    foundBump = true;
                    logToChat(String.format("bump! between %s and %s", name(iShip.ship), name(jShip.ship)));
                    drawComparison(map, iShip.compareShape);
                    drawComparison(map, jShip.compareShape);
                }
            }
        }

        if (!foundBump) {
            logToChat("No bumps!");
        }
    }

    private boolean checkBump(Shape shape1, Shape shape2, boolean drawOverlap, Map map) {
        Area area1 = new Area(shape1);
        area1.intersect(new Area(shape2));
        if (!area1.isEmpty()) {
            if (drawOverlap && map != null) {
                Shape scaled = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom())
                        .createTransformedShape(area1);

                Graphics2D graphics = (Graphics2D) map.getView().getGraphics();
                graphics.setColor(Color.red);
                graphics.fill(scaled);
            }
            return true;
        }
        return false;
    }

    private void drawComparison(Map map, Shape compareShape) {
        Shape scaled = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom())
                .createTransformedShape(compareShape);

        Graphics2D graphics = (Graphics2D) map.getView().getGraphics();
        graphics.setColor(Color.orange);
        graphics.fill(scaled);
    }

    private ShipCompareShape getShipCompareShape(Decorator ship) {
        GamePiece inner = null;
        double radians = 0;
        Shape rawShape = null;
        Decorator shipIter = ship;
        while (inner == null && shipIter.getInner() != null) {
            GamePiece piece = shipIter.getInner();
            if (piece instanceof FreeRotator) {
                radians = ((FreeRotator) piece).getAngleInRadians();
            }
            if (piece instanceof NonRectangular) {
                rawShape = piece.getShape();
            }
            if (piece instanceof Decorator) {
                shipIter = (Decorator) piece;
            } else {
                inner = shipIter;
            }
        }

        Shape transformed = AffineTransform.getScaleInstance(1.01d, 1.01d).createTransformedShape(rawShape);

        transformed = AffineTransform
                .getTranslateInstance(ship.getPosition().getX(), ship.getPosition().getY())
                .createTransformedShape(transformed);

        double centerX = ship.getPosition().getX();
        double centerY = ship.getPosition().getY() + 0.5; // adjust for something?
        transformed = AffineTransform
                .getRotateInstance(radians, centerX, centerY)
                .createTransformedShape(transformed);

        return new ShipCompareShape(ship, transformed);
    }

    private String name(GamePiece ship) {
        return (String) ship.getProperty("Craft ID #");
    }

    private static class ShipCompareShape {
        public ShipCompareShape(Decorator ship, Shape compareShape) {
            this.ship = ship;
            this.compareShape = compareShape;
        }

        Decorator ship;
        Shape compareShape;
    }
}
