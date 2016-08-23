/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.data;
import java.util.*;
import snap.util.*;

/**
 * This condition subclass represents a list of conditions.
 */
public class ConditionList extends Condition implements PropChangeListener {
    
    // A list of operators for each condition
    List <Operator>     _operators = new ArrayList();
    
    // A list of conditions
    List <Condition>    _conditions = new ArrayList();
    
    // Constants for PropertyChange
    public static final String Condition_Prop = "Condition";
    public static final String Operator_Prop = "Operator";
    
/**
 * Returns the operators.
 */
public List <Operator> getOperators()  { return _operators; }

/**
 * Returns the conditions.
 */
public List <Condition> getConditions()  { return _conditions; }

/**
 * Sets the conditions.
 */
public void setConditions(List <Condition>  theConditions)
{
    for(Condition condition : theConditions)
        addCondition(Operator.And, condition);
}

/**
 * Returns the number of conditions in this composite.
 */
public int getConditionCount()  { return _conditions.size(); }

/**
 * Returns the condition at the given index.
 */
public Condition getCondition(int anIndex)  { return _conditions.get(anIndex); }

/**
 * Adds a condition.
 */
public ConditionList addCondition(Operator anOperator, Condition aCondition)
{
    return addCondition(anOperator, aCondition, getConditionCount());
}

/**
 * Adds a condition at given index.
 */
public ConditionList addCondition(Operator anOperator, Condition aCondition, int anIndex)
{
    // Add operator and condition
    _operators.add(anIndex, anOperator);
    _conditions.add(anIndex, aCondition);
    
    // Start listening to child PropertyChanges, fire PropertyChange and return
    aCondition.addPropChangeListener(this);
    firePropChange(Condition_Prop, null, aCondition, anIndex);
    return this;
}

/**
 * Adds a condition for given
 */
public ConditionList addCondition(String aPropertyName, Operator anOperator, Object aValue)
{
    return addCondition(Operator.And, aPropertyName, anOperator, aValue);
}

/**
 * Adds a condition for given
 */
public ConditionList addCondition(Operator anOperator, String aPropertyName, Operator anOperator2, Object aValue)
{
    return addCondition(anOperator, new Condition(aPropertyName, anOperator2, aValue), getConditionCount());
}

/**
 * Removes a condition from a given index.
 */
public Condition removeCondition(int anIndex)
{
    // Remove condition and operator
    Condition condition = _conditions.remove(anIndex);
    _operators.remove(anIndex);
    
    // Stop listening to child PropertyChanges, fire PropertyChange and return
    condition.removePropChangeListener(this);
    firePropChange(Condition_Prop, condition, null, anIndex);
    return condition;
}

/**
 * Removes the given condition.
 */
public int removeCondition(Condition aCondition)
{
    int index = indexOfCondition(aCondition);
    if(index>=0) removeCondition(index);
    return index;
}

/**
 * Returns the index of a given condition.
 */
public int indexOfCondition(Condition aCondition)  { return ListUtils.indexOfId(_conditions, aCondition); }

/**
 * Returns the operator at the given index.
 */
public Operator getOperator(int anIndex)  { return _operators.get(anIndex); }

/**
 * Sets the operator at the given index.
 */
public void setOperator(Operator anOperator, int anIndex)
{
    // If value already set, just return
    if(anOperator==getOperator(anIndex)) return;
    
    // Cache old value, set value and fire PropertyChange
    Operator oldValue = getOperator(anIndex);
    _operators.set(anIndex, anOperator);
    firePropChange(Operator_Prop, oldValue, anOperator, anIndex);
}

/**
 * Override to handle list.
 */
public boolean getValue(Entity anEntity, Object anObj)
{
    boolean result = false;
    for(int i=0, iMax=getConditionCount(); i<iMax; i++) {
        Condition cond = getCondition(i);
        Operator op = getOperator(i);
        if(op==Operator.Or && result)
            break;
        result = (i==0 || op==Operator.Or || result) && cond.getValue(anEntity, anObj);
    }
    
    // Return result
    return result;
}

/**
 * Catches child property changes and forwards them to our listener.
 */
public void propertyChange(PropChange anEvent)  { firePropChange(anEvent); }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, check class, get other condition list
    if(anObj==this) return true;
    if(!(anObj instanceof ConditionList)) return false;
    ConditionList other = (ConditionList)anObj;
    
    // Check operators and conditions
    if(!other.getOperators().equals(getOperators())) return false;
    if(!other.getConditions().equals(getConditions())) return false;
    return true; // Return true since all checks passed
}

/**
 * Standard clone implementation.
 */
public ConditionList clone()
{
    // Do normal clone, clear operator and condition lists and re-add cloned conditions
    ConditionList clone = (ConditionList)super.clone();
    clone._operators = new ArrayList(getConditionCount());
    clone._conditions = new ArrayList(getConditionCount());
    for(int i=0, iMax=getConditionCount(); i<iMax; i++) clone.addCondition(getOperator(i), getCondition(i).clone());
    return clone; // Return clone
}

/**
 * RMJSONArchiver.GetKeys method.
 */
public List<String> getJSONKeys()  { return Arrays.asList("Conditions"); }

/** XML Archival. */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement xml = new XMLElement("condition");
    for(int i=0, iMax=getConditionCount(); i<iMax; i++) {
        Condition condition = getCondition(i);
        XMLElement conditionXML = condition.toXML(anArchiver);
        conditionXML.addAttribute(new XMLAttribute("composite-operator", getOperator(i).toString()), 0);
        xml.add(conditionXML); }
    return xml;
}

/** XML Unarchival. */
public ConditionList fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    List <XMLElement> conditionXMLs = anElement.getElements("condition");
    for(XMLElement conditionXML : conditionXMLs) {
        Condition condition = new Condition().fromXML(anArchiver, conditionXML);
        Operator operator = Operator.valueOf(conditionXML.getAttributeValue("composite-operator"));
        addCondition(operator, condition); }
    return this;
}

/**
 * Returns a string for condition.
 */
public String toString()  { return getConditions().toString(); }

}