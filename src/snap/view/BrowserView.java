/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.geom.*;
import snap.gfx.*;
import snap.props.PropSet;
import snap.util.*;

/**
 * A browser class.
 */
public class BrowserView<T> extends ParentView implements Selectable<T> {

    // The first column
    private BrowserCol<T> _col0;

    // The resolver
    private TreeResolver<T> _resolver;

    // Row height
    private int _rowHeight;

    // The Cell Configure method
    private Consumer<ListCell<T>> _cellConf;

    // The preferred number of columns
    private int _prefColCount;

    // The preferred column width
    private int _prefColWidth;

    // The selected column index
    private int _selCol = -1;

    // The view that holds the columns
    private RowView _colView;

    // The ScrollView to hold ColView
    private ScrollView _scrollView;

    // The image to be used for branch
    private Image _branchImage;

    // Whether list needs to scroll selection to visible after next layout or show
    private boolean _needsScrollSelToVisible;

    // Constants for properties
    public static final String PrefColCount_Prop = "PrefColCount";
    public static final String PrefColWidth_Prop = "PrefColWidth";

    // Constants for defaults
    public static final int DEFAULT_ROW_HEIGHT = 20;
    public static final int DEFAULT_PREF_COL_COUNT = 2;
    public static final int DEFAULT_PREF_COL_WIDTH = 150;
    public static final int MAX_VISIBLE_COL_COUNT = 4;

    /**
     * Creates a new BrowserView.
     */
    public BrowserView()
    {
        // Basic config
        super();
        setFocusable(true);
        setFocusWhenPressed(true);
        setActionable(true);
        enableEvents(KeyEvents);

        // Set default values
        _rowHeight = DEFAULT_ROW_HEIGHT;
        _prefColCount = DEFAULT_PREF_COL_COUNT;
        _prefColWidth = DEFAULT_PREF_COL_WIDTH;

        // Create ColView to hold columns
        _colView = new RowView();
        _colView.setFillHeight(true);

        // Create ScrollView to hold ColView
        _scrollView = new ScrollView(_colView);
        _scrollView.setFillHeight(true);
        addChild(_scrollView);

        // Add default column
        _col0 = addCol();

        // Set resolver
        _resolver = new TreeResolver.Adapter<>();
    }

    /**
     * Returns the ScrollView.
     */
    public ScrollView getScrollView()  { return _scrollView; }

    /**
     * Returns the preferred number of visible columns in the browser.
     */
    public int getPrefColCount()  { return _prefColCount; }

    /**
     * Sets the preferred number of visible columns in the browser.
     */
    public void setPrefColCount(int aValue)
    {
        _prefColCount = aValue;
    }

    /**
     * Returns the preferred column width.
     */
    public int getPrefColWidth()  { return _prefColWidth; }

    /**
     * Sets the preferred column width.
     */
    public void setPrefColWidth(int aWidth)
    {
        _prefColWidth = aWidth;
    }

    /**
     * Returns the row height.
     */
    public int getRowHeight()  { return _rowHeight; }

    /**
     * Sets the row height.
     */
    public void setRowHeight(int aValue)
    {
        // Set value
        firePropChange("RowHeight", _rowHeight, _rowHeight = aValue);

        // Update columns
        for (BrowserCol<T> col : getCols())
            col.setRowHeight(aValue);
    }

    /**
     * Called to set method for rendering.
     */
    public Consumer<ListCell<T>> getCellConfigure()  { return _cellConf; }

    /**
     * Called to set method for rendering.
     */
    public void setCellConfigure(Consumer<ListCell<T>> aCC)
    {
        _cellConf = aCC;
    }

    /**
     * Returns the items.
     */
    @Override
    public List<T> getItems()
    {
        return _col0.getItems();
    }

    /**
     * Sets the items.
     */
    @Override
    public void setItems(List<T> theItems)
    {
        // If already set, just return
        if (ListUtils.equalsId(theItems, getItems())) return;
        if (theItems.equals(getItems())) return;

        // Get current selected item and col0 selected item
        T selItem = getSelItem();
        T selItem0 = _col0.getSelItem();

        // Set items
        _col0.setItems(theItems);

        // Reset SelItem
        if (selItem0 == _col0.getSelItem())
            setSelItem(selItem);
        else setSelItem(null);
    }

    /**
     * Called to update given item that may have changed.
     */
    public void updateItem(T theItems)
    {
        boolean found = false;

        // Iterate over columns
        for (BrowserCol<T> col : getCols()) {

            // If item already found in previous column, update all items
            if (found) {
                col.updateItems();
                continue;
            }

            // If item found in this column, update item and set found
            if (col.getItems().contains(theItems)) {
                col.updateItem(theItems);
                if (!col.getSelItem().equals(theItems))
                    return;
                found = true;
            }
        }
    }

    /**
     * Called to update items that have changed.
     */
    public void updateItems(T[] theItems)
    {
        _col0.updateItems(theItems);
    }

    /**
     * Returns the resolver.
     */
    public TreeResolver<T> getResolver()  { return _resolver; }

    /**
     * Sets the resolver.
     */
    public void setResolver(TreeResolver<T> aResolver)
    {
        _resolver = aResolver;
    }

    /**
     * Returns the column count.
     */
    public int getColCount()  { return _colView.getChildCount(); }

    /**
     * Returns the browser column list at given index.
     */
    public BrowserCol<T> getCol(int anIndex)
    {
        ScrollView scrollView = (ScrollView) _colView.getChild(anIndex);
        return (BrowserCol<T>) scrollView.getContent();
    }

    /**
     * Returns the last column.
     */
    public BrowserCol<T> getLastCol()
    {
        return getCol(getColCount() - 1);
    }

    /**
     * Returns the browser columns.
     */
    public List<BrowserCol<T>> getCols()
    {
        List<ScrollView> children = _colView.getChildrenForClass(ScrollView.class);
        return ListUtils.map(children, child -> (BrowserCol<T>) child.getContent());
    }

    /**
     * Adds a column.
     */
    protected BrowserCol<T> addCol()
    {
        // Create new browser column and set index
        BrowserCol<T> browserCol = new BrowserCol<>(this);
        int colIndex = browserCol._index = getColCount();

        // Wrap in ScrollView
        ScrollView scrollView = new ScrollView(browserCol);
        scrollView.setBorder(null);
        scrollView.setGrowWidth(true);
        scrollView.setShowHBar(false);
        scrollView.setShowVBar(true);
        scrollView.setBarSize(14);

        // Add to ColBox
        _colView.addChild(scrollView);

        // If not root column, set items from last
        if (colIndex > 0) {
            BrowserCol<T> lastCol = getCol(colIndex - 1);
            T item = lastCol.getSelItem();
            List<T> items = getChildren(item);
            browserCol.setItems(items);
        }

        // Reset all cached widths
        for (BrowserCol<T> col : getCols())
            col.relayoutParent();

        // Return column
        return browserCol;
    }

    /**
     * Removes a column.
     */
    protected void removeCol(int anIndex)
    {
        _colView.removeChild(anIndex);
    }

    /**
     * Returns the currently selected column.
     */
    public BrowserCol<T> getSelCol()
    {
        int selColIndex = getSelColIndex();
        return selColIndex >= 0 ? getCol(selColIndex) : null;
    }

    /**
     * Returns the currently selected column.
     */
    public int getSelColIndex()  { return _selCol; }

    /**
     * Sets the selected column index.
     */
    protected void setSelColIndex(int anIndex)
    {
        // Set value
        _selCol = anIndex;

        // Iterate back from end to index and remove unused columns
        for (int i = getColCount() - 1; i > anIndex; i--)
            removeCol(i);

        // See if we need to add column
        T selItem = getSelItem();
        if (selItem != null && isParent(selItem))
            addCol();

        // Reset ColView.PrefWidth
        resetColViewWidth();

        // Scroll selection to visible
        scrollSelToVisible();
    }

    /**
     * Returns the selected index.
     */
    public int getSelIndex()
    {
        BrowserCol<T> selCol = getSelCol();
        return selCol != null ? selCol.getSelIndex() : -1;
    }

    /**
     * Sets the selected index.
     */
    public void setSelIndex(int anIndex)
    {
        BrowserCol<T> selCol = getSelCol();
        if (selCol != null)
            selCol.setSelIndex(anIndex);
    }

    /**
     * Returns the selected item.
     */
    public T getSelItem()
    {
        BrowserCol<T> selCol = getSelCol();
        return selCol != null ? selCol.getSelItem() : null;
    }

    /**
     * Sets the selected item.
     */
    public void setSelItem(T anItem)
    {
        // If null item, reset to first column
        if (anItem == null) {
            getCol(0).setSelIndex(-1);
            setSelColIndex(0);
            return;
        }

        // If already set, just return
        if (anItem.equals(getSelItem())) return;

        // If item in last column, select it
        BrowserCol<T> lastCol = getLastCol();
        if (lastCol.getItems().contains(anItem)) {
            lastCol.setSelItem(anItem);
            setSelColIndex(lastCol.getIndex());
            return;
        }

        // If column found with matching selected item, select column
        BrowserCol<T> newSelCol = getColWithSelItem(anItem);
        if (newSelCol != null) {
            setSelColIndex(newSelCol.getIndex());
            return;
        }

        // Otherwise, select item parent
        T itemParent = getParent(anItem);
        setSelItem(itemParent);

        // Select item and column
        lastCol = getLastCol();
        lastCol.setSelItem(anItem);
        setSelColIndex(lastCol.getIndex());
    }

    /**
     * Scrolls current selection to visible.
     */
    public void scrollSelToVisible()
    {
        // If ColView NeedsLayout, come back later
        if (_colView.isNeedsLayout() || ViewUtils.isMouseDown()) {
            _needsScrollSelToVisible = true;
            return;
        }

        // Reset NeedsScrollSelToVisible
        _needsScrollSelToVisible = false;

        // Scroll last column to visible
        ScrollView lastColScroll = getLastCol().getParent(ScrollView.class);
        scrollToVisible(lastColScroll.getBounds());
    }

    /**
     * Returns the column that has selected item.
     */
    protected BrowserCol<T> getColWithSelItem(T anItem)
    {
        // Iterate over columns and return last with matching SelItem
        for (int i = getColCount() - 1; i >= 0; i--) {
            BrowserCol<T> browserCol = getCol(i);
            if (browserCol.getSelItem() == anItem)
                return browserCol;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the path constructed by appending the selected item in each column by a separator string.
     */
    public String getSelPathForSeparator(String aSeparator)
    {
        // Create string buffer for path
        StringBuilder selPath = new StringBuilder();

        // Iterate over browser columns to add selected row items
        for (int i = 0, iMax = getColCount(); i < iMax; i++) {
            BrowserCol<T> browserCol = getCol(i);
            T colSelItem = browserCol.getSelItem();
            if (colSelItem == null)
                break;
            if (i > 0)
                selPath.append(aSeparator);
            selPath.append(getText(colSelItem));
        }

        // Return path string
        return selPath.toString();
    }

    /**
     * Process events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle KeyPress
        if (anEvent.isKeyPress()) {

            // Get selected column (just return if none)
            BrowserCol<T> selCol = getSelCol();
            if (selCol == null)
                return;

            // Handle keys
            switch (anEvent.getKeyCode()) {
                case KeyCode.UP -> { selCol.selectUp(); anEvent.consume(); }
                case KeyCode.DOWN -> { selCol.selectDown(); anEvent.consume(); }
                case KeyCode.ENTER -> selCol.processEnterAction(anEvent);
            }
        }
    }

    /**
     * PreferredSize.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        double prefW = getPrefColCount() * getPrefColWidth();
        return prefW + ins.getWidth();
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double prefH = 0;
        for (BrowserCol<T> col : getCols())
            prefH = Math.max(prefH, col.getBestHeight(-1));
        return prefH + ins.getHeight();
    }

    /**
     * Override to layout ScrollView.
     */
    protected void layoutImpl()
    {
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        _scrollView.setBounds(areaX, areaY, areaW, areaH);

        // Handle NeedsScrollSelToVisible
        if (_needsScrollSelToVisible)
            scrollSelToVisible();
    }

    /**
     * Called when Column count or width changes to reset ColView.PrefWidth.
     */
    protected void resetColViewWidth()
    {
        // Get best column width for current browser width
        Insets ins = _scrollView.getInsetsAll();
        double areaW = getWidth() - ins.getWidth();
        double prefColWidth = getPrefColWidth();
        int availableColCount = (int) Math.floor(areaW / prefColWidth);
        int bestColCount = Math.min(Math.max(availableColCount, 1), MAX_VISIBLE_COL_COUNT);
        int bestColWidth = (int) Math.floor(areaW / bestColCount);

        // Calculate ColView width and set
        int colCount = getColCount();
        double colViewWidth = Math.max(bestColWidth * colCount, areaW);
        _colView.setPrefWidth(colViewWidth);

        //
        Scroller scroller = _scrollView.getScroller();
        double scrollRatioX = scroller.getScrollRatioX();
        if (scrollRatioX > 0 || scrollRatioX < 1)
            getEnv().runLater(() -> scroller.setScrollRatioX(1));
    }

    /**
     * Override to update ColView.PrefWidth.
     */
    @Override
    public void setWidth(double aValue)
    {
        // Do normal version
        if (aValue == getWidth()) return;
        super.setWidth(aValue);

        // Update ColView.PrefWidth
        resetColViewWidth();
    }

    /**
     * Called to configure browser cell.
     */
    protected void configureBrowserCell(BrowserCol<T> aCol, ListCell<T> aCell)
    {
        // Set real text for item, image for item, and make text grow width
        T item = aCell.getItem();
        if (item == null)
            return;
        String name = getText(item);
        aCell.setText(name);
        Image image = getImage(item);
        if (image != null)
            aCell.setImage(image);

        // If parent, add branch icon
        if (isParent(item)) {
            Image branchImage = getBranchImage(item);
            if (branchImage == null)
                branchImage = getBranchImage();
            if (branchImage != null) {
                aCell.setImageAfter(branchImage);
                aCell.getGraphicAfter().setLeanX(HPos.RIGHT);
            }
        }

        // If cell configure, call that
        Consumer<ListCell<T>> cellConf = getCellConfigure();
        if (cellConf != null)
            cellConf.accept(aCell);
    }

    /**
     * Returns the icon to indicate branch nodes in a browser (right arrow by default).
     */
    public Image getBranchImage()
    {
        // If already set, just return
        if (_branchImage != null) return _branchImage;

        // Create image
        Image branchImage = Image.getImageForSize(9, 11, true);
        Polygon poly = new Polygon(1.5, 1.5, 7.5, 5.5, 1.5, 9.5);
        Painter pntr = branchImage.getPainter();
        pntr.setColor(Color.BLACK);
        pntr.fill(poly);
        pntr.flush();

        // Set, return
        return _branchImage = branchImage;
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // PrefColCount, PrefColWidth
        aPropSet.addPropNamed(PrefColCount_Prop, int.class, DEFAULT_PREF_COL_COUNT);
        aPropSet.addPropNamed(PrefColWidth_Prop, int.class, DEFAULT_PREF_COL_WIDTH);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String propName)
    {
        return switch (propName) {

            // PrefColCount, PrefColWidth
            case PrefColCount_Prop -> getPrefColCount();
            case PrefColWidth_Prop -> getPrefColWidth();

            // Do normal version
            default -> super.getPropValue(propName);
        };
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String propName, Object aValue)
    {
        switch (propName) {

            // PrefColCount, PrefColWidth
            case PrefColCount_Prop -> setPrefColCount(Convert.intValue(aValue));
            case PrefColWidth_Prop -> setPrefColWidth(Convert.intValue(aValue));

            // Do normal version
            default -> super.setPropValue(propName, aValue);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive PrefColCount, PrefColWidth
        if (getPrefColCount() != 2) e.add(PrefColCount_Prop, getPrefColCount());
        if (getPrefColWidth() != 150) e.add(PrefColWidth_Prop, getPrefColWidth());

        // Archive
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Archive PrefColCount, PrefColWidth
        if (anElement.hasAttribute(PrefColCount_Prop))
            setPrefColCount(anElement.getAttributeIntValue(PrefColCount_Prop));
        if (anElement.hasAttribute(PrefColWidth_Prop))
            setPrefColWidth(anElement.getAttributeIntValue(PrefColWidth_Prop));
    }

    // Convenience resolver methods
    private boolean isParent(T anItem)  { return _resolver.isParent(anItem); }
    private T getParent(T anItem)  { return _resolver.getParent(anItem); }
    private List<T> getChildren(T aParent)  { return _resolver.getChildren(aParent); }
    private String getText(T anItem)  { return _resolver.getText(anItem); }
    private Image getImage(T anItem)  { return _resolver.getImage(anItem); }
    private Image getBranchImage(T anItem)  { return _resolver.getBranchImage(anItem); }
}