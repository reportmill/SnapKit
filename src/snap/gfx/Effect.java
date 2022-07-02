/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.props.Prop;
import snap.props.PropObject;
import snap.props.StringCodec;
import snap.util.*;

/**
 * A class to represent a visual effect that can be applied to drawing done in a Painter (like blur, shadow, etc.).
 */
public class Effect extends PropObject implements XMLArchiver.Archivable {

    /**
     * Returns the name of the effect.
     */
    public String getName()
    {
        String cname = getClass().getSimpleName();
        return cname.substring(0, cname.length() - 6);
    }

    /**
     * Returns the bounds required to render this effect applied to given rect.
     */
    public Rect getBounds(Rect aRect)
    {
        return aRect;
    }

    /**
     * Apply the effect from given DVR to painter.
     */
    public void applyEffect(PainterDVR aPDVR, Painter aPntr, Rect aRect)
    {
        System.err.println(getClass().getSimpleName() + ".apply: Not implemented");
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + " { " + propStrings + " }";
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        // Add Name
        StringBuffer sb = new StringBuffer();
        String name = getName();
        StringUtils.appendProp(sb, "Name", name);

        // Add Props
        Prop[] props = getPropsForArchival();
        for (Prop prop : props) {
            String propName = prop.getName();
            if (!isPropDefault(propName) && !prop.isRelation()) {
                Object value = getPropValue(propName);
                if (StringCodec.SHARED.isCodeable(value)) {
                    String valueStr = StringCodec.SHARED.codeString(value);
                    StringUtils.appendProp(sb, propName, valueStr);
                }
            }
        }

        // Return
        return sb.toString();
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        String name = getClass().getSimpleName(); // "effect"
        XMLElement e = new XMLElement(name);
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        return this;
    }

}