package snap.gfx3d;
import snap.gfx.Image;

/**
 * This class represents an image texture.
 */
public class Texture {

    // The image
    private Image  _image;

    // Whether the image is flipped
    private boolean  _flipped;

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

    /**
     * Returns whether the image is flipped.
     */
    public boolean isFlipped()  { return _flipped; }

    /**
     * Sets whether the image is flipped.
     */
    public void setFlipped(boolean aValue)  { _flipped = aValue; }
}
