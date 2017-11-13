/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A View that holds another view.
 */
public class BoxView extends ParentView {

    // The content
    View       _child;
    
    // The Box layout
    BoxLayout  _layout = new BoxLayout(this);
    
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
public View getContent()  { return _child; }

/**
 * Sets the box content.
 */
public void setContent(View aView)
{
    if(aView==_child) return;
    _child = aView;
    removeChildren();
    if(_child!=null) addChild(_child);
}

/**
 * Returns whether children will be resized to fill width.
 */
public boolean isFillWidth()  { return _layout.isFillWidth(); }

/**
 * Sets whether children will be resized to fill width.
 */
public void setFillWidth(boolean aValue)  { _layout.setFillWidth(aValue); repaint(); relayoutParent(); }

/**
 * Returns whether children will be resized to fill height.
 */
public boolean isFillHeight()  { return _layout.isFillHeight(); }

/**
 * Sets whether children will be resized to fill height.
 */
public void setFillHeight(boolean aValue)  { _layout.setFillHeight(aValue); repaint(); relayoutParent(); }

/**
 * Override to change to CENTER.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER; }

/**
 * Override.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(aH); }

/**
 * Override.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(aW); }

/**
 * Override.
 */
protected void layoutImpl()  { _layout.layoutChildren(); }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive FillWidth
    if(isFillWidth()) e.add("FillWidth", true);
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

    // Unarchive Spacing, FillWidth
    if(anElement.hasAttribute("FillWidth")) setFillWidth(anElement.getAttributeBoolValue("FillWidth"));
    if(anElement.hasAttribute("FillHeight")) setFillHeight(anElement.getAttributeBoolValue("FillHeight"));
}

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive Content
    if(getContent()==null) return;
    anElement.add(anArchiver.toXML(getContent(), this));
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive first view
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class childClass = anArchiver.getClass(childXML.getName());
        if(childClass!=null && View.class.isAssignableFrom(childClass)) {
            View view = (View)anArchiver.fromXML(childXML, this);
            setContent(view); break;
        }
    }
}

/**
 * A layout for Box.
 */
public static class BoxLayout extends ViewLayout {
    
    // Whether to fill width, height
    boolean       _fillWidth, _fillHeight;
    
    /** Creates a new BoxLayout for given parent. */
    public BoxLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns whether layout should fill width. */
    public boolean isFillWidth()  { return _fillWidth; }
    
    /** Sets whether to fill width. */
    public void setFillWidth(boolean aValue)  { _fillWidth = aValue; }
    
    /** Returns whether layout should fill height. */
    public boolean isFillHeight()  { return _fillHeight; }
    
    /** Sets whether to fill height. */
    public void setFillHeight(boolean aValue)  { _fillHeight = aValue; }
    
    /** Returns preferred width of layout. */
    public double getPrefWidthImpl(double aH)
    {
        View child = getChild();
        double bw = child.getBestWidth(aH);
        return bw;
    }
    
    /** Returns preferred height of layout. */
    public double getPrefHeight(double aW)
    {
        View child = getChild();
        double bh = child.getBestHeight(aW);
        return bh;
    }
    
    /** Performs layout. */
    public void layoutChildren()  { layout(_parent, getChild(), null, _fillWidth, _fillHeight); }
    
    /**
     * Performs Box layout for given parent, child and fill width/height.
     */
    public static void layout(View aPar, View aChild, Insets theIns, boolean isFillWidth, boolean isFillHeight)
    {
        // If no child, just return
        if(aChild==null) return;
        
        // Get parent bounds for insets (just return if empty)
        Insets ins = theIns!=null? theIns : aPar.getInsetsAll();
        double px = ins.left, py = ins.top;
        double pw = aPar.getWidth() - px - ins.right; if(pw<0) pw = 0; if(pw<=0) return;
        double ph = aPar.getHeight() - py - ins.bottom; if(ph<0) ph = 0; if(ph<=0) return;
        
        // Get content width/height
        double cw = isFillWidth || aChild.isGrowWidth()? pw : aChild.getBestWidth(-1); if(cw>pw) cw = pw;
        double ch = isFillHeight? ph : aChild.getBestHeight(cw);
        
        // Handle normal layout
        double dx = pw - cw, dy = ph - ch;
        double sx = aChild.getLeanX()!=null? getLeanX(aChild) : getAlignX(aPar);
        double sy = aChild.getLeanY()!=null? getLeanY(aChild) : getAlignY(aPar);
        aChild.setBounds(px+dx*sx, py+dy*sy, cw, ch);
    }
}

}