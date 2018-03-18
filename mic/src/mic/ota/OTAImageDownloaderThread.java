package mic.ota;

import mic.Util;
import mic.XWImageUtils;

import java.util.ArrayList;

public class OTAImageDownloaderThread implements Runnable {
//    private OTAImageDownloader imageDownloader;

    public OTAImageDownloaderThread()
    {

    }

    private void executeWork(ArrayList<String> pilotImageDownloadList)
    {
        int percent = 0;
        int total = pilotImageDownloadList.size();
        int done = 0;
        String text = "";
        for(String pilotImage: pilotImageDownloadList)
        {
            Util.logToChat("OTAIDT: Downloading "+pilotImage);
            text = "Downloading "+pilotImage;
            OTAImageDownloader.updateProgress(percent,text);
            XWImageUtils.downloadAndSaveImageFromOTA("pilots",pilotImage);
            Util.logToChat("OTAIDT: Download Complete: "+pilotImage);
            done++;

            percent = (done * 100/total) ;
            OTAImageDownloader.updateProgress(percent,text);
           // Thread.yield();

        }
        OTAImageDownloader.setComplete(true);
    }


    @Override
    public void run() {
        executeWork(OTAImageDownloader.getPilotImageDownloadList());
    }
}
