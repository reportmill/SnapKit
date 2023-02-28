/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.props.Undoer;
import snap.text.*;
import snap.util.*;

/**
 * This view subclass wraps a TextArea in a ScrollView.
 */
public class TextView extends ParentView {

    // The TextArea
    private TextArea  _textArea;
    
    // The ScrollView for the TextArea
    private ScrollView  _scrollView;

    // Listener to propagate Action from TextArea to TextView
    private EventListener  _actionEvtLsnr = e -> fireActionEvent(e);

    // Constants for properties
    public static final String WrapLines_Prop = TextArea.WrapLines_Prop;
    public static final String FireActionOnEnterKey_Prop = TextArea.FireActionOnEnterKey_Prop;
    public static final String FireActionOnFocusLost_Prop = TextArea.FireActionOnFocusLost_Prop;
    public static final String Selection_Prop = TextArea.Selection_Prop;

    /**
     * Constructor.
     */
    public TextView()
    {
        // Create/configure TextArea
        _textArea = createTextArea();

        // Create/add ScrollView
        _scrollView = new ScrollView(_textArea);
        addChild(_scrollView);

        // Other configuration
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
    public ScrollView getScrollView()  { return _scrollView; }

    /**
     * Returns the TextDoc.
     */
    public TextDoc getTextDoc()  { return _textArea.getTextDoc(); }

    /**
     * Returns the text that is being edited.
     */
    public TextBox getTextBox()  { return _textArea.getTextBox(); }

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
     * Returns whether to wrap lines that overrun bounds.
     */
    public boolean isWrapLines()  { return _textArea.isWrapLines(); }

    /**
     * Sets whether to wrap lines that overrun bounds.
     */
    public void setWrapLines(boolean aValue)
    {
        _textArea.setWrapLines(aValue);
        _scrollView.setFillWidth(aValue);
    }

    /**
     * Returns whether text supports multiple styles.
     */
    public boolean isRichText()  { return _textArea.isRichText(); }

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
    public boolean isFireActionOnEnterKey()  { return _textArea.isFireActionOnEnterKey(); }

    /**
     * Sets whether text area sends action on return.
     */
    public void setFireActionOnEnterKey(boolean aValue)
    {
        _textArea.setFireActionOnEnterKey(aValue);
        setActionable(isFireActionOnEnterKey() || isFireActionOnFocusLost());

        // Enable
        if (aValue)
            _textArea.addEventHandler(_actionEvtLsnr, Action);
        else _textArea.removeEventHandler(_actionEvtLsnr);
    }

    /**
     * Returns whether text view fires action on focus lost (if text changed).
     */
    public boolean isFireActionOnFocusLost()  { return _textArea.isFireActionOnFocusLost(); }

    /**
     * Sets whether text area sends action on focus lost (if text changed).
     */
    public void setFireActionOnFocusLost(boolean aValue)
    {
        _textArea.setFireActionOnFocusLost(aValue);
        setActionable(isFireActionOnEnterKey() || isFireActionOnFocusLost());

        // Enable
        if (aValue)
            _textArea.addEventHandler(_actionEvtLsnr, Action);
        else _textArea.removeEventHandler(_actionEvtLsnr);
    }

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
    protected double getPrefWidthImpl(double aH)
    {
        return BoxView.getPrefWidth(this, _scrollView, aH);
    }

    /**
     * Returns the height needed to display all characters.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return BoxView.getPrefHeight(this, _scrollView, aW);
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        BoxView.layout(this, _scrollView, true, true);
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return "Text"; }

    /**
     * Override to sync ScrollView and TextArea.
     */
    @Override
    public void setBorderRadius(double aValue)
    {
        super.setBorderRadius(aValue);
        _scrollView.setBorderRadius(aValue);
        _textArea.setBorderRadius(aValue);
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive Rich, Editable, WrapLines
        if (getTextArea().isRichText()) e.add("Rich", true);
        if (!isEditable()) e.add("Editable", false);
        if (isWrapLines()) e.add(WrapLines_Prop, true);

        // If RichText, archive rich text
        if (getTextArea().isRichText()) {
            e.removeElement("font");
            XMLElement richTextXML = anArchiver.toXML(getTextDoc());
            richTextXML.setName("RichText");
            if (richTextXML.size() > 0)
                e.add(richTextXML);
        }

        // Otherwise, archive text string
        else if (getText() != null && getText().length() > 0)
            e.add("text", getText());

        // Archive FireActionOnEnterKey, FireActionOnFocusLost
        if (isFireActionOnEnterKey()) e.add(FireActionOnEnterKey_Prop, true);
        if (isFireActionOnFocusLost()) e.add(FireActionOnFocusLost_Prop, true);
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive Rich, Editable, WrapLines
        if (anElement.hasAttribute("Rich"))
            getTextBox().setRichText(anElement.getAttributeBoolValue("Rich"));
        if (anElement.hasAttribute("Editable"))
            setEditable(anElement.getAttributeBoolValue("Editable"));
        if (anElement.hasAttribute(WrapLines_Prop))
            setWrapLines(anElement.getAttributeBoolValue(WrapLines_Prop));
        if (anElement.hasAttribute("WrapText"))
            setWrapLines(anElement.getAttributeBoolValue("WrapText"));

        // If RichText, unarchive rich text
        XMLElement richTextXML = anElement.get("RichText");
        if (richTextXML != null) {
            RichText richText = (RichText) getTextDoc();
            getUndoer().disable();
            richText.fromXML(anArchiver, richTextXML);
            getUndoer().enable();
        }

        // Otherwise unarchive text. Text can be "text" or "value" attribute, or as content (CDATA or otherwise)
        else {
            String str = anElement.getAttributeValue("text",  anElement.getAttributeValue("value", anElement.getValue()));
            if (str != null && str.length() > 0)
                setText(str);
        }

        // Unarchive FireActionOnEnterKey, FireActionOnFocusLost
        if (anElement.hasAttribute(FireActionOnEnterKey_Prop))
            setFireActionOnEnterKey(anElement.getAttributeBoolValue(FireActionOnEnterKey_Prop));
        if (anElement.hasAttribute(FireActionOnFocusLost_Prop))
            setFireActionOnFocusLost(anElement.getAttributeBoolValue(FireActionOnFocusLost_Prop));
    }
}