/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.util.*;

/**
 * An Effect subclass to represent a drop shadow.
 */
public class ShadowEffect extends Effect {

    // The shadow radius
    private double  _radius = 10;
    
    // The shadow offset
    private double  _dx, _dy;
    
    // Fill color
    private Color  _color = Color.BLACK;
    
    // Whether effect is simple
    private boolean  _simple;

    /**
     * Creates a new ShadowEffect.
     */
    public ShadowEffect()  { }

    /**
     * Creates a new ShadowEffect for given radius, color and offset.
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
        if (_dx!=0 || _dy!=0) {
            rect.offset(_dx, _dy);
            rect.union(aRect);
        }
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
        Image img = getShadowImage(aPDVR, aRect);
        aPntr.drawImage(img, -radius*2 + dx, -radius*2 + dy, img.getWidth(), img.getHeight());
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
        Image img = getShadowImage(aPDVR, aRect);
        aPntr.drawImage(img, -radius*2 + dx, -radius*2 + dy, img.getWidth(), img.getHeight());
    }

    /**
     * Returns the effect image.
     */
    public Image getShadowImage(PainterDVR aPDVR, Rect aRect)
    {
        // If marked shape is rect and opaque, return simple shadow image
        if (_simple)
            return getShadowImage(aRect, getRadius(), getColor());
        if (aPDVR.getMarkedShape() instanceof Rect && aPDVR.isMarkedShapeOpaque())
            return getShadowImage(aPDVR.getMarkedShape().getBounds(), getRadius(), getColor());

        // Create new image for dvr
        int radius = (int) getRadius(); //if (radius>2) return getShadowImageSimple(aRect);
        Image simg = aPDVR.getImage(aRect, radius*2);

        // Blur image and return
        simg.blur(radius, getColor());
        return simg;
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
        ShadowEffect eff = new ShadowEffect(_radius, _color, _dx, _dy); eff._simple = true; return eff;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        ShadowEffect other = anObj instanceof ShadowEffect ? (ShadowEffect)anObj : null; if (other==null) return false;
        if (other._radius != _radius) return false;
        if (other._dx != _dx || other._dy != _dy) return false;
        if (other._color != _color) return false;
        if (other._simple != _simple) return false;
        return true;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return StringUtils.toString(this, "Color", "Radius", "DX", "DY").toString();
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
        if (color!=null)
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
        Image simg = Image.get(contentW + rad4, contentH + rad4, true);
        Painter pntr = simg.getPainter();
        pntr.setColor(aColor);
        pntr.fillRect(rad3, rad3, contentW-rad2, contentH-rad2);

        // Copy over corners
        pntr.drawImage(s0, 0, 0, rad3, rad3, 0, 0, rad3, rad3);               // Upper left
        pntr.drawImage(s0, rad3, 0, 1, rad3, rad3, 0, contentW-rad2, rad3);     // Upper Center
        pntr.drawImage(s0, rad3+1, 0, rad3, rad3, rad+contentW, 0, rad3, rad3);     // Upper right
        pntr.drawImage(s0, 0, rad3, rad3, 1, 0, rad3, rad3, contentH-rad2);            // Left
        pntr.drawImage(s0, rad3+1, rad3, rad3, 1, rad+contentW, rad3, rad3, contentH-rad2);  // Right
        pntr.drawImage(s0, 0, rad3+1, rad3, rad3, 0, rad+contentH, rad3, rad3);           // Lower left
        pntr.drawImage(s0, rad3, rad3+1, 1, rad3, rad3, rad+contentH, contentW-rad2, rad3);   // Lower Center
        pntr.drawImage(s0, rad3+1, rad3+1, rad3, rad3, rad+contentW, rad+contentH, rad3, rad3); // Lower right
        return simg;
    }
}