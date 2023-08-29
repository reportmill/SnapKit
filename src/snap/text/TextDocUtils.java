package snap.text;
import snap.props.PropChange;

/**
 * Utility methods to support TextBlock.
 */
public class TextDocUtils {

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
