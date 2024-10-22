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
        XMLElement e = super.toXML(anArchiver);

        // Archive Radius, Altitude, Azimuth
        if (!isPropDefault(Radius_Prop)) e.add(Radius_Prop, getRadius());
        if (!isPropDefault(Azimuth_Prop)) e.add(Azimuth_Prop, getAzimuth());
        if (!isPropDefault(Altitude_Prop)) e.add(Altitude_Prop, getAltitude());

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
        if (anElement.hasAttribute(Radius_Prop)) _radius = anElement.getAttributeIntValue(Radius_Prop);
        if (anElement.hasAttribute(Azimuth_Prop)) _azimuth = anElement.getAttributeFloatValue(Azimuth_Prop);
        if (anElement.hasAttribute(Altitude_Prop)) _altitude = anElement.getAttributeFloatValue(Altitude_Prop);

        // Return
        return this;
    }


    /**
     * Returns a string encoding of this effect.
     */
    @Override
    public String codeString()
    {
        String sb = "emboss" + '(' +
                FormatUtils.formatNum(getRadius()) + ' ' +
                FormatUtils.formatNum(getAltitude()) + ' ' +
                FormatUtils.formatNum(getAzimuth()) + ')';
        return sb;
    }

    /**
     * Returns a emboss effect for given coded string.
     */
    public static EmbossEffect of(Object anObj)
    {
        if (anObj == null || anObj instanceof EmbossEffect)
            return (EmbossEffect) anObj;

        // Get string
        String str = anObj.toString().toLowerCase().replace("emboss", "");
        str = str.replace('(', ' ').replace(')', ' ').trim();

        // Get parts
        String[] parts = str.split("\\s");
        if (parts.length < 3)
            return new EmbossEffect();

        double radius = Convert.doubleValue(parts[0]);
        double altitude = Convert.doubleValue(parts[1]);
        double azimuth = Convert.doubleValue(parts[2]);
        return new EmbossEffect(altitude, azimuth, radius);
    }
}