package snap.view;

/**
 * A layout that just delegates back to the view.
 */
public class CallbackLayout extends ViewLayout<View> {

    /**
     * Constructor.
     */
    public CallbackLayout(View aView)
    {
        super(aView);
    }

    @Override
    public double getPrefWidth(double aH)
    {
        return _view.getPrefWidthImpl(aH);
    }

    @Override
    public double getPrefHeight(double aW)
    {
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
}
