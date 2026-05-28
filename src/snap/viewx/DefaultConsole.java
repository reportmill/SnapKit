package snap.viewx;
import snap.gfx.Color;
import snap.util.SnapEnv;
import snap.view.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a real implementation of Console.
 */
public class DefaultConsole extends ViewController implements Console {

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

    // Classloader used by console
    private static ClassLoader _classLoader = DefaultConsole.class.getClassLoader();

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
     * Initializes the UI panel. This method provides the ability to alter any settings or components of the View that
     * were not set by {@link #createUI()}.
     * <br><br>
     * This method is called automatically by SnapKit after the view has been initialized, and does not need to be
     * called inside of an implementation.
     * <br><br>
     * Implementation note: It is not always necessary to implement this method, especially if the {@code createUI()}
     * method was written by hand. It provides a way to add more initialization logic when the class has been loaded
     * from a .snp file.
     */
    @Override
    protected void initUI() {

    }

    /**
     * Called automatically by SnapKit after a user reacts with a UI component, this method allows the resetting of
     * the UI. It will not cause accidental {@code respondUI(ViewEvent)} calls. It allows the user to reset or change
     * aspects of the UI after an interaction, such as might be required for an animation or image draw.
     * <br> <br>
     * This method is overridable with no default implementation.
     */
    @Override
    protected void resetUI() {

    }

    /**
     * Called automatically by SnapKit when it detects a ViewEvent. This method should be overridden to respond to UI
     * controls, and provide feedback to user interactions.
     * <br>
     * If you are coming from a Swing environment, this class serves the same purposes as the action listeners attached
     * to each individual component. In this case, all of the events are funnelled into the same method, making it
     * easier to keep track of interactions. Everything is managed from the same location.
     *
     * @param anEvent
     */
    @Override
    protected void respondUI(ViewEvent anEvent) {

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
     * Returns the console to be used by console.
     */
    public static ClassLoader getConsoleClassLoader()  { return _classLoader; }

    /**
     * Sets the console to be used by console.
     */
    public static void setConsoleClassLoader(ClassLoader aClassLoader)  { _classLoader = aClassLoader; }

    /**
     * Called when console is created.
     */
    private static void handleConsoleCreated()
    {
        Console defaultConsole = getShared();

        // Show console in window
        if (defaultConsole instanceof ViewController) {
            ViewController viewController = (ViewController) defaultConsole;
            View consoleView = viewController.getUI();
            if (!SnapEnv.isWebVM)
                consoleView.setPrefSize(700, 900);
            viewController.getWindow().setMaximized(SnapEnv.isWebVM);
            ViewUtils.runLater(() -> viewController.setWindowVisible(true));
        }
    }
}
