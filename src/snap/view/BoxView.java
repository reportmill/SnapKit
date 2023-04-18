/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Pos;
import snap.util.*;

/**
 * A View that holds another view.
 */
public class BoxView extends ParentView implements ViewHost {
    
    // The content view
    private View  _content;

    // Whether to fill width
    private boolean  _fillWidth;
    
    // Whether to fill height
    private boolean  _fillHeight;

    // Constants for properties
    public static final String FillWidth_Prop = "FillWidth";
    public static final String FillHeight_Prop = "FillHeight";
    public static final String Content_Prop = "Content";

    /**
     * Creates a new Box.
     */
    public BoxView()  { }

    /**
     * Creates a new Box for content.
     */
    public BoxView(View aContent)  { setContent(aContent); }

    /**
     * Creates a new Box for content with FillWidth, FillHeight params.
     */
    public BoxView(View aContent, boolean isFillWidth, boolean isFillHeight)
    {
        setContent(aContent);
        setFillWidth(isFillWidth);
        setFillHeight(isFillHeight);
    }

    /**
     * Returns the box content.
     */
    public View getContent()  { return _content; }

    /**
     * Sets the box content.
     */
    public void setContent(View aView)
    {
        // If already set, just return
        if (aView == getContent()) return;

        // Remove old content, set/add new content
        View oldContent = _content;
        if (oldContent != null)
            removeChild(_content);

        // Set/add new
        _content = aView;
        if (_content != null)
            addChild(_content);

        // Fire prop change
        firePropChange(Content_Prop, oldContent, _content);
    }

    /**
     * Returns whether children will be resized to fill width.
     */
    public boolean isFillWidth()  { return _fillWidth; }

    /**
     * Sets whether children will be resized to fill width.
     */
    public void setFillWidth(boolean aValue)
    {
        if (aValue == _fillWidth) return;
        firePropChange(FillWidth_Prop, _fillWidth, _fillWidth = aValue);
        relayout();
    }

    /**
     * Returns whether children will be resized to fill height.
     */
    public boolean isFillHeight()  { return _fillHeight; }

    /**
     * Sets whether children will be resized to fill height.
     */
    public void setFillHeight(boolean aValue)
    {
        if (aValue == _fillHeight) return;
        firePropChange(FillHeight_Prop, _fillHeight, _fillHeight = aValue);
        relayout();
    }

    /**
     * Override to change to CENTER.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER; }

    /**
     * Override.
     */
    protected double getPrefWidthImpl(double aH)
    {
        BoxViewProxy<?> viewProxy = getViewProxy();
        return viewProxy.getPrefWidth(aH);
    }

    /**
     * Override.
     */
    protected double getPrefHeightImpl(double aW)
    {
        BoxViewProxy<?> viewProxy = getViewProxy();
        return viewProxy.getPrefHeight(aW);
    }

    /**
     * Override.
     */
    protected void layoutImpl()
    {
        BoxViewProxy<?> viewProxy = getViewProxy();
        viewProxy.layoutView();
    }

    /**
     * Override to return BoxViewProxy.
     */
    @Override
    protected BoxViewProxy<?> getViewProxy()
    {
        return new BoxViewProxy<>(this);
    }

    /**
     * ViewHost method: Override to return 1 if content is present.
     */
    public int getGuestCount()  { return getContent() != null ? 1 : 0; }

    /**
     * ViewHost method: Override to return content (and complain if index beyond 0).
     */
    public View getGuest(int anIndex)
    {
        if (anIndex > 0)
            throw new IndexOutOfBoundsException("Index " + anIndex + " beyond 0");
        return getContent();
    }

    /**
     * ViewHost method: Override to set content.
     */
    public void addGuest(View aChild, int anIndex)
    {
        if (anIndex > 0)
            System.err.println("BoxView: Attempt to addGuest beyond 0");
        setContent(aChild);
    }

    /**
     * ViewHost method: Override to clear content (and complain if index beyond 0).
     */
    public View removeGuest(int anIndex)
    {
        if (anIndex > 0)
            throw new IndexOutOfBoundsException("Index " + anIndex + " beyond 0");
        View content = getContent();
        setContent(null);
        return content;
    }

    /**
     * Sets the content with animation.
     */
    public void setContentWithAnim(View aView)
    {
        // If already set, just return
        View content = getContent();
        if (aView == content)
            return;

        // Set PrefSize to current size
        double viewW = getWidth();
        double viewH = getHeight();
        setPrefSize(viewW, viewH);
        setClipToBounds(true);

        // Get anim - finished/cleared in case it was running
        ViewAnim anim = getAnim(0).finish().clear();
        anim.setOnFinish(() -> setContentAnimDone(aView));

        // Get end anim
        ViewAnim anim500 = anim.getAnim(500);

        // Handle showing: Add view, config anim and play
        if (aView != null) {
            setContent(aView);
            double newW = aView.getBestWidth(-1);
            double newH = aView.getBestHeight(-1);
            anim500.setPrefSize(newW, newH);
        }

        // Handle hiding: Config anim and play
        else {
            anim500.setPrefSize(0, 0);
        }

        // Start anim
        anim.play();
    }

    /**
     * Called when setContentWithAnim() is done.
     */
    private void setContentAnimDone(View aView)
    {
        if (aView == null)
            setContent(null);
        setPrefSize(-1, -1);
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive FillWidth, FillHeight
        if (isFillWidth())
            e.add(FillWidth_Prop, true);
        if (isFillHeight())
            e.add(FillHeight_Prop, true);
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive FillWidth, FillHeight
        if (anElement.hasAttribute(FillWidth_Prop))
            setFillWidth(anElement.getAttributeBoolValue(FillWidth_Prop));
        if (anElement.hasAttribute(FillHeight_Prop))
            setFillHeight(anElement.getAttributeBoolValue(FillHeight_Prop));
    }

    /**
     * Returns preferred width of layout.
     */
    public static double getPrefWidth(ParentView aParent, View aChild, double aH)
    {
        BoxViewProxy<?> viewProxy = new BoxViewProxy<>(aParent, aChild, false, false);
        return viewProxy.getPrefWidth(aH);
    }

    /**
     * Returns preferred height of layout.
     */
    public static double getPrefHeight(ParentView aParent, View aChild, double aW)
    {
        BoxViewProxy<?> viewProxy = new BoxViewProxy<>(aParent, aChild, false, false);
        return viewProxy.getPrefHeight(aW);
    }

    /**
     * Performs Box layout for given parent, child and fill width/height.
     */
    public static void layout(ParentView aPar, View aChild, boolean isFillWidth, boolean isFillHeight)
    {
        if (aChild == null) return;
        BoxViewProxy<?> viewProxy = new BoxViewProxy<>(aPar, aChild, isFillWidth, isFillHeight);
        viewProxy.layoutView();
    }
}