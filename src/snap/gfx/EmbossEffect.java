/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.util.*;

/**
 * An Effect subclass that make drawing look slightly 3D with raised or lowered edges.
 */
public class EmbossEffect extends Effect {

    // Light source position
    private double  _altitude = 60;
    
    // Light source position
    private double  _azimuth = 120;
    
    // Radius to use when bluring the bump map mask
    private double  _radius = 10;

    /**
     * Creates an emboss effect.
     */
    public EmbossEffect()  { }

    /**
     * Creates an emboss effect for given altitude, azimuth and radius.
     */
    public EmbossEffect(double anAlt, double anAzi, double aRad)
    {
        super();
        _altitude = anAlt; _azimuth = anAzi; _radius = aRad;
    }

    /**
     * Returns altitude of light source.
     */
    public double getAltitude()  { return _altitude; }

    /**
     * Returns angle of light source.
     */
    public double getAzimuth()  { return _azimuth; }

    /**
     * Returns radius of edge rounding.
     */
    public double getRadius()  { return _radius; }

    /**
     * Override to account for blur radius.
     */
    public Rect getBounds(Rect aRect) { Rect rect = aRect.getInsetRect(-getRadius()); return rect; }

    /**
     * Performs the ShadowEffect with given PainterDVR.
     */
    public void applyEffect(PainterDVR aPDVR, Painter aPntr, Rect aRect)
    {
        Image img = getEmbossImage(aPDVR, aRect);
        aPntr.drawImage(img, 0, 0, img.getWidth(), img.getHeight());
    }

    /**
     * Creates emboss image.
     */
    public Image getEmbossImage(PainterDVR aPDVR, Rect aRect)
    {
        Image eimg = aPDVR.getImage(aRect, 0);
        eimg.emboss(getRadius(), getAzimuth(), getAltitude());
        return eimg;
    }

    /**
     * Returns a new Emboss for given altitude.
     */
    public EmbossEffect copyForAltitude(double anAlt)
    {
        return new EmbossEffect(anAlt, _azimuth, _radius);
    }

    /**
     * Returns a new Emboss for given azimuth.
     */
    public EmbossEffect copyForAzimuth(double anAzi)
    {
        return new EmbossEffect(_altitude, anAzi, _radius);
    }

    /**
     * Returns a new Emboss for given radius.
     */
    public EmbossEffect copyForRadius(double aRad)
    {
        return new EmbossEffect(_altitude, _azimuth, aRad);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        EmbossEffect other = anObj instanceof EmbossEffect ? (EmbossEffect)anObj : null; if (other==null) return false;
        if (other._radius != _radius) return false;
        if (other._altitude != _altitude) return false;
        if (other._azimuth != _azimuth) return false;
        return true;
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes and set type
        XMLElement e = super.toXML(anArchiver); e.add("type", "emboss");

        // Archive Radius, Altitude, Azimuth
        if (getRadius()!=10) e.add("radius", getRadius());
        if (getAzimuth()!=120) e.add("azimuth", getAzimuth());
        if (getAltitude()!=60) e.add("altitude", getAltitude());

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Uanrchive Radius, Altitude, Azimuth
        _radius = anElement.getAttributeIntValue("radius", 10);
        _azimuth = anElement.getAttributeFloatValue("azimuth", 120);
        _altitude = anElement.getAttributeFloatValue("altitude", 60);
        return this;  // Return this effect
    }
}