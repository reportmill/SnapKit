/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.Path;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

import java.util.Objects;

/**
 * A View to show/set a simple color selection.
 */
public class ColorButton extends View {

    // The color
    private Color  _color = Color.BLACK;
    
    // The title
    private String  _title;
    
    // The color image
    private Image  _image;
    
    // Whether button is armed
    private boolean  _armed;
    
    // Whether button is under mouse
    private boolean  _targeted;
    
    // Whether button is being tracked by mouse
    private boolean  _tracked;
    
    // The Popup
    private PopupWindow  _popup;
    
    // The down arrow image
    private static Image   _arrowImg;

    // Constants for properties
    public static final String Color_Prop = "Color";

    /**
     * Creates a new ColorButton.
     */
    public ColorButton()
    {
        enableEvents(MouseEvents);
        enableEvents(Action);
        setPrefSize(32,22);
    }

    /**
     * Returns the color.
     */
    public Color getColor()  { return _color; }

    /**
     * Sets the color.
     */
    public void setColor(Color aColor)
    {
        // If already set, just return
        if (Objects.equals(aColor, _color)) return;

        // Set, fire prop change, repaint
        firePropChange(Color_Prop, _color, _color = aColor);
        repaint();

        // If Title set, save to prefs under that name
        if (getTitle() != null) {
            Prefs.get().setValue(_title, _color == null ? null : _color.toHexString());
        }
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
        String cstr = Prefs.get().getString(_title);
        if (cstr != null)
            _color = Color.get(cstr);
        repaint();
    }

    /**
     * Returns the image.
     */
    public Image getImage()
    {
        // If already set, just return
        if (_image != null) return _image;

        // Handle standard stroke color image
        if (_title.startsWith("Stroke"))
            return _image = Image.get(getClass(), "pkg.images/Color_StrokeColor.png");

        // Handle standard text color image
        if (_title.startsWith("Text"))
            return _image = Image.get(getClass(), "pkg.images/Color_TextColor.png");

        // Return default color button image
        return _image = Image.get(getClass(), "pkg.images/Color_FillColor.png");
    }

    /**
     * Sets the image.
     */
    public void setImage(Image anImage)
    {
        _image = anImage;
        repaint();
    }

    /**
     * Paints the button.
     */
    protected void paintFront(Painter aPntr)
    {
        // Paint background (clear if normal, grey if armed)
        double areaW = getWidth() - 1;
        double areaH = getHeight() - 1;
        if (_armed) {
            aPntr.setPaint(Color.LIGHTGRAY);
            aPntr.fillRect(0, 0, areaW, areaH);
        }

        // Paint border if targeted (under mouse)
        if (_targeted) {
            aPntr.setColor(Color.BLACK);
            aPntr.drawRect(.5,.5,areaW - 1,areaH - 1);
            aPntr.drawLine(areaW - 10,0,areaW - 10, areaH);
        }

        // Paint base icon
        aPntr.drawImage(getImage(), 2, 2);

        // Paint arrow
        aPntr.drawImage(getDownArrowImage(), 21, 9);

        // If color set, paint color swatch
        if (_color != null) {
            aPntr.setColor(_color);
            aPntr.fillRect(3, 15, 14, 4);
            aPntr.setColor(_color.darker());
            aPntr.drawRect(3, 15, 14, 4);
        }

        // Otherwise paint null color
        else {
            aPntr.setColor(Color.DARKGRAY);
            aPntr.drawRect(3, 15, 14, 4);
        }
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseEnter
        if (anEvent.isMouseEnter()) {
            _targeted = true;
            _armed = _tracked;
            repaint();
        }

        // Handle MouseExit
        else if (anEvent.isMouseExit())  {
            _targeted = _armed = false;
            repaint();
        }

        // Handle MousePress
        else if (anEvent.isMousePress())  {
            _tracked = true;
            _armed = true;
            repaint();
        }

        // Handle MouseRelease
        else if (anEvent.isMouseRelease())  {
            if (_armed)
                fire(anEvent);
            _armed = _tracked = false;
            repaint();
        }
    }

    /**
     * Handles button click.
     */
    public void fire(ViewEvent anEvent)
    {
        // If mouse click in button part, fireActionEvent
        if (anEvent == null || anEvent.getX() < getWidth() - 10)
            fireActionEvent(anEvent);

        // Otherwise (in menu part), show popup
        else {
            PopupWindow popup = getPopup();
            popup.show(this,0, getHeight());
        }
    }

    /**
     * Returns the popup.
     */
    public PopupWindow getPopup()
    {
        // If already created, just return
        if (_popup != null) return _popup;

        // Create/configure titleLabel
        Label titleLabel = new Label(getTitle());
        titleLabel.setPadding(2, 2, 2, 20);

        // Create/configure "None" Menu
        MenuItem noneMenu = new MenuItem();
        noneMenu.setName("NoneMenu");
        noneMenu.setText("None");
        noneMenu.addEventHandler(e -> handlePopupMenuEvent(e), Action);

        // Create/configure "More..." menu
        MenuItem moreMenu = new MenuItem();
        moreMenu.setName("MoreMenu");
        moreMenu.setText("More...");
        moreMenu.addEventHandler(e -> handlePopupMenuEvent(e), Action);

        // Create/configure main content view (ColView)
        ColView content = new ColView();
        content.setFillWidth(true);
        content.setPadding(2,2,2,2);
        content.addChild(titleLabel);
        content.addChild(new Separator());
        content.addChild(new ColorBoxesPane());
        content.addChild(new Separator());
        content.addChild(noneMenu);
        content.addChild(moreMenu);

        // Create PopupWindow and content
        PopupWindow popup = new PopupWindow();
        popup.setContent(content);

        // Set/return
        return _popup = popup;
    }

    /**
     * Handles popup menu item events.
     */
    protected void handlePopupMenuEvent(ViewEvent anEvent)
    {
        // Handle None: Set null color and fireActionEvent for ColorButton
        if (anEvent.equals("NoneMenu")) {
            setColor(null);
            fireActionEvent(anEvent);
        }

        // Handle MoreMenu: Make ColorPanel visible
        if (anEvent.equals("MoreMenu"))
            ColorPanel.getShared().setWindowVisible(true);

        // Hide Popup
        getPopup().hide();
    }

    /**
     * Returns an Icon of a down arrow.
     */
    private Image getDownArrowImage()
    {
        // If already set, just return
        if (_arrowImg != null) return _arrowImg;

        // Create DownArrow image
        Image img = Image.get(11,10,true);
        Painter pntr = img.getPainter();
        Path p = new Path(); p.moveTo(2.5f, 1f);
        p.lineTo(5.5f, 6f); p.lineTo(8.5f, 1f); p.close();
        pntr.setColor(Color.BLACK);
        pntr.fill(p);
        return _arrowImg = img;
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
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = super.toXML(anArchiver);
        if (getTitle() != null && getTitle().length() > 0)
            e.add("Title", getTitle());
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXML(anArchiver, anElement);
        if (anElement.hasAttribute("Title"))
            setTitle(anElement.getAttributeValue("Title"));
        return this;
    }

    /**
     * ColorBoxesPane.
     */
    public class ColorBoxesPane extends View {

        // Mouse x/y
        int  _mx = -1, _my = -1;

        // Armed x/y
        int _armx = -1, _army = -1;

        /** Creates new ColorBoxesPane. */
        public ColorBoxesPane()
        {
            setPrefSize(8*20,5*20);
            enableEvents(MouseEvents);
        }

        /** Paint ColorBoxesPane. */
        protected void paintFront(Painter aPntr)
        {
            int mx = _mx, my = _my;
            int armx = _armx, army = _army;
            if (armx >= 0 && (armx != mx || army != my)) {
                mx = my = armx = army = -1;
            }

            for (int i=0; i<8; i++) {
                for (int j = 0; j < 5; j++) {
                    if (i == armx && j == army) {
                        aPntr.setColor(Color.GRAY);
                        aPntr.fillRect(i * 20 + 1, j * 20 + 1, 18, 18);
                    }

                    aPntr.setColor(_colors[i + j * 8]);
                    aPntr.fillRect(i * 20 + 4, j * 20 + 4, 12, 12);
                    aPntr.setColor(Color.BLACK);
                    aPntr.drawRect(i * 20 + 4.5, j * 20 + 4.5, 11, 11);
                    if (i == mx && j == my)
                        aPntr.drawRect(i * 20 + 1.5, j * 20 + 1.5, 17, 17);
                }
            }
        }

        /** Handle Events. */
        protected void processEvent(ViewEvent anEvent)
        {
            if (anEvent.isMouseMove()) {
                _mx = (int) anEvent.getX()/20;
                _my = (int) anEvent.getY()/20;
                repaint();
            }

            if (anEvent.isMouseExit()) {
                _mx = _my = -1; repaint();
            }

            if (anEvent.isMousePress()) {
                _armx = (int) anEvent.getX()/20;
                _army = (int) anEvent.getY()/20;
                repaint();
            }
            if (anEvent.isMouseDrag()) {
                _mx = (int) anEvent.getX()/20;
                _my = (int) anEvent.getY()/20;
                repaint();
            }

            if (anEvent.isMouseRelease()) {
                int mx = (int) anEvent.getX()/20;
                int my = (int) anEvent.getY()/20;
                if (_armx == mx && _army == my) {
                    setColor(_colors[_armx+_army*8]);
                    fire(null);
                    getPopup().hide();
                }
                _mx = _my = _armx = _army = -1;
                repaint();
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