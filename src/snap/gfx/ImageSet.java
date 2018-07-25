package snap.gfx;
import java.util.Collections;
import java.util.List;

/**
 * A class to manage a set of images.
 */
public class ImageSet {

    // The list of images
    List <Image>    _images = Collections.EMPTY_LIST;
    
/**
 * Creates a set of images.
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
    System.out.println("GetImage: " + index);
    return getImage(index);
}

}