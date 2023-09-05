package snap.text;
import snap.util.MathUtils;

/**
 * A class to hold the font metrics for text.
 */
public class TextMetrics {

    // The max ascent for text
    protected double _ascent;

    // The max descent for text
    protected double _descent;

    // The leading for line text
    protected double _leading;

    // The max Ascent for line text
    protected double _lineAdvance;

    /**
     * Constructor.
     */
    public TextMetrics(TextLine textLine)
    {
        // Get values for first run
        TextRun run = textLine.getRun(0);
        _ascent = run.getAscent();
        _descent = run.getDescent();
        _leading = run.getLeading();

        // Update for successive runs
        run = run.getNext();
        while (run != null) {
            _ascent = Math.max(_ascent, run.getAscent());
            _descent = Math.max(_descent, run.getDescent());
            _leading = Math.max(_leading, run.getLeading());
            run = run.getNext();
        }

        // Calculate LineAdvance
        TextLineStyle lineStyle = textLine.getLineStyle();
        _lineAdvance = _ascent + _descent + _leading;
        _lineAdvance = MathUtils.clamp(_lineAdvance, lineStyle.getMinHeight(), lineStyle.getMaxHeight());
        _lineAdvance *= lineStyle.getSpacingFactor();
        _lineAdvance += lineStyle.getSpacing();
    }

    /**
     * Returns the max ascent of the chars in this line.
     */
    public double getAscent()  { return _ascent; }

    /**
     * Returns the max descent of the chars in this line.
     */
    public double getDescent()  { return _descent; }

    /**
     * Returns the leading of the chars in this line.
     */
    public double getLeading()  { return _leading; }

    /**
     * Returns the vertical distance for any line below this line.
     */
    public double getLineAdvance()  { return _lineAdvance; }
}
