package mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mjuneau on 7/8/18.
 */


    public class TokensFromWeb extends ArrayList<TokensFromWeb.Token>
    {

    private static String BUILDERS_REFERENCE_LIST = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/master/builders.json";


    private static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        private static String TOKENREQUIREMENTSURL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/master/json/token_requirements.json";
        public TokensFromWeb(){ super(); }

        private List<Token> tokenRequirementsList = null;
        public void loadData() {
            if(tokenRequirementsList == null){
                try{
                    InputStream inputStream = new BufferedInputStream((new URL(TOKENREQUIREMENTSURL)).openStream());
                    this.tokenRequirementsList = mapper.readValue(inputStream, mapper.getTypeFactory().constructCollectionType(List.class, TokensFromWeb.Token.class));

                }catch(Exception e){
                    System.out.println("Unhandled error parsing remote json: \n" + e.toString());
                }
            }
        }
        public List<Token> getTokenData() { return tokenRequirementsList; }


        public static class Token{
            public Token() { super(); }

            @JsonProperty("name")
            private String name;

            @JsonProperty("actions")
            private List<String> actions;

            @JsonProperty("upgrades")
            private List<String> upgrades;

            @JsonProperty("pilotsOrShips")
            private List<String> pilotsOrShips;

            public String getName() { return this.name; }
            public List<String> getActions() { return this.actions; }
            public List<String> getUpgrades() { return this.upgrades; }
            public List<String> getPilotsOrShips() { return this.pilotsOrShips; }
        }




    public static List<String> loadForPilot(VassalXWSPilotPieces2e pilot) {


        List<String> tokens = Lists.newArrayList();

        TokensFromWeb tokensFromWeb = new TokensFromWeb();
        tokensFromWeb.loadData();


        //for (Tokens2e token : values()) {
        for (TokensFromWeb.Token aTokenFromWeb : tokensFromWeb.getTokenData()) {
            if (pilot.getShipData() != null) {
                boolean passThroughShipActions = false;
                if(pilot.getPilotData().getShipActions().size() > 0)
                    for(XWS2Pilots.PilotAction shipAction : pilot.getPilotData().getShipActions()){
                        if (aTokenFromWeb.getActions().contains(shipAction.getType())) {
                            tokens.add(aTokenFromWeb.getName());
                        }
                        passThroughShipActions = true;
                }
                if(passThroughShipActions == false)
                    for (XWS2Pilots.PilotAction action : pilot.getShipData().getActions()) {
                    if (aTokenFromWeb.getActions().contains(action.getType())) {
                        tokens.add(aTokenFromWeb.getName());
                    }
                }

                if (pilot.getPilotData() != null) {
                    String shipPilot = pilot.getPilotData().getXWS();
                    String ship = pilot.getShipData().getCleanedName();
                    if (aTokenFromWeb.getPilotsOrShips().contains(shipPilot) || aTokenFromWeb.getPilotsOrShips().contains(ship)) {
                        tokens.add(aTokenFromWeb.getName());
                    }
                }

            }

            for (VassalXWSPilotPieces2e.Upgrade upgrade : pilot.getUpgrades()) {
                if (aTokenFromWeb.getUpgrades().contains(upgrade.getXwsName())) {
                    tokens.add(aTokenFromWeb.getName());
                }
            }
        }
        return tokens;
    }
}
