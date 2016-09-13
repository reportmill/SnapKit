package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * An view subclass for editing a single line of text.
 */
public class TextField extends TextViewBase {
    
    // Whether the mouse is currently down
    boolean              _mouseDown;
    
    // The value of text on focus gained
    String               _focusGainedVal;
    
    // The column count to be used for preferred width (if set)
    int                  _colCount = 20;
    
    // A label in the background for promt text and/or in text controls
    Label                _label = new Label();
    
    // The string to show when textfield is empty
    String               _promptText;
    
    // Constants for properties
    public static final String ColumnCount_Prop = "ColumnCount";
    
    // The color of the border when focused
    static Color    FOCUSED_COLOR = Color.get("#039ed3");
    
/**
 * Creates a new TextField.
 */
public TextField()
{
    setEditable(true);
    setFill(Color.WHITE);
    enableEvents(Action);
    
    // Configure label and set
    _label.setPadding(0,0,0,0);
    addChild(_label);
}

/**
 * Returns the column count.
 */
public int getColumnCount()  { return _colCount; }

/**
 * Sets the column count.
 */
public void setColumnCount(int aValue)
{
    firePropChange(ColumnCount_Prop, _colCount, _colCount=aValue);
    relayoutParent();
}

/**
 * Returns the prompt text.
 */
public String getPromptText()  { return _promptText; }

/**
 * Sets the prompt text.
 */
public void setPromptText(String aStr)
{
    if(SnapUtils.equals(aStr, _promptText)) return;
    _label.setText(aStr); _label.setTextFill(Color.LIGHTGRAY);
    if(_promptText==null) _label.getStringView().addPropChangeListener(pce -> setTextBoxBounds(), X_Prop);
    firePropChange("PromptText", _promptText, _promptText = aStr);
}

/**
 * Returns the label in the background.
 */
public Label getLabel()  { return _label; }

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

/**
 * Returns the padding default.
 */
public Insets getDefaultPadding()  { return _def; } static Insets _def = new Insets(2,2,2,5);

/**
 * Override to return white.
 */
public Paint getDefaultFill()  { return Color.WHITE; }

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll(); int ccount = getColumnCount();
    double pw = super.getPrefWidthImpl(aH) - ins.left - ins.right + 10;
    if(ccount>0) pw = ccount*getFont().charAdvance('X');
    pw = Math.max(pw, _label.getPrefWidth());
    return ins.left + pw + ins.right;
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    double ph = super.getPrefHeightImpl(aW) - ins.top - ins.bottom;
    ph = Math.max(ph, _label.getPrefHeight());
    return ins.top + ph + ins.bottom + 6;
}

/**
 * Layout children.
 */
protected void layoutChildren()
{
    checkFont();
    Insets ins = getInsetsAll();
    double x = ins.left, y = ins.top, w = getWidth() - x - ins.right, h = getHeight() - y - ins.bottom;
    _label.setBounds(x, y, w, h);
}

/**
 * Paint component.
 */
protected void paintBack(Painter aPntr)
{
    double w = getWidth(), h = getHeight(); aPntr.clearRect(0,0,w,h);
    RoundRect rrect = new RoundRect(.5,.5,w-1,h-1,3);
    aPntr.setPaint(getFill()); aPntr.fill(rrect);
    aPntr.setColor(isFocused()? FOCUSED_COLOR : Color.LIGHTGRAY);
    aPntr.setStroke(Stroke.Stroke1); aPntr.draw(rrect);
}

/**
 * Override to select all on FocusGained (except when mouse-clicked).
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMousePressed()) _mouseDown = true;
    super.processEvent(anEvent);
    if(anEvent.isMouseReleased()) _mouseDown = false;
}

/**
 * Override to track FocusGainedValue.
 */
protected void setFocused(boolean aValue)
{
    if(aValue==isFocused()) return; super.setFocused(aValue);
    
    // If focus gained, set FocusedGainedValue and select all (if not from mouse press)
    if(aValue) {
        _focusGainedVal = getText();
        if(!_mouseDown) selectAll();
    }
    
    // If focus lost and FocusGainedVal changed, fire action
    else if(!SnapUtils.equals(_focusGainedVal, getText()))
        fireActionEvent();
}

/**
 * Sets the Text.Rect from text area.
 */
protected Rect getTextBoxBounds()
{
    Insets ins = getInsetsAll(); boolean wrap = isWrapText();
    double promptx = _promptText!=null? _label.getStringView().getX() : 0;
    double x = ins.left + promptx, w = getWidth() - x - ins.right;
    double y = ins.top, h = getHeight() - y - ins.bottom;
    return new Rect(x, y, w, h);
}

/**
 * Override to update Prompt label.
 */
protected void textDidChange()
{
    super.textDidChange();
    if(_promptText!=null) _label.getStringView().setVisible(length()==0);
}

/**
 * Override to reset FocusedGainedVal.
 */
public void fireActionEvent()
{
    super.fireActionEvent();
    _focusGainedVal = getText();
}

/**
 * Override to only paint selection when focused.
 */
protected void paintSel(Painter aPntr)  { if(isFocused()) super.paintSel(aPntr); }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive text component attributes
    XMLElement e = super.toXMLView(anArchiver);
    if(getColumnCount()!=20) e.add(ColumnCount_Prop, getColumnCount());
    return e; // Return element
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXMLView(anArchiver, anElement);
    if(anElement.hasAttribute(ColumnCount_Prop)) setColumnCount(anElement.getAttributeIntValue(ColumnCount_Prop));
}

/**
 * Sets the given TextField to animate background label alignment from center to left when focused.
 */
public static void setBackLabelAlignAnimatedOnFocused(TextField aTextField, boolean aValue)
{
    aTextField.getLabel().setAlign(Pos.CENTER);
    aTextField.addPropChangeListener(pce -> {
        if(aTextField.isFocused()) ViewAnim.setAlign(aTextField.getLabel(), Pos.CENTER_LEFT, 200);
        else ViewAnim.setAlign(aTextField.getLabel(), Pos.CENTER, 600);
    }, View.Focused_Prop);
}

}