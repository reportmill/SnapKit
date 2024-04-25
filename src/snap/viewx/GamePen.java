/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.Path2D;
import snap.geom.Point;
import snap.gfx.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Pen for GameActor.
 */
class GamePen {

    // Whether pen is down
    private boolean _penDown;

    // The current pen color
    private Color _color = Color.BLUE;

    // The current pen width
    private double _width = 5;

    // The current path
    private Path2D _path;

    // The group of paths
    protected List<PenPath> _paths = Collections.EMPTY_LIST;

    // The current coords
    private double _x, _y;

    /**
     * Constructor.
     */
    public GamePen()
    {
        super();
    }

    /**
     * Returns pen down.
     */
    public boolean isPenDown()  { return _penDown; }

    /**
     * Sets pen down.
     */
    public void setPenDown(boolean aValue)
    {
        _penDown = aValue;
        if (!aValue) _path = null;
    }

    /**
     * Sets the pen down at given location.
     */
    public void penDown(Point aPnt)
    {
        setPenDown(true);
        _x = aPnt.x;
        _y = aPnt.y;
    }

    /**
     * Clears the pen.
     */
    public void clear()
    {
        _paths.clear();
        _path = null;
    }

    /**
     * Sets the stroke color.
     */
    public void setColor(String aString)
    {
        _color = Color.get(aString);
        _path = null;
    }

    /**
     * Sets the stroke width.
     */
    public void setWidth(double aValue)
    {
        _width = aValue;
        _path = null;
    }

    /**
     * Does a move to.
     */
    public void lineTo(Point aPnt)
    {
        // If pen not down, just return
        if (!_penDown) return;

        // Get path and add line
        if (_path == null)
            _path = createPath();
        _path.lineTo(_x = aPnt.x, _y = aPnt.y);
    }

    /**
     * Creates a new path.
     */
    protected Path2D createPath()
    {
        PenPath path = new PenPath(_color, _width);
        path.moveTo(_x, _y);
        if (_paths == Collections.EMPTY_LIST)
            _paths = new ArrayList<>();
        _paths.add(path);
        return path;
    }

    /**
     * A path subclass to hold color and width.
     */
    protected static class PenPath extends Path2D {
        Color _color;
        double _width;

        public PenPath(Color aColor, double aWidth)
        {
            _color = aColor;
            _width = aWidth;
        }

        public Color getColor()  { return _color; }

        public double getWidth()  { return _width; }
    }
}