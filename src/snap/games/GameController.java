package snap.games;
import snap.util.ClassUtils;
import snap.view.*;

/**
 * The controller class for a GameView.
 */
public class GameController extends ViewOwner {

    // The frame rate
    private double _frameRate = 30;

    // The GameView
    protected GameView _gameView;

    // Whether to auto-play game when shown
    private boolean _autoPlay = true;

    // The GameView box
    private BoxView _gameBox;

    // The animation timer
    private ViewTimer _timer = new ViewTimer(this::stepGameFrame, getFrameDelay());

    // Constants for properties
    public static final String GameView_Prop = "GameView";
    public static final String Playing_Prop = "Playing";

    /**
     * Constructor.
     */
    public GameController()
    {
        this(null);
    }

    /**
     * Constructor for given GameView.
     */
    public GameController(GameView gameView)
    {
        super();
        setGameView(gameView);
        addPropChangeListener(pc -> handleShowingChange(), Showing_Prop);
    }

    /**
     * Returns the GameView.
     */
    public GameView getGameView()  { return _gameView; }

    /**
     * Sets the GameView.
     */
    public void setGameView(GameView gameView)
    {
        if (gameView == _gameView) return;
        batchPropChange(GameView_Prop, _gameView, _gameView = gameView);
        _gameView._controller = this;
        if (_gameBox != null)
            _gameBox.setContent(_gameView);
        if (isShowing())
            _gameView.requestFocus();
        setFirstFocus(_gameView);
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
        _gameView.stepGameFrame();
    }

    /**
     * Reset GameView.
     */
    public void resetGameView()
    {
        getGameView().stop();
        GameView gameView = ClassUtils.newInstance(_gameView.getClass());
        setGameView(gameView);
    }

    /**
     * Called when game view has showing change.
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
        _gameBox = new BoxView(_gameView, true, true);
        return _gameBox;
    }
}