package snap.webapi;

/**
 * This class is a wrapper for Web API Storage (https://developer.mozilla.org/en-US/docs/Web/API/Storage).
 */
public class Storage extends JSProxy {

    // The local storage
    private static Storage _localStorage;

    /**
     * Constructor.
     */
    public Storage(Object programJS)
    {
        super(programJS);
    }

    public String getItem(String aKey)  { return (String) call("getItem", aKey); }

    public void setItem(String aKey, String aValue)
    {
        if (aValue == null)
            removeItem(aKey);
        else call("setItem", aKey, aValue);
    }

    public void removeItem(String aKey)  { call("removeItem", aKey); }

    public String getKey(int anIndex)  { return (String) call("key", anIndex); }

    public void clear()  { call("clear"); }

    /**
     * Returns the shared local storage.
     */
    public static Storage getLocalStorage()
    {
        if (_localStorage != null) return _localStorage;
        Window window = Window.get();
        Object localStorageJS = window.getMember("localStorage");
        return _localStorage = new Storage(localStorageJS);
    }
}
