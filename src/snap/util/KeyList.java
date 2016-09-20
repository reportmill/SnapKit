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
    Object           _object;

    // The key
    String           _key;
    
    // An optional key to allow de-referencing of object attributes
    String           _subkey;
    
    // The count method
    Method           _sizeMethod;
    
    // The get method
    Method           _getMethod;
    
    // The direct list of objects derived from plural of key (fallback for size & get method if missing)
    List             _list;
    
/**
 * Creates a new key list. 
 */
public KeyList(Object anObj, String aKey)  { this(anObj, aKey, null); }

/**
 * Creates a new key list. 
 */
public KeyList(Object anObj, String aKey, String aSubkey)
{
    // Set object
    _object = anObj;
    
    // If no object, panic
    if(_object==null)
        throw new InvalidKeyListException("Key-list object is null");
    
    // Set key (without "get" prefix if it's there)
    _key = aKey;
    if(_key.startsWith("get"))
        _key = _key.substring(3);
    
    // If no key, panic
    if(_key==null)
        throw new InvalidKeyListException("Key-list key is null");
    
    // Get object class
    Class objClass = anObj.getClass();
    
    // Special case for class methods
    if(anObj instanceof Class) {
        _object = null;
        objClass = (Class)anObj;
    }
    
    // Get size method with getKeyCount
    try { _sizeMethod = objClass.getMethod("get" + _key + "Count"); }
    catch(NoSuchMethodException e) { }
    catch(SecurityException e) { }
    
    // If not found, try again with getKeySize
    if(_sizeMethod==null)
        try { _sizeMethod = objClass.getMethod("get" + _key + "Size"); }
        catch(NoSuchMethodException e) { }
        catch(SecurityException e) { }
        
    // If size method is null, try to find list directly
    if(_sizeMethod==null) {
        try { _list = (List)anObj.getClass().getMethod("get" + _key + "s").invoke(anObj); }
        catch(InvocationTargetException e) { throw new RuntimeException(e); }
        catch(Exception e) { }
    }

    // If still not found, panic
    if(_sizeMethod==null && _list==null)
        throw new InvalidKeyListException("Couldn't find get" + _key + "Size/Count method");
    
    // Get get method
    try { _getMethod = objClass.getMethod("get" + _key, int.class); }
    catch(NoSuchMethodException e) { }
    catch(SecurityException e) { }
    
    // If still not found, panic
    if(_getMethod==null && _list==null)
        throw new InvalidKeyListException("Couldn't find get" + _key + " method");
    
    // Set subkey
    _subkey = aSubkey;
}

/**
 * Returns the number of objects for key-list key.
 */
public int size()
{
    // If size method is available, evaluate
    if(_sizeMethod!=null)
        try { return ((Number)_sizeMethod.invoke(_object, new Object[0])).intValue(); }
        catch(Exception e) { throw new InvalidKeyListException(_sizeMethod.getName() + ": " +
            e.getClass().getSimpleName() + " " + e.getCause().getMessage()); }
    
    // Otherwise return list size (shouldn't be possible to be null)
    return _list.size();
}

/**
 * Returns the list element at the given index.
 */
public Object get(int anIndex)
{
    // Declare variable for object
    Object object = null;
    
    // If get method is available, evaluate
    if(_getMethod!=null)
        try { object = _getMethod.invoke(_object, anIndex); }
        catch(Exception e) { throw new InvalidKeyListException(_getMethod.getName() + ": " +
            e.getClass().getSimpleName() + " " + e.getCause().getMessage()); }
        
    // Otherwise get list object (shouldn't be possible to be null)
    else object = _list.get(anIndex);
    
    // If subkey, try to evaluate
    if(_subkey!=null)
        object = Key.getValue(object, _subkey);
    
    // Return object
    return object;
}

/**
 * Resets the object at the given index to given object.
 */
public Object set(int anIndex, Object anObj)
{
    // Get object class (or Object.class, if null)
    Class objClass = anObj!=null? anObj.getClass() : Object.class;
    
    // Try to find set method (catch and ignore acceptable exceptions)
    for(Class oclass=objClass; oclass!=null; oclass=oclass.getSuperclass()) try {
        Method set = _object.getClass().getMethod("set" + _key, int.class, oclass);
        return set.invoke(_object, anIndex, anObj);
    }
    
    // Catch exceptions
    catch(NoSuchMethodException e) { }
    catch(SecurityException e) { }
    catch(InvocationTargetException e) { throw new InvalidKeyListException(e.getMessage(), e); }
    catch(IllegalAccessException e) { throw new InvalidKeyListException(e.getMessage(), e); }
    
    // If not found, try args in other order
    for(Class oclass=objClass; oclass!=null; oclass=oclass.getSuperclass()) try {
        Method set = _object.getClass().getMethod("set" + _key, oclass, int.class);
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
    for(Class oclass=objClass; oclass!=null; oclass=oclass.getSuperclass()) try {
        Method add = _object.getClass().getMethod("add" + _key, int.class, oclass);
        add.invoke(_object, anIndex, anObj);
        return;
    }
    
    // Catch exceptions
    catch(NoSuchMethodException e) { }
    catch(SecurityException e) { }
    catch(InvocationTargetException e) { throw new InvalidKeyListException(e.getMessage(), e); }
    catch(IllegalAccessException e) { throw new InvalidKeyListException(e.getMessage(), e); }
    
    // If not found, try args in other order
    for(Class oclass=objClass; oclass!=null; oclass=oclass.getSuperclass()) try {
        Method add = _object.getClass().getMethod("add" + _key, oclass, int.class);
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
        Method remove = _object.getClass().getMethod("remove" + _key, int.class);
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
 * Creates a new object for this list.
 */
public Object create()
{
    // Try to find create method (catch and ignore acceptable exceptions)
    try {
        Method create = _object.getClass().getMethod("create" + _key);
        return create.invoke(_object, (Object[])null);
    }
    catch(NoSuchMethodException e) { }
    catch(SecurityException e) { }
    catch(InvocationTargetException e) { throw new InvalidKeyListException(e.getMessage(), e); }
    catch(IllegalAccessException e) { throw new InvalidKeyListException(e.getMessage(), e); }
    
    // If method not found, throw UnsupportedOperationException    
    throw new UnsupportedOperationException("No " + _object.getClass() + "." + "create" + _key + "() method found.");
}

/**
 * An exception class which is thrown when creating a new key-list with invalid object key.
 */
public static class InvalidKeyListException extends RuntimeException {
     public InvalidKeyListException(String aMessage) { super(aMessage); }
     public InvalidKeyListException(String aMessage, Throwable aCause) { super(aMessage, aCause); }
}

}