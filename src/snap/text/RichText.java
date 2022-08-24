/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.props.PropChange;
import snap.util.*;

/**
 * This class represents a block of text (lines).
 */
public class RichText extends TextDoc implements XMLArchiver.Archivable {

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
        while (aStart < anEnd) {

            // Set style
            RichTextLine line = (RichTextLine) getLineForCharIndex(aStart);
            int lineStart = line.getStart();
            TextRun run = getRunForCharIndex(aStart);
            TextStyle oldStyle = run.getStyle();
            if (aStart - lineStart > run.getStart())
                run = line.splitRunForCharIndex(run, aStart - lineStart - run.getStart());
            if (anEnd - lineStart < run.getEnd())
                line.splitRunForCharIndex(run, anEnd - lineStart - run.getStart());
            run.setStyle(aStyle);
            aStart = run.getEnd() + lineStart;

            // Fire prop change
            if (isPropChangeEnabled()) {
                int runStart = run.getStart() + lineStart, runEnd = run.getEnd() + lineStart;
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

            // Get line for start
            TextLine line = getLineForCharIndex(aStart);
            int lineStart = line.getStart();

            // Get run for start
            TextRun run = line.getRunForCharIndex(aStart - lineStart);
            int runEnd = run.getEnd();

            // Get run style and modify for given style key/value
            TextStyle style = run.getStyle().copyFor(aKey, aValue);
            setStyle(style, aStart, Math.min(runEnd + lineStart, anEnd));

            // Reset start to run end
            aStart = runEnd + lineStart;
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
            int lineStart = line.getStart();
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
}