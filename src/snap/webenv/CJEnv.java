package snap.webenv;
import snap.gfx.FontFile;
import snap.gfx.GFXEnv;
import snap.gfx.Image;
import snap.gfx.SoundClip;
import snap.util.FileUtils;
import snap.util.Prefs;
import snap.util.SnapEnv;
import snap.web.MIMEType;
import snap.web.WebFile;
import snap.web.WebURL;
import snap.webapi.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A GFXEnv implementation for CheerpJ.
 */
public class CJEnv extends GFXEnv {
    
    // The shared Env
    private static CJEnv  _shared;

    // Font names, Family names
    private static String[]  _fontNames = {
        "Arial", "Arial Bold", "Arial Italic", "Arial Bold Italic",
        "Times New Roman", "Times New Roman Bold", "Times New Roman Italic", "Times New Roman Bold Italic",
    };
    private static String[]  _famNames = { "Arial", "Times New Roman" };

    /**
     * Creates a CJEnv.
     */
    public CJEnv()
    {
        if (_env == null) {
            _env = _shared = this;
        }

        // Set browser platform
        if (Navigator.isWindows()) SnapEnv.isWebVM_Windows = true;
        else if (Navigator.isMac()) SnapEnv.isWebVM_Mac = true;
        else if (Navigator.isIOS()) SnapEnv.isWebVM_iOS = true;
    }

    /**
     * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
     */
    public String[] getFontNames()  { return _fontNames; }

    /**
     * Returns a list of all system family names.
     */
    public String[] getFamilyNames()  { return _famNames; }

    /**
     * Returns a list of all font names for a given family name.
     */
    public String[] getFontNames(String aFamilyName)
    {
        // Get system fonts and create new list for font family
        String[] fonts = getFontNames();
        List<String> familyNames = new ArrayList<>();

        // Iterate over fonts
        for(String name : fonts) {

            // If family name is equal to given family name, add font name
            if(name.contains(aFamilyName) && !familyNames.contains(name))
                familyNames.add(name);
        }

        // Get font names as array and sort
        String[] familyArray = familyNames.toArray(new String[0]);
        Arrays.sort(familyArray);

        // Return
        return familyArray;
    }

    /**
     * Returns a font file for given name.
     */
    public FontFile getFontFile(String aName)
    {
        return new CJFontFile(aName);
    }

    /**
     * Creates image from source.
     */
    public Image getImageForSource(Object aSource)
    {
        return new CJImage(aSource);
    }

    /**
     * Creates image for width, height and alpha and dpi scale (0 = screen dpi, 1 = 72 dpi, 2 = 144 dpi).
     */
    public Image getImageForSizeAndDpiScale(double aWidth, double aHeight, boolean hasAlpha, double dpiScale)
    {
        if (dpiScale <= 0)
            dpiScale = getScreenScale();
        return new CJImage(aWidth, aHeight, hasAlpha, dpiScale);
    }

    /**
     * Returns a sound for given source.
     */
    public SoundClip getSound(Object aSource)
    {
        return new CJSoundClip(aSource);
    }

    /**
     * Creates a sound for given source.
     */
    public SoundClip createSound()  { return null; }

    /**
     * Returns prefs.
     */
    @Override
    public Prefs getPrefs(String aName)
    {
        if (SnapEnv.isJxBrowser)
            return super.getPrefs(aName);
        return new CJPrefs(aName);
    }

    /**
     * Returns the screen resolution.
     */
    public double getScreenResolution()  { return 72; }

    /**
     * Returns the screen scale. Usually 1, but could be 2 for HiDPI/Retina displays.
     */
    public double getScreenScale()  { return Window.getDevicePixelRatio(); }

    /**
     * Tries to open the given file name with the platform reader.
     */
    public void openFile(Object aSource)
    {
        // Get Java File for source
        if (aSource instanceof WebFile)
            aSource = ((WebFile) aSource).getJavaFile();
        if (aSource instanceof WebURL)
            aSource = ((WebURL) aSource).getJavaUrl();
        java.io.File file = FileUtils.getFile(aSource);

        // Get file name, type, bytes
        String filename = file.getName();
        String mimeType = MIMEType.getMimeTypeForPath(filename);
        byte[] fileBytes = FileUtils.getBytes(file);

        // Create file and URL string
        File fileJS = new File(filename, mimeType, fileBytes);
        String fileUrlAddress = fileJS.createURL();

        // Open file url address
        Window.get().open(fileUrlAddress, "_blank");
    }

    /**
     * Download a file.
     */
    @Override
    public void downloadFile(WebFile aFile)
    {
        // Create Blob and get URL address
        byte[] fileBytes = aFile.getBytes();
        String fileType = aFile.getFileType();
        String mimeType = MIMEType.getMimeTypeForFileType(fileType);
        Blob fileBlob = new Blob(fileBytes, mimeType);
        String fileUrlAddress = fileBlob.createURL(); // URL.createObjectURL(blob)

        // Create anchor element and configure url and file name
        HTMLAnchorElement anchorElement = (HTMLAnchorElement) HTMLDocument.getDocument().createElement("a");
        anchorElement.setHref(fileUrlAddress);
        anchorElement.setDownload(aFile.getName());

        // Programmatically click the link to trigger the download
        anchorElement.click();

        // Revoke the object URL after download
        //URL.revokeObjectURL(url);
    }

    /**
     * Tries to open the given URL with the platform reader.
     */
    public void openURL(Object aSource)
    {
        WebURL url = WebURL.getUrl(aSource);
        String urlAddress = url != null ? url.getString() : null;
        if (urlAddress != null)
            urlAddress = urlAddress.replace("!", "");
        System.out.println("Open URL: " + urlAddress);
        Window.get().open(urlAddress, "_blank"); //, "menubar=no");
    }

    /**
     * Plays a beep.
     */
    public void beep()  { }

    /**
     * This is really just here to help with TeaVM.
     */
    public Method getMethod(Class<?> aClass, String aName, Class<?>... theClasses) throws NoSuchMethodException
    {
        return aClass.getMethod(aName, theClasses);
    }

    /**
     * This is really just here to help with TeaVM.
     */
    public void exit(int aValue)  { }

    /**
     * Executes a process.
     */
    @Override
    public Process execProcess(String[] args)  { return new CJProcess(args); }

    /**
     * Returns new CJViewEnv.
     */
    @Override
    protected snap.view.ViewEnv createViewEnv()  { return new CJViewEnv(); }

    /**
     * Returns a shared instance.
     */
    public static CJEnv get()
    {
        if (_shared != null) return _shared;
        return _shared = new CJEnv();
    }
}