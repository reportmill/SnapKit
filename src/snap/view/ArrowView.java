package snap.view;
import snap.geom.Polygon;
import snap.geom.Pos;
import snap.gfx.*;
import snap.util.*;

/**
 * A view to show up to four arrows.
 */
public class ArrowView extends ParentView {
    
    // The ColView to hold up/down buttons
    ColView          _col;

    // The arrow buttons
    Button           _ubtn, _dbtn, _lbtn, _rbtn, _lastBtn;
    
    // Constants for buttons
    public enum Arrow { Up, Down, Left, Right }
    
    // The arrow images
    static Image     _uimg, _dimg, _limg, _rimg;
  
    // Constants for properties
    public static final String ShowUp_Prop = "ShowUp";
    public static final String ShowDown_Prop = "ShowDown";
    public static final String ShowLeft_Prop = "ShowLeft";
    public static final String ShowRight_Prop = "ShowRight";
    
    /**
     * Creates a new ArrowView.
     */
    public ArrowView()
    {
        // Enable Action event
        enableEvents(Action);

        // Create/configure Col view to hold up/down buttons
        _col = new ColView(); _col.setAlign(Pos.CENTER);
        addChild(_col);

        // Enable up/down buttons by default
        setShowUp(true); setShowDown(true);
    }

    /**
     * Returns whether to show up button.
     */
    public boolean isShowUp()  { return _ubtn!=null; }

    /**
     * Sets whether to show up button.
     */
    public void setShowUp(boolean aValue)
    {
        // If already set, just return
        if (aValue==isShowUp()) return;

        // Add button
        if (aValue) {
            _ubtn = new Button(); _ubtn.setPrefWidth(14); _ubtn.setMinHeight(9);
            _ubtn.setRadius(3); _ubtn.setPosition(Pos.TOP_CENTER); _ubtn.setImage(getUpArrowImage());
            _ubtn.addEventHandler(e -> buttonDidFire(_ubtn), Action);
            _col.addChild(_ubtn, 0);
        }

        // Remove button
        else { _col.removeChild(_ubtn); _ubtn = null; }
    }

    /**
     * Returns whether to show down button.
     */
    public boolean isShowDown()  { return _dbtn!=null; }

    /**
     * Sets whether to show down button.
     */
    public void setShowDown(boolean aValue)
    {
        // If already set, just return
        if (aValue==isShowDown()) return;

        // Add button
        if (aValue) {
            _dbtn = new Button(); _dbtn.setPrefWidth(14); _dbtn.setMinHeight(9);
            _dbtn.setRadius(3); _dbtn.setPosition(Pos.BOTTOM_CENTER); _dbtn.setImage(getDownArrowImage());
            _dbtn.addEventHandler(e -> buttonDidFire(_dbtn), Action);
            _col.addChild(_dbtn);
        }

        // Remove button
        else { _col.removeChild(_dbtn); _dbtn = null; }
    }

    /**
     * Returns whether to show left button.
     */
    public boolean isShowLeft()  { return _lbtn!=null; }

    /**
     * Sets whether to show left button.
     */
    public void setShowLeft(boolean aValue)
    {
        // If already set, just return
        if (aValue==isShowLeft()) return;

        // Add button
        if (aValue) {
            _lbtn = new Button(); _lbtn.setPrefHeight(14); _lbtn.setMinWidth(9);
            _lbtn.setRadius(3); _lbtn.setPosition(Pos.CENTER_LEFT); _lbtn.setImage(getLeftArrowImage());
            _lbtn.addEventHandler(e -> buttonDidFire(_lbtn), Action);
            addChild(_lbtn, 0);
        }

        // Remove button
        else { removeChild(_lbtn); _lbtn = null; }
    }

    /**
     * Returns whether to show right button.
     */
    public boolean isShowRight()  { return _rbtn!=null; }

    /**
     * Sets whether to show right button.
     */
    public void setShowRight(boolean aValue)
    {
        // If already set, just return
        if (aValue==isShowRight()) return;

        // Add button
        if (aValue) {
            _rbtn = new Button(); _rbtn.setPrefHeight(14); _rbtn.setMinWidth(9);
            _rbtn.setRadius(3); _rbtn.setPosition(Pos.CENTER_RIGHT); _rbtn.setImage(getRightArrowImage());
            _rbtn.addEventHandler(e -> buttonDidFire(_rbtn), Action);
            addChild(_rbtn);
        }

        // Remove button
        else { removeChild(_rbtn); _rbtn = null; }
    }

    /**
     * Returns the last arrow.
     */
    public Arrow getLastArrow()
    {
        if (_lastBtn==_ubtn) return Arrow.Up;
        if (_lastBtn==_dbtn) return Arrow.Down;
        if (_lastBtn==_lbtn) return Arrow.Left;
        if (_lastBtn==_rbtn) return Arrow.Right;
        return null;
    }

    /**
     * Returns whether last button was up.
     */
    public boolean isUp()  { return _lastBtn==_ubtn; }

    /**
     * Returns whether last button was down.
     */
    public boolean isDown()  { return _lastBtn==_dbtn; }

    /**
     * Returns whether last button was left.
     */
    public boolean isLeft()  { return _lastBtn==_lbtn; }

    /**
     * Returns whether last button was right.
     */
    public boolean isRight()  { return _lastBtn==_rbtn; }

    /**
     * Called when button fires.
     */
    protected void buttonDidFire(Button aBtn)
    {
        _lastBtn = aBtn;
        fireActionEvent(null);
    }

    /**
     * Returns an image of a up arrow.
     */
    public static Image getUpArrowImage()
    {
        if (_uimg!=null) return _uimg;
        Image img = Image.get(9,7,true); Painter pntr = img.getPainter();
        Polygon poly = new Polygon(1.5, 5.5, 7.5, 5.5, 4.5, 1.5);
        pntr.setColor(Color.DARKGRAY); pntr.draw(poly); pntr.fill(poly);
        return _uimg = img;
    }

    /**
     * Returns an image of a down arrow.
     */
    public static Image getDownArrowImage()
    {
        if (_dimg!=null) return _dimg;
        Image img = Image.get(9,7,true); Painter pntr = img.getPainter();
        Polygon poly = new Polygon(1.5, 1.5, 7.5, 1.5, 4.5, 5.5);
        pntr.setColor(Color.DARKGRAY); pntr.draw(poly); pntr.fill(poly);
        return _dimg = img;
    }

    /**
     * Returns an image of a left arrow.
     */
    public static Image getLeftArrowImage()
    {
        if (_limg!=null) return _limg;
        Image img = Image.get(7,9,true); Painter pntr = img.getPainter();
        Polygon poly = new Polygon(5.5, 1.5, 5.5, 7.5, 1.5, 4.5);
        pntr.setColor(Color.DARKGRAY); pntr.draw(poly); pntr.fill(poly);
        return _limg = img;
    }

    /**
     * Returns an image of a right arrow.
     */
    public static Image getRightArrowImage()
    {
        if (_rimg!=null) return _rimg;
        Image img = Image.get(7,9,true); Painter pntr = img.getPainter();
        Polygon poly = new Polygon(1.5, 1.5, 1.5, 7.5, 5.5, 4.5);
        pntr.setColor(Color.DARKGRAY); pntr.draw(poly); pntr.fill(poly);
        return _rimg = img;
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)  { return RowView.getPrefWidth(this, aH); }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)  { return RowView.getPrefHeight(this, aW); }

    /**
     * Layout children.
     */
    protected void layoutImpl()  { RowView.layout(this, false); }

    /**
     * Returns the default alignment.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER; }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ShowUp, ShowDown, ShowLeft, ShowRight
        if (!isShowUp()) e.add(ShowUp_Prop, isShowUp());
        if (!isShowDown()) e.add(ShowDown_Prop, isShowDown());
        if (isShowLeft()) e.add(ShowLeft_Prop, isShowLeft());
        if (isShowRight()) e.add(ShowRight_Prop, isShowRight());

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

        // Unarchive ShowUp, ShowDown, ShowLeft, ShowRight
        if (anElement.hasAttribute(ShowUp_Prop))
            setShowUp(anElement.getAttributeBoolValue(ShowUp_Prop));
        if (anElement.hasAttribute(ShowDown_Prop))
            setShowDown(anElement.getAttributeBoolValue(ShowDown_Prop));
        if (anElement.hasAttribute(ShowLeft_Prop))
            setShowLeft(anElement.getAttributeBoolValue(ShowLeft_Prop));
        if (anElement.hasAttribute(ShowRight_Prop))
            setShowRight(anElement.getAttributeBoolValue(ShowRight_Prop));
    }
}