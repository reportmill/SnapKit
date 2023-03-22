/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Pos;
import snap.props.PropSet;
import snap.util.*;

/**
 * A View subclass to layout child views vertically, from top to bottom.
 */
public class ColView extends ChildView {

    // Whether to fill to with
    private boolean  _fillWidth;
    
    // Constants for properties
    public static final String FillWidth_Prop = "FillWidth";

    // Constants for property defaults
    private static final boolean DEFAULT_COL_VIEW_VERTICAL = true;

    /**
     * Constructor.
     */
    public ColView()
    {
        super();
    }

    /**
     * Returns whether children will be resized to fill width.
     */
    public boolean isFillWidth()  { return _fillWidth; }

    /**
     * Sets whether children will be resized to fill width.
     */
    public void setFillWidth(boolean aValue)
    {
        if (aValue == _fillWidth) return;
        firePropChange(FillWidth_Prop, _fillWidth, _fillWidth = aValue);
        relayout();
    }

    /**
     * Returns the default alignment.
     */
    public Pos getDefaultAlign()  { return Pos.TOP_LEFT; }

    /**
     * Returns the preferred width.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        ColViewProxy<?> viewProxy = getViewProxy();
        return viewProxy.getPrefWidth(aH);
    }

    /**
     * Returns the preferred height.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        ColViewProxy<?> viewProxy = getViewProxy();
        return viewProxy.getPrefHeight(aW);
    }

    /**
     * Layout children.
     */
    @Override
    protected void layoutImpl()
    {
        ColViewProxy<?> viewProxy = getViewProxy();
        viewProxy.layoutView();
    }

    /**
     * Override to return ColViewProxy.
     */
    @Override
    protected ColViewProxy<?> getViewProxy()
    {
        return new ColViewProxy<>(this);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // FillWidth
        aPropSet.addPropNamed(FillWidth_Prop, boolean.class, false);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // FillWidth
            case FillWidth_Prop: return isFillWidth();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // FillWidth
            case FillWidth_Prop: setFillWidth(Convert.boolValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Override for custom defaults.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Vertical
        if (aPropName == Vertical_Prop)
            return DEFAULT_COL_VIEW_VERTICAL;

        // Do normal version
        return super.getPropDefault(aPropName);
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive FillWidth
        if (isFillWidth())
            e.add(FillWidth_Prop, true);
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive FillWidth
        if (anElement.hasAttribute(FillWidth_Prop))
            setFillWidth(anElement.getAttributeBoolValue(FillWidth_Prop));
    }

    /**
     * Returns preferred width of given parent with given children.
     */
    public static double getPrefWidth(ParentView aPar, double aH)
    {
        ColViewProxy<?> viewProxy = new ColViewProxy<>(aPar);
        return viewProxy.getPrefWidth(aH);
    }

    /**
     * Returns preferred height of given parent with given children.
     */
    public static double getPrefHeight(ParentView aParent, double aW)
    {
        ColViewProxy<?> viewProxy = new ColViewProxy<>(aParent);
        return viewProxy.getPrefHeight(aW);
    }

    /**
     * Performs layout for given parent with option to fill width.
     */
    public static void layout(ParentView aParent, boolean isFillWidth)
    {
        // Get layout children (just return if none)
        if (aParent.getChildrenManaged().length == 0) return;

        // Get Parent ColViewProxy and layout views
        ColViewProxy<?> viewProxy = new ColViewProxy<>(aParent);
        viewProxy.setFillWidth(isFillWidth);
        viewProxy.layoutView();
    }
}