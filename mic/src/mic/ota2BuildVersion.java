package mic;

import VASSAL.build.GameModule;
import VASSAL.tools.DataArchive;
import com.fasterxml.jackson.annotation.JsonProperty;
import mic.ota.XWOTAUtils;

import java.io.File;
import java.io.InputStream;

public class ota2BuildVersion {
    public static String remoteUrl = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA2e/master/version";
    public static String ota2Zip = "OTA2.zip";

    @JsonProperty("build")
    private int build;

    public int getBuild(){ return build; }

    public static int checkRemoteBuildVersion() {
        ota2BuildVersion ota2Object;
        try {
            ota2Object = Util.loadRemoteJson(ota2BuildVersion.remoteUrl, ota2BuildVersion.class);
        }catch(Exception e)
        {
            return -1;
        }
        return ota2Object.getBuild();
    }

    public static int checkLocalBuildVersion() {
        String pathToUse = XWOTAUtils.getModulePath();

        if(ota2BuildVersion.checkExistenceOfLocalOTA2Zip() == false)
            //can't find the local depot, rebuld it from...
            // a stashed probably deprecated local copy or from remote
        {
            Util.logToChat("--- Building a local journal of the over-the-air system.");
            return 0;
        }
        else{
            try { //we know the zip file exists, so check it out
                DataArchive dataArchive = new DataArchive(pathToUse + File.separator + ota2BuildVersion.ota2Zip);
                InputStream inputStream = dataArchive.getInputStream("version");
                ota2BuildVersion ota2Object = Util.loadClasspathJsonInDepot("version", ota2BuildVersion.class, inputStream);
                inputStream.close();
                dataArchive.close();
                return ota2Object.getBuild();
            } catch(Exception e){
                Util.logToChat("Couldn't load the local over-the-air journal");
            }
        }
        return 0;
    }

    public static boolean checkExistenceOfLocalOTA2Zip()
    {
        File dummyFile = new File(GameModule.getGameModule().getDataArchive().getName());
        String path = dummyFile.getPath();
        File theZip = new File( path.substring(0,path.lastIndexOf(File.separator)) + File.separator + ota2BuildVersion.ota2Zip);
        return theZip.exists() && theZip.isFile();
    }


}
