package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to show children with user adjustable divider.
 */
public class SplitView extends ParentView {

    // The divider location
    int                _dividerLoc;
    
    // The list of items
    List <View>        _items = new ArrayList();
    
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
public int getItemCount()  { return _items.size(); }

/**
 * Override to make sure dividers are in place.
 */
public void addItem(View aView)  { addItem(aView, getItemCount()); }

/**
 * Returns the SplitView items.
 */
public List <View> getItems()  { return _items; }

/**
 * Override to make sure dividers are in place.
 */
public void addItem(View aView, int anIndex)
{
    //System.out.println("Add Child");
    addChild(aView, anIndex);
    _items.add(aView);
    _cbnds.add(new Rect());
}

/**
 * Override to remove unused dividers.
 */
public int removeItem(View aView)
{
    int index = ListUtils.indexOfId(_items, aView);
    if(index>=0) { _items.remove(index); _cbnds.remove(index); }
    return removeChild(aView);
}

/**
 * Sets the item at index.
 */
public void setItem(View aView, int anIndex)
{
    View old = anIndex<getItemCount()? _items.get(anIndex) : null;
    int index = old!=null? removeItem(old) : -1;
    addItem(aView, index>=0? index : getItemCount());
}

/**
 * Sets the splitview items to given views
 */
public void setItems(View ... theViews)
{
    removeItems();
    for(View view : theViews) addItem(view);
}

/**
 * Sets the splitview items to given views
 */
public void removeItems()  { for(View view : getItems().toArray(new View[0])) removeItem(view); }

/**
 * Returns the child size at index.
 */
protected double getChildSize(int anIndex)
{
    Rect bnds = _cbnds.get(anIndex);
    return isHorizontal()? bnds.getWidth() : bnds.getHeight();
}

/**
 * Sets an child size.
 */
protected void setChildSize(int anIndex, double aSize)
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
protected void setChildSize(View aView, double aSize)
{
    int index = ListUtils.indexOfId(_items, aView);
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
 * Adds a child with animation.
 */
public void addItemWithAnim(View aView, double aSize)
{
    if(isVertical()) aView.setHeight(1); else aView.setWidth(1);
    addItem(aView);
    setChildSize(aView, 1);
    getAnim(0).clear().getAnim(500).setValue("Divider", 1d, aSize).setOnFrame(a -> doAnim(a,aView)).play();
}

/**
 * Removes a child with animation.
 */
public void removeItemWithAnim(View aView)
{
    double size = isVertical()? aView.getHeight() : aView.getWidth();
    ViewAnim anim = getAnim(0).clear().getAnim(500).setValue("Divider", size, 1d);
    anim.setOnFrame(a -> doAnim(a,aView)).setOnFinish(a -> removeItem(aView)).play();
}

/** Called on each frame of SplitView anim. */
private void doAnim(ViewAnim anAnim, View aView)
{
    double val = (double)anAnim.getValue("Divider");
    setChildSize(aView, Math.round(val));
}

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    double pw = 0; Insets ins = getInsetsAll();
    if(isHorizontal()) { for(View item : getItems()) pw += item.getPrefWidth(); pw += (_items.size()-1)*8; }
    else for(View item : getItems()) pw = Math.max(pw, item.getPrefWidth());
    return ins.getLeft() + pw + ins.getRight();
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    double ph = 0; Insets ins = getInsetsAll();
    if(isVertical()) { for(View item : getItems()) ph += item.getPrefHeight(); ph += (_items.size()-1)*8; }
    else for(View item : getItems()) ph = Math.max(ph, item.getPrefHeight());
    return ins.getTop() + ph + ins.getBottom();
}

/**
 * Override to layout children.
 */
protected void layoutChildren()
{
    View children[] = _items.toArray(new View[0]);
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
    
    for(int i=0,iMax=_items.size()-1;i<iMax;i++) { Rect rect = _cbnds.get(i);
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
public Border getDefaultBorder()  { return SPLIT_VIEW_BORDER; }

/**
 * XML archival deep.
 */
public void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive items
    for(View item : getItems()) {
        XMLElement cxml = anArchiver.toXML(item, this);
        anElement.add(cxml);
    }    
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive as child nodes
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class cls = anArchiver.getClass(childXML.getName());
        if(cls!=null && View.class.isAssignableFrom(cls)) {
            View view = (View)anArchiver.fromXML(childXML, this);
            addItem(view);
        }
    }
}

}