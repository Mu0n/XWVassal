
package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mic.Util.*;

/**
 * Created by Mic on 23/03/2017.
 */

enum ImagesUsedForRanges {
    ProbeDroid("Probe-range1","Probe-range2","Probe-range", 0, -50),
    GasCloud1("Gascloud1_r1","Gascloud1_r2","Gascloud1_TL", 0, 0),
    GasCloud2("Gascloud2_r1","Gascloud2_r2","Gascloud2_TL", 0, 0),
    GasCloud3("Gascloud3_r1","Gascloud3_r2","Gascloud3_TL", 0, 0),
    GasCloud4("Gascloud4_r1","Gascloud4_r2","Gascloud4_TL", 0, 0),
    GasCloud5("Gascloud5_r1","Gascloud5_r2","Gascloud5_TL", 0, 0),
    GasCloud6("Gascloud6_r1","Gascloud6_r2","Gascloud6_TL", 0, 0),
    BuzzDroid("","","", 0, 0),
    Asteroid1("Asteroid1_r1","Asteroid1_r2","Asteroid1_TL", 0, 0),
    Asteroid2("Asteroid2_r1","Asteroid2_r2","Asteroid2_TL", 0, 0),
    Asteroid3("Asteroid3_r1","Asteroid3_r2","Asteroid3_TL", 0, 0),
    Asteroid4("Asteroid4_r1","Asteroid4_r2","Asteroid4_TL", 0, 0),
    Asteroid5("Asteroid5_r1","Asteroid5_r2","Asteroid5_TL", 0, 0),
    Asteroid6("Asteroid6_r1","Asteroid6_r2","Asteroid6_TL", 0, 0),
    Asteroid7("Asteroid_7b_r1","Asteroid_7b_r2","Asteroid_7b_range", -5, -3),
    Asteroid8("Asteroid_8b_r1","Asteroid_8b_r2","Asteroid_8b_range", -5, -3),
    Asteroid9("Asteroid_9b_r1","Asteroid_9b_r2","Asteroid_9b_range", 3, 0),
    Asteroid10("Asteroid_10b_r1","Asteroid_10b_r2","Asteroid_10b_range", -2, -3),
    Asteroid11("Asteroid_11b_r1","Asteroid_11b_r2","Asteroid_11b_range", -5, -3),
    Asteroid12("Asteroid_12b_r1","Asteroid_12b_r2","Asteroid_12b_range", -5, -3),
    Pomasteroid1("pomasteroid1_r1","pomasteroid1_r2","pomasteroid1_TL", -7, -4),
    Pomasteroid2("pomasteroid2_r1","pomasteroid2_r2","pomasteroid2_TL", -4, -3),
    Pomasteroid3("pomasteroid3_r1","pomasteroid3_r2","pomasteroid3_TL", -6, -4),
    Container("","","", 0, 0),
    Debris1("Debris1_r1","Debris1_r2","Debris1_TL", -5, -3),
    Debris2("Debris2_r1","Debris2_r2","Debris2_TL", -5, -3),
    Debris3("Debris3_r1","Debris3_r2","Debris3_TL", 3, 0),
    Debris4("Debris4_r1","Debris4_r2","Debris4_TL", -2, -3),
    Debris5("Debris5_r1","Debris5_r2","Debris5_TL", -5, -3),
    Debris6("Debris6_r1","Debris6_r2","Debris6_TL", -5, -3),
    Pomdebris1("pomdebris1_r1","pomdebris1_r2","pomdebris1_TL", 1, -1),
    Pomdebris2("pomdebris2_r1","pomdebris2_r2","pomdebris2_TL", -2, -5),
    Pomdebris3("pomdebris3_r1","pomdebris3_r2","pomdebris3_TL", -2, -3),
    Cargo("Cloud_Debris_range1","","", 0, 0),
    SpareParts("","","", 0, 0),
    SeismicCharge("Mine_Explosion","Mine_Explosion2","Mine_Explosion3",0 ,0),
    ProtonBomb("Mine_Explosion","Mine_Explosion2","Mine_Explosion3",0 ,0),
    ThermalDetonators("Mine_Explosion","Mine_Explosion2","Mine_Explosion3",0 ,0),
    IonBombs("Mine_Explosion","Mine_Explosion2","Mine_Explosion3",0 ,0),
    Bomblet("Mine_Explosion","Mine_Explosion2","Mine_Explosion3",0 ,0),
    ElectroProton("Mine_Explosion","Mine_Explosion2","Mine_Explosion3",0,0),
    Sensorbuoy("Buoy-range1","Buoy-range2","Buoy-range",0,0),
    ElectroCloudChaff("Electro-Chaff_Cloud_r1", "Electro-Chaff_Cloud_r2","Electro-Chaff_Cloud_r3",0,0);

    private final String r1Img;
    private final String r2Img;
    private final String r3Img;
    private final int offX;
    private final int offY;

    ImagesUsedForRanges(String r1, String r2, String r3, int ox, int oy){
        r1Img = r1;
        r2Img = r2;
        r3Img = r3;
        offX = ox;
        offY = oy;
    }

    public String getR1Img(){ return r1Img+".png"; }
    public String getR2Img(){ return r2Img+".png"; }
    public String getR3Img(){ return r3Img+".png"; }
    public int getOffX() { return offX; }
    public int getOffY() { return offY; }
}

public class AutoRangeForTokens extends Decorator implements EditablePiece {

    protected VASSAL.build.module.Map map;
    private static final int findObstaclesShips = 1;
    private static final int findShips = 2;
    private static final int findObstacles = 3;

    public MapVisualizations previousCollisionVisualization = null;

    private Shape shapeForRange1;
    private Shape shapeForRange2;
    private Shape shapeForRange3;

    String bigAnnounce = "";
    private final FreeRotator testRotator;
    private FreeRotator myRotator = null;

    int whichOption = -1; //which autorange option. See the Map above. -1 if none is selected.

    public static final String ID = "RangeForObstacles";

    private static Map<KeyStroke, Integer> keyStrokeToOptions = ImmutableMap.<KeyStroke, Integer>builder()
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_L,KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), findObstaclesShips) //find obstacles and ships
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), findShips) //find only ships
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O,KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK,false), findObstacles) //find only obstacles
            .build();
    private static Map<String, ImagesUsedForRanges> pieceNameToImgs = ImmutableMap.<String, ImagesUsedForRanges>builder()
            .put("Asteroid 1", ImagesUsedForRanges.Asteroid1)
            .put("Asteroid 2", ImagesUsedForRanges.Asteroid2)
            .put("Asteroid 3", ImagesUsedForRanges.Asteroid3)
            .put("Asteroid 4", ImagesUsedForRanges.Asteroid4)
            .put("Asteroid 5", ImagesUsedForRanges.Asteroid5)
            .put("Asteroid 6", ImagesUsedForRanges.Asteroid6)
            .put("Asteroid 7", ImagesUsedForRanges.Asteroid7)
            .put("Asteroid 8", ImagesUsedForRanges.Asteroid8)
            .put("Asteroid 9", ImagesUsedForRanges.Asteroid9)
            .put("Asteroid 10", ImagesUsedForRanges.Asteroid10)
            .put("Asteroid 11", ImagesUsedForRanges.Asteroid11)
            .put("Asteroid 12", ImagesUsedForRanges.Asteroid12)
            .put("Container", ImagesUsedForRanges.Container)
            .put("Debris 1", ImagesUsedForRanges.Debris1)
            .put("Debris 2", ImagesUsedForRanges.Debris2)
            .put("Debris 3", ImagesUsedForRanges.Debris3)
            .put("Debris 4", ImagesUsedForRanges.Debris4)
            .put("Debris 5", ImagesUsedForRanges.Debris5)
            .put("Debris 6", ImagesUsedForRanges.Debris6)
            .put("Cargo", ImagesUsedForRanges.Cargo)
            .put("Spare Parts", ImagesUsedForRanges.SpareParts)
            .put("DRK-1 Probe Droid", ImagesUsedForRanges.ProbeDroid)
            .put("Buzz Droid Swarm", ImagesUsedForRanges.BuzzDroid)
            .put("Gas Cloud 1", ImagesUsedForRanges.GasCloud1)
            .put("Gas Cloud 2", ImagesUsedForRanges.GasCloud2)
            .put("Gas Cloud 3", ImagesUsedForRanges.GasCloud3)
            .put("Gas Cloud 4", ImagesUsedForRanges.GasCloud4)
            .put("Gas Cloud 5", ImagesUsedForRanges.GasCloud5)
            .put("Gas Cloud 6", ImagesUsedForRanges.GasCloud6)
            .put("Seismic Charge", ImagesUsedForRanges.SeismicCharge)
            .put("Proton Bomb", ImagesUsedForRanges.ProtonBomb)
            .put("Thermal Detonators", ImagesUsedForRanges.ThermalDetonators)
            .put("Ion Bombs", ImagesUsedForRanges.IonBombs)
            .put("Bomblet", ImagesUsedForRanges.Bomblet)
            .put("Electro-Proton Bomb", ImagesUsedForRanges.ElectroProton)
            .put("Sensor Buoy (Red) Token",ImagesUsedForRanges.Sensorbuoy)
            .put("Sensor Buoy (Blue) Token",ImagesUsedForRanges.Sensorbuoy)
            .put("ElectroChaffCloud",ImagesUsedForRanges.ElectroCloudChaff)
            .build();

    public AutoRangeForTokens() {
        this(null);
    }
    public AutoRangeForTokens(GamePiece piece) {
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
        // launch();
        map = VASSAL.build.module.Map.getMapById("Map0");
    }

    public void mySetState(String newState) {

    }

    public String myGetState() {
        return "";
    }

    public String myGetType() {
        return ID;
    }

    private int getKeystrokeToOptions(KeyStroke keyStroke) {
        //OLD WAY
        // String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToOptions.containsKey(keyStroke)) {
            return keyStrokeToOptions.get(keyStroke).intValue();
        }
        return -1;
    }
    private String getAssociatedImage(String name, int range){
        if(pieceNameToImgs.containsKey(name)){
            switch(range){
                case 1:
                    return pieceNameToImgs.get(name).getR1Img();
                case 2:
                    return pieceNameToImgs.get(name).getR2Img();
                case 3:
                default:
                    return pieceNameToImgs.get(name).getR3Img();
            }
        }
        return "";
    }

    private int getOffX(String name){
        if(pieceNameToImgs.containsKey(name)) return pieceNameToImgs.get(name).getOffX();
        return 0;
    }

    private int getOffY(String name){
        if(pieceNameToImgs.containsKey(name)) return pieceNameToImgs.get(name).getOffY();
        return 0;
    }

    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }

    public Command myKeyEvent(KeyStroke stroke) {
        return null;
    }

    public Command keyEvent(KeyStroke stroke) {

        ArrayList<RangeFindings> rfindings = new ArrayList<RangeFindings>(); //findings compiled here

        // check to see if the this code needs to respond to the event
        //identify which autorange option was used by using the static Map defined above in the globals, store it in an int
        whichOption = getKeystrokeToOptions(stroke);

        //we have a valid keystroke option, let's deal with it
        if(whichOption != -1 && stroke.isOnKeyRelease()==false){
            verifyBasicShapes();
            prepAnnouncementStart();
            Shape shapeOfPieceItself = getRawShape(this);

            Shape tShapeR0 = getTransformedShape(shapeOfPieceItself);
            Shape tShapeR1 = getTransformedShape(shapeForRange1);
            Shape tShapeR2 = getTransformedShape(shapeForRange2);
            Shape tShapeR3 = getTransformedShape(shapeForRange3);

            //add the ships to the detection
            if(whichOption == findShips || whichOption == findObstaclesShips) {

                //Loop over every other target ship
                List<BumpableWithShape> BWS = getOtherShipsOnMap();
                for (BumpableWithShape b : BWS) {
                    //Range 0 check
                    if (shapesOverlap(tShapeR0, b.shape)) {
                        rfindings.add(new RangeFindings(0, b.pilotName));
                        continue;
                    }
                    //Range 1 check
                    if(shapeForRange1 !=null && shapesOverlap(tShapeR1, b.shape)){
                        rfindings.add(new RangeFindings(1, b.pilotName));
                        previousCollisionVisualization = new MapVisualizations();
                        previousCollisionVisualization.add(tShapeR1);
                        previousCollisionVisualization.execute();
                        continue;
                    }
                    //Range 2 check
                    if(shapeForRange2 !=null && shapesOverlap(tShapeR2,  b.shape)){
                        rfindings.add(new RangeFindings(2, b.pilotName));
                        continue;
                    }
                    //Range 3 check
                    if(shapeForRange3 !=null && shapesOverlap(tShapeR3, b.shape)){
                        rfindings.add(new RangeFindings(3, b.pilotName));
                    }
                }
            }

            //add the obstacles to the detection
            if(whichOption == findObstacles || whichOption == findObstaclesShips) {
                //Loop over every other target ship
                List<BumpableWithShape> BWS = getBumpablesOnMap(false);
                for (BumpableWithShape b : BWS) {
                    //Range 0 check
                    if (shapesOverlap(tShapeR0, b.shape)) {
                        rfindings.add(new RangeFindings(0, b.pilotName));
                        continue;
                    }
                    //Range 1 check
                    if(shapeForRange1 !=null && shapesOverlap(tShapeR1, b.shape)){
                        rfindings.add(new RangeFindings(1, b.pilotName));
                        continue;
                    }
                    //Range 2 check
                    if(shapeForRange2 !=null && shapesOverlap(tShapeR2,  b.shape)){
                        rfindings.add(new RangeFindings(2, b.pilotName));
                        continue;
                    }
                    //Range 3 check
                    if(shapeForRange3 !=null && shapesOverlap(tShapeR3, b.shape)){
                        rfindings.add(new RangeFindings(3, b.pilotName));
                    }
                }
            }

            makeBigAnnounceCommand(bigAnnounce, rfindings);
        }

        return piece.keyEvent(stroke); //did not find anything worth react to, so send back the key for others to deal with it
    }

    private List<BumpableWithShape> getBumpablesOnMap(Boolean wantShipsToo) {

        List<BumpableWithShape> bumpables = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_an_asteroid")) {
                // comment out this line and the next three that add to bumpables if bumps other than with ships shouldn't be detected yet
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece, "Asteroid", "2".equals(testFlipString), false));
            } else if (piece.getState().contains("this_is_a_debris")) {
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece,"Debris","2".equals(testFlipString), false));
            } else if (piece.getState().contains("this_is_a_bomb")) {
                bumpables.add(new BumpableWithShape((Decorator)piece, "Mine", false, false));
            } else if (piece.getState().contains("this_is_a_gascloud")) {
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece, "GasCloud", "2".equals(testFlipString), false));
            }else if (piece.getState().contains("this_is_a_remote")) {
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece, "Remote", "2".equals(testFlipString), false));
            }else if(wantShipsToo == true && piece.getState().contains("this_is_a_ship")){
                //MrMurphM
                //    GamePiece newPiece = PieceCloner.getInstance().clonePiece(piece);
                //    newPiece.setPosition(piece.getPosition());
                //END
                BumpableWithShape tentativeBumpable = new BumpableWithShape((Decorator)piece, "Ship",false,
                        this.getInner().getState().contains("this_is_2pointoh"));
                if (getId().equals(tentativeBumpable.bumpable.getId())) {
                    continue;
                }
                bumpables.add(tentativeBumpable);

            }
        }
        return bumpables;
    }
    private void verifyBasicShapes() {
        if(shapeForRange1==null || shapeForRange2 ==null || shapeForRange3 ==null) logToChat("* -- Generating shapes for the first range check of this token, this will take a few seconds (and will only be done the first time).");

        String pieceName = (Decorator.getInnermost(this)).getName();

        if(shapeForRange1==null){
            shapeForRange1 = getRawShapeFromFile(getAssociatedImage(pieceName,1));
        }
        if(shapeForRange2==null){
            shapeForRange2 = getRawShapeFromFile(getAssociatedImage(pieceName,2));
        }
        if(shapeForRange3==null){
            shapeForRange3 = getRawShapeFromFile(getAssociatedImage(pieceName,3));
        }

    }

    private Shape getRawShapeFromFile(String fileName){
        Shape finalShape;
        if(fileName.equals("")) return null;
        finalShape = Util.getImageShape(fileName);

        return finalShape;
    }
    public static double rotX(double x, double y, double angle){
        return Math.cos(-Math.PI*angle/180.0f)*x - Math.sin(-Math.PI*angle/180.0f)*y;
    }
    public static double rotY(double x, double y, double angle){
        return Math.sin(-Math.PI*angle/180.0f)*x + Math.cos(-Math.PI*angle/180.0f)*y;
    }
    private Shape getTransformedShape(Shape rawShape){
        //Info Gathering: gets the angle from repoTemplate which deals with degrees, local space with ship at 0,0, pointing up
        double globalShipAngle = getRotator().getAngle(); //ship angle


        //Step 1: Translation
        double offx = this.getPosition().getX();
        double offy = this.getPosition().getY();



        String pieceName = (Decorator.getInnermost(this)).getName();
        if(pieceName.equals("DRK-1 Probe Droid")){
            globalShipAngle-=36.0f;
            offx+=1.0f;
            offy-=3.0f;
        }


        double off2x = getOffX(pieceName);
        double off2y = getOffY(pieceName);

        double toffx = rotX(off2x, off2y, globalShipAngle);
        double toffy = rotY(off2x, off2y, globalShipAngle);

        //STEP 2: translation into place
        Shape transformedShape = AffineTransform.
                getTranslateInstance((int)offx + (int)toffx , (int)offy + (int)toffy).
                createTransformedShape(rawShape);
        double roundedAngle = convertAngleToGameLimits(globalShipAngle);
        transformedShape = AffineTransform
                .getRotateInstance(Math.toRadians(-roundedAngle), (int)offx + (int)toffx , (int)offy+ (int)toffy)
                .createTransformedShape(transformedShape);

        return transformedShape;
    }

    private double convertAngleToGameLimits(double angle) {
        this.testRotator.setAngle(angle);
        return this.testRotator.getAngle();
    }
    private FreeRotator getRotator() {
        if (this.myRotator == null) {
            this.myRotator = ((FreeRotator) Decorator.getDecorator(getOutermost(this), FreeRotator.class));
        }
        return this.myRotator;
    }
    private void prepAnnouncementStart() {
        //prepares the initial part of the chat window string that announces the firing options
        //bigAnnounce = "*** Calculating ranges from " + this.piece.getName() + " to ";


        GamePiece outermost = Decorator.getInnermost(this);

        String originPieceName = outermost.getName();
        //String[] parts = originPieceName.split("\\(");
        //String finalName = (parts.length>1?parts[0]:originPieceName);
        String firstAnnounce = "*** Calculating ranges from " + originPieceName + " to ";

        switch (whichOption) {
            case findObstaclesShips:
                firstAnnounce += "ships and obstacles.";
                break;
            case findShips:
                firstAnnounce += "ships.";
                break;
            case findObstacles:
                firstAnnounce += "obstacles.";
                break;
        }
        logToChat(firstAnnounce);
    }
    private void makeBigAnnounceCommand(String bigAnnounce, ArrayList<RangeFindings> rfindings) {
        String range1String = "";
        String range2String = "";
        String range3String = "";
        String range0String = "";

        boolean hasR1 = false;
        boolean hasR2 = false;
        boolean hasR3 = false;
        boolean hasR0 = false;

        for (RangeFindings rf : rfindings) {
            if (rf.range == 0) {
                hasR0 = true;
                range0String += rf.fullName + " | ";
            }
            if (rf.range == 1) {
                hasR1 = true;
                range1String += rf.fullName + " | ";
            }
            if (rf.range == 2) {
                hasR2 = true;
                range2String += rf.fullName + " | ";
            }
            if (rf.range == 3) {
                hasR3 = true;
                range3String += rf.fullName + " | ";
            }
        }

        String result = bigAnnounce + (hasR0 ? "*** Range 0: " + range0String + "\n" : "") +
                (hasR1 ? "*** Range 1: " + range1String + "\n" : "") +
                (hasR2 ? "*** Range 2: " + range2String + "\n" : "") +
                (hasR3 ? "*** Range 3: " + range3String + "\n" : "");
        if (hasR0 == false && hasR1 == false && hasR2 == false && hasR3 == false) result = "Nothing in range.";

        logToChat(result);
    }
    private List<BumpableWithShape> getOtherShipsOnMap() {
        List<BumpableWithShape> ships = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_a_ship") && piece.getId() != this.piece.getId()) {
                ships.add(new BumpableWithShape((Decorator)piece, "Ship",
                        piece.getProperty("Pilot Name").toString(), piece.getProperty("Craft ID #").toString(),
                        piece.getState().contains("this_is_2pointoh")));
            }
        }
        return ships;
    }


    public String getDescription() {
        return "Custom range detection for tokens (mic.AutoRangeForTokens)";
    }

    public void mySetType(String type) {

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

    public static class RangeFindings {
        public int range=0;
        public String fullName="";
        public boolean isObstructed=false;

        public RangeFindings(){ /* To allow serizliation */ }
        public RangeFindings(int range, String fullName){
            this.fullName = fullName;
            this.range = range;
            this.isObstructed = false;
        }
    }


}
