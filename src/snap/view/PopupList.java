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
    
    // The node that popup was shown around
    View              _view;
    
    // An EventListener to handle events from source node
    EventListener     _lsnr = e -> handleEvent(e);

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
    _popup = new PopupWindow(); _popup.setFocusable(false);
    _scrollView = new ScrollView(this); _scrollView.setBorder(null);
    _popup.setContent(_scrollView); setGrowWidth(true); setGrowHeight(true);
    return _popup;
}

/**
 * Shows the node.
 */
public void show(View aView, double aX, double aY)
{
    // Set preferred size
    getPopup().setMaxHeight(_visRowCount>=0? -1 : Math.abs(_visRowCount)*getRowHeight());
    _scrollView.setPrefHeight(_visRowCount>=0? _visRowCount*getRowHeight() : -1);
    
    _view = aView;
    relayout();
    getPopup().show(_view = aView, aX, aY);
    
    // Watch
    if(aView!=null)
        aView.addEventFilter(_lsnr, KeyPress);
}

/**
 * Hides the node.
 */
public void hide()  { getPopup().hide(); }

/**
 * Override to remove Node event listener when not showing.
 */
protected void setShowing(boolean aValue)
{
    if(aValue==isShowing()) return; super.setShowing(aValue);
    if(!isShowing()) {
        _view.removeEventHandler(_lsnr, KeyPress); _view = null; }
}

/**
 * Override to resize if showing and VisRowCount is really Max (negative).
 */
public void setItems(java.util.List <T> theItems)
{
    super.setItems(theItems);
    if(isShowing() && getVisRowCount()<0)
        getPopup().getHelper().setPrefSize();
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
 * Handle events.
 */
protected void handleEvent(ViewEvent anEvent)
{
    if(anEvent.isUpArrow() || anEvent.isDownArrow() || anEvent.isEnterKey())
        processEvent(anEvent);
}

}