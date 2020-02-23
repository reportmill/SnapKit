package snap.view;
import snap.geom.Arc;

/**
 * A view to show an Arc.
 */
public class ArcView extends View {
    
    // The start angle
    double         _start;
    
    // The sweep angle
    double         _sweep = 360;
    
    // The arc
    Arc            _arc;
    
    // Constants for ArcView properties
    public static final String StartAngle_Prop = "StartAngle";
    public static final String SweepAngle_Prop = "SweepAngle";

/**
 * Creates a new ArcView.
 */
public ArcView()  { }

/**
 * Creates a new ArcView.
 */
public ArcView(double aX, double aY, double aW, double aH, double aSA, double aSW)
{
    setBounds(aX,aY,aW,aH); setStartAngle(aSA); setSweepAngle(aSW);
}

/**
 * Returns the arc.
 */
public Arc getArc()
{
    return _arc!=null? _arc : (_arc = new Arc(0,0,getWidth(),getHeight(), getStartAngle(), getSweepAngle()));
}

/**
 * Returns the start angle.
 */
public double getStartAngle()  { return _start; }

/**
 * Sets the start angle.
 */
public void setStartAngle(double anAngle)
{
    if(anAngle==_start) return;
    repaint();
    firePropChange(StartAngle_Prop, _start, _start = anAngle); _arc = null;
}

/**
 * Returns the sweep angle.
 */
public double getSweepAngle()  { return _sweep; }

/**
 * Sets the sweep angle.
 */
public void setSweepAngle(double anAngle)
{
    if(anAngle==_sweep) return;
    repaint();
    firePropChange(SweepAngle_Prop, _sweep, _sweep = anAngle); _arc = null;
}

/**
 * Returns the bounds path.
 */
public Arc getBoundsShape()  { return getArc(); }

/**
 * Override to reset arc.
 */
public void setWidth(double aValue)  { if(aValue==getWidth()) return; super.setWidth(aValue); _arc = null; }

/**
 * Override to reset arc.
 */
public void setHeight(double aValue)  { if(aValue==getHeight()) return; super.setHeight(aValue); _arc = null; }

}