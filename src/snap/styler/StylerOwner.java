package snap.styler;
import snap.view.ViewOwner;

/**
 * A simple subclass of ViewOwner to work with a Styler.
 */
public class StylerOwner extends ViewOwner {

    // The Styler
    private Styler _styler;

    /**
     * Returns the styler.
     */
    public Styler getStyler()  { return _styler; }

    /**
     * Sets the styler.
     */
    public void setStyler(Styler aStyler)
    {
        _styler = aStyler;
    }
}
