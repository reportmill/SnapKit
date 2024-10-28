package snap.viewx;
import snap.gfx.Color;
import snap.util.SnapUtils;
import snap.view.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a real implementation of Console.
 */
public class DefaultConsole extends ViewOwner implements Console {

    // The Console view
    private ColView _consoleView;

    // The maximum number of console items
    private int _maxItemCount = 1000;

    // Whether console has overflowed
    private boolean _overflowed;

    // A cache of views for console items
    private Map<Object,View> _itemViewsCache = new HashMap<>();

    // The shared console
    private static Console _shared = null;

    // The
    private static Runnable _consoleCreatedHandler = () -> handleConsoleCreated();

    /**
     * Constructor.
     */
    public DefaultConsole()
    {
        super();

        // Create config ConsoleView
        _consoleView = new ColView();
        _consoleView.setFill(new Color(.99));
        _consoleView.setPadding(5, 5, 5, 5);
        _consoleView.setSpacing(6);
        _consoleView.setGrowWidth(true);

        // Set shared
        if (_shared == null)
            _shared = this;
    }

    /**
     * Shows the given object to user.
     */
    @Override
    public void show(Object anObj)
    {
        // Handle overflow
        if (getItemCount() + 1 > _maxItemCount) {
            if (!_overflowed) {
                _overflowed = true;
                showImpl("Output suspended - Too much output!!!");
            }
        }

        // Forward to ConsoleView
        showImpl(anObj);
    }

    /**
     * Called by shell when there is output.
     */
    protected void showImpl(Object anObj)
    {
        // Get view for output object and add
        View replView = getViewForObject(anObj);
        if (!replView.isShowing()) {

            // Add in event thread
            runLater(() -> _consoleView.addChild(replView));

            // This helps WebVM
            Thread.yield();
        }
    }

    /**
     * Resets the console.
     */
    @Override
    public void resetConsole()
    {
        _consoleView.removeChildren();
        _itemViewsCache.clear();
    }

    /**
     * Returns the number of items on the console.
     */
    @Override
    public int getItemCount()  { return _consoleView.getChildCount(); }

    /**
     * Returns the console view.
     */
    @Override
    public View getConsoleView()  { return _consoleView; }

    /**
     * Creates a view for given object.
     */
    protected View getViewForObject(Object aValue)
    {
        // Handle simple value: just create/return new view
        if (isSimpleValue(aValue))
            return DefaultConsoleUtils.createBoxViewForValue(aValue);

        // Handle other values: Get cached view and create if not yet cached
        View view = _itemViewsCache.get(aValue);
        if (view == null) {
            view = DefaultConsoleUtils.createBoxViewForValue(aValue);
            _itemViewsCache.put(aValue, view);
        }

        // Return
        return view;
    }

    /**
     * Returns whether given value is simple (String, Number, Boolean, Character, Date).
     */
    protected boolean isSimpleValue(Object anObj)
    {
        return anObj instanceof Boolean ||
                anObj instanceof Number ||
                anObj instanceof String ||
                anObj instanceof Date;
    }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        View consoleView = getConsoleView();
        ScrollView scrollView = new ScrollView(consoleView);
        scrollView.setFillWidth(consoleView != null && consoleView.isGrowWidth());
        return scrollView;
    }

    /**
     * Returns the shared console.
     */
    protected static Console getShared()
    {
        if (_shared != null) return _shared;

        // Create default console
        DefaultConsole defaultConsole = new DefaultConsole();

        // Auto-show console
        if (_consoleCreatedHandler != null)
            _consoleCreatedHandler.run();

        // Set and return
        return _shared = defaultConsole;
    }

    /**
     * Sets the shared console.
     */
    protected static void setShared(Console aConsole)  { _shared = aConsole; }

    /**
     * Returns the console created handler.
     */
    protected static Runnable getConsoleCreatedHandler()  { return _consoleCreatedHandler; }

    /**
     * Sets the console created handler.
     */
    protected static void setConsoleCreatedHandler(Runnable aRun)  { _consoleCreatedHandler = aRun; }

    /**
     * Called when console is created.
     */
    private static void handleConsoleCreated()
    {
        Console defaultConsole = getShared();

        // Show console in window
        if (defaultConsole instanceof ViewOwner) {
            ViewOwner viewOwner = (ViewOwner) defaultConsole;
            View consoleView = viewOwner.getUI();
            if (!SnapUtils.isWebVM)
                consoleView.setPrefSize(700, 900);
            viewOwner.getWindow().setMaximized(SnapUtils.isWebVM);
            ViewUtils.runLater(() -> viewOwner.setWindowVisible(true));
        }
    }
}
