/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.props.PropSet;
import snap.util.*;

/**
 * A View subclass to layout child views horizontally, from left to right.
 */
public class RowView extends ChildView {
    
    // Whether to fill to height
    private boolean _fillHeight;

    // Whether to wrap closely around children and project their margins
    private boolean _hugging;
    
    // Constants for properties
    public static final String FillHeight_Prop = "FillHeight";
    public static final String Hugging_Prop = "Hugging";

    /**
     * Constructor.
     */
    public RowView()
    {
        super();
    }

    /**
     * Returns whether children will be resized to fill height.
     */
    public boolean isFillHeight()  { return _fillHeight; }

    /**
     * Sets whether children will be resized to fill height.
     */
    public void setFillHeight(boolean aValue)
    {
        if (aValue == _fillHeight) return;
        firePropChange(FillHeight_Prop, _fillHeight, _fillHeight = aValue);
        relayout();
    }

    /**
     * Returns whether to wrap closely around children and project their margins.
     */
    public boolean isHugging()  { return _hugging; }

    /**
     * Sets whether to wrap closely around children and project their margins.
     */
    public void setHugging(boolean aValue)
    {
        if (aValue == _hugging) return;
        firePropChange(Hugging_Prop, _hugging, _hugging = aValue);
        relayout();
    }

    /**
     * Override to return row layout.
     */
    @Override
    protected RowViewProxy<?> getViewProxyImpl()  { return new RowViewProxy<>(this); }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // FillHeight, Hugging
        aPropSet.addPropNamed(FillHeight_Prop, boolean.class, false);
        aPropSet.addPropNamed(Hugging_Prop, boolean.class, false);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        return switch (aPropName) {

            // FillHeight, Hugging
            case FillHeight_Prop -> isFillHeight();
            case Hugging_Prop -> isFillHeight();

            // Do normal version
            default -> super.getPropValue(aPropName);
        };
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // FillHeight, Hugging
            case FillHeight_Prop -> setFillHeight(Convert.boolValue(aValue));
            case Hugging_Prop -> setFillHeight(Convert.boolValue(aValue));

            // Do normal version
            default -> super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive FillHeight, Hugging
        if (isFillHeight()) e.add(FillHeight_Prop, true);
        if (isHugging()) e.add(Hugging_Prop, true);
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive FillHeight, Hugging
        if (anElement.hasAttribute(FillHeight_Prop))
            setFillHeight(anElement.getAttributeBoolValue(FillHeight_Prop, false));
        if (anElement.hasAttribute(Hugging_Prop))
            setHugging(anElement.getAttributeBoolValue(Hugging_Prop, false));
    }

    /**
     * Returns preferred width of given parent using RowView layout.
     */
    public static double getPrefWidth(View aParent, double aH)
    {
        RowViewProxy<?> viewProxy = new RowViewProxy<>(aParent);
        return viewProxy.getPrefWidth(aH);
    }

    /**
     * Returns preferred height of given parent using RowView layout.
     */
    public static double getPrefHeight(View aParent, double aW)
    {
        RowViewProxy<?> viewProxy = new RowViewProxy<>(aParent);
        return viewProxy.getPrefHeight(aW);
    }

    /**
     * Performs layout for given parent with given children.
     */
    public static void layout(ParentView aParent, boolean isFillHeight)
    {
        // If no children, just return
        if (aParent.getChildrenManaged().length == 0) return;

        // Get Parent ColViewProxy and layout views
        RowViewProxy<?> viewProxy = new RowViewProxy<>(aParent);
        viewProxy.setFillHeight(isFillHeight);
        viewProxy.layoutView();
    }
}