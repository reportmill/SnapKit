package snap.gfx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class to manage a set of images.
 */
public class ImageSet {

    // The list of images
    private List<Image>  _images = Collections.EMPTY_LIST;
    
    /**
     * Constructor.
     */
    public ImageSet(List <Image> theImages)
    {
        _images = theImages;
        for(Image img : _images) img.setImageSet(this);
    }

    /**
     * Returns the number of images.
     */
    public int getCount()  { return _images.size(); }

    /**
     * Returns the individual image at index.
     */
    public Image getImage(int anIndex)
    {
        return _images.get(anIndex);
    }

    /**
     * Returns the next image.
     */
    public int getIndex(Image anImage)  { return _images.indexOf(anImage); }

    /**
     * Returns the next image.
     */
    public Image getNext(Image anImage)
    {
        int index = (_images.indexOf(anImage)+1)%getCount();
        return getImage(index);
    }

    /**
     * Returns the image set scaled.
     */
    public Image getImageScaled(double aRatio)
    {
        List<Image> images = new ArrayList<>();
        for(Image img : _images) {
            img = img.cloneForScale(aRatio);
            images.add(img);
        }
        ImageSet iset = new ImageSet(images);
        return iset.getImage(0);
    }

    /**
     * Returns a sheet image.
     */
    public Image getSpriteSheetImage()
    {
        Image img0 = getImage(0);
        int imageW = img0.getPixWidth();
        int imageH = img0.getPixHeight();
        int count = getCount();

        Image imgSheet = Image.getImageForSize(imageW * count, imageH, true);
        Painter pntr = imgSheet.getPainter();
        for(int i = 0; i < count; i++) {
            Image image = getImage(i);
            pntr.drawImage(image, i * imageW, 0);
        }

        // Return
        return imgSheet;
    }
}