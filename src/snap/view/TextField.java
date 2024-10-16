/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.gfx.*;
import snap.props.PropChange;
import snap.props.PropSet;
import snap.text.TextAdapter;
import snap.text.TextBlock;
import snap.util.*;
import java.util.Objects;

/**
 * A view subclass for editing a single line of text.
 */
public class TextField extends ParentView {

    // The TextAdapter
    private TextAdapter _textAdapter;
    
    // The column count to be used for preferred width (if set)
    private int _colCount;
    
    // The color for the text
    private Color _textColor;
    
    // A label in the background for prompt text or other background decoration
    private Label _promptLabel;
    
    // The string to show when textfield is empty
    private String _promptText;
    
    // Whether to send action on focus lost (if content changed)
    private boolean _fireActionOnFocusLost;

    // Whether text field accepts multiple lines of text
    private boolean _multiline;

    // The value of text on focus gained
    protected String _focusGainedText;
    
    // Whether text has been edited since last focus
    private boolean _edited;

    // Whether text field is showing an auto-completion
    private boolean _autoCompleting;

    // Constants for properties
    public static final String ColCount_Prop = "ColCount";
    public static final String PromptText_Prop = "PromptText";
    public static final String Multiline_Prop = "Multiline";
    public static final String FireActionOnFocusLost_Prop = "FireActionOnFocusLost";
    public static final String Selection_Prop = TextAdapter.Selection_Prop;
    public static final String Edited_Prop = "Edited";

    // Constants for property defaults
    private static final int DEFAULT_COL_COUNT = 12;
    private static final boolean DEFAULT_FIRE_ACTION_ON_FOCUS_LOST = true;

    /**
     * Constructor.
     */
    public TextField()
    {
        super();
        _colCount = DEFAULT_COL_COUNT;
        _fireActionOnFocusLost = DEFAULT_FIRE_ACTION_ON_FOCUS_LOST;

        // Override default properties
        setFocusable(true);
        setFocusWhenPressed(true);
        setActionable(true);
        enableEvents(MouseEvents);
        enableEvents(KeyEvents);
        enableEvents(Action);

        // Create TextAdapter
        _textAdapter = new TextAdapter(new TextBlock());
        _textAdapter.setView(this);
        _textAdapter.setEditable(true);
        _textAdapter.addPropChangeListener(this::handleTextAdapterPropChange);
        _textAdapter.addSourceTextPropChangeListener(this::handleSourceTextPropChange);

        // Configure label and set
        _promptLabel = new Label();
        _promptLabel.setPadding(0, 0, 0, 0);
        _promptLabel.setPickable(false);
        addChild(_promptLabel);
    }

    /**
     * Override to support TextColor.
     */
    @Override
    protected void initStyleProps()
    {
        super.initStyleProps();
        ViewStyle viewStyle = ViewTheme.get().getViewStyleForClass(getClass());
        _textColor = viewStyle.getTextColor();
    }

    /**
     * Returns the text color.
     */
    @Override
    public Color getTextColor()  { return _textColor; }

    /**
     * Sets the text color.
     */
    @Override
    public void setTextColor(Color aPaint)
    {
        if (Objects.equals(aPaint, _textColor)) return;
        firePropChange(TextColor_Prop, _textColor, _textColor = aPaint);
        repaint();
    }

    /**
     * Returns the column count.
     */
    public int getColCount()  { return _colCount; }

    /**
     * Sets the column count.
     */
    public void setColCount(int aValue)
    {
        firePropChange(ColCount_Prop, _colCount, _colCount = aValue);
        relayoutParent();
    }

    /**
     * Returns the total column width.
     */
    private double getTotalColWidth()  { return Math.ceil(_colCount * getFont().charAdvance('X')); }

    /**
     * Returns the prompt text.
     */
    public String getPromptText()  { return _promptText; }

    /**
     * Sets the prompt text.
     */
    public void setPromptText(String aStr)
    {
        if (Objects.equals(aStr, _promptText)) return;
        _promptLabel.setText(aStr);
        _promptLabel.setTextColor(Color.LIGHTGRAY);
        firePropChange(PromptText_Prop, _promptText, _promptText = aStr);
    }

    /**
     * Returns whether text view fires action on focus lost (if text changed).
     */
    public boolean isFireActionOnFocusLost()  { return _fireActionOnFocusLost; }

    /**
     * Sets whether text area sends action on focus lost (if text changed).
     */
    public void setFireActionOnFocusLost(boolean aValue)
    {
        if (aValue == _fireActionOnFocusLost) return;
        firePropChange(FireActionOnFocusLost_Prop, _fireActionOnFocusLost, _fireActionOnFocusLost = aValue);
    }

    /**
     * Returns whether text field accepts multiple lines of text.
     */
    public boolean isMultiline()  { return _multiline; }

    /**
     * Sets whether text field accepts multiple lines of text.
     */
    public void setMultiline(boolean aValue)
    {
        if (aValue == isMultiline()) return;
        firePropChange(Multiline_Prop, _multiline, _multiline = aValue);
    }

    /**
     * Returns the label in the background.
     */
    public Label getLabel()  { return _promptLabel; }

    /**
     * Override to track FocusGainedValue.
     */
    protected void setFocused(boolean aValue)
    {
        // Do normal version
        if (aValue == isFocused()) return;
        super.setFocused(aValue);

        // Repaint
        repaint();

        // Handle focus gained: set FocusedGainedValue and select all (if not from mouse press)
        if (aValue) {
            _focusGainedText = getText();
            _edited = false;
        }

        // Handle focus lost: If FocusGainedVal changed, fire action
        else {
            if (isEdited() && isFireActionOnFocusLost())
                fireActionEvent(null);
        }
    }

    /**
     * Override to reset FocusedGainedVal.
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        _focusGainedText = getText();
        _edited = false;
        super.fireActionEvent(anEvent);
        _focusGainedText = getText();
        setEdited(false);
    }

    /**
     * Returns whether text has been edited since last focus (while focused).
     */
    public boolean isEdited()  { return _edited; }

    /**
     * Sets whether text has been edited since last focus (while focused).
     */
    protected void setEdited(boolean aValue)
    {
        if (aValue == isEdited()) return;
        firePropChange(Edited_Prop, _edited, _edited = aValue);
    }

    /**
     * Returns the number of characters in the text string.
     */
    public int length()  { return _textAdapter.length(); }

    /**
     * Returns the plain string of the text being edited.
     */
    public String getText()  { return _textAdapter.getText(); }

    /**
     * Set text string of text editor.
     */
    public void setText(String aString)  { _textAdapter.setText(aString); }

    /**
     * Returns the character index of the start of the text selection.
     */
    public int getSelStart()  { return _textAdapter.getSelStart(); }

    /**
     * Returns the character index of the end of the text selection.
     */
    public int getSelEnd()  { return _textAdapter.getSelEnd(); }

    /**
     * Returns whether the selection is empty.
     */
    public boolean isSelEmpty()  { return _textAdapter.isSelEmpty(); }

    /**
     * Sets the character index of the text cursor.
     */
    public void setSel(int newStartEnd)  { _textAdapter.setSel(newStartEnd); }

    /**
     * Sets the character index of the start and end of the text selection.
     */
    public void setSel(int aStart, int anEnd)  { _textAdapter.setSel(aStart, anEnd); }

    /**
     * Selects all the characters in the text editor.
     */
    public void selectAll()
    {
        // If mouse down, come back later
        if (ViewUtils.isMouseDown()) {
            getEnv().runLater(() -> selectAll());
            return;
        }

        // Set selection
        _textAdapter.selectAll();
    }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceChars(String aString)  { _textAdapter.replaceChars(aString); }

    /**
     * Sets text that represents a completion of current text. This preserves the capitalization of chars in the current
     * text and sets the selection to the remainder. If next key press is delete, removes the added remainder.
     */
    public void setCompletionText(String aString)
    {
        // Get text - just return if completion string is just text
        String text = getText();
        if (!StringUtils.startsWithIC(aString, text))
            return;

        // Get completion string by appending chars to original text and set
        String text2 = text + aString.substring(text.length());
        setText(text2);

        // Set sel and mark as CompletionSel
        setSel(text.length(), text2.length());
        _autoCompleting = true;
    }

    /**
     * Paints TextField.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        aPntr.save();
        Rect textBounds = _textAdapter.getTextBounds();
        aPntr.clip(textBounds);

        // Paint text (only paint selection when focused)
        if (isFocused())
            _textAdapter.paintSel(aPntr);
        _textAdapter.paintText(aPntr);

        aPntr.restore();
    }

    /**
     * Calculates the preferred width.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        double prefW1 = getColCount() > 0 ? getTotalColWidth() : _textAdapter.getPrefWidth();
        double prefW2 = _promptLabel.getPrefWidth();
        double prefW3 = Math.max(prefW1, prefW2);
        return prefW3 + ins.getWidth();
    }

    /**
     * Calculates the preferred height.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double prefW = aW >= 0 ? aW - ins.getWidth() : aW;
        double prefH1 = _textAdapter.getPrefHeight(prefW);
        double prefH2 = _promptLabel.getPrefHeight();
        double prefH3 = Math.max(prefH1, prefH2);
        return prefH3 + ins.getHeight();
    }

    /**
     * Layout children.
     */
    @Override
    protected void layoutImpl()
    {
        // Layout PromptLabel
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        _promptLabel.setBounds(areaX, areaY, areaW, areaH);

        // Reset text bounds
        updateTextBounds();
    }

    /**
     * Process event.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        switch (anEvent.getType()) {
            case MousePress: _textAdapter.mousePressed(anEvent); break;
            case MouseDrag: _textAdapter.mouseDragged(anEvent); break;
            case MouseRelease: _textAdapter.mouseReleased(anEvent); break;
            case MouseMove: _textAdapter.mouseMoved(anEvent); break;
            case KeyPress: keyPressed(anEvent); break;
            case KeyType: _textAdapter.keyTyped(anEvent); break;
            case KeyRelease: _textAdapter.keyReleased(anEvent); break;
            case Action: processActionEvent(anEvent);
        }

        // Consume all mouse events
        if (anEvent.isMouseEvent())
            anEvent.consume();
    }

    /**
     * Called when a key is pressed.
     */
    protected void keyPressed(ViewEvent anEvent)
    {
        // Handle EnterKey
        if (anEvent.isEnterKey() && !isMultiline() &&
            !anEvent.isShortcutDown() && !anEvent.isControlDown() && !anEvent.isAltDown()) {
            selectAll();
            fireActionEvent(anEvent);
        }

        // Handle Escape
        else if (anEvent.isEscapeKey())
            escape(anEvent);

        // Forward to text adapter
        else _textAdapter.keyPressed(anEvent);

        // Handle BackSpace: If auto-completing, run extra time to delete selection and char
        if (_autoCompleting && anEvent.isBackSpaceKey()) {
            _autoCompleting = false;
            _textAdapter.deleteBackward();
        }
    }

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
            case SharedAction.Cut_Action_Name: _textAdapter.cut(); anEvent.consume(); break;
            case SharedAction.Copy_Action_Name: _textAdapter.copy(); anEvent.consume(); break;
            case SharedAction.Paste_Action_Name: _textAdapter.paste(); anEvent.consume(); break;
            case SharedAction.SelectAll_Action_Name: selectAll(); anEvent.consume(); break;
        }
    }

    /**
     * Called when escape key is pressed to cancels editing in TextField.
     * First cancel resets focus gained value. Second hands focus to previous view.
     */
    public void escape(ViewEvent anEvent)
    {
        // If not focused, just return
        if (!isFocused()) return;

        // If value has changed since focus gained, reset to original value
        if (isEdited()) {
            setText(_focusGainedText);
            setEdited(false);
            selectAll();
            if (anEvent != null)
                anEvent.consume();
        }

        // Otherwise hand focus to previous view
        else {
            getWindow().requestFocus(null);
            anEvent.consume();
        }
    }

    /**
     * Called when TextAdapter has prop change.
     */
    private void handleTextAdapterPropChange(PropChange aPC)
    {
        // Handle Selection
        if (aPC.getPropName() == TextAdapter.Selection_Prop) {
            firePropChange(Selection_Prop, aPC.getOldValue(), aPC.getNewValue());
            _autoCompleting = false;
        }
    }

    /**
     * Called when SourceText changes (chars added, updated or deleted).
     */
    private void handleSourceTextPropChange(PropChange aPC)
    {
        // If PromptText present, update PromptLabel.Text
        if (_promptText != null)
            _promptLabel.setText(length() == 0 ? _promptText : "");

        // If focused and text has changed, updated Edited
        if (isFocused() && !isEdited() && !Objects.equals(getText(), _focusGainedText))
            setEdited(true);

        // Relayout parent and repaint
        relayoutParent();
        relayout();
        repaint();
    }

    /**
     * Updates the text bounds.
     */
    private void updateTextBounds()
    {
        // Get text bounds and set
        Rect textBounds = getTextBounds();
        _textAdapter.setTextBounds(textBounds);

        // Promote to WrapLines if text is long
        if (!_textAdapter.isWrapLines()) {
            double prefW = _textAdapter.getPrefWidth();
            if (prefW > textBounds.width)
                runLater(() -> _textAdapter.setWrapLines(true));
        }

        // Check for whether to wrap in scroll view
        if (_textAdapter.getLineCount() > 1)
            ViewUtils.checkWantsScrollView(this);
    }

    /**
     * Returns the text bounds.
     */
    private Rect getTextBounds()
    {
        // Get basic bounds for TextField size/insets and font/string width/height
        Insets ins = getInsetsAll();
        double viewW = getWidth();
        double viewH = getHeight();
        double textX = ins.left + _promptLabel.getTextBounds().x;
        double textY = ins.top;
        double textW = viewW - ins.getWidth();
        double prefH = _textAdapter.getPrefHeight(textW);
        double textH = Math.min(prefH, viewH - ins.getHeight());

        // Adjust rect by alignment
        double alignX = ViewUtils.getAlignX(this);
        double alignY = ViewUtils.getAlignY(this);
        if (alignX > 0) {
            double extra = viewW - textX - ins.right - textW;
            textX = Math.max(textX + extra * alignX, textX);
        }
        if (alignY > 0) {
            double extra = viewH - textY - ins.bottom - textH;
            textY = Math.max(textY + Math.round(extra * alignY), textY);
        }

        // Create/return rect
        return new Rect(textX, textY, textW, textH);
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return "Text"; }

    /**
     * Override to customize for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // ColCount, PromptText, Multiline
        aPropSet.addPropNamed(ColCount_Prop, int.class, DEFAULT_COL_COUNT);
        aPropSet.addPropNamed(PromptText_Prop, String.class, EMPTY_OBJECT);
        aPropSet.addPropNamed(Multiline_Prop, boolean.class, false);
    }

    /**
     * Returns the value for given prop name.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // ColCount, PromptText, Multiline
            case ColCount_Prop: return getColCount();
            case PromptText_Prop: return getPromptText();
            case Multiline_Prop: return isMultiline();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Sets the value for given prop name.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch (aPropName) {

            // ColCount, PromptText, Multiline
            case ColCount_Prop: setColCount(Convert.intValue(aValue)); break;
            case PromptText_Prop: setPromptText(Convert.stringValue(aValue)); break;
            case Multiline_Prop: setMultiline(Convert.boolValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = getText();
        if (str.length() > 40)
            str = str.substring(0, 40) + "...";
        return getClass().getSimpleName() + ": " + str;
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Do normal version
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ColCount, PromptText, Multiline
        if (!isPropDefault(ColCount_Prop)) e.add(ColCount_Prop, getColCount());
        if (!isPropDefault(PromptText_Prop)) e.add(PromptText_Prop, getPromptText());
        if (!isPropDefault(Multiline_Prop)) e.add(Multiline_Prop, isMultiline());
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Do normal version
        super.fromXMLView(anArchiver, anElement);

        // Unarchive ColCount, PromptText, Multiline
        if (anElement.hasAttribute(ColCount_Prop))
            setColCount(anElement.getAttributeIntValue(ColCount_Prop));
        if (anElement.hasAttribute(PromptText_Prop))
            setPromptText(anElement.getAttributeValue(PromptText_Prop));
        if (anElement.hasAttribute(Multiline_Prop))
            setMultiline(anElement.getAttributeBoolValue(Multiline_Prop));
    }

    /**
     * Sets the given TextField to animate background label alignment from center to left when focused.
     */
    public static void setBackLabelAlignAnimatedOnFocused(TextField aTextField, boolean aValue)
    {
        Label textFieldLabel = aTextField.getLabel();
        textFieldLabel.setAlign(Pos.CENTER);
        aTextField.addPropChangeListener(pce -> {
            if (aTextField.isFocused())
                ViewAnimUtils.setAlign(textFieldLabel, Pos.CENTER_LEFT, 200);
            else ViewAnimUtils.setAlign(textFieldLabel, Pos.CENTER, 600);
            textFieldLabel.getChild(0).getAnim(0).setOnFrame(aTextField::relayout);
        }, View.Focused_Prop);
    }
}