package snap.text;
import snap.geom.Path2D;
import snap.geom.Shape;
import snap.props.PropChange;

/**
 * Utility methods to support TextBlock.
 */
public class TextDocUtils {

    /**
     * Returns a path for two char indexes - it will be a simple box with extensions for first/last lines.
     */
    public static Shape getPathForCharRange(TextBlock textBlock, int aStartCharIndex, int aEndCharIndex)
    {
        // Create new path for return
        Path2D path = new Path2D();

        // If invalid range, just return
        if (aStartCharIndex > textBlock.getEndCharIndex() || aEndCharIndex < textBlock.getStartCharIndex())
            return path;
        if (aEndCharIndex > textBlock.getEndCharIndex())
            aEndCharIndex = textBlock.getEndCharIndex();

        // Get StartLine, EndLine and start/end points
        TextLine startLine = textBlock.getLineForCharIndex(aStartCharIndex);
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
            else textBlock.addChars((CharSequence) newValue, null, index);
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
            textBlock.setStyle((TextStyle) newVal, _start, _end);
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
