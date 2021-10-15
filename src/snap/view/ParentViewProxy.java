/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

import snap.util.MathUtils;

/**
 * A ViewProxy that can layout content in the manner of BoxView for any View.
 */
public abstract class ParentViewProxy<T extends View> extends ViewProxy<T> {

    /**
     * Constructor for given parent view.
     */
    public ParentViewProxy(View aParent)
    {
        super(aParent);
    }

    /**
     * Returns the best width for view - accounting for pref/min/max.
     */
    public double getBestWidth(double aH)
    {
        double prefW = getPrefWidth(aH);

        View view = getView();
        if (view != null) {
            double minW = view.getMinWidth();
            double maxW = view.getMaxWidth();
            prefW = MathUtils.clamp(prefW, minW, maxW);
        }

        return prefW;
    }

    /**
     * Returns the best height for view - accounting for pref/min/max.
     */
    public double getBestHeight(double aW)
    {
        double prefH = getPrefHeight(aW);

        View view = getView();
        if (view != null) {
            double minH = view.getMinHeight();
            double maxH = view.getMaxHeight();
            prefH = MathUtils.clamp(prefH, minH, maxH);
        }

        return prefH;
    }

    /**
     * Returns preferred width of layout.
     */
    public double getPrefWidth(double aH)
    {
        setSize(-1, aH);
        layoutProxy();
        double prefW = getPrefWidthImpl(aH);
        return prefW;
    }

    /**
     * Returns preferred height of layout.
     */
    public double getPrefHeight(double aW)
    {
        setSize(aW, -1);
        layoutProxy();
        double prefH = getPrefHeightImpl(aW);
        return prefH;
    }

    /**
     * Performs BoxView layout.
     */
    public void layoutView()
    {
        // Layout
        layoutProxy();

        // Apply bounds
        setBoundsInClient();
    }

    /**
     * Returns preferred width of layout.
     */
    protected abstract double getPrefWidthImpl(double aH);

    /**
     * Returns preferred height of layout.
     */
    protected abstract double getPrefHeightImpl(double aW);

    /**
     * Performs Box layout for given parent, child and fill width/height.
     */
    public abstract void layoutProxy();
}