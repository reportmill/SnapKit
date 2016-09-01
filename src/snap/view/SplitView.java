package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to show children with user adjustable divider.
 */
public class SplitView extends ChildView {

    // The divider location
    int                _dividerLoc;
    
    // The list of kids
    List <View>        _kids = new ArrayList();
    
    // The list of child bounds
    List <Rect>        _cbnds = new ArrayList();
    
    // The default border
    static final Border SPLIT_VIEW_BORDER = Border.createLineBorder(Color.LIGHTGRAY,1);

/**
 * Creates a new SplitView.
 */
public SplitView()
{
    enableEvents(MousePressed, MouseDragged, MouseMoved, MouseExited);
    setBorder(SPLIT_VIEW_BORDER);
}

/**
 * Returns the number of items.
 */
public int getItemCount()  { return _kids.size(); }

/**
 * Sets the item at index.
 */
public void setItem(View aView, int anIndex)
{
    View old = anIndex<getItemCount()? _kids.get(anIndex) : null;
    int index = old!=null? removeChild(old) : -1;
    addChild(aView, index>=0? index : getChildCount());
}

/**
 * Override to make sure dividers are in place.
 */
public void addChild(View aChild, int anIndex)
{
    //System.out.println("Add Child");
    super.addChild(aChild, anIndex);
    _kids.add(aChild);
    _cbnds.add(new Rect());
}

/**
 * Override to remove unused dividers.
 */
public int removeChild(View aView)
{
    int index = ListUtils.indexOfId(_kids, aView);
    if(index>=0) { _kids.remove(index); _cbnds.remove(index); }
    return super.removeChild(aView);
}

/**
 * Returns the child size at index.
 */
public double getChildSize(int anIndex)
{
    Rect bnds = _cbnds.get(anIndex);
    return isHorizontal()? bnds.getWidth() : bnds.getHeight();
}

/**
 * Sets an child size.
 */
public void setChildSize(int anIndex, double aSize)
{
    //System.out.println("SetChildSize: " + anIndex + ", " + aSize);
    Rect bnds = _cbnds.get(anIndex);
    Insets ins = getInsetsAll();
    double px = ins.left, py = ins.top, pw = getWidth() - px - ins.right, ph = getHeight() - py - ins.bottom;
    
    if(isHorizontal()) bnds.setRect(bnds.x, py, aSize, ph);
    else bnds.setRect(px, bnds.y, pw, aSize);
    relayout(); repaint();
}

/**
 * Sets an child size.
 */
public void setChildSize(View aView, double aSize)
{
    int index = ListUtils.indexOfId(_kids, aView);
    if(index>=0) setChildSize(index,aSize);
}

/**
 * Sets an child ratio.
 */
public void setChildRatio(int anIndex, double aRatio)
{
    setChildSize(anIndex, aRatio*(isHorizontal()? getWidth() : getHeight()));
}

/**
 * Sets an child ratio.
 */
public void setChildRatio(View aView, double aRatio)
{
    setChildSize(aView, aRatio*(isHorizontal()? getWidth() : getHeight()));
}

/**
 * Adds a child with animation.
 */
public void addChildWithAnim(View aView, double aSize)
{
    if(isVertical()) aView.setHeight(1); else aView.setWidth(1);
    addChild(aView);
    setChildSize(aView, 1);
    Anim anim = new Anim(this, "Divider", 1d, aSize, 500);
    anim.setOnFrame(a -> setChildSize(aView, Math.round((double)a.getValue())));
    anim.play();
}

/**
 * Removes a child with animation.
 */
public void removeChildWithAnim(View aView)
{
    double size = isVertical()? aView.getHeight() : aView.getWidth();
    Anim anim = new Anim(this, "Divider", size, 1d, 500);
    anim.setOnFrame(a -> setChildSize(aView, Math.round((double)a.getValue())));
    anim.setOnFinish(a -> removeChild(aView));
    anim.play();
}

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    double pw = 0; Insets ins = getInsetsAll();
    if(isHorizontal()) { for(View child : getChildren()) pw += child.getPrefWidth(); pw += (_kids.size()-1)*8; }
    else for(View child : getChildren()) pw = Math.max(pw,child.getPrefWidth());
    return ins.getLeft() + pw + ins.getRight();
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    double ph = 0; Insets ins = getInsetsAll();
    if(isVertical()) { for(View child : getChildren()) ph += child.getPrefHeight(); ph += (_kids.size()-1)*8; }
    else for(View child : getChildren()) ph = Math.max(ph,child.getPrefHeight());
    return ins.getTop() + ph + ins.getBottom();
}

/**
 * Override to layout children.
 */
protected void layoutChildren()
{
    //System.out.println("LayoutChildren " + (_cbnds.size()>1? _cbnds.get(1) : ""));
    View children[] = _kids.toArray(new View[0]); //getChildArray();
    Insets ins = getInsetsAll();
    double px = ins.left, py = ins.top, pw = getWidth() - px - ins.right, ph = getHeight() - py - ins.bottom;
    
    // Handle Horizontal
    if(isHorizontal()) {
        
        // Normalize child bounds
        double x = px, tw = 0; int gc = 0;
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect bnds = _cbnds.get(i);
            if(bnds.isEmpty()) bnds.setRect(x,py,child.getPrefWidth(),ph);
            else if(!SnapUtils.equals(bnds.getX(),x)) bnds.setRect(x,py,bnds.getWidth(),ph);
            x = bnds.getMaxX() + 8; tw = bnds.getMaxX() - px;
            if(child.isGrowWidth()) gc++;
        }
        if(gc==0) gc = children.length;
        
        // Get diff and adjust child bounds
        double diff = pw - tw; x = px;
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect bnds = _cbnds.get(i);
            if(gc==iMax || child.isGrowWidth()) bnds.setRect(x,py,bnds.width+diff/gc,ph);
            else if(!SnapUtils.equals(bnds.getX(),x)) bnds.setRect(x,py,bnds.getWidth(),ph);
            double cx = Math.round(bnds.x), cw = Math.round(bnds.getMaxX()-cx);
            child.setBounds(cx,py,cw,ph); x = bnds.getMaxX() + 8;
        }
    }
    
    // Handle Vertical
    else {
        
        // Normalize child bounds
        double y = py, th = 0; int gc = 0;
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect bnds = _cbnds.get(i);
            if(bnds.isEmpty()) bnds.setRect(px,y,pw,child.getPrefHeight());
            else if(!SnapUtils.equals(bnds.getY(),y)) bnds.setRect(px,y,pw,bnds.getHeight());
            y = bnds.getMaxY() + 8; th = bnds.getMaxY() - py;
            if(child.isGrowHeight()) gc++;
        }
        if(gc==0) gc = children.length;
        
        // Get diff and adjust child bounds
        double diff = ph - th; y = py;
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect bnds = _cbnds.get(i);
            if(gc==iMax || child.isGrowHeight()) bnds.setRect(px,y,pw,bnds.height+diff/gc);
            else if(!SnapUtils.equals(bnds.getY(),y)) bnds.setRect(px,y,pw,bnds.getHeight());
            double cy = Math.round(bnds.y), ch = Math.round(bnds.getMaxY()-cy);
            child.setBounds(px,cy,pw,ch); y = bnds.getMaxY() + 8;
        }
    }
}

/**
 * Override to wrap in Painter and forward.
 */
protected void paintFront(Painter aPntr)
{
    Insets ins = getInsetsAll();
    double px = ins.left, py = ins.top, pw = getWidth() - px - ins.right, ph = getHeight() - py - ins.bottom;
    //aPntr.clearRect(px,py,pw,ph); //aPntr.setColor(Color.PINK); aPntr.fillRect(0,0,getWidth(),getHeight());
    
    for(int i=0,iMax=_kids.size()-1;i<iMax;i++) { Rect rect = _cbnds.get(i);
        if(isHorizontal()) {
            double dx = Math.round(rect.getMaxX());
            aPntr.setPaint(getDivFill()); aPntr.fillRect(dx,py,8,ph);
            aPntr.setColor(Color.LIGHTGRAY); aPntr.setStroke(Stroke.Stroke1); aPntr.drawRect(dx+.5,.5,8-1,py+ph-1);
        }
        else {
            double dy = Math.round(rect.getMaxY());
            aPntr.setPaint(getDivFill()); aPntr.fillRect(px,dy,pw,8);
            aPntr.setColor(Color.LIGHTGRAY); aPntr.setStroke(Stroke.Stroke1); aPntr.drawRect(.5,dy+.5,px+pw-1,8-1);
        }
    }
}

/**
 * Returns the divider fill.
 */
private Paint getDivFill()
{
    if(_dfill!=null) return _dfill;
    Color c1 = Color.get("#fbfbfb"), c2 = Color.get("#e3e3e3");
    double x1 = 0, y1 = .5, x2 = 1, y2 = .5; if(isVertical()) { x1 = x2 = .5; y1 = 0; y2 = 1; }
    return _dfill = new GradientPaint(x1,y1,x2,y2, GradientPaint.getStops(0,c1,1,c2));
} Paint _dfill;

/**
 * Handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MousePressed: Set Mouse location and divider
    if(anEvent.isMousePressed()) {
        _mx = anEvent.getX(); _my = anEvent.getY(); _mdiv = getDivider(_mx,_my); }
    
    // Handle MouseDragged: If divider pressed, reset adjacent sides
    else if(anEvent.isMouseDragged() && _mdiv>=0) {
        double dx = anEvent.getX() - _mx, dy = anEvent.getY() - _my;
        setChildSize(_mdiv, getChildSize(_mdiv) + (isHorizontal()? dx : dy));
        setChildSize(_mdiv+1, getChildSize(_mdiv+1) - (isHorizontal()? dx : dy));
        _mx = anEvent.getX(); _my = anEvent.getY();
        anEvent.consume();
    }
    
    // Handle MouseMoved: Set cursor
    else if(anEvent.isMouseMoved()) {
        if(getDivider(anEvent.getX(),anEvent.getY())>=0) {
            setCursor(isHorizontal()? Cursor.E_RESIZE : Cursor.N_RESIZE); _cset = true; }
        else if(_cset) { setCursor(null); _cset = false; }
    }
    
    // Handle MouseExited: Reset cursor
    else if(anEvent.isMouseExited())
        if(_cset) { setCursor(null); _cset = false; }

} double _mx, _my; int _mdiv; boolean _cset;

/**
 * Returns the divider for the given location.
 */
private int getDivider(double aX, double aY)
{
    if(isHorizontal()) {
        for(int i=0;i<_cbnds.size()-1;i++) { double xmin = _cbnds.get(i).getMaxX(), xmax = xmin + 8;
            if(xmin<=aX && aX<xmax)
                return i; }
    }
    else {
        for(int i=0;i<_cbnds.size()-1;i++) { double ymin = _cbnds.get(i).getMaxY(), ymax = ymin + 8;
            if(ymin<=aY && aY<ymax)
                return i; }
    }
    return -1;
}

/**
 * Returns the default border.
 */
public Border getBorderDefault()  { return SPLIT_VIEW_BORDER; }

/**
 * XML archival deep.
 */
public void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive children
    for(View child : getChildren()) {
        XMLElement cxml = anArchiver.toXML(child, this);
        cxml.removeAttribute("x"); cxml.removeAttribute("y"); cxml.removeAttribute("asize");
        anElement.add(cxml);
    }    
}

}