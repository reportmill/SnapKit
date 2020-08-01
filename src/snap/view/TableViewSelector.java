package snap.view;

/**
 * A class to handle TableView input interaction.
 */
class TableViewSelector {

    // The TableView
    private final TableView  _table;

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
        // Handle MousePress
        if (anEvent.isMousePress())
            mousePress(anEvent);

        // Handle Mouse double-click
        if (anEvent.isMouseClick() && anEvent.getClickCount()==2 && _table.isEditable()) {
            ListCell cell = _table.getCellForXY(anEvent.getX(), anEvent.getY());
            _table.editCell(cell);
        }

        // Handle KeyPress
        else if (anEvent.isKeyPress())
            processKeyEvent(anEvent);
    }

    /**
     * MousePress.
     */
    public void mousePress(ViewEvent anEvent)
    {
        TableCol col = _table.getColForX(anEvent.getX());
        int index = col!=null ? col.getColIndex() : -1;
        if (index>=0 && index!=_table.getSelCol()) {
            _table.setSelCol(index);
            _table.fireActionEvent(anEvent);
            anEvent.consume();
        }
    }

    /**
     * Handle events.
     */
    protected void processKeyEvent(ViewEvent anEvent)
    {
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
