/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.props.PropSet;
import snap.text.TextModel;
import snap.text.TextStyle;
import snap.util.*;
import java.util.List;
import java.util.Objects;

/**
 * A class to display simple text with associcated image(s).
 */
public class Label extends ParentView {
    
    // The text area
    protected TextArea _textArea;
    
    // The graphics view
    protected View _graphic;
    
    // The graphics view after text
    private View _graphicAfter;
    
    // The image name, if loaded from local resource
    private String _imageName;
    
    // Constants for properties
    public static final String ImageName_Prop = "ImageName";
    public static final String Graphic_Prop = "Graphic";
    public static final String GraphicAfter_Prop = "GraphicAfter";
    public static final String Editing_Prop = "Editing";

    /**
     * Constructor.
     */
    public Label()
    {
        super();

        // Create text area and add
        _textArea = new TextArea(TextModel.createDefaultTextModel());
        _textArea.setVisible(false);
        addChild(_textArea);
    }

    /**
     * Constructor with given text.
     */
    public Label(String aStr)
    {
        this();
        setText(aStr);
    }

    /**
     * Returns the text.
     */
    public String getText()  { return _textArea.getText(); }

    /**
     * Sets the text.
     */
    public void setText(String aValue)
    {
        // If value already set, just return
        String oldVal = getText();
        if (Objects.equals(aValue, oldVal))
            return;

        // Set value and fire prop change
        _textArea.setText(aValue);
        _textArea.setVisible(aValue != null && !aValue.isEmpty());
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
     * Returns the text color.
     */
    @Override
    public Color getTextColor()  { return _textArea.getTextColor(); }

    /**
     * Sets the text color.
     */
    @Override
    public void setTextColor(Color aColor)  { _textArea.setTextColor(aColor); }

    /**
     * Sets the text style to style updated for style string.
     */
    public void setTextStyleString(String styleString)
    {
        TextModel textModel = _textArea.getTextModel();
        TextStyle textStyle = textModel.getTextStyleForCharIndex(0);
        TextStyle textStyle2 = textStyle.copyForStyleString(styleString);
        textModel.setTextStyle(textStyle2, 0, textModel.length());
    }

    /**
     * Returns the TextArea.
     */
    protected TextArea getTextArea()  { return _textArea; }

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
    public String getImageName()  { return _imageName; }

    /**
     * Sets the image name, if loaded from local resource.
     */
    public void setImageName(String aName)
    {
        if (Objects.equals(aName, _imageName)) return;
        firePropChange(ImageName_Prop, _imageName, _imageName = aName);
        repaint();
    }

    /**
     * Returns the text bounds.
     */
    public Rect getTextBounds()
    {
        if (!isNeedsLayout() && _textArea.isShowing())
            return _textArea.getBounds();

        // Layout children and return text bounds
        int textIndex = _textArea.indexInParent();
        ParentViewProxy<?> viewProxy = isHorizontal() ? new RowViewProxy<>(this) : new ColViewProxy<>(this);
        List<View> children = _graphic != null ? List.of(_graphic, _textArea) : List.of(_textArea);
        List<ViewProxy<?>> childProxies = ListUtils.map(children, child -> ViewProxy.getProxy(child));
        viewProxy.setChildren(childProxies);
        viewProxy.layoutProxy();
        ViewProxy<?> textProxy = childProxies.get(textIndex);
        return textProxy;
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
        return ColView.getPrefHeight(this, -1, false);
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        if (isHorizontal())
            RowView.layout(this, false);
        else ColView.layout(this, false);
    }

    /**
     * Override to forward to text area.
     */
    @Override
    public void setAlign(Pos aPos)
    {
        super.setAlign(aPos);
        _textArea.setAlignX(getAlignX());
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return "Text"; }

    /**
     * Initialize Props. Override to provide custom defaults.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // ImageName, Graphic, GraphicAfter
        aPropSet.addPropNamed(ImageName_Prop, String.class, EMPTY_OBJECT);
        //aPropSet.addPropNamed(Graphic_Prop, View.class);
        //aPropSet.addPropNamed(GraphicAfter_Prop, View.class);
    }

    /**
     * Returns the value for given prop name.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        return switch (aPropName) {

            // ImageName, Graphic, GraphicAfter
            case ImageName_Prop -> getImageName();
            case Graphic_Prop -> getGraphic();
            case GraphicAfter_Prop -> getGraphicAfter();

            // Do normal version
            default -> super.getPropValue(aPropName);
        };
    }

    /**
     * Sets the value for given prop name.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch (aPropName) {

            // ImageName, Graphic, GraphicAfter
            case ImageName_Prop -> setImageName(Convert.stringValue(aValue));
            case Graphic_Prop -> setGraphic((View) aValue);
            case GraphicAfter_Prop -> setGraphicAfter(((View) aValue));

            // Do normal version
            default -> super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ImageName
        if (!isPropDefault(ImageName_Prop))
            e.add(ImageName_Prop, getImageName());

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

        // Unarchive ImageName
        if (anElement.hasAttribute(ImageName_Prop)) {
            String imageName = anElement.getAttributeValue(ImageName_Prop);
            setImageName(imageName);
            Image image = ViewArchiver.getImage(anArchiver, imageName);
            if (image != null)
                setImage(image);
        }
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return getClass().getSimpleName() + " { text=" + getText() + "}";
    }
}