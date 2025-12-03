package snap.webapi;

/**
 * This class is a wrapper for Web API CanvasGradient (https://developer.mozilla.org/en-US/docs/Web/API/CanvasGradient).
 */
public class CanvasGradient extends JSProxy {

    /**
     * Constructor.
     */
    public CanvasGradient(Object gradientJS)
    {
        super(gradientJS);
    }

    /**
     * Adds a color stop for given offset and color.
     */
    public void addColorStop(double offset, String color)  { call("addColorStop", offset, color); }
}
