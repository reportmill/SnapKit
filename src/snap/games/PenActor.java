package snap.games;
import snap.geom.Path2D;
import snap.geom.Point;
import snap.gfx.*;
import java.util.ArrayList;
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
    private Path2D _penPath;

    // The group of paths
    protected List<PenPath> _penPaths = new ArrayList<>();

    /**
     * Constructor.
     */
    public PenActor()
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
        if (!aValue)
            _penPath = null;
    }

    /**
     * Set pen down.
     */
    public void penDown()  { setPenDown(true); }

    /**
     * Clears the pen.
     */
    public void clearPen()
    {
        _penPaths.clear();
        _penPath = null;
    }

    /**
     * Returns the pen stroke color.
     */
    public Color getPenColor()  { return _penColor; }

    /**
     * Sets the pen stroke color.
     */
    public void setPenColor(String aString)
    {
        _penColor = Color.get(aString);
        _penPath = null;
    }

    /**
     * Returns the pen stroke width.
     */
    public double getPenWidth()  { return _penWidth; }

    /**
     * Sets the stroke width.
     */
    public void setPenWidth(double aValue)
    {
        _penWidth = aValue;
        _penPath = null;
    }

    /**
     * Returns the pen point.
     */
    public Point getPenPoint()
    {
        return localToParent(getWidth() / 2, getHeight() / 2);
    }

    /**
     * Override to update pen path.
     */
    @Override
    public void moveBy(double aCount)
    {
        // Create new path if needed
        if (_penPath == null && _penDown)
            _penPath = createPenPath();

        // Do normal version
        super.moveBy(aCount);

        // If pen not down, just return
        if (!_penDown) return;

        // Add line to point
        Point penPoint = getPenPoint();
        _penPath.lineTo(penPoint.x, penPoint.y);
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
     * Creates a new path.
     */
    private Path2D createPenPath()
    {
        PenPath path = new PenPath(_penColor, _penWidth);
        Point penPoint = getPenPoint();
        path.moveTo(penPoint.x, penPoint.y);
        _penPaths.add(path);
        return path;
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
