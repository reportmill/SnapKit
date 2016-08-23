package snap.view;
import snap.gfx.*;

/**
 * A view subclass to display a text string in a single style.
 */
public class StringView extends View {
    
    // Text
    String         _text;
    
    // The text paint
    Paint          _textFill = Color.BLACK;
    
/**
 * Returns the text.
 */
public String getText()  { return _text; }

/**
 * Sets the text.
 */
public void setText(String aValue)  { _text = aValue; relayoutParent(); repaint(); }

/**
 * Returns the text fill.
 */
public Paint getTextFill()  { return _textFill; }

/**
 * Sets the text fill.
 */
public void setTextFill(Paint aPnt)  { _textFill = aPnt; repaint(); }

/**
 * Returns the default alignment.
 */    
public Pos getAlignmentDefault()  { return Pos.CENTER_LEFT; }

/**
 * Returns the preferred width.
 */
public double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll();
    return ins.left + (_text!=null? Math.ceil(getFont().getStringAdvance(_text)) : 0) + ins.right;
}

/**
 * Returns the preferred height.
 */
public double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    return ins.top + Math.ceil(getFont().getLineHeight()) + ins.bottom;
}

/**
 * Paints node.
 */
public void paintFront(Painter aPntr)
{
    // Clear rect
    if(_text==null) return;
    double width = getWidth(), height = getHeight();
    
    // Calcuate text x/y based on insets, font and alignment
    Insets ins = getInsetsAll(); Font font = getFont(); if(font==null) return;
    double x = ins.left, y = ins.top;
    double ax = ViewUtils.getAlignX(this), ay = ViewUtils.getAlignY(this);
    if(ax>0) { double extra = width - x - ins.right - font.getStringAdvance(_text); 
        x = Math.max(extra*ax,x); }
    if(ay>0) { double extra = height - y - ins.bottom - Math.ceil(font.getLineHeight());
        y = Math.max(Math.round(extra*ay),y); }
        
    // Set font/paint and draw string
    aPntr.setFont(font); aPntr.setPaint(_textFill);
    aPntr.drawString(_text, x, y+Math.ceil(font.getAscent()));
}

}