/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.gfx.*;
import snap.props.PropSet;

/**
 * A View subclass to represent a DocView page.
 */
public class PageView extends ChildView {
    
    // Constants for property overrides
    private static final Color DEFAULT_PAGE_VIEW_FILL = Color.WHITE;
    private static final Border DEFAULT_PAGE_VIEW_BORDER = Border.createLineBorder(Color.BLACK, 1);

    /**
     * Constructor.
     */
    public PageView()
    {
        super();
        _fill = DEFAULT_PAGE_VIEW_FILL;
        _border = DEFAULT_PAGE_VIEW_BORDER;
        setEffect(new ShadowEffect().copySimple());
    }

    /**
     * Returns the document.
     */
    public DocView getDoc()
    {
        return getParent(DocView.class);
    }

    /**
     * Returns the margin rect.
     */
    public Rect getMarginRect()
    {
        Insets ins = getDoc()!=null ? getDoc().getPageMargin() : null;
        if (ins == null) return null;
        Rect marg = getBoundsLocal();
        marg.x += ins.left; marg.width -= ins.getWidth();
        marg.y += ins.top; marg.height -= ins.getHeight();
        return marg;
    }

    /**
     * Override to paint page margin.
     */
    protected void paintBack(Painter aPntr)
    {
        super.paintBack(aPntr);
        if(aPntr.isPrinting() || getDoc() == null) return;
        Rect marg = getMarginRect(); if(marg == null) return;
        aPntr.setColor(Color.LIGHTGRAY);
        aPntr.setStroke(Stroke.Stroke1);
        aPntr.draw(marg);
    }

    /**
     * Override to customize for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Reset defaults
        aPropSet.getPropForName(Fill_Prop).setDefaultValue(DEFAULT_PAGE_VIEW_FILL);
        aPropSet.getPropForName(Border_Prop).setDefaultValue(DEFAULT_PAGE_VIEW_BORDER);
    }
}