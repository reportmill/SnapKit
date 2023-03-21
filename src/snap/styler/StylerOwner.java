package snap.styler;
import snap.view.Label;
import snap.view.View;
import snap.view.ViewHost;
import snap.view.ViewOwner;

/**
 * A simple subclass of ViewOwner to work with a Styler.
 */
public class StylerOwner extends ViewOwner {

    // The Styler
    private Styler _styler;

    // The Collapser
    protected Collapser  _collapser;

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

    /**
     * Returns the Collapser.
     */
    public Collapser getCollapser()
    {
        if (_collapser != null) return _collapser;

        // Create/add label
        Label label = Collapser.createLabel("");
        View view = getUI();
        ViewHost host = view.getHost();
        int index = view.indexInHost();
        host.addGuest(label, index);

        // Create Collapser
        _collapser = new Collapser(view, label);

        // Return
        return _collapser;
    }
}
