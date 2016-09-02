package snap.view;
import java.util.List;
import java.util.function.Consumer;
import snap.gfx.*;
import snap.util.ListUtils;

/**
 * A custom class.
 */
public class TreeCol <T> extends ListView <T> {

    // The header value
    String             _headerText;
    
    // Whether is resizable
    boolean            _resizable;
    
/**
 * Creates a new TreeCol.
 */
public TreeCol()
{
    setGrowWidth(true);
    setAltPaint(null);
    setFocusWhenPressed(false);
}

/**
 * Returns the tree.
 */
public TreeView getTree()  { return (TreeView)getParent(); }

/**
 * Returns the header value.
 */
public String getHeaderText()  { return _headerText; }

/**
 * Sets the header value.
 */
public void setHeaderText(String aValue)
{
    firePropChange("HeaderText", _headerText, _headerText = aValue);
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
public double getRowHeight()  { return getTree().getRowHeight(); }

/**
 * Sets the selected index.
 */
public void setSelectedIndex(int anIndex)
{
    if(anIndex==getSelectedIndex()) return;
    super.setSelectedIndex(anIndex);
    //getTable().setSelectedCol(getColIndex());
    getTree().setSelectedIndex(anIndex);
}

/**
 * Returns the column index.
 */
public int getColIndex()  { return ListUtils.indexOfId(getTree().getCols(), this); }

/**
 * Called to set method for rendering.
 */
public Consumer <ListCell<T>> getCellConfigure()
{
    Consumer <ListCell<T>> cconf = super.getCellConfigure();
    return cconf!=null? cconf : getTree().getCellConfigure();
}

/**
 * Overrride to add branch icons.
 */
protected void configureCell(ListCell <T> aCell)
{
    // Do normal version
    super.configureCell(aCell);
    
    // Get tree, column index and cell item
    TreeView <T> tree = getTree();
    int col = getColIndex();
    T item = aCell.getItem(); if(item==null) return;
    
    // Configure text
    aCell.setText(tree.getText(item, col));
    if(col>0) return;
    
    // Configure graphic
    //Image img = tree.getImage(item); if(img!=null) aCell.setImage(img);
    View graphic = tree.getGraphic(item); if(graphic!=null) aCell.setGraphic(graphic);
    
    // Calculate and set cell indent
    int rootIndent = tree.isShowRoot()? 20 : 0; if(!tree.isParent(item)) rootIndent += 18;
    int pcount = tree.getParentCount(item);
    aCell.setPadding(0, 2, 0, rootIndent + pcount*20);
    
    // Add Expand/Collapse graphic
    if(tree.isParent(item)) {
        Image bimg = tree.isExpanded(item)? tree.getExpandedImage() : tree.getCollapsedImage();
        ImageView iview = new ImageView(bimg); iview.setPadding(4,4,4,4);
        View gview = aCell.getGraphic()!=null? new Label(iview, null, aCell.getGraphic()) : iview;
        aCell.setGraphic(gview);
        iview.addEventHandler(e -> { tree.toggleItem(item); e.consume(); }, MousePressed);
    }
}

/**
 * Override to set TreeView.SelCol.
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMousePressed()) getTree()._selCol = ListUtils.indexOfId(getTree().getCols(), this);
    super.processEvent(anEvent);
}

/**
 * Override to return false (if col is setting items, they must be new).
 */
protected boolean equalsItems(List theItems)  { return false; }

}