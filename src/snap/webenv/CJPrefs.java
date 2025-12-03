package snap.webenv;
import snap.util.Convert;
import snap.util.Prefs;
import snap.webapi.Storage;
import java.util.ArrayList;
import java.util.List;

/**
 * A Prefs implementation for CheerpJ using LocalStorage.
 */
public class CJPrefs extends Prefs {

    // The name of this prefs node
    private String  _name;

    /**
     * Constructor.
     */
    public CJPrefs(String aName)
    {
        _name = aName;
        assert (_name != null);
    }

    /**
     * Override to return name.
     */
    @Override
    public String getName()  { return _name; }

    /**
     * Override to use LocalStorage.
     */
    @Override
    public Object getValue(String aKey, Object aDefault)
    {
        // Get key for name
        String key = aKey;
        if (_name != null)
            key = _name + '.' + key;

        // Get value from LocalStorage
        Storage localStorage = Storage.getLocalStorage();
        String val = localStorage.getItem(key);
        return val != null ? val : aDefault;
    }

    /**
     * Override to use LocalStorage.
     */
    @Override
    public void setValue(String aKey, Object aValue)
    {
        // Get key for name
        String key = aKey;
        if (_name != null)
            key = _name + '.' + key;

        // Get string value and set in LocalStorage
        String valueStr = Convert.stringValue(aValue);
        Storage localStorage = Storage.getLocalStorage();
        localStorage.setItem(key, valueStr);
    }

    /**
     * Override to use LocalStorage.
     */
    @Override
    public String[] getKeys()
    {
        Storage localStorage = Storage.getLocalStorage();

        // Get keys until null
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {

            // Get key - just break if null
            String key = localStorage.getKey(i);
            if (key == null)
                break;

            // Strip name
            if (_name != null && key.startsWith(_name))
                key = key.substring(_name.length());
            keys.add(key);
        }

        // Return array
        return keys.toArray(new String[0]);
    }

    /**
     * Clears all the preferences.
     */
    public void clear()
    {
        Storage localStorage = Storage.getLocalStorage();
        String[] keys = getKeys();
        for (String key : keys)
            localStorage.removeItem(_name + '.' + key);
    }

    /**
     * Returns a child node for name.
     */
    @Override
    public Prefs getChild(String aName)
    {
        String childName = _name + '.' + aName;
        return new CJPrefs(childName);
    }
}
