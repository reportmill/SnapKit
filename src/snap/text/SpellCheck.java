/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
//import com.wintertree.ssce.*;
import java.util.*;

/**
 * This class provides provides generic spell check API ideal for RM's needs. The abstraction should let us
 * plug in any spell check technology.
 */
public class SpellCheck {
    
//    // An object that can pull a word from a string
//    static StringWordParser _parser = new StringWordParser(true);
//
//    // An object that can find the next misspelled word from a parser
//    static SpellingSession  _speller;
    
/**
 * Returns the first misspelled word in a given string starting at the given index (or null or no misspelled words).
 */    
public static Word getMisspelledWord(String aString, int anIndex)
{
//    // If given index is already beyond string bounds, just return null
//    if(anIndex>=aString.length()) return null;
//
//    // Configure word parser for given string and index
//    _parser.setText(aString);
//    _parser.setCursor(anIndex);
//
//    // Get Speller, create new Word and declare variable for check result
//    SpellingSession speller = getSpeller();
//    Word word = new Word();
//    int cr;
//
//    // Iterate over words
//    while(((cr=speller.check(_parser, word._otherWord)) & SpellingSession.END_OF_TEXT_RSLT) == 0) {
//
//        // If misspelled, configure word and return
//        if((cr & SpellingSession.MISSPELLED_WORD_RSLT) != 0) {
//            word._start = _parser.getCursor();
//            word._string = _parser.nextWord();
//            word._end = word._start + word._string.length();
//            return word;
//        }
//
//        // Find next word
//        _parser.nextWord();
//    }
    
    // If no more misspelled words, return null
    return null;
}

///**
// * Creates and returns a speller.
// */
//private static SpellingSession getSpeller()
//{
//    // If speller not created, create and set
//    if(_speller==null) try {
//
//        // Set license key
//        LicenseKey.setKey(0x80CD5259);
//
//        // Get lexicons
//        InputStream is1 = SpellCheck.class.getResourceAsStream("/com/wintertree/ssceam.tlx");
//        StreamTextLexicon lex1 = new StreamTextLexicon(is1);
//        InputStream is2 = SpellCheck.class.getResourceAsStream("/com/wintertree/ssceam2.clx");
//        CompressedLexicon lex2 = new CompressedLexicon(is2);
//        InputStream is3 = SpellCheck.class.getResourceAsStream("/com/wintertree/tech.tlx");
//        StreamTextLexicon lex3 = new StreamTextLexicon(is3);
//
//        // Get speller
//        _speller = new SpellingSession();
//
//        // Set lexicons
//        _speller.setLexicons(new Lexicon[] { lex1, lex2, lex3 });
//
//    // Catch exceptions
//    } catch(Exception e) { e.printStackTrace(); }
//
//    // Return speller
//    return _speller;
//}

/**
 * An inner class to represent a misspelled word.
 */
public static class Word {
    
    // The misspelled word
    String         _string;
    
    // The start/end index from original text
    int            _start, _end;
    
    // I can't figure this out, but needed for WinterTree SpellingSession check()
    StringBuffer   _otherWord = new StringBuffer();
    
    /** Returns the string. */
    public String getString() { return _string; }
    
    /** Returns the start index. */
    public int getStart() { return _start; }
    
    /** Returns the end index. */
    public int getEnd() { return _end; }
    
    /** Returns a list of suggestions. */
    public List <String> getSuggestions()
    {
        // Get speller, create suggestion set and fill with suggestions
//        SpellingSession speller = getSpeller();
//        SuggestionSet set = new SuggestionSet(8);
//        speller.suggest(getString(), SpellingSession.MAX_SUGGEST_DEPTH, new EnglishPhoneticComparator(), set);
        
        // Create list and fill from suggestion set and return
        List list = new ArrayList();
//        for(Enumeration e=set.words(); e.hasMoreElements();) list.add(e.nextElement());
        return list;
    }
    
    /** Returns string representation of word. */
    public String toString() { return "Misspelled word: " + (_string!=null? _string : ""); }
}
    
}