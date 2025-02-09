/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.props.PropSet;
import snap.util.*;

/**
 * An effect that performs a simple blur.
 */
public class BlurEffect extends Effect {

    // The radius
    private double  _radius;

    // Constants for properties
    public static final String Radius_Prop = "Radius";

    // Constants for defaults
    public static final double DEFAULT_RADIUS = 5;

    /**
     * Constructor.
     */
    public BlurEffect()
    {
        _radius = DEFAULT_RADIUS;
    }

    /**
     * Constructor for given radius.
     */
    public BlurEffect(double aRadius)  { _radius = aRadius; }

    /**
     * Returns the radius.
     */
    public double getRadius()  { return _radius; }

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
        Image blurImage = getBlurImage(aPDVR, aRect);
        aPntr.drawImage(blurImage, -radius * 2, -radius * 2, blurImage.getWidth(), blurImage.getHeight());
    }

    /**
     * Returns the blur image.
     */
    public Image getBlurImage(PainterDVR aPDVR, Rect aRect)
    {
        int radius = (int) getRadius();
        Image blurImage = aPDVR.getImage(aRect, radius*2);
        blurImage.blur(radius, null);
        return blurImage;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        BlurEffect other = anObj instanceof BlurEffect ? (BlurEffect) anObj : null;
        if(other == null) return false;
        if (other._radius != _radius) return false;
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

        // Radius
        aPropSet.addPropNamed(Radius_Prop, double.class, DEFAULT_RADIUS);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Radius
        if (aPropName.equals(Radius_Prop))
            return getRadius();

        // Do normal version
        return super.getPropValue(aPropName);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Radius
        if (aPropName.equals(Radius_Prop))
            _radius = Convert.doubleValue(aValue);

        // Do normal version
        else super.setPropValue(aPropName, aValue);
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = super.toXML(anArchiver);
        if (!isPropDefault(Radius_Prop)) e.add(Radius_Prop, _radius);
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXML(anArchiver, anElement);
        if (anElement.hasAttribute(Radius_Prop)) _radius = anElement.getAttributeIntValue(Radius_Prop);
        return this;
    }

    /**
     * Returns a string encoding of this effect.
     */
    @Override
    public String codeString()
    {
        return "blur" + '(' + FormatUtils.formatNum(getRadius()) + ')';
    }

    /**
     * Returns a blur effect for given object (coded string).
     */
    public static BlurEffect of(Object anObj)
    {
        if (anObj == null || anObj instanceof BlurEffect)
            return (BlurEffect) anObj;

        // Get string
        String str = anObj.toString().toLowerCase().replace("blur", "");
        str = str.replace('(', ' ').replace(')', ' ').trim();

        // Get parts
        String[] parts = str.split("\\s");
        if (parts.length < 1)
            return new BlurEffect();

        double radius = Convert.doubleValue(parts[0]);
        return new BlurEffect(radius);
    }
}