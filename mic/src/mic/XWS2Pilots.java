package mic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import mic.ota.OTAContentsChecker;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XWS2Pilots {
    public static String remoteUrl = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/data/manifest.json";
    //public static String remoteUrl = "https://raw.githubusercontent.com/Mu0nHub/xwing-data2/master/data/manifest.json";
    public static String guidoRootUrl = "https://raw.githubusercontent.com/guidokessels/xwing-data2/master/";
    //public static String guidoRootUrl = "https://raw.githubusercontent.com/Mu0nHub/xwing-data2/master/";


    @JsonProperty("name")
    private String name;

    @JsonProperty("dial")
    private List<String> dial = Lists.newArrayList();

    @JsonProperty("size")
    private String size;

    @JsonProperty("faction")
    private String faction;

    @JsonProperty("stats")
    private List<Stat2e> stats = Lists.newArrayList();

    @JsonProperty("pilots")
    private List<Pilot2e> pilots = Lists.newArrayList();

    @JsonProperty("actions")
    private List<PilotAction> actions = Lists.newArrayList();

    @JsonProperty("xws")
    private String xws;
    @JsonProperty("has_dual_base")
    private Boolean hasDualBase = false;
    @JsonProperty("dual_base_toggle_menu_text")
    private String dualBaseToggleMenuText;
    @JsonProperty("dual_base_image_1_identifier")
    private String baseImage1Identifier;
    @JsonProperty("dual_base_image_2_identifier")
    private String baseImage2Identifier;
    @JsonProperty("dual_base_report_1_identifier")
    private String baseReport1Identifier;
    @JsonProperty("dual_base_report_2_identifier")
    private String baseReport2Identifier;

    public String getShipXWS() { return this.xws; }
    public Boolean hasDualBase() { return this.hasDualBase; }
    private void setDualBase(Boolean hasDualBase)
    {
        this.hasDualBase = hasDualBase;
    }

    public String getDualBaseToggleMenuText()
    {
        return this.dualBaseToggleMenuText;
    }
    private void setDualBaseToggleMenuText(String dualBaseToggleMenuText){ this.dualBaseToggleMenuText = dualBaseToggleMenuText; }

    public String getBaseImage1Identifier()
    {
        return baseImage1Identifier;
    }
    public void setBaseImage1(String baseImage1Identifier)
    {
        this.baseImage1Identifier = baseImage1Identifier;
    }
    public String getBaseImage2Identifier()
    {
        return baseImage2Identifier;
    }
    public void setBaseImage2(String baseImage2Identifier)
    {
        this.baseImage2Identifier = baseImage2Identifier;
    }

    public String getBaseReport1Identifier()
    {
        return baseReport1Identifier;
    }
    public void setBaseReport1Identifier(String baseReport1Identifier){ this.baseReport1Identifier = baseReport1Identifier; }
    public String getBaseReport2Identifier()
    {
        return baseReport2Identifier;
    }
    public void setBaseReport2Identifier(String baseReport2Identifier){ this.baseReport2Identifier = baseReport2Identifier; }


    public String getName() {return this.name;}
    public String getCleanedName() { return Canonicalizer.getCleanedName(this.name); }
    public List<String> getDial() {return this.dial;}
    public String getSize() { return this.size;}
    public String getFaction() {return this.faction;}
    public List<Stat2e> getStats() {return this.stats;}
    public List<Pilot2e> getPilots() { return this.pilots;}
    public List<PilotAction> getActions() { return this.actions; }

    public int getInitiative(){
        for(Stat2e stat : stats)
        {
            if(stat.getType().equals("initiative")) return stat.getValue();
        }
        return 0;
    }
    public int getHull(){
        for(Stat2e stat : stats)
        {
            if(stat.getType().equals("hull")) return stat.getValue();
        }
        return 0;
    }
    public int getAgility(){
        for(Stat2e stat : stats)
        {
            if(stat.getType().equals("agility")) return stat.getValue();
        }
        return 0;
    }
    public int getShields(){
        for(Stat2e stat : stats)
        {
            if(stat.getType().equals("shields")) return stat.getValue();
        }
        return 0;
    }
    //charge impossible on ships?
    public int getCharge(){
        for(Stat2e stat : stats)
        {
            if(stat.getType().equals("charge")) return stat.getValue();
        }
        return 0;
    }
    //force impossible on ships?
    public int getForce(){
        for(Stat2e stat : stats)
        {
            if(stat.getType().equals("force")) return stat.getValue();
        }
        return 0;
    }
    public int getFrontArc(){
        for(Stat2e stat : stats)
        {
            if(stat.getArc().equals("Front Arc") && stat.getType().equals("attack")) return stat.getValue();
        }
        return 0;
    }
    public int getRearArc(){
        for(Stat2e stat : stats)
        {
            if(stat.getArc().equals("Rear Arc") && stat.getType().equals("attack")) return stat.getValue();
        }
        return 0;
    }
    public boolean hasSmallBase() {
        if(size.equals("small") || size.equals("Small")) return true;
        return false;
    }

    public boolean containsPilot(String pilotName){
        for(Pilot2e pilot : getPilots())
        {
            Util.logToChat("scanning pilot " + pilot.getName());
            if(pilot.getName().equals(pilotName)) return true;
        }
        return false;
    }

    public boolean containsCleanedPilot(String cleanedPilotName){
        for(Pilot2e pilot : getPilots())
        {
            Util.logToChat("scanning pilot " + pilot.getName());
            if(Canonicalizer.getCleanedName(pilot.getName()).equals(cleanedPilotName)) return true;
        }
        return false;
    }



    public static class PilotAction{
        public PilotAction() { super();}

        @JsonProperty("difficulty")
        private String difficulty = "";

        @JsonProperty("type")
        private String type = "";


        public String getDifficulty() { return difficulty;}
        public String getType() { return type; }
    }
    public static class Stat2e{
        public Stat2e() { super(); }

        public Stat2e(String type, String arc, int value){
            this.type = type;
            this.arc = arc;
            this.value = value;
        }

        public Stat2e(String type, int value){
            this.type = type;
            this.value = value;
        }

        @JsonProperty("type")
        private String type;

        @JsonProperty("arc")
        private String arc = "";

        @JsonProperty("value")
        private int value;

        public String getType(){
            return this.type;
        }

        public String getArc(){
            return this.arc;
        }

        public int getValue(){
            return this.value;
        }


    }

    public static class Pilot2e{
        public Pilot2e() { super(); }

        public Pilot2e(String name, String caption, int initiative, int limited, String ability){
            this.name = name;
            this.caption = caption;
            this.initiative = initiative;
            this.limited = limited;
            this.ability = ability;
        }

        private String faction;

        @JsonProperty("name")
        private String name="unknown";

        @JsonProperty("caption")
        private String caption;

        @JsonProperty("initiative")
        private int initiative;

        @JsonProperty("limited")
        private int limited;

        @JsonProperty("ability")
        private String ability;

        @JsonProperty("force")
        private ForceData forceData = new ForceData(0,0);

        @JsonProperty("charges")
        private ChargeData chargeData = new ChargeData(0,0);

        @JsonProperty("xws")
        private String xws = "";

        @JsonProperty("conditions")
        private List<String> conditions = Lists.newArrayList();

        @JsonProperty("shipActions")
        private List<PilotAction> shipActions = Lists.newArrayList();

        @JsonProperty("shipAbility")
        private ShipAbility shipAbility = new ShipAbility();

        @JsonProperty("image")
        private String image;

        public String getImage() { return image; }

        public String getName(){return this.name;}
        public String getCaption() {return this.caption;}
        public int getInitiative() { return this.initiative;}
        public int getLimited() { return this.limited;}
        public String getAbility() { return this.ability; }
        public String getXWS() { return this.xws; }
        public List<String> getConditions() { return this.conditions; }
        public List<PilotAction> getShipActions() { return this.shipActions; }
        public ShipAbility getShipAbility() { return this.shipAbility; }
        public ForceData getForceData() { return this.forceData;}
        public ChargeData getChargeData() { return this.chargeData;}

        //not part of the JSON, but useful
        public String getFaction(){
            return faction;
        }
        public void setFaction(String wantedFaction){
            this.faction = wantedFaction;
        }

        public boolean isUnique() { if(limited>0) return true;
        return false;}
    }

    public static class ChargeData{
        public ChargeData() { super(); }
        public ChargeData(int value, int recovers)
        {
            this.value = value;
            this.recovers = recovers;
        }
        @JsonProperty("value")
        private int value;

        @JsonProperty("recovers")
        private int recovers;

        public int getValue(){ return value; }
        public int getRecovers() { return recovers; }
    }
    public static class ForceData{
        public ForceData() {super();}
        public ForceData(int value, int recovers) {
            this.value = value;
            this.recovers = recovers;
        }

        @JsonProperty("value")
        private int value;

        @JsonProperty("recovers")
        private int recovers;

        public int getValue(){ return value; }
        public int getRecovers() { return recovers; }
    }

    public static class ShipAbility{
        public ShipAbility() {super(); }
        public ShipAbility(String name, String text){
            this.name = name;
            this.text = text;
        }

        @JsonProperty("name")
        private String name;

        @JsonProperty("text")
        private String text;

        public String getName(){ return this.name;}
        public String getText(){ return this.text; }
    }

    public static class pilotsDataSources{
        public pilotsDataSources() { super(); }
        public pilotsDataSources(List<OneFactionGroup> factionPilots){
            this.factionPilots = factionPilots;
        }
        @JsonProperty("version")
        private String version;

        @JsonProperty("pilots")
        List<OneFactionGroup> factionPilots = Lists.newArrayList();

        public List<OneFactionGroup> getPilots(){return this.factionPilots;}
        public String getVersion(){return this.version;}
        public tripleVersion getTripleVersion(){
            if(this.version.contains(".")){
                String[] parts = this.version.split("\\.");
                int length = parts.length;
                if(length==3)
                {
                    tripleVersion myTV = new tripleVersion(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                    return myTV;
                }
                return null;
            }
            else{
                Util.logToChat("ERROR: the manifest.json didn't have a well formatted version number.");
                return null;
            }
        }
    }

    public static class OneFactionGroup{
        public OneFactionGroup() { super(); }
        public OneFactionGroup(String faction, List<String> suffixes){
            this.faction = faction;
            this.suffixes = suffixes;
        }

        @JsonProperty("faction")
        private String faction;

        @JsonProperty("ships")
        private List<String> suffixes = Lists.newArrayList();

        public String getFaction() { return faction; }
        public List<String> getShipUrlSuffixes() { return suffixes; }
    }

    public static XWS2Pilots ShipXWSFoundInDualBaseList(List<XWS2Pilots> theList, String xwsCheck){
        for(XWS2Pilots ship : theList){
            if(ship.getShipXWS().equals(xwsCheck)) return ship;
        }
        return null;
    }

    public static class tripleVersion{
        private int major=0;
        private int minor=0;
        private int patch=0;

        public tripleVersion(int major, int minor, int patch){
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        public int getMajor(){ return this.major; }
        public int getMinor(){ return this.minor; }
        public int getPatch(){ return this.patch; }

        public void setMajor(int n){ this.major = n;}
        public void setMinor(int n){ this.minor = n;}
        public void setPatch(int n){ this.patch = n;}

        public boolean isNewerThan(tripleVersion toCompare){
            if(this.getMajor() > toCompare.getMajor()) return true;
            else if(this.getMajor() < toCompare.getMajor()) return false;

            if(this.getMinor() > toCompare.getMinor()) return true;
            else if(this.getMinor() < toCompare.getMinor()) return false;

            if(this.getPatch() > toCompare.getPatch()) return true;
            else if(this.getPatch() < toCompare.getPatch()) return false;

            //both versions are exactly identical
            return false;
        }

        public void displayInChat(String source){
            Util.logToChat("The " + source + " version of xwing-data2 is " + Integer.toString(major)+"."+Integer.toString(minor)+"."+Integer.toString(patch));
        }
    }

    public static tripleVersion checkRemoteManifestVersion(){
        pilotsDataSources whereToGetPilots = Util.loadRemoteJson(remoteUrl, pilotsDataSources.class);
        return whereToGetPilots.getTripleVersion();
    }

    public static tripleVersion checkLocalManifestVersion(){
        pilotsDataSources whereToGetPilots = Util.loadClasspathJson("images/manifest.json", pilotsDataSources.class);
        return whereToGetPilots.getTripleVersion();
    }

    public static List<XWS2Pilots> loadFromRemote() {
        pilotsDataSources whereToGetPilots = Util.loadRemoteJson(remoteUrl, pilotsDataSources.class);
        List<XWS2Pilots> dualBaseXWS2Ships = Lists.newArrayList();
        try {
            dualBaseXWS2Ships = loadRemoteJsonArrayOfXWS2Pilots(new URL(OTAContentsChecker.OTA_DISPATCHER_SHIPS_JSON_URL_2E));
        } catch (Exception e) {
        }

    List<XWS2Pilots> allPilots = Lists.newArrayList();
        for(OneFactionGroup oSDS : whereToGetPilots.getPilots()){
            for(String suffix : oSDS.getShipUrlSuffixes())
            {
                try {
                    //Dual Base detection
                    //dualBaseXWSShips is an extra XWS2Pilot list that's loaded using a subset of JsonProperties right here, against a dispatcher_ships.json
                    //if both dualBaseXWSShip's xws and the cleaned getName of pilottoAdd matches, then copy over the dual based information
                    XWS2Pilots pilotsToAdd = Util.loadRemoteJson(guidoRootUrl + suffix, XWS2Pilots.class);
                    XWS2Pilots dualBasedInfoFound = ShipXWSFoundInDualBaseList(dualBaseXWS2Ships, Canonicalizer.getCleanedName(pilotsToAdd.getName()));
                    if(dualBasedInfoFound !=null ) {
                        pilotsToAdd.setBaseImage1(dualBasedInfoFound.getBaseImage1Identifier());
                        pilotsToAdd.setBaseImage2(dualBasedInfoFound.getBaseImage2Identifier());
                        pilotsToAdd.setBaseReport1Identifier(dualBasedInfoFound.getBaseReport1Identifier());
                        pilotsToAdd.setBaseReport2Identifier(dualBasedInfoFound.getBaseReport2Identifier());
                        pilotsToAdd.setDualBase(dualBasedInfoFound.hasDualBase());
                        pilotsToAdd.setDualBaseToggleMenuText(dualBasedInfoFound.getDualBaseToggleMenuText());
                    }
                    //adding faction to every pilot
                    for(XWS2Pilots.Pilot2e p : pilotsToAdd.getPilots()){
                     p.setFaction(pilotsToAdd.getFaction());
                    }

                    allPilots.add(pilotsToAdd);
                }catch (Exception e){
                    Util.logToChat(e.getMessage() + " on ship " + suffix);
                    continue;
                }
            }

        }
        return allPilots;
    }
    public static List<XWS2Pilots> loadRemoteJsonArrayOfXWS2Pilots(URL url) {
        try {
            InputStream inputStream = new BufferedInputStream(url.openStream());
            List<XWS2Pilots> rawData = mapper.readValue(inputStream,  mapper.getTypeFactory().constructCollectionType(List.class, XWS2Pilots.class));
            return rawData;
        } catch (Exception e) {
            System.out.println("Unhandled error parsing remote json: \n" + e.toString());
            return null;
        }
    }

    private static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static XWS2Pilots getSpecificShipFromShipXWS(String  searchedXWS2Name, List<XWS2Pilots> allShips) {
        for(XWS2Pilots aShip : allShips){
            if(Canonicalizer.getCleanedName(aShip.getName()).equals(searchedXWS2Name)){
                return aShip;
            }
        }
        return null;
    }


    public static XWS2Pilots getSpecificShipFromPilotXWS2(String searchedXWS2Name, List<XWS2Pilots> allShips){
        for(XWS2Pilots aShip : allShips)
        {
            for(XWS2Pilots.Pilot2e aPilot : aShip.getPilots())
            {
                String foundXWS2="";
                try{
                    foundXWS2 =  aPilot.getXWS();
                }catch(Exception e){}

                if(foundXWS2.equals(searchedXWS2Name)) {
                    return aShip;
                }
            }
        }
        return null;
    }

    public static XWS2Pilots.Pilot2e getSpecificPilot(String searchedXWS2Name, List<XWS2Pilots> allShips){
        for(XWS2Pilots aShip : allShips)
        {
            for(XWS2Pilots.Pilot2e aPilot : aShip.getPilots())
            {
                String theXWS2String = aPilot.getXWS();
                if(theXWS2String.equals(searchedXWS2Name)) return aPilot;
            }
        }
        return null;
    }
}
