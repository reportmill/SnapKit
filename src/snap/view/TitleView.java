/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A view to attach a title to another view.
 */
public class TitleView extends HostView {

    // The title label
    Label          _label;

    // The content
    View           _content;
    
    // The Style
    TitleStyle     _tstyle;
    
    // Whether Title view is collapsible
    boolean        _collapsible;
    
    // Whether Title view is expanded
    boolean        _expanded;
    
    // Images for collapsed/expanded
    View           _clpView, _expView;
    
    // A listener for label click (if collapsable)
    EventListener  _labelPressLsnr;
    
    // Constants for TitleView styles
    public enum TitleStyle { Plain, EtchBorder }
    
    // Constants for properties
    public static final String Collapsible_Prop = "Collapsible";
    public static final String Expanded_Prop = "Expanded";
    public static final String TitleStyle_Prop = "TitleStyle";
    
/**
 * Creates a new TitleView.
 */
public TitleView()
{
    setPadding(2,2,2,2);
    _label = new Label();
    setTitleStyle(TitleStyle.EtchBorder);
    addChild(_label);
}

/**
 * Creates a new TitleView for given title.
 */
public TitleView(String aTitle)  { this(); setText(aTitle); }

/**
 * Creates a new TitleView for given title.
 */
public TitleView(String aTitle, View aView)  { this(); setText(aTitle); setContent(aView); }

/**
 * Returns the label.
 */
public Label getLabel()  { return _label; }

/**
 * Override to get from label.
 */
public String getText()  { return _label.getText(); }

/**
 * Override to set to label.
 */
public void setText(String aTitle)  { _label.setText(aTitle); }

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
    if(_content!=null) removeChild(_content);
    _content = aView;
    if(_content!=null) addChild(_content);
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
    updateTitleStyle();
}

/**
 * Returns whether title view is collapsible.
 */
public boolean isCollapsible()  { return _collapsible; }

/**
 * Sets whether title view is collapsible.
 */
public void setCollapsible(boolean aValue)
{
    // Do normal setter
    if(aValue==_collapsible) return;
    firePropChange(Collapsible_Prop, _collapsible, _collapsible=aValue);
    
    // If collapsible: Enable action event and listen for label click
    if(aValue) {
        enableEvents(Action);
        _label.addEventHandler(_labelPressLsnr = e -> TitleView.this.fireActionEvent(), MousePress);
    }
    
    // If not collapsible: Disable action event and stop listen for lable click
    else {
        disableEvents(Action);
        _label.removeEventHandler(_labelPressLsnr, MousePress);
    }
    
    // Update graphic
    updateGraphic();
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
    updateGraphic();
}

/**
 * Sets the expanded animated.
 */
protected void setExpandedAnimated(boolean aValue)
{
    // Cache current size and set new Expanded value
    double w = getWidth(), h = getHeight();
    setExpanded(aValue);
    
    // Gget new PrefSize
    setPrefSize(-1,-1);
    double pw = getPrefWidth(), ph = getPrefHeight();
    
    // Set pref size to current size and expanded to true (for duration of anim) and trigger anim to new size
    setPrefSize(w,h);
    setExpanded(true);
    getAnimCleared(500).setPrefSize(pw,ph).setOnFinish(a -> setExpanded(aValue)).play();
}

/**
 * Updates the graphic.
 */
protected void updateGraphic()
{
    View graphic = isCollapsible()? (isExpanded()? getExpandedGraphic() : getCollapsedGraphic()) : null;
    _label.setGraphic(graphic);
}

/**
 * Updates the TitleStyle.
 */
protected void updateTitleStyle()
{
    TitleStyle tstyle = getTitleStyle();
    
    // Configure EtchBorder
    if(tstyle==TitleStyle.EtchBorder)
        _label.setPadding(0,0,0,10);
        
    // Configure Plain
    else if(tstyle==TitleStyle.Plain)
        _label.setPadding(0,0,0,0);
}

/**
 * HostView method.
 */
public int getGuestCount()  { return getContent()!=null? 1 : 0; }

/**
 * HostView method.
 */
public View getGuest(int anIndex)  { return getContent(); }

/**
 * HostView method.
 */
public void addGuest(View aChild, int anIndex)
{
    setContent(aChild);
    fireGuestPropChange(null,aChild,0);
}

/**
 * HostView method.
 */
public View removeGuest(int anIndex)
{
    View cont = getContent(); setContent(null);
    fireGuestPropChange(cont,null,0);
    return cont;
}

/**
 * Returns whether content is visible.
 */
public boolean isContentVisible()  { return _content!=null && (!isCollapsible() || isExpanded()); }

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
    // Get inset bounds
    Insets ins = getInsetsAll();
    double x = ins.left, y = ins.top, w = getWidth() - x - ins.right, h = getHeight() - y - ins.bottom;
    
    // Layout label
    double lw = _label.getPrefWidth(), lh = _label.getPrefHeight();
    _label.setBounds(0,0,lw,lh);
    
    // Layout content
    if(isContentVisible())
        _content.setBounds(x, y + lh, w, h - lh + 2);
    else if(_content!=null) _content.setBounds(x,y,0,0);
}

/**
 * Override to paint title and stroke.
 */
protected void paintFront(Painter aPntr)
{
    switch(_tstyle) {
        case EtchBorder: paintStyleEtchBorder(aPntr); break;
    }
}

/**
 * Override to paint title and stroke.
 */
protected void paintStyleEtchBorder(Painter aPntr)
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
    Polygon poly = new Polygon(.5, 1.5, 8.5, 1.5, 4.5, 7.5);
    ShapeView sview = new ShapeView(poly); sview.setPrefSize(9,9);
    sview.setFill(Color.GRAY); sview.setBorder(Color.GRAY, 1);
    return _expView = sview;
}

/**
 * Returns an image of a down arrow.
 */
public View getCollapsedGraphic()
{
    // If down arrow icon hasn't been created, create it
    if(_clpView!=null) return _clpView;
    Polygon poly = new Polygon(2.5, .5, 2.5, 8.5, 8.5, 4.5);
    ShapeView sview = new ShapeView(poly); sview.setPrefSize(9,9);
    sview.setFill(Color.GRAY); sview.setBorder(Color.GRAY, 1);
    return _clpView = sview;
}

/**
 * Override because TeaVM hates reflection.
 */
public Object getValue(String aPropName)
{
    if(aPropName.equals("Value") || aPropName==Expanded_Prop) return isExpanded();
    return super.getValue(aPropName);
}

/**
 * Override because TeaVM hates reflection.
 */
public void setValue(String aPropName, Object aValue)
{
    if(aPropName.equals("Value") || aPropName==Expanded_Prop) setExpanded(SnapUtils.boolValue(aValue));
    else super.setValue(aPropName, aValue);
}

/**
 * Returns a mapped property name name.
 */
protected String getValuePropName()  { return Expanded_Prop; }

/**
 * XML archival.
 */
protected XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Do normal archival
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Text, TitleStyle
    String text = getText(); if(text!=null && text.length()>0) e.add(Text_Prop, text);
    if(getTitleStyle()!=TitleStyle.EtchBorder) e.add(TitleStyle_Prop, getTitleStyle());
    
    // Archive Expandable, Expanded
    if(isCollapsible()) e.add(Collapsible_Prop, true);
    if(isCollapsible() && !isExpanded()) e.add(Expanded_Prop, false);
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Do normal version
    super.fromXMLView(anArchiver,anElement);
    
    // Unarchive Title (legacy)
    if(anElement.hasAttribute(Text_Prop)) setText(anElement.getAttributeValue(Text_Prop));
    if(anElement.hasAttribute("Title")) setText(anElement.getAttributeValue("Title"));
    
    // Unrchive TitleStyle
    String tstr = anElement.getAttributeValue(TitleStyle_Prop);
    TitleStyle tstyl = tstr!=null? TitleStyle.valueOf(tstr) : null;
    if(tstyl!=null) setTitleStyle(tstyl);
    
    // Unrchive Expandable, Expanded
    if(anElement.hasAttribute(Collapsible_Prop)) setCollapsible(anElement.getAttributeBoolValue(Collapsible_Prop));
    if(anElement.hasAttribute(Expanded_Prop)) setExpanded(anElement.getAttributeBoolValue(Expanded_Prop));
}

}