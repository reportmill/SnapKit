/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A class to display simple text with associcated image(s).
 */
public class Label extends ParentView {
    
    // The view to show text string
    protected StringView  _strView;
    
    // The graphics view
    protected View  _graphic;
    
    // The graphics view after text
    private View  _graphicAfter;
    
    // The image name, if loaded from local resource
    private String  _iname;
    
    // The spacing between text and graphic
    private double  _spacing = 4;
    
    // The rounding radius
    private double  _rad;
    
    // Whether label text is editable
    private boolean  _editable, _editing;
    
    // A textfield for editing
    private TextField  _editor;
    
    // Constants for properties
    public static final String Editable_Prop = "Editable";
    public static final String Editing_Prop = "Editing";
    public static final String Graphic_Prop = "Graphic";
    public static final String GraphicAfter_Prop = "GraphicAfter";
    public static final String Radius_Prop = "Radius";

    /**
     * Creates a label node.
     */
    public Label()
    {
        super();
    }

    /**
     * Creates a label node with given text.
     */
    public Label(String aStr)
    {
        setText(aStr);
    }

    /**
     * Creates a label node with given graphic, text, and after graphic.
     */
    public Label(View aGrph, String aStr, View aGrphAfter)
    {
        setGraphic(aGrph);
        setText(aStr);
        setGraphicAfter(aGrphAfter);
    }

    /**
     * Returns the text.
     */
    public String getText()
    {
        return _strView != null ? _strView.getText() : null;
    }

    /**
     * Sets the text.
     */
    public void setText(String aValue)
    {
        // If value already set or setting null in label with no StringView, just return
        String oldVal = getText(); if (SnapUtils.equals(aValue, oldVal)) return;
        if (aValue == null && !isStringViewSet())
            return;

        // Set value and fire prop change
        StringView sview = getStringView();
        sview.setText(aValue);
        firePropChange(Text_Prop, oldVal, aValue);
    }

    /**
     * Returns the image.
     */
    public Image getImage()
    {
        return _graphic instanceof ImageView ? ((ImageView)_graphic).getImage() : null;
    }

    /**
     * Sets the image.
     */
    public void setImage(Image anImage)
    {
        Image image = getImage(); if (anImage == image) return;
        if (_graphic instanceof ImageView)
            ((ImageView)_graphic).setImage(anImage);
        else setGraphic(new ImageView(anImage)); //firePropChange("Image", image, anImage); delete soon
    }

    /**
     * Returns the image after text.
     */
    public Image getImageAfter()
    {
        return _graphicAfter instanceof ImageView ? ((ImageView)_graphicAfter).getImage() : null;
    }

    /**
     * Sets the image after text.
     */
    public void setImageAfter(Image anImage)
    {
        Image image = getImage(); if (anImage == image) return;
        if (_graphicAfter instanceof ImageView)
            ((ImageView)_graphicAfter).setImage(anImage);
        else setGraphicAfter(new ImageView(anImage)); //firePropChange("Image", image, anImage); delete soon
    }

    /**
     * Returns the text fill.
     */
    public Paint getTextFill()
    {
        return _strView != null ? _strView.getTextFill() : null;
    }

    /**
     * Sets the text fill.
     */
    public void setTextFill(Paint aPaint)
    {
        getStringView().setTextFill(aPaint);
    }

    /**
     * Returns the StringView.
     */
    public boolean isStringViewSet()  { return _strView!=null; }

    /**
     * Returns the StringView.
     */
    public StringView getStringView()
    {
        // If StringView already set, just return
        if (_strView != null) return _strView;

        // Create, configure, add StringView and return
        _strView = new StringView();
        _strView.setGrowWidth(isEditable());
        _strView.setAlign(getAlign().getHPos());
        addChild(_strView, getGraphic() != null ? 1 : 0);
        return _strView;
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
        // If already set, just return
        View old = getGraphic(); if (aGraphic == old) return;

        // Remove old
        if (_graphic != null && _graphic.getParent() != null)
            removeChild(_graphic);

        // Set
        _graphic = aGraphic;

        // Add new
        if (_graphic != null)
            addChild(_graphic, 0);

        // Fire prop change
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
        // If already set, just return
        View old = getGraphicAfter(); if (aGraphic == old) return;

        // Remove old
        if (_graphicAfter != null && _graphicAfter.getParent() != null)
            removeChild(_graphicAfter);

        // Set new
        _graphicAfter = aGraphic;

        // Add new
        if (_graphicAfter != null)
            addChild(_graphicAfter);

        // Fire prop change
        firePropChange(GraphicAfter_Prop, old, _graphicAfter);
    }

    /**
     * Returns the image name, if loaded from local resource.
     */
    public String getImageName()  { return _iname; }

    /**
     * Sets the image name, if loaded from local resource.
     */
    public void setImageName(String aName)
    {
        _iname = aName;
    }

    /**
     * Returns the spacing between text and graphics.
     */
    public double getSpacing()  { return _spacing; }

    /**
     * Sets the spacing between text and graphics.
     */
    public void setSpacing(double aValue)
    {
        if (aValue == _spacing) return;
        firePropChange(Spacing_Prop, _spacing, _spacing = aValue);
        relayout();
        relayoutParent();
    }

    /**
     * Returns the rounding radius.
     */
    public double getRadius()  { return _rad; }

    /**
     * Sets the rounding radius.
     */
    public void setRadius(double aValue)
    {
        if (aValue == _rad) return;
        firePropChange(Radius_Prop, _rad, _rad = aValue);
        repaint();
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
        if (aValue == isEditable()) return;
        firePropChange(Editable_Prop, _editable, _editable = aValue);

        // Enable/Disable MosueRelease
        if (aValue)
            enableEvents(MouseRelease);
        else disableEvents(MouseRelease);

        // If Editable, StringView should fill width
        if (isStringViewSet())
            getStringView().setGrowWidth(isEditable());
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
        if (aValue == isEditing()) return;
        _editing = aValue;

        // Handle set true
        if (aValue) {
            TextField editor = getEditor();
            editor.setText(getText());
            Rect bnds = getStringView().getBounds();
            bnds.inset(-2);
            editor.setBounds(bnds);
            addChild(editor);
            editor.selectAll();
            editor.requestFocus();
            getStringView().setPaintable(false);
        }

        // Handle set false
        else {
            removeChild(_editor);
            setText(_editor.getText());
            getStringView().setPaintable(true);
            _editor = null;
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
        if (_editor != null) return _editor;

        // Create and return editor
        TextField editor = new TextField();
        editor.setManaged(false);
        editor.setRadius(2);
        editor.setFill(new Color(1,.95));
        editor.setBorder(new Color(1,.3,.3,.5), 1);
        editor.setBorder(editor.getBorder().copyForInsets(Insets.EMPTY));
        editor.setPadding(2,2,2,2);
        editor.setAlign(getAlign().getHPos());
        editor.setFont(getFont());
        editor.addEventHandler(e -> editorFiredAction(), Action);
        editor.addPropChangeListener(pc -> editorFocusChanged(editor), Focused_Prop);
        return _editor = editor;
    }

    /**
     * Called when editor fires action.
     */
    protected void editorFiredAction()
    {
        setEditing(false);
        fireActionEvent(null);
    }

    /**
     * Called when editor focus changes.
     */
    protected void editorFocusChanged(TextField editor)
    {
        if (!editor.isFocused())
            setEditing(false);
    }

    /**
     * Override to handle optional rounding radius.
     */
    public Shape getBoundsShape()
    {
        if (_rad>=0)
            return new RoundRect(0,0,getWidth(),getHeight(),_rad);
        return super.getBoundsShape();
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        if (isEditable() && anEvent.isMouseRelease() && anEvent.getClickCount()==2)
            setEditing(true);
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        if (isHorizontal())
            return RowView.getPrefWidth(this, aH);
        return ColView.getPrefWidth(this, -1);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        if (isHorizontal())
            return RowView.getPrefHeight(this, aW);
        return ColView.getPrefHeight(this, -1);
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        if (isHorizontal()) RowView.layout(this, false);
        else ColView.layout(this, false);
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
        if (isStringViewSet()) getStringView().setAlign(aPos.getHPos());
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive Text and ImageName
        String text = getText();
        if (text!=null && text.length()>0) e.add("text", text);
        String iname = getImageName();
        if (iname!=null) e.add("image", iname);

        // Archive Spacing, Radius
        if (getSpacing()!=4) e.add(Spacing_Prop, getSpacing());
        if (getRadius()!=0) e.add(Radius_Prop, getRadius());

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
        if (iname!=null) {
            setImageName(iname);
            Image image = ViewArchiver.getImage(anArchiver, iname);
            if (image!=null) setImage(image);
        }

        // Unarchive Spacing, Radius
        if (anElement.hasAttribute(Spacing_Prop))
            setSpacing(anElement.getAttributeDoubleValue(Spacing_Prop));
        if (anElement.hasAttribute(Radius_Prop))
            setRadius(anElement.getAttributeDoubleValue(Radius_Prop));
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return getClass().getSimpleName() + " { text=" + getText() + "}";
    }
}