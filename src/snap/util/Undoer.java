/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;

/**
 * Undoer - this object manages undo by keeping lists of property changes.
 */
public class Undoer {
    
    // The current undo set to add new property changes to
    UndoSet             _activeUndoSet = new UndoSet();
    
    // The list of undo sets
    List <UndoSet>      _undoSets = new Vector();
    
    // The list of redo sets
    List <UndoSet>      _redoSets = new Vector();
    
    // Whether undoer is disabled
    int                 _disabled = 0;

/**
 * Returns the active undo set.
 */
public UndoSet getActiveUndoSet()  { return _activeUndoSet; }

/**
 * Sets the active event (presumably from undo sets list).
 */
public void setActiveUndoSet(UndoSet anUndoSet)
{
    _activeUndoSet = anUndoSet;
    _undoSets.remove(anUndoSet);
}

/**
 * Sets the title of the current undo.
 */
public void setUndoTitle(String aString)
{
    if(_activeUndoSet._undoTitle==null)
        _activeUndoSet._undoTitle = aString;
}

/**
 * Returns the list of undo sets.
 */
public List <UndoSet> getUndoSets()  { return _undoSets; }

/**
 * Returns the list of redo sets.
 */
public List <UndoSet> getRedoSets()  { return _redoSets; }

/**
 * Returns the last undo.
 */
public UndoSet getUndoSetLast()  { return ListUtils.getLast(_undoSets); }

/**
 * Returns the last redo.
 */
public UndoSet getRedoSetLast()  { return ListUtils.getLast(_redoSets); }

/**
 * Returns the list of objects that should be selected after current undo is fired.
 */
public Object getUndoSelection()  { return _activeUndoSet._undoSelection; }

/**
 * Sets the list of objects that should be selected after current undo is fired.
 */
public void setUndoSelection(Object aList)  { _activeUndoSet._undoSelection = aList; }

/**
 * Returns the list of objects that should be selected after current undo is redone.
 */
public Object getRedoSelection()  { return _activeUndoSet._redoSelection; }

/**
 * Sets the list of objects that should be selected after current undo is redone.
 */
public void setRedoSelection(Object aList)  { _activeUndoSet._redoSelection = aList; }

/**
 * Adds a property change.
 */
public void addPropChange(PropChange anEvent)
{
    // If undoer is disabled, just return
    if(!isEnabled())
        return;
    
    // Add change
    _activeUndoSet.addPropChange(anEvent);
}

/**
 * Pushes current undo record on the stack and opens new one.
 */
public void saveChanges()
{
    // If changed objects still exist, push them onto _undos stack
    if(_activeUndoSet.getChangeCount()>0) {
        
        // Add current undo
        _undoSets.add(_activeUndoSet);
        
        // Create new current undo
        _activeUndoSet = new UndoSet();
        
        // Clear redos
        _redoSets.clear();
    }

    // If no changed objects, just reset current undo
    else _activeUndoSet.reset();
}

/**
 * Pops last undo set off stack an has it update all changed objects it contains.
 */
public UndoSet undo()
{
    // Save changes in case previous changes haven't been committed
    saveChanges();

    // Disable undoer so no registration happens
    disable();

    // If there are Undos, remove last undo, execute and add to RedoSets
    UndoSet undoSet = null;
    if(_undoSets.size()>0) {
        undoSet = _undoSets.remove(_undoSets.size()-1);
        _redoSets.add(undoSet);
        undoSet.undo();
    }

    // Enable undoer and return UndoSet
    enable();
    return undoSet;
}

/**
 * Pops last redo set off stack and has it update all changed objects it contains.
 */
public UndoSet redo()
{
    // Save changes in case previous changes haven't been committed
    saveChanges();

    // Disable undoer so no registration happens
    disable();
    
    // If there are Redos, remove last redo, execute and add to RedoSets
    UndoSet undoSet = null;
    if(_redoSets.size()>0) {
        undoSet = _redoSets.remove(_redoSets.size()-1);
        _undoSets.add(undoSet);
        undoSet.redo();
    }

    // Enable undoer and return UndoSet
    enable();
    return undoSet;
}

/**
 * Returns whether undoer is disabled.
 */
public boolean isEnabled()  { return _disabled==0; }

/**
 * Disables undoer so it can receive new changes.
 */
public void disable()  { _disabled++; }

/**
 * Enables undoer.
 */
public void enable()  { _disabled--; }

/**
 * Resets the undoer to its initial state (good to do when a document is saved).
 */
public void reset()
{
    _activeUndoSet.reset();
    _undoSets.clear();
    _redoSets.clear();
}

/**
 * Returns whether undoer has Undo changes stored away.
 */
public boolean hasUndos()  { return _undoSets.size()>0 || getActiveUndoSet().getChangeCount()>0; }

/**
 * Returns whether undoer has Redo changes stored away.
 */
public boolean hasRedos()  { return getRedoSetLast()!=null; }

/**
 * An interface for undo/redo selection.
 */
public interface Selection {
    public void setSelection();
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    return StringUtils.format("Undoer { UndoCount=%d, RedoCount=%d }", getUndoSets().size(), getRedoSets().size());
}

}