/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.io.File;
import java.lang.reflect.*;
import java.util.*;

/**
 * This class provides an optimized convenience for getting named values from arbitrary objects.
 */
public class Key {

    // A map of getter class maps for encountered classes (each map has an entry with getter for encountered keys)
    private static Map _getterClasses = new Hashtable(10);

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
     * Returns whether given string is a valid key (starts with letter and only contains letters, digits,
     * white space and under bars).
     */
    public static boolean isKey(String aString)
    {
        // If the first character isn't a letter or under bar, return false
        if (aString==null || aString.length()==0 || (!Character.isLetter(aString.charAt(0)) && aString.charAt(0)!='_'))
            return false;

        // If successive chars aren't letters, digits whitespace or under bar, return false
        for (int i=1, iMax=aString.length(); i<iMax; i++) { char c = aString.charAt(i);
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c) && c!='_')
                return false;
        }

        // Return true since tests passed
        return true;
    }

    /**
     * Returns a value for given object and key.
     */
    public static Object getValue(Object anObj, String aKey)
    {
        if (anObj instanceof Get)
            return ((Get)anObj).getKeyValue(aKey);
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

        // Get object in normalized format (just return if null)
        //Object obj = ReportMill.convertFromAppServerType(anObj); if (obj==null) return null;
        Object obj = anObj; if (obj==null) return null;

        // Get accessor for object and aKey and evaluate
        KeyAccessor accessor = getAccessor(obj, aKey);
        Object value = accessor.get(obj);

        // Convert value from appserver type and return
        //value = ReportMill.convertFromAppServerType(value);
        return value;
    }

    /**
     * Sets a value for given object and key and value.
     */
    public static void setValue(Object anObj, String aKey, Object aValue) throws Exception
    {
        // If object is List, set value on objects
        if (anObj instanceof List) { List list = (List)anObj;
            for (Object item : list)
                setValue(item, aKey, aValue);
        }

        // If object is map, just put value
        else if (anObj instanceof Map) { Map map = (Map)anObj;
            if (aValue!=null) map.put(aKey, aValue);
            else map.remove(aKey);
        }

        // If object adheres to GetSet, set value
        else if (anObj instanceof GetSet)
            ((GetSet)anObj).setKeyValue(aKey, aValue);

        // Otherwise use reflection
        else setValueReflect(anObj, aKey, aValue);
    }

    /**
     * Sets a value for given object and key and value.
     */
    public static void setValueReflect(Object anObj, String aKey, Object aValue) throws Exception
    {
        // If object is null, throw NPE
        if (anObj==null) throw new NullPointerException("Key.setValue: trying to set key " + aKey + " on null");

        // Get accessor and set
        KeyAccessor acsr = getAccessor(anObj, aKey);
        acsr.set(anObj, aValue);
    }

    /**
     * Sets the value but only prints a warning if it fails.
     */
    public static void setValueSafe(Object anObj, String aKey, Object aValue)
    {
        try { setValue(anObj, aKey, aValue); }
        catch(Exception e) {
            Class cls = ClassUtils.getClass(anObj);
            String msg = (cls!=null ? cls.getSimpleName() : "null") + ", " + aKey + ", " + aValue;
            System.err.println("Key.setValue (" + msg + ") failed: " + e);
        }
    }

    /**
     * Sets the value but only prints a warning if it fails.
     */
    public static void setValueReflectSafe(Object anObj, String aKey, Object aValue)
    {
        try { setValueReflect(anObj, aKey, aValue); }
        catch(Exception e) {
            Class cls = ClassUtils.getClass(anObj);
            String msg = (cls!=null ? cls.getSimpleName() : "null") + ", " + aKey + ", " + aValue;
            System.err.println("Key.setValue (" + msg + ") failed: " + e);
        }
    }

    /**
     * Tries to set value in given object, ignoring failure exceptions.
     */
    public static void setValueSilent(Object anObj, String aKey, Object aValue)
    {
        try { setValue(anObj, aKey, aValue); }
        catch(Exception e) { }
    }

    /**
     * Returns whether given object has an accessor for given key.
     */
    public static boolean hasKey(Object anObj, String aKey)
    {
        // Get object in normalized format (just return false if null)
        Object obj = anObj; //ReportMill.convertFromAppServerType(anObj);
        if (obj==null)
            return false;

        // Get accessor for object (class) and aKey
        KeyAccessor accessor = getAccessor(obj, aKey);

        // Return whether accessor type is unknown
        return accessor._type == KeyAccessor.Type.Unknown;
    }

    /**
     * Returns an int value for a key.
     */
    public static int getIntValue(Object anObj, String aKey)  { return SnapUtils.intValue(getValue(anObj, aKey)); }

    /**
     * Returns a string value for a key.
     */
    public static String getStringValue(Object anObj, String aKey)  { return SnapUtils.stringValue(getValue(anObj, aKey)); }

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
        Map classMap = (Map)_getterClasses.get(anObj.getClass());
        if (classMap==null)
            _getterClasses.put(anObj.getClass(), classMap = new Hashtable());

        // Get accessor (if null, create and add)
        KeyAccessor accessor = (KeyAccessor)classMap.get(aKey);
        if (accessor==null)
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
        if (name.startsWith("is") && name.length()>2 && Character.isUpperCase(name.charAt(2)))
            name = name.substring(2);

        // Clear "get" prefix
        else if (name.startsWith("get") && name.length()>3 && Character.isUpperCase(name.charAt(3)))
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
     * KeyAccessor - enclosed class for actually getting/setting values for a given object (class) and key.
     */
    public static class KeyAccessor {

        // The object class this accessor works with
        Class     _class;

        // The base key (no prefix, starts with upper case)
        String    _key;

        // The raw key first passed in
        String    _rawKey;

        // Getter type: Map(0), ValueForKey(1), Field(2), Method(3)
        Type      _type;

        // The method, if type is Method
        Method    _getMethod;

        // Args for ValueForKey or Method
        Object    _getMethodArgs[];

        // The method, if type is Method
        Method    _setMethod;

        // The field, if type is Field
        //Field     _field;

        // A bogus method to act as void
        static Method  _nullMethod = getMethod(String.class, "toString");

        // Some constants
        public static enum Type { Map, Methods, Field, Enum, ListKey, Unknown };

        /** Creates a new getter for given object and key (caches type).     */
        public KeyAccessor(Object anObj, String aKey)
        {
            // Get object class
            _class = anObj instanceof Class ? (Class) anObj : anObj.getClass();

            // Get the raw key and standardized key (attribute name)
            _rawKey = aKey;
            _key = getStandard(aKey);

            // If object is Map, return value for key
            if (anObj instanceof Map) {
                _type = Type.Map; return; }

            // If object is Enum,
            if (anObj instanceof Enum) {
                _type = Type.Enum; return; }

            // Try to find get method
            if (getGetMethod()!=null) {
                _type = Type.Methods; return; }

            // See if object has field (ivar)
            //try { _field = anObj.getClass().getField(_rawKey); if (_field!=null) { _type = Type.Field; return; } }
            //catch(Exception e) { }

            // See if key is list key
            //try { new KeyList(anObj, _rawKey); _type = Type.ListKey; return; }
            //catch(KeyList.InvalidKeyListException e) { }

            // Since nothing else panned out, set type to Unknown
            _type = Type.Unknown;
        }

        /** This method actually retrieves a value for an object and a key. */
        public Object get(Object anObj)
        {
            // Handle different getter types
            switch (_type) {

                // Handle TYPE_MAP
                case Map: {
                    Object value = ((Map)anObj).get(_rawKey);
                    if (value==null && _rawKey!=_key)
                        value = ((Map)anObj).get(_key);
                    return value;
                }

                // Handle TYPE_METHODS
                case Methods:

                    // Invoke method
                    try { return _getMethod.invoke(anObj, _getMethodArgs); }

                    // Catch InvocationTargetException and complain
                    catch(InvocationTargetException  e) {
                        String name = _getMethod.getName(), cname = anObj.getClass().getName();
                        System.err.println("Key: ITException evaluating key " + name + " on object of class " + cname);
                        e.getCause().printStackTrace();
                    }

                    // Catch IllegalAccessException and complain
                    catch(IllegalAccessException e) {
                        String name = _getMethod.getName(), cname = anObj.getClass().getName();
                        System.err.println("Key: IAException evaluating key " + name + " on object of class " + cname);
                        e.printStackTrace();
                    }

                    // Catch anything else
                    catch(Throwable e) {
                        String name = _getMethod.getName(); if (name.equals("valueForKey")) return null;
                        String cname = anObj.getClass().getName();
                        System.err.println("Key: Exception evaluating key " + name + " on object of class " + cname);
                        e.printStackTrace();
                        return null;
                    }

                // Handle TYPE_FIELD
                case Field: return null;
                    //try { return _field.get(anObj); } catch(Throwable e) { e.printStackTrace(); return null; }

                // Handle TYPE_ENUM
                case Enum: {
                    Enum enum1 = (Enum)anObj;
                    if (_rawKey.equals("ordinal")) return enum1.ordinal();
                    if (_rawKey.equals("name")) return enum1.name();
                    if (_rawKey.equals("toString")) return enum1.toString();
                    Enum enum2 = null; try { enum2 = Enum.valueOf(enum1.getClass(), _rawKey); } catch(Exception e) { }
                    return enum1.equals(enum2);
                }

                // Handle TYPE_LISTKEY
                //case ListKey: return new KeyList(anObj, _rawKey);

                // Handle TYPE_UNKNOWN
                default: //System.err.println("Key: Key not found: " + _key + " for class " + _class.getSimpleName());
                    return null;
            }
        }

        /** Searches for a get method for given key then caches and returns result. */
        public Method getGetMethod()
        {
            // If get method already found, just return it
            if (_getMethod!=null) return _getMethod;

            // Look for "get" or "is" method
            _getMethod = getMethod(_class, "get" + _key);
            if (_getMethod==null)
                _getMethod = getMethod(_class, "is" + _key);

            // Look for method as given by raw key
            if (_getMethod==null)
                _getMethod = getMethod(_class, _rawKey);

            // If method was found, set type to Method (3) and return
            if (_getMethod!=null) {
                _getMethodArgs = new Object[0];
                return _getMethod;
            }

            // See if object responds to valueForKey(String)
            _getMethod = getMethod(_class, "valueForKey", String.class);
            if (_getMethod!=null) {
                _getMethodArgs = new Object[] { _rawKey };
                return _getMethod;
            }

            // Return _getMethod
            return _getMethod;
        }

        /** This method actually retrieves a value for an object and a key. */
        public void set(Object anObj, Object aValue) throws Exception
        {
            // Get set method
            Method method = getSetMethod();
            if (method==null) throw new NoSuchMethodException(_class.getName() + '.' + "set" + _key);
            Class methodClass = method.getParameterTypes()[0];

            // Get Value (if list, use first item)
            Object value = aValue;
            if (value instanceof List && ((List)value).size()>0)
                value = ((List)value).get(0);

            // Do type conversion for number types
            if (methodClass==int.class || methodClass==Integer.class)
                value = value instanceof Integer ? (Integer) value : SnapUtils.intValue(value);
            else if (methodClass==short.class || methodClass==Short.class)
                value = value instanceof Short ? (Short) value : (short)SnapUtils.intValue(value);
            else if (methodClass==long.class || methodClass==Long.class)
                value = value instanceof Long ? (Long) value : SnapUtils.longValue(value);
            else if (methodClass==float.class || methodClass==float.class)
                value = value instanceof Float ? (Float) value : SnapUtils.floatValue(value);
            else if (methodClass==double.class || methodClass==Double.class)
                value = value instanceof Double ? (Double) value : SnapUtils.doubleValue(value);
            else if (methodClass==boolean.class || methodClass==Boolean.class)
                value = value instanceof Boolean ? (Boolean) value : SnapUtils.boolValue(value);

            // Do type conversion on core object types: String, Number, Date
            else if (methodClass==String.class)
                value = SnapUtils.stringValue(value);
            else if (Number.class.isAssignableFrom(methodClass))
                value = SnapUtils.numberValue(value);
            else if (Date.class.isAssignableFrom(methodClass))
                value = SnapUtils.getDate(value);

            // Enum conversion
            else if (Enum.class.isAssignableFrom(methodClass) && value instanceof String)
                value = Enum.valueOf(methodClass, (String)value);

            // Bogus file conversion stuff
            else if (methodClass==File.class && value instanceof String)
                value = new File((String)value);

            // If method takes list, go back to using original value
            else if (List.class.isAssignableFrom(methodClass))
                value = aValue;

            // Invoke set method
            method.invoke(anObj, value);
        }

        /** Searches for a set method for given key then caches and returns result. */
        public Method getSetMethod() throws Exception
        {
            // If set method already set, just return it
            if (_setMethod!=null)
                return _setMethod != _nullMethod ? _setMethod : null;

            // Get key with "set" prefix
            String key = "set" + _key;

            // Get get method
            Method getMethod = getGetMethod();
            if (getMethod==null)
                throw new NoSuchMethodException(_class.getName() + '.' + "get" + _key);

            // Get arg class
            Class argClass = getMethod.getReturnType();

            // Try to get a method of the same name, with no arguments
            try { return _setMethod = ClassUtils.getMethodOrThrow(_class, key, argClass); }
            catch(Exception e) { _setMethod = _nullMethod; throw e; }
        }
    }

    /** Class.getMethod wrapper to isolate call to one place. */
    private static Method getMethod(Class aClass, String aName, Class ... theClasses)
    {
        try { return ClassUtils.getMethodOrThrow(aClass, aName, theClasses); }
        catch(NoSuchMethodException e) { return null; }
    }
}