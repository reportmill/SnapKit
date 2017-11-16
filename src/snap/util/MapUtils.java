/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;

/**
 * Utility methods for use with Java.util.Map.
 */
public class MapUtils {
  
/**
 * Creates a new map for given key and object.
 */
public static Map newMap(Object aKey, Object anObj)
{
    Map map = new HashMap(1);
    map.put(aKey, anObj);
    return map;
}

/**
 * Creates a new map with given args in key/value sequence.
 */
public static Map newMap(Object ... theKeyValues)
{
    // Get map size and complain if not even
    int size = theKeyValues.length/2;
    if(size*2!=theKeyValues.length)
        System.err.println("MapUtils.newMap: Unbalanced key/values provided");
    Map map = new HashMap(size);
    for(int i=0; i<size; i++)
        map.put(theKeyValues[i*2], theKeyValues[i*2+1]);
    return map;
}

/**
 * Returns the size of given map (accepts null map).
 */
public static int size(Map aMap)  { return aMap==null? 0 : aMap.size(); }

/**
 * Returns value for aKey in given map (accepts null map).
 */
public static <K,V> V get(Map<K,V> aMap, K aKey)  { return get(aMap, aKey, null); }

/**
 * Returns value for aKey in given map with an optional default value for missing keys.
 */
public static <K,V> V get(Map<K,V> aMap, K aKey, V defaultValue)
{
    V obj = aMap!=null && aKey!=null? aMap.get(aKey) : null;
    return obj!=null? obj : defaultValue;
}

/**
 * Adds given key and value to given map (removes key if value is null).
 */
public static <K,V> V put(Map<K,V> aMap, K aKey, V aValue)
{
    if(aValue==null)
        return aMap.remove(aKey);
    return aMap.put(aKey, aValue);
}

/**
 * Clones a map.
 */
public static Map clone(Map aMap)
{
    if(aMap instanceof HashMap)
        return (Map)((HashMap)aMap).clone();
    if(aMap instanceof Hashtable)
        return (Map)((Hashtable)aMap).clone();
    return aMap==null? null : new HashMap(aMap);
}

/**
 * Returns a key value interpreted as a boolean.
 */
public static <K,V> boolean boolValue(Map<K,V> aMap, K aKey)  { return boolValue(aMap, aKey, false); }

/**
 * Returns a key value interpreted as a boolean (with optional default value).
 */
public static <K,V> boolean boolValue(Map<K,V> aMap, K aKey, boolean defaultValue)
{
    if(aMap==null) return defaultValue;
    Object obj = aMap.get(aKey);
    if(obj instanceof Boolean)
        return ((Boolean)obj).booleanValue();
    return defaultValue;
}

/**
 * Returns a key value interpreted as an int.
 */
public static <K,V> int intValue(Map<K,V> aMap, K aKey)  { return intValue(aMap, aKey, 0); }

/**
 * Returns a key value interpreted as an int (with optional default value).
 */
public static <K,V>int intValue(Map<K,V> aMap, K aKey, int defaultValue)
{
    if(aMap==null) return defaultValue;
    Object obj = aMap.get(aKey);
    if(obj instanceof Number)
        return ((Number)obj).intValue();
    return defaultValue;

}

/**
 * Returns a key value interpreted as a float.
 */
public static <K,V> float floatValue(Map<K,V> aMap, K aKey)  { return floatValue(aMap, aKey, 0); }

/**
 * Returns a key value interpreted as a float (with optional default value).
 */
public static <K,V> float floatValue(Map<K,V> aMap, K aKey, float defaultValue)
{
    if(aMap==null) return defaultValue;
    Object obj = aMap.get(aKey);
    if(obj instanceof Number)
        return ((Number)obj).floatValue();
    return defaultValue;

}

/**
 * Same as putAll, but only adds absent keys (option to copy if there are absent keys).
 */
public static Map putAllIfAbsent(Map m1, Map m2, boolean copyIfAbsent)
{
    Map m = m1;
    for(Iterator i=m2.keySet().iterator(); i.hasNext();) {
        Object key = i.next();
        if(m.get(key)==null) { // If absent...
            if(copyIfAbsent) { // Clone if adding first absent key
                m = SnapUtils.clone(m1);
                copyIfAbsent = false;
            }
            m.put(key, m2.get(key)); // Add key
        }
    }
    
    return m;
}

/**
 * Returns the key for a given object in the given map.
 */
public static Object getKey(Map aMap, Object aValue)
{
    for(Iterator i=aMap.keySet().iterator(); i.hasNext();) {
       Object key = i.next();
       if(aMap.get(key).equals(aValue))
            return key;
    }
    
    return null;
}

/**
 * Returns the key for a given identical object in the given map.
 */
public static Object getKeyId(Map aMap, Object aValue)
{
    for(Iterator i=aMap.keySet().iterator(); i.hasNext();) {
       Object key = i.next();
       if(aMap.get(key)==aValue)
            return key;
    }
    
    return null;
}

/**
 * Returns any element from a map.
 */
public static Object anyElement(Map aMap)
{
    Iterator i = aMap.values().iterator();
    return i.hasNext()? i.next() : null;
}

/**
 * Returns a string representation of map that doesn't recurse.
 */
public static String toStringSafe(Map aMap)
{
    // Get string buffer with map class and open bracket
    StringBuffer sb = new StringBuffer(aMap.getClass().getSimpleName() + " { ");
    
    // Iterate over entries and append "key:value, " (convert non-core values to hashcode)
    for(Map.Entry entry : (Set<Map.Entry>)aMap.entrySet()) { Object key = entry.getKey(), val = entry.getValue();
        if(!(val instanceof String) && !(val instanceof Number) && !(val instanceof Date))
            val = System.identityHashCode(val);
        sb.append(key).append(':').append(val).append(", ");
    }
    
    // Strip last delimiter and add close bracket
    if(!aMap.isEmpty()) sb.delete(sb.length()-2, sb.length());
    sb.append(" }");
    
    // Return string
    return sb.toString();
}
  
}