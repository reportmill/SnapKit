/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
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
        setRichText(true);
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element named xstring
        XMLElement xml = new XMLElement("xstring");

        // Declare loop variables for text attributes: TextTyle, LineStyle, Font, Color, Format, Outline, Underline, Scripting, CS
        TextStyle textStyle = getDefaultStyle();
        TextLineStyle lineStyle = getDefaultLineStyle();
        Font font = textStyle.getFont();
        Color color = textStyle.getColor();
        TextFormat format = textStyle.getFormat();
        Border border = null;
        int scripting = 0;
        float charSpacing = 0;
        boolean underline = false;

        // Iterate over runs
        for (TextLine line : getLines()) {
            for (TextRun run : line.getRuns()) {

                // If font changed for run, write font element
                if (!Objects.equals(font, run.getFont())) {
                    font = run.getFont();
                    xml.add(anArchiver.toXML(font));
                }

                // If color changed for run, write color
                if (!Objects.equals(color, run.getColor())) {
                    color = run.getColor();
                    xml.add(anArchiver.toXML(color));
                }

                // If format changed for run, write format
                if (!Objects.equals(format, run.getFormat())) {
                    format = run.getFormat();
                    if (format == null) xml.add(new XMLElement("format"));
                    else xml.add(anArchiver.toXML(format));
                }

                // If paragraph style changed for run, write paragraph
                if (!Objects.equals(lineStyle, line.getLineStyle())) {
                    lineStyle = line.getLineStyle();
                    xml.add(anArchiver.toXML(lineStyle));
                }

                // If underline style changed, write underline
                if (underline != run.isUnderlined()) {
                    underline = run.isUnderlined();
                    xml.add(new XMLElement("underline"));
                    if (!underline) xml.get(xml.size() - 1).add("style", -1);
                }

                // If border changed, write border
                if (!Objects.equals(border, run.getBorder())) {
                    border = run.getBorder();
                    xml.add(new XMLElement("TextBorder"));
                    if (border != null) {
                        if (border.getWidth() != 1) xml.get(xml.size() - 1).add("stroke", border.getWidth());
                        if (border.getColor() != null)
                            xml.get(xml.size() - 1).add("color", "#" + border.getColor().toHexString());
                    } else xml.get(xml.size() - 1).add("off", true);
                }

                // If scripting changed, write scripting
                if (scripting != run.getScripting()) {
                    scripting = run.getScripting();
                    XMLElement se = new XMLElement("scripting");
                    if (scripting != 0) se.add("val", scripting);
                    xml.add(se);
                }

                // If char spacing changed, write char spacing
                if (charSpacing != run.getCharSpacing()) {
                    charSpacing = run.getCharSpacing();
                    XMLElement charSpacingXML = new XMLElement("char-spacing");
                    charSpacingXML.add("value", charSpacing);
                    xml.add(charSpacingXML);
                }

                // Write run string
                if (run.length() > 0)
                    xml.add(new XMLElement("string", run.getString()));
            }
        }

        // Return
        return xml;
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

        // Return
        return this;
    }
}