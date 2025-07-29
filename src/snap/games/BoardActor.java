package snap.games;

/**
 * An actor subclass for board games.
 */
public class BoardActor extends Actor {

    /**
     * Constructor.
     */
    public BoardActor()
    {
        super();
    }

    /**
     * Returns the cell X.
     */
    public int getCellX()
    {
        BoardGameView gameView = (BoardGameView) getGameView();
        double cellW = gameView != null ? gameView.getCellWidth() : 1;
        return (int) Math.floor(getX() / cellW);
    }

    /**
     * Returns the cell Y.
     */
    public int getCellY()
    {
        BoardGameView gameView = (BoardGameView) getGameView();
        double cellH = gameView != null ? gameView.getCellHeight() : 1;
        return (int) Math.floor(getY() / cellH);
    }
}
