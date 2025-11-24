package snap.text;

/**
 * This class is a TextModel implementation that holds an array of text lines.
 */
public class TextBlock extends TextModel {

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
        super();
        _rich = isRich;
        TextLine defaultLine = new TextLine(this);
        addLine(defaultLine, 0);
    }
}
