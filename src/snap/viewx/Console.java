package snap.viewx;
import snap.view.View;

/**
 * This class provides an interface to users.
 */
public interface Console {

    /**
     * Shows the given object to user.
     */
    void show(Object anObj);

    /**
     * Resets the console.
     */
    void resetConsole();

    /**
     * Returns the number of items on the console.
     */
    int getItemCount();

    /**
     * Returns the console view.
     */
    View getConsoleView();

    /**
     * Returns the shared console.
     */
    static Console getShared()  { return DefaultConsole.getShared(); }

    /**
     * Sets the shared console.
     */
    static void setShared(Console aConsole)  { DefaultConsole.setShared(aConsole); }

    /**
     * Returns the console created handler.
     */
    static Runnable getConsoleCreatedHandler()  { return DefaultConsole.getConsoleCreatedHandler(); }

    /**
     * Sets the console created handler.
     */
    static void setConsoleCreatedHandler(Runnable aRun)  { DefaultConsole.setConsoleCreatedHandler(aRun); }

    /**
     * An interface for a helper.
     */
    public interface Helper {
        View createViewForObject(Object anObj);
        String createStringForObject(Object anObj);
    }
}
