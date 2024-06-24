package snap.view;

/**
 * This class represents a user action.
 */
public class SharedAction {

    // Name
    private String _name;

    // Common action names
    public static final String Cut_Action_Name = "Cut";
    public static final String Copy_Action_Name = "Copy";
    public static final String Paste_Action_Name = "Paste";
    public static final String Delete_Action_Name = "Delete";
    public static final String SelectAll_Action_Name = "SelectAll";
    public static final String Undo_Action_Name = "Undo";
    public static final String Redo_Action_Name = "Redo";

    // Common actions
    public static final SharedAction Cut_Action = new SharedAction(Cut_Action_Name);
    public static final SharedAction Copy_Action = new SharedAction(Copy_Action_Name);
    public static final SharedAction Paste_Action = new SharedAction(Paste_Action_Name);
    public static final SharedAction Delete_Action = new SharedAction(Delete_Action_Name);
    public static final SharedAction SelectAll_Action = new SharedAction(SelectAll_Action_Name);
    public static final SharedAction Undo_Action = new SharedAction(Undo_Action_Name);
    public static final SharedAction Redo_Action = new SharedAction(Redo_Action_Name);

    /**
     * Constructor.
     */
    public SharedAction(String aName)
    {
        _name = aName;
    }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }
}
