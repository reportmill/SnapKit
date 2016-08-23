/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;
import snap.util.JSONArchiver.*;
import snap.web.WebFile;

/**
 * An object to get, set and store site settings.
 */
public class Settings extends SnapObject implements GetKeys, GetValue, SetValue, GetClass, Key.GetSet, WebFile.Updater {

    // The file for the source of settings
    WebFile          _file;
    
    // The file bytes after last read
    byte             _fileBytes[];

    // The parent settings
    Settings         _parent;
    
    // The key of this settings in parent settings
    String           _key;

    // The map
    Map              _map = new HashMap();

/**
 * Creates new Settings.
 */
public Settings()  { }

/**
 * Creates a new Settings from a byte source.
 */
public Settings(WebFile aFile)
{
    _file = aFile; _file.setProp("Settings", this);
    readFile();

    // Watch for file reverted
    aFile.addPropChangeListener(e -> {
        if(e.getPropertyName()==WebFile.Bytes_Prop && e.getNewValue()!=_fileBytes) readFile(); });
}

/**
 * Returns the source file.
 */
public WebFile getFile()  { return _parent!=null? _parent.getFile() : _file; }

/**
 * Reads the source file.
 */
private void readFile()
{
    _fileBytes = _file.getBytes(); if(_fileBytes==null) return;
    _map.clear();
    JSONArchiver archiver = new JSONArchiver().addImport("snap.util.*").setRootObject(this);
    String string = StringUtils.getString(_file.getBytes());
    string = string.replace("SnapSettings", "Settings");
    try { archiver.readString(string); }
    catch(Exception e) { System.err.println("Settings.createSettings: Couldn't read bytes"); }
}

/**
 * Returns a value.
 */
public Object get(String aKey)  { return get(aKey, (Object)null); }

/**
 * Returns a value.
 */
public Object get(String aKey, Object aDefault)
{
    int index = aKey.indexOf('.');
    String key = index>0? aKey.substring(0, index) : aKey, remainder = index>0? aKey.substring(index+1) : null;
    if(remainder!=null) { Settings settings = getSettings(key);
        return settings!=null? settings.get(remainder, aDefault) : aDefault; }
    Object value = _map.get(aKey);
    return value!=null? value : aDefault;
}

/**
 * Returns a value of a given class.
 */
public <T> T get(String aKey, Class<T> aClass)  { return ClassUtils.getInstance(get(aKey), aClass); }

/**
 * Sets a value.
 */
public void put(String aKey, Object aValue)
{
    int index = aKey.indexOf('.');
    String key = index>0? aKey.substring(0, index) : aKey, remainder = index>0? aKey.substring(index+1) : null;
    if(remainder!=null) {
        getSettings(key, true).put(remainder, aValue); return; }
    Object value = simplePut(aKey, aValue);
    if(!SnapUtils.equals(aValue, value))
        firePropChange(aKey, value, aValue, -1);
}

/**
 * Sets a value for a simple key without firing property change.
 */
protected Object simplePut(String aKey, Object aValue)
{
    return aValue!=null? _map.put(aKey, aValue) : _map.remove(aKey);
}

/**
 * Returns as a String value.
 */
public String getStringValue(String aKey)  { return getStringValue(aKey, null); }

/**
 * Returns as a String value.
 */
public String getStringValue(String aKey, String aDefault)  { return SnapUtils.stringValue(get(aKey, aDefault)); }

/**
 * Returns an bool value.
 */
public boolean getBoolValue(String aKey)  { return getBoolValue(aKey, false); }

/**
 * Returns an bool value.
 */
public boolean getBoolValue(String aKey, boolean aDefault)  { return SnapUtils.boolValue(get(aKey, aDefault)); }

/**
 * Returns an int value.
 */
public int getIntValue(String aKey)  { return getIntValue(aKey, 0); }

/**
 * Returns an int value.
 */
public int getIntValue(String aKey, int aDefault)  { return SnapUtils.intValue(get(aKey, aDefault)); }

/**
 * Returns a float value.
 */
public float getFloatValue(String aKey)  { return getFloatValue(aKey, 0); }

/**
 * Returns a float value.
 */
public float getFloatValue(String aKey, float aDefault)  { return SnapUtils.floatValue(get(aKey, aDefault)); }

/**
 * Returns a List value.
 */
public SettingsList getList(String aKey)  { return getList(aKey, false); }

/**
 * Returns a List value, with option to create if missing.
 */
public SettingsList getList(String aKey, boolean doCreate)
{
    int index = aKey.indexOf('.');
    String key = index>0? aKey.substring(0, index) : aKey, remainder = index>0? aKey.substring(index+1) : null;
    if(remainder!=null) { Settings settings = getSettings(key, doCreate);
        return settings!=null? settings.getList(remainder, doCreate) : null; }
    Object value = get(aKey); if(value instanceof SettingsList) return (SettingsList)value;
    if(value==null && !doCreate) return null;
    SettingsList jsList = new SettingsList(); jsList._key = aKey;
    if(value instanceof List) jsList._list.addAll((List)value);
    simplePut(aKey, jsList);
    return jsList;
}

/**
 * Adds a list item.
 */
public void addListItem(String aKey, Object aValue)  { getList(aKey, true).add(aValue); }

/**
 * Adds a list item at given index.
 */
public void addListItem(String aKey, Object aValue, int anIndex)  { getList(aKey, true).add(anIndex, aValue); }

/**
 * Removes a list item at given index.
 */
public Object removeListItem(String aKey, int anIndex)  { return getList(aKey).remove(anIndex); }

/**
 * Removes a list item at given index.
 */
public int removeListItem(String aKey, Object aValue)
{
    List list = getList(aKey); if(list==null) return -1;
    int index = list.indexOf(aValue);
    if(index>=0) removeListItem(aKey, index);
    return index;
}

/**
 * Returns the settings for key.
 */
public Settings getSettings(String aKey)  { return getSettings(aKey, false); }

/**
 * Returns the settings for key, with option to create if missing.
 */
public Settings getSettings(String aKey, boolean doCreate)
{
    Object value = get(aKey); if(value instanceof Settings) return (Settings)value;
    if(value==null && !doCreate) return null;
    Settings settings = new Settings();
    settings._parent = this; settings._key = aKey;
    if(value instanceof Map) { Map<String,Object> map = (Map)value;
        for(Map.Entry<String,Object> entry : map.entrySet())
            settings.simplePut(entry.getKey(), entry.getValue()); }
    simplePut(aKey, settings);
    return settings;
}

/**
 * Override to forward to parents if present.
 */
protected void firePropChange(String aName, Object oldVal, Object newVal, int anIndex)
{
    if(_parent!=null) _parent.firePropChange(_key + '.' + aName, oldVal, newVal, anIndex);
    else {
        super.firePropChange(aName, oldVal, newVal, anIndex);
        getFile().setUpdater(this);
    }
}

/**
 * The List.
 */
public class SettingsList<E> extends AbstractList<E> {

    // The real list
    List <E>    _list = new ArrayList();

    // The key
    String      _key;

    /** Return list size. */
    public int size()  { return _list.size(); }
    
    /** Return list item at index. */
    public E get(int anIndex)  { return _list.get(anIndex); }
    
    /** Add list item. */
    public void add(int anIndex, E anItem)
    {
        _list.add(anIndex, anItem);
        firePropChange(_key, null, anItem, anIndex);
    }
    
    /** Remove list item. */
    public E remove(int anIndex)
    {
        E item = _list.remove(anIndex);
        firePropChange(_key, item, null, anIndex);
        return item;
    }
    
    /** Returns settings at index. */
    public Settings getSettings(int anIndex, boolean doCreate)
    {
        Object value = anIndex<size() || !doCreate? get(anIndex) : null;
        if(value instanceof Settings) return (Settings)value;
        Settings settings = new Settings();
        settings._parent = Settings.this; settings._key = _key + '[' + anIndex + ']';
        if(value instanceof Map) { Map<String,Object> map = (Map)value;
            for(Map.Entry<String,Object> entry : map.entrySet())
                settings.simplePut(entry.getKey(), entry.getValue()); }
        if(anIndex<size())
            _list.set(anIndex, (E)settings);
        else add(anIndex, (E)settings);
        return settings;
    }
}

public String getJSONClass()  { return _parent!=null? HashMap.class.getName() : getClass().getName(); }
public void setJSONValue(String aKey, Object aValue)  { simplePut(aKey, aValue); }
public Object getJSONValue(String aKey)  { return get(aKey); }
public Collection <String> getJSONKeys()  { return _map.keySet(); }
public Object getKeyValue(String aKey)  { return get(aKey); }
public boolean setKeyValue(String aKey, Object aValue)  { put(aKey, aValue); return true; }

/**
 * Standard toString implementation.
 */
public String toString()
{
    JSONArchiver archiver = new JSONArchiver();
    return archiver.writeObject(this).toString();
}

/**
 * Saves the file.
 */
public void updateFile(WebFile aFile)
{
    _fileBytes = StringUtils.getBytes(toString());
    aFile.setBytes(_fileBytes);
}

/**
 * Returns the settings for given file.
 */
public static synchronized Settings get(WebFile aFile)
{
    Settings stgs = (Settings)aFile.getProp("Settings");
    if(stgs==null) stgs = new Settings(aFile);
    return stgs;
}

}