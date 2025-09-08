package snap.viewx;
import java.util.*;

import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Transform;
import snap.gfx.*;
import snap.util.Interpolator;
import snap.view.*;

/**
 * A demo to show explosions.
 * <p>
 * Things to do:
 * - Change rotation animation to be zero
 * - Declare variable for PIECE_COUNT and replace occurances of constant "10"
 * - Add Slider with bounds 220,220,220,20
 */
public class Explode extends View {

    // The view to be exploded
    private View _view;

    // The parent to hold explosion
    private ParentView _host;

    // The image to be exploded
    private Image _image;

    // The width/height of grid
    private int _gridW, _gridH;

    // The run time of the explosion in millis
    private int _runTime = 1000;

    // The function to be called when done
    private Runnable _onDone;

    // Whether to remove view when finished
    private boolean _removeOnDone;

    // Whether to restore view opacity when finished
    private boolean _restoreOnDone;

    // Whether to reverse (construct)
    private boolean _reverse;

    // Width/height of image/view pieces
    private double _iw, _ih, _vw, _vh;

    // The offset from this view to explode view
    private Point _offset;
    private double _scale = 1;

    // The array of fragments
    private Frag[][] _frags;

    // The interpolator
    private Interpolator _interp = Interpolator.EASE_OUT;

    // Constants
    private static Random _rand = new Random();

    /**
     * Constructor.
     */
    public Explode(View aView, int aGridWidth, int aGridHeight)
    {
        this(aView, aGridWidth, aGridHeight, null);
    }

    /**
     * Constructor.
     */
    public Explode(View aView, int aGridWidth, int aGridHeight, Runnable aRun)
    {
        _view = aView;
        _gridW = aGridWidth;
        _gridH = aGridHeight;
        _onDone = aRun;
        setHostView(_view.getParent());
    }

    /**
     * Sets the image.
     */
    public Explode setImage(Image anImage)
    {
        _image = anImage;
        return this;
    }

    /**
     * Sets whether to remove view when explode is done.
     */
    public void removeOnDone()  { _removeOnDone = true; }

    /**
     * Sets whether to restore view opacity when finished.
     */
    public void restoreOnDone()  { _restoreOnDone = true; }

    /**
     * Sets the view that to holds this view for display.
     */
    public Explode setHostView(ParentView aView)
    {
        // Just return if null
        if (aView == null) return this;

        // Set host
        _host = aView;

        // Calculate offset from host view to victim view
        _offset = Point.ZERO;
        for (View v = _view; v != _host; v = v.getParent())
            _offset = _offset.addXY(v.getX(), v.getY());
        for (View v = _view; v != _host; v = v.getParent())
            _scale *= v.getScaleX();

        // Other stuff
        setManaged(false);
        setPickable(false);
        setScale(_scale);
        setSize(_host.getWidth(), _host.getHeight());
        setPrefSize(_host.getWidth(), _host.getHeight());
        return this;
    }

    /**
     * Sets the runtime.
     */
    public Explode setRunTime(int aValue)
    {
        _runTime = aValue;
        return this;
    }

    /**
     * Reverses the effect (construct instead of explode).
     */
    public Explode reverse()
    {
        _interp = Interpolator.EASE_IN;
        _reverse = !_reverse;
        return this;
    }

    /**
     * Configures explosion animation.
     */
    public void configure()
    {
        // Create image if needed
        if (_image == null) {
            _view.setOpacity(1);
            _image = ViewUtils.getImageForScale(_view, 1);
        }

        // Set sizes for image/view pieces
        _iw = _image.getWidth() / _gridW;
        _ih = _image.getHeight() / _gridH;
        _vw = _view.getWidth() / _gridW;
        _vh = _view.getHeight() / _gridH;

        // Create explode pieces
        _frags = new Frag[_gridW][_gridH];
        for (int i = 0; i < _gridW; i++)
            for (int j = 0; j < _gridH; j++)
                _frags[i][j] = getFrag(i * _iw, j * _ih, i * _vw + _offset.x, j * _vh + _offset.y);

        // Configure this view and add to parent
        ViewUtils.addChild(_host, this);

        // Start animation with hooks to call animFrame and animFinish
        ViewAnim anim0 = getAnim(0);
        anim0.setOnFrame(() -> animDidFrame());
        anim0.setOnFinish(() -> animDidFinish());
        getAnim(_runTime).play();

        // Hide view
        _view.setOpacity(0);

        // If reversed, configure frags exploded
        if (_reverse)
            animDidFrame();
    }

    /**
     * Plays explosion animation.
     */
    public void play()
    {
        if (getParent() == null)
            configure();
        getAnim(0).play();
    }

    /**
     * Plays explosion animation and restores view when done.
     */
    public void playAndRestore()
    {
        _restoreOnDone = true;
        configure();
        getAnim(0).play();
    }

    /**
     * Plays explosion after delay.
     */
    public void playDelayed(int aDelay)
    {
        if (aDelay > 0) ViewUtils.runDelayed(() -> play(), aDelay);
        else play();
    }

    /**
     * Returns a frag for given rect of image.
     */
    Frag getFrag(double aX, double aY, double dX, double dY)
    {
        // Create random destination for piece
        double angle = Math.toRadians(_rand.nextDouble() * 360);
        double dist = 100 + _rand.nextDouble() * 200;
        double x1 = dX + dist * Math.sin(angle);
        double y1 = dY + dist * Math.cos(angle);

        // Create new frag
        Frag frag = new Frag();
        frag.ix = aX;
        frag.iy = aY;
        frag.x = frag.x0 = dX;
        frag.y = frag.y0 = dY;
        frag.x1 = x1;
        frag.y1 = y1;

        // Create random rotation and duration of piece
        if (_iw > 4 || _ih > 4)
            frag.rot1 = _rand.nextDouble() * 720 - 360;

        // Create random duration of piece
        int varTime = _runTime / 4;
        frag.time = _runTime - varTime + _rand.nextInt(varTime);

        // Return
        return frag;
    }

    /**
     * Called on each frame.
     */
    void animDidFrame()
    {
        // Get current anim time
        int time = getAnim(0).getTime();
        if (_reverse) time = Math.max(_runTime - time, 0);

        // Update frags and paint
        Rect rect = updateFrags(time);
        repaint(rect);
    }

    /**
     * Called when finished.
     */
    void animDidFinish()
    {
        // Remove original view from parent
        if (_removeOnDone && _view.getParent() != null) {
            ViewUtils.removeChild(_view.getParent(), _view);
            _view.setOpacity(1);
        }

        // Restore original view
        if (_restoreOnDone)
            _view.setOpacity(1);

        // Remove this view from parent
        ViewUtils.removeChild(getParent(), this);

        // Call OnDone
        if (_onDone != null)
            _onDone.run();
    }

    /**
     * Updates frags and return repaint rect.
     */
    private Rect updateFrags(double aTime)
    {
        // Create vars to track repaint rect
        double x0 = Float.MAX_VALUE, y0 = Float.MAX_VALUE, x1 = -x0, y1 = -y0;

        // Iterate over frags and update
        for (int i = 0; i < _gridW; i++)
            for (int j = 0; j < _gridH; j++) {
                Frag f = _frags[i][j];
                updateFrag(f, aTime);
                x0 = Math.min(x0, f.x - _vw);
                y0 = Math.min(y0, f.y - _vh);
                x1 = Math.max(x1, f.x + _vw);
                y1 = Math.max(y1, f.y + _vh);
            }

        // Return repaint rect
        Rect rect = new Rect(x0, y0, x1 - x0, y1 - y0);
        if (_reverse) rect.inset(-10);
        return rect;
    }

    /**
     * Updates a given frag.
     */
    private void updateFrag(Frag aFrag, double aTime)
    {
        double ratio = aTime / aFrag.time;
        if (ratio > 1) ratio = 1;
        aFrag.x = _interp.getValue(ratio, aFrag.x0, aFrag.x1);
        aFrag.y = _interp.getValue(ratio, aFrag.y0, aFrag.y1);
        aFrag.rot = _interp.getValue(ratio, 0, aFrag.rot1);
        aFrag.op = 1 - ratio;
    }

    /**
     * Override to paint frags.
     */
    protected void paintFront(Painter aPntr)
    {
        for (int i = 0; i < _gridW; i++)
            for (int j = 0; j < _gridH; j++) {
                Frag f = _frags[i][j];
                paintFrag(aPntr, f);
            }
        aPntr.setOpacity(1);
    }

    /**
     * Paints a given frag.
     */
    private void paintFrag(Painter aPntr, Frag aFrag)
    {
        double ix = aFrag.ix, iy = aFrag.iy;
        double vx = aFrag.x, vy = aFrag.y;
        Transform xfm = new Transform(vx + _iw / 2, vy + _ih / 2);
        xfm.rotate(aFrag.rot);
        xfm.translate(-_iw / 2, -_ih / 2);
        aPntr.save();
        aPntr.setOpacity(aFrag.op);
        aPntr.transform(xfm);
        aPntr.drawImage(_image, ix, iy, _iw, _ih, 0, 0, _vw, _vh);
        aPntr.restore();
    }

    /**
     * A class to represent a fragment of exploded image.
     */
    private static class Frag {

        // The x/y of image piece
        double ix, iy;

        // The dest coordinates, rotation and opacity
        double x, y, rot, op = 1;

        // The original and final coordinates view piece
        double x0, y0, x1, y1, rot1;

        // The final time
        double time;
    }
}