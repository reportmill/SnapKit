/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.RoundRect;
import snap.geom.Shape;
import snap.gfx.*;
import snap.props.PropObject;
import snap.props.PropSet;
import snap.util.*;
import java.util.Objects;

/**
 * A View for Buttons.
 */
public class ButtonBase extends ParentView {
    
    // The button label
    private Label  _label;
    
    // Whether button displays the standard background area
    protected boolean _showArea;
    
    // The position of the button when in a group (determines corner rendering)
    private Pos _position;

    // The image name, if loaded from local resource
    private String  _imageName;

    // The shared action
    private SharedAction _sharedAction;
    
    // Whether button is pressed
    private boolean  _pressed;
    
    // Whether button is under mouse
    protected boolean  _targeted;
    
    // Whether button is being tracked by mouse
    private boolean  _tracked;
    
    // Constants for properties
    public static final String ShowArea_Prop = "ShowArea";
    public static final String Position_Prop = "Position";
    public static final String ImageName_Prop = "ImageName";
    public static final String Pressed_Prop = "Pressed";
    public static final String Targeted_Prop = "Targeted";

    // Button states
    public static final int BUTTON_NORMAL = 0;
    public static final int BUTTON_OVER = 1;
    public static final int BUTTON_PRESSED = 2;

    // Constants for property defaults
    private static final boolean DEFAULT_SHOW_AREA = true;

    /**
     * Constructor.
     */
    public ButtonBase()
    {
        super();
        _showArea = DEFAULT_SHOW_AREA;

        // Config
        setFocusable(true);
        setActionable(true);
        enableEvents(MouseEvents);
        enableEvents(KeyPress);
    }

    /**
     * Returns the label.
     */
    public Label getLabel()
    {
        if (_label != null) return _label;
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
    public void setText(String aStr)  { getLabel().setText(aStr); }

    /**
     * Returns the image.
     */
    public Image getImage()  { return getLabel().getImage(); }

    /**
     * Sets the image.
     */
    public void setImage(Image anImage)  { getLabel().setImage(anImage); }

    /**
     * Returns the image after text.
     */
    public Image getImageAfter()  { return getLabel().getImageAfter(); }

    /**
     * Sets the image after text.
     */
    public void setImageAfter(Image anImage)  { getLabel().setImageAfter(anImage); }

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
     * Returns the text color.
     */
    @Override
    public Color getTextColor()  { return getLabel().getTextColor(); }

    /**
     * Sets the text color.
     */
    @Override
    public void setTextColor(Color aColor)  { getLabel().setTextColor(aColor); }

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
     * Returns whether button displays the standard background area.
     */
    public boolean isShowArea()  { return _showArea; }

    /**
     * Sets whether button displays the standard background area.
     */
    public void setShowArea(boolean aValue)
    {
        if (aValue == _showArea) return;
        firePropChange(ShowArea_Prop, _showArea, _showArea = aValue);
    }

    /**
     * Returns the position of the button when in a group (determines corner rendering).
     */
    public Pos getPosition()  { return _position; }

    /**
     * Sets the position of the button when in a group (determines corner rendering).
     */
    public void setPosition(Pos aPos)
    {
        if (aPos == _position) return;
        firePropChange(Position_Prop, _position, _position = aPos);
        repaint();
    }

    /**
     * Returns the shared action (if set).
     */
    public SharedAction getSharedAction()  { return _sharedAction; }

    /**
     * Sets the shared action (if set).
     */
    public void setSharedAction(SharedAction sharedAction)
    {
        _sharedAction = sharedAction;
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
        if (aValue == _pressed) return;
        firePropChange(Pressed_Prop, _pressed, _pressed = aValue);
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
        if (aValue == _targeted) return;
        firePropChange(Targeted_Prop, _targeted, _targeted = aValue);
        repaint();
    }

    /**
     * Returns whether button is selected (really for ToggleButton subclasses).
     */
    public boolean isSelected()  { return false; }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // If disabled, just return
        if (isDisabled()) return;

        // Handle MouseEnter
        if (anEvent.isMouseEnter()) {
            setTargeted(true);
            setPressed(_tracked);
        }

        // Handle MouseExit
        else if (anEvent.isMouseExit()) {
            setTargeted(false);
            setPressed(false);
        }

        // Handle MousePress
        else if (anEvent.isMousePress()) {
            _tracked = true;
            setPressed(true);
            anEvent.consume();
        }

        // Handle MouseReleased
        else if (anEvent.isMouseRelease()) {
            if (_pressed)
                fireActionEvent(anEvent);
            _pressed = _tracked = false;
            repaint();
        }

        // Handle KeyPress + Enter
        if (anEvent.isKeyPress() && anEvent.getKeyCode() == KeyCode.SPACE)
            fireActionEvent(anEvent);
    }

    /**
     * Perform button click.
     */
    public void fire()  { fireActionEvent(null); }

    /**
     * Override to consume event.
     */
    @Override
    protected void fireActionEvent(ViewEvent anEvent)
    {
        super.fireActionEvent(anEvent);
        if (anEvent != null)
            anEvent.consume();
    }

    /**
     * Override to set SourceAction if set.
     */
    @Override
    protected ViewEvent createActionEvent(ViewEvent sourceEvent)
    {
        ViewEvent actionEvent = super.createActionEvent(sourceEvent);
        if (_sharedAction != null)
            actionEvent.setSharedAction(_sharedAction);
        return actionEvent;
    }

    /**
     * Paint Button.
     */
    protected final void paintFront(Painter aPntr)
    {
        paintButton(aPntr);
    }

    /**
     * Paint Button.
     */
    protected void paintButton(Painter aPntr)
    {
        // If ShowArea, use ButtonArea to paint actual button background
        if (isShowArea()) {
            ButtonPainter buttonPainter = ViewTheme.get().getButtonPainter();
            buttonPainter.paintButton(aPntr, this);
        }

        // If not ShowArea, paint rects for Pressed or Targeted
        else {

            // If pressed, paint background
            if (isPressed() || isSelected()) {
                Shape shape = getBoundsShape();
                Paint fill = ViewUtils.getSelectFill();
                aPntr.fillWithPaint(shape, fill);
                if (getBorder() != null)
                    getBorder().paint(aPntr, shape);
            }

            // If Targeted, paint border
            else if (isTargeted()) {
                Shape shape = getBoundsShape();
                Paint fill = ViewUtils.getTargetFill();
                aPntr.fillWithPaint(shape, fill);
                if (getBorder() != null)
                    getBorder().paint(aPntr, shape);
            }
        }
    }

    /**
     * Override to do bogus disabled painting.
     */
    public void paintAll(Painter aPntr)
    {
        // If disabled, paint semi-transparent, with round-rect above
        if (isDisabled()) {

            // Do normal paint at half transparent
            double oldOpacity = aPntr.getOpacity();
            aPntr.setOpacity(oldOpacity * .5);
            super.paintAll(aPntr);
            aPntr.setOpacity(oldOpacity);

            // Paint semi-transparent round rect on top
            Color DISABLED_FILL = new Color(1, 1, 1, .2);
            aPntr.setColor(DISABLED_FILL);
            RoundRect rect = new RoundRect(0, 0, getWidth(), getHeight(), 4);
            aPntr.fill(rect);
        }

        // Otherwise paint normal version
        else super.paintAll(aPntr);
    }

    /**
     * Returns the ViewProxy to layout button.
     */
    @Override
    protected BoxViewProxy<?> getViewProxyImpl()
    {
        // Create ViewProxy with Label ViewProxy as Content
        BoxViewProxy<?> viewProxy = new BoxViewProxy<>(this);
        ViewProxy<?> labelProxy = getLabel().getViewProxy();
        viewProxy.setContent(labelProxy);

        // If ShowArea, add padding
        if (isShowArea()) {
            Insets padding = Insets.add(viewProxy.getPadding(), 2, 2, 2, 2);
            viewProxy.setPadding(padding);
        }

        // Return ViewProxy
        return viewProxy;
    }

    /**
     * Override to make sure button is no longer targeted.
     */
    @Override
    public void setDisabled(boolean aValue)
    {
        // Do normal version
        if (aValue == isDisabled()) return;
        super.setDisabled(aValue);

        // If disabled, turn off Targeted
        if (aValue)
            setTargeted(false);
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        super.initProps(aPropSet);

        // ShowArea, Position, ImageName
        aPropSet.addPropNamed(ShowArea_Prop, boolean.class, DEFAULT_SHOW_AREA);
        aPropSet.addPropNamed(Position_Prop, Pos.class);
        aPropSet.addPropNamed(ImageName_Prop, String.class, PropObject.EMPTY_OBJECT);
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        return switch (aPropName) {

            // ShowArea, Position, ImageName
            case ShowArea_Prop -> isShowArea();
            case Position_Prop -> getPosition();
            case ImageName_Prop -> getImageName();

            // Do normal version
            default -> super.getPropValue(aPropName);
        };
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // ShowArea, Position, ImageName
            case ShowArea_Prop -> setShowArea(Convert.boolValue(aValue));
            case Position_Prop -> setPosition(Pos.of(aValue));
            case ImageName_Prop -> setImageName(Convert.stringValue(aValue));

            // Do normal version
            default -> super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival.
     */
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ImageName
        if (!isPropDefault(ImageName_Prop))
            e.add(ImageName_Prop, getImageName());

        // Archive ShowArea, Position
        if (!isPropDefault(ShowArea_Prop))
            e.add(ShowArea_Prop, isShowArea());
        if (!isPropDefault(Position_Prop))
            e.add(Position_Prop, getPosition());

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

        // Unarchive ShowArea, Position
        if (anElement.hasAttribute(ShowArea_Prop)) // Archival legacy: ShowBorder
            setShowArea(anElement.getAttributeBoolValue(ShowArea_Prop));
        if (anElement.hasAttribute(Position_Prop))
            setPosition(anElement.getAttributeEnumValue(Position_Prop, Pos.class, null));
    }
}