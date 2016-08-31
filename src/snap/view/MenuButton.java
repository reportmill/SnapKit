package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.*;

/**
 * RMShape subclass for MenuButton.
 */
public class MenuButton extends BorderView {

    // Whether button has border
    boolean              _showBorder = true;
    
    // Whether button shows down arrow
    boolean              _showArrow;

    // The popup point
    Point                _popPoint;
    
    // The popup size
    Size                 _popSize;
    
    // The items
    List <MenuItem>  _items = new ArrayList();
    
    // The label
    Label            _label;

    // Whether button is armed
    boolean              _armed;
    
    // Whether button is under mouse
    boolean              _targeted;
    
    // Whether button is being tracked by mouse
    boolean              _tracked;
    
    // The down arrow image
    static Image         _arrowImg;

/**
 * Creates a new MenuButton.
 */
public MenuButton()
{
    setShowArrow(true);
    enableEvents(MouseEvents);
}

/**
 * Returns the text.
 */
public String getText()  { return getLabel().getText(); }

/**
 * Sets the text.
 */
public void setText(String aString)  { getLabel().setText(aString); }

/**
 * Returns the image.
 */
public Image getImage()  { return getLabel().getImage(); }

/**
 * Sets the image.
 */
public void setImage(Image anImage)  { getLabel().setImage(anImage); }

/**
 * Returns the items.
 */
public List <MenuItem> getItems()  { return _items; }

/**
 * Sets the items.
 */
public void setItems(List <MenuItem> theItems)
{
    _items.clear();
    if(theItems!=null) for(MenuItem mi : theItems) addItem(mi);
}

/**
 * Adds a new item.
 */
public void addItem(MenuItem anItem)  { _items.add(anItem); }

/**
 * Returns whether button shows border.
 */
public boolean isShowBorder()  { return _showBorder; }

/**
 * Sets whether button shows border.
 */
public void setShowBorder(boolean aValue)  { firePropChange("ShowBorder", _showBorder, _showBorder = aValue); }

/**
 * Returns whether button should show arrow.
 */
public boolean isShowArrow()  { return _showArrow; }

/**
 * Sets whether button should show arrow.
 */
public void setShowArrow(boolean aValue)
{
    if(aValue==isShowArrow()) return;
    View iview = aValue? new ImageView(getArrowImage()) : null;
    setRight(iview); if(iview!=null) iview.setPadding(0,2,0,2);
    firePropChange("ShowArrow", _showArrow, _showArrow=aValue);
}

/**
 * Returns the popup point.
 */
public Point getPopupPoint()  { return _popPoint; }

/**
 * Sets the popup point.
 */
public void setPopupPoint(Point aValue)  { firePropChange("PopupPoint", _popPoint, _popPoint = aValue); }

/**
 * Returns the popup size.
 */
public Size getPopupSize()  { return _popSize; }

/**
 * Sets the popup size.
 */
public void setPopupSize(Size aValue)  { firePropChange("PopupSize", _popSize, _popSize = aValue); }

/**
 * Handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MouseEntered, MouseExited, MousePressed, MouseReleased
    if(anEvent.isMouseEntered()) { _targeted = true; _armed = _tracked; repaint(); }
    else if(anEvent.isMouseExited())  { _targeted = _armed = false; repaint(); }
    else if(anEvent.isMousePressed())  { _tracked = true; _armed = true; if(_armed) fire(anEvent); repaint(); }
    else if(anEvent.isMouseReleased())  { _armed = _tracked = false; repaint(); }
}

/**
 * Handles button click.
 */
public void fire(ViewEvent anEvent)
{
    Menu popup = new Menu();
    for(MenuItem item : getItems()) popup.addItem(item);
    popup.show(this,0,getHeight());
}

/**
 * Returns the label.
 */
private Label getLabel()
{
    if(_label!=null) return _label;
    _label = new Label(); setCenter(_label);
    return _label;
}

/**
 * Returns the insets.
 */
public Insets getInsetsAll()
{
    Insets pad = getPadding();
    if(isShowBorder()) pad = new Insets(pad.top+2,pad.right+2,pad.bottom+2,pad.left+2);
    return pad;
}

/**
 * Paint Button.
 */
public void paintFront(Painter aPntr)
{
    if(isShowBorder()) {
        int state = _armed? Painter.BUTTON_PRESSED : _targeted? Painter.BUTTON_OVER : Painter.BUTTON_NORMAL;
        aPntr.drawButton2(0,0,getWidth(),getHeight(), state);
    }
}

/**
 * Returns an Icon of a down arrow.
 */
private Image getArrowImage()
{
    // If down arrow icon hasn't been created, create it
    if(_arrowImg!=null) return _arrowImg;
    Image img = Image.get(9,7,true); Painter pntr = img.getPainter();
    Polygon poly = new Polygon(1.5, 1.5, 7.5, 1.5, 4.5, 5.5);
    pntr.setColor(new Color("#FFFFFF99")); pntr.drawLine(4.5,8,2,2); pntr.drawLine(4.5,8,7,2);
    pntr.setColor(Color.DARKGRAY); pntr.draw(poly); pntr.fill(poly); pntr.flush();
    return _arrowImg = img;
}

/**
 * Override to send to items.
 */
public void setOwner(ViewOwner anOwner)
{
    super.setOwner(anOwner);
    for(View child : _items) child.setOwner(anOwner);
}

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Text and Image (name)
    String text = getText(); if(text!=null && text.length()>0) e.add("text", text);
    Image image = getImage(); String iname = image!=null? image.getName() : null;
    if(iname!=null) e.add("image", iname);

    // Archive ShowBorder, ShowArrow, PopupPoint, PopupSize
    if(!isShowBorder()) e.add("ShowBorder", false);
    if(!isShowArrow()) e.add("ShowArrow", false);
    if(getPopupPoint()!=null) { e.add("popup-x", getPopupPoint().x); e.add("popup-y", getPopupPoint().y); }
    if(getPopupSize()!=null) {
        e.add("popup-width", getPopupSize().width); e.add("popup-height", getPopupSize().height); }
        
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    
    // Unarchive Text and Image name
    String text = anElement.getAttributeValue("text", anElement.getAttributeValue("value"));
    if(text!=null) setText(text);
    String iname = anElement.getAttributeValue("image");
    Image image = iname!=null? Image.get(anArchiver.getSourceURL(), iname) : null;
    if(image!=null) setImage(image);

    // Unarchive ShowBorder, ShowArrow
    if(anElement.hasAttribute("ShowBorder")) setShowBorder(anElement.getAttributeBooleanValue("ShowBorder"));
    setShowArrow(anElement.getAttributeBooleanValue("ShowArrow", true));
    
    // Unarchive PopupPoint
    if(anElement.hasAttribute("popup-x") || anElement.hasAttribute("popup-y")) {
        int x = anElement.getAttributeIntValue("popup-x");
        int y = anElement.getAttributeIntValue("popup-y");
        setPopupPoint(new Point(x, y));
    }
    
    // Unarchive PopupSize
    if(anElement.hasAttribute("popup-width") || anElement.hasAttribute("popup-height")) {
        int w = anElement.getAttributeIntValue("popup-width");
        int h = anElement.getAttributeIntValue("popup-height");
        setPopupSize(new Size(w, h));
    }
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive shapes
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        
        // Get child class - if RMShape, unarchive and add
        Class childClass = anArchiver.getClass(childXML.getName());
        if(childClass!=null && MenuItem.class.isAssignableFrom(childClass)) {
            MenuItem mitem = (MenuItem)anArchiver.fromXML(childXML, this);
            addItem(mitem);
        }
    }
}

}