/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Rect;
import snap.gfx.Color;

/**
 * A label subclass used to render items in Lists, Tables, Trees, Browsers.
 */
public class ListCell <T> extends Label {

    // The ListView that created this cell
    protected ListView<T> _listView;

    // The cell item
    protected T  _item;
    
    // The cell row/column
    protected int  _row, _col;
    
    // Whether cell is selected
    private boolean  _sel;

    // Whether label text is currently being edited
    private boolean  _editing;

    // A textfield for editing
    private TextField  _editor;

    /**
     * Creates a new ListCell.
     */
    public ListCell(ListView<T> theList, T anItem, int aRow, int aCol, boolean isSel)
    {
        _listView = theList;
        _item = anItem;
        _row = aRow; _col = aCol;
        _sel = isSel;
    }

    /**
     * Returns the ListView.
     */
    public ListView<T> getListView()  { return _listView; }

    /**
     * Returns the item.
     */
    public T getItem()  { return _item; }

    /**
     * Returns the row.
     */
    public int getRow()  { return _row; }

    /**
     * Returns the column.
     */
    public int getCol()  { return _col; }

    /**
     * Returns whether cell is selected.
     */
    public boolean isSelected()  { return _sel; }

    /**
     * Returns whether editable.
     */
    public boolean isEditing()  { return _editing; }

    /**
     * Sets editing.
     */
    public void setEditing(boolean aValue)
    {
        // If value already set, just return
        if (aValue == isEditing()) return;
        _editing = aValue;

        // Handle set true
        if (aValue) {

            // Get editor
            TextField editor = getEditor();
            editor.setText(getText());

            // Set text bounds
            Rect textBounds = getTextBounds();
            textBounds.inset(-2);
            editor.setBounds(textBounds);

            // Add editor
            addChild(editor);
            editor.selectAll();
            editor.requestFocus();
        }

        // Handle set false
        else {
            removeChild(_editor);
            setText(_editor.getText());
            _editor = null;
        }

        // Fire prop change
        firePropChange(Editing_Prop, !aValue, aValue);

        // Nofity ListView
        if (_listView != null)
            _listView.cellEditingChanged(this);
    }

    /**
     * Returns the editor.
     */
    public TextField getEditor()
    {
        // If editor set, return
        if (_editor != null) return _editor;

        // Create and return editor
        TextField editor = new TextField();
        editor.setManaged(false);
        editor.setBorderRadius(2);
        editor.setFill(new Color(1,.95));
        editor.setBorder(new Color(1,.3,.3,.5), 1);
        editor.setPadding(1,1,1,1);
        editor.setAlignX(getAlignX());
        editor.setFont(getFont());
        editor.addEventHandler(this::handleEditorActionEvent, Action);
        editor.addPropChangeListener(pc -> editorFocusChanged(editor), Focused_Prop);
        return _editor = editor;
    }

    /**
     * Called when editor text field fires action event.
     */
    protected void handleEditorActionEvent(ViewEvent anEvent)
    {
        setEditing(false);
        fireActionEvent(anEvent);
    }

    /**
     * Called when editor focus changes.
     */
    protected void editorFocusChanged(TextField editor)
    {
        if (!editor.isFocused())
            setEditing(false);
    }
}