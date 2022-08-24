package snap.text;
import snap.props.PropChange;

/**
 * Utility methods to support BaseText.
 */
public class BaseTextUtils {

    /**
     * A property change event for addChars/removeChars.
     */
    public static class CharsChange extends PropChange {

        /** Constructor. */
        public CharsChange(BaseText aBaseText, Object oldV, Object newV, int anInd)
        {
            super(aBaseText, BaseText.Chars_Prop, oldV, newV, anInd);
        }

        public CharSequence getOldValue()  { return (CharSequence) super.getOldValue(); }

        public CharSequence getNewValue()  { return (CharSequence) super.getNewValue(); }

        public void doChange(Object oldValue, Object newValue)
        {
            BaseText baseText = (BaseText) getSource();
            int index = getIndex();

            if (oldValue != null)
                baseText.removeChars(index, index + ((CharSequence) oldValue).length());
            else baseText.addChars((CharSequence) newValue, null, index);
        }

        public PropChange merge(PropChange anEvent)
        {
            BaseText baseText = (BaseText) getSource();
            CharsChange event = (CharsChange) anEvent;
            CharSequence newVal = getNewValue();
            CharSequence eventNewVal = event.getNewValue();
            int index = getIndex();

            if (newVal != null && eventNewVal != null && newVal.length() + index == event.getIndex())
                return new CharsChange(baseText,null, newVal.toString() + eventNewVal, index);
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
        public StyleChange(BaseText aBaseText, Object oV, Object nV, int aStart, int anEnd)
        {
            super(aBaseText, BaseText.Style_Prop, oV, nV, -1);
            _start = aStart;
            _end = anEnd;
        }

        public int getStart()  { return _start; }

        public int getEnd()  { return _end; }

        public void doChange(Object oldVal, Object newVal)
        {
            RichText baseText = (RichText) getSource();
            baseText.setStyle((TextStyle) newVal, _start, _end);
        }
    }

    /**
     * A property change event for RMXStringRun.Style change.
     */
    public static class LineStyleChange extends PropChange {

        /** Constructor. */
        public LineStyleChange(BaseText aBaseText, Object oV, Object nV, int anIndex)
        {
            super(aBaseText, BaseText.LineStyle_Prop, oV, nV, anIndex);
        }

        public void doChange(Object oval, Object nval)
        {
            BaseText baseText = (BaseText) getSource();
            BaseTextLine line = baseText.getLine(getIndex());
            baseText.setLineStyle((TextLineStyle) nval, line.getStart(), line.getStart());
        }
    }
}
