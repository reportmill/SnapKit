package snap.view;

/**
 * A host View that lays out children one after another, wrapping to new row (or col) to fit.
 */
public class FlowView extends ChildView {

    /**
     * Returns preferred width of given parent using RowView layout.
     */
    public static double getPrefWidth(View aPar, double aH)
    {
        // Get parent as proxy
        ViewProxy par = ViewProxy.getProxy(aPar);
        par.setSize(-1, aH);

        // Layout proxy children and return children max x with insets
        layoutProxy(par, false);
        return par.getChildrenMaxXAllWithInsets();
    }

    /**
     * Returns preferred height of given parent using RowView layout.
     */
    public static double getPrefHeight(View aPar, double aW)
    {
        // Get parent as proxy
        ViewProxy par = ViewProxy.getProxy(aPar);
        par.setSize(aW, -1);

        // Layout proxy children and return children max y with insets
        layoutProxy(par, false);
        return par.getChildrenMaxYAllWithInsets();
    }

    /**
     * Performs layout for given parent with given children.
     */
    public static void layout(ParentView aPar, boolean isFillHeight)
    {
        // Get Parent ViewProxy with Children proxies
        ViewProxy par = ViewProxy.getProxy(aPar);
        if (par.getChildren().length==0) return;

        // Do Proxy layout
        layoutProxy(par, isFillHeight);

        // Push layout bounds back to real
        par.setBoundsInClient();
    }

    /**
     * Performs layout for given parent with given children.
     */
    public static void layoutProxy(ViewProxy aPar, boolean isFillHeight)
    {
        // Get children (just return if empty) and create rects
        ViewProxy children[] = aPar.getChildren(); if (children.length==0) return;

        // Iterate over children to break into rows
        for (int i=0; i<children.length; i++) {


        }
    }
}
