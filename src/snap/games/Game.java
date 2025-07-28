package snap.games;
import snap.util.ClassUtils;
import snap.view.ViewUtils;

/**
 * A starter class.
 */
public class Game {

    /**
     * Shows a game in window for given GameView or GameController class.
     */
    public static void showGameForClass(Class<?> gameClass)
    {
        ViewUtils.runLater(() -> showGameForClassImpl(gameClass));
    }

    /**
     * Shows a game in window for given GameView or GameController class.
     */
    private static void showGameForClassImpl(Class<?> gameClass)
    {
        // Handle GameView
        if (GameView.class.isAssignableFrom(gameClass)) {
            GameView gameView = (GameView) ClassUtils.newInstance(gameClass);
            GameController gameController = new GameController(gameView);
            gameController.setWindowVisible(true);
        }

        // Handle GameController
        if (GameController.class.isAssignableFrom(gameClass)) {
            GameController gameController = (GameController) ClassUtils.newInstance(gameClass);
            gameController.setWindowVisible(true);
        }
    }

    /**
     * Shows a game in developer console for given GameView or GameController class.
     */
    public static void showDevConsoleForClass(Class<?> gameClass)
    {
        ViewUtils.runLater(() -> showDevConsoleForClassImpl(gameClass));
    }

    /**
     * Shows a game in window for given GameView or GameController class.
     */
    private static void showDevConsoleForClassImpl(Class<?> gameClass)
    {
        // Handle GameView
        if (GameView.class.isAssignableFrom(gameClass)) {
            GameView gameView = (GameView) ClassUtils.newInstance(gameClass);
            GameController gameController = new GameController(gameView);
            DevConsole devConsole = new DevConsole(gameController);
            devConsole.setWindowVisible(true);
        }

        // Handle GameController
        if (GameController.class.isAssignableFrom(gameClass)) {
            GameController gameController = (GameController) ClassUtils.newInstance(gameClass);
            DevConsole devConsole = new DevConsole(gameController);
            devConsole.setWindowVisible(true);
        }
    }
}
