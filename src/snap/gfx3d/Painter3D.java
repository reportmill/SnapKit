/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Stroke;

/**
 * This class renders 2D painting on a 3D plane.
 */
public class Painter3D implements Cloneable {

    // The size of paint canvas in 2D
    private double  _width, _height;

    // The current color
    private Color  _color = Color.BLACK;

    // The current stroke
    private Stroke  _stroke = Stroke.Stroke1;

    // The offset for layers
    private double  _layerOffset;

    // The last moveTo location
    private double  _moveX, _moveY;

    // The last lineTo location
    private double  _lastX, _lastY;

    // The VertexArray holding all rendered triangles (head of list)
    private VertexArray  _vertexArray;

    // The end of the VertexArray list
    private VertexArray  _vertexArrayEnd;

    /**
     * Constructor.
     */
    public Painter3D(double aWidth, double aHeight)
    {
        _width = aWidth;
        _height = aHeight;
    }

    /**
     * Returns the width.
     */
    public double getWidth()  { return _width; }

    /**
     * Returns the height.
     */
    public double getHeight()  { return _height; }

    /**
     * Returns the current paint.
     */
    public Color getColor()  { return _color; }

    /**
     * Sets the current paint.
     */
    public void setColor(Color aColor)  { _color = aColor; }

    /**
     * Returns the current stroke.
     */
    public Stroke getStroke()  { return _stroke; }

    /**
     * Sets the current stroke.
     */
    public void setStroke(Stroke aStroke)
    {
        _stroke = aStroke;
    }

    /**
     * Draws the given shape.
     */
    public void draw(Shape aShape)
    {
        // Get PathIter and path iteration vars
        PathIter pathIter = aShape.getPathIter(null);
        double[] pnts = new double[6];

        // Iterate over PathIter segs to add with moveTo, lineTo, etc.
        while (pathIter.hasNext()) {
            Seg seg = pathIter.getNext(pnts);
            switch (seg) {
                case MoveTo: moveTo(pnts[0], pnts[1]); break;
                case LineTo: lineTo(pnts[0], pnts[1]); break;
                case QuadTo: quadTo(pnts[0], pnts[1], pnts[2], pnts[3]); break;
                case CubicTo: cubicTo(pnts[0], pnts[1], pnts[2], pnts[3], pnts[4], pnts[5]); break;
                case Close: closePath(); break;
            }
        }
    }

    /**
     * Draws a rect.
     */
    public void drawRect(double aX, double aY, double aW, double aH)
    {
        draw(new Rect(aX, aY, aW, aH));
    }

    /**
     * Adds a moveTo segment.
     */
    public void moveTo(double aX, double aY)
    {
        _moveX = _lastX = aX;
        _moveY = _lastY = aY;
    }

    /**
     * Adds a lineTo segment.
     */
    public void lineTo(double aX, double aY)
    {
        // Get vectors for across the line and perpendicular to line
        Vector3D acrossVector = new Vector3D(aX - _lastX, aY - _lastY, 0).normalize();
        Vector3D downVector = new Vector3D(0, 0, 1).getCrossProduct(acrossVector).normalize();

        // Scale across/down vectors by stroke half width
        double halfWidth = _stroke.getWidth() / 2;
        acrossVector.scale(halfWidth);
        downVector.scale(halfWidth);

        // Get upper left point
        double upperLeftX = _lastX + downVector.x - acrossVector.x;
        double upperLeftY = _lastY + downVector.y - acrossVector.y;

        // Get lower left point
        double lowerLeftX = _lastX - downVector.x - acrossVector.x;
        double lowerLeftY = _lastY - downVector.y - acrossVector.y;

        // Get upper right point
        double upperRightX = aX + downVector.x + acrossVector.x;
        double upperRightY = aY + downVector.y + acrossVector.y;

        // Get lower right point
        double lowerRightX = aX - downVector.x + acrossVector.x;
        double lowerRightY = aY - downVector.y + acrossVector.y;
        double z = _layerOffset;

        // Add vertexes for points
        VertexArray vertexArray = getVertexArrayEnd();
        vertexArray.addStripPoints(upperLeftX, upperLeftY, z, lowerLeftX, lowerLeftY, z,
                                   upperRightX, upperRightY, z, lowerRightX, lowerRightY, z);

        // Update lastX/Y
        _lastX = aX;
        _lastY = aY;
    }

    /**
     * Adds a quad segment.
     */
    public void quadTo(double aCX, double aCY, double aX, double aY)
    {
        System.err.println("Painter3D:quadTo: Not implemented");
        lineTo(aX, aY);
    }

    /**
     * Adds a cubic segment.
     */
    public void cubicTo(double aC1X, double aC1Y, double aC2X, double aC2Y, double aX, double aY)
    {
        System.err.println("Painter3D:cubicTo: Not implemented");
        lineTo(aX, aY);
    }

    /**
     * Adds a close path segment.
     */
    public void closePath()
    {
        lineTo(_moveX, _moveY);
    }

    /**
     * Adds an offset (along normal) so successive drawing is above previous drawing.
     */
    public void addLayerOffset(double aDist)
    {
        _layerOffset += aDist;
    }

    /**
     * Returns the end of the VertexArray for writing.
     */
    protected VertexArray getVertexArrayEnd()
    {
        // If already set, just return
        if (_vertexArrayEnd != null) return _vertexArrayEnd;

        // Create new VertexArrayEnd and add to VertexArray
        _vertexArrayEnd = new VertexArray();
        _vertexArrayEnd.setColor(getColor());

        // Add to VertexArray
        if (_vertexArray != null)
            _vertexArray.setNext(_vertexArrayEnd);
        else _vertexArray = _vertexArrayEnd;

        // Return VertexArrayEnd
        return _vertexArrayEnd;
    }

    /**
     * Returns the VertexArray for drawing.
     */
    public VertexArray getVertexArray()  { return _vertexArray; }

    /**
     * Standard clone implementation.
     */
    @Override
    public Painter3D clone()
    {
        // Do normal clone
        Painter3D clone;
        try { clone = (Painter3D) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }

        // Clone VertexArray, VertexArrayEnd
        if (_vertexArray != null) {
            clone._vertexArray = _vertexArray.clone();
            clone._vertexArrayEnd = clone._vertexArray;
            while (clone._vertexArrayEnd.getNext() != null)
                clone._vertexArrayEnd = clone._vertexArrayEnd.getNext();
        }

        // Return clone
        return clone;
    }
}
