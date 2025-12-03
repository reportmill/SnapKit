package snap.webapi;

/**
 * This class is a wrapper for Web API Text (https://developer.mozilla.org/en-US/docs/Web/API/Text).
 */
public class Text extends CharacterData {

    /**
     * Constructor.
     */
    public Text(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Returns a string containing the text of all Text nodes logically adjacent to this Node, concatenated in document order.
     */
    public String getWholeText()  { return (String) call("wholeText"); }
}
