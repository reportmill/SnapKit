package snap.util;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.text.TextStyle;

import java.util.Arrays;
import java.util.List;

/**
 * Utility methods for Markdown.
 */
public class MarkdownUtils {

    // header styles
    private static TextStyle _headerStyle1, _headerStyle2;

    // The content style
    private static TextStyle  _contentStyle;

    // The code style
    private static TextStyle  _codeStyle;

    /**
     * Returns the header level for given header node.
     */
    public static int getHeaderLevel(MarkdownNode headerNode)
    {
        Object headerLevelObj = headerNode.getAttributeValue(MarkdownNode.HEADER_LEVEL);
        return headerLevelObj instanceof Number headerLevel ? headerLevel.intValue() : 0;
    }

    /**
     * Returns a Jepl string for Markdown string.
     */
    public static String getJeplForJMD(String className, CharSequence markdownChars)
    {
        MarkdownNode rootMarkdownNode = new MarkdownParser().parseMarkdownChars(markdownChars);
        List<MarkdownNode> rootNodes = rootMarkdownNode.getChildNodes();

        StringBuilder sb = new StringBuilder();
        int methodCount = 0;
        for (MarkdownNode node : rootNodes) {
            if (node.getNodeType() == MarkdownNode.NodeType.RunBlock) {
                sb.append("public static void method").append(methodCount++).append("()\n{\n");
                sb.append(node.getText());
                sb.append("\n}\n\n");
            }
        }

        // Append main method
        sb.append("public static void main(String[] args)\n{\n");
        sb.append("    new snap.viewx.JMDViewer(").append(className).append(".class);");
        sb.append("\n}\n\n");

        // Return string
        return sb.toString();
    }

    /**
     * Returns the header 1 style.
     */
    public static TextStyle getHeaderStyleForLevel(int headerLevel)
    {
        if (headerLevel == 1)
            return getHeaderStyle1();
        return getHeaderStyle2();
    }

    /**
     * Returns the header 1 style.
     */
    private static TextStyle getHeaderStyle1()
    {
        if (_headerStyle1 != null) return _headerStyle1;

        // Create, configure, return
        TextStyle textStyle = TextStyle.DEFAULT;
        Font headerFont = new Font("Arial Bold", 34);
        Color headerColor = Color.BLACK;
        return _headerStyle1 = textStyle.copyForStyleValues(headerFont, headerColor);
    }

    /**
     * Returns the header 2 style.
     */
    private static TextStyle getHeaderStyle2()
    {
        if (_headerStyle2 != null) return _headerStyle2;

        // Create, configure, return
        TextStyle textStyle = TextStyle.DEFAULT;
        Font headerFont = new Font("Arial Bold", 24);
        Color headerColor = Color.BLACK;
        return _headerStyle2 = textStyle.copyForStyleValues(headerFont, headerColor);
    }

    /**
     * Returns the content style.
     */
    public static TextStyle getContentStyle()
    {
        // If already set, just return
        if (_contentStyle != null) return _contentStyle;

        // Create, configure
        TextStyle textStyle = TextStyle.DEFAULT;
        Font contentFont = Font.Arial16;
        Color contentColor = Color.BLACK;
        TextStyle contentStyle = textStyle.copyForStyleValues(contentFont, contentColor);

        // Set, return
        return _contentStyle = contentStyle;
    }

    /**
     * Returns the code style.
     */
    public static TextStyle getCodeStyle()
    {
        // If already set, just return
        if (_codeStyle != null) return _codeStyle;

        // Create, configure
        Font codeFont = Font.getCodeFontForSize(13);
        TextStyle textStyle = TextStyle.DEFAULT;
        Color codeColor = Color.GRAY3;
        TextStyle codeStyle = textStyle.copyForStyleValues(codeFont, codeColor);

        // Set, return
        return _codeStyle = codeStyle;
    }

    /**
     * Processes raw text block into string the way Java compiler would.
     */
    protected static String getStringForTextBlock(String textBlock)
    {
        // Split into lines
        String[] lines = textBlock.split("\n", -1);

        // Determine common leading whitespace (ignoring empty lines)
        int minIndent = Integer.MAX_VALUE;
        for (String line : lines) {
            int leadingSpaces = CharSequenceUtils.getIndentLength(line);
            if (leadingSpaces > 0 && leadingSpaces < line.length())
                minIndent = Math.min(minIndent, leadingSpaces);
        }

        // Remove last line if blank
        if (lines.length > 0 && lines[lines.length - 1].isBlank())
            lines = Arrays.copyOf(lines, lines.length - 1);

        // Remove common leading whitespace and handle escape sequences
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            String trimmedLine = line.length() >= minIndent ? line.substring(minIndent) : line;
            trimmedLine = trimmedLine.replace("\\s", " ");
            result.append(trimmedLine).append("\n");
        }

        // Remove the final newline
        if (!result.isEmpty())
            result.setLength(result.length() - 1);

        return result.toString();
    }
}
