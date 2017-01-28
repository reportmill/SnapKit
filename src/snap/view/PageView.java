/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
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
    setEffect(new ShadowEffect());
}

/**
 * Override to return white.
 */
public Paint getDefaultFill()  { return Color.WHITE; }

/**
 * Returns the default border.
 */
public Border getDefaultBorder()  { return PAGE_VIEW_BORDER; }

}