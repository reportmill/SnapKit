/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A custom class.
 */
public class Label extends ParentView {
    
    // The text node
    StringView      _strView;
    
    // The graphics node
    View            _graphic;
    
    // The graphics node after text
    View            _graphicAfter;
    
    // The image name, if loaded from local resource
    String          _iname;
    
    // The spacing between text and graphic
    double          _spacing = 4;
    
    // The layout
    ViewLayout      _layout;

    // Constants for properties
    public static final String Graphic_Prop = "Graphic";
    public static final String GraphicAfter_Prop = "GraphicAfter";
    public static final String StringView_Prop = "StringView";
    
/**
 * Creates a label node.
 */
public Label()  { }

/**
 * Creates a label node with given text.
 */
public Label(String aStr)  { setText(aStr); }

/**
 * Creates a label node with given graphic, text, and after graphic.
 */
public Label(View aGrph, String aStr, View aGrphAfter) { setGraphic(aGrph); setText(aStr); setGraphicAfter(aGrphAfter);}

/**
 * Returns the text.
 */
public String getText()  { return _strView!=null? _strView.getText() : null; }

/**
 * Sets the text.
 */
public void setText(String aValue)
{
    if(SnapUtils.equals(aValue,getText())) return;
    if(aValue==null) { setStringView(null); return; }
    StringView sview = getStringView(true);
    sview.setText(aValue);
}

/**
 * Returns the image.
 */
public Image getImage()  { return _graphic instanceof ImageView? ((ImageView)_graphic).getImage() : null; }

/**
 * Sets the image.
 */
public void setImage(Image anImage)
{
    Image image = getImage(); if(anImage==image) return;
    if(_graphic instanceof ImageView) ((ImageView)_graphic).setImage(anImage);
    else setGraphic(new ImageView(anImage));
    firePropChange("Image", image, anImage);
}

/**
 * Returns the image after text.
 */
public Image getImageAfter() { return _graphicAfter instanceof ImageView? ((ImageView)_graphicAfter).getImage() : null;}

/**
 * Sets the image after text.
 */
public void setImageAfter(Image anImage)
{
    Image image = getImage(); if(anImage==image) return;
    if(_graphicAfter instanceof ImageView) ((ImageView)_graphicAfter).setImage(anImage);
    else setGraphicAfter(new ImageView(anImage));
    firePropChange("Image", image, anImage);
}

/**
 * Returns the text fill.
 */
public Paint getTextFill()  { return _strView!=null? _strView.getTextFill() : null; }

/**
 * Sets the text fill.
 */
public void setTextFill(Paint aPaint)  { getStringView(true).setTextFill(aPaint); }

/**
 * Returns the StringView.
 */
public StringView getStringView()  { return _strView; }

/**
 * Returns the StringView with option to create if missing.
 */
public StringView getStringView(boolean doCreate)
{
    if(_strView!=null || !doCreate) return _strView;
    StringView sview = new StringView(); setStringView(sview);
    return _strView;
}

/**
 * Sets the text node.
 */
public void setStringView(StringView aStrView)
{
    View old = getStringView(); if(aStrView==old) return;
    if(_strView!=null && _strView.getParent()!=null) removeChild(_strView);
    _strView = aStrView;
    if(_strView!=null) addChild(_strView, getGraphic()!=null? 1 : 0);
    firePropChange(StringView_Prop, old, _graphic);
}

/**
 * Returns the graphic node.
 */
public View getGraphic()  { return _graphic; }

/**
 * Sets the graphic node.
 */
public void setGraphic(View aGraphic)
{
    View old = getGraphic(); if(aGraphic==old) return;
    if(_graphic!=null && _graphic.getParent()!=null) removeChild(_graphic);
    _graphic = aGraphic;
    if(_graphic!=null) addChild(_graphic, 0);
    firePropChange(Graphic_Prop, old, _graphic);
}

/**
 * Returns the graphic node after the text.
 */
public View getGraphicAfter()  { return _graphicAfter; }

/**
 * Sets the graphic node after the text.
 */
public void setGraphicAfter(View aGraphic)
{
    View old = getGraphicAfter(); if(aGraphic==old) return;
    if(_graphicAfter!=null && _graphicAfter.getParent()!=null) removeChild(_graphicAfter);
    _graphicAfter = aGraphic;
    if(_graphicAfter!=null) addChild(_graphicAfter);
    firePropChange(GraphicAfter_Prop, old, _graphicAfter);
}

/**
 * Returns the image name, if loaded from local resource.
 */
public String getImageName()  { return _iname; }

/**
 * Sets the image name, if loaded from local resource.
 */
public void setImageName(String aName)  { _iname = aName; }

/**
 * Returns the spacing between text and graphics.
 */
public double getSpacing()  { return _spacing; }

/**
 * Sets the spacing between text and graphics.
 */
public void setSpacing(double aValue)
{
    _spacing = aValue;
    if(_layout instanceof ViewLayout.HBoxLayout) ((ViewLayout.HBoxLayout)_layout).setSpacing(_spacing);
    if(_layout instanceof ViewLayout.VBoxLayout) ((ViewLayout.VBoxLayout)_layout).setSpacing(_spacing);
    relayoutParent();
}

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

/**
 * Returns the preferred width.
 */
public double getPrefWidthImpl(double aH)  { return getLayout().getPrefWidth(-1); }

/**
 * Returns the preferred height.
 */
public double getPrefHeightImpl(double aW)  { return getLayout().getPrefHeight(-1); }

/**
 * Layout children.
 */
protected void layoutChildren()  { getLayout().layoutChildren(); }

/**
 * Returns the layout.
 */
protected ViewLayout getLayout()
{
    if(_layout!=null) return _layout;
    if(isHorizontal()) {
        ViewLayout.HBoxLayout hbox = new ViewLayout.HBoxLayout(this); hbox.setSpacing(_spacing);
        return _layout = hbox;
    }
    ViewLayout.VBoxLayout vbox = new ViewLayout.VBoxLayout(this); vbox.setSpacing(_spacing);
    return _layout = vbox;
}

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return "Text"; }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);

    // Archive Text and ImageName
    String text = getText(); if(text!=null && text.length()>0) e.add("text", text);
    String iname = getImageName(); if(iname!=null) e.add("image", iname);

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
}

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + " { text=" + getText() + "}"; }

}