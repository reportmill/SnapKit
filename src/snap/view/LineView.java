package snap.view;
import snap.gfx.*;

/**
 * A View subclass to draw a line.
 */
public class LineView extends View {

    // The line
    Line           _line;
    
/**
 * Creates a new LineView.
 */
public LineView()
{
    _line = new Line(0,0,0,0);
    setBorder(Color.BLACK, 1);
}

/**
 * Creates a new LineView for given points.
 */
public LineView(double x0, double y0, double x1, double y1)
{
    _line = new Line(x0, y0, x1, y1);
    setBorder(Color.BLACK, 1);
}

/**
 * Override to return path as bounds shape.
 */
public Shape getBoundsShape()  { return _line; }

/**
 * Override to reset line.
 */
public void setWidth(double aValue)
{
    if(aValue==getWidth()) return; super.setWidth(aValue);
    _line.x1 = aValue;
}

/**
 * Override to reset line.
 */
public void setHeight(double aValue)
{
    if(aValue==getHeight()) return; super.setHeight(aValue);
    _line.y1 = aValue;
}

}