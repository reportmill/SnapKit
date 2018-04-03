package snap.view;
import snap.gfx.*;

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
  
/**
 * Creates a new ArrowView.
 */
public ArrowView()
{
    // Enable Action event
    enableEvents(Action);
    
    // Create/configure Col view to hold up/down buttons
    _col = new ColView();
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
    if(aValue==isShowUp()) return;
    
    // Add button
    if(aValue) {
        _ubtn = new Button(); _ubtn.setImage(getUpArrowImage());
        _ubtn.setRadius(3); _ubtn.setPosition(Pos.TOP_CENTER);
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
    if(aValue==isShowDown()) return;
    
    // Add button
    if(aValue) {
        _dbtn = new Button(); _dbtn.setImage(getDownArrowImage());
        _dbtn.setRadius(3); _dbtn.setPosition(Pos.BOTTOM_CENTER);
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
    if(aValue==isShowLeft()) return;
    
    // Add button
    if(aValue) {
        _lbtn = new Button(); _lbtn.setImage(getLeftArrowImage());
        _lbtn.setRadius(3); _lbtn.setPosition(Pos.CENTER_LEFT);
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
    if(aValue==isShowLeft()) return;
    
    // Add button
    if(aValue) {
        _rbtn = new Button(); _rbtn.setImage(getRightArrowImage());
        _rbtn.setRadius(3); _rbtn.setPosition(Pos.CENTER_RIGHT);
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
    if(_lastBtn==_ubtn) return Arrow.Up;
    if(_lastBtn==_dbtn) return Arrow.Down;
    if(_lastBtn==_lbtn) return Arrow.Left;
    if(_lastBtn==_rbtn) return Arrow.Right;
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
    fireActionEvent();
}

/**
 * Returns an image of a up arrow.
 */
public static Image getUpArrowImage()
{
    if(_uimg!=null) return _uimg;
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
    if(_dimg!=null) return _dimg;
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
    if(_limg!=null) return _limg;
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
    if(_rimg!=null) return _rimg;
    Image img = Image.get(9,7,true); Painter pntr = img.getPainter();
    Polygon poly = new Polygon(1.5, 1.5, 1.5, 7.5, 5.5, 4.5);
    pntr.setColor(Color.DARKGRAY); pntr.draw(poly); pntr.fill(poly);
    return _rimg = img;
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return RowView.getPrefWidth(this, null, 0, aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return RowView.getPrefHeight(this, null, aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { RowView.layout(this, null, null, false, 0); }

}