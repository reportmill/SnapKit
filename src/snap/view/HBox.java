/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to layout child views horizontally, from left to right.
 */
public class HBox extends ChildView {
    
    // The HBox layout
    HBoxLayout  _layout = new HBoxLayout(this);
    
/**
 * Returns the spacing.
 */
public double getSpacing()  { return _layout.getSpacing(); }

/**
 * Sets the spacing.
 */
public void setSpacing(double aValue)  { _layout.setSpacing(aValue); }

/**
 * Returns whether children will be resized to fill height.
 */
public boolean isFillHeight()  { return _layout.isFillHeight(); }

/**
 * Sets whether children will be resized to fill height.
 */
public void setFillHeight(boolean aValue)  { _layout.setFillHeight(aValue); }

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

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
    
    // Archive Spacing, FillHeight
    if(getSpacing()!=0) e.add("Spacing", getSpacing());
    if(isFillHeight()) e.add("FillHeight", true);
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);

    // Unarchive Spacing, FillHeight
    setSpacing(anElement.getAttributeFloatValue("Spacing", 0));
    setFillHeight(anElement.getAttributeBoolValue("FillHeight", false));
}

/**
 * A Horizontal box layout.
 */
public static class HBoxLayout extends ViewLayout {
    
    // The spacing between nodes
    double        _spacing;
    
    // Whether to fill to height
    boolean       _fillHeight;
    
    /** Creates a new HBox layout for given parent. */
    public HBoxLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns the spacing. */
    public double getSpacing()  { return _spacing; }
    
    /** Sets the spacing. */
    public void setSpacing(double aValue)  { _spacing = aValue; }
    
    /** Returns whether layout should fill height. */
    public boolean isFillHeight()  { return _fillHeight; }
    
    /** Sets whether layout should fill height. */
    public void setFillHeight(boolean aValue)  { _fillHeight = aValue; }
    
    /** Returns preferred width of layout. */
    protected double getPrefWidthImpl(double aH)
    {
        View children[] = getChildren(); int ccount = children.length;
        double w = 0; for(View child : children) w += child.getBestWidth(aH); if(ccount>1) w += (ccount-1)*_spacing;
        return w;
    }
    
    /** Returns preferred height of layout. */
    protected double getPrefHeightImpl(double aW)
    {
        View children[] = getChildren();
        double h = 0; for(View child : children) h = Math.max(h, child.getBestHeight(aW));
        return h;
    }
        
    /** Performs layout. */
    public void layoutChildren(double px, double py, double pw, double ph)
    {
        View children[] = getChildren(); int ccount = children.length; if(ccount==0) return;
        Rect cbnds[] = new Rect[children.length];
        double cx = px, ay = getAlignY(_parent);
        int grow = 0;
        
        // Layout children
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];
            double ch = _fillHeight || child.isGrowHeight()? ph : Math.min(child.getBestHeight(-1), ph);
            double cw = child.getBestWidth(ch), cy = py;
            if(ph>ch && !_fillHeight) { double ay2 = Math.max(ay,getLeanY(child)); cy += Math.round((ph-ch)*ay2); }
            cbnds[i] = new Rect(cx, cy, cw, ch); cx += cw + _spacing;
            if(child.isGrowWidth()) grow++;
        }
        
        // Calculate extra space
        double extra = px + pw - (cx - _spacing);
        
        // If grow shapes, add grow
        if(extra!=0 && grow>0) { double dx = 0;
            for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
                if(dx!=0) cbnd.setX(cbnd.getX() + dx);
                if(child.isGrowWidth()) { cbnd.setWidth(cbnd.getWidth()+extra/grow); dx += extra/grow; }
            }
        }
        
        // Otherwise, check for horizontal alignment/lean shift
        else if(extra>0) {
            double ax = getAlignX(_parent);
            for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
                ax = Math.max(ax, getLeanX(child)); double dx = extra*ax;
                if(dx>0) cbnd.setX(cbnd.getX() + extra*ax);
            }
        }
        
        // Reset children bounds
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect bnds = cbnds[i];
            child.setBounds(bnds); }
    }
}

}