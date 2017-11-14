/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;
import snap.web.*;

/**
 * A view subclass for displaying and editing large blocks of text and rich text.
 */
public class TextView extends ParentView {

    // The basic text view
    TextArea               _textArea;
    
    // The ScrollView for the TextArea
    ScrollView             _scroll;
    
    // Whether to send action on return
    boolean               _fireActionOnReturn;

    // Whether to send action on focus lost (if content changed)
    boolean               _fireActionOnFocusLost;
    
    // The content on focus gained
    String                _focusGainedText;
    
    // Constants for properties
    public static final String Editable_Prop = "Editable";
    public static final String WrapText_Prop = "WrapText";
    public static final String FireActionOnReturn_Prop = "FireActionOnReturn";
    public static final String FireActionOnFocusLost_Prop = "FireActionOnFocusLost";
    public static final String Selection_Prop = "Selection";

/**
 * Creates a new TextView.
 */
public TextView()
{
    // Create/configure TextArea
    _textArea = createTextArea();
    
    // Create/add ScrollView
    _scroll = new ScrollView(_textArea);
    addChild(_scroll);
    
    // Make Editable and White by default
    setEditable(true);
    _textArea.setFill(Color.WHITE);
}

/**
 * Returns the TextArea.
 */
public TextArea getTextArea()  { return _textArea; }

/**
 * Creates the TextArea.
 */
protected TextArea createTextArea()  { return new TextArea(); }

/**
 * Returns the ScrollView.
 */
public ScrollView getScrollView()  { return _scroll; }

/**
 * Returns the rich text.
 */
public RichText getRichText()  { return _textArea.getRichText(); }

/**
 * Returns the text that is being edited.
 */
public TextBox getTextBox()  { return _textArea.getTextBox(); }

/**
 * Returns the source of current content (URL, File, String path, etc.)
 */
public Object getSource()  { return getTextBox().getSource(); }

/**
 * Sets the source of current content (URL, File, String path, etc.)
 */
public void setSource(Object aSource)  { _textArea.setSource(aSource); }

/**
 * Returns the source URL.
 */
public WebURL getSourceURL()  { return _textArea.getSourceURL(); }

/**
 * Returns the source file.
 */
public WebFile getSourceFile()  { return _textArea.getSourceFile(); }

/**
 * Returns the plain string of the text being edited.
 */
public String getText()  { return _textArea.getText(); }

/**
 * Set text string of text editor.
 */
public void setText(String aString)  { _textArea.setText(aString); }

/**
 * Returns whether Text shape is editable.
 */
public boolean isEditable()  { return _textArea.isEditable(); }

/**
 * Sets whether Text shape is editable.
 */
public void setEditable(boolean aValue)  { _textArea.setEditable(aValue); }

/**
 * Returns whether text wraps.
 */
public boolean isWrapText()  { return _textArea.isWrapText(); }

/**
 * Sets whether text wraps.
 */
public void setWrapText(boolean aValue)  { _textArea.setWrapText(aValue); }

/**
 * Returns whether text supports multiple styles.
 */
public boolean isRich()  { return !isPlainText(); }

/**
 * Sets whether text supports multiple styles.
 */
public void setRich(boolean aValue)  { setPlainText(!aValue); }

/**
 * Returns whether text is plain text (has only one font, color. etc.).
 */
public boolean isPlainText()  { return _textArea.isPlainText(); }

/**
 * Sets whether text is plain text (has only one font, color. etc.).
 */
public void setPlainText(boolean aValue)  { _textArea.setPlainText(aValue); }

/**
 * Returns the default style for text.
 */
public TextStyle getDefaultStyle()  { return _textArea.getDefaultStyle(); }

/**
 * Sets the default style.
 */
public void setDefaultStyle(TextStyle aStyle)  { _textArea.setDefaultStyle(aStyle); }

/**
 * Returns the default line style for text.
 */
public TextLineStyle getDefaultLineStyle()  { return _textArea.getDefaultLineStyle(); }

/**
 * Sets the default line style.
 */
public void setDefaultLineStyle(TextLineStyle aLineStyle)  { _textArea.setDefaultLineStyle(aLineStyle); }

/**
 * Returns whether text view fires action on return.
 */
public boolean isFireActionOnReturn()  { return _textArea.isFireActionOnReturn(); }

/**
 * Sets whether text area sends action on return.
 */
public void setFireActionOnReturn(boolean aValue)  { _textArea.setFireActionOnReturn(aValue); }

/**
 * Returns whether text view fires action on focus lost (if text changed).
 */
public boolean isFireActionOnFocusLost()  { return _textArea.isFireActionOnFocusLost(); }

/**
 * Sets whether text area sends action on focus lost (if text changed).
 */
public void setFireActionOnFocusLost(boolean aValue)  { _textArea.setFireActionOnFocusLost(aValue); }

/**
 * Returns the number of characters in the text string.
 */
public int length()  { return _textArea.length(); }

/**
 * Returns the individual character at given index.
 */
public char charAt(int anIndex)  { return _textArea.charAt(anIndex); }

/**
 * Returns whether the selection is empty.
 */
public boolean isSelEmpty()  { return _textArea.isSelEmpty(); }

/**
 * Returns the initial character index of the selection (usually SelStart).
 */
public int getSelAnchor()  { return _textArea.getSelAnchor(); }

/**
 * Returns the final character index of the selection (usually SelEnd).
 */
public int getSelIndex()  { return _textArea.getSelIndex(); }

/**
 * Returns the character index of the start of the text selection.
 */
public int getSelStart()  { return _textArea.getSelStart(); }

/**
 * Returns the character index of the end of the text selection.
 */
public int getSelEnd()  { return _textArea.getSelEnd(); }

/**
 * Returns the text selection.
 */
public TextSel getSel()  { return _textArea.getSel(); }

/**
 * Sets the character index of the text cursor.
 */
public void setSel(int newStartEnd)  { setSel(newStartEnd, newStartEnd); }

/**
 * Sets the character index of the start and end of the text selection.
 */
public void setSel(int aStart, int aEnd)  { _textArea.setSel(aStart, aEnd); }

/**
 * Selects all the characters in the text editor.
 */
public void selectAll()  { _textArea.selectAll(); }

/**
 * Returns the font of the current selection or cursor.
 */
public Font getFont()  { return _textArea.getFont(); }

/**
 * Sets the font of the current selection or cursor.
 */
public void setFont(Font aFont)  { _textArea.setFont(aFont); }

/**
 * Returns the color of the current selection or cursor.
 */
public Paint getTextFill()  { return _textArea.getTextFill(); }

/**
 * Sets the color of the current selection or cursor.
 */
public void setTextFill(Paint aColor)  { _textArea.setTextFill(aColor); }

/**
 * Returns whether TextView is underlined.
 */
public boolean isUnderlined()  { return _textArea.isUnderlined(); }

/**
 * Sets whether TextView is underlined.
 */
public void setUnderlined(boolean aValue)  { _textArea.setUnderlined(aValue); }

/**
 * Returns the text line alignment.
 */
public HPos getLineAlign()  { return _textArea.getLineAlign(); }

/**
 * Sets the text line alignment.
 */
public void setLineAlign(HPos anAlign)  { _textArea.setLineAlign(anAlign); }

/**
 * Returns whether the text line justifies text.
 */
public boolean isLineJustify()  { return _textArea.isLineJustify(); }

/**
 * Sets whether the text line justifies text.
 */
public void setLineJustify(boolean aValue)  { _textArea.setLineJustify(aValue); }

/**
 * Returns the style at given char index.
 */
public TextStyle getStyleAt(int anIndex)  { return _textArea.getStyleAt(anIndex); }

/**
 * Returns the TextStyle for the current selection and/or input characters.
 */
public TextStyle getSelStyle()  { return _textArea.getSelStyle(); }

/**
 * Sets the attributes that are applied to current selection or newly typed chars.
 */
public void setSelStyleValue(String aKey, Object aValue)  { _textArea.setSelStyleValue(aKey, aValue); }

/**
 * Returns the TextLineStyle for currently selection.
 */
public TextLineStyle getLineStyle()  { return _textArea.getLineStyle(); }

/**
 * Sets the line attributes that are applied to current selection or newly typed chars.
 */
public void setLineStyleValue(String aKey, Object aValue)  { _textArea.setLineStyleValue(aKey, aValue); }

/**
 * Adds the given string to end of text.
 */
public void addChars(String aStr, Object ... theAttrs)  { _textArea.addChars(aStr, theAttrs); }

/**
 * Adds the given string with given style to text at given index.
 */
public void addChars(String aStr, TextStyle aStyle)  { _textArea.addChars(aStr, aStyle); }

/**
 * Adds the given string with given style to text at given index.
 */
public void addChars(String aStr, TextStyle aStyle, int anIndex) { _textArea.addChars(aStr, aStyle, anIndex, anIndex); }

/**
 * Deletes the current selection.
 */
public void delete()  { _textArea.delete(); }

/**
 * Deletes the given range of chars.
 */
public void delete(int aStart, int anEnd, boolean doUpdateSel) { _textArea.delete(aStart, anEnd, doUpdateSel); }

/**
 * Replaces the current selection with the given string.
 */
public void replaceChars(String aString)  { replaceChars(aString, null, getSelStart(), getSelEnd(), true);}

/**
 * Replaces the current selection with the given string.
 */
public void replaceChars(String aString, TextStyle aStyle, int aStart, int anEnd, boolean doUpdateSel)
{
    _textArea.replaceChars(aString, aStyle, aStart, anEnd, doUpdateSel);
}

/**
 * Clears the text.
 */
public void clear()  { _textArea.clear(); }

/**
 * Returns the number of lines.
 */
public int getLineCount()  { return _textArea.getLineCount(); }

/**
 * Returns the individual line at given index.
 */
public TextBoxLine getLine(int anIndex)  { return _textArea.getLine(anIndex); }

/**
 * Returns the last line.
 */
public TextBoxLine getLineLast()  { return _textArea.getLineLast(); }

/**
 * Returns the line for the given character index.
 */
public TextBoxLine getLineAt(int anIndex)  { return _textArea.getLineAt(anIndex); }

/**
 * Returns the token for given character index.
 */
public TextBoxToken getTokenAt(int anIndex)  { return _textArea.getTokenAt(anIndex); }

/**
 * Returns the char index for given point in text coordinate space.
 */
public int getCharIndex(double anX, double aY)  { return _textArea.getCharIndex(anX, aY); }

/**
 * Returns the selection color.
 */
public Color getSelColor()  { return _textArea.getSelColor(); }

/**
 * Returns whether ProgressBar is animating.
 */
public boolean isCaretAnim()  { return _textArea.isCaretAnim(); }

/**
 * Sets anim.
 */
public void setCaretAnim(boolean aValue)  { _textArea.setCaretAnim(aValue); }

/**
 * Returns the font scale of the text box.
 */
public double getFontScale()  { return _textArea.getFontScale(); }

/**
 * Sets the font scale of the text box.
 */
public void setFontScale(double aValue)  { _textArea.setFontScale(aValue); }

/**
 * Scales font sizes of all text in TextBox to fit in bounds by finding/setting FontScale.
 */
public void scaleTextToFit()  { _textArea.scaleTextToFit(); }

/**
 * Opens a given link.
 */
protected void openLink(String aLink)  { System.out.println("Open Link: " + aLink); }

/**
 * Returns the undoer.
 */
public Undoer getUndoer()  { return _textArea.getUndoer(); }

/**
 * Called to undo the last edit operation in the editor.
 */
public boolean undo()  { return _textArea.undo(); }

/**
 * Called to redo the last undo operation in the editor.
 */
public boolean redo()  { return _textArea.redo(); }

/**
 * Returns the width needed to display all characters.
 */
protected double getPrefWidthImpl(double aH)  { return BoxView.getPrefWidth(this, _scroll, aH); }

/**
 * Returns the height needed to display all characters.
 */
protected double getPrefHeightImpl(double aW)  { return BoxView.getPrefHeight(this, _scroll, aW); }

/**
 * Actual method to layout children.
 */
protected void layoutImpl()  { BoxView.layout(this, _scroll, null, true, true); }

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return "Text"; }

/**
 * Standard toString implementation.
 */
public String toString()
{
    String str = getText(); if(str.length()>40) str = str.substring(0,40) + "...";
    return getClass().getSimpleName() + ": " + str;
}

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Rich, Editable, WrapText
    if(isRich()) e.add("Rich", true);
    if(!isEditable()) e.add("Editable", false);
    if(isWrapText()) e.add("WrapText", true);

    // If RichText, archive rich text
    if(isRich()) {
        e.removeElement("font");
        XMLElement rtxml = anArchiver.toXML(getRichText()); rtxml.setName("RichText");
        if(rtxml.size()>0) e.add(rtxml); //for(int i=0, iMax=rtxml.size(); i<iMax; i++) e.add(rtxml.get(i));
    }

    // Otherwise, archive text string
    else if(getText()!=null && getText().length()>0) e.add("text", getText());
    
    // Archive FireActionOnReturn, FireActionOnFocusLost
    if(isFireActionOnReturn()) e.add(FireActionOnReturn_Prop, true);
    if(isFireActionOnFocusLost()) e.add(FireActionOnFocusLost_Prop, true);
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    
    // Hack for archived rich stuff
    XMLElement rtxml = anElement.get("RichText");
    if(rtxml==null && anElement.get("string")!=null) rtxml = anElement;
    if(rtxml!=null) setPlainText(false);
    
    // Unarchive Rich, Editable, WrapText
    if(anElement.hasAttribute("Rich")) setPlainText(!anElement.getAttributeBoolValue("Rich"));
    if(anElement.hasAttribute("Editable")) setEditable(anElement.getAttributeBoolValue("Editable"));
    if(anElement.hasAttribute("WrapText")) setWrapText(anElement.getAttributeBoolValue("WrapText"));

    // Unarchive margin, valign (should go soon)
    if(anElement.hasAttribute("margin")) setPadding(Insets.get(anElement.getAttributeValue("margin")));
    if(anElement.hasAttribute("valign")) {
        String align = anElement.getAttributeValue("valign");
        if(align.equals("top")) setAlign(Pos.get(HPos.LEFT,VPos.TOP));
        else if(align.equals("middle")) setAlign(Pos.get(HPos.LEFT,VPos.CENTER));
        else if(align.equals("bottom")) setAlign(Pos.get(HPos.LEFT,VPos.BOTTOM));
    }
    
    // If Rich, unarchive rich text
    if(isRich()) {
        getUndoer().disable();
        if(rtxml!=null) getRichText().fromXML(anArchiver, rtxml);
        getUndoer().enable();
    }

    // Otherwise unarchive text. Text can be "text" or "value" attribute, or as content (CDATA or otherwise)
    else {
        String str = anElement.getAttributeValue("text",  anElement.getAttributeValue("value", anElement.getValue()));
        if(str!=null && str.length()>0)
            setText(str);
    }
    
    // Unarchive FireActionOnReturn, FireActionOnFocusLost
    if(anElement.hasAttribute(FireActionOnReturn_Prop))
        setFireActionOnReturn(anElement.getAttributeBoolValue(FireActionOnReturn_Prop));
    if(anElement.hasAttribute(FireActionOnFocusLost_Prop))
        setFireActionOnFocusLost(anElement.getAttributeBoolValue(FireActionOnFocusLost_Prop));
}

}