package snap.styler;
import snap.view.DefaultViewController;
import snap.view.ViewController;

/**
 * A simple subclass of view controller to work with a Styler.
 */
public class StylerOwner extends DefaultViewController {

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
