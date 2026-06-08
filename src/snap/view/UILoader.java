package snap.view;
import snap.web.WebURL;

/**
 * This class handles loading UI from
 */
public class UILoader {

    /**
     * Loads the snap UI file for given view controller and returns root view.
     */
    public static ParentView loadViewForController(ViewController viewController)
    {
        // Get snap UI file URL for controller (complain if not found)
        WebURL snapUrl = getSnapUrlForController(viewController);
        if (snapUrl == null)
            throw new RuntimeException("UILoader.loadViewForController: Couldn't find source for controller: " + viewController.getClass().getName());

        // Forward
        return loadViewForControllerAndUrl(viewController, snapUrl);
    }

    /**
     * Loads the snap UI file for given view controller and URL and returns root view.
     */
    public static ParentView loadViewForControllerAndUrl(ViewController viewController, WebURL snapUrl)
    {
        ViewArchiver2 archiver = new ViewArchiver2();
        archiver.setOwner(viewController);
        return (ParentView) archiver.readPropObjectFromXmlUrl(snapUrl);
    }

    /**
     * Loads the snap UI file for given class and returns root view.
     */
    public static ParentView loadViewForClass(Class<?> aClass)
    {
        // Get snap UI file URL for controller (complain if not found)
        WebURL snapUrl = getSnapUrlForClass(aClass);
        if (snapUrl == null)
            throw new RuntimeException("UILoader.loadViewForClass: Couldn't find source for controller: " + aClass.getName());

        // Forward
        return loadViewForControllerAndUrl(null, snapUrl);
    }

    /**
     * Loads the snap UI file for given URL and returns root view.
     */
    public static ParentView loadViewForUrl(WebURL snapUrl)
    {
        return loadViewForControllerAndUrl(null, snapUrl);
    }

    /**
     * Loads the snap UI file for given snap UI string and returns root view.
     */
    public static View loadViewForString(String snapString)
    {
        return loadViewForControllerAndString(null, snapString);
    }

    /**
     * Loads the snap UI file for given controller and snap UI string and returns root view.
     */
    public static View loadViewForControllerAndString(ViewController viewController, String snapString)
    {
        ViewArchiver2 archiver = new ViewArchiver2();
        archiver.setOwner(viewController);
        return (View) archiver.readPropObjectFromXmlString(snapString);
    }

    /**
     * Loads the snap UI file for given bytes and returns root view.
     */
    public static View loadViewForBytes(byte[] fileBytes)
    {
        ViewArchiver2 archiver = new ViewArchiver2();
        return (View) archiver.readPropObjectFromXmlBytes(fileBytes);
    }

    /**
     * Returns a UI source for given view controller.
     */
    public static WebURL getSnapUrlForController(ViewController viewController)
    {
        return getSnapUrlForClass(viewController.getClass());
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

    /**
     * Returns whether to use real classes.
     */
    public static boolean isUseRealClassDefault()  { return ViewArchiver2.isUseRealClassDefault(); }

    /**
     * Sets whether to use real classes.
     */
    public static void setUseRealClassDefault(boolean aValue)  { ViewArchiver2.setUseRealClassDefault(aValue);}
}
