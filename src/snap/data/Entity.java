/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.data;
import java.util.*;
import snap.util.*;
import snap.web.WebFile;

/**
 * This class represents an entity for a data source. It has a list of properties, some of which are simple
 * attributes and some of which are relationships.
 */
public class Entity extends SnapObject implements JSONArchiver.GetKeys, XMLArchiver.Archivable {
    
    // The schema that owns this entity
    Schema             _schema;

    // Entity name
    String             _name;
    
    // Whether entity exists in data source
    boolean            _exists;
    
    // Entity properties
    List <Property>    _props = new ArrayList();
    
    // The class that this entity represents
    Class              _class;
    
    // The key/key-chain to the property(s) that returns best string description of an entity instance
    String             _descKey;
    
    // Cached lists of properties that are attributes (simple properties), relations, primaries, etc.
    List <Property>    _attrs, _relations, _primaries, _attrsSorted, _relationsSorted;
    
    // The source file
    WebFile            _source;
    
    // A Listener to catch child Property PropChanges
    PropChangeListener _propLsnr = pc -> propertyDidPropChange(pc);
    
    // Constants for properties
    final public static String Name_Prop = "Name";
    final public static String Exists_Prop = "Exists";

/**
 * Creates an empty entity.
 */
public Entity()  { }

/**
 * Creates an entity with the given name.
 */
public Entity(String aName)  { setName(aName); }

/**
 * Returns the schema that owns this entity.
 */
public Schema getSchema()  { return _schema; }

/**
 * Sets the schema that owns this entity.
 */
public void setSchema(Schema aSchema)  { _schema = aSchema; }

/**
 * Returns a named entity using entity resolver.
 */
public Entity getEntity(String aName)  { return _schema!=null? _schema.getEntity(aName) : null; }

/**
 * Returns the name of the entity.
 */
public String getName()  { return _name; }

/**
 * Sets the name of the entity.
 */
public void setName(String aName)
{
    String name = aName!=null? aName.trim().replace(" ", "") : null;
    if(SnapUtils.equals(name, _name)) return;
    firePropChange(Name_Prop, _name, _name = name);
}

/**
 * Returns whether entity exists in data source (has been saved and, if so, not deleted).
 */
public boolean getExists()  { return _exists; }

/**
 * Sets whether entity exists in data source (has been saved and, if so, not deleted).
 */
public void setExists(boolean aFlag)
{
    if(aFlag==_exists) return;
    firePropChange(Exists_Prop, _exists, _exists = aFlag);
}

/**
 * Returns the number of properties.
 */
public int getPropertyCount()  { return _props.size(); }

/**
 * Returns the property at the given index.
 */
public Property getProperty(int anIndex)  { return _props.get(anIndex); }

/**
 * Returns the list of properties.
 */
public List <Property> getProperties()  { return _props; }

/**
 * Sets a list of properties.
 */
public void setProperties(List <Property> theProps)
{
    while(getPropertyCount()>0) removeProperty(0);
    for(Property prop : theProps) addProperty(prop);
}

/**
 * Adds a given property.
 */
public void addProperty(Property aProperty)
{
    Property duplicate = getProperty(aProperty.getName());
    int index = duplicate==null? getPropertyCount() : removeProperty(duplicate);
    addProperty(aProperty, index);
}

/**
 * Adds a given property at given index.
 */
public void addProperty(Property aProperty, int anIndex)
{
    // Add property to list
    _props.add(anIndex, aProperty);
    aProperty.setEntity(this);  // Set Property.Entity to this
    aProperty.addPropChangeListener(_propLsnr);  // Start listening to PropertyChanges
    _attrs = _attrsSorted = _relations = _relationsSorted = _primaries = null;  // Reset cached lists
    
    // Fire property change event
    firePropChange("Property", null, aProperty, anIndex);
}

/**
 * Adds given properties.
 */
public void addProperty(Property ... theProperties)  { for(Property p : theProperties) addProperty(p); }

/**
 * Removes a property at given index.
 */
public Object removeProperty(int anIndex)
{
    // Remove property from list
    Property property = _props.remove(anIndex);
    property.removePropChangeListener(_propLsnr);  // Stop listening to PropertyChanges
    _attrs = _attrsSorted = _relations = _relationsSorted = _primaries = null;  // Reset cached lists

    // Fire property change event and return
    firePropChange("Property", property, null, anIndex);
    return property;
}

/**
 * Removes the given property.
 */
public int removeProperty(Property aProperty)
{
    int index = ListUtils.indexOfId(_props, aProperty);
    if(index>=0) removeProperty(index);
    return index;
}

/**
 * Returns the property with the given name.
 */
public Property getProperty(String aName)
{
    // Get name (if it has prefix of a standard accessor, strip is/get)
    String name = aName; if(name==null || name.length()==0) return null;
    if(name.startsWith("is") && name.length()>2 && Character.isUpperCase(name.charAt(2))) name = name.substring(2);
    if(name.startsWith("get") && name.length()>3 && Character.isUpperCase(name.charAt(3))) name = name.substring(3);
    
    // Iterate over properties and return the first that matches given name
    for(Property property : getProperties())
        if(name.equalsIgnoreCase(property.getStandardName()))
            return property;
    return null;  // Return null since not found
}

/**
 * Returns the number of attributes.
 */
public int getAttributeCount()  { return getAttributes().size(); }

/**
 * Returns the attribute at the given index.
 */
public Property getAttribute(int anIndex)  { return getAttributes().get(anIndex); }

/**
 * Returns the list of attributes.
 */
private List <Property> getAttributes()  { return _attrs!=null? _attrs : (_attrs=createAttributes()); }

/**
 * Creates the list of attributes.
 */
private List <Property> createAttributes()
{
    List <Property> attrs = new ArrayList();
    for(int i=0, iMax=getPropertyCount(); i<iMax; i++) if(getProperty(i).isAttribute()) attrs.add(getProperty(i));
    return attrs;
}

/**
 * Returns the attribute with the given name.
 */
public Property getAttribute(String aName)
{
    // Iterate over attributes and return first attribute with given name
    for(Property property : getAttributes())
        if(property.getName().equalsIgnoreCase(aName))
            return property;
    return null; // Return null since not found
}

/**
 * Returns the number of relations in the entity.
 */
public int getRelationCount()  { return getRelations().size(); }

/**
 * Returns the relation at the given index.
 */
public Property getRelation(int anIndex)  { return getRelations().get(anIndex); }

/**
 * Returns the list of relations in the entity.
 */
public List <Property> getRelations()  { return _relations!=null? _relations : (_relations=createRelations()); }

/**
 * Creates the list of relations in the entity.
 */
private List <Property> createRelations()
{
    List <Property> rels = new ArrayList();
    for(Property property : getProperties()) if(property.isRelation()) rels.add(property);
    return rels;
}

/**
 * Returns the relation for the given key path.
 */
public Property getRelation(String aName)
{
    Property prop = getProperty(aName);
    return prop!=null && prop.isRelation()? prop : null;
}

/**
 * Returns the attribute at the given index in a sorted attributes list.
 */
public Property getAttributeSorted(int anIndex)  { return getAttributesSorted().get(anIndex); }

/**
 * Returns the list of attributes sorted.
 */
private List <Property> getAttributesSorted()
{
    if(_attrsSorted==null) Collections.sort(_attrsSorted = new ArrayList(getAttributes()));
    return _attrsSorted;
}

/**
 * Returns the relation at the given index in the sorted list of relations.
 */
public Property getRelationSorted(int anIndex)  { return getRelationsSorted().get(anIndex); }

/**
 * Returns the list of relations sorted.
 */
private List <Property> getRelationsSorted()
{
    if(_relationsSorted==null) Collections.sort(_relationsSorted = new ArrayList(_relations));
    return _relationsSorted;
}

/**
 * Returns the primary key property.
 */
public Property getPrimary()  { List <Property> p = getPrimaries(); return p.size()>0? p.get(0) : null; }

/**
 * Returns the list of primary attributes for this entity.
 */
public List <Property> getPrimaries()
{
    // If primaries not loaded, load them
    if(_primaries==null) {
        _primaries = new ArrayList();
        for(Property property : getProperties()) if(property.isPrimary()) _primaries.add(property);
    }

    // Return primaries list
    return _primaries;
}

/**
 * Returns the class that this entity represents.
 */
public Class getEntityClass()  { return _class; }

/**
 * Sets the class that this entity represents.
 */
public void setEntityClass(Class aClass)  { _class = aClass; }

/**
 * Returns the property with the given name.
 */
public Property getKeyPathProperty(String aKeyPath)
{
    String pnames[] = aKeyPath!=null? aKeyPath.split("\\.") : new String[0]; Property prop = null;
    for(String pname : pnames) {
        Entity entity = prop!=null? prop.getRelationEntity() : this; if(entity==null) return null;
        prop = entity.getProperty(pname); if(prop==null) break; }
    return prop;
}

/** RMKey.Get implementation to return Property for key. */
//public Object getKeyValue(String k){ Property p = getProperty(k); return p!=null? p : RMKey.getValueImpl(this, k);}

/**
 * Returns the key/key-chain to the property(s) that returns best string description of an entity instance.
 */
public String getDescriptorKey()  { return _descKey; }

/**
 * Sets the key/key-chain to the property(s) that returns best string description of an entity instance.
 */
public void setDescriptorKey(String aValue)
{
    if(SnapUtils.equals(aValue, getDescriptorKey())) return;  // If already set, just return
    firePropChange("DescriptorKey", _descKey, _descKey = aValue);
}

/**
 * Returns a guess of descriptor key (or the actual one, if set).
 */
public String getDescriptorKeyGuess()
{
    // If actual descriptor key exists or "Name" property exists, return it
    if(getDescriptorKey()!=null) return getDescriptorKey();
    Property prop = getProperty("Name"); if(prop!=null) return prop.getName();
    
    // Return first String property or first property
    for(Property p : getProperties()) if(p.getType()==Property.Type.String) return p.getName();
    return getPropertyCount()>0? getProperty(0).getName() : null;
}

/**
 * PropChangeListener implementation to forward Property property changes to entity property change listener.
 */
protected void propertyDidPropChange(PropChange anEvent)  { firePropChange(anEvent); }

/**
 * Returns the source file.
 */
public WebFile getSourceFile()  { return _source; }

/**
 * Sets the source file.
 */
public void setSourceFile(WebFile aSource)  { _source = aSource; }

/**
 * Standard equals method.
 */
public boolean equals(Object anObj)
{
    // Check identity and get other entity
    if(anObj==this) return true;
    Entity other = anObj instanceof Entity? (Entity)anObj : null; if(other==null) return false;
    
    // Check Name, Properties
    if(!SnapUtils.equals(other._name, _name)) return false;
    if(!SnapUtils.equals(other._props, _props)) return false;
    return true;  // Return true since all checks passed
}

/**
 * Standard clone implementation.
 */
public Entity clone()
{
    // Do normal version, reset property list and clone properties
    Entity clone = (Entity)super.clone();
    clone._props = new ArrayList();
    for(Property property : getProperties()) clone.addProperty(property.clone());
    return clone;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get element named entity
    XMLElement e = new XMLElement("entity");
    
    // Archive Name and Properties
    if(_name!=null && _name.length()>0) e.add("name", _name);
    for(int i=0, iMax=getPropertyCount(); i<iMax; i++)
        e.add(getProperty(i).toXML(anArchiver));
        
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public Entity fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive Name and Properties
    _name = anElement.getAttributeValue("name", anElement.getName());
    for(int i=0, iMax=anElement.size(); i<iMax; i++)
        addProperty(new Property().fromXML(anArchiver, anElement.get(i)));
    
    // Return this entity
    return this;
}

/**
 * Returns bytes for this entity.
 */
public byte[] toBytes()
{
    String json = new JSONArchiver().writeObject(this).toString();
    return StringUtils.getBytes(json);
}

/**
 * Returns entity from bytes.
 */
public Entity fromBytes(byte theBytes[])
{
    String string = StringUtils.getString(theBytes);
    return (Entity)new JSONArchiver().setRootObject(this).readString(string);
}

/**
 * Saves the entity to its source.
 */
public void save() throws Exception  { getSchema().getSite().saveEntity(this); }

/**
 * Saves this entity from its source.
 */
public void delete() throws Exception  { getSchema().getSite().deleteEntity(this); }

/**
 * Returns keys to archive JSON.
 */
public List <String> getJSONKeys() { return Arrays.asList("Name", "Properties"); }

/**
 * Returns a string representation of entity (its name).
 */
public String toString()  { return getName(); }

}