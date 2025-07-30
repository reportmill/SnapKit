package snap.games;
import snap.gfx.Image;
import snap.util.ClassUtils;
import snap.view.ViewUtils;
import java.util.HashMap;

/**
 * A starter class.
 */
public class Game {

    // A cache of game images
    private static HashMap<String,Image> _images = new HashMap<>();

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

    /**
     * Returns an image for given name.
     */
    public static Image getImageForName(String imageName)
    {
        return _images.get(imageName);
    }


    /**
     * Returns an image for given name.
     */
    public static void setImageForName(String imageName, Image image)
    {
        _images.put(imageName, image);
    }

    /**
     * Returns an image for given name and class.
     */
    public static Image getImageForClassResource(Class<?> aClass, String imageName)
    {
        Image image = getImageForName(imageName);
        if (image != null)
            return image;

        image = Image.getImageForClassResource(aClass, imageName);
        if (image != null)
            setImageForName(imageName, image);
        return image;
    }
}
