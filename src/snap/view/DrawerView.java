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

    // The Content view
    private View  _content;

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
        // Configure basic attributes
        setPadding(24, 8, 8, 8);
        setFill(ViewUtils.getBackFill());
        setBorder(Color.GRAY, 1);
        setBorderRadius(BORDER_RADIUS);
        setLean(Pos.TOP_RIGHT);
        setEffect(SHADOW_EFFECT);
        setManaged(false);

        // Configure action
        enableEvents(MousePress, MouseDrag, MouseRelease, MouseEnter, MouseExit, MouseMove);

        // Create/add DrawerLabel
        Label drawerLabel = getDrawerLabel();
        addChild(drawerLabel);

        // Create/add Close box
        View closedBox = getCloseBox();
        addChild(closedBox);
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
    public View getContent()  { return _content; }

    /**
     * Sets the box content.
     */
    public void setContent(View aView)
    {
        // If already set, just return
        if (aView == _content) return;

        // Remove old content
        if (_content != null)
            removeChild(_content);

        // Add new Content in box
        BoxView boxView = new BoxView(aView, true, true);
        boxView.setPadding(1, 1, 1, 1);
        boxView.setBorder(Border.createLoweredBevelBorder());
        _content = boxView;

        // Add new Content
        if (_content != null)
            addChild(_content);
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
        tabButton.setSize(22, 88);
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
        label.setTextFill(Color.DARKGRAY);
        label.setManaged(false);
        label.setLean(Pos.CENTER);
        label.setRotate(-90);

        // Add prop change listener to explicitly set size on any change
        label.addPropChangeListener(pc -> label.setSize(label.getPrefSize()));

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
        drawerLabel.setTextFill(Color.GRAY);
        drawerLabel.setManaged(false);
        drawerLabel.setLean(Pos.TOP_CENTER);

        // Add prop change listener to explicitly set size on any change
        drawerLabel.addPropChangeListener(pc -> drawerLabel.setSize(drawerLabel.getPrefSize()));

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
    }

    /**
     * Returns the view to attach to.
     */
    public ParentView getAttachView()
    {
        return getTabButton().getParent();
    }

    /**
     * Shows the drawer.
     */
    public void show()
    {
        // If already showing, just return
        if (isShowing()) return;

        // Resize to view
        Size size = getPrefSize();
        size.width = Math.max(size.width, getWidth());
        size.height = Math.max(size.height, getHeight());
        setSize(size);
        setOpacity(1);

        // Get attach view and add this DrawerView
        ParentView parView = getAttachView();
        parView.setClipToBounds(true);
        ViewUtils.addChild(parView, this);

        // Adjust DrawerY if needed
        if (getMargin().top == 0 || getMargin().top + getHeight() / 2 > parView.getHeight())
            setDrawerY(-1);

        // Create SlideAnim if not yet set
        if (_slideAnim == null)
            _slideAnim = new ViewAnim(this);

        // Start animate in
        setTransX(size.width);
        _slideAnim.clear().getAnim(800).setTransX(BORDER_RADIUS);
        _slideAnim.play();

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
        _slideAnim.clear().getAnim(800).setTransX(getWidth());
        _slideAnim.setOnFinish(() -> hideDrawerDone());
        _slideAnim.play();

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
        Explode exp = new Explode(this, 25, 25, () -> hideDrawerDone());
        exp.setHostView(getParent().getParent());
        exp.setRunTime(1400);
        exp.play();
    }

    /**
     * Cleanup when hideDrawer animation done.
     */
    protected void hideDrawerDone()
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
            double explodeX = _closeBox.getBounds().getMidX();
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
     * Override to return preferred width of content.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return BoxView.getPrefWidth(this, getContent(), aH);
    }

    /**
     * Override to return preferred height of content.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return BoxView.getPrefHeight(this, getContent(), aW);
    }

    /**
     * Override to layout content.
     */
    protected void layoutImpl()
    {
        BoxView.layout(this, getContent(), true, true);
    }

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