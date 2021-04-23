/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.util.*;

/**
 * An effect that performs a simple blur.
 */
public class BlurEffect extends Effect {

    // The radius
    private double  _radius = 5;

    /**
     * Creates a new BlurEffect.
     */
    public BlurEffect()  { }

    /**
     * Creates a new BlurEffect for given radius.
     */
    public BlurEffect(double aRadius)  { _radius = aRadius; }

    /**
     * Returns the radius.
     */
    public double getRadius()  { return _radius; }

    /**
     * Sets the radius.
     */
    public void setRadius(double aValue)  { _radius = aValue; }

    /**
     * Override to account for blur radius.
     */
    public Rect getBounds(Rect aRect)
    {
        Rect rect = aRect.getInsetRect(-getRadius());
        return rect;
    }

    /**
     * Apply the effect from given DVR to painter.
     */
    public void applyEffect(PainterDVR aPDVR, Painter aPntr, Rect aRect)
    {
        // If radius is less than 1, do default drawing and return
        int radius = (int) getRadius();
        if (radius < 1) {
            aPDVR.exec(aPntr);
            return;
        }

        // Get effect image for shape and draw at offset (blur effect draws as a complete replacement for shape drawing)
        Image img = getBlurImage(aPDVR, aRect);
        aPntr.drawImage(img, -radius*2, -radius*2, img.getWidth(), img.getHeight());
    }

    /**
     * Returns the blur image.
     */
    public Image getBlurImage(PainterDVR aPDVR, Rect aRect)
    {
        int radius = (int)getRadius();
        Image bimg = aPDVR.getImage(aRect, radius*2);
        bimg.blur(radius, null);
        return bimg;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        BlurEffect other = anObj instanceof BlurEffect ? (BlurEffect)anObj : null; if(other==null) return false;
        if (other._radius != _radius) return false;
        return true;
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = super.toXML(anArchiver);
        e.add("type", "blur");
        e.add("radius", _radius);
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXML(anArchiver, anElement);
        _radius = anElement.getAttributeIntValue("radius");
        return this;
    }
}