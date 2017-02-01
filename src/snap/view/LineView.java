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
 * Returns the first point x.
 */
public double getX0()  { return _line.x0; }

/**
 * Returns the first point y.
 */
public double getY0()  { return _line.y0; }

/**
 * Returns the second point x.
 */
public double getX1()  { return _line.x1; }

/**
 * Returns the second point y.
 */
public double getY1()  { return _line.y1; }

/**
 * Returns the line.
 */
public Line getLine()  { return _line; }

/**
 * Sets the line.
 */
public void setLine(double x0, double y0, double x1, double y1)
{
    _line.x0 = x0; _line.y0 = y0; _line.x1 = x1; _line.y1 = y1;
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
 * Returns the origin position.
 */
public Pos getOriginPos()
{
    double x0 = getX0(), y0 = getY0(), x1 = getX1(), y1 = getY1();
    if(x0<=x1 && y0<=y1) return Pos.TOP_LEFT;
    if(x0<=x1 && y0>y1) return Pos.BOTTOM_LEFT;
    if(x0>x1 && y0<=y1) return Pos.TOP_RIGHT;
    return Pos.BOTTOM_RIGHT;
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
    resetLine();
}

/**
 * Override to reset line.
 */
public void setHeight(double aValue)
{
    if(aValue==getHeight()) return; super.setHeight(aValue);
    resetLine();
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
 * Paints background.
 */
protected void paintBack(Painter aPntr)
{
    Paint fill = getFill();
    Border border = getBorder(); if(fill==null && border==null) return;
    Shape shape = getBoundsShape();
    if(fill!=null) {
        aPntr.setPaint(fill); aPntr.fill(shape); }
    if(border!=null)
        border.paint(aPntr, shape);
}

}