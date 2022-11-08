package snap.viewx;
import snap.util.KeyChain;
import snap.view.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A ViewOwner subclass that supports binding.
 */
public class BindingViewOwner extends ViewOwner {

    // A map of binding values not explicitly defined in model
    private Map<String,Object> _modelValues = new HashMap<>();

    // A map of maps that perform value conversions
    private Map<String,Map>  _conversionMaps = new HashMap<>();

    /**
     * Returns the map of maps, each of which is used to perform value conversions.
     */
    public Map <String,Map> getConversionMaps()  { return _conversionMaps; }

    /**
     * Adds a binding to a UI view.
     */
    public void addViewBinding(Object anObj, String aPropName, String aKeyPath)
    {
        View view = getView(anObj);
        view.addBinding(aPropName, aKeyPath);
    }

    /**
     * Reset bindings for UI view (recurses for children).
     */
    protected void resetViewBindings(View aView)
    {
        // If Owner of view doesn't match, just return
        //ViewHelper helper = aView.getHelper(); if (helper.isValueAdjusting()) return;
        Object owner = aView.getOwner(); if (owner!=this) return;

        // Iterate over view bindings and reset
        for (Binding binding : aView.getBindings())
            setBindingViewValue(binding);

        // Iterate over view children and recurse
        if (aView instanceof ParentView) {
            ParentView pview = (ParentView) aView;
            for (View child : pview.getChildren())
                resetViewBindings(child);
        }
    }

    /**
     * Returns the UI view value for the given binding.
     */
    protected Object getBindingViewValue(Binding aBinding)
    {
        // Get value from UI view
        View view = aBinding.getView(View.class); if (view == null) return null;
        Object value = view.getPropValue(aBinding.getPropertyName());

        // If conversion key is present, do conversion
        String convKey = aBinding.getConversionKey();
        if (convKey != null)
            value = getConversionMapKey(convKey, value);

        // If binding format is available, try to parse
        //if (aBinding.getFormat()!=null && value instanceof String)
        //    try { value = aBinding.getFormat().parseObject((String)value); } catch(Exception e) { }

        // Return value
        return value;
    }

    /**
     * Sets the view value for the given binding from the key value.
     */
    protected void setBindingViewValue(Binding aBinding)
    {
        View view = aBinding.getView(View.class); if (view == null) return;
        String pname = aBinding.getPropertyName();
        Object value = getBindingModelValue(aBinding);
        view.setPropValue(pname, value);
    }

    /**
     * Returns the key value for a given binding.
     */
    protected Object getBindingModelValue(Binding aBinding)
    {
        // Get binding key and value
        String key = aBinding.getKey();
        Object value = getModelValue(key);

        // If conversion key is present, do conversion
        String convKey = aBinding.getConversionKey();
        if (convKey != null)
            value = getConversionMapValue(convKey, value);

        // If format is present, format value
        //if (aBinding.getFormat()!=null && value!=null)
        //    try { value = aBinding.getFormat().format(value); }
        //    catch(Exception e) { System.err.println("ViewOwner.getBindingKeyValue: " + e); }

        // This is probably the wrong thing to do - maybe should be in JTextComponentHpr somehow
        if (value instanceof Date)
            value = DateFormat.getDateInstance(DateFormat.MEDIUM).format(value);

        // Return value
        return value;
    }

    /**
     * Sets the key value for the given binding from the UI view.
     */
    protected void setBindingModelValue(Binding aBinding)
    {
        Object value = getBindingViewValue(aBinding); // Get value from view
        setModelValue(aBinding.getKey(), value); // Set value in model
    }

    /**
     * Returns a named map to perform value conversions.
     */
    public Map <String,String> getConversionMap(String aName)
    {
        Map map = _conversionMaps.get(aName);
        if (map==null)
            _conversionMaps.put(aName, map = new HashMap());
        return map;
    }

    /**
     * Converts a UI view value to binder object value using conversion key map.
     */
    protected Object getConversionMapKey(String aConversionMapName, Object aValue)
    {
        // Get conversion map (just return original object if null)
        Map <String, String> map = getConversionMap(aConversionMapName); if (map==null) return aValue;
        for (Map.Entry entry : map.entrySet()) // Return key for value (just return original object if null)
            if (entry.getValue().equals(aValue.toString()))
                return entry.getKey();
        return aValue.toString(); // Return original object, since value not found in conversion map
    }

    /**
     * Converts a binder object value to UI view using conversion key map.
     */
    public Object getConversionMapValue(String aConversionMapName, Object aKey)
    {
        Map map = getConversionMap(aConversionMapName); if (map==null) return aKey;
        String value = (String)map.get(aKey.toString());
        return value!=null? value : aKey.toString();
    }

    /**
     * Returns the model value for given key expression from this ViewOwner.
     */
    public Object getModelValue(String aKey)
    {
        Object value = KeyChain.getValue(this, aKey);
        if (value == null) value = KeyChain.getValue(_modelValues, aKey);
        return value;
    }

    /**
     * Sets the model value for given key expression and value for this ViewOwner.
     */
    public void setModelValue(String aKey, Object aValue)
    {
        try { KeyChain.setValue(this, aKey, aValue); }
        catch(Exception e) { KeyChain.setValueSafe(_modelValues, aKey, aValue); }
    }

    /**
     * Called to reset bindings and resetUI().
     */
    @Override
    protected void processResetUI()
    {
        boolean old = setSendEventDisabled(true);
        try {
            resetViewBindings(getUI()); // Reset bindings
            this.resetUI();
        }
        finally { setSendEventDisabled(old); }
    }

    /**
     * Called to invoke respondUI().
     */
    @Override
    protected void processEvent(ViewEvent anEvent)
    {
        // Get binding for property name and have it retrieve value
        View view = anEvent.getView();
        Binding binding = anEvent.isActionEvent() ? view.getBinding("Value") : null;
        if (binding != null)
            setBindingModelValue(binding);

        // Do normal version
        super.processEvent(anEvent);
    }
}
