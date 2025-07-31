package snap.games;
import snap.gfx.Image;
import snap.util.ClassUtils;
import snap.view.ViewUtils;
import java.util.HashMap;
import java.util.List;

/**
 * A starter class.
 */
public class Game {

    // A cache of game images
    private static HashMap<String,Image> _images = new HashMap<>();

    // Constant for no image
    private static Image NULL_IMAGE = Image.getImageForSize(1, 1, true);

    // Constant for image file types
    private static final List<String> IMAGE_FILE_TYPES = List.of(".png", ".jpg", ".jpeg", ".gif");

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
            GameController gameController = gameView.getController();
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
            GameController gameController = gameView.getController();
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
        Image image = _images.get(imageName);
        return image != NULL_IMAGE ? image : null;
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
        if (image == null)
            image = Image.getImageForClassResource(aClass, "images/" + imageName);
        if (image != null)
            setImageForName(imageName, image);
        return image;
    }

    /**
     * Returns the default image for given class.
     */
    public static Image getImageForClass(Class<?> aClass)
    {
        Image image = _images.get(aClass.getName());
        if (image != null)
            return image != NULL_IMAGE ? image : null;

        // Look for all possible image names and return if found
        for (String fileType : IMAGE_FILE_TYPES) {
            String imageName = aClass.getSimpleName() + fileType;
            image = getImageForClassResource(aClass, imageName);
            if (image != null) {
                setImageForName(aClass.getName(), image);
                return image;
            }
        }

        // Put null image in cache and return not found
        setImageForName(aClass.getName(), NULL_IMAGE);
        return null;
    }
}
