package snap.util;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a style sheet.
 */
public class StyleSheet {

    // The list of rules
    private List<Rule> _rules;

    /**
     * Constructor.
     */
    public StyleSheet()
    {
        _rules = new ArrayList<>();
    }

    /**
     * Returns the rules.
     */
    public List<Rule> getRules()  { return _rules; }

    /**
     * Adds a style rule.
     */
    public void addRule(Rule styleRule)
    {
        _rules.add(styleRule);
    }

    /**
     * This class represents a style rule.
     */
    public record Rule(Selector selector, List<Declaration> declarations) {
        @Override
        public String toString() {
            return selector.fullName() + " { " + ListUtils.joinStrings(declarations, "; ") + " }";
        }
    }

    /**
     * This class represents a style rule selector.
     */
    public record Selector(String name, String pseudoClass) {
        public String fullName() {
            String fullName = name();
            if (pseudoClass != null) fullName += ':' + pseudoClass;
            return fullName;
        }
    }

    /**
     * This class represents a style rule declaration.
     */
    public record Declaration(String key, String value) {
        @Override
        public String toString()  { return key + ": " + value; }
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder("StyleSheet {\n");
        _rules.forEach(rule -> sb.append("    ").append(rule.toString()).append("\n"));
        return sb.append("}\n").toString();
    }

    /**
     * Creates a style sheet for given string.
     */
    public static StyleSheet createStyleSheetForString(String styleSheetString)
    {
        return new StyleSheetParser().parseString(styleSheetString);
    }
}
