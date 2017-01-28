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
public LineView()  { }

/**
 * Creates a new LineView for given points.
 */
public LineView(double x0, double y0, double x1, double y1)
{
    _line = new Line(x0, y0, x1, y1);
    setBorder(Color.BLACK, 1);
}

}