package snap.webenv;
import snap.util.Convert;
import snap.util.FormatUtils;
import snap.view.ViewUtils;
import snap.webapi.*;

/**
 * Utility methods.
 */
public class CJUtils {

    // The frame delay - 25 milliseconds (40 frames per second)
    private static final int FRAME_DELAY = 25;

    /**
     * Removes given element from parent by fading it out first.
     */
    public static void removeElementWithFadeAnim(HTMLElement snapLoader, int overTimeMillis)
    {
        // Get opacity and calculate decrement for 20 frames
        String opacityStr = snapLoader.getStyle().getPropertyValue("opacity");
        double opacity = Convert.doubleValue(opacityStr); if (opacity == 0) opacity = 1;
        int frameCount = (int) Math.ceil(overTimeMillis / (double) FRAME_DELAY);
        double opacityDecr = opacity / frameCount;

        // Trigger first frame of anim
        removeElementWithFadeAnimFrame(snapLoader, opacity, opacityDecr);
    }

    /**
     * Runs a frame of removeElementWithFadeAnim().
     */
    private static void removeElementWithFadeAnimFrame(HTMLElement snapLoader, double opacity, double opacityDecr)
    {
        // Get next opacity
        double newOpacity = opacity - opacityDecr;

        // If at opacity end, remove element and return
        if (newOpacity <= 0) {
            Node parentNode = snapLoader.getParentNode();
            if (parentNode != null)
                parentNode.removeChild(snapLoader);
            return;
        }

        // Set new opacity and run again
        snapLoader.getStyle().setProperty("opacity", FormatUtils.formatNum(newOpacity));
        ViewUtils.runDelayed(() -> removeElementWithFadeAnimFrame(snapLoader, newOpacity, opacityDecr), FRAME_DELAY);
    }
}
