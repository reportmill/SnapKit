package snap.webapi;

/**
 * This class is a wrapper for Web API HTMLImageElement (https://developer.mozilla.org/en-US/docs/Web/API/HTMLImageElement).
 */
public class HTMLImageElement extends HTMLElement implements CanvasImageSource {

    /**
     * Constructor.
     */
    public HTMLImageElement(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Returns the image source.
     */
    public String getSrc()  { return getMemberString("src"); }

    /**
     * Sets the image source.
     */
    public void setSrc(String aSrc)  { setMemberString("src", aSrc); }

    /**
     * Return image width.
     */
    public int getWidth()  { return getMemberInt("width"); }

    /**
     * Set image height.
     */
    public void setWidth(int aValue)  { setMemberInt("width", aValue); }

    /**
     * Return image height.
     */
    public int getHeight()  { return getMemberInt("height"); }

    /**
     * Set image height.
     */
    public void setHeight(int aValue)  { setMemberInt("height", aValue);}

    /**
     * Returns the HTMLImageElement crossOrigin property.
     */
    public String getCrossOrigin()  { return getMemberString("crossOrigin"); }

    /**
     * Sets the HTMLImageElement crossOrigin property.
     */
    public void setCrossOrigin(String aString)  { setMemberString("crossOrigin", aString); }
}
