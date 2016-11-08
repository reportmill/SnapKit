/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A ListNode that displays in a PopupNode.
 */
public class PopupList <T> extends ListView <T> {

    // The PopupNode
    PopupWindow       _popup;
    
    // The node that popup was shown around
    View              _view;
    
    // An EventListener to handle events from source node
    EventListener     _lsnr = e -> handleEvent(e);

/**
 * Creates a new PopupList.
 */
public PopupList()  { setFocusWhenPressed(false); }

/**
 * Returns the popup.
 */
public PopupWindow getPopup()
{
    if(_popup!=null) return _popup;
    _popup = new PopupWindow(); _popup.setFocusable(false);
    ScrollView spane = new ScrollView(this); spane.setBorder(null);
    _popup.setContent(spane); setGrowWidth(true); setGrowHeight(true);
    return _popup;
}

/**
 * Shows the node.
 */
public void show(View aView, double aX, double aY)
{
    _view = aView;
    relayout();
    getPopup().show(_view = aView, aX, aY);
    
    // Watch
    if(aView!=null)
        aView.addEventHandler(_lsnr, KeyPress);
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
 * Override to hide popuplist.
 */
public void fireActionEvent()
{
    super.fireActionEvent();
    hide();
}

/**
 * Override to limit pref height.
 */
public double getScrollPrefHeight()  { return getRowHeight()*Math.min(getItems().size(),20); }

/**
 * Handle events.
 */
protected void handleEvent(ViewEvent anEvent)
{
    if(anEvent.isUpArrow() || anEvent.isDownArrow() || anEvent.isEnterKey())
        processEvent(anEvent);
}

}