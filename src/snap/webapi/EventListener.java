package snap.webapi;

/**
 * This class is a wrapper for Web API EventListener (https://developer.mozilla.org/en-US/docs/Web/API/EventListener).
 */
public interface EventListener <E extends Event> {

    void handleEvent(E var1);
}
