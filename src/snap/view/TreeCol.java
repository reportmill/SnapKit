/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.List;
import java.util.function.Consumer;
import snap.gfx.*;
import snap.util.ArrayUtils;

/**
 * A ListArea subclass that represents a column in TreeView.
 */
public class TreeCol <T> extends ListArea <T> {

    // The TreeView
    private TreeView  _tree;
    
    // The header value
    private String  _headerText;
    
    // Whether is resizable
    private boolean  _resizable;
    
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
    public TreeView getTree()  { return _tree; }

    /**
     * Sets the tree.
     */
    protected void setTree(TreeView aTV)  { _tree = aTV; }

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
     * Returns the column index.
     */
    public int getColIndex()  { return ArrayUtils.indexOfId(getTree().getCols(), this); }

    /**
     * Override to suppress setting items in pick list (already done by TreeView).
     */
    public void setItems(List <T> theItems)  { }

    /**
     * Override to set TreeView.SelCol.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        if (anEvent.isMousePress()) {
            int ind = ArrayUtils.indexOfId(getTree().getCols(), this);
            getTree().setSelCol(ind);
        }
        super.processEvent(anEvent);
    }

    /**
     * Override to have tree fireAction.
     */
    protected void fireActionEvent(ViewEvent anEvent)  { _tree.fireActionEvent(anEvent); }

    /**
     * Called to set method for rendering.
     */
    public Consumer <ListCell<T>> getCellConfigure()
    {
        Consumer <ListCell<T>> cconf = super.getCellConfigure();
        return cconf!=null ? cconf : getTree().getCellConfigure();
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
        T item = aCell.getItem(); if (item==null) return;

        // Configure text
        aCell.setText(tree.getText(item, col));
        if (col>0) return;

        // Configure graphic
        //Image img = tree.getImage(item); if (img!=null) aCell.setImage(img);
        View graphic = tree.getGraphic(item);
        if (graphic!=null) aCell.setGraphic(graphic);

        // Calculate and set cell indent
        int indent = (int)tree.getExpandedImage().getWidth() + 4 + 4;
        int levels = tree.getParentCount(item); if (!tree.isParent(item)) levels++;
        aCell.setPadding(0, 2, 0, levels*indent);

        // If parent, configure Expand/Collapse image
        if (tree.isParent(item)) {
            Image bimg = tree.isExpanded(item) ? tree.getExpandedImage() : tree.getCollapsedImage();
            ImageView iview = (ImageView)aCell.getChild("BranchImageView");
            if (iview!=null) {
                iview.setImage(bimg); return; }

            iview = new ImageView(bimg);
            iview.setName("BranchImageView");
            iview.setPadding(2,4,2,4);
            View gview = aCell.getGraphic()!=null ? new Label(iview,null,aCell.getGraphic()) : iview;
            if (gview instanceof Label) gview.setSpacing(0);
            aCell.setGraphic(gview);
            iview.addEventHandler(e -> { tree.toggleItem(item); e.consume(); }, MousePress);
        }
    }
}