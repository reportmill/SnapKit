/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.text.SpellCheck;
import snap.view.*;

/**
 * This class uses the SpellCheck class to check spelling.
 */
public abstract class SpellCheckPanel extends ViewOwner {

    // The current suspected misspelled word
    private SpellCheck.Word _word;

    /**
     * Constructor.
     */
    public SpellCheckPanel()
    {
        super();
    }

    /**
     * Runs the spell check panel.
     */
    public void show(View aView)
    {
        // Make window visible
        WindowView window = getWindow();
        window.setAlwaysOnTop(true);
        window.setSaveName("SpellCheck");
        window.setType(WindowView.Type.UTILITY);
        window.setVisible(true);

        // Find next misspelling
        findNextMisspelling();
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Configure SuggestionList to watch for MouseClick
        ListView<?> suggestionList = getView("SuggestionList", ListView.class);
        suggestionList.addEventHandler(this::handleSuggestionListMouseRelease, MouseRelease);
    }

    /**
     * Reset the UI.
     */
    public void resetUI()
    {
        // Reset WordText
        setViewValue("WordText", _word == null ? "" : _word.getString());

        // Reset SuggestionList Items and SelIndex
        Object[] items = _word != null ? _word.getSuggestions().toArray() : new Object[0];
        setViewItems("SuggestionList", items);
        if (items.length > 0)
            setViewSelIndex("SuggestionList", 0);
    }

    /**
     * Handles okay button.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle CloseButton: Just close the window and return
        if (anEvent.equals("CloseButton")) {
            setWindowVisible(false);
            return;
        }

        // Handle IgnoreButton: find and hilight next misspelled word
        if (anEvent.equals("FindNextButton"))
            findNextMisspelling();

        // Handle CorrectButton
        else if (anEvent.equals("CorrectButton"))
            doCorrection();
    }

    private void handleSuggestionListMouseRelease(ViewEvent anEvent)
    {
        if (anEvent.getClickCount() > 1)
            doCorrection();
    }

    /**
     * Find next misspelling.
     */
    public void findNextMisspelling()
    {
        // Get text editor and text
        String textString = getTextString();
        if (textString == null)
            return;
        int textSelEnd = getSelEnd();

        // Get next misspelled word
        _word = SpellCheck.getMisspelledWord(textString, textSelEnd);

        // If not null, select word
        if (_word != null)
            setSel(_word.getStart(), _word.getEnd());

            // Otherwise, set selection to end and beep
        else {
            setSel(getTextLength());
            beep();
        }

        // Reset panel
        resetLater();
    }

    /**
     * Do correction.
     */
    public void doCorrection()
    {
        // Get suggested word from list
        String correctWord = getViewStringValue("SuggestionList");

        // Replace in text editor
        if (_word != null && correctWord != null) {
            setSel(_word.getStart(), _word.getEnd());
            replaceChars(correctWord);
        }

        // Find next misspelling
        findNextMisspelling();
    }

    /**
     * Returns the text that this spell check panel should be checking.
     */
    protected abstract String getTextString();

    protected int getTextLength()  { return 0; }  // getTextAdapter().length()

    protected int getSelEnd()  { return 0; }  // getTextAdapter().getSelEnd();

    protected void setSel(int aStart)
    {
        setSel(aStart, aStart);
    }

    protected void setSel(int aStart, int anEnd)  { }  // getTextAdapter().setSel(aStart, anEnd);

    protected void replaceChars(String aString)  { }  // getTextAdapter().replaceChars(aString);
}