/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;
import snap.web.WebURL;

/**
 * This class manages archival and unarchival to/from XMLElements.
 *
 * For archival, objects simply implement the toXML() method to configure and return XMLElements. Archiver's
 * toXML() method manages the process, allowing for object references.
 *
 * For unarchival, classes register for particular element names. Then during Archiver.fromXML(), Archiver will
 * call fromXML() on the classes for encountered tags to reconstruct the object graph.
 */
public class XMLArchiver {

    // The owner class that initated archival/unarchival
    private Object _owner;
    
    // The URL of the source the archiver is reading from
    private WebURL _surl;

    // Root element for unarchival
    private XMLElement _root;
    
    // The root object to be used in unarchival
    private Object _rootObject;
    
    // The version of unarchived object
    private double _version;

    // Whether element should ignore case when asking for attributes/elements by name
    private boolean _ignoreCase;

    // Unarchival keeps track of read elements to guarantee that each element results in one instance
    private Map <XMLElement, Object> _readElements = new HashMap<>();
    
    // list of objects that are archived by reference
    private List _references = new ArrayList();
    
    // Archiver manages archival of shared BLOBs external to normal element hierarchy
    private List<Resource> _resources = new ArrayList<>();
    
    // The map of classes for unarchival
    private Map<String,Class> _classMap;
    
    // The stack of parents
    private Deque _parentStack = new ArrayDeque();
    
    /**
     * Returns the WebURL of the currently loading archive.
     */
    public WebURL getSourceURL()  { return _surl; }

    /**
     * Sets the WebURL of the currently loading archive.
     */
    public void setSourceURL(WebURL aURL)  { _surl = aURL; }

    /**
     * Returns the owner.
     */
    public Object getOwner()  { return _owner; }

    /**
     * Sets the owner.
     */
    public void setOwner(Object anOwner)  { _owner = anOwner; }

    /**
     * Returns the owner class.
     */
    public Class getOwnerClass()  { return _owner!=null? _owner.getClass() : null; }

    /**
     * Returns the object that the archiver should read "into".
     */
    public Object getRootObject()  { return _rootObject; }

    /**
     * Sets the object that the archiver should read "into".
     */
    public void setRootObject(Object anObj)  { _rootObject = anObj; }

    /**
     * Returns the version of the document.
     */
    public double getVersion()  { return _version; }

    /**
     * Sets the version of the document.
     */
    public void setVersion(double aVersion)  { _version = aVersion; }

    /**
     * Returns whether element should ignore case when asking for attributes/elements by name.
     */
    public boolean isIgnoreCase()  { return _ignoreCase; }

    /**
     * Sets whether element should ignore case when asking for attributes/elements by name.
     */
    public void setIgnoreCase(boolean aVal)  { _ignoreCase = aVal; }

    /**
     * Returns the root xml.
     */
    public XMLElement getRootXML()  { return _root; }

    /**
     * Returns the class map.
     */
    public Map <String, Class> getClassMap()
    {
        return _classMap!=null? _classMap : (_classMap=createClassMap());
    }

    /**
     * Creates the class map.
     */
    protected Map <String, Class> createClassMap()  { throw new RuntimeException("No class map"); }

    /**
     * Returns a root object unarchived from a generic input source (a File, String path, InputStream, URL, byte[], etc.).
     */
    public Object readFromXMLSource(Object aSource)
    {
        // Get bytes from source - if not found or empty, complain
        byte bytes[] = SnapUtils.getBytes(aSource);
        if (bytes==null || bytes.length==0)
            throw new RuntimeException("XMLArchiver.readObject: Cannot read source: " + aSource);

        // Try to get SourceURL from source
        if (getSourceURL()==null) {
            WebURL surl = WebURL.getURL(aSource);
            setSourceURL(surl);
        }

        // ReadObject(bytes) and return
        return readFromXMLBytes(bytes);
    }

    /**
     * Returns a root object unarchived from an RMByteSource.
     */
    public Object readFromXMLBytes(byte theBytes[])
    {
        XMLElement xml = XMLElement.getElement(theBytes);
        if (isIgnoreCase())
            xml.setIgnoreCase(true);
        return readFromXML(xml);
    }

    /**
     * Returns a root object unarchived from the XML source (a File, String path, InputStream, URL, byte[], etc.).
     * You can also provide a root object to be read "into", and an owner that the object is being read "for".
     */
    public Object readFromXML(XMLElement theXML)
    {
        // Read xml
        _root = theXML;

        // Get resources from top level xml
        getResources(_root);

        // Unarchive from xml and return
        Object object = fromXML(_root, null);

        // Add runOnFinished() instead of this! Old: Send fromXMLFinish to objects
        /*for (XMLElement element : (List <XMLElement>)_references) {
            Object obj = fromXML(element, (Class)null, null); if (obj==null) continue;
            Method meth = ClassUtils.getMethod(obj, "fromXMLFinish", XMLArchiver.class, XMLElement.class);
            meth.invoke(obj, this, element);
        }*/

        // Return object
        return object;
    }

    /**
     * Returns an xml element for a given object.
     * This top level method encodes resources, in addition to doing the basic toXML stuff.
     */
    public XMLElement writeToXML(Object anObj)
    {
        // Write object
        XMLElement xml = toXML(anObj);

        // Archive resources
        for (XMLArchiver.Resource resource : getResources()) {
            XMLElement resourceXML = new XMLElement("resource");
            resourceXML.add("name", resource.getName());
            resourceXML.setValueBytes(resource.getBytes());
            xml.add(resourceXML);
        }

        // Return xml
        return xml;
    }

    /**
     * Writes given object to XML and returns the XML bytes.
     */
    public byte[] writeToXMLBytes(Object anObj)
    {
        XMLElement xml = writeToXML(anObj);
        return xml.getBytes();
    }

    /**
     * Returns an object unarchived from the given element.
     */
    public Object fromXML(XMLElement anElement, Object anOwner)
    {
        return fromXML(anElement, null, anOwner);
    }

    /**
     * Returns an object unarchived from the given element by instantiating the given class.
     */
    public <T> T fromXML(XMLElement anElement, Class<T> aClass, Object anOwner)
    {
        // Handle Lists Special
        if (anElement.getName().equals("alist")) { List list = new ArrayList();
            for (int i=0, iMax=anElement.size(); i<iMax; i++)
                list.add(fromXML(anElement.get(i), anOwner));
            return (T)list;
        }

        // See if anElement has already been read
        Object readObject = _readElements.get(anElement);
        if (readObject!=null && (aClass==null || aClass.isInstance(readObject)))
            return (T)readObject;

        // If root element and owner is same class, set read object to owner
        if (anElement==_root && getRootObject()!=null)
            readObject = getRootObject();

        // If class was provided, try to instantiate
        else {

            // Get class
            Class cls = aClass;
            if (cls==null)
                cls = getClassForXML(anElement);

            // If no class, throw exception
            if (cls==null)
                throw new RuntimeException("XMLArchiver: Can't find class for element: " + anElement.getName());

            // Create new object
            readObject = newInstance(cls);
        }

        // If couldn't create new instance, return null (should throw exception instead, I think)
        if (readObject==null) {
            System.err.println("XMLArchiver.fromXML: Couldn't find class for: " + anElement.getName());
            return null;
        }

        // Add new instance to readElement's map
        _readElements.put(anElement, readObject);

        // Call fromXML on object
        Object obj = fromXML(anElement, readObject, anOwner);

        // If fromXML returned a different object, swap it in
        if (obj!=readObject)
            _readElements.put(anElement, readObject = obj);

        // Return read object
        return (T)readObject;
    }

    /**
     * Calls fromXML on given object.
     */
    public Object fromXML(XMLElement anElement, Object anObj, Object anOwner)
    {
        // Archive given object (with given owner on top of ParentStack)
        if (anOwner!=null)
            pushParent(anOwner);
        Object obj = ((Archivable)anObj).fromXML(this, anElement);
        if (anOwner!=null)
            popParent();
        return obj;
    }

    /**
     * Writes the given object to XML elements.
     */
    public XMLElement toXML(Object anObj)  { return toXML(anObj, null); }

    /**
     * Writes the given object to XML elements.
     */
    public XMLElement toXML(Object anObj, Object anOwner)
    {
        // Handle Lists Special
        if (anObj instanceof List) { List list = (List)anObj;
            XMLElement e = new XMLElement("alist");
            for (int i=0, iMax=list.size(); i<iMax; i++)
                e.add(toXML(list.get(i)));
            return e;
        }

        // Unarchive given object (with given Owner on top of ParentStack)
        if (anOwner!=null) pushParent(anOwner);
        XMLElement xml = ((Archivable)anObj).toXML(this);
        if (anOwner!=null) popParent();
        return xml;
    }

    /**
     * Returns the class for a given element name.
     */
    public Class getClass(String aName)
    {
        // Get class from map (if found, just return)
        Object clss = getClassMap().get(aName);
        if (clss instanceof Class)
            return (Class)clss;

        // Load class from string
        if (clss!=null) try {
            ClassLoader classLoader = getClass().getClassLoader();
            Class c2 = Class.forName(clss.toString(), true, classLoader);
            getClassMap().put(aName, c2);
            return c2;
        }

        // Catch exceptions - print stack and return null
        catch(Exception e) { e.printStackTrace(); }

        // Return null since class not found
        return null;
    }

    /**
     * Returns the class for a given element.
     */
    protected Class getClassForXML(XMLElement anElement)
    {
        // Get class for element name
        String name = anElement.getName();
        Class cls = getClass(name);

        // If element has type, see if there is class for type-name
        String type = anElement.getAttributeValue("type");
        if (type!=null) {
            Class c2 = getClass(type + "-" + name);
            if (c2!=null)
                cls = c2;
        }

        // Return class
        return cls;
    }

    /**
     * Returns a new instance of an object given a class.
     */
    protected Object newInstance(Class aClass)
    {
        try { return aClass.newInstance(); }
        catch(InstantiationException | IllegalAccessException e) { throw new RuntimeException(e); }
    }

    /**
     * Returns a reference id for the given object (used in archival).
     */
    public int getReference(Object anObj)
    {
        return getReference(anObj, true);
    }

    /**
     * Returns a reference id for given object if in references list with option to add if absent (used in archival).
     */
    public int getReference(Object anObj, boolean add)
    {
        // If object is in list (or if not asked to add) return its current index
        int index = ListUtils.indexOfId(_references, anObj);
        if (index>=0 || !add)
            return index;

        // Add object to references and return id
        _references.add(anObj);
        return _references.size() - 1;
    }

    /**
     * Returns an object for a given reference (used in unarchival).
     */
    public Object getReference(String aName, XMLElement anElement)
    {
        // Get xref id and recursively search for element that contains it
        int xref = anElement.getAttributeIntValue(aName, -1);
        if (xref<0) return null;
        return getReference(xref, _root);
    }

    /**
     * Returns an object unarchived from element that has the given xref id.
     */
    private Object getReference(int xref, XMLElement anElement)
    {
        // If anElement has matching xref attribute/id, return unarchived object
        if (anElement.getAttributeIntValue("xref", -1)==xref)
            return fromXML(anElement, null);

        // Iterate over element's children and recurse
        for (int i=0, iMax=anElement.size(); i<iMax; i++) {
            XMLElement e = anElement.get(i);
            Object obj = getReference(xref, e);
            if (obj!=null)
                return obj;
        }

        // If xref not found it this element or its children, return null
        return null;
    }

    /**
     * Returns the index of the first child element with the given name.
     */
    public int indexOf(XMLElement anElement, Class aClass)  { return indexOf(anElement, aClass, 0); }

    /**
     * Returns the index of the first child element with the given name at or beyond the given index.
     */
    public int indexOf(XMLElement anElement, Class aClass, int startIndex)
    {
        // Iterate over element children from start index, and if child has matching class, return its index
        for (int i=startIndex, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
            Class childClass = getClassForXML(childXML);
            if (childClass!=null && aClass.isAssignableFrom(childClass))
                return i;
        }
        return -1; // Return -1 since element name not found
    }

    /**
     * Returns the list of objects of the given name and/or class (either can be null) unarchived from the given element.
     */
    public List fromXMLList(XMLElement anElement, String aName, Class aClass, Object anOwner)
    {
        // Declare variable for list
        List list = new Vector();

        // If name is provided, iterate over elements, unarchive and add to list
        if (aName!=null) {
            for (XMLElement e : anElement.getElements(aName)) {
                Object obj = fromXML(e, aClass, anOwner);
                list.add(obj);
            }
        }

        // Iterate over elements, unarchive, and if class, add to list
        else for (int i=0, iMax=anElement.size(); i<iMax; i++) {
            XMLElement xml = anElement.get(i);
            Object obj = fromXML(xml, anOwner);
            if (aClass.isInstance(obj))
                list.add(obj);
        }

        // Return to list
        return list;
    }

    /**
     * Returns a copy of the given object using archival.
     */
    public <T> T copy(T anObj)
    {
        XMLElement xml = toXML(anObj);
        if (isIgnoreCase())
            xml.setIgnoreCase(true);
        return (T)fromXML(xml, null);
    }

    /**
     * Returns the top parent from the parent stack.
     */
    public Object getParent()  { return _parentStack.peekFirst(); }

    /**
     * Returns the first parent from the parent stack of given class.
     */
    public <T> T getParent(Class <T> aClass)
    {
        for (Object o : _parentStack)
            if (aClass.isInstance(o))
                return (T)o;
        return null;
    }

    /**
     * Pushes a parent on the parent stack.
     */
    protected void pushParent(Object anObj)  { _parentStack.addFirst(anObj); }

    /**
     * Pops a parent from the parent stack.
     */
    protected Object popParent()  { return _parentStack.removeFirst(); }

    /**
     * Returns the list of optional resources associated with this archiver.
     */
    public List <Resource> getResources()  { return _resources; }

    /**
     * Returns an individual resource associated with this archiver, by index.
     */
    public Resource getResource(int anIndex)  { return _resources.get(anIndex); }

    /**
     * Returns an individual resource associated with this archiver, by name.
     */
    public byte[] getResource(String aName)
    {
        for (int i=0, iMax=_resources.size(); i<iMax; i++)
            if (getResource(i)._name.equals(aName))
                return getResource(i)._bytes;
        return null;
    }

    /**
     * Adds a byte array resource to this archiver (only if absent).
     */
    public String addResource(byte bytes[], String aName)
    {
        // If resource has already been added, just return it's name
        for (int i=0, iMax=_resources.size(); i<iMax; i++)
            if (getResource(i).equals(bytes))
                return getResource(i).getName();

        // If new resource, add it
        _resources.add(new Resource(bytes, aName));

        // Return given name
        return aName;
    }

    /**
     * Reads resources from <resource> elements in given xml (top-level) element, converts from ASCII encoding and
     * adds to archiver.
     */
    protected void getResources(XMLElement anElement)
    {
        // Get resources from top level <resource> tags
        for (int i=anElement.indexOf("resource"); i>=0; i=anElement.indexOf("resource", i)) {

            // Get/remove current resource element
            XMLElement e =  anElement.removeElement(i);

            // Get resource name and bytes
            String name = e.getAttributeValue("name");
            byte bytes[] = e.getValueBytes();

            // Add resource bytes for name
            addResource(bytes, name);
        }
    }

    /**
     * An interface for objects that are archivable.
     */
    public interface Archivable {

        /** Archival. */
        XMLElement toXML(XMLArchiver anArchiver);

        /** Unarchival. */
        Object fromXML(XMLArchiver anArchiver, XMLElement anElement);
    }

    /**
     * This inner class represents a named resource associated with an archiver.
     */
    public static class Resource {

        // The resource bytes
        byte    _bytes[];

        // The resource name
        String  _name;

        // Returns resource bytes
        public byte[] getBytes() { return _bytes; }

        // Returns resource name
        public String getName() { return _name; }

        // Creates new resource for given bytes and name
        public Resource(byte bytes[], String aName) { _bytes = bytes; _name = aName; }

        // Standard equals implementation
        public boolean equals(byte bytes[])
        {
            if (bytes.length!=_bytes.length) return false;
            for (int i=0, iMax=bytes.length; i<iMax; i++)
                if (bytes[i]!=_bytes[i])
                    return false;
            return true;
        }
    }
}