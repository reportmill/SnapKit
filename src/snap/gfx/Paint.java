/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;

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
    public Paint copyFor(Rect aRect);
    
    /**
     * Returns the name for paint.
     */
    default String getName()  { return getClass().getSimpleName(); }

}