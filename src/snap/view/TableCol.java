/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.function.Consumer;
import snap.util.*;

/**
 * Represents a Table Column.
 */
public class TableCol <T> extends ListView <T> {

    // The header value
    String             _headerVal;
    
    // Whether is resizable
    boolean            _resizable;

/**
 * Creates a new TableCol.
 */
public TableCol()  { setFocusWhenPressed(false); }

/**
 * Returns the table.
 */
public TableView getTable()  { return (TableView)getParent(); }

/**
 * Returns the header value.
 */
public String getHeaderValue()  { return _headerVal; }

/**
 * Sets the header value.
 */
public void setHeaderValue(String aValue)
{
    firePropChange("HeaderValue", _headerVal, _headerVal = aValue);
}

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
 * Sets the selected index.
 */
public void setSelectedIndex(int anIndex)
{
    if(anIndex==getSelectedIndex()) return;
    super.setSelectedIndex(anIndex);
    //getTable().setSelectedCol(getColIndex());
    getTable().setSelectedIndex(anIndex);
}

/**
 * Returns the column index.
 */
public int getColIndex()  { return ArrayUtils.indexOfId(getTable().getCols(), this); }

/**
 * Called to set method for rendering.
 */
public Consumer <ListCell<T>> getCellConfigure()
{
    Consumer <ListCell<T>> cconf = super.getCellConfigure();
    return cconf!=null? cconf : getTable().getCellConfigure();
}

/**
 * Override to set Table.SelCol.
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMousePress()) getTable()._selCol = ArrayUtils.indexOfId(getTable().getCols(), this);
    super.processEvent(anEvent);
}

/**
 * XML archival - table columns.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Create xml for column
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Header, Resizable
    if(getHeaderValue()!=null) e.add("Header", getHeaderValue());
    if(isResizable()) e.add("Resizable", false);
    
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
    
    // Unarchive Header, Resizable
    if(anElement.hasAttribute("Header")) setHeaderValue(anElement.getAttributeValue("Header"));
    if(anElement.hasAttribute("Resizable")) setResizable(anElement.getAttributeBoolValue("Resizable"));
}

}