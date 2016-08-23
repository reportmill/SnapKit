package snap.viewx;
import java.util.*;
import snap.util.*;
import snap.view.*;

/**
 * This class provides a UI panel to inform users that an exception was hit and send info back to developer.
 * 
 * Activate by setting in thread:
 * 
 *   ExceptionReporter er = new ExceptionReporter();
 *   er.setURL("http://www.website.com/cgi-bin/cgiemail/email/snap-exception.txt");
 *   er.setInfo("MyApp Version X, Build Date: " + MyUtils.getBuildDate());
 *   Thread.setDefaultUncaughtExceptionHandler(er);
 */
public class ExceptionReporter extends ViewOwner implements Thread.UncaughtExceptionHandler {
    
    // The cgimail template URL
    String         _url = "http://www.website.com/cgi-bin/cgiemail/email/exception.txt";
    
    // An info string
    String         _info = "MyApp Version 1.0, BuildDate: Unknown";
    
    // Tells whether this exception reporter has been run before
    boolean        _done = false;
    
/**
 * Returns the URL.
 */
public String getURL()  { return _url; }

/**
 * Sets the URL.
 */
public void setURL(String aURL)  { _url = aURL; }

/**
 * Returns the info string.
 */
public String getInfo()  { return _info; }

/**
 * Sets the info string.
 */
public void setInfo(String aStr)  { _info = aStr; }

/**
 * Creates a new exception reporter for given throwable.
 */
public void uncaughtException(Thread t, Throwable aThrowable)
{
    // Get root exception
    while(aThrowable.getCause()!=null)
        aThrowable = aThrowable.getCause();
    
    // Go ahead and print stack trace
    aThrowable.printStackTrace();
    
    // If exception reporting not enabled, just return (otherwise mark done, because we only offer this once)
    if(_done || !PrefsUtils.prefs().getBoolean("ExceptionReportingEnabled", true))
        return;
    else _done = true;

    // Set preferred size
    getUI().setPrefSize(585, 560);
    
    // Default user/email values in UI
    setViewValue("UserText", PrefsUtils.prefs().get("ExceptionUserName", ""));
    setViewValue("EmailText", PrefsUtils.prefs().get("ExceptionEmail", ""));
    
    // Start the exception text with environment info
    StringBuffer eBuffer = new StringBuffer();
    eBuffer.append(getInfo() + "\n");
    eBuffer.append("Java VM: " + System.getProperty("java.version"));
    eBuffer.append(" (" + System.getProperty("java.vendor") + ")\n");
    eBuffer.append("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")\n\n");
    
    // Get the backtrace of the throwable into a string and append it to our exception text
    eBuffer.append("Backtrace:\n");
    eBuffer.append(StringUtils.getStackTraceString(aThrowable));
    
    // Finally, set the exception text in the UI
    setViewValue("BacktraceText", eBuffer.toString());
    getView("BacktraceText", TextViewBase.class).setSelStart(0);
    getView("BacktraceText", TextViewBase.class).setSelEnd(0);
    
    // Run panel (just return if cancelled)
    DialogBox dbox = new DialogBox("ReportMill Exception Reporter");
    dbox.setContent(getUI()); dbox.setOptions("Submit", "Cancel");
    if(!dbox.showConfirmDialog(null)) return;

    // Update preferences and send exception
    PrefsUtils.prefsPut("ExceptionUserName", getViewStringValue("UserText"));
    PrefsUtils.prefsPut("ExceptionEmail", getViewStringValue("EmailText"));
    sendException();
}

/**
 * Send exception via cgiemail at reportmill.com.
 */
public void sendException()
{        
    // Set keys user-email, user-name, user-comment, and exception represent (they are used in cgiemail template)
    final Map map = new HashMap();
    map.put("user-name", getViewStringValue("UserText"));
    map.put("user-email", getViewStringValue("EmailText"));
    map.put("user-comment", getViewStringValue("ScenarioText"));
    map.put("exception", getViewStringValue("BacktraceText"));
        
    // Send email in background thread
    new Thread() { public void run() {
        Exception e = URLUtils.sendCGIEmail(_url, map);
        if(e!=null) e.printStackTrace();
    }}.start();
}

}