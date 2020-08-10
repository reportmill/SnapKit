package snap.view;
import snap.util.ListSel2D;

/**
 * A class to handle TableView input interaction.
 */
class TableViewSelector {

    // The TableView
    private final TableView  _table;

    // Whether selection allowed on MouseDrag
    protected boolean  _dragSelect;

    // The Selection on MousePress
    private ListSel2D _mouseDownSel;

    // The new SelAnchor (index of MousePress)
    protected int  _newAnchorX, _newAnchorY;

    // Whether list previously wanted drag
    private boolean  _dragGestureEnabled;

    /**
     * Constructor.
     */
    public TableViewSelector(TableView aTV)
    {
        _table = aTV;
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseEvent
        if (anEvent.isMouseEvent())
            processMouseEvent(anEvent);

        // Handle KeyPress
        else if (anEvent.isKeyPress())
            processKeyEvent(anEvent);
    }

    /**
     * Process events.
     */
    protected void processMouseEvent(ViewEvent anEvent)
    {
        // Dispatch MousePress
        if (anEvent.isMousePress())
            mousePress(anEvent);
        else if (anEvent.isMouseDrag())
            mouseDrag(anEvent);
        else if (anEvent.isMouseRelease())
            mouseRelease(anEvent);
        anEvent.consume();
    }

    /**
     * MousePress.
     */
    public void mousePress(ViewEvent anEvent)
    {
        // Cache MouseDown Selection
        _mouseDownSel = _table.getSel2D();

        // Get SelAnchor of MousePress
        _newAnchorX = _table.getColIndexForX(anEvent.getX());
        _newAnchorY = _table.getRowIndexForY(anEvent.getY());

        // Set DragSelect
        _dragSelect = !_table.getEventAdapter().isEnabled(ViewEvent.Type.DragGesture) || anEvent.getClickCount()>1;

        // Do basic Press or Drag selection
        mousePressOrDrag(anEvent);

        // Set whether wants drag
        _dragGestureEnabled = _dragSelect && _table.getEventAdapter().isEnabled(View.DragGesture);
        if (_dragGestureEnabled)
            _table.getEventAdapter().setEnabled(View.DragGesture, false);
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
        ListCell cell = _table.getCellForXY(anEvent.getX(), anEvent.getY());
        if (cell==null || !cell.isEnabled())
            return;
        int newLeadX = cell.getCol();
        int newLeadY = cell.getRow();

        // Handle ShortCut down: If AnchorCellSelected then add, otherwise remove
        if (anEvent.isShortcutDown()) {
            ListSel2D sel = _mouseDownSel.copyForMetaAdd(_newAnchorX, _newAnchorY, newLeadX, newLeadY);
            _table.setSel2D(sel);
        }

        // Handle Shift down: Select
        else if (anEvent.isShiftDown()) {
            ListSel2D sel = _mouseDownSel.copyForShiftAdd(_newAnchorX, _newAnchorY, newLeadX, newLeadY);
            _table.setSel2D(sel);
        }

        // Handle normal drag selection
        else {
            ListSel2D sel = new ListSel2D(_newAnchorX, _newAnchorY, newLeadX, newLeadY);
            _table.setSel2D(sel);
        }
    }

    /**
     * MouseRelease.
     */
    public void mouseRelease(ViewEvent anEvent)
    {
        // Re-instate DragGesture if needed
        if (_dragGestureEnabled)
            _table.getEventAdapter().setEnabled(View.DragGesture, true);

        // Start editing if needed
        if (anEvent.isMouseClick() && anEvent.getClickCount()>1 && _table.isEditable()) {
            ListCell cell = _table.getCellForXY(anEvent.getX(), anEvent.getY()); if (cell==null) return;
            _table.editCell(cell);
        }

        // Otherwise, just fire action
        else _table.fireActionEvent(anEvent);
    }

    /**
     * Handle events.
     */
    protected void processKeyEvent(ViewEvent anEvent)
    {
        // If shortcut key, just return
        if (anEvent.isShortcutDown() || anEvent.isControlDown())
            return;

        // Handle special keys
        int kcode = anEvent.getKeyCode();
        switch (kcode) {
            case KeyCode.UP: _table.selectUp(); _table.fireActionEvent(anEvent); anEvent.consume(); break;
            case KeyCode.DOWN: _table.selectDown(); _table.fireActionEvent(anEvent); anEvent.consume(); break;
            case KeyCode.LEFT: _table.selectLeft(); _table.fireActionEvent(anEvent); anEvent.consume(); break;
            case KeyCode.RIGHT: _table.selectRight(); _table.fireActionEvent(anEvent); anEvent.consume(); break;
            default: {
                char c = anEvent.getKeyChar();
                boolean printable = Character.isJavaIdentifierPart(c); // Lame
                if (_table.isEditable() && printable) {
                    ListCell cell = _table.getSelCell();
                    _table.editCell(cell);
                }
            }
        }
    }
}
