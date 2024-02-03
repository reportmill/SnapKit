/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.Path2D;
import snap.geom.Point;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class supports convenient drawing methods for quick and convenient vector graphics drawing.
 */
public class QuickDrawPen {

    // The QuickDraw view that owns this pen
    protected QuickDraw _drawView;

    // Pen Color
    private Color  _color = Color.BLUE;

    // Pen width
    private double  _width = 3;

    // Angle
    private double  _direction = 0;

    // The path
    private PenPath  _penPath;

    // The list of paths
    private List<PenPath>  _penPaths = new ArrayList<>();

    // The path animator
    protected QuickDrawPenAnim _animPen;

    // Whether pen is animating
    private boolean  _animating;

    /**
     * Constructor.
     */
    protected QuickDrawPen(QuickDraw aDrawView)
    {
        _drawView = aDrawView;
    }

    /**
     * Returns the current pen color.
     */
    public Color getColor()  { return _color; }

    /**
     * Sets the current pen color.
     */
    public void setColor(Color aColor)
    {
        // If already set, just return
        if (Objects.equals(aColor, getColor())) return;

        // Set
        _color = aColor;

        // If PenPath has segment, clear it
        if (_penPath != null) {
            if (_penPath.getPointCount() <= 1)
                _penPath._color = aColor;
            else _penPath = null;
        }
    }

    /**
     * Returns the current pen width.
     */
    public double getWidth()  { return _width; }

    /**
     * Sets the current pen width.
     */
    public void setWidth(double aValue)
    {
        // If already set, just return
        if (aValue == getWidth()) return;

        // Set
        _width = aValue;

        // If PenPath has segment, clear it
        if (_penPath != null) {
            if (_penPath.getPointCount() <= 1)
                _penPath._width = aValue;
            else _penPath = null;
        }
    }

    /**
     * Returns the current angle in degrees of path drawing.
     *
     * Default is zero degrees - so calling forward() adds a horizontal line segment to the right.
     */
    public double getDirection()  { return _direction; }

    /**
     * Sets the current angle in degrees of path drawing.
     */
    public void setDirection(double theDegrees)
    {
        // If already set, just return
        if (theDegrees == getDirection()) return;

        // Set
        _direction = theDegrees;
    }

    /**
     * Moves the current path point to given point.
     */
    public void moveTo(double aX, double aY)
    {
        // Forward to pen and repaint
        PenPath penPath = getPenPath();
        penPath.moveTo(aX, aY);
        _drawView.repaint();
    }

    /**
     * Adds a line segment to current draw path from last path point to given point.
     */
    public void lineTo(double aX, double aY)
    {
        // Forward to pen and repaint
        PenPath penPath = getPenPath();
        penPath.lineTo(aX, aY);
        _drawView.repaint();
    }

    /**
     * Adds a close segment to current draw path.
     */
    public void closePath()
    {
        // Forward to pen and repaint
        PenPath penPath = getPenPath();
        penPath.close();
        _drawView.repaint();
    }

    /**
     * Moves the default draw path forward by given length for current Direction.
     */
    public void forward(double aLength)
    {
        // Get last point
        PenPath penPath = getPenPath();
        Point point = penPath.getLastPoint();
        if (point == null) {
            penPath.moveTo(0, 0);
            point = penPath.getLastPoint();
        }

        // Calculate next point using Direction and length
        double nextX = point.x + aLength * Math.cos(Math.toRadians(_direction));
        double nextY = point.y + aLength * Math.sin(Math.toRadians(_direction));
        lineTo(nextX, nextY);
    }

    /**
     * Sets the path drawing direction to the current direction plus given angle.
     */
    public void turn(double anAngle)
    {
        setDirection(_direction + anAngle);
    }

    /**
     * Returns the current pen path.
     */
    protected PenPath getPenPath()
    {
        // If already set, just return
        if (_penPath != null) return _penPath;

        // If no current path, create/add
        _penPath = new PenPath(getColor(), getWidth());

        // If previous path exists, initialize PenPath to last point
        if (_penPaths.size() > 0) {
            PenPath lastPenPath = _penPaths.get(_penPaths.size() - 1);
            Point lastPoint = lastPenPath.getLastPoint();
            _penPath.moveTo(lastPoint.x, lastPoint.y);
        }

        // Add new path
        _penPaths.add(_penPath);

        // Return
        return _penPath;
    }

    /**
     * Override to paint the path.
     */
    protected void paintPaths(Painter aPntr)
    {
        for (PenPath penPath : _penPaths) {
            aPntr.setColor(penPath.getColor());
            aPntr.setStroke(Stroke.getStroke(penPath.getWidth()));
            aPntr.draw(penPath);
        }
    }

    /**
     * Returns whether pen is animating.
     */
    public boolean isAnimating()  { return _animating; }

    /**
     * Sets whether pen is animating.
     */
    protected void setAnimating(boolean aValue)
    {
        if (aValue == _animating) return;
        _animating = aValue;
    }

    /**
     * Returns an AnimPen.
     */
    public QuickDrawPen getAnimPen()
    {
        if (_animPen != null) return _animPen;
        _animPen = new QuickDrawPenAnim(_drawView, this);
        return _animPen;
    }

    /**
     * A path subclass to hold color and width.
     */
    protected static class PenPath extends Path2D {

        // Ivars
        private Color  _color;
        private double  _width;

        /**
         * Constructor.
         */
        public PenPath(Color aColor, double aWidth)
        {
            super();
            _color = aColor;
            _width = aWidth;
        }

        /**
         * Returns the color.
         */
        public Color getColor()  { return _color; }

        /**
         * Returns the stroke width.
         */
        public double getWidth()  { return _width; }
    }
}
