/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.gfx.*;

/**
 * A View subclass to represent a DocView page.
 */
public class PageView extends ChildView {
    
    // Constant
    static final Border PAGE_VIEW_BORDER = Border.createLineBorder(Color.BLACK, 1);

    /**
     * Creates a new PageView.
     */
    public PageView()
    {
        setFill(Color.WHITE);
        setBorder(PAGE_VIEW_BORDER);
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
     * Override to return white.
     */
    public Paint getDefaultFill()  { return Color.WHITE; }

    /**
     * Returns the default border.
     */
    public Border getDefaultBorder()  { return PAGE_VIEW_BORDER; }

    /**
     * Override to page page margin.
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
}