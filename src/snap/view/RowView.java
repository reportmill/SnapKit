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
    protected ViewLayout getViewLayoutImpl()  { return new RowViewLayout(this); }

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
            case Hugging_Prop -> isHugging();

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
            case Hugging_Prop -> setHugging(Convert.boolValue(aValue));

            // Do normal version
            default -> super.setPropValue(aPropName, aValue);
        }
    }
}