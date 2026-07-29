package snap.gfx;
import snap.geom.Rect;
import snap.geom.RectBase;
import snap.geom.RoundRect;

/**
 * A class to manage an image rendered in a rectangular area.
 */
public class ImageBox extends Rect {

    // The image
    private Image _image;

    // The image bounds
    private Rect _imageBounds;

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
        super(aX, aY, aW, aH);
        _image = anImage;
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
        if (_imageBounds != null) return _imageBounds;
        return getBounds();
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
        Rect imageBounds = getImageBounds();
        double imageX = imageBounds.x + aX;
        double imageY = imageBounds.y + aY;
        aPntr.drawImage(_image, imageX, imageY, imageBounds.width, imageBounds.height);
    }
}
