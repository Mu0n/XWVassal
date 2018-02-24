package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Map;


public class StemPilot extends Decorator implements EditablePiece {

    private static Map<String, String> factionBacks = ImmutableMap.<String, String>builder()
            .put("Rebel Alliance","Pilot-Rebel_Back.jpg")
            .put("Resistance","Pilot-Resistance_Back.jpg")
            .put("Galactic Empire","Pilot-Imperial_Back.jpg")
            .put("First Order","Pilot-First_Order_Back.jpg")
            .put("Scum & Villainy","Pilot-S&V_Back.jpg")
            .build();

    public static final String ID = "stemPilot";


    public StemPilot(){
        this(null);
    }

    public StemPilot(GamePiece piece){
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
        return "Custom StemShip (mic.StemPilot)";
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
    public static class PilotGenerateCommand extends Command {

        GamePiece piece;
//        static String xwsPilotName = "";
        String shipName;
        static String faction = "";
        static String pilotName = "";
        String shipXWS = "";
        static String pilotXWSencoding = "";
        //static String stemPieceName = "";
        PilotGenerateCommand(String pilotXWS, GamePiece piece, String thisFaction, String xwsShipName, String inPilotName)
        {
            //xwsPilotName = pilotXWS;

            shipXWS = xwsShipName;
            faction = thisFaction;
            pilotName = inPilotName;
            String factionXWS = faction.toLowerCase().replaceAll(" ","");
            pilotXWSencoding = factionXWS+"_"+shipXWS+"_"+pilotXWS;

            this.piece = piece;
/*
            if (shipNumber != null && shipNumber > 0) {
                this.piece.setProperty("Pilot ID #", shipNumber);
            } else {
                this.piece.setProperty("Pilot ID #", "");
            }

            this.piece.setProperty("Ship Type",shipName);
            this.piece.setProperty("Pilot Name",pilotName);
*/
        }

        // construct the Pilot Card piece
        protected void executeCommand()
        {
            String factionXWS = faction.toLowerCase().replaceAll(" ","");
            String pilotCardImage = "Pilot_"+pilotXWSencoding+".jpg";

            // check to see if the image for the pilot card exists locally
            if(!Util.imageExistsInModule(pilotCardImage.trim()))
            {
                // image doesn't exist
                Util.logToChat("Pilot image not found: "+pilotCardImage);

                // download the image from OTA
                Util.downloadAndSaveImageFromOTA("pilots", pilotCardImage);
            }




            // now add the image front and back

            piece = buildImageLayer(piece,pilotCardImage.trim(),pilotName, faction);


/*
            // set the firing arcs on the cardboard
            piece = buildCardboardFiringArcs(piece,faction,arcList,size);


            // TODO set the actual Firing Arcs

            // set the actions on the cardboard
            piece = buildCardboardActions(piece, actionList, size);

            //TODO add the actions
            //TODO add the side actions
            piece =  buildSideActions(piece,size);

*/
            //TODO add the ship layer
            //TODO add the rotate & pivot

            // build the layers for the maneuvers on the dial
  //          buildManeuvers(piece, newMoveList);

            // build the dial back and dial hide images
 //           buildDialMask(piece,xwsShipName,faction);

        }

        private GamePiece buildImageLayer(GamePiece piece, String pilotCardImage, String pilotName, String faction)
        {

            String backImage = factionBacks.get(faction);

            StringBuilder sb = new StringBuilder();

            sb.append("emb2;Activate;2;;Flip;2;;;2;;;;1;false;0;0;");
            sb.append(pilotCardImage);//Front image
            sb.append(",");
            sb.append(backImage);//Back image
            sb.append(";Destroyed,");
            sb.append(pilotName);//pilot name
            sb.append(";true;PilotImage;;;false;;1;1;true;65,130;70,130;");//LEAVE

            Embellishment emb = (Embellishment)Util.getEmbellishment(piece,"Layer - PilotImage");
            emb.mySetType(sb.toString());

            return piece;
        }
/*
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
            //emb2;;2;;Cycle 3rd Action;2;;;2;;Clear 3rd Action;90,195;1;false;128;30;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side1;;;false;;1;1;true;;65,195;

            //2nd 128,0
            //emb2;;2;;Cycle 2nd Action;2;;;2;;Clear 2nd Action;90,130;1;false;128;0;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side2;;;false;;1;1;true;;65,130;

            //1st 128,-30
            //emb2;;2;;Cycle 1st Action;2;;;2;;Clear 1st Action;90,65;1;false;128;-30;,Action-Focus.png,Action-Evade.png,Action-Barrel_Roll.png,Action-Boost.png,Action-Elite.png,Action-Crew.png,Action-Bomb.png,Action-Reinforce.png,Action-Coordinate.png,Action-Illicit.png,Action-Rotate_Arc.png,Action-SLAM.png,Action-Cloak.png,Action-Reload.png,Action-Target_Lock.png,Action-Mech.png;,,,,,,,,,,,,,,,,;true;Actions_Side3;;;false;;1;1;true;;65,65;

            return piece;
        }
*/
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

        }
*/
/*
        private GamePiece buildCardboardFiringArcs(GamePiece piece,String faction, List<String> arcList, String size)
        {
            StringBuilder arcImagePrefixSB = new StringBuilder();
            arcImagePrefixSB.append(size);
            arcImagePrefixSB.append("/");

            // find the faction
            if(faction.equals("Rebel Alliance") || faction.equals("Resistance"))
            {
                arcImagePrefixSB.append("rebel");
            }else if(faction.equals("Galactic Empire") ||faction.equals("First Order"))
            {
                arcImagePrefixSB.append("imperial");
            }else if(faction.equals("Scum & Villainy"))
            {
                arcImagePrefixSB.append("scum");
            }

            arcImagePrefixSB.append("/");

            String arcImage = "";
            for(String arc : arcList)
            {
                // look up the image for the arc
                arcImage = (String)cardboardFiringArcImages.get(arcImagePrefixSB.toString() + arc);

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

        }
*/

        protected Command myUndoCommand() {
            return null;
        }

        //the following class is used to send the info to the other player whenever a ship generation command is issued, so it can be done locally on all machines playing/watching the game
        //only the ship XWS string is sent
        public static class PilotGeneratorEncoder implements CommandEncoder {
            private static final Logger logger = LoggerFactory.getLogger(StemPilot.class);
            private static final String commandPrefix = "PilotGeneratorEncoder=";

            public static StemPilot.PilotGenerateCommand.PilotGeneratorEncoder INSTANCE = new StemPilot.PilotGenerateCommand.PilotGeneratorEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding PilotGenerateCommand");

                command = command.substring(commandPrefix.length());
                try {
                    pilotXWSencoding = command.toString();
                } catch (Exception e) {
                    logger.error("Error decoding PilotGenerateCommand", e);
                    return null;
                }
                return null;
            }

            public String encode(Command c) {
                if (!(c instanceof PilotGenerateCommand)) {
                    return null;
                }
                logger.info("Encoding PilotGenerateCommand");
                PilotGenerateCommand dialGenCommand = (PilotGenerateCommand) c;
                try {
                    return commandPrefix + pilotXWSencoding;
                } catch(Exception e) {
                    logger.error("Error encoding PilotGenerateCommand", e);
                    return null;
                }
            }
        }

    }




}


