package snap.webenv;
import snap.util.ArrayUtils;
import snap.util.SnapUtils;
import snap.view.ViewUtils;
import snap.webapi.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * This Process subclass tries to implement Process for CheerpJ.
 */
public class CJProcess extends Process {

    // The main class name
    private String _mainClassName;

    // The class path
    private String _classPath;

    // The app name
    private String _appName;

    // Whether to use CJDom
    private boolean _useCJDom;

    // The iframe that holds new process
    private HTMLIFrameElement _iframe;

    // The iframe.document
    HTMLDocument _iframeDoc;

    // The div element that holds the console
    private HTMLDivElement _consoleDiv;

    // A listener to make iframe/screen visible when window shown
    private MutationObserver _iframeChildListChangeLsnr;

    // The input stream
    private ReadWriteInputStream _inputStream;

    // The error stream
    private ReadWriteInputStream _errorStream;

    /**
     * Constructor.
     */
    public CJProcess(String[] args)
    {
        super();

        // Get main class and class path
        int classPathArgIndex = ArrayUtils.indexOf(args, "-cp");
        _classPath = args[classPathArgIndex + 1];
        _mainClassName = args[classPathArgIndex + 2];

        // Get app name from main class name
        int dotIndex = _mainClassName.lastIndexOf('.');
        _appName = dotIndex < 0 ? _mainClassName : _mainClassName.substring(dotIndex + 1);

        // If UseCJDom, add CJDom and SnapCJ
        _useCJDom = args[0].equals("java-dom");

        System.out.println("MainClass: " + _mainClassName);
        System.out.println("ClassPath: " + _classPath);

        execProcess(args);
    }

    /**
     * Executes a process.
     */
    public void execProcess(String[] args)
    {
        // Create Standard out/err input streams
        _inputStream = new ReadWriteInputStream();
        //_errorStream = new ReadWriteInputStream();

        // Create IFrame with source to launcher.html (just has script for cjloader in header)
        HTMLDocument doc = HTMLDocument.getDocument();
        _iframe = (HTMLIFrameElement) doc.createElement("iframe");
        _iframe.setSrc("launcher.html");
        String style = "background-color: white; border: none; box-sizing: border-box; box-shadow: grey 1px 1px 8px;";
        String position = "margin: 0; padding: 0; position: absolute; right: 33px; bottom: 3px; width: 50%; height: 75%;";
        _iframe.getStyle().setCssText(style + position);
        _iframe.getStyle().setProperty("z-index", "-9");

        // Add to doc body
        HTMLBodyElement body = doc.getBody();
        body.appendChild(_iframe);

        // Listen for iframe src load
        _iframe.addEventListener("load", e -> handleIframeFinishedLoad());
    }

    /**
     * Called after iframe src is loaded.
     */
    private void handleIframeFinishedLoad()
    {
        // Get/set iframeDoc
        _iframeDoc = _iframe.getContentDocument();

        // If CJDom, add loader script, otherwise add SwingParent div
        //if (_useCJDom) addLoaderScript(); else
        if (!_useCJDom)
            addSwingParentDiv();

        addConsoleDiv();

        // If using CJDom, add cjdom.js
        //if (_useCJDom) { addCJDomScript(); return; }

        // Otherwise just add script
        addMainScript();

        // Listen for iframe child list changes
        _iframeChildListChangeLsnr = new MutationObserver(this::handleIframeChildListChange);
        _iframeChildListChangeLsnr.observe(_iframe.getContentDocument().getBody(), MutationObserver.Option.childList, MutationObserver.Option.subtree);
    }

    /**
     * Adds the SwingParent div used by cheerpjCreateDisplay().
     */
    private void addSwingParentDiv()
    {
        HTMLDivElement swingParentDiv = (HTMLDivElement) _iframeDoc.createElement("div");
        swingParentDiv.setId("SwingParent");
        swingParentDiv.getStyle().setCssText("margin: 0; width: 100%; height: 100%;");
        HTMLBodyElement body = _iframeDoc.getBody();
        body.appendChild(swingParentDiv);
    }

    /**
     * Adds the Console div.
     */
    private void addConsoleDiv()
    {
        _consoleDiv = (HTMLDivElement) _iframeDoc.createElement("div");
        _consoleDiv.setId("console");
        _consoleDiv.getStyle().setProperty("display", "none");
        HTMLBodyElement body = _iframeDoc.getBody();
        body.appendChild(_consoleDiv);

        // Register for mutations
        MutationObserver mutationObserver = new MutationObserver(this::handleConsoleDivChanges);
        mutationObserver.observe(_consoleDiv, MutationObserver.Option.childList);
    }

    /**
     * Adds the main script.
     */
    private void addMainScript()
    {
        // Create script to run main for new class and class path
        HTMLScriptElement mainScript = (HTMLScriptElement) _iframeDoc.createElement("script");
        String scriptText = getMainScriptText();
        mainScript.setText(scriptText);

        // Add script
        HTMLHtmlElement iframeHtml = _iframeDoc.getDocumentElement();
        iframeHtml.appendChild(mainScript);
    }

    /**
     * Returns the main script text.
     */
    private String getMainScriptText()
    {
        // Get args for cheerpjInit()
        String initArgs = "version:" + SnapUtils.getJavaVersionInt();
        if (_useCJDom)
            initArgs += ", natives: cjdomNativeMethods";

        // Create script string
        String script = """
              async function myInit() {
                await cheerpjInit({ {INIT_ARGS} });{CREATE_DISPLAY}
                await cheerpjRunMain('{MAIN_CLASS}', '{CLASS_PATH}');
                document.getElementById('console').appendChild(new Text('Process exited'));
              }
              myInit();
            """;

        // Insert initialization args, CheerpJ display, main class and class path
        script = script.replace("{INIT_ARGS}", initArgs);
        script = script.replace("{CREATE_DISPLAY}", !_useCJDom ? "\n    cheerpjCreateDisplay(-1, -1, document.getElementById('SwingParent'));" : "");
        script = script.replace("{MAIN_CLASS}", _mainClassName);
        script = script.replace("{CLASS_PATH}", _classPath);
        return script;
    }

    /**
     * Adds loader script.
     */
    private void addLoaderScript()
    {
        if (!_classPath.contains("SnapKit"))
            return;

        // Create script to run main for new class and class path
        HTMLScriptElement loaderScript = (HTMLScriptElement) _iframeDoc.createElement("script");
        String scriptText = getLoaderScriptText();
        loaderScript.setText(scriptText);

        // Add script to body
        HTMLBodyElement iframeBody = _iframeDoc.getBody();
        iframeBody.appendChild(loaderScript);
    }

    /**
     * Returns the loader script text.
     */
    private String getLoaderScriptText()
    {
        String script = """
            var iframe = document.createElement('iframe');
            iframe.id = 'snap_loader'; iframe.width = '100%'; iframe.height = '100%';
            iframe.style = 'margin: 0; padding: 0; border: none;';
            iframe.src = 'https://reportmill.com/SnapCode/loader/#{APP_NAME}';
            document.body.appendChild(iframe);
        """;

        // Insert app name and return
        return script.replace("{APP_NAME}", _appName);
    }

    /**
     * Adds CJDom script.
     */
    private void addCJDomScript()
    {
        // Create script to import cjdom.js
        HTMLScriptElement cjdomScript = (HTMLScriptElement) _iframeDoc.createElement("script");
        cjdomScript.setSrc("cjdom.js");

        // Add script to <html> element
        HTMLHtmlElement iframeHtml = _iframeDoc.getDocumentElement();
        iframeHtml.appendChild(cjdomScript);

        // Listen for load then add main script, otherwise they will load at same time instead of in order
        cjdomScript.addEventListener("load", e -> addMainScript());
    }

    /**
     * Override to remove iframe from parent.
     */
    @Override
    public void destroy()
    {
        if (_iframe == null) return;

        // Remove iframe from parent
        CJUtils.removeElementWithFadeAnim(_iframe, 200);
        _iframe = null;

        // Close input stream
        try { _inputStream.close(); }
        catch (Exception ignore) { }
    }

    @Override
    public OutputStream getOutputStream()  { return null; }

    @Override
    public InputStream getInputStream()  { return _inputStream; }

    @Override
    public InputStream getErrorStream()  { return _errorStream; }

    @Override
    public int waitFor() { return 0; }

    @Override
    public int exitValue()  { return 0; }

    /**
     * Called to handle changes to console div.
     */
    private void handleConsoleDivChanges(MutationRecord[] mutationRecords)
    {
        String consoleDivText = _consoleDiv.getInnerText();
        if (consoleDivText.length() > _inputStream._writeBytesLength) {
            String newText = consoleDivText.substring(_inputStream._writeBytesLength);
            _inputStream.addString(newText);
            if (newText.contains("exited"))
                ViewUtils.runLater(() -> destroy());
        }
    }

    /**
     * Called to handle child list changes to main iframe and make iframe order front if swing/snapkit window created.
     */
    private void handleIframeChildListChange(MutationRecord[] mutationRecords)
    {
        // Iterate over mutation records
        for (MutationRecord mutationRecord : mutationRecords) {
            Node[] addedNodes = mutationRecord.getAddedNodes();
            for (Node addedNode : addedNodes) {
                if (addedNode instanceof HTMLDivElement div) {

                    // If Swing/Snapkit window, make iframe visible and disconnect observer
                    if (Objects.equals(div.getId(), "WindowDiv") || div.getClassName().startsWith("cjTitleBar")) {
                        _iframe.getStyle().setProperty("z-index", "1");
                        _iframeChildListChangeLsnr.disconnect();
                        return;
                    }
                }
            }
        }
    }

    /**
     * An InputStream that lets you add bytes on the fly.
     */
    private static class ReadWriteInputStream extends InputStream {

        // The byte array to write to
        private byte[] _writeBytesBuffer = new byte[0];

        // The byte array to read from
        private byte[] _readBytesBuffer = new byte[1];

        // The index of the next character to read
        private int _readBytesIndex;

        // The currently marked position
        private int _markedIndex;

        // The number of bytes write bytes.
        private int _writeBytesLength;

        // Whether waiting for more input
        private boolean  _waiting;

        // Whether closed
        private boolean _closed;

        /** Constructor */
        public ReadWriteInputStream()
        {
            super();
        }

        /** Adds string to stream. */
        public void addString(String aStr)
        {
            byte[] bytes = aStr.getBytes();
            addBytes(bytes);
        }

        /** Adds bytes to stream. */
        public void addBytes(byte[] addBytes)
        {
            // Add new bytes to write buffer
            int oldLength = _writeBytesBuffer.length;
            _writeBytesBuffer = Arrays.copyOf(_writeBytesBuffer, oldLength + addBytes.length);
            System.arraycopy(addBytes, 0, _writeBytesBuffer, oldLength, addBytes.length);
            _writeBytesLength = _writeBytesBuffer.length;

            // If waiting, wake up
            if (_waiting) {
                synchronized (this) {
                    try { notifyAll(); }
                    catch(Exception e) { throw new RuntimeException(e); }
                }
            }
        }

        /** Reads the next byte of data from this input stream. */
        @Override
        public int read()
        {
            int len = read(_readBytesBuffer, 0, 1);
            return len > 0 ? _readBytesBuffer[0] : -1;
        }

        /** Reads up to <code>len</code> bytes of data into an array of bytes from this input stream. */
        @Override
        public int read(byte[] theBytes, int offset, int length)
        {
            // If closed, just return EOF
            if (_closed)
                return -1;

            // If no bytes available, make read thread wait
            while (_readBytesIndex >= _writeBytesLength) {
                synchronized (this) {
                    try { _waiting = true; wait(); }
                    catch(Exception ignore) { }
                    finally { _waiting = false; }
                    if (_closed)
                        return -1;
                }
            }

            // Get available bytes to read - if none, return EOF
            int availableBytesCount = _writeBytesLength - _readBytesIndex;
            if (availableBytesCount <= 0 || _closed)
                return -1;

            // If buffer larger than available bytes, trim bytes read
            if (length > availableBytesCount)
                length = availableBytesCount;

            // Copy bytes
            System.arraycopy(_writeBytesBuffer, _readBytesIndex, theBytes, offset, length);
            _readBytesIndex += length;

            // Return bytes read
            return length;
        }

        /** Skips <code>n</code> bytes of input from this input stream. */
        @Override
        public synchronized long skip(long n)
        {
            long k = _writeBytesLength - _readBytesIndex;
            if (n < k) {
                k = n < 0 ? 0 : n;
            }
            _readBytesIndex += k;
            return k;
        }

        /** Returns the number of remaining bytes that can be read (or skipped over) from this input stream. */
        public synchronized int available() { return _writeBytesLength - _readBytesIndex; }

        /** Tests if this <code>InputStream</code> supports mark/reset. */
        public boolean markSupported() { return true; }

        /** Set the current marked position in the stream. */
        public void mark(int readAheadLimit) { _markedIndex = _readBytesIndex; }

        /** Resets the buffer to the marked position. */
        public synchronized void reset() { _readBytesIndex = _markedIndex; }

        /** Closing a <tt>BytesArrayInputStream</tt> has no effect. */
        public void close()
        {
            _closed = true;

            // If waiting, wake up
            if (_waiting) {
                synchronized (this) {
                    try { notifyAll(); }
                    catch(Exception e) { throw new RuntimeException(e); }
                }
            }
        }
    }
}
