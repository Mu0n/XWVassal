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

    BufferedImage image;
    int localX, localY; //coordinates in the local system
    int globalX, globalY; //coordinates in the global system
    boolean toggle; //used for toggling if needed
    KeyStroke associatedKeyStroke; //can produce a keystroke to the ship piece if this attribute is defined
    int whichTripleChoice = 0; //can select one of 3 choices in certain situations (barrel rolls, tallon rolls, etc)
    Shape nonRect;

    public MouseShipGUIElement(){
        localX = localY = 0;
        toggle = false;
    }

    public MouseShipGUIElement(String slotName, String imageName, int wantedX, int wantedY, KeyStroke wantedKeyStroke, int wantedTripleChoice){
        globalX = wantedX;
        globalY = wantedY;
        associatedKeyStroke = wantedKeyStroke;
        whichTripleChoice = wantedTripleChoice;


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
    public void setLocalX(int newX){
        localX = newX;
    }

    public void setLocalY(int newY){
        localY = newY;
    }

    public int getX(){
        return localX;
    }

    public int getY(){
        return localY;
    }

    public boolean isToggled(){
        return toggle;
    }

    public void setToggle(boolean wantedToggle){
        toggle = wantedToggle;
    }

    public void toggleIt(){
        if(toggle) toggle = false;
        else toggle = true;
    }

    public int getTripleChoice(){
        return whichTripleChoice;
    }
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
