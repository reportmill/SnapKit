/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.props.PropSet;
import snap.util.*;

/**
 * An Effect subclass to represent a drop shadow.
 */
public class ShadowEffect extends Effect {

    // The shadow radius
    private double  _radius;
    
    // The shadow offset
    private double  _dx, _dy;
    
    // Fill color
    private Color  _color;
    
    // Whether effect is simple
    private boolean  _simple;

    // Constants for properties
    public static final String Radius_Prop = "Radius";
    public static final String DX_Prop = "DX";
    public static final String DY_Prop = "DY";
    public static final String Color_Prop = "Color";

    // Constants for defaults
    public static final double DEFAULT_RADIUS = 10d;
    public static final Color DEFAULT_COLOR = Color.BLACK;

    /**
     * Constructor.
     */
    public ShadowEffect()
    {
        _radius = DEFAULT_RADIUS;
        _color = DEFAULT_COLOR;
    }

    /**
     * Constructor for given radius, color and offset.
     */
    public ShadowEffect(double aRadius, Color aColor, double aDX, double aDY)
    {
        _radius = aRadius; _dx = aDX; _dy = aDY; _color = aColor;
    }

    /**
     * Returns the radius of the blur.
     */
    public double getRadius()  { return _radius; }

    /**
     * Returns the X offset of the shadow.
     */
    public double getDX()  { return _dx; }

    /**
     * Returns the Y offset of the shadow.
     */
    public double getDY()  { return _dy; }

    /**
     * Returns the color associated with this fill.
     */
    public Color getColor()  { return _color; }

    /**
     * Returns whether this shadow should just be rect.
     */
    public boolean isSimple()  { return _simple; }

    /**
     * Override to account for blur radius and shadow offset.
     */
    public Rect getBounds(Rect aRect)
    {
        Rect rect = aRect.getInsetRect(-getRadius());
        if (_dx != 0 || _dy != 0) {
            rect.offset(_dx, _dy);
            rect.union(aRect);
        }

        // Return
        return rect;
    }

    /**
     * Performs the ShadowEffect with given PainterDVR.
     */
    public void applyEffect(PainterDVR aPDVR, Painter aPntr, Rect aRect)
    {
        int radius = (int) getRadius();
        int dx = (int) getDX();
        int dy = (int) getDY();
        Image shadowImage = getShadowImage(aPDVR, aRect);
        double drawX = -radius * 2 + dx;
        double drawY = -radius * 2 + dy;
        aPntr.drawImage(shadowImage, drawX, drawY);

        // Draw contents of PainterDVR
        aPDVR.exec(aPntr);
    }

    /**
     * Performs the ShadowEffect with given PainterDVR.
     */
    public void applyEffectShadowOnly(PainterDVR aPDVR, Painter aPntr, Rect aRect)
    {
        int radius = (int) getRadius();
        int dx = (int) getDX();
        int dy = (int) getDY();
        Image shadowImage = getShadowImage(aPDVR, aRect);
        double drawX = -radius * 2 + dx;
        double drawY = -radius * 2 + dy;
        aPntr.drawImage(shadowImage, drawX, drawY);
    }

    /**
     * Returns the effect image.
     */
    public Image getShadowImage(PainterDVR aPDVR, Rect aRect)
    {
        // If Simple, return simple shadow image
        if (_simple)
            return getShadowImage(aRect, getRadius(), getColor());

        // If marked shape is rect and opaque, return simple shadow image
        if (aPDVR.getMarkedShape() instanceof Rect && aPDVR.isMarkedShapeOpaque())
            return getShadowImage(aPDVR.getMarkedShape().getBounds(), getRadius(), getColor());

        // Create new image for dvr
        int radius = (int) getRadius(); //if (radius>2) return getShadowImageSimple(aRect);
        Image shadowImage = aPDVR.getImage(aRect, radius * 2);

        // Blur image and return
        shadowImage.blur(radius, getColor());
        return shadowImage;
    }

    /**
     * Returns a copy of this shadow with given radius.
     */
    public ShadowEffect copyForRadius(double aRad)
    {
        return new ShadowEffect(aRad, _color, _dx, _dy);
    }

    /**
     * Returns a copy of this shadow with given offset.
     */
    public ShadowEffect copyForOffset(double aDX, double aDY)
    {
        return new ShadowEffect(_radius, _color, aDX, aDY);
    }

    /**
     * Returns a copy of this shadow with given color.
     */
    public ShadowEffect copyForColor(Color aColor)
    {
        return new ShadowEffect(_radius, aColor, _dx, _dy);
    }

    /**
     * Returns a copy of this shadow with given color.
     */
    public ShadowEffect copySimple()
    {
        ShadowEffect copy = new ShadowEffect(_radius, _color, _dx, _dy);
        copy._simple = true;
        return copy;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        ShadowEffect other = anObj instanceof ShadowEffect ? (ShadowEffect) anObj : null;
        if (other == null) return false;
        if (other._radius != _radius) return false;
        if (other._dx != _dx || other._dy != _dy) return false;
        if (other._color != _color) return false;
        if (other._simple != _simple) return false;
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

        // Radius, DX, DY, Color
        aPropSet.addPropNamed(Radius_Prop, double.class, DEFAULT_RADIUS);
        aPropSet.addPropNamed(DX_Prop, double.class, 0d);
        aPropSet.addPropNamed(DY_Prop, double.class, 0d);
        aPropSet.addPropNamed(Color_Prop, Color.class, DEFAULT_COLOR);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Radius, DX, DY, Color
            case Radius_Prop: return getRadius();
            case DX_Prop: return getDX();
            case DY_Prop: return getDY();
            case Color_Prop: return getColor();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Radius, DX, DY, Color
            case Radius_Prop: _radius = Convert.doubleValue(aValue); break;
            case DX_Prop: _dx = Convert.doubleValue(aValue); break;
            case DY_Prop: _dy = Convert.doubleValue(aValue); break;
            case Color_Prop: _color = (Color) aValue;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic effect attributes and set type
        XMLElement e = super.toXML(anArchiver); e.add("type", "shadow");

        // Archive Radius, DX & DY
        e.add("radius", _radius);
        e.add("dx", _dx);
        e.add("dy", _dy);
        if (!getColor().equals(Color.BLACK))
            e.add("color", "#" + getColor().toHexString());

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic effect attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive Radius, DX & DY
        _radius = anElement.getAttributeIntValue("radius");
        _dx = anElement.getAttributeIntValue("dx");
        _dy = anElement.getAttributeIntValue("dy");
        String color = anElement.getAttributeValue("color");
        if (color != null)
            _color = new Color(color);

        // Return this effect
        return this;
    }

    /**
     * Returns the effect image for an opaque rect by making a small shadow and blitting over the 8 pieces.
     */
    public static Image getShadowImage(Rect aRect, double aRad, Color aColor)
    {
        // Get info
        int rad = (int) aRad, rad2 = rad*2, rad3 = rad*3, rad4 = rad*4, rad6 = rad*6;
        int contentW = (int) Math.round(aRect.width);
        int contentH = (int) Math.round(aRect.height);

        // Create image with mini version of shadowed rect
        Image s0 = Image.get(rad6+1,rad6+1, true);
        Painter spntr = s0.getPainter();
        spntr.setColor(Color.BLACK);
        spntr.fillRect(rad2, rad2,rad2+1,rad2+1);
        s0.blur(rad, aColor);

        // Create image for full size shadow and fill unblurred content area
        Image shadowImage = Image.get(contentW + rad4, contentH + rad4, true);
        Painter pntr = shadowImage.getPainter();
        pntr.setColor(aColor);
        pntr.fillRect(rad3, rad3, contentW - rad2, contentH - rad2);

        // Copy over corners
        pntr.drawImage(s0, 0, 0, rad3, rad3, 0, 0, rad3, rad3);               // Upper left
        pntr.drawImage(s0, rad3, 0, 1, rad3, rad3, 0, contentW-rad2, rad3);     // Upper Center
        pntr.drawImage(s0, rad3+1, 0, rad3, rad3, rad+contentW, 0, rad3, rad3);     // Upper right
        pntr.drawImage(s0, 0, rad3, rad3, 1, 0, rad3, rad3, contentH-rad2);            // Left
        pntr.drawImage(s0, rad3+1, rad3, rad3, 1, rad+contentW, rad3, rad3, contentH-rad2);  // Right
        pntr.drawImage(s0, 0, rad3+1, rad3, rad3, 0, rad+contentH, rad3, rad3);           // Lower left
        pntr.drawImage(s0, rad3, rad3+1, 1, rad3, rad3, rad+contentH, contentW-rad2, rad3);   // Lower Center
        pntr.drawImage(s0, rad3+1, rad3+1, rad3, rad3, rad+contentW, rad+contentH, rad3, rad3); // Lower right

        // Return
        return shadowImage;
    }
}