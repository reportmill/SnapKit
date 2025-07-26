package snap.games;
import snap.geom.Path2D;
import snap.geom.Point;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This actor subclass manages a pen to draw the path that it moves.
 */
public class PenActor extends Actor {

    // Whether pen is down
    private boolean _penDown;

    // The current pen color
    private Color _penColor = Color.BLUE;

    // The current pen width
    private double _penWidth = 5;

    // The current path
    private Path2D _path;

    // The group of paths
    protected List<PenPath> _penPaths = Collections.EMPTY_LIST;

    // The current coords
    private double _penX, _penY;

    /**
     * Constructor.
     */
    public PenActor()
    {
        super();
    }

    /**
     * Returns the pen point.
     */
    public Point getPenPoint()
    {
        return localToParent(getWidth() / 2, getHeight() / 2);
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
        if (!aValue)
            _path = null;
    }

    /**
     * Set pen down.
     */
    public void penDown()
    {
        if (isShowing())
            penDown(getPenPoint());
        else setPenDown(true);
    }

    /**
     * Sets the pen down at given location.
     */
    public void penDown(Point aPnt)
    {
        setPenDown(true);
        _penX = aPnt.x;
        _penY = aPnt.y;
    }

    /**
     * Clears the pen.
     */
    public void clearPen()
    {
        _penPaths.clear();
        _path = null;
    }

    /**
     * Sets the stroke color.
     */
    public void setPenColor(String aString)
    {
        _penColor = Color.get(aString);
        _path = null;
    }

    /**
     * Sets the stroke width.
     */
    public void setWidth(double aValue)
    {
        _penWidth = aValue;
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
        _path.lineTo(_penX = aPnt.x, _penY = aPnt.y);
    }

    /**
     * Creates a new path.
     */
    protected Path2D createPath()
    {
        PenPath path = new PenPath(_penColor, _penWidth);
        path.moveTo(_penX, _penY);
        if (_penPaths == Collections.EMPTY_LIST)
            _penPaths = new ArrayList<>();
        _penPaths.add(path);
        return path;
    }

    /**
     * Move sprite forward.
     */
    @Override
    public void moveBy(double aCount)
    {
        // Do normal version
        super.moveBy(aCount);

        // Update pen
        lineTo(getPenPoint());
    }

    /**
     * Override to update pen location.
     */
    @Override
    protected void setShowing(boolean aValue)
    {
        if (aValue == isShowing()) return;
        super.setShowing(aValue);
        if (isPenDown())
            penDown();
    }

    /**
     * Paints the pen.
     */
    protected void paintPen(Painter aPntr)
    {
        // Iterate over pen paths and paint
        for (PenPath pp : _penPaths) {
            aPntr.setColor(pp.getColor());
            aPntr.setStroke(Stroke.getStrokeRound(pp.getWidth()));
            aPntr.draw(pp);
        }
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
