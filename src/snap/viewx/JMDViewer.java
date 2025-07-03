package snap.viewx;
import snap.view.*;
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

    // The console
    private JMDRunConsole _console = new JMDRunConsole();

    /**
     * Constructor.
     */
    public JMDViewer(Class<?> mainClass)
    {
        _mainClass = mainClass;
        DefaultConsole.setConsoleClassLoader(mainClass.getClassLoader());
        runJavaMarkdown();
    }

    /**
     * Returns the main file.
     */
    public WebFile getMainFile()
    {
        if (_mainFile != null) return _mainFile;
        WebURL mainFileUrl = WebURL.getResourceUrl(_mainClass, _mainClass.getSimpleName() + ".jmd");
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
        Console.setShared(_console);

        // Create Markdown view
        WebFile mainFile = getMainFile();
        _markDownView = new MarkDownView();
        _markDownView.setMarkDown(mainFile.getText());
        _markDownView.setGrowWidth(true);

        // Iterate over markdown view runnables and run methods
        ViewList children = _markDownView.getChildren();
        int runnableCount = 0;
        for (View child : children) {
            if (Objects.equals(child.getName(), "Runnable"))
                runMethodForRunnableViewAtIndex((BoxView) child, runnableCount++);
        }

        // Set DoneConsole and run old create handler
        Console.setShared(new JMDDoneConsole());
        if (oldConsoleCreatedHandler != null)
            oldConsoleCreatedHandler.run();
    }

    /**
     * Runs method for runnable view.
     */
    private void runMethodForRunnableViewAtIndex(BoxView runnableBoxView, int index)
    {
        // Run method
        runMethod("method" + index);

        // Add children to runnable box view
        ParentView parentView = (ParentView) _console.getConsoleView();
        View lastChild = parentView.getChildCount() > 0 ? parentView.getChild(0) : null;
        if (lastChild != null)
            runnableBoxView.setContent(lastChild);
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

    /**
     * Console class for Java markdown.
     */
    protected static class JMDRunConsole extends DefaultConsole {

        @Override
        protected void showImpl(Object anObj)
        {
            View replView = getViewForObject(anObj);
            if (!replView.isShowing()) {
                ColView consoleView = (ColView) getConsoleView();
                consoleView.addChild(replView);
            }
        }
    }

    /**
     * Console class for Java markdown.
     */
    private class JMDDoneConsole extends DefaultConsole {

        @Override
        public View getConsoleView()  { return _markDownView; }
    }
}
