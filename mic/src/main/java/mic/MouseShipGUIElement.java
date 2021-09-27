/* Created by Mic on July 1st, 2019
 * This is used to track the local position of a mouse ship GUI element, toggle status, etc.
 */

package mic;

import VASSAL.build.GameModule;
import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.GamePiece;
import VASSAL.counters.NonRectangular;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.FileArchive;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

public class MouseShipGUIElement {

    BufferedImage image, imageSpent;  //imageSpent is for when the toggle button is off
    String imageName;
    int globalX, globalY; //coordinates in the global system
    boolean toggle; //used for toggling if needed
    KeyStroke associatedKeyStroke; //can produce a keystroke to the ship piece if this attribute is defined
    int whichTripleChoice = 0; //can select one of 3 choices in certain situations (barrel rolls, tallon rolls, etc)
    Shape nonRect;
    int page = 0; //used for page toggling, to keep the GUI light in appearance. By default, stuff should be at page 0 to always appear
    String logMessage; //used when a move must happen and output a logtochat message (such as boost or slam)

    public MouseShipGUIElement(){
        toggle = false;
    }

    public String getLogMessage(){
        return logMessage;
    }
    public String getImageName(){
        return imageName;
    }

    //Slightly different constructor for when you need 2 graphics for a toggle button
    public MouseShipGUIElement(int wantedPage, String slotName, String wantedImageName, String imageNameInactive, int wantedX, int wantedY, KeyStroke wantedKeyStroke, int wantedTripleChoice) {
        page = wantedPage;
        globalX = wantedX;
        globalY = wantedY;
        associatedKeyStroke = wantedKeyStroke;
        whichTripleChoice = wantedTripleChoice;
        imageName = wantedImageName;


        List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);
        GamePiece piece = null;

        if(slotName != null) {
            for (PieceSlot pieceSlot : pieceSlots) {
                String sName = pieceSlot.getConfigureName();
                if (sName.equals(slotName)) {
                    piece = Util.newPiece(pieceSlot);
                    nonRect = piece.getShape();
                    break;
                }
            }
        }
        //load the images
        try {
            GameModule gameModule = GameModule.getGameModule();
            DataArchive dataArchive = gameModule.getDataArchive();
            FileArchive fileArchive = dataArchive.getArchive();

            InputStream inputstream = new BufferedInputStream(fileArchive.getInputStream("images/" + imageName));
            image = ImageIO.read(inputstream);
            inputstream.close();

            InputStream inputstream2 = new BufferedInputStream(fileArchive.getInputStream("images/" + imageNameInactive));
            imageSpent = ImageIO.read(inputstream2);
            inputstream2.close();

        }
        catch(Exception e){
            Util.logToChat("Failed to load GUI images " + imageName);
        }
    }

    //main constructor
    public MouseShipGUIElement(int wantedPage, String slotName, String wantedImageName, int wantedX, int wantedY, KeyStroke wantedKeyStroke, int wantedTripleChoice, String wantedLogMessage){
        page = wantedPage;
        globalX = wantedX;
        globalY = wantedY;
        associatedKeyStroke = wantedKeyStroke;
        whichTripleChoice = wantedTripleChoice;
        imageName = wantedImageName;
        logMessage = wantedLogMessage;

        List<PieceSlot> pieceSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);
        GamePiece piece = null;

        if(slotName != null) {
            for (PieceSlot pieceSlot : pieceSlots) {
                String sName = pieceSlot.getConfigureName();
                if (sName.equals(slotName)) {
                    piece = Util.newPiece(pieceSlot);
                    nonRect = piece.getShape();
                    break;
                }
            }
        }
        //load the image
        try {
            GameModule gameModule = GameModule.getGameModule();
            DataArchive dataArchive = gameModule.getDataArchive();
            FileArchive fileArchive = dataArchive.getArchive();

            InputStream inputstream = new BufferedInputStream(fileArchive.getInputStream("images/" + imageName));
            image = ImageIO.read(inputstream);
            inputstream.close();
        }
        catch(Exception e){
            Util.logToChat("Failed to load GUI image " + imageName);
        }
    }

    public boolean isToggled(){
        return toggle;
    }

    public void setToggle(boolean wantedToggle){
        toggle = wantedToggle;
    }

    public int getPage(){
        return page;
    }

    public void toggleIt(){
        if(toggle) toggle = false;
        else toggle = true;
    }

    public int getTripleChoice(){
        return whichTripleChoice;
    }

    //scale is done outside of this method
    public AffineTransform getTransformForClick(double scale, int abx, int aby){
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(abx + globalX, aby + globalY);
        return affineTransform;
    }

    public AffineTransform getTransformForDraw(double scale){
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.scale(scale, scale);
        affineTransform.translate(globalX, globalY);
        return affineTransform;
    }

    public AffineTransform getTransformForDraw(double scale, double scale2){
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.scale(scale*scale2, scale*scale2);
        affineTransform.translate(globalX, globalY);
        return affineTransform;
    }

    public Shape getNonRect(){
        return nonRect;
    }
}
