package snap.webenv;
import snap.geom.Rect;
import snap.view.WindowView;

/**
 * A WindowHpr to map WindowView to CJWindow.
 */
public class CJWindowHpr extends WindowView.WindowHpr {

    // The snap CJWindow
    protected CJWindow  _winNtv;

    /**
     * Constructor.
     */
    public CJWindowHpr()
    {
        super();
    }

    /**
     * Initializes helper for given window.
     */
    @Override
    public void initForWindow(WindowView aWin)  { _winNtv = new CJWindow(aWin); }

    /**
     * Initializes native window.
     */
    public void initializeNativeWindow()  { _winNtv.initWindow(); }

    /**
     * Window/Popup method: Shows the window.
     */
    public void show()  { _winNtv.show(); }

    /**
     * Window/Popup method: Hides the window.
     */
    public void hide()  { _winNtv.hide(); }

    /**
     * Window/Popup method: Order window to front.
     */
    public void toFront()  { _winNtv.toFront(); }

    /**
     * Registers a view for repaint.
     */
    public void requestPaint(Rect aRect)  { _winNtv.paintViews(aRect); }
}
