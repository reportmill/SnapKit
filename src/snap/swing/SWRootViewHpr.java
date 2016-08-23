package snap.swing;
import snap.gfx.Rect;
import snap.view.*;

/**
 * ViewHelper subclass for RootView/SWRootView.
 */
public class SWRootViewHpr <T extends SWRootView> extends ViewHelper <T> {

    /** Creates the native. */
    protected T createNative()  { return (T)new SWRootView(); }

    /** Override to set RootView in SWRootView. */
    public void setView(View aView)  { super.setView(aView); get().setRootView((RootView)aView); }
    
    /** Sets the cursor. */
    public void setCursor(Cursor aCursor)  { get().setCursor(AWT.get(aCursor)); }
    
    /** Registers a view for repaint. */
    public void requestPaint(Rect aRect)  { get().repaint(aRect); }
}