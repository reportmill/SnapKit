/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.props.PropObject;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * KeyAccessor - package class for actually getting/setting values for a given object (class) and key.
 */
class KeyAccessor {

    // The object class this accessor works with
    private Class<?>  _class;

    // The base key (no prefix, starts with upper case)
    private String  _key;

    // The raw key first passed in
    private String  _rawKey;

    // Getter type: Map(0), ValueForKey(1), Field(2), Method(3)
    protected Type  _type;

    // The method, if type is Method
    private Method  _getMethod;

    // Args for ValueForKey or Method
    private Object[]  _getMethodArgs;

    // The method, if type is Method
    private Method  _setMethod;

    // Whether set method is set
    private boolean  _setMethodSet;

    // The field, if type is Field
    private Field  _field;

    // Whether to allow fields
    protected static boolean  _allowFields;

    // Some constants
    public enum Type { Map, PropObject, Method, Field, Enum, Unknown }

    /**
     * Creates a new getter for given object and key (caches type).
     */
    public KeyAccessor(Object anObj, String aKey)
    {
        // Get object class
        _class = anObj instanceof Class ? (Class<?>) anObj : anObj.getClass();

        // Get the raw key and standardized key (attribute name)
        _rawKey = aKey;
        _key = Key.getStandard(aKey);

        // If object is Map, return value for key
        if (anObj instanceof Map) {
            _type = Type.Map;
            return;
        }

        // Handle PropObject
        if (anObj instanceof PropObject) {
            _type = Type.PropObject;
            return;
        }

        // If object is Enum,
        if (anObj instanceof Enum) {
            _type = Type.Enum;
            return;
        }

        // Try to find get method
        if (getGetMethod() != null) {
            _type = Type.Method;
            return;
        }

        // See if object has field (ivar)
        if (_allowFields) {
            try {
                _field = _class.getField(_rawKey);
                _type = Type.Field;
                return;
            }
            catch (Exception e) { }
        }

        // Since nothing else panned out, set type to Unknown
        _type = Type.Unknown;
    }

    /**
     * This method actually retrieves a value for an object and a key.
     */
    public Object get(Object anObj)
    {
        // Handle different getter types
        switch (_type) {

            // Handle TYPE_MAP
            case Map: {
                Object value = ((Map<?, ?>) anObj).get(_rawKey);
                if (value == null && _rawKey != _key)
                    value = ((Map<?, ?>) anObj).get(_key);
                return value;
            }

            // Handle PropObject
            case PropObject: return ((PropObject) anObj).getPropValue(_rawKey);

            // Handle TYPE_METHOD
            case Method:

                // Invoke method
                try {
                    return _getMethod.invoke(anObj, _getMethodArgs);
                }

                // Catch InvocationTargetException and complain
                catch (InvocationTargetException e) {
                    String methodName = _getMethod.getName();
                    String className = anObj.getClass().getName();
                    System.err.println("KeyAccessor.get: ITException for key " + methodName + " and class " + className);
                    e.getCause().printStackTrace();
                }

                // Catch IllegalAccessException and complain
                catch (IllegalAccessException e) {
                    String methodName = _getMethod.getName();
                    String className = anObj.getClass().getName();
                    System.err.println("KeyAccessor.get: IAException for key " + methodName + " and class " + className);
                    e.printStackTrace();
                }

                // Catch anything else
                catch (Throwable e) {
                    String methodName = _getMethod.getName();
                    if (methodName.equals("valueForKey")) return null;
                    String className = anObj.getClass().getName();
                    System.err.println("KeyAccessor.get: Exception for key " + methodName + " and class " + className);
                    e.printStackTrace();
                    return null;
                }

            // Handle TYPE_FIELD
            case Field:
                try {
                    return _field.get(anObj);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                    return null;
                }

                // Handle TYPE_ENUM
            case Enum: {
                Enum<?> enum1 = (Enum<?>) anObj;
                if (_rawKey.equals("ordinal"))
                    return enum1.ordinal();
                if (_rawKey.equals("name"))
                    return enum1.name();
                if (_rawKey.equals("toString"))
                    return enum1.toString();
                Enum<?> enum2 = null;
                try {
                    enum2 = Enum.valueOf(enum1.getClass(), _rawKey);
                }
                catch (Exception e) { }
                return enum1.equals(enum2);
            }

            // Handle TYPE_UNKNOWN
            default: //System.err.println("Key: Key not found: " + _key + " for class " + _class.getSimpleName());
                return null;
        }
    }

    /**
     * Searches for a get method for given key then caches and returns result.
     */
    public Method getGetMethod()
    {
        // If get method already found, just return it
        if (_getMethod != null) return _getMethod;

        // Look for "get" or "is" method
        _getMethod = getMethod(_class, "get" + _key);
        if (_getMethod == null)
            _getMethod = getMethod(_class, "is" + _key);

        // Look for method as given by raw key
        if (_getMethod == null)
            _getMethod = getMethod(_class, _rawKey);

        // If method was found, set type to Method (3) and return
        if (_getMethod != null) {
            _getMethodArgs = new Object[0];
            return _getMethod;
        }

        // See if object responds to valueForKey(String)
        _getMethod = getMethod(_class, "valueForKey", String.class);
        if (_getMethod != null) {
            _getMethodArgs = new Object[]{_rawKey};
            return _getMethod;
        }

        // Return
        return _getMethod;
    }

    /**
     * This method actually retrieves a value for an object and a key.
     */
    public void set(Object anObj, Object aValue) throws Exception
    {
        // Handle PropObject
        if (_type == Type.PropObject) {
            ((PropObject) anObj).setPropValue(_rawKey, aValue);
            return;
        }

        // Get set method
        Method method = getSetMethod();
        if (method == null)
            throw new NoSuchMethodException(_class.getName() + '.' + "set" + _key);
        Class<?> methodClass = method.getParameterTypes()[0];

        // Get Value (if list, use first item)
        Object value = aValue;
        if (value instanceof List && ((List<?>) value).size() > 0)
            value = ((List<?>) value).get(0);

        // Do type conversion for number types
        if (methodClass == int.class || methodClass == Integer.class)
            value = value instanceof Integer ? (Integer) value : Convert.intValue(value);
        else if (methodClass == short.class || methodClass == Short.class)
            value = value instanceof Short ? (Short) value : (short) Convert.intValue(value);
        else if (methodClass == long.class || methodClass == Long.class)
            value = value instanceof Long ? (Long) value : Convert.longValue(value);
        else if (methodClass == float.class || methodClass == Float.class)
            value = value instanceof Float ? (Float) value : Convert.floatValue(value);
        else if (methodClass == double.class || methodClass == Double.class)
            value = value instanceof Double ? (Double) value : Convert.doubleValue(value);
        else if (methodClass == boolean.class || methodClass == Boolean.class)
            value = value instanceof Boolean ? (Boolean) value : Convert.boolValue(value);

            // Do type conversion on core object types: String, Number, Date
        else if (methodClass == String.class)
            value = Convert.stringValue(value);
        else if (Number.class.isAssignableFrom(methodClass))
            value = Convert.numberValue(value);
        else if (Date.class.isAssignableFrom(methodClass))
            value = Convert.getDate(value);

            // Enum conversion
        else if (Enum.class.isAssignableFrom(methodClass) && value instanceof String) {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) methodClass;
            value = Enum.valueOf(enumClass, (String) value);
        }

        // Bogus file conversion stuff
        else if (methodClass == File.class && value instanceof String)
            value = new File((String) value);

            // If method takes list, go back to using original value
        else if (List.class.isAssignableFrom(methodClass))
            value = aValue;

        // Invoke set method
        method.invoke(anObj, value);
    }

    /**
     * Searches for a set method for given key then caches and returns result.
     */
    public Method getSetMethod() throws Exception
    {
        // If set method already set, just return it
        if (_setMethodSet)
            return _setMethod;

        // Get key with "set" prefix
        String key = "set" + _key;

        // Get get method
        Method getMethod = getGetMethod();
        if (getMethod == null)
            throw new NoSuchMethodException(_class.getName() + '.' + "get" + _key);

        // Get arg class
        Class<?> argClass = getMethod.getReturnType();

        // Try to get a method of the same name, with no arguments
        _setMethodSet = true;
        return _setMethod = getMethod(_class, key, argClass);
    }

    /**
     * Class.getMethod wrapper to isolate call to one place.
     */
    private static Method getMethod(Class<?> aClass, String aName, Class<?>... theClasses)
    {
        try { return ClassUtils.getMethodOrThrow(aClass, aName, theClasses); }
        catch (NoSuchMethodException e) {
            return null;
        }
    }
}
