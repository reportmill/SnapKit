package snap.swing;
import javax.swing.SwingUtilities;
import snap.util.*;
import snap.view.*;
import snap.viewx.*;

/**
 * An app for Snap client desktop applications.
 */
public class SnapApp extends ViewOwner implements Thread.UncaughtExceptionHandler {

    // The WebBrowser
    private WebBrowser  _browser;

    // Launch args
    private static String[]  _args;

    // File Name
    private static String  _fileName;

    /**
     * Main.
     */
    public static void main(final String[] args)
    {
        // Set args
        _args = args;
        _fileName = getParameter(args, "file");

        // Run this app
        SwingUtilities.invokeLater(() -> new SnapApp().main());
    }

    /**
     * Main.
     */
    public void main()
    {
        // Install Exception reporter
        Thread.setDefaultUncaughtExceptionHandler(this);

        // Install default snap preferences
        Prefs prefs = Prefs.getPrefsForName("/snap/swing");
        Prefs.setPrefsDefault(prefs);

        // Set URL file in background
        getUI();
        _browser.setURLString(_fileName);
        getWindow().setTitle(FilePathUtils.getFileNameSimple(_fileName));

        // Make window visible
        getWindow().setSaveName("SnapBrowser");
        getWindow().setSaveSize(true);
        getWindow().setVisible(true);
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        String text = "  " + FilePathUtils.getFileNameSimple(getParameter(_args, "file"));
        WebBrowserPane browserPane;
        if (isParameter(_args, "iphone"))
            browserPane = new WebBrowserPanes.iPhone();
        else browserPane = new WebBrowserPanes.Labeled(text);
        _browser = browserPane.getBrowser();
        return browserPane.getUI();
    }

    /**
     * Returns given parameter.
     */
    private static boolean isParameter(String[] args, String aName)
    {
        return getArgIndex(args, aName) >= 0;
    }

    /**
     * Returns given parameter.
     */
    private static String getParameter(String[] args, String aName)
    {
        int index = getArgIndex(args, aName);
        return index >= 0 && index + 1 < _args.length ? _args[index + 1] : null;
    }

    /**
     * Returns the arg index for a given arg.
     */
    private static int getArgIndex(String[] args, String aName)
    {
        for (int i = 0; i < args.length; i++)
            if (args[i].equals(aName)) return i;
        return -1;
    }

    /**
     * Called when exception is thrown at the top level.
     */
    public void uncaughtException(Thread th, Throwable t)
    {
        t.printStackTrace();
        _browser.showException(t);
    }
}