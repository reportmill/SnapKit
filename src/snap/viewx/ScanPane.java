package snap.viewx;
import java.io.*;
import java.util.Arrays;
import snap.gfx.*;
import snap.text.TextStyle;
import snap.view.*;

/**
 * A class to display and process System IN/OUT/ERR.
 *
 * This can be useful to quickly add UI for Java command line programs.
 */
public class ScanPane extends ViewOwner {
    
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
        ScrollView scroll = new ScrollView(_scanView);
        scroll.setPrefSize(640,480);
        return scroll;
    }

    /**
     * A ConsoleView subclass that works with scanner.
     */
    public static class ScanView extends ConsoleView {

        /** Creates a new ScanView. */
        public ScanView()
        {
            setPadding(4,4,4,4);
            setPlainText(true);
            setFont(new Font("Arial", 16));
            getRichText().setDefaultStyle(new TextStyle(new Font("Arial", 16)));
        }

        /** Override to send to process. */
        protected void processEnterAction()
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
            _scanView.addChars(String.valueOf(Character.valueOf((char)b)));
        }

        /** Override to send to ScanView. */
        public void write(byte[] buf, int off, int len)
        {
            super.write(buf, off, len);
            String str = new String(buf, off, len);
            _scanView.addChars(str);
        }
    }

    /**
     * An InputStream that lets you add bytes on the fly.
     */
    private static class BytesInputStream extends InputStream {

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
            if (buf!=null)
                add(buf);
        }

        /** Adds string to stream. */
        public void add(String aStr)
        {
            add(aStr.getBytes());
        }

        /** Adds bytes to stream. */
        public void add(byte theBytes[])
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
        public int read()
        {
            int len = read(buf2, 0, 1);
            return len>0 ? buf2[0] : -1;
        }

        /** Reads up to <code>len</code> bytes of data into an array of bytes from this input stream. */
        public int read(byte b[], int off, int len)
        {
            while (pos >= count) {
                synchronized(this) {
                    try { _waiting = true; wait(); }
                    catch(Exception e) { throw new RuntimeException(e); } //return -1;
                }
            }

            int avail = count - pos;
            if (len > avail) len = avail;
            if (len <= 0) return 0;
            System.arraycopy(buf, pos, b, off, len);
            pos += len; return len;
        }

        /** Skips <code>n</code> bytes of input from this input stream. */
        public synchronized long skip(long n)
        {
            long k = count - pos;
            if (n < k) { k = n < 0 ? 0 : n; }
            pos += k; return k;
        }

        /** Returns the number of remaining bytes that can be read (or skipped over) from this input stream. */
        public synchronized int available() { return count - pos; }

        /** Tests if this <code>InputStream</code> supports mark/reset. */
        public boolean markSupported() { return true; }

        /** Set the current marked position in the stream. */
        public void mark(int readAheadLimit) { mark = pos; }

        /** Resets the buffer to the marked position. */
        public synchronized void reset() { pos = mark; }

        /** Closing a <tt>BytesArrayInputStream</tt> has no effect. */
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