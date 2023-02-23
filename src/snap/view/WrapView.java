package snap.view;
import snap.geom.Insets;
import snap.geom.Point;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.util.MathUtils;

/**
 * A parent view to correctly layout a content view even if rotated and/or scaled.
 */
public class WrapView extends ParentView {

    // Whether to fill width
    private boolean  _fillWidth;

    // Whether to fill height
    private boolean  _fillHeight;

    // The content
    private View  _content;

    // Constants for properties
    public static final String FillWidth_Prop = "FillWidth";
    public static final String FillHeight_Prop = "FillHeight";

    /**
     * Constructor for given view.
     */
    public WrapView(View aView)
    {
        _content = aView;
        addChild(aView);
    }

    /**
     * Constructor for given view.
     */
    public WrapView(View aView, boolean fillWidth, boolean fillHeight)
    {
        this(aView);
        setFillWidth(fillWidth);
        setFillHeight(fillHeight);
    }

    /**
     * Returns whether children will be resized to fill width.
     */
    public boolean isFillWidth()  { return _fillWidth; }

    /**
     * Sets whether children will be resized to fill width.
     */
    public void setFillWidth(boolean aValue)
    {
        if (aValue == _fillWidth) return;
        firePropChange(FillWidth_Prop, _fillWidth, _fillWidth = aValue);
        relayout();
    }

    /**
     * Returns whether children will be resized to fill height.
     */
    public boolean isFillHeight()  { return _fillHeight; }

    /**
     * Sets whether children will be resized to fill height.
     */
    public void setFillHeight(boolean aValue)
    {
        if (aValue == _fillHeight) return;
        firePropChange(FillHeight_Prop, _fillHeight, _fillHeight = aValue);
        relayout();
    }

    /**
     * Returns the content.
     */
    public View getContent()  { return _content; }

    /**
     * Sets the content.
     */
    public void setContent(View aView)
    {
        if (_content != null)
            removeChild(_content);
        _content = aView;
        if (_content != null)
            addChild(_content);
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        if (_content == null)
            return ins.getWidth();

        // Simple case of 90 degrees
        if (isContentAt90()) {
            double childH = _content.getPrefHeight();
            double prefW = childH + ins.getWidth();
            return Math.round(prefW);
        }

        // General case
        double childW = _content.getPrefWidth();
        double childH = _content.getPrefHeight();
        Rect areaBounds = new Rect(0,0, childW, childH);
        Rect areaBounds2 = _content.localToParent(areaBounds).getBounds();
        double areaW = Math.round(areaBounds2.width);
        return areaW + ins.getWidth();
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        if (_content == null)
            return ins.getHeight();

        // Simple case of 90 degrees
        if (isContentAt90()) {
            double childW = _content.getPrefWidth();
            double prefH = childW + ins.getHeight();
            return Math.round(prefH);
        }

        // General case
        double childW = _content.getPrefWidth();
        double childH = _content.getPrefHeight();
        Rect areaBounds = new Rect(0,0, childW, childH);
        Rect areaBounds2 = _content.localToParent(areaBounds).getBounds();
        double areaH = Math.round(areaBounds2.height);
        return areaH + ins.getHeight();
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // If no content, just return
        if (_content == null) return;

        // Get size of parent and content and set
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();

        // Get Child Width/Height - this is me being stupid/lazy
        double childW = Math.min(_content.getPrefWidth(), Math.max(areaW, areaH));
        double childH = _content.getPrefHeight();

        // Still being lazy
        boolean isContentAt90 = isContentAt90();
        if (_fillWidth) {
            if (isContentAt90)
                childH = areaW;
        }
        if (_fillHeight) {
            if (isContentAt90)
                childW = areaH;
        }

        // Set content bounds
        _content.setBounds(0, 0, childW, childH);

        // Get size of content in parent
        Rect areaBounds = new Rect(0,0, childW, childH);
        Rect areaBounds2 = _content.localToParent(areaBounds).getBounds();

        // Get location of content center based on parent align/insets and parent/content sizes
        Pos align = getAlign();
        double alignX = ViewUtils.getAlignX(align.getHPos());
        double alignY = ViewUtils.getAlignY(align.getVPos());
        double childX = areaX + (areaW - areaBounds2.width) * alignX + areaBounds2.width / 2;
        double childY = areaY + (areaH - areaBounds2.height) * alignY + areaBounds2.height / 2;

        // Get center point in content coords, translate to content origin and set content XY local
        Point childXY = _content.parentToLocal(childX, childY);
        childXY.x += _content.getTransX() - childW / 2;
        childXY.y += _content.getTransY() - childH / 2;
        setViewXYLocal(_content, childXY.x, childXY.y);
    }

    /**
     * Sets the view x/y with given point in current local coords such that new origin will be at that point.
     */
    private static void setViewXYLocal(View aView, double aX, double aY)
    {
        double viewX = aView.getX();
        double viewY = aView.getY();
        if (aView.isLocalToParentSimple()) {
            aView.setXY(viewX + aX, viewY + aY);
            return;
        }

        Point p0 = aView.localToParent(0, 0);
        Point p1 = aView.localToParent(aX, aY);
        double newX = viewX + p1.x - p0.x;
        double newY = viewY + p1.y - p0.y;
        aView.setXY(newX, newY);
    }

    /**
     * Returns whether content is at 90 degrees.
     */
    private boolean isContentAt90()
    {
        double rotate = _content.getRotate();
        return MathUtils.equals(Math.abs(rotate), 90);
    }

    /**
     * Override to align to middle.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER; }
}