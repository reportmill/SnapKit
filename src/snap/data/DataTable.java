/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.data;
import java.util.*;
import snap.util.SnapObject;

/**
 * A class to front for a table of data.
 */
public class DataTable extends SnapObject {

    // The data source
    DataSite                      _site;
    
    // The entity
    Entity                       _entity;
    
    // The default table view
    DataTableView                _tview;
    
    // The rows that have been loaded locally
    Map <Object,Row>             _localRows = new HashMap();
    
    // A map of properties associated with file
    Map                          _props = new HashMap();
    
    // Constants for property changes
    static final String LocalRow_Prop = "LocalRow";
    
/**
 * Returns the data site for this table.
 */
public DataSite getSite()  { return _site; }

/**
 * Returns the data site for this table.
 */
protected void setSite(DataSite aSite)  { _site = aSite; }

/**
 * Returns the table name.
 */
public String getName()  { return getEntity().getName(); }

/**
 * Returns the entity for this table.
 */
public Entity getEntity()  { return _entity; }

/**
 * Sets the entity.
 */
protected void setEntity(Entity anEntity)  { _entity = anEntity; }

/**
 * Returns a file property for key.
 */
public Object getProp(String aKey)  { return _props.get(aKey); }

/**
 * Sets a property for a key.
 */
public void setProp(String aKey, Object aValue)  { _props.put(aKey, aValue); }

/**
 * Returns a local row for a primary value.
 */
public synchronized Row getLocalRow(Object aPrimaryValue)  { return _localRows.get(aPrimaryValue); }

/**
 * Adds a local row.
 */
protected synchronized void addLocalRow(Row aRow)
{
    // Put row (just return if identical)
    Row old = _localRows.put(aRow.getPrimaryValue(), aRow); if(aRow==old || !aRow.getExists()) return;
    firePropChange(LocalRow_Prop, aRow, old);
}

/**
 * Removes a local row.
 */
protected synchronized void removeLocalRow(Row aRow)
{
    // Remove row
    Row old = _localRows.remove(aRow.getPrimaryValue()); if(old==null) return;
    firePropChange(LocalRow_Prop, null, old);
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    return "DataTable { site=\"" + getSite().getURLString() + "\", entity=\"" + _entity.getName() + "\" }";
}

}