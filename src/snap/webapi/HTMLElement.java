package snap.webapi;

/**
 * This class is a wrapper for Web API HTMLElement (https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement).
 */
public class HTMLElement extends Element implements EventTarget {

    // The style element
    private CSSStyleDeclaration _style;

    /**
     * Constructor.
     */
    public HTMLElement(Object jsoObj)
    {
        super(jsoObj);
    }

    /**
     * Returns the offset top.
     */
    public int getOffsetTop()  { return getMemberInt("offsetTop"); }

    /**
     * Returns the offset left.
     */
    public int getOffsetLeft()  { return getMemberInt("offsetLeft"); }

    /**
     * Returns the inline style of an element.
     */
    public CSSStyleDeclaration getStyle()
    {
        if (_style != null) return _style;
        Object styleJS = getMember("style");
        return _style = new CSSStyleDeclaration(styleJS);
    }

    /**
     * Returns a string representing the rendered text content of an element.
     */
    public String getInnerText()  { return getMemberString("innerText"); }

    /**
     * Sets a string representing the rendered text content of an element.
     */
    public void setInnerText(String aString)  { setMemberString("innerText", aString); }

    /**
     * Sets whether html element has content editable.
     */
    public void setContentEditable(boolean aValue)
    {
        setMemberString("contentEditable", aValue ? "true" : "false");
        setMemberInt("tabIndex", 0);
    }

    public int getClientWidth()  { return getMemberInt("clientWidth"); }

    public int getClientHeight()  { return getMemberInt("clientHeight"); }

    public void focus()  { call("focus"); }

    public void blur()  { call("blur"); }

    public void click()  { WebEnv.get().click(_jsObj); }

    /**
     * Returns the wrapped HTML element for given tag name.
     */
    public static HTMLElement getElementForName(String tagName, Object jsObj)
    {
        return switch (tagName.toLowerCase()) {
            case "a" -> new HTMLAnchorElement(jsObj);
            case "audio" -> new HTMLAudioElement(jsObj);
            case "canvas" -> new HTMLCanvasElement(jsObj);
            case "div" -> new HTMLDivElement(jsObj);
            case "img" -> new HTMLImageElement(jsObj);
            case "input" -> new HTMLInputElement(jsObj);
            case "body" -> new HTMLBodyElement(jsObj);
            case "html" -> new HTMLHtmlElement(jsObj);
            case "iframe" -> new HTMLIFrameElement(jsObj);
            case "script" -> new HTMLScriptElement(jsObj);
            default -> new HTMLElement(jsObj);
        };
    }
}
