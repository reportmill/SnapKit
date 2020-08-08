/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Rect;

/**
 * The top level View in a window.
 */
public class RootView extends ParentView {
    
    // The content
    private View  _content;
    
    // Constants for properties
    public static final String Content_Prop = "Content";

    /**
     * Creates a RootView.
     */
    public RootView()
    {
        enableEvents(KeyEvents);
        setFocusable(true);
        setFocusPainted(false);
        setFill(ViewUtils.getBackFill());
    }

    /**
     * Returns the content.
     */
    public View getContent()  { return _content; }

    /**
     * Sets the content.
     */
    public void setContent(View aView)
    {
        View old = _content; if(aView==old) return;
        if (_content!=null)
            removeChild(_content);
        _content = aView;
        if (_content!=null)
            addChild(_content);
        firePropChange(Content_Prop, old, _content);
    }

    /**
     * Override to return this RootView.
     */
    public RootView getRootView()  { return this; }

    /**
     * Override to register for layout.
     */
    protected void setNeedsLayoutDeep(boolean aVal)
    {
        if (aVal==isNeedsLayoutDeep()) return;
        super.setNeedsLayoutDeep(aVal);
        ViewUpdater updater = getUpdater();
        if (updater!=null)
            updater.relayoutLater();
    }

    /**
     * Override to actually paint in this RootView.
     */
    protected void repaintInParent(Rect aRect)  { repaint(); }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return BoxView.getPrefWidth(this, _content, aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return BoxView.getPrefHeight(this, _content, aW);
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        BoxView.layout(this, _content, null, true, true);
    }
}