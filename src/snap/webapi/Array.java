package snap.webapi;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a wrapper for Web API Array (https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array).
 */
public class Array<E> extends JSProxy {

    /**
     * Constructor.
     */
    public Array(Object arrayJS)
    {
        super(arrayJS);
    }

    /**
     * Constructor.
     */
    public Array(Object[] theObjects)
    {
        super(WebEnv.get().newArrayJSForLength(theObjects.length));
        for (int i = 0; i < theObjects.length; i++)
            set(i, theObjects[i]);
    }

    /**
     * The length of the array.
     */
    public int getLength()  { return getMemberInt("length"); }

    /**
     * Returns value at given index.
     */
    public E get(int index)  { return (E) getSlot(index); }

    /**
     * Sets the given value at given index.
     */
    public void set(int index, Object aValue)
    {
        Object value = aValue;
        if (aValue instanceof JSProxy)
            value = ((JSProxy) aValue)._jsObj;
        setSlot(index, value);
    }

    /**
     * Returns a list for this array.
     */
    public List<E> toList()
    {
        int length = getLength();
        List<E> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++)
            list.add(get(i));
        return list;
    }

    /**
     * Returns array of objects as given class.
     */
    public <T> T[] toArray(T[] anArray)
    {
        int length = getLength();
        for (int i = 0; i < length; i++)
            anArray[i] = (T) get(i);
        return anArray;
    }

    /**
     * Returns array of objects as given class.
     */
    public <T> T[] toArray(Class<T> aClass)
    {
        int length = getLength();
        T[] array = (T[]) java.lang.reflect.Array.newInstance(aClass, length);
        return toArray(array);
    }
}
