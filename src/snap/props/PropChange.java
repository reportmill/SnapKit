/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.Key;
import snap.util.KeyChain;

/**
 * A class to describe object property changes.
 */
public class PropChange {
    
    // The source
    private Object _source;
    
    // The property name
    private String _propName;
    
    // The old value
    private Object _oldVal;
    
    // The new value
    private Object _newVal;

    // The index
    private int _index = -1;

    // The next batch change
    protected PropChange _nextBatchPropChange;

    /**
     * Constructor.
     */
    public PropChange(Object aSource, String aProp, Object oldVal, Object newVal)
    {
        _source = aSource; _propName = aProp; _oldVal = oldVal; _newVal = newVal;
    }

    /**
     * Constructor with index.
     */
    public PropChange(Object aSource, String aProp, Object oldVal, Object newVal, int anIndex)
    {
        _source = aSource; _propName = aProp; _oldVal = oldVal; _newVal = newVal; _index = anIndex;
    }

    /**
     * Returns the source.
     */
    public Object getSource()  { return _source; }

    /**
     * Returns the Property name.
     */
    public String getPropName()  { return _propName; }

    /**
     * Returns the Property name.
     */
    public String getPropertyName()  { return _propName; }

    /**
     * Returns the old value.
     */
    public Object getOldValue()  { return _oldVal; }

    /**
     * Returns the new value.
     */
    public Object getNewValue()  { return _newVal; }

    /**
     * Returns the index.
     */
    public int getIndex()  { return _index; }

    /**
     * Undoes this change.
     */
    public void undoChange()
    {
        doChange(getNewValue(), getOldValue());
    }

    /**
     * Redoes this change.
     */
    public void redoChange()
    {
        doChange(getOldValue(), getNewValue());
    }

    /**
     * Does this change with given new/old values.
     */
    protected void doChange(Object oldVal, Object newVal)
    {
        Object source = getSource();
        if (source instanceof DoChange doChange)
            doChange.processPropChange(this, oldVal, newVal);
        else doChange(source, getPropName(), oldVal, newVal, getIndex());
    }

    /**
     * Attempts to merge the given property change into this property change.
     */
    public PropChange merge(PropChange propChange)
    {
        // If index, return false
        if (getIndex() >= 0 || propChange.getIndex() >= 0)
            return null;

        // Create new merged event and return it
        return new PropChange(getSource(), getPropName(), getOldValue(), propChange.getNewValue());
    }

    /**
     * Returns the next batch prop change.
     */
    public PropChange getNextBatchPropChange()  { return _nextBatchPropChange; }

    /**
     * Clears the next batch prop change.
     */
    public void clearNextBatchPropChange()  { _nextBatchPropChange = null; }

    /**
     * Simple to string.
     */
    public String toString()
    {
        String className = _source != null ? _source.getClass().getSimpleName() : "BatchPlaceHolderPropChange";
        String propName = getPropName();
        Object oldVal = getOldValue();
        String oldValStr = oldVal != null ? oldVal.toString().replace("\n", "\\n") : null;
        Object newVal = getNewValue();
        String newValStr = newVal != null ? newVal.toString().replace("\n", "\\n") : null;
        int index = getIndex();
        String indexStr = index > 0 ? (" at " + index) : "";
        return className + " " + propName + " (set " + oldValStr + " to " + newValStr + ")" + indexStr;
    }

    /**
     * Performs the given change by using RMKey.setValue or RMKeyList add/remove.
     */
    public static void doChange(Object aSource, String aProp, Object oldVal, Object newVal, int anIndex)
    {
        // If indexed change, create KeyList and add/remove
        if (anIndex >= 0) { //KeyList.setValue(aSource, aProp, newVal, anIndex);
            System.out.println("PropChange.doChange: No support for indexed prop: aProp");
            return;
        }

        // If PropObject, use
        if (aSource instanceof PropObject propObject)
            propObject.setPropValue(aProp, newVal);

        // If source is GetSet, use GetSet interface
        else if (aSource instanceof Key.GetSet getSet)
            getSet.setKeyValue(aProp, newVal);

        // Otherwise, do KeyChain.setValue
        else KeyChain.setValueSafe(aSource, aProp, newVal);
    }

    /**
     * An interface for objects that can do a PropChange.
     */
    public interface DoChange {
        void processPropChange(PropChange aPC, Object oldVal, Object newVal);
    }
}