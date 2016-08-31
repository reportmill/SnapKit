package snap.view;
import snap.gfx.Pos;
import snap.util.*;

/**
 * A View that holds another view.
 */
public class Box extends ParentView {

    // The content
    View       _child;
    
    // The Box layout
    ViewLayout.BoxLayout  _layout = new ViewLayout.BoxLayout(this);
    
/**
 * Creates a new Box.
 */
public Box()  { }

/**
 * Creates a new Box for content.
 */
public Box(View aContent)  { setContent(aContent); }

/**
 * Returns the box content.
 */
public View getContent()  { return _child; }

/**
 * Sets the box content.
 */
public void setContent(View aView)
{
    if(aView==_child) return;
    removeChildren();
    addChild(_child = aView);
}

/**
 * Returns whether children will be resized to fill width.
 */
public boolean isFillWidth()  { return _layout.isFillWidth(); }

/**
 * Sets whether children will be resized to fill width.
 */
public void setFillWidth(boolean aValue)  { _layout.setFillWidth(aValue); repaint(); relayoutParent(); }

/**
 * Returns whether children will be resized to fill height.
 */
public boolean isFillHeight()  { return _layout.isFillHeight(); }

/**
 * Sets whether children will be resized to fill height.
 */
public void setFillHeight(boolean aValue)  { _layout.setFillHeight(aValue); repaint(); relayoutParent(); }

/**
 * Returns whether layout should scale instead of size.
 */
public boolean isScaleToFit()  { return _layout.isScaleToFit(); }

/**
 * Sets whether layout should scale instead of size.
 */
public void setScaleToFit(boolean aValue)  { _layout.setScaleToFit(aValue); repaint(); relayoutParent(); }

/**
 * Returns whether to scale up as well as down.
 */
public boolean isScaleUp()  { return isFillWidth(); }

/**
 * Sets whether to scale up as well as down.
 */
public void setScaleUp(boolean aValue)  { setFillWidth(aValue); }

/**
 * Override to change to CENTER.
 */    
public Pos getAlignDefault()  { return Pos.CENTER; }

/**
 * Override.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(aH); }

/**
 * Override.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(aW); }

/**
 * Override.
 */
protected void layoutChildren()  { _layout.layoutChildren(); }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive FillWidth
    if(isFillWidth()) e.add("FillWidth", true);
    if(isFillHeight()) e.add("FillHeight", true);
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);

    // Unarchive Spacing, FillWidth
    if(anElement.hasAttribute("FillWidth")) setFillWidth(anElement.getAttributeBoolValue("FillWidth"));
    if(anElement.hasAttribute("FillHeight")) setFillHeight(anElement.getAttributeBoolValue("FillHeight"));
}

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive Content
    if(getContent()==null) return;
    anElement.add(anArchiver.toXML(getContent(), this));
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive first view
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class childClass = anArchiver.getClass(childXML.getName());
        if(childClass!=null && View.class.isAssignableFrom(childClass)) {
            View view = (View)anArchiver.fromXML(childXML, this);
            setContent(view); break;
        }
    }
}

}