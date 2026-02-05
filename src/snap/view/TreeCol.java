/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.List;
import java.util.function.Consumer;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;
import snap.util.ListUtils;

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
    private static final Insets BRANCH_IMAGE_PADDING = new Insets(3, 4, 2, 4);
    
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
    public int getColIndex()  { return ListUtils.indexOfId(getTree().getCols(), this); }

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
            int colIndex = ListUtils.indexOfId(getTree().getCols(), this);
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
    @Override
    protected void configureCell(ListCell <T> aCell)
    {
        // Do normal version
        configureCellFills(aCell);
        Consumer<ListCell<T>> cellConfigure = getCellConfigure();
        if (cellConfigure != null)
            cellConfigure.accept(aCell);

        // Get cell item
        T item = aCell.getItem();
        if (item == null)
            return;

        // Get tree and tree resolver
        TreeView <T> tree = getTree();
        TreeResolver<T> treeResolver = tree.getResolver();

        // If no cell configure, configure text (from item text function or tree resolver)
        if (cellConfigure == null) {
            var itemTextFunc = getItemTextFunction();
            String itemText = itemTextFunc != null ? itemTextFunc.apply(item) : treeResolver.getText(item);
             aCell.setText(itemText);
        }

        // Configure cell image
        Image itemImage = treeResolver.getImage(item);
        if (itemImage != null)
            aCell.setImage(itemImage);

        // If not main column, just return
        int col = getColIndex();
        if (col > 0)
            return;

        // Calculate indent level and set cell indent
        int indentLevel = tree.getItemParentCount(item) + 1;
        double indentW = tree.getExpandedImage().getWidth() + BRANCH_IMAGE_PADDING.getWidth();
        aCell.setPadding(0, 2, 0, indentLevel * indentW);

        // If item is parent, configure branch image
        if (tree.isItemParent(item)) {

            // If branch image view already present, update image and return
            ImageView branchImageView = (ImageView) aCell.getChildForName("BranchImageView");
            if (branchImageView != null) {
                branchImageView.setImage(tree.isItemExpanded(item) ? tree.getExpandedImage() : tree.getCollapsedImage());
                return;
            }

            // Create branch image view
            Image branchImage = tree.isItemExpanded(item) ? tree.getExpandedImage() : tree.getCollapsedImage();
            branchImageView = new ImageView(branchImage);
            branchImageView.setName("BranchImageView");
            branchImageView.setMargin(0, 0, 0, (indentLevel - 1) * indentW);
            branchImageView.setPadding(BRANCH_IMAGE_PADDING);
            branchImageView.setLean(Pos.CENTER_LEFT);
            branchImageView.setManaged(false);
            branchImageView.addEventHandler(e -> handleBranchImageViewMousePress(e, item), MousePress);
            ViewUtils.addChild(aCell, branchImageView);
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