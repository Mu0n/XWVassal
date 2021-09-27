package mic.ota;

public class OTAShipBase {
    private String shipName;
    private String shipXws;
    private String identifier;
    private String faction;
    private String shipBaseImageName;
    private String shipImageName;
    private boolean status;
    private boolean statusOTA;

    public OTAShipBase()
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
    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }
    public String getIdentifier()
    {
        return this.identifier;
    }
    public void setFaction(String faction)
    {
        this.faction = faction;
    }
    public String getFaction()
    {
        return this.faction;
    }
    public void setShipBaseImageName(String shipBaseImageName)
    {
        this.shipBaseImageName = shipBaseImageName;
    }
    public String getShipBaseImageName()
    {
        return this.shipBaseImageName;
    }
    public void setshipImageName(String shipImageName)
    {
        this.shipImageName = shipImageName;
    }
    public String getshipImageName()
    {
        return this.shipImageName;
    }
    public void setStatus(boolean status)
    {
        this.status = status;
    }
    public boolean getStatus()
    {
        return this.status;
    }
    //refers to the ship Gfx present in OTA, but the shipbase is affected, so...
    public boolean getStatusOTA() { return this.statusOTA; }

    public void setStatusOTA(boolean existsInOTA) { this.statusOTA = existsInOTA; }
}
