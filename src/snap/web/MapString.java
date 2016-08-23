/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.*;

/**
 * A class for working with parameter strings as found in a URL query part or fragment id, typically with the format:
 * field1=value1&field2=value2
 */
public class MapString {

    // The string
    String         _string;
    
    // The list of field strings and value string
    List <String>  _fields, _values;

/**
 * Creates a new MapString for given string.
 */
public MapString(String aString)  { _string = aString!=null? aString : ""; }

/**
 * Returns the string.
 */
public String getString()  { return _string!=null? _string : (_string=createString()); }

/**
 * Creates the string from fields/values.
 */
protected String createString()
{
    StringBuffer sb = new StringBuffer();
    for(String field : getFields()) { String value = getValue(field);
        if(sb.length()>0) sb.append('&'); sb.append(field);
        if(value.length()>0) sb.append('=').append(value);
    }
    return sb.toString();
}

/**
 * Returns the number of fields.
 */
public int getFieldCount()  { return getFields().size(); }

/**
 * Returns the individual field at given index.
 */
public String getField(int anIndex)  { return getFields().get(anIndex); }

/**
 * Returns the individual value at given index.
 */
public String getValue(int anIndex)  { return _values.get(anIndex); }

/**
 * Returns the fields list.
 */
public List <String> getFields()  { if(_fields==null) loadFields(); return _fields; }

/**
 * Returns a field index for given field string.
 */
public int getFieldIndex(String aName)
{
    for(int i=0, iMax=getFieldCount(); i<iMax; i++) if(aName.equals(getField(i))) return i;
    return -1;
}

/**
 * Returns a field value string for given field string.
 */
public String getValue(String aFieldName)
{
    int index = getFieldIndex(aFieldName);
    return index>=0? getValue(index) : null;
}

/**
 * Sets a field value for given field name and value.
 */
public MapString setValue(String aFieldName, Object aValue)
{
    String value = aValue!=null? aValue.toString() : null;
    int index = getFieldIndex(aFieldName);
    if(index>=0) {
        if(value!=null) _values.set(index, value);
        else { _fields.remove(index); _values.remove(index); }
    } else if(value!=null) { _fields.add(aFieldName); _values.add(value); }
    _string = null;
    return this;
}

/**
 * Creates the fields list.
 */
protected void loadFields()
{
    _fields = new ArrayList(); _values = new ArrayList();
    String fieldValues[] = _string.split("&");
    for(String fieldValue : fieldValues) {
        String parts[] = fieldValue.split("="); if(parts[0].length()==0) continue;
        _fields.add(parts[0]);
        _values.add(parts.length>1? parts[1] : "");
    }
}

/**
 * Standard toString implementation.
 */
public String toString()  { return getString(); }

}