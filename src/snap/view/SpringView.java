/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.util.*;

/**
 * A View subclass that lays out children using auto sizing settings.
 */
public class SpringView extends ChildView {

    // The HBox layout
    ViewLayout.SpringLayout  _layout = new ViewLayout.SpringLayout(this);
    
    // A PropChangeListener to resetSpringInfo when child bounds change outside of layout
    PropChangeListener       _pcl = pce -> childPropChange(pce);
    
/**
 * Override to add layout info.
 */
public void addChild(View aChild, int anIndex)
{
    super.addChild(aChild, anIndex);
    _layout.addSpringInfo(aChild);
    aChild.addPropChangeListener(_pcl);
}

/**
 * Override to remove layout info.
 */
public View removeChild(int anIndex)
{
    View child = super.removeChild(anIndex);
    _layout.removeSpringInfo(child);
    child.removePropChangeListener(_pcl);
    return child;
}

/**
 * Resets spring info for given child (or all children if null).
 */
public void resetSpringInfo(View aChild)
{
    if(aChild!=null)
        _layout.addSpringInfo(aChild);
    else for(View v : getChildren())
        _layout.addSpringInfo(v);
}

/**
 * Layout children.
 */
protected void layoutImpl()  { _layout.layoutChildren(); }

/**
 * Called when child property changes.
 */
protected void childPropChange(PropChange aPCE)
{
    if(isInLayout()) return;
    String pname = aPCE.getPropertyName();
    if(pname==X_Prop || pname==Y_Prop || pname==Width_Prop || pname==Height_Prop)
        resetSpringInfo((View)aPCE.getSource());
}

/**
 * XML Archival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    setPrefSize(getWidth(), getHeight());
}

}