package snap.view;
import snap.web.WebURL;

/**
 * This class handles loading UI from
 */
public class UILoader {

    /**
     * Loads the snap UI file for given view owner and returns root view.
     */
    public static ParentView loadViewForOwner(ViewOwner anOwner)
    {
        // Get snap UI file URL for owner (complain if not found)
        WebURL snapUrl = getSnapUrlForOwner(anOwner);
        if (snapUrl == null)
            throw new RuntimeException("UILoader.loadViewForOwner: Couldn't find source for owner: " + anOwner.getClass().getName());

        // Forward
        return loadViewForOwnerAndUrl(anOwner, snapUrl);
    }

    /**
     * Loads the snap UI file for given view owner and URL and returns root view.
     */
    public static ParentView loadViewForOwnerAndUrl(ViewOwner anOwner, WebURL snapUrl)
    {
        ViewArchiver archiver = new ViewArchiver();
        archiver.setOwner(anOwner);
        return (ParentView) archiver.readXmlFromUrl(snapUrl);
    }

    /**
     * Loads the snap UI file for given class and returns root view.
     */
    public static ParentView loadViewForClass(Class<?> aClass)
    {
        // Get snap UI file URL for owner (complain if not found)
        WebURL snapUrl = getSnapUrlForClass(aClass);
        if (snapUrl == null)
            throw new RuntimeException("UILoader.loadViewForClass: Couldn't find source for owner: " + aClass.getName());

        // Forward
        return loadViewForOwnerAndUrl(null, snapUrl);
    }

    /**
     * Loads the snap UI file for given URL and returns root view.
     */
    public static ParentView loadViewForUrl(WebURL snapUrl)
    {
        return loadViewForOwnerAndUrl(null, snapUrl);
    }

    /**
     * Loads the snap UI file for given snap UI string and returns root view.
     */
    public static View loadViewForString(String snapString)
    {
        return loadViewForOwnerAndString(null, snapString);
    }

    /**
     * Loads the snap UI file for given owner and snap UI string and returns root view.
     */
    public static View loadViewForOwnerAndString(ViewOwner anOwner, String snapString)
    {
        ViewArchiver archiver = new ViewArchiver();
        archiver.setOwner(anOwner);
        return (View) archiver.readXmlFromString(snapString);
    }

    /**
     * Loads the snap UI file for given bytes and returns root view.
     */
    public static View loadViewForBytes(byte[] fileBytes)
    {
        ViewArchiver archiver = new ViewArchiver();
        return (View) archiver.readXmlFromBytes(fileBytes);
    }

    /**
     * Returns a UI source for given view owner.
     */
    public static WebURL getSnapUrlForOwner(ViewOwner anOwner)
    {
        return getSnapUrlForClass(anOwner.getClass());
    }

    /**
     * Returns a UI source for given class.
     */
    public static WebURL getSnapUrlForClass(Class<?> aClass)
    {
        // Look for snap file with same name as class
        String filename = aClass.getSimpleName() + ".snp";
        WebURL snapUrl = WebURL.getResourceUrl(aClass, filename);
        if (snapUrl != null)
            return snapUrl;

        // Try again for superclass
        return aClass != Object.class ? getSnapUrlForClass(aClass.getSuperclass()) : null;
    }
}
