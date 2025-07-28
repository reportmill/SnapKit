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
     * Override to add controls.
     */
    protected View createUI()
    {
        _gameView = new GameView();
        return _gameView;
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
     * Reset GameView.
     */
    protected void resetGameView()
    {
        getGameView().stop();
        View pauseBtn = getView("PauseButton");
        if (pauseBtn != null) {
            pauseBtn.setText("Run");
            pauseBtn.setName("RunButton");
            getView("ActButton").setDisabled(false);
        }

        GameView gameView = (GameView) super.createUI();
        _gameView.removeChildren();
        for (int i = 0, iMax = gameView.getChildCount(); i < iMax; i++)
            _gameView.addChild(gameView.getChild(0));

        //World world = null;
        //try { world = _firstWorld.getClass().newInstance(); }
        //catch(Exception e) { new RuntimeException(e); }
        //setWorld(world);
    }
}