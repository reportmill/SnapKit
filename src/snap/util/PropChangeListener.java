package snap.util;

/**
 * An interface to respond to PropChange objects.
 */
public interface PropChangeListener {

    /** Called when there is a property change. */
    void propertyChange(PropChange aPC);

}