/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.text.SpellCheck;
import snap.text.TextEditor;
import snap.view.*;

/**
 * This class uses the SpellCheck class to check spelling.
 */
public abstract class SpellCheckPanel extends ViewOwner {
    
    // The editor
    View _view;

    // The current suspected misspelled word
    private SpellCheck.Word _word;

/**
 * Runs the spell check panel.
 */
public void show(View aView)
{
    // Make window visible and find next misspelling
    getWindow().setSaveName("SpellCheck");
    getWindow().setVisible(true);
    findNextMisspelling();
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Configure SuggestionList to watch for MouseClick
    enableEvents("SuggestionList", MouseRelease);
}

/**
 * Reset the UI.
 */
public void resetUI()
{
    // Reset WordText
    setViewValue("WordText", _word==null? "" : _word.getString());
    
    // Reset SuggestionList Items and SelIndex
    Object items[] = _word!=null? _word.getSuggestions().toArray() : new Object[0];
    setViewItems("SuggestionList", items);
    if(items.length>0) setViewSelIndex("SuggestionList", 0);
}

/**
 * Handles okay button.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle CloseButton: Just close the window and return
    if(anEvent.equals("CloseButton")) {
        setWindowVisible(false); return; }
    
    // Handle IgnoreButton: find and hilight next misspelled word
    if(anEvent.equals("FindNextButton"))
        findNextMisspelling();
    
    // Handle CorrectButton
    else if(anEvent.equals("CorrectButton"))
        doCorrection();
    
    // Handle SuggestionList
    else if(anEvent.equals("SuggestionList") && anEvent.getClickCount()>1)
        doCorrection();
}

/**
 * Find next misspelling.
 */
public void findNextMisspelling()
{
    // Get text editor and text
    TextEditor textEditor = getTextEditor();
    String text = getText(); if(text==null) return;
    
    // Get next misspelled word
    _word = SpellCheck.getMisspelledWord(text, textEditor.getSelEnd());
    
    // If not null, select word
    if(_word!=null)
        textEditor.setSel(_word.getStart(), _word.getEnd());
    
    // Otherwise, set selection to end and beep
    else {
        textEditor.setSel(textEditor.length());
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
    // Get main editor and text editor and text
    TextEditor textEditor = getTextEditor();
    
    // Get suggested word from list
    String correctWord = getViewStringValue("SuggestionList");
    
    // Replace in text editor
    if(_word!=null && correctWord!=null) {
        textEditor.setSel(_word.getStart(), _word.getEnd());
        textEditor.replaceChars(correctWord);
    }
    
    // Find next misspelling
    findNextMisspelling();
}

/**
 * Returns the text that this spell check panel should be checking.
 */
protected abstract String getText();

/**
 * Returns the TextEditor.
 */
protected abstract TextEditor getTextEditor();

}