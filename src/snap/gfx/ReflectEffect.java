/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.props.PropSet;
import snap.util.*;

/**
 * An Effect subclass to render a reflection.
 */
public class ReflectEffect extends Effect {

    // The height of the reflected image as fraction of shape height (defaults to 1)
    private double  _refHeight;

    // The height of the faded region as a fraction of reflection height (defaults to .5)
    private double  _fadeHeight;
    
    // The height of the gap between the shape and the reflection in points (defaults to 0)
    private double  _gap;

    // Constants for properties
    public static final String ReflectHeight_Prop = "ReflectHeight";
    public static final String FadeHeight_Prop = "FadeHeight";
    public static final String Gap_Prop = "Gap";

    // Constants for defaults
    public static final double DEFAULT_REFLECT_HEIGHT = 1d;
    public static final double DEFAULT_FADE_HEIGHT = .5d;
    public static final double DEFAULT_GAP = 0d;

    /**
     * Constructor.
     */
    public ReflectEffect()
    {
        _refHeight = DEFAULT_REFLECT_HEIGHT;
        _fadeHeight = DEFAULT_FADE_HEIGHT;
        _gap = DEFAULT_GAP;
    }

    /**
     * Constructor for given reflect height, fade height and gap.
     */
    public ReflectEffect(double aRefHt, double aFadeHt, double aGap)
    {
        _refHeight = aRefHt;
        _fadeHeight = aFadeHt;
        _gap = aGap;
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
        double extraH = getGap() + bounds.height * getReflectHeight() * getFadeHeight();
        bounds.height += extraH;
        return bounds;
    }

    /**
     * Performs the ReflectEffect with given PainterDVR.
     */
    public void applyEffect(PainterDVR dvrPntr, Painter aPntr, Rect aRect)
    {
        // If valid reflection and fade heights, get reflection image for shape and draw at offset
        if (getReflectHeight() > 0 && getFadeHeight() > 0) {
            Image reflectImage = getReflectImage(dvrPntr, aRect);
            double drawX = aRect.x;
            double drawY = aRect.getMaxY() + getGap();
            double drawW = reflectImage.getWidth();
            double drawH = reflectImage.getHeight();
            aPntr.drawImage(reflectImage, drawX, drawY, drawW, drawH);
        }

        // Do normal effect paint
        dvrPntr.exec(aPntr);
    }

    /**
     * Returns the effect image.
     */
    public Image getReflectImage(PainterDVR aPDVR, Rect aRect)
    {
        // Get shape image width and height
        double refHeight = getReflectHeight();
        double fadeHeight = getFadeHeight();
        int width = (int) Math.round(aRect.width);
        int height = (int) Math.ceil(aRect.height * refHeight * fadeHeight);

        // Get original graphics as flipped image
        Image img = Image.getImageForSize(width, height, true);
        Painter pntr = img.getPainter();
        pntr.setImageQuality(1);
        pntr.clipRect(0, 0, width, height);
        pntr.save();
        pntr.scale(1,-refHeight);
        pntr.translate(-aRect.x, -aRect.getMaxY());
        aPDVR.exec(pntr);
        pntr.restore();

        // Create gradient paint to fade image out
        Color c1 = new Color(1d,.5);
        Color c2 = new Color(1d, 0d);
        GradientPaint.Stop[] stops = GradientPaint.getStops(0, c1, 1, c2);
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
    public ReflectEffect copyForReflectHeight(double reflectHeight)  { return new ReflectEffect(reflectHeight, _fadeHeight, _gap); }

    /**
     * Returns a reflection for given fade height.
     */
    public ReflectEffect copyForFadeHeight(double fadeHeight)  { return new ReflectEffect(_refHeight, fadeHeight, _gap); }

    /**
     * Returns a reflection for given gap.
     */
    public ReflectEffect copyForGap(double aGap)  { return new ReflectEffect(_refHeight, _fadeHeight, aGap); }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        ReflectEffect other = anObj instanceof ReflectEffect ? (ReflectEffect) anObj : null;
        if (other == null) return false;
        if (other._refHeight != _refHeight) return false;
        if (other._fadeHeight != _fadeHeight) return false;
        if (other._gap != _gap) return false;
        return true;
    }

    /**
     * Override to configure props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // ReflectHeight, FadeHeight, Gap
        aPropSet.addPropNamed(ReflectHeight_Prop, double.class, DEFAULT_REFLECT_HEIGHT);
        aPropSet.addPropNamed(FadeHeight_Prop, double.class, DEFAULT_FADE_HEIGHT);
        aPropSet.addPropNamed(Gap_Prop, double.class, DEFAULT_GAP);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        return switch (aPropName) {

            // ReflectHeight, FadeHeight, Gap
            case ReflectHeight_Prop -> getReflectHeight();
            case FadeHeight_Prop -> getFadeHeight();
            case Gap_Prop -> getGap();

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

            // ReflectHeight, FadeHeight, Gap
            case ReflectHeight_Prop: setReflectHeight(Convert.doubleValue(aValue)); break;
            case FadeHeight_Prop: setFadeHeight(Convert.doubleValue(aValue)); break;
            case Gap_Prop: setGap(Convert.doubleValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Returns a string encoding of this effect.
     */
    @Override
    public String codeString()
    {
        String sb = "reflect" + '(' +
                FormatUtils.formatNum(getReflectHeight()) + ' ' +
                FormatUtils.formatNum(getFadeHeight()) + ' ' +
                FormatUtils.formatNum(getGap()) + ')';
        return sb;
    }

    /**
     * Returns a reflect effect for given coded string.
     */
    public static ReflectEffect of(Object anObj)
    {
        if (anObj == null || anObj instanceof ReflectEffect)
            return (ReflectEffect) anObj;

        // Get string
        String str = anObj.toString().toLowerCase().replace("reflect", "");
        str = str.replace('(', ' ').replace(')', ' ').trim();

        // Get parts
        String[] parts = str.split("\\s");
        if (parts.length < 3)
            return new ReflectEffect();

        double reflectHeight = Convert.doubleValue(parts[0]);
        double fadeHeight = Convert.doubleValue(parts[1]);
        double gap = Convert.doubleValue(parts[2]);
        return new ReflectEffect(reflectHeight, fadeHeight, gap);
    }
}