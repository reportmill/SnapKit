/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.util.Convert;
import snap.viewx.Explode;

/**
 * A View that slides in/out of another view.
 */
public class DrawerView extends ParentView {

    // The animation time
    private int _animTime = 1000;

    // The View to attach to
    private ParentView _attachView;

    // The view that holds the content
    private BoxView _contentBox;

    // The button tab used to trigger drawer
    private Button  _tabButton;

    // The label on the tab button
    private Label  _tabLabel;

    // The label at the top of drawer
    private Label  _drawerLabel;

    // The CloseBox
    private View  _closeBox;

    // Dedicated anim so nothing interferes with drawer open/close
    private ViewAnim  _slideAnim;

    // Whether drawer is currently hiding
    private boolean  _hiding;

    // The size of the draw last sized
    private Rect  _minBounds;

    // Some drag vars
    private boolean  _mouseDragged;
    private Point  _mouseDownPnt;
    private double  _mouseDownY, _mouseDownW, _mouseDownH;
    private boolean  _resizingDrawer, _resizingDrawerTop;

    // Constants
    private static final Effect SHADOW_EFFECT = new ShadowEffect(10, Color.GRAY, 0, 0);
    private static final int BORDER_RADIUS = 5;

    // The ratio used to center draw when first shown (.5 would be center)
    private static final double CENTERING_RATIO = .67;

    // Constants for properties
    public static final String DrawerY_Prop = "DrawerY";

    /**
     * Constructor.
     */
    public DrawerView()
    {
        super();
        setLean(Pos.TOP_RIGHT);
        setManaged(false);

        // Create add content box
        _contentBox = new BoxView(null, true, true);
        addChild(_contentBox);

        // Make decorated by default
        setDecorated(true);
    }

    /**
     * Constructor for given view.
     */
    public DrawerView(View aView)
    {
        this();
        setContent(aView);
    }

    /**
     * Returns the box content.
     */
    public View getContent()  { return _contentBox.getContent(); }

    /**
     * Sets the box content.
     */
    public void setContent(View aView)
    {
        _contentBox.setContent(aView);
    }

    /**
     * Returns the animation time.
     */
    public int getAnimTime()  { return _animTime; }

    /**
     * Sets the animation time.
     */
    public void setAnimTime(int aValue)
    {
        if (aValue == getAnimTime()) return;
        _animTime = aValue;
    }

    /**
     * Returns whether decorated.
     */
    public boolean isDecorated()  { return getCloseBox().getParent() != null; }

    /**
     * Sets whether decorated.
     */
    public void setDecorated(boolean aValue)
    {
        if (aValue == isDecorated()) return;

        // Configure decorated
        if (aValue) {

            // Basic attributes
            setPadding(24, 8, 8, 8);
            setFill(ViewUtils.getBackFill());
            setBorder(Color.GRAY, 1);
            setBorderRadius(BORDER_RADIUS);
            setEffect(SHADOW_EFFECT);

            // Create/add DrawerLabel
            Label drawerLabel = getDrawerLabel();
            addChild(drawerLabel);

            // Create/add Close box
            View closedBox = getCloseBox();
            addChild(closedBox);

            // Configure content box
            _contentBox.setPadding(1, 1, 1, 1);
            _contentBox.setBorder(Border.createLoweredBevelBorder());

            // Enable events
            enableEvents(MousePress, MouseDrag, MouseRelease, MouseEnter, MouseExit, MouseMove);
        }

        // Configure plain
        else {
            setPadding(Insets.EMPTY);
            setFill(null);
            setBorder(null);
            setBorderRadius(0);
            setEffect(null);
            removeChild(getDrawerLabel());
            removeChild(getCloseBox());
            _contentBox.setPadding(Insets.EMPTY);
            _contentBox.setBorder(null);
            disableEvents(MousePress, MouseDrag, MouseRelease, MouseEnter, MouseExit, MouseMove);
        }
    }

    /**
     * Returns the tab button.
     */
    public Button getTabButton()
    {
        // If already set, return
        if (_tabButton != null) return _tabButton;

        // Create/configure TabButton
        Button tabButton = new Button();
        tabButton.setPrefSize(22, 88);
        tabButton.setManaged(false);
        tabButton.setLean(Pos.CENTER_RIGHT);
        tabButton.setPosition(Pos.CENTER_LEFT);
        tabButton.setEffect(new ShadowEffect(5, Color.GRAY, 0, 0));

        // Add tab label
        Label tabLabel = getTabLabel();
        ViewUtils.addChild(tabButton, tabLabel);

        // Add EventHandler to call show when clicked
        tabButton.addEventHandler(e -> toggleDrawer(), View.Action);

        // Set, return
        return _tabButton = tabButton;
    }

    /**
     * Returns the tab label.
     */
    public Label getTabLabel()
    {
        // If already set, just return
        if (_tabLabel != null) return _tabLabel;

        // Create/configure
        Label label = new Label();
        label.setFont(Font.Arial12.getBold());
        label.setTextColor(Color.DARKGRAY);
        label.setManaged(false);
        label.setLean(Pos.CENTER);
        label.setRotate(-90);

        // Set, return
        return _tabLabel = label;
    }

    /**
     * Returns the drawer label.
     */
    public Label getDrawerLabel()
    {
        // If already set, just return
        if (_drawerLabel != null) return _drawerLabel;

        // Create/configure
        Label drawerLabel = new Label();
        drawerLabel.setPadding(6, 0, 0, 0);
        drawerLabel.setFont(Font.Arial12.getBold());
        drawerLabel.setTextColor(Color.GRAY);
        drawerLabel.setManaged(false);
        drawerLabel.setLean(Pos.TOP_CENTER);

        // Set, return
        return _drawerLabel = drawerLabel;
    }

    /**
     * Returns the close box view.
     */
    protected View getCloseBox()
    {
        // If already set, just return
        if (_closeBox != null) return _closeBox;

        // Create/configure
        Polygon poly = new Polygon(0, 2, 2, 0, 5, 3, 8, 0, 10, 2, 7, 5, 10, 8, 8, 10, 5, 7, 2, 10, 0, 8, 3, 5);
        ShapeView closeBox = new ShapeView(poly);
        closeBox.setManaged(false);
        closeBox.setLean(Pos.TOP_RIGHT);
        closeBox.setBorder(Color.BLACK, .5);
        closeBox.setMargin(7, 14, 0, 0);
        closeBox.setPrefSize(11, 11);

        // Set, return
        return _closeBox = closeBox;
    }

    /**
     * Sets the CloseBox highlight.
     */
    protected void setCloseBoxHighlight(boolean aValue)
    {
        View closeBox = getCloseBox();
        closeBox.setFill(aValue ? Color.PINK : null);
        closeBox.setBorder(Color.BLACK, aValue ? 1 : .5);
    }

    /**
     * Shows the tab button in given view.
     */
    public void showTabButton(ParentView aView)
    {
        View btn = getTabButton();
        ViewUtils.addChild(aView, btn);
        _attachView = aView;
    }

    /**
     * Returns the view to attach to.
     */
    public ParentView getAttachView()  { return _attachView; }

    /**
     * Sets the view to attach to.
     */
    public void setAttachView(ParentView aView)
    {
        _attachView = aView;
    }

    /**
     * Shows the drawer.
     */
    public void show()  { showDrawer(_attachView); }

    /**
     * Shows the drawer.
     */
    public void showDrawer(ParentView parentView)
    {
        // If already showing, just return
        if (isShowing()) return;

        // Reset attach view
        setAttachView(parentView);

        // Resize to view
        Size size = getPrefSize();
        double drawerW = Math.max(size.width, getWidth());
        double drawerH = Math.max(size.height, getHeight());
        setSize(drawerW, drawerH);
        setOpacity(1);

        // Get attach view and add this DrawerView
        parentView.setClipToBounds(true);
        ViewUtils.addChild(parentView, this);

        // Adjust DrawerY if needed
        if (getLean() == Pos.TOP_RIGHT && (getMargin().top == 0 || getMargin().top + getHeight() / 2 > parentView.getHeight()))
            setDrawerY(-1);

        // Animate drawer in
        animateShowDrawer();

        // Hide the TabButton
        getTabButton().setVisible(false);
    }

    /**
     * Hides the drawer.
     */
    public void hide()
    {
        // If hidden, just return
        if (!isShowing() || _hiding) return;

        // Animate out
        _hiding = true;
        animateHideDrawer();

        // Show the TabButton
        getTabButton().setVisible(true);
    }

    /**
     * Animates show drawer (move in).
     */
    private void animateShowDrawer()
    {
        // Create SlideAnim if not yet set
        if (_slideAnim == null)
            _slideAnim = new ViewAnim(this);

        // Start animate in
        switch (getLeanX()) {
            case RIGHT -> {
                setTransX(getWidth());
                _slideAnim.clear().getAnim(getAnimTime()).setTransX(getBorderRadius());
            }
            case CENTER -> {
                setTransY(-getHeight());
                _slideAnim.clear().getAnim(getAnimTime()).setTransY(-getBorderRadius());
            }
        }
        _slideAnim.play();
    }

    /**
     * Animates hide drawer (move out).
     */
    private void animateHideDrawer()
    {
        switch (getLeanX()) {
            case RIGHT -> _slideAnim.clear().getAnim(getAnimTime()).setTransX(getWidth());
            case CENTER -> _slideAnim.clear().getAnim(getAnimTime()).setTransY(-getHeight());
        }
        _slideAnim.setOnFinish(this::handleAnimateHideDrawerFinished);
        _slideAnim.play();
    }

    /**
     * Cleanup when hideDrawer animation done.
     */
    protected void handleAnimateHideDrawerFinished()
    {
        _hiding = false;
        ParentView parView = getAttachView();
        ViewUtils.removeChild(parView, this);
        setOpacity(1);
        setMaximized(false);

        // Show the TabButton
        getTabButton().setVisible(true);
    }

    /**
     * Hides the drawer.
     */
    public void explode()
    {
        // If hidden, just return
        if (!isShowing() || _hiding) return;

        // Animate out
        _hiding = true;
        Explode exp = new Explode(this, 25, 25, this::handleAnimateHideDrawerFinished);
        exp.setHostView(getParent().getParent());
        exp.setRunTime(1400);
        exp.play();
    }

    /**
     * Shows/Hides DrawerView.
     */
    public void toggleDrawer()
    {
        if (isShowing()) hide();
        else show();
    }

    /**
     * Returns whether drawer is maximized.
     */
    public boolean isMaximized()  { return _minBounds != null; }

    /**
     * Resize the drawer to maximum size for view.
     */
    public void setMaximized(boolean aValue)
    {
        // If already set, just return
        if (aValue == isMaximized()) return;

        // If setting, grow window
        if (aValue) {
            double pw = getParent().getWidth();
            double ph = getParent().getHeight();
            double nw = pw - 12;
            double nh = ph - 24;
            _minBounds = getBounds();
            _slideAnim.clear().getAnim(400).setWidth(nw).setHeight(nh).setValue(DrawerY_Prop, 12).play();
        }

        // Otherwise, shrink size
        else {

            // Get bounds
            double newY = _minBounds.y;
            double newW = _minBounds.width;
            double newH = _minBounds.height;
            _minBounds = null;

            // Handle not showing
            if (!isShowing()) {
                setSize(newW, newH);
                setDrawerY(newY);
            }

            // Handle showing
            else {
                _slideAnim.clear().getAnim(400).setWidth(newW).setHeight(newH).setValue(DrawerY_Prop, newY).play();
            }
        }
    }

    /**
     * ProcessEvent.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MousePress
        if (anEvent.isMousePress()) {

            // Clear MouseDownPoint and if not in margin, just bail
            _mouseDownPnt = null;
            if (!inMargin(anEvent))
                return;

            // Set MouseDown vars
            _mouseDownPnt = anEvent.getPoint(getParent());
            _mouseDownY = getMargin().top;
            _mouseDownW = getWidth();
            _mouseDownH = getHeight();
            _mouseDragged = false;
            _resizingDrawer = inResizeCorner(anEvent);
            _resizingDrawerTop = inResizeTopCorner(anEvent);
        }

        // Handle MouseDrag
        else if (anEvent.isMouseDrag()) {

            // If no MouseDownPoint, bail
            if (_mouseDownPnt == null) return;

            // Get new point and change
            Point pnt = anEvent.getPoint(getParent());
            double dx = pnt.x - _mouseDownPnt.x;
            double dy = pnt.y - _mouseDownPnt.y;

            // Either resize or reposition
            if (_resizingDrawer)
                setDrawerSize(_mouseDownW - dx, _mouseDownH + dy);
            else if (_resizingDrawerTop) {
                setDrawerSize(_mouseDownW - dx, _mouseDownH - dy);
                setDrawerY(_mouseDownY + dy);
            }
            else setDrawerY(Math.max(_mouseDownY + dy, 0));

            // If significant change, set MouseDragged
            if (Math.abs(dx) > 2 || Math.abs(dy) > 2)
                _mouseDragged = true;
        }

        // Handle MouseRelease
        if (anEvent.isMouseRelease()) {

            // Clear MouseDownPoint
            _mouseDownPnt = null;

            // If click was inside content, just return
            if (_mouseDragged || !inMargin(anEvent))
                return;

            // Toggle drawer
            double explodeX = _closeBox.getMidX();
            if (anEvent.getX() > explodeX)
                explode();
            else toggleDrawer();
        }

        // Handle MouseEnter, MouseExit, MouseMove
        else if (anEvent.isMouseEnter() || anEvent.isMouseMove())
            setCloseBoxHighlight(inMargin(anEvent));

            // Handle MouseExit
        else if (anEvent.isMouseExit())
            setCloseBoxHighlight(false);
    }

    /**
     * Sets the drawer Y relative to parent.
     */
    private void setDrawerY(double aY)
    {
        // Get Y value (if less than zero, adjust to place drawer in middle of parent)
        double marginTop = aY;
        if (marginTop < 0) {
            double extraH = getParent().getHeight() - getHeight();
            marginTop = Math.round(extraH * CENTERING_RATIO);
        }

        // Get margin, adjust and update (just return if already at Y)
        Insets margin = getMargin().clone();
        if (margin.top == marginTop)
            return;
        margin.top = marginTop;
        setMargin(margin);
    }

    /**
     * Sets the drawer Y relative to parent.
     */
    private void setDrawerSize(double aW, double aH)
    {
        Size minSize = getMinSize();
        double newW = Math.max(aW, minSize.width);
        double newH = Math.max(aH, minSize.height);
        setSize(newW, newH);
        relayoutParent();
    }

    /**
     * Returns whether event point is in margin.
     */
    private boolean inMargin(ViewEvent anEvent)
    {
        Rect contentRect = getBoundsLocal().getInsetRect(getInsetsAll());
        boolean inContent = contentRect.contains(anEvent.getPoint());
        return !inContent;
    }

    /**
     * Returns whether event point is bottom corner.
     */
    private boolean inResizeCorner(ViewEvent anEvent)
    {
        Insets ins = getInsetsAll();
        Rect bounds = getBoundsLocal();
        bounds.setRect(0, bounds.height - ins.bottom, ins.left, ins.bottom);
        return bounds.contains(anEvent.getPoint());
    }

    /**
     * Returns whether event point is top corner.
     */
    private boolean inResizeTopCorner(ViewEvent anEvent)
    {
        Insets ins = getInsetsAll();
        Rect bounds = getBoundsLocal();
        bounds.setRect(0, 0, ins.left, ins.top);
        return bounds.contains(anEvent.getPoint());
    }

    /**
     * Override to return box layout.
     */
    @Override
    protected ViewLayout<?> getViewLayoutImpl()  { return new BoxViewLayout<>(this, _contentBox, true, true); }

    /**
     * Override to handle DrawerY.
     */
    public Object getPropValue(String aPropName)
    {
        if (aPropName == DrawerY_Prop)
            return getMargin().top;
        return super.getPropValue(aPropName);
    }

    /**
     * Override to handle DrawerY.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        if (aPropName == DrawerY_Prop)
            setDrawerY(Convert.doubleValue(aValue));
        else super.setPropValue(aPropName, aValue);
    }
}