/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.ListUtils;
import snap.view.ViewUtils;
import java.util.*;

/**
 * Undoer - this object manages undo by keeping lists of property changes.
 */
public class Undoer extends PropObject {

    // The list of undo sets
    private List<UndoSet> _undoSets;

    // The list of redo sets
    private List<UndoSet> _redoSets;

    // The current undo set to add new property changes to
    private UndoSet _activeUndoSet;

    // Whether to auto save changes - this should default to true!!!
    private boolean _autoSave;

    // Whether undoer is disabled
    private int _disabled = 0;

    // Whether undoer is at last save state for client
    private boolean _atLastSaveState;

    // The undo state at last save state for client
    private List<UndoSet> _lastSaveState;

    // The run to auto save
    private Runnable _autoSaveRun;

    // Constants for properties
    public static final String AtLastSaveState_Prop = "AtLastSaveState";

    // A shared instance of an Undoer that is disabled
    public static final Undoer DISABLED_UNDOER = new DisabledUndoer();

    /**
     * Constructor.
     */
    public Undoer()
    {
        super();

        _undoSets = new ArrayList<>();
        _redoSets = new ArrayList<>();
        _activeUndoSet = new UndoSet();
        markLastSaveState();
    }

    /**
     * Returns the active undo set.
     */
    public UndoSet getActiveUndoSet()  { return _activeUndoSet; }

    /**
     * Sets the title of the current undo.
     */
    public void setUndoTitle(String aString)
    {
        if (_activeUndoSet._undoTitle == null)
            _activeUndoSet._undoTitle = aString;
    }

    /**
     * Returns the list of undo sets.
     */
    public List<UndoSet> getUndoSets()  { return _undoSets; }

    /**
     * Returns the list of redo sets.
     */
    public List<UndoSet> getRedoSets()  { return _redoSets; }

    /**
     * Returns the last undo.
     */
    public UndoSet getLastUndoSet()  { return ListUtils.getLast(_undoSets); }

    /**
     * Returns the last redo.
     */
    public UndoSet getLastRedoSet()  { return ListUtils.getLast(_redoSets); }

    /**
     * Sets whether to auto save changes.
     */
    public void setAutoSave(boolean aValue)  { _autoSave = aValue; }

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
    public void addPropChange(PropChange propChange)
    {
        // If undoer is disabled, just return
        if (!isEnabled())
            return;

        // Add or merge change
        boolean didMerge = mergePropChange(propChange);
        if (!didMerge)
            _activeUndoSet.addPropChange(propChange);

        // If AutoSave, register to call saveChange()
        if (_autoSave) {
            if (_autoSaveRun == null) {
                if (ViewUtils.isMouseDown())
                    ViewUtils.runOnMouseUp(_autoSaveRun = this::saveChangesAndClose);
                else ViewUtils.runLater(_autoSaveRun = this::saveChanges);
            }
        }
    }

    /**
     * Tries to merge the prop change event.
     */
    private boolean mergePropChange(PropChange propChange)
    {
        // If at last save state for client, just return false
        if (isAtLastSaveState())
            return false;

        // If ActiveUndoSet is not empty, just return false
        if (!_activeUndoSet.isEmpty())
            return false;

        // LastUndoSet is null or closed, just return false
        UndoSet lastUndoSet = getLastUndoSet();
        if (lastUndoSet == null || lastUndoSet._closed)
            return false;

        // If Mouse is down, just return false
        if (ViewUtils.isMouseDown()) {
            lastUndoSet._closed = true;
            return false;
        }

        // Try to merge and return result
        return lastUndoSet.mergePropChange(propChange);
    }

    /**
     * Pushes current undo record on the stack and opens new one.
     */
    public void saveChanges()
    {
        // If active undo set has changes, add it to UndoSets and create new one
        if (!_activeUndoSet.isEmpty()) {

            // Add current undo
            _undoSets.add(_activeUndoSet);

            // Create new current undo
            _activeUndoSet = new UndoSet();

            // Clear redos
            _redoSets.clear();
        }

        // If no outstanding changes, just reset current undo
        else _activeUndoSet.reset();

        resetAtLastSaveState();
        _autoSaveRun = null;
    }

    /**
     * Saves changes and closes last undo set.
     */
    private void saveChangesAndClose()
    {
        saveChanges();
        UndoSet lastUndoSet = getLastUndoSet();
        if (lastUndoSet != null)
            lastUndoSet._closed = true;
    }

    /**
     * Pops last undo set off stack and has it update all changed objects it contains.
     */
    public UndoSet undo()
    {
        // Save changes in case previous changes haven't been committed
        saveChanges();

        // Disable undoer so no registration happens
        disable();

        // If there are Undos, remove last undo, execute and add to RedoSets
        UndoSet undoSet = null;
        if (!_undoSets.isEmpty()) {
            undoSet = _undoSets.remove(_undoSets.size() - 1);
            undoSet._closed = true;
            _redoSets.add(undoSet);
            undoSet.undo();
        }

        // Enable undoer and return
        enable();
        resetAtLastSaveState();
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
        if (!_redoSets.isEmpty()) {
            undoSet = _redoSets.remove(_redoSets.size() - 1);
            _undoSets.add(undoSet);
            undoSet.redo();
        }

        // Enable undoer and return UndoSet
        enable();
        resetAtLastSaveState();
        return undoSet;
    }

    /**
     * Returns whether undoer is disabled.
     */
    public boolean isEnabled()  { return _disabled == 0; }

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
        _disabled = 0;
        resetAtLastSaveState();
    }

    /**
     * Returns whether undoer has Undo changes stored away.
     */
    public boolean hasUndos()
    {
        return !_undoSets.isEmpty() || !_activeUndoSet.isEmpty();
    }

    /**
     * Returns whether undoer has Redo changes stored away.
     */
    public boolean hasRedos()
    {
        return getLastRedoSet() != null;
    }

    /**
     * Returns whether undoer is at last save state for client.
     */
    public boolean isAtLastSaveState()  { return _atLastSaveState; }

    /**
     * Sets whether undoer is at last save state for client.
     */
    private void setAtLastSaveState(boolean aValue)
    {
        if (aValue == isAtLastSaveState()) return;
        firePropChange(AtLastSaveState_Prop, _atLastSaveState, _atLastSaveState = aValue);
    }

    /**
     * Resets the AtLastSaveState property.
     */
    private void resetAtLastSaveState()
    {
        boolean atLastSaveState = _lastSaveState.equals(_undoSets);
        setAtLastSaveState(atLastSaveState);
    }

    /**
     * Sets the last save state.
     */
    public void markLastSaveState()
    {
        if (isAtLastSaveState()) return;
        _lastSaveState = List.copyOf(_undoSets);
        setAtLastSaveState(true);
    }

    /**
     * An interface for undo/redo selection.
     */
    public interface Selection {
        void setSelection();
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return String.format("Undoer { UndoCount=%d, RedoCount=%d }", getUndoSets().size(), getRedoSets().size());
    }

    /**
     * This Undoer subclass is disabled.
     */
    private static class DisabledUndoer extends Undoer {
        @Override
        public boolean isEnabled()  { return false; }
        @Override
        public void addPropChangeListener(PropChangeListener aPCL)  { }
    }
}