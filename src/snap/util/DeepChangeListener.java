/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * A listener to get property changes and nested property changes.
 */
public interface DeepChangeListener {

    /**
     * Deep property changes (as well as normal property changes).
     */
    void deepChange(Object aSource, PropChange anEvent);
}
