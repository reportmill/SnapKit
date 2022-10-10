/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a line of text in a Text.
 */
public class SubTextLine extends TextLine {

    // The SubText that holds this object
    private SubText  _subText;

    // The real TextLine that this object maps to
    private TextLine  _textLine;

    // The end char index in SubText
    private int _endInSubText;

    /**
     * Constructor.
     */
    public SubTextLine(SubText aSubText, TextLine aTextLine, int startInTextDoc, int endInTextDoc)
    {
        super(aSubText);

        // Set ivars
        _subText = aSubText;
        _textLine = aTextLine;
        _startCharIndex = startInTextDoc - aSubText._start;
        _endInSubText = endInTextDoc - aSubText._start;

        // Create RunList and get loop vars for TextLine runs
        List<TextRun> runsList = new ArrayList<>();
        int offsetFromTextLineToSubLine = startInTextDoc - _textLine.getStartCharIndex();
        int runStartInTextLine = offsetFromTextLineToSubLine;
        TextRun textRun = _textLine.getRunForCharIndex(runStartInTextLine);

        // Get runs
        while (textRun != null) {
            int runLength = textRun.getEnd() - runStartInTextLine;
            TextRun subRun = new TextRun(this);
            subRun._start = runStartInTextLine - offsetFromTextLineToSubLine;
            subRun._length = runLength;
            subRun._style = textRun.getStyle();
            subRun._index = runsList.size();
            runsList.add(subRun);
            runStartInTextLine += runLength;
            textRun = textRun.getNext();
        }

        // Get Runas as array
        _runs = runsList.toArray(new TextRun[0]);
    }

    /**
     * Converts index from this subLine to textLine.
     */
    private final int convertSubLineToTextLine(int charIndexInSubLine)
    {
        return _subText._start + _startCharIndex - _textLine._startCharIndex + charIndexInSubLine;
    }

    /**
     * Returns the length of this text line.
     */
    public int length()  { return _endInSubText - _startCharIndex; }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)
    {
        int textLineIndex = convertSubLineToTextLine(anIndex);
        return _textLine.charAt(textLineIndex);
    }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)
    {
        int startInTextLine = convertSubLineToTextLine(aStart);
        int endInTextLine = convertSubLineToTextLine(anEnd);
        return _textLine.subSequence(startInTextLine, endInTextLine);
    }

    /**
     * Returns the index of given string in line.
     */
    public int indexOf(String aStr, int aStart)
    {
        int textLineIndex = convertSubLineToTextLine(aStart);
        return _textLine.indexOf(aStr, textLineIndex);
    }

    /**
     * Returns the string for the line.
     */
    public String getString()
    {
        return subSequence(0, length()).toString();
    }

    /**
     * Returns the line style.
     */
    public TextLineStyle getLineStyle()  { return _textLine.getLineStyle(); }

    /**
     * Sets the line style.
     */
    public void setLineStyle(TextLineStyle aLineStyle)  { _textLine.setLineStyle(aLineStyle); }

    /**
     * Returns a copy of this line for given char range.
     */
    public TextLine copyForRange(int aStart, int aEnd)
    {
        return _textLine.copyForRange(aStart + _startCharIndex, aEnd + _startCharIndex);
    }
}