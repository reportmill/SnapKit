/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Paint;

/**
 * This class helps PropArchiver archive some common SnapKit classes.
 */
public class PropArchiverHpr {

    // The PropArchiver associated with this instance
    private PropArchiver  _archiver;

    /**
     * Constructor.
     */
    public PropArchiverHpr(PropArchiver anArchiver)
    {
        _archiver = anArchiver;
    }

    /**
     * Converts given object to PropNode or primitive.
     */
    protected Object convertObjectToPropNodeOrPrimitive(Object anObj)
    {
        if (anObj instanceof Font)
            return convertFontToPropNode((Font) anObj);
        if (anObj instanceof Paint)
            return convertPaintToPropNode((Paint) anObj);
        return null;
    }

    /**
     * Converts given font to PropNode.
     */
    protected Object convertFontToPropNode(Font aFont)
    {
        PropNode propNode = new PropNode(aFont, _archiver);
        propNode.addNativeValueForPropName("Name", aFont.getNameEnglish());
        propNode.addNativeValueForPropName("Size", aFont.getSize());
        return propNode;
    }

    /**
     * Converts given Paint to PropNode.
     */
    protected PropNode convertPaintToPropNode(Paint aPaint)
    {
        if (aPaint instanceof Color)
            return convertColorToPropNode((Color) aPaint);
        System.err.println("PropArchiverHpr.convertPaintToPropNode: Unsupported paint: " + aPaint.getClass());
        return null;
    }

    /**
     * Converts given Color to PropNode.
     */
    protected PropNode convertColorToPropNode(Color aColor)
    {
        PropNode propNode = new PropNode(aColor, _archiver);
        propNode.addNativeValueForPropName("Color", '#' + aColor.toHexString());
        return propNode;
    }
}
