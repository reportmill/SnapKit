/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.props.PropSet;
import snap.util.*;

/**
 * An Effect subclass that make drawing look slightly 3D with raised or lowered edges.
 */
public class EmbossEffect extends Effect {

    // Light source position
    private double  _altitude;
    
    // Light source position
    private double  _azimuth;
    
    // Radius to use when bluring the bump map mask
    private double  _radius;

    // Constants for properties
    public static final String Altitude_Prop = "Altitude";
    public static final String Azimuth_Prop = "Azimuth";
    public static final String Radius_Prop = "Radius";

    // Constants for defaults
    public static final double DEFAULT_ALTITUDE = 60;
    public static final double DEFAULT_AZIMUTH = 120;
    public static final double DEFAULT_RADIUS = 10;

    /**
     * Creates an emboss effect.
     */
    public EmbossEffect()
    {
        _altitude = DEFAULT_ALTITUDE;
        _azimuth = DEFAULT_AZIMUTH;
        _radius = DEFAULT_RADIUS;
    }

    /**
     * Creates an emboss effect for given altitude, azimuth and radius.
     */
    public EmbossEffect(double anAlt, double anAzi, double aRad)
    {
        super();
        _altitude = anAlt;
        _azimuth = anAzi;
        _radius = aRad;
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
    public Rect getBounds(Rect aRect)
    {
        Rect rect = aRect.getInsetRect(-getRadius());
        return rect;
    }

    /**
     * Performs the ShadowEffect with given PainterDVR.
     */
    public void applyEffect(PainterDVR aPDVR, Painter aPntr, Rect aRect)
    {
        Image embossImage = getEmbossImage(aPDVR, aRect);
        aPntr.drawImage(embossImage, 0, 0);
    }

    /**
     * Creates emboss image.
     */
    public Image getEmbossImage(PainterDVR aPDVR, Rect aRect)
    {
        Image embossImage = aPDVR.getImage(aRect, 0);
        embossImage.emboss(getRadius(), getAzimuth(), getAltitude());
        return embossImage;
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
        EmbossEffect other = anObj instanceof EmbossEffect ? (EmbossEffect) anObj : null;
        if (other == null) return false;
        if (other._radius != _radius) return false;
        if (other._altitude != _altitude) return false;
        if (other._azimuth != _azimuth) return false;
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

        // Altitude, Azimuth, Radius
        aPropSet.addPropNamed(Altitude_Prop, double.class, DEFAULT_ALTITUDE);
        aPropSet.addPropNamed(Azimuth_Prop, double.class, DEFAULT_AZIMUTH);
        aPropSet.addPropNamed(Radius_Prop, double.class, DEFAULT_RADIUS);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Altitude, Azimuth, Radius
            case Altitude_Prop: return getAltitude();
            case Azimuth_Prop: return getAzimuth();
            case Radius_Prop: return getRadius();

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

            // Altitude, Azimuth, Radius
            case Altitude_Prop: _altitude = Convert.doubleValue(aValue); break;
            case Azimuth_Prop: _azimuth = Convert.doubleValue(aValue); break;
            case Radius_Prop: _radius = Convert.doubleValue(aValue); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes and set type
        XMLElement e = super.toXML(anArchiver); e.add("type", "emboss");

        // Archive Radius, Altitude, Azimuth
        if (getRadius() != DEFAULT_RADIUS) e.add("radius", getRadius());
        if (getAzimuth() != DEFAULT_AZIMUTH) e.add("azimuth", getAzimuth());
        if (getAltitude() != DEFAULT_ALTITUDE) e.add("altitude", getAltitude());

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

        // Return
        return this;
    }
}