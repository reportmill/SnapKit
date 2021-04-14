package snap.viewx;
import snap.gfx.Font;
import snap.text.TextLineStyle;
import snap.text.TextStyle;
import snap.util.KeyChain;
import snap.util.SnapUtils;
import snap.view.BoxView;
import snap.view.ScrollView;
import snap.view.View;
import snap.view.ViewOwner;

/**
 * A DevPane to show the console.
 */
public class DevPaneConsole extends ViewOwner {

    // The ConsoleView
    private ConsoleView  _consoleView;

    // A counter of current input
    private int  _inputIndex = 1;

    @Override
    protected View createUI()
    {
        // Create ConsoleView
        _consoleView = new DevConsoleView();
        setFirstFocus(_consoleView);

        // Create ScrollView for ConsoleView
        ScrollView scroll = new ScrollView(_consoleView);

        // Create BoxView for ScrollView
        BoxView boxView = new BoxView(scroll, true, true);
        boxView.setPadding(10, 10, 10, 10);
        return boxView;
    }

    /**
     * A ConsoleView subclass that works with scanner.
     */
    public class DevConsoleView extends ConsoleView {

        /**
         * Constructor.
         */
        public DevConsoleView()
        {
            setPadding(4,4,4,4);
            setPlainText(true);

            // Change Console Font
            TextStyle textStyle = getRichText().getDefaultStyle();
            TextStyle textStyle2 = textStyle.copyFor(new Font("Arial", 12));
            getRichText().setDefaultStyle(textStyle2);

            // Change line spacing
            TextLineStyle lineStyle = getRichText().getDefaultLineStyle();
            TextLineStyle lineStyle2 = lineStyle.copyFor(TextLineStyle.SPACING_KEY, 5);
            getRichText().setDefaultLineStyle(lineStyle2);

            // Set Prompt
            setPrompt("In[" + _inputIndex + "]:= ");
        }

        /**
         * Override to send to process.
         */
        @Override
        protected String executeCommandImpl(String aCommand)
        {
            String expr = aCommand;
            Object val = KeyChain.getValue(new Object(), expr);
            String valStr = SnapUtils.stringValue(val);
            String outStr = "Out[" + _inputIndex + "]:= " + valStr;
            _inputIndex++;
            setPrompt("In[" + _inputIndex + "]:= ");

            return outStr;
        }
    }

}
