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
public int getColIndex()  { return ListUtils.indexOfId(getTable().getCols(), this); }

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
    if(anEvent.isMousePressed()) getTable()._selCol = ListUtils.indexOfId(getTable().getCols(), this);
    super.processEvent(anEvent);
}

/**
 * XML archival - table columns.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Create xml for column
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive HeaderValue
    if(getHeaderValue()!=null) e.add("Header", getHeaderValue());
    
    // Archive ItemKey
    if(getItemKey()!=null && getItemKey().length()>0 && !getItemKey().equals(getHeaderValue()))
        e.add("ItemKey", getItemKey());
    
    // Archive model index
    //if(getModelIndex()!=anIndex) cxml.add("index", getModelIndex());
    
    // Resizable
    if(!isResizable()) e.add("resizable", false);
    
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
    
    // Unarchive ColumnHeader
    if(anElement.hasAttribute("Header")) setHeaderValue(anElement.getAttributeValue("Header"));
    
    // Unarchive ItemKey
    if(anElement.hasAttribute("ItemKey")) setItemKey(anElement.getAttributeValue("ItemKey"));
    
    // Unarchive Width, MinWidth, MaxWidth, PrefWidth, Resizable, GrowWidth
    if(anElement.hasAttribute("width")) setWidth(anElement.getAttributeIntValue("width"));
    if(anElement.hasAttribute("MinWidth")) setMinWidth(anElement.getAttributeIntValue("MinWidth"));
    if(anElement.hasAttribute("PrefWidth")) setPrefWidth(anElement.getAttributeIntValue("PrefWidth"));
    if(anElement.hasAttribute("resizable")) setResizable(anElement.getAttributeBoolValue("resizable"));
    if(anElement.hasAttribute("GrowWidth")) setGrowWidth(anElement.getAttributeBoolValue("GrowWidth"));
}

}