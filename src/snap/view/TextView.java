/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.props.PropChange;
import snap.props.PropSet;
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

    // Constants for properties
    public static final String WrapLines_Prop = TextArea.WrapLines_Prop;
    public static final String FireActionOnEnterKey_Prop = TextArea.FireActionOnEnterKey_Prop;
    public static final String FireActionOnFocusLost_Prop = TextArea.FireActionOnFocusLost_Prop;

    /**
     * Constructor.
     */
    public TextView()
    {
        this(false);
    }

    /**
     * Constructor with option for RichText.
     */
    public TextView(boolean isRichText)
    {
        // Create/configure TextArea
        _textArea = new TextArea(isRichText);
        _textArea.addPropChangeListener(pc -> textAreaDidPropChange(pc));

        // Create/add ScrollView
        _scrollView = new ScrollView(_textArea);
        addChild(_scrollView);

        // Other configuration
        _textArea.setFill(Color.WHITE);
        _textArea.setEditable(true);
    }

    /**
     * Returns the TextArea.
     */
    public TextArea getTextArea()  { return _textArea; }

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
     * Override to support properties for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        super.initProps(aPropSet);

        // Editable, WrapLines_Prop, FireActionOnEnterKey, FireActionOnFocusLost
        //aPropSet.addPropNamed(Editable_Prop, boolean.class);
        aPropSet.addPropNamed(WrapLines_Prop, boolean.class, false);
        aPropSet.addPropNamed(FireActionOnEnterKey_Prop, boolean.class, false);
        aPropSet.addPropNamed(FireActionOnFocusLost_Prop, boolean.class, false);
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Editable, WrapLines_Prop, FireActionOnEnterKey, FireActionOnFocusLost
            //case Editable_Prop: return isEditable();
            case WrapLines_Prop: return isWrapLines();
            case FireActionOnEnterKey_Prop: return _textArea.isFireActionOnEnterKey();
            case FireActionOnFocusLost_Prop: return _textArea.isFireActionOnFocusLost();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Editable, WrapLines_Prop, FireActionOnEnterKey, FireActionOnFocusLost
            //case Editable_Prop: setEditable(Convert.boolValue(aValue)); break;
            case WrapLines_Prop: setWrapLines(Convert.boolValue(aValue)); break;
            case FireActionOnEnterKey_Prop: _textArea.setFireActionOnEnterKey(Convert.boolValue(aValue)); break;
            case FireActionOnFocusLost_Prop: _textArea.setFireActionOnFocusLost(Convert.boolValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue); break;
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