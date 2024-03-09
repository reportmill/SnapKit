package snap.parse;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds TokenLines.
 */
public class TokenDoc {

    // The chars
    protected CharSequence _chars;

    // The list of token lines
    private List<TokenLine> _lines = new ArrayList<>();

    // The start line index (if doing parse of partial doc)
    protected int _startLineIndex;

    /**
     * Constructor.
     */
    public TokenDoc(CharSequence theChars)
    {
        super();
        _chars = theChars;
    }

    /**
     * Returns the number of lines.
     */
    public int getLineCount()  { return _lines.size(); }

    /**
     * Returns the individual line at given index.
     */
    public TokenLine getLine(int anIndex)
    {
        int lineIndex = anIndex;
        return _lines.get(lineIndex);
    }

    /**
     * Adds a line for char range.
     */
    public TokenLine addLineForCharRange(int startCharIndex, int endCharIndex)
    {
        TokenLine tokenLine = new TokenLine(this, startCharIndex, endCharIndex, getLineCount());
        _lines.add(tokenLine);
        return tokenLine;
    }

    /**
     * Returns the last line.
     */
    public TokenLine getLastLine()
    {
        int lineCount = getLineCount();
        return lineCount > 0 ? getLine(lineCount - 1) : null;
    }
}
