/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;

/**
 * An archiver to read/write objects from/to JSON.
 */
public class JSArchiver {

    // The list of imports
    List <String>        _imports = new ArrayList();
    
    // The map of previously encountered class strings and their evaluated classpaths
    Map <String,Object>  _classPaths = new HashMap();
    
    // Objects that have been processed
    List                 _objects = new ArrayList();
    
    // The String used to indicate the Class attribute of JSON node
    String               _classId = "Class";
    
    // The string used to indicate a reference to a previously read/written object
    String               _jsonRefString = "JSON-Ref:";

    // The string used to indicate a date value embedded in a String node
    String               _jsonDateString = "JSON-Date:";

    /**
     * Creates a new archiver.
     */
    public JSArchiver()
    {
        addImport("java.util.*", "snap.web.*", "snap.websites.*");
    }

    /**
     * Creates a new archiver for given import(s).
     */
    public JSArchiver(String ... theImports)
    {
        addImport(theImports);
    }

    /**
     * Returns the object to be loaded into (null by default).
     */
    public Object getRootObject()  { return _rootObj; }

    /**
     * Sets the object to be loaded into (null by default).
     */
    public JSArchiver setRootObject(Object anObj)  { _rootObj = anObj; return this; } Object _rootObj;

    /**
     * Returns the class string.
     */
    public String getClassId()  { return _classId; }

    /**
     * Read an object from JSON.
     */
    public Object readSource(Object aSource)
    {
        JSValue node = aSource instanceof JSValue ? (JSValue)aSource :
            new JSParser().readSource(aSource);
        return readNode(node);
    }

    /**
     * Read an object from JSON.
     */
    public Object readString(String aString)
    {
        JSValue node = new JSParser().readString(aString);
        return readNode(node);
    }

    /**
     * Read an object from JSON.
     */
    public Object readNode(JSValue aNode)
    {
        // Handle Object
        if (aNode instanceof JSObject) {

            // Get class
            JSObject objectJS = (JSObject) aNode;
            JSValue classNode = objectJS.getValue(getClassId());
            String className = classNode != null ? classNode.getValueAsString() : null;
            Class classs = className != null ? getClassForName(className) : null;

            // Create object
            Object intoObj = _rootObj; _rootObj = null;
            Object object = intoObj != null ? intoObj : classs != null ? ClassUtils.newInstance(classs) : null;
            if (object == null)
                object = new HashMap();

            // Add to objects
            if (!(object instanceof Date))
                _objects.add(object);

            // Get values
            Map<String, JSValue> keyValues = objectJS.getKeyValues();
            for (Map.Entry<String, JSValue> entry : keyValues.entrySet()) {
                String key = entry.getKey();
                JSValue valueJS = entry.getValue();
                if (!key.equals(getClassId())) {
                    Object value = readNode(valueJS);
                    setValue(object, key, value);
                }
            }

            // Return object
            return object;
        }

            // Handle Array: Create list, get values and return list
        if (aNode instanceof JSArray) {

            // Get array
            JSArray arrayJS = (JSArray) aNode;
            int count = arrayJS.getValueCount();
            List list = new ArrayList();

            // Iterate over nodes, read and add to new list
            for (int i = 0; i < count; i++) {
                JSValue itemJS = arrayJS.getValue(i);
                Object item = readNode(itemJS);
                list.add(item);
            }

            // Return
            return list;
        }

        Object value = aNode.getValue();

        // Handle String JSON Reference
        if (value instanceof String) {
            String string = (String) value;
            if (string.startsWith(_jsonRefString)) {
                int index = Convert.intValue(string.substring(_jsonRefString.length()));
                return _objects.get(index);
            }
            if (string.startsWith(_jsonDateString)) {
                long time = Convert.longValue(string.substring(_jsonDateString.length()));
                return new Date(time);
            }
        }

        // Handle String, Number, Boolean, Null
        return value;
    }

    /**
     * Write an object to JSON.
     */
    public JSValue writeObject(Object anObj)
    {
        // If POJO or Map, add to read objects list
        if (anObj instanceof GetKeys || anObj instanceof Map) {

            // If already read, return ref
            int index = ListUtils.indexOfId(_objects, anObj);
            if (index >= 0)
                return new JSValue(_jsonRefString + index);

            // Otherwise add to ref
            _objects.add(anObj);
        }

        // Handle GetJSONKeys Object
        if (anObj instanceof GetKeys) {

            // Get object and objectJS
            GetKeys getKeys = (GetKeys) anObj;
            JSObject objectJS = new JSObject();

            // Put class
            String classPath = getClassPath(anObj);
            if (!classPath.equals("HashMap")) {
                String classId = getClassId();
                objectJS.setNativeValue(classId, classPath);
            }

            // Iterate over keys and put values
            for (String key : getKeys.getJSONKeys()) {

                // Get value and default value
                Object value = getValue(anObj, key);
                Object dvalue = getValue(anObj, key + "Default");
                if (value instanceof Boolean && dvalue == null)
                    dvalue = Boolean.FALSE;

                // If not default value, write and add
                if (!Objects.equals(value, dvalue)) {
                    JSValue valueJS = writeObject(value);
                    objectJS.setValue(key, valueJS);
                }
            }

            // Return
            return objectJS;
        }

        // Handle Map - iterate over keys and put value
        if (anObj instanceof Map) {

            // Get map and mapJS
            Map<String,Object> map = (Map) anObj;
            JSObject mapJS = new JSObject();

            // Iterate over keys, write and add for each
            for (String key : map.keySet()) {
                Object value = getValue(map, key);
                JSValue valueJS = writeObject(value);
                mapJS.setValue(key, valueJS);
            }

            // Return
            return mapJS;
        }

        // Handle List - iterate over items and add value
        if (anObj instanceof List) {

            // Get array and arrayJS
            List list = (List) anObj;
            JSArray arrayJS = new JSArray();

            // Iterate over items, write and add for each
            for (Object item : list) {
                JSValue itemJS = writeObject(item);
                arrayJS.addValue(itemJS);
            }

            // Return
            return arrayJS;
        }

        // Handle String, Enum, Number, Boolean, Null
        if (anObj instanceof String || anObj instanceof Enum || anObj instanceof Number || anObj instanceof Boolean || anObj == null)
            return new JSValue(anObj);

        // Handle Date (was: aNode.addValue(getClassId(),getClassPath(anObj));aNode.addValue("Time",date.getTime()); )
        if (anObj instanceof Date) {
            Date date = (Date)anObj;
            return new JSValue(_jsonDateString + date.getTime());
        }

        // Otherwise complain
        System.err.println("JSONArchiver.writeObject: Can't write object of class: " + anObj.getClass().getName());
        return new JSValue();
    }

    /**
     * Returns a value for a key.
     */
    protected Object getValue(Object anObj, String aKey)
    {
        if (anObj instanceof GetValue)
            return ((GetValue)anObj).getJSONValue(aKey);
        if (anObj instanceof Map)
            return ((Map)anObj).get(aKey);
        return Key.getValue(anObj, aKey);
    }

    /**
     * Set a value for a key.
     */
    protected void setValue(Object anObj, String aKey, Object aValue)
    {
        if (anObj instanceof SetValue)
            ((SetValue)anObj).setJSONValue(aKey, aValue);
        else if (anObj instanceof Map)
            ((Map)anObj).put(aKey, aValue);
        else Key.setValueSafe(anObj, aKey, aValue);
    }

    /**
     * Adds an import.
     */
    public JSArchiver addImport(String ... theImports)
    {
        for (String imp : theImports)
            _imports.add(0, imp);
        return this;
    }

    /**
     * Returns a class path for an object (shortend if in imports).
     */
    protected String getClassPath(Object anObj)
    {
        // Get basic class path and check for cached version
        String classPath = anObj instanceof GetClass ? ((GetClass)anObj).getJSONClass() : anObj.getClass().getName();
        String classPath2 = (String)_classPaths.get(classPath);

        // If not found, do real work and cache
        if (classPath2==null)
            _classPaths.put(classPath, classPath2 = getClassPathImpl(classPath));

        // Return class path
        return classPath2;
    }

    /**
     * Returns a class path for an object (shortend if in imports).
     */
    protected String getClassPathImpl(String classPath)
    {
        // Check for cached version and return if found
        String classPathCached = classPath;

        // Get simple name and prefix
        String classSimpleName = null, classSimplePrefix = null;
        int index = classPath.lastIndexOf('.');
        if (index>0) {
            classSimpleName = classPath.substring(index+1);
            classSimplePrefix = classPath.substring(0, index);
        }

        // Get simple name inner and prefix
        String classInnerName = null, classInnerPrefix = null;
        index = classPath.lastIndexOf('$');
        if (index>0) {
            classInnerName = classPath.substring(index+1);
            classInnerPrefix = classPath.substring(0, index);
        }

        // Iterate over imports
        for (String imp : _imports) {

            // If inner class simple name has import, just use class inner name
            if (classInnerName!=null && imp.startsWith(classInnerPrefix) &&
                (imp.substring(classInnerPrefix.length()+1).equals("*") ||
                imp.substring(classInnerPrefix.length()+1).equals(classInnerName))) {
                classPathCached = classInnerName;
                break;
            }

            // If class simple name has import, just use class simple name
            if (classSimpleName!=null && imp.startsWith(classSimplePrefix) &&
                (imp.substring(classSimplePrefix.length()+1).equals("*") ||
                imp.substring(classSimplePrefix.length()+1).equals(classSimpleName))) {
                classPathCached = classSimpleName;
                break;
            }
        }

        // Return class path
        return classPathCached;
    }

    /**
     * Returns a class for name.
     */
    protected Class getClassForName(String aClassName)
    {
        // If class name in cache, return class
        Class classs = (Class)_classPaths.get(aClassName);
        if (classs!=null)
            return classs;

        // Otherwise, get class by doing real lookup
        classs = getClassForNameImpl(aClassName);
        if (classs!=null)
            _classPaths.put(aClassName, classs);

        // If class not found, complain
        else System.err.println("RMJSONArchiver: Class not found: " + aClassName);

        // Return class
        return classs;
    }

    /**
     * Returns a class for name.
     */
    protected Class getClassForNameImpl(String aClassName)
    {
        // If has class path, try to just find class
        Class classs = aClassName.contains(".") ? getClassForNameReal(aClassName) : null;

        // Iterate over imports
        if (classs==null)
        for (String imp : _imports) {

            // If import ends with .ClassName, try it and break if found
            if (imp.endsWith("." + aClassName) || imp.endsWith("$" + aClassName)) {
                classs = getClassForNameReal(imp);
                if (classs!=null) break;
            }

            // If import ends with
            else if (imp.endsWith(".*")) {
                classs = getClassForNameReal(imp.substring(0, imp.length()-1) + aClassName);
                if (classs==null)
                    classs = getClassForNameReal(imp.substring(0, imp.length()-2) + "$" + aClassName);
                if (classs!=null) break;
            }
        }

        // If still null, just try it
        if (classs==null)
            classs = getClassForNameReal(aClassName);

        // Return class
        return classs;
    }

    /**
     * Returns a class for name.
     */
    private static Class getClassForNameReal(String aClassName)
    {
        ClassLoader classLoader = JSArchiver.class.getClassLoader();
        try { return Class.forName(aClassName, false, classLoader); }
        catch(ClassNotFoundException e) { return null; }
        catch(NoClassDefFoundError t) { System.err.println("JSArchiver.getClassForNameReal: " + t); return null; }
        catch(Throwable t) { System.err.println("JSArchiver.getClassForNameReal: " + t); return null; }
    }

    /**
     * An interface so objects can provide archival attributes to archiver.
     */
    public interface GetKeys {

        /** Returns a list of keys to be uses to retrieve persistent attributes from an object. */
        Collection <String> getJSONKeys();
    }

    /**
     * An interface so objects can provide archival values to archiver.
     */
    public interface GetValue {

        /** Returns a list of keys to be uses to retrieve persistent attributes from an object. */
        Object getJSONValue(String aKey);
    }

    /**
     * An interface so objects can set archival values from archiver.
     */
    public interface SetValue {

        /** Returns a list of keys to be uses to retrieve persistent attributes from an object. */
        void setJSONValue(String aKey, Object aValue);
    }

    /**
     * An interface so objects can provide archival class to archiver.
     */
    public interface GetClass {

        /** Returns a list of keys to be uses to retrieve persistent attributes from an object. */
        String getJSONClass();
    }
}