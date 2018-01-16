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

    private static Map<String, String> cardboardFiringArcImages = ImmutableMap.<String, String>builder()
            .put("small/rebel/Front","Stem Small Rebel Cardboard Front Arc")
            .put("small/rebel/Turret","Stem Small Rebel Cardboard Turret Arc")
            .put("small/rebel/Auxiliary Rear","Stem Small Rebel Cardboard Aux Rear Arc")
            .put("small/rebel/Auxiliary 180","Stem Small Rebel Cardboard Aux 180 Arc")
            .put("small/rebel/Mobile","Stem Small Rebel Cardboard Mobile Arc")
            .put("small/rebel/Bullseye","Stem Small Rebel Cardboard Bullseye Arc")
            .put("small/imperial/Front","Stem Small Imperial Cardboard Front Arc")
            .put("small/imperial/Turret","Stem Small Imperial Cardboard Turret Arc")
            .put("small/imperial/Auxiliary Rear","Stem Small Imperial Cardboard Aux Rear Arc")
            .put("small/imperial/Auxiliary 180","Stem Small Imperial Cardboard Aux 180 Arc")
            .put("small/imperial/Mobile","Stem Small Imperial Cardboard Mobile Arc")
            .put("small/imperial/Bullseye","Stem Small Imperial Cardboard Bullseye Arc")
            .put("small/scum/Front","Stem Small Scum Cardboard Front Arc")
            .put("small/scum/Turret","Stem Small Scum Cardboard Turret Arc")
            .put("small/scum/Auxiliary Rear","Stem Small Scum Cardboard Aux Rear Arc")
            .put("small/scum/Auxiliary 180","Stem Small Scum Cardboard Aux 180 Arc")
            .put("small/scum/Mobile","Stem Small Scum Cardboard Mobile Arc")
            .put("small/scum/Bullseye","Stem Small Scum Cardboard Bullseye Arc")
            .put("large/rebel/Front","Stem Large Rebel Cardboard Front Arc")
            .put("large/rebel/Turret","Stem Large Rebel Cardboard Turret Arc")
            .put("large/rebel/Auxiliary Rear","Stem Large Rebel Cardboard Aux Rear Arc")
            .put("large/rebel/Auxiliary 180","Stem Large Rebel Cardboard Aux 180 Arc")
            .put("large/rebel/Mobile","Stem Large Rebel Cardboard Mobile Arc")
            .put("large/rebel/Bullseye","Stem Large Rebel Cardboard Bullseye Arc")
            .put("large/imperial/Front","Stem Large Imperial Cardboard Front Arc")
            .put("large/imperial/Turret","Stem Large Imperial Cardboard Turret Arc")
            .put("large/imperial/Auxiliary Rear","Stem Large Imperial Cardboard Aux Rear Arc")
            .put("large/imperial/Auxiliary 180","Stem Large Imperial Cardboard Aux 180 Arc")
            .put("large/imperial/Mobile","Stem Large Imperial Cardboard Mobile Arc")
            .put("large/imperial/Bullseye","Stem Large Imperial Cardboard Bullseye Arc")
            .put("large/scum/Front","Stem Large Scum Cardboard Front Arc")
            .put("large/scum/Turret","Stem Large Scum Cardboard Turret Arc")
            .put("large/scum/Auxiliary Rear","Stem Large Scum Cardboard Aux Rear Arc")
            .put("large/scum/Auxiliary 180","Stem Large Scum Cardboard Aux 180 Arc")
            .put("large/scum/Mobile","Stem Large Scum Cardboard Mobile Arc")
            .put("large/scum/Bullseye","Stem Large Scum Cardboard Bullseye Arc")
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
        ShipGenerateCommand(String thisName, GamePiece piece, String thisFaction, String thisSize) {

            // fetch the maneuver array of arrays according to the xws name passed on from autospawn or other means
            xwsShipName = thisName;
            faction = thisFaction;
            MasterShipData.ShipData shipData = MasterShipData.getShipData(xwsShipName);
            arcList = shipData.getFiringArcs();
            shipName = shipData.getName();
            this.piece = piece;
            this.size = shipData.getSize();

        }

        // construct the arcs Layers trait (Embellishment class)
        protected void executeCommand()
        {

            //TODO TEMP CODE to pick up details


/*
            Embellishment myEmb = (Embellishment)Util.getEmbellishment(piece,"Layer - Arc");
            Util.logToChat(myEmb.myGetType());
            Util.logToChat("====");
            Util.logToChat(myEmb.getType());
            */

            //TODO set cardboard arcs
           buildCardboardFiringArcs(piece,faction,arcList,size);

/*
            // TODO set the actual Firing Arcs

            //TODO Add the cardboard actions
            //TODO add the actions
            //TODO add the ship layer
            //TODO add the rotate & pivot

            // build the layers for the maneuvers on the dial
  //          buildManeuvers(piece, newMoveList);

            // build the dial back and dial hide images
 //           buildDialMask(piece,xwsShipName,faction);
*/
        }

        private void buildCardboardFiringArcs(GamePiece piece,String faction, List<String> arcList, String size)
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

                StringBuilder sb = new StringBuilder();
                sb.append("emb2;Activate;2;;;2;;;2;;;;1;false;0;0;");
                sb.append(arcImage);
                sb.append(";;false;Arc;;;false;;1;1;true;65,130;;");
                // add the arc
                Embellishment arcEmb = new Embellishment();
                arcEmb.mySetType(sb.toString());



                GamePiece p = piece;

               GamePiece innermostPiece = piece;

                while (p instanceof Decorator) {
                    Util.logToChat(p.getName() + " " + p.getClass().getName());

                    p = ((Decorator) p).getInner();

                }



            }

        }

/*
        // build the maneuvers layer
        private void buildManeuvers(GamePiece piece, List moveList)
        {
            // build the type string
            StringBuilder stateString = new StringBuilder();
            StringBuilder moveNamesString = new StringBuilder();

            // start the state string
            stateString.append("emb2;;2;;Right;2;;Left;2;;;;;false;0;-38;");

            // loop through the maneuvers from the xws-data
            int count = 0;
            String moveImage;
            for (String move : newMoveList)
            {

                // look up the image for the maneuver
                moveImage = (String)dialManeuverImages.get(move);
                if(moveImage == null)
                {
                    logToChat("Can't find image for move: " + move);
                }else{

                    count++;
                    if(count != 1)
                    {
                        stateString.append(",");
                        moveNamesString.append(",");
                    }
                    // add the maneuver image to the dial
                    stateString.append(moveImage);

                    // build move names string
                    String speed = move.substring(0,1);
                    String moveCode = move.substring(1,2);
                    String moveName = maneuverNames.get(moveCode);
                    moveNamesString.append(moveName).append(" ").append(speed);

                }
            }

            // add in move names
            stateString.append(";").append(moveNamesString.toString());

            // finish the type string
            stateString.append(";true;Move;;;false;;1;1;true;;46,0;44,0");

            Embellishment myEmb = (Embellishment)Util.getEmbellishment(piece,"Layer - Move");
            //Embellishment myEmb = (Embellishment)Decorator.getDecorator(piece,Embellishment.class);

            myEmb.mySetType(stateString.toString());

        }

*/

        protected Command myUndoCommand() {
            return null;
        }

        //the following class is used to send the info to the other player whenever a dial generation command is issued, so it can be done locally on all machines playing/watching the game
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


