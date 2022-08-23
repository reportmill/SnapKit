/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.*;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.geom.HPos;
import snap.props.PropChange;
import snap.props.PropChangeSupport;
import snap.util.*;

/**
 * This class represents a block of text (lines).
 */
public class RichText extends BaseText implements Cloneable, XMLArchiver.Archivable {

    /**
     * Constructor.
     */
    public RichText()
    {
        addLine(createLine(), 0);
    }

    /**
     * Creates RichText initialized with given String and attributes (font, color).
     */
    public RichText(CharSequence theChars, Object... theAttrs)
    {
        this();
        addCharsWithStyleValues(theChars, theAttrs);
    }

    /**
     * Sets the text to the given string.
     */
    public void setString(String aString)
    {
        setPropChangeEnabled(false);
        replaceChars(aString, null, 0, length());
        setPropChangeEnabled(true);
    }

    /**
     * Sets the default style.
     */
    public void setDefaultStyle(TextStyle aStyle)
    {
        super.setDefaultStyle(aStyle);
        for (BaseTextLine line : getLines())
            ((RichTextLine) line).setStyle(aStyle);
    }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle aLineStyle)
    {
        super.setDefaultLineStyle(aLineStyle);
        for (BaseTextLine line : getLines())
            line.setLineStyle(aLineStyle);
    }

    /**
     * Whether this text is really just plain text (has single font, color, etc.). Defaults to false.
     */
    public boolean isPlainText()  { return _plainText; }

    /**
     * Sets whether this text really just plain text (has single font, color, etc.).
     */
    public void setPlainText(boolean aValue)
    {
        _plainText = aValue;
    }

    /**
     * Adds characters with attributes to this text at given index.
     */
    public void addChars(CharSequence theChars)
    {
        addChars(theChars, null, length());
    }

    /**
     * Adds characters with attributes to this text at given index.
     */
    public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        // If no chars, just return
        if (theChars == null) return;

        // If monofont, clear attributes
        if (isPlainText()) theStyle = null;

        // Get line for index - if adding at text end and last line and ends with newline, create/add new line
        BaseTextLine line = getLineForCharIndex(anIndex);
        if (anIndex == line.getEnd() && line.isLastCharNewline()) {
            RichTextLine remainder = (RichTextLine) line.splitLineAtIndex(line.length());
            addLine(remainder, line.getIndex() + 1);
            line = remainder;
        }

        // Add chars line by line
        int start = 0;
        int len = theChars.length();
        int lindex = anIndex - line.getStart();
        while (start < len) {

            // Get index of newline in insertion chars (if there) and end of line block
            int newline = StringUtils.indexAfterNewline(theChars, start);
            int end = newline > 0 ? newline : len;

            // Get chars and add
            CharSequence chars = start == 0 && end == len ? theChars : theChars.subSequence(start, end);
            line.addChars(chars, theStyle, lindex);

            // If newline added and there are more chars in line, split line and add remainder
            if (newline > 0 && (end < len || lindex + chars.length() < line.length())) {
                RichTextLine remainder = (RichTextLine) line.splitLineAtIndex(lindex + chars.length());
                addLine(remainder, line.getIndex() + 1);
                line = remainder;
                lindex = 0;
            }

            // Set start to last end
            start = end;
        }

        // Send PropertyChange
        if (isPropChangeEnabled())
            firePropChange(new CharsChange(null, theChars, anIndex));
        _width = -1;
    }

    public void addCharsWithStyleMap(CharSequence theChars, Map<String, Object> theAttrs)
    {
        TextStyle style = getStyleForCharIndex(length());
        style = style.copyFor(theAttrs);
        addChars(theChars, style, length());
    }

    /**
     * Appends the given chars with the given attribute(s).
     */
    public void addCharsWithStyleValues(CharSequence theChars, Object... theAttrs)
    {
        // Get style at end and get first attribute
        TextStyle style = getStyleForCharIndex(length());
        Object attr0 = theAttrs != null && theAttrs.length > 0 ? theAttrs[0] : null;

        // Get modified style for given attributes
        if (attr0 instanceof TextStyle)
            style = (TextStyle) attr0;
        else if (attr0 != null)
            style = style.copyFor(theAttrs);

        // Add chars
        addChars(theChars, style, length());
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStart, int anEnd)
    {
        // If empty range, just return
        if (anEnd == aStart) return;

        // If PropChangeEnabled, get chars to be deleted
        CharSequence removedChars = isPropChangeEnabled() ? subSequence(aStart, anEnd) : null;

        // Delete lines/chars for range
        int end = anEnd;
        while (end > aStart) {

            // Get line at end index
            BaseTextLine line = getLineForCharIndex(end);
            if (end == line.getStart())
                line = getLine(line.getIndex() - 1);

            // Get Line.Start
            int lineStart = line.getStart();
            int start = Math.max(aStart, lineStart);

            // If whole line in range, remove line
            if (start == lineStart && end == line.getEnd() && getLineCount() > 1)
                removeLine(line.getIndex());

            // Otherwise remove chars (if no newline afterwards, join with next line)
            else {
                line.removeChars(start - lineStart, end - lineStart);
                if (!line.isLastCharNewline() && line.getIndex() + 1 < getLineCount()) {
                    BaseTextLine next = removeLine(line.getIndex() + 1);
                    line.appendLine(next);
                }
            }

            // Reset end
            end = lineStart;
        }

        // If deleted chars is set, send property change
        if (removedChars != null)
            firePropChange(new CharsChange(removedChars, null, aStart));
        _width = -1;
    }

    /**
     * Replaces chars in given range, with given String, using the given attributes.
     */
    public void replaceChars(CharSequence theChars, int aStart, int anEnd)
    {
        replaceChars(theChars, null, aStart, anEnd);
    }

    /**
     * Replaces chars in given range, with given String, using the given attributes.
     */
    public void replaceChars(CharSequence theChars, TextStyle theStyle, int aStart, int anEnd)
    {
        // Get style and linestyle for add chars
        TextStyle style = theStyle != null ? theStyle : getStyleForCharIndex(aStart);
        TextLineStyle lineStyle = theChars != null && theChars.length() > 0 && !isPlainText() ? getLineStyleForCharIndex(aStart) : null;

        // Remove given range and add chars
        if (anEnd > aStart)
            removeChars(aStart, anEnd);
        addChars(theChars, style, aStart);

        // Restore LineStyle (needed if range includes a newline)
        if (lineStyle != null)
            setLineStyle(lineStyle, aStart, aStart + theChars.length());
    }

    /**
     * Adds a RichText to this string at given index.
     */
    public void addText(RichText aRichText, int anIndex)
    {
        for (BaseTextLine line : aRichText.getLines()) {
            BaseTextRun[] lineRuns = line.getRuns();
            for (BaseTextRun run : lineRuns) {
                int index = anIndex + line.getStart() + run.getStart();
                addChars(run.getString(), run.getStyle(), index);
                setLineStyle(line.getLineStyle(), index, index + run.length());
            }
        }
    }

    /**
     * Replaces the chars in given range, with given RichText.
     */
    public void replaceText(RichText aRichText, int aStart, int anEnd)
    {
        if (anEnd > aStart)
            removeChars(aStart, anEnd);
        addText(aRichText, aStart);
    }

    /**
     * Sets a given style to a given range.
     */
    public void setStyle(TextStyle aStyle, int aStart, int anEnd)
    {
        // If single style, set style on all line runs
        if (isPlainText()) {
            TextStyle ostyle = getStyleForCharIndex(aStart);
            for (BaseTextLine line : _lines)
                ((RichTextLine) line).setStyle(aStyle);
            if (isPropChangeEnabled())
                firePropChange(new StyleChange(ostyle, aStyle, 0, length()));
        }

        // Iterate over runs in range and set style
        else while (aStart < anEnd) {
            RichTextLine line = (RichTextLine) getLineForCharIndex(aStart);
            int lineStart = line.getStart();
            BaseTextRun run = getRunForCharIndex(aStart);
            TextStyle ostyle = run.getStyle();
            if (aStart - lineStart > run.getStart())
                run = line.splitRunForCharIndex(run, aStart - lineStart - run.getStart());
            if (anEnd - lineStart < run.getEnd())
                line.splitRunForCharIndex(run, anEnd - lineStart - run.getStart());
            run.setStyle(aStyle);
            aStart = run.getEnd() + lineStart;
            if (isPropChangeEnabled())
                firePropChange(new StyleChange(ostyle, aStyle, run.getStart() + lineStart, run.getEnd() + lineStart));
        }

        _width = -1;
    }

    /**
     * Sets a given style value to given value for a given range.
     */
    public void setStyleValue(Object aValue)
    {
        setStyleValue(aValue, 0, length());
    }

    /**
     * Sets a given style value to given value for a given range.
     */
    public void setStyleValue(Object aValue, int aStart, int aEnd)
    {
        String key = TextStyle.getStyleKey(aValue);
        setStyleValue(key, aValue, aStart, aEnd);
    }

    /**
     * Sets a given attribute to a given value for a given range.
     */
    public void setStyleValue(String aKey, Object aValue)
    {
        setStyleValue(aKey, aValue, 0, length());
    }

    /**
     * Sets a given attribute to a given value for a given range.
     */
    public void setStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        // If not multifont, set attribute and invalidate everything
        if (isPlainText()) {
            TextStyle style = getStyleForCharIndex(aStart).copyFor(aKey, aValue);
            setStyle(style, aStart, anEnd);
        }

        // Iterate over lines in range and set attribute
        else while (aStart < anEnd) {
            BaseTextLine line = getLineForCharIndex(aStart);
            int lstart = line.getStart();
            BaseTextRun run = getRunForCharIndex(aStart);
            int rend = run.getEnd();
            TextStyle style = run.getStyle().copyFor(aKey, aValue);
            setStyle(style, aStart, Math.min(rend + lstart, anEnd));
            aStart = rend + lstart;
        }
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyle(TextLineStyle aStyle, int aStart, int anEnd)
    {
        // Handle PlainText
        if (isPlainText()) {
            TextLineStyle oldStyle = getLine(0).getLineStyle();
            for (BaseTextLine line : getLines())
                line.setLineStyle(aStyle);
            if (isPropChangeEnabled())
                firePropChange(new LineStyleChange(oldStyle, aStyle, 0));
        }

        // Handle MultiStyle
        else {
            int sline = getLineForCharIndex(aStart).getIndex(), eline = getLineForCharIndex(anEnd).getIndex();
            for (int i = sline; i <= eline; i++) {
                BaseTextLine line = getLine(i);
                TextLineStyle ostyle = line.getLineStyle();
                line.setLineStyle(aStyle);
                if (isPropChangeEnabled())
                    firePropChange(new LineStyleChange(ostyle, aStyle, i));
            }
        }

        _width = -1;
    }

    /**
     * Sets a given style to a given range.
     */
    public void setLineStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        // Handle PlainText
        if (isPlainText()) {
            TextLineStyle ostyle = getLine(0).getLineStyle();
            TextLineStyle nstyle = ostyle.copyFor(aKey, aValue);
            setLineStyle(nstyle, 0, length());
        }

        // Handle MultiStyle
        else {
            int sline = getLineForCharIndex(aStart).getIndex(), eline = getLineForCharIndex(anEnd).getIndex();
            for (int i = sline; i <= eline; i++) {
                BaseTextLine line = getLine(i);
                TextLineStyle oldStyle = line.getLineStyle(), nstyle = oldStyle.copyFor(aKey, aValue);
                line.setLineStyle(nstyle);
                if (isPropChangeEnabled())
                    firePropChange(new LineStyleChange(oldStyle, nstyle, i));
            }
            _width = -1;
        }
    }

    /**
     * Clears the text.
     */
    public void clear()
    {
        removeChars(0, length());
        setStyle(getDefaultStyle(), 0, 0);
        setLineStyle(getDefaultLineStyle(), 0, 0);
    }

    /**
     * Returns the individual block in this doc.
     */
    @Override
    public RichTextLine getLine(int anIndex)
    {
        return (RichTextLine) super.getLine(anIndex);
    }

    /**
     * Creates a new block for use in this text.
     */
    protected RichTextLine createLine()
    {
        return new RichTextLine(this);
    }

    /**
     * Returns whether text contains an underlined run.
     */
    public boolean isUnderlined()
    {
        if (isPlainText())
            return getStyleForCharIndex(0).isUnderlined();
        for (BaseTextLine line : _lines)
            if (line.isUnderlined())
                return true;
        return false;
    }

    /**
     * Sets the RichText to be underlined.
     */
    public void setUnderlined(boolean aFlag)
    {
        setStyleValue(TextStyle.UNDERLINE_KEY, aFlag ? 1 : null, 0, length());
    }

    /**
     * Returns the horizontal alignment of the first paragraph of the RichText.
     */
    public HPos getAlignX()
    {
        return getLineStyleForCharIndex(0).getAlign();
    }

    /**
     * Sets the horizontal alignment of the xstring.
     */
    public void setAlignX(HPos anAlignX)
    {
        setLineStyleValue(TextLineStyle.ALIGN_KEY, anAlignX, 0, length());
    }

    /**
     * Scales all the fonts in text by given factor.
     */
    public void scaleFonts(double aScale)
    {
        if (aScale == 1) return;
        for (BaseTextLine line : getLines()) {
            int lstart = line.getStart();
            for (BaseTextRun run : line.getRuns()) {
                int rstrt = run.getStart(), rend = run.getEnd();
                setStyle(run.getStyle().copyFor(run.getFont().scaleFont(aScale)), lstart + rstrt, lstart + rend);
            }
        }
    }

    /**
     * Returns an RichText for given char range.
     */
    public RichText subtext(int aStart, int aEnd)
    {
        // Create new RichText and iterate over lines in range to add copies for subrange
        RichText rtext = new RichText();
        rtext._lines.remove(0);
        int sline = getLineForCharIndex(aStart).getIndex(), eline = getLineForCharIndex(aEnd).getIndex();
        for (int i = sline; i <= eline; i++) {
            RichTextLine line = getLine(i);
            int lstart = line.getStart();
            int start = Math.max(aStart - lstart, 0), end = Math.min(aEnd - lstart, line.length());
            RichTextLine lcopy = line.subline(start, end);
            rtext.addLine(lcopy, rtext.getLineCount());
        }

        // Return rtext
        return rtext;
    }

    /**
     * Standard clone implementation.
     */
    public RichText clone()
    {
        // Do normal clone
        RichText clone;
        try { clone = (RichText) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }

        // Reset lines array and length
        clone._lines = new ArrayList<>(getLineCount());
        clone._length = 0;

        // Copy lines deep
        for (int i = 0, iMax = getLineCount(); i < iMax; i++) {
            RichTextLine line = getLine(i);
            RichTextLine lineClone = line.clone();
            clone.addLine(lineClone, i);
        }

        // Reset PropChangeSupport and return
        clone._pcs = PropChangeSupport.EMPTY;
        return clone;
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element named xstring
        XMLElement e = new XMLElement("xstring");

        // Declare loop variable for xstring attributes: Font, Color, Paragraph, Format, Outline, Underline, Scripting, CS
        TextStyle style = getDefaultStyle();
        TextLineStyle lstyle = getDefaultLineStyle();
        Font font = style.getFont();
        Color color = style.getColor();
        TextFormat format = style.getFormat();
        Border border = null; //RMParagraph pgraph = getDefaultParagraph();
        int scripting = 0;
        float charSpacing = 0;
        boolean underline = false;

        // Iterate over runs
        for (BaseTextLine line : getLines()) {
            for (int i = 0, iMax = line.getRunCount(); i < iMax; i++) {
                BaseTextRun run = line.getRun(i);

                // If font changed for run, write font element
                if (!SnapUtils.equals(font, run.getFont())) {
                    font = run.getFont();
                    e.add(anArchiver.toXML(font));
                }

                // If color changed for run, write color
                if (!SnapUtils.equals(color, run.getColor())) {
                    color = run.getColor();
                    e.add(anArchiver.toXML(color));
                }

                // If format changed for run, write format
                if (!SnapUtils.equals(format, run.getFormat())) {
                    format = run.getFormat();
                    if (format == null) e.add(new XMLElement("format"));
                    else e.add(anArchiver.toXML(format));
                }

                // If paragraph style changed for run, write paragraph
                if (!SnapUtils.equals(lstyle, line.getLineStyle())) {
                    lstyle = line.getLineStyle();
                    e.add(anArchiver.toXML(lstyle));
                }

                // If underline style changed, write underline
                if (underline != run.isUnderlined()) {
                    underline = run.isUnderlined();
                    e.add(new XMLElement("underline"));
                    if (!underline) e.get(e.size() - 1).add("style", -1);
                }

                // If border changed, write border
                if (!SnapUtils.equals(border, run.getBorder())) {
                    border = run.getBorder();
                    e.add(new XMLElement("TextBorder"));
                    if (border != null) {
                        if (border.getWidth() != 1) e.get(e.size() - 1).add("stroke", border.getWidth());
                        if (border.getColor() != null)
                            e.get(e.size() - 1).add("color", "#" + border.getColor().toHexString());
                    } else e.get(e.size() - 1).add("off", true);
                }

                // If scripting changed, write scripting
                if (scripting != run.getScripting()) {
                    scripting = run.getScripting();
                    XMLElement se = new XMLElement("scripting");
                    if (scripting != 0) se.add("val", scripting);
                    e.add(se);
                }

                // If char spacing changed, write char spacing
                if (charSpacing != run.getCharSpacing()) {
                    charSpacing = run.getCharSpacing();
                    XMLElement charSpacingXML = new XMLElement("char-spacing");
                    charSpacingXML.add("value", charSpacing);
                    e.add(charSpacingXML);
                }

                // Write run string
                if (run.length() > 0)
                    e.add(new XMLElement("string", run.getString()));
            }
        }

        // Return xml element
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Get map for run attributes
        TextStyle style = getDefaultStyle();
        TextLineStyle lstyle = null;

        // Iterate over child elements to snag common attributes
        for (int i = 0, iMax = anElement.size(); i < iMax; i++) {
            XMLElement e = anElement.get(i);

            // Unarchive string
            if (e.getName().equals("string")) {
                String str = e.getValue();
                if (str == null || str.length() == 0) continue;
                int len = length();
                addChars(str, style, len);
                if (lstyle != null) {
                    setLineStyle(lstyle, len, len + str.length());
                    lstyle = null;
                }
            }

            // Unarchive font element
            else if (e.getName().equals("font")) {
                Font font = anArchiver.fromXML(e, Font.class, null);
                style = style.copyFor(font);
            }

            // Unarchive color element
            else if (e.getName().equals("color")) {
                Color color = anArchiver.fromXML(e, Color.class, null);
                style = style.copyFor(color);
            }

            // If format changed for segment, write format
            else if (e.getName().equals("format")) {
                Object fmt = anArchiver.fromXML(e, null);
                style = style.copyFor(TextStyle.FORMAT_KEY, fmt);
            }

            // Unarchive pgraph element
            else if (e.getName().equals("pgraph"))
                lstyle = anArchiver.fromXML(e, TextLineStyle.class, null);

                // Unarchive underline element
            else if (e.getName().equals("underline")) {
                if (e.getAttributeIntValue("style") < 0)
                    style = style.copyFor(TextStyle.UNDERLINE_KEY, null);
                else style = style.copyFor(TextStyle.UNDERLINE_KEY, 1);
            }

            // Unarchive outline element
            else if (e.getName().equals("outline")) {
                if (e.getAttributeBoolValue("off"))
                    style = style.copyFor(TextStyle.BORDER_KEY, null);
                else {
                    double swidth = e.getAttributeFloatValue("stroke", 1);
                    String cstr = e.getAttributeValue("color");
                    Color color = Color.get(cstr);
                    Border border = Border.createLineBorder(style.getColor(), swidth);
                    style = style.copyFor(border);
                    style = style.copyFor(color);
                }
            }

            // Unarchive outline element
            else if (e.getName().equals("TextBorder")) {
                double stroke = e.getAttributeFloatValue("stroke", 1);
                String cstr = e.getAttributeValue("color");
                Color color = Color.get(cstr);
                Border border = Border.createLineBorder(color, stroke);
                style = style.copyFor(border);
            }

            // Unarchive scripting
            else if (e.getName().equals("scripting")) {
                int scripting = e.getAttributeIntValue("val");
                style = style.copyFor(TextStyle.SCRIPTING_KEY, scripting);
            }

            // Unarchive char spacing
            else if (e.getName().equals("char-spacing")) {
                double cspace = e.getAttributeFloatValue("value");
                style = style.copyFor(TextStyle.CHAR_SPACING_KEY, cspace);
            }
        }

        // If no string was read, apply attributes anyway
        if (length() == 0)
            getLine(0).getRun(0).setStyle(style);

        // Return this xstring
        return this;
    }

    /**
     * A property change event for addChars/removeChars.
     */
    public class CharsChange extends PropChange {
        public CharsChange(Object oldV, Object newV, int anInd)
        {
            super(RichText.this, Chars_Prop, oldV, newV, anInd);
        }

        public CharSequence getOldValue()
        {
            return (CharSequence) super.getOldValue();
        }

        public CharSequence getNewValue()
        {
            return (CharSequence) super.getNewValue();
        }

        public void doChange(Object oldValue, Object newValue)
        {
            if (oldValue != null)
                removeChars(getIndex(), getIndex() + ((CharSequence) oldValue).length());
            else addChars((CharSequence) newValue, null, getIndex());
        }

        public PropChange merge(PropChange anEvent)
        {
            CharsChange event = (CharsChange) anEvent;
            if (getNewValue() != null && event.getNewValue() != null && getNewValue().length() + getIndex() == event.getIndex())
                return new CharsChange(null, getNewValue().toString() + event.getNewValue(), getIndex());
            return null;
        }
    }

    /**
     * A property change event for RMXStringRun.Style change.
     */
    public class StyleChange extends PropChange {
        int _start, _end;

        public StyleChange(Object oV, Object nV, int aStart, int anEnd)
        {
            super(RichText.this, Style_Prop, oV, nV, -1);
            _start = aStart;
            _end = anEnd;
        }

        public int getStart()  { return _start; }

        public int getEnd()  { return _end; }

        public void doChange(Object oldVal, Object newVal)
        {
            setStyle((TextStyle) newVal, _start, _end);
        }
    }

    /**
     * A property change event for RMXStringRun.Style change.
     */
    public class LineStyleChange extends PropChange {
        public LineStyleChange(Object oV, Object nV, int anIndex)
        {
            super(RichText.this, LineStyle_Prop, oV, nV, anIndex);
        }

        public void doChange(Object oval, Object nval)
        {
            BaseTextLine line = getLine(getIndex());
            setLineStyle((TextLineStyle) nval, line.getStart(), line.getStart());
        }
    }
}