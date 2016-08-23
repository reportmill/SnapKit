/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.util.*;

/**
 * This class maps a UI node value to a model key value (some kind of display object like a Swing component value or
 * an RMShape size).
 */
public class Binding implements Cloneable, XMLArchiver.Archivable {

    // The UI node (most commonly a snap View)
    Object          _view;
    
    // The property name that is being bound
    String          _propName;
    
    // The key that is used to get the property value from the bound object
    String          _key;
    
    // The conversion key used to get conversion map to convert bound key-value to UI node
    String          _conversionKey;
    
    // The format to be used
    //Format          _format;

/**
 * Creates a new binding.
 */
public Binding() { }

/**
 * Creates a new binding for property name and key.
 */
public Binding(String aPropName, String aKey)  { setPropertyName(aPropName); setKey(aKey); }

/**
 * Returns the UI view.
 */
public Object getView()  { return _view; }

/**
 * Sets the UI view.
 */
public void setView(Object anObj)  { _view = anObj; }

/**
 * Returns the UI view.
 */
public <T> T getView(Class<T> aClass)  { return ClassUtils.getInstance(getView(), aClass); }

/**
 * Returns the property name.
 */
public String getPropertyName()  { return _propName; }

/**
 * Sets the property name.
 */
public void setPropertyName(String aPropertyName)  { _propName = aPropertyName; }

/**
 * Returns the key that is used to get the property value from the bound object.
 */
public String getKey()  { return _key; }

/**
 * Sets the key that is used to get the property value from the bound object.
 */
public void setKey(String aKey)  { _key = aKey; }

/**
 * Returns the conversion key used to get conversion map to convert bound object value to UI node.
 */
public String getConversionKey()  { return _conversionKey; }

/**
 * Sets the conversion key used to get conversion map to convert bound object value to UI node.
 */
public void setConversionKey(String aKey)  { _conversionKey = aKey; }

/**
 * Returns the format object to be used to format values to strings.
 */
//public Format getFormat()  { return _format; }

/**
 * Sets the format object to be used to convert values to strings.
 */
//public void setFormat(Format aFormat)  { _format = aFormat; }

/**
 * Standard clone implementation.
 */
public Binding clone()
{
    // Do normal object cone, clear UI node and return
    Binding clone; try { clone = (Binding)super.clone(); }
    catch(CloneNotSupportedException e) { return null; }
    clone._view = null;
    return clone;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get new RXElement
    XMLElement e = new XMLElement("binding");
    
    // Archive PropertyName, Key, ConversionKey
    e.add("aspect", getPropertyName());
    if(getKey()!=null && getKey().length()>0) e.add("key", getKey());
    if(getConversionKey()!=null && getConversionKey().length()>0) e.add("conversion-key", getConversionKey());
    
    // Archive Format
    //if(getFormat()!=null) e.add(anArchiver.toXML(getFormat()));
    
    // Return xml element
    return e;
}

/**
 * XML unarchival.
 */
public Binding fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive PropertyName, Key, ConversionKey
    setPropertyName(anElement.getAttributeValue("aspect"));
    setKey(anElement.getAttributeValue("key"));
    if(anElement.hasAttribute("conversion-key")) setConversionKey(anElement.getAttributeValue("conversion-key"));
    
    // Unarchive format
    /*if(anElement.get("format")!=null) { XMLElement formatXML = anElement.get("format"); Format format;
        if(formatXML.getAttributeValue("type")==null) format = null;
        else if(formatXML.getAttributeValue("type").equals("number"))
            format = anArchiver.fromXML(formatXML, RMNumberFormat.class, null);
        else format = anArchiver.fromXML(formatXML, RMDateFormat.class, null);
        setFormat(format);
    }*/

    // Return this binding
    return this;
}

/**
 * Returns a string representation.
 */
public String toString()
{
    String wname = getView()!=null? getView().getClass().getSimpleName() : null, pname = getPropertyName();
    return "Binding: " + wname + ": " + pname + " = " + getKey();
}

}