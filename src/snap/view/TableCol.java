/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.List;
import java.util.function.Consumer;

import snap.props.PropSet;
import snap.util.*;

/**
 * Represents a Table Column.
 */
public class TableCol <T> extends ListView <T> {
    
    // The Table
    protected TableView<T> _table;

    // The header value
    private Label _header;
    
    // Whether is resizable
    private boolean _resizable;

    // Constants for properties
    public static final String HeaderText_Prop = "HeaderText";
    public static final String Resizable_Prop = "Resizable";

    /**
     * Constructor.
     */
    public TableCol()
    {
        super();
        setFocusable(false);
        setOverflow(Overflow.Visible);

        // Create and config header
        _header = new Label();
        _header.setPadding(4,4,4,4);

        // Events
        disableEvents(MousePress, MouseDrag, MouseRelease);
    }

    /**
     * Returns the table.
     */
    public TableView<T> getTable()  { return _table; }

    /**
     * Returns the header label.
     */
    public Label getHeader()  { return _header; }

    /**
     * Returns the header text.
     */
    public String getHeaderText()  { return _header.getText(); }

    /**
     * Sets the header text.
     */
    public void setHeaderText(String aValue)  { _header.setText(aValue); }

    /**
     * Returns whether resizable.
     */
    public boolean isResizable()  { return _resizable; }

    /**
     * Sets the resizable.
     */
    public void setResizable(boolean aValue)
    {
        firePropChange("Resizable", _resizable, _resizable = aValue);
    }

    /**
     * Override to get row height from table.
     */
    public double getRowHeight()  { return getTable().getRowHeight(); }

    /**
     * Override to get row height from table.
     */
    protected double getRowHeightSuper()  { return super.getRowHeight(); }

    /**
     * Returns the column index.
     */
    public int getColIndex()  { return ArrayUtils.indexOfId(getTable().getCols(), this); }

    /**
     * Override to return table cell configure if one isn't set for col.
     */
    public Consumer <ListCell<T>> getCellConfigure()
    {
        Consumer <ListCell<T>> cellConfigure = super.getCellConfigure();
        return cellConfigure != null ? cellConfigure : getTable().getCellConfigure();
    }

    /**
     * Override to suppress setting items in pick list (already done by TableView).
     */
    public void setItems(List <T> theItems)  { }

    /**
     * Override to account for header (if showing).
     */
    protected double getPrefWidthImpl(double aH)
    {
        // Do normal version
        double prefW = super.getPrefWidthImpl(aH);

        // Add Header.PrefWidth
        TableView<T> table = getTable();
        if (table != null && table.isShowHeader()) {
            double headerPrefW = getHeader().getPrefWidth(aH);
            prefW = Math.max(headerPrefW, prefW);
        }

        // Return
        return prefW;
    }

    /**
     * Override to forward to TableView.
     */
    @Override
    protected void cellEditingChanged(ListCell<T> aCell)
    {
        super.cellEditingChanged(aCell);
        TableView<T> table = getTable(); if (table == null) return;
        table.cellEditingChanged(aCell);
    }

    /**
     * Override to forward to table.
     */
    protected View getFocusNext(View aChild)  { return getTable().getFocusNext(); }

    /**
     * Override to forward to table.
     */
    protected View getFocusPrev(View aChild)  { return getTable().getFocusPrev(); }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // HeaderText, Resizable
        aPropSet.addPropNamed(HeaderText_Prop, String.class, EMPTY_OBJECT);
        aPropSet.addPropNamed(Resizable_Prop, boolean.class, false);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // HeaderText, Resizable
            case HeaderText_Prop: return getHeaderText();
            case Resizable_Prop: return isResizable();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // HeaderText, Resizable
            case HeaderText_Prop: setHeaderText(Convert.stringValue(aValue)); break;
            case Resizable_Prop: setResizable(Convert.boolValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival - table columns.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Create xml for column
        XMLElement e = super.toXMLView(anArchiver);

        // Archive HeaderText, Resizable
        if (!isPropDefault(HeaderText_Prop)) e.add(HeaderText_Prop, getHeaderText());
        if (!isPropDefault(Resizable_Prop)) e.add(Resizable_Prop, false);

        // Return column xml
        return e;
    }

    /**
     * XML unarchival - table columns.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Do normal version
        super.fromXMLView(anArchiver, anElement);

        // Unarchive HeaderText, Resizable
        if (anElement.hasAttribute(HeaderText_Prop)) setHeaderText(anElement.getAttributeValue(HeaderText_Prop));
        if (anElement.hasAttribute(Resizable_Prop)) setResizable(anElement.getAttributeBoolValue(Resizable_Prop));
    }
}