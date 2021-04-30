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
        // Get animator
        ViewAnim anim = aView.getAnim(0).finish().clear().getAnim(500);
        anim.setInterpolator(Interpolator.EASE_OUT);

        // Handle show inspector
        if (aValue) {
            double prefW = aView.isPrefWidthSet() ? aView.getPrefWidth() : -1;
            double prefH = aView.isPrefHeightSet() ? aView.getPrefHeight() : -1;
            if (doWidth)
                aView.setPrefWidth(1);
            if (doHeight)
                aView.setPrefHeight(1);
            aView.setVisible(true);
            if (doWidth)
                anim.setPrefWidth(prefW);
            if (doHeight)
                anim.setPrefHeight(prefH);
            anim.setOpacity(1);
            anim.setOnFinish(() -> {
                aView.setPrefWidth(prefW);
                aView.setPrefHeight(prefH);
            });
            anim.play();
        }

        // Handle hide inspector
        else {
            double prefW = aView.isPrefWidthSet() ? aView.getPrefWidth() : -1;
            double prefH = aView.isPrefHeightSet() ? aView.getPrefHeight() : -1;
            if (doWidth)
                anim.setPrefWidth(1);
            if (doHeight)
                anim.setPrefHeight(1);
            anim.setOpacity(0);
            anim.setOnFinish(a -> {
                aView.setVisible(false);
                aView.setPrefWidth(prefW);
                aView.setPrefHeight(prefH);
            });
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
