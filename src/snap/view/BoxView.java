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
    View          _content;

    // The spacing between nodes
    double        _spacing;
    
    // Whether to fill width, height
    boolean       _fillWidth, _fillHeight;
    
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
        setContent(aContent); setFillWidth(isFillWidth); setFillHeight(isFillHeight);
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
        if (aView==getContent()) return;

        // Remove old content, set/add new content
        if (_content!=null) removeChild(_content);
        _content = aView;
        if (_content!=null) addChild(_content);
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
        if (aValue==_spacing) return;
        firePropChange(Spacing_Prop, _spacing, _spacing = aValue);
        relayout(); relayoutParent();
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
        if (aValue==_fillWidth) return;
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
        if (aValue==_fillHeight) return;
        firePropChange(FillHeight_Prop, _fillHeight, _fillHeight = aValue);
        relayout();
    }

    // Whether child will crop to height if not enough space available
    private boolean  _cropHeight;

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
        //if (isHorizontal()) return RowView.getPrefWidth(this, null, getSpacing(), aH);
        //return ColView.getPrefWidth(this, null, aH);
    }

    /**
     * Override.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return getPrefHeight(this, getContent(), aW);
        //if (isHorizontal()) return RowView.getPrefHeight(this, null, aW);
        //return ColView.getPrefHeight(this, null, _spacing, aW);
    }

    /**
     * Override.
     */
    protected void layoutImpl()
    {
        layout(this, getContent(), null, _fillWidth, _fillHeight);
        //if (isHorizontal()) RowView.layout(this, null, null, isFillWidth(), isFillHeight(), getSpacing());
        //else ColView.layout(this, null, null, isFillWidth(), isFillHeight(), getSpacing());
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
    public static double getPrefWidth(ParentView aPar, View aChild, double aH)
    {
        // Get insets (just return if empty)
        Insets ins = aPar.getInsetsAll(); if (aChild==null) return ins.getWidth();
        Insets marg = aChild.getMargin();

        // Get height without insets, get best width and return
        double h = aH>=0 ? (aH - ins.getHeight()) : aH;
        double bw = aChild.getBestWidth(h) + marg.getWidth();
        return bw + ins.getWidth();
    }

    /**
     * Returns preferred height of layout.
     */
    public static double getPrefHeight(ParentView aPar, View aChild, double aW)
    {
        // Get insets (just return if empty)
        Insets ins = aPar.getInsetsAll(); if (aChild==null) return ins.getHeight();
        Insets marg = aChild.getMargin();

        // Get width without insets, get best height and return
        double w = aW>=0 ? (aW - ins.getWidth()) : aW;
        double bh = aChild.getBestHeight(w) + marg.getHeight();
        return bh + ins.getHeight();
    }

    /**
     * Performs Box layout for given parent, child and fill width/height.
     */
    public static void layout(ParentView aPar, View aChild, Insets theIns, boolean isFillWidth, boolean isFillHeight)
    {
        // If no child, just return
        if (aChild==null) return;

        // Get parent bounds for insets (just return if empty)
        Insets ins = theIns!=null ? theIns : aPar.getInsetsAll();
        Insets marg = aChild.getMargin();
        double areaX = ins.left + marg.left;
        double areaY = ins.top + marg.top;
        double areaW = aPar.getWidth() - ins.getWidth() - marg.getWidth(); if (areaW<=0) return;
        double areaH = aPar.getHeight() - ins.getHeight() - marg.getHeight(); if (areaH<=0) return;

        // Get content width/height
        double childW = isFillWidth || aChild.isGrowWidth() ? areaW : aChild.getBestWidth(-1); if (childW>areaW) childW = areaW;
        double childH = isFillHeight ? areaH : aChild.getBestHeight(childW);

        // If child needs crop, make sure it fits in space
        if (aPar instanceof BoxView && ((BoxView)aPar).isCropHeight())
            childH = Math.min(childH, areaH);

        // Get content alignment as modifer/factor (0 = left, 1 = right)
        double alignX = aChild.getLeanX()!=null ? ViewUtils.getLeanX(aChild) : ViewUtils.getAlignX(aPar);
        double alignY = aChild.getLeanY()!=null ? ViewUtils.getLeanY(aChild) : ViewUtils.getAlignY(aPar);

        // Calc X/Y and set bounds
        double childX = areaX + Math.round((areaW - childW)*alignX);
        double childY = areaY + Math.round((areaH - childH)*alignY);
        aChild.setBounds(childX, childY, childW, childH);
    }
}