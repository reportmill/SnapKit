package snap.util;

/**
 * A listener to get property changes and nested property changes.
 */
public interface DeepChangeListener {

    /**
     * Deep property changes (as well as normal property changes).
     */
    void deepChange(PropChangeListener aSource, PropChange anEvent);
}
