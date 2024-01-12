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

    // Whether an undo is currently available
    private boolean _undoAvailable;

    // The run to auto save
    private Runnable _autoSaveRun;

    // Constants for properties
    public static final String UndoAvailable_Prop = "UndoAvailable";

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
    public UndoSet getUndoSetLast()  { return ListUtils.getLast(_undoSets); }

    /**
     * Returns the last redo.
     */
    public UndoSet getRedoSetLast()  { return ListUtils.getLast(_redoSets); }

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
    public void addPropChange(PropChange anEvent)
    {
        // If undoer is disabled, just return
        if (!isEnabled())
            return;

        // If active set is empty, attempt to merge with last undo set
        boolean didMerge = false;
        if (_activeUndoSet.isEmpty()) {
            UndoSet lastUndoSet = getUndoSetLast();
            if (lastUndoSet != null && !lastUndoSet._closed && !ViewUtils.isMouseDown())
                didMerge = lastUndoSet.mergePropChange(anEvent);
        }

        // Add change
        if (!didMerge)
            _activeUndoSet.addPropChange(anEvent);
        resetUndoAvailable();

        // If AutoSave, register to call saveChange()
        if (_autoSave && _autoSaveRun == null) {
            if (ViewUtils.isMouseDown())
                ViewUtils.runOnMouseUp(_autoSaveRun = this::saveChanges);
            else ViewUtils.runLater(_autoSaveRun = this::saveChanges);
        }
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

        resetUndoAvailable();
        _autoSaveRun = null;
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
        if (_undoSets.size() > 0) {
            undoSet = _undoSets.remove(_undoSets.size() - 1);
            undoSet._closed = true;
            _redoSets.add(undoSet);
            undoSet.undo();
        }

        // Enable undoer and return
        enable();
        resetUndoAvailable();
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
        if (_redoSets.size() > 0) {
            undoSet = _redoSets.remove(_redoSets.size() - 1);
            _undoSets.add(undoSet);
            undoSet.redo();
        }

        // Enable undoer and return UndoSet
        enable();
        resetUndoAvailable();
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
        resetUndoAvailable();
    }

    /**
     * Returns whether undoer has Undo changes stored away.
     */
    public boolean hasUndos()
    {
        return _undoSets.size() > 0 || !_activeUndoSet.isEmpty();
    }

    /**
     * Returns whether undoer has Redo changes stored away.
     */
    public boolean hasRedos()
    {
        return getRedoSetLast() != null;
    }

    /**
     * Returns whether an undo is available.
     */
    public boolean isUndoAvailable()  { return _undoAvailable; }

    /**
     * Sets whether an undo is available.
     */
    private void setUndoAvailable(boolean aValue)
    {
        if (aValue == _undoAvailable) return;
        firePropChange(UndoAvailable_Prop, _undoAvailable, _undoAvailable = aValue);
    }

    /**
     * Resets the UndoAvailable property.
     */
    private void resetUndoAvailable()
    {
        boolean undoAvailable = hasUndos();
        setUndoAvailable(undoAvailable);
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