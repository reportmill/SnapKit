/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.props.PropChange;
import snap.util.*;

import java.util.Objects;

/**
 * This class represents a block of text (lines).
 */
public class RichText extends TextDoc implements XMLArchiver.Archivable {

    /**
     * Constructor.
     */
    public RichText()
    {
        super();

        // Set DefaultStyle, because RichText never inherits from parent
        _defaultTextStyle = TextStyle.DEFAULT;
    }

    /**
     * Creates RichText initialized with given String and attributes (font, color).
     */
    public RichText(CharSequence theChars, Object... theAttrs)
    {
        this();

        // Add attributes
        TextStyle style = getDefaultStyle().copyFor(theAttrs);
        addChars(theChars, style, 0);
    }

    /**
     * Override to indicate rich text supported.
     */
    @Override
    public boolean isRichText()  { return true; }

    /**
     * Creates a new TextLine for use in this text.
     */
    @Override
    protected TextLine createLine()  { return new RichTextLine(this); }

    /**
     * Sets a given style to a given range.
     */
    @Override
    public void setStyle(TextStyle aStyle, int aStart, int anEnd)
    {
        // Iterate over runs in range and set style
        for (int textCharIndex = aStart; textCharIndex < anEnd; ) {

            // Set style
            RichTextLine line = (RichTextLine) getLineForCharIndex(textCharIndex);
            int lineStart = line.getStartCharIndex();
            TextRun run = getRunForCharRange(textCharIndex, anEnd);

            // If run is larger than range, trim to size
            if (textCharIndex - lineStart > run.getStartCharIndex()) {
                int newRunStart = textCharIndex - lineStart - run.getStartCharIndex();
                run = line.splitRunForCharIndex(run, newRunStart);
            }
            if (anEnd - lineStart < run.getEndCharIndex()) {
                int newRunEnd = anEnd - lineStart - run.getStartCharIndex();
                line.splitRunForCharIndex(run, newRunEnd);
            }

            // Set style
            TextStyle oldStyle = run.getStyle();
            run.setStyle(aStyle);
            textCharIndex = run.getEndCharIndex() + lineStart;

            // Fire prop change
            if (isPropChangeEnabled()) {
                int runStart = run.getStartCharIndex() + lineStart;
                int runEnd = run.getEndCharIndex() + lineStart;
                PropChange pc = new TextDocUtils.StyleChange(this, oldStyle, aStyle, runStart, runEnd);
                firePropChange(pc);
            }
        }

        _width = -1;
    }

    /**
     * Sets a given attribute to a given value for a given range.
     */
    @Override
    public void setStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        // Iterate over lines in range and set attribute
        while (aStart < anEnd) {

            // Get run for range
            TextRun textRun = getRunForCharRange(aStart, anEnd);
            RichTextLine textLine = (RichTextLine) textRun.getLine();
            int lineStart = textLine.getStartCharIndex();
            int runEndInText = textRun.getEndCharIndex() + lineStart;
            int newStyleEndInText = Math.min(runEndInText, anEnd);

            // Get current run style, get new style for given key/value
            TextStyle style = textRun.getStyle();
            TextStyle newStyle = style.copyFor(aKey, aValue);

            // Set new style for run range
            setStyle(newStyle, aStart, newStyleEndInText);

            // Reset start to run end
            aStart = runEndInText;
        }
    }

    /**
     * Sets a given style to a given range.
     */
    @Override
    public void setLineStyle(TextLineStyle aStyle, int aStart, int anEnd)
    {
        // Handle MultiStyle
        int startLineIndex = getLineForCharIndex(aStart).getIndex();
        int endLineIndex = getLineForCharIndex(anEnd).getIndex();
        for (int i = startLineIndex; i <= endLineIndex; i++) {
            TextLine line = getLine(i);
            TextLineStyle oldStyle = line.getLineStyle();
            line.setLineStyle(aStyle);
            if (isPropChangeEnabled())
                firePropChange(new TextDocUtils.LineStyleChange(this, oldStyle, aStyle, i));
        }

        _width = -1;
    }

    /**
     * Sets a given style to a given range.
     */
    @Override
    public void setLineStyleValue(String aKey, Object aValue, int aStart, int anEnd)
    {
        // Handle MultiStyle
        int startLineIndex = getLineForCharIndex(aStart).getIndex();
        int endLineIndex = getLineForCharIndex(anEnd).getIndex();
        for (int i = startLineIndex; i <= endLineIndex; i++) {
            TextLine line = getLine(i);
            TextLineStyle oldStyle = line.getLineStyle();
            TextLineStyle newStyle = oldStyle.copyFor(aKey, aValue);
            line.setLineStyle(newStyle);
            if (isPropChangeEnabled())
                firePropChange(new TextDocUtils.LineStyleChange(this, oldStyle, newStyle, i));
        }

        _width = -1;
    }

    /**
     * Returns whether text contains an underlined run.
     */
    @Override
    public boolean isUnderlined()
    {
        for (TextLine line : _lines)
            if (line.isUnderlined())
                return true;
        return false;
    }

    /**
     * Returns a copy of this text for given char range.
     */
    public RichText copyForRange(int aStart, int aEnd)
    {
        // Create new RichText and iterate over lines in range to add copies for subrange
        RichText textCopy = new RichText();
        textCopy._lines.remove(0);

        // Get start/end line indexes
        int startLineIndex = getLineForCharIndex(aStart).getIndex();
        int endLineIndex = getLineForCharIndex(aEnd).getIndex();

        // Iterate over lines and add
        for (int i = startLineIndex; i <= endLineIndex; i++) {
            TextLine line = getLine(i);
            int lineStart = line.getStartCharIndex();
            int start = Math.max(aStart - lineStart, 0), end = Math.min(aEnd - lineStart, line.length());
            TextLine lineCopy = line.copyForRange(start, end);
            textCopy.addLine(lineCopy, textCopy.getLineCount());
        }

        // Return
        return textCopy;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public RichText clone()  { return (RichText) super.clone(); }

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
        for (TextLine line : getLines()) {
            for (int i = 0, iMax = line.getRunCount(); i < iMax; i++) {
                TextRun run = line.getRun(i);

                // If font changed for run, write font element
                if (!Objects.equals(font, run.getFont())) {
                    font = run.getFont();
                    e.add(anArchiver.toXML(font));
                }

                // If color changed for run, write color
                if (!Objects.equals(color, run.getColor())) {
                    color = run.getColor();
                    e.add(anArchiver.toXML(color));
                }

                // If format changed for run, write format
                if (!Objects.equals(format, run.getFormat())) {
                    format = run.getFormat();
                    if (format == null) e.add(new XMLElement("format"));
                    else e.add(anArchiver.toXML(format));
                }

                // If paragraph style changed for run, write paragraph
                if (!Objects.equals(lstyle, line.getLineStyle())) {
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
                if (!Objects.equals(border, run.getBorder())) {
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
}