package snap.view;
import snap.geom.Insets;

/**
 * A ViewHost to arrange child views in a grid.
 */
public class GridView extends ChildView {

    // Whether grid is uniform cell size
    private boolean  _uniform = true;

    // The spacing between cells horizontal
    private double  _spacingX = 0;

    // The spacing between cells vertical
    private double  _spacingY = 0;

    // The preferred cell width
    private double  _cellPrefWidth = -1;

    // The preferred cell height
    private double  _cellPrefHeight = -1;

    /**
     * Constructor.
     */
    public GridView()
    {
        super();
    }

    /**
     * Returns whether grid is uniform size.
     */
    public boolean isUniform()  { return _uniform; }

    /**
     * Returns the spacing between cells horizontal.
     */
    public double getSpacingX()  { return _spacingX; }

    /**
     * Returns the spacing between cells vertical.
     */
    public double getSpacingY()  { return _spacingY; }

    /**
     * Returns the preferred cell width.
     */
    private double getCellPrefWidth()
    {
        if (_cellPrefWidth > 0) return _cellPrefWidth;

        if (isUniform()) {
            View cell = getChildren().getFirst();
            return cell != null ? cell.getPrefWidth() : 0;
        }

        // Otherwise, just max pref width
        double maxPrefW = 0;
        for (View child : getChildrenManaged())
            maxPrefW = Math.max(maxPrefW, child.getPrefWidth());
        return maxPrefW;
    }

    /**
     * Returns the preferred cell height.
     */
    private double getCellPrefHeight()
    {
        if (_cellPrefHeight > 0) return _cellPrefHeight;

        if (isUniform()) {
            View cell = getChildren().getFirst();
            return cell != null ? cell.getPrefHeight() : 0;
        }

        // Otherwise, just max pref height of cells
        double maxPrefH = 0;
        for (View child : getChildrenManaged())
            maxPrefH = Math.max(maxPrefH, child.getPrefWidth());
        return maxPrefH;
    }

    /**
     * Returns the number of columns that can be show for current width and children.
     */
    public int getColCountVisible()
    {
        Insets ins = getInsetsAll();
        double areaW = getWidth() - ins.getWidth();
        return getColCountForWidth(areaW);
    }

    /**
     * Returns the number of columns that can be show for given width.
     */
    public int getColCountForWidth(double aWidth)
    {
        // Get width of one cell
        double cellW = getCellPrefWidth(); if ( cellW <= 0) return -1;
        return (int) Math.floor(aWidth / cellW);
    }

    /**
     * Returns the number of rows for given column count.
     */
    public int getRowCountForCols(int aColCount)
    {
        if (aColCount == 0) return 0;
        int cellCount = getChildCountManaged();
        return (int) Math.ceil(cellCount / (double) aColCount);
    }

    /**
     * Override to use grid layout.
     */
    @Override
    protected double computePrefWidth(double aH)
    {
        // Get number of columns and single cell width
        int colCount = getChildCountManaged();
        double cellWidth = getCellPrefWidth();

        // Gets width of cells, spacing and insets
        double cellsWidth = cellWidth*colCount + getSpacingX()*(colCount-1);
        Insets ins = getInsetsAll();
        return cellsWidth + ins.getWidth();
    }

    /**
     * Override to use grid layout.
     */
    @Override
    protected double computePrefHeight(double aW)
    {
        // Get number of rows and single cell height
        int colCount = aW >= 0 ? getColCountForWidth(aW) : Integer.MAX_VALUE;
        int rowCount = getRowCountForCols(colCount);
        double cellHeight = getCellPrefHeight();

        // Gets height of cells, spacing and insets
        double cellsHeight = cellHeight*rowCount + getSpacingY()*(rowCount-1);
        Insets ins = getInsetsAll();
        return cellsHeight + ins.getHeight();
    }

    /**
     * Override to use grid layout.
     */
    @Override
    protected void layoutImpl()
    {
        // If no children, just return
        if (getChildCountManaged() == 0) return;

        // Get children and number of cols
        View[] children = getChildrenManaged();
        int cellCount = children.length;
        int colCount = getColCountVisible();
        int rowCount = getRowCountForCols(colCount);

        // Loop vars
        Insets ins = getInsetsAll();
        double cellW = getCellPrefWidth();
        double cellH = getCellPrefHeight();
        double cellY = ins.top;

        // Iterate over children and place
        for (int i = 0; i < rowCount; i++) {
            double cellX = ins.left;
            for (int j = 0; j < colCount; j++) {
                int ind = i * colCount + j;
                if (ind >= cellCount) break;
                View child = children[ind];
                child.setBounds(cellX, cellY, cellW, cellH);
                cellX += cellW + getSpacingX();
            }
            cellY += cellH + getSpacingY();
        }
    }
}
