/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A View for Buttons.
 */
public class ButtonBase extends ParentView {
    
    // The button label
    Label                   _label;
    
    // The image name, if loaded from local resource
    String                  _iname;
    
    // Whether button displays the standard background area
    boolean                 _showArea = getDefaultShowArea();
    
    // The position of the button when in a group (determines corner rendering)
    Pos                     _pos;
    
    // The radius of the button rounding
    double                  _rad = 4;
    
    // Whether button is pressed
    boolean                 _pressed;
    
    // Whether button is under mouse
    boolean                 _targeted;
    
    // Whether button is being tracked by mouse
    boolean                 _tracked;
    
    // The button fill
    Paint                   _btnFill;
    
    // Constants for properties
    public static final String Image_Prop = "Image";
    public static final String ImageName_Prop = "ImageName";
    public static final String Pressed_Prop = "Pressed";
    public static final String ShowArea_Prop = "ShowArea";
    public static final String Position_Prop = "Position";
    public static final String Radius_Prop = "Radius";
    public static final String Targeted_Prop = "Targeted";
    public static final String Text_Prop = "Text";
    
    // Button states
    public static final int BUTTON_NORMAL = 0;
    public static final int BUTTON_OVER = 1;
    public static final int BUTTON_PRESSED = 2;

/**
 * Creates a new ButtonBaseNode.
 */
public ButtonBase()
{
    setFocusable(true);
    enableEvents(MouseEvents); enableEvents(KeyPress, Action);
}

/**
 * Returns the label.
 */
public Label getLabel()
{
    if(_label!=null) return _label;
    _label = new Label();
    addChild(_label);
    return _label;
}

/**
 * Returns the text.
 */
public String getText()  { return getLabel().getText(); }

/**
 * Sets the text.
 */
public void setText(String aStr)
{
    if(SnapUtils.equals(aStr,getText())) return;
    getLabel().setText(aStr); relayout();
}

/**
 * Returns the image.
 */
public Image getImage()  { return getLabel().getImage(); }

/**
 * Sets the image.
 */
public void setImage(Image anImage)  { getLabel().setImage(anImage); relayout(); }

/**
 * Returns the image after text.
 */
public Image getImageAfter()  { return getLabel().getImageAfter(); }

/**
 * Sets the image after text.
 */
public void setImageAfter(Image anImage)  { getLabel().setImageAfter(anImage); relayout(); }

/**
 * Returns the graphic node.
 */
public View getGraphic()  { return getLabel().getGraphic(); }

/**
 * Sets the graphic node.
 */
public void setGraphic(View aGraphic)  { getLabel().setGraphic(aGraphic); }

/**
 * Returns the graphic node after text.
 */
public View getGraphicAfter()  { return getLabel().getGraphicAfter(); }

/**
 * Sets the graphic node after text.
 */
public void setGraphicAfter(View aGraphic)  { getLabel().setGraphicAfter(aGraphic); }

/**
 * Returns the image name, if loaded from local resource.
 */
public String getImageName()  { return _iname; }

/**
 * Sets the image name, if loaded from local resource.
 */
public void setImageName(String aName)
{
    if(SnapUtils.equals(aName, _iname)) return;
    firePropChange(ImageName_Prop, _iname, _iname = aName);
    repaint();
}

/**
 * Returns whether button is pressed (visibly).
 */
public boolean isPressed()  { return _pressed; }

/**
 * Sets whether button is pressed (visibly).
 */
protected void setPressed(boolean aValue)
{
    if(aValue==_pressed) return;
    firePropChange(Pressed_Prop, _pressed, _pressed=aValue);
    repaint();
}

/**
 * Returns whether button is under mouse.
 */
public boolean isTargeted()  { return _targeted; }

/**
 * Sets whether button is under mouse.
 */
protected void setTargeted(boolean aValue)
{
    if(aValue==_targeted) return;
    firePropChange(Targeted_Prop, _targeted, _targeted=aValue);
    repaint();
}

/**
 * Returns whether button displays the standard background area.
 */
public boolean isShowArea()  { return _showArea; }

/**
 * Sets whether button displays the standard background area.
 */
public void setShowArea(boolean aValue)
{
    if(aValue==_showArea) return;
    firePropChange(ShowArea_Prop, _showArea, _showArea=aValue);
}

/**
 * Returns whether button displays standard background area by default.
 */
protected boolean getDefaultShowArea()  { return true; }

/**
 * Returns the position of the button when in a group (determines corner rendering).
 */
public Pos getPosition()  { return _pos; }

/**
 * Sets the position of the button when in a group (determines corner rendering).
 */
public void setPosition(Pos aPos)
{
    if(aPos==_pos) return;
    firePropChange(Position_Prop, _pos, _pos = aPos);
    repaint();
}

/**
 * Returns the radius of the round.
 */
public double getRadius()  { return _rad; }

/**
 * Sets the radius of the round.
 */
public void setRadius(double aValue)
{
    if(MathUtils.equals(aValue, _rad)) return;
    firePropChange(Radius_Prop, _rad, _rad = aValue);
    repaint();
}

/**
 * Returns the button fill.
 */
public Paint getButtonFill()  { return _btnFill; }

/**
 * Sets the button fill.
 */
public void setButtonFill(Paint aPaint)  { _btnFill = aPaint; }

/**
 * Returns the insets.
 */
public Insets getInsetsAll()
{
    Insets pad = getPadding();
    if(isShowArea()) pad = new Insets(pad.top+2,pad.right+2,pad.bottom+2,pad.left+2);
    return pad;
}

/**
 * Handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // If disabled, just return
    if(isDisabled()) return;
    
    // Handle MouseEnter, MouseExit, MousePress, MouseRelease
    if(anEvent.isMouseEnter()) { setTargeted(true); setPressed(_tracked); repaint(); }
    else if(anEvent.isMouseExit())  { setTargeted(false); setPressed(false); repaint(); }
    else if(anEvent.isMousePress())  { _tracked = true; setPressed(true); repaint(); anEvent.consume(); }
    else if(anEvent.isMouseRelease())  {
        if(_pressed) fireActionEvent(anEvent);
        _pressed = _tracked = false; repaint();
    }
    
    // Handle KeyPress + Enter
    if(anEvent.isKeyPress() && anEvent.getKeyCode()==KeyCode.SPACE)
        fireActionEvent(anEvent);
}

/**
 * Perform button click.
 */
public void fire()  { fireActionEvent(null); }

/**
 * Override to consume event.
 */
protected void fireActionEvent(ViewEvent anEvent)
{
    super.fireActionEvent(anEvent);
    if(anEvent!=null) anEvent.consume();
}

/**
 * Paint Button.
 */
public void paintFront(Painter aPntr)
{
    // Reset stroke
    aPntr.setStroke(Stroke.Stroke1);

    if(isShowArea()) {
        ButtonArea bp = new ButtonArea();
        bp.configureFromButton(this);
        bp.paint(aPntr);
    }
    else {
        if(isPressed()) {
            aPntr.setPaint(Color.LIGHTGRAY); aPntr.fillRect(0,0,getWidth(),getHeight()); }
        if(isTargeted()) {
            aPntr.setPaint(Color.GRAY); aPntr.drawRect(.5,.5,getWidth()-1,getHeight()-1); }
    }
}

/**
 * Override to do bogus disabled painting.
 */
public void paintAll(Painter aPntr)
{
    super.paintAll(aPntr);
    if(isDisabled()) {
        aPntr.setColor(new Color(1,1,1,.5)); aPntr.fillRect(0,0,getWidth(),getHeight()); }
}

/**
 * Returns the default alignment for button.
 */
public Pos getDefaultAlign()  { return Pos.CENTER; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return BoxView.getPrefWidth(this, getLabel(), aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return BoxView.getPrefHeight(this, getLabel(), aW); }

/**
 * Override to layout children.
 */
protected void layoutImpl()  { BoxView.layout(this, getLabel(), null, false, false); }

/**
 * XML archival.
 */
protected XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);

    // Archive Text and ImageName
    String text = getText(); if(text!=null && text.length()>0) e.add("text", text);
    String iname = getImageName(); if(iname!=null) e.add("image", iname);
    
    // Archive ShowArea, Position
    if(isShowArea()!=getDefaultShowArea()) e.add(ShowArea_Prop, isShowArea());
    if(getPosition()!=null) e.add(Position_Prop, getPosition());
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    
    // Unarchive Text and ImageName
    setText(anElement.getAttributeValue("text", anElement.getAttributeValue("value")));
    String iname = anElement.getAttributeValue("image");
    if(iname!=null) {
        setImageName(iname);
        Image image = ViewArchiver.getImage(anArchiver, iname);
        if(image!=null) setImage(image);
    }
    
    // Unarchive ShowArea, Position
    if(anElement.hasAttribute(ShowArea_Prop)) setShowArea(anElement.getAttributeBoolValue(ShowArea_Prop));
    if(anElement.hasAttribute("ShowBorder")) setShowArea(anElement.getAttributeBoolValue("ShowBorder"));
    if(anElement.hasAttribute(Position_Prop)) setPosition(Pos.valueOf(anElement.getAttributeValue(Position_Prop)));
}

}