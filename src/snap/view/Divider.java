package snap.view;
import snap.gfx.*;

/**
 * A View to represent a movable separation between views.
 */
public class Divider extends View {
    
    // The size
    int       _size = 8;
    
    // The location, if explicitly positioned
    double    _loc = -1;
    
    // The remainder, if explicitly position relative to right side
    double    _rem = -1;
    
    // The last mouse down location
    double _mx, _my;
    
/**
 * Creates a new Divider.
 */
public Divider()
{
    enableEvents(MousePressed, MouseDragged);
}

/**
 * Returns the location.
 */
public double getLocation()  { return _loc; }

/**
 * Sets the location.
 */
public void setLocation(double aValue)
{
    _loc = aValue;
    relayoutParent();
}

/**
 * Returns the remainder.
 */
public double getRemainder()  { return _rem; }

/**
 * Sets the remainder.
 */
public void setRemainder(double aValue)
{
    _rem = aValue;
    relayoutParent();
}

/**
 * Returns the remainder as location.
 */
public double getRemainderAsLocation()
{
    ParentView par = getParent(); if(par==null) return _rem;
    return _rem - par.getWidth() - par.getInsetsAll().right;
}

/**
 * Override.
 */
public boolean isVertical()  { return getParent()!=null? !getParent().isVertical() : super.isVertical(); }

/**
 * Override.
 */
protected void setParent(ParentView aPar)
{
    super.setParent(aPar);
    setCursor(isVertical()? Cursor.E_RESIZE:Cursor.N_RESIZE);
}

/**
 * Override to wrap in Painter and forward.
 */
protected void paintFront(Painter aPntr)
{
    Insets ins = getInsetsAll();
    double px = ins.left, py = ins.top, pw = getWidth() - px - ins.right, ph = getHeight() - py - ins.bottom;
    aPntr.setPaint(getDivFill()); aPntr.fillRect(px,py,pw,ph);
    aPntr.setColor(Color.LIGHTGRAY); aPntr.setStroke(Stroke.Stroke1); aPntr.drawRect(px+.5,py+.5,pw-1,ph-1);
}

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll(); int s = isHorizontal()? 0 : _size; return ins.getLeft() + s + ins.getRight();
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll(); int s = isHorizontal()? _size : 0; return ins.getTop() + s + ins.getBottom();
}

/**
 * Handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MousePressed: Set Mouse location and divider
    if(anEvent.isMousePressed()) { _mx = anEvent.getX(); _my = anEvent.getY(); }
    
    // Handle MouseDragged: If divider pressed, reset adjacent sides
    else if(anEvent.isMouseDragged()) {
        int index = getParent().indexOfChild(this);
        View peer = getParent().getChild(index-1);
        Point pnt = localToParent(anEvent.getX() - _size/2, anEvent.getY() - _size/2);
        double loc = isVertical()? pnt.getX() - peer.getX() : pnt.getY() - peer.getY();
        setLocation(loc);
        System.out.println("SetLocation: " + loc);
    }
}

/**
 * Returns the divider fill.
 */
private Paint getDivFill()
{
    if(_dfill!=null) return _dfill;
    Color c1 = Color.get("#fbfbfb"), c2 = Color.get("#e3e3e3");
    double x1 = 0, y1 = .5, x2 = 1, y2 = .5; if(isHorizontal()) { x1 = x2 = .5; y1 = 0; y2 = 1; }
    return _dfill = new GradientPaint(x1,y1,x2,y2, GradientPaint.getStops(0,c1,1,c2));
} Paint _dfill;

}