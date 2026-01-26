package snap.view;
import snap.geom.Polygon;
import snap.geom.Pos;
import snap.gfx.*;

/**
 * A view to show up to four arrows.
 */
public class ArrowView extends ParentView {

    // The ColView to hold up/down buttons
    private ColView _col;

    // The arrow buttons
    private Button _upButton, _downButton, _leftButton, _rightButton;

    // The last pressed button
    private Button _lastButton;

    // The arrow images
    private static Image _upImage, _downImage, _leftImage, _rightImage;

    /**
     * Creates a new ArrowView.
     */
    public ArrowView()
    {
        super();
        _align = Pos.CENTER;

        // Enable Action event
        setActionable(true);

        // Create/configure Col view to hold up/down buttons
        _col = new ColView();
        _col.setAlign(Pos.CENTER);
        addChild(_col);

        // Enable up/down buttons by default
        setShowUp(true);
        setShowDown(true);
    }

    /**
     * Returns whether to show up button.
     */
    public boolean isShowUp()  { return _upButton != null; }

    /**
     * Sets whether to show up button.
     */
    public void setShowUp(boolean aValue)
    {
        // If already set, just return
        if (aValue == isShowUp()) return;

        // Add button
        if (aValue) {
            _upButton = createArrowButton(14, 9);
            _upButton.setBorderRadius(3);
            _upButton.setPosition(Pos.TOP_CENTER);
            _upButton.setImage(getUpArrowImage());
            _col.addChild(_upButton, 0);
        }

        // Remove button
        else {
            _col.removeChild(_upButton);
            _upButton = null;
        }
    }

    /**
     * Returns whether to show down button.
     */
    public boolean isShowDown()  { return _downButton != null; }

    /**
     * Sets whether to show down button.
     */
    public void setShowDown(boolean aValue)
    {
        // If already set, just return
        if (aValue == isShowDown()) return;

        // Add button
        if (aValue) {
            _downButton = createArrowButton(14, 9);
            _downButton.setPosition(Pos.BOTTOM_CENTER);
            _downButton.setImage(getDownArrowImage());
            _col.addChild(_downButton);
        }

        // Remove button
        else {
            _col.removeChild(_downButton);
            _downButton = null;
        }
    }

    /**
     * Returns whether to show left button.
     */
    public boolean isShowLeft()  { return _leftButton != null; }

    /**
     * Sets whether to show left button.
     */
    public void setShowLeft(boolean aValue)
    {
        // If already set, just return
        if (aValue == isShowLeft()) return;

        // Add button
        if (aValue) {
            _leftButton = createArrowButton(9, 14);
            _leftButton.setPosition(Pos.CENTER_LEFT);
            _leftButton.setImage(getLeftArrowImage());
            addChild(_leftButton, 0);
        }

        // Remove button
        else {
            removeChild(_leftButton);
            _leftButton = null;
        }
    }

    /**
     * Returns whether to show right button.
     */
    public boolean isShowRight()  { return _rightButton != null; }

    /**
     * Sets whether to show right button.
     */
    public void setShowRight(boolean aValue)
    {
        // If already set, just return
        if (aValue == isShowRight()) return;

        // Add button
        if (aValue) {
            _rightButton = createArrowButton(9, 14);
            _rightButton.setPosition(Pos.CENTER_RIGHT);
            _rightButton.setImage(getRightArrowImage());
            addChild(_rightButton);
        }

        // Remove button
        else {
            removeChild(_rightButton);
            _rightButton = null;
        }
    }

    /**
     * Returns whether last button was up.
     */
    public boolean isUp()  { return _lastButton == _upButton; }

    /**
     * Returns whether last button was down.
     */
    public boolean isDown()  { return _lastButton == _downButton; }

    /**
     * Returns whether last button was left.
     */
    public boolean isLeft()  { return _lastButton == _leftButton; }

    /**
     * Returns whether last button was right.
     */
    public boolean isRight()  { return _lastButton == _rightButton; }

    /**
     * Called when button fires.
     */
    protected void buttonDidFire(Button aBtn)
    {
        _lastButton = aBtn;
        fireActionEvent(null);
    }

    /**
     * Returns an image of a up arrow.
     */
    public static Image getUpArrowImage()
    {
        if (_upImage != null) return _upImage;
        return _upImage = createArrowImage(9, 7, new Polygon(1.5, 5.5, 7.5, 5.5, 4.5, 1.5));
    }

    /**
     * Returns an image of a down arrow.
     */
    public static Image getDownArrowImage()
    {
        if (_downImage != null) return _downImage;
        return _downImage = createArrowImage(9, 7, new Polygon(1.5, 1.5, 7.5, 1.5, 4.5, 5.5));
    }

    /**
     * Returns an image of a left arrow.
     */
    public static Image getLeftArrowImage()
    {
        if (_leftImage != null) return _leftImage;
        return _leftImage = createArrowImage(7, 9, new Polygon(5.5, 1.5, 5.5, 7.5, 1.5, 4.5));
    }

    /**
     * Returns an image of a right arrow.
     */
    public static Image getRightArrowImage()
    {
        if (_rightImage != null) return _rightImage;
        return _rightImage = createArrowImage(7, 9, new Polygon(1.5, 1.5, 1.5, 7.5, 5.5, 4.5));
    }

    /**
     * Override to return row layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new RowViewLayout(this, false); }

    /**
     * Creates an arrow button.
     */
    private Button createArrowButton(int buttonW, int buttonH)
    {
        Button button = new Button();
        button.setMinSize(buttonW, buttonH);
        button.setPadding(0, 0, 0,0);
        button.setBorderRadius(3);
        button.addEventHandler(e -> buttonDidFire(button), Action);
        return button;
    }

    /**
     * Creates an arrow image.
     */
    private static Image createArrowImage(int imageW, int imageH, Polygon imageShape)
    {
        Image img = Image.getImageForSize(imageW, imageH, true);
        Painter pntr = img.getPainter();
        pntr.setColor(Color.DARKGRAY);
        pntr.draw(imageShape);
        pntr.fill(imageShape);
        return img;
    }
}