package snap.viewx;
import snap.util.SnapUtils;
import snap.view.ScrollView;
import snap.view.View;
import snap.view.ViewOwner;
import snap.view.ViewUtils;

/**
 * This class is a real implementation of Console.
 */
public class DefaultConsole extends ViewOwner implements Console {

    // The Console view
    private ConsoleView _consoleView;

    // The maximum number of console items
    private int _maxItemCount = 1000;

    // Whether console has overflowed
    private boolean _overflowed;

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
        _consoleView = new ConsoleView();

        // Set shared
        if (_shared == null)
            _shared = this;
    }

    /**
     * Shows the given object to user.
     */
    public void show(Object anObj)
    {
        // Handle overflow
        if (getItemCount() + 1 > _maxItemCount) {
            if (!_overflowed) {
                _overflowed = true;
                _consoleView.showObject("Output suspended - Too much output!!!");
            }
        }

        // Forward to ConsoleView
        _consoleView.showObject(anObj);
    }

    /**
     * Resets the console.
     */
    public void resetConsole()
    {
        _consoleView.resetDisplay();
    }

    /**
     * Returns the number of items on the console.
     */
    public int getItemCount()
    {
        return _consoleView.getChildCount();
    }

    /**
     * Returns the console view.
     */
    @Override
    public ConsoleView getConsoleView()  { return _consoleView; }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        ScrollView scrollView = new ScrollView(_consoleView);
        _consoleView.setGrowWidth(true);
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
     * Sets the console created handler.
     */
    private static void handleConsoleCreated()
    {
        Console defaultConsole = getShared();
        View consoleView = defaultConsole.getConsoleView();
        if (!SnapUtils.isWebVM)
            consoleView.setPrefSize(700, 900);
        if (defaultConsole instanceof ViewOwner) {
            ViewOwner viewOwner = (ViewOwner) defaultConsole;
            viewOwner.getWindow().setMaximized(SnapUtils.isWebVM);
            ViewUtils.runLater(() -> viewOwner.setWindowVisible(true));
        }
    }
}
