/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;

/**
 * This class provides an optimized convenience for getting named values from arbitrary objects.
 */
public class Key {

    // A map of getter class maps for encountered classes (each map has an entry with getter for encountered keys)
    private static Map<Class<?>,Map<String,KeyAccessor>>  _getterClasses = new HashMap<>();

    /**
     * This is interface is implemented by objects that can get key values themselves.
     */
    public interface Get {
        Object getKeyValue(String aKey);
    }

    /**
     * This is interface is implemented by objects that can get/set key value themselves.
     */
    public interface GetSet extends Get {
        void setKeyValue(String aKey, Object aValue);
    }

    /**
     * Returns a value for given object and key.
     */
    public static Object getValue(Object anObj, String aKey)
    {
        if (anObj instanceof Get)
            return ((Get) anObj).getKeyValue(aKey);
        return getValueImpl(anObj, aKey);
    }

    /**
     * Returns a value for given object and key.
     */
    public static Object getValueImpl(Object anObj, String aKey)
    {
        // If key is this, return object
        if ("this".equals(aKey)) return anObj;

        // If key is idHashCode, return system identity hash code
        if ("idHashCode".equals(aKey)) return System.identityHashCode(anObj);

        // If null, just return
        if (anObj == null)
            return null;

        // Get accessor for object and aKey and evaluate
        KeyAccessor accessor = getAccessor(anObj, aKey);
        Object value = accessor.get(anObj);

        // Return
        return value;
    }

    /**
     * Sets a value for given object and key and value.
     */
    public static void setValue(Object anObj, String aKey, Object aValue) throws Exception
    {
        // If object is List, set value on objects
        if (anObj instanceof List) {
            List<?> list = (List<?>) anObj;
            for (Object item : list)
                setValue(item, aKey, aValue);
        }

        // If object is map, just put value
        else if (anObj instanceof Map) {
            Map<String,Object> map = (Map<String,Object>) anObj;
            if (aValue != null)
                map.put(aKey, aValue);
            else map.remove(aKey);
        }

        // If object adheres to GetSet, set value
        else if (anObj instanceof GetSet)
            ((GetSet) anObj).setKeyValue(aKey, aValue);

        // Otherwise use reflection
        else setValueReflect(anObj, aKey, aValue);
    }

    /**
     * Sets a value for given object and key and value.
     */
    public static void setValueReflect(Object anObj, String aKey, Object aValue) throws Exception
    {
        // If object is null, throw NPE
        if (anObj == null)
            throw new NullPointerException("Key.setValue: trying to set key " + aKey + " on null");

        // Get accessor and set
        KeyAccessor acsr = getAccessor(anObj, aKey);
        acsr.set(anObj, aValue);
    }

    /**
     * Sets the value but only prints a warning if it fails.
     */
    public static void setValueSafe(Object anObj, String aKey, Object aValue)
    {
        try {
            setValue(anObj, aKey, aValue);
        }
        catch (Exception e) {
            Class<?> cls = ClassUtils.getClass(anObj);
            String msg = (cls != null ? cls.getSimpleName() : "null") + ", " + aKey + ", " + aValue;
            System.err.println("Key.setValue (" + msg + ") failed: " + e);
        }
    }

    /**
     * Sets the value but only prints a warning if it fails.
     */
    public static void setValueReflectSafe(Object anObj, String aKey, Object aValue)
    {
        try {
            setValueReflect(anObj, aKey, aValue);
        }
        catch (Exception e) {
            Class<?> cls = ClassUtils.getClass(anObj);
            String msg = (cls != null ? cls.getSimpleName() : "null") + ", " + aKey + ", " + aValue;
            System.err.println("Key.setValue (" + msg + ") failed: " + e);
        }
    }

    /**
     * Returns whether given object has an accessor for given key.
     */
    public static boolean hasKey(Object anObj, String aKey)
    {
        // Get object in normalized format (just return false if null)
        Object obj = anObj;
        if (obj == null)
            return false;

        // Get accessor for object (class) and aKey
        KeyAccessor accessor = getAccessor(obj, aKey);

        // Return whether accessor type is unknown
        return accessor._type == KeyAccessor.Type.Unknown;
    }

    /**
     * Returns an int value for a key.
     */
    public static int getIntValue(Object anObj, String aKey)
    {
        return Convert.intValue(getValue(anObj, aKey));
    }

    /**
     * Returns a string value for a key.
     */
    public static String getStringValue(Object anObj, String aKey)
    {
        return Convert.stringValue(getValue(anObj, aKey));
    }

    /**
     * Returns a value as given class, if appropriate.
     */
    public static <T> T getValue(Object anObj, String aKey, Class<T> aClass)
    {
        return ClassUtils.getInstance(getValue(anObj, aKey), aClass);
    }

    /**
     * Returns the accessor object for a given object (class) and key.
     */
    public synchronized static KeyAccessor getAccessor(Object anObj, String aKey)
    {
        // Get accessor class map for object class (if null, create and add)
        Class<?> objClass = anObj.getClass();
        Map<String,KeyAccessor> classMap = _getterClasses.get(objClass);
        if (classMap == null)
            _getterClasses.put(objClass, classMap = new HashMap<>());

        // Get accessor (if null, create and add)
        KeyAccessor accessor = classMap.get(aKey);
        if (accessor == null)
            classMap.put(aKey, accessor = new KeyAccessor(anObj, aKey));

        // Return accessor
        return accessor;
    }

    /**
     * Returns the key in a standard format (strip is/get prefix and start with capital letter).
     */
    public static String getStandard(String aKey)
    {
        // Set name to key
        String name = aKey;

        // Clear "is" prefix
        if (name.startsWith("is") && name.length() > 2 && Character.isUpperCase(name.charAt(2)))
            name = name.substring(2);

            // Clear "get" prefix
        else if (name.startsWith("get") && name.length() > 3 && Character.isUpperCase(name.charAt(3)))
            name = name.substring(3);

        // Make sure first character is upper case
        if (Character.isLowerCase(name.charAt(0)))
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        // Make sure first character isn't number
        if (Character.isDigit(name.charAt(0)))
            name = aKey;

        // Return name
        return name;
    }

    /**
     * Sets whether Key is allowed to try to resolve values using object fields.
     */
    public static void setAllowFields(boolean aValue)  { KeyAccessor._allowFields = aValue; }
}