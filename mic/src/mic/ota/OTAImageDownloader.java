package mic.ota;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import mic.ProgressBar;
import mic.StemPilot;
import mic.Util;
import mic.XWImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class OTAImageDownloader extends Decorator implements EditablePiece {

    public static final String ID = "otaImageDownloader";
    public final static int ONE_SECOND = 1000;
    private static int percentComplete = 0;
    private static String progressText = "";
    private static boolean downloadComplete = false;
    private static ArrayList<String> pilotImageDownloadList = new ArrayList();
  //  private static ProgressBar2 pg;
    private static ProgressBar progressBar;
    private static Timer timer;
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
    public KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }
    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        return null;
    }

    public String getDescription() {
        return "Custom OTA Image Downloader (mic.ota.OTAImageDownloader)";
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


    public static void updateProgress(int percent, String text)
    {

        percentComplete = percent;
        progressText = text;
        Util.logToChat("OTAID: ProgressUpdated: "+ percent + " "+text);
    }


    public static void setComplete(boolean complete)
    {
        downloadComplete = complete;
    }

    public static ArrayList<String> getPilotImageDownloadList()
    {
        return pilotImageDownloadList;
    }

    //this is the command that takes a ship xws name, fetches the maneuver info and constructs the dial layer by layer
    public static class ImageDownloadCommand extends Command {

        ArrayList<String> pilotImages;
        static String imageEncodingString = "";
        public ImageDownloadCommand(ArrayList<String> pilotImagesToDownload)
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
        public void executeCommand()
        {
            // loop through each image to see which ones we need
            if(pilotImages != null)
            {
                for(String pilotImage : pilotImages)
                {
                    // check each one
                    if(!XWImageUtils.imageExistsInModule(pilotImage))
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


                for(String pilotImage: pilotImageDownloadList)
                {
                   // Util.logToChat("OTAIDT: Downloading "+pilotImage);
                   // text = "Downloading "+pilotImage;
                   // OTAImageDownloader.updateProgress(percent,text);

                    XWImageUtils.downloadAndSaveImageFromOTA("pilots",pilotImage);
                  //  Util.logToChat("OTAIDT: Download Complete: "+pilotImage);
                  //  done++;

                   // percent = (done * 100/total) ;
                   // OTAImageDownloader.updateProgress(percent,text);

                }






/*

                progressBar = new ProgressBar();
                progressBar.setVisible(true);
                progressBar.openFrame();


                // start the new worker thread
                OTAImageDownloaderThread workerThread = new OTAImageDownloaderThread();
                Thread thread = new Thread(workerThread);
                thread.start();

                final java.util.Timer timer = new java.util.Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try{
                            if(downloadComplete) {
                                timer.cancel();
                                progressBar.closeFrame();
                                return;
                            }
                            progressBar.updateBar(percentComplete,progressText);
                            Thread.yield();
                        } catch (Exception e) {
                            //logger.error("Error rendering collision visualization", e);
                        }
                    }
                }, 0,ONE_SECOND);
*/


/*
                while(!downloadComplete)
                {
                    progressBar.updateBar(percentComplete,progressText);

                    try {
                        Thread.sleep(500);
                    }catch(InterruptedException e)
                    {

                    }
                }
                progressBar.closeFrame();
                */
/*
                timer = new Timer(ONE_SECOND, new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        progressBar.updateBar(percentComplete,progressText);
                        progressBar.repaint();
                        if (downloadComplete) {
                            timer.stop();
                            progressBar.closeFrame();
                        }
                    }
                });*/


/*
                // loop until complete
                while(!downloadComplete) {

                    Timer timer = new Timer(100, this);
                    timer.setInitialDelay(1900);
                    timer.start();
                    // update the progress bar
                }



                pg.start("Downloading required images");
                // download each one
                int percent = 0;
                int total = pilotImageDownloadList.size();
                int done = 0;
                String text = "";
                for(String pilotImage: pilotImageDownloadList)
                {
                    text = "Downloading "+pilotImage;
                    pg.updateBar(percent,text);
                    Util.downloadAndSaveImageFromOTA("pilots",pilotImage);

                    done++;

                    // TODO update progress bar
                    percent = (done * 100/total) ;
                    pg.updateBar(percent,text);
*/
                }

                // TODO close progress bar
            }



        public Command myUndoCommand() {
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
