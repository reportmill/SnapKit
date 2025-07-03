package snap.viewx;
import snap.util.*;
import snap.view.*;
import snap.web.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * A DevPane to show the console.
 */
public class DevPaneExceptions extends ViewOwner {

    // The selected index
    private int _selIndex = -1;

    // The status of the last send
    private String _sendStatus;

    // The Thrown ListView
    private ListView<ThrownException> _thrownExceptionsList;

    // The StackTrace TextView
    private TextView _stackTraceText;

    // The Description TextView
    private TextView  _descriptionText;

    // The list of exceptions
    private static ThrownException[] _thrownExceptions = new ThrownException[0];

    // The user name
    private static String _userName;

    // The user email
    private static String _userEmail;

    // The app name
    private static String _appName;

    // The app info
    private static String _appInfo;

    // Whether to ignore successive exceptions
    private static boolean _ignoreSuccessiveExceptions;

    // Whether exception was hit
    private static boolean _exceptionWasHit;

    // Constants
    private static final String EXCEPTION_USER_NAME_KEY = "ExceptionUserName";
    private static final String EXCEPTION_USER_EMAIL_KEY = "ExceptionEmail";
    private static final String IGNORE_SUCCESSIVE_EXCEPTIONS_KEY = "IgnoreSuccessiveExceptions";

    /**
     * Constructor.
     */
    public DevPaneExceptions()
    {
        super();
    }

    /**
     * Returns the selected index.
     */
    public int getSelIndex()  { return _selIndex; }

    /**
     * Sets the selected index.
     */
    public void setSelIndex(int aValue)
    {
        int value = Math.min(aValue, _thrownExceptions.length - 1);
        if (aValue == _selIndex) return;
        _selIndex = value;
        resetLater();
    }

    /**
     * Returns the selected exception.
     */
    private ThrownException getSelThrownException()
    {
        int selIndex = getSelIndex();
        return selIndex >= 0 ? _thrownExceptions[selIndex] : null;
    }

    /**
     * Returns the last send exception status.
     */
    private String getSendStatus()  { return _sendStatus; }

    /**
     * Set the last send exception status.
     */
    private void setSendStatus(String aValue)
    {
        _sendStatus = aValue;
        resetLater();
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        _thrownExceptionsList = getView("ThrownListView", ListView.class);
        _thrownExceptionsList.setItemTextFunction(ThrownException::getTitle);
        _stackTraceText = getView("StackTraceText", TextView.class);
        _stackTraceText.getTextBlock().setRichText(false);
        _descriptionText = getView("DescriptionText", TextView.class);
        _descriptionText.getTextBlock().setRichText(false);

        // Initialize UserName, UserEmail
        Prefs prefs = Prefs.getDefaultPrefs();
        _userName = prefs.getString(EXCEPTION_USER_NAME_KEY, "");
        _userEmail = prefs.getString(EXCEPTION_USER_EMAIL_KEY, "");
        _ignoreSuccessiveExceptions = prefs.getBoolean(IGNORE_SUCCESSIVE_EXCEPTIONS_KEY, false);
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Update ThrownListView
        int selIndex = getSelIndex();
        _thrownExceptionsList.setSelIndex(selIndex);

        // Update StackTraceText, DescriptionText
        ThrownException thrownException = getSelThrownException();
        _stackTraceText.setEnabled(thrownException != null);
        _stackTraceText.setText(thrownException != null ? thrownException.getStackTraceText() : null);
        _descriptionText.setEnabled(thrownException != null);
        _descriptionText.setText(thrownException != null ? thrownException.getDescriptionText() : null);

        // Update UserNameText, UserEmailText
        setViewValue("UserNameText", getUserName());
        setViewValue("UserEmailText", getUserEmail());

        // Update SendExceptionButton, SendStatusLabel, IgnoreSuccessiveCheckBox, ClearAllButton
        setViewEnabled("SendExceptionButton", thrownException != null);
        setViewValue("SendStatusLabel", getSendStatus());
        setViewValue("IgnoreSuccessiveCheckBox", isIgnoreSuccessiveExceptions());
        setViewEnabled("ClearAllButton", _thrownExceptions.length > 0);
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        switch (anEvent.getName()) {

            // Handle ThrownListView
            case "ThrownListView": setSelIndex(anEvent.getSelIndex()); break;

            // Handle DescriptionText
            case "DescriptionText": {
                ThrownException thrownException = getSelThrownException();
                thrownException.setDescriptionText(anEvent.getStringValue());
                break;
            }

            // Handle UserNameText, UserEmailText
            case "UserNameText": setUserName(anEvent.getStringValue()); break;
            case "UserEmailText": setUserEmail(anEvent.getStringValue()); break;

            // Handle SendExceptionButton
            case "SendExceptionButton": {
                ThrownException thrownException = getSelThrownException();
                thrownException.setDescriptionText(_descriptionText.getText());
                runLater(this::handleSendExceptionButton);
                break;
            }

            // Handle ClearAllButton
            case "ClearAllButton":
                _thrownExceptions = new ThrownException[0];
                thrownExceptionsDidChange();
                break;

            // Handle IgnoreSuccessiveCheckBox
            case "IgnoreSuccessiveCheckBox": setIgnoreSuccessiveExceptions(anEvent.getBoolValue()); break;

            // Handle TriggerNPEButton
            case "TriggerNPEButton": String str = null; str.length(); break;
        }
    }

    /**
     * Called when ThrownExceptions changes.
     */
    private void thrownExceptionsDidChange()
    {
        _thrownExceptionsList.setItems(_thrownExceptions);
        setSelIndex(_thrownExceptions.length > 0 ? 0 : -1);
    }

    /**
     * Override to show last exception.
     */
    @Override
    protected void setShowing(boolean aValue)
    {
        super.setShowing(aValue);
        if (aValue)
            thrownExceptionsDidChange();
    }

    /**
     * Handle SendExceptionButton.
     */
    private void handleSendExceptionButton()
    {
        setSendStatus("Sending...");
        ThrownException thrownException = getSelThrownException();
        TaskRunner<String> sendExceptionRunner = sendException(thrownException);
        sendExceptionRunner.setOnSuccess(str -> {
            setSendStatus(str);
            runDelayed(() -> setSendStatus(null), 5000);
        });
    }

    /**
     * Shows the given exception.
     */
    public static void showException(Throwable anExc)
    {
        // Print to console
        anExc.printStackTrace();

        // If an exception has already been hit and user wants to ignore successive exceptions, just return
        if (_exceptionWasHit && _ignoreSuccessiveExceptions)
            return;

        //anExc.fillInStackTrace();
        ThrownException thrownException = new ThrownException(anExc);
        _thrownExceptions = ArrayUtils.add(_thrownExceptions, thrownException, 0);
        _exceptionWasHit = true;

        // Show Exception Pane
        View currentView = DevPane.getDefaultDevPaneView();
        if (currentView == null)
            return;
        boolean isShowing = DevPane.isDevPaneShowing(currentView);
        if (!isShowing)
            DevPane.setDevPaneShowing(currentView, true);

        // Show DevPane with Exceptions tab
        ViewUtils.runLater(() -> {
            DevPane devPane = DevPane.getDevPane(currentView);
            devPane.showTabForClass(DevPaneExceptions.class);
            DevPaneExceptions exceptionPane = devPane.getPaneForClass(DevPaneExceptions.class);
            exceptionPane.thrownExceptionsDidChange();
        });
    }

    /**
     * Sets this class to start watching for uncaught exceptions.
     */
    public static void setDefaultUncaughtExceptionHandler()
    {
        Thread.setDefaultUncaughtExceptionHandler((thread,throwable) -> showException(throwable));
    }

    /**
     * Returns user name.
     */
    public static String getUserName()  { return _userName; }

    /**
     * Sets user name.
     */
    public static void setUserName(String aValue)
    {
        if (Objects.equals(aValue, _userName)) return;
        _userName = aValue;
        Prefs.getDefaultPrefs().setValue(EXCEPTION_USER_NAME_KEY, _userName);
    }

    /**
     * Returns user email.
     */
    public static String getUserEmail()  { return _userEmail; }

    /**
     * Sets user email.
     */
    public static void setUserEmail(String aValue)
    {
        if (Objects.equals(aValue, _userEmail)) return;
        _userEmail = aValue;
        Prefs.getDefaultPrefs().setValue(EXCEPTION_USER_EMAIL_KEY, _userEmail);
    }

    /**
     * Returns the full email string.
     */
    private static String getFullEmailString()
    {
        // UserName, UserEmail
        String userName = getUserName();
        String userEmail = getUserEmail();

        // Add UserName <UserEmail> (or "Anonymous" if not set)
        String fromAddr = userName != null && userName.length() > 0 ? userName : "Anonymous";
        if (userEmail != null && userEmail.length() > 0)
            fromAddr += " <" + userEmail + '>';

        // Return
        return fromAddr;
    }

    /**
     * Returns the App name.
     */
    public static String getAppName()  { return _appName; }

    /**
     * Sets the App name.
     */
    public static void setAppName(String aValue)  { _appName = aValue; }

    /**
     * Returns the AppInfo.
     */
    public static String getAppInfo()  { return _appInfo; }

    /**
     * Sets the AppInfo.
     */
    public static void setAppInfo(String aValue)  { _appInfo = aValue; }

    /**
     * Returns whether to ignore previous exceptions.
     */
    public static boolean isIgnoreSuccessiveExceptions()  { return _ignoreSuccessiveExceptions; }

    /**
     * Sets whether to ignore previous exceptions.
     */
    public static void setIgnoreSuccessiveExceptions(boolean aValue)
    {
        if (aValue == _ignoreSuccessiveExceptions) return;;
        _ignoreSuccessiveExceptions = aValue;
    }

    /**
     * Send exception.
     */
    private static TaskRunner<String> sendException(ThrownException thrownException)
    {
        // Get to address, from address and subject
        String toAddr = "support@reportmill.com";
        String fromAddr = getFullEmailString();
        String subject = getAppName() + " Exception Report";

        // Get body
        String scenario = thrownException.getDescriptionText();
        if (scenario == null || scenario.length() == 0) scenario = "<Not provided>";
        String stackTraceText = thrownException.getStackTraceText();
        String body = String.format("%s\n\nFrom:\n%s\n\nUser Scenario:\n%s\n\n%s", subject, fromAddr, scenario, stackTraceText);

        // Wrap sendMail in TaskRunner and start
        TaskMonitor taskMonitor = new TaskMonitor("Send Exception " + thrownException.getTitle());
        TaskRunner<String> taskRunner = new TaskRunner<>(() -> sendMail(toAddr, fromAddr, subject, body));
        taskRunner.setMonitor(taskMonitor);
        taskRunner.start();

        // Return
        return taskRunner;
    }

    /**
     * Sends an email with given from, to, subject, body and SendMail url.
     */
    private static String sendMail(String toAddr, String fromAddr, String aSubj, String aBody)
    {
        // Get URL
        WebURL sendMailUrl = WebURL.getUrl("https://www.reportmill.com/cgi-bin/SendMail.py");
        assert (sendMailUrl != null);

        // Create full message text
        String messageText = String.format("To=%s\nFrom=%s\nSubject=%s\n%s", toAddr, fromAddr, aSubj, aBody);

        // Post text bytes
        byte[] postBytes = messageText.getBytes();
        try {
            WebSite site = sendMailUrl.getSite();
            WebRequest req = new WebRequest(sendMailUrl);
            req.setPostBytes(postBytes);
            WebResponse resp = site.getResponse(req);
            if (resp.getException() != null)                // If response hit exception, throw it
                return resp.getException().getMessage();
            if (resp.getCode() != WebResponse.OK)
                return resp.getCodeString();
            return "Success!";
        }

        // Complain
        catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * A class to hold exceptions.
     */
    private static class ThrownException {

        // The thrown time
        private long _thrownTime;

        // The stack trace text
        private String _stackTraceText;

        // The description text
        private String _descriptionText;

        // The title text
        private String _title;

        /**
         * Constructor.
         */
        public ThrownException(Throwable anExc)
        {
            // Set time and stack trace text
            _thrownTime = System.currentTimeMillis();
            String stackTraceText = StringUtils.getStackTraceString(anExc);
            String stackTraceTextHeader = getStackTraceTextHeader();
            _stackTraceText = stackTraceTextHeader + stackTraceText;

            // Get root cause
            Throwable rootCause = anExc;
            while (rootCause.getCause() != null)
                rootCause = rootCause.getCause();

            // Get and set title
            Date date = new Date(_thrownTime);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm");
            String dateStr = dateFormat.format(date);
            _title = dateStr + ' ' + rootCause.getClass().getSimpleName();
        }

        /**
         * Returns title text.
         */
        public String getTitle()  { return _title; }

        /**
         * Returns the thrown time.
         */
        public long getThrownTime()  { return _thrownTime; }

        /**
         * Returns the stack trace text.
         */
        public String getStackTraceText()  { return _stackTraceText; }

        /**
         * Returns the description text.
         */
        public String getDescriptionText()  { return _descriptionText; }

        /**
         * Sets the description text.
         */
        public void setDescriptionText(String aValue)
        {
            _descriptionText = aValue;
        }

        /**
         * Returns the header for StackTraceText.
         */
        private static String getStackTraceTextHeader()
        {
            // Append AppInfo
            String appInfo = getAppInfo();
            StringBuilder sb = new StringBuilder(appInfo).append('\n');

            // Append Java VM info
            String javaVersion = System.getProperty("java.version");
            String javaVendor = System.getProperty("java.vendor");
            sb.append("Java VM: ").append(javaVersion).append(" (").append(javaVendor).append(")\n");

            // Append OS
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            sb.append("OS: ").append(osName).append(" (").append(osVersion).append(")\n\n");

            // Return string
            return sb.toString();
        }
    }
}
