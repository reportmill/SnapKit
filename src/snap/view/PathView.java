/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.util.*;

/**
 * A class to show a path shape.
 */
public class PathView extends View {

    // The path shape
    private Path2D _path;

    /**
     * Constructor.
     */
    public PathView()
    {
        super();
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

        // Archive path - was: e.add(_path.toXML(anArchiver));
        Shape path = getPath();
        if (path != null) {
            String svgString = path.getSvgString().replace('\n', ' ');
            e.add("SvgString", svgString);
        }

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

        // Unarchive path - was: pathXML = anElement.get("path"); _path = anArchiver.fromXML(pathXML, Path.class, this);
        String svgString = anElement.getAttributeValue("SvgString");
        if (svgString != null) {
            _path = new Path2D();
            _path.appendSvgString(svgString);
        }

        // Return
        return this;
    }
}