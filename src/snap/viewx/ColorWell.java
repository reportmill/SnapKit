/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.Path;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * This control view displays a color value and interacts with ColorPanel when selected.
 */
public class ColorWell extends View {
    
    // The current color
    private Color  _color = Color.BLACK;
    
    // Whether the well can be selected 
    private boolean  _selectable = true;
    
    // Whether color well is selected
    private boolean  _selected;
    
    // Indicates that this well is the current drag source
    private boolean  _dragging;
    
    // Constants for properties
    public static final String Color_Prop = "Color";
    public static final String Selectable_Prop = "Selectable";

    /**
     * Creates a new ColorWell.
     */
    public ColorWell()
    {
        enableEvents(Action, MouseRelease, DragGesture, DragSourceEnd);
        enableEvents(DragEvents);
    }

    /**
     * Returns the color represented by this color well.
     */
    public Color getColor()  { return _color; }

    /**
     * Sets the color represented by this color well.
     */
    public void setColor(Color aColor)
    {
        // If already set, just return
        if (SnapUtils.equals(aColor, _color)) return;

        // Set value, fire prop change and repaint
        firePropChange(Color_Prop, _color, _color = aColor);
        repaint();

        // If selected, forward to ColorPanel
        if (isSelected()) {
            Color color = getColor();
            ColorPanel.getShared().setColor(color);
        }
    }

    /**
     * Returns whether color well is selected.
     */
    public boolean isSelected()  { return _selected; }

    /**
     * Sets whether color well is selected.
     */
    public void setSelected(boolean aValue)
    {
        // If disabled, just return
        if (aValue == _selected || !isEnabled()) return;

        // Set new value
        _selected = aValue;

        // Set appropriate border
        repaint();

        // If color well is selected, set in color panel and make color panel visible
        if (_selected && ColorPanel.getShared().getColorWell() != this) {
            ColorPanel.getShared().setColorWell(this);
            ColorPanel.getShared().setWindowVisible(true);
        }

        // If color well is de-selected, set color panel's color well to null
        else if (!_selected && ColorPanel.getShared().getColorWell() == this)
            ColorPanel.getShared().setColorWell(null);
    }

    /**
     * Returns whether or not the well can be selected.
     */
    public boolean isSelectable()  { return _selectable; }

    /**
     * Sets whether or not the well can be selected.
     */
    public void setSelectable(boolean aValue)
    {
        if (aValue == isSelectable()) return;
        firePropChange(Selectable_Prop, _selectable, _selectable = aValue);
        repaint();
    }

    /**
     * Override to make sure any colorwell that is disabled is also deselected
     */
    public void setDisabled(boolean aValue)
    {
        if (aValue)
            setSelected(false);
        super.setDisabled(aValue);
    }

    /**
     * Paints the color well.
     */
    protected void paintFront(Painter aPntr)
    {
        // Get bounds rect
        Rect rect = new Rect(0, 0, getWidth(), getHeight());

        // If Selectable and Selected, paint red border with white lip
        if (isSelectable() && isSelected()) {
            aPntr.setColor(Color.RED); aPntr.fill(rect); rect.inset(1);
            aPntr.setColor(Color.WHITE); aPntr.fill(rect); rect.inset(4);
        }

        // If Selectable and not Selected, paint gray border with raised 3D rect border (4) a lowered border
        else if (isSelectable()) {
            aPntr.setColor(Color.GRAY);
            aPntr.drawRect(rect.x,rect.y,rect.width-1,rect.height-1); rect.inset(1);
            aPntr.setColor(Color.LIGHTGRAY.brighter().brighter().brighter());
            aPntr.fill3DRect(rect.x,rect.y,rect.width,rect.height,true); rect.inset(4);
            aPntr.fill3DRect(rect.x,rect.y,rect.width,rect.height,false); rect.inset(2);
        }

        // If not selectable, paint simple black or gray border
        else {
            aPntr.setColor(Color.BLACK.equals(getColor()) ? Color.GRAY : Color.BLACK);
            aPntr.fill(rect); rect.inset(1);
        }

        // Paint color well area not covered by border
        if (getColor() != null)
            paintSwatch(aPntr, getColor(), rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Paints a color swatch in a standard way.  Used to paint color wells, drag images, and color docks.
     */
    public static void paintSwatch(Painter aPntr, Color c, double x, double y, double w, double h)
    {
        // Draw given color in given rect
        aPntr.setColor(c); aPntr.fillRect(x, y, w, h);

        // If color has an alpha component, fill half the well with a fully-opaque version of the color
        if (c.getAlphaInt() != 255) {
            aPntr.setColor(new Color(c.getRGB() | 0xFF000000));
            Path p = new Path(); p.moveTo(x, y+h); p.lineTo(x+w, y+h); p.lineTo(x+w, y); p.close();
            aPntr.fill(p);
        }
    }

    /**
     * Calls mouse methods.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // If disabled, just return
        if (!isEnabled()) return;

        // Handle MouseRelease: Toggle selection or just show color panel
        if (anEvent.isMouseRelease()) {
            if (isSelectable())
                setSelected(!isSelected());
            else showColorPanel();
        }

        // Handle DragEnter, DragOver
        else if (anEvent.isDragEnter() || anEvent.isDragOver()) {
            Clipboard cb = anEvent.getClipboard();
            if (!_dragging && cb.hasColor())
                anEvent.acceptDrag();
        }

        // Handle DragExit
        else if (anEvent.isDragExit())
            repaint();

        // Handle DragDrop
        else if (anEvent.isDragDrop()) {
            anEvent.acceptDrag(); //dtde.getDropAction());
            Clipboard dboard = anEvent.getClipboard();
            Color color = dboard.getColor();
            setColor(color);
            anEvent.dropComplete(); //true //else dtde.rejectDrop();
            fireActionEvent(anEvent);
        }

        // Handle DragGesture
        else if (anEvent.isDragGesture()) {
            Color color = getColor();
            Image image = Image.get(14,14,true);
            Painter pntr = image.getPainter();
            paintSwatch(pntr,color,0,0,14,14);
            pntr.setColor(Color.BLACK);
            pntr.drawRect(0,0,14-1,14-1);
            Clipboard cboard = anEvent.getClipboard();
            cboard.addData(color);
            cboard.setDragImage(image);
            cboard.startDrag();
            _dragging = true;
        }

        // Handle DragSourceEnd
        else if (anEvent.isDragSourceEnd())
            _dragging = false;

        // Repaint
        repaint();
    }

    /**
     * Shows the color panel.
     */
    public void showColorPanel()
    {
        ColorPanel panel = ColorPanel.getShared();
        panel.setColor(getColor());
        panel.resetLater();
    }

    /**
     * Override to set Selected to false when hidden.
     */
    protected void setShowing(boolean aValue)
    {
        if (aValue == isShowing()) return;
        super.setShowing(aValue);
        setSelected(false);
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return Color_Prop; }

    /**
     * Override because TeaVM hates reflection.
     */
    public Object getPropValue(String aPropName)
    {
        if (aPropName.equals("Value") || aPropName.equals(Color_Prop))
            return getColor();
        return super.getPropValue(aPropName);
    }

    /**
     * Override because TeaVM hates reflection.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        if (aPropName.equals("Value") || aPropName.equals(Color_Prop))
            setColor((Color) aValue);
        else super.setPropValue(aPropName, aValue);
    }

    /**
     * Called by ColorPanel when user selects color in color panel.
     */
    protected void colorPanelChangedColor(ColorPanel aCP, ViewEvent anEvent)
    {
        Color color = aCP.getColor();
        setColor(color);
        fireActionEvent(anEvent);
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXML(anArchiver);
        if (!isSelectable())
            e.add(Selectable_Prop, false);
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXML(anArchiver, anElement);
        if (anElement.hasAttribute(Selectable_Prop))
            setSelectable(anElement.getAttributeBoolValue(Selectable_Prop));
        return this;
    }
}