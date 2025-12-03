package snap.webapi;

/**
 * Base class for objects that wrap JavaScript objects.
 */
public class JSProxy {

    // The JavaScript Blob object
    protected Object _jsObj;

    /**
     * Constructor.
     */
    public JSProxy()
    {
        super();
    }

    /**
     * Constructor.
     */
    public JSProxy(Object jsObj)
    {
        super();
        _jsObj = jsObj;
    }

    /**
     * Returns the JSObject.
     */
    public Object getJS()  { return _jsObj; }

    /**
     * Returns a named member of a JavaScript object.
     */
    public Object getMember(String aName)  { return WebEnv.get().getMember(_jsObj, aName); }

    /**
     * Sets a named member of a JavaScript object.
     */
    public void setMember(String aName, Object aValue)  { WebEnv.get().setMember(_jsObj, aName, aValue); }

    /**
     * Returns a named member of a JavaScript object.
     */
    public String getMemberString(String aName)  { return WebEnv.get().getMemberString(_jsObj, aName); }

    /**
     * Sets a named member of a JavaScript object.
     */
    public void setMemberString(String aName, String aValue)  { WebEnv.get().setMemberString(_jsObj, aName, aValue); }

    /**
     * Returns a named member of a JavaScript object.
     */
    public boolean getMemberBoolean(String aName)  { return WebEnv.get().getMemberBoolean(_jsObj, aName); }

    /**
     * Sets a named member of a JavaScript object.
     */
    public void setMemberBoolean(String aName, boolean aValue)  { WebEnv.get().setMemberBoolean(_jsObj, aName, aValue); }

    /**
     * Returns a named member of a JavaScript object as int.
     */
    public int getMemberInt(String aName)  { return WebEnv.get().getMemberInt(_jsObj, aName); }

    /**
     * Sets a named member of a JavaScript object.
     */
    public void setMemberInt(String aName, int aValue)  { WebEnv.get().setMemberInt(_jsObj, aName, aValue); }

    /**
     * Returns a named member of a JavaScript object as double.
     */
    public double getMemberDouble(String aName)  { return WebEnv.get().getMemberDouble(_jsObj, aName); }

    /**
     * Sets a named member of a JavaScript object as double.
     */
    public void setMemberDouble(String aName, double aValue)  { WebEnv.get().setMemberDouble(_jsObj, aName, aValue); }

    /**
     * Calls a method.
     */
    public Object call(String aName, Object... args)  { return WebEnv.get().call(_jsObj, aName, args); }

    /**
     * Returns an indexed member of a JavaScript object.
     */
    public Object getSlot(int anIndex)  { return WebEnv.get().getSlot(_jsObj, anIndex); }

    /**
     * Sets an indexed member of a JavaScript object.
     */
    public void setSlot(int anIndex, Object aValue)  { WebEnv.get().setSlot(_jsObj, anIndex, aValue); }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return getClass().getName();
    }
}
