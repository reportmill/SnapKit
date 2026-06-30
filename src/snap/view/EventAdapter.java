/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.util.ArrayUtils;
import snap.util.ListUtils;

/**
 * This class manages which node events are sent to which targets.
 */
public class EventAdapter {
    
    // The event filters
    private EventListenerList _filters = EMPTY_LISTENER_LIST;
    
    // The event handlers
    private EventListenerList _handlers = EMPTY_LISTENER_LIST;

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
    public EventListener[] getHandlers()  { return _handlers._listeners; }

    /**
     * Adds an event filter.
     */
    public void addFilter(EventListener aLsnr, EventType... theTypes)
    {
        if (_filters == EMPTY_LISTENER_LIST) _filters = new EventListenerList();
        _filters.addListener(aLsnr, theTypes);
    }

    /**
     * Removes an event filter.
     */
    public void removeFilter(EventListener aLsnr, EventType... theTypes)
    {
        _filters.removeListener(aLsnr, theTypes);
    }

    /**
     * Adds an event handler.
     */
    public void addHandler(EventListener aLsnr, EventType... theTypes)
    {
        if (_handlers == EMPTY_LISTENER_LIST) _handlers = new EventListenerList();
        _handlers.addListener(aLsnr, theTypes);
    }

    /**
     * Removes an event handler.
     */
    public void removeHandler(EventListener aLsnr, EventType... theTypes)
    {
        _handlers.removeListener(aLsnr, theTypes);
    }

    /**
     * Returns whether given type is enabled.
     */
    public boolean isTypeEnabled(EventType aType)  { return isFilterTypeEnabled(aType) || isHandlerTypeEnabled(aType); }

    /**
     * Returns whether given type is enabled for any filter.
     */
    public boolean isFilterTypeEnabled(EventType aType)  { return _filters.isTypeEnabled(aType); }

    /**
     * Returns whether given type is enabled for given filter.
     */
    public boolean isFilterTypeEnabledForFilter(EventType aType, EventListener aFilter)  { return _filters.isTypeEnabledForListener(aType, aFilter); }

    /**
     * Returns whether given type is enabled for any filter.
     */
    public boolean isHandlerTypeEnabled(EventType aType)  { return _handlers.isTypeEnabled(aType); }

    /**
     * Returns whether given type is enabled for given filter.
     */
    public boolean isHandlerTypeEnabledForHandler(EventType aType, EventListener aHandler)  { return _handlers.isTypeEnabledForListener(aType, aHandler); }

    /**
     * This class manages a list of event listeners.
     */
    private static class EventListenerList {

        // The event filters
        protected EventListener[] _listeners = EMPTY_LISTENER_ARRAY;

        // A map of listeners to types
        protected Map <Object,Set<EventType>> _listenerTypes = new HashMap<>();

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
        public void addListener(EventListener aLsnr, EventType... theTypes)
        {
            _listeners = ArrayUtils.addId(_listeners, aLsnr);

            // Add new types and enable
            Set<EventType> eventTypes = _listenerTypes.computeIfAbsent(aLsnr, k -> new HashSet<>());
            for (EventType eventType : theTypes) {
                eventTypes.add(eventType);
                _enabledTypesBitSet.set(eventType.ordinal(), true);
            }
        }

        /**
         * Removes given event listener.
         */
        public void removeListener(EventListener aLsnr, EventType... theTypes)
        {
            // Update types from given types
            Set<EventType> eventTypes = _listenerTypes.get(aLsnr);
            if (eventTypes == null)
                return;
            if (theTypes.length == 0) {
                theTypes = eventTypes.toArray(new EventType[0]);
                eventTypes.clear();
            }
            else for (EventType eventType : theTypes)
                eventTypes.remove(eventType);

            // If empty, remove types for listener
            if (eventTypes.isEmpty())
                _listenerTypes.remove(aLsnr);

            // Reset enabled types
            for (EventType eventType : theTypes) {
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
        public boolean isTypeEnabled(EventType aType)  { return _enabledTypesBitSet.get(aType.ordinal()); }

        /**
         * Returns whether given type is enabled for given listener.
         */
        public boolean isTypeEnabledForListener(EventType aType, EventListener aLsnr)
        {
            Set<EventType> listenerTypes = _listenerTypes.get(aLsnr);
            return listenerTypes != null && listenerTypes.contains(aType);
        }
    }
}