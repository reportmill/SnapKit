/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.ListUtils;
import java.util.*;

/**
 * A class for working with parameter strings as found in a URL query part or fragment id, typically with the format:
 * field1=value1&field2=value2
 */
public class ParsedUrlArgs {

    // The string
    private String  _string;
    
    // The list of field strings
    private List <String>  _fields;

    // The list of value strings
    private List <String>  _values;

    /**
     * Constructor for given string.
     */
    public ParsedUrlArgs(String aString)
    {
        _string = aString != null? aString : "";
    }

    /**
     * Returns the string.
     */
    public String getString()
    {
        if (_string != null) return _string;
        return _string = createString();
    }

    /**
     * Creates the string from fields/values.
     */
    protected String createString()
    {
        List<String> fields = getFields();
        StringBuilder sb = new StringBuilder();

        for (String field : fields) {
            String value = getValue(field);
            if (sb.length() > 0)
                sb.append('&');
            sb.append(field);
            if (value.length() > 0)
                sb.append('=').append(value);
        }

        return sb.toString();
    }

    /**
     * Returns the individual value at given index.
     */
    public String getValue(int anIndex)  { return _values.get(anIndex); }

    /**
     * Returns the fields list.
     */
    public List <String> getFields()
    {
        if (_fields != null) return _fields;
        loadFields();
        return _fields;
    }

    /**
     * Returns a field index for given field string.
     */
    public int getFieldIndex(String aName)
    {
        List<String> fields = getFields();
        return ListUtils.findMatchIndex(fields, field -> aName.equals(field));
    }

    /**
     * Returns a field value string for given field string.
     */
    public String getValue(String aFieldName)
    {
        int fieldIndex = getFieldIndex(aFieldName);
        return fieldIndex >= 0 ? getValue(fieldIndex) : null;
    }

    /**
     * Sets a field value for given field name and value.
     */
    public ParsedUrlArgs setValue(String aFieldName, Object aValue)
    {
        String value = aValue != null ? aValue.toString() : null;
        int fieldIndex = getFieldIndex(aFieldName);
        if (fieldIndex >= 0) {
            if (value != null)
                _values.set(fieldIndex, value);
            else {
                _fields.remove(fieldIndex);
                _values.remove(fieldIndex);
            }
        }
        else if (value != null) {
            _fields.add(aFieldName);
            _values.add(value);
        }
        _string = null;
        return this;
    }

    /**
     * Creates the fields list.
     */
    protected void loadFields()
    {
        _fields = new ArrayList<>();
        _values = new ArrayList<>();
        String[] fieldValues = _string.split("&");

        for(String fieldValue : fieldValues) {
            String[] parts = fieldValue.split("=");
            if(parts[0].length() == 0)
                continue;
            _fields.add(parts[0]);
            _values.add(parts.length > 1 ? parts[1] : "");
        }
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()  { return getString(); }
}