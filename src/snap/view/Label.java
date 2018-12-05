/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A class to display simple text with associcated image(s).
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
    
    // Whether label text is editable
    boolean         _editable, _editing;
    
    // A textfield for editing
    TextField       _editor;
    
    // Constants for properties
    public static final String Editable_Prop = "Editable";
    public static final String Editing_Prop = "Editing";
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
    StringView sview = new StringView(); sview.setGrowWidth(true); sview.setAlign(getAlign().getHPos());
    setStringView(sview);
    return _strView;
}

/**
 * Sets the text node.
 */
protected void setStringView(StringView aStrView)
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
    if(aValue==_spacing) return;
    firePropChange(Spacing_Prop, _spacing, _spacing = aValue);
    relayout(); relayoutParent();
}

/**
 * Returns whether label text is editable.
 */
public boolean isEditable()  { return _editable; }

/**
 * Sets whether label text is editable.
 */
public void setEditable(boolean aValue)
{
    if(aValue==isEditable()) return;
    firePropChange(Editable_Prop, _editable, _editable = aValue);
    if(aValue) enableEvents(MouseRelease);
    else disableEvents(MouseRelease);
}

/**
 * Returns whether editable.
 */
public boolean isEditing()  { return _editing; }

/**
 * Sets editing.
 */
public void setEditing(boolean aValue)
{
    // If value already set, just return
    if(aValue==isEditing()) return;
    _editing = aValue;
    
    // Handle set true
    if(aValue) {
        TextField editor = getEditor(); editor.setText(getText());
        Rect bnds = getStringView(true).getBounds(); bnds.inset(-2); editor.setBounds(bnds);
        addChild(editor); editor.requestFocus();
        getStringView().setPaintable(false);
    }
    
    // Handle set false
    else {
        removeChild(_editor);
        setText(_editor.getText());
        getStringView().setPaintable(true); _editor = null;
    }
    
    // Fire prop change
    firePropChange(Editing_Prop, !aValue, aValue);
}

/**
 * Returns the editor.
 */
public TextField getEditor()
{
    // If editor set, return
    if(_editor!=null) return _editor;
    
    // Create and return editor
    TextField editor = new TextField(); editor.setManaged(false);editor.setRadius(2);
    editor.setFill(new Color(1,.95));
    editor.setBorder(new Color(1,.3,.3,.5), 1); editor.getBorder().setInsets(Insets.EMPTY);
    editor.setPadding(2,2,2,2); editor.setAlign(getAlign().getHPos()); editor.setFont(getFont());
    editor.addEventHandler(e -> setEditing(false), Action);
    editor.addPropChangeListener(pc -> { if(!editor.isFocused()) setEditing(false); }, Focused_Prop);
    return _editor = editor;
}

/**
 * Handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMouseRelease() && anEvent.getClickCount()==2)
        setEditing(true);
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    if(isHorizontal())
        return RowView.getPrefWidth(this, null, getSpacing(), aH);
    return ColView.getPrefWidth(this, null, -1);
}

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    if(isHorizontal())
        return RowView.getPrefHeight(this, null, aW);
    return ColView.getPrefHeight(this, null, getSpacing(), -1);
}

/**
 * Layout children.
 */
protected void layoutImpl()
{
    if(isHorizontal()) RowView.layout(this, null, null, false, getSpacing());
    else ColView.layout(this, null, null, false, getSpacing());
}

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return "Text"; }

/**
 * Override to make default align center-left.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

/**
 * Override to forward to StringView.
 */
public void setAlign(Pos aPos)
{
    super.setAlign(aPos);
    if(getStringView()!=null) getStringView().setAlign(aPos.getHPos());
}

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