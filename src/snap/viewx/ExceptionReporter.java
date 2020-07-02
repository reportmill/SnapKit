/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.util.*;
import snap.view.*;
import snap.web.WebURL;

/**
 * This class provides a UI panel to inform users that an exception was hit and send info back to developer.
 * 
 * Activate by setting in thread:
 * 
 *   ExceptionReporter er = new ExceptionReporter("AppName");
 *   er.setURL("http://www.reportmill.com/cgi-bin/SendMail.py");
 *   er.setToAddress("support@domain.com")
 *   er.setInfo("MyApp Version X, Build Date: " + MyUtils.getBuildDate());
 *   Thread.setDefaultUncaughtExceptionHandler(er);
 */
public class ExceptionReporter extends ViewOwner implements Thread.UncaughtExceptionHandler {
    
    // The app name
    private String  _appName = "Application";
    
    // The URL to post send mail request to
    private String  _url = "http://www.reportmill.com/cgi-bin/SendMail.py";
    
    // The user to send mail to
    private String  _toAddr = "support@domain.com";
    
    // The subject to send mail with
    private String  _subject = "Exception Report";
    
    // An info string
    private String  _info = "MyApp Version 1.0, BuildDate: Unknown";
    
    // Tells whether this exception reporter has been run before
    private boolean  _done = false;

    /**
     * Creates a new ExceptionReporter with given app name.
     */
    public ExceptionReporter(String aName)  { _appName = aName; }

    /**
     * Returns the app name.
     */
    public String getAppName()  { return _appName; }

    /**
     * Returns the URL.
     */
    public String getURL()  { return _url; }

    /**
     * Sets the URL.
     */
    public void setURL(String aURL)  { _url = aURL; }

    /**
     * Returns the user to send mail to.
     */
    public String getToAddress()  { return _toAddr; }

    /**
     * Sets the user to send mail to.
     */
    public void setToAddress(String aStr)  { _toAddr = aStr; }

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
        Prefs prefs = Prefs.get();
        if (_done || !prefs.getBoolean("ExceptionReportingEnabled", true))
            return;
        else _done = true;

        // Set preferred size
        getUI().setPrefSize(585, 560);

        // Default user/email values in UI
        setViewValue("UserText", prefs.getString("ExceptionUserName", ""));
        setViewValue("EmailText", prefs.getString("ExceptionEmail", ""));

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
        getView("BacktraceText", TextView.class).setSel(0,0);

        // Run panel (just return if cancelled)
        DialogBox dbox = new DialogBox("ReportMill Exception Reporter");
        dbox.setContent(getUI()); dbox.setOptions("Submit", "Cancel");
        if (!dbox.showConfirmDialog(null)) return;

        // Update preferences and send exception
        prefs.setValue("ExceptionUserName", getViewStringValue("UserText"));
        prefs.setValue("ExceptionEmail", getViewStringValue("EmailText"));
        sendException();
    }

    /**
     * Send exception via SendMail.py at reportmill.com.
     */
    public void sendException()
    {
        // Get to address
        String toAddr = "support@reportmill.com";

        // Get from address
        String name = getViewStringValue("UserText");
        int nlen = name!=null ? name.length() : 0;
        String email = getViewStringValue("EmailText");
        int elen = email!=null ? email.length() : 0;
        if (nlen>0 && elen>0) email = name + " <" + email + '>';
        else if (nlen>0) email = name;
        else if (elen==0) email = "Anonymous";
        String fromAddr = email;

        // Get subject
        String subject = _appName + " Exception Report";

        // Get body
        String scenario = getViewStringValue("ScenarioText");
        if (scenario==null || scenario.length()==0) scenario = "<Not provided>";
        String btrace = getViewStringValue("BacktraceText");
        String body = String.format("%s\n\nFrom:\n%s\n\nUser Scenario:\n%s\n\n%s", subject, fromAddr, scenario, btrace);

        // Send email in background thread
        new Thread() { public void run() {
            String str = sendMail(getToAddress(), fromAddr, subject, body, _url);
            if (str!=null) System.out.println("ExceptionReporter Response: " + str);
        }}.start();
    }

    /**
     * Sends an email with given from, to, subject, body and SendMail url.
     */
    public static String sendMail(String toAddr, String fromAddr, String aSubj, String aBody, String aURL)
    {
        // Create full message text, create URL, post text bytes and return response string
        String text = String.format("To=%s\nFrom=%s\nSubject=%s\n%s", toAddr, fromAddr, aSubj, aBody);
        WebURL url = WebURL.getURL(aURL);
        byte bytes[] = url.postBytes(text.getBytes());
        return bytes!=null? new String(bytes) : null;
    }
}