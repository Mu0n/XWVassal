package mic;

import static mic.Util.logToChat;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.List;

import javax.swing.*;

import com.google.common.collect.Lists;

import VASSAL.build.AbstractBuildable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.counters.CalculatedProperty;
import VASSAL.counters.Decorator;
import VASSAL.counters.DynamicProperty;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.NonRectangular;

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
        map.getView().addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {

            }

            public void keyPressed(KeyEvent e) {

            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == 'c' || e.getKeyChar() == 'C') {
                    logToChat("C was pressed");
                    for (GamePiece ship : getShipsOnMap(map)) {
                        for (String propName : ((Decorator) ship).getPropertyNames()) {
                            logToChat("ship:" + propName + ":" + ((Decorator) ship).getProperty(propName));
                        }

                        for (DynamicProperty prop : getDecorators(ship, DynamicProperty.class)) {
                            logToChat("dyn:" + prop.getKey() + ":" + prop.getValue());
                        }

                        for (CalculatedProperty prop : getDecorators(ship, CalculatedProperty.class)) {
                            for (String propName : prop.getPropertyNames()) {
                                logToChat("calc:" + propName + ":" + prop.getProperty(propName));
                            }
                        }
                    }
                }
            }
        });

//        final PieceCollection collection = map.getPieceCollection();
//        map.setPieceCollection(new PieceCollection() {
//            public void moveToFront(GamePiece gamePiece) {
//                collection.moveToFront(gamePiece);
//            }
//
//            public void moveToBack(GamePiece gamePiece) {
//                collection.moveToBack(gamePiece);
//            }
//
//            public GamePiece[] getPieces() {
//                return collection.getPieces();
//            }
//
//            public GamePiece[] getAllPieces() {
//                return collection.getAllPieces();
//            }
//
//            public boolean canMerge(GamePiece gamePiece, GamePiece gamePiece1) {
//                return collection.canMerge(gamePiece, gamePiece1);
//            }
//
//            public int indexOf(GamePiece gamePiece) {
//                return collection.indexOf(gamePiece);
//            }
//
//            public void remove(GamePiece gamePiece) {
//                collection.remove(gamePiece);
//                if (gamePiece.getState().contains("Ship")) {
//                    logToChat("Removed ship");
//                }
//            }
//
//            public void add(GamePiece gamePiece) {
//                collection.add(gamePiece);
//                if (gamePiece.getState().contains("Ship")) {
//                    logToChat("Added ship");
//                }
//            }
//
//            public void clear() {
//                logToChat("Clearing all pieces");
//                collection.clear();
//            }
//        });
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

    private List<GamePiece> getShipsOnMap(Map map) {
        List<GamePiece> ships = Lists.newArrayList();

        GamePiece[] pieces = map.getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("Ship")) {
                ships.add(piece);
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

    private String name(Decorator ship) {
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
