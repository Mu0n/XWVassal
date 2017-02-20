package mic;

import static mic.Util.logToChat;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import com.google.common.base.Joiner;
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
import javafx.scene.shape.Path;
import mic.manuvers.ManeuverPaths;
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

    public void addTo(final Buildable parent) {

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
                performTemplateMove(ManeuverPaths.LBk1);
            }
        });
        map.getToolBar().add(button);

        button = new JButton("Right bank 1");
        button.setAlignmentY(0.0F);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                performTemplateMove(ManeuverPaths.RBk1);
            }
        });
        map.getToolBar().add(button);


        button = new JButton("Left turn 1");
        button.setAlignmentY(0.0F);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                performTemplateMove(ManeuverPaths.LT1);
            }
        });
        map.getToolBar().add(button);

        button = new JButton("Right turn 1");
        button.setAlignmentY(0.0F);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                performTemplateMove(ManeuverPaths.RT1);
            }
        });
        map.getToolBar().add(button);


        button = new JButton("Straight 1");
        button.setAlignmentY(0.0F);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                performTemplateMove(ManeuverPaths.Str1);
            }
        });
        map.getToolBar().add(button);

        button = new JButton("Straight 2");
        button.setAlignmentY(0.0F);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                performTemplateMove(ManeuverPaths.Str2);
            }
        });
        map.getToolBar().add(button);

        button = new JButton("Straight 3");
        button.setAlignmentY(0.0F);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                performTemplateMove(ManeuverPaths.Str3);
            }
        });
        map.getToolBar().add(button);

        button = new JButton("Check bump");
        button.setAlignmentY(0.0F);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                checkBumpAfterMove();
            }
        });
        map.getToolBar().add(button);


        map.getView().addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {

            }

            public void keyPressed(KeyEvent e) {

            }

            public void keyReleased(KeyEvent e) {
                if ((e.getKeyChar() != 'c' && e.getKeyChar() != 'C')) {
                    return;
                }

                //checkBumpAfterMove();
            }
        });
    }

    private void checkBumpAfterMove() {
        final Map map = getMap();
        final Decorator movedShip = getSelectedShip(map);
        if (movedShip == null) {
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
        logToChat(String.format("pos=%s,%s shipBounds=%s compareBounds=%s lastMove=%s",
                movedCompareShape.ship.getPosition().getX(),
                movedCompareShape.ship.getPosition().getY(),
                movedCompareShape.ship.boundingBox(),
                movedCompareShape.compareShape.getBounds2D(),
                lastMove
        ));

        ManeuverPaths lastMovePath = ManeuverPaths.fromLastMove(lastMove);
        final List<PathPart> pathParts = lastMovePath.getTransformedInversePathParts(
                movedCompareShape.ship.getPosition().getX(),
                movedCompareShape.ship.getPosition().getY(),
                movedCompareShape.angleDegrees - 180,
                movedCompareShape.rawShape.getBounds().width > 114);

        int currentPathPart = -1;
        while(checkAllBumps(movedCompareShape, map)) {
            currentPathPart++;
            PathPart part = pathParts.get(currentPathPart);
            movedCompareShape.compareShape = AffineTransform
                    .getTranslateInstance(part.getX(), part.getY())
                    .createTransformedShape(movedCompareShape.rawShape);
            movedCompareShape.compareShape = AffineTransform
                    .getRotateInstance(-part.getAngle() * (Math.PI / 180), part.getX(), part.getY() + 0.5)
                    .createTransformedShape(movedCompareShape.compareShape);
        }

        logToChat("No more bumps after " + currentPathPart + " path parts");

        PathPart finalPart = new PathPart(
                pathParts.get(currentPathPart).getX(),
                pathParts.get(currentPathPart).getY(),
                pathParts.get(currentPathPart).getAngle() + 180);
        setShipPosition(map, movedShip, finalPart);

        Executors.newCachedThreadPool().submit(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(500l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                drawPath(map, pathParts);

                Shape finalCompareShape = new Area(movedCompareShape.compareShape);
                finalCompareShape = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom()).createTransformedShape(finalCompareShape);
                Graphics2D graphics = (Graphics2D)map.getView().getGraphics();
                graphics.setColor(Color.orange);
                graphics.fill(finalCompareShape);
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

        final Map map = getMap();

        final Decorator ship = getSelectedShip(map);

        double angle = ((FreeRotator) Decorator.getDecorator(ship, FreeRotator.class)).getAngle();
        final ShipCompareShape compare = getShipCompareShape(ship);

        final boolean isLargeBase = compare.rawShape.getBounds().width > 113;

        Graphics2D graphics = (Graphics2D) map.getView().getGraphics();

        Path2D.Double movedPoint = new Path2D.Double();
        movedPoint.moveTo(ship.getPosition().getX(), ship.getPosition().getY());
        movedPoint.lineTo(ship.getPosition().getX(), ship.getPosition().getY());
        graphics.setColor(Color.blue);
        graphics.setStroke(new BasicStroke(10.0f));
        graphics.draw(movedPoint);

        logToChat(String.format("bbox=%s, pos=%f,%f, angle=%f, local_bearing=%s, _Facing=%s, _Degrees=%s, isLargeBase=%s",
                compare.rawShape.getBounds2D(),
                ship.getPosition().getX(),
                ship.getPosition().getY(),
                angle,
                ship.getProperty("local_bearing"),
                ship.getProperty("_Facing"),
                ship.getProperty("_Degrees"),
                isLargeBase));
    }

    private void setShipPosition(Map map, Decorator ship, PathPart part) { 
        Point point = new Point((int) Math.floor(part.getX() + 0.5), (int) Math.floor(part.getY() + 0.5));

        map.removePiece(ship);
        ChangeTracker changeTracker = new ChangeTracker(ship);
        ((FreeRotator)Decorator.getDecorator(ship, FreeRotator.class)).setAngle(part.getAngle());
        Command rotateCommand = changeTracker.getChangeCommand();

        GameModule.getGameModule().sendAndLog(rotateCommand);
        ship.setPosition(point);
        map.addPiece(ship);
    }

    private void drawPath(Map map, List<PathPart> parts) {
        Path2D.Double templatePath = new Path2D.Double();

        templatePath.moveTo(parts.get(0).getX(), parts.get(0).getY());
        for (int i = 1; i < parts.size(); i++) {
            templatePath.lineTo(parts.get(i).getX(), parts.get(i).getY());

        }


        final Graphics2D graphics = (Graphics2D) map.getView().getGraphics();

        final Shape renderPath = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom())
                .createTransformedShape(templatePath);

        graphics.setColor(Color.orange);
        graphics.setStroke(new BasicStroke(2.0f));
        graphics.draw(renderPath);
    }

    private void performTemplateMove(final ManeuverPaths path) {
        final Map map = getMap();

        final Decorator ship = getSelectedShip(map);
        if (ship == null) {
            return;
        }

        double angle = ((FreeRotator) Decorator.getDecorator(ship, FreeRotator.class)).getAngle();
        final ShipCompareShape compare = getShipCompareShape(ship);

        final boolean isLargeBase = compare.rawShape.getBounds().width > 113;

        logToChat(String.format("bbox=%s, pos=%f,%f, angle=%f, local_bearing=%s, _Facing=%s, _Degrees=%s, isLargeBase=%s",
                compare.rawShape.getBounds2D(),
                ship.getPosition().getX(),
                ship.getPosition().getY(),
                angle,
                ship.getProperty("local_bearing"),
                ship.getProperty("_Facing"),
                ship.getProperty("_Degrees"),
                isLargeBase));

        double startX = ship.getPosition().getX();
        double startY = ship.getPosition().getY();

        List<PathPart> parts = path.getTransformedPathParts(
                startX,
                startY,
                angle,
                isLargeBase);


        PathPart part = parts.get(parts.size() - 1);

        setShipPosition(map, ship, part);

        drawPath(map, parts);
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

        Shape transformed = AffineTransform.getScaleInstance(1.01d, 1.01d).createTransformedShape(rawShape);

        transformed = AffineTransform
                .getTranslateInstance(ship.getPosition().getX(), ship.getPosition().getY())
                .createTransformedShape(transformed);

        double centerX = ship.getPosition().getX();
        double centerY = ship.getPosition().getY() + 0.5; // adjust for something?
        transformed = AffineTransform
                .getRotateInstance(degToRad(angleDeg), centerX, centerY)
                .createTransformedShape(transformed);

        return new ShipCompareShape(ship, rawShape, transformed, angleDeg);
    }

    private double degToRad(double deg) {
        return deg * (Math.PI / 180);
    }

    private void printPropertyNames(GamePiece piece) {
        List<String> propNames = Lists.newArrayList();
        while (piece instanceof Decorator) {
            propNames.addAll(((Decorator) piece).getPropertyNames());
            piece = ((Decorator) piece).getInner();
        }
        logToChat(Joiner.on(',').join(propNames));
    }

    private String name(GamePiece ship) {
        return (String) ship.getProperty("Craft ID #");
    }

    private static class ShipCompareShape {
        public ShipCompareShape(Decorator ship, Shape rawShape, Shape compareShape, double angleDegrees) {
            this.rawShape = rawShape;
            this.ship = ship;
            this.compareShape = compareShape;
            this.angleDegrees = angleDegrees;
        }

        Shape rawShape;
        double angleDegrees;
        Decorator ship;
        Shape compareShape;
    }
}
