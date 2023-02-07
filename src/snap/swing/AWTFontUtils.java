package snap.swing;
import java.awt.*;
import java.util.*;
import java.util.List;
import snap.gfx.Font;
import snap.util.StringUtils;

/**
 * Provides some utility methods for Fonts.
 */
public class AWTFontUtils {

    // Fonts - caches requested font names for fast successive lookups
    private static Map<String,java.awt.Font>  _fontCache = new Hashtable<>();
    
    // The array of system fonts
    private static java.awt.Font[]  _fonts;

    // Cached list of all AWT font names
    private static String[]  _fontNames;
    
    // Cached list of all AWT font family names
    private static String[]  _familyNames;
    
    // A list of all fonts
    private static Font[]  _altFonts;
    
    /**
     * Returns a Font for a given name and size.
     */
    public static java.awt.Font getFont(String aName, double aSize)
    {
        // Get font from fonts map, if not in map, guess font for name and put in map
        java.awt.Font font = _fontCache.get(aName);
        if (font == null)
            _fontCache.put(aName, font = guessFont(aName));

        // Return font adjusted for requested size
        return font.deriveFont((float) aSize);
    }

    /**
     * Returns the array of system fonts.
     */
    public static java.awt.Font[] getFonts()
    {
        if (_fonts != null) return _fonts;
        return _fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    }

    /**
     * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
     */
    public static synchronized String[] getFontNames()
    {
        // If fontNames has been loaded already, just return it
        if (_fontNames != null) return _fontNames;

        // Get system fonts and create list for font names and family names
        java.awt.Font[] fonts = getFonts();
        List<String> fontNames = new ArrayList<>(fonts.length);
        List<String> familyNames = new ArrayList<>(fonts.length / 2);

        // Iterate over fonts
        for (java.awt.Font font : fonts) {

            // Get current loop font name and family name
            String name = font.getFontName();
            String family = font.getFamily();

            // skip fonts with bad names
            if (StringUtils.isEmpty(name) || StringUtils.isEmpty(family))
                continue;

            // If font name or family name doesn't start with a letter, skip this font
            if (!Character.isLetter(name.charAt(0)) || !Character.isLetter(family.charAt(0)))
                continue;

            // If font name hasn't been encountered yet, add it to list
            if (!fontNames.contains(name))
                fontNames.add(name);

            // If family name hasn't been encountered yet, add it to list
            if (!familyNames.contains(family))
                familyNames.add(family);
        }

        // Get String array for font names and sort
        _fontNames = fontNames.toArray(new String[0]);
        Arrays.sort(_fontNames);

        // Get String array for family names and sort
        _familyNames = familyNames.toArray(new String[0]);
        Arrays.sort(_familyNames);

        // Return font names
        return _fontNames;
    }

    /**
     * Returns a list of all system family names.
     */
    public static String[] getFamilyNames()
    {
        // If family names haven't been loaded, call allFontNames
        if (_familyNames == null)
            getFontNames();
        return _familyNames;
    }

    /**
     * Returns a list of all font names for a given family name.
     */
    public static String[] getFontNames(String aFamilyName)
    {
        // Get system fonts and create new list for font family
        java.awt.Font[] fonts = getFonts();
        List<String> family = new ArrayList<>();

        // Iterate over fonts
        for (java.awt.Font font : fonts) {

            // Get current loop font name and family name
            String name = font.getFontName();
            String fam = font.getFamily();

            // If family name is equal to given family name, add font name
            if (fam.equals(aFamilyName) && !family.contains(name))
                family.add(name);
        }

        // Get font names as array and sort
        String[] familyArray = family.toArray(new String[0]);
        Arrays.sort(familyArray);

        // Return list for font family
        return familyArray;
    }

    /**
     * Returns the system font with the most similar name to the given name.
     */
    public static java.awt.Font guessFont(String aName)
    {
        // Get normalized font name and array of system fonts
        String name = getFontNameNormalized(aName);
        java.awt.Font[] fonts = getFonts();

        // Iterate over system fonts and if one has same name, return it
        for (java.awt.Font font : fonts) {
            String fontName = getFontNameNormalized(font.getFontName(Locale.ENGLISH));
            if (name.equals(fontName))
                return font;
        }

        // Declare variable for guess font, maximum matches found and minNameLengthDelta
        java.awt.Font guessFont = null;
        int maxMatches = 0;
        int minNameLengthDelta = 999;

        // Iterate over all fonts to see if one has similar name
        for (java.awt.Font font : fonts) {

            // Get font name normalized and break it into pieces
            String fName = font.getFontName(Locale.ENGLISH);
            String fontName = getFontNameNormalized(fName);
            String[] pieces = fontName.split(" ");

            // Declare vars for determining relevance
            boolean substantialMatch = false;
            int matches = 0;

            // Iterate over pieces of current font name
            for (String piece : pieces) {

                // Get current piece (just skip it if zero length)
                if (piece.length() == 0)
                    continue;

                // If piece is found in font name, increment pieces count and possibly declare substantial match
                if (aName.contains(piece)) {

                    // Just skip out if piece is part of another piece
                    if ((piece.equalsIgnoreCase("Bold") ||
                            piece.equalsIgnoreCase("Italic") ||
                            piece.equalsIgnoreCase("Oblique")) &&
                            (aName.indexOf("BoldItalic") > 0 || aName.indexOf("BoldOblique") > 0))
                        break;

                    // If piece isn't common descriptor, mark substantial match true
                    if (!(piece.equalsIgnoreCase("Regular") ||
                            piece.equalsIgnoreCase("Medium") ||
                            piece.equalsIgnoreCase("Bold") ||
                            piece.equalsIgnoreCase("Italic") ||
                            piece.equalsIgnoreCase("Oblique") ||
                            piece.equalsIgnoreCase("BoldItalic") ||
                            piece.equalsIgnoreCase("BoldOblique") ||
                            piece.equalsIgnoreCase("Rounded")))
                        substantialMatch = true;

                    // Increment found pieces counter
                    matches++;
                }
            }

            // Calculate name length difference
            int nameLengthDelta = Math.abs(aName.length() - fontName.length());

            // If we found a substantial piece and this is highest pieces/missingPieces count, cache ttfName et. al.
            if (substantialMatch && (matches > maxMatches || (matches == maxMatches && nameLengthDelta < minNameLengthDelta))) {
                guessFont = font;
                maxMatches = matches;
                minNameLengthDelta = nameLengthDelta;
            }
        }

        // If guessFont is still null, return something
        if (guessFont == null) {
            guessFont = new java.awt.Font(aName, java.awt.Font.PLAIN, 1000);
            if (!name.equals(getFontNameNormalized(guessFont.getFontName())) &&
               !name.equals(getFontNameNormalized(guessFont.getFontName(Locale.ENGLISH))) &&
               !name.equals(getFontNameNormalized(guessFont.getFamily())) &&
               !name.equals(getFontNameNormalized(guessFont.getFamily(Locale.ENGLISH))) &&
               !name.equals(getFontNameNormalized(guessFont.getPSName())))
               System.out.println("FontUtils: Couldn't find font for " + aName + " (using " + guessFont.getFontName() + ")");
        }

        // Return guess font
        return guessFont;
    }

    /**
     * Returns a "cleaned up" or standardized version of the given font name:
     *   1. Remove MT or MS or PS
     *   2. Convert all non alpha numeric characters (essentially just dashes?) to spaces
     *   3. Add space between any adjacent pair of lower-case:upper-case chars
     */
    public static String getFontNameNormalized(String aName)
    {
        // 1. Remove MS, MT and PS
        aName = StringUtils.delete(aName, "MS");
        aName = StringUtils.delete(aName, "MT");
        aName = StringUtils.delete(aName, "PS");

        // Get string buffer for name
        StringBuilder name = new StringBuilder(aName);

        // Iterate over chars
        for (int i = 0; i < name.length() - 1; i++) {

            // 2. Convert any non alpha-numeric characters to space
            if (!Character.isLetterOrDigit(name.charAt(i)))
                name.setCharAt(i, ' ');

            // 3. Add space between any adjacent pair of camel case chars
            if (Character.isLetterOrDigit(name.charAt(i)) && Character.isLowerCase(name.charAt(i)) &&
                Character.isLetterOrDigit(name.charAt(i+1)) && Character.isUpperCase(name.charAt(i+1)))
                name.insert(i+1, ' ');

            // Coalesce adjacent space
            if (name.charAt(i) == ' ' && name.charAt(i+1) == ' ') {
                name.deleteCharAt(i+1);
                i--;
            }
        }

        // Return name
        return name.toString().trim();
    }

    /**
     * Returns an alternate font for given char, if one is found that can display it
     */
    public static Font getAltFont(char aChar)
    {
        Font[] altFonts = getAltFonts();

        // Iterate over alternate fonts and return first that can display
        for (Font font : altFonts) {
            if (font.canDisplay(aChar))
                return font;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the list of suggested alternate fonts.
     */
    public static Font[] getAltFonts()
    {
        if (_altFonts != null) return _altFonts;
        return _altFonts = createAltFonts();
    }

    /**
     * Returns the list of suggested alternate fonts.
     */
    private static Font[] createAltFonts()
    {
        // Yuichi recommended:
        // Japanese, Simplified Chinese, Traditional Chinese, Korean
        // Windows: "MS Gothic", "MS Hei", "MingLiU", "Gulimche"
        // OSX: "Hiragino Kaku Go-W3", "Hei", "Apple LiGothic", "Apple Gothic"
        //String names[] = { "Symbol", "Song", "MS Gothic", "Hiragino Kaku Gothic Pro", "MS Hei", "Hei", "MingLiU",
        //    "Apple LiGothic", "Gulimche", "Apple Gothic", "Song" };
        //for (int i=0; i<names.length; i++) { Font f = Font.getFont(names[i], 12, false);
        //    if (f!=null && !_altFonts.contains(f)) _altFonts.add(f); }

        // Get all system fonts and create alt fonts list with RMFont for each system font
        java.awt.Font[] fonts = getFonts();
        List<Font> altFonts = new ArrayList<>(fonts.length);
        for (java.awt.Font font : fonts)
            altFonts.add(new Font(font.getFontName(), 12));

        // Make sure Symbol is the first font
        altFonts.add(0, new Font("Symbol", 12));

        // Return array
        return altFonts.toArray(new Font[0]);
    }
}