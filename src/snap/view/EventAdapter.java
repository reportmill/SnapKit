/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.util.ArrayUtils;
import snap.view.ViewEvent.Type;

/**
 * This class manages which node events are sent to which targets.
 */
public class EventAdapter {
    
    // Bit set of enabled events
    BitSet                  _bitset = new BitSet();
    
    // A map of listeners to types
    Map <Object,Set<Type>>  _types = new HashMap();
    
    // The event filters
    EventListener           _filters[] = _emptyLsnrs;
    
    // The event handlers
    EventListener           _handlers[] = _emptyLsnrs;
    
    // Shared empty list
    private static final EventListener _emptyLsnrs[] = new EventListener[0];

/**
 * Called to regsiter types for a listener.
 */
public void enableEvents(Object aLsnr, Type ... theTypes)
{
    Set <Type> types = _types.get(aLsnr); if(types==null) _types.put(aLsnr, types=new HashSet());
    Collections.addAll(types, theTypes);
    for(Type typ : theTypes) setEnabled(typ, true);
}

/**
 * Called to unregsiter types for a listener.
 */
public void disableEvents(Object aLsnr, Type ... theTypes)
{
    // Update types for given Listener removed types
    Set <Type> types = _types.get(aLsnr); if(types==null) return;
    if(theTypes==null || theTypes.length==0) types.clear();
    else for(Type t : theTypes) types.remove(t); if(types.size()==0) _types.remove(aLsnr);
    
    // Get types to turn off (types that are currently enabled, but no longer in all types)
    Set <Type> allTypes = new HashSet(), offTypes = new HashSet();
    for(Map.Entry <Object,Set<Type>> entry : _types.entrySet()) allTypes.addAll(entry.getValue());
    for(Type t : getEnabledEvents()) if(!allTypes.contains(t)) offTypes.add(t);
    for(Type t : offTypes) setEnabled(t, false);
}

/**
 * Returns whether given type is enabled.
 */
public boolean isEnabled(Type aType)  { return _bitset.get(aType.ordinal()); }

/**
 * Sets whether a given type is enabled.
 */
public void setEnabled(Type aType, boolean aValue)
{
    if(isEnabled(aType)==aValue) return;
    _bitset.set(aType.ordinal(), aValue);
}

/**
 * Returns an array of enabled events.
 */
public Type[] getEnabledEvents()
{
    List <Type> types = new ArrayList();
    for(Type type : Type.values()) if(isEnabled(type)) types.add(type);
    return types.toArray(new Type[types.size()]);
}

/**
 * Adds an event filter.
 */
public void addFilter(EventListener aLsnr, ViewEvent.Type ... theTypes)
{
    _filters = ArrayUtils.addId(_filters, aLsnr);
    enableEvents(aLsnr, theTypes);
}

/**
 * Removes an event filter.
 */
public void removeFilter(EventListener aLsnr, ViewEvent.Type ... theTypes)
{
    disableEvents(aLsnr, theTypes);
    if(_types.get(aLsnr)==null)
        _filters = ArrayUtils.remove(_filters, aLsnr);
}

/**
 * Adds an event handler.
 */
public void addHandler(EventListener aLsnr, ViewEvent.Type ... theTypes)
{
    _handlers = ArrayUtils.addId(_handlers, aLsnr);
    enableEvents(aLsnr, theTypes);
}

/**
 * Removes an event handler.
 */
public void removeHandler(EventListener aLsnr, ViewEvent.Type ... theTypes)
{
    disableEvents(aLsnr, theTypes);
    if(_types.get(aLsnr)==null)
        _handlers = ArrayUtils.remove(_handlers, aLsnr);
}

}