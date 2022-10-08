package snap.viewx;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.parse.CodeTokenizer;
import snap.parse.ParseToken;
import snap.text.TextDoc;
import snap.text.TextStyle;
import snap.view.TextArea;
import snap.web.WebURL;

/**
 * A TextArea subclass for showing code.
 */
public class CodeView extends TextArea {

    // Colors
    private static Color  _commentColor = new Color("#3F7F5F"); //336633
    private static Color  _reservedWordColor = new Color("#660033");
    private static Color  _stringLiteralColor = new Color("#C80000"); // CC0000

    /**
     * Creates a new CodeView.
     */
    public CodeView()
    {
        setFill(Color.WHITE);
        setRichText(true);
        setDefaultStyle(new TextStyle(Font.Arial14));
        setEditable(true);
    }

    public void setSource(Object anObj)
    {
        super.setSource(anObj);
        syntaxColor();
    }

    public void setText(String aStr)
    {
        super.setText(aStr);
        syntaxColor();
    }

    /**
     * Performs coloring.
     */
    protected void syntaxColor()
    {
        CodeTokenizer tokenizer = new CodeTokenizer();
        tokenizer.setReadSingleLineComments(true);
        tokenizer.setReadMultiLineComments(true);
        for (String tok : getPatterns()) tokenizer.addPattern(tok, tok);

        tokenizer.setInput(getText());
        TextDoc textDoc = getTextDoc();

        for (ParseToken tok = tokenizer.getNextToken(); tok != null; tok = tokenizer.getNextToken()) {
            String pattern = tok.getPattern();
            ParseToken stok = tok.getSpecialToken();
            if (Character.isLetter(pattern.charAt(0)))
                textDoc.setStyleValue(TextStyle.COLOR_KEY, _reservedWordColor, tok.getStartCharIndex(), tok.getEndCharIndex());
            else if (pattern.startsWith("\\\""))
                textDoc.setStyleValue(TextStyle.COLOR_KEY, _stringLiteralColor, tok.getStartCharIndex(), tok.getEndCharIndex());
            while (stok != null) {
                textDoc.setStyleValue(TextStyle.COLOR_KEY, _commentColor, stok.getStartCharIndex(), stok.getEndCharIndex());
                stok = stok.getSpecialToken();
            }
        }
    }

    /**
     * Returns the patterns.
     */
    protected String[] getPatterns()
    {
        String tokensText = WebURL.getURL(getClass(), "JTokens.txt").getText();
        String[] tokens = tokensText.split("\\s");
        for (int i = 0; i < tokens.length; i++)
            tokens[i] = tokens[i].substring(1, tokens[i].length() - 1);
        return tokens;
    }
}