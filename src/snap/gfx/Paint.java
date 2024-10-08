/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.props.StringCodec;
import snap.util.XMLArchiver;

/**
 * A class to represent a fill for a Shape or text (Color, GradientPaint, ImagePaint).
 */
public interface Paint extends XMLArchiver.Archivable, StringCodec.Codeable {

    /**
     * Returns whether paint is defined in terms independent of primitive to be filled.
     */
    public boolean isAbsolute();
    
    /**
     * Returns whether paint is opaque.
     */
    public boolean isOpaque();
    
    /**
     * Returns an absolute paint for given bounds of primitive to be filled.
     */
    public Paint copyForRect(Rect aRect);
    
    /**
     * Returns the name for paint.
     */
    default String getName()  { return getClass().getSimpleName(); }

    /**
     * Returns the closest color approximation of this paint.
     */
    public Color getColor();

    /**
     * Returns a copy of this paint modified for given color.
     */
    public Paint copyForColor(Color aColor);

    /**
     * Returns Paint as string.
     */
    @Override
    default String codeString()  { return getColor().toColorString(); }

    /**
     * Returns Paint for string.
     */
    @Override
    default Paint decodeString(String aString)  { return of(aString); }

    /**
     * Returns a color from given object.
     */
    static Paint of(Object anObj)
    {
        if (anObj instanceof Paint || anObj == null)
            return (Paint) anObj;
        if (anObj.equals("null"))
            return null;
        return Color.get(anObj);
    }
}