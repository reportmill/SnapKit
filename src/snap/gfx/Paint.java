/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;

import snap.geom.Rect;

/**
 * A class to represent a fill for a Shape or text (Color, GradientPaint, ImagePaint).
 */
public interface Paint {

    /**
     * Returns whether paint is defined in terms independent of primitive to be filled.
     */
    public boolean isAbsolute();
    
    /**
     * Returns whether paint is opaque.
     */
    public boolean isOpaque();
    
    /**
     * Returns an absolute paint for given bounds of primitive to be filled.
     */
    public Paint copyForRect(Rect aRect);
    
    /**
     * Returns the name for paint.
     */
    default String getName()  { return getClass().getSimpleName(); }

    /**
     * Returns the closest color approximation of this paint.
     */
    public Color getColor();

    /**
     * Returns a copy of this paint modified for given color.
     */
    public Paint copyForColor(Color aColor);

    /**
     * Returns the snap version of this fill.
     */
    default Paint snap()  { return this; }
}