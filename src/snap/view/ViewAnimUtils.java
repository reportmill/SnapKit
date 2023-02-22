package snap.view;
import snap.geom.Pos;
import snap.util.Interpolator;

/**
 * Utility methods for ViewAnim.
 */
public class ViewAnimUtils {

    /**
     * Sets a view to be visible/not-visible with anim.
     */
    public static ViewAnim setVisible(View aView, boolean aValue, boolean doWidth, boolean doHeight)
    {
        // If already set, just return
        if (aValue == aView.isVisible())
            return aView.getAnim(0);

        // Get animator
        ViewAnim anim = aView.getAnim(0).finish().clear().getAnim(500);
        anim.setInterpolator(Interpolator.EASE_OUT);

        // Handle show inspector
        if (aValue) {

            // Get old PrefW/PrefH values
            double oldPrefW = aView.isPrefWidthSet() ? aView.getPrefWidth() : -1;
            double oldPrefH = aView.isPrefHeightSet() ? aView.getPrefHeight() : -1;
            boolean oldClipToBounds = aView.isClipToBounds();

            // Make sure view is visible but transparent
            aView.setVisible(true);
            aView.setOpacity(0);
            aView.setClipToBounds(true);

            // If animating width, set PrefWidth from 1 to actual PrefWidth
            if (doWidth) {
                double prefW = aView.getPrefWidth();
                aView.setPrefWidth(1);
                anim.setPrefWidth(prefW);
            }

            // If animating width, set PrefWidth from 1 to actual PrefWidth
            if (doHeight) {
                double prefH = aView.getPrefHeight();
                aView.setPrefHeight(1);
                anim.setPrefHeight(prefH);
            }

            // Animate opacity to 1
            anim.setOpacity(1);

            // On Finish, restore old PrefW, PrefH, ClipToBounds
            anim.setOnFinish(() -> {
                aView.setPrefWidth(oldPrefW);
                aView.setPrefHeight(oldPrefH);
                aView.setClipToBounds(oldClipToBounds);
            });

            // Start anim
            anim.play();
        }

        // Handle hide inspector
        else {

            // Get old PrefW/PrefH values
            double prefW = aView.isPrefWidthSet() ? aView.getPrefWidth() : -1;
            double prefH = aView.isPrefHeightSet() ? aView.getPrefHeight() : -1;
            boolean oldClipToBounds = aView.isClipToBounds();

            // Make sure view is clipping to bounds
            aView.setClipToBounds(true);

            // If animating width, set PrefWidth to 1
            if (doWidth)
                anim.setPrefWidth(1);

            // If animating height, set PrefHeight to 1
            if (doHeight)
                anim.setPrefHeight(1);

            // Animate opacity to zero
            anim.setOpacity(0);

            // On finish, make not visible and restore old PrefW, PrefH, ClipToBounds
            anim.setOnFinish(a -> {
                aView.setVisible(false);
                aView.setPrefWidth(prefW);
                aView.setPrefHeight(prefH);
                aView.setClipToBounds(oldClipToBounds);
            });

            // Start anim
            anim.play();
        }

        // Return anim
        return anim;
    }

    /**
     * Sets the alignment.
     */
    public static void setAlign(View aView, Pos aPos, int aTime)
    {
        // Get par, child
        ParentView par = aView instanceof ParentView ? (ParentView) aView : null; if (par == null) return;
        View child0 = par.getChildCount()>0 ? par.getChild(0) : null; if (child0 == null) return;

        // Get x/y
        double x0 = child0.getX();
        double y0 = child0.getY();

        // Set alignment
        par.setAlign(aPos);
        par.layoutDeep();

        // Get new x/y
        double x1 = child0.getX();
        double y1 = child0.getY();
        for (View child : par.getChildren()) {
            child.setTransX(x0-x1);
            child.setTransY(y0-y1);
            ViewAnim anim = child.getAnimCleared(aTime);
            anim.setTransX(0);
            anim.setTransY(0);
            anim.play();
        }
    }

    /**
     * Adds a item with animation.
     */
    protected static void addSplitViewItemWithAnim(SplitView aSplitView, View aView, double aSize, int anIndex)
    {
        // Add view as item
        aSplitView.addItem(aView, anIndex);

        // Get new Divider for view
        Divider div = anIndex == 0 ? aSplitView.getDivider(0) : aSplitView.getDivider(anIndex - 1);

        // If first view, configure anim for given size as Location
        if (anIndex == 0) {
            div.setLocation(0);
            div.getAnimCleared(500).setValue(Divider.Location_Prop, 1d, aSize).play();
        }

        // If successive view, configure anim for given size as Remainder
        else {
            div.setRemainder(1);
            div.getAnimCleared(500).setValue(Divider.Remainder_Prop, 1d, aSize).play();
        }
    }

    /**
     * Removes a item with animation.
     */
    protected static void removeSplitViewItemWithAnim(SplitView aSplitView, View aView)
    {
        // Get index, divider and Location/Remainder for given view
        int index = aSplitView.indexOfItem(aView);
        Divider div = index == 0 ? aSplitView.getDivider(0) : aSplitView.getDivider(index - 1);
        double size = aSplitView.isVertical() ? aView.getHeight() : aView.getWidth();

        // If first item, set Location animated
        if (index == 0) {
            div.setLocation(size);
            ViewAnim anim = div.getAnim(0).clear();
            anim.getAnim(500).setValue(Divider.Location_Prop, size, 1d);
            anim.setOnFinish(() -> aSplitView.removeItem(aView)).needsFinish().play();
        }

        // If not first item, set Remainder animated
        else {
            div.setRemainder(size);
            ViewAnim anim = div.getAnim(0).clear();
            anim.getAnim(500).setValue(Divider.Remainder_Prop, size, 1d);
            anim.setOnFinish(() -> aSplitView.removeItem(aView)).needsFinish().play();
        }
    }

    /**
     * Sets a child visible with animation.
     */
    protected static void setSplitViewItemVisibleWithAnim(SplitView aSplitView, View aView, boolean aValue)
    {
        // If already set, just return
        if (aValue == aView.isVisible()) return;

        // Get index, divider and size
        int index = aSplitView.indexOfItem(aView), time = 500;
        Divider div = index == 0 ? aSplitView.getDivider(0) : aSplitView.getDivider(index - 1);
        double size = aSplitView.isVertical() ? aView.getHeight() : aView.getWidth();

        // Clear running anims
        aView.getAnimCleared(0);
        div.getAnimCleared(0);

        // Handle show item
        if (aValue) {

            // If first item, set Location
            double dsize = div.getSpan();
            if (index == 0) {
                div.setLocation(0);
                div.getAnim(time).setValue(Divider.Location_Prop, dsize, size).play();
            }

            // If not first item, set Remainder
            else {
                div.setRemainder(1);
                div.getAnim(time).setValue(Divider.Remainder_Prop, dsize, size).play();
            }

            // Show view and divider
            aView.setVisible(true);
            aView.setOpacity(0);
            aView.getAnim(time).setOpacity(1).play();
            div.setOpacity(0);
            div.getAnim(time).setOpacity(1).play();
        }

        // Handle hide item
        else {

            // If first item, set location
            if (index == 0) {
                div.setLocation(size);
                div.getAnim(time).setValue(Divider.Location_Prop, size, 1d).play();
            }

            // If non-first item, set remainder
            else {
                div.setRemainder(size);
                div.getAnim(time).setValue(Divider.Remainder_Prop, size, 1d).play();
            }

            // Clear
            aView.setOpacity(1);
            div.setOpacity(1);
            div.getAnim(time).setOpacity(0).play();

            // Configure anim
            aView.getAnim(time).setOpacity(0).setOnFinish(() -> setItemVisibleWithAnimDone(aSplitView, aView, div, size)).play();
        }
    }

    /**
     * Called when setItemVisibleWithAnim is done.
     */
    private static void setItemVisibleWithAnimDone(SplitView aSplitView, View aView,Divider aDiv, double size)
    {
        aView.setVisible(false);
        aView.setOpacity(1);
        aDiv.setOpacity(1);
        if (aSplitView.isVertical())
            aView.setHeight(size);
        else aView.setWidth(size);
    }
}
