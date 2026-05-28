package snap.viewx;
import snap.gfx.Font;
import snap.view.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * A class to display and process System IN/OUT/ERR.
 * This can be useful to quickly add UI for Java command line programs.
 */
public class ScanPane extends ViewController {
    
    // The ScanView
    private ScanView  _scanView;
    
    // The input stream
    private static BytesInputStream  _in = new BytesInputStream(null);
    
    // The public input stream
    public static final InputStream in = _in;
    
    // The public out and err PrintStreams
    public static PrintStream out = System.out;
    public static PrintStream err = System.err;
    
    /**
     * Constructor.
     */
    public ScanPane()
    {
        out = new SPPrintStream(System.out);
        err = new SPPrintStream(System.err);

        System.setOut(out);
        System.setErr(err);
    }

    /**
     * Returns the ScanView.
     */
    public ScanView getScanView()  { return _scanView; }

    /**
     * Clears the ScanView.
     */
    public void clear()  { _scanView.clear(); }

    /**
     * Creates the UI.
     */
    protected View createUI()
    {
        _scanView = new ScanView();
        setFirstFocus(_scanView);
        ScrollView scrollView = new ScrollView(_scanView);
        scrollView.setPrefSize(640,480);
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
     * A ConsoleView subclass that works with scanner.
     */
    public static class ScanView extends ConsoleTextArea {

        /** Creates a new ScanView. */
        public ScanView()
        {
            setPadding(4,4,4,4);
            setFont(Font.Arial16);
        }

        /** Override to send to process. */
        protected void handleEnterAction()
        {
            String str = getInput();
            _in.add(str);
        }
    }

    /**
     * A PrintStream to stand in for System.out and System.err.
     */
    private class SPPrintStream extends PrintStream {

        /** Creates new SPPrintStream. */
        public SPPrintStream(OutputStream aPS)
        {
            super(aPS);
        }

        /** Override to send to ScanView. */
        public void write(int b)
        {
            super.write(b);

            String str = String.valueOf(Character.valueOf((char) b));
            runLater(() -> _scanView.addChars(str));
        }

        /** Override to send to ScanView. */
        public void write(byte[] buf, int off, int len)
        {
            super.write(buf, off, len);
            String str = new String(buf, off, len);
            runLater(() -> _scanView.addChars(str));
        }
    }

    /**
     * An InputStream that lets you add bytes on the fly.
     */
    public static class BytesInputStream extends InputStream {

        // The byte array
        private byte[]  buf = new byte[0];
        private byte[]  buf2 = new byte[1];

        // The index of the next character to read, the currently marked position, and the number of bytes.
        private int  pos, mark, count;

        // Whether waiting for more input
        private boolean  _waiting;

        // Creates a <code>ByteArrayInputStream</code>
        public BytesInputStream(byte[] buf)
        {
            if (buf != null)
                add(buf);
        }

        /** Adds string to stream. */
        public void add(String aStr)
        {
            add(aStr.getBytes());
        }

        /** Adds bytes to stream. */
        public void add(byte[] theBytes)
        {
            int len = buf.length;
            buf = Arrays.copyOf(buf, len + theBytes.length);
            System.arraycopy(theBytes, 0, buf, len, theBytes.length);
            count = buf.length;
            if (_waiting) {
                synchronized(this) {
                    try { notifyAll(); _waiting = false; }
                    catch(Exception e) { throw new RuntimeException(e); }
                }
            }
        }

        /** Reads the next byte of data from this input stream. */
        @Override
        public int read()
        {
            int len = read(buf2, 0, 1);
            return len > 0 ? buf2[0] : -1;
        }

        /** Reads up to <code>len</code> bytes of data into an array of bytes from this input stream. */
        @Override
        public int read(byte[] theBytes, int off, int len)
        {
            while (pos >= count) {
                synchronized(this) {
                    try { _waiting = true; wait(); }
                    catch(Exception e) { throw new RuntimeException(e); } //return -1;
                }
            }

            int avail = count - pos;
            if (len > avail) len = avail;
            if (len <= 0)
                return 0;
            System.arraycopy(buf, pos, theBytes, off, len);
            pos += len;
            return len;
        }

        /** Skips <code>n</code> bytes of input from this input stream. */
        @Override
        public synchronized long skip(long n)
        {
            long k = count - pos;
            if (n < k) {
                k = n < 0 ? 0 : n;
            }
            pos += k;
            return k;
        }

        /** Returns the number of remaining bytes that can be read (or skipped over) from this input stream. */
        public synchronized int available() { return count - pos; }

        /** Tests if this <code>InputStream</code> supports mark/reset. */
        public boolean markSupported() { return true; }

        /** Set the current marked position in the stream. */
        public void mark(int readAheadLimit) { mark = pos; }

        /** Resets the buffer to the marked position. */
        public synchronized void reset() { pos = mark; }

        /** Closing a {@code BytesArrayInputStream} has no effect. */
        public void close() throws IOException  { }
    }

    /** Steals from system in and adds to ScanPane in. */
    /*private static void activateSystemInStealer() {
        if (!_activated) { _activated = true; new Thread(() -> activateSystemInStealer()).start(); return; }
        BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
        try { for (String line=bin.readLine(); line!=null; line=bin.readLine()) {
            _shared._scanView.addChars(line + '\n'); _in.add(line + '\n');
        }} catch(Exception e) { throw new RuntimeException(e); }
    } static boolean _activated; */
}