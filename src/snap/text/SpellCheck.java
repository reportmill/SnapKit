/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.*;

/**
 * This class provides generic spell check API to allow support for any spell check technology.
 */
public class SpellCheck {

    // The shared SpellCheck
    private static SpellCheck _shared;

    // The shared SpellCheck
    private static Class<? extends SpellCheck> _sharedClass;

    /**
     * Constructor.
     */
    public SpellCheck()
    {
        super();
    }

    /**
     * Returns the next misspelled word in given string.
     */
    public Word getNextMisspelledWord(String aString, int anIndex)
    {
        return null;
    }

    /**
     * Returns the suggestions for given word.
     */
    protected List<String> getSuggestionsForWord(Word aWord)
    {
        return Collections.EMPTY_LIST;
    }

    /**
     * Returns the first misspelled word in a given string starting at the given index (or null or no misspelled words).
     */
    public static Word getMisspelledWord(String aString, int anIndex)
    {
        SpellCheck spellCheck = getShared();
        return spellCheck.getNextMisspelledWord(aString, anIndex);
    }

    /**
     * Returns the shared spell checker.
     */
    public static SpellCheck getShared()
    {
        if (_shared != null) return _shared;

        // Create new instance
        Class<? extends SpellCheck> sharedClass = _sharedClass != null ? _sharedClass : SpellCheck.class;
        SpellCheck shared;
        try { shared = sharedClass.getConstructor().newInstance(); }
        catch (Exception e) { throw new RuntimeException(e); }

        // Return
        return _shared = shared;
    }

    /**
     * Returns the shared spell checker.
     */
    public static void setSharedClass(Class<? extends SpellCheck> aClass)
    {
        if (aClass == _sharedClass) return;
        _sharedClass = aClass;
        _shared = null;
    }

    /**
     * An inner class to represent a misspelled word.
     */
    public static class Word {

        // The misspelled word
        protected String _string;

        // The start/end index from original text
        protected int _start, _end;

        /**
         * Constructor for given word at index.
         */
        public Word(String aString, int aStart)
        {
            _string = aString;
            _start = aStart;
            _end = aStart + aString.length();
        }

        /** Returns the string. */
        public String getString() { return _string; }

        /** Returns the start index. */
        public int getStart() { return _start; }

        /** Returns the end index. */
        public int getEnd() { return _end; }

        /** Returns a list of suggestions. */
        public List<String> getSuggestions()
        {
            SpellCheck spellCheck = getShared();
            return spellCheck.getSuggestionsForWord(this);
        }

        /** Returns string representation of word. */
        public String toString() { return "Misspelled word: " + (_string != null ? _string : ""); }
    }
}