/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;

import snap.geom.Rect;
import snap.geom.RoundRect;
import snap.gfx.*;
import snap.view.*;

/**
 * This class provides UI for selecting a color.
 */
public class ColorPanel extends ViewOwner {
    
    // The currently selected color
    Color              _color = Color.BLACK;
    
    // The ColorWell that gets notification of color changes
    ColorWell          _colorWell;
    
    // The ColorWell to fall back on when no current ColorWell set
    ColorWell          _defColorWell;
    
    // The list of previously selected colors
    List <Color>       _recentColors = new ArrayList();
    
    // The shared instance of the color panel
    private static ColorPanel   _shared;

    // Image names
    private static String _iNames[] = { "Spectrum", "Paradise", "Floral", "Fall Leaves", "Metal", "Wood" };
    
    // Image file names
    static Object _images[] = { "spectrum.jpg", "paradise.jpg", "tulips.jpg", "fall.jpg","metal.jpg","wood.jpg" };

/**
 * Creates a new color panel.
 */
public ColorPanel()  { _shared = this; }

/**
 * Returns the shared instance of the color panel.
 */
public static ColorPanel getShared()  { return _shared!=null? _shared : (_shared=new ColorPanel()); }

/**
 * Returns the current color of the color panel.
 */
public Color getColor()  { return _color; }

/**
 * Sets the current color of the color panel.
 */
public void setColor(Color aColor)
{
    // Set new color
    if(aColor==null) aColor = Color.BLACK;
    if(aColor.equals(_color)) return;
    _color = aColor;
}

/**
 * Add to the list of Recent Colors shown on the Color Panel
 */
public void addRecentColor(Color aColor)
{
    // If the color is already in the list, delete it from its old position so it gets moved to the head of the list.
    int oldPosition = _recentColors.indexOf(aColor);
    if(oldPosition != -1) 
        _recentColors.remove(oldPosition);
    else if(oldPosition==0)
        return;
    
    // Add it at the beginning
    _recentColors.add(0, aColor);
    
    // old ones fall off the end of the list.  If you want to keep a color forever, stick it in the dock.
    while(_recentColors.size()>8) _recentColors.remove(_recentColors.size()-1);
    
    // Update HistoryMenuButton
    //MenuButton mbutton = getNative("HistoryMenuButton", MenuButton.class);
    //JPopupMenu pmenu = mbutton.getPopupMenu(); pmenu.removeAll();
    //for(int i=0, iMax=_recentColors.size(); i<iMax; i++) pmenu.add(new ColorMenuItem(_recentColors.get(i)));
    //for(Object mitem : pmenu.getComponents()) initUI(mitem);
}

/**
 * Returns the ColorWell that receives notifications of color panel changes.
 */
public ColorWell getColorWell()  { return _colorWell; }

/**
 * Sets the ColorWell that receives notifications of color panel changes.
 */
public void setColorWell(ColorWell aColorWell)
{
    // If already set, just return
    ColorWell newCW = aColorWell!=null? aColorWell : getDefaultColorWell();
    if(newCW==_colorWell) return;
    
    // Set new ColorWell
    ColorWell oldCW = _colorWell;
    _colorWell = newCW;
    
    // If new ColorWell non-null, notifify of change (to set color)
    if(_colorWell!=null) {
        setColor(_colorWell.getColor());
        _colorWell.setSelected(true);
    }
        
    // If old ColorWell non-null, notifify of change
    if(oldCW!=null)
        oldCW.setSelected(false);
    
    // Reset color panel
    resetLater();
}

/**
 * Returns the ColorWell to fall back on when no ColorWell set.
 */
public ColorWell getDefaultColorWell()  { return _defColorWell; }

/**
 * Sets the ColorWell to fall back on when no ColorWell set.
 */
public void setDefaultColorWell(ColorWell aColorWell)
{
    _defColorWell = aColorWell;
    if(getColorWell()==null)
        setColorWell(_defColorWell);
}

/**
 * Returns the selected picker (0=Image, 1=RGB Sliders, 2=Gray Sliders, 3=SwatchPicker).
 */
public int getSelectedPicker()  { return getViewIntValue("PickerPanel"); }

/**
 * Sets the selected picker (0=Image, 1=RGB Sliders, 2=Gray Sliders, 3=SwatchPicker).
 */
public void setSelectedPicker(int aPicker)
{
    setViewSelIndex("PickerPanel", aPicker);
    resetLater();
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Configure ImagePickerLabel
    getView("ImagePickerLabel", ImagePicker.class).setImage(getImage(0));
    
    // Configure ImageComboBox
    setViewItems("ImageComboBox", _iNames);
    setViewSelItem("ImageComboBox", _iNames[0]);

    // Start with black in the list of recent colors, more will be added as they are inspected or set
    addRecentColor(Color.BLACK);
            
    // Load Dock's colors from preferences
    ColorDock cdock = getView("ColorDock", ColorDock.class);
    cdock.setPersistent(true);
    cdock.setSelectable(false);

    // Configure SwatchPicker
    getView("SwatchPicker", ChildView.class).addChild(new SwatchPanel());
    getUI().setClipToBounds(true);
}

/**
 * Resets UI controls.
 */
protected void resetUI()
{
    // Get color (if null, replace with color clear)
    Color color = getColor(); if(color==null) color = new Color(0,0,0,0);
    
    // Reset color sliders if needed
    switch(getViewIntValue("PickerPanel")) {
    
        // Update RGB picker
        case 1:
            
            // Update RGB sliders/text
            setViewValue("RedSlider", color.getRedInt());
            setViewValue("RedText", color.getRedInt());
            setViewValue("GreenSlider", color.getGreenInt());
            setViewValue("GreenText", color.getGreenInt());
            setViewValue("BlueSlider", color.getBlueInt());
            setViewValue("BlueText", color.getBlueInt());
            setViewValue("AlphaSlider", color.getAlphaInt());
            setViewValue("AlphaText", color.getAlphaInt());
            
            // Update HextText
            setViewValue("HexText", toHexString(color));
            
            // Update the slider color gradients
            int r = color.getRedInt(), g = color.getGreenInt(), b = color.getBlueInt();
            GradSlider slider = getView("RedSlider", GradSlider.class);
            slider.setColors(new Color(0,g,b), new Color(255,g,b));
            slider = getView("GreenSlider", GradSlider.class);
            slider.setColors(new Color(r,0,b), new Color(r,255,b));
            slider = getView("BlueSlider", GradSlider.class);
            slider.setColors(new Color(r,g,0), new Color(r,g,255));
            slider = getView("AlphaSlider", GradSlider.class);
            slider.setColors(new Color(r,g,b,0), new Color(r,g,b,255));
            
            // Break
            break;
            
        // Update gray scale picker
        case 2:
            
            // Update gray/alpha siders/text
            setViewValue("GraySlider", color.getRedInt());
            setViewValue("GrayText", color.getRedInt());
            setViewValue("GrayAlphaSlider", color.getAlphaInt());
            setViewValue("GrayAlphaText", color.getAlphaInt());
            
            // Update the slider alpha gradient
            r = color.getRedInt();
            slider = getView("GrayAlphaSlider", GradSlider.class);
            slider.setColors(new Color(r,r,r,0), new Color(r,r,r,255));
    }
    
    // Update the display well
    setViewValue("DisplayColorWell", color);
}

/**
 * Responds to changes to the UI controls.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ImagePickerButton, RGBPickerButton, GrayPickerButton, SwatchPickerButton
    if(anEvent.equals("ImagePickerButton")) setSelectedPicker(0);
    if(anEvent.equals("RGBPickerButton")) setSelectedPicker(1);
    if(anEvent.equals("GrayPickerButton")) setSelectedPicker(2);
    if(anEvent.equals("SwatchPickerButton")) setSelectedPicker(3);
        
    // Handle ImageLabel
    Color newColor = null;
    if(anEvent.equals("ImagePickerLabel"))
        newColor = anEvent.getView(ImagePicker.class).getColor();
    
    // Handle ImageComboBox
    if(anEvent.equals("ImageComboBox")) {
        Image img = getImage(anEvent.getSelIndex());
        getView("ImagePickerLabel", ImagePicker.class).setImage(img);
    }
    
    // Handle GraySlider or GrayAlphaSlider
    if(anEvent.equals("GraySlider") || anEvent.equals("GrayAlphaSlider")) {
        int g = getViewIntValue("GraySlider"), a = getViewIntValue("GrayAlphaSlider");
        newColor = new Color(g, g, g, a);
    }
    
    // Handle RedSlider, GreenSlider, BlueSlider, AlphaSlider
    if(anEvent.equals("RedSlider") || anEvent.equals("GreenSlider") || anEvent.equals("BlueSlider")
        || anEvent.equals("AlphaSlider")) {
        int r = getViewIntValue("RedSlider"), g = getViewIntValue("GreenSlider");
        int b = getViewIntValue("BlueSlider"), a = getViewIntValue("AlphaSlider");
        newColor = new Color(r, g, b, a);
    }
    
    // Handle GrayText, GrayAlphaText 
    if(anEvent.equals("GrayText") || anEvent.equals("GrayAlphaText")) {
        int g = getViewIntValue("GrayText"), a = getViewIntValue("GrayAlphaText");
        g = g<0? 0 : g>255 ? 255 : g; a = a<0? 0 : a>255 ? 255 : a;
        newColor = new Color(g, g, g, a);
    }
    
    // Handle RedText, GreenText, BlueText, AlphaText
    if(anEvent.equals("RedText") || anEvent.equals("GreenText") || anEvent.equals("BlueText")
        || anEvent.equals("AlphaText")) {
        int r = getViewIntValue("RedText"), g = getViewIntValue("GreenText");
        int b = getViewIntValue("BlueText"), a = getViewIntValue("AlphaText");
        r = r<0? 0 : r>255 ? 255 : r; g = g<0? 0 : g>255 ? 255 : g;
        b = b<0? 0 : b>255 ? 255 : b; a = a<0? 0 : a>255 ? 255 : a;
        newColor = new Color(r, g, b, a);
    }
    
    // Handle Recent Colors Dropdown menu
    //if(anEvent.getView() instanceof ColorMenuItem) setColor(anEvent.getView(ColorMenuItem.class).getColor());
        
    // Handle SwatchPanel
    if(anEvent.equals("SwatchPanel"))
        newColor = ((SwatchPanel)anEvent.getView()).getColor();
        
    // Handle HexText
    if(anEvent.equals("HexText"))
        newColor = fromHexString(anEvent.getStringValue());
        
    // Handle ColorDock
    if(anEvent.equals("ColorDock")) {
        ColorDock cdock = getView("ColorDock", ColorDock.class);
        newColor = cdock.getColor();
    }

    // If new color, set and fireActionEvent
    if(newColor!=null) {
        setColor(newColor);
        fireActionEvent(anEvent);
    }
}

/**
 * Called when color changed by user.
 */
protected void fireActionEvent(ViewEvent anEvent)
{
    // If ColorWell present, notify of user initiated color change
    if(_colorWell!=null)
        _colorWell.colorPanelChangedColor(this, anEvent);
    
    // If not interactively setting color (like from a mouse or slider drag), add color to RecentColorList
    if(!ViewUtils.isMouseDown())
        addRecentColor(getColor());
}

/**
 * Override to de-select color well on hide.
 */
protected void showingChanged()
{
    if(!getUI().isShowing())
        setColorWell(null);
}

/** Convert to/from hex string. */
private String toHexString(Color aColor)  { return '#' + aColor.toHexString(); }
private Color fromHexString(String aHS)  { Color c = Color.get(aHS); return c!=null? c : Color.BLACK; }

/**
 * Returns the buffered image from the list of images.
 */
private Image getImage(int anIndex)
{
    // If image at given index is still a String, convert to image
    if(_images[anIndex] instanceof String) {
        String iname = (String)_images[anIndex];
        Image img = Image.get(ColorPanel.class, iname);
        _images[anIndex] = img;
    }
    
    // Return image at index
    return (Image)_images[anIndex];
}

/**
 * An inner class to act as an image color picker.
 */
public static class ImagePicker extends View {
    
    // The image
    Image      _img;
    
    // The color
    Color  _color = Color.WHITE;
    
    /** Creates new ImagePicker. */
    public ImagePicker()
    {
        enableEvents(MousePress, MouseDrag, MouseRelease, Action);
        setCursor(Cursor.CROSSHAIR);
    }
    
    /** Returns the image. */
    public Image getImage()  { return _img; }
    
    /** Sets the image. */
    public void setImage(Image anImage)
    {
        _img = anImage; repaint();
        if(!_img.isLoaded())
            _img.addLoadListener(() -> relayoutParent());
    }
    
    /** Returns the color. */
    public Color getColor()  { return _color; }
    
    /** Paints the image. */
    protected void paintFront(Painter aPntr)
    {
        int x = (int)Math.round(getWidth() - _img.getWidth())/2; if(x<0) x = 0;
        int y = (int)Math.round(getHeight() - _img.getHeight())/2; if(y<0) y = 0;
        aPntr.setColor(Color.WHITE); aPntr.fillRect(0,0,getWidth(),getHeight());
        aPntr.drawImage(_img, x, y);
    }
    
    /** Handle events. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMouseEvent()) {
            int dx = (int)Math.round(getWidth() - _img.getWidth())/2; if(dx<0) dx = 0;
            int dy = (int)Math.round(getHeight() - _img.getHeight())/2; if(dy<0) dy = 0;
            int mx = (int)anEvent.getX(), my = (int)anEvent.getY();
            int x = mx - dx, y = my - dy;
            if(x>=0 && x<_img.getWidth() && y>=0 && y<_img.getHeight())
                _color = new Color(_img.getRGB(x,y));
            fireActionEvent(anEvent);
        }
    }
    
    /** Override to provide size. */
    protected double getPrefWidthImpl(double aH)  { return _img!=null? _img.getWidth() : 0; }
    protected double getPrefHeightImpl(double aW)  { return _img!=null? _img.getHeight() : 0; }
}

/**
 * A panel for picking colors from a set of swatches.
 */
private class SwatchPanel extends View {
    
    // Constants
    int WIDTH = 16, HEIGHT = 14, SIZE = 12;
    
    /** Creates new SwatchPanel. */
    public SwatchPanel()
    {
        enableEvents(MouseRelease, Action);
        setName("SwatchPanel"); setBounds(30,10,WIDTH*SIZE,HEIGHT*SIZE);
    }
    
    /** Returns the color. */
    public Color getColor()  { return _color; } Color _color = Color.BLACK;
    
    /** Paint component. */
    protected void paintFront(Painter aPntr)
    {
        for(int i=0,k=0;i<HEIGHT;i++) for(int j=0;j<WIDTH;j++,k++) {
            Color c = new Color(_webColors[k].substring(2));
            int x = j*SIZE, y = i*SIZE, w = SIZE, h = SIZE;
            aPntr.setColor(c); aPntr.fillRect(x,y,w,h);
            aPntr.setColor(Color.DARKGRAY); aPntr.drawRect(x+.5,y+.5,w-1,h-1);
        }
    }
    
    /** Handle Events. */
    public void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMouseEvent()) {
            int x = (int)anEvent.getX()/SIZE, y = (int)anEvent.getY()/SIZE;
            _color = new Color(_webColors[y*WIDTH+x].substring(2));
            fireActionEvent(anEvent);
        }
    }
}

/**
 * A list of hard-coded colors for the swatch panel.
 */
private static String _webColors[] = {
    "0x990033", "0xFF3366", "0xCC0033", "0xFF0033", "0xFF9999", "0xCC3366", "0xFFCCFF", "0xCC6699",
    "0x993366", "0x660033", "0xCC3399", "0xFF99CC", "0xFF66CC", "0xFF99FF", "0xFF6699", "0xCC0066",
    "0xFF0066", "0xFF3399", "0xFF0099", "0xFF33CC", "0xFF00CC", "0xFF66FF", "0xFF33FF", "0xFF00FF",
    "0xCC0099", "0x990066", "0xCC66CC", "0xCC33CC", "0xCC99FF", "0xCC66FF", "0xCC33FF", "0x993399",
    "0xCC00CC", "0xCC00FF", "0x9900CC", "0x990099", "0xCC99CC", "0x996699", "0x663366", "0x660099",
    "0x9933CC", "0x660066", "0x9900FF", "0x9933FF", "0x9966CC", "0x330033", "0x663399", "0x6633CC",
    "0x6600CC", "0x9966FF", "0x330066", "0x6600FF", "0x6633FF", "0xCCCCFF", "0x9999FF", "0x9999CC",
    "0x6666CC", "0x6666FF", "0x666699", "0x333366", "0x333399", "0x330099", "0x3300CC", "0x3300FF",
    "0x3333FF", "0x3333CC", "0x0066FF", "0x0033FF", "0x3366FF", "0x3366CC", "0x000066", "0x000033",
    "0x0000FF", "0x000099", "0x0033CC", "0x0000CC", "0x336699", "0x0066CC", "0x99CCFF", "0x6699FF",
    "0x003366", "0x6699CC", "0x006699", "0x3399CC", "0x0099CC", "0x66CCFF", "0x3399FF", "0x003399",
    "0x0099FF", "0x33CCFF", "0x00CCFF", "0x99FFFF", "0x66FFFF", "0x33FFFF", "0x00FFFF", "0x00CCCC",
    "0x009999", "0x669999", "0x99CCCC", "0xCCFFFF", "0x33CCCC", "0x66CCCC", "0x339999", "0x336666",
    "0x006666", "0x003333", "0x00FFCC", "0x33FFCC", "0x33CC99", "0x00CC99", "0x66FFCC", "0x99FFCC",
    "0x00FF99", "0x339966", "0x006633", "0x336633", "0x669966", "0x66CC66", "0x99FF99", "0x66FF66",
    "0x339933", "0x99CC99", "0x66FF99", "0x33FF99", "0x33CC66", "0x00CC66", "0x66CC99", "0x009966",
    "0x009933", "0x33FF66", "0x00FF66", "0xCCFFCC", "0xCCFF99", "0x99FF66", "0x99FF33", "0x00FF33",
    "0x33FF33", "0x00CC33", "0x33CC33", "0x66FF33", "0x00FF00", "0x66CC33", "0x006600", "0x003300",
    "0x009900", "0x33FF00", "0x66FF00", "0x99FF00", "0x66CC00", "0x00CC00", "0x33CC00", "0x339900",
    "0x99CC66", "0x669933", "0x99CC33", "0x336600", "0x669900", "0x99CC00", "0xCCFF66", "0xCCFF33",
    "0xCCFF00", "0x999900", "0xCCCC00", "0xCCCC33", "0x333300", "0x666600", "0x999933", "0xCCCC66",
    "0x666633", "0x999966", "0xCCCC99", "0xFFFFCC", "0xFFFF99", "0xFFFF66", "0xFFFF33", "0xFFFF00",
    "0xFFCC00", "0xFFCC66", "0xFFCC33", "0xCC9933", "0x996600", "0xCC9900", "0xFF9900", "0xCC6600",
    "0x993300", "0xCC6633", "0x663300", "0xFF9966", "0xFF6633", "0xFF9933", "0xFF6600", "0xCC3300",
    "0x996633", "0x330000", "0x663333", "0x996666", "0xCC9999", "0x993333", "0xCC6666", "0xFFCCCC",
    "0xFF3333", "0xCC3333", "0xFF6666", "0x660000", "0x990000", "0xCC0000", "0xFF0000", "0xFF3300",
    "0xCC9966", "0xFFCC99", "0xFFFFFF", "0xCCCCCC", "0x999999", "0x666666", "0x333333", "0x000000",
    "0x000000", "0x000000", "0x000000", "0x000000", "0x000000", "0x000000", "0x000000", "0x000000"
};

/**
 * A Slider subclass to show colors in slider groove.
 */
public static class GradSlider extends Slider {
    Color _c1 = Color.BLACK, _c2 = Color.WHITE; //Image _kimg = Image.get(ColorPanel.class, "thumb.png");
    
    public void setColors(Color start, Color end) { _c1 = start; _c2 = end; repaint(); }
    public void paintTrack(Painter g)
    {        
        Rect tbnds = getTrackBounds(); tbnds.inset(0,-1);
        RoundRect tbndsRnd = new RoundRect(tbnds.x, tbnds.y, tbnds.width, tbnds.height, 3);
        g.setPaint(new GradientPaint(1, 1, _c1, tbnds.width, 1, _c2)); g.fill(tbndsRnd);
        g.setColor(Color.GRAY); g.draw(tbndsRnd);
    }
}

/**
 * An inner JMenuItem subclass that draws a solid color and can highlight the selected item non-destructively.
 */
/*private static class ColorMenuItem extends JMenuItem {
    public ColorMenuItem(Color c) { super(); _color=c; }
    public Color getColor() { return _color; }  Color _color;
    public boolean isHighlighted() { // What nonsense.  There's got to be an easier way
        MenuElement path[] = MenuSelectionManager.defaultManager().getSelectedPath();
        return path!=null && path.length==2 && this.equals(path[1]); }
    protected void paintComponent(Graphics g) {
        Rectangle r = getBounds(); g.setColor(Color.white); g.fillRect(0,0,r.width,r.height);
        paintSwatch((Graphics2D)g, _color, 0, 0, r.width, r.height);
        if (isHighlighted()) {
            g.setColor(Color.white); g.drawRect(0,0,r.width-1,r.height-1);
            g.setColor(Color.black); g.drawRect(1,1,r.width-3,r.height-3);
        }
    }
}*/
    
}