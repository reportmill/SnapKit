/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.data;
import java.util.*;
import snap.util.*;

/**
 * This class represents a fetch of a set of data table rows for a given query.
 */
public class DataTableView implements PropChangeListener {

    // The name of this view
    String             _name;

    // The data table
    DataTable          _table;
    
    // The query
    Query              _query;

    // The data rows
    List <Row>         _rows;
    
/**
 * Returns the data site.
 */
public DataSite getSite()  { return _table.getSite(); }

/**
 * Returns the table.
 */
public DataTable getTable()  { return _table; }

/**
 * Returns the table.
 */
protected void setTable(DataTable aTable)  { _table = aTable; }

/**
 * Returns the entity for the table file.
 */
public Entity getTableEntity()  { return getTable().getEntity(); }

/**
 * Returns the table view name.
 */
public String getName()  { return _name; }

/**
 * Sets the table view name.
 */
protected void setName(String aName)  { _name = aName; }

/**
 * Returns the query.
 */
public Query getQuery()  { return _query!=null? _query : (_query=createQuery()); }

/**
 * Sets the query.
 */
public void setQuery(Query aQuery)  { _query = aQuery; }

/**
 * Creates the default query.
 */
protected Query createQuery()  { return new Query(getTableEntity()); }

/**
 * Returns whether rows for this table view have been set.
 */
public boolean isRowsSet()  { return _rows!=null; }

/**
 * Returns the list of rows for this table view.
 */
public synchronized List <Row> getRows()  { return _rows!=null? _rows : (_rows = getRowsImpl()); }

/**
 * Returns the list of rows for this table view.
 */
protected List <Row> getRowsImpl()  { return getSite().getRows(getQuery()); }

/**
 * Adds a row.
 */
protected void addRow(Row aRow)  { _rows.add(aRow); }

/**
 * Removes a row.
 */
protected void removeRow(Row aRow)  { _rows.remove(aRow); }

/**
 * Clears existing objects from this table.
 */
public void refresh()  { _query = null; _rows = null; }

/**
 * Property change.
 */
public void propertyChange(PropChange anEvent)
{
    // Handle LocalRow Add/Remove
    if(anEvent.getPropertyName()==DataTable.LocalRow_Prop) {
        Row orow = (Row)anEvent.getOldValue(), nrow = (Row)anEvent.getNewValue();
        
        // Handle LocalRow Add
        if(nrow!=null) {
            if(!isRowsSet()) return;
            Condition condition = getQuery().getCondition();
            if(condition==null || condition.getValue(getTableEntity(), nrow))
                addRow(nrow);
        }
        
        // Handle LocalRow Remove
        else if(orow!=null) {
            if(!isRowsSet()) return;
            Condition condition = getQuery().getCondition();
            if(condition==null || condition.getValue(getTableEntity(), orow))
                removeRow(orow);
        }
    }
}

/**
 * Returns a map of TableViews for given Table.
 */
private static Map <String,DataTableView> getTableViewsMap(DataTable aTable)
{
    Map tviews = (Map)aTable.getProp("TableViews");
    if(tviews==null) aTable.setProp("TableViews", tviews=new HashMap());
    return tviews;
}

/**
 * Returns the known table views.
 */
public static List <? extends DataTableView> getTableViews(DataTable aTable)
{
    return new ArrayList(getTableViewsMap(aTable).values());
}

/**
 * Returns a named table view.
 */
public static DataTableView getTableView(DataTable aTable, String aName)  { return getTableView(aTable, aName, false); }

/**
 * Returns a named table view.
 */
public static synchronized DataTableView getTableView(DataTable aTable, String aName, boolean doCreate)
{
    Map <String,DataTableView> tviews = getTableViewsMap(aTable);
    DataTableView tview = tviews.get(aName);
    if(tview==null && doCreate) {
        tview = new DataTableView(); tview.setTable(aTable); tview.setName(aName); tviews.put(aName, tview);
        aTable.addPropChangeListener(tview);
    }
    return tview;
}

}