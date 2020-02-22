package snap.viewx;
import java.util.*;
import snap.gfx.*;
import snap.parse.*;
import snap.text.TextStyle;
import snap.view.TextArea;
import snap.web.WebURL;

/**
 * A TextArea subclass for showing code.
 */
public class CodeView extends TextArea {
    
    // Colors
    static Color      _commentColor = new Color("#3F7F5F"); //336633
    static Color      _reservedWordColor = new Color("#660033");
    static Color      _stringLiteralColor = new Color("#C80000"); // CC0000

/**
 * Creates a new CodeView.
 */
public CodeView()
{
    setFill(Color.WHITE);
    setPlainText(false);
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
    Tokenizer tokenizer = new Tokenizer();
    tokenizer.setReadSingleLineComments(true);
    tokenizer.setReadMultiLineComments(true);
    for(String tok : getPatterns()) tokenizer.addPattern(tok, tok);
    
    tokenizer.setInput(getText());
    
    for(Token tok=tokenizer.getNextToken();tok!=null;tok=tokenizer.getNextToken()) {
        String pattern = tok.getPattern(); Token stok = tok.getSpecialToken();
        if(Character.isLetter(pattern.charAt(0)))
            getTextBox().setStyleValue(TextStyle.COLOR_KEY, _reservedWordColor, tok.getInputStart(), tok.getInputEnd());
        else if(pattern.startsWith("\\\""))
            getTextBox().setStyleValue(TextStyle.COLOR_KEY, _stringLiteralColor, tok.getInputStart(), tok.getInputEnd());
        while(stok!=null) {
            getTextBox().setStyleValue(TextStyle.COLOR_KEY, _commentColor, stok.getInputStart(), stok.getInputEnd());
            stok = stok.getSpecialToken();
        }
    }
    
}

/**
 * Returns the patterns.
 */
protected String[] getPatterns()
{
    List list = new ArrayList();
    String str = WebURL.getURL(getClass(), "JTokens.txt").getText();
    String toks[] = str.split("\\s");
    for(int i=0;i<toks.length;i++) toks[i] = toks[i].substring(1,toks[i].length()-1);
    return toks;
}

}