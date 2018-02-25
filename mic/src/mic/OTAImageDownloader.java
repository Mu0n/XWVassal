package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class OTAImageDownloader extends Decorator implements EditablePiece {

    public static final String ID = "otaImageDownloader";

    public OTAImageDownloader(){
        this(null);
    }

    public OTAImageDownloader(GamePiece piece){
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
        return "Custom OTA Image Downloader (mic.OTAImageDownloader)";
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
    public static class ImageDownloadCommand extends Command {

        ArrayList<String> pilotImages;
        static String imageEncodingString = "";
        ImageDownloadCommand(ArrayList<String> pilotImagesToDownload)
        {
            pilotImages = pilotImagesToDownload;

            if(pilotImages != null && pilotImages.size() >0)
            {
                StringBuilder sb = new StringBuilder();
                int index = 0;
                for(String pilotImage : pilotImages)
                {
                    index++;
                    if(index != 1)
                    {
                        sb.append(",");
                    }
                    sb.append(pilotImage);

                }
                imageEncodingString = sb.toString();
            }
        }

        // construct the Pilot Card piece
        protected void executeCommand()
        {
            ArrayList<String> pilotImageDownloadList = new ArrayList();
            // loop through each image to see which ones we need
            if(pilotImages != null)
            {
                for(String pilotImage : pilotImages)
                {
                    // check each one
                    if(!Util.imageExistsInModule(pilotImage))
                    {
                        // this pilot image is not stored locally.  add it to the list to download
                        pilotImageDownloadList.add(pilotImage);
                    }
                }
            }

            // now download them
            if(pilotImageDownloadList.size() > 0)
            {
                // TODO pop up a progress bar

                // download each one
                for(String pilotImage: pilotImageDownloadList)
                {
                    Util.downloadAndSaveImageFromOTA("pilots",pilotImage);
                    // TODO update progress bar
                }

                // TODO close progress bar
            }

        }

        protected Command myUndoCommand() {
            return null;
        }
        public static class ImageDownloaderEncoder implements CommandEncoder {
            private static final Logger logger = LoggerFactory.getLogger(StemPilot.class);
            private static final String commandPrefix = "ImageDownloaderEncoder=";

            public static OTAImageDownloader.ImageDownloadCommand.ImageDownloaderEncoder INSTANCE = new OTAImageDownloader.ImageDownloadCommand.ImageDownloaderEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding ImageDownloadCommand");

                command = command.substring(commandPrefix.length());
                try {
                    imageEncodingString = command.toString();
                } catch (Exception e) {
                    logger.error("Error decoding ImageDownloadCommand", e);
                    return null;
                }
                return null;
            }

            public String encode(Command c) {
                if (!(c instanceof StemPilot.PilotGenerateCommand)) {
                    return null;
                }
                logger.info("Encoding ImageDownloadCommand");
                StemPilot.PilotGenerateCommand dialGenCommand = (StemPilot.PilotGenerateCommand) c;
                try {
                    return commandPrefix + imageEncodingString;
                } catch(Exception e) {
                    logger.error("Error encoding ImageDownloadCommand", e);
                    return null;
                }
            }
        }
    }
}
