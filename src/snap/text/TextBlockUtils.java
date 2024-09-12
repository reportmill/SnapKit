package snap.text;
import snap.geom.Path2D;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.props.PropChange;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility methods to support TextBlock.
 */
public class TextBlockUtils {

    /**
     * Returns a path for two char indexes - it will be a simple box with extensions for first/last lines.
     */
    public static Shape getPathForCharRange(TextBlock textBlock, int aStartCharIndex, int aEndCharIndex)
    {
        // Create new path for return
        Path2D path = new Path2D();

        // If invalid range, just return
        if (aStartCharIndex > textBlock.getEndCharIndex()) // || aEndCharIndex < textBlock.getStartCharIndex())
            return path;
        if (aEndCharIndex > textBlock.getEndCharIndex())
            aEndCharIndex = textBlock.getEndCharIndex();

        // Get StartLine for start char index (maybe adjust if at ambiguous start/end of lines and mouse Y available)
        TextLine startLine = textBlock.getLineForCharIndex(aStartCharIndex);
        if (aStartCharIndex == aEndCharIndex && aStartCharIndex == startLine.getStartCharIndex() && textBlock._mouseY > 0) {
            TextLine altStartLine = textBlock.getLineForY(textBlock._mouseY);
            if (altStartLine == startLine.getPrevious())
                startLine = altStartLine;
        }

        // Get end line
        TextLine endLine = aStartCharIndex == aEndCharIndex ? startLine : textBlock.getLineForCharIndex(aEndCharIndex);
        double startX = startLine.getTextXForCharIndex(aStartCharIndex - startLine.getStartCharIndex());
        double startY = startLine.getTextBaseline();
        double endX = endLine.getTextXForCharIndex(aEndCharIndex - endLine.getStartCharIndex());
        double endY = endLine.getTextBaseline();
        startX = Math.min(startX, textBlock.getMaxX());
        endX = Math.min(endX, textBlock.getMaxX());

        // Get start top/height
        double startTop = startLine.getTextY() - 1;
        double startHeight = startLine.getHeight() + 2;

        // Get path for upper left corner of sel start
        path.moveTo(startX, startTop + startHeight);
        path.lineTo(startX, startTop);
        if (aStartCharIndex == aEndCharIndex)
            return path;

        // If selection spans more than one line, add path components for middle lines and end line
        if (startY != endY) {  //!SnapMath.equals(startY, endY)
            double endTop = endLine.getTextY() - 1;
            double endHeight = endLine.getHeight() + 2;
            path.lineTo(textBlock.getWidth(), startTop);
            path.lineTo(textBlock.getWidth(), endTop);
            path.lineTo(endX, endTop);
            path.lineTo(endX, endTop + endHeight);
            path.lineTo(textBlock.getX(), endTop + endHeight);
            path.lineTo(textBlock.getX(), startTop + startHeight);
        }

        // If selection spans only one line, add path components for upper-right, lower-right
        else {
            path.lineTo(endX, startTop);
            path.lineTo(endX, startTop + startHeight);
        }

        // Close path and return
        path.close();
        return path;
    }

    /**
     * Returns underlined runs for text box.
     */
    public static TextRun[] getUnderlineRuns(TextBlock textBlock, Rect aRect)
    {
        // Get lines
        List<TextLine> textLines = textBlock.getLines();
        List<TextRun> underlineRuns = new ArrayList<>();

        // Iterate over lines to add underline runs to list
        for (TextLine line : textLines) {

            // If line above rect, continue, if below, break
            if (aRect != null) {
                if (line.getMaxY() < aRect.y) continue;
                else if (line.getY() >= aRect.getMaxY())
                    break;
            }

            // If run underlined, add to list
            for (TextRun run : line.getRuns())
                if (run.getTextStyle().isUnderlined())
                    underlineRuns.add(run);
        }

        // Return
        return underlineRuns.toArray(new TextRun[0]);
    }

    /**
     * Paint TextBox to given painter.
     */
    public static void paintTextBlockUnderlines(Painter aPntr, TextBlock textBlock, Rect clipRect)
    {
        TextRun[] underlineRuns = getUnderlineRuns(textBlock, clipRect);

        for (TextRun run : underlineRuns) {

            // Set underline color and width
            TextLine textLine = run.getLine();
            Font font = run.getFont();
            double underlineOffset = Math.ceil(Math.abs(font.getUnderlineOffset()));
            double underlineThickness = font.getUnderlineThickness();
            aPntr.setColor(run.getColor());
            aPntr.setStrokeWidth(underlineThickness);

            // Get underline endpoints and draw line
            double lineX = textLine.getTextX() + run.getX();
            double lineMaxX = lineX + run.getWidth() - run.getTrailingWhitespaceWidth();
            double lineY = textLine.getTextBaseline() + underlineOffset;
            aPntr.drawLine(lineX, lineY, lineMaxX, lineY);
        }
    }

    /**
     * Returns the tokens.
     */
    public static TextToken[] createTokensForTextLine(TextLine aTextLine)
    {
        // Loop vars
        List<TextToken> tokens = new ArrayList<>();
        int tokenStart = 0;
        int lineLength = aTextLine.length();

        // Get Run info
        TextRun textRun = aTextLine.getRun(0);
        int textRunEnd = textRun.getEndCharIndex();

        // Iterate over line chars
        while (tokenStart < lineLength) {

            // Find token start: Skip past whitespace
            while (tokenStart < textRunEnd && Character.isWhitespace(aTextLine.charAt(tokenStart)))
                tokenStart++;

            // Find token end: Skip to first non-whitespace char
            int tokenEnd = tokenStart;
            while (tokenEnd < textRunEnd && !Character.isWhitespace(aTextLine.charAt(tokenEnd)))
                tokenEnd++;

            // If chars found, create/add token
            if (tokenStart < tokenEnd) {
                TextToken token = new TextToken(aTextLine, tokenStart, tokenEnd, textRun.getTextStyle());
                tokens.add(token);
                tokenStart = tokenEnd;
            }

            // If at RunEnd but not LineEnd, update Run info with next run
            if (tokenStart == textRunEnd && tokenStart < lineLength) {
                textRun = textRun.getNext();
                textRunEnd = textRun.getEndCharIndex();
            }
        }

        // Return
        return tokens.toArray(new TextToken[0]);
    }

    /**
     * Sets the Mouse Y for given text block to assist in caret placement (can be ambiguous for start/end of line).
     */
    public static void setMouseY(TextBlock textBlock, double aY)  { textBlock._mouseY = aY; }

    /**
     * XML archival.
     */
    public static XMLElement toXML(TextBlock textBlock, XMLArchiver anArchiver)
    {
        // Get new element named xstring
        XMLElement xml = new XMLElement("xstring");

        // Declare loop variables for text attributes: TextTyle, LineStyle, Font, Color, Format, Outline, Underline, Scripting, CS
        TextStyle textStyle = textBlock.getDefaultTextStyle();
        TextLineStyle lineStyle = textBlock.getDefaultLineStyle();
        Font font = textStyle.getFont();
        Color color = textStyle.getColor();
        TextFormat format = textStyle.getFormat();
        Border border = null;
        int scripting = 0;
        double charSpacing = 0;
        boolean underline = false;

        // Iterate over runs
        for (TextLine line : textBlock.getLines()) {
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
    public static void fromXML(TextBlock textBlock, XMLArchiver anArchiver, XMLElement anElement)
    {
        // Get map for run attributes
        TextStyle style = textBlock.getDefaultTextStyle();
        TextLineStyle lineStyle = null;

        // Iterate over child elements to snag common attributes
        for (int i = 0, iMax = anElement.size(); i < iMax; i++) {
            XMLElement e = anElement.get(i);

            // Unarchive string
            if (e.getName().equals("string")) {
                String str = e.getValue();
                if (str == null || str.isEmpty()) continue;
                int len = textBlock.length();
                textBlock.addCharsWithStyle(str, style);
                if (lineStyle != null) {
                    textBlock.setLineStyle(lineStyle, len, len + str.length());
                    lineStyle = null;
                }
            }

            // Unarchive font element
            else if (e.getName().equals("font")) {
                Font font = anArchiver.fromXML(e, Font.class, null);
                style = style.copyForStyleValue(font);
            }

            // Unarchive color element
            else if (e.getName().equals("color")) {
                Color color = anArchiver.fromXML(e, Color.class, null);
                style = style.copyForStyleValue(color);
            }

            // If format changed for segment, write format
            else if (e.getName().equals("format")) {
                Object fmt = anArchiver.fromXML(e, null);
                style = style.copyForStyleKeyValue(TextStyle.Format_Prop, fmt);
            }

            // Unarchive pgraph element
            else if (e.getName().equals("pgraph"))
                lineStyle = anArchiver.fromXML(e, TextLineStyle.class, null);

                // Unarchive underline element
            else if (e.getName().equals("underline")) {
                if (e.getAttributeIntValue("style") < 0)
                    style = style.copyForStyleKeyValue(TextStyle.Underline_Prop, null);
                else style = style.copyForStyleKeyValue(TextStyle.Underline_Prop, 1);
            }

            // Unarchive outline element
            else if (e.getName().equals("outline")) {
                if (e.getAttributeBoolValue("off"))
                    style = style.copyForStyleKeyValue(TextStyle.Border_Prop, null);
                else {
                    double swidth = e.getAttributeFloatValue("stroke", 1);
                    String cstr = e.getAttributeValue("color");
                    Color color = Color.get(cstr);
                    Border border = Border.createLineBorder(style.getColor(), swidth);
                    style = style.copyForStyleValue(border);
                    style = style.copyForStyleValue(color);
                }
            }

            // Unarchive outline element
            else if (e.getName().equals("TextBorder")) {
                double stroke = e.getAttributeFloatValue("stroke", 1);
                String cstr = e.getAttributeValue("color");
                Color color = Color.get(cstr);
                Border border = Border.createLineBorder(color, stroke);
                style = style.copyForStyleValue(border);
            }

            // Unarchive scripting
            else if (e.getName().equals("scripting")) {
                int scripting = e.getAttributeIntValue("val");
                style = style.copyForStyleKeyValue(TextStyle.Scripting_Prop, scripting);
            }

            // Unarchive char spacing
            else if (e.getName().equals("char-spacing")) {
                double cspace = e.getAttributeFloatValue("value");
                style = style.copyForStyleKeyValue(TextStyle.CharSpacing_Prop, cspace);
            }
        }

        // If no string was read, apply attributes anyway
        if (textBlock.length() == 0)
            textBlock.getLine(0).getRun(0).setTextStyle(style);
    }

    /**
     * A property change event for addChars/removeChars.
     */
    public static class CharsChange extends PropChange {

        /** Constructor. */
        public CharsChange(TextBlock aTextBlock, Object oldV, Object newV, int anInd)
        {
            super(aTextBlock, TextBlock.Chars_Prop, oldV, newV, anInd);
        }

        public CharSequence getOldValue()  { return (CharSequence) super.getOldValue(); }

        public CharSequence getNewValue()  { return (CharSequence) super.getNewValue(); }

        public void doChange(Object oldValue, Object newValue)
        {
            TextBlock textBlock = (TextBlock) getSource();
            int index = getIndex();

            if (oldValue != null)
                textBlock.removeChars(index, index + ((CharSequence) oldValue).length());
            else textBlock.addChars((CharSequence) newValue, index);
        }

        public PropChange merge(PropChange anEvent)
        {
            TextBlock textBlock = (TextBlock) getSource();
            CharsChange event = (CharsChange) anEvent;
            CharSequence newVal = getNewValue();
            CharSequence eventNewVal = event.getNewValue();
            int index = getIndex();

            if (newVal != null && eventNewVal != null && newVal.length() + index == event.getIndex())
                return new CharsChange(textBlock,null, newVal.toString() + eventNewVal, index);
            return null;
        }
    }

    /**
     * A property change event for TextBlock.Style change.
     */
    public static class StyleChange extends PropChange {

        // Ivars
        int _start, _end;

        /** Constructor. */
        public StyleChange(TextBlock aTextBlock, Object oV, Object nV, int aStart, int anEnd)
        {
            super(aTextBlock, TextBlock.Style_Prop, oV, nV, -1);
            _start = aStart;
            _end = anEnd;
        }

        public int getStart()  { return _start; }

        public int getEnd()  { return _end; }

        public void doChange(Object oldVal, Object newVal)
        {
            TextBlock textBlock = (TextBlock) getSource();
            textBlock.setTextStyle((TextStyle) newVal, _start, _end);
        }
    }

    /**
     * A property change event for TextBlock.Style change.
     */
    public static class LineStyleChange extends PropChange {

        /** Constructor. */
        public LineStyleChange(TextBlock aTextBlock, Object oV, Object nV, int anIndex)
        {
            super(aTextBlock, TextBlock.LineStyle_Prop, oV, nV, anIndex);
        }

        public void doChange(Object oval, Object nval)
        {
            TextBlock textBlock = (TextBlock) getSource();
            TextLine line = textBlock.getLine(getIndex());
            textBlock.setLineStyle((TextLineStyle) nval, line.getStartCharIndex(), line.getStartCharIndex());
        }
    }
}
