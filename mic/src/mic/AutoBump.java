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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import VASSAL.counters.Properties;
import mic.manuvers.ManuverPaths;
import mic.manuvers.PathPart;

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

        final Map map = getMap();

        JButton mapDebugButton = new JButton("Debug map");
        mapDebugButton.setAlignmentY(0.0F);
        mapDebugButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mapDebug();
            }
        });
        map.getToolBar().add(mapDebugButton);

        JButton button = new JButton("Left bank 1");
        button.setAlignmentY(0.0F);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                performTemplateMove(ManuverPaths.LBk1);
            }
        });
        map.getToolBar().add(button);

        button = new JButton("Right bank 1");
        button.setAlignmentY(0.0F);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                performTemplateMove(ManuverPaths.RBk1);
            }
        });
        map.getToolBar().add(button);


        button = new JButton("Left turn 1");
        button.setAlignmentY(0.0F);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                performTemplateMove(ManuverPaths.LT1);
            }
        });
        map.getToolBar().add(button);

        button = new JButton("Right turn 1");
        button.setAlignmentY(0.0F);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                performTemplateMove(ManuverPaths.RT1);
            }
        });
        map.getToolBar().add(button);

        map.getView().addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {

            }

            public void keyPressed(KeyEvent e) {

            }

            public void keyReleased(KeyEvent e) {
                final Decorator movedShip = getSelectedShip(map);
                if ((e.getKeyChar() != 'c' && e.getKeyChar() != 'C') || movedShip == null) {
                    return;
                }

                final String lastMove = (String) movedShip.getProperty("LastMove");
                if (lastMove == null || lastMove.length() == 0) {
                    return;
                }

                final ShipCompareShape movedCompareShape = getShipCompareShape(movedShip);

                if (checkAllBumps(movedCompareShape, map) == false) {
                    return; // All safe
                }
                logToChat("bump");
                logToChat(String.format("pos=%s,%s shipBounds=%s compareBounds=%s",
                        movedCompareShape.ship.getPosition().getX(),
                        movedCompareShape.ship.getPosition().getY(),
                        movedCompareShape.ship.boundingBox(),
                        movedCompareShape.compareShape.getBounds2D()));
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

    Decorator getSelectedShip(Map map) {
        for (GamePiece ship : getShipsOnMap(map)) {
            if (Boolean.TRUE.equals(ship.getProperty(Properties.SELECTED))) {
                return (Decorator) ship;
            }
        }
        return null;
    }

    public void mapDebug() {

    }

    private void performTemplateMove(final ManuverPaths path) {
        Map map = getMap();

        final Decorator ship = getSelectedShip(map);
        if (ship == null) {
            return;
        }

        double angle = ((FreeRotator) Decorator.getDecorator(ship, FreeRotator.class)).getAngle();
        double cumulativeAngle = ((FreeRotator) Decorator.getDecorator(ship, FreeRotator.class)).getCumulativeAngle();

        logToChat(String.format("angle=%f, cumulativeAngle=%s", angle, cumulativeAngle));

        final ShipCompareShape compare = getShipCompareShape(ship);

        ExecutorService pool = Executors.newCachedThreadPool();

        pool.submit(new Runnable() {
            public void run() {
                // TODO catch this
                List<PathPart> parts = path.getTransformedPathParts(
                        compare.ship.getPosition().getX(),
                        compare.ship.getPosition().getY(),
                        compare.angleDegrees);
                for (PathPart part : parts) {

                    compare.compareShape = AffineTransform
                            .getTranslateInstance(part.getX(), part.getY())
                            .createTransformedShape(compare.compareShape);
                    compare.compareShape = AffineTransform
                            .getRotateInstance(degToRad(part.getAngle()), part.getX(), part.getY())
                            .createTransformedShape(compare.compareShape);

                    ship.setPosition(new Point((int) Math.floor(part.getX() + 0.5), (int) Math.floor(part.getX() + 0.5)));
                    ((FreeRotator) Decorator.getDecorator(ship, FreeRotator.class)).setAngle(part.getAngle());
                    try {
                        Thread.sleep(10l);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                }

//                logToChat(String.format("Trying %f, %f (%f)", coords[0], coords[1], coords[2] * (180 / Math.PI)));
//                    if (checkAllBumps(movedCompareShape, map) == false) {
//                        movedShip.setPosition(new Point((int) Math.floor(coords[0] + 0.5), (int) Math.floor(coords[1] + 0.5)));
//                        ((FreeRotator) Decorator.getDecorator(movedShip, FreeRotator.class)).setAngle(coords[2] * (180 / Math.PI));
//                        break;
//                    }

            }
        });
    }


    private boolean checkAllBumps(ShipCompareShape movedCompareShape, Map map) {
        for (GamePiece ship : getShipsOnMap(map)) {
            if (ship.equals(movedCompareShape.ship)) {
                continue;
            }
            ShipCompareShape testCompareShape = getShipCompareShape((Decorator) ship);
            if (checkBump(movedCompareShape.compareShape, testCompareShape.compareShape, false, map)) {
                return true;
            }
        }
        return false;
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
        double angleDeg = 0;
        Shape rawShape = null;
        Decorator shipIter = ship;
        while (inner == null && shipIter.getInner() != null) {
            GamePiece piece = shipIter.getInner();
            if (piece instanceof FreeRotator) {
                angleDeg = ((FreeRotator) piece).getAngle();
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

        logToChat(String.format("%s: ", rawShape.getBounds2D()));
        Shape transformed = AffineTransform.getScaleInstance(1.01d, 1.01d).createTransformedShape(rawShape);

        transformed = AffineTransform
                .getTranslateInstance(ship.getPosition().getX(), ship.getPosition().getY())
                .createTransformedShape(transformed);

        double centerX = ship.getPosition().getX();
        double centerY = ship.getPosition().getY() + 0.5; // adjust for something?
        transformed = AffineTransform
                .getRotateInstance(degToRad(angleDeg), centerX, centerY)
                .createTransformedShape(transformed);

        return new ShipCompareShape(ship, transformed, angleDeg);
    }

    private double degToRad(double deg) {
        return deg * (Math.PI / 180);
    }

    private String name(GamePiece ship) {
        return (String) ship.getProperty("Craft ID #");
    }

    private static class ShipCompareShape {
        public ShipCompareShape(Decorator ship, Shape compareShape, double angleDegrees) {
            this.ship = ship;
            this.compareShape = compareShape;
            this.angleDegrees = angleDegrees;
        }

        double angleDegrees;
        Decorator ship;
        Shape compareShape;
    }
}
