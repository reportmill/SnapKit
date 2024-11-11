/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.props.PropSet;
import snap.text.TextStyle;
import snap.util.*;
import java.util.Objects;

/**
 * A class to display simple text with associcated image(s).
 */
public class Label extends ParentView {
    
    // The view to show text string
    protected StringView  _stringView;
    
    // The graphics view
    protected View  _graphic;
    
    // The graphics view after text
    private View  _graphicAfter;
    
    // The image name, if loaded from local resource
    private String  _imageName;
    
    // Constants for properties
    public static final String ImageName_Prop = "ImageName";
    public static final String Graphic_Prop = "Graphic";
    public static final String GraphicAfter_Prop = "GraphicAfter";
    public static final String Editing_Prop = "Editing";

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
        this();
        setText(aStr);
    }

    /**
     * Creates a label node with given graphic, text, and after graphic.
     */
    public Label(View aGrph, String aStr, View aGrphAfter)
    {
        this();
        setGraphic(aGrph);
        setText(aStr);
        setGraphicAfter(aGrphAfter);
    }

    /**
     * Returns the text.
     */
    public String getText()
    {
        return _stringView != null ? _stringView.getText() : null;
    }

    /**
     * Sets the text.
     */
    public void setText(String aValue)
    {
        // If value already set or setting null in label with no StringView, just return
        String oldVal = getText(); if (Objects.equals(aValue, oldVal)) return;
        if (aValue == null && !isStringViewSet())
            return;

        // Set value and fire prop change
        StringView stringView = getStringView();
        stringView.setText(aValue);
        stringView.setVisible(aValue != null && !aValue.isEmpty());
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
    public Color getTextColor()
    {
        return _stringView != null ? _stringView.getTextColor() : null;
    }

    /**
     * Sets the text color.
     */
    @Override
    public void setTextColor(Color aColor)
    {
        getStringView().setTextColor(aColor);
    }

    /**
     * Returns the text style.
     */
    public TextStyle getTextStyle()  { return getStringView().getTextStyle(); }

    /**
     * Sets the text style.
     */
    public void setTextStyle(TextStyle textStyle)
    {
        getStringView().setTextStyle(textStyle);
    }

    /**
     * Sets the text style to style updated for style string.
     */
    public void setTextStyleString(String styleString)
    {
        TextStyle textStyle = getTextStyle();
        TextStyle textStyle2 = textStyle.copyForStyleString(styleString);
        setTextStyle(textStyle2);
    }

    /**
     * Returns the StringView.
     */
    protected boolean isStringViewSet()  { return _stringView != null; }

    /**
     * Returns the StringView.
     */
    protected StringView getStringView()
    {
        // If StringView already set, just return
        if (_stringView != null) return _stringView;

        // Create, configure, add StringView and return
        _stringView = new StringView();
        _stringView.setAlignX(getAlignX());
        addChild(_stringView, getGraphic() != null ? 1 : 0);
        return _stringView;
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
        if (_stringView == null)
            return Rect.ZeroRect;
        if (!isNeedsLayout() && _stringView.isShowing())
            return _stringView.getBounds();

        // Layout children and return text bounds
        int textIndex = _stringView.indexInParent();
        ParentViewProxy<?> viewProxy = isHorizontal() ? new RowViewProxy<>(this) : new ColViewProxy<>(this);
        View[] children = _graphic != null && _graphic.isShowing() ? new View[] { _graphic, _stringView } : new View[] { _stringView };
        ViewProxy<?>[] childProxies = ArrayUtils.map(children, child -> ViewProxy.getProxy(child), ViewProxy.class);
        viewProxy.setChildren(childProxies);
        viewProxy.layoutProxy();
        ViewProxy<?> textProxy = childProxies[textIndex];
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
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return "Text"; }

    /**
     * Override to forward to StringView.
     */
    public void setAlign(Pos aPos)
    {
        super.setAlign(aPos);
        if (isStringViewSet())
            getStringView().setAlignX(getAlignX());
    }

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
        switch (aPropName) {

            // ImageName, Graphic, GraphicAfter
            case ImageName_Prop: return getImageName();
            case Graphic_Prop: return getGraphic();
            case GraphicAfter_Prop: return getGraphicAfter();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
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
            case ImageName_Prop: setImageName(Convert.stringValue(aValue)); break;
            case Graphic_Prop: setGraphic((View) aValue); break;
            case GraphicAfter_Prop: setGraphicAfter(((View) aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
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