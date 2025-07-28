package snap.games;
import snap.view.*;

/**
 * The controller class for a GameView.
 */
public class GameController extends ViewOwner {

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
        getGameView().doAct();
    }

    /**
     * Reset GameView.
     */
    public void resetGameView()
    {
        getGameView().stop();

        GameView gameView = (GameView) super.createUI();
        _gameView.removeChildren();
        for (int i = 0, iMax = gameView.getChildCount(); i < iMax; i++)
            _gameView.addChild(gameView.getChild(0));
    }

    /**
     * Override to add controls.
     */
    protected View createUI()
    {
        if (_gameView == null)
            _gameView = new GameView();
        return _gameView;
    }
}