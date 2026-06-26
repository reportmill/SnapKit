/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.util.ArrayUtils;
import snap.util.ListUtils;
import snap.view.ViewEvent.Type;

/**
 * This class manages which node events are sent to which targets.
 */
public class EventAdapter {
    
    // The event filters
    private EventListenerList _filters = EMPTY_LISTENER_LIST;
    
    // The event handlers
    private EventListenerList _handlers = EMPTY_LISTENER_LIST;

    // The handler for view
    private EventListener _viewHandler = e -> {};

    // Array of handlers minus view handler
    private EventListener[] _externalHandlers;

    // Shared empty listener list
    private static final EventListenerList EMPTY_LISTENER_LIST = new EventListenerList();

    /**
     * Constructor.
     */
    public EventAdapter()
    {
        super();
    }

    /**
     * Returns the filters.
     */
    public EventListener[] getFilters()  { return _filters._listeners; }

    /**
     * Returns the handlers.
     */
    private EventListener[] getHandlers()  { return _handlers._listeners; }

    /**
     * Returns the external handlers (handlers excluding view handler).
     */
    public EventListener[] getExternalHandlers()
    {
        if (_externalHandlers != null) return _externalHandlers;
        _externalHandlers = getHandlers();
        if (_externalHandlers.length > 0 && _externalHandlers[0] == _viewHandler)
            _externalHandlers = Arrays.copyOfRange(_externalHandlers, 1, _externalHandlers.length);
        return _externalHandlers;
    }

    /**
     * Adds an event filter.
     */
    public void addFilter(EventListener aLsnr, Type ... theTypes)
    {
        if (_filters == EMPTY_LISTENER_LIST) _filters = new EventListenerList();
        _filters.addListener(aLsnr, theTypes);
    }

    /**
     * Removes an event filter.
     */
    public void removeFilter(EventListener aLsnr, Type ... theTypes)
    {
        _filters.removeListener(aLsnr, theTypes);
    }

    /**
     * Adds an event handler.
     */
    public void addHandler(EventListener aLsnr, Type ... theTypes)
    {
        if (_handlers == EMPTY_LISTENER_LIST) _handlers = new EventListenerList();
        _handlers.addListener(aLsnr, theTypes);
        _externalHandlers = null;
    }

    /**
     * Removes an event handler.
     */
    public void removeHandler(EventListener aLsnr, Type ... theTypes)
    {
        _handlers.removeListener(aLsnr, theTypes);
        _externalHandlers = null;
    }

    /**
     * Called to register types for a listener.
     */
    public void enableEvents(Type ... theTypes)
    {
        addHandler(_viewHandler, theTypes);

        // Make sure view handler is always first
        if (_handlers._listeners.length > 0 && _handlers._listeners[0] != _viewHandler)
            _handlers._listeners = ArrayUtils.moveToFront(_handlers._listeners, _viewHandler);
    }

    /**
     * Called to unregister types for a listener.
     */
    public void disableEvents(Type ... theTypes)  { removeHandler(_viewHandler, theTypes); }

    /**
     * Returns whether given type is enabled.
     */
    public boolean isTypeEnabled(Type aType)  { return isFilterTypeEnabled(aType) || isHandlerTypeEnabled(aType); }

    /**
     * Returns whether given type is enabled for any filter.
     */
    public boolean isFilterTypeEnabled(Type aType)  { return _filters.isTypeEnabled(aType); }

    /**
     * Returns whether given type is enabled for given filter.
     */
    public boolean isFilterTypeEnabledForFilter(Type aType, EventListener aFilter)  { return _filters.isTypeEnabledForListener(aType, aFilter); }

    /**
     * Returns whether given type is enabled for any filter.
     */
    public boolean isHandlerTypeEnabled(Type aType)  { return _handlers.isTypeEnabled(aType); }

    /**
     * Returns whether given type is enabled for given filter.
     */
    public boolean isHandlerTypeEnabledForHandler(Type aType, EventListener aHandler)  { return _handlers.isTypeEnabledForListener(aType, aHandler); }

    /**
     * This class manages a list of event listeners.
     */
    private static class EventListenerList {

        // The event filters
        protected EventListener[] _listeners = EMPTY_LISTENER_ARRAY;

        // A map of listeners to types
        protected Map <Object,Set<Type>> _listenerTypes = new HashMap<>();

        // Bit set of enabled event types indexed by event Type.ordinal()
        private BitSet _enabledTypesBitSet = new BitSet();

        // Shared empty list
        private static final EventListener[] EMPTY_LISTENER_ARRAY = new EventListener[0];

        /**
         * Constructor.
         */
        public EventListenerList()
        {
            super();
        }

        /**
         * Adds a given event listener for given types.
         */
        public void addListener(EventListener aLsnr, ViewEvent.Type ... theTypes)
        {
            _listeners = ArrayUtils.addId(_listeners, aLsnr);

            // Add new types and enable
            Set<Type> eventTypes = _listenerTypes.computeIfAbsent(aLsnr, k -> new HashSet<>());
            for (Type eventType : theTypes) {
                eventTypes.add(eventType);
                _enabledTypesBitSet.set(eventType.ordinal(), true);
            }
        }

        /**
         * Removes given event listener.
         */
        public void removeListener(EventListener aLsnr, ViewEvent.Type ... theTypes)
        {
            // Update types from given types
            Set<Type> eventTypes = _listenerTypes.get(aLsnr);
            if (eventTypes == null)
                return;
            if (theTypes.length == 0) {
                theTypes = eventTypes.toArray(new Type[0]);
                eventTypes.clear();
            }
            else for (Type eventType : theTypes)
                eventTypes.remove(eventType);

            // If empty, remove types for listener
            if (eventTypes.isEmpty())
                _listenerTypes.remove(aLsnr);

            // Reset enabled types
            for (Type eventType : theTypes) {
                boolean enabled = ListUtils.hasMatch(_listenerTypes.values(), set -> set.contains(eventType));
                _enabledTypesBitSet.set(eventType.ordinal(), enabled);
            }

            // If no listener types left, remove listener
            if (_listenerTypes.get(aLsnr) == null)
                _listeners = ArrayUtils.remove(_listeners, aLsnr);
        }

        /**
         * Returns whether given type is enabled.
         */
        public boolean isTypeEnabled(Type aType)  { return _enabledTypesBitSet.get(aType.ordinal()); }

        /**
         * Returns whether given type is enabled for given listener.
         */
        public boolean isTypeEnabledForListener(Type aType, EventListener aLsnr)
        {
            Set<Type> listenerTypes = _listenerTypes.get(aLsnr);
            return listenerTypes != null && listenerTypes.contains(aType);
        }
    }
}