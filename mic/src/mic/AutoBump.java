package mic;

import static mic.Util.logToChat;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.List;

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

        getMap().getToolBar().add(mapDebugButton);
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
