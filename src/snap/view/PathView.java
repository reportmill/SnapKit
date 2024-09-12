/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.props.PropSet;
import snap.util.*;

/**
 * A class to show a path shape.
 */
public class PathView extends View {

    // The path shape
    private Path2D _path;

    // Constants for properties
    public static final String SvgString_Prop = "SvgString";

    /**
     * Constructor.
     */
    public PathView()
    {
        super();
    }

    /**
     * Returns the SvgString.
     */
    public String getSvgString()  { return _path != null ? _path.getSvgString().replace('\n', ' ') : ""; }

    /**
     * Sets the SvgString.
     */
    public void setSvgString(String svgString)
    {
        Path2D path = new Path2D();
        if (svgString != null)
            path.appendSvgString(svgString);
        setPath(path);
    }

    /**
     * Returns the path.
     */
    public Path2D getPath()
    {
        if (_path != null) return _path;
        return _path = new Path2D();
    }

    /**
     * Sets the path.
     */
    public void setPath(Shape aPath)
    {
        _path = aPath instanceof Path2D ? (Path2D) aPath : new Path2D(aPath);
        repaint();
    }

    /**
     * Replace the polygon's current path with a new path, adjusting the shape's bounds to match the new path.
     */
    public void resetPathAndBounds(Shape aShape)
    {
        // Set the new path and new size
        Path2D newPath = new Path2D(aShape);
        setPath(newPath);
        Rect bounds = newPath.getBounds();
        setSizeLocal(bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Override to return path as bounds shape.
     */
    public Shape getBoundsShape()
    {
        Shape path = getPath();
        Rect boundsLocal = getBoundsLocal();
        return path.copyFor(boundsLocal);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // SvgString
        aPropSet.addPropNamed(SvgString_Prop, String.class, EMPTY_OBJECT);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // SvgString
        if (aPropName.equals(SvgString_Prop))
            return getSvgString();

        // Do normal version
        return super.getPropValue(aPropName);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // SvgString
        if (aPropName.equals(SvgString_Prop))
            setSvgString(Convert.stringValue(aValue));

        // Do normal version
        else super.setPropValue(aPropName, aValue);
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Shape path = getPath();
        Rect pathBounds = path.getBounds();
        Insets ins = getInsetsAll();
        return pathBounds.getMaxX() + ins.getWidth();
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Shape path = getPath();
        Rect pathBounds = path.getBounds();
        Insets ins = getInsetsAll();
        return pathBounds.getMaxY() + ins.getHeight();
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Do normal version
        XMLElement e = super.toXML(anArchiver);

        // Archive path
        if (!isPropDefault(SvgString_Prop))
            e.add(SvgString_Prop, getSvgString());

        // Return
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Do normal version
        super.fromXML(anArchiver, anElement);

        // Unarchive path
        if (anElement.hasAttribute(SvgString_Prop))
            setSvgString(anElement.getAttributeValue(SvgString_Prop));

        // Return
        return this;
    }
}