package snap.webapi;

/**
 * This class is a wrapper for Web API HTMLIFrameElement (https://developer.mozilla.org/en-US/docs/Web/API/HTMLIFrameElement).
 */
public class HTMLIFrameElement extends HTMLElement {

    /**
     * Constructor.
     */
    public HTMLIFrameElement(Object jsObj)
    {
        super(jsObj);
    }

//    String getAlign();
//
//    void setAlign(String var1);
//
//    String getFrameBorder();
//
//    void setFrameBorder(String var1);
//
//    String getHeight();
//
//    void setHeight(String var1);
//
//    String getLongDesc();
//
//    void setLongDesc(String var1);
//
//    String getMarginHeight();
//
//    void setMarginHeight(String var1);
//
//    String getMarginWidth();
//
//    void setMarginWidth(String var1);
//
//    String getName();
//
//    void setName(String var1);
//
//    String getScrolling();
//
//    void setScrolling(String var1);
//
//    String getSrc();
//
    public void setSrc(String aString)
    {
        setMemberString("src", aString);
    }
//
//    String getWidth();
//
//    void setWidth(String var1);
//
    public HTMLDocument getContentDocument()
    {
        Object contentDocJS = getMember("contentDocument");
        return new HTMLDocument(contentDocJS);
    }
}
