package snap.swing;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import snap.gfx.*;
import snap.view.*;

/**
 * A ViewHelper for JPopupMenu.
 */
public class SWPopupWindowHpr  <T extends SWPopupWindowHpr.SnapPopupMenu> extends ViewHelper <T> {
    
    // PopupMenuListener
    PopupMenuListener      _pl;

/** Creates the native. */
protected T createNative()  { return (T)new SnapPopupMenu(); }

/**
 * Shows the popup at given point relative to given view.
 */
public void show(View aView, double aX, double aY)
{
    // Add PopupWindow.RootView to JPopupMenu
    PopupWindow pwin = getView(PopupWindow.class);
    RootView rview = pwin.getRootView();
    JComponent rviewNtv = rview.getNative(JComponent.class);
    get().add(rviewNtv);
    
    // Get proper bounds for given view and x/y (and available screen space at that point)
    Rect bnds = getPrefBounds(aView, aX, aY); bnds.snap();
    int x = (int)bnds.x, y = (int)bnds.y, bw = (int)bnds.width, bh = (int)bnds.height;
    
    // Set popup size
    get().setPopupSize(bw,bh); //get().setPreferredSize(new java.awt.Dimension(bw,bh)); get().pack();
    get().setBorder(BorderFactory.createEmptyBorder()); // MacOS adds 4pt border to top/bottom
    
    // Get ParentPopup and suppress close if found
    RootView viewRoot = aView!=null? aView.getRootView() : null;
    JComponent viewRootNtv = viewRoot!=null? viewRoot.getNative(JComponent.class) : null;
    SnapPopupMenu pp = viewRootNtv!=null? SwingUtils.getParent(viewRootNtv, SnapPopupMenu.class) : null;
    if(pp!=null) pp.setSuppressNextClose(true);

    // Show popup and set RootView.Showing.
    get().setFocusable(pwin.isFocusable());
    get().show(viewRootNtv, x, y);
    setRootViewShowing(pp);
}

/**
 * Hides the popup.
 */
public void hide()  { get().setVisible(false); }

/**
 * Sets the window size to preferred size.
 */
public void setPrefSize()
{
    Rect bnds = getPrefBounds(null, get().getX(), get().getY()); bnds.snap();
    get().setPopupSize((int)bnds.width, (int)bnds.height);
}

/**
 * Returns the preferred bounds for given view and location.
 */
public Rect getPrefBounds(View aView, double aX, double aY)
{
    // Get X and Y relative to aView.RootView
    RootView rview = aView!=null? aView.getRootView() : null;
    JComponent rviewNtv = rview!=null? rview.getNative(JComponent.class) : null;
    int x = (int)aX, y = (int)aY;
    if(rview!=null && rview!=aView) {
        Point pnt = aView.localToParent(rview, x, y); x = (int)pnt.x; y = (int)pnt.y; }
    
    // Get to best size (why doesn't this happen automatically?)
    PopupWindow pview = getView(PopupWindow.class);
    Size bs = pview.getBestSize();
    int bw = (int)Math.round(bs.getWidth()), bh = (int)Math.round(bs.getHeight());
    
    // If size not available at location, shrink size (because otherwise, window will automatically be moved)
    if(rview!=null) {
        Dimension ss = SwingUtils.getScreenSizeAvailable(rviewNtv, x, y);
        bw = Math.min(bw, ss.width); bh = Math.min(bh,ss.height);
    }
    
    // Return rect
    return new Rect(x, y, bw, bh);
}

/**
 * Sets RootView.Showing with listener to set false (and remove listener).
 */
public void setRootViewShowing(SnapPopupMenu pp)
{
    PopupWindow pwin = getView(PopupWindow.class);
    ViewUtils.setShowing(pwin, true);
    _pl = new PopupMenuListener() {
        public void popupMenuCanceled(PopupMenuEvent e) { }
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            ViewUtils.setShowing(pwin,false); get().removePopupMenuListener(_pl); _pl = null;
            if(pp!=null) SwingUtilities.invokeLater(() -> pp.setVisible(false));
        }
    };
    get().addPopupMenuListener(_pl);
}


/**
 * A JPopupMenu subclass to facilitate PopupWindow features.
 */
public static class SnapPopupMenu extends JPopupMenu {
    
    /** Sets whether to suppress popup hide calls. */
    public void setSuppressNextClose(boolean b)  { _noClose = b; } boolean _noClose;
    
    /** Override to suppress window close. */ 
    public void setVisible(boolean b) { if(b || !_noClose) super.setVisible(b); _noClose = false; }
}

}