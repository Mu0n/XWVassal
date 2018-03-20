package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.Embellishment;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
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

//    @Override
//    public Command keyEvent(KeyStroke stroke) {
//        //check to see if 'x' was pressed
//        if(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK, true).equals(stroke)) {
//            logToChatWithTime("temporary trigger for Dial generation -will be eventually ported to autospawn\nPossibly to a right click menu as well with a dynamically fetched list of all ships??");
//            GamePiece piece = getInner();

//            // this is hardcoded - need to fix
//            DialGenerateCommand myDialGen = new DialGenerateCommand("attackshuttle", piece, "Rebel Alliance");
//            Command stringOCommands = piece.keyEvent(stroke);
//            stringOCommands.append(myDialGen);

//            myDialGen.execute();
//            return stringOCommands;
//        }

//        return piece.keyEvent(stroke);
//    }

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
        ShipGenerateCommand(String shipXws,   GamePiece piece, String faction, String xwsPilot ) {

            // fetch the maneuver array of arrays according to the xws name passed on from autospawn or other means
            xwsShipName = shipXws;
            this.faction = faction;
            MasterShipData.ShipData shipData = MasterShipData.getShipData(xwsShipName);
            //arcList = shipData.getFiringArcs();
            shipName = shipData.getName();
            this.piece = piece;
            this.xwsPilot = xwsPilot;
            this.size = shipData.getSize();
           // this.actionList = shipData.getActions();
        }

        // construct the arcs Layers trait (Embellishment class)
        protected void executeCommand()
        {

            // find the appropriate baseImage
            piece = buildShipBaseLayer(piece,faction,xwsShipName,xwsPilot, size);

            // set the firing arcs on the cardboard
          //  piece = buildCardboardFiringArcs(piece,faction,arcList,size);


            // set the actions on the cardboard
       //     piece = buildCardboardActions(piece, actionList, size);

            //TODO add the actions
            //TODO add the side actions
         //   piece =  buildSideActions(piece,size);


            //TODO add the ship layer
            //TODO add the rotate & pivot

            // build the layers for the maneuvers on the dial
  //          buildManeuvers(piece, newMoveList);

            // build the dial back and dial hide images
 //           buildDialMask(piece,xwsShipName,faction);

        }

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
            String shipBaseImage[] = findShipBaseImage(faction,xwsShipName, xwsPilot);


            boolean dualArt = false;
            if(shipBaseImage[1] != null && !shipBaseImage[1].equals(""))
            {
                dualArt = true;
            }

            //emb2;Activate;2;;Ghost;2;;;2;;;;1;false;0;0;Ship_generic_small.png,Ship_Small_SeeThrough.png;,;true;Base Ship;;;false;;1;1;true;65,130;71,130;

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

        private String[] findShipBaseImage(String faction, String xwsShipName, String xwsPilot)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(SHIP_BASE_IMAGE_PREFIX);
            sb.append("_");
            sb.append(XWImageUtils.simplifyFactionName(faction));
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


