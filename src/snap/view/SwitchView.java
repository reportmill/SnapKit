/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Insets;
import snap.util.*;

/**
 * A pane to show a specific child pane from a list.
 */
public class SwitchView extends ChildView implements View.Selectable {

    // The selected index
    int        _sindex;

/**
 * Returns the view with the given name.
 */
public View getPane(String aName)
{
    for(View child : getChildren()) if(aName.equals(child.getName())) return child;
    return null; // Return null since pane not found
}

/**
 * Returns the SwitchView's selected index.
 */
public int getSelectedIndex()  { return _sindex; }

/**
 * Sets the SwitchView's selected index.
 */
public void setSelectedIndex(int anIndex)
{
    firePropChange(SelectedIndex_Prop, _sindex, _sindex=anIndex);
    relayout(); relayoutParent(); repaint();
}

/**
 * Returns the currently visible view.
 */
public View getSelectedPane()
{
    return _sindex>=0 && _sindex<getChildCount()? getChild(_sindex) : null;
}

/**
 * Sets the given view as the selected view.
 */
public void setSelectedPane(View aPane)
{
    for(int i=0, iMax=getChildCount(); i<iMax; i++)
        if(getChild(i)==aPane) setSelectedIndex(i);
}

/**
 * Returns the selected name.
 */
public String getSelectedName()
{
    int index = getSelectedIndex();
    return index<0? null : getChild(index).getName();
}

/**
 * Sets the selected pane to the first with the given name.
 */
public void setSelectedName(String aName)
{
    int index = -1;
    for(int i=0, iMax=getChildCount(); i<iMax && index<0; i++)
        if(aName.equals(getChild(i).getName()))
            index = i;
    setSelectedIndex(index);
}

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return SelectedIndex_Prop; }

/**
 * Override to return preferred width of content.
 */
protected double getPrefWidthImpl(double aH)
{
    View c = getSelectedPane(); double cw = c!=null? c.getPrefWidth() : 0;
    Insets pad = getInsetsAll(); double pl = pad.getLeft(), pr = pad.getRight();
    return cw + pl + pr;
}

/**
 * Override to return preferred height of content.
 */
protected double getPrefHeightImpl(double aW)
{
    View c = getSelectedPane(); double ch = c!=null? c.getPrefHeight() : 0;
    Insets pad = getInsetsAll(); double pt = pad.getTop(), pb = pad.getBottom();
    return ch + pt + pb;
}

/**
 * Override to layout content.
 */
protected void layoutImpl()
{
    Insets ins = getInsetsAll();
    double tp = ins.top, rt = ins.right, bt = ins.bottom, lt = ins.left;
    double w = getWidth() - lt - rt, h = getHeight() - tp - bt;
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { View child = getChild(i);
        child.setBounds(lt, tp, w, h); child.setVisible(i==_sindex); child.setPickable(i==_sindex); }
}

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    if(getSelectedIndex()>0) e.add(SelectedIndex_Prop, getSelectedIndex());
    return e;
}

/**
 * XML unarchival for children.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    setSelectedIndex(anElement.getAttributeIntValue(SelectedIndex_Prop, 0));
}

}