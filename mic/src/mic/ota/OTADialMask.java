package mic.ota;

public class OTADialMask {
    private String shipName;
    private String shipXws;
    private String faction;
    private String dialHideImageName;
    private String dialMaskImageName;
    private boolean status;
    private boolean statusOTA;

    public OTADialMask()
    {
        super();

    }

    public void setShipName(String shipName)
    {
        this.shipName = shipName;
    }
    public String getShipName()
    {
        return this.shipName;
    }
    public void setShipXws(String shipXws)
    {
        this.shipXws = shipXws;
    }
    public String getShipXws()
    {
        return this.shipXws;
    }
    public void setFaction(String faction)
    {
        this.faction = faction;
    }
    public String getFaction()
    {
        return this.faction;
    }
    public void setDialHideImageName(String dialHideImageName)
    {
        this.dialHideImageName = dialHideImageName;
    }
    public String getDialHideImageName()
    {
        return this.dialHideImageName;
    }
    public void setDialMaskImageName(String dialMaskImageName)
    {
        this.dialMaskImageName = dialMaskImageName;
    }
    public String getDialMaskImageName()
    {
        return this.dialMaskImageName;
    }
    public void setStatus(boolean status)
    {
        this.status = status;
    }
    public boolean getStatus()
    {
        return this.status;
    }
    public boolean getStatusOTA()
    {
        return this.statusOTA;
    }

    public void setStatusOTA(boolean statusOTA) {
        this.statusOTA = statusOTA;
    }
}
