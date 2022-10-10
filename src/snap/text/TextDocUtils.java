package snap.text;
import snap.props.PropChange;

/**
 * Utility methods to support TextDoc.
 */
public class TextDocUtils {

    /**
     * A property change event for addChars/removeChars.
     */
    public static class CharsChange extends PropChange {

        /** Constructor. */
        public CharsChange(TextDoc aTextDoc, Object oldV, Object newV, int anInd)
        {
            super(aTextDoc, TextDoc.Chars_Prop, oldV, newV, anInd);
        }

        public CharSequence getOldValue()  { return (CharSequence) super.getOldValue(); }

        public CharSequence getNewValue()  { return (CharSequence) super.getNewValue(); }

        public void doChange(Object oldValue, Object newValue)
        {
            TextDoc textDoc = (TextDoc) getSource();
            int index = getIndex();

            if (oldValue != null)
                textDoc.removeChars(index, index + ((CharSequence) oldValue).length());
            else textDoc.addChars((CharSequence) newValue, null, index);
        }

        public PropChange merge(PropChange anEvent)
        {
            TextDoc textDoc = (TextDoc) getSource();
            CharsChange event = (CharsChange) anEvent;
            CharSequence newVal = getNewValue();
            CharSequence eventNewVal = event.getNewValue();
            int index = getIndex();

            if (newVal != null && eventNewVal != null && newVal.length() + index == event.getIndex())
                return new CharsChange(textDoc,null, newVal.toString() + eventNewVal, index);
            return null;
        }
    }

    /**
     * A property change event for RMXStringRun.Style change.
     */
    public static class StyleChange extends PropChange {

        // Ivars
        int _start, _end;

        /** Constructor. */
        public StyleChange(TextDoc aTextDoc, Object oV, Object nV, int aStart, int anEnd)
        {
            super(aTextDoc, TextDoc.Style_Prop, oV, nV, -1);
            _start = aStart;
            _end = anEnd;
        }

        public int getStart()  { return _start; }

        public int getEnd()  { return _end; }

        public void doChange(Object oldVal, Object newVal)
        {
            TextDoc textDoc = (TextDoc) getSource();
            textDoc.setStyle((TextStyle) newVal, _start, _end);
        }
    }

    /**
     * A property change event for RMXStringRun.Style change.
     */
    public static class LineStyleChange extends PropChange {

        /** Constructor. */
        public LineStyleChange(TextDoc aTextDoc, Object oV, Object nV, int anIndex)
        {
            super(aTextDoc, TextDoc.LineStyle_Prop, oV, nV, anIndex);
        }

        public void doChange(Object oval, Object nval)
        {
            TextDoc textDoc = (TextDoc) getSource();
            TextLine line = textDoc.getLine(getIndex());
            textDoc.setLineStyle((TextLineStyle) nval, line.getStartCharIndex(), line.getStartCharIndex());
        }
    }
}
