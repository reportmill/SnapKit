/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.List;
import java.util.function.Consumer;
import snap.geom.Insets;
import snap.gfx.*;
import snap.util.ArrayUtils;

/**
 * A ListView subclass that represents a column in TreeView.
 */
public class TreeCol <T> extends ListView <T> {

    // The TreeView
    private TreeView<T> _tree;
    
    // The header value
    private String _headerText;
    
    // Whether is resizable
    private boolean _resizable;

    // Constant for branch image padding
    private static final Insets BRANCH_IMAGE_PADDING = new Insets(2, 4, 2, 4);
    
    /**
     * Constructor.
     */
    public TreeCol()
    {
        super();
        setFocusable(false);
        setGrowWidth(true);
        setOverflow(Overflow.Visible);
        setAltRowColor(null);
    }

    /**
     * Returns the tree.
     */
    public TreeView<T> getTree()  { return _tree; }

    /**
     * Sets the tree.
     */
    protected void setTree(TreeView<T> treeView)  { _tree = treeView; }

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
    public void setItemsList(List <T> theItems)  { }

    /**
     * Override to set TreeView.SelCol.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        if (anEvent.isMousePress()) {
            int colIndex = ArrayUtils.indexOfId(getTree().getCols(), this);
            getTree().setSelCol(colIndex);
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
        Consumer <ListCell<T>> cellConf = super.getCellConfigure();
        return cellConf != null ? cellConf : getTree().getCellConfigure();
    }

    /**
     * Override to add branch icons.
     */
    protected void configureCell(ListCell <T> aCell)
    {
        // Do normal version
        super.configureCell(aCell);

        // Get tree, column index and cell item
        TreeView <T> tree = getTree();
        int col = getColIndex();
        T item = aCell.getItem();
        if (item == null)
            return;

        // Configure cell text
        String itemText = tree.getItemText(item, col);
        aCell.setText(itemText);
        if (col > 0)
            return;

        // Configure cell graphic
        View itemGraphic = tree.getItemGraphic(item);
        if (itemGraphic != null)
            aCell.setGraphic(itemGraphic);

        // Calculate indent level
        int indentLevel = tree.getItemParentCount(item);
        if (!tree.isItemParent(item))
            indentLevel++;

        // Set cell indent
        double indentW = tree.getExpandedImage().getWidth() + BRANCH_IMAGE_PADDING.getWidth();
        aCell.setPadding(0, 2, 0, indentLevel * indentW);

        // If item is parent, configure branch image
        if (tree.isItemParent(item)) {

            // Get branch image
            Image branchImage = tree.isItemExpanded(item) ? tree.getExpandedImage() : tree.getCollapsedImage();

            // If branch image view already present, update image and return
            ImageView branchImageView = (ImageView) aCell.getChildForName("BranchImageView");
            if (branchImageView != null) {
                branchImageView.setImage(branchImage);
                return;
            }

            // Create branch image view
            branchImageView = new ImageView(branchImage);
            branchImageView.setName("BranchImageView");
            branchImageView.setPadding(BRANCH_IMAGE_PADDING);
            branchImageView.addEventHandler(e -> handleBranchImageViewMousePress(e, item), MousePress);

            // If no item graphic, just set branch image as cell graphic
            if (itemGraphic == null)
                aCell.setGraphic(branchImageView);

            // If both a branch image and an item graphic, wrap in a label
            else {
                View branchImageAndItemGraphicLabel = new Label(branchImageView, null, itemGraphic);
                branchImageAndItemGraphicLabel.setSpacing(0);
                aCell.setGraphic(branchImageAndItemGraphicLabel);
            }
        }
    }

    /**
     * Called when branch image view is clicked.
     */
    private void handleBranchImageViewMousePress(ViewEvent anEvent, T anItem)
    {
        TreeView <T> tree = getTree();
        tree.toggleItem(anItem);
        anEvent.consume();
    }
}