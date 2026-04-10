package snap.view;

/**
 * This class represents the name of a style state.
 */
public class PseudoClass {

    // The name
    private final String _name;

    // Common classes
    public static final PseudoClass Hover = new PseudoClass("Hover");
    public static final PseudoClass Active = new PseudoClass("Active");
    public static final PseudoClass Selected = new PseudoClass("Selected");
    public static final PseudoClass Alternate = new PseudoClass("Alternate");
    public static final PseudoClass Link = new PseudoClass("Link");
    public static final PseudoClass Visited = new PseudoClass("Visited");
    public static final PseudoClass Focus = new PseudoClass("Focus");

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
     * Returns a pseudo class for given string.
     */
    public static PseudoClass valueOf(String name) { return new PseudoClass(name); }
}
