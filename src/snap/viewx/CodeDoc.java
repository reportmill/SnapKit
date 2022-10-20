/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.parse.CodeTokenizer;
import snap.parse.ParseToken;
import snap.parse.Tokenizer;
import snap.text.*;
import snap.web.WebURL;
import java.util.ArrayList;
import java.util.List;

/**
 * This TextDoc subclass loads Java code with syntax coloring.
 */
public class CodeDoc extends TextDoc {

    // The tokenizer
    private CodeTokenizer  _tokenizer;

    // Constants for Syntax Coloring
    private static Color COMMENT_COLOR = new Color("#3F7F5F"); //336633
    private static Color RESERVED_WORD_COLOR = new Color("#660033");
    private static Color STRING_LITERAL_COLOR = new Color("#C80000"); // CC0000

    /**
     * Constructor.
     */
    public CodeDoc()
    {
        super();

        setDefaultStyle(new TextStyle(Font.Arial14));

        // Create/config Tokenizer with comments support and Java patterns
        _tokenizer = new CodeTokenizer();
        _tokenizer.setReadSingleLineComments(true);
        _tokenizer.setReadMultiLineComments(true);
        String[] javaTokenPatterns = getPatterns();
        for (String token : javaTokenPatterns)
            _tokenizer.addPattern(token, token);
    }

    /**
     * Override to create tokens.
     */
    @Override
    protected TextToken[] createTokensForTextLine(TextLine aTextLine)
    {
        // Get iteration vars
        List<TextToken> tokens = new ArrayList<>();
        TextRun textRun = aTextLine.getRun(0);

        // Get first token in line
        Exception exception = null;
        ParseToken parseToken = null;
        try { parseToken = getNextToken(aTextLine); }
        catch (Exception e) {
            exception = e;
            System.out.println("JavaTextDoc.createTokensForTextLine: Parse error: " + e);
        }

        // Get line parse tokens and create TextTokens
        while (parseToken != null) {

            // Get token start/end
            int tokenStart = parseToken.getStartCharIndex();
            int tokenEnd = parseToken.getEndCharIndex();

            // Create TextToken
            TextToken textToken = new TextToken(aTextLine, tokenStart, tokenEnd, textRun);
            textToken.setName(parseToken.getName());
            tokens.add(textToken);

            // Get/set token color
            Color color = getColorForParseToken(parseToken);
            if (color != null)
                textToken.setTextColor(color);

            // Get next token
            try { parseToken = getNextToken(null); }
            catch (Exception e) {
                exception = e;
                parseToken = null;
                System.out.println("JavaTextDoc.createTokensForTextLine: Parse error: " + e);
            }
        }

        // If exception was hit, create token for rest of line
        if (exception != null) {
            int tokenStart = _tokenizer.getCharIndex();
            int tokenEnd = aTextLine.length();
            TextToken textToken = new TextToken(aTextLine, tokenStart, tokenEnd, textRun);
            tokens.add(textToken);
        }

        // Return
        return tokens.toArray(new TextToken[0]);
    }

    /**
     * Returns the next token.
     */
    private ParseToken getNextToken(TextLine aTextLine)
    {
        // If TextLine provided, do set up
        if (aTextLine != null) {

            // If this line is InMultilineComment (do this first, since it may require use of Text.Tokenizer)
            TextLine prevTextLine = aTextLine.getPrevious();
            TextToken prevTextLineLastToken = prevTextLine != null ? prevTextLine.getLastToken() : null;
            boolean inUnterminatedComment = isTextTokenUnterminatedMultilineComment(prevTextLineLastToken);

            // Reset input for Tokenizer
            _tokenizer.setInput(aTextLine);

            // Get first line token: Handle if already in Multi-line
            if (inUnterminatedComment)
                return _tokenizer.getMultiLineCommentTokenMore();
        }

        // Return next token
        return _tokenizer.getNextSpecialTokenOrToken();
    }

    /**
     * Returns whether given TextToken is an unterminated comment.
     */
    private boolean isTextTokenUnterminatedMultilineComment(TextToken aTextToken)
    {
        if (aTextToken == null)
            return false;
        String name = aTextToken.getName();
        if (name != Tokenizer.MULTI_LINE_COMMENT)
            return false;
        String tokenStr = aTextToken.getString();
        if (tokenStr.endsWith("*/"))
            return false;
        return true;
    }

    /**
     * Checks the given token for syntax coloring.
     */
    public static Color getColorForParseToken(ParseToken aToken)
    {
        // Handle comments
        String tokenName = aToken.getName();
        if (tokenName == CodeTokenizer.SINGLE_LINE_COMMENT || tokenName == CodeTokenizer.MULTI_LINE_COMMENT)
            return COMMENT_COLOR;

        // Handle reserved words
        String tokenPattern = aToken.getPattern();
        char firstPatternChar = tokenPattern.charAt(0);
        if (Character.isLetter(firstPatternChar))
            return RESERVED_WORD_COLOR;

        // Handle string literals
        if (tokenPattern.startsWith("\\\"") || tokenPattern.startsWith("\\\'"))
            return STRING_LITERAL_COLOR;

        // Return none
        return null;
    }

    /**
     * Returns the patterns.
     */
    private static String[] getPatterns()
    {
        WebURL tokensURL = WebURL.getURL(CodeDoc.class, "JTokens.txt");
        String tokensText = tokensURL.getText();
        String[] tokens = tokensText.split("\\s");
        for (int i = 0; i < tokens.length; i++)
            tokens[i] = tokens[i].substring(1, tokens[i].length() - 1);
        return tokens;
    }

    /**
     * Returns a new CodeDoc from given source.
     */
    public static CodeDoc newFromSource(Object aSource)
    {
        CodeDoc codeDoc = new CodeDoc();
        codeDoc.setSource(aSource);
        return codeDoc;
    }
}
