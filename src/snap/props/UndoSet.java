/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.ListUtils;
import java.util.*;

/**
 * This class represents a single undo but holds a list of PropChanges.
 */
public class UndoSet {

    // The title of the undo
    protected String  _undoTitle;

    // The list of change events
    private List<PropChange>  _changes = new ArrayList<>();

    // The objects to be selected if undo is executed
    protected Object  _undoSelection;

    // The objects to be selected if redo is selected
    protected Object  _redoSelection;

    // Whether this set is fully closed (can't try to merge)
    protected boolean _closed;

    /**
     * Creates a new empty undo set.
     */
    public UndoSet()
    {
    }

    /**
     * Returns the undo title.
     */
    public String getUndoTitle()  { return _undoTitle; }

    /**
     * Returns the full title of this undo when used for undo.
     */
    public String getFullUndoTitle()  { return _undoTitle == null ? "Undo" : "Undo " + _undoTitle; }

    /**
     * Returns the full title of this undo when used for redo.
     */
    public String getFullRedoTitle()  { return _undoTitle == null ? "Redo" : "Redo " + _undoTitle; }

    /**
     * Returns whether this set has no changes.
     */
    public boolean isEmpty()  { return _changes.size() == 0; }

    /**
     * Returns the number of changes.
     */
    public int getChangeCount()  { return _changes.size(); }

    /**
     * Returns the selection to be set if undo is executed.
     */
    public List<PropChange> getChanges()  { return _changes; }

    /**
     * Returns the selection to be set if undo is executed.
     */
    public Object getUndoSelection()
    {
        return _undoSelection;
    }

    /**
     * Returns the selection to be set if redo is executed.
     */
    public Object getRedoSelection()  { return _redoSelection == null ? _undoSelection : _redoSelection; }

    /**
     * Tries to merge property change.
     */
    public boolean mergePropChange(PropChange newPC)
    {
        // Iterate over changes and if duplicate exists, coalesce (go backward so we only check last same prop name event)
        for (int i = _changes.size() - 1; i >= 0; i--) {
            PropChange oldPC = _changes.get(i);

            // If source and prop are equal, try merge
            if (oldPC.getSource() == newPC.getSource() && oldPC.getPropName().equals(newPC.getPropName())) {

                // If merge successful, replace event
                PropChange mergePC = oldPC.merge(newPC);
                if (mergePC == null)
                    return false;

                // Remove old event and add new
                _changes.remove(i);
                if (!Objects.equals(mergePC.getOldValue(), mergePC.getNewValue()))
                    _changes.add(mergePC);
                return true;
            }
        }

        // Return no merge
        return false;
    }

    /**
     * Adds a property change.
     */
    public void addPropChange(PropChange anEvent)
    {
        // Iterate over changes and if duplicate exists, coalesce (go backward so we only check last same prop name event)
        for (int i = _changes.size() - 1; i >= 0; i--) {
            PropChange e = _changes.get(i);
            if (e.getSource() == anEvent.getSource() && e.getPropName().equals(anEvent.getPropName())) {
                PropChange event = e.merge(anEvent);
                if (event != null) {
                    anEvent = event;
                    _changes.remove(i);
                }
                break;
            }
        }

        // If values are equal, just return
        if (Objects.equals(anEvent.getOldValue(), anEvent.getNewValue()))
            return;

        // Add change
        _changes.add(anEvent);
    }

    /**
     * Executes undo for this event.
     */
    public void undo()
    {
        // Iterate over changes and execute
        for (int i = _changes.size() - 1; i >= 0; i--) {
            PropChange pce = _changes.get(i);
            System.out.println("Undoing " + getPropChangeString(pce, true));
            pce.undoChange();
        }

        // If undo selection is set, trigger it
        if (_undoSelection instanceof Undoer.Selection)
            ((Undoer.Selection) _undoSelection).setSelection();
    }

    /**
     * Executes redo for this event.
     */
    public void redo()
    {
        // Iterate over changes and execute
        for (PropChange pce : _changes) {
            System.out.println("Redoing " + getPropChangeString(pce, false));
            pce.redoChange();
        }

        // If redo selection is set, trigger it
        if (_redoSelection instanceof Undoer.Selection)
            ((Undoer.Selection) _redoSelection).setSelection();
    }

    /**
     * Resets this undo event for reuse.
     */
    public void reset()
    {
        _undoTitle = null;
        _undoSelection = _redoSelection = null;
        _changes.clear();
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String undoTitle = "UndoSet(" + getUndoTitle() + "): ";
        String propChangesStr = ListUtils.mapToStringsAndJoin(getChanges(), pc -> getPropChangeString(pc, true), ", ");
        return undoTitle + propChangesStr;
    }

    /**
     * Returns a string for a property change event.
     */
    private static String getPropChangeString(PropChange anEvent, boolean doUndo)
    {
        String source = anEvent.getSource().getClass().getSimpleName();
        String propName = anEvent.getPropName();
        Object oldValue = anEvent.getOldValue();
        Object newValue = anEvent.getNewValue();
        String oldValueStr = oldValue != null ? oldValue.toString().replace("\n", "\\n") : null;
        String newValueStr = newValue != null ? newValue.toString().replace("\n", "\\n") : null;
        return String.format("%s %s (set %s to %s)", source, propName, doUndo ? newValueStr : oldValueStr, doUndo ? oldValueStr : newValueStr);
    }
}