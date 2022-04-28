package snap.gfx3d;
import snap.gfx.Image;

/**
 * This class represents an image texture.
 */
public class Texture {

    // The image
    private Image  _image;

    /**
     * Constructor.
     */
    public Texture(Image anImage)
    {
        _image = anImage;
    }

    /**
     * Returns the image.
     */
    public Image getImage()  { return _image; }
}
