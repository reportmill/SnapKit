/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A ListNode that displays in a PopupNode.
 */
public class PopupList <T> extends ListView <T> {
    
    // The number of visible rows to show
    int               _visRowCount = -15;

    // The PopupNode
    PopupWindow       _popup;
    
    // The scroll view holding the popup list
    ScrollView        _scrollView;
    
    // The view given with last show
    View              _showView;
    
    // EventListener to listen to events from show view
    EventListener     _lsnr;
    
/**
 * Creates a new PopupList.
 */
public PopupList()  { setFocusWhenPressed(false); }

/**
 * Returns the number of visible rows to be shown in list.
 */
public int getVisRowCount()  { return _visRowCount; }

/**
 * Sets the number of visible rows to be shown in list (if negative, it's a maximum count).
 */
public void setVisRowCount(int aValue)
{
    _visRowCount = aValue;
}

/**
 * Returns the popup.
 */
public PopupWindow getPopup()
{
    if(_popup!=null) return _popup;
    PopupWindow popup = new PopupWindow(); popup.setFocusable(false);
    _scrollView = new ScrollView(this); _scrollView.setBorder(null);
    popup.setContent(_scrollView); setGrowWidth(true); setGrowHeight(true);
    popup.addPropChangeListener(pce -> popupWindowShowingChanged(), Showing_Prop);
    return _popup = popup;
}

/**
 * Shows the node.
 */
public void show(View aView, double aX, double aY)
{
    // Set preferred size
    PopupWindow popup = getPopup();
    popup.setMaxHeight(_visRowCount>=0? -1 : Math.abs(_visRowCount)*getRowHeight());
    _scrollView.setPrefHeight(_visRowCount>=0? _visRowCount*getRowHeight() : -1);
    
    // Show window
    popup.show(_showView = aView, aX, aY);
}

/**
 * Hides the node.
 */
public void hide()  { getPopup().hide(); }

/**
 * Override to resize if showing and VisRowCount is really Max (negative).
 */
public void setItems(java.util.List <T> theItems)
{
    super.setItems(theItems);
    if(isShowing() && getVisRowCount()<0)
        getPopup().pack();
}

/**
 * Override to hide popuplist.
 */
public void fireActionEvent()
{
    super.fireActionEvent();
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