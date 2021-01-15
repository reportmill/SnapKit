/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.MathUtils;

/**
 * A Shape subclass to represent a segment of an ellipse with start and sweep angle.
 */
public class Arc extends RectBase {
    
    // The Arc start angle
    private double  _start;
    
    // The Arc sweep angle
    private double  _sweep;
    
    // The ratio of the radius that describes the hole
    private double  _holeRatio;
    
    // The Closure
    private Closure  _closure = Closure.Pie;
    
    // The Closure
    public enum Closure { Open, Chord, Pie }
    
    /**
     * Creates a new Arc.
     */
    public Arc(double aX, double aY, double aW, double aH, double aStart, double aSweep)
    {
         x = aX; y = aY; width = aW; height = aH;
         _start = aStart; _sweep = aSweep;
    }

    /**
     * Creates a new Arc.
     */
    public Arc(double aX, double aY, double aW, double aH, double aStart, double aSweep, double aHoleRatio)
    {
         x = aX; y = aY; width = aW; height = aH;
         _start = aStart; _sweep = aSweep;
         _holeRatio = aHoleRatio;
    }

    /**
     * Returns the start angle for the oval.
     */
    public double getStartAngle()  { return _start; }

    /**
     * Sets the start angle for the oval.
     */
    public void setStartAngle(double aValue)  { _start = aValue; }

    /**
     * Returns the sweep angle for the oval.
     */
    public double getSweepAngle()  { return _sweep; }

    /**
     * Sets the sweep angle for the oval.
     */
    public void setSweepAngle(double aValue)  { _sweep = aValue; }

    /**
     * Returns the ratio that describes the hole (0 = no hole, 1 = no wedge).
     */
    public double getHoleRatio()  { return _holeRatio; }

    /**
     * Sets the ratio that describes the hole (0 = no hole, 1 = no wedge).
     */
    public void setHoleRatio(double aValue)  { _holeRatio = aValue; }

    /**
     * Returns how an open arc is handles the gap.
     */
    public Closure getClosure()  { return _closure; }

    /**
     * Sets how an open arc is handles the gap.
     */
    public void setClosure(Closure aClosure)  { _closure = aClosure; }

    /**
     * Returns the path iter.
     */
    public PathIter getPathIter(Transform aTrans)
    {
        return getPath().getPathIter(aTrans);
    }

    /**
     * Returns the shape in rect.
     */
    public Shape copyFor(Rect aRect)
    {
        return new Arc(aRect.x, aRect.y, aRect.width, aRect.height, _start, _sweep, _holeRatio);
    }

    /**
     * Adds elements describing an oval in the given rect to this path. Need a real PathIter some day.
     */
    private Path getPath()
    {
        // Get basic arc info
        double startAngle = getStartAngle();
        double sweep = getSweepAngle();
        double endAngle = startAngle + sweep;
        double holeRatio = getHoleRatio();

        // Get basic bounds info: half-width/height, the x/y mid-points
        Rect bounds = getBounds();
        double hw = bounds.width/2f, hh = bounds.height/2f;
        double midX = bounds.getMidX(), midY = bounds.getMidY();

        // Create new path and set the "magic" oval factor I calculated in Mathematica one time
        Path path = new Path();
        double magic = .5523f;

        // Calculate inner start point, outer point
        double x0 = midX + cos(startAngle)*hw*holeRatio;
        double y0 = midY + sin(startAngle)*hh*holeRatio;
        double x1 = midX + cos(startAngle)*hw;
        double y1 = midY + sin(startAngle)*hh;

        // If connect was requested draw line from current point (or rect center) to start point of oval
        if (getClosure()==Closure.Pie && sweep<360) {
            path.moveTo(x0, y0);
            path.lineTo(x1, y1);
        }

        // If connect wasn't requested move to start point of oval. */
        else path.moveTo(x1, y1);

        // Append sweep
        appendArc(path, midX, midY, hw, hh, startAngle, sweep);

        // If HoleRatio, append reverse sweep
        if (holeRatio>0) {

            // Append line to inner arc (or close/move if full circle)
            double x = midX + cos(endAngle)*hw*holeRatio;
            double y = midY + sin(endAngle)*hh*holeRatio;
            if (sweep<360) path.lineTo(x, y);
            else { path.close(); path.moveTo(x, y); }

            // Append reverse arc for hole
            appendArc(path, midX, midY, hw*holeRatio, hh*holeRatio, endAngle, -sweep);
        }

        // Close path
        path.close();

        // Return path
        return path;
    }

    /**
     * Appends a 90 deg sweep to given path.
     */
    private static void appendArc(Path aPath, double midX, double midY, double hw, double hh, double angle, double aSweep)
    {
        // Limit sweep to 90, take care of remainder via recursion, later
        double sweep = aSweep>90 ? 90 : aSweep<-90 ? -90 : aSweep;
        double sweepRem = sweep==90 || sweep==-90 ? aSweep - sweep : 0;
        double sweepRatio = sweep==90 ? 1 : sweep==-90 ? -1 : sweep/90;
        double angle2 = angle + sweep;

        // Calculate control points (magic was a value I calculated in Mathematica one time)
        final double magic = .5523f;
        double cp0x = midX + cos(angle)*hw - sin(angle)*hw*magic*sweepRatio;
        double cp0y = midY + sin(angle)*hh + cos(angle)*hh*magic*sweepRatio;
        double cp1x = midX + cos(angle2)*hw + sin(angle2)*hw*magic*sweepRatio;
        double cp1y = midY + sin(angle2)*hh - cos(angle2)*hh*magic*sweepRatio;
        double x2 = midX + cos(angle2)*hw;
        double y2 = midY + sin(angle2)*hh;

        // Append path
        aPath.curveTo(cp0x, cp0y, cp1x, cp1y, x2, y2);

        // If remainder, recurse
        if (sweepRem!=0)
            appendArc(aPath, midX, midY, hw, hh, angle+sweep, sweepRem);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        if (!super.equals(anObj)) return false;
        Arc other = anObj instanceof Arc ? (Arc) anObj : null; if (other==null) return false;
        return MathUtils.equals(other._start,_start) && MathUtils.equals(other._sweep,_sweep) && other._closure==_closure;
    }

    // Convenience wraps for sin/cos.
    private static final double sin(double angle)  { return MathUtils.sin(angle); }
    private static final double cos(double angle)  { return MathUtils.cos(angle); }
}