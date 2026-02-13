/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.util.*;
import snap.view.*;

/**
 * This class provides a UI panel to inform users that an exception was hit and send info back to developer.
 * <br>
 * Activate by setting in thread:
 * <br>
 *   ExceptionReporter er = new ExceptionReporter("AppName");
 *   er.setURL({@literal <url_to_some_SendMail.py>});
 *   er.setToAddress("support@domain.com")
 *   er.setInfo("MyApp Version X, Build Date: " + MyUtils.getBuildDate());
 *   Thread.setDefaultUncaughtExceptionHandler(er);
 */
public class ExceptionReporter extends ViewOwner implements Thread.UncaughtExceptionHandler {
    
    // Backtrace text
    private String _backtraceText;
    
    // Tells whether this exception reporter has been run before
    private boolean _done = false;

    // The app name
    private static String _appName;

    // An info string, for example: "MyApp Version 1.0, BuildDate: Unknown"
    private static String _appInfo;

    /**
     * Constructor with given app name.
     */
    public ExceptionReporter()
    {
        super();
    }

    /**
     * Returns the app name.
     */
    public static String getAppName()  { return _appName; }

    /**
     * Sets the info string.
     */
    public static void setAppName(String aStr)  { _appName = aStr; }

    /**
     * Returns the app info string.
     */
    public static String getAppInfo()  { return _appInfo; }

    /**
     * Sets the info string.
     */
    public static void setAppInfo(String aStr)  { _appInfo = aStr; }

    /**
     * UncaughtExceptionHandler method: Shows exception reporter panel for given exception.
     */
    @Override
    public void uncaughtException(Thread t, Throwable aThrowable)
    {
        aThrowable.printStackTrace();
        showExceptionPanelForException(aThrowable);
    }

    /**
     * Shows exception reporter panel for given exception string.
     */
    public void showExceptionPanelForException(Throwable aThrowable)
    {
        // Get root exception
        Throwable throwable = aThrowable;
        while (throwable.getCause() != null)
            throwable = throwable.getCause();

        // Get stack trace string and call
        String exceptionString = StringUtils.getStackTraceString(throwable);
        ViewUtils.runLater(() -> showExceptionPanelForExceptionString(exceptionString));
    }

    /**
     * Shows exception reporter panel for given exception string.
     */
    public void showExceptionPanelForExceptionString(String exceptionString)
    {
        // If exception reporting not enabled, just return (otherwise mark done, because we only offer this once)
        if (_done || !Prefs.getDefaultPrefs().getBoolean("ExceptionReportingEnabled", true))
            return;
        _done = true;

        // Get the body text
        _backtraceText = exceptionString;

        // Set preferred size
        getUI().setPrefSize(585, 560);

        // Initialize ScenarioText, BacktraceText
        setViewText("ScenarioText", "");
        setViewText("BacktraceText", _backtraceText);
        getView("BacktraceText", TextView.class).setSel(0,0);

        // Run panel (just return if cancelled)
        DialogBox dialogBox = new DialogBox("ReportMill Exception Reporter");
        dialogBox.setContent(getUI());
        dialogBox.setOptions("Submit", "Cancel");
        if (!dialogBox.showConfirmDialog(null))
            return;

        // Update preferences and send exception
        sendException();
    }

    /**
     * Send exception via SendMail.py at reportmill.com.
     */
    public void sendException()
    {
        // Get from address, subject and scenario text
        String fromAddr = UserInfo.getFullUserEmail();
        String subject = getAppName() + " Exception Report";
        String scenarioText = getViewText("ScenarioText");

        // Get header: From: Joe, User Scenario: ...
        String header = "From: " + fromAddr + "\n\n";
        if (getAppInfo() != null)
            header += getAppInfo() + "\n\n";
        if (scenarioText != null && !scenarioText.isEmpty())
            header += "User Scenario: " + scenarioText + "\n\n";

        // Create body from header + backtrace + system info
        String body = header + "Backtrace:\n" + _backtraceText + "\n\n" + SnapUtils.getSystemInfo();

        // Send email in background thread
        new Thread(() -> {
            String str = UserInfo.sendMail("support@reportmill.com", fromAddr, subject, body);
            if (str!=null) System.out.println("ExceptionReporter Response: " + str);
        }).start();
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Update UserText, EmailText
        setViewValue("UserText", UserInfo.getUserName());
        setViewValue("EmailText", UserInfo.getUserEmail());
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        switch (anEvent.getName()) {

            // Handle UserText, EmailText
            case "UserText" -> UserInfo.setUserName(anEvent.getStringValue());
            case "EmailText" -> UserInfo.setUserEmail(anEvent.getStringValue());
        }
    }
}