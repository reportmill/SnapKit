/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.util.*;

/**
 * A paint implementation to fill shapes with an image pattern.
 */
public class ImagePaint implements Paint, XMLArchiver.Archivable {

    // The image
    private Image  _image;
    
    // The bounds
    private double  _x, _y, _w, _h;
    
    // Whether paint is defined in terms independent of primitive to be filled
    private boolean  _abs = true;
    
    /**
     * Creates a new ImagePaint.
     */
    public ImagePaint()  { }

    /**
     * Creates a new ImagePaint.
     */
    public ImagePaint(Image anImage)
    {
        this(anImage, 0, 0, anImage.getWidth(), anImage.getHeight(), true);
    }

    /**
     * Creates a new ImagePaint.
     */
    public ImagePaint(Image anImage, Rect aRect)
    {
        this(anImage, aRect, true);
    }

    /**
     * Creates a new ImagePaint.
     */
    public ImagePaint(Image anImage, Rect aRect, boolean isAbs)
    {
        this(anImage, aRect.x, aRect.y, aRect.width, aRect.height, isAbs);
    }

    /**
     * Creates a new ImagePaint.
     */
    public ImagePaint(Image anImage, double aX, double aY, double aW, double aH, boolean isAbs)
    {
        _image = anImage;
        _x = aX; _y = aY;
        _w = aW; _h = aH;
        _abs = isAbs;
    }

    /**
     * Returns the image.
     */
    public Image getImage()  { return _image; }

    /**
     * Returns the bounds x.
     */
    public double getX()  { return _x; }

    /**
     * Returns the bounds y.
     */
    public double getY()  { return _y; }

    /**
     * Returns the bounds width.
     */
    public double getWidth()  { return _w; }

    /**
     * Returns the bounds height.
     */
    public double getHeight()  { return _h; }

    /**
     * Returns the bounds.
     */
    public Rect getBounds()  { return new Rect(_x, _y, _w, _h); }

    /**
     * Returns the scale x of the image fill image.
     */
    public double getScaleX()  { return isAbsolute()? getWidth()/getImage().getWidth() : getWidth(); }

    /**
     * Returns the scale y of the image fill image.
     */
    public double getScaleY()  { return isAbsolute()? getHeight()/getImage().getHeight() : getHeight(); }

    /**
     * Returns whether paint is defined in terms independent of primitive to be filled.
     */
    public boolean isTiled()  { return isAbsolute(); }

    /**
     * Returns whether paint is defined in terms independent of primitive to be filled.
     */
    public boolean isAbsolute()  { return _abs; }

    /**
     * Returns whether paint is opaque.
     */
    public boolean isOpaque()  { return !_image.hasAlpha(); }

    /**
     * Returns the closest color approximation of this paint.
     */
    public Color getColor()  { return Color.BLACK; }

    /**
     * Returns a copy of this paint modified for given color.
     */
    public Paint copyForColor(Color aColor)  { return this; }

    /**
     * Returns an absolute paint for given bounds of primitive to be filled.
     */
    public ImagePaint copyForRect(Rect aRect)
    {
        if (_abs) return this;
        double x = aRect.x + aRect.width*_x, w = aRect.width*_w;
        double y = aRect.y + aRect.height*_y, h = aRect.height*_h;
        return new ImagePaint(_image, x, y, w, h, true);
    }

    /**
     * Returns an paint for given bounds and absolute flag.
     */
    public ImagePaint copyForRectAndTile(Rect aRect, boolean isAbs)
    {
        return new ImagePaint(_image, aRect.x, aRect.y, aRect.width, aRect.height, isAbs);
    }

    /**
     * Creates a new image fill identical to this image fill, but with new value for given attribute.
     */
    public ImagePaint copyTiled(boolean isTiled)
    {
        ImagePaint copy;
        if (isTiled) copy = new ImagePaint(getImage());
        else copy = new ImagePaint(getImage(), new Rect(0,0,1,1), false);
        return copy;
    }

    /**
     * Creates a new image fill identical to this image fill, but with new value for given attribute.
     */
    public ImagePaint copyForScale(double aScaleX, double aScaleY)
    {
        double w = isAbsolute()? getImage().getWidth()*aScaleX : aScaleX;
        double h = isAbsolute()? getImage().getHeight()*aScaleY : aScaleY;
        return copyForRectAndTile(new Rect(getX(),getY(),w,h), isAbsolute());
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        ImagePaint other = anObj instanceof ImagePaint ? (ImagePaint) anObj : null; if (other == null) return false;
        if (other._abs != _abs) return false;
        if (other._x != _x || other._y != _y || other._w != _w || other._h != _h) return false;
        return true;
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic fill attributes and set type
        XMLElement e = new XMLElement("fill"); e.add("type", "image");

        // Archive ImageData
        if (_image.getBytes() != null) {
            String resName = anArchiver.addResource(_image.getBytes(), "" + System.identityHashCode(_image));
            e.add("resource", resName);
        }

        // Archive Tile
        if (!isAbsolute())
            e.add("Tile", false);

        // Archive bounds
        if (_x != 0)
            e.add("x", _x);
        if (_y != 0)
            e.add("y", _y);
        if (_w != _image.getWidth())
            e.add("w", _w);
        if (_h != _image.getHeight())
            e.add("h", _h);

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive ImageName: get resource bytes, page and set ImageData
        String iname = anElement.getAttributeValue("resource");
        if (iname!=null) {
            byte[] bytes = anArchiver.getResource(iname); // Get resource bytes
            _image = Image.get(bytes); // Create new image data
            _w = _image.getWidth();
            _h = _image.getHeight();
        }

        // Unarchive Tile, legacy FillStyle (Stretch=0, Tile=1, Fit=2, FitIfNeeded=3)
        if (anElement.hasAttribute("Tile") && !anElement.getAttributeBoolValue("Tile") ||
            (anElement.hasAttribute("fillstyle") && anElement.getAttributeIntValue("fillstyle")!=1)) {
            _abs = false;
            _w = _h = 1;
        }

        // Unarchive bounds
        if (anElement.hasAttribute("x"))
            _x = anElement.getAttributeFloatValue("x");
        if (anElement.hasAttribute("y"))
            _y = anElement.getAttributeFloatValue("y");
        if (anElement.hasAttribute("w"))
            _w = anElement.getAttributeFloatValue("w");
        if (anElement.hasAttribute("h"))
            _h = anElement.getAttributeFloatValue("h");

        // Unarchive ScaleX, ScaleY
        double sx = anElement.getAttributeFloatValue("scale-x", 1);
        double sy = anElement.getAttributeFloatValue("scale-y", 1);
        if (sx != 1 || sy != 1) {
            _w = _abs ? _w*sx : sx;
            _h = _abs ? _h*sy : sy;
        }

        // Return this image fill
        return this;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return StringUtils.format("ImagePaint { x=%s, y=%s, w=%s, h=%s, abs=%s", fmt(_x), fmt(_y), fmt(_w), fmt(_h), _abs);
    }

    // Used for print
    private static String fmt(double aValue)  { return _fmt.format(aValue); }
    private static java.text.DecimalFormat _fmt = new java.text.DecimalFormat("0.##");
}