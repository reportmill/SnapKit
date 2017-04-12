/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Pos;
import snap.util.*;

/**
 * A custom class.
 */
public class HBox extends ChildView {
    
    // The HBox layout
    ViewLayout.HBoxLayout  _layout = new ViewLayout.HBoxLayout(this);
    
/**
 * Returns the spacing.
 */
public double getSpacing()  { return _layout.getSpacing(); }

/**
 * Sets the spacing.
 */
public void setSpacing(double aValue)  { _layout.setSpacing(aValue); }

/**
 * Returns whether children will be resized to fill height.
 */
public boolean isFillHeight()  { return _layout.isFillHeight(); }

/**
 * Sets whether children will be resized to fill height.
 */
public void setFillHeight(boolean aValue)  { _layout.setFillHeight(aValue); }

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

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
protected void layoutImpl()  { _layout.layoutChildren(); }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Spacing, FillHeight
    if(getSpacing()!=0) e.add("Spacing", getSpacing());
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

    // Unarchive Spacing, FillHeight
    setSpacing(anElement.getAttributeFloatValue("Spacing", 0));
    setFillHeight(anElement.getAttributeBoolValue("FillHeight", false));
}

}