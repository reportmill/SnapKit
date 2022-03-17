/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Stroke;
import snap.util.ArrayUtils;

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

    // The array of discrete tasks
    private PaintTask[]  _tasks = new PaintTask[0];

    // The current task path
    private Path2D  _taskPath;

    // The VertexArray
    private VertexArray  _vertexArray;

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
        flushTask();

        // Create new PaintTask, add to Tasks property - - Need to transform shape!
        PaintTask task = new PaintTask(aShape, getColor(), getStroke(), _layerOffset);
        addPaintTask(task);
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
        if (_taskPath == null) _taskPath = new Path2D();
        _taskPath.moveTo(aX, aY);
    }

    /**
     * Adds a lineTo segment.
     */
    public void lineTo(double aX, double aY)
    {
        if (_taskPath == null) _taskPath = new Path2D();
        _taskPath.lineTo(aX, aY);
    }

    /**
     * Adds a quad segment.
     */
    public void quadTo(double aCX, double aCY, double aX, double aY)
    {
        if (_taskPath == null) _taskPath = new Path2D();
        _taskPath.quadTo(aCX, aCY, aX, aY);
    }

    /**
     * Adds a cubic segment.
     */
    public void cubicTo(double aC1X, double aC1Y, double aC2X, double aC2Y, double aX, double aY)
    {
        if (_taskPath == null) _taskPath = new Path2D();
        _taskPath.curveTo(aC1X, aC1Y, aC2X, aC2Y, aX, aY);
    }

    /**
     * Adds a close path segment.
     */
    public void closePath()
    {
        if (_taskPath == null) _taskPath = new Path2D();
        _taskPath.close();
    }

    /**
     * Adds an offset (along normal) so successive drawing is above previous drawing.
     */
    public void addLayerOffset(double aDist)
    {
        _layerOffset += aDist;
    }

    /**
     * Flushes the current task.
     */
    public void flushTask()
    {
        // If no TaskPath, just return
        if (_taskPath == null) return;

        // Create Task for TaskPath, add to Tasks and clear TaskPath
        PaintTask task = new PaintTask(_taskPath, getColor(), getStroke(), _layerOffset);
        addPaintTask(task);
        _taskPath = null;
    }

    /**
     * Returns the PaintTasks.
     */
    public PaintTask[] getPaintTasks()
    {
        flushTask();
        return _tasks;
    }

    /**
     * Adds a PaintTask.
     */
    protected void addPaintTask(PaintTask aTask)
    {
        _tasks = ArrayUtils.add(_tasks, aTask);
        _vertexArray = null;
    }

    /**
     * Returns the VertexArray for drawing.
     */
    public VertexArray getVertexArray()
    {
        // If already set, just return
        if (_vertexArray != null) return _vertexArray;

        // Get tasks (if none, return empty VertexArray)
        PaintTask[] tasks = getPaintTasks();
        if (tasks.length == 0)
            return new VertexArray();

        // Create/build VertexArray list for each Task
        VertexArray vertexArray = tasks[0].getVertexArray();
        VertexArray vertexArrayEnd = vertexArray;
        for (int i = 1; i < tasks.length; i++) {
            VertexArray vertexArrayNext = tasks[i].getVertexArray();
            vertexArrayEnd.setNext(vertexArrayNext);
            vertexArrayEnd = vertexArrayNext;
        }

        // Return
        return _vertexArray = vertexArray;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public Painter3D clone()
    {
        flushTask();

        // Do normal clone
        Painter3D clone;
        try { clone = (Painter3D) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }

        // Clone PaintTasks
        clone._tasks = _tasks.clone();
        for (int i = 0; i < _tasks.length; i++)
            clone._tasks[i] = _tasks[i].clone();

        // Return clone
        return clone;
    }

    /**
     * This class represents a discrete paint task.
     */
    private static class PaintTask implements Cloneable {

        // The shape
        private Shape  _shape;

        // The paint color
        private Color  _color;

        // The stroke
        private Stroke  _stroke;

        // The layer offset
        private double  _layerOffset;

        /**
         * Constructor.
         */
        public PaintTask(Shape aShape, Color aColor, Stroke aStroke, double anOffset)
        {
            _shape = aShape;
            _color = aColor;
            _stroke = aStroke;
            _layerOffset = anOffset;
        }

        /**
         * Returns shape.
         */
        public Shape getShape()  { return _shape; }

        /**
         * Returns color.
         */
        public Color getColor()  { return _color; }

        /**
         * Returns stroke.
         */
        public Stroke getStroke()  { return _stroke; }

        /**
         * Returns a VertexArray.
         */
        public VertexArray getVertexArray()
        {
            VertexArray vertexArray = VertexArrayUtils.getStrokedShapeVertexArray(_shape, _color, _stroke, _layerOffset);
            return vertexArray;
        }

        /**
         * Standard clone implementation.
         */
        @Override
        public PaintTask clone()
        {
            PaintTask clone;
            try { clone = (PaintTask) super.clone(); }
            catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
            return clone;
        }
    }
}
