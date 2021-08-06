/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.util.*;

/**
 * A View that holds another view.
 */
public class BoxView extends ParentView implements ViewHost {
    
    // The content view
    private View  _content;

    // The spacing between nodes
    private double  _spacing;
    
    // Whether to fill width
    private boolean  _fillWidth;
    
    // Whether to fill height
    private boolean  _fillHeight;

    // Whether child will crop to height if not enough space available
    private boolean  _cropHeight;

    // Constants for properties
    public static final String FillWidth_Prop = "FillWidth";
    public static final String FillHeight_Prop = "FillHeight";
    
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
        if (_content != null) removeChild(_content);
        _content = aView;
        if (_content != null) addChild(_content);
    }

    /**
     * Returns the spacing.
     */
    public double getSpacing()  { return _spacing; }

    /**
     * Sets the spacing.
     */
    public void setSpacing(double aValue)
    {
        if (aValue == _spacing) return;
        firePropChange(Spacing_Prop, _spacing, _spacing = aValue);
        relayout();
        relayoutParent();
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
     * Returns whether child will crop to height if needed.
     */
    public boolean isCropHeight()  { return _cropHeight; }

    /**
     * Sets whether child will crop to height if needed.
     */
    public void setCropHeight(boolean aValue)
    {
        _cropHeight = aValue;
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
        return getPrefWidth(this, getContent(), aH);
    }

    /**
     * Override.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return getPrefHeight(this, getContent(), aW);
    }

    /**
     * Override.
     */
    protected void layoutImpl()
    {
        layout(this, getContent(), _fillWidth, _fillHeight);
    }

    /**
     * ViewHost method: Override to return 1 if content is present.
     */
    public int getGuestCount()  { return getContent()!=null ? 1 : 0; }

    /**
     * ViewHost method: Override to return content (and complain if index beyond 0).
     */
    public View getGuest(int anIndex)
    {
        if (anIndex>0) throw new IndexOutOfBoundsException("Index " + anIndex + " beyond 0");
        return getContent();
    }

    /**
     * ViewHost method: Override to set content.
     */
    public void addGuest(View aChild, int anIndex)
    {
        if (anIndex>0) System.err.println("BoxView: Attempt to addGuest beyond 0");
        setContent(aChild);
    }

    /**
     * ViewHost method: Override to clear content (and complain if index beyond 0).
     */
    public View removeGuest(int anIndex)
    {
        if (anIndex>0) throw new IndexOutOfBoundsException("Index " + anIndex + " beyond 0");
        View cont = getContent(); setContent(null);
        return cont;
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive Spacing, FillWidth, FillHeight
        if (getSpacing()!=0) e.add(Spacing_Prop, getSpacing());
        if (isFillWidth()) e.add(FillWidth_Prop, true);
        if (isFillHeight()) e.add(FillHeight_Prop, true);
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive Spacing, FillWidth, FillHeight
        if (anElement.hasAttribute(Spacing_Prop))setSpacing(anElement.getAttributeFloatValue(Spacing_Prop));
        if (anElement.hasAttribute(FillWidth_Prop)) setFillWidth(anElement.getAttributeBoolValue(FillWidth_Prop));
        if (anElement.hasAttribute(FillHeight_Prop)) setFillHeight(anElement.getAttributeBoolValue(FillHeight_Prop));
    }

    /**
     * Returns preferred width of layout.
     */
    public static double getPrefWidth(ParentView aParent, View aChild, double aH)
    {
        ViewProxy<?> viewProxy = ViewProxy.getProxy(aParent);
        viewProxy.setContent(ViewProxy.getProxy(aChild));
        double prefW = getPrefWidthProxy(viewProxy, aH);
        return prefW;
    }

    /**
     * Returns preferred height of layout.
     */
    public static double getPrefHeight(ParentView aParent, View aChild, double aW)
    {
        ViewProxy<?> viewProxy = ViewProxy.getProxy(aParent);
        viewProxy.setContent(ViewProxy.getProxy(aChild));
        double prefH = getPrefHeightProxy(viewProxy, aW);
        return prefH;
    }

    /**
     * Performs Box layout for given parent, child and fill width/height.
     */
    public static void layout(ParentView aPar, View aChild, boolean isFillWidth, boolean isFillHeight)
    {
        // If no child, just return
        if (aChild == null) return;

        // Create ViewProxy for parent/child view
        ViewProxy<?> viewProxy = new ViewProxy<>(aPar);
        viewProxy.setContent(ViewProxy.getProxy(aChild));
        viewProxy.setFillWidth(isFillWidth);
        viewProxy.setFillHeight(isFillHeight);

        // Layout
        layoutProxy(viewProxy);

        // Apply bounds
        viewProxy.setBoundsInClient();
    }

    /**
     * Returns preferred width of given parent proxy using RowView layout.
     */
    public static double getPrefWidthProxy(ViewProxy<?> aParentProxy, double aH)
    {
        aParentProxy.setSize(-1, aH);
        layoutProxy(aParentProxy);
        return aParentProxy.getChildrenMaxXLastWithInsets();
    }

    /**
     * Returns preferred height of given parent proxy using RowView layout.
     */
    public static double getPrefHeightProxy(ViewProxy<?> aParentProxy, double aW)
    {
        aParentProxy.setSize(aW, -1);
        layoutProxy(aParentProxy);
        return aParentProxy.getChildrenMaxYLastWithInsets();
    }

    /**
     * Performs Box layout for given parent, child and fill width/height.
     */
    public static void layoutProxy(ViewProxy<?> aParentProxy)
    {
        // Get parent info
        double viewW = aParentProxy.getWidth();
        double viewH = aParentProxy.getHeight();
        boolean isFillWidth = aParentProxy.isFillWidth();
        boolean isFillHeight = aParentProxy.isFillHeight();

        // Get child
        ViewProxy<?> child = aParentProxy.getContent(); if (child == null) return;

        // Get parent bounds for insets (just return if empty)
        Insets borderInsets = aParentProxy.getBorderInsets();
        Insets pad = aParentProxy.getPadding();
        Insets marg = child.getMargin();
        double areaX = borderInsets.left + Math.max(pad.left, marg.left);
        double areaY = borderInsets.top + Math.max(pad.top, marg.top);
        double areaW = Math.max(viewW - borderInsets.right - Math.max(pad.right, marg.right) - areaX, 0);
        double areaH = Math.max(viewH - borderInsets.bottom - Math.max(pad.bottom, marg.bottom) - areaY, 0);

        // Get content width
        double childW;
        if (viewW < 0)
            childW = child.getBestWidth(-1);
        else if (isFillWidth || child.isGrowWidth())
            childW = areaW;
        else childW = child.getBestWidth(-1);  // if (childW > areaW) childW = areaW;

        // Get content height
        double childH;
        if (viewH < 0)
            childH = child.getBestHeight(childW);
        else if (isFillHeight || child.isGrowHeight())
            childH = areaH;
        else childH = child.getBestHeight(childW);

        // If Parent.Width -1, just return (laying out for PrefWidth/PrefHeight)
        if (viewW < 0 || viewH < 0) {
            child.setBounds(areaX, areaY, childW, childH);
            return;
        }

        // If child needs crop, make sure it fits in space
        View parentView = aParentProxy.getView();
        if (parentView instanceof BoxView && ((BoxView) parentView).isCropHeight())
            childH = Math.min(childH, areaH);

        // Get content alignment as modifer/factor (0 = left, 1 = right)
        double alignX = child.getLeanX() != null ? child.getLeanXAsDouble() : aParentProxy.getAlignXAsDouble();
        double alignY = child.getLeanY() != null ? child.getLeanYAsDouble() : aParentProxy.getAlignYAsDouble();

        // Calc X/Y and set bounds
        double childX = areaX + Math.round((areaW - childW) * alignX);
        double childY = areaY + Math.round((areaH - childH) * alignY);
        child.setBounds(childX, childY, childW, childH);
    }
}