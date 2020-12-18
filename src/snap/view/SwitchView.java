/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.util.*;

/**
 * A pane to show a specific child pane from a list.
 */
public class SwitchView extends ChildView implements Selectable {

    // The selected index
    private int _selIndex;

    /**
     * Returns the view with the given name.
     */
    public View getPane(String aName)
    {
        for (View child : getChildren()) if (aName.equals(child.getName())) return child;
        return null; // Return null since pane not found
    }

    /**
     * Returns the SwitchView's selected index.
     */
    public int getSelIndex()  { return _selIndex; }

    /**
     * Sets the SwitchView's selected index.
     */
    public void setSelIndex(int anIndex)
    {
        firePropChange(SelIndex_Prop, _selIndex, _selIndex =anIndex);
        relayout(); relayoutParent(); repaint();
    }

    /**
     * Returns the currently visible view.
     */
    public View getSelectedPane()
    {
        return _selIndex >=0 && _selIndex <getChildCount() ? getChild(_selIndex) : null;
    }

    /**
     * Sets the given view as the selected view.
     */
    public void setSelectedPane(View aPane)
    {
        for (int i=0, iMax=getChildCount(); i<iMax; i++)
            if (getChild(i)==aPane) setSelIndex(i);
    }

    /**
     * Returns the selected name.
     */
    public String getSelectedName()
    {
        int index = getSelIndex();
        return index<0 ? null : getChild(index).getName();
    }

    /**
     * Sets the selected pane to the first with the given name.
     */
    public void setSelectedName(String aName)
    {
        int index = -1;
        for (int i=0, iMax=getChildCount(); i<iMax && index<0; i++)
            if (aName.equals(getChild(i).getName()))
                index = i;
        setSelIndex(index);
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return SelIndex_Prop; }

    /**
     * Override to return preferred width of content.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        View child = getSelectedPane();
        double childW = child!=null ? child.getPrefWidth() : 0;
        return childW + ins.getWidth();
    }

    /**
     * Override to return preferred height of content.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        View child = getSelectedPane();
        double childH = child!=null ? child.getPrefHeight() : 0;
        return childH + ins.getHeight();
    }

    /**
     * Override to layout content.
     */
    protected void layoutImpl()
    {
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        for (int i=0, iMax=getChildCount(); i<iMax; i++) { View child = getChild(i);
            child.setBounds(areaX, areaY, areaW, areaH);
            child.setVisible(i == getSelIndex());
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);
        if (getSelIndex()>0) e.add(SelIndex_Prop, getSelIndex());
        return e;
    }

    /**
     * XML unarchival for children.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);
        setSelIndex(anElement.getAttributeIntValue(SelIndex_Prop, 0));
    }
}