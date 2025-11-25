package snap.text;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a TextModel implementation using an array of text lines.
 */
public class TextBlock extends TextModel {

    // The TextLines in this text
    protected List<TextLine> _lines = new ArrayList<>();

    /**
     * Constructor.
     */
    public TextBlock()
    {
        this(false);
    }

    /**
     * Constructor with option to make rich text.
     */
    public TextBlock(boolean isRich)
    {
        super(isRich);
        TextLine defaultLine = new TextLine(this);
        addLine(defaultLine, 0);
    }

    /**
     * Returns the string for the text.
     */
    public String getString()
    {
        StringBuilder sb = new StringBuilder(length());
        _lines.forEach(line -> sb.append(line._sb));
        return sb.toString();
    }

    /**
     * Returns the number of block in this doc.
     */
    public int getLineCount()  { return _lines.size(); }

    /**
     * Returns the individual block in this doc.
     */
    public TextLine getLine(int anIndex)  { return _lines.get(anIndex); }

    /**
     * Returns the list of blocks.
     */
    public List<TextLine> getLines()  { return _lines; }

    /**
     * Adds a block at given index.
     */
    protected void addLine(TextLine aLine, int anIndex)
    {
        _lines.add(anIndex, aLine);
        aLine._textModel = this;
        updateLines(anIndex - 1);
    }

    /**
     * Removes the block at given index.
     */
    protected void removeLine(int anIndex)
    {
        TextLine line = _lines.remove(anIndex);
        line._textModel = null;
        updateLines(anIndex - 1);
    }

    /**
     * Sets the width.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue == _width) return;
        super.setWidth(aValue);
        _lines.forEach(line -> line.updateAlignmentAndJustify());
    }

    /**
     * Standard clone implementation.
     */
//    @Override
//    public TextModel clone()
//    {
//        // Do normal clone
//        TextBlock clone;
//        try { clone = (TextBlock) super.clone(); }
//        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
//
//        // Reset lines array and length
//        clone._lines = new ArrayList<>(getLineCount());
//        clone._length = 0;
//
//        // Copy lines deep
//        for (int i = 0, iMax = getLineCount(); i < iMax; i++) {
//            TextLine line = getLine(i);
//            TextLine lineClone = line.clone();
//            clone.addLine(lineClone, i);
//        }
//
//        // Return
//        return clone;
//    }
}
