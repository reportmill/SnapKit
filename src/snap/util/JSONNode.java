/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;
import snap.web.WebFile;
import snap.web.WebURL;

/**
 * Represents a node in a JSON tree.
 */
public class JSONNode {
    
    // The type of node
    Type              _type = Type.Null;
    
    // The key
    String            _key;

    // The value
    Object            _value;
    
    // Constants for JSON types
    public enum Type { Object, Array, String, Number, Boolean, Null };
    
    /**
     * Creates a new node.
     */
    public JSONNode()  { }

    /**
     * Creates a new node.
     */
    public JSONNode(String aKey, Object aSource)  { setKey(aKey); setValue(aSource); }

    /**
     * Returns the the node type.
     */
    public Type getType()  { return _type; }

    /**
     * Sets the node type.
     */
    protected void setType(Type aType)  { _type = aType; }

    /**
     * Returns the node key.
     */
    public String getKey()  { return _key; }

    /**
     * Sets the node key.
     */
    protected void setKey(String aKey)  { _key = aKey; }

    /**
     * Returns the value.
     */
    public Object getValue()  { return _value; }

    /**
     * Sets the value.
     */
    protected void setValue(Object anObj)
    {
        // Handle enum special
        if (anObj instanceof Enum)
            anObj = anObj.toString();

        // Handle Map
        if (anObj instanceof Map) { Map map = (Map)anObj;
            for (Map.Entry entry : (Set<Map.Entry>)map.entrySet())
                addKeyValue(entry.getKey().toString(), entry.getValue());
        }

        // Handle List
        else if (anObj instanceof List) { List list = (List)anObj;
            for (Object item : list)
                addValue(item);
        }

        // Handle String, Number, Boolean, Null
        else if (anObj instanceof String) { _value = anObj; setType(Type.String); }
        else if (anObj instanceof Number) { _value = anObj; setType(Type.Number); }
        else if (anObj instanceof Boolean) { _value = anObj; setType(Type.Boolean); }
        else if (anObj==null) { _value = anObj; setType(Type.Null); }
        else throw new RuntimeException("RMJSONNode: Unsupported core type (" + anObj.getClass().getName() + ")");
    }

    /**
     * Returns whether node is type Object.
     */
    public boolean isObject()  { return _type==Type.Object; }

    /**
     * Returns whether node is type Array.
     */
    public boolean isArray()  { return _type==Type.Array; }

    /**
     * Returns whether node is type String.
     */
    public boolean isString()  { return _type==Type.String; }

    /**
     * Returns whether node is type Number.
     */
    public boolean isNumber()  { return _type==Type.Number; }

    /**
     * Returns whether node is type Boolean.
     */
    public boolean isBoolean()  { return _type==Type.Boolean; }

    /**
     * Returns whether node is type Null.
     */
    public boolean isNull()  { return _type==Type.Null; }

    /**
     * Returns the value as String if type is String.
     */
    public String getString()  { return SnapUtils.stringValue(_value); }

    /**
     * Returns the value as Number if type is Number.
     */
    public Number getNumber()  { return (Number)_value; }

    /**
     * Returns the value as Map if type is Map.
     */
    public Boolean getBoolean()  { return SnapUtils.booleanValue(_value); }

    /**
     * Returns the value as List if type is List.
     */
    public List <JSONNode> getNodes()
    {
        if (_value==null) _value = new ArrayList();
        return (List)_value;
    }

    /**
     * Returns the number of JSON nodes if node type Object or Array.
     */
    public int getNodeCount()  { return isArray() || isObject() ? getNodes().size() : 0; }

    /**
     * Returns value for index if node type Array.
     */
    public JSONNode getNode(int anIndex)  { return getNodes().get(anIndex); }

    /**
     * Adds a node at end of array.
     */
    public void addNode(JSONNode aNode)
    {
        int index = aNode.getKey()!=null? getNodeIndex(aNode.getKey()) : getNodeCount();
        if (index<0) index = getNodeCount();
        addNode(aNode, index);
    }

    /**
     * Adds a node at given array index.
     */
    public void addNode(JSONNode aNode, int anIndex)  { getNodes().add(anIndex, aNode); }

    /**
     * Removes a node at given array index.
     */
    public JSONNode removeNode(int anIndex)  { return getNodes().remove(anIndex); }

    /**
     * Removes a given node.
     */
    public int removeNode(JSONNode aNode)
    {
        int index = getNodes().indexOf(aNode);
        if (index>=0) removeNode(index);
        return index;
    }

    /**
     * Sets a node at given index to new node.
     */
    public JSONNode setNode(JSONNode aNode, int anIndex)  { return getNodes().set(anIndex, aNode); }

    /**
     * Returns the key at given index.
     */
    public String getKey(int anIndex)  { return getNodes().get(anIndex).getKey(); }

    /**
     * Gets value for index.
     */
    public Object getValue(int anIndex)  { return getNodes().get(anIndex).getValue(); }

    /**
     * Returns value for key if node type Object.
     */
    public JSONNode getNode(String aKey)
    {
        int index = getNodeIndex(aKey);
        return index>=0 ? getNodes().get(index) : null;
    }

    /**
     * Returns the value for node with key.
     */
    public Object getNodeValue(String aKey)
    {
        JSONNode o = getNode(aKey);
        return o==null ? null : o.getValue();
    }

    /**
     * Returns the string value for node with key.
     */
    public String getNodeString(String aKey)
    {
        JSONNode n = getNode(aKey);
        return n!=null ? n.getString() : null;
    }

    /**
     * Returns value for key if node type Object.
     */
    public int getNodeIndex(String aKey)
    {
        List <JSONNode> list = getNodes();
        for (int i=0, iMax=list.size(); i<iMax; i++)
            if (aKey.equals(list.get(i).getKey())) return i;
        return -1;
    }

    /**
     * Adds a value.
     */
    public void addValue(Object anObj)
    {
        // Promote to Array type
        _type = Type.Array;

        // Create/add node
        JSONNode node = anObj instanceof JSONNode ? (JSONNode)anObj : new JSONNode(null, anObj);
        getNodes().add(node);
    }

    /**
     * Adds a value for key.
     */
    public void addKeyValue(String aKey, Object aValue)
    {
        // Promote to Object type
        _type = Type.Object;

        // Get Node
        JSONNode node = aValue instanceof JSONNode ? (JSONNode)aValue : new JSONNode(aKey, aValue);
        if (aKey!=null) node.setKey(aKey); // Really?

        // Add node
        int index = getNodeIndex(aKey);
        if (index>=0) setNode(node, index);
        else addNode(node);
    }

    /**
     * Returns the node as a map.
     */
    public Map getAsMap()
    {
        // If not object, complain and return null
        if (getType()!=Type.Object) {
            System.err.println("JSONNode.getAsMap: Type isn't Object"); return null; }

        // Create map, iterate over keys to add values and return map
        Map map = new LinkedHashMap();
        for (int i=0;i<getNodeCount();i++) { String key = getKey(i);
            Object val = getNodeValue(key);
            map.put(key,val);
        }
        return map;
    }

    /**
     * Returns the node as a native object.
     */
    public Object getNative()
    {
        switch(getType()) {

            // Handle Object (map): Create map, iterate over keys to add values and return map
            case Object: { Map map = new LinkedHashMap();
                for (int i=0;i<getNodeCount();i++) { String key = getKey(i);
                    JSONNode node = getNode(key);
                    Object val = node.getNative();
                    map.put(key,val);
                }
                return map;
            }

            // Handle Array
            case Array: { List list = new ArrayList();
                for (int i=0;i<getNodeCount();i++) { JSONNode node = getNode(i);
                    Object val = node.getNative();
                    list.add(val);
                }
                return list;
            }

            // Handle anything else
            default: return getValue();
        }
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        JSONNode other = anObj instanceof JSONNode? (JSONNode)anObj : null; if (other==null) return false;
        if (other._type!=_type) return false;
        if (!SnapUtils.equals(other._key, _key)) return false;
        if (!SnapUtils.equals(other._value, _value)) return false;
        return true;
    }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()
    {
        int hc = _key!=null? _key.hashCode() : 0;
        if (_value!=null) hc += _value.hashCode();
        return hc;
    }

    /**
     * Returns a string representation of node (as JSON, of course).
     */
    public String toString()  { return new JSONWriter().getString(this); }

    /**
     * Returns a string representation of node (as JSON, of course).
     */
    public String toStringCompacted()  { return new JSONWriter().setCompacted(true).getString(this); }

    /**
     * Reads JSON from a source.
     */
    public static JSONNode readSource(Object aSource)
    {
        WebURL url = WebURL.getURL(aSource);
        WebFile file = url.getFile();
        return new JSONParser().readString(url.getText());
    }
}