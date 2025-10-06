package snap.viewx;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.text.TextModel;
import snap.text.TextLineStyle;
import snap.text.TextStyle;
import snap.util.Convert;
import snap.util.KeyChain;
import snap.view.BoxView;
import snap.view.ScrollView;
import snap.view.View;
import snap.view.ViewOwner;
import java.io.PrintStream;

/**
 * A DevPane to show the console.
 */
public class DevPaneConsole extends ViewOwner {

    // The ConsoleView
    private static ConsoleTextArea _consoleTextArea;

    // A counter of current input
    private int  _inputIndex = 1;

    // PrintStreams to capture StdOut, StdErr
    private static ProxyPrintStream _stdOut, _stdErr;

    /**
     * Constructor.
     */
    public DevPaneConsole()
    {
        super();
    }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        // Create ConsoleView
        _consoleTextArea = new DevConsoleView();
        setFirstFocus(_consoleTextArea);

        // Create ScrollView for ConsoleView
        ScrollView scroll = new ScrollView(_consoleTextArea);

        // Create BoxView for ScrollView
        BoxView boxView = new BoxView(scroll, true, true);
        boxView.setPadding(10, 10, 10, 10);
        return boxView;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        if (_stdOut != null) {
            _consoleTextArea.addCharsWithStyle(_stdOut._sb.toString(), _stdOut._textStyle);
            _consoleTextArea.addCharsWithStyle(_stdErr._sb.toString(), _stdErr._textStyle);
        }
    }

    /**
     * Tell DevPaneConsole to start watching StdOut, StdErr.
     */
    public static void captureSystemOutAndErr()
    {
        // Set System out/err to catch console output
        System.setOut(_stdOut = new ProxyPrintStream(System.out));
        System.setErr(_stdErr = new ProxyPrintStream(System.err));
    }

    /**
     * A ConsoleView subclass that works with scanner.
     */
    private class DevConsoleView extends ConsoleTextArea {

        /**
         * Constructor.
         */
        public DevConsoleView()
        {
            setPadding(4,4,4,4);

            // Change Console Font
            TextModel textModel = getTextModel();
            textModel.setDefaultFont(Font.Arial12);

            // Change line spacing
            TextLineStyle lineStyle = textModel.getDefaultLineStyle();
            TextLineStyle lineStyle2 = lineStyle.copyForPropKeyValue(TextLineStyle.Spacing_Prop, 5);
            textModel.setDefaultLineStyle(lineStyle2);

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
            String valStr = Convert.stringValue(val);
            String outStr = "Out[" + _inputIndex + "]:= " + valStr;
            _inputIndex++;
            setPrompt("In[" + _inputIndex + "]:= ");

            return outStr;
        }
    }

    /**
     * A PrintStream to stand in for System.out and System.err.
     */
    private static class ProxyPrintStream extends PrintStream {

        // A buffer to store output when console not active
        private StringBuilder _sb = new StringBuilder();

        // TextStyle
        private TextStyle _textStyle;

        /**
         * Constructor.
         */
        public ProxyPrintStream(PrintStream printStream)
        {
            super(printStream);
            if (printStream == System.err)
                _textStyle = TextStyle.DEFAULT.copyForStyleValue(Color.RED);
        }

        /**
         * Override to send to ScanView.
         */
        public void write(int b)
        {
            // Do normal version
            super.write(b);

            // Write char to console
            String str = String.valueOf(Character.valueOf((char) b));
            writeString(str);
        }

        /**
         * Override to send to ScanView.
         */
        public void write(byte[] buf, int off, int len)
        {
            // Do normal version
            super.write(buf, off, len);

            // Write buff to console
            String str = new String(buf, off, len);
            writeString(str);
        }

        /**
         * Override to send to ScanView.
         */
        public void writeString(String aString)
        {
            // Write buff to console
            if (_consoleTextArea != null)
                _consoleTextArea.addCharsWithStyle(aString, _textStyle);
            else _sb.append(aString);
        }
    }
}
