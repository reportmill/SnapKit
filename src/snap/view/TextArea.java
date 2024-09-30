/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.gfx.*;
import snap.props.*;
import snap.text.*;
import snap.util.*;
import java.util.Objects;

/**
 * A view subclass for displaying and editing a TextBlock.
 */
public class TextArea extends ParentView {

    // The text adapter
    private TextAdapter _textAdapter;

    // The text being edited
    private TextBlock _textBlock;

    // Whether to synchronize text area font with text block
    private boolean _syncTextFont = true;

    // Whether as-you-type spell checking is enabled
    public static boolean isSpellChecking = Prefs.getDefaultPrefs().getBoolean("SpellChecking", false);

    // Whether hyphenating is activated
    private static boolean _hyphenating = Prefs.getDefaultPrefs().getBoolean("Hyphenating", false);

    // Constants for properties
    public static final String RichText_Prop = TextAdapter.RichText_Prop;
    public static final String Editable_Prop = TextAdapter.Editable_Prop;
    public static final String WrapLines_Prop = TextAdapter.WrapLines_Prop;
    public static final String SourceText_Prop = TextAdapter.SourceText_Prop;
    public static final String Selection_Prop = TextAdapter.Selection_Prop;

    /**
     * Constructor.
     */
    public TextArea()
    {
        this(false);
    }

    /**
     * Constructor with option for RichText.
     */
    public TextArea(boolean isRichText)
    {
        super();

        // Create/set default TextBlock
        _textBlock = new TextBox(isRichText);

        // Create TextAdapter
        _textAdapter = createTextAdapter(_textBlock);
        _textAdapter.setView(this);
        _textAdapter.addPropChangeListener(this::handleTextAdapterPropChange);
    }

    /**
     * Constructor for source text block.
     */
    public TextArea(TextBlock sourceText)
    {
        super();
        setFocusPainted(false);
        enableEvents(Action);

        // Set default TextBlock
        _textBlock = sourceText;

        // Create TextAdapter
        _textAdapter = createTextAdapter(_textBlock);
        _textAdapter.setView(this);
        _textAdapter.addPropChangeListener(this::handleTextAdapterPropChange);
    }

    /**
     * Override to create TextAreaKeys.
     */
    protected TextAdapter createTextAdapter(TextBlock textBlock)  { return new TextAdapter(textBlock); }

    /**
     * Returns the text block that holds the text.
     */
    public TextBlock getTextBlock()  { return _textAdapter.getTextBlock(); }

    /**
     * Returns the root text block.
     */
    public TextBlock getSourceText()  { return _textAdapter.getSourceText(); }

    /**
     * Sets the source TextBlock.
     */
    public void setSourceText(TextBlock aTextBlock)  { _textAdapter.setSourceText(aTextBlock); }

    /**
     * Returns the plain string of the text being edited.
     */
    public String getText()  { return _textAdapter.getText(); }

    /**
     * Set text string of text editor.
     */
    public void setText(String aString)  { _textAdapter.setText(aString); }

    /**
     * Returns whether Text shape is editable.
     */
    public boolean isEditable()  { return _textAdapter.isEditable(); }

    /**
     * Sets whether Text shape is editable.
     */
    public void setEditable(boolean aValue)  { _textAdapter.setEditable(aValue); }

    /**
     * Returns whether to wrap lines that overrun bounds.
     */
    public boolean isWrapLines()  { return _textAdapter.isWrapLines(); }

    /**
     * Sets whether to wrap lines that overrun bounds.
     */
    public void setWrapLines(boolean aValue)  { _textAdapter.setWrapLines(aValue); }

    /**
     * Returns whether to synchronize text area font with text block.
     */
    public boolean isSyncTextFont()  { return _syncTextFont; }

    /**
     * Sets whether to synchronize text area font with text block.
     */
    public void setSyncTextFont(boolean aValue)  { _syncTextFont = aValue; }

    /**
     * Returns whether editor is doing check-as-you-type spelling.
     */
    public boolean isSpellChecking()  { return _textAdapter.isSpellChecking(); }

    /**
     * Returns whether text supports multiple styles.
     */
    public boolean isRichText()  { return _textAdapter.isRichText(); }

    /**
     * Sets whether text supports multiple styles.
     */
    public void setRichText(boolean aValue)  { _textAdapter.setRichText(aValue); }

    /**
     * Returns the default text style for text.
     */
    public TextStyle getDefaultTextStyle()  { return _textAdapter.getDefaultTextStyle(); }

    /**
     * Sets the default text style for text.
     */
    public void setDefaultTextStyle(TextStyle textStyle)  { _textAdapter.setDefaultTextStyle(textStyle); }

    /**
     * Sets default text style for given style string.
     */
    public void setDefaultTextStyleString(String styleString)  { _textAdapter.setDefaultTextStyleString(styleString); }

    /**
     * Returns the default line style for text.
     */
    public TextLineStyle getDefaultLineStyle()  { return _textAdapter.getDefaultLineStyle(); }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle aLineStyle)  { _textAdapter.setDefaultLineStyle(aLineStyle); }

    /**
     * Returns the number of characters in the text string.
     */
    public int length()  { return _textAdapter.length(); }

    /**
     * Returns the individual character at given index.
     */
    public char charAt(int anIndex)  { return _textAdapter.charAt(anIndex); }

    /**
     * Returns whether the selection is empty.
     */
    public boolean isSelEmpty()  { return _textAdapter.isSelEmpty(); }

    /**
     * Returns the final character index of the selection (usually SelEnd).
     */
    public int getSelIndex()  { return _textAdapter.getSelIndex(); }

    /**
     * Returns the character index of the start of the text selection.
     */
    public int getSelStart()  { return _textAdapter.getSelStart(); }

    /**
     * Returns the character index of the end of the text selection.
     */
    public int getSelEnd()  { return _textAdapter.getSelEnd(); }

    /**
     * Returns the text selection.
     */
    public TextSel getSel()  { return _textAdapter.getSel(); }

    /**
     * Sets the character index of the text cursor.
     */
    public void setSel(int newStartEnd)  { setSel(newStartEnd, newStartEnd); }

    /**
     * Sets the character index of the start and end of the text selection.
     */
    public void setSel(int aStart, int aEnd)  { _textAdapter.setSel(aStart, aEnd); }

    /**
     * Selects all the characters in the text editor.
     */
    public void selectAll()  { _textAdapter.selectAll(); }

    /**
     * Returns the font of the text block.
     */
    public Font getTextFont()  { return _textAdapter.getTextFont(); }

    /**
     * Sets the font of the text block.
     */
    public void setTextFont(Font aFont)  { _textAdapter.setTextFont(aFont); }

    /**
     * Returns the color of the current selection or cursor.
     */
    @Override
    public Color getTextColor()  { return _textAdapter.getTextColor(); }

    /**
     * Sets the color of the current selection or cursor.
     */
    @Override
    public void setTextColor(Color aColor)  { _textAdapter.setTextColor(aColor); }

    /**
     * Returns the format of the current selection or cursor.
     */
    public TextFormat getFormat()  { return _textAdapter.getFormat(); }

    /**
     * Sets the format of the current selection or cursor, after trying to expand the selection to encompass currently
     * selected, @-sign delineated key.
     */
    public void setFormat(TextFormat aFormat)  { _textAdapter.setFormat(aFormat); }

    /**
     * Returns whether TextView is underlined.
     */
    public boolean isUnderlined()  { return _textAdapter.isUnderlined(); }

    /**
     * Sets whether TextView is underlined.
     */
    public void setUnderlined(boolean aValue)  { _textAdapter.setUnderlined(aValue); }

    /**
     * Adds the given chars to end of text.
     */
    public void addChars(CharSequence theChars)  { addCharsWithStyle(theChars, null, length()); }

    /**
     * Adds the given chars at given char index.
     */
    public void addChars(CharSequence theChars, int charIndex)  { addCharsWithStyle(theChars, null, charIndex); }

    /**
     * Adds the given chars with given style to text end.
     */
    public void addCharsWithStyle(CharSequence theChars, TextStyle textStyle)
    {
        addCharsWithStyle(theChars, textStyle, length());
    }

    /**
     * Adds the given chars with given style to text at given index.
     */
    public void addCharsWithStyle(CharSequence theChars, TextStyle textStyle, int charIndex)
    {
        _textAdapter.addCharsWithStyle(theChars, textStyle, charIndex);
    }

    /**
     * Adds the given chars to text with given style string.
     */
    public void addCharsWithStyleString(CharSequence theChars, String styleString)
    {
        _textAdapter.addCharsWithStyleString(theChars, styleString);
    }

    /**
     * Deletes the given range of chars.
     */
    public void removeChars(int aStart, int anEnd)  { _textAdapter.removeChars(aStart, anEnd); }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceChars(CharSequence theChars)
    {
        int startCharIndex = getSelStart();
        int endCharIndex = getSelEnd();
        replaceCharsWithStyle(theChars, null, startCharIndex, endCharIndex);
    }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceChars(CharSequence theChars, int startCharIndex, int endCharIndex)
    {
        replaceCharsWithStyle(theChars, null, startCharIndex, endCharIndex);
    }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceCharsWithStyle(CharSequence theChars, TextStyle textStyle, int aStart, int anEnd)
    {
        _textAdapter.replaceCharsWithStyle(theChars, textStyle, aStart, anEnd);
    }

    /**
     * Deletes the current selection.
     */
    public void delete()  { _textAdapter.delete(); }

    /**
     * Replaces the current selection with the given contents (TextBlock or String).
     */
    public void replaceCharsWithContent(Object theContent)  { _textAdapter.replaceCharsWithContent(theContent); }

    /**
     * Clears the text.
     */
    public void clear()  { _textAdapter.clear(); }

    /**
     * Returns the number of lines.
     */
    public int getLineCount()  { return _textAdapter.getLineCount(); }

    /**
     * Returns the individual line at given index.
     */
    public TextLine getLine(int anIndex)  { return _textAdapter.getLine(anIndex); }

    /**
     * Returns the line for the given character index.
     */
    public TextLine getLineForCharIndex(int anIndex)  { return _textAdapter.getLineForCharIndex(anIndex); }

    /**
     * Returns the token for given character index.
     */
    public TextToken getTokenForCharIndex(int anIndex)  { return _textAdapter.getTokenForCharIndex(anIndex); }

    /**
     * Returns the char index for given point in text coordinate space.
     */
    public int getCharIndexForXY(double anX, double aY)  { return _textAdapter.getCharIndexForXY(anX, aY); }

    /**
     * Paint text.
     */
    protected void paintFront(Painter aPntr)  { _textAdapter.paintText(aPntr); }

    /**
     * Process event. Make this public so TextArea can be used to edit text outside of normal Views.
     */
    public void processEvent(ViewEvent anEvent)
    {
        switch (anEvent.getType()) {
            case MousePress: mousePressed(anEvent); break;
            case MouseDrag: mouseDragged(anEvent); break;
            case MouseRelease: mouseReleased(anEvent); break;
            case MouseMove: mouseMoved(anEvent); break;
            case KeyPress: keyPressed(anEvent); break;
            case KeyType: keyTyped(anEvent); break;
            case KeyRelease: keyReleased(anEvent); break;
            case Action: processActionEvent(anEvent);
        }

        // Consume all mouse events
        if (anEvent.isMouseEvent()) anEvent.consume();
    }

    /**
     * Handles mouse pressed.
     */
    protected void mousePressed(ViewEvent anEvent)  { _textAdapter.mousePressed(anEvent); }

    /**
     * Handles mouse dragged.
     */
    protected void mouseDragged(ViewEvent anEvent)  { _textAdapter.mouseDragged(anEvent); }

    /**
     * Handles mouse released.
     */
    protected void mouseReleased(ViewEvent anEvent)  { _textAdapter.mouseReleased(anEvent); }

    /**
     * Handle MouseMoved.
     */
    protected void mouseMoved(ViewEvent anEvent)  { _textAdapter.mouseMoved(anEvent); }

    /**
     * Called when a key is pressed.
     */
    protected void keyPressed(ViewEvent anEvent)  { _textAdapter.keyPressed(anEvent); }

    /**
     * Called when a key is typed.
     */
    protected void keyTyped(ViewEvent anEvent)  { _textAdapter.keyTyped(anEvent); }

    /**
     * Called when a key is released.
     */
    protected void keyReleased(ViewEvent anEvent)  { _textAdapter.keyReleased(anEvent); }

    /**
     * Called when action event is received.
     */
    protected void processActionEvent(ViewEvent anEvent)
    {
        // Get shared action name
        SharedAction action = anEvent.getSharedAction();
        String actionName = action !=  null ? action.getName() : null;
        if (actionName == null)
            return;

        // Handle shared actions
        switch (action.getName()) {
            case SharedAction.Cut_Action_Name: cut(); anEvent.consume(); break;
            case SharedAction.Copy_Action_Name: copy(); anEvent.consume(); break;
            case SharedAction.Paste_Action_Name: paste(); anEvent.consume(); break;
            case SharedAction.SelectAll_Action_Name: selectAll(); anEvent.consume(); break;
            case SharedAction.Undo_Action_Name: undo(); anEvent.consume(); break;
            case SharedAction.Redo_Action_Name: redo(); anEvent.consume(); break;
        }
    }

    /**
     * Returns the font scale of the text box.
     */
    public double getFontScale()  { return _textAdapter.getFontScale(); }

    /**
     * Sets the font scale of the text box.
     */
    public void setFontScale(double aValue)  { _textAdapter.setFontScale(aValue); }

    /**
     * Scales font sizes of all text in TextBox to fit in bounds by finding/setting FontScale.
     */
    public void scaleTextToFit()  { _textAdapter.scaleTextToFit(); }

    /**
     * Copies the current selection onto the clip board, then deletes the current selection.
     */
    public void cut()  { _textAdapter.cut(); }

    /**
     * Copies the current selection onto the clipboard.
     */
    public void copy()  { _textAdapter.copy(); }

    /**
     * Pasts the current clipboard data over the current selection.
     */
    public void paste()  { _textAdapter.paste(); }

    /**
     * Returns the clipboard content.
     */
    protected Object getClipboardContent(Clipboard clipboard)  { return _textAdapter.getClipboardContent(clipboard); }

    /**
     * Returns the undoer.
     */
    public Undoer getUndoer()  { return _textAdapter.getUndoer(); }

    /**
     * Called to undo the last text change.
     */
    public void undo()  { _textAdapter.undo(); }

    /**
     * Called to redo the last text change.
     */
    public void redo()  { _textAdapter.redo(); }

    /**
     * Returns the width needed to display all characters.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        double prefW = _textAdapter.getPrefWidth();
        return prefW + ins.getWidth();
    }

    /**
     * Returns the height needed to display all characters.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double prefW = aW >= 0 ? aW - ins.getWidth() : aW;
        double prefH = _textAdapter.getPrefHeight(prefW);
        return prefH + ins.getHeight();
    }

    /**
     * Sets the font of the current selection or cursor.
     */
    @Override
    public void setFont(Font aFont)
    {
        if (Objects.equals(aFont, getFont())) return;
        super.setFont(aFont);

        // If SyncTextFont, forward to TextBlock
        if (isSyncTextFont())
            setTextFont(getFont());
    }

    /**
     * Override to update font.
     */
    @Override
    protected void parentFontChanged()
    {
        // Handle RichText: Just return
        if (isRichText()) return;

        // Do normal version
        super.parentFontChanged();

        // If SyncTextFont, forward to TextBlock
        if (isSyncTextFont())
            setTextFont(getFont());
    }

    /**
     * Called when TextAdapter has prop change.
     */
    private void handleTextAdapterPropChange(PropChange aPC)
    {
        // If SourceText prop change, forward on
        if (aPC.getSource() instanceof TextBlock) {
            handleSourceTextPropChange(aPC);
            return;
        }

        // Handle Selection, Editable
        switch (aPC.getPropName()) {
            case Selection_Prop: handleSelectionChanged(); break;
            case Editable_Prop: handleEditableChanged(); break;
        }
    }

    /**
     * Called when TextAdapter selection changes.
     */
    protected void handleSelectionChanged()  { }

    /**
     * Sets whether Text shape is editable.
     */
    private void handleEditableChanged()
    {
        boolean editable = isEditable();
        firePropChange(Editable_Prop, !editable, editable);

        // If editable, set some related attributes
        if (editable) {
            enableEvents(MouseEvents);
            enableEvents(KeyEvents);
            setFocusable(true);
            setFocusWhenPressed(true);
            setFocusKeysEnabled(false);
            setFocusPainted(false);
        }

        else {
            disableEvents(MouseEvents);
            disableEvents(KeyEvents);
            setFocusable(false);
            setFocusWhenPressed(false);
            setFocusKeysEnabled(true);
        }
    }

    /**
     * Called when SourceText changes (chars added, updated or deleted).
     */
    protected void handleSourceTextPropChange(PropChange aPC)
    {
        // Forward on to listeners
        firePropChange(aPC);

        // Handle DefaultTextStyle and SyncTextFont
        String propName = aPC.getPropName();
        if (propName == TextBlock.DefaultTextStyle_Prop && isSyncTextFont()) {
            Font font = _textBlock.getDefaultFont();
            setFont(font);
        }

        // Relayout and repaint
        relayoutParent();
        relayout();
        repaint();
    }

    /**
     * Returns whether layout tries to hyphenate wrapped words.
     */
    public static boolean isHyphenating()  { return _hyphenating; }

    /**
     * Sets whether layout tries to hyphenate wrapped words.
     */
    public static void setHyphenating(boolean aValue)
    {
        if (aValue == isHyphenating()) return;
        Prefs.getDefaultPrefs().setValue("Hyphenating", _hyphenating = aValue);
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return "Text"; }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = getText();
        if (str.length() > 40) str = str.substring(0, 40) + "...";
        return getClass().getSimpleName() + ": " + str;
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        super.initProps(aPropSet);

        // RichText, Editable, WrapLines
        //aPropSet.addPropNamed(RichText_Prop, boolean.class, false);
        aPropSet.addPropNamed(Editable_Prop, boolean.class, false);
        aPropSet.addPropNamed(WrapLines_Prop, boolean.class, false);
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // RichText, Editable, WrapLines
            case RichText_Prop: return isRichText();
            case Editable_Prop: return isEditable();
            case WrapLines_Prop: return isWrapLines();

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

            // RichText, Editable, WrapLines_Prop
            case RichText_Prop: setRichText(Convert.boolValue(aValue)); break;
            case Editable_Prop: setEditable(Convert.boolValue(aValue)); break;
            case WrapLines_Prop: setWrapLines(Convert.boolValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue); break;
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement xml = super.toXML(anArchiver);

        // Archive Editable, WrapLines
        if (!isPropDefault(Editable_Prop)) xml.add(Editable_Prop, isEditable());
        if (!isPropDefault(WrapLines_Prop)) xml.add(WrapLines_Prop, isWrapLines());
        return xml;
    }

    /**
     * XML unarchival.
     */
    public TextArea fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXML(anArchiver, anElement);

        // Unarchive Editable, WrapLines
        if (anElement.hasAttribute(Editable_Prop))
            setEditable(anElement.getAttributeBoolValue(Editable_Prop));
        if (anElement.hasAttribute(WrapLines_Prop))
            setWrapLines(anElement.getAttributeBoolValue(WrapLines_Prop));

        return this;
    }

    @Deprecated
    public TextStyle getDefaultStyle()  { return getDefaultTextStyle(); }
    public void setDefaultStyle(TextStyle aStyle)  { setDefaultTextStyle(aStyle); }
}