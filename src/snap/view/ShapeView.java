package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to render shapes.
 */
public class ShapeView extends View {
    
    Shape   _shape;

/**
 * Creates a new ShapeNode.
 */
public ShapeView()  { }

/**
 * Creates a new ShapeNode for given shape.
 */
public ShapeView(Shape aShape)  { setShape(aShape); }

/**
 * Returns the shape.
 */
public Shape getShape()  { return _shape!=null? _shape : (_shape=new Rect()); }

/**
 * Sets the shape.
 */
public void setShape(Shape aShape)
{
    _shape = aShape;
}

/**
 * Override to return path as bounds shape.
 */
public Shape getBoundsShape()  { return getShape().getShapeInRect(getBoundsInside()); }

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll();
    return ins.left + getShape().getBounds().getMaxX() + ins.right;
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    return ins.top + getShape().getBounds().getMaxY() + ins.bottom;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = super.toXML(anArchiver); e.setName("polygon"); // Archive basic shape attributes and reset name
    //e.add(_path.toXML(anArchiver));                                    // Archive path
    return e;                                                          // Return xml element
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXML(anArchiver, anElement);                         // Unarchive basic shape attributes
    XMLElement pathXML = anElement.get("path");                        // Unarchive path
    //_path = anArchiver.fromXML(pathXML, Path.class, this);
    return this;
}

}