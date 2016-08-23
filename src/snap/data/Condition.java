/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.data;
import java.util.*;
import snap.util.*;

/**
 * Represents a condition that is part of a SQL where clause.
 */
public class Condition extends SnapObject implements JSONArchiver.GetKeys, XMLArchiver.Archivable {

     // The property name
     String          _propertyName;
     
     // The operator
     Operator        _operator;
     
     // The value
     Object          _value;
     
     // Whether condition should be negated
     boolean         _negated;
     
     // Whether condition should ignore case (if applicable)
     boolean         _ignoreCase;
     
     // Date constraint
     DateConstraint  _dateConstraint = getDateConstraintDefault();
     
    // Supported condition operators
    public enum Operator {
        Equals, LessThan, LessThanOrEqual, GreaterThan, GreaterThanOrEqual,
        StartsWith, EndsWith, Contains,
        Before, After, WithinNext, WithinLast,
        Empty,
        And, Or,
        Like;
    }
    
    // Date constraints
    public enum DateConstraint { Second, Minute, Hour, Day, Week, Month, Year };
    
    // Constants for PropertyChange
    public static final String PropertyName_Prop = "PropertyName";
    public static final String Operator_Prop = "Operator";
    public static final String Value_Prop = "Value";
    public static final String Negated_Prop = "Negated";
    public static final String IgnoreCase_Prop = "IgnoreCase";
    public static final String DateConstraint_Prop = "DateConstraint";
 
/**
 * Creates a new select condition.
 */
public Condition()  { }

/**
 * Creates a new select condition.
 */
public Condition(String aPropertyName, Operator anOperator, Object aValue)
{
    setPropertyName(aPropertyName);
    setOperator(anOperator);
    setValue(aValue);
}

/**
 * Returns the property name.
 */
public String getPropertyName()  { return _propertyName; }

/**
 * Sets the property name.
 */
public void setPropertyName(String aValue)
{
    if(SnapUtils.equals(aValue, _propertyName)) return;
    firePropChange(PropertyName_Prop, _propertyName, _propertyName = aValue);
}

/**
 * Returns the operator.
 */
public Operator getOperator()  { return _operator; }

/**
 * Sets the operator.
 */
public void setOperator(Operator anOperator)
{
    if(SnapUtils.equals(anOperator, _operator)) return;
    firePropChange(Operator_Prop, _operator, _operator = anOperator);
}

/**
 * Returns the value.
 */
public Object getValue()  { return _value; }

/**
 * Sets the value.
 */
public void setValue(Object aValue)
{
    if(SnapUtils.equals(aValue, _value)) return;
    firePropChange(Value_Prop, _value, _value = aValue);
}

/**
 * Returns whether condition is negated.
 */
public boolean isNegated()  { return _negated; }

/**
 * Sets whether condition is negated.
 */
public void setNegated(boolean aValue)
{
    if(aValue==_negated) return;
    firePropChange(Negated_Prop, _negated, _negated = aValue);
}

/**
 * Returns whether condition ignores case (string types).
 */
public boolean getIgnoreCase()  { return _ignoreCase; }

/**
 * Sets whether condition ignores case (string types).
 */
public void setIgnoreCase(boolean aValue)
{
    if(aValue==_ignoreCase) return;
    firePropChange(IgnoreCase_Prop, _ignoreCase, _ignoreCase = aValue);
}

/**
 * Returns date constraint.
 */
public DateConstraint getDateConstraint()  { return _dateConstraint; }

/**
 * Sets the date constraint.
 */
public void setDateConstraint(DateConstraint aDateConstraint)
{
    if(aDateConstraint==_dateConstraint) return;
    firePropChange(DateConstraint_Prop, _dateConstraint, _dateConstraint = aDateConstraint);
}

/**
 * Returns the date constraint default.
 */
public DateConstraint getDateConstraintDefault()  { return DateConstraint.Second; }

/**
 * Returns the date constraint multiplier.
 */
public long getDateConstraintMultiplier()
{
    switch (getDateConstraint()) {
        case Second: return 1;
        case Minute: return 60;
        case Hour: return 60*60;
        case Day: return 60*60*24;
        case Week: return 60*60*24*7;
        case Month: return 60l*60*24*30; // Bogus!
        case Year: return 60l*60*24*365; // Still bogus!
        default: return 1;
    }
}

/**
 * Returns the valid operators for a given property type.
 */
public static List <Condition.Operator> getOperators(Property.Type aType)
{
    // Handle type String
    if(aType==Property.Type.String)
        return Arrays.asList(Operator.Equals, Operator.StartsWith, Operator.EndsWith, Operator.Contains,
            Operator.Empty); 
    
    // Handle type Number
    if(aType==Property.Type.Number)
        return Arrays.asList(Operator.Equals, Operator.GreaterThan, Operator.LessThan, Operator.Empty);
    
    // Handle type boolean
    if(aType==Property.Type.Boolean)
        return Arrays.asList(Operator.And, Operator.Or);
    
    // Handle type Date
    if(aType==Property.Type.Date)
        return Arrays.asList(Operator.Equals, Operator.Before, Operator.After,
            Operator.WithinLast, Operator.WithinNext, Operator.Empty);
    
    // Handle type Relation
    if(aType==Property.Type.Relation)
        return Arrays.asList(Operator.Equals, Operator.Empty);
    
    // Return Empty operator as list
    return Arrays.asList(Operator.Empty);
}

/**
 * Returns the value of evaluating this condition on given object.
 */
public boolean getValue(Entity anEntity, Object anObj)
{
    // Get property from entity
    Property property = anEntity.getProperty(getPropertyName());
    if(property==null) {
        System.err.println("RMCondition.getValue: Entity property not found (" +
            anEntity.getName() + '.' + getPropertyName() + ")"); return false; }
    
    // Get property value
    Object propertyValue = anObj instanceof Row? ((Row)anObj).getValue(property) :
        Key.getValue(anObj, property.getName());
    propertyValue = property.convertValue(propertyValue);

    // Get operator and value
    Operator op = getOperator();
    Object value = getValue();
    if(op!=Operator.WithinLast)
        value = property.convertValue(value);
    
    // Handle operators
    switch(op) {
        case Equals: return SnapUtils.equals(propertyValue, value);
        case LessThan: return SnapUtils.compare(propertyValue, value)<0;
        case LessThanOrEqual: return SnapUtils.compare(propertyValue, value)<=0;
        case GreaterThan: return SnapUtils.compare(propertyValue, value)>0;
        case GreaterThanOrEqual: return SnapUtils.compare(propertyValue, value)>=0;
        case StartsWith:
        case EndsWith:
        case Contains:
            String s1 = SnapUtils.stringValue(propertyValue); if(s1==null) return false;
            String s2 = SnapUtils.stringValue(value); if(s2==null) return false;
            if(op==Operator.StartsWith) return s1.startsWith(s2);
            else if(op==Operator.EndsWith) return s1.endsWith(s2);
            return s1.contains(s2);
        case WithinLast:
        case WithinNext:
            Date date = SnapUtils.getDate(propertyValue); if(date==null) return false;
            long time = date!=null? date.getTime() : System.currentTimeMillis();
            long interval = Math.round(SnapUtils.doubleValue(value)*1000)*getDateConstraintMultiplier();
            long now = System.currentTimeMillis();
            if(op==Operator.WithinLast) return now - time <= interval;
            return time>=now && time<now + interval;
        case Before:
        case After:
            Date date1 = SnapUtils.getDate(propertyValue); if(date1==null) return false;
            Date date2 = SnapUtils.getDate(value); if(date2==null) return false;
            int compare = date1.compareTo(date2);
            return op==Operator.Before? (compare<0) : (compare>=0);
        case Empty: return propertyValue==null;
        default: throw new UnsupportedOperationException("RMCondition getValue operator not supported " + op);
    }
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, check class, get other condition
    if(anObj==this) return true;
    if(!(anObj instanceof Condition)) return false;
    Condition other = (Condition)anObj;
    
    // Check property name, operator, value
    if(other.getPropertyName()!=getPropertyName()) return false;
    if(other.getOperator()!=getOperator()) return false;
    if(other.getValue()!=getValue()) return false;
    
    // Check negated, ignore case and date constraint
    if(other.isNegated()!=isNegated()) return false;
    if(other.getIgnoreCase()!=getIgnoreCase()) return false;
    if(other.getDateConstraint()!=getDateConstraint()) return false;
    return true; // Return true since all checks passed
}

/**
 * Standard clone implementation (to get co-variant return type).
 */
public Condition clone()  { return (Condition)super.clone(); }

/**
 * XML Archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Create xml element
    XMLElement xml = new XMLElement("condition");
    
    // Archive PropertyName, Operator, Value, Negated, IgnoreCase, DateConstraint and return
    xml.add("property-name", getPropertyName());
    xml.add("operator", getOperator());
    xml.add("value", getValue());
    if(isNegated()) xml.add("negated", true);
    if(getIgnoreCase()) xml.add("ignore-case", true);
    if(getDateConstraint()!=DateConstraint.Second) xml.add("date-constraint", getDateConstraint());
    return xml;
}

/**
 * XML Unarchival.
 */
public Condition fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // If has child conditions, unarchive and return composite
    if(anElement.getElement("condition")!=null)
        return new ConditionList().fromXML(anArchiver, anElement);
    
    // Unarchive PropertyName, Operator, Value, Negated, IgnoreCase, DateConstraint and return
    if(anElement.hasAttribute("property-name")) setPropertyName(anElement.getAttributeValue("property-name"));
    else setPropertyName(anElement.getAttributeValue("left-side"));
    setOperator(Operator.valueOf(anElement.getAttributeValue("operator")));
    if(anElement.hasAttribute("value")) setValue(anElement.getAttributeValue("value"));
    else setValue(anElement.getAttributeValue("right-side"));
    if(anElement.hasAttribute("negated")) setNegated(anElement.getAttributeBoolValue("negated"));
    if(anElement.hasAttribute("ignore-case")) setIgnoreCase(anElement.getAttributeBoolValue("ignore-case"));
    if(anElement.hasAttribute("date-constraint"))
        setDateConstraint(DateConstraint.valueOf(anElement.getAttributeValue("date-constraint")));
    return this;
}

/**
 * RMJSONArchiver.GetKeys method.
 */
public List<String> getJSONKeys()
{
    return Arrays.asList("PropertyName", "Operator", "Value", "Negated", "IgnoreCase", "DateConstraint");
}

/**
 * Returns a string for condition.
 */
public String toString()
{
    return getPropertyName() + " " + (isNegated()? " Not" : "") + getOperator() + " " + getValue();
}

}