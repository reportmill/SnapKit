/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A view to attach a title to another view.
 */
public class TitleView extends ParentView {

    // The title label
    Label          _label;

    // The content
    View           _content;
    
    // The Style
    TitleStyle     _tstyle;
    
    // Whether Title view is expandable
    boolean        _expandable;
    
    // Whether Title view is expanded
    boolean        _expanded;
    
    // Images for collapsed/expanded
    View           _clpView, _expView;
    
    // Constants for TitleView styles
    public enum TitleStyle {
        ChiselBorder,
        Plain
    }
    
    // Constants for properties
    public static final String Content_Prop = "Content";
    public static final String Expandable_Prop = "Expandable";
    public static final String Expanded_Prop = "Expanded";
    public static final String TitleStyle_Prop = "TitleStyle";
    
/**
 * Creates a new TitleView.
 */
public TitleView()
{
    setPadding(2,2,2,2);
    _label = new Label();
    setTitleStyle(TitleStyle.ChiselBorder);
    addChild(_label);
}

/**
 * Returns the title.
 */
public String getTitle()  { return _label.getText(); }

/**
 * Sets the title.
 */
public void setTitle(String aTitle)  { _label.setText(aTitle); }

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
    _content = aView; addChild(aView);
    firePropChange(Content_Prop, old, aView);
}

/**
 * Returns the title style.
 */
public TitleStyle getTitleStyle()  { return _tstyle; }

/**
 * Sets the title style.
 */
public void setTitleStyle(TitleStyle aTS)
{
    if(aTS==_tstyle) return;
    firePropChange(TitleStyle_Prop, _tstyle, _tstyle=aTS);
    relayout(); relayoutParent(); repaint();
    
    // Configure ChiselBorder
    if(_tstyle==TitleStyle.ChiselBorder)
        _label.setPadding(0,0,0,10);
        
    // Configure Plain
    else if(_tstyle==TitleStyle.Plain)
        _label.setPadding(0,0,0,0);
}

/**
 * Returns whether title view is expandable.
 */
public boolean isExpandable()  { return _expandable; }

/**
 * Sets whether title view is expandable.
 */
public void setExpandable(boolean aValue)
{
    if(aValue==_expandable) return;
    firePropChange(Expandable_Prop, _expandable, _expandable=aValue);
    
    // If expandable
    if(aValue) {
        enableEvents(Action);
        _label.addEventHandler(e -> TitleView.this.fireActionEvent(), MousePress);
    }
    else {
        disableEvents(Action);
        //_label.removeEventHandler(e -> TitleView.this.fireActionEvent(), MousePress);
    }
    
    View graphic = aValue? (isExpanded()? getExpandedGraphic() : getCollapsedGraphic()) : null;
    _label.setGraphic(graphic);
}

/**
 * Returns whether title view is expanded.
 */
public boolean isExpanded()  { return _expanded; }

/**
 * Sets whether title view is expanded.
 */
public void setExpanded(boolean aValue)
{
    if(aValue==_expanded) return;
    firePropChange(Expanded_Prop, _expanded, _expanded=aValue);
    
    View graphic = isExpandable()? (aValue? getExpandedGraphic() : getCollapsedGraphic()) : null;
    _label.setGraphic(graphic);
}

/**
 * Sets the expanded animated.
 */
public void setExpandedAnimated(boolean aValue)
{
    double w = getWidth(), h = getHeight();
    setExpanded(aValue);
    setPrefSize(-1,-1);
    double pw = getPrefWidth(), ph = getPrefHeight();
    setPrefSize(w,h);
    setExpanded(true);
    getAnimCleared(500).setPrefSize(pw,ph).setOnFinish(a -> setExpanded(aValue)).play();
}

/**
 * Returns whether content is visible.
 */
public boolean isContentVisible()  { return _content!=null && (!isExpandable() || isExpanded()); }

/**
 * Override to return preferred width of content.
 */
protected double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll();
    double cw = _label.getPrefWidth();
    View c = getContent(); if(c!=null && isContentVisible()) cw = Math.max(cw, c.getPrefWidth());
    return ins.left + cw + ins.right;
}

/**
 * Override to return preferred height of content.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    double ch = _label.getPrefHeight();
    View c = getContent(); if(c!=null && isContentVisible()) ch += c.getPrefHeight();
    return ins.top + ch + ins.bottom;
}

/**
 * Override to layout content.
 */
protected void layoutImpl()
{
    Insets ins = getInsetsAll();
    double x = ins.left, y = ins.top, w = getWidth() - x - ins.right, h = getHeight() - y - ins.bottom;
    
    // Layout label
    double lw = _label.getPrefWidth(), lh = _label.getPrefHeight();
    _label.setBounds(0,0,lw,lh);
    
    // Layout content
    if(isContentVisible())
        _content.setBounds(x, y + lh, w, h - lh + 2);
    else _content.setBounds(x,y,0,0);
}

/**
 * Override to paint title and stroke.
 */
protected void paintFront(Painter aPntr)
{
    switch(_tstyle) {
        case ChiselBorder: paintStyleChiselBorder(aPntr); break;
    }
}

/**
 * Override to paint title and stroke.
 */
protected void paintStyleChiselBorder(Painter aPntr)
{
    double w = getWidth(), h = getHeight();
    double sw = _label.getMaxX(), sh = _label.getHeight();
    double x1 = 5+sw, y1 = sh/2;
    
    // Paint chisel border
    Path path = new Path(); path.moveTo(x1,y1); path.lineTo(w-2,y1); path.lineTo(w-2,h-2); path.lineTo(.5,h-2);
    path.lineTo(.5,y1); path.lineTo(5,y1);
    aPntr.translate(1,1); aPntr.setPaint(Color.WHITE); aPntr.setStroke(Stroke.Stroke1); aPntr.draw(path);
    aPntr.translate(-1,-1);
    aPntr.setPaint(Color.LIGHTGRAY); aPntr.draw(path);
    
    // Paint title
    //aPntr.setFont(getFont()); aPntr.setColor(Color.BLACK); if(_title!=null) aPntr.drawString(_title, 10, asc);
}

/**
 * Override to flip expanded.
 */
public void fireActionEvent()
{
    super.fireActionEvent();
    setExpandedAnimated(!isExpanded());
}

/**
 * Returns an Icon of a down arrow.
 */
public View getExpandedGraphic()
{
    // If down arrow icon hasn't been created, create it
    if(_expView!=null) return _expView;
    Polygon poly = new Polygon(1.5, 1.5, 7.5, 1.5, 4.5, 5.5);
    ShapeView sview = new ShapeView(poly); sview.setPrefSize(9,9); sview.setFill(Color.BLACK);
    return _expView = sview;
}

/**
 * Returns an image of a down arrow.
 */
public View getCollapsedGraphic()
{
    // If down arrow icon hasn't been created, create it
    if(_clpView!=null) return _clpView;
    Polygon poly = new Polygon(1.5, 1.5, 1.5, 7.5, 5.5, 4.5);
    ShapeView sview = new ShapeView(poly); sview.setPrefSize(9,9); sview.setFill(Color.BLACK);
    return _clpView = sview;
}

/**
 * XML archival.
 */
protected XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Do normal archival
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Title, TitleStyle
    if(getTitle()!=null && getTitle().length()>0) e.add("Title", getTitle());
    if(getTitleStyle()!=TitleStyle.ChiselBorder) e.add(TitleStyle_Prop, getTitleStyle());
    
    // Archive Expandable, Expanded
    if(isExpandable()) e.add(Expandable_Prop, true);
    if(isExpandable() && !isExpanded()) e.add(Expanded_Prop, false);
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Do normal version
    super.fromXMLView(anArchiver,anElement);
    
    // Unrchive Title, TitleStyle
    setTitle(anElement.getAttributeValue("Title"));
    String tstr = anElement.getAttributeValue(TitleStyle_Prop);
    TitleStyle tstyl = tstr!=null? TitleStyle.valueOf(tstr) : null;
    if(tstyl!=null) setTitleStyle(tstyl);
    
    // Unrchive Expandable, Expanded
    if(anElement.hasAttribute(Expandable_Prop)) setExpandable(anElement.getAttributeBoolValue(Expandable_Prop));
    if(anElement.hasAttribute(Expanded_Prop)) setExpanded(anElement.getAttributeBoolValue(Expanded_Prop));
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
    // Iterate over child elements and unarchive child
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class childClass = anArchiver.getClass(childXML.getName());
        if(childClass!=null && View.class.isAssignableFrom(childClass)) {
            View view = (View)anArchiver.fromXML(childXML, this);
            setContent(view); break;
        }
    }
}

}