package snap.view;
import snap.util.ListSel;

/**
 * A class to handle ListView selection.
 */
class ListViewSelector<T> {

    // This ListView
    private final ListView<T> _listView;

    // The Selection on MousePress
    private ListSel _mouseDownSel;

    // The new SelAnchor (index of MousePress)
    private int _newAnchor;

    // Whether list has drag gesture enabled
    private boolean _dragGestureEnabled;

    /**
     * Constructor.
     */
    public ListViewSelector(ListView<T> listView)
    {
        _listView = listView;
    }

    /**
     * Process events.
     */
    protected void processMouseEvent(ViewEvent anEvent)
    {
        switch (anEvent.getType()) {
            case MousePress -> mousePress(anEvent);
            case MouseDrag -> mouseDrag(anEvent);
            case MouseRelease -> mouseRelease(anEvent);
            case MouseExit -> _listView.setTargetedIndex(-1);
            case MouseMove -> {
                if (_listView.isTargeting()) {
                    int index = _listView.getRowIndexForY(anEvent.getY());
                    if (index >= _listView.getItemCount())
                        index = -1;
                    _listView.setTargetedIndex(index);
                }
            }
        }

        // Consume all mouse events
        anEvent.consume();
    }

    /**
     * MousePress.
     */
    private void mousePress(ViewEvent anEvent)
    {
        _mouseDownSel = _listView.getSel();
        _newAnchor = _listView.getRowIndexForY(anEvent.getY());
        _dragGestureEnabled = _listView.getEventAdapter().isEnabled(ViewEvent.Type.DragGesture);

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
        int newLead = _listView.getRowIndexForY(anEvent.getY());
        ListCell<?> cell = _listView.getCellForRow(newLead);
        if (cell == null || !cell.isEnabled())
            return;

        // Handle ShortCut down: If AnchorCellSelected then add, otherwise remove
        if (anEvent.isShortcutDown() && !anEvent.isPopupTrigger()) {
            ListSel sel = _mouseDownSel.copyForMetaAdd(_newAnchor, newLead);
            _listView.setSel(sel);
        }

        // Handle Shift down: Select
        else if (anEvent.isShiftDown()) {
            ListSel sel = _mouseDownSel.copyForShiftAdd(_newAnchor, newLead);
            _listView.setSel(sel);
        }

        // Handle normal drag selection
        else {
            ListSel sel = new ListSel(_newAnchor, newLead);
            _listView.setSel(sel);
        }
    }

    /**
     * MouseRelease.
     */
    private void mouseRelease(ViewEvent anEvent)
    {
        // Fire action event
        _listView.fireActionEvent(anEvent);

        // Start editing if needed
        if (anEvent.isMouseClick() && anEvent.getClickCount() > 1 && _listView.isEditable()) {
            ListCell<T> cell = _listView.getCellForY(anEvent.getY());
            if (cell != null)
                _listView.editCell(cell);
        }
    }
}
