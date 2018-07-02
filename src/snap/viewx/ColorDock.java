/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;
import snap.gfx.*;
import snap.util.Prefs;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * A ColorWell subclass that handle a whole grid of color swatches, including drag and drop support.
 */
public class ColorDock extends ColorWell {

    // A hashtable to map row,col coordinates to colors in the dock (which is a sparse array of unlimited size)
    Map <String,Color>      _colors = new Hashtable();
    
    // Whether dock colors are saved to prefs
    boolean                 _persist;
    
    // The selected swatch
    Swatch                  _selSwatch;
    
    // The drag point (swatch) in color dock
    Point                   _dragPoint = null;
    
    // The size of the individual swatches
    static int              SWATCH_SIZE = 13;
    
    // The border for color dock
    static final Border     COLOR_DOCK_BORDER = Border.createLoweredBevelBorder();
    
/**
 * Creates a new color dock.
 */
public ColorDock()
{
    enableEvents(MousePress);
    setBorder(COLOR_DOCK_BORDER);
}

/**
 * Returns the color at given row & column.
 */
public Color getColor(int aRow, int aCol)
{
    Color color = _colors.get(aRow + "," + aCol);
    return color!=null? color : Color.WHITE;
}

/**
 * Sets the color at the given row & column.
 */
public void setColor(Color aColor, int aRow, int aCol)
{
    String key = aRow + "," + aCol;                // Get key
    if(aColor!=null) _colors.put(key, aColor);     // If color isn't null, add to map
    else _colors.remove(key);                      // If color is null, remove map key
    if(_persist) saveToPrefs(getName(), aRow, aCol);
}

/**
 * Returns whether this doc writes itself out to preferences.
 */
public boolean isPersistent()  { return _persist; }

/**
 * Sets whether this dock writes itself out to preferences.
 */
public void setPersistent(boolean aFlag)  { _persist = aFlag; if(_persist) readFromPrefs(getName()); }

/**
 * Returns the swatch at given index.
 */
public Swatch getSwatch(int anIndex)
{
    int row = anIndex/getColCount(), col = anIndex%getColCount();
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
    int row = (int)((aY - ins.top)/SWATCH_SIZE);
    int col = (int)((aX - ins.left)/SWATCH_SIZE);
    return getSwatch(row,col);
}

/**
 * Returns the color at the given swatch index.
 */
public Color getColor(int anIndex)  { return getSwatch(anIndex).getColor(); }

/**
 * Sets the color at the given swatch index.
 */
public void setColor(Color aColor, int anIndex)  { getSwatch(anIndex).setColor(aColor); }

/**
 * Returns the number of rows in this color dock.
 */
public int getRowCount()
{
    int height = (int)Math.round(getHeight() - getInsetsAll().getHeight());
    return height/SWATCH_SIZE + (height%SWATCH_SIZE !=0 ? 1 : 0);    
}

/**
 * Returns the number of columns in this color dock.
 */
public int getColCount()
{
    int width = (int)Math.round(getWidth() - getInsetsAll().getWidth());
    return width/SWATCH_SIZE;// + (width%swatchW != 0 ? 1 : 0);
}

/**
 * Returns the total number of visible swatches.
 */
public int getSwatchCount()  { return getRowCount()*getColCount(); }

/**
 * Returns the selected swatch.
 */
public Swatch getSelSwatch()  { return _selSwatch; }

/**
 * Sets the selected swatch.
 */
public void setSelSwatch(Swatch aSwatch)
{
    if(SnapUtils.equals(aSwatch,_selSwatch)) return;
    _selSwatch = aSwatch;
    setColor(_selSwatch!=null? _selSwatch.getColor() : null);
    repaint();
}

/**
 * Returns the selected swatch index.
 */
public int getSelIndex()  { return _selSwatch!=null? _selSwatch.getIndex() : -1; }

/**
 * Resets the colors in colordock to white.
 */
public void resetColors()  { _colors.clear(); }

/**
 * Override to set color of selected swatch.
 */
public void setColor(Color aColor)
{
    // If there is selected swatch, set color
    if(getSelSwatch()!=null) getSelSwatch().setColor(aColor);
    
    // Do normal version
    super.setColor(aColor);
}

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
    int swatchW = SWATCH_SIZE, swatchH = SWATCH_SIZE;
    
    // Fill background to white and clip
    aPntr.setColor(Color.WHITE); aPntr.fillRect(ins.left, ins.top, width, height);
    aPntr.clipRect(ins.left, ins.top, width, height);
    
    // Make as many rows & columns as will fit, and fill any that are present in the sparse array.
    int nrows = getRowCount(), ncols = getColCount();
    for(int row=0; row<nrows; ++row) {
        for(int col=0; col<ncols; ++col) {
            Color color = getColor(row, col);
            ColorWell.paintSwatch(aPntr, color, ins.left+col*swatchW+2, ins.top+row*swatchH+2, swatchW-3, swatchH-3);
        }
    }
    
    // Draw grid between swatches
    aPntr.setColor(Color.LIGHTGRAY);
    for(int row=0; row<=nrows; row++) aPntr.drawLine(ins.left, ins.top+row*swatchH, ins.left+width,ins.top+row*swatchH);
    for(int col=0; col<=ncols; col++) aPntr.drawLine(ins.left+col*swatchW, ins.top,ins.left+col*swatchW,ins.top+height);
    
    // If selected swatch or drag point, highlight
    if(getSelSwatch()!=null || _dragPoint!=null) {
        Swatch swatch = _dragPoint!=null? getSwatchAt(_dragPoint) : getSelSwatch();
        int row = swatch.getRow(), col = swatch.getCol();
        double x = ins.left + col*swatchW, y = ins.top + row*swatchH;
        
        // Draw red rect and redraw smaller swatch
        aPntr.setColor(Color.RED); aPntr.drawRect(x,y,swatchW,swatchH); aPntr.drawRect(x+1,y+1,swatchW-2,swatchH-2);
        aPntr.setColor(Color.WHITE); aPntr.drawRect(x+2,y+2,swatchW-4,swatchH-4);
    }
}

/**
 * Handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MousePress: Select swatch under at point
    if(anEvent.isMousePress()) { if(!isEnabled()) return;
        Swatch swatch = getSwatchAt(anEvent.getPoint());
        setSelSwatch(swatch);
    }
    
    // Handle MouseRelease: Set selection or just show color panel
    else if(anEvent.isMouseRelease()) {
        if(isSelectable()) setSelected(true);
        else showColorPanel();
    }
    
    // Handle DragEnter, DragOver
    else if(anEvent.isDragEnter() || anEvent.isDragOver()) { Clipboard dboard = anEvent.getClipboard();
        if(!_dragging && dboard.hasColor()) {
            anEvent.acceptDrag(); _dragPoint = anEvent.getPoint(); repaint(); }
    }
    
    // Handle DragExit
    else if(anEvent.isDragExit()) {
        _dragPoint = null; repaint(); }
    
    // Handle DragDrop
    else if(anEvent.isDragDrop()) { Clipboard dboard = anEvent.getClipboard();
        Color color = dboard.getColor();
        Swatch swatch = getSwatchAt(anEvent.getPoint()); swatch.setColor(color);
        anEvent.dropComplete(); _dragPoint = null; repaint();
    }
    
    // Otherwise, do normal version
    else super.processEvent(anEvent);
}

/** 
 * Update an individual color at {row,column} in the preferences
 */
protected void saveToPrefs(String aName, int aRow, int aCol) 
{
    // Get the app's preferences node and sub-node for the list of colors
    Prefs prefs = Prefs.get().getChild(aName);
    
    // Get color, rgb value, key, then if not white put value, otherwise remove
    Color c = getColor(aRow, aCol);
    int rgb = c.getRGBA();
    String key = aRow + "," + aCol;
    if(rgb!=0xFFFFFFFF) prefs.set(key, rgb);
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
    Prefs prefs = Prefs.get().getChild(aName);
    String keys[] = prefs.getKeys();
    for(String key : keys) {
        int rgba = prefs.getInt(key,0xFFFFFFFF);  // Get color rgb for current loop key
        Color color = new Color(rgba);             // Get color from rgb
        _colors.put(key, color);                    // Add color to map
    }
}

/**
 * Override to return dock border.
 */
public Border getDefaultBorder()  { return COLOR_DOCK_BORDER; }

/**
 * A class to represent a color swatch.
 */
private class Swatch {
    
    // The row, column
    int          _row, _col;
    
    // The color
    Color        _color;
    
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
        if(_color==null) _color = ColorDock.this.getColor(_row,_col);
        return _color;
    }
    
    /** Sets the color. */
    public void setColor(Color aColor)
    {
        _color = aColor;
        ColorDock.this.setColor(aColor, _row, _col);
    }
    
    /** Standard equals. */
    public boolean equals(Object anObj)
    {
        if(anObj==this) return true;
        Swatch other = anObj instanceof Swatch? (Swatch)anObj : null; if(other==null) return false;
        return _row==other._row && _col==other._col;
    }
}

}