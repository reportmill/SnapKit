package snap.view;
import snap.util.ListSel;

/**
 * A class to handle ListArea selection.
 */
class ListAreaSelector {

    // This ListArea
    private final ListArea  _listArea;

    // Whether selection allowed on MouseDrag
    protected boolean  _dragSelect;

    // The Selection on MousePress
    private ListSel  _mouseDownSel;

    // The new SelAnchor (index of MousePress)
    protected int  _newAnchor;

    // Whether list previously wanted drag
    private boolean  _dragGestureEnabled;

    /**
     * Constructor.
     */
    public ListAreaSelector(ListArea aListArea)
    {
        _listArea = aListArea;
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
                case KeyCode.UP: _listArea.selectUp(); anEvent.consume(); break;
                case KeyCode.DOWN: _listArea.selectDown(); anEvent.consume(); break;
                case KeyCode.ENTER: _listArea.processEnterAction(anEvent); break;
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
            _listArea.setTargetedIndex(-1);

        // Handle MouseMove
        else if (anEvent.isMouseMove() && _listArea.isTargeting()) {
            int index = _listArea.getRowIndexForY(anEvent.getY());
            if (index>= _listArea.getItemCount()) index = -1;
            _listArea.setTargetedIndex(index);
        }

        // Consume all mouse events
        anEvent.consume();
    }

    /**
     * MousePress.
     */
    public void mousePress(ViewEvent anEvent)
    {
        // Cache MouseDown Selection
        _mouseDownSel = _listArea.getSel();

        // Get SelAnchor of MousePress
        _newAnchor = _listArea.getRowIndexForY(anEvent.getY());

        // Set DragSelect
        _dragSelect = !_listArea.getEventAdapter().isEnabled(ViewEvent.Type.DragGesture) || anEvent.getClickCount()>1;

        // Do basic Press or Drag selection
        mousePressOrDrag(anEvent);

        // Set whether wants drag
        _dragGestureEnabled = _dragSelect && _listArea.getEventAdapter().isEnabled(ViewEvent.Type.DragGesture);
        if (_dragGestureEnabled)
            _listArea.getEventAdapter().setEnabled(ViewEvent.Type.DragGesture, false);
    }

    /**
     * MouseDrag.
     */
    public void mouseDrag(ViewEvent anEvent)
    {
        if (_dragSelect)
            mousePressOrDrag(anEvent);
    }

    /**
     * Basic Press or Drag selection.
     */
    protected void mousePressOrDrag(ViewEvent anEvent)
    {
        // Get row-index/cell at mouse point (if no cell, just return)
        int newLead = _listArea.getRowIndexForY(anEvent.getY());
        ListCell cell = _listArea.getCellForRow(newLead);
        if (cell==null || !cell.isEnabled())
            return;

        // Handle ShortCut down: If AnchorCellSelected then add, otherwise remove
        if (anEvent.isShortcutDown()) {
            ListSel sel = _mouseDownSel.copyForMetaAdd(_newAnchor, newLead);
            _listArea.setSel(sel);
        }

        // Handle Shift down: Select
        else if (anEvent.isShiftDown()) {
            ListSel sel = _mouseDownSel.copyForShiftAdd(_newAnchor, newLead);
            _listArea.setSel(sel);
        }

        // Handle normal drag selection
        else {
            ListSel sel = new ListSel(_newAnchor, newLead);
            _listArea.setSel(sel);
        }
    }

    /**
     * MouseRelease.
     */
    public void mouseRelease(ViewEvent anEvent)
    {
        // Fire action event
        _listArea.fireActionEvent(anEvent);

        // Re-instate DragGesture if needed
        if (_dragGestureEnabled)
            _listArea.getEventAdapter().setEnabled(ViewEvent.Type.DragGesture, true);

        // Start editing if needed
        if (anEvent.isMouseClick() && anEvent.getClickCount()>1 && _listArea.isEditable()) {
            ListCell cell = _listArea.getCellForY(anEvent.getY()); if (cell==null) return;
            _listArea.editCell(cell);
        }
    }
}
