package snap.viewx;
import snap.util.*;
import snap.view.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A DevPane to show the console.
 */
public class DevPaneExceptions extends ViewOwner {

    // The selected index
    private int _selIndex = -1;

    // The Thrown ListView
    private ListView<ThrownException> _thrownExceptionsList;

    // The StackTrace TextView
    private TextView _stackTraceText;

    // The list of exceptions
    private static ThrownException[] _thrownExceptions = new ThrownException[0];

    // Whether to ignore successive exceptions
    private static boolean _ignoreSuccessiveExceptions;

    // Whether exception was hit
    private static boolean _exceptionWasHit;

    // Constants
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
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        _thrownExceptionsList = getView("ThrownListView", ListView.class);
        _thrownExceptionsList.setItemTextFunction(ThrownException::getTitle);
        _stackTraceText = getView("StackTraceText", TextView.class);

        // Initialize UserName, UserEmail
        Prefs prefs = Prefs.getDefaultPrefs();
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

        // Update StackTraceText
        ThrownException thrownException = getSelThrownException();
        _stackTraceText.setEnabled(thrownException != null);
        _stackTraceText.setText(thrownException != null ? thrownException.getStackTraceText() : null);

        // Update SendExceptionButton, IgnoreSuccessiveCheckBox, ClearAllButton
        setViewEnabled("SendExceptionButton", thrownException != null);
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
            case "ThrownListView" -> setSelIndex(anEvent.getSelIndex());

            // Handle SendExceptionButton
            case "SendExceptionButton" -> runLater(this::sendSelectedException);

            // Handle ClearAllButton
            case "ClearAllButton" -> {
                _thrownExceptions = new ThrownException[0];
                thrownExceptionsDidChange();
            }

            // Handle IgnoreSuccessiveCheckBox
            case "IgnoreSuccessiveCheckBox" -> setIgnoreSuccessiveExceptions(anEvent.getBoolValue());

            // Handle TriggerNPEButton
            case "TriggerNPEButton" -> { String str = null; str.length(); }
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
     * Sends the currently selected exception.
     */
    private void sendSelectedException()
    {
        ThrownException thrownException = getSelThrownException();
        if (thrownException != null)
            new ExceptionReporter().showExceptionPanelForExceptionString(thrownException.getStackTraceText());
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
            DevPane devPane = DevPane.getDevPane(currentView); assert devPane != null;
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
     * Sets the App name.
     */
    public static void setAppName(String aValue)  { ExceptionReporter.setAppName(aValue); }

    /**
     * Sets the AppInfo.
     */
    public static void setAppInfo(String aValue)  { ExceptionReporter.setAppInfo(aValue); }

    /**
     * Returns whether to ignore previous exceptions.
     */
    public static boolean isIgnoreSuccessiveExceptions()  { return _ignoreSuccessiveExceptions; }

    /**
     * Sets whether to ignore previous exceptions.
     */
    public static void setIgnoreSuccessiveExceptions(boolean aValue)
    {
        if (aValue == _ignoreSuccessiveExceptions) return;
        _ignoreSuccessiveExceptions = aValue;
    }

    /**
     * A class to hold exceptions.
     */
    private static class ThrownException {

        // The title text
        private String _title;

        // The stack trace text
        private String _stackTraceText;

        /**
         * Constructor.
         */
        public ThrownException(Throwable aThrowable)
        {
            // Set time and stack trace text
            long thrownTime = System.currentTimeMillis();
            _stackTraceText = StringUtils.getStackTraceString(aThrowable);

            // Get root cause
            Throwable rootCause = aThrowable;
            while (rootCause.getCause() != null)
                rootCause = rootCause.getCause();

            // Get and set title
            Date date = new Date(thrownTime);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm");
            String dateStr = dateFormat.format(date);
            _title = dateStr + ' ' + rootCause.getClass().getSimpleName();
        }

        /**
         * Returns title text.
         */
        public String getTitle()  { return _title; }

        /**
         * Returns the stack trace text.
         */
        public String getStackTraceText()  { return _stackTraceText; }
    }
}
