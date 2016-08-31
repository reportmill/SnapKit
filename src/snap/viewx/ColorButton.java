package snap.viewx;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * A custom class.
 */
public class ColorButton extends View {

    // The color
    Color          _color = Color.BLACK;
    
    // The title
    String         _title;
    
    // The color image
    Image          _image;
    
    // Whether button is armed
    boolean        _armed;
    
    // Whether button is under mouse
    boolean        _targeted;
    
    // Whether button is being tracked by mouse
    boolean        _tracked;
    
    // The Popup
    PopupWindow    _popup;
    
    // The down arrow image
    static Image   _arrowImg;

/**
 * Creates a new ColorButton.
 */
public ColorButton()  { enableEvents(MouseEvents); enableEvents(Action); repaint(); }

/**
 * Returns the color.
 */
public Color getColor()  { return _color; }

/**
 * Sets the color.
 */
public void setColor(Color aColor)
{
    _color = aColor; repaint();
    
    if(getTitle()!=null)
        PrefsUtils.prefsPut(_title, _color==null? null : _color.toHexString());
}

/**
 * Returns the title.
 */
public String getTitle()  { return _title; }

/**
 * Sets the title.
 */
public void setTitle(String aTitle)
{
    _title = aTitle;
        
    // If color string is found, set color
    String cstr = PrefsUtils.prefs().get(_title, null);
    if(cstr!=null)
        _color = Color.get(cstr);
    repaint();
}

/**
 * Returns the image.
 */
public Image getImage()
{
    if(_image!=null) return _image;
    if(_title.startsWith("Stroke ")) return _image = Image.get(getClass(), "Color_StrokeColor.png");
    if(_title.startsWith("Text ")) return _image = Image.get(getClass(), "Color_TextColor.png");
    return _image = Image.get(getClass(), "Color_FillColor.png");
}

/**
 * Sets the image.
 */
public void setImage(Image anImage)  { _image = anImage; repaint(); }

/**
 * Paints the button.
 */
protected void paintFront(Painter aPntr)
{
    // Paint background (clear if normal, grey if armed)
    double w = getWidth()-1, h = getHeight()-1;
    if(_armed) { aPntr.setPaint(Color.LIGHTGRAY); aPntr.fillRect(0,0,w,h); }
    
    // Paint border if targeted (under mouse)
    if(_targeted) { aPntr.setColor(Color.BLACK); aPntr.drawRect(.5,.5,w-1,h-1); aPntr.drawLine(w-10,0,w-10,h); }
    
    // Paint base icon
    aPntr.drawImage(getImage(), 2, 2);
    
    // Paint arrow
    aPntr.drawImage(getDownArrowImage(), 21, 9);
    
    // Paint color swatch
    if(_color==null) { aPntr.setColor(Color.DARKGRAY); aPntr.drawRect(3, 15, 14, 4); }
    else {
        aPntr.setColor(_color); aPntr.fillRect(3, 15, 14, 4);
        aPntr.setColor(_color.darker()); aPntr.drawRect(3, 15, 14, 4);
    }
}

/**
 * Handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MouseEntered, MouseExited, MousePressed, MouseReleased
    if(anEvent.isMouseEntered()) { _targeted = true; _armed = _tracked; repaint(); }
    else if(anEvent.isMouseExited())  { _targeted = _armed = false; repaint(); }
    else if(anEvent.isMousePressed())  { _tracked = true; _armed = true; repaint(); }
    else if(anEvent.isMouseReleased())  { if(_armed) fire(anEvent); _armed = _tracked = false; repaint(); }
}

/**
 * Handles button click.
 */
public void fire(ViewEvent anEvent)
{
    if(anEvent==null || anEvent.getX()<getWidth()-10)
        fireActionEvent();
    else getPopup().show(this,0,getHeight());
}

/**
 * Returns the popup.
 */
public PopupWindow getPopup()
{
    // If already created, just return
    if(_popup!=null) return _popup;
    
    // Create PopupWindow and content
    PopupWindow popup = new PopupWindow(); Insets pad = new Insets(2,2,2,20);
    VBox content = new VBox(); content.setFillWidth(true); content.setPadding(2,2,2,2);
    popup.setContent(content);
    
    // Add title and ColorBoxesPane
    Label titleLabel = new Label(); titleLabel.setText(getTitle()); titleLabel.setPadding(pad);
    content.addChild(titleLabel);
    content.addChild(new Separator());
    content.addChild(new ColorBoxesPane());
    
    // Add Menus
    content.addChild(new Separator());
    MenuItem noneMenu = new MenuItem(); noneMenu.setName("NoneMenu"); noneMenu.setText("None");
    content.addChild(noneMenu); noneMenu.addEventHandler(e -> handlePopupMenuEvent(e), Action);
    MenuItem moreMenu = new MenuItem(); moreMenu.setName("MoreMenu"); moreMenu.setText("More...");
    content.addChild(moreMenu); moreMenu.addEventHandler(e -> handlePopupMenuEvent(e), Action);
    return _popup = popup;
}

/**
 * Handles popup menu item events.
 */
protected void handlePopupMenuEvent(ViewEvent anEvent)
{
    // Handle None: Set null color and fireActionEvent for ColorButton
    if(anEvent.equals("NoneMenu")) {
        setColor(null);
        fireActionEvent();
    }
    
    // Handle MoreMenu: Make ColorPanel visible
    if(anEvent.equals("MoreMenu"))
        ColorPanel.getShared().setWindowVisible(true);
        
    // Hide Popup
    getPopup().hide();
}

/**
 * Returns an Icon of a down arrow.
 */
private Image getDownArrowImage()
{
    // If down arrow icon hasn't been created, create it
    if(_arrowImg!=null) return _arrowImg;
    Image img = Image.get(11,10,true); Painter pntr = img.getPainter();
    Path p = new Path(); p.moveTo(2.5f, 1f); p.lineTo(5.5f, 6f); p.lineTo(8.5f, 1f); p.close();
    pntr.setColor(Color.BLACK); pntr.fill(p); pntr.flush();
    return _arrowImg = img;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = super.toXML(anArchiver);
    if(getTitle()!=null && getTitle().length()>0) e.add("Title", getTitle());
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXML(anArchiver, anElement);
    if(anElement.hasAttribute("Title")) setTitle(anElement.getAttributeValue("Title"));
    return this;
}

/**
 * ColorBoxesPane.
 */
public class ColorBoxesPane extends View {
    
    // Mouse x/y, Armed x/y
    int  _mx = -1, _my = -1, _armx = -1, _army = -1;
    
    /** Creates new ColorBoxesPane. */
    public ColorBoxesPane()  { setPrefSize(8*20,5*20); enableEvents(MouseEvents); }
    
    /** Paint ColorBoxesPane. */
    protected void paintFront(Painter aPntr)
    {
        int mx = _mx, my = _my, armx = _armx, army = _army;
        if(armx>=0 && (armx!=mx || army!=my)) { mx = my = armx = army = -1; }
        for(int i=0;i<8;i++) for(int j=0;j<5;j++) {
            if(i==armx && j==army) { aPntr.setColor(Color.GRAY); aPntr.fillRect(i*20+1,j*20+1,18,18); }
            aPntr.setColor(_colors[i+j*8]); aPntr.fillRect(i*20+4,j*20+4,12,12);
            aPntr.setColor(Color.BLACK); aPntr.drawRect(i*20+4.5,j*20+4.5,11,11);
            if(i==mx && j==my) aPntr.drawRect(i*20+1.5,j*20+1.5,17,17);
        }
    }
    
    /** Handle Events. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMouseMoved()) { _mx = (int)anEvent.getX()/20; _my = (int)anEvent.getY()/20; repaint(); }
        if(anEvent.isMouseExited()) { _mx = _my = -1; repaint(); }
        if(anEvent.isMousePressed()) { _armx = (int)anEvent.getX()/20; _army = (int)anEvent.getY()/20; repaint(); }
        if(anEvent.isMouseDragged()) { _mx = (int)anEvent.getX()/20; _my = (int)anEvent.getY()/20; repaint(); }
        if(anEvent.isMouseReleased()) {
            int mx = (int)anEvent.getX()/20, my = (int)anEvent.getY()/20;
            if(_armx==mx && _army==my) {
                setColor(_colors[_armx+_army*8]); fire(null); getPopup().hide(); }
            _mx = _my = _armx = _army = -1; repaint();
        }
    }
}

// Get colors
static Color _colors[] = {
    Color.BLACK, new Color(158,61,12), new Color(61,61,12), new Color(12,61,12),
    new Color(12,61,109), new Color(12,12,134), new Color(58,58,155), new Color(61,61,61),
    new Color(134,12,12), new Color(255,109,12), new Color(134,134,12), new Color(12,134,12),
    new Color(12,134,134), new Color(9,9,252), new Color(105,105,154), new Color(134,134,134),
    new Color(255,12,12), new Color(255,158,12), new Color(158,196,12), new Color(61,158,109),
    new Color(61,206,206), new Color(61,109,255), new Color(129,8,129), new Color(155,155,155),
    new Color(255,12,255), new Color(255,206,12), new Color(255,255,12), new Color(12,255,12),
    new Color(4,247,247), new Color(12,206,255), new Color(158,61,109), new Color(195,195,195),
    new Color(255,158,206), new Color(255,206,158), new Color(255,255,158), new Color(206,255,206),
    new Color(205,254,254), new Color(158,206,255), new Color(206,158,255), new Color(255,255,255),
};
    
}