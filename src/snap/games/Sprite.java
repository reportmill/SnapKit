package snap.games;
import snap.geom.Rect;
import snap.gfx.GFXEnv;
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

    // The visible bounds of frame images
    private Rect _visibleBounds;

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
     * Returns the visible bounds of the image.
     */
    public Rect getVisibleBounds()
    {
        if (_visibleBounds != null) return _visibleBounds;
        Rect visibleBounds = getFrameImageForIndex(0).getVisibleBounds();
        for (int i = 1, iMax = getFrameCount(); i < iMax; i++)
            visibleBounds.union(getFrameImageForIndex(i).getVisibleBounds());
        return _visibleBounds = visibleBounds;
    }

    /**
     * Returns this sprite flipped horizontally.
     */
    public Sprite getFlippedX()
    {
        if (_flippedX != null) return _flippedX;
        Sprite flippedSprite = new Sprite(getSourceURL());
        flippedSprite._frameImages = getFrameImages().stream().map(Image::copyFlippedX).toList();
        return _flippedX = flippedSprite;
    }

    /**
     * Returns a sprite with max frame size.
     */
    public Sprite getSpriteWithMaxFrameSize(double maxFrameW, double maxFrameH)
    {
        // Get source image and frame size
        Image sourceImage = getSourceImage();
        sourceImage.waitForImageLoad();
        int oldFrameW = sourceImage.getPixWidth() / getFrameCount();
        int oldFrameH = sourceImage.getPixHeight();

        // Get scale factor needed to shrink source image to max frame size
        double scale = 1;
        if (oldFrameW > maxFrameW)
            scale = maxFrameW / oldFrameW;
        if (oldFrameH > maxFrameH)
            scale = Math.min(scale, maxFrameH / oldFrameH);

        // If this sprite already within max frame size, just return
        if (scale == 1)
            return this;

        // Calculate new frame size for scale and get new sprite image
        int newFrameW = (int) (oldFrameW * scale);
        int newFrameH = (int) (oldFrameH * scale);

        // Get image dpi scale and increase it if screen supports it and there was significant shrinkage
        double dpiScale = sourceImage.getDpiScale();
        if (dpiScale < GFXEnv.getEnv().getScreenDpiScale())
            dpiScale = GFXEnv.getEnv().getScreenDpiScale();

        // Get sprite image at new frame size
        Image newSpriteImage = sourceImage.copyForSizeAndDpiScale(newFrameW * getFrameCount(), newFrameH, dpiScale);

        // Create new sprite for new source image and return
        Sprite newSprite = new Sprite(getSourceURL());
        newSprite._sourceImage = newSpriteImage;
        return newSprite;
    }
}
