/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.util.*;

/**
 * An Effect subclass to render a reflection.
 */
public class ReflectEffect extends Effect {

    // The height of the reflected image as fraction of shape height (defaults to 1)
    private double  _refHeight = 1;

    // The height of the faded region as a fraction of reflection height (defaults to .5)
    private double  _fadeHeight = .5f;
    
    // The height of the gap between the shape and the reflection in points (defaults to 0)
    private double  _gap = 0;

    /**
     * Creates a new ReflectEffect.
     */
    public ReflectEffect()  { }

    /**
     * Creates a new ReflectEffect for given reflect height, fade height and gap.
     */
    public ReflectEffect(double aRefHt, double aFadeHt, double aGap)
    {
        _refHeight = aRefHt; _fadeHeight = aFadeHt; _gap = aGap;
    }

    /**
     * Returns the height of the reflected image as fraction of shape height (defaults to 1).
     */
    public double getReflectHeight()  { return _refHeight; }

    /**
     * Sets the height of the reflected image as fraction of shape height.
     */
    protected void setReflectHeight(double aValue)  { _refHeight = aValue; }

    /**
     * Returns the height of the faded region as a fraction of reflection height (defaults to .5).
     */
    public double getFadeHeight()  { return _fadeHeight; }

    /**
     * Sets the height of the faded region as a fraction of reflection height.
     */
    protected void setFadeHeight(double aValue)  { _fadeHeight = aValue; }

    /**
     * Returns the height of the gap between the shape and the reflection in points (defaults to 0).
     */
    public double getGap()  { return _gap; }

    /**
     * Sets the height of the gap between the shape and the reflection in points.
     */
    protected void setGap(double aValue)  { _gap = aValue; }

    /**
     * Override to extend height by gap and reflection fade height.
     */
    public Rect getBounds(Rect aRect)
    {
        Rect bounds = aRect.clone();
        bounds.height = bounds.height + getGap() + bounds.height*getReflectHeight()*getFadeHeight();
        return bounds;
    }

    /**
     * Performs the ReflectEffect with given PainterDVR.
     */
    public void applyEffect(PainterDVR aPDVR, Painter aPntr, Rect aRect)
    {
        // If valid reflection and fade heights, get reflection image for shape and draw at offset
        if (getReflectHeight()>0 && getFadeHeight()>0) {
            Image img = getReflectImage(aPDVR, aRect);
            aPntr.drawImage(img, aRect.getX(), aRect.getMaxY()+getGap(), img.getWidth(), img.getHeight());
        }

        // Do normal effect paint
        aPDVR.exec(aPntr);
    }

    /**
     * Returns the effect image.
     */
    public Image getReflectImage(PainterDVR aPDVR, Rect aRect)
    {
        // Get shape image width and height
        double refHeight = getReflectHeight();
        double fadeHeight = getFadeHeight();
        int width = (int) Math.round(aRect.getWidth());
        int height = (int) Math.ceil(aRect.getHeight()*refHeight*fadeHeight);

        // Get original graphics as flipped image
        Image img = Image.get(width, height, true);
        Painter pntr = img.getPainter();
        pntr.setImageQuality(1);
        pntr.clipRect(0, 0, width, height);
        pntr.save();
        pntr.scale(1,-refHeight);
        pntr.translate(-aRect.getX(), -aRect.getMaxY());
        aPDVR.exec(pntr);
        pntr.restore();

        // Create gradient paint to fade image out
        Color c1 = new Color(1d,.5);
        Color c2 = new Color(1d, 0d);
        GradientPaint.Stop stops[] = GradientPaint.getStops(0, c1, 1, c2);
        GradientPaint mask = new GradientPaint(0, 0, 0, 1, stops);

        // Set composite to change mask colors to gradient and return image
        pntr.setComposite(Painter.Composite.DST_IN);
        pntr.setPaint(mask);
        pntr.fillRect(0, 0, width, height);
        return img;
    }

    /**
     * Returns a reflection for given reflect height.
     */
    public ReflectEffect copyForReflectHeight(double aRH)
    {
        return new ReflectEffect(aRH, _fadeHeight, _gap);
    }

    /**
     * Returns a reflection for given fade height.
     */
    public ReflectEffect copyForFadeHeight(double aFH)
    {
        return new ReflectEffect(_refHeight, aFH, _gap);
    }

    /**
     * Returns a reflection for given gap.
     */
    public ReflectEffect copyForGap(double aGap)
    {
        return new ReflectEffect(_refHeight, _fadeHeight, aGap);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        ReflectEffect other = anObj instanceof ReflectEffect ? (ReflectEffect)anObj :null; if (other==null) return false;
        if (other._refHeight!=_refHeight) return false;
        if (other._fadeHeight!=_fadeHeight) return false;
        if (other._gap!=_gap) return false;
        return true;
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = super.toXML(anArchiver); e.add("type", "reflection");       // Archive basic attributes and set type
        if (getReflectHeight()!=.5) e.add("reflection-height", getReflectHeight()); // Archive ReflectHeight
        if (getFadeHeight()!=1) e.add("fade-height", getFadeHeight());              // Archive FadeHeight, Gap
        if (getGap()!=0) e.add("gap-height", getGap());
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive ReflectHeight, FadeHeight, Gap
        if (anElement.hasAttribute("reflection-height"))
            setReflectHeight(anElement.getAttributeFloatValue("reflection-height"));
        if (anElement.hasAttribute("fade-height")) setFadeHeight(anElement.getAttributeFloatValue("fade-height"));
        if (anElement.hasAttribute("gap-height")) setGap(anElement.getAttributeFloatValue("gap-height"));
        return this;
    }
}