package snap.swing;
import java.awt.Window;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * A ViewHelper for WindowView/Swing-Window.
 */
public class SWWindowHpr <T extends Window> extends ViewHelper <T> {
    
/** Creates the native. */
protected T createNative()
{
    if(getView(WindowView.class).getType().equals(WindowView.TYPE_MAIN)) return (T)new JFrame();
    return (T)new JDialog();
}

/** Sets the x value. */
public void setX(double aValue)  { get().setLocation((int)aValue, get().getY()); }

/** Sets the y value. */
public void setY(double aValue)  { get().setLocation(get().getX(), (int)aValue); }

/** Sets the width value. */
public void setWidth(double aValue)  { get().setSize((int)aValue, get().getHeight()); }

/** Sets the height value. */
public void setHeight(double aValue)  { get().setSize(get().getWidth(), (int)aValue); }

/** Override to get node as WindowView. */
public WindowView getView()  { return (WindowView)super.getView(); }
    
/**
 * Configure component.
 */
protected void init()
{
    // Get native, node and root pane
    Window win = get();
    WindowView node = getView();
    RootView rpane = node.getRootView();

    // Add component listener to sync win bounds changes with node
    win.addComponentListener(new ComponentAdapter() {
        public void componentMoved(ComponentEvent e) { boundsChanged(); }
        public void componentResized(ComponentEvent e) { boundsChanged(); }
        public void componentShown(ComponentEvent e) { showingChanged(); }
        public void componentHidden(ComponentEvent e) { showingChanged(); }
    });
    
    // Add WindowListener to dispatch window events
    win.addWindowListener(new WindowAdapter() {
        public void windowActivated(WindowEvent anEvent)  { activeChanged(); }
        public void windowDeactivated(WindowEvent anEvent)  { activeChanged(); }
        public void windowOpened(WindowEvent anEvent)  { sendWinEvent(anEvent, ViewEvent.Type.WinOpened); }
        public void windowClosing(WindowEvent anEvent)  { sendWinEvent(anEvent, ViewEvent.Type.WinClosing); }
        public void windowClosed(WindowEvent anEvent)  { }
    });
        
    // Configure JFrame
    if(win instanceof JFrame) { JFrame frame = (JFrame)get();
        
        // Set window attributes: Title, AlwaysOnTop, Modal and Resizable
        frame.setTitle(node.getTitle());
        frame.setAlwaysOnTop(node.isAlwaysOnTop()); //setIconImage(getIconImage());
        frame.setResizable(node.isResizable()); //if(_windowListener!=null) addWindowListener(_windowListener);
    
        // Install WindowView.RootView as ContentPane
        frame.setContentPane(rpane.getNative(JComponent.class));
    }
    
    // Configure JDialog
    else if(win instanceof JDialog) { JDialog frame = (JDialog)get();
    
        // Set window attributes: Title, AlwaysOnTop, Modal and Resizable
        frame.setTitle(node.getTitle());
        frame.setAlwaysOnTop(node.isAlwaysOnTop()); //setIconImage(getIconImage());
        frame.setModal(node.isModal());
        frame.setResizable(node.isResizable());
        if(node.getType()==WindowView.TYPE_UTILITY)
            frame.getRootPane().putClientProperty("Window.style", "small");
        else if(node.getType()==WindowView.TYPE_PLAIN)
            frame.setUndecorated(true);
        //setStyle(getStyle()); //if(_windowListener!=null) addWindowListener(_windowListener);
                
        // Install WindowView.RootView as ContentPane
        frame.setContentPane(rpane.getNative(JComponent.class));
    }
    
    // Size window to root pane and update bounds
    win.pack();
    boundsChanged();
}

/**
 * Checks whether window has been initialized.
 */
public void checkInit()  { if(!_initChecked) { _initChecked = true; init(); } } boolean _initChecked;

/**
 * Show window at given screen x and y.
 */
public void show(View aView, double aSX, double aSY)
{
    // On first show, configure window
    checkInit();
        
    // Get native and node
    Window win = get();
    WindowView node = getView();
    
    // If always-on-top, turn this on (since this is can be turned off in setWindowVisible(false))
    //if(node.isAlwaysOnTop()) win.setAlwaysOnTop(true);

    // Set window visible
    win.setLocation((int)Math.round(aSX),(int)Math.round(aSY));
    win.setVisible(true);
    
    // If window is modal, just return
    if(node.isModal())
        return;
    
    // If window has frame save name, set listener
    if(node.getSaveName()!=null && node.getProp("FrameSaveListener")==null) {
        FrameSaveListener fsl = new FrameSaveListener(node);
        node.setProp("FrameSaveListener", fsl);
        win.addComponentListener(fsl);
    }
    
    // If window is always-on-top or does hide-on-deactivate, add listener to handle app deactivate stuff
    //if(node.isAlwaysOnTop() || node.isHideOnDeactivate()) {
        //if(_actvAdptr==null) _actvAdptr = new ActivationAdapter(win,node.isHideOnDeactivate()));
        //_actvAdptr.setEnabled(true); }
}

/**
 * Hides the window.
 */
public void hide()
{
    get().setVisible(false);
    showingChanged();
}

/**
 * Order window to front.
 */
public void toFront()  { get().toFront(); }

/**
 * Sets the window size to preferred size.
 */
public void setPrefSize()  { get().pack(); }

/**
 * Sets the title of the window.
 */
public void setTitle(String aTitle)
{
    Window win = get();
    if(win instanceof JFrame)
        ((JFrame)win).setTitle(aTitle);
    else if(win instanceof JDialog)
        ((JDialog)win).setTitle(aTitle);
    else System.err.println("WindowHpr.setTitle: Not supported " + win.getClass());
}

/**
 * Sets the title of the window.
 */
public void setResizable(boolean aValue)
{
    Window win = get();
    if(win instanceof JFrame) ((JFrame)win).setResizable(aValue);
    else if(win instanceof JDialog) ((JDialog)win).setResizable(aValue);
    else System.err.println("WindowHpr.setTitle: Not supported " + win.getClass());
}

/**
 * Sets the document file for the window title bar proxy icon.
 */
public void setDocFile(File aFile)
{
    if(get() instanceof RootPaneContainer) { RootPaneContainer rpc = (RootPaneContainer)get();
        JRootPane rpane = rpc.getRootPane(); if(rpane==null) return;
        rpane.putClientProperty("Window.documentFile", aFile);
    }
}

/** 
 * Sets the image property of given object to given string.
 */
public void setImage(Image anImage)  { get().setIconImage(AWT.get(anImage)); }

/**
 * Called when window is shown/hidden.
 */
protected void showingChanged()
{
    Window win = get(); WindowView node = getView();
    boolean showing = get().isShowing();
    ViewUtils.setShowing(node, showing);
    if(showing) node.getRootView().repaint();
}
    
/**
 * Handles active changed.
 */
protected void activeChanged()
{
    Window win = get(); boolean active = win.isActive();
    WindowView node = getView();
    ViewUtils.setFocused(node, active);
    ViewEvent.Type etype = active? ViewEvent.Type.WinActivated : ViewEvent.Type.WinDeactivated;
    if(node.getEventAdapter().isEnabled(etype))
        sendWinEvent(null, etype);
}

/**
 * Handles bounds changed.
 */
protected void boundsChanged()
{
    Window win = get();
    WindowView node = getView();
    RootView rpane = node.getRootView();
    JComponent rpaneNtv = rpane.getNative(JComponent.class);
    node.setBounds(win.getX(),win.getY(),win.getWidth(),win.getHeight());
    java.awt.Point pnt = SwingUtilities.convertPoint(rpaneNtv, 0, 0, win);
    rpane.setBounds(pnt.x, pnt.y, rpaneNtv.getWidth(), rpaneNtv.getHeight());
}

/**
 * Called when Node changes to update native.
 */
public void propertyChange(PropChange aPC)
{
    super.propertyChange(aPC);
    String pname = aPC.getPropertyName();
    if(pname==WindowView.AlwaysOnTop_Prop) get().setAlwaysOnTop(SnapUtils.booleanValue(aPC.getNewValue()));
    else if(pname==WindowView.Image_Prop) setImage((Image)aPC.getNewValue());
    else if(pname==WindowView.Title_Prop) setTitle((String)aPC.getNewValue());
    else if(pname==WindowView.Resizable_Prop) setResizable(SnapUtils.booleanValue(aPC.getNewValue()));
}

/**
 * Sends the given event.
 */
protected void sendWinEvent(WindowEvent anEvent, ViewEvent.Type aType)
{
    View node = getView(); if(!node.getEventAdapter().isEnabled(aType)) return;
    ViewEvent event = node.getEnv().createEvent(node, anEvent, aType, null);
    node.fireEvent(event);
    if(aType==ViewEvent.Type.WinClosing && get() instanceof JFrame && event.isConsumed())
        ((JFrame)get()).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
}

/** A component listener to save frame to preferences on move. */
private static class FrameSaveListener extends ComponentAdapter {
    public FrameSaveListener(WindowView aWN)  { _wnode = aWN; } WindowView _wnode;
    public void componentMoved(ComponentEvent e)  { setFrameString(e); }
    public void componentResized(ComponentEvent e)  { setFrameString(e); }
    private void setFrameString(ComponentEvent e)  { _wnode.saveFrame(); }
}

}