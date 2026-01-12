package snap.view;

/**
 * A layout that delegates back to the view.
 */
public class NullLayout extends ViewProxy<View> {

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
        View view = getView();
        if (view.isPrefWidthSet())
            return view.getPrefWidth(aH);
        return view.getPrefWidthImpl(aH);
    }

    @Override
    public double getPrefHeight(double aW)
    {
        View view = getView();
        if (view.isPrefHeightSet())
            return view.getPrefHeight(aW);
        return view.getPrefHeightImpl(aW);
    }

    @Override
    public void layoutView()
    {
        if (getView() instanceof ParentView parentView) {
            parentView.layoutImpl();

            // Copy children bounds from parent view layout back to child layouts
            ViewProxy<?>[] children = getChildren();
            if (children != null) {
                for (ViewProxy<?> child : children) {
                    View childView = child.getView();
                    if (childView != null)
                        child.setBounds(childView.getX(), childView.getY(), childView.getWidth(), childView.getHeight());
                }
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
