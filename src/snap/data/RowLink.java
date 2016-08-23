/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.data;
import java.util.*;
import snap.util.SnapUtils;

/**
 * This object represents a relation between a source row and a destination row or rows.
 */
public class RowLink {

    // The source row
    Row               _row;
    
    // The relation property
    Property          _relation;
    
    // The remote primary value
    Object            _remoteValue;
    
    // The remote row
    Row               _remoteRow;
    
    // The remote values
    List              _remoteValues;
    
    // The remote rows
    List <Row>        _remoteRows;

/**
 * Creates a new RowLink for a given row, a relation, a remote value and a remote row.
 */
public RowLink(Row aRow, Property aRelation, Object aRemoteValue)
{
    // Set row and relation
    _row = aRow;
    _relation = aRelation;
    
    // Set remote value based on type
    if(aRemoteValue instanceof Row)
        _remoteRow = (Row)aRemoteValue;
    else if(aRemoteValue instanceof List) { List list = (List)aRemoteValue;
        if(list.size()>0 && list.get(0) instanceof Row)
            _remoteRows = list;
        else _remoteValues = list;
    }
    else _remoteValue = aRemoteValue;
}

/**
 * Returns the row.
 */
public Row getRow()  { return _row; }

/**
 * Returns the data site.
 */
public DataSite getSite()  { return _row.getSite(); }

/**
 * Returns the relation.
 */
public Property getRelation()  { return _relation; }

/**
 * Returns whether relation is to-one.
 */
public boolean isToOne()  { return !_relation.isToMany(); }

/**
 * Returns whether relation is to-many.
 */
public boolean isToMany()  { return _relation.isToMany(); }

/**
 * Returns the remote value.
 */
public Object getRemoteValue()  { return _remoteValue!=null? _remoteValue : (_remoteValue=getRemoteValueImpl()); }

/**
 * Returns the remote value.
 */
private Object getRemoteValueImpl()
{
    if(_remoteRow==null) return null;
    return _remoteRow.get(getRelation().getRelationRemoteProperty());
}

/**
 * Returns the remote values.
 */
public List getRemoteValues()  { return _remoteValues!=null? _remoteValues : (_remoteValues=getRemoteValuesImpl()); }

/**
 * Returns the remote values.
 */
private List getRemoteValuesImpl()
{
    List <Row> rows = getRemoteRows(); if(rows==null) return Collections.emptyList();
    List values = new ArrayList(rows.size());
    for(Row row : rows) values.add(row.getPrimaryValue());
    return values;
}

/**
 * Returns the remote value or values.
 */
public Object getRemoteValueOrValues()  { return isToOne()? getRemoteValue() : getRemoteValues(); }

/**
 * Returns whether RemoteRow or RemoteRows is set.
 */
public boolean isRemoteRowSet()  { return (isToOne()? _remoteRow : _remoteRows)==null; }

/**
 * Returns the remote row.
 */
public Row getRemoteRow()  { return _remoteRow!=null? _remoteRow : (_remoteRow=getRemoteRowImpl()); }

/**
 * Returns the remote row.
 */
private Row getRemoteRowImpl()
{
    if(_remoteValue==null) return null;
    return getSite().getRow(getRelation().getRelationEntity(), _remoteValue);
}

/**
 * Returns the remote rows.
 */
public List <Row> getRemoteRows()  { return _remoteRows!=null? _remoteRows : (_remoteRows=getRemoteRowsImpl()); }

/**
 * Returns the remote rows.
 */
private List <Row> getRemoteRowsImpl()
{
    Query query = new Query(getRelation().getRelationEntity());
    String remotePropertyName = getRelation().getRelationRemotePropertyName();
    query.addCondition(remotePropertyName, Condition.Operator.Equals, getRow().getPrimaryValue());
    List <Row> rows = getSite().getRows(query);
    return rows;
}

/**
 * Returns the remote row or rows.
 */
public Object getRemoteRowOrRows()  { return isToOne()? getRemoteRow() : getRemoteRows(); }

/**
 * Returns whether this RowLink can be resolved.
 */
public boolean isResolved()
{
    if(isToOne()) return getRemoteValue()!=null || getRemoteRow()==null;
    List values = getRemoteValues(); for(Object value : values) if(value==null) return false;
    return true;
}

/**
 * Returns any unresolved rows.
 */
public Row[] getUnresolvedRows()
{
    if(isResolved()) return null;
    if(isToOne()) return new Row[] { getRemoteRow() };
    List values = getRemoteValues(); List <Row> rows = getRemoteRows(), urows = new ArrayList();
    for(int i=0, iMax=values.size(); i<iMax; i++)
        if(values.get(i)==null)
            urows.add(rows.get(i));
    return urows.toArray(new Row[urows.size()]);
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity and get other RowLink
    if(anObj==this) return true;
    RowLink other = anObj instanceof RowLink? (RowLink)anObj : null; if(other==null) return false;
    
    // Check Relation
    if(other.getRelation()!=getRelation()) return false;
    
    // Handle ToOne
    if(isToOne()) {
        Object v1 = getRemoteValue(), v2 = other.getRemoteValue();
        if(v1==null && v2==null) return getRemoteRow()==other.getRemoteRow();
        return SnapUtils.equals(v1, v2);
    }
    
    // Handle ToMany
    return other.getRemoteValues().equals(getRemoteValues());
}

/**
 * Standard hashcode implementation.
 */
public int hashCode()  { Property r = getRelation(); return r!=null? r.hashCode() : 0; }

}