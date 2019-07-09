package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import mic.ota.OTAContentsChecker;
import mic.ota.OTAMasterShips;
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
    private static final String SHIP_BASE_IMAGE_PREFIX = "SB_2e";

    private static Map<String, String> firingArcTypes = ImmutableMap.<String, String>builder()
            // Front Arc
            .put("small/rebelalliance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Rebel_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/galacticempire/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Empire_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/scumandvillainy/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Scum_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/resistance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Resistance_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/firstorder/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_FirstOrder_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/galacticrepublic/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Republic_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("small/separatistalliance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-480;,Arc_2e_CIS_Small.svg;,;true;Arc;;;false;;1;1;true;;70,130;")

            .put("medium/rebelalliance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_Rebel_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/galacticempire/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_Empire_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/scumandvillainy/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_Scum_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/resistance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_Resistance_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/firstorder/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_FirstOrder_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/galacticrepublic/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_Republic_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("medium/separatistalliance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-508;,Arc_2e_CIS_Medium.svg;,;true;Arc;;;false;;1;1;true;;70,130;")

            .put("large/rebelalliance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_Rebel_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/galacticempire/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_Empire_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/scumandvillainy/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_Scum_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/resistance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_Resistance_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/firstorder/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_FirstOrder_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/galacticrepublic/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_Republic_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")
            .put("large/separatistalliance/Front Arc","emb2;;2;;Toggle Firing Arc;2;;;2;;;;;true;0;-537;,Arc_2e_CIS_Large.svg;,;true;Arc;;;false;;1;1;true;;70,130;")

            // Full Front Arc
            .put("small/rebelalliance/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_Rebel_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/galacticempire/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_Empire_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/scumandvillainy/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_Scum_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/resistance/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_Resistance_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/firstorder/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_FirstOrder_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/galacticrepublic/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-452;,Arc_2e_Republic_Small_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("small/separatistalliance/Full Front Arc","emb2;;2;;Show Full Front Arc;2;;;2;;;;;true;0;-452;,Arc_2e_CIS_Small_FFA.svg;,;true;Toggle Full Front Arc;;;false;;1;1;true;;78,130;")

            .put("medium/rebelalliance/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_Rebel_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/galacticempire/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_Empire_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/scumandvillainy/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_Scum_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/resistance/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_Resistance_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/firstorder/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_FirstOrder_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/galacticrepublic/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_Republic_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")
            .put("medium/separatistalliance/Full Front Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;-466;,Arc_2e_CIS_Medium_FFA.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;78,130;")

            .put("large/rebelalliance/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Rebel_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/galacticempire/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Empire_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/scumandvillainy/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Scum_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/resistance/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Resistance_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/firstorder/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_FirstOrder_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/galacticrepublic/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_Republic_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")
            .put("large/separatistalliance/Full Front Arc","emb2;;2;;Show Aux Arc;2;;;2;;;;;true;0;-480;,Arc_2e_CIS_Large_FFA.svg;,;true;Show Big Aux Arc;;;false;;1;1;true;;78,130;")

            // Full Back Arc
            .put("small/rebelalliance/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;452;,Arc_2e_Rebel_Small_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("small/galacticempire/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;452;,Arc_2e_Empire_Small_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("small/scumandvillainy/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;452;,Arc_2e_Scum_Small_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("small/resistance/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;452;,Arc_2e_Resistance_Small_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("small/firstorder/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;452;,Arc_2e_FirstOrder_Small_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("small/galacticrepublic/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;452;,Arc_2e_Republic_Small_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("small/separatistalliance/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;452;,Arc_2e_CIS_Small_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")

            .put("medium/rebelalliance/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;466;,Arc_2e_Rebel_Medium_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("medium/galacticempire/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;466;,Arc_2e_Empire_Medium_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("medium/scumandvillainy/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;466;,Arc_2e_Scum_Medium_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("medium/resistance/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;466;,Arc_2e_Resistance_Medium_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("medium/firstorder/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;466;,Arc_2e_FirstOrder_Medium_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("medium/galacticrepublic/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;466;,Arc_2e_Republic_Medium_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("medium/separatistalliance/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;466;,Arc_2e_CIS_Medium_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")

            .put("large/rebelalliance/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;480;,Arc_2e_Rebel_Large_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("large/galacticempire/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;480;,Arc_2e_Empire_Large_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("large/scumandvillainy/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;480;,Arc_2e_Scum_Large_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("large/resistance/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;480;,Arc_2e_Resistance_Large_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("large/firstorder/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;480;,Arc_2e_FirstOrder_Large_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("large/galacticrepublic/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;480;,Arc_2e_Republic_Large_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")
            .put("large/separatistalliance/Full Back Arc","emb2;;2;;Show Full Back Arc;2;;;2;;;;;true;0;480;,Arc_2e_CIS_Large_FBA.svg;,;true;Toggle Full Back Arc;;;false;;1;1;true;;78,520;")

            // Rear Arc
            .put("small/rebelalliance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_Rebel_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/galacticempire/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_Empire_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/scumandvillainy/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_Scum_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/resistance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_Resistance_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/firstorder/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_FirstOrder_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/galacticrepublic/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_Republic_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("small/separatistalliance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;481;,Arc_2e_CIS_Small_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")

            .put("medium/rebelalliance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_Rebel_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/galacticempire/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_Empire_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/scumandvillainy/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_Scum_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/resistance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_Resistance_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/firstorder/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_FirstOrder_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/galacticrepublic/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_Republic_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("medium/separatistalliance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;509;,Arc_2e_CIS_Medium_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")

            .put("large/rebelalliance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_Rebel_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/galacticempire/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_Empire_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/scumandvillainy/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_Scum_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/resistance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_Resistance_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/firstorder/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_FirstOrder_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/galacticrepublic/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_Republic_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")
            .put("large/separatistalliance/Rear Arc","emb2;;2;;Show Auxiliary Arc;2;;;2;;;;;true;0;537;,Arc_2e_CIS_Large_Aux.svg;,;true;Show Auxiliary Firing Arc;;;false;;1;1;true;;86,130;")

            // Bullseye
            .put("small/rebelalliance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_Rebel.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/galacticempire/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_Empire.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/scumandvillainy/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_Scum.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/resistance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_Resistance.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/firstorder/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_FirstOrder.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/galacticrepublic/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_Republic.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("small/separatistalliance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-479;,Arc_2e_BE_CIS.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")

            .put("medium/rebelalliance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_Rebel.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/galacticempire/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_Empire.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/scumandvillainy/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_Scum.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/resistance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_Resistance.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/firstorder/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_FirstOrder.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/galacticrepublic/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_Republic.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("medium/separatistalliance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-507;,Arc_2e_BE_CIS.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")

            .put("large/rebelalliance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_Rebel.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/galacticempire/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_Empire.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/scumandvillainy/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_Scum.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/resistance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_Resistance.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/firstorder/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_FirstOrder.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/galacticrepublic/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_Republic.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")
            .put("large/separatistalliance/Bullseye","emb2;;2;;Toggle Bullseye Arc;2;;;2;;;;;true;0;-535;,Arc_2e_BE_CIS.svg;,;true;Show Bullseye Arc;;;false;;1;1;true;;88,130;")


            //Left Arc
            .put("small/rebelalliance/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-476;0;,Arc_2e_Rebel_Small_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("small/galacticempire/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-476;0;,Arc_2e_Empire_Small_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("small/scumandvillainy/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-476;0;,Arc_2e_Scum_Small_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("small/resistance/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-476;0;,Arc_2e_Resistance_Small_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("small/firstorder/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-476;0;,Arc_2e_FirstOrder_Small_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("small/galacticrepublic/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-476;0;,Arc_2e_Republic_Small_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("small/separatistalliance/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-476;0;,Arc_2e_CIS_Small_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")

            .put("medium/rebelalliance/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-505;0;,Arc_2e_Rebel_Medium_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("medium/galacticempire/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-505;0;,Arc_2e_Empire_Medium_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("medium/scumandvillainy/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-505;0;,Arc_2e_Scum_Medium_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("medium/resistance/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-505;0;,Arc_2e_Resistance_Medium_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("medium/firstorder/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-505;0;,Arc_2e_FirstOrder_Medium_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("medium/galacticrepublic/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-505;0;,Arc_2e_Republic_Medium_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("medium/separatistalliance/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-505;0;,Arc_2e_CIS_Medium_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")

            .put("large/rebelalliance/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-533;0;,Arc_2e_Rebel_Large_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("large/galacticempire/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-533;0;,Arc_2e_Empire_Large_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("large/scumandvillainy/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-533;0;,Arc_2e_Scum_Large_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("large/resistance/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-533;0;,Arc_2e_Resistance_Large_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("large/firstorder/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-533;0;,Arc_2e_FirstOrder_Large_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("large/galacticrepublic/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-533;0;,Arc_2e_Republic_Large_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")
            .put("large/separatistalliance/Left Arc","emb2;;2;;Toggle Left Arc;2;;;2;;;;;true;-533;0;,Arc_2e_CIS_Large_Left.svg;,;true;Show Left Arc;;;false;;1;1;true;;71,130;")

            //Right Arc
            .put("small/rebelalliance/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;476;0;,Arc_2e_Rebel_Small_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("small/galacticempire/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;476;0;,Arc_2e_Empire_Small_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("small/scumandvillainy/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;476;0;,Arc_2e_Scum_Small_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("small/resistance/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;476;0;,Arc_2e_Resistance_Small_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("small/firstorder/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;476;0;,Arc_2e_FirstOrder_Small_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("small/galacticrepublic/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;476;0;,Arc_2e_Republic_Small_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("small/separatistalliance/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;476;0;,Arc_2e_CIS_Small_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")

            .put("medium/rebelalliance/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;505;0;,Arc_2e_Rebel_Medium_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("medium/galacticempire/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;505;0;,Arc_2e_Empire_Medium_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("medium/scumandvillainy/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;505;0;,Arc_2e_Scum_Medium_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("medium/resistance/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;505;0;,Arc_2e_Resistance_Medium_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("medium/firstorder/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;505;0;,Arc_2e_FirstOrder_Medium_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("medium/galacticrepublic/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;505;0;,Arc_2e_Republic_Medium_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("medium/separatistalliance/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;505;0;,Arc_2e_CIS_Medium_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")

            .put("large/rebelalliance/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;533;0;,Arc_2e_Rebel_Large_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("large/galacticempire/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;533;0;,Arc_2e_Empire_Large_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("large/scumandvillainy/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;533;0;,Arc_2e_Scum_Large_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("large/resistance/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;533;0;,Arc_2e_Resistance_Large_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("large/firstorder/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;533;0;,Arc_2e_FirstOrder_Large_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("large/galacticrepublic/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;533;0;,Arc_2e_Republic_Large_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
            .put("large/separatistalliance/Right Arc","emb2;;2;;Toggle Right Arc;2;;;2;;;;;true;533;0;,Arc_2e_CIS_Large_Right.svg;,;true;Show Right Arc;;;false;;1;1;true;;71,520;")
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
        String dualBaseToggleMenuText;
        VassalXWSPilotPieces2e source;

        ShipGenerateCommand(VassalXWSPilotPieces2e source, GamePiece piece) {
        //ShipGenerateCommand(String shipXws,   GamePiece piece, String faction, String xwsPilot) {

            // fetch the maneuver array of arrays according to the xws name passed on from autospawn or other means
            xwsShipName = mic.Canonicalizer.getCleanedName(source.getShipData().getName());
            this.faction = mic.Canonicalizer.getCleanedName(source.getShipData().getFaction());
            shipName = source.getShipData().getName();
            this.piece = piece;
            this.xwsPilot = source.getPilotData().getXWS();
            this.size = source.getShipData().getSize();

            //TODO later make it reliable to detect bomb capability, possibly parsing upgrade cards?
            this.needsBombCapability = true;
            this.source = source;
        }

        protected void executeCommand()
        {
            // find the appropriate baseImage
            piece = buildShipBaseLayer(piece);

            // Add the Target Lock capability
           piece = addTargetLock(piece,faction,size);

            // add the firing arcs needed
            piece = addFiringArcs(piece);


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
           /* while(i.hasNext())
            {
                arc = i.next().getArc();
                if(arc.equals("Rear Arc")) {
                    emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Show Auxiliary Firing Arc");
                } else if(arc.equals("Full Front Arc")) {
                    emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Show Full Front Arc");
                } else continue;

                    arcKey = mic.Canonicalizer.getCleanedName(source.getShipData().getSize()) + "/" + mic.Canonicalizer.getCleanedName(source.getPilotData().getFaction()) + "/" + arc;

                    newType = firingArcTypes.get(arcKey);

                    if(newType != null && !newType.isEmpty())
                    {
                        emb.mySetType(newType);
                    }
                    newType = null;

            }*/
            //add rear arc for all
            emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Show Auxiliary Firing Arc");
            arc = "Rear Arc";
            arcKey = mic.Canonicalizer.getCleanedName(source.getShipData().getSize()) + "/" + mic.Canonicalizer.getCleanedName(source.getPilotData().getFaction()) + "/" + arc;
            newType = firingArcTypes.get(arcKey);
            if(newType != null && !newType.isEmpty())
            {
                emb.mySetType(newType);
            }

            //add full front arc
            emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Show Full Front Arc");
            arc = "Full Front Arc";
            arcKey = mic.Canonicalizer.getCleanedName(source.getShipData().getSize()) + "/" + mic.Canonicalizer.getCleanedName(source.getPilotData().getFaction()) + "/" + arc;
            newType = firingArcTypes.get(arcKey);
            if(newType != null && !newType.isEmpty())
            {
                emb.mySetType(newType);
            }

            //add full back arc
            emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Show Full Back Arc");
            arc = "Full Back Arc";
            arcKey = mic.Canonicalizer.getCleanedName(source.getShipData().getSize()) + "/" + mic.Canonicalizer.getCleanedName(source.getPilotData().getFaction()) + "/" + arc;
            newType = firingArcTypes.get(arcKey);
            if(newType != null && !newType.isEmpty())
            {
                emb.mySetType(newType);
            }

            //add left arc
            emb = (Embellishment) Util.getEmbellishment(newGamePiece, "Layer - Show Left Arc");
            arc = "Left Arc";
            arcKey = mic.Canonicalizer.getCleanedName(source.getShipData().getSize()) + "/" + mic.Canonicalizer.getCleanedName(source.getPilotData().getFaction()) + "/" + arc;
            newType = firingArcTypes.get(arcKey);
            if(newType != null && !newType.isEmpty())
            {
                emb.mySetType(newType);
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
            arcKey = mic.Canonicalizer.getCleanedName(source.getShipData().getSize()) + "/"
                    + mic.Canonicalizer.getCleanedName(source.getPilotData().getFaction()) + "/" + arc;
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

            Embellishment myEmb = (Embellishment)Util.getEmbellishment(newGamePiece,targetLockLayerName);

            newSize = mic.Canonicalizer.getCleanedName(newSize);

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
            }else if(newSize.equals("small") && faction.equals("separatistalliance") )
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
            }else if(newSize.equals("medium") && faction.equals("separatistalliance") )
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
            }else if(newSize.equals("large") && faction.equals("separatistalliance") )
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
/*
        private GamePiece addBombCapability(GamePiece piece, String size)
        {
            String normalSmallBombSpanwerType = "placemark;;;VASSAL.build.module.PieceWindow\\/VASSAL.build.widget.TabWidget\\/VASSAL.build.widget.TabWidget:Chits\\/VASSAL.build.widget.ListWidget:Bombs\\/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;-338;true;;Place Bomb Spawner;12875;0;false\\\\\\\\\\\\\\\\\\";
            String normalMediumBombSpawnerType = "placemark;;;VASSAL.build.module.PieceWindow\\/VASSAL.build.widget.TabWidget\\/VASSAL.build.widget.TabWidget:Chits\\/VASSAL.build.widget.ListWidget:Bombs\\/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;-338;true;;Place Bomb Spawner;12885;0;false\\\\\\\\\\\\\\\\\\";
            String normalLargeBombSpawnerType = "placemark;;;VASSAL.build.module.PieceWindow\\/VASSAL.build.widget.TabWidget\\/VASSAL.build.widget.TabWidget:Chits\\/VASSAL.build.widget.ListWidget:Bombs\\/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;-396;true;;Place Bomb Spawner;12417;0;false\\\\\\\\\\\\\\\\\\\\\\";
            String frontalSmallBombSpawnerType = "placemark;;;VASSAL.build.module.PieceWindow\\/VASSAL.build.widget.TabWidget\\/VASSAL.build.widget.TabWidget:Chits\\/VASSAL.build.widget.ListWidget:Bombs\\/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;339;true;63743,0,rotate180;Place Frontal Bomb Spawner;12874;0;false\\\\\\\\\\\\\\\\";
            String frontalMediumBombSpawnerType = "placemark;;;VASSAL.build.module.PieceWindow\\/VASSAL.build.widget.TabWidget\\/VASSAL.build.widget.TabWidget:Chits\\/VASSAL.build.widget.ListWidget:Bombs\\/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;339;true;63743,0,rotate180;Place Frontal Bomb Spawner;12884;0;false\\\\\\\\\\\\\\\\";
            String frontalLargeBombSpawnerType = "placemark;;;VASSAL.build.module.PieceWindow\\/VASSAL.build.widget.TabWidget\\/VASSAL.build.widget.TabWidget:Chits\\/VASSAL.build.widget.ListWidget:Bombs\\/VASSAL.build.widget.PieceSlot:Bomb Spawner;null;0;339;true;63743,0,rotate180;Place Frontal Bomb Spawner;12887;0;false\\\\\\\\\\\\\\\\";

            // get the Embellishments

            PlaceMarker normalPlaceMarker = (PlaceMarker)Util.getPlaceMarkerTrait(piece,"Place Marker - Place Bomb Spawner");
            PlaceMarker frontalPlaceMarker = (PlaceMarker)Util.getPlaceMarkerTrait(piece,"Place Marker - Place Frontal Bomb Spawner");

            // inject the type into the
            if(Canonicalizer.getCleanedName(size).equals("small"))
            {
                normalPlaceMarker.mySetType(normalSmallBombSpanwerType);
                frontalPlaceMarker.mySetType(frontalSmallBombSpawnerType);

            }
            else if(Canonicalizer.getCleanedName(size).equals("medium"))
            {
                normalPlaceMarker.mySetType(normalMediumBombSpawnerType);
                frontalPlaceMarker.mySetType(frontalMediumBombSpawnerType);

            }
            else if(Canonicalizer.getCleanedName(size).equals("large"))
            {
                normalPlaceMarker.mySetType(normalLargeBombSpawnerType);
                frontalPlaceMarker.mySetType(frontalLargeBombSpawnerType);
            }

            return piece;

        }
*/

        private GamePiece buildShipBaseLayer(GamePiece piece)
        {
            //  overwrite the layer with a new state
            if(!source.getShipData().hasDualBase()) {
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
                // this is a dual based ship
                StringBuffer sb = new StringBuffer();
                sb.append(SHIP_BASE_IMAGE_PREFIX);
                sb.append("_");
                sb.append(mic.Canonicalizer.getCleanedName(faction));
                sb.append("_");
                sb.append(xwsShipName);
                sb.append("_");

                String baseImage1 = sb.toString() + source.getShipData().getBaseImage1Identifier()+".png";
                String baseImage2 = sb.toString() + source.getShipData().getBaseImage2Identifier()+".png";

                // build image name from identifier
                sb = new StringBuffer();
                //emb2;Activate;2;;;2;;;2;;;;1;false;0;0;Ship_generic_large.png,Ship_Big_SeeThrough.png,Ship_generic_large.png,Ship_Big_SeeThrough.png;base1,ghost1,base2,ghost2;false;Base Ship;;;true;ULevel;1;1;true;65,130;;
                sb.append("emb2;Activate;2;;;2;;;2;;;;1;false;0;0;");
                sb.append(baseImage1);
                if(size.equals("Small")) {
                    sb.append(",Ship_Small_SeeThrough.png,");

                }else if(size.equals("Medium")) {
                    sb.append(",Ship_Medium_SeeThrough.png,");
                }
                else if(size.equals("Large")) {
                    sb.append(",Ship_Big_SeeThrough.png,");
                }
                sb.append(baseImage2);
                if(size.equals("Small")) {
                    sb.append(",Ship_Small_SeeThrough.png");

                }else if(size.equals("Medium")) {
                    sb.append(",Ship_Medium_SeeThrough.png,");
                }
                else if(size.equals("Large")) {
                    sb.append(",Ship_Big_SeeThrough.png");
                }
              //  sb.append(";base1,ghost1,base2,ghost2;false;Base Ship;;;true;ULevel;1;1;true;65,130;;");

                sb.append(";");
                sb.append(source.getShipData().getBaseReport1Identifier());
                sb.append(",ghost1,");
                sb.append(source.getShipData().getBaseReport2Identifier());
                sb.append(",ghost2;false;Base Ship;;;true;ULevel;1;1;true;65,130;;");
                Embellishment myEmb = (Embellishment)Util.getEmbellishment(piece,BASE_SHIP_LAYER_NAME);

                myEmb.mySetType(sb.toString());

                // now set the trigger action

                sb = new StringBuffer();
                sb.append("macro;Toggle Ship Base;");
                sb.append(source.getShipData().getDualBaseToggleMenuText());

                //total hack to make x-wing sfoils work with pivotatk instead of pivot like is used in U-Wings
                String pivotTypeString = "dopivot";
                if(size.equals("small") && "standard".equals(source.getShipData().getBaseReport1Identifier()))  pivotTypeString ="dopivotatk";

                sb.append(";85,520;{ULevel==1 || ULevel==3};;63743\\,0\\,"+pivotTypeString+";false;;;counted;;;;false;;1;1");
                TriggerAction trig = (TriggerAction)Util.getTriggerAction(piece,TOGGLE_BASE_TRIGGER_ACTION_NAME);

                if(trig!=null) trig.mySetType(sb.toString());
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
            String shipImageSuffix = "";
            OTAMasterShips data = Util.loadRemoteJson(OTAContentsChecker.OTA_SHIPS_JSON_URL_2E, OTAMasterShips.class);
            /*
            Util.logToChat("stempship2e line 553 data is " + (data==null?"null":"not null"));
            Util.logToChat("stempship2e line 554 getLoadedData is " + (data.getLoadedData()==null?"null":"not null"));
            Util.logToChat("stempship2e line 554 entrySet is " + (data.getLoadedData().entrySet()==null?"null":"not null"));
            */
            for(Map.Entry<String, OTAMasterShips.OTAShip> entry : data.getLoadedData(2).entrySet()){
                if(entry.getValue().getXws().equals(xwsShipName) && entry.getValue().getIdentifier().equals(xwsPilot))
                {
                    shipImageSuffix = "_" + entry.getValue().getIdentifier();
                }
            }

            //shipxws+"_"+identifier

            // if there's a blank string in shipImage[0], then it's a standard art
            // if there's a string in shipImage[1], then it's a dual base ship
            // otherwise, use the shipImage[0]
            if(shipImageSuffix == null || shipImageSuffix.equals(""))
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
                sb.append(shipImageSuffix);
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
                sb.append("Ship2e_wip_");
                sb.append(mic.Canonicalizer.getCleanedName(size));
                sb.append(".png");

                shipArt = sb.toString();
              //  shipArt[1] = "";

            }


            return shipArt;
        }


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


