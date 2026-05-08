package snap.games;
import snap.gfx.Image;
import snap.gfx.ImageUtils;
import snap.util.Convert;
import snap.web.WebURL;
import java.util.List;

/**
 * This class represents an image or animated set of images.
 */
public class Sprite {

    // The source image url
    private WebURL _sourceUrl;

    // The source image
    private Image _sourceImage;

    // The frame count
    private int _frameCount;

    // The list of frame images
    private List<Image> _frameImages;

    // The frame per second
    private int _framesPerSecond = 15;

    // The cached version of this sprite flipped horizontally
    private Sprite _flippedX;

    /**
     * Constructor.
     */
    public Sprite(WebURL aSourceUrl)
    {
        _sourceUrl = aSourceUrl;
        _frameCount = 1;

        if (aSourceUrl.getFilename().contains("_strip"))
            _frameCount = Convert.intValue(aSourceUrl.getFilename());
    }

    /**
     * Returns the source image url.
     */
    public WebURL getSourceURL() { return _sourceUrl; }

    /**
     * Returns the source image.
     */
    public Image getSourceImage()
    {
        if (_sourceImage != null) return _sourceImage;
        return _sourceImage = Image.getImageForUrl(_sourceUrl);
    }

    /**
     * Returns the number of frames.
     */
    public int getFrameCount()  { return _frameCount; }

    /**
     * Returns the frame images.
     */
    public List<Image> getFrameImages()
    {
        if (_frameImages != null) return _frameImages;
        Image sourceImage = getSourceImage();
        return _frameImages = ImageUtils.getFrameImagesForSpriteStripImageAndFrameCount(sourceImage, getFrameCount());
    }

    /**
     * Returns the individual image at index.
     */
    public Image getFrameImageForIndex(int anIndex)  { return getFrameImages().get(anIndex); }

    /**
     * Returns the frames per second.
     */
    public int getFramesPerSecond()  { return _framesPerSecond; }

    /**
     * Sets the frames per second.
     */
    public void setFramesPerSecond(int aValue)  { _framesPerSecond = aValue; }

    /**
     * Returns this sprite flipped horizontally.
     */
    public Sprite getFlippedX()
    {
        if (_flippedX != null) return _flippedX;
        Sprite flippedSprite = new Sprite(getSourceURL());
        flippedSprite._sourceImage = ImageUtils.getImageFlippedX(_sourceImage);
        return _flippedX = flippedSprite;
    }
}
