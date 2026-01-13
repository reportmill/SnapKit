package snap.view;

/**
 * A layout that delegates back to the view.
 */
public class NullLayout extends ViewLayout<View> {

    /**
     * Constructor.
     */
    public NullLayout(View aView)
    {
        super(aView);
    }

    @Override
    public double getPrefWidth(double aH)
    {
        if (_view.isPrefWidthSet())
            return _view.getPrefWidth(aH);
        return _view.getPrefWidthImpl(aH);
    }

    @Override
    public double getPrefHeight(double aW)
    {
        if (_view.isPrefHeightSet())
            return _view.getPrefHeight(aW);
        return _view.getPrefHeightImpl(aW);
    }

    @Override
    public void layoutView()
    {
        if (_view instanceof ParentView parentView) {
            parentView.layoutImpl();

            // Copy children bounds from parent view layout back to child layouts
            ViewLayout<?>[] children = getChildren();
            for (ViewLayout<?> child : children) {
                View childView = child.getView();
                if (childView != null)
                    child.setBounds(childView.getX(), childView.getY(), childView.getWidth(), childView.getHeight());
            }
        }
    }

    @Override
    protected double getPrefWidthImpl(double aH)  { return 0; }

    @Override
    protected double getPrefHeightImpl(double aW)  { return 0; }

    @Override
    public void layoutProxy()  { }
}
