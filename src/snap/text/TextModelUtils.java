package snap.text;
import snap.geom.HPos;
import snap.geom.Path2D;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.props.PropChange;
import snap.util.XMLAttribute;
import snap.util.XMLElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods to support TextModel.
 */
public class TextModelUtils {

    /**
     * Returns a path for two char indexes - it will be a simple box with extensions for first/last lines.
     */
    public static Shape getPathForCharRange(TextLayout textLayout, int aStartCharIndex, int aEndCharIndex)
    {
        // Create new path for return
        Path2D path = new Path2D();

        // If invalid range, just return
        if (aStartCharIndex > textLayout.getEndCharIndex()) // || aEndCharIndex < textModel.getStartCharIndex())
            return path;
        if (aEndCharIndex > textLayout.getEndCharIndex())
            aEndCharIndex = textLayout.getEndCharIndex();

        // Get StartLine for start char index (maybe adjust if at ambiguous start/end of lines and mouse Y available)
        TextLine startLine = textLayout.getLineForCharIndex(aStartCharIndex);
        if (aStartCharIndex == aEndCharIndex && aStartCharIndex == startLine.getStartCharIndex() && textLayout.getTextModel()._mouseY > 0) {
            TextLine altStartLine = textLayout.getLineForY(textLayout.getTextModel()._mouseY);
            if (altStartLine == startLine.getPrevious())
                startLine = altStartLine;
        }

        // Get end line
        TextLine endLine = aStartCharIndex == aEndCharIndex ? startLine : textLayout.getLineForCharIndex(aEndCharIndex);
        double startX = startLine.getTextXForCharIndex(aStartCharIndex - startLine.getStartCharIndex());
        double startY = startLine.getTextBaseline();
        double endX = endLine.getTextXForCharIndex(aEndCharIndex - endLine.getStartCharIndex());
        double endY = endLine.getTextBaseline();
        startX = Math.min(startX, textLayout.getMaxX());
        endX = Math.min(endX, textLayout.getMaxX());

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
            path.lineTo(textLayout.getWidth(), startTop);
            path.lineTo(textLayout.getWidth(), endTop);
            path.lineTo(endX, endTop);
            path.lineTo(endX, endTop + endHeight);
            path.lineTo(textLayout.getX(), endTop + endHeight);
            path.lineTo(textLayout.getX(), startTop + startHeight);
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
     * Sets the Mouse Y for given text model to assist in caret placement (can be ambiguous for start/end of line).
     */
    public static void setMouseY(TextLayout textLayout, double aY)
    {
        TextModel textModel = textLayout.getTextModel();
        textModel._mouseY = aY;
    }

    /**
     * This method returns the range of the @-sign delinated key closest to the current selection (or null if not found).
     */
    public static TextSel smartFindFormatRange(TextLayout textLayout, int selStart, int selEnd)
    {
        String string = textLayout.getString();
        int prevAtSignIndex = -1;
        int nextAtSignIndex = -1;

        // See if selection contains an '@'
        if (selEnd > selStart)
            prevAtSignIndex = string.indexOf("@", selStart);
        if (prevAtSignIndex >= selEnd)
            prevAtSignIndex = -1;

        // If there wasn't an '@' in selection, see if there is one before the selected range
        if (prevAtSignIndex < 0)
            prevAtSignIndex = string.lastIndexOf("@", selStart - 1);

        // If there wasn't an '@' in or before selection, see if there is one after the selected range
        if (prevAtSignIndex < 0)
            prevAtSignIndex = string.indexOf("@", selEnd);

        // If there is a '@' in, before or after selection, see if there is another after it
        if (prevAtSignIndex >= 0)
            nextAtSignIndex = string.indexOf("@", prevAtSignIndex + 1);

        // If there is a '@' in, before or after selection, but not one after it, see if there is one before that
        if (prevAtSignIndex >= 0 && nextAtSignIndex < 0)
            nextAtSignIndex = string.lastIndexOf("@", prevAtSignIndex - 1);

        // If both a previous and next '@', select the chars inbetween
        if (prevAtSignIndex >= 0 && nextAtSignIndex >= 0 && prevAtSignIndex != nextAtSignIndex) {
            int start = Math.min(prevAtSignIndex, nextAtSignIndex);
            int end = Math.max(prevAtSignIndex, nextAtSignIndex);
            return new TextSel(textLayout, start, end + 1);
        }

        // Return null since range not found
        return null;
    }

    /**
     * TextModel archival.
     */
    public static XMLElement textModelToXML(TextModel textModel)
    {
        XMLElement xml = new XMLElement("TextModel");
        TextStyle lastTextStyle = textModel.getDefaultTextStyle();
        TextLineStyle lastLineStyle = textModel.getDefaultLineStyle();

        // Iterate over runs
        for (TextLine line : textModel.getLines()) {
            for (TextRun run : line.getRuns()) {

                // If text style changed, write style element
                TextStyle textStyle = run.getTextStyle();
                if (!textStyle.equals(lastTextStyle)) {
                    xml.add(textStyleToXML(textStyle, lastTextStyle));
                    lastTextStyle = textStyle;
                }

                // If line style changed, write line style
                TextLineStyle lineStyle = line.getLineStyle();
                if (!lineStyle.equals(lastLineStyle)) {
                    xml.add(lineStyleToXML(lineStyle, lastLineStyle));
                    lastLineStyle = lineStyle;
                }

                // Write run string
                if (!run.isEmpty())
                    xml.add(new XMLElement("String", run.getString()));
            }
        }

        // Return
        return xml;
    }

    /**
     * TextModel unarchival.
     */
    public static TextModel textModelFromXML(XMLElement anElement)
    {
        TextModel textModel = TextModel.createDefaultTextModel(true);
        TextStyle textStyle = textModel.getDefaultTextStyle();
        TextLineStyle lineStyle = textModel.getDefaultLineStyle();
        boolean lineStyleChanged = false;

        // Iterate over child elements to snag common attributes
        for (XMLElement e : anElement.getElements()) {

            switch (e.getName()) {

                // Unarchive string
                case "String" -> {
                    String str = e.getValue();
                    if (str == null || str.isEmpty()) continue;
                    int len = textModel.length();
                    textModel.addCharsWithStyle(str, textStyle);
                    if (lineStyleChanged) {
                        textModel.setLineStyle(lineStyle, len, len + str.length());
                        lineStyleChanged = false;
                    }
                }

                // Unarchive text style element
                case "Style" -> textStyle = textStyleFromXML(e, textStyle);

                // Unarchive line style element
                case "LineStyle" -> {
                    lineStyle = lineStyleFromXML(e, lineStyle);
                    lineStyleChanged = true;
                }
            }
        }

        // If no string was read, apply attributes anyway
        if (textModel.isEmpty())
            textModel.getLine(0).getRun(0).setTextStyle(textStyle);

        return textModel;
    }

    /**
     * TextStyle archival (Font, Color, Underline, CharSpacing, Scripting, Format, Border, Link).
     */
    private static XMLElement textStyleToXML(TextStyle textStyle, TextStyle referenceStyle)
    {
        XMLElement xml = new XMLElement("Style");
        if (!textStyle.getFont().equals(referenceStyle.getFont()))
            xml.add("Font", textStyle.getFont().codeString());
        if (!textStyle.getColor().equals(referenceStyle.getColor()))
            xml.add("TextColor", textStyle.getColor().codeString());
        if (textStyle.isUnderlined() != referenceStyle.isUnderlined())
            xml.add("Underline", Boolean.toString(textStyle.isUnderlined()));
        if (textStyle.getCharSpacing() != referenceStyle.getCharSpacing())
            xml.add("CharSpacing", textStyle.getCharSpacing());
        if (textStyle.getScripting() != referenceStyle.getScripting())
            xml.add("Scripting", textStyle.getScripting());
        return xml;
    }

    /**
     * TextStyle unarchival.
     */
    private static TextStyle textStyleFromXML(XMLElement xml, TextStyle referenceStyle)
    {
        TextStyle textStyle = referenceStyle;
        for (XMLAttribute attribute : xml.getAttributes()) {
            switch (attribute.getName()) {
                case "Font" -> {
                    Font font = Font.of(attribute.getValue());
                    textStyle = textStyle.copyForStyleKeyValue(TextStyle.Font_Prop, font);
                }
                case "TextColor" -> {
                    Color color = Color.get(attribute.getValue());
                    textStyle = textStyle.copyForStyleKeyValue(TextStyle.Color_Prop, color);
                }
                case "Underline" -> {
                    boolean underline = attribute.getValue().equals("true");
                    textStyle = textStyle.copyForStyleKeyValue(TextStyle.Underline_Prop, underline ? 1 : 0);
                }
                case "CharSpacing" -> {
                    double charSpacing = attribute.getDoubleValue();
                    textStyle = textStyle.copyForStyleKeyValue(TextStyle.CharSpacing_Prop, charSpacing);
                }
                case "Scripting" -> {
                    double scripting = attribute.getDoubleValue();
                    textStyle = textStyle.copyForStyleKeyValue(TextStyle.Scripting_Prop, scripting);
                }
            }
        }
        return textStyle;
    }

    /**
     * TextLineStyle archival (Align, Justify, FirstIndent, LeftIndent, RightIndent, Spacing, SpacingFactor,
     * NewlineSpacing, MinHeight, MaxHeight).
     */
    private static XMLElement lineStyleToXML(TextLineStyle textStyle, TextLineStyle referenceStyle)
    {
        XMLElement xml = new XMLElement("LineStyle");
        if (textStyle.getAlign() != referenceStyle.getAlign())
            xml.add("Align", textStyle.getAlign().toString());
        if (textStyle.isJustify() != referenceStyle.isJustify())
            xml.add("Justify", Boolean.toString(textStyle.isJustify()));
        if (textStyle.getFirstIndent() != referenceStyle.getFirstIndent())
            xml.add("FirstIndent", textStyle.getFirstIndent());
        if (textStyle.getLeftIndent() != referenceStyle.getLeftIndent())
            xml.add("LeftIndent", textStyle.getLeftIndent());
        if (textStyle.getRightIndent() != referenceStyle.getRightIndent())
            xml.add("RightIndent", textStyle.getRightIndent());
        if (textStyle.getSpacing() != referenceStyle.getSpacing())
            xml.add("Spacing", textStyle.getSpacing());
        if (textStyle.getSpacingFactor() != referenceStyle.getSpacingFactor())
            xml.add("SpacingFactor", textStyle.getSpacingFactor());
        if (textStyle.getNewlineSpacing() != referenceStyle.getNewlineSpacing())
            xml.add("NewlineSpacing", textStyle.getNewlineSpacing());
        if (textStyle.getMinHeight() != referenceStyle.getMinHeight())
            xml.add("MinHeight", textStyle.getMinHeight());
        if (textStyle.getMaxHeight() != referenceStyle.getMaxHeight())
            xml.add("MaxHeight", textStyle.getMaxHeight());
        return xml;
    }

    /**
     * TextLineStyle unarchival.
     */
    private static TextLineStyle lineStyleFromXML(XMLElement xml, TextLineStyle referenceStyle)
    {
        TextLineStyle lineStyle = referenceStyle;
        for (XMLAttribute attribute : xml.getAttributes()) {
            lineStyle = switch (attribute.getName()) {
                case "Align" -> lineStyle.copyForPropKeyValue(TextLineStyle.Align_Prop, HPos.valueOf(attribute.getValue()));
                case "Justify" -> lineStyle.copyForPropKeyValue(TextLineStyle.Justify_Prop, attribute.getValue().equals("true"));
                case "FirstIndent" -> lineStyle.copyForPropKeyValue(TextLineStyle.FirstIndent_Prop, attribute.getDoubleValue());
                case "LeftIndent" -> lineStyle.copyForPropKeyValue(TextLineStyle.LeftIndent_Prop, attribute.getDoubleValue());
                case "RightIndent" -> lineStyle.copyForPropKeyValue(TextLineStyle.RightIndent_Prop, attribute.getDoubleValue());
                case "Spacing" -> lineStyle.copyForPropKeyValue(TextLineStyle.Spacing_Prop, attribute.getDoubleValue());
                case "SpacingFactor" -> lineStyle.copyForPropKeyValue(TextLineStyle.SpacingFactor_Prop, attribute.getDoubleValue());
                case "NewlineSpacing" -> lineStyle.copyForPropKeyValue(TextLineStyle.NewlineSpacing_Prop, attribute.getDoubleValue());
                case "MinHeight" -> lineStyle.copyForPropKeyValue(TextLineStyle.MinHeight_Prop, attribute.getDoubleValue());
                case "MaxHeight" -> lineStyle.copyForPropKeyValue(TextLineStyle.MaxHeight_Prop, attribute.getDoubleValue());
                default -> lineStyle;
            };
        }
        return lineStyle;
    }

    /**
     * A property change event for addChars/removeChars.
     */
    public static class CharsChange extends PropChange {

        /** Constructor. */
        public CharsChange(TextModel textModel, Object oldV, Object newV, int anInd)
        {
            super(textModel, TextModel.Chars_Prop, oldV, newV, anInd);
        }

        public CharSequence getOldValue()  { return (CharSequence) super.getOldValue(); }

        public CharSequence getNewValue()  { return (CharSequence) super.getNewValue(); }

        public void doChange(Object oldValue, Object newValue)
        {
            TextModel textModel = (TextModel) getSource();
            int index = getIndex();

            if (oldValue != null)
                textModel.removeChars(index, index + ((CharSequence) oldValue).length());
            else textModel.addChars((CharSequence) newValue, index);
        }

        public PropChange merge(PropChange propChange)
        {
            TextModel textModel = (TextModel) getSource();
            CharsChange event = (CharsChange) propChange;
            CharSequence newVal = getNewValue();
            CharSequence eventNewVal = event.getNewValue();
            int index = getIndex();

            if (newVal != null && eventNewVal != null && newVal.length() + index == event.getIndex())
                return new CharsChange(textModel,null, newVal.toString() + eventNewVal, index);
            return null;
        }
    }

    /**
     * A property change event for TextModel.Style change.
     */
    public static class StyleChange extends PropChange {

        // Ivars
        int _start, _end;

        /** Constructor. */
        public StyleChange(TextModel textModel, Object oV, Object nV, int aStart, int anEnd)
        {
            super(textModel, TextModel.Style_Prop, oV, nV, -1);
            _start = aStart;
            _end = anEnd;
        }

        public int getStart()  { return _start; }

        public int getEnd()  { return _end; }

        public void doChange(Object oldVal, Object newVal)
        {
            TextModel textModel = (TextModel) getSource();
            textModel.setTextStyle((TextStyle) newVal, _start, _end);
        }
    }

    /**
     * A property change event for TextModel.Style change.
     */
    public static class LineStyleChange extends PropChange {

        /** Constructor. */
        public LineStyleChange(TextModel textModel, Object oV, Object nV, int anIndex)
        {
            super(textModel, TextModel.LineStyle_Prop, oV, nV, anIndex);
        }

        public void doChange(Object oval, Object nval)
        {
            TextModel textModel = (TextModel) getSource();
            TextLine line = textModel.getLine(getIndex());
            textModel.setLineStyle((TextLineStyle) nval, line.getStartCharIndex(), line.getStartCharIndex());
        }
    }
}
