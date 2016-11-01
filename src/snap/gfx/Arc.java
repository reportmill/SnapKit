/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.MathUtils;

/**
 * A custom class.
 */
public class Arc extends RectBase {
    
    // The Arc start angle
    double     _start;
    
    // The Arc sweep angle
    double     _sweep;
    
    // The Closure
    Closure    _closure = Closure.Pie;
    
    // The Closure
    public enum Closure { Open, Chord, Pie }
    
/**
 * Creates a new Arc.
 */
public Arc(double aX, double aY, double aW, double aH, double aStart, double aSweep)
{
     x = aX; y = aY; width = aW; height = aH; _start = aStart; _sweep = aSweep;
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
public PathIter getPathIter(Transform aTrans)  { return getPath().getPathIter(aTrans); }

/**
 * Returns the shape in rect.
 */
public Shape copyFor(Rect aRect)
{
    return new Arc(aRect.x, aRect.y, aRect.width, aRect.height, _start, _sweep);
}

/**
 * Adds elements describing an oval in the given rect to this path. Need a real PathIter some day.
 */
private Path getPath()
{
    // Get half-width/height, the x/y mid-points and the "magic" oval factor I calculated in Mathematica one time
    Rect bounds = getBounds(); double startAngle = getStartAngle(), sweep = getSweepAngle();
    double hw = bounds.width/2f, hh = bounds.height/2f;
    double midX = bounds.getMidX(), midY = bounds.getMidY();
    double magic = .5523f;
    Path path = new Path();
    
    // If connect was requested draw line from current point (or rect center) to start point of oval
    if(getClosure()==Closure.Pie && sweep<360) {
        path.moveTo(midX, midY);
        path.lineTo(midX + MathUtils.cos(startAngle)*hw, midY + MathUtils.sin(startAngle)*hh);
    }
    
    // If connect wasn't requested move to start point of oval. */
    else path.moveTo(midX + MathUtils.cos(startAngle)*hw, midY + MathUtils.sin(startAngle)*hh);

    // Make bezier for upper right quadrant PScurveto(-hw*f, -hh, -hw, -hh*f, -hw, 0);
    double angle = startAngle, endAngle = startAngle + sweep;
    for(; angle + 90 <= endAngle; angle += 90)
        path.curveTo(midX + MathUtils.cos(angle)*hw - MathUtils.sin(angle)*hw*magic,
                midY + MathUtils.sin(angle)*hh + MathUtils.cos(angle)*hh*magic,
                midX + MathUtils.cos(angle+90)*hw + MathUtils.sin(angle+90)*hw*magic,
                midY + MathUtils.sin(angle+90)*hh - MathUtils.cos(angle+90)*hh*magic,
                midX + MathUtils.cos(angle+90)*hw,
                midY + MathUtils.sin(angle+90)*hh);

    // If sweep did not end on a quadrant boundary, add remainder of quadrant
    if(angle < startAngle + sweep) {
        double sweepRatio = MathUtils.mod(sweep, 90f)/90f; // Math.IEEEremainder(sweep, 90)/90;
        path.curveTo(midX + MathUtils.cos(angle)*hw - MathUtils.sin(angle)*hw*magic*sweepRatio,
                midY + MathUtils.sin(angle)*hh + MathUtils.cos(angle)*hh*magic*sweepRatio,
                midX + MathUtils.cos(startAngle+sweep)*hw + MathUtils.sin(startAngle+sweep)*hw*magic*sweepRatio,
                midY + MathUtils.sin(startAngle+sweep)*hh - MathUtils.cos(startAngle+sweep)*hh*magic*sweepRatio,
                midX + MathUtils.cos(startAngle+sweep)*hw,
                midY + MathUtils.sin(startAngle+sweep)*hh);
    }
    
    // Close path and return
    path.close(); return path;
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    if(!super.equals(anObj)) return false;
    Arc other = anObj instanceof Arc? (Arc)anObj : null; if(other==null) return false;
    return MathUtils.equals(other._start,_start) && MathUtils.equals(other._sweep,_sweep) && other._closure==_closure;
}

}