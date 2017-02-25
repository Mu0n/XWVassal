package mic;

import static mic.Util.logToChat;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.List;

import com.google.common.collect.Lists;

import VASSAL.build.AbstractBuildable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.Decorator;
import VASSAL.counters.FreeRotator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.NonRectangular;
import VASSAL.counters.Properties;
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

//        final Map map = getMap();
//
//        JButton mapDebugButton = new JButton("Debug map");
//        mapDebugButton.setAlignmentY(0.0F);
//        mapDebugButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                mapDebug();
//            }
//        });
//        map.getToolBar().add(mapDebugButton);
//
//        JButton button = new JButton("Left bank 1");
//        button.setAlignmentY(0.0F);
//        button.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                performTemplateMove(ManeuverPaths.LBk1);
//            }
//        });
//        map.getToolBar().add(button);
//
//        button = new JButton("Right bank 1");
//        button.setAlignmentY(0.0F);
//        button.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                performTemplateMove(ManeuverPaths.RBk1);
//            }
//        });
//        map.getToolBar().add(button);
//
//
//        button = new JButton("Left turn 1");
//        button.setAlignmentY(0.0F);
//        button.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                performTemplateMove(ManeuverPaths.LT1);
//            }
//        });
//        map.getToolBar().add(button);
//
//        button = new JButton("Right turn 1");
//        button.setAlignmentY(0.0F);
//        button.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                performTemplateMove(ManeuverPaths.RT1);
//            }
//        });
//        map.getToolBar().add(button);
//
//
//        button = new JButton("Straight 1");
//        button.setAlignmentY(0.0F);
//        button.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                performTemplateMove(ManeuverPaths.Str1);
//            }
//        });
//        map.getToolBar().add(button);
//
//        button = new JButton("Straight 2");
//        button.setAlignmentY(0.0F);
//        button.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                performTemplateMove(ManeuverPaths.Str2);
//            }
//        });
//        map.getToolBar().add(button);
//
//        button = new JButton("Straight 3");
//        button.setAlignmentY(0.0F);
//        button.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                performTemplateMove(ManeuverPaths.Str3);
//            }
//        });
//        map.getToolBar().add(button);
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

        Decorator rotator = (FreeRotator) Decorator.getDecorator(ship, FreeRotator.class);
        Util.logToChat(rotator.myGetType());


        double angle = ((FreeRotator) Decorator.getDecorator(ship, FreeRotator.class)).getAngle();
        Shape rawShape = Decorator.getDecorator(ship, NonRectangular.class).getShape();

        final boolean isLargeBase = rawShape.getBounds().width > 113;

        logToChat(String.format("pos=%f,%f, angle=%f, local_bearing=%s, _Facing=%s, _Degrees=%s, isLargeBase=%s, id=%s",
                ship.getPosition().getX(),
                ship.getPosition().getY(),
                angle,
                ship.getProperty("local_bearing"),
                ship.getProperty("_Facing"),
                ship.getProperty("_Degrees"),
                isLargeBase,
                ship.getId()));
    }

    private void setShipPosition(Map map, Decorator ship, PathPart part) {
        Point point = new Point((int) Math.floor(part.getX() + 0.5), (int) Math.floor(part.getY() + 0.5));

        map.removePiece(ship);
        ChangeTracker changeTracker = new ChangeTracker(ship);
        ((FreeRotator) Decorator.getDecorator(ship, FreeRotator.class)).setAngle(part.getAngle());
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
        Shape rawShape = Decorator.getDecorator(ship, NonRectangular.class).getShape();

        final boolean isLargeBase = rawShape.getBounds().width > 113;

        logToChat(String.format("bbox=%s, pos=%f,%f, angle=%f, local_bearing=%s, _Facing=%s, _Degrees=%s, isLargeBase=%s",
                rawShape.getBounds2D(),
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
}
