/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to layout child views vertically, from top to bottom.
 */
public class ColView extends ChildView {

    // The VBox layout
    VBoxLayout  _layout = new VBoxLayout(this);
    
/**
 * Returns the spacing.
 */
public double getSpacing()  { return _layout.getSpacing(); }

/**
 * Sets the spacing.
 */
public void setSpacing(double aValue)  { _layout.setSpacing(aValue); }

/**
 * Returns whether children will be resized to fill width.
 */
public boolean isFillWidth()  { return _layout.isFillWidth(); }

/**
 * Sets whether children will be resized to fill width.
 */
public void setFillWidth(boolean aValue)  { _layout.setFillWidth(aValue); }

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.TOP_LEFT; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { _layout.layoutChildren(); }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Spacing, FillWidth
    if(getSpacing()!=0) e.add("Spacing", getSpacing());
    if(isFillWidth()) e.add("FillWidth", true);
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);

    // Unarchive Spacing, FillWidth
    if(anElement.hasAttribute("Spacing")) setSpacing(anElement.getAttributeFloatValue("Spacing"));
    if(anElement.hasAttribute("FillWidth")) setFillWidth(anElement.getAttributeBoolValue("FillWidth"));
}

/**
 * A Vertical box layout.
 */
public static class VBoxLayout extends ViewLayout {
    
    // The spacing between nodes
    double        _spacing;
    
    // Whether to fill to with
    boolean       _fillWidth;
    
    /** Creates a new VBox layout for given parent. */
    public VBoxLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns the spacing. */
    public double getSpacing()  { return _spacing; }
    
    /** Sets the spacing. */
    public void setSpacing(double aValue)  { _spacing = aValue; }
    
    /** Returns whether layout should fill width. */
    public boolean isFillWidth()  { return _fillWidth; }
    
    /** Sets whether layout should fill width. */
    public void setFillWidth(boolean aValue)  { _fillWidth = aValue; }
    
    /** Returns preferred width of layout. */
    protected double getPrefWidthImpl(double aH)
    {
        View children[] = getChildren();
        double w = 0; for(View child : children) w = Math.max(w, child.getBestWidth(aH));
        return w;
    }
    
    /** Returns preferred height of layout. */
    protected double getPrefHeightImpl(double aW)
    {
        View children[] = getChildren(); int ccount = children.length;
        double h = 0; for(View child : children) h += child.getBestHeight(aW); if(ccount>1) h += (ccount-1)*_spacing;
        return h;
    }
        
    /** Performs layout in content rect. */
    public void layoutChildren()  { layout(_parent, getChildren(), null, _fillWidth, _spacing); }
    
    /** Performs layout in content rect. */
    public static void layout(View aPar, View children[], Insets theIns, boolean isFillWidth, double aSpacing)
    {
        // If no children, just return
        if(children.length==0) return;
        
        // Get parent bounds for insets
        Insets ins = theIns!=null? theIns : aPar.getInsetsAll();
        double px = ins.left, py = ins.top;
        double pw = aPar.getWidth() - px - ins.right; if(pw<0) pw = 0; if(pw<=0) return;
        double ph = aPar.getHeight() - py - ins.bottom; if(ph<0) ph = 0; if(ph<=0) return;
        
        // Get child bounds
        Rect cbnds[] = new Rect[children.length];
        double cy = py, ax = getAlignX(aPar);
        int grow = 0;
        
        // Layout children
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];
            double cw = isFillWidth || child.isGrowWidth()? pw : Math.min(child.getBestWidth(-1), pw);
            double ch = child.getBestHeight(cw), cx = px;
            if(pw>cw && !isFillWidth) { double ax2 = Math.max(ax,getLeanX(child)); cx += Math.round((pw-cw)*ax2); }
            cbnds[i] = new Rect(cx,cy,cw,ch); cy += ch + aSpacing;
            if(child.isGrowHeight()) grow++;
        }
        
        // Calculate extra space (return if none)
        double extra = py + ph - (cy - aSpacing);
        
        // If grow shapes, add grow
        if(extra!=0 && grow>0) { double dy = 0;
            for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
                if(dy!=0) cbnd.setY(cbnd.getY() + dy);
                if(child.isGrowHeight()) { cbnd.setHeight(cbnd.getHeight()+extra/grow); dy += extra/grow; }
            }
        }
        
        // Otherwise, check for vertical alignment/lean shift
        else if(extra>0) {
            double ay = getAlignY(aPar);
            for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
                ay = Math.max(ay, getLeanY(child)); double dy = extra*ay;
                if(dy>0) cbnd.setY(cbnd.getY() + extra*ay);
            }
        }

        // Reset children bounds
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect bnds = cbnds[i];
            child.setBounds(bnds); }
    }
}

}