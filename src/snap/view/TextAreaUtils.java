/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.text.TextSel;

/**
 * This class provides utility methods for TextArea.
 */
public class TextAreaUtils {

    /**
     * This method returns the range of the @-sign delinated key closest to the current selection (or null if not found).
     */
    public static TextSel smartFindFormatRange(TextArea aTextArea)
    {
        int selStart = aTextArea.getSelStart();
        int selEnd = aTextArea.getSelEnd();
        int prevAtSignIndex = -1;
        int nextAtSignIndex = -1;
        String string = aTextArea.getText();

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
            return new TextSel(aTextArea.getTextBox(), start, end + 1);
        }

        // Return null since range not found
        return null;
    }

}
