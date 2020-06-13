/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;

/**
 * An archiver to read/write objects from/to JSON.
 */
public class JSONArchiver {

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
    public JSONArchiver()
    {
        addImport("java.util.*", "snap.web.*", "snap.websites.*");
    }

    /**
     * Creates a new archiver for given import(s).
     */
    public JSONArchiver(String ... theImports)
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
    public JSONArchiver setRootObject(Object anObj)  { _rootObj = anObj; return this; } Object _rootObj;

    /**
     * Returns the class string.
     */
    public String getClassId()  { return _classId; }

    /**
     * Read an object from JSON.
     */
    public Object readSource(Object aSource)
    {
        JSONNode node = aSource instanceof JSONNode ? (JSONNode)aSource :
            new JSONParser().readSource(aSource);
        return readNode(node);
    }

    /**
     * Read an object from JSON.
     */
    public Object readString(String aString)
    {
        JSONNode node = new JSONParser().readString(aString);
        return readNode(node);
    }

    /**
     * Read an object from JSON.
     */
    public Object readNode(JSONNode aNode)
    {
        // Handle Types
        switch (aNode.getType()) {

            // Handle Object
            case Object: {

                // Get class
                JSONNode classNode = aNode.getNode(getClassId());
                String className = classNode!=null ? classNode.getString() : null;
                Class classs = className!=null ? getClassForName(className) : null;

                // Create object
                Object intoObj = _rootObj; _rootObj = null;
                Object object = intoObj!=null ? intoObj : classs!=null ? ClassUtils.newInstance(classs) : null;
                if (object==null)
                    object = new HashMap();

                // Add to objects
                if (!(object instanceof Date))
                    _objects.add(object);

                // Get values
                for (JSONNode child : aNode.getNodes()) { String key = child.getKey();
                    if (!key.equals(getClassId()))
                        setValue(object, key, readNode(aNode.getNode(key))); }

                // Return object
                return object;
            }

            // Handle Array: Create list, get values and return list
            case Array: {
                List list = new ArrayList();
                for (int i=0, iMax=aNode.getNodeCount(); i<iMax; i++)
                    list.add(readNode(aNode.getNode(i)));
                return list;
            }

            // Handle String JSON Reference
            case String:
                if (aNode.getString().startsWith(_jsonRefString)) {
                    int index = SnapUtils.intValue(aNode.getString().substring(_jsonRefString.length()));
                    return _objects.get(index);
                }
                if (aNode.getString().startsWith(_jsonDateString)) {
                    long time = SnapUtils.longValue(aNode.getString().substring(_jsonDateString.length()));
                    return new Date(time);
                }

            // Handle String, Number, Boolean, Null
            default: return aNode.getValue();
        }
    }

    /**
     * Write an object to JSON.
     */
    public JSONNode writeObject(Object anObj)
    {
        // Create node
        JSONNode node = new JSONNode();

        // If POJO or Map, add to read objects list
        if (anObj instanceof GetKeys || anObj instanceof Map) {
            int index = ListUtils.indexOfId(_objects, anObj);
            if (index>=0) {
                node.setValue(_jsonRefString + index); return node; }
            else _objects.add(anObj);
        }

        // Handle GetJSONKeys Object
        if (anObj instanceof GetKeys) { GetKeys getKeys = (GetKeys)anObj;

            // Put class
            String classPath = getClassPath(anObj);
            if (!classPath.equals("HashMap"))
                node.addKeyValue(getClassId(), getClassPath(anObj));

            // Iterate over keys and put values
            for (String key : getKeys.getJSONKeys()) {
                Object value = getValue(anObj, key);
                Object dvalue = getValue(anObj, key + "Default");
                if (value instanceof Boolean && dvalue==null) dvalue = Boolean.FALSE;
                if (!SnapUtils.equals(value, dvalue))
                    node.addKeyValue(key, writeObject(value));
            }
        }

        // Handle Map - iterate over keys and put value
        else if (anObj instanceof Map) { Map <String, Object> map = (Map)anObj;
            for (String key : map.keySet())
                node.addKeyValue(key, writeObject(getValue(map, key))); }

        // Handle List - iterate over items and add value
        else if (anObj instanceof List) { List list = (List)anObj;
            for (Object item : list)
                node.addValue(writeObject(item)); }

        // Handle String, Enum, Number, Boolean, Null
        else if (anObj instanceof String || anObj instanceof Enum || anObj instanceof Number ||
                anObj instanceof Boolean || anObj==null)
            node.setValue(anObj);

        // Handle Date (was: aNode.addValue(getClassId(),getClassPath(anObj));aNode.addValue("Time",date.getTime()); )
        else if (anObj instanceof Date) { Date date = (Date)anObj;
            node.setValue(_jsonDateString + date.getTime()); }

        // Otherwise complain
        else System.err.println("RMJSONArchiver.writeObject: Can't write object of class: " + anObj.getClass().getName());

        // Return node
        return node;
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
    public JSONArchiver addImport(String ... theImports)
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
        Class classs = aClassName.contains(".") ? ClassUtils.getClass(aClassName) : null;

        // Iterate over imports
        if (classs==null)
        for (String imp : _imports) {

            // If import ends with .ClassName, try it and break if found
            if (imp.endsWith("." + aClassName) || imp.endsWith("$" + aClassName)) {
                classs = ClassUtils.getClass(imp);
                if (classs!=null) break;
            }

            // If import ends with
            else if (imp.endsWith(".*")) {
                classs = ClassUtils.getClass(imp.substring(0, imp.length()-1) + aClassName);
                if (classs==null)
                    classs = ClassUtils.getClass(imp.substring(0, imp.length()-2) + "$" + aClassName);
                if (classs!=null) break;
            }
        }

        // If still null, just try it
        if (classs==null)
            classs = ClassUtils.getClass(aClassName);

        // Return class
        return classs;
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