/* ==================================================================
 * This file is part of JTextCheck - a Java text-checking API
 * Homepage: http://linux.org.mt/projects/jtextcheck/
 * Copyright 2003 Ramon Casha
 *
 * Licensed under the GNU LGPL v2.1. You can find the text of this
 * license at http://www.gnu.org/copyleft/lesser.html
 * ================================================================== */
package snap.text;
import java.io.*;
import java.util.*;
import snap.util.*;
import snap.web.WebURL;

/**
 * Module to load and process an OpenOffice.org-style hyphenation dictionary.
 *
 * This module is essentially a translation into Java of C code developed 
 * for OpenOffice.org by Peter Novodvorsky, which was in turn based on 
 * Raph Levien's linhnj, which was in turn based on Knuth's TeX algorithm.
 *
 * @author Ramon Casha (ramon.casha@linux.org.mt)
 * @author Peter Novodvorsky (nidd@alt-linux.org)
 * @author Raph Levien (raph@acm.org)
 */
public class TextHyphenDict {
    
    // Current array of states
    ArrayList            _states = new ArrayList();
    
    // Shared instance
    static TextHyphenDict    _shared;
    
    /** Create an instance based on the given filename. Reads and parses the given file into internal storage. */
    public TextHyphenDict()
    {
        try { loadHyphenDict(); }
        catch(Exception e) { e.printStackTrace(); }
    }
    
    /** Return the shared instance of the HyphenDict. */
    public static TextHyphenDict getShared()  { return _shared!=null? _shared : (_shared = new TextHyphenDict()); }
    
    /** Returns the hyphen just prior to line end given chars, start index, chars end and line end. */
    public int getHyphen(CharSequence anInput, int aStart, int anEnd)
    {
        // If there are only a few characters before line end, just bail
        if(anEnd-aStart<3)
            return -1;

        // Find word end
        int wordEnd = anEnd;
        for(int i=aStart; i<anEnd; i++)
            if(Character.isWhitespace(anInput.charAt(i))) {
                wordEnd = i; break; }
        
        // If word is zero length, return
        if(wordEnd-aStart<=2)
            return -1;
        
        // Get word
        String word = anInput.subSequence(aStart, wordEnd).toString();
        
        // Return
        int hyphen = getHyphen(word, wordEnd - aStart);
        return hyphen>0? (aStart + hyphen) : -1;
            
        // Get Hyphens and bail if null
        //int hyphens[] = getHyphens(word); if(hyphens==null) return -1;
        // If hyphen prior to lineEnd, return it
        //for(int i=hyphens.length-1; i>=0; i--) if(start + hyphens[i]<lineEnd) return start + hyphens[i];
        // If no hyphens before lineEnd: return -1;
    }
    
    public int getHyphen(String aWord, int lineEnd)
    {
        // Returns a string buffer for word with hyphen data inserted
        StringBuffer hyphens = getHyphensBuffer(aWord);

        // Added this code to return hyphen index instead of hyphen array below
        for(int i=lineEnd-2; i>0; i--)
            if((hyphens.charAt(i)&1)!=0)
                return i+1;
        
        // 
        return -1;        
    }
    
    public int[] getHyphens(String aWord)
    {
        // Returns a string buffer for word with hyphen data inserted
        StringBuffer hyphens = getHyphensBuffer(aWord);

        // count the number of hyphens
        int nHyphens = 0;
        for(int i=0; i<hyphens.length(); i++)
            if((hyphens.charAt(i)&1) != 0)
                nHyphens++;
        
        // convert into an array of integers        
        if(nHyphens > 0) {
            int[] hypos = new int[nHyphens];
            for(int i=0, hno=0; i<hyphens.length(); i++)
                if((hyphens.charAt(i)&1) != 0)
                    hypos[hno++] = i+1;
            
            // Return hyphens
            return hypos;
        }
        
        // Return null
        return null;
    }
    
    /** Perform hyphenation on a word. Fills in the word's hyphenationPositions array. */
    public StringBuffer getHyphensBuffer(String aWord)
    {
        // Get word as lowercase 
        String lcword = aWord.toLowerCase();
        int nulen = lcword.length()+3;
        
        StringBuffer prepWord = new StringBuffer(nulen);
        prepWord.append('.');
        prepWord.append(lcword);
        prepWord.append('.');
        
        StringBuffer hyphens = mkBuffer(nulen,'0');
        
        HyphenState state = (HyphenState)_states.get(0);
        
        // Iterate over word characters
        NEXT_LETTER: for(int i=0;i<prepWord.length();i++) {
            
            // Get current character
            char ch = prepWord.charAt(i);
            
            // Iterate?
            TRANS_LOOP: while(true) {
                
                // Null state?
                if(state == null) {
                    state = (HyphenState)_states.get(0);
                    continue NEXT_LETTER;
                }
                
                // Iterate?
                for(int k = 0; k < state.getNumTrans(); k++) {
                    HyphenTrans trans = state.getTrans(k);
                    if(trans.ch == ch) {
                        state = trans.newState;
                        break TRANS_LOOP;
                    }
                }
                
                // State?
                state = state.fallbackState;
            }
            
            // If match
            if(state.match != null) {
                
                // Get match length and offset
                int ml = state.match.length();
                int offset = i + 1 - ml;
                
                // Iterate?
                for(int k = 0; k < ml; k++)
                    if(hyphens.charAt(offset + k) < state.match.charAt(k))
                        hyphens.setCharAt(offset + k, state.match.charAt(k));
            }
            
        }
        
        // Iterate?
        for(int i=0; i<prepWord.length() - 4; i++)
            hyphens.setCharAt(i, hyphens.charAt(i + 1));
        
        // Set char
        hyphens.setCharAt(0, '0');
        
        // Iterate
        for(int i=prepWord.length()-4; i<lcword.length(); i++)
            hyphens.setCharAt(i, '0');
        
        // Set length
        hyphens.setLength(lcword.length());
        
        return hyphens;
    }
   
    /** Loads hyphen dict from source. */
    private void loadHyphenDict() throws IOException
    {
        // Get bytes for aSource
        byte bytes[] = SnapUtils.getBytes(WebURL.getURL(getClass(), "TextHyphenDict_US.dic"));
        
        // Get Charset
        String charset = null;
        for(int i=0; i<bytes.length; i++) {
            
            if(bytes[i]=='\n') {
                charset = new String(bytes, 0, i);
                charset = StringUtils.delete(charset, "charset "); // some dicts start with "charset " - don't know why
                break;
            }
        }
        
        // Get reader with charset
        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(bytes), charset);
        
        // Get buffered reader and skip charset line
        BufferedReader bufferedReader = new BufferedReader(reader);
        bufferedReader.readLine(); // skip encoding line
        
        // this hashmap points to the states, keyed by word
        HashMap hashmap = new HashMap();
        
        // create the first one.
        HyphenState hs = new HyphenState();
        hashmap.put("", hs);
        _states.add(hs);
        
        String buf;
        while((buf=bufferedReader.readLine()) != null) {
            if (buf.charAt(0) == '%') continue; // i suppose these are comments
            
            // prepare some buffers
            StringBuffer pattern = mkBuffer(buf.length()+1, '0');
            StringBuffer word = mkBuffer(buf.length(), '~');
            
            // put the read line into the pattern and word buffers
            int j = 0;
            //pattern.setCharAt(j, '0');
            for (int i=0 ; i < buf.length(); i++) {
                char c = buf.charAt(i);
                if (c >= '0' && c <= '9') {
                    pattern.setCharAt(j, buf.charAt(i));
                } else {
                    word.setCharAt(j++, buf.charAt(i));
                }
            }

            // trim the word and pattern
            word.setLength(j);
            pattern.setLength(j+1);
            // Kill leading zeroes
            while(pattern.charAt(0) == '0') pattern.deleteCharAt(0);

            // find or add state
            HyphenState found = (HyphenState)hashmap.get(word.toString());
            HyphenState state = found;
            if (found == null) {
                state = new HyphenState();
                hashmap.put(word.toString(), state);
                _states.add(state);
            }
            state.setMatch(pattern.toString());
            
            // put in prefix transitions
            for (; found == null ;j--) {
                HyphenState lastState = state;
                char ch = word.charAt(j - 1);
                word.setLength(j - 1);
                found = (HyphenState)hashmap.get(word.toString());
                state = found;
                if(found == null) {
                    state = new HyphenState();
                    hashmap.put(word.toString(), state);
                    _states.add(state);
                }
                HyphenTrans ht = new HyphenTrans();
                ht.ch = ch;
                ht.newState = lastState;
                state.addTrans(ht);
            }
        }
        
        // put in fallback states
        for (Object obj : hashmap.keySet()) {
            String key = (String) obj;
            HyphenState e = (HyphenState) hashmap.get(key);
            for (int j = 1; true; j++) {
                if (j > key.length()) {
                    break;
                }
                HyphenState state = (HyphenState) hashmap.get(key.substring(j));
                if (state != null) {
                    e.fallbackState = state;
                    break;
                }
            }
        }
        
    }
    
    /** Create a StringBuffer of the specified length and fill it with a character */
    private static StringBuffer mkBuffer(int len, char filler)
    {
        StringBuffer ret = new StringBuffer(len);
        for(int i=0;i<len;i++)
            ret.append(filler);
        return ret;
    }
    
    /** HyphenState. */
    static class HyphenState {
        
        // Match
        String match = null;
        
        // Fallbackstate
        HyphenState fallbackState = null;
        
        // trans list
        private ArrayList trans = new ArrayList();
        
        /** Getter for property match. */
        public String getMatch() { return match; }
        
        /** Setter for property match. */
        public void setMatch(java.lang.String match) { this.match = match; }
        
        /** Getter for property fallbackState. */
        public HyphenState getFallbackState() { return fallbackState; }
        
        /** Setter for property fallbackState. */
        public void setFallbackState(HyphenState fallbackState) { this.fallbackState = fallbackState; }
        
        public void addTrans(HyphenTrans trans) { this.trans.add(trans); }
        
        public HyphenTrans getTrans(int index) { return (HyphenTrans)trans.get(index); }
        
        public int getNumTrans() { return trans.size(); }
    }
    
    /** HyphenTrans. */
    static class HyphenTrans {
        
        // Character
        public char ch;
        
        // State
        public HyphenState newState;
    }
}