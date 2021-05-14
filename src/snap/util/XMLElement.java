/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;

/**
 * The <code>RXElement</code> class represents an XML element and simply manages a list of XML elements
 * (recursively) and a list of attributes.
 *
 * For the sake of efficiency, when you need to iterate over a list of elements for a given name, you can do this:
 *
 *    for (int i=anElement.indexOf(elementName); i>=0; i=anElement.indexOf(elementName, i+1))
 *      anElement.get(i).doThis();
 */
public class XMLElement implements Cloneable {

    // The name of the attribute
    private String  _name;

    // The full name of the attribute (if namespaced)
    private String  _fname;

    // The value string of the attribute
    private String  _value;

    // The list of attributes associated with this element
    private List <XMLAttribute>  _attributes;
    
    // The list of child elements associated with this element
    private List <XMLElement>  _elements;
    
    // The namespace
    private String  _nspace;
    
    // Processing instruction data
    private String  _pidata;
    
    // Whether element should ignore case when asking for attributes/elements by name
    private boolean  _ignoreCase;
    
    // A shared XMLParser
    private static XMLParser  _xmlParser;
    
    /**
     * Creates a new element.
     */
    public XMLElement() { }

    /**
     * Creates a new element with given name.
     */
    public XMLElement(String aName)  { setName(aName); }

    /**
     * Creates a new element with given name and value.
     */
    public XMLElement(String aName, String aValue)  { setName(aName); setValue(aValue); }

    /**
     * Returns the name for this attribute.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name for this attribute.
     */
    public void setName(String aName)  { _name = aName; }

    /**
     * Returns the full name for this attribute.
     */
    public String getFullName()  { return _fname!=null ? _fname : _name; }

    /**
     * Sets the full name for this attribute.
     */
    public void setFullName(String aName)
    {
        _fname = aName;
        int ind = aName.indexOf(':');
        if (ind>0) aName = aName.substring(ind+1);
        setName(aName);
    }

    /**
     * Returns the prefix if full name is different.
     */
    public String getPrefix()
    {
        if (getFullName().equals(getName())) return null;
        int ind = getFullName().indexOf(':');
        if (ind>0) return getFullName().substring(0, ind);
        return null;
    }

    /**
     * Returns the value for this attribute.
     */
    public String getValue()  { return _value; }

    /**
     * Sets the value for this attribute.
     */
    public void setValue(String aValue)  { _value = aValue; }

    /**
     * Returns the namespace.
     */
    public String getNamespace()  { return _nspace; }

    /**
     * Sets the namespace.
     */
    public void setNamespace(String aURI)  { _nspace = aURI; }

    /**
     * Returns whether element is processing instruction.
     */
    public boolean isProcInstr()  { return _pidata!=null; }

    /**
     * Returns the processing instruction.
     */
    public String getProcInstrData()  { return _pidata; }

    /**
     * Sets the processing instruction data.
     */
    public void setProcInstrData(String aStr)  { _pidata = aStr; }

    /**
     * Returns a new element hierarchy loaded from aSource (File, String path, InputStream or whatever).
     */
    public static synchronized XMLElement getElement(Object aSource)
    {
        // If source is xml element, just return it
        if (aSource instanceof XMLElement)
            return (XMLElement)aSource;

        // Create and return new element from source
        if (_xmlParser==null) _xmlParser = new XMLParser();
        try { return _xmlParser.parseXML(aSource); }
        catch(Throwable t) { throw new RuntimeException(t); } // Catch and re-throw exceptions
    }

    /**
     * Returns the number of child attributes for this element.
     */
    public int getAttributeCount()  { return _attributes==null ? 0 : _attributes.size(); }

    /**
     * Returns the specific child attribute at the given index.
     */
    public XMLAttribute getAttribute(int anIndex)  { return _attributes.get(anIndex); }

    /**
     * Returns the list of child attributes for this element.
     */
    public List <XMLAttribute> getAttributes()  { return _attributes; }

    /**
     * Adds an attribute.
     */
    public void addAttribute(XMLAttribute anAttribute)
    {
        // Get any existing attribute with same name
        XMLAttribute oldValue = getAttribute(anAttribute.getName());

        // If attribute with same name already exists, replace it at same index
        if (oldValue!=null) {
            int index = removeAttribute(oldValue);
            addAttribute(anAttribute, index);
        }

        // Otherwise, add attribute at end
        else addAttribute(anAttribute, getAttributeCount());
    }

    /**
     * Adds an attribute at given index.
     */
    public void addAttribute(XMLAttribute anAttribute, int anIndex)
    {
        if (_attributes==null) _attributes = new ArrayList<>();  // Create attributes list if absent
        _attributes.add(anIndex, anAttribute);                // Add attribute at given index
    }

    /**
     * Removes the attribute at given index.
     */
    public XMLAttribute removeAttribute(int anIndex)  { return _attributes.remove(anIndex); }

    /**
     * Removes the given attribute.
     */
    public int removeAttribute(XMLAttribute anAttribute)
    {
        int index = getAttributeIndex(anAttribute);
        if (index>=0) removeAttribute(index);
        return index;
    }

    /**
     * Removes the attribute with given name.
     */
    public XMLAttribute removeAttribute(String aName)
    {
        XMLAttribute attribute = getAttribute(aName);
        if (attribute!=null) removeAttribute(attribute);
        return attribute;
    }

    /**
     * Returns this index of the given attribute.
     */
    public int getAttributeIndex(XMLAttribute anAttribute)  { return ListUtils.indexOfId(getAttributes(), anAttribute); }

    /**
     * Returns the specific child attribute with the given name (or null if not found).
     */
    public XMLAttribute getAttribute(String aName)
    {
        for (int i=0, iMax=getAttributeCount(); i<iMax; i++)
            if (equals(getAttribute(i).getName(), aName))
                return getAttribute(i);
        return null;
    }

    /**
     * Returns the index of the attribute with the given name (or -1 if not found).
     */
    public int getAttributeIndex(String aName)
    {
        for (int i=0, iMax=getAttributeCount(); i<iMax; i++)
            if (equals(getAttribute(i).getName(), aName))
                return i;
        return -1;
    }

    /**
     * Returns child element list size.
     */
    public int size()
    {
        return getElementCount();
    }

    /**
     * Returns the specific child element at the given index.
     */
    public XMLElement get(int anIndex)
    {
        return getElement(anIndex);
    }

    /**
     * Returns the first child element with the given name.
     */
    public XMLElement get(String aName)
    {
        return getElement(aName);
    }

    /**
     * Returns the number of child elements.
     */
    public int getElementCount()
    {
        return _elements == null ? 0 : _elements.size();
    }

    /**
     * Returns the individual element at given index.
     */
    public XMLElement getElement(int anIndex)
    {
        return _elements.get(anIndex);
    }

    /**
     * Returns the list of elements.
     */
    public List<XMLElement> getElements()
    {
        if (_elements != null) return _elements;
        return _elements = new ArrayList<>();
    }

    /**
     * Adds given element to elements list.
     */
    public void addElement(XMLElement anElement)
    {
        addElement(anElement, getElementCount());
    }

    /**
     * Adds given element to elements list at given index.
     */
    public void addElement(XMLElement anElement, int anIndex)
    {
        List<XMLElement> elements = getElements();
        elements.add(anIndex, anElement);
    }

    /**
     * Removes element at given index.
     */
    public XMLElement removeElement(int anIndex)
    {
        return _elements.remove(anIndex);
    }

    /**
     * Removes given element.
     */
    public int removeElement(XMLElement anElement)
    {
        int index = getElementIndex(anElement);
        if (index >= 0)
            removeElement(index);
        return index;
    }

    /**
     * Returns the index of the given element.
     */
    public int getElementIndex(XMLElement anElement)
    {
        return ListUtils.indexOfId(getElements(), anElement);
    }

    /**
     * Returns the number of child elements with the given name.
     */
    public int getElementCount(String aName)
    {
        int count = 0;
        for (int i=0, iMax=getElementCount(); i<iMax; i++)
            if (equals(getElement(i).getName(), aName))
                count++;
        return count;
    }

    /**
     * Returns the index of element with given name.
     */
    public int getElementIndex(String aName, int start)
    {
        for (int i=start, iMax=getElementCount(); i<iMax; i++)
            if (equals(getElement(i).getName(), aName))
                return i;
        return -1;
    }

    /**
     * Returns the first element for a given name.
     */
    public XMLElement getElement(String aName)
    {
        for (int i=0, iMax=getElementCount(); i<iMax; i++)
            if (equals(getElement(i).getName(), aName))
                return getElement(i);
        return null;
    }

    /**
     * Removes the first element with given name and returns it.
     */
    public XMLElement removeElement(String aName)
    {
        XMLElement e = getElement(aName);
        if (e != null)
            removeElement(e);
        return e;
    }

    /**
     * Returns the list of child elements with given name.
     */
    public List<XMLElement> getElements(String aName)
    {
        List<XMLElement> elements = new ArrayList<>();
        for (int i=0, iMax=getElementCount(); i<iMax; i++)
            if (equals(getElement(i).getName(), aName))
                elements.add(getElement(i));
        return elements;
    }

    /**
     * Removes elements for given element name.
     */
    public List<XMLElement> removeElements(String aName)
    {
        List<XMLElement> elements = getElements(aName);
        for (XMLElement e : elements) removeElement(e);
        return elements;
    }

    /**
     * Checks for presence of an attribute.
     */
    public boolean hasAttribute(String aName)  { return getAttribute(aName)!=null; }

    /**
     * Returns the attribute string value for the given attribute name.
     */
    public String getAttributeValue(String aName)  { return getAttributeValue(aName, null); }

    /**
     * Returns the string value for the given attribute name (or the given default value, if name not found).
     */
    public String getAttributeValue(String aName, String defaultValue)
    {
        XMLAttribute attr = getAttribute(aName);
        return attr == null ? defaultValue : attr.getValue();
    }

    /**
     * Returns the boolean value for the given attribute name.
     */
    public boolean getAttributeBoolValue(String aName)  { return getAttributeBoolValue(aName, false); }

    /**
     * Returns the boolean value for the given attribute name (or the given default value, if name not found).
     */
    public boolean getAttributeBoolValue(String aName, boolean defaultValue)
    {
        XMLAttribute attr = getAttribute(aName);
        return attr == null ? defaultValue : attr.getValue().equals("true");
    }

    /**
     * Returns the Boolean value for the given attribute name.
     */
    public Boolean getAttributeBooleanValue(String aName)  { return getAttributeBooleanValue(aName, null); }

    /**
     * Returns the Boolean value for the given attribute name (or the given default value, if name not found).
     */
    public Boolean getAttributeBooleanValue(String aName, Boolean defaultValue)
    {
        XMLAttribute attr = getAttribute(aName);
        return attr != null ? attr.getValue().equals("true") : defaultValue;
    }

    /**
     * Returns the int value for the given attribute name.
     */
    public int getAttributeIntValue(String aName)
    {
        return getAttributeIntValue(aName, 0);
    }

    /**
     * Returns the int value for the given attribute name (or the given default value, if name not found).
     */
    public int getAttributeIntValue(String aName, int defaultValue)
    {
        XMLAttribute attr = getAttribute(aName);
        return attr == null ? defaultValue : attr.getIntValue();
    }

    /**
     * Returns the float value for the given attribute name.
     */
    public float getAttributeFloatValue(String aName)
    {
        return getAttributeFloatValue(aName, 0);
    }

    /**
     * Returns the float value for the given attribute name (or the given default value, if name not found).
     */
    public float getAttributeFloatValue(String aName, float defaultValue)
    {
        XMLAttribute attr = getAttribute(aName);
        return attr == null ? defaultValue : attr.getFloatValue();
    }

    /**
     * Returns the double value for the given attribute name.
     */
    public double getAttributeDoubleValue(String aName)  { return getAttributeDoubleValue(aName, 0); }

    /**
     * Returns the double value for the given attribute name (or the given default value, if name not found).
     */
    public double getAttributeDoubleValue(String aName, double defaultValue)
    {
        XMLAttribute attr = getAttribute(aName);
        return attr == null ? defaultValue : attr.getFloatValue();
    }

    /**
     * Returns the Number value for the given attribute name.
     */
    public Number getAttributeNumberValue(String aName)
    {
        return getAttributeNumberValue(aName, 0);
    }

    /**
     * Returns the Number value for the given attribute name (or the given default value, if name not found).
     */
    public Number getAttributeNumberValue(String aName, Number defaultValue)
    {
        XMLAttribute attr = getAttribute(aName);
        return attr == null ? defaultValue : attr.getNumberValue();
    }

    /**
     * Returns the Enum of a specific type for the given attribute name (or the given default value, if name not found).
     */
    public <T extends Enum<T>> T getAttributeEnumValue(String aName, Class<T> enumClass, T defaultValue)
    {
        String attrVal = getAttributeValue(aName);
        T enumVal = attrVal != null ? EnumUtils.valueOfIC(enumClass, attrVal) : null;
        return enumVal != null ? enumVal : defaultValue;
    }

    /**
     * Adds a new attribute with the given name and string value.
     */
    public XMLElement add(String aName, String aValue)
    {
        return add(new XMLAttribute(aName, aValue));
    }

    /**
     * Adds a new attribute with the given name using the object's toString() method.
     */
    public XMLElement add(String aName, Object aValue)
    {
        return add(aName, aValue.toString());
    }

    /**
     * Adds a new attribute with the given name and boolean value.
     */
    public XMLElement add(String aName, boolean aValue)
    {
        XMLAttribute attr = new XMLAttribute(aName, aValue);
        return add(attr);
    }

    /**
     * Adds a new attribute with the given name and Boolean value.
     */
    public XMLElement add(String aName, Boolean aValue)
    {
        XMLAttribute attr = new XMLAttribute(aName, aValue);
        return add(attr);
    }

    /**
     * Adds a new attribute with the given name and int value.
     */
    public XMLElement add(String aName, int aValue)
    {
        XMLAttribute attr = new XMLAttribute(aName, aValue);
        return add(attr);
    }

    /**
     * Adds a new attribute with the given name and float value.
     */
    public XMLElement add(String aName, double aValue)
    {
        XMLAttribute attr = new XMLAttribute(aName, aValue);
        return add(attr);
    }

    /**
     * Adds the given attribute to this element's list of attributes.
     */
    public XMLElement add(XMLAttribute anAttribute)
    {
        addAttribute(anAttribute);
        return this;
    }

    /**
     * Adds the given attribute to this element's list of attributes.
     */
    public XMLElement add(XMLElement anElement)
    {
        addElement(anElement);
        return this;
    }

    /**
     * Adds all the given child elements and attributes of given element to this element.
     */
    public void addAll(XMLElement anElement)
    {
        // Add all attributes
        for (int i=0, iMax=anElement.getAttributeCount(); i<iMax; i++)
            addAttribute(anElement.getAttribute(i));

        // Add all child elements
        for (int i=0, iMax=anElement.getElementCount(); i<iMax; i++)
            addElement(anElement.getElement(i));
    }

    /**
     * Returns the index of the first child element with the given name.
     */
    public int indexOf(String aName)
    {
        return getElementIndex(aName, 0);
    }

    /**
     * Returns the index of the first child element with the given name at or beyond the given index.
     */
    public int indexOf(String aName, int startIndex)
    {
        return getElementIndex(aName, startIndex);
    }

    /**
     * Returns the element value as bytes.
     */
    public byte[] getValueBytes()
    {
        String byteString = getValue();
        byteString = StringUtils.replace(byteString, "xxx", "]]>");
        byte bytes[] = ASCIICodec.decodeASCII85(byteString);
        return bytes;
    }

    /**
     * Sets the element value from bytes.
     */
    public void setValueBytes(byte theBytes[])
    {
        // Get Ascii85 byte string, swap out illegal chars, set value and set encoding
        String byteString = ASCIICodec.encodeASCII85(theBytes);
        byteString = StringUtils.replace(byteString, "]]>", "xxx");
        setValue(byteString);
        add("rm-byte-encoding", "Ascii85");
    }

    /**
     * Returns whether element should ignore case when asking for attributes/elements by name.
     */
    public boolean isIgnoreCase()  { return _ignoreCase; }

    /**
     * Sets whether element should ignore case when asking for attributes/elements by name.
     */
    public void setIgnoreCase(boolean aVal)
    {
        _ignoreCase = aVal;
        if (_elements!=null)
            for (XMLElement e : _elements) e.setIgnoreCase(aVal);
    }

    /**
     * A string compare that honors CaseSensitive setting.
     */
    protected boolean equals(String aStr1, String aStr2)
    {
        return _ignoreCase ? aStr1.equalsIgnoreCase(aStr2) : aStr1.equals(aStr2);
    }

    /**
     * Returns a clone of this element.
     */
    public XMLElement clone()
    {
        XMLElement clone;
        try { clone = (XMLElement)super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }

        clone._attributes = null; clone._elements = null;
        for (int i=0; i<getAttributeCount(); i++) clone.addAttribute(getAttribute(i).clone());
        for (int i=0; i<getElementCount(); i++) clone.addElement(getElement(i).clone());
        return clone;
    }

    /**
     * Returns a string representation of this element (XML).
     */
    public String getString()
    {
        // Create new StringBuffer with XML header, recursively write elements, return StringBuffer string
        StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        write(sb, null, 0, "  ");
        while (Character.isWhitespace(sb.charAt(sb.length()-1)))
            sb.setLength(sb.length()-1);
        return sb.toString();
    }

    /**
     * Returns a string representation of this element (XML).
     */
    public String toString()
    {
        System.out.println("XMLElement.toString: Use getString() instead");
        return getString();
    }

    /**
     * Returns XML bytes for this element.
     */
    public byte[] getBytes()
    {
        String str = getString();
        return StringUtils.getBytes(str, "UTF-8");
    }

    /**
     * Writes the element to the given string buffer using the given indent level and indent string.
     */
    public void write(StringBuffer aSB, String indentStr)  { write(aSB, null, 0, indentStr); }

    /**
     * Writes the element to the given string buffer using the given indent level and indent string.
     */
    public void write(StringBuffer aSB, String aNameSpace, int indent, String indentStr)
    {
        // Append indentation
        for (int i=0; i<indent; i++) aSB.append(indentStr);

        // If processing instruction, just append name and data and return
        if (isProcInstr()) {
            aSB.append("<?").append(getName()).append(' ').append(getProcInstrData()).append("?>\n"); return; }

        // Append tag start (<) and name
        aSB.append('<');
        aSB.append(getFullName());

        // Append namespace
        if (!SnapUtils.equals(getNamespace(), aNameSpace)) {
            aSB.append(' ').append("xmlns");
            String prfx = getPrefix(); if (prfx!=null) aSB.append(':').append(prfx);
            aSB.append("=\"").append(getNamespace()).append('"');
        }

        // Append attributes
        for (int i=0, iMax=getAttributeCount(); i<iMax; i++) { XMLAttribute attr = getAttribute(i);
            aSB.append(' ').append(attr.getFullName()).append("=\"");
            append(aSB, attr.getValue()); aSB.append('"');
        }

        // If no child elements and no element value, close and return
        if (getElementCount()==0 && getValue()==null) {
            aSB.append(" />\n"); return; }

        // If element value, close start tag, add element value and add end tag
        else if (getValue()!=null) {

            // Close opening tag
            aSB.append('>');

            // If there is a byte encoding, write out value as CDATA: <![CDATA[ {value} ]]>
            if (hasAttribute("rm-byte-encoding")) {
                aSB.append('\n');
                for (int i=0; i<indent+1; i++) aSB.append(indentStr);
                aSB.append("<![CDATA[\n");
                aSB.append(getValue());
                aSB.append("]]>\n");
                for (int i=0; i<indent; i++) aSB.append(indentStr);
            }

            // If not CData, just write the value
            else append(aSB, getValue());

            // Append closing tag
            aSB.append("</"); aSB.append(getFullName()); aSB.append(">\n");
        }

        // Append child element value
        else {
            aSB.append(">\n");
            for (int i=0, iMax=size(); i<iMax; i++)
                get(i).write(aSB, getNamespace(), indent+1, indentStr);
            for (int i=0; i<indent; i++) aSB.append(indentStr);
            aSB.append("</"); aSB.append(getFullName()); aSB.append(">\n");
        }
    }

    /**
     * Append String to StringBuffer, converting chars to XML compliant chars.
     */
    private void append(StringBuffer aSB, String aString)
    {
        for (int i=0, iMax=aString.length(); i<iMax; i++) {
            char c = aString.charAt(i);
            switch(c) {
                case '&': aSB.append("&amp;"); break;
                case '"': aSB.append("&quot;"); break;
                case '\'': aSB.append("&apos;"); break;
                case '<': aSB.append("&lt;"); break;
                case '>': aSB.append("&gt;"); break;
                case '\n':
                case '\r': aSB.append("&#").append((int)c).append(";"); break;
                default:
                    if (c>127 || c<32) aSB.append("&#").append((int)c).append(";"); // Only 7 bit ASCII?
                    else aSB.append(c);
            }
        }
    }
}