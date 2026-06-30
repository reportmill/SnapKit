package snap.view;
import snap.util.ListSel2D;

/**
 * A class to handle TableView input interaction.
 */
class TableViewSelector {

    // The TableView
    private final TableView<?> _table;

    // The Selection on MousePress
    private ListSel2D _mouseDownSel;

    // The new SelAnchor (index of MousePress)
    private int _newAnchorX, _newAnchorY;

    // Whether table has drag gesture enabled
    private boolean _dragGestureEnabled;

    /**
     * Constructor.
     */
    public TableViewSelector(TableView<?> tableView)
    {
        _table = tableView;
    }

    /**
     * Handle mouse events.
     */
    protected void handleMouseEvent(ViewEvent anEvent)
    {
        // Dispatch MousePress
        switch (anEvent.getType()) {
            case MousePress -> mousePress(anEvent);
            case MouseDrag -> mouseDrag(anEvent);
            case MouseRelease -> mouseRelease(anEvent);
        }
        anEvent.consume();
    }

    /**
     * MousePress.
     */
    private void mousePress(ViewEvent anEvent)
    {
        _mouseDownSel = _table.getSel2D();
        _newAnchorX = _table.getColIndexForX(anEvent.getX());
        _newAnchorY = _table.getRowIndexForY(anEvent.getY());
        _dragGestureEnabled = _table.getEventAdapter().isTypeEnabled(EventType.DragGesture);

        mousePressOrDrag(anEvent);
    }

    /**
     * MouseDrag.
     */
    private void mouseDrag(ViewEvent anEvent)
    {
        if (!_dragGestureEnabled)
            mousePressOrDrag(anEvent);
    }

    /**
     * Basic Press or Drag selection.
     */
    private void mousePressOrDrag(ViewEvent anEvent)
    {
        // Get row-index/cell at mouse point (if no cell, just return)
        ListCell<?> cell = _table.getCellForXY(anEvent.getX(), anEvent.getY());
        if (cell == null || !cell.isEnabled())
            return;

        // Get row/col
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
    private void mouseRelease(ViewEvent anEvent)
    {
        // Start editing if needed
        if (anEvent.isMouseClick() && anEvent.getClickCount() > 1 && _table.isEditable()) {
            ListCell<?> cell = _table.getCellForXY(anEvent.getX(), anEvent.getY());
            if (cell == null)
                return;
            _table.editCell(cell);
        }

        // Otherwise, just fire action
        else _table.fireActionEvent(anEvent);
    }
}
