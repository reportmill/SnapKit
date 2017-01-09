/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * This Swing control class displays a color value and kicks off the ColorPanel when clicked.
 */
public class ColorWell extends View {
    
    // The current color
    Color              _color = Color.BLACK;
    
    // Whether the well can be selected 
    boolean            _selectable = true;
    
    // Whether color well is selected
    boolean            _selected;
    
    // Indicates that this well is the current drag source
    boolean            _dragging;
    
    // Constants for properties
    public static final String Color_Prop = "Color";

/**
 * Creates a new ColorWell.
 */
public ColorWell()
{
    enableEvents(Action, MouseRelease, DragGesture, DragSourceEnd); enableEvents(DragEvents);
    repaint();
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
    if(SnapUtils.equals(aColor, _color)) return;
    firePropChange(Color_Prop, _color, _color = aColor);
    repaint();
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
    if(!isEnabled()) return;
    
    // Set new value
    _selected = aValue;
    
    // Set appropriate border
    repaint();

    // If color well is selected, set in color panel and make color panel visible
    if(_selected && ColorPanel.getShared().getColorWell()!=this) {
        ColorPanel.getShared().setColorWell(this);
        ColorPanel.getShared().setWindowVisible(true);
    }
    
    // If color well is de-selected, set color panel's color well to null
    else if(!_selected && ColorPanel.getShared().getColorWell()==this)
        ColorPanel.getShared().setColorWell(null);
}

/**
 * Returns whether or not the well can be selected.
 */
public boolean isSelectable()  { return _selectable; }

/**
 * Sets whether or not the well can be selected.
 */
public void setSelectable(boolean flag)  { _selectable = flag; repaint(); }

/**
 * Override to make sure any colorwell that is disabled is also deselected
 */
public void setDisabled(boolean aValue)
{
    super.setDisabled(aValue);
    if(aValue) setSelected(false);
}

/**
 * Paints the color well.
 */
protected void paintFront(Painter aPntr) 
{
    // Get bounds rect
    Rect rect = new Rect(0, 0, getWidth(), getHeight());
    
    // If Selectable and Selected, paint red border with white lip
    if(isSelectable() && isSelected()) {
        aPntr.setColor(Color.RED); aPntr.fill(rect); rect.inset(1);
        aPntr.setColor(Color.WHITE); aPntr.fill(rect); rect.inset(4);
    }
    
    // If Selectable and not Selected, paint gray border with raised 3D rect border (4) a lowered border
    else if(isSelectable()) {
        aPntr.setColor(Color.GRAY);
        aPntr.drawRect(rect.x,rect.y,rect.width-1,rect.height-1); rect.inset(1);
        aPntr.setColor(Color.LIGHTGRAY.brighter().brighter().brighter());
        aPntr.fill3DRect(rect.x,rect.y,rect.width,rect.height,true); rect.inset(4);
        aPntr.fill3DRect(rect.x,rect.y,rect.width,rect.height,false); rect.inset(2);
    }
    
    // If not selectable, paint simple black or gray border
    else {
        aPntr.setColor(Color.BLACK.equals(getColor())? Color.GRAY : Color.BLACK);
        aPntr.fill(rect); rect.inset(1);
    }
    
    // Paint color well area not covered by border
    if(getColor()!=null) paintSwatch(aPntr,getColor(),rect.x,rect.y,rect.width,rect.height);
}

/**
 * Paints a color swatch in a standard way.  Used to paint color wells, drag images, and color docks.
 */
public static void paintSwatch(Painter aPntr, Color c, double x, double y, double w, double h)
{
    // Draw given color in given rect
    aPntr.setColor(c); aPntr.fillRect(x, y, w, h);
    
    // If color has an alpha component, fill half the well with a fully-opaque version of the color
    if(c.getAlphaInt()!=255) {
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
    if(!isEnabled()) return;
    
    // Handle MouseClick
    if(anEvent.isMouseClick()) {
        
        // If is selectable, toggle selected state
        if(isSelectable())
            setSelected(!isSelected());
        
        // If isn't selectable, make the color panel display this color
        else {
            ColorPanel panel = ColorPanel.getShared();
            panel.setColor(getColor());
            panel.resetLater();
        }
    }
    
    // Handle DragEnter
    else if(anEvent.isDragEnter()) {
        Clipboard cb = anEvent.getDragboard();
        if(!_dragging && cb.hasColor()) anEvent.acceptDrag(); //dtde.getDropAction());
        //else dtde.rejectDrag();
    }
    
    // Handle DragEnter
    else if(anEvent.isDragOver()) { Clipboard cb = anEvent.getDragboard();
        if(!_dragging && cb.hasColor()) anEvent.acceptDrag(); }
    
    // Handle DragExit
    else if(anEvent.isDragExit())
        repaint();
        
    // Handle DragDrop
    else if(anEvent.isDragDrop()) {
        anEvent.acceptDrag(); //dtde.getDropAction());
        Clipboard dboard = anEvent.getDragboard();
        Color color = dboard.getColor();
        setColor(color);
        anEvent.dropComplete(); //true //else dtde.rejectDrop();
    }
    
    // Handle DragGesture
    else if(anEvent.isDragGesture()) {
        Color color = getColor();
        Image image = Image.get(14,14,true); Painter pntr = image.getPainter();
        paintSwatch(pntr,color,0,0,14,14); pntr.setColor(Color.BLACK); pntr.drawRect(0,0,14-1,14-1); pntr.flush();
        Clipboard dboard = anEvent.getDragboard();
        dboard.setContent(color);
        dboard.setDragImage(image);
        dboard.startDrag();
        _dragging = true;
    }
    
    // Handle DragSourceEnd
    else if(anEvent.isDragSourceEnd())
        _dragging = false;
        
    // Repaint
    repaint();
}

/**
 * Override to set Selected to false when hidden.
 */
protected void setShowing(boolean aValue)
{
    if(aValue==isShowing()) return;
    super.setShowing(aValue); setSelected(false);
}

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return "Color"; }

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXML(anArchiver);
    if(!isSelectable()) e.add("Selectable", false);
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXML(anArchiver, anElement);
    if(anElement.hasAttribute("Selectable")) setSelectable(anElement.getAttributeBoolValue("Selectable"));
    return this;
}

}