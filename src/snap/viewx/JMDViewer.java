package snap.viewx;
import snap.view.BoxView;
import snap.view.ParentView;
import snap.view.View;
import snap.view.ViewUtils;
import snap.web.WebFile;
import snap.web.WebURL;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * This class views a Java markdown file.
 */
public class JMDViewer {

    // The main file
    private WebFile _mainFile;

    // The main class
    private Class<?> _mainClass;

    // Markdown view
    private MarkDownView _markDownView;

    /**
     * Constructor.
     */
    public JMDViewer(Class<?> mainClass)
    {
        _mainClass = mainClass;
        runJavaMarkdown();
    }

    /**
     * Returns the main file.
     */
    public WebFile getMainFile()
    {
        if (_mainFile != null) return _mainFile;
        WebURL mainFileUrl = WebURL.getURL(_mainClass, _mainClass.getSimpleName() + ".jmd");
        return _mainFile = mainFileUrl != null ? mainFileUrl.getFile() : null;
    }

    /**
     * Returns the main class.
     */
    private Class<?> getMainClass()  { return _mainClass; }

    /**
     * Runs markdown.
     */
    private void runJavaMarkdown()
    {
        Runnable oldConsoleCreatedHandler = Console.getConsoleCreatedHandler();
        Console.setConsoleCreatedHandler(null);

        // Create Markdown view
        WebFile mainFile = getMainFile();
        _markDownView = new MarkDownView();
        _markDownView.setMarkDown(mainFile.getText());
        _markDownView.setGrowWidth(true);

        // Iterate over markdown view runnables and run methods
        View[] children = _markDownView.getChildren();
        int runnableCount = 0;
        for (View child : children) {
            if (Objects.equals(child.getName(), "Runnable"))
                runMethod("method" + (runnableCount++));
        }

        // Come back later when console is loaded
        ViewUtils.runLater(() -> runJavaMarkdownFinished(oldConsoleCreatedHandler));
    }

    /**
     * Runs markdown.
     */
    private void runJavaMarkdownFinished(Runnable oldConsoleCreatedHandler)
    {
        Console console = Console.getShared();
        ParentView parentView = (ParentView) console.getConsoleView();

        // Iterate over markdown view runnables and set children
        View[] children = _markDownView.getChildren();
        for (View child : children) {
            if (Objects.equals(child.getName(), "Runnable")) {
                BoxView boxView = (BoxView) child;
                View lastChild = parentView.getChildCount() > 0 ? parentView.getChild(0) : null;
                boxView.setContent(lastChild);
            }
        }

        // Add MarkDownView
        Console.setShared(null);
        Console.setConsoleCreatedHandler(oldConsoleCreatedHandler);
        Console.getShared().show(_markDownView);
    }

    /**
     * Runs the main method.
     */
    private void runMethod(String methodName)
    {
        // Get main method and invoke
        try {
            Class<?> mainClass = getMainClass();
            if (mainClass == null) {
                System.out.println("Can't find main class for: " + getMainFile());
                return;
            }
            Method mainMethod = mainClass.getMethod(methodName);
            mainMethod.invoke(null);
        }

        // Handle exception: Just print - goes to RunTool console
        catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
