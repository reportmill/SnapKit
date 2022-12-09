/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;
import snap.geom.Insets;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.Prefs;
import snap.util.StringUtils;
import snap.view.*;

/**
 * A View that handle a whole grid of color swatches, including drag and drop support.
 */
public class ColorDock extends View {

    // A list of colors to show in dock
    private List<Color>  _colors = new ArrayList<>();
    
    // Whether dock colors are saved to prefs
    private boolean  _persist;
    
    // The selected swatch
    private Swatch  _selSwatch;
    
    // The hidden ColorWell to talk to ColorPanel
    private ColorWell  _colorWell;
    
    // The drag point (swatch) in color dock
    private Point  _dragPoint = null;
    
    // Indicates that this well is the current drag source
    private boolean  _dragging;
    
    // The size of the individual swatches
    private static int  SWATCH_SIZE = 13;
    
    // The border for color dock
    private static final Border  COLOR_DOCK_BORDER = Border.createLoweredBevelBorder();
    
    /**
     * Creates a new color dock.
     */
    public ColorDock()
    {
        // Configure this view
        setActionable(true);
        enableEvents(MousePress, MouseRelease);
        enableEvents(ViewEvent.Type.DragGesture, ViewEvent.Type.DragSourceEnd); enableEvents(DragEvents);
        setBorder(COLOR_DOCK_BORDER);

        // Create ColorWell
        _colorWell = new ColorWell();
        _colorWell.addEventHandler(e -> colorWellDidFireAction(e), Action);
    }

    /**
     * Returns the color of the selected swatch.
     */
    public Color getColor()
    {
        Swatch swatch = getSelSwatch();
        return swatch != null? swatch.getColor() : Color.WHITE;
    }

    /**
     * Sets the color of the selected swatch.
     */
    public void setColor(Color aColor)
    {
        Swatch swatch = getSelSwatch();
        if (swatch == null)
            return;
        swatch.setColor(aColor);
    }

    /**
     * Returns the color at the given swatch index.
     */
    public Color getColor(int anIndex)
    {
        // If index beyond range, return white
        if (anIndex >= _colors.size())
            return Color.WHITE;

        // Return color at given index
        return _colors.get(anIndex);
    }

    /**
     * Sets the color at the given swatch index.
     */
    public void setColor(Color aColor, int anIndex)
    {
        // If beyond 1000, just bail
        if (anIndex > 1000) return;

        // Fill list to index
        while(anIndex >= _colors.size())
            _colors.add(Color.WHITE);

        // Set value at index and repaint
        _colors.set(anIndex, aColor != null ? aColor : Color.WHITE);
        repaint();

        // If Persist, save
        if (_persist)
            saveToPrefs(getName(), anIndex);
    }

    /**
     * Sets the colors.
     */
    public void setColors(List<? extends Color> theColors)
    {
        resetColors();
        for (int i = 0, iMax = theColors.size(); i < iMax; i++)
            setColor(theColors.get(i), i);
    }

    /**
     * Returns the color at given row & column.
     */
    public Color getColor(int aRow, int aCol)
    {
        int colCount = getColCount();
        int index = aRow * colCount + aCol;
        return getColor(index);
    }

    /**
     * Sets the color at the given row & column.
     */
    public void setColor(int aRow, int aCol, Color aColor)
    {
        // Get index and set color (the whole row/col thing was (is) so bogus)
        int colCount = getColCount();
        if (colCount == 0)
            colCount = 20;
        int index = aRow*colCount + aCol;
        setColor(aColor, index);
    }

    /**
     * Returns whether this doc writes itself out to preferences.
     */
    public boolean isPersistent()  { return _persist; }

    /**
     * Sets whether this dock writes itself out to preferences.
     */
    public void setPersistent(boolean aFlag)
    {
        _persist = aFlag;
        if (_persist)
            readFromPrefs(getName());
    }

    /**
     * Returns the swatch at given index.
     */
    public Swatch getSwatch(int anIndex)
    {
        int row = anIndex / getColCount();
        int col = anIndex % getColCount();
        return getSwatch(row, col);
    }

    /**
     * Returns the swatch at given row+col.
     */
    public Swatch getSwatch(int aRow, int aCol)  { return new Swatch(aRow,aCol); }

    /**
     * Returns the swatch at given point.
     */
    public Swatch getSwatchAt(Point aPoint)  { return getSwatchAt(aPoint.x, aPoint.y); }

    /**
     * Returns the swatch at given point.
     */
    public Swatch getSwatchAt(double aX, double aY)
    {
        Insets ins = getInsetsAll();
        int row = (int) ((aY - ins.top) / SWATCH_SIZE);
        int col = (int) ((aX - ins.left) / SWATCH_SIZE);
        return getSwatch(row,col);
    }

    /**
     * Returns the number of rows in this color dock.
     */
    public int getRowCount()
    {
        int height = (int)Math.round(getHeight() - getInsetsAll().getHeight());
        return height / SWATCH_SIZE + (height % SWATCH_SIZE != 0 ? 1 : 0);
    }

    /**
     * Returns the number of columns in this color dock.
     */
    public int getColCount()
    {
        int width = (int)Math.round(getWidth() - getInsetsAll().getWidth());
        return width / SWATCH_SIZE;
    }

    /**
     * Returns the total number of visible swatches.
     */
    public int getSwatchCount()  { return getRowCount() * getColCount(); }

    /**
     * Returns the selected swatch.
     */
    public Swatch getSelSwatch()  { return _selSwatch; }

    /**
     * Sets the selected swatch.
     */
    public void setSelSwatch(Swatch aSwatch)
    {
        if (Objects.equals(aSwatch, _selSwatch)) return;
        _selSwatch = aSwatch;
        repaint();
    }

    /**
     * Returns the selected swatch index.
     */
    public int getSelIndex()  { return _selSwatch != null? _selSwatch.getIndex() : -1; }

    /**
     * Resets the colors in colordock to white.
     */
    public void resetColors()  { _colors.clear(); }

    /**
     * Returns whether color dock is selected.
     */
    public boolean isSelected()  { return _colorWell.isSelected(); }

    /**
     * Sets whether color dock is selected.
     */
    public void setSelected(boolean aValue)
    {
        _colorWell.setColor(getColor());
        _colorWell.setSelected(aValue);
    }

    /**
     * Returns whether or not the dock can be selected.
     */
    public boolean isSelectable()  { return _colorWell.isSelectable(); }

    /**
     * Sets whether or not the dock can be selected.
     */
    public void setSelectable(boolean aValue)  { _colorWell.setSelectable(aValue); }

    /**
     * Paints this color dock component.
     */
    protected void paintFront(Painter aPntr)
    {
        // Get bounds and insets
        Rect bounds = getBounds();
        Insets ins = getInsetsAll();

        // Get content size
        double width = bounds.width - ins.getWidth();
        double height = bounds.height - ins.getHeight();

        // Get swatch size
        int swatchW = SWATCH_SIZE;
        int swatchH = SWATCH_SIZE;

        // Fill background to white and clip
        aPntr.setColor(Color.WHITE); aPntr.fillRect(ins.left, ins.top, width, height);
        aPntr.clipRect(ins.left, ins.top, width, height);

        // Make as many rows & columns as will fit, and fill any that are present in the sparse array.
        int nrows = getRowCount();
        int ncols = getColCount();
        for (int row = 0; row < nrows; ++row) {
            for (int col = 0; col < ncols; ++col) {
                Color color = getColor(row, col);
                ColorWell.paintSwatch(aPntr, color, ins.left+col*swatchW+2, ins.top+row*swatchH+2, swatchW-3, swatchH-3);
            }
        }

        // Draw grid between swatches
        aPntr.setColor(Color.LIGHTGRAY);
        for (int row = 0; row <= nrows; row++)
            aPntr.drawLine(ins.left, ins.top+row*swatchH, ins.left+width,ins.top+row*swatchH);
        for (int col = 0; col <= ncols; col++)
            aPntr.drawLine(ins.left+col*swatchW, ins.top,ins.left+col*swatchW,ins.top+height);

        // If selected swatch or drag point, highlight
        if (getSelSwatch() != null || _dragPoint != null) {
            Swatch swatch = _dragPoint != null ? getSwatchAt(_dragPoint) : getSelSwatch();
            int row = swatch.getRow();
            int col = swatch.getCol();
            double x = ins.left + col * swatchW;
            double y = ins.top + row * swatchH;

            // Draw red rect and redraw smaller swatch
            aPntr.setColor(Color.RED);
            aPntr.drawRect(x, y, swatchW, swatchH);
            aPntr.drawRect(x + 1,y + 1,swatchW - 2,swatchH - 2);
            aPntr.setColor(Color.WHITE);
            aPntr.drawRect(x + 2,y + 2,swatchW - 4,swatchH - 4);
        }
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MousePress: Select swatch at event point
        if (anEvent.isMousePress()) {
            if (!isEnabled()) return;
            Swatch swatch = getSwatchAt(anEvent.getPoint());
            setSelSwatch(swatch);
        }

        // Handle MouseRelease: Select this ColorDock or fireActionEvent
        else if (anEvent.isMouseRelease()) {
            if (isSelectable())
                setSelected(true);
            else fireActionEvent(anEvent);
        }

        // Handle DragEnter, DragOver
        else if (anEvent.isDragEnter() || anEvent.isDragOver()) {
            Clipboard dboard = anEvent.getClipboard();
            if (!_dragging && dboard.hasColor()) {
                anEvent.acceptDrag();
                _dragPoint = anEvent.getPoint(); repaint();
            }
        }

        // Handle DragExit
        else if (anEvent.isDragExit()) {
            _dragPoint = null; repaint(); }

        // Handle DragDrop
        else if (anEvent.isDragDrop())
            colorDropped(anEvent);

        // Handle DragGesture
        else if (anEvent.isDragGesture())
            startColorDrag(anEvent);

        // Handle DragSourceEnd
        else if (anEvent.isDragSourceEnd())
            _dragging = false;
    }

    /**
     * Called when color is dropped on dock.
     */
    protected void colorDropped(ViewEvent anEvent)
    {
        // Get color from clipboard
        Clipboard clipboard = anEvent.getClipboard();
        Color color = clipboard.getColor();

        // Get swatch at point, set color and select swatch
        Swatch swatch = getSwatchAt(anEvent.getPoint());
        swatch.setColor(color);
        setSelSwatch(swatch);

        // Finish drop, repaint, clear point
        anEvent.dropComplete();
        repaint(); _dragPoint = null;
    }

    /**
     * Called when color is dropped on dock.
     */
    protected void startColorDrag(ViewEvent anEvent)
    {
        // Get color and create image
        Color color = getColor();
        Image image = Image.get(14,14,true);
        Painter pntr = image.getPainter();
        ColorWell.paintSwatch(pntr, color,0,0,14,14);
        pntr.setColor(Color.BLACK);
        pntr.drawRect(0,0,14-1,14-1);
        pntr.flush();

        // Get clipboard and add color+image
        Clipboard clipboard = anEvent.getClipboard();
        clipboard.addData(color);
        clipboard.setDragImage(image);

        // Start drag
        clipboard.startDrag(); _dragging = true;
    }

    /**
     * Called when color well fires action event.
     */
    protected void colorWellDidFireAction(ViewEvent anEvent)
    {
        // Get color from hidden ColorWell and set in current swatch
        Color color = _colorWell.getColor();
        Swatch swatch = getSelSwatch();
        if (swatch == null)
            return;
        swatch.setColor(color);

        // Fire action event
        ViewEvent inputEvent = anEvent.getParentEvent();
        fireActionEvent(inputEvent);
    }

    /**
     * Update an individual color at {row,column} in the preferences
     */
    protected void saveToPrefs(String aName, int anIndex)
    {
        // Get the app's preferences node and sub-node for the list of colors
        Prefs prefs = Prefs.getDefaultPrefs().getChild(aName);

        // Get color, rgb value, key, then if not white put value, otherwise remove
        Color color = getColor(anIndex); // Legacy: c = getColor(aRow, aCol);
        int rgb = color.getRGBA();

        // Get key, if not white put value, otherwise remove
        String key = String.valueOf(anIndex); // Legacy: key = aRow + "," + aCol;
        if (rgb != 0xFFFFFFFF)
            prefs.setValue(key, rgb);
        else prefs.remove(key);
    }

    /**
     * Read color well color from preferences.
     */
    protected void readFromPrefs(String aName)
    {
        // Reset colors map
        resetColors();

        // Get named node and node keys and iterate over keys
        Prefs prefs = Prefs.getDefaultPrefs().getChild(aName);
        String[] keys = prefs.getKeys();
        for (String key : keys) {

            // Get color for key
            int rgba = prefs.getInt(key, 0xFFFFFFFF);
            Color color = new Color(rgba);

            // Legacy: If key contains separator, add by row/col
            if (key.indexOf(',')>0) {
                String[] rcStr = key.split(",");
                if (rcStr.length < 2) continue;
                int row = StringUtils.intValue(rcStr[0]);
                int col = StringUtils.intValue(rcStr[1]);
                setColor(row, col, color);
            }

            // Otherwise get index
            else {
                int ind = StringUtils.intValue(key);
                setColor(color, ind);
            }
        }
    }

    /**
     * Override to set Selected to false when hidden.
     */
    @Override
    protected void setShowing(boolean aValue)
    {
        // Do normal version
        if (aValue == isShowing()) return;
        super.setShowing(aValue);

        // Reset selected and swatch
        setSelected(false);
        setSelSwatch(null);
    }

    /**
     * Override to return dock border.
     */
    public Border getDefaultBorder()  { return COLOR_DOCK_BORDER; }

    /**
     * A class to represent a color swatch.
     */
    protected class Swatch {

        // The row, column
        protected int  _row, _col;

        // The color
        protected Color  _color;

        /** Creates swatch for row, col. */
        public Swatch(int aRow, int aCol)  { _row = aRow; _col = aCol; }

        /** Returns the row. */
        public int getRow()  { return _row; }

        /** Returns the column. */
        public int getCol()  { return _col; }

        /** Returns the index of swatch in dock. */
        public int getIndex()  { return _row*getColCount() + _col; }

        /** Returns the color. */
        public Color getColor()
        {
            if (_color != null) return _color;
            return _color = ColorDock.this.getColor(_row,_col);
        }

        /** Sets the color. */
        public void setColor(Color aColor)
        {
            _color = aColor;
            ColorDock.this.setColor(_row, _col, aColor);
        }

        /** Standard equals. */
        public boolean equals(Object anObj)
        {
            if (anObj == this) return true;
            Swatch other = anObj instanceof Swatch? (Swatch)anObj : null;
            if (other == null) return false;
            return _row == other._row && _col == other._col;
        }
    }
}