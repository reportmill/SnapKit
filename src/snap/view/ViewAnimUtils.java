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
}
