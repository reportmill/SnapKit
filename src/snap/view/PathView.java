/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A custom class.
 */
public class PathView extends View {
    
    Path   _path;

/**
 * Returns the path.
 */
public Path getPath()  { return _path!=null? _path : (_path=new Path()); }

/**
 * Override to return path as bounds shape.
 */
public Shape getBoundsShape()  { return getPath().getShapeInRect(getBoundsInside()); }

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll();
    return ins.left + getPath().getBounds().getMaxX() + ins.right;
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    return ins.top + getPath().getBounds().getMaxY() + ins.bottom;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = super.toXML(anArchiver);                     // Archive basic shape attributes
    e.add(_path.toXML(anArchiver));                             // Archive path
    return e;                                                   // Return xml element
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXML(anArchiver, anElement);                         // Unarchive basic shape attributes
    XMLElement pathXML = anElement.get("path");                        // Unarchive path
    _path = anArchiver.fromXML(pathXML, Path.class, this);
    return this;
}

}