package snap.data;
import java.util.*;
import snap.util.*;
import snap.web.*;

/**
 * This class performs data retrieval and udpates on a WebSite.
 */
public class DataSite extends SnapObject implements PropChangeListener {

    // The WebSite
    WebSite                   _wsite;
    
    // The schema
    Schema                    _schema;
    
    // The entities
    Map <String, Entity>      _entities = new HashMap();
    
    // The DataTables
    Map <String,DataTable>    _dataTables = new HashMap();
    
/**
 * Returns the WebSite.
 */
public WebSite getWebSite()  { return _wsite; }

/**
 * Returns the name.
 */
public String getName()  { return _wsite.getName(); }

/**
 * Returns the URL root.
 */
public String getURLString()  { return _wsite.getURLString(); }

/**
 * Returns the schema of represented WebSite as a hierarchy of RMEntity and RMProperty objects.
 */
public synchronized Schema getSchema()
{
    if(_schema==null) {
        _schema = createSchema(); _schema.setName(getName()); _schema.setSite(this); }
    return _schema;
}

/**
 * Creates the schema.
 */
protected Schema createSchema()  { return new Schema(); }

/**
 * Creates an entity for given name.
 */
public synchronized Entity createEntity(String aName)
{
    // If entity already exists, just return it
    Entity entity = _entities.get(aName); if(entity!=null) return entity;
    
    // Create and add entity
    _entities.put(aName, entity = createEntityImpl(aName));
    entity.setName(aName);
    entity.setSchema(getSchema());
    return entity;
}

/**
 * Returns the entity for given name.
 */
protected Entity createEntityImpl(String aName)  { return new Entity(); }

/**
 * Returns the entity for given name.
 */
public synchronized Entity getEntity(String aName)
{
    // Get entity from files cache
    Entity entity = _entities.get(aName);
    if(entity!=null && entity.getExists())
        return entity;
    
    // Get entity for name from data source
    try { entity = getEntityImpl(aName); }
    catch(Exception e) { throw new RuntimeException(e); }
    
    // If found, set Exists to true
    if(entity!=null) {
        entity.setExists(true);
        getSchema().addEntity(entity);
    }
    
    // Return entity
    return entity;
}

/**
 * Returns the entity for given name.
 */
protected Entity getEntityImpl(String aName) throws Exception
{
    WebFile efile = _wsite.getFile("/" + aName + ".table");
    if(efile!=null) {
        Entity entity = createEntity(efile.getSimpleName());
        try { return entity.fromBytes(efile.getBytes()); }
        catch(Exception e) { throw new RuntimeException(e); }
    }
    return null;
}

/**
 * Saves the given entity.
 */
public void saveEntity(Entity anEntity) throws Exception
{
    saveEntityImpl(anEntity);
    if(!anEntity.getExists()) {
        anEntity.setExists(true);
        getSchema().addEntity(anEntity);
    }
}

/**
 * Saves the given entity.
 */
protected void saveEntityImpl(Entity anEntity) throws Exception
{
    WebFile efile = anEntity.getSourceFile(); if(efile==null) return;
    efile.setBytes(anEntity.toBytes());
    efile.save();
}

/**
 * Saves the given entity.
 */
public void deleteEntity(Entity anEntity) throws Exception
{
    deleteEntityImpl(anEntity);
    anEntity.setExists(false);
    getSchema().removeEntity(anEntity);
}

/**
 * Saves the given entity.
 */
protected void deleteEntityImpl(Entity anEntity) throws Exception
{
    WebFile efile = anEntity.getSourceFile(); if(efile==null) return;
    efile.delete();
}

/**
 * Returns the list of known data tables.
 */
public synchronized List <DataTable> getDataTables()  { return new ArrayList(_dataTables.values()); }

/**
 * Returns the DataTable for given name
 */
public synchronized DataTable getDataTable(String aName)
{
    DataTable dtable = _dataTables.get(aName);
    if(dtable==null) {
        dtable = createDataTable(aName); if(dtable==null) return null;
        _dataTables.put(aName, dtable);
    }
    return dtable;
}

/**
 * Returns the DataTable for given name.
 */
protected DataTable createDataTable(String aName)
{
    Entity entity = getEntity(aName); if(entity==null) return null;
    DataTable table = createDataTableImpl(); table.setSite(this); table.setEntity(entity);
    return table;
}

/**
 * Creates an instance of DataTable.
 */
protected DataTable createDataTableImpl()  { return new DataTable(); }

/**
 * Returns a row for an entity and primary value that is guaranteed to be unique for this data source.
 */
public Row createRow(Entity anEntity, Object aPrimaryValue)  { return createRow(anEntity, aPrimaryValue, null); }

/**
 * Returns a row for an entity and primary value that is guaranteed to be unique for this data source.
 */
public synchronized Row createRow(Entity anEntity, Object aPrimaryValue, Map aMap)
{
    // If PrimaryValue provided, check/set LocalRows cache
    Row row = null;
    if(aPrimaryValue!=null) {
        DataTable dtable = getDataTable(anEntity.getName());
        row = dtable.getLocalRow(aPrimaryValue); if(row!=null) return row;
        row = createRowImpl(anEntity, aPrimaryValue); row.setSite(this); row.setEntity(anEntity);
        row.put(anEntity.getPrimary(), aPrimaryValue);
        dtable.addLocalRow(row);
    }
    
    // Otherwise just create row
    else { row = createRowImpl(anEntity, null); row.setSite(this); row.setEntity(anEntity); }
    
    // Initialize values, start listening to PropertyChanges and return
    row.initValues(aMap);
    row.addPropChangeListener(this);
    return row;
}

/**
 * Creates a new row for source.
 */
protected Row createRowImpl(Entity anEntity, Object aPrimaryValue)  { return new Row(); }

/**
 * Returns a row for a given entity and primary value.
 */
public synchronized Row getRow(Entity anEntity, Object aPrimaryValue)
{
    // Make sure PrimaryValue is non-null
    assert(aPrimaryValue!=null);
    
    // See if there is a local row - if so return it
    DataTable dtable = getDataTable(anEntity.getName());
    Row row = dtable.getLocalRow(aPrimaryValue);
    if(row!=null && row.getExists())
        return row;
    
    // Fetch row - if found, set exists
    row = getRowImpl(anEntity, aPrimaryValue);
    if(row!=null)
        row.setExists(true);

    // Return row
    return row;
}

/**
 * Returns a row for a given entity and primary value.
 */
protected Row getRowImpl(Entity anEntity, Object aPrimaryValue)
{
    Query query = new Query(anEntity);
    query.addCondition(anEntity.getPrimary().getName(), Condition.Operator.Equals, aPrimaryValue);
    return getRow(query);
}

/**
 * Returns a row for given query.
 */
public Row getRow(Query aQuery)  { List <Row> rows = getRows(aQuery); return rows.size()>0? rows.get(0) : null; }

/**
 * Returns a set of rows for the given properties and condition.
 */
public synchronized List <Row> getRows(Query aQuery)
{
    // Get query entity (just return if null)
    String ename = aQuery.getEntityName();
    Entity entity = getEntity(ename); if(entity==null) return null;
    
    // Fetch rows, set Exists and return
    List <Row> rows; try { rows = getRowsImpl(entity, aQuery); }
    catch(Exception e) { throw new RuntimeException(e); }
    for(Row row : rows) row.setExists(true);
    return rows;
}

/**
 * Returns a set of rows for the given properties and condition.
 */
protected List <Row> getRowsImpl(Entity anEntity, Query aQuery) throws Exception { throw notImpl("getRowsImpl"); }

/**
 * Inserts or updates a given row.
 */
public synchronized void saveRow(Row aRow) throws Exception
{
    // If row exists and hasn't changed, just return
    boolean exists = aRow.getExists(); if(exists && !aRow.isModified()) return;
    
    // If there are UnresolvedRelationRows, make sure they get saved
    Row urows[] = aRow.getUnresolvedRelationRows();
    if(urows!=null) {
        if(!exists) { saveRowImpl(aRow); aRow.setExists(true); } // Save this row first in case of circular reference
        for(Row urow : urows)
            urow.save();
    }

    // Save row for real
    saveRowImpl(aRow);
    
    // Set row exists and not modified and add to DataTable
    aRow.setExists(true);
    aRow.setModified(false);
    if(!exists) {
        DataTable dtable = getDataTable(aRow.getEntity().getName());
        dtable.addLocalRow(aRow);
    }
}

/**
 * Inserts or updates a given row.
 */
protected void saveRowImpl(Row aRow) throws Exception  { throw notImpl("saveRowImpl"); }

/**
 * Deletes a given row.
 */
public synchronized void deleteRow(Row aRow) throws Exception
{
    // Delete row
    deleteRowImpl(aRow);
    
    // Set Exists to false and remove from table
    aRow.setExists(false);
    DataTable dtable = getDataTable(aRow.getEntity().getName());
    dtable.removeLocalRow(aRow);    
}

/**
 * Deletes a given row.
 */
protected void deleteRowImpl(Row aRow) throws Exception  { throw notImpl("deleteRowImpl"); }

/**
 * Handle property changes on row objects by forwarding to listener.
 */
public void propertyChange(PropChange anEvent)
{
    // Forward to deep change listeners
    for(int i=0, iMax=getListenerCount(DeepChangeListener.class); i<iMax; i++)
        getListener(DeepChangeListener.class, i).deepChange(this, anEvent);
}

/**
 * Clears site Schema and ClassLoader.
 */
public synchronized void refresh()  { _schema = null; _wsite.refresh(); }

/**
 * Flushes any unsaved changes to backing store.
 */
public void flush() throws Exception  { _wsite.flush(); }

/** Returns a "not implemented" exception for string (method name). */
private Exception notImpl(String aStr)  { return new Exception(getClass().getName() + ": Not implemented:" + aStr); }

/**
 * Returns a DataSite for given WebSite.
 */
public static DataSite get(WebSite aSite)
{
    DataSite dsite = _dsites.get(aSite);
    if(dsite==null) {
        dsite = new FileDataSite(); dsite._wsite = aSite;
        _dsites.put(aSite, dsite);
    }
    return dsite;
}
static Map <WebSite,DataSite> _dsites = new HashMap();

}