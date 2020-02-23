/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;

import snap.geom.Path;
import snap.geom.Point;
import snap.gfx.*;

/**
 * A Pen for SnapActor.
 */
class SnapPen {

    // Whether pen is down
    boolean         _penDown;
    
    // The current pen color
    Color           _color = Color.BLUE;
    
    // The current pen width
    double          _width = 5;

    // The current path
    Path _path;
    
    // The group of paths
    List <PenPath>  _paths = Collections.EMPTY_LIST;
    
    // The current coords
    double          _x, _y;

/**
 * Returns pen down.
 */
public boolean isPenDown()  { return _penDown; }

/**
 * Sets pen down.
 */
public void setPenDown(boolean aValue)  { _penDown = aValue; if(!aValue) _path = null; }

/**
 * Sets the pen down at given location.
 */
public void penDown(Point aPnt)  { setPenDown(true); _x = aPnt.x; _y = aPnt.y; }

/**
 * Clears the pen.
 */
public void clear()  { _paths.clear(); _path = null; }

/**
 * Sets the stroke color.
 */
public void setColor(String aString)  { _color = Color.get(aString); _path = null; }

/**
 * Sets the stroke width.
 */
public void setWidth(double aValue)  { _width = aValue; _path = null; }

/**
 * Does a move to.
 */
public void lineTo(Point aPnt)
{
    // If pen not down, just return
    if(!_penDown) return;
    
    // Get path and add line
    if(_path==null) _path = createPath();
    _path.lineTo(_x = aPnt.x, _y = aPnt.y);
}

/**
 * Creates a new path.
 */
Path createPath()
{
    PenPath path = new PenPath(_color, _width); path.moveTo(_x, _y);
    if(_paths==Collections.EMPTY_LIST) _paths = new ArrayList();
    _paths.add(path);
    return path;
}

/**
 * A path subclass to hold color and width.
 */
protected class PenPath extends Path {
    Color _color; double _width;
    public PenPath(Color aColor, double aWidth)  { _color = aColor; _width = aWidth; }
    public Color getColor()  { return _color; }
    public double getWidth()  { return _width; }
}

}