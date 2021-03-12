/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.lang.reflect.*;
import java.util.*;

/**
 * A list implementation that tries to get objects from aKey+"Size" or aKey+"Count" and aKey(index),
 * eg., getThingCount(), getThing(index).
 */
public class KeyList extends AbstractList {

    // The object
    private Object  _object;

    // The key
    private String  _key;
    
    // The count method
    private Method  _sizeMethod;
    
    // The get method
    private Method  _getMethod;
    
    /**
     * Creates a KeyList.
     */
    public KeyList(Object anObj, String aKey)
    {
        // Set object
        _object = anObj;

        // If no object, panic
        if (_object==null)
            throw new InvalidKeyListException("Key-list object is null");

        // Set key (without "get" prefix if it's there)
        _key = aKey;
        if (_key.startsWith("get"))
            _key = _key.substring(3);

        // If no key, panic
        if (_key==null)
            throw new InvalidKeyListException("Key-list key is null");

        // Get object class
        Class objClass = anObj.getClass();

        // Special case for class methods
        if (anObj instanceof Class) {
            _object = null;
            objClass = (Class)anObj;
        }

        // Get size method with getKeyCount
        try { _sizeMethod = getMethodOrThrow(objClass, "get" + _key + "Count"); }
        catch(NoSuchMethodException e) { }
        catch(SecurityException e) { }

        // If not found, try again with getKeySize
        if (_sizeMethod==null)
            try { _sizeMethod = getMethodOrThrow(objClass, "get" + _key + "Size"); }
            catch(NoSuchMethodException e) { }
            catch(SecurityException e) { }

        // If still not found, panic
        if (_sizeMethod==null)
            throw new InvalidKeyListException("Couldn't find get" + _key + "Size/Count method");

        // Get get method
        try { _getMethod = getMethodOrThrow(objClass, "get" + _key, int.class); }
        catch(NoSuchMethodException e) { }
        catch(SecurityException e) { }

        // If still not found, panic
        if (_getMethod==null)
            throw new InvalidKeyListException("Couldn't find get" + _key + " method");
    }

    /**
     * Returns the number of objects for key-list key.
     */
    public int size()
    {
        // If size method is available, evaluate
        try { return ((Number)_sizeMethod.invoke(_object)).intValue(); }
        catch(Exception e) { throw new InvalidKeyListException(_sizeMethod.getName() + ": " +
            e.getClass().getSimpleName() + " " + e.getCause().getMessage()); }
    }

    /**
     * Returns the list element at the given index.
     */
    public Object get(int anIndex)
    {
        // Declare variable for object
        Object obj = null;

        // If get method is available, evaluate
        if (_getMethod!=null)
            try { obj = _getMethod.invoke(_object, anIndex); }
            catch(Exception e) { throw new InvalidKeyListException(_getMethod.getName() + ": " +
                e.getClass().getSimpleName() + " " + e.getCause().getMessage()); }

        // Return object
        return obj;
    }

    /**
     * Resets the object at the given index to given object.
     */
    public Object set(int anIndex, Object anObj)
    {
        // Get object class (or Object.class, if null)
        Class objClass = anObj!=null? anObj.getClass() : Object.class;

        // Try to find set method (catch and ignore acceptable exceptions)
        for (Class oclass=objClass; oclass!=null; oclass=oclass.getSuperclass()) try {
            Method set = getMethodOrThrow(_object, "set" + _key, int.class, oclass);
            return set.invoke(_object, anIndex, anObj);
        }

        // Catch exceptions
        catch(NoSuchMethodException e) { }
        catch(SecurityException e) { }
        catch(InvocationTargetException e) { throw new InvalidKeyListException(e.getMessage(), e); }
        catch(IllegalAccessException e) { throw new InvalidKeyListException(e.getMessage(), e); }

        // If not found, try args in other order
        for (Class oclass=objClass; oclass!=null; oclass=oclass.getSuperclass()) try {
            Method set = getMethodOrThrow(_object, "set" + _key, oclass, int.class);
            return set.invoke(_object, anObj, anIndex);
        }

        // Catch exceptions
        catch(NoSuchMethodException e) { }
        catch(SecurityException e) { }
        catch(InvocationTargetException e) { throw new InvalidKeyListException(e.getMessage(), e); }
        catch(IllegalAccessException e) { throw new InvalidKeyListException(e.getMessage(), e); }

        // If method not found, throw UnsupportedOperationException
        throw new UnsupportedOperationException("No " + _object.getClass() + "." + "set" + _key + "() method found.");
    }

    /**
     * Adds the object at the given index.
     */
    public void add(int anIndex, Object anObj)
    {
        // Get object class (or Object.class, if null)
        Class objClass = anObj!=null? anObj.getClass() : Object.class;

        // Try to find add method (catch and ignore acceptable exceptions)
        for (Class oclass=objClass; oclass!=null; oclass=oclass.getSuperclass()) try {
            Method add = getMethodOrThrow(_object, "add" + _key, int.class, oclass);
            add.invoke(_object, anIndex, anObj);
            return;
        }

        // Catch exceptions
        catch(NoSuchMethodException e) { }
        catch(SecurityException e) { }
        catch(InvocationTargetException e) { throw new InvalidKeyListException(e.getMessage(), e); }
        catch(IllegalAccessException e) { throw new InvalidKeyListException(e.getMessage(), e); }

        // If not found, try args in other order
        for (Class oclass=objClass; oclass!=null; oclass=oclass.getSuperclass()) try {
            Method add = getMethodOrThrow(_object, "add" + _key, oclass, int.class);
            add.invoke(_object, anObj, anIndex);
            return;
        }

        // Catch exceptions
        catch(NoSuchMethodException e) { }
        catch(SecurityException e) { }
        catch(InvocationTargetException e) { throw new InvalidKeyListException(e.getMessage(), e); }
        catch(IllegalAccessException e) { throw new InvalidKeyListException(e.getMessage(), e); }

        // If method not found, throw UnsupportedOperationException
        throw new UnsupportedOperationException("No " + _object.getClass() + "." + "add" + _key + "() method found.");
    }

    /**
     * Removes the object at the given index.
     */
    public Object remove(int anIndex)
    {
        // Try to find remove method (catch and ignore acceptable exceptions)
        try {
            Method remove = getMethodOrThrow(_object, "remove" + _key, int.class);
            return remove.invoke(_object, anIndex);
        }
        catch(NoSuchMethodException e) { }
        catch(SecurityException e) { }
        catch(InvocationTargetException e) { throw new InvalidKeyListException(e.getMessage(), e); }
        catch(IllegalAccessException e) { throw new InvalidKeyListException(e.getMessage(), e); }

        // If method not found, throw UnsupportedOperationException
        throw new UnsupportedOperationException("No " + _object.getClass() + "." + "remove" + _key + "() method found.");
    }

    /**
     * Class.getMethod wrapper to isolate call to one place.
     */
    static Method getMethodOrThrow(Object anObj, String aName, Class ... theClasses) throws NoSuchMethodException
    {
        return getMethodOrThrow(anObj.getClass(), aName, theClasses);
    }

    /**
     * Class.getMethod wrapper to isolate call to one place.
     */
    static Method getMethodOrThrow(Class aClass, String aName, Class ... theClasses) throws NoSuchMethodException
    {
        return ClassUtils.getMethodOrThrow(aClass, aName, theClasses);
    }

    /**
     * An exception class which is thrown when creating a new key-list with invalid object key.
     */
    public static class InvalidKeyListException extends RuntimeException {
         public InvalidKeyListException(String aMessage) { super(aMessage); }
         public InvalidKeyListException(String aMessage, Throwable aCause) { super(aMessage, aCause); }
    }

    /**
     * Returns a key list value for given object, key and index.
     */
    public static Object getValue(Object anObj, String aKey, int anIndex)  { return new KeyList(anObj, aKey).get(anIndex); }

    /**
     * Sets a key list value for given object, key and index.
     */
    public static void setValue(Object anObj, String aKey, Object aValue, int anIndex)
    {
        KeyList klist = new KeyList(anObj, aKey);
        if (aValue!=null) klist.add(anIndex, aValue);
        else klist.remove(anIndex);
    }
}