package mic;

/**
 * Created by TerTer on 2017-03-11.
 */
public class UserInformer {

    private static boolean informUserAboutUWingPivotWing = false;
    private static boolean informUserAboutAdaptability = false;

    public static void setInformUserAboutUWingPivotWing() {
        informUserAboutUWingPivotWing = true;
    }

    public static void setInformUserAboutAdaptability() {
        informUserAboutAdaptability = true;
    }

    public static void informUser()
    {
        if (informUserAboutUWingPivotWing)
        {
            Util.logToChat("Remember to set Pivot Wing to desired side during Setup. Attack Mode is chosen by default. Alt-U on the U-Wing to switch, Ctrl-F on Pivot Wing to flip.");
            informUserAboutUWingPivotWing = false;
        }
        if (informUserAboutAdaptability)
        {
            Util.logToChat("Remember to set Adaptability to desired side during Setup. +1 Pilot skill is chosen by default. Ctrl+Alt+P on ship to switch. Ctrl-F on Adaptability to flip.");
            informUserAboutAdaptability = false;
        }
    }
}
