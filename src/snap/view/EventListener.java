/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * An interface for objects to receive Node events.
 */
public interface EventListener {

    // The method called to send an event to listener
    void fireEvent(ViewEvent anEvent);

}