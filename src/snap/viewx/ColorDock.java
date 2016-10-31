/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;
import snap.gfx.*;
import snap.util.Prefs;
import snap.view.*;

/**
 * A ColorWell subclass that handle a whole grid of color swatches, including drag and drop support.
 */
public class ColorDock extends View {

    // Whether changes to the dock cause the colors to be saved to the preferences database
    boolean                 _persistent;
    
    // A hashtable to map row,col coordinates to colors in the dock (which is a sparse array of unlimited size)
    Map <String,Color>  _colors = new Hashtable();
    
    // The drag point (swatch) in color dock
    Point                   _dragPoint = null;
    
    // Whether draggine
    boolean                 _dragging;

    // The size of the individual swatches
    static int              SWATCH_SIZE = 13;
    
    // The border for color dock
    static final Border     COLOR_DOCK_BORDER = Border.createLoweredBevelBorder();
    
/**
 * Creates a new color dock.
 */
public ColorDock()
{
    enableEvents(MouseRelease, DragEnter, DragOver, DragExit, DragDrop, DragGesture);
    setBorder(COLOR_DOCK_BORDER);
}

/**
 * Returns whether this doc writes itself out to preferences.
 */
public boolean isPersistent()  { return _persistent; }

/**
 * Sets whether this dock writes itself out to preferences.
 */
public void setPersistent(boolean aFlag)
{
    _persistent = aFlag;
    if(_persistent)
        readFromPreferences(getName());
}

/**
 * Returns the color at the given row & column.
 */
public Color getColor(int aRow, int aCol)
{
    Color color = _colors.get(aRow + "," + aCol);  // Get color from color map
    return color==null? Color.WHITE : color;       // Return color (or just white if null)
}

/**
 * Sets the color at the given row & column.
 */
public void setColor(Color aColor, int aRow, int aCol)
{
    String key = aRow + "," + aCol;                // Get key
    if(aColor!=null) _colors.put(key, aColor);     // If color isn't null, add to map
    else _colors.remove(key);                      // If color is null, remove map key
}

/**
 * Returns the color at the given swatch index.
 */
public Color getColor(int anIndex)
{
    int row = anIndex/getColumnCount();
    int col = anIndex%getColumnCount();
    return getColor(row, col);    
}

/**
 * Sets the color at the given swatch index.
 */
public void setColor(Color aColor, int anIndex)
{
    int row = anIndex/getColumnCount();
    int col = anIndex%getColumnCount();
    setColor(aColor, row, col);
}

/**
 * Returns the color at the mouse location within the component.
 */
public Color getColor(Point aPoint)
{
    int row = getRow(aPoint);
    int col = getColumn(aPoint);
    return getColor(row, col);
}

/**
 * Returns the color at the mouse location within the component.
 */
public void setColor(Color aColor, Point aPoint)
{
    int row = getRow(aPoint);
    int col = getColumn(aPoint);
    setColor(aColor, row, col);
}

/**
 * Resets the colors in colordock to white.
 */
public void resetColors()  { _colors.clear(); }

/**
 * Returns the row for the given y coordinate.
 */
public int getRow(Point aPoint)  { Insets ins = getInsetsAll(); return (int)((aPoint.getY() - ins.top)/SWATCH_SIZE); }

/**
 * Returns the column for the given x coordinate.
 */
public int getColumn(Point aPoint)  { Insets ins = getInsetsAll(); return (int)((aPoint.getX() - ins.left)/SWATCH_SIZE); }

/**
 * Returns the number of rows in this color dock.
 */
public int getRowCount()
{
    Insets ins = getInsetsAll(); int height = (int)Math.round(getHeight() - (ins.top+ins.bottom));
    return height/SWATCH_SIZE + (height%SWATCH_SIZE !=0 ? 1 : 0);    
}

/**
 * Returns the number of columns in this color dock.
 */
public int getColumnCount()
{
    Insets ins = getInsetsAll(); int width = (int)Math.round(getWidth() - (ins.left+ins.right));
    return width/SWATCH_SIZE;// + (width%swatchW != 0 ? 1 : 0);
}

/**
 * Returns the total number of visible swatches.
 */
public int getSwatchCount()  { return getRowCount()*getColumnCount(); }

/**
 * Returns the swatch index for given point.
 */
public int getSwatchIndex(Point aPoint)
{
    // If point is null, return -1
    if(aPoint==null) return -1;
    
    // Get row and column for selected point
    int row = getRow(aPoint);
    int col = getColumn(aPoint);
    
    // Return selected index
    return row*getColumnCount() + col;
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
    double width = bounds.width-(ins.left+ins.right);
    double height = bounds.height-(ins.top+ins.bottom);
    
    // Get swatch size
    int swatchW = SWATCH_SIZE, swatchH = SWATCH_SIZE;
    
    // Get row & column count
    int ncols = getColumnCount();
    int nrows = getRowCount();
    
    // Set color to white and fill background
    aPntr.setColor(Color.WHITE); aPntr.fillRect(ins.left, ins.top, width, height);
    
    // Make as many rows & columns as will fit, and fill any that are present in the sparse array.
    for(int row=0; row<nrows; ++row) {
        for(int col=0; col<ncols; ++col) {
            Color color = getColor(row, col);
            ColorWell.paintSwatch(aPntr, color, ins.left+col*swatchW+2, ins.top+row*swatchH+2, swatchW-3, swatchH-3);
        }
    }
    
    // Draw borders
    aPntr.setColor(Color.LIGHTGRAY);
    for(int row=0; row<=nrows; ++row)
        aPntr.drawLine(ins.left, ins.top+row*swatchH, ins.left+width, ins.top+row*swatchH);
    for(int col=0; col<=ncols; ++col)
        aPntr.drawLine(ins.left+col*swatchW, ins.top, ins.left+col*swatchW, ins.top+height);
    
    // If dragging, hilight the drag destination
    if(_dragPoint!=null) {
        int row = getRow(_dragPoint);           // Get select row & column
        int col = getColumn(_dragPoint);
        double x = ins.left + col*swatchW;      // Get select x y
        double y = ins.top + row*swatchH;       // Draw red rect
        aPntr.setColor(Color.RED); aPntr.drawRect(x,y,swatchW,swatchH); aPntr.drawRect(x+1,y+1,swatchW-2,swatchH-2);
        aPntr.setColor(Color.WHITE); aPntr.drawRect(x+2,y+2,swatchW-4,swatchH-4);
    }
}

/**
 * Handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MouseClick: Make ColorPanel display clicked color
    if(anEvent.isMouseClick()) {
        if(!isEnabled()) return;
        ColorPanel panel = ColorPanel.getShared();
        panel.setColor(getColor(anEvent.getPoint()));
        panel.resetLater();
    }
    
    // Handle DragEnter
    else if(anEvent.isDragEnter()) {
        Dragboard dboard = anEvent.getDragboard();
        if(dboard.hasColor()) {
            anEvent.acceptDrag();
            _dragPoint = anEvent.getPoint(); repaint();
        } //else dtde.rejectDrag();
    }
    
    // Handle DragOver
    else if(anEvent.isDragOver()) {
        _dragPoint = anEvent.getPoint(); repaint(); }
    
    // Handle DragExit
    else if(anEvent.isDragExit()) {
        _dragPoint = null; repaint(); }
    
    // Handle DragDrop
    else if(anEvent.isDragDrop()) {
        Dragboard dboard = anEvent.getDragboard();
        Color color = dboard.getColor();
        dropColor(color, anEvent.getPoint());
        anEvent.dropComplete(); _dragPoint = null; repaint();
    }
    
    // Handle DragGesture
    else if(anEvent.isDragGesture()) {
        Color color = getColor(anEvent.getPoint());
        Image image = Image.get(14,14,true); Painter pntr = image.getPainter();
        ColorWell.paintSwatch(pntr,color,0,0,14,14); pntr.setColor(Color.BLACK);
        pntr.drawRect(0,0,14-1,14-1); pntr.flush();
        Dragboard dboard = anEvent.getDragboard();
        dboard.setContent(color);
        dboard.setDragImage(image);
        dboard.startDrag();
        _dragging = true;
    }
    
    // Handle DragSourceEnd
    else if(anEvent.isDragSourceEnd())
        _dragging = false;
}

/**
 * DropTargetListener method.
 */
public void dropColor(Color aColor, Point aPoint)
{ 
    // Get row and column for last drag point
    int row = getRow(aPoint), col = getColumn(aPoint);
    
    // Set color - if color dock is persistent, save new color to preferences
    setColor(aColor, row, col); //setColor(aColor);
    if(_persistent) saveToPreferences(getName(), row, col);
}

/** 
 * Update an individual color at {row,column} in the preferences
 */
public void saveToPreferences(String aName, int aRow, int aColumn) 
{
    // Get the app's preferences node and sub-node for the list of colors
    Prefs prefs = Prefs.get().getChild(aName);
    
    // Get color, rgb value, key, then if not white put value, otherwise remove
    Color c = getColor(aRow, aColumn);
    int rgb = c.getRGBA();
    String key = aRow + "," + aColumn;
    if(rgb!=0xFFFFFFFF) prefs.set(key, rgb);
    else prefs.remove(key);
}

/**
 * Read color well color from preferences.
 */
public void readFromPreferences(String aName)
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
 * Returns the default border.
 */
public Border getDefaultBorder()  { return COLOR_DOCK_BORDER; }

}