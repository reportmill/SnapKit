/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Size;

/**
 * A BoxView subclass that scales the content instead of resize.
 */
public class ScaleBox extends BoxView {

    // Whether to preserve natural aspect when scaling
    private boolean  _keepAspect;

    // Constants for properties
    public static final String KeepAspect_Prop = "KeepAspect";

    /**
     * Creates a new ScaleBox.
     */
    public ScaleBox()  { }

    /**
     * Creates a new ScaleBox for content.
     */
    public ScaleBox(View aContent)
    {
        this();
        setContent(aContent);
    }

    /**
     * Creates a new ScaleBox for content with FillWidth, FillHeight params.
     */
    public ScaleBox(View aContent, boolean isFillWidth, boolean isFillHeight)
    {
        this(aContent);
        setFillWidth(isFillWidth);
        setFillHeight(isFillHeight);
    }

    /**
     * Returns whether to preserve natural aspect of content when scaling.
     */
    public boolean isKeepAspect()  { return _keepAspect; }

    /**
     * Sets whether to preserve natural aspect of content when scaling.
     */
    public void setKeepAspect(boolean aValue)
    {
        if (aValue == isKeepAspect()) return;
        firePropChange(KeepAspect_Prop, _keepAspect, _keepAspect = aValue);
        relayout();
    }

    /**
     * Returns the aspect of the content.
     */
    protected double getAspect()
    {
        View child = getContent(); if (child == null) return 1;
        Size bestSize = child.getBestSize();
        return bestSize.width / bestSize.height;
    }

    /**
     * Override to return ScaleBoxProxy.
     */
    @Override
    protected ScaleBoxProxy getViewProxy()
    {
        return new ScaleBoxProxy(this);
    }
}