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
    
    // The event filters
    protected EventListener[] _filters = EMPTY_LISTENER_ARRAY;
    
    // The event handlers
    protected EventListener[] _handlers = EMPTY_LISTENER_ARRAY;

    // A map of listeners to types
    protected Map <Object,Set<Type>> _types = new HashMap<>();

    // Bit set of enabled event types indexed by event Type.ordinal()
    private BitSet _enabledTypesBitSet = new BitSet();

    // Shared empty list
    private static final EventListener[] EMPTY_LISTENER_ARRAY = new EventListener[0];

    /**
     * Constructor.
     */
    public EventAdapter()
    {
        super();
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
        if (_types.get(aLsnr) == null)
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
        if (_types.get(aLsnr) == null)
            _handlers = ArrayUtils.remove(_handlers, aLsnr);
    }

    /**
     * Called to register types for a listener.
     */
    public void enableEvents(Object aLsnr, Type ... theTypes)
    {
        // Get Types for Listener
        Set<Type> eventTypes = _types.get(aLsnr);
        if (eventTypes == null)
            _types.put(aLsnr, eventTypes = new HashSet<>());

        // Add new types and enable
        Collections.addAll(eventTypes, theTypes);
        for (Type eventType : theTypes)
            setEnabled(eventType, true);
    }

    /**
     * Called to unregister types for a listener.
     */
    public void disableEvents(Object aLsnr, Type ... theTypes)
    {
        // Update types for given Listener removed types
        Set<Type> eventTypes = _types.get(aLsnr);
        if (eventTypes == null)
            return;

        // Update types from given types
        if (theTypes == null || theTypes.length == 0)
            eventTypes.clear();
        else for (Type t : theTypes)
            eventTypes.remove(t);

        // If empty, remove types for listener
        if (eventTypes.isEmpty())
            _types.remove(aLsnr);

        // Reset enabled types
        resetEnabledTypes();
    }

    /**
     * Returns whether given type is enabled.
     */
    public boolean isEnabled(Type aType)
    {
        return _enabledTypesBitSet.get(aType.ordinal());
    }

    /**
     * Sets whether a given type is enabled.
     */
    public void setEnabled(Type aType, boolean aValue)
    {
        if (isEnabled(aType) == aValue) return;
        _enabledTypesBitSet.set(aType.ordinal(), aValue);
    }

    /**
     * Resets the enabled types.
     */
    private void resetEnabledTypes()
    {
        _enabledTypesBitSet.clear();
        for (Map.Entry <Object,Set<Type>> entry : _types.entrySet()) {
            Set<Type> enabledTypes = entry.getValue();
            enabledTypes.forEach(type -> setEnabled(type, true));
        }
    }

    /**
     * Clears the adapter.
     */
    public void clear()
    {
        _types.clear();
        _filters = _handlers = EMPTY_LISTENER_ARRAY;
        _enabledTypesBitSet.clear();
    }
}