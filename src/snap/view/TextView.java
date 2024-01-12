/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.props.PropChange;
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
    private EventListener  _actionEvtLsnr;

    /**
     * Constructor.
     */
    public TextView()
    {
        // Create/configure TextArea
        _textArea = createTextArea();
        _textArea.addPropChangeListener(pc -> textAreaDidPropChange(pc));

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
     * Returns the text that is being edited.
     */
    public TextBlock getTextBlock()  { return _textArea.getTextBlock(); }

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
     * Sets whether to wrap lines that overrun bounds.
     */
    public void setWrapLines(boolean aValue)
    {
        _textArea.setWrapLines(aValue);
        _scrollView.setFillWidth(aValue);
    }

    /**
     * Returns the default style for text.
     */
    public TextStyle getDefaultStyle()  { return _textArea.getDefaultStyle(); }

    /**
     * Sets the default style.
     */
    public void setDefaultStyle(TextStyle aStyle)  { _textArea.setDefaultStyle(aStyle); }

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
     * Called when TextArea gets prop changes.
     */
    protected void textAreaDidPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();

        // Handle WrapLines
        if (propName == TextArea.WrapLines_Prop)
            _scrollView.setFillWidth(_textArea.isWrapLines());

        // Handle FireActionOnEnterKey, FireActionOnFocusLost
        if (propName == TextArea.FireActionOnEnterKey_Prop || propName == TextArea.FireActionOnFocusLost_Prop) {
            boolean propagateTextAreaFireAction = _textArea.isFireActionOnEnterKey() || _textArea.isFireActionOnFocusLost();
            setActionable(propagateTextAreaFireAction);
            if (propagateTextAreaFireAction) {
                if (_actionEvtLsnr == null) {
                    _actionEvtLsnr =  e -> fireActionEvent(e);
                    _textArea.addEventHandler(_actionEvtLsnr, Action);
                }
            }
            else if (_actionEvtLsnr != null) {
                _textArea.removeEventHandler(_actionEvtLsnr);
                _actionEvtLsnr = null;
            }
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        XMLElement xml = super.toXMLView(anArchiver);
        _textArea.toXMLTextArea(anArchiver, xml);
        return xml;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXMLView(anArchiver, anElement);
        _textArea.fromXMLTextArea(anArchiver, anElement);
    }
}