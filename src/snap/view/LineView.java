package snap.view;
import snap.geom.Line;
import snap.geom.Pos;
import snap.gfx.*;

/**
 * A View subclass to draw a line.
 */
public class LineView extends View {
    
    // The coordinates
    double         _x0, _y0, _x1, _y1;

    // The line
    Line _line;
    
/**
 * Creates a new LineView.
 */
public LineView()  { setBorder(Color.BLACK, 1); }

/**
 * Creates a new LineView for given points.
 */
public LineView(double x0, double y0, double x1, double y1)
{
    setLineInParent(x0, y0, x1, y1);
    setBorder(Color.BLACK, 1);
}

/**
 * Returns the first point x.
 */
public double getX0()  { return _x0; }

/**
 * Returns the first point y.
 */
public double getY0()  { return _y0; }

/**
 * Returns the second point x.
 */
public double getX1()  { return _x1; }

/**
 * Returns the second point y.
 */
public double getY1()  { return _y1; }

/**
 * Returns the line.
 */
public Line getLine()  { return _line!=null? _line : (_line = new Line(_x0, _y0, _x1, _y1)); }

/**
 * Sets the line.
 */
public void setLine(double x0, double y0, double x1, double y1)
{
    _x0 = x0; _y0 = y0; _x1 = x1; _y1 = y1; _line = null;
}

/**
 * Sets the line.
 */
public void setLineInParent(double x0, double y0, double x1, double y1)
{
    double x = Math.min(x0, x1), w = Math.max(x0, x1) - x;
    double y = Math.min(y0, y1), h = Math.max(y0, y1) - y;
    setBounds(x, y, w, h);
    setLine(x0 - x, y0 - y, x1 - x, y1 - y);
}

/**
 * Resets the line from bounds.
 */
protected void resetLine()
{
    double w = getWidth(), h = getHeight();
    switch(getOriginPos()) {
        case TOP_LEFT: setLine(0, 0, w, h); break;
        case TOP_RIGHT: setLine(w, 0, 0, h); break;
        case BOTTOM_LEFT: setLine(0, h, w, 0); break;
        case BOTTOM_RIGHT: setLine(w, h, 0, 0); break;
    }
}

/**
 * Returns the origin position.
 */
public Pos getOriginPos()
{
    if(_x0<=_x1 && _y0<=_y1) return Pos.TOP_LEFT;
    if(_x0<=_x1 && _y0>_y1) return Pos.BOTTOM_LEFT;
    if(_x0>_x1 && _y0<=_y1) return Pos.TOP_RIGHT;
    return Pos.BOTTOM_RIGHT;
}

/**
 * Override to return path as bounds shape.
 */
public Line getBoundsShape()  { return getLine(); }

/**
 * Override to reset line.
 */
public void setWidth(double aValue)  { if(aValue==getWidth()) return; super.setWidth(aValue); resetLine(); }

/**
 * Override to reset line.
 */
public void setHeight(double aValue)  { if(aValue==getHeight()) return; super.setHeight(aValue); resetLine(); }

}