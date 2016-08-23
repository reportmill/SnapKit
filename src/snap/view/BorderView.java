package snap.view;
import snap.gfx.Pos;
import snap.util.*;

/**
 * A custom class.
 */
public class BorderView extends ParentView {
    
    // The layout
    ViewLayout.BorderLayout _layout = new ViewLayout.BorderLayout(this);

/**
 * Returns the center node.
 */
public View getCenter()  { return _layout.getCenter(); }

/**
 * Sets the center node.
 */
public void setCenter(View aView)
{
    View old = getCenter(); if(aView==old) return;
    if(old!=null) removeChild(old); if(aView!=null) addChild(aView);
    _layout.setCenter(aView);
    firePropChange("Center", old, aView);
}

/**
 * Returns the top node.
 */
public View getTop()  { return _layout.getTop(); }

/**
 * Sets the top node.
 */
public void setTop(View aView)
{
    View old = getTop(); if(aView==old) return;
    if(old!=null) removeChild(old); if(aView!=null) addChild(aView);
    _layout.setTop(aView);
    firePropChange("Top", old, aView);
}

/**
 * Returns the bottom node.
 */
public View getBottom()  { return _layout.getBottom(); }

/**
 * Sets the bottom node.
 */
public void setBottom(View aView)
{
    View old = getBottom(); if(aView==old) return;
    if(old!=null) removeChild(old); if(aView!=null) addChild(aView);
    _layout.setBottom(aView);
    firePropChange("Bottom", old, aView);
}

/**
 * Returns the left node.
 */
public View getLeft()  { return _layout.getLeft(); }

/**
 * Sets the left node.
 */
public void setLeft(View aView)
{
    View old = getLeft(); if(aView==old) return;
    if(old!=null) removeChild(old); if(aView!=null) addChild(aView);
    _layout.setLeft(aView);
    firePropChange("Left", old, aView);
}

/**
 * Returns the right node.
 */
public View getRight()  { return _layout.getRight(); }

/**
 * Sets the right node.
 */
public void setRight(View aView)
{
    View old = getRight(); if(aView==old) return;
    if(old!=null) removeChild(old); if(aView!=null) addChild(aView);
    _layout.setRight(aView);
    firePropChange("Right", old, aView);
}

/**
 * Returns whether layout should fill center when bigger than pref size.
 */
public boolean isFillCenter()  { return _layout.isFillCenter(); }

/**
 * Sets whether to fill center when bigger than pref size.
 */
public void setFillCenter(boolean aValue)  { _layout.setFillCenter(aValue); }
    
/**
 * Returns the default alignment.
 */    
public Pos getAlignmentDefault()  { return Pos.CENTER; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(aW); }

/**
 * Layout children.
 */
protected void layoutChildren()  { _layout.layoutChildren(); }

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive Top, Left, Center, Bottom, Right
    if(getTop()!=null) { XMLElement top = new XMLElement("Top"); anElement.add(top);
        top.add(anArchiver.toXML(getTop(), this)); }
    if(getLeft()!=null) { XMLElement lft = new XMLElement("Left"); anElement.add(lft);
        lft.add(anArchiver.toXML(getLeft(), this)); }
    if(getCenter()!=null) { XMLElement ctr = new XMLElement("Center"); anElement.add(ctr);
        ctr.add(anArchiver.toXML(getCenter(), this)); }
    if(getBottom()!=null) { XMLElement btm = new XMLElement("Bottom"); anElement.add(btm);
        btm.add(anArchiver.toXML(getBottom(), this)); }
    if(getRight()!=null) { XMLElement rgt = new XMLElement("Right"); anElement.add(rgt);
        rgt.add(anArchiver.toXML(getRight(), this)); }
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive Top
    XMLElement top = anElement.get("Top"); top = top!=null && top.getElementCount()>0? top.getElement(0) : null;
    Object topNode = top!=null? anArchiver.fromXML(top, this) : null;
    if(topNode instanceof View) setTop((View)topNode);
    
    // Unarchive Left
    XMLElement lft = anElement.get("Left"); lft = lft!=null && lft.getElementCount()>0? lft.getElement(0) : null;
    Object lftNode = lft!=null? anArchiver.fromXML(lft, this) : null;
    if(lftNode instanceof View) setLeft((View)lftNode);
    
    // Unarchive Center
    XMLElement ctr = anElement.get("Center"); ctr = ctr!=null && ctr.getElementCount()>0? ctr.getElement(0) : null;
    Object ctrNode = ctr!=null? anArchiver.fromXML(ctr, this) : null;
    if(ctrNode instanceof View) setCenter((View)ctrNode);
    
    // Unarchive Bottom
    XMLElement btm = anElement.get("Bottom"); btm = btm!=null && btm.getElementCount()>0? btm.getElement(0) : null;
    Object btmNode = btm!=null? anArchiver.fromXML(btm, this) : null;
    if(btmNode instanceof View) setCenter((View)btmNode);
    
    // Unarchive Right
    XMLElement rgt = anElement.get("Right"); rgt = rgt!=null && rgt.getElementCount()>0? rgt.getElement(0) : null;
    Object rgtNode = rgt!=null? anArchiver.fromXML(rgt, this) : null;
    if(rgtNode instanceof View) setRight((View)rgtNode);
}

}