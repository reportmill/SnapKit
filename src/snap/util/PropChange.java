/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * A class to describe object property changes.
 */
public class PropChange {
    
    // The source
    Object       _src;
    
    // The property name
    String       _pname;
    
    // The old/new values
    Object       _oval, _nval;
    
    // The index
    int          _index = -1;

/**
 * Creates a new PropChange.
 */
public PropChange(Object aSource, String aProp, Object oldVal, Object newVal)
{
    _src = aSource; _pname = aProp; _oval = oldVal; _nval = newVal;
}

/**
 * Creates a new PropChange with index.
 */
public PropChange(Object aSource, String aProp, Object oldVal, Object newVal, int anIndex)
{
    _src = aSource; _pname = aProp; _oval = oldVal; _nval = newVal; _index = anIndex;
}

/**
 * Returns the source.
 */
public Object getSource()  { return _src; }

/**
 * Returns the Property name.
 */
public String getPropertyName()  { return _pname; }

/**
 * Returns the old value.
 */
public Object getOldValue()  { return _oval; }

/**
 * Returns the new value.
 */
public Object getNewValue()  { return _nval; }

/**
 * Returns the index.
 */
public int getIndex()  { return _index; }

/**
 * Undoes this change.
 */
public void undoChange()  { doChange(getNewValue(), getOldValue()); }

/**
 * Redoes this change.
 */
public void redoChange()  { doChange(getOldValue(), getNewValue()); }

/**
 * Does this change with given new/old values.
 */
protected void doChange(Object oldVal, Object newVal)
{
    doChange(getSource(), getPropertyName(), oldVal, newVal, getIndex());
}

/**
 * Attempts to merge the given property change into this property change.
 */
public PropChange merge(PropChange anEvent)
{
    // If index, return false
    if(getIndex()>=0 || anEvent.getIndex()>=0)
        return null;
    
    // Create new merged event and return it 
    return new PropChange(getSource(), getPropertyName(), getOldValue(), anEvent.getNewValue());
}

/**
 * Simple to string.
 */
public String toString()
{
    String cname = getSource().getClass().getSimpleName(), pname = getPropertyName();
    Object oldV = getOldValue(); String oldS = oldV!=null? oldV.toString().replace("\n", "\\n") : null;
    Object newV = getNewValue(); String newS = newV!=null? newV.toString().replace("\n", "\\n") : null;
    int index = getIndex(); String istring = index>0? (" at " + index) : "";
    return cname + " " + pname + " (set " + oldS + " to " + newS + ")" + istring;
}

/**
 * Performs the given change by using RMKey.setValue or RMKeyList add/remove.
 */
public static void doChange(Object aSource, String aProp, Object oldVal, Object newVal, int anIndex)
{
    // If indexed change, create KeyList and add/remove
    if(anIndex>=0) {
        //Object otherVal = list.size()>anIndex? list.get(anIndex) : null;
        //if(newVal!=null && newVal!=otherVal) list.add(anIndex, newVal);
        //else if(oldVal!=null && oldVal==otherVal) list.remove(anIndex);
        KeyList.setValue(aSource, aProp, newVal, anIndex);
    }
    
    // If plain change, do KeyChain.setValue on new value
    else KeyChain.setValueSafe(aSource, aProp, newVal);
}

}