/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.props.PropObject;
import snap.props.StringCodec;
import snap.util.*;

/**
 * A class to represent a visual effect that can be applied to drawing done in a Painter (like blur, shadow, etc.).
 */
public abstract class Effect extends PropObject implements XMLArchiver.Archivable, StringCodec.Codeable {

    /**
     * Constructor.
     */
    public Effect()
    {
        super();
    }

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
    public abstract void applyEffect(PainterDVR aPDVR, Painter aPntr, Rect aRect);

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        String name = getClass().getSimpleName();
        XMLElement e = new XMLElement(name);
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)  { return this; }

    /**
     * Returns a string encoding of this effect.
     */
    @Override
    public String codeString()  { return "unknown"; }

    /**
     * Parses an effect from given object.
     */
    public static Effect of(Object anObj)
    {
        // Handle Effect or null
        if (anObj instanceof Effect || anObj == null)
            return (Effect) anObj;

        // Get effect name
        String str = anObj.toString().replace('(', ' ');
        String[] parts = str.split("\\s");
        String effectName = parts.length > 0 ? parts[0].toLowerCase() : "";

        // Parse effect
        switch (effectName) {
            case "shadow": return ShadowEffect.of(anObj);
            case "blur": return BlurEffect.of(anObj);
            case "emboss": return EmbossEffect.of(anObj);
            case "reflect": return ReflectEffect.of(anObj);
            case "null": return null;
            default: System.err.println("Effect.of: Invalid effect string: " + anObj); return null;
        }
    }
}