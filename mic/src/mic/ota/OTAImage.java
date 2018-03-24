package mic.ota;

public class OTAImage {
    private String imageType;
    private String objectName;
    private String imageName;

    public OTAImage()
    {
        super();
    }

    public String getImageType()
    {
        return this.imageType;
    }

    public void setImageType(String imageType)
    {
        this.imageType = imageType;
    }

    public String getObjectName()
    {
        return this.objectName;
    }

    public void setObjectName(String objectName)
    {
        this.objectName = objectName;
    }

    public String getImageName()
    {
        return this.imageName;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }
}
