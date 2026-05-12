/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Rect;
import snap.gfx.Painter;
import snap.gfx.RoughPainter;

/**
 * The top level View in a window.
 */
public class RootView extends ParentView {
    
    // The content
    private View  _content;

    // Whether to paint rough
    private boolean _paintRough;
    
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
        View old = _content; if(aView == old) return;
        if (_content != null)
            removeChild(_content);
        _content = aView;
        if (_content != null)
            addChild(_content);
        firePropChange(Content_Prop, old, _content);
    }

    /**
     * Returns whether to paint rough.
     */
    public boolean isPaintRough()  { return _paintRough; }

    /**
     * Sets whether to paint rough.
     */
    public void setPaintRough(boolean aValue)
    {
        if (aValue == isPaintRough()) return;
        _paintRough = aValue;
        repaint();
    }

    /**
     * Override to return this RootView.
     */
    @Override
    public RootView getRootView()  { return this; }

    /**
     * Override to register for layout.
     */
    protected void setNeedsLayoutDeep(boolean aVal)
    {
        if (aVal == isNeedsLayoutDeep()) return;
        super.setNeedsLayoutDeep(aVal);
        ViewUpdater updater = getUpdater();
        if (updater != null)
            updater.relayoutLater();
    }

    /**
     * Override to handle PaintRough.
     */
    @Override
    protected void paintAll(Painter aPntr)
    {
        if (_paintRough) {
            RoughPainter roughPainter = new RoughPainter(aPntr);
            roughPainter.setFillStyle(RoughPainter.FillStyle.CROSS_HATCH);
            super.paintAll(roughPainter);
        }

        else super.paintAll(aPntr);
    }

    /**
     * Override to actually paint in this RootView.
     */
    protected void repaintInParent(Rect aRect)  { repaint(); }

    /**
     * Override to return Box layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new BoxViewLayout(this, _content, true, true); }
}