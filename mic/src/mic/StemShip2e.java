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


public class StemShip2e extends Decorator implements EditablePiece {
    public static final String ID = "stemship";

    private static final String BASE_SHIP_LAYER_NAME = "Layer - Base Ship";
    private static final String TOGGLE_BASE_TRIGGER_ACTION_NAME = "Trigger Action   - Toggle Ship Base";
    private static final String SHIP_BASE_IMAGE_PREFIX = "Ship_2e_Base";

    private static Map<String, String> firingArcTypes = ImmutableMap.<String, String>builder()
            // Front Arc
            .put("small/rebelalliance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Rebel_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/galacticempire/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Empire_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/scumandvillainy/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Scum_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/resistance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Resistance_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/firstorder/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_FirstOrder_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/galacticrepublic/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Republic_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/cis/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_CIS_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")

            .put("medium/rebelalliance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_Rebel_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/galacticempire/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_Empire_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/scumandvillainy/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_Scum_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/resistance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_Resistance_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/firstorder/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_FirstOrder_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/galacticrepublic/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_Republic_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/cis/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_CIS_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")

            .put("large/rebelalliance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_Rebel_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/galacticempire/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_Empire_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/scumandvillainy/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_Scum_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/resistance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_Resistance_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/firstorder/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_FirstOrder_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/galacticrepublic/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_Republic_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/cis/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_CIS_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")

            // Full Front Arc
            .put("small/rebelalliance/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_Rebel_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/galacticempire/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_Empire_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/scumandvillainy/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_Scum_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/resistance/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_Resistance_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/firstorder/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_FirstOrder_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/galacticrepublic/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_Republic_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/cis/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_CIS_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")

            .put("medium/rebelalliance/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_Rebel_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/galacticempire/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_Empire_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/scumandvillainy/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_Scum_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/resistance/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_Resistance_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/firstorder/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_FirstOrder_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/galacticrepublic/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_Republic_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/cis/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_CIS_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")

            .put("large/rebelalliance/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Rebel_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/galacticempire/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Empire_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/scumandvillainy/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Scum_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/resistance/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Resistance_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/firstorder/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_FirstOrder_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/galacticrepublic/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Republic_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/cis/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_CIS_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")

            // Rear Arc
            .put("small/rebelalliance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_Rebel_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/galacticempire/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_Empire_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/scumandvillainy/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_Scum_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/resistance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_Resistance_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/firstorder/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_FirstOrder_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/galacticrepublic/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_Republic_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/cis/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_CIS_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")

            .put("medium/rebelalliance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_Rebel_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/galacticempire/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_Empire_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/scumandvillainy/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_Scum_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/resistance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_Resistance_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/firstorder/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_FirstOrder_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/galacticrepublic/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_Republic_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/cis/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_CIS_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")

            .put("large/rebelalliance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_Rebel_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/galacticempire/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_Empire_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/scumandvillainy/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_Scum_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/resistance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_Resistance_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/firstorder/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_FirstOrder_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/galacticrepublic/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_Republic_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/cis/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_CIS_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")

            // Bullseye
            .put("small/rebelalliance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_Rebel.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/galacticempire/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_Empire.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/scumandvillainy/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_Scum.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/resistance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_Resistance.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/firstorder/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_FirstOrder.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/galacticrepublic/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_Republic.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/cis/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_CIS.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")

            .put("medium/rebelalliance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_Rebel.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/galacticempire/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_Empire.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/scumandvillainy/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_Scum.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/resistance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_Resistance.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/firstorder/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_FirstOrder.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/galacticrepublic/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_Republic.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/cis/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_CIS.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")

            .put("large/rebelalliance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_Rebel.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/galacticempire/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_Empire.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/scumandvillainy/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_Scum.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/resistance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_Resistance.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/firstorder/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_FirstOrder.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/galacticrepublic/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_Republic.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/cis/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_CIS.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")

            .build();

    public StemShip2e(){
        this(null);
    }

    public StemShip2e(GamePiece piece){
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
        String xwsPilot = "";
        boolean needsBombCapability;
        boolean dualBase;
        String dualBaseToggleMenuText;
        String base1ReportIdentifier;
        String base2ReportIdentifier;
        VassalXWSPilotPieces2e source;

        ShipGenerateCommand(VassalXWSPilotPieces2e source, String shipXws,   GamePiece piece, String faction, String xwsPilot,
                            boolean needsBombCapability, Boolean hasDualBase,
                            String dualBaseToggleMenuText, String base1ReportIdentifier, String base2ReportIdentifier) {
        //ShipGenerateCommand(String shipXws,   GamePiece piece, String faction, String xwsPilot) {

            // fetch the maneuver array of arrays according to the xws name passed on from autospawn or other means
            xwsShipName = Canonicalizer.getCleanedName(shipXws);
            this.faction = Canonicalizer.getCleanedName(faction);
            shipName = source.getShipData().getName();
            this.piece = piece;
            this.xwsPilot = xwsPilot;
            this.size = source.getShipData().getSize();
            this.needsBombCapability = needsBombCapability;
            this.source = source;

            this.dualBase = hasDualBase == null ? false : hasDualBase.booleanValue();
            this.dualBaseToggleMenuText = dualBaseToggleMenuText;
            this.base1ReportIdentifier = base1ReportIdentifier;
            this.base2ReportIdentifier = base2ReportIdentifier;

        }

        protected void executeCommand()
        {
            // find the appropriate baseImage
            piece = buildShipBaseLayer(piece,faction,xwsShipName,xwsPilot, size, dualBase, dualBaseToggleMenuText, base1ReportIdentifier,base2ReportIdentifier);

            // Add the Target Lock capability
           piece = addTargetLock(piece,faction,size);

            // add the firing arcs needed
            piece = addFiringArcs(piece);

            if(this.needsBombCapability) {
                piece = addBombCapability(piece,size);
            }

        }


        private GamePiece addFiringArcs(GamePiece newGamePiece)
        {
            String newFaction = XWOTAUtils.simplifyFactionName(faction);

            // first get the list of arcs needed
            String arc = null;
            Iterator<XWS2Pilots.Stat2e> i = source.getShipData().getStats().iterator();
            Embellishment emb = null;
            String arcKey = null;
            String newType = null;
            while(i.hasNext())
            {
                arc = i.next().getArc();
                if(arc.equals("Rear Arc")) {
                    emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Show Auxiliary Firing Arc");
                } else if(arc.equals("Full Front Arc")) {
                    emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Show Full Front Arc");
                } else continue;

                    arcKey = Canonicalizer.getCleanedName(source.getShipData().getSize()) + "/" + Canonicalizer.getCleanedName(source.getPilotData().getFaction()) + "/" + arc;

                    newType = firingArcTypes.get(arcKey);
                    if(newType != null && !newType.isEmpty())
                    {
                        emb.mySetType(newType);
                    }
                    newType = null;

            }

            //add front arc for all, for card effects mostly even if there's no front arc primary weapon
            emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Arc");
            arc = "Front Arc";
            arcKey = Canonicalizer.getCleanedName(source.getShipData().getSize()) + "/"
                    + Canonicalizer.getCleanedName(source.getPilotData().getFaction()) + "/" + arc;
            newType = firingArcTypes.get(arcKey);
            if(newType != null && !newType.isEmpty())
            {
                emb.mySetType(newType);
            }

            //add bullseye for all
            emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Show Bullseye Arc");
            arc = "Bullseye";
            arcKey = Canonicalizer.getCleanedName(source.getShipData().getSize()) + "/"
                    + Canonicalizer.getCleanedName(source.getPilotData().getFaction()) + "/" + arc;
            newType = firingArcTypes.get(arcKey);
            if(newType != null && !newType.isEmpty())
            {
                emb.mySetType(newType);
            }
            return newGamePiece;
        }

        private GamePiece addTargetLock(GamePiece newGamePiece, String faction, String newSize)
        {
            final String targetLockLayerName = "Layer - Lock";

            final String cisSmallImage = "TargetLock_CIS.svg";
            final String cisMediumImage = "TargetLock_CIS_Medium.svg";
            final String cisLargeImage = "TargetLock_CIS_Large.svg";

            final String firstorderSmallImage = "TargetLock_FirstOrder.svg";
            final String firstorderMediumImage = "TargetLock_FirstOrder_Medium.svg";
            final String firstorderLargeImage = "TargetLock_FirstOrder_Large.svg";

            final String rebelSmallImage = "TargetLock_Rebel.svg";
            final String rebelMediumImage = "TargetLock_Rebel_Medium.svg";
            final String rebelLargeImage = "TargetLock_Rebel_Large.svg";

            final String republicSmallImage = "TargetLock_Republic.svg";
            final String republicMediumImage = "TargetLock_Republic_Medium.svg";
            final String republicLargeImage = "TargetLock_Republic_Large.svg";

            final String resistanceSmallImage = "TargetLock_Resistance.svg";
            final String resistanceMediumImage = "TargetLock_Resistance_Medium.svg";
            final String resistanceLargeImage = "TargetLock_Resistance_Large.svg";

            final String imperialSmallImage = "TargetLock_Imperial.svg";
            final String imperialMediumImage = "TargetLock_Imperial_Medium.svg";
            final String imperialLargeImage = "TargetLock_Imperial_Large.svg";

            final String scumSmallImage = "TargetLock_Scum.svg";
            final String scumMediumImage = "TargetLock_Scum_Medium.svg";
            final String scumLargeImage = "TargetLock_Scum_Large.svg";

            String newFaction = XWOTAUtils.simplifyFactionName(faction);

            Embellishment myEmb = (Embellishment)Util.getEmbellishment(newGamePiece,targetLockLayerName);

            newSize = Canonicalizer.getCleanedName(newSize);

            StringBuilder sb = new StringBuilder();
            sb.append("emb2;;2;;Toggle Lock;2;;;2;;;;1;true;0;0;,");
            if(newSize.equals("small") && faction.equals("rebelalliance"))
            {
                sb.append(rebelSmallImage);
            }else if(newSize.equals("small") && faction.equals("galacticempire") )
            {
                sb.append(imperialSmallImage);
            }else if(newSize.equals("small") && faction.equals("scumandvillainy") )
            {
                sb.append(scumSmallImage);
            }else if(newSize.equals("small") && faction.equals("resistance") )
            {
                sb.append(resistanceSmallImage);
            }else if(newSize.equals("small") && faction.equals("firstorder") )
            {
                sb.append(firstorderSmallImage);
            }else if(newSize.equals("small") && faction.equals("galacticrepublic") )
            {
                sb.append(republicSmallImage);
            }else if(newSize.equals("small") && faction.equals("cis") )
            {
                sb.append(cisSmallImage);
            }



            else if(newSize.equals("medium") && faction.equals("rebelalliance"))
            {
                sb.append(rebelMediumImage);
            }else if(newSize.equals("medium") && faction.equals("galacticempire") )
            {
                sb.append(imperialMediumImage);
            }else if(newSize.equals("medium") && faction.equals("scumandvillainy") )
            {
                sb.append(scumMediumImage);
            }else if(newSize.equals("medium") && faction.equals("resistance") )
            {
                sb.append(resistanceMediumImage);
            }else if(newSize.equals("medium") && faction.equals("firstorder") )
            {
                sb.append(firstorderMediumImage);
            }else if(newSize.equals("medium") && faction.equals("galacticrepublic") )
            {
                sb.append(republicMediumImage);
            }else if(newSize.equals("medium") && faction.equals("cis") )
            {
                sb.append(cisMediumImage);
            }



            else if(newSize.equals("large") && faction.equals("rebelalliance"))
            {
                sb.append(rebelLargeImage);
            }else if(newSize.equals("large") && faction.equals("galacticempire") )
            {
                sb.append(imperialLargeImage);
            }else if(newSize.equals("large") && faction.equals("scumandvillainy") )
            {
                sb.append(scumLargeImage);
            }else if(newSize.equals("large") && faction.equals("resistance") )
            {
                sb.append(resistanceLargeImage);
            }else if(newSize.equals("large") && faction.equals("firstorder") )
            {
                sb.append(firstorderLargeImage);
            }else if(newSize.equals("large") && faction.equals("galacticrepublic") )
            {
                sb.append(republicLargeImage);
            }else if(newSize.equals("large") && faction.equals("cis") )
            {
                sb.append(cisLargeImage);
            }

            sb.append(";,;true;Lock;;;false;;0;1;true;;76,130;");
            try{

                myEmb.mySetType(sb.toString());
            }catch(Exception e){
                Util.logToChat("stemship2e line 406 can't load the TL gfx");
            }

            return newGamePiece;
        }

        private GamePiece addBombCapability(GamePiece piece, String size)
        {
            String normalSmallBombSpanwerType = "placemark;Place Bomb Spawner;66,130;VASSAL.build.module.PieceWindow/VASSAL.build.widget.TabWidget/VASSAL.build.widget.TabWidget:Chits/VASSAL.build.widget.ListWidget:Bombs/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;-338;true;;Place Bomb Spawner;12376;0;false";
            String normalLargeBombSpawnerType = "placemark;Place Bomb Spawner;66,130;VASSAL.build.module.PieceWindow/VASSAL.build.widget.TabWidget/VASSAL.build.widget.TabWidget:Chits/VASSAL.build.widget.ListWidget:Bombs/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;-396;true;;Place Bomb Spawner;12407;0;false";
            String frontalSmallBombSpawnerType = "placemark;Place Frontal Bomb Spawner;66,195;VASSAL.build.module.PieceWindow/VASSAL.build.widget.TabWidget/VASSAL.build.widget.TabWidget:Chits/VASSAL.build.widget.ListWidget:Bombs/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;339;true;63743,0,rotate180;Place Frontal Bomb Spawner;12378;0;false";
            String frontalLargeBombSpawnerType = "placemark;Place Frontal Bomb Spawner;66,195;VASSAL.build.module.PieceWindow/VASSAL.build.widget.TabWidget/VASSAL.build.widget.TabWidget:Chits/VASSAL.build.widget.ListWidget:Bombs/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;394;true;63743,0,rotate180;Place Frontal Bomb Spawner;12408;0;false";

            // get the Embellishments

            PlaceMarker normalPlaceMarker = (PlaceMarker)Util.getPlaceMarkerTrait(piece,"Place Marker - Place Bomb Spawner");
            PlaceMarker frontalPlaceMarker = (PlaceMarker)Util.getPlaceMarkerTrait(piece,"Place Marker - Place Frontal Bomb Spawner");

            // inject the type into the
            if(size.equals("small"))
            {
                normalPlaceMarker.mySetType(normalSmallBombSpanwerType);
                frontalPlaceMarker.mySetType(frontalSmallBombSpawnerType);

            }else if(size.equals("large"))
            {
                normalPlaceMarker.mySetType(normalLargeBombSpawnerType);
                frontalPlaceMarker.mySetType(frontalLargeBombSpawnerType);
            }


            return piece;
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

        private GamePiece buildShipBaseLayer(GamePiece piece, String faction,
                                             String xwsShipName, String xwsPilot,
                                             String size, boolean dualBase, String dualBaseMenuText,
                                             String base1ReportIdentifier, String base2ReportIdentifier)
        {



        //    boolean dualArt = false;
         //   if(shipBaseImage[1] != null && !shipBaseImage[1].equals(""))
        //    {
        //        dualArt = true;
         //   }

            //  overwrite the layer with a new state
            if(!dualBase) {
                // first find the base image name
                String shipBaseImage = findShipBaseImage(xwsShipName, xwsPilot, size);
               // mic.Util.logToChat(xwsShipName + " is NOT dual based");
                StringBuffer sb = new StringBuffer();
                sb.append("emb2;Activate;2;;Ghost;2;;;2;;;;1;false;0;0;");
                sb.append(shipBaseImage);

                if(size.equals("small")) {
                    sb.append(",Ship_Small_SeeThrough.png");

                }else if(size.equals("medium")) {
                    sb.append(",Ship-Medium_SeeThrough");

                }else if(size.equals("large")) {
                    sb.append(",Ship_Big_SeeThrough.png");

                }
                sb.append(";base1,ghost1;false;Base Ship;;;true;ULevel;1;1;true;65,130;;");
                // now get the Layer
                Embellishment myEmb = (Embellishment)Util.getEmbellishment(piece,BASE_SHIP_LAYER_NAME);
             //   mic.Util.logToChat(myEmb.myGetType());
                myEmb.mySetType(sb.toString());


            }else{
           //     mic.Util.logToChat(xwsShipName + " is dual based");


                // this is a dual based ship

                MasterShipData.ShipData shipData = MasterShipData.getShipData(xwsShipName);

                // get the 2 base images
                String baseImage1Identifier = shipData.getBaseImage1Identifier();
                String baseImage2Identifier = shipData.getBaseImage2Identifier();

                StringBuffer sb = new StringBuffer();
                sb.append(SHIP_BASE_IMAGE_PREFIX);
                sb.append("_");
                sb.append(XWOTAUtils.simplifyFactionName(faction));
                sb.append("_");
                sb.append(xwsShipName);
                sb.append("_");

                String baseImage1 = sb.toString() + baseImage1Identifier+".png";
                String baseImage2 = sb.toString() + baseImage2Identifier+".png";


                // build image name from identifier

                sb = new StringBuffer();
                //emb2;Activate;2;;;2;;;2;;;;1;false;0;0;Ship_generic_large.png,Ship_Big_SeeThrough.png,Ship_generic_large.png,Ship_Big_SeeThrough.png;base1,ghost1,base2,ghost2;false;Base Ship;;;true;ULevel;1;1;true;65,130;;
                sb.append("emb2;Activate;2;;;2;;;2;;;;1;false;0;0;");
                sb.append(baseImage1);
                if(size.equals("small")) {
                    sb.append(",Ship_Small_SeeThrough.png,");

                }else if(size.equals("large")) {
                    sb.append(",Ship_Big_SeeThrough.png,");
                }
                sb.append(baseImage2);
                if(size.equals("small")) {
                    sb.append(",Ship_Small_SeeThrough.png");

                }else if(size.equals("large")) {
                    sb.append(",Ship_Big_SeeThrough.png");
                }
              //  sb.append(";base1,ghost1,base2,ghost2;false;Base Ship;;;true;ULevel;1;1;true;65,130;;");

                sb.append(";");
                sb.append(base1ReportIdentifier);
                sb.append(",ghost1,");
                sb.append(base2ReportIdentifier);
                sb.append(",ghost2;false;Base Ship;;;true;ULevel;1;1;true;65,130;;");
                Embellishment myEmb = (Embellishment)Util.getEmbellishment(piece,BASE_SHIP_LAYER_NAME);

                myEmb.mySetType(sb.toString());


                // now set the trigger action

                sb = new StringBuffer();
                sb.append("macro;Toggle Ship Base;");
                sb.append(dualBaseMenuText);

                //total hack to make x-wing sfoils work with pivotatk instead of pivot like is used in U-Wings
                String pivotTypeString = "dopivot";
                if(size.equals("small") && "standard".equals(baseImage1Identifier))  pivotTypeString ="dopivotatk";

                sb.append(";85,520;{ULevel==1 || ULevel==3};;63743\\,0\\,"+pivotTypeString+";false;;;counted;;;;false;;1;1");
                TriggerAction trig = (TriggerAction)Util.getTriggerAction(piece,TOGGLE_BASE_TRIGGER_ACTION_NAME);

                trig.mySetType(sb.toString());

                /*
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
*/
            }

            return piece;

        }

        private String findShipBaseImage(String xwsShipName, String xwsPilot, String size)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(SHIP_BASE_IMAGE_PREFIX);
            sb.append("_");
            sb.append(this.faction);
            sb.append("_");
            sb.append(xwsShipName);

            boolean dualArt = false;
            String dualBase = null;
            // now check for alt art
            String shipImage = AltArtShipPicker.getNewAltArtShip(xwsPilot, xwsShipName, this.faction);

            // if there's a blank string in shipImage[0], then it's a standard art
            // if there's a string in shipImage[1], then it's a dual base ship
            // otherwise, use the shipImage[0]
            if(shipImage == null || shipImage.equals(""))
            {
                // standard art
                sb.append("_standard");
           // }else if(shipImage[1] != null && !shipImage[1].equals(""))
          //  {
         //       // this is a dual art card.
          //      dualArt = true;
          //      sb.append("_").append(shipImage[0]);
          //      dualBase = sb.toString();
            }else{
                sb.append("_").append(shipImage);
            }
            sb.append(".png");

            String shipArt = new String();
            shipArt = sb.toString();
        //    mic.Util.logToChat(shipArt);
        //    if(dualArt)
         //   {

          //      shipArt[1] = dualBase+shipImage[1]+".png";
         //   }

            // check to make sure the image(s) exist
            if(!XWOTAUtils.imageExistsInModule(shipArt))
            {
                // image doesn't exist, so use a WIP image.


                // build the name
                sb = new StringBuffer();
                sb.append(SHIP_BASE_IMAGE_PREFIX).append("_");
                sb.append(this.faction);
                sb.append("_wip_");
                sb.append(size);
                sb.append(".png");

                shipArt = sb.toString();
              //  shipArt[1] = "";

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
            private static final Logger logger = LoggerFactory.getLogger(StemShip2e.class);
            private static final String commandPrefix = "ShipGeneratorEncoder=";

            public static StemShip2e.ShipGenerateCommand.ShipGeneratorEncoder INSTANCE = new StemShip2e.ShipGenerateCommand.ShipGeneratorEncoder();

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


