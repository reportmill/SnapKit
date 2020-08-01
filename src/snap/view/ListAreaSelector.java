package snap.view;

/**
 * A class to handle ListArea selection.
 */
class ListAreaSelector {

    // This ListArea
    private final ListArea _list;

    // Whether selection allowed on MouseDrag
    protected boolean  _dragSelect;

    // The old SelAnchor/SelLead on MousePress
    protected int _oldAnchor, _oldLead;

    // The new SelAnchor (index of MousePress)
    protected int _newAnchor;

    // Whether selection loop is to remove selection (MousePress hit selected cell)
    protected boolean  _anchorCellSelected;

    // The Selection on MousePress
    private int _mouseDownSel[];

    // Whether list previously wanted drag
    private boolean _dragGestureEnabled;

    /**
     * Constructor.
     */
    public ListAreaSelector(ListArea aListArea)
    {
        _list = aListArea;
    }

    /**
     * Process events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseEvent
        if (anEvent.isMouseEvent())
            processMouseEvent(anEvent);

        // Handle KeyPress
        else if (anEvent.isKeyPress()) {
            int kcode = anEvent.getKeyCode();
            switch(kcode) {
                case KeyCode.UP: _list.selectUp(); anEvent.consume(); break;
                case KeyCode.DOWN: _list.selectDown(); anEvent.consume(); break;
                case KeyCode.ENTER: _list.fireActionEvent(anEvent); anEvent.consume(); break;
            }
        }
    }

    /**
     * Process events.
     */
    protected void processMouseEvent(ViewEvent anEvent)
    {
        // Handle MousePress
        if (anEvent.isMousePress())
            mousePress(anEvent);

        // Handle MouseDrag
        else if (anEvent.isMouseDrag())
            mouseDrag(anEvent);

        // Handle MouseRelease
        else if (anEvent.isMouseRelease())
            mouseRelease(anEvent);

        // Handle MouseExit
        else if (anEvent.isMouseExit())
            _list.setTargetedIndex(-1);

        // Handle MouseMove
        else if (anEvent.isMouseMove() && _list.isTargeting()) {
            int index = _list.getRowForY(anEvent.getY());
            if (index>= _list.getItemCount()) index = -1;
            _list.setTargetedIndex(index);
        }

        // Consume all mouse events
        anEvent.consume();
    }

    /**
     * MousePress.
     */
    public void mousePress(ViewEvent anEvent)
    {
        // Get SelAnchor, SelLead on MousePress
        _oldAnchor = _list._items.getSelAnchor();
        _oldLead = _list._items.getSelLead();

        // Get SelAnchor of MousePress
        _newAnchor = _list.getRowForY(anEvent.getY());

        // Get whether MousePress hit selected cell
        ListCell anchorCell = _list.getCellForRow(_newAnchor);
        _anchorCellSelected = anchorCell!=null && anchorCell.isSelected();

        // Cache MouseDown Selection
        _mouseDownSel = _list.getSelIndexes();

        // Set DragSelect
        _dragSelect = !_list.getEventAdapter().isEnabled(ViewEvent.Type.DragGesture) || anEvent.getClickCount()>1;

        // Do basic Press or Drag selection
        mousePressOrDrag(anEvent);

        // Set whether wants drag
        _dragGestureEnabled = isDragSelect() && _list.getEventAdapter().isEnabled(View.DragGesture);
        if (_dragGestureEnabled)
            _list.getEventAdapter().setEnabled(View.DragGesture, false);
    }

    /**
     * MouseDrag.
     */
    public void mouseDrag(ViewEvent anEvent)
    {
        if (isDragSelect())
            mousePressOrDrag(anEvent);
    }

    /**
     * Basic Press or Drag selection.
     */
    protected void mousePressOrDrag(ViewEvent anEvent)
    {
        // Get row-index/cell at mouse point (if no cell, just return)
        int newLead = _list.getRowForY(anEvent.getY());
        ListCell cell = _list.getCellForRow(newLead);
        if (cell==null || !cell.isEnabled())
            return;

        // Handle ShortCut down: If AnchorCellSelected then add, otherwise remove
        if (anEvent.isShortcutDown()) {

            // Restore MouseDown selection
            _list.setSelIndexes(_mouseDownSel);

            // Handle adding
            if (!_anchorCellSelected)
                _list._items.addSelInterval(_newAnchor, newLead);
            else _list._items.removeSelInterval(_newAnchor, newLead);
        }

        // Handle Shift down: Select
        else if (anEvent.isShiftDown() && _mouseDownSel.length>0) {

            // Restore MouseDown selection
            _list.setSelIndexes(_mouseDownSel);

            // Clear OldRange
            _list._items.removeSelInterval(_oldAnchor, _oldLead);

            // Get NewAnchor: If NewAnchor is before OldAnchor+OldLead then use OldLead, otherwise use OldAnchor
            boolean isBefore = _newAnchor<_oldAnchor && _oldAnchor<=_oldLead || _newAnchor>_oldAnchor && _oldAnchor>=_oldLead;
            int newAnchor = isBefore ? _oldLead : _oldAnchor;

            // Add interval from NewAnchor to NewLead
            _list._items.addSelInterval(newAnchor, newLead);
        }

        // Handle normal drag selection
        else {
            if (_anchorCellSelected && _list.isMultiSel())
                return;
            _list._items.setSelInterval(_newAnchor, newLead);
        }
    }

    /**
     * MouseRelease.
     */
    public void mouseRelease(ViewEvent anEvent)
    {
        // Fire action event
        _list.fireActionEvent(anEvent);

        // Re-instate DragGesture if needed
        if (_dragGestureEnabled)
            _list.getEventAdapter().setEnabled(View.DragGesture, true);

        // Start editing if needed
        if (anEvent.isMouseClick() && anEvent.getClickCount()>1 && _list.isEditable()) {
            ListCell cell = _list.getCellForY(anEvent.getY()); if (cell==null) return;
            _list.editCell(cell);
        }
    }

    /**
     * Returns whether selection allowed on MouseDrag.
     * Need to disable if DragEvents are enabled, unless event is double-click.
     */
    public boolean isDragSelect()
    {
        return _dragSelect;
    }
}
