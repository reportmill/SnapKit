package snap.gfx;
import snap.geom.Rect;
import snap.geom.RoundRect;

/**
 * A class to manage an image rendered in a rectangular area.
 */
public class ImageBox extends RoundRect {

    // The image
    private Image  _image;

    // The image bounds
    private Rect  _imageBounds;

    /**
     * Constructor.
     */
    public ImageBox(Image anImage)
    {
        this(anImage, 0, 0, anImage.getWidth(), anImage.getHeight());
    }

    /**
     * Constructor.
     */
    public ImageBox(Image anImage, Rect theBounds)
    {
        this(anImage, theBounds.x, theBounds.y, theBounds.width, theBounds.height);
    }

    /**
     * Constructor.
     */
    public ImageBox(Image anImage, double aW, double aH)
    {
        this(anImage, 0, 0, aW, aH);
    }

    /**
     * Constructor.
     */
    public ImageBox(Image anImage, double aX, double aY, double aW, double aH)
    {
        _image = anImage;
        setRect(aX, aY, aW, aH);
    }

    /**
     * Returns the image.
     */
    public Image getImage()  { return _image; }

    /**
     * Returns the image bounds.
     */
    public Rect getImageBounds()
    {
        // If explicitly set, just return
        if (_imageBounds != null) return _imageBounds;

        // Otherwise calculate
        return getBounds();
    }

    /**
     * Sets the image bounds.
     */
    public void setImageBounds(Rect aRect)
    {
        setImageBounds(aRect.x, aRect.y, aRect.width, aRect.height);
    }

    /**
     * Sets the image bounds.
     */
    public void setImageBounds(double aX, double aY, double aW, double aH)
    {
        _imageBounds = new Rect(aX, aY, aW, aH);
    }

    /**
     * Paints the image box at given point.
     */
    public void paintImageBox(Painter aPntr, double aX, double aY)
    {
        Rect imgBnds = getImageBounds();
        double imgX = imgBnds.x + aX;
        double imgY = imgBnds.y + aY;
        aPntr.drawImage(_image, imgX, imgY, imgBnds.width, imgBnds.height);
    }
}
