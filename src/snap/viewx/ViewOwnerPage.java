package snap.viewx;
import snap.view.*;

/**
 * A WebPage subclass to wrap a ViewOwner in a WebPage.
 */
public class ViewOwnerPage extends WebPage {

    // The ViewOwner
    ViewOwner              _owner;

/**
 * Creates a new JFXPage for given JFXOwner.
 */
public ViewOwnerPage(ViewOwner anOwner)  { _owner = anOwner; }

/**
 * Returns the ViewOwner for this page.
 */
public ViewOwner getViewOwner()  { return _owner; }

/**
 * Override to return ViewOwner UI.
 */
protected View createUI()  { return getViewOwner().getUI(); }

/**
 * Override to forward to ViewOwner.
 */
public Object getFirstFocus()  { return getViewOwner().getFirstFocus(); }

}