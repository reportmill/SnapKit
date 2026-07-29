package snap.gfx;
import snap.geom.Rect;
import snap.geom.Size;
import snap.view.View;
import snap.view.ViewUtils;

/**
 * A class to manage an image rendered in a rectangular area.
 */
public class ImageBox {

    // The image
    private Image _image;

    // The image bounds
    private Rect _imageBounds;

    // The box size
    private Size _boxSize;

    /**
     * Constructor.
     */
    public ImageBox(Image anImage, double imageX, double imageY, double imageW, double imageH, double boxW, double boxH)
    {
        _image = anImage;
        _imageBounds = new Rect(imageX, imageY, imageW, imageH);
        _boxSize = new Size(boxW, boxH);
    }

    /**
     * Returns the image.
     */
    public Image getImage()  { return _image; }

    /**
     * Returns the image bounds.
     */
    public Rect getImageBounds()  { return _imageBounds; }

    /**
     * Returns the box width.
     */
    public double getWidth()  { return _boxSize.width; }

    /**
     * Returns the box height.
     */
    public double getHeight()  { return _boxSize.height; }

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

    /**
     * Returns an image box for a View and DPI scale (1 = 72 dpi, 2 = 144 dpi, 0 = device dpi).
     */
    public static ImageBox getImageBoxForView(View aView)
    {
        // Get size of view and image and offset of view in image (if effect)
        double viewW = aView.getWidth();
        double viewH = aView.getHeight();
        int imageW = (int) Math.ceil(viewW);
        int imageH = (int) Math.ceil(viewH);
        int imageX = 0;
        int imageY = 0;

        // If View has effect, image will likely be larger and not positioned at view origin
        Effect viewEffect = aView.getEffect();
        if (viewEffect != null) {
            Rect effectBounds = viewEffect.getBounds(aView.getBoundsLocal());
            imageX = (int) Math.round(effectBounds.x);
            imageY = (int) Math.round(effectBounds.y);
            imageW = (int) Math.ceil(effectBounds.width);
            imageH = (int) Math.ceil(effectBounds.height);
        }

        // Create image, paint view and return
        Image image = Image.getImageForSizeAndDpiScale(imageW, imageH, true, -1);
        Painter pntr = image.getPainter();
        pntr.translate(-imageX, -imageY);
        ViewUtils.paintView(aView, pntr);

        // Return new ImageBox for image, image bounds, view size
        return new ImageBox(image, imageX, imageY, imageW, imageH, viewW, viewH);
    }
}
