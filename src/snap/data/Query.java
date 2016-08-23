/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.data;
import java.util.*;
import snap.util.*;

/**
 * An object to fetch a dataset from a data source entity with a set condition and parameters.
 */
public class Query extends SnapObject implements JSONArchiver.GetKeys, PropChangeListener {

    // The data source entity to fetch from
    Entity             _entity;
    
    // The data source entity name to fetch from
    String             _entityName;
    
    // The data condition
    Condition          _condition;
    
    // A list of sorts
    List <Sort>        _sorts = new ArrayList();
    
    // The fetch limit
    int                _fetchLimit = getFetchLimitDefault();
    
    // Constants for PropertyChange
    public static final String Condition_Prop = "Condition";
    public static final String Sort_Prop = "Sort";

/**
 * Creates a new query.
 */
public Query()  { }

/**
 * Creates a new query with given entity name.
 */
public Query(String anEntityName)  { setEntityName(anEntityName); }

/**
 * Creates a new query with given entity.
 */
public Query(Entity anEntity)  { setEntityName(anEntity.getName()); }

/**
 * Returns the data source entity name.
 */
public String getEntityName()  { return _entityName; }

/**
 * Sets the data source entity name.
 */
public void setEntityName(String anEntityName)  { _entityName = anEntityName; }

/**
 * Returns the condition.
 */
public Condition getCondition()  { return _condition; }

/**
 * Sets the condition.
 */
public void setCondition(Condition aCondition)
{
    // If old condition, stop listening to PropertyChange, if new one, start listening
    if(_condition!=null) _condition.removePropChangeListener(this);
    if(aCondition!=null) aCondition.addPropChangeListener(this);
    
    // Set condition and fire property change
    firePropChange(Condition_Prop, _condition, _condition = aCondition);
}

/**
 * Adds a condition to query.
 */
public Query addCondition(Condition aCondition)
{
    // Handle if current condition isn't set
    if(getCondition()==null)
        setCondition(aCondition);
    
    // Handle if current condition is already a list
    else if(getCondition() instanceof ConditionList)
        ((ConditionList)getCondition()).addCondition(Condition.Operator.And, aCondition);
    
    // Handle if current condition is simple (create condition list with current and new and set)
    else {
        Condition condition = getCondition();
        ConditionList conditionList = new ConditionList();
        conditionList.addCondition(Condition.Operator.And, condition);
        conditionList.addCondition(Condition.Operator.And, aCondition);
        setCondition(conditionList);
    }
    
    // Return this query
    return this;
}

/**
 * Adds a condition to query for given property name, operator and value.
 */
public Query addCondition(String aPropertyName, Condition.Operator anOperator, Object aValue)
{
    return addCondition(new Condition(aPropertyName, anOperator, aValue));
}

/**
 * Returns the fetch limit.
 */
public int getFetchLimit()  { return _fetchLimit; }

/**
 * Sets the fetch limit.
 */
public void setFetchLimit(int aValue)  { _fetchLimit = aValue; }

/**
 * Returns the fetch limit default (Integer.MAX_VALUE).
 */
public int getFetchLimitDefault()  { return Integer.MAX_VALUE; }

/**
 * Returns the number of sorts.
 */
public int getSortCount()  { return getSorts().size(); }

/**
 * Returns the list of sorts.
 */
public List <Sort> getSorts()  { return _sorts; }

/**
 * Sets the list of sorts.
 */
public void setSorts(List <Sort> theSorts)  { for(Sort sort : theSorts) addSort(sort); }

/**
 * Adds a sort or sorts to list.
 */
public void addSort(Sort aSort)  { addSort(aSort, _sorts.size()); }

/**
 * Adds a sort at given index.
 */
public void addSort(Sort aSort, int anIndex)
{
    // Add sort and fire PropertyChange
    _sorts.add(anIndex, aSort);
    firePropChange(Sort_Prop, null, aSort, anIndex);
}

/**
 * Removes a sort at given index.
 */
public Sort removeSort(int anIndex)
{
    // Remove sort, fire PropertyChange and return
    Sort sort = _sorts.remove(anIndex);
    firePropChange(Sort_Prop, sort, null, anIndex);
    return sort;
}

/**
 * Removes given sort.
 */
public int removeSort(Sort aSort)
{
    int index = ListUtils.indexOfId(_sorts, aSort);
    if(index>=0) removeSort(index);
    return index;
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity and get other query
    if(anObj==this) return true;
    Query other = (Query)anObj;
    
    // Check entity name, condition, fetch limit
    if(!SnapUtils.equals(other._entityName, _entityName)) return false;
    if(!SnapUtils.equals(other._condition, _condition)) return false;
    if(other._fetchLimit!=_fetchLimit) return false;
    return true; // Return true since all checks passed
}

/**
 * Implement PropChangeListener method to forward on.
 */
public void propertyChange(PropChange anEvent)  { firePropChange(anEvent); }

/**
 * Standard clone implementation.
 */
public Query clone()
{
    // Do normal version, clone Condition and return
    Query clone = (Query)super.clone();
    clone._condition = _condition!=null? _condition.clone() : null;
    return clone;
}

/**
 * RMJSONArchiver.GetKeys method.
 */
public List <String> getJSONKeys()  { return Arrays.asList("EntityName", "Condition", "FetchLimit", "Sorts"); }

/**
 * Returns a string representation of query.
 */
public String toString()
{
    String format = "%s { EntityName:%s Condition:%s }";
    return String.format(format, getClass().getSimpleName(), getEntityName(), getCondition());
}

}