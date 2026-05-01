package snap.games;
import snap.util.ClassUtils;
import snap.util.SnapEnv;
import snap.view.*;
import snap.web.WebURL;

/**
 * The controller class for a StageView.
 */
public class GameController extends DefaultViewController {

    // The frame rate
    private double _frameRate = 30;

    // The StageView
    protected StageView _stageView;

    // Whether to auto-play game when shown
    private boolean _autoPlay = true;

    // The StageView box
    private BoxView _stageViewBox;

    // The animation timer
    private ViewTimer _timer = new ViewTimer(this::stepGameFrame, getFrameDelay());

    // Constants for properties
    public static final String StageView_Prop = "StageView";
    public static final String Playing_Prop = "Playing";

    /**
     * Constructor.
     */
    public GameController()
    {
        this(new StageView());
    }

    /**
     * Constructor for given StageView.
     */
    public GameController(StageView stageView)
    {
        super();
        setStageView(stageView);
        addPropChangeListener(pc -> handleShowingChange(), Showing_Prop);
    }

    /**
     * Returns the StageView.
     */
    public StageView getStageView()  { return _stageView; }

    /**
     * Sets the StageView.
     */
    public void setStageView(StageView stageView)
    {
        if (stageView == _stageView) return;
        batchPropChange(StageView_Prop, _stageView, _stageView = stageView);
        _stageView._controller = this;
        if (_stageViewBox != null)
            _stageViewBox.setContent(_stageView);
        if (isShowing())
            _stageView.requestFocus();
        setFirstFocus(_stageView);
        fireBatchPropChanges();
    }

    /**
     * Returns the frame rate.
     */
    public double getFrameRate()  { return _frameRate; }

    /**
     * Sets the frame rate.
     */
    public void setFrameRate(double aValue)
    {
        _frameRate = aValue;
        _timer.setPeriod(getFrameDelay());
    }

    /**
     * Returns the frame delay in milliseconds.
     */
    public int getFrameDelay()
    {
        return _frameRate <= 0 ? Integer.MAX_VALUE : (int) Math.round(1000 / _frameRate);
    }

    /**
     * Returns whether game auto-starts.
     */
    public boolean isAutoPlay()  { return _autoPlay; }

    /**
     * Sets whether game auto-starts.
     */
    public void setAutoPlay(boolean aValue)  { _autoPlay = aValue; }

    /**
     * Returns whether game is playing.
     */
    public boolean isPlaying()  { return _timer.isRunning(); }

    /**
     * Sets whether game is playing.
     */
    public void setPlaying(boolean aValue)
    {
        if (aValue == isPlaying()) return;
        if (aValue)
            _timer.start();
        else _timer.stop();
        firePropChange(Playing_Prop, !aValue, aValue);
    }

    /**
     * Starts the game timer.
     */
    public void playGame()  { setPlaying(true); }

    /**
     * Stops the game timer.
     */
    public void stopGame()  { setPlaying(false); }

    /**
     * Steps the game forward a frame.
     */
    public void stepGameFrame()
    {
        _stageView.stepGameFrame();
    }

    /**
     * Reset StageView.
     */
    public void resetStageView()
    {
        getStageView().stop();
        StageView stageView = ClassUtils.newInstance(_stageView.getClass());
        setStageView(stageView);
    }

    /**
     * Called when stage view has showing change.
     */
    protected void handleShowingChange()
    {
        // If showing, autostart
        if (isShowing()) {
            if (_autoPlay)
                runDelayed(this::playGame, 800);
            _autoPlay = false;
        }

        // If hiding, stop playing
        else {
            _autoPlay = isPlaying();
            stopGame();
        }
    }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        if (_stageView == null || _stageView.getClass() == StageView.class) {
            WebURL snapFileUrl = UILoader.getSnapUrlForClass(getClass());
            if (snapFileUrl != null && snapFileUrl.getFile().getExists())
                _stageView = (StageView) UILoader.loadViewForControllerAndUrl(this, snapFileUrl);
        }

        // Create BoxView to hold stage view
        _stageViewBox = new BoxView(_stageView, true, true);

        // If browser, wrap in ScaleBox to fit smaller spaces
        if (SnapEnv.isWebVM) {
            ScaleBox scaleBox = new ScaleBox(_stageViewBox);
            scaleBox.setPadding(5, 5, 5, 5);
            return scaleBox;
        }

        // Return
        return _stageViewBox;
    }

    /**
     * Override to maximize window when in browser.
     */
    @Override
    protected void initWindow(WindowView aWindow)
    {
        if (SnapEnv.isWebVM)
            aWindow.setMaximized(true);
    }
}