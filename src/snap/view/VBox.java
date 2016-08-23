package snap.view;
import snap.gfx.Pos;
import snap.util.*;

/**
 * A custom class.
 */
public class VBox extends ChildView {

    // The VBox layout
    ViewLayout.VBoxLayout  _layout = new ViewLayout.VBoxLayout(this);
    
/**
 * Returns the spacing.
 */
public double getSpacing()  { return _layout.getSpacing(); }

/**
 * Sets the spacing.
 */
public void setSpacing(double aValue)  { _layout.setSpacing(aValue); }

/**
 * Returns whether children will be resized to fill width.
 */
public boolean isFillWidth()  { return _layout.isFillWidth(); }

/**
 * Sets whether children will be resized to fill width.
 */
public void setFillWidth(boolean aValue)  { _layout.setFillWidth(aValue); }

/**
 * Returns the default alignment.
 */    
public Pos getAlignmentDefault()  { return Pos.TOP_LEFT; }

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
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes and reset element name
    XMLElement e = super.toXMLView(anArchiver); e.setName("VBox");
    
    // Archive Spacing, FillWidth
    if(getSpacing()!=0) e.add("Spacing", getSpacing());
    if(isFillWidth()) e.add("FillWidth", true);
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
    if(anElement.hasAttribute("Spacing")) setSpacing(anElement.getAttributeFloatValue("Spacing"));
    if(anElement.hasAttribute("FillWidth")) setFillWidth(anElement.getAttributeBoolValue("FillWidth"));
}

}