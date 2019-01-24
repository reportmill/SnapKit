/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A ListView subclass that displays in a PopupWindow.
 */
public class PopupList <T> extends ListView <T> {
    
    // The PopupNode
    PopupWindow       _popup;
    
    // The view given with last show
    View              _showView;
    
    // EventListener to listen to events from show view
    EventListener     _lsnr;
    
/**
 * Creates a new PopupList.
 */
public PopupList()
{
    getListArea().setFocusWhenPressed(false);
    getScrollView().setBorder(null);
}

/**
 * Returns the popup.
 */
public PopupWindow getPopup()
{
    if(_popup!=null) return _popup;
    PopupWindow popup = new PopupWindow(); popup.setFocusable(false);
    popup.setContent(this);
    popup.addPropChangeListener(pce -> popupWindowShowingChanged(), Showing_Prop);
    return _popup = popup;
}

/**
 * Shows the node.
 */
public void show(View aView, double aX, double aY)
{
    // Get popup and set best size
    PopupWindow popup = getPopup();
    popup.pack();
    
    // Show window
    popup.show(_showView = aView, aX, aY);
}

/**
 * Hides the node.
 */
public void hide()  { getPopup().hide(); }

/**
 * Override to resize if showing.
 */
public void setItems(java.util.List <T> theItems)
{
    super.setItems(theItems);
    if(isShowing())
        getPopup().pack();
}

/**
 * Override to hide popuplist.
 */
protected void fireActionEvent(ViewEvent anEvent)
{
    super.fireActionEvent(anEvent);
    hide();
}

/**
 * Called when owner View has KeyPress events.
 */
protected void handleShowViewEvent(ViewEvent anEvent)
{
    if(anEvent.isUpArrow() || anEvent.isDownArrow() || anEvent.isEnterKey())
        processEvent(anEvent);
}

/**
 * Called when PopupWindow is shown/hidden.
 */
protected void popupWindowShowingChanged()
{
    if(_showView==null) return;
    PopupWindow popup = getPopup(); boolean showing = popup.isShowing();
    
    // If showing, add EventListener, otherwise, remove
    if(showing)
        _showView.addEventFilter(_lsnr = e -> handleShowViewEvent(e), KeyPress);
    else { _showView.removeEventFilter(_lsnr, KeyPress); _lsnr = null; _showView = null; }
}

}