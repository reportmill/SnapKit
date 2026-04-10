package snap.view;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the name of a style state.
 */
public class PseudoClass {

    // The name
    private final String _name;

    // All instances map
    private static Map<String,PseudoClass> _pseudoClasses = new HashMap<>();

    // Common classes
    public static final PseudoClass Normal = valueOf("Normal");
    public static final PseudoClass Hover = valueOf("Hover");
    public static final PseudoClass Active = valueOf("Active");
    public static final PseudoClass Selected = valueOf("Selected");
    public static final PseudoClass Alternate = valueOf("Alternate");
    public static final PseudoClass Link = valueOf("Link");
    public static final PseudoClass Visited = valueOf("Visited");
    public static final PseudoClass Focus = valueOf("Focus");

    /**
     * Constructor.
     */
    private PseudoClass(String name)
    {
        _name = name;
    }

    /**
     * Returns the name.
     */
    public String name()  { return _name; }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()  { return _name; }

    /**
     * Returns the pseudo class for given string.
     */
    public static PseudoClass valueOf(String name)
    {
        PseudoClass pseudoClass = _pseudoClasses.get(name);
        if (pseudoClass != null)
            return pseudoClass;
        _pseudoClasses.put(name, pseudoClass = new PseudoClass(name));
        return pseudoClass;
    }
}
