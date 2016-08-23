package snap.gfx;

/**
 * A custom class.
 */
public interface Paint {

    /**
     * Returns whether paint is defined in terms independent of primitive to be filled.
     */
    public boolean isAbsolute();
    
    /**
     * Returns whether paint is opaque.
     */
    public boolean isOpaque();
    
    /**
     * Returns an absolute paint for given bounds of primitive to be filled.
     */
    public Paint copyFor(Rect aRect);

}