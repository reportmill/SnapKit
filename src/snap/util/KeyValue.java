package snap.util;

/**
 * This class represents a simple Key/Value pair.
 */
public class KeyValue<T> {

    // The key
    private String  _key;

    // The Value
    private T  _value;

    /**
     * Constructor.
     */
    public KeyValue(String aKey, T aValue)
    {
        _key = aKey;
        _value = aValue;
    }

    /**
     * Returns the key.
     */
    public String getKey()  { return _key; }

    /**
     * Returns the value.
     */
    public T getValue()  { return _value; }
}
