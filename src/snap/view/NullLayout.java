package snap.view;

/**
 * A layout that just delegates back to the view.
 */
public class NullLayout extends ViewLayout {

    /**
     * Constructor.
     */
    public NullLayout(View aView)
    {
        super(aView);
    }

    @Override
    public double computePrefWidth(double aH)  { return 0; }

    @Override
    public double computePrefHeight(double aW)  { return 0; }

    @Override
    public void layoutView()  { }
}
