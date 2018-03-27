package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import mic.ota.XWOTAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class StemShip extends Decorator implements EditablePiece {
    public static final String ID = "stemship";

    private static final String BASE_SHIP_LAYER_NAME = "Layer - Base Ship";
    private static final String SHIP_BASE_IMAGE_PREFIX = "Ship_Base";

    private static Map<String, String> cardboardActionImages = ImmutableMap.<String, String>builder()
            .put("Focus","Action_Focus.png")
            .put("Target Lock","Action_Target_Lock.png")
            .put("Boost","Action_Boost.png")
            .put("Evade","Action_Evade.png")
            .put("Barrel Roll","Action_Barrel_Roll.png")
            .put("Cloak","Action_Cloak.png")
            .put("SLAM","Action_Slam.png")
            .put("Rotate Arc","Action_Rotate_Arc.png")
            .put("Reinforce","Action_Reinforce.png")
            .put("Reload","Action_Reload.png")
            .put("Coordinate","Action_Coordinate.png")
            .build();

    private static Map<String, String> cardboardActionCoordinates = ImmutableMap.<String, String>builder()
            .put("small1","47;-40")
            .put("small2","47;-23")
            .put("small3","47;-6")
            .put("small4","47;11")
            .put("small5","47;28")
            .put("large1","95;-40")
            .put("large2","95;-23")
            .put("large3","95;-6")
            .put("large4","95;11")
            .put("large5","95;28")
            .build();

    private static Map<String, String> firingArcTypes = ImmutableMap.<String, String>builder()
            // Front
            .put("small/rebelalliance/Front","emb2;;2;;Show Firing Arc;2;;;2;;;;;true;0;-479;,AltArc_Rebel.svg;,;true;Show Firing Arc;;;false;;1;1;true;;70,130;")

            .put("small/galacticempire/Front","emb2;;2;;Show Firing Arc;2;;;2;;;;;true;0;-479;,AltArc_Imperial.svg;,;true;Show Firing Arc;;;false;;1;1;true;;70,130;")
            .put("small/scumandvillainy/Front","emb2;;2;;Show Firing Arc;2;;;2;;;;;true;0;-479;,AltArc_Scum.svg;,;true;Show Firing Arc;;;false;;1;1;true;;70,130;")
            .put("large/rebelalliance/Front","emb2;;2;;Show Firing Arc;2;;;2;;;;;true;0;-536;,Big_Firing-Arc_Rebel.svg;,;true;Show Big Firing Arc;;;false;;1;1;true;;70,130;")
            .put("large/galacticempire/Front","emb2;;2;;Show Firing Arc;2;;;2;;;;;true;0;-536;,Big_Firing-Arc_Imperial.svg;,;true;Show Big Firing Arc;;;false;;1;1;true;;70,130;")
            .put("large/scumandvillainy/Front","emb2;;2;;Show Firing Arc;2;;;2;;;;;true;0;-536;,Big_Firing-Arc_Scum.svg;,;true;Show Big Firing Arc;;;false;;1;1;true;;70,130;")

            // Aux 180
            .put("small/rebelalliance/Auxiliary 180","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-423;,auzituck_arc.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/galacticempire/Auxiliary 180","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-423;,small_imperial_aux180_arc.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/scumandvillainy/Auxiliary 180","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-423;,small_scum_aux180_arc.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("large/rebelalliance/Auxiliary 180","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,large_rebel_aux180_arc.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/galacticempire/Auxiliary 180","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,large_imperial_aux180_arc.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/scumandvillainy/Auxiliary 180","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,hound's_tooth_arc.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")

            // Aux Rear
            .put("small/rebelalliance/Auxiliary Rear","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,AltArc_Rebel_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/galacticempire/Auxiliary Rear","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,AltArc_Imperial_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/scumandvillainy/Auxiliary Rear","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,AltArc_Scum_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/rebelalliance/Auxiliary Rear","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Big_Firing-Arc_Rebel_Aux.svg;,;true;Show Big Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/galacticempire/Auxiliary Rear","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Big_Firing-Arc_Imperial_Aux.svg;,;true;Show Big Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/scumandvillainy/Auxiliary Rear","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Big_Firing-Arc_Scum_Aux.svg;,;true;Show Big Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")

            // Bullseye
            .put("small/rebelalliance/Bullseye","")
            .put("small/galacticempire/Bullseye","")
            .put("small/scumandvillainy/Bullseye","emb2;;2;;Show Bullseye Arc;2;;;2;;;;;true;0;-479;,Bullseye_Arc_Scum.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/rebelalliance/Bullseye","")
            .put("large/galacticempire/Bullseye","")
            .put("large/scumandvillainy/Bullseye","")

            .build();

    public StemShip(){
        this(null);
    }

    public StemShip(GamePiece piece){
        setInner(piece);

    }

    @Override
    public void mySetState(String s) {

    }
    @Override
    public String myGetState() {
        return "";
    }
    @Override
    public String myGetType() {
        return ID;
    }
    @Override
    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }
    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        return null;
    }


    public String getDescription() {
        return "Custom StemShip (mic.StemShip)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    public void draw(Graphics graphics, int i, int i1, Component component, double v) {
        this.piece.draw(graphics, i, i1, component, v);
    }

    public Rectangle boundingBox() {
        return this.piece.boundingBox();
    }

    public Shape getShape() {
        return this.piece.getShape();
    }

    public String getName() {
        return this.piece.getName();
    }


    //this is the command that takes a ship xws name, fetches the maneuver info and constructs the dial layer by layer
    public static class ShipGenerateCommand extends Command {
        GamePiece piece;
        static String xwsShipName = "";

        List<String> arcList;
        String shipName;
        String faction = "";
        String size = "";
       // List<String> actionList;
        String xwsPilot = "";
     //   boolean needsBombCapability;
        boolean shipContainsMobileArc;

        //ShipGenerateCommand(String shipXws,   GamePiece piece, String faction, String xwsPilot, boolean needsBombCapability) {
        ShipGenerateCommand(String shipXws,   GamePiece piece, String faction, String xwsPilot, boolean shipContainsMobileArc) {

            // fetch the maneuver array of arrays according to the xws name passed on from autospawn or other means
            xwsShipName = shipXws;
            this.faction = faction;
            MasterShipData.ShipData shipData = MasterShipData.getShipData(xwsShipName);
            //arcList = shipData.getFiringArcs();
            shipName = shipData.getName();
            this.piece = piece;
            this.xwsPilot = xwsPilot;
            this.size = shipData.getSize();
            this.shipContainsMobileArc = shipContainsMobileArc;
         //   this.needsBombCapability = needsBombCapability;
           // this.actionList = shipData.getActions();
        }


        protected void executeCommand()
        {
            // find the appropriate baseImage
            piece = buildShipBaseLayer(piece,faction,xwsShipName,xwsPilot, size);

            // Add the Target Lock capability
            piece = addTargetLock(piece,faction,size);

            // add the firing arcs needed
            piece = addFiringArcs(piece,faction,size,xwsShipName, shipContainsMobileArc);

        //    if(!this.needsBombCapability) {
        //        piece = removeBombCapability(piece);
         //   }

        }

        private GamePiece addFiringArcs(GamePiece newGamePiece, String faction, String newSize, String xws, boolean shipContainsMobileArc )
        {
            String newFaction = XWOTAUtils.simplifyFactionName(faction);

            // first get the list of arcs needed
            List<String> firingArcs = MasterShipData.getShipData(xwsShipName).getFiringArcs();
            String arc = null;
            Iterator<String> i = firingArcs.iterator();
            Embellishment emb = null;
            String arcKey = null;
            String newType = null;
            while(i.hasNext())
            {
                arc = i.next();
                if(!arc.equals("Mobile") && !arc.equals("Turret")) {
                    if (arc.equals("Front")) {
                        emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Show Front Firing Arc");

                    } else {
                        emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Show Auxiliary Firing Arc");
                    }

                    arcKey = newSize + "/" + newFaction + "/" + arc;
                    newType = firingArcTypes.get(arcKey);
                    if(newType != null && !newType.isEmpty())
                    {
                        emb.mySetType(newType);
                    }

                }
            }

            if(shipContainsMobileArc)
            {
                //TODO add in the mobile arc stuff
                // Only need to change the color of the front arc
                // "Show Big Firing Arc" - Purple Front Arc
                // "Show Big Firing Arc legacy" - Scum Front Arc (Not needed)
                // "Show Big Auxiliary Firing Arc"  - Purple Aux Rear Arc
                // "Show Big Auxiliary Firing Arc Legacy" - Scum Rear Aux Arc (not needed?)
                // "Show Left Mobile Arc" - Left Purple Arc
                // "Show Right Mobile Arc" - Right Purple Arc

                // Submenu Show Firing Arcs
                //  Show Firing Arc
                // Show Auxiliary Firing Arc
                // Show Mobile Turret Arc
                // Show front arc firing options
                // Show back arc firing options
                // show target lock
                // show target lock firing options

            }

            return newGamePiece;
        }

        private GamePiece addTargetLock(GamePiece newGamePiece, String faction, String newSize)
        {

            final String targetLockLayerName = "Layer - Show Target Lock";
            final String rebelSmallImage = "TargetLock_Rebel.svg";
            final String rebelLargeImage = "TargetLock_Rebel_Large.svg";
            final String imperialSmallImage = "TargetLock_Imperial.svg";
            final String imperialLargeImage = "TargetLock_Imperial_Large.svg";
            final String scumSmallImage = "TargetLock_Scum.svg";
            final String scumLargeImage = "TargetLock_Scum_Large.svg";

            String newFaction = XWOTAUtils.simplifyFactionName(faction);

            Embellishment myEmb = (Embellishment)Util.getEmbellishment(newGamePiece,targetLockLayerName);

            StringBuilder sb = new StringBuilder();
            sb.append("emb2;;2;;Show Target Lock;2;;;2;;;;;true;0;0;,");
            if(newSize.equals("small") && newFaction.equals("rebelalliance"))
            {
                sb.append(rebelSmallImage);
            }else if(newSize.equals("small") && newFaction.equals("galacticempire") )
            {
                sb.append(imperialSmallImage);
            }else if(newSize.equals("small") && newFaction.equals("scumandvillainy") )
            {
                sb.append(scumSmallImage);
            }else if(newSize.equals("large") && newFaction.equals("rebelalliance"))
            {
                sb.append(rebelLargeImage);
            }else if(newSize.equals("large") && newFaction.equals("galacticempire") )
            {
                sb.append(imperialLargeImage);
            }else if(newSize.equals("large") && newFaction.equals("scumandvillainy") )
            {
                sb.append(scumLargeImage);
            }

            sb.append(";,;true;Show Target Lock;;;false;;1;1;true;;76,130;");

            myEmb.mySetType(sb.toString());

            return newGamePiece;
        }
/*
        private GamePiece removeBombCapability(GamePiece piece)
        {
            PlaceMarker normalBombPlaceMarker = (PlaceMarker)Util.getPlaceMarkerTrait(piece,"Place Marker - Place Bomb Spawner");
            PlaceMarker frontalBombPlaceMarker = (PlaceMarker)Util.getPlaceMarkerTrait(piece,"Place Marker - Place Frontal Bomb Spawner");

            if(normalBombPlaceMarker != null && normalBombPlaceMarker.myGetType() != null) {
                normalBombPlaceMarker.mySetType("");
                Util.logToChat("ship has the normal place marker");
            }else{
                Util.logToChat("ship doesn't have the normal place marker");
            }
            if(frontalBombPlaceMarker != null && frontalBombPlaceMarker.myGetType() != null) {
                frontalBombPlaceMarker.mySetType("");
                Util.logToChat("ship has the frontal place marker");
            }else{
                Util.logToChat("ship doesn't have the frontal place marker");
            }

            return piece;
        }*/
/*
        private GamePiece addBombCapability(GamePiece piece, String size)
        {
            String normalSmallBombSpanwerType = "placemark;Place Bomb Spawner;66,130;VASSAL.build.module.PieceWindow/VASSAL.build.widget.TabWidget/VASSAL.build.widget.TabWidget:Chits/VASSAL.build.widget.ListWidget:Bombs/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;-338;true;;Place Bomb Spawner;12376;0;false";
            String normalLargeBombSpawnerType = "placemark;Place Bomb Spawner;66,130;VASSAL.build.module.PieceWindow/VASSAL.build.widget.TabWidget/VASSAL.build.widget.TabWidget:Chits/VASSAL.build.widget.ListWidget:Bombs/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;-396;true;;Place Bomb Spawner;12407;0;false";
            String frontalSmallBombSpawnerType = "placemark;Place Frontal Bomb Spawner;66,195;VASSAL.build.module.PieceWindow/VASSAL.build.widget.TabWidget/VASSAL.build.widget.TabWidget:Chits/VASSAL.build.widget.ListWidget:Bombs/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;339;true;63743,0,rotate180;Place Frontal Bomb Spawner;12378;0;false";
            String frontalLargeBombSpawnerType = "placemark;Place Frontal Bomb Spawner;66,195;VASSAL.build.module.PieceWindow/VASSAL.build.widget.TabWidget/VASSAL.build.widget.TabWidget:Chits/VASSAL.build.widget.ListWidget:Bombs/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;394;true;63743,0,rotate180;Place Frontal Bomb Spawner;12408;0;false";

            // create the appropriate markers
            PlaceMarker normalBombSpawnMarker = new PlaceMarker();
            PlaceMarker frontalBombSpawnMarker = new PlaceMarker();

            // inject the type into the
            if(size.equals("small"))
            {
                normalBombSpawnMarker.mySetType(normalSmallBombSpanwerType);
                frontalBombSpawnMarker.mySetType(frontalSmallBombSpawnerType);

            }else if(size.equals("large"))
            {
                normalBombSpawnMarker.mySetType(normalLargeBombSpawnerType);
                frontalBombSpawnMarker.mySetType(frontalLargeBombSpawnerType);
            }

            normalBombSpawnMarker.setGpId("");
            frontalBombSpawnMarker.setGpId("");

      //      if(!size.equals("large")) {
                // now inject the markers into the piece
                normalBombSpawnMarker.setInner(piece);
                frontalBombSpawnMarker.setInner(normalBombSpawnMarker);


       //     }else{
       //         PlaceMarker marker1 = (PlaceMarker)Util.getPlaceMarkerTrait(piece,"Place Marker - Place Bomb Spawner");
        //        Util.logToChat("Place Bomb Spawner: "+marker1.myGetType());
        //        PlaceMarker marker2 = (PlaceMarker)Util.getPlaceMarkerTrait(piece,"Place Marker - Place Frontal Bomb Spawner");
        //        Util.logToChat("Place Frontal Bomb Spawner: "+marker2.myGetType());
        //    }

            return piece;
        }*/

        private GamePiece buildSideActions(GamePiece piece, String size)
        {
            String action1 = "";
            String action2 = "";
            String action3 = "";
            //large
            if(size.equals("large"))
            {
                action3 = "emb2;;2;;Cycle 3rd Action;2;;;2;;Clear 3rd Action;90,195;1;false;128;30;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side1;;;false;;1;1;true;;65,195;";
                action2 = "emb2;;2;;Cycle 2nd Action;2;;;2;;Clear 2nd Action;90,130;1;false;128;0;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side2;;;false;;1;1;true;;65,130;";
                action1 = "emb2;;2;;Cycle 1st Action;2;;;2;;Clear 1st Action;90,65;1;false;128;-30;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side3;;;false;;1;1;true;;65,65;";
            }else if(size.equals("small"))
            {
                action3 = "emb2;;2;;Cycle 3rd Action;2;;;2;;Clear 3rd Action;90,195;1;false;70;30;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side1;;;false;;1;1;true;;65,195;";
                action2 = "emb2;;2;;Cycle 2nd Action;2;;;2;;Clear 2nd Action;90,130;1;false;70;0;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side2;;;false;;1;1;true;;65,130;";
                action1 = "emb2;;2;;Cycle 1st Action;2;;;2;;Clear 1st Action;90,65;1;false;70;-30;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side3;;;false;;1;1;true;;65,65;";
            }
            //small

            Embellishment actionEmb = new Embellishment();
            actionEmb.mySetType(action3);
            actionEmb.setInner(piece);


            piece = actionEmb;

            actionEmb = new Embellishment();
            actionEmb.mySetType(action2);
            actionEmb.setInner(piece);

            piece = actionEmb;

            actionEmb = new Embellishment();
            actionEmb.mySetType(action1);
            actionEmb.setInner(piece);

            piece = actionEmb;

            /*
            //3rd 128,30
            emb2;;2;;Cycle 3rd Action;2;;;2;;Clear 3rd Action;90,195;1;false;128;30;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side1;;;false;;1;1;true;;65,195;

            //2nd 128,0
            emb2;;2;;Cycle 2nd Action;2;;;2;;Clear 2nd Action;90,130;1;false;128;0;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side2;;;false;;1;1;true;;65,130;

            //1st 128,-30
            emb2;;2;;Cycle 1st Action;2;;;2;;Clear 1st Action;90,65;1;false;128;-30;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side3;;;false;;1;1;true;;65,65;
*/
            return piece;
        }
/*
        private GamePiece buildCardboardActions(GamePiece piece, List<String> actionList, String size)
        {

            String actionImage = null;
            int actionNumber = 0;
            for(String action : actionList)
            {
                actionImage = (String)cardboardActionImages.get(action);

                actionNumber++;

                // build the action string
                StringBuilder sb = new StringBuilder();
                sb.append("emb2;Activate;2;;;2;;;2;;;;1;false;");
                sb.append((String)cardboardActionCoordinates.get(size+actionNumber));
                sb.append(";");
                sb.append(actionImage);
                sb.append(";;false;Action_");
                sb.append(action);
                sb.append(";;;false;;1;1;true;65,130;;");

                // add the action
                Embellishment actionEmb = new Embellishment();
                actionEmb.mySetType(sb.toString());
                actionEmb.setInner(piece);

                // the embellishment is now the outer piece
                piece = actionEmb;
            }
            return piece;

        }*/

        private GamePiece buildShipBaseLayer(GamePiece piece, String faction, String xwsShipName, String xwsPilot, String size)
        {

            // first find the base image name
            String shipBaseImage[] = findShipBaseImage(faction,xwsShipName, xwsPilot, size);

            boolean dualArt = false;
            if(shipBaseImage[1] != null && !shipBaseImage[1].equals(""))
            {
                dualArt = true;
            }

            //  overwrite the layer with a new state
            if(!dualArt) {
                StringBuffer sb = new StringBuffer();
                sb.append("emb2;Activate;2;;Ghost;2;;;2;;;;1;false;0;0;");
                sb.append(shipBaseImage[0]);
                if(size.equals("small")) {
                    sb.append(",Ship_Small_SeeThrough.png");

                }else if(size.equals("large")) {
                    sb.append(",Ship_Big_SeeThrough.png");

                }
                sb.append(";,;true;Base Ship;;;false;;1;1;true;65,130;71,130;");
                // now get the Layer
                Embellishment myEmb = (Embellishment)Util.getEmbellishment(piece,BASE_SHIP_LAYER_NAME);
                myEmb.mySetType(sb.toString());


            }else{
                // this is dual based
                StringBuffer sb = new StringBuffer();
                sb.append("emb2;Activate;2;;Ghost;2;;;2;;;;1;false;0;0;");
                sb.append(shipBaseImage[0]);
                if(size.equals("small")) {
                    sb.append(",Ship_Small_SeeThrough.png,");
                }else if(size.equals("large"))
                {
                    sb.append(",Ship_Big_SeeThrough.png,");
                }
                sb.append(shipBaseImage[1]);

                if(size.equals("small")) {
                    sb.append(",Ship_Small_SeeThrough.png;");
                }else if(size.equals("large"))
                {
                    sb.append(",Ship_Big_SeeThrough.png;");
                }
                sb.append("Attack,Ghost1,Landing,Ghost2;false;Base Ship;;;true;ULevel;1;1;true;65,130;71,130;");

                // now get the Layer
                Embellishment myEmb = (Embellishment)Util.getEmbellishment(piece,BASE_SHIP_LAYER_NAME);
                myEmb.mySetType(sb.toString());


                //emb2;Activate;2;;Ghost;2;;;2;;;;1;false;0;0;Ship-U-Wing_Atk.png,Ship_Big_SeeThrough.png,
                // Ship-U-Wing_Lan.png
                // ,Ship_Big_SeeThrough.png;Attack,Ghost1,Landing,Ghost2;false;Base Ship;;;true;ULevel;1;1;true;65,130;71,130;


                //Embellishment myEmb = (Embellishment)Util.getEmbellishment(piece,BASE_SHIP_LAYER_NAME);
               // Util.logToChat(myEmb.myGetType());

            }

            return piece;

        }

        private String[] findShipBaseImage(String faction, String xwsShipName, String xwsPilot, String size)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(SHIP_BASE_IMAGE_PREFIX);
            sb.append("_");
            sb.append(XWOTAUtils.simplifyFactionName(faction));
            sb.append("_");
            sb.append(xwsShipName);

            boolean dualArt = false;
            String dualBase = null;
            // now check for alt art
            String[] shipImage = AltArtShipPicker.getNewAltArtShip(xwsPilot, xwsShipName, faction);

            // if there's a blank sting in shipImage[0], then it's a standard art
            // if there's a string in shipImage[1], then it's a dual base ship
            // otherwise, use the shipImage[0]
            if(shipImage[0].equals(""))
            {
                // standard art
                sb.append("_standard");
            }else if(shipImage[1] != null && !shipImage[1].equals(""))
            {
                // this is a dual art card.
                dualArt = true;
                sb.append("_").append(shipImage[0]);
                dualBase = sb.toString();
            }else{
                sb.append("_").append(shipImage[0]);
            }
            sb.append(".png");

            String[] shipArt = new String[2];
            shipArt[0] = sb.toString();
            if(dualArt)
            {

                shipArt[1] = dualBase+shipImage[1]+".png";
            }

            // check to make sure the image(s) exist
            if(!XWOTAUtils.imageExistsInModule(shipArt[0]))
            {
                // image doesn't exist, so use a WIP image.


                // build the name
                sb = new StringBuffer();
                sb.append(SHIP_BASE_IMAGE_PREFIX).append("_");
                sb.append(XWOTAUtils.simplifyFactionName(faction));
                sb.append("_wip_");
                sb.append(size);
                sb.append(".png");

                shipArt[0] = sb.toString();
                shipArt[1] = "";

            }


            return shipArt;
        }

/*
        private GamePiece buildCardboardFiringArcs(GamePiece piece,String faction, List<String> arcList, String size)
        {


            String arcImage = "";
            for(String arc : arcList)
            {
                // look up the image for the arc

                arcImage = XWImageUtils.buildFiringArcImageName(size,faction,arc);


                // build the arc string
                StringBuilder sb = new StringBuilder();
                sb.append("emb2;Activate;2;;;2;;;2;;;;1;false;0;0;");
                sb.append(arcImage);
                sb.append(";;false;Arc_");
                sb.append(arc); // add arc name to name of Emb
                sb.append(";;;false;;1;1;true;65,130;;");

                // add the arc
                Embellishment arcEmb = new Embellishment();
                arcEmb.mySetType(sb.toString());
                arcEmb.setInner(piece);

                // the embellishment is now the outer piece
                piece = arcEmb;

            }
            return piece;

        }*/


        protected Command myUndoCommand() {
            return null;
        }

        //the following class is used to send the info to the other player whenever a ship generation command is issued, so it can be done locally on all machines playing/watching the game
        //only the ship XWS string is sent
        public static class ShipGeneratorEncoder implements CommandEncoder {
            private static final Logger logger = LoggerFactory.getLogger(StemShip.class);
            private static final String commandPrefix = "ShipGeneratorEncoder=";

            public static StemShip.ShipGenerateCommand.ShipGeneratorEncoder INSTANCE = new StemShip.ShipGenerateCommand.ShipGeneratorEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding ShipGenerateCommand");

                command = command.substring(commandPrefix.length());
                try {
                    xwsShipName = command.toString();
                } catch (Exception e) {
                    logger.error("Error decoding ShipGenerateCommand", e);
                    return null;
                }
                return null;
            }

            public String encode(Command c) {
                if (!(c instanceof ShipGenerateCommand)) {
                    return null;
                }
                logger.info("Encoding ShipGenerateCommand");
               ShipGenerateCommand dialGenCommand = (ShipGenerateCommand) c;
                try {
                    return commandPrefix + xwsShipName;
                } catch(Exception e) {
                    logger.error("Error encoding ShipGenerateCommand", e);
                    return null;
                }
            }
        }

    }




}


