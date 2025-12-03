package snap.webapi;

/**
 * This class is a wrapper for Web API HTMLScriptElement (https://developer.mozilla.org/en-US/docs/Web/API/HTMLScriptElement).
 */
public class HTMLScriptElement extends HTMLElement {

    /**
     * Constructor.
     */
    public HTMLScriptElement(Object jsObj)
    {
        super(jsObj);
    }

//    String getText();
//
    public void setText(String aString)
    {
        setMemberString("text", aString);
    }
//
//    String getHtmlFor();
//
//    void setHtmlFor(String var1);
//
//    String getEvent();
//
//    void setEvent(String var1);
//
//    String getCharset();
//
//    void setCharset(String var1);
//
//    boolean getDefer();
//
//    void setDefer(boolean var1);
//
//    String getSrc();
//
    public void setSrc(String srcString)  { setMemberString("src", srcString); }

    public void setAsync(boolean aValue)  { setMemberBoolean("async", aValue); }

//
//    String getType();
//
//    void setType(String var1);
}
