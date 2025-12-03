package snap.webapi;

/**
 * This class is a wrapper for Web API CharacterData (https://developer.mozilla.org/en-US/docs/Web/API/CharacterData).
 */
public class CharacterData extends Node {

    /**
     * Constructor.
     */
    public CharacterData(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Returns the string representing the textual data contained in this object.
     */
    public String getData()  { return getMemberString("data"); }

    /**
     * Returns a number representing the size of the string contained in the object.
     */
    public int getLength()  { return getMemberInt("length"); }
}
