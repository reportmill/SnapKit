/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.util.*;

/**
 * A View subclass that lays out children using auto sizing settings.
 */
public class SpringView extends ChildView {

    // The last set size
    double _ow, _oh;
    
    // The SpringInfos for children
    Map <Object,SpringInfo> _sinfos = new HashMap();
    
    // A PropChangeListener to resetSpringInfo when child bounds change outside of layout
    PropChangeListener       _pcl = pce -> childPropChange(pce);
    
    /**
     * Override to add layout info.
     */
    public void addChild(View aChild, int anIndex)
    {
        super.addChild(aChild, anIndex);
        addSpringInfo(aChild);
        aChild.addPropChangeListener(_pcl);
    }

    /**
     * Override to remove layout info.
     */
    public View removeChild(int anIndex)
    {
        View child = super.removeChild(anIndex);
        removeSpringInfo(child);
        child.removePropChangeListener(_pcl);
        return child;
    }

    /**
     * Resets spring info for given child (or all children if null).
     */
    public void resetSpringInfo(View aChild)
    {
        if (aChild!=null)
            addSpringInfo(aChild);
        else for (View v : getChildren())
            addSpringInfo(v);
    }

    /**
     * Returns spring info for child.
     */
    protected SpringInfo getSpringInfo(View aChild)  { return _sinfos.get(aChild); }

    /**
     * Adds spring info for child.
     */
    protected void addSpringInfo(View aChild)
    {
        double pw = getWidth(), ph = getHeight();
        double x = aChild.getX(), y = aChild.getY(), w = aChild.getWidth(), h = aChild.getHeight();
        SpringInfo sinfo = new SpringInfo(x,y,w,h,pw,ph);
        _sinfos.put(aChild, sinfo); _ow = _oh = 0;
    }

    /**
     * Removes spring info for child.
     */
    protected void removeSpringInfo(View aChild)  { _sinfos.remove(aChild); _ow = _oh = 0; }

    /**
     * Returns preferred width.
     */
    protected double getPrefWidthImpl(double aH)  { return getWidth(); }

    /**
     * Returns preferred height.
     */
    protected double getPrefHeightImpl(double aW)  { return getHeight(); }

    /**
     * Override to perform layout.
     */
    protected void layoutImpl()
    {
        View children[] = getChildren();
        double pw = getWidth(), ph = getHeight(); if (pw==_ow && ph==_oh) return;
        for (View child : children) layoutChild(child, pw, ph);
        _ow = pw; _oh = ph;
    }

    /**
     * Returns the child rects for given parent height.
     */
    protected void layoutChild(View aChild, double newPW, double newPH)
    {
        SpringInfo sinfo = getSpringInfo(aChild);
        String asize = aChild.getAutosizing();
        double oldPW = sinfo.pwidth;
        double oldPH = sinfo.pheight;
        boolean lms = asize.charAt(0)=='~', ws = asize.charAt(1)=='~', rms = asize.charAt(2)=='~';
        boolean tms = asize.charAt(4)=='~', hs = asize.charAt(5)=='~', bms = asize.charAt(6)=='~';
        double x1 = sinfo.x;
        double y1 = sinfo.y;
        double w1 = sinfo.width;
        double h1 = sinfo.height;
        double sw = (lms ? x1 : 0) + (ws ? w1 : 0) + (rms ? oldPW - (x1 + w1) : 0);
        double sh = (tms ? y1 : 0) + (hs ? h1 : 0) + (bms ? oldPH - (y1 + h1) : 0);
        double dw = newPW - oldPW;
        double dh = newPH - oldPH;

        // Calculate new bounds and set
        double childX = (!lms || sw==0) ? x1 : (x1 + dw*x1/sw);
        double childY = (!tms || sh==0) ? y1 : (y1 + dh*y1/sh);
        double childW = (!ws || sw==0) ? w1 : (w1 + dw*w1/sw);
        double childH = (!hs || sh==0) ? h1 : (h1 + dh*h1/sh);
        aChild.setBounds(childX, childY, childW, childH);
    }

    /**
     * Called when child property changes.
     */
    protected void childPropChange(PropChange aPCE)
    {
        if (isInLayout()) return;
        String pname = aPCE.getPropName();
        if (pname==X_Prop || pname==Y_Prop || pname==Width_Prop || pname==Height_Prop)
            resetSpringInfo((View)aPCE.getSource());
    }

    /**
     * XML Archival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);
        setPrefSize(getWidth(), getHeight());
    }

    /**
     * A class to hold info for a spring child.
     */
    public static class SpringInfo {

        // The bounds and original parent width/height
        double x, y, width, height, pwidth, pheight;

        /** Creates a SpringInfo. */
        public SpringInfo(double aX, double aY, double aW, double aH, double aPW, double aPH) {
            x = aX; y = aY; width = aW; height = aH; pwidth = aPW; pheight = aPH; }

        // Sets the rect
        public void setRect(double aX, double aY, double aW, double aH)  { x = aX; y = aY; width = aW; height = aH; }
    }
}