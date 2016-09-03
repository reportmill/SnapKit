package snap.swing;
import javax.swing.SwingUtilities;
import snap.util.*;
import snap.view.*;
import snap.viewx.*;
import snap.web.*;

/**
 * An app for Snap client desktop applications.
 */
public class SnapApp extends ViewOwner implements Thread.UncaughtExceptionHandler {

    // The WebBrowser
    WebBrowser           _browser;
    
    // Launch args
    static String        _args[], _fname;
    
/**
 * Main.
 */
public static void main(final String args[])
{
    // Set args
    _args = args;
    _fname = getParameter(args, "file");
    
    // If file is local class file, see if we should just run it
    if(StringUtils.startsWithIC(_fname, "Local:") && StringUtils.endsWithIC(_fname, ".class")) {
        WebURL url = WebURL.getURL(_fname);
        WebFile cfile = url.getFile();
        Class cls = ClassUtils.getClass(cfile);
        if(cls!=null && getHasMain(cls)) {
            runMain(cls); return; }
        //if(cls!=null && Application.class.isAssignableFrom(cls)) { Application.launch(cls, args); return; }
    }
    
    // Run this app
    SwingUtilities.invokeLater(() -> new SnapApp().main()); //Application.launch(FXApp.class, args);
}

/**
 * Main.
 */
public void main()
{
    // Install Exception reporter
    Thread.setDefaultUncaughtExceptionHandler(this);

    // Install Snap preferences class
    PrefsUtils.setPrefsClass(SnapApp.class);
    
    // Set URL file in background
    getUI();
    _browser.setURLString(_fname);
    getWindow().setTitle(FilePathUtils.getFileNameSimple(_fname));
    
    // Make window visible
    getWindow().setSaveName("SnapBrowser");
    getWindow().setSaveSize(true);
    getWindow().setVisible(true);
    
    // Exit on close
    //Stage stage = (Stage)getWindow().getNative();
    //stage.setOnCloseRequest(e -> runLater(() ->  System.exit(0)));
}

/**
 * Create UI.
 */
protected View createUI()
{
    String text = "  " + FilePathUtils.getFileNameSimple(getParameter(_args, "file"));
    WebBrowserPane browserPane;
    if(isParameter(_args, "iphone")) browserPane = new WebBrowserPanes.iPhone();
    else browserPane = new WebBrowserPanes.Labeled(text);
    _browser = browserPane.getBrowser();
    return browserPane.getUI();
}

/**
 * Returns given parameter.
 */
static boolean isParameter(String args[], String aName)  { return getArgIndex(args, aName)>=0; }

/**
 * Returns given parameter.
 */
static String getParameter(String args[], String aName)
{
    int index = getArgIndex(args, aName);
    return index>=0 && index+1<_args.length? _args[index+1] : null;
}

/**
 * Returns the arg index for a given arg.
 */
static int getArgIndex(String args[], String aName)
{
    for(int i=0; i<_args.length; i++) if(_args[i].equals(aName)) return i;
    return -1;
}
    
/**
 * Called when exception is thrown at the top level.
 */
public void uncaughtException(Thread th, Throwable t)  { t.printStackTrace(); _browser.showException(t); }

/** Returns whether class file has main method. */
private static boolean getHasMain(Class aClass)
{
    try { return aClass.getMethod("main", String[].class)!=null; }
    catch(Exception e) { return false; }
}

/** Runs the main method of compiled class. */
private static void runMain(Class aClass)
{
    try { aClass.getMethod("main", String[].class).invoke(null, (Object)new String[0]); }
    catch(Exception e) { throw new RuntimeException(e); }
}

}