/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Size;

/**
 * A Box subclass that scales it's content instead of resize.
 * FillWidth, FillHeight, KeepAspect, FillAlways default to true.
 */
public class ScaleBox extends Box {

/**
 * Creates a new ScaleBox.
 */
public ScaleBox()  { _layout = new ScaleLayout(this); } //setFillWidth(true); setFillHeight(true); 

/**
 * Creates a new ScaleBox for content.
 */
public ScaleBox(View aContent)  { this(); setContent(aContent); }

/**
 * Creates a new ScaleBox for content with FillWidth, FillHeight params.
 */
public ScaleBox(View aContent, boolean isFillWidth, boolean isFillHeight)
{
    this(aContent); setFillWidth(isFillWidth); setFillHeight(isFillHeight);
}

/**
 * A layout for ScaleBox.
 */
public static class ScaleLayout extends BoxLayout {
    
    /** Creates a new ScaleLayout for given parent. */
    public ScaleLayout(ParentView aPar)  { super(aPar); }
    
    /** Returns preferred width of layout. */
    public double getPrefWidth(double aH)
    {
        // If scaling and value provided, return value by aspect
        if(aH>=0 && (_fillWidth || aH<getPrefHeight(-1))) return aH*getAspect();
        return super.getPrefWidth(aH);
    }
    
    /** Returns preferred height of layout. */
    public double getPrefHeight(double aW)
    {
        // If scaling and value provided, return value by aspect
        if(aW>=0 && (_fillWidth || aW<getPrefWidth(-1))) return aW/getAspect();
        return super.getPrefHeight(aW);
    }
    
    /** Performs layout in content rect. */
    public void layoutChildren(double px, double py, double pw, double ph)
    {
        // Get content width/height
        View child = getChild();
        double cw = child.getBestWidth(-1);
        double ch = child.getBestHeight(cw);
        
        // Handle ScaleToFit: Set content bounds centered, calculate scale and set
        if(_fillWidth || _fillHeight || cw>pw || ch>ph)  {
            double cx = px + (pw-cw)/2, cy = py + (ph-ch)/2;
            child.setBounds(cx, cy, cw, ch);
            double sx = _fillWidth || cw>pw? pw/cw : 1;
            double sy = _fillHeight || ch>ph? ph/ch : 1;
            if(_fillWidth && _fillHeight) sx = sy = Math.min(sx,sy); // KeepAspect?
            child.setScaleX(sx); child.setScaleY(sy);
            return;
        }
        
        // Handle normal layout
        if(cw>pw) cw = pw; if(ch>ph) ch = ph;
        double dx = pw - cw, dy = ph - ch;
        double sx = child.getLeanX()!=null? getLeanX(child) : getAlignX(_parent);
        double sy = child.getLeanY()!=null? getLeanY(child) : getAlignY(_parent);
        child.setBounds(px+dx*sx, py+dy*sy, cw, ch);
    }
    
    /** Returns the aspect of the content. */
    protected double getAspect()
    {
        View c = getChild();
        Size bs = c.getBestSize();
        return c.isHorizontal()? bs.width/bs.height : bs.height/bs.width;
    }
}
    
}