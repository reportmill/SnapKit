package snap.games;
import snap.util.ClassUtils;
import snap.view.*;

/**
 * The controller class for a GameView.
 */
public class GameController extends ViewOwner {

    // The GameView box
    private BoxView _gameBox;

    // The GameView
    protected GameView _gameView;

    /**
     * Constructor.
     */
    public GameController()
    {
        super();
    }

    /**
     * Constructor for given GameView.
     */
    public GameController(GameView gameView)
    {
        super();
        _gameView = gameView;
    }

    /**
     * Returns the GameView.
     */
    public GameView getGameView()
    {
        if (_gameView == null) getUI();
        return _gameView;
    }

    /**
     * Steps the game.
     */
    public void stepGame()
    {
        getGameView().stepGameTime();
    }

    /**
     * Reset GameView.
     */
    public void resetGameView()
    {
        getGameView().stop();
        _gameView = ClassUtils.newInstance(_gameView.getClass());
        _gameView.setAutoPlay(false);
        _gameBox.setContent(_gameView);
    }

    /**
     * Override to add controls.
     */
    @Override
    protected View createUI()
    {
        if (_gameView == null)
            _gameView = new GameView();
        return _gameBox = new BoxView(_gameView, true, true);
    }
}