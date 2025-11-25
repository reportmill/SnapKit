/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.props.PropChange;
import snap.util.*;
import java.util.List;
import java.util.Objects;

/**
 * This class is the basic text storage class, holding a list of TextLine.
 */
public abstract class TextModel extends TextLayout implements XMLArchiver.Archivable {

    // The default text style for this text
    protected TextStyle _defaultTextStyle = TextStyle.DEFAULT;

    // The default line style for this text
    protected TextLineStyle _defaultLineStyle = TextLineStyle.DEFAULT;

    // Whether property change is enabled
    protected boolean _propChangeEnabled = true;

    // The last mouse Y, to help in caret placement (can be ambiguous for start/end of line)
    protected double _mouseY;

    // Constants for properties
    public static final String Chars_Prop = "Chars";
    public static final String Style_Prop = "Style";
    public static final String LineStyle_Prop = "LineStyle";
    public static final String DefaultTextStyle_Prop = "DefaultTextStyle";
    public static final String DefaultLineStyle_Prop = "DefaultLineStyle";

    /**
     * Constructor.
     */
    public TextModel()
    {
        super();
    }

    /**
     * Constructor with option to make rich text.
     */
    public TextModel(boolean isRich)
    {
        super();
        _rich = isRich;
    }

    /**
     * Sets whether text supports multiple styles.
     */
    public void setRichText(boolean aValue)
    {
        if (aValue == isRichText()) return;
        _rich = aValue;

        // Set DefaultStyle, because RichText never inherits from parent
        if (aValue && _defaultTextStyle == null)
            _defaultTextStyle = TextStyle.DEFAULT;
    }

    /**
     * Sets the text to the given string.
     */
    public void setString(String aString)
    {
        String string = aString != null ? aString : "";

        if (!string.contentEquals(this)) {
            //setPropChangeEnabled(false);
            replaceChars(string, 0, length());
            //setPropChangeEnabled(true);
        }
    }

    /**
     * Adds characters to this text.
     */
    public void addChars(CharSequence theChars)  { addCharsWithStyle(theChars, null, length()); }

    /**
     * Adds characters to this text at given index.
     */
    public void addChars(CharSequence theChars, int anIndex)  { addCharsWithStyle(theChars, null, anIndex); }

    /**
     * Adds characters with given style to this text.
     */
    public void addCharsWithStyle(CharSequence theChars, TextStyle theStyle)  { addCharsWithStyle(theChars, theStyle, length()); }

    /**
     * Adds characters with given style to this text at given index.
     */
    public abstract void addCharsWithStyle(CharSequence theChars, TextStyle theStyle, int anIndex);

    /**
     * Removes characters in given range.
     */
    public abstract void removeChars(int startCharIndex, int endCharIndex);

    /**
     * Replaces chars in given range, with given String, using the given attributes.
     */
    public void replaceChars(CharSequence theChars, int startCharIndex, int endCharIndex)
    {
        replaceCharsWithStyle(theChars, null, startCharIndex, endCharIndex);
    }

    /**
     * Replaces chars in given range, with given String, using the given attributes.
     */
    public void replaceCharsWithStyle(CharSequence theChars, TextStyle theStyle, int startCharIndex, int endCharIndex)
    {
        // Get TextStyle for add chars range (if not provided)
        TextStyle style = theStyle;
        if (style == null)
            style = getTextStyleForCharRange(startCharIndex, endCharIndex);

        // Remove given range and add chars
        if (endCharIndex > startCharIndex)
            removeChars(startCharIndex, endCharIndex);
        addCharsWithStyle(theChars, style, startCharIndex);
    }

    /**
     * Clears the text.
     */
    public void clear()
    {
        // Remove chars and reset style
        removeChars(0, length());
        setTextStyle(getDefaultTextStyle(), 0, 0);
        setLineStyle(getDefaultLineStyle(), 0, 0);
    }

    /**
     * Sets the given text style for given range.
     */
    public void setTextStyle(TextStyle textStyle, int startCharIndex, int endCharIndex)
    {
        // If plaint text, just return (can't apply style to range for plain text)
        if (!isRichText()) return;

        // Get run iter and split end runs
        TextRunIter runIter = getRunIterForCharRange(startCharIndex, endCharIndex);
        runIter.splitEndRuns();
        TextLine startLine = runIter.getLine();

        // Iterate over runs and reset style
        for (TextRun textRun : runIter) {

            // Set style
            TextStyle oldStyle = textRun.getTextStyle();
            textRun.setTextStyle(textStyle);

            // Fire prop change
            if (isPropChangeEnabled()) {
                int lineStartCharIndex = textRun.getLine().getStartCharIndex();
                int runStart = textRun.getStartCharIndex() + lineStartCharIndex;
                int runEnd = textRun.getEndCharIndex() + lineStartCharIndex;
                PropChange pc = new TextModelUtils.StyleChange(this, oldStyle, textStyle, runStart, runEnd);
                firePropChange(pc);
            }
        }

        // Update lines
        startLine.updateText();
        _prefW = -1;
    }

    /**
     * Sets a text style value for given key, value and range.
     */
    public void setTextStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        // If plaint text, just return (can't apply style to range for plain text)
        if (!isRichText()) return;

        // Iterate over lines in range and set attribute
        while (aStart < anEnd) {

            // Get run for range
            TextRun textRun = getRunForCharRange(aStart, anEnd);
            TextLine textLine = textRun.getLine();
            int lineStart = textLine.getStartCharIndex();
            int runEndInText = textRun.getEndCharIndex() + lineStart;
            int newStyleEndInText = Math.min(runEndInText, anEnd);

            // Get current run style, get new style for given key/value
            TextStyle style = textRun.getTextStyle();
            TextStyle newStyle = style.copyForStyleKeyValue(aKey, aValue);

            // Set new style for run range
            setTextStyle(newStyle, aStart, newStyleEndInText);

            // Reset start to run end
            aStart = runEndInText;
        }
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyle(TextLineStyle aStyle, int aStart, int anEnd)
    {
        // Handle Rich
        if (isRichText()) {
            setLineStyleRich(aStyle, aStart, anEnd);
            return;
        }

        // Handle Plain: Propagate to Lines
        TextLineStyle oldStyle = getLine(0).getLineStyle();
        if (aStyle.equals(oldStyle))
            return;

        // Propagate to all lines
        for (TextLine line : getLines())
            line.setLineStyle(aStyle);

        // Fire prop change
        if (isPropChangeEnabled())
            firePropChange(new TextModelUtils.LineStyleChange(this, oldStyle, aStyle, 0));

        _prefW = -1;
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        // Handle Rich
        if (isRichText())
            setLineStyleValueRich(aKey, aValue, aStart, anEnd);

        // Handle plain
        else {
            TextLineStyle oldStyle = getLine(0).getLineStyle();
            TextLineStyle newStyle = oldStyle.copyForPropKeyValue(aKey, aValue);
            setLineStyle(newStyle, 0, length());
        }

        _prefW = -1;
    }

    /**
     * Sets a given style to a given range.
     */
    private void setLineStyleRich(TextLineStyle aStyle, int aStart, int anEnd)
    {
        // Get start/end line indexes for char range
        int startLineIndex = getLineForCharIndex(aStart).getLineIndex();
        int endLineIndex = getLineForCharIndex(anEnd).getLineIndex();

        // Iterate over lines and set new style
        for (int i = startLineIndex; i <= endLineIndex; i++) {
            TextLine line = getLine(i);
            TextLineStyle oldStyle = line.getLineStyle();
            if (!aStyle.equals(oldStyle)) {
                line.setLineStyle(aStyle);
                if (isPropChangeEnabled())
                    firePropChange(new TextModelUtils.LineStyleChange(this, oldStyle, aStyle, i));
            }
        }
    }

    /**
     * Sets a given style to a given range.
     */
    private void setLineStyleValueRich(String aKey, Object aValue, int aStart, int anEnd)
    {
        // Get start/end line indexes
        int startLineIndex = getLineForCharIndex(aStart).getLineIndex();
        int endLineIndex = getLineForCharIndex(anEnd).getLineIndex();

        // Iterate over lines and set value independently for each
        for (int i = startLineIndex; i <= endLineIndex; i++) {
            TextLine line = getLine(i);
            TextLineStyle oldStyle = line.getLineStyle();
            TextLineStyle newStyle = oldStyle.copyForPropKeyValue(aKey, aValue);
            if (!newStyle.equals(oldStyle)) {
                line.setLineStyle(newStyle);
                if (isPropChangeEnabled())
                    firePropChange(new TextModelUtils.LineStyleChange(this, oldStyle, newStyle, i));
            }
        }
    }

    /**
     * Returns the default text style for text.
     */
    public TextStyle getDefaultTextStyle()  { return _defaultTextStyle; }

    /**
     * Sets the default text style.
     */
    public void setDefaultTextStyle(TextStyle textStyle)
    {
        // If already set, just return
        if (Objects.equals(textStyle, _defaultTextStyle)) return;

        // Set
        TextStyle oldStyle = _defaultTextStyle;
        _defaultTextStyle = textStyle;

        // Update existing lines
        if (!isRichText() || length() == 0) {
            for (TextLine line : getLines())
                line.setTextStyle(textStyle);
        }

        // Fire prop change
        if (isPropChangeEnabled())
            firePropChange(DefaultTextStyle_Prop, oldStyle, textStyle);
    }

    /**
     * Sets default text style for given style string.
     */
    public void setDefaultTextStyleString(String styleString)
    {
        TextStyle textStyle = getDefaultTextStyle();
        TextStyle textStyle2 = textStyle.copyForStyleString(styleString);
        setDefaultTextStyle(textStyle2);
    }

    /**
     * Returns the default line style for text.
     */
    public TextLineStyle getDefaultLineStyle()  { return _defaultLineStyle; }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle lineStyle)
    {
        // If already set, just return
        if (Objects.equals(lineStyle, _defaultLineStyle)) return;

        // Set
        TextLineStyle oldStyle = _defaultLineStyle;
        _defaultLineStyle = lineStyle;

        // Upgrade existing lines
        if (!isRichText() || length() == 0) {
            for (TextLine line : getLines())
                line.setLineStyle(lineStyle);
        }

        // Fire prop change
        if (isPropChangeEnabled())
            firePropChange(DefaultLineStyle_Prop, oldStyle, lineStyle);
    }

    /**
     * Returns the default font.
     */
    public Font getDefaultFont()  { return _defaultTextStyle.getFont(); }

    /**
     * Sets the default font.
     */
    public void setDefaultFont(Font aFont)
    {
        if (aFont.equals(getDefaultFont())) return;
        TextStyle newTextStyle = _defaultTextStyle.copyForStyleValue(aFont);
        setDefaultTextStyle(newTextStyle);
    }

    /**
     * Returns the default text color.
     */
    public Color getDefaultTextColor()  { return _defaultTextStyle.getColor(); }

    /**
     * Sets the default text color.
     */
    public void setDefaultTextColor(Color aColor)
    {
        if (aColor == null) aColor = Color.BLACK;
        if (aColor.equals(getDefaultTextColor())) return;
        TextStyle newTextStyle = _defaultTextStyle.copyForStyleValue(aColor);
        setDefaultTextStyle(newTextStyle);
    }

    /**
     * Returns a TextRunIter to easily traverse the runs for a given range of chars.
     */
    public TextRunIter getRunIterForCharRange(int startCharIndex, int endCharIndex)
    {
        return new TextRunIter(this, startCharIndex, endCharIndex, true);
    }

    /**
     * Sets text to be underlined.
     */
    public void setUnderlined(boolean aFlag)
    {
        setTextStyleValue(TextStyle.UNDERLINE_KEY, aFlag ? 1 : null, 0, length());
    }

    /**
     * Sets the horizontal alignment of the text.
     */
    public void setAlignX(HPos anAlignX)
    {
        setLineStyleValue(TextLineStyle.Align_Prop, anAlignX, 0, length());
    }

    /**
     * Scales all the fonts in text by given factor.
     */
    public void scaleFonts(double aScale)
    {
        // If scale 1, just return
        if (aScale == 1) return;

        // Iterate over lines
        for (TextLine line : getLines()) {
            for (TextRun run : line.getRuns()) {
                TextStyle runStyle = run.getTextStyle();
                TextStyle runStyleScaled = runStyle.copyForStyleValue(run.getFont().copyForScale(aScale));
                run.setTextStyle(runStyleScaled);
            }
        }
    }

    /**
     * Returns whether property change is enabled.
     */
    public boolean isPropChangeEnabled()  { return _propChangeEnabled; }

    /**
     * Sets whether property change is enabled.
     */
    public void setPropChangeEnabled(boolean aValue)  { _propChangeEnabled = aValue; }

    /**
     * Adds characters for given TextModel to this text at given index.
     */
    public void addCharsForTextModel(TextModel textModel, int anIndex)
    {
        List<TextLine> textLines = textModel.getLines();
        for (TextLine line : textLines) {
            TextRun[] lineRuns = line.getRuns();
            for (TextRun run : lineRuns) {
                int index = anIndex + line.getStartCharIndex() + run.getStartCharIndex();
                addCharsWithStyle(run.getString(), run.getTextStyle(), index);
                setLineStyle(line.getLineStyle(), index, index + run.length());
            }
        }
    }

    /**
     * Returns a copy of this text for given char range.
     */
    public TextModel copyForRange(int aStart, int anEnd)
    {
        // Get empty copy
        TextModel textCopy = TextModel.createDefaultTextModel(isRichText());
        textCopy.setDefaultTextStyle(getDefaultTextStyle());
        textCopy.setDefaultLineStyle(getDefaultLineStyle());

        // Add chars for range
        TextRunIter runIter = getRunIterForCharRange(aStart, anEnd);
        for (TextRun textRun : runIter)
            textCopy.addCharsWithStyle(textRun.getString(), textRun.getTextStyle());

        // Return
        return textCopy;
    }

    /**
     * Creates TextTokens for a TextLine.
     */
    protected TextToken[] createTokensForTextLine(TextLine aTextLine)
    {
        return TextModelUtils.createTokensForTextLine(aTextLine);
    }

    /**
     * Returns underlined runs for text.
     */
    public TextRun[] getUnderlineRuns(Rect aRect)  { return TextModelUtils.getUnderlineRuns(this, aRect); }

    /**
     * XMLArchiver.Archivable archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)  { return TextModelUtils.toXML(this, anArchiver); }

    /**
     * XMLArchiver.Archivable unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        TextModelUtils.fromXML(this, anArchiver, anElement);
        return this;
    }

    /**
     * Creates a default text model.
     */
    public static TextModel createDefaultTextModel()  { return new TextBlock(); }

    /**
     * Creates a default text model.
     */
    public static TextModel createDefaultTextModel(boolean isRich)  { return new TextBlock(isRich); }
}
