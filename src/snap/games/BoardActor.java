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
        BoardStageView stageView = getStageView(BoardStageView.class);
        double cellW = stageView != null ? stageView.getCellWidth() : 1;
        return (int) Math.floor(getX() / cellW);
    }

    /**
     * Returns the cell Y.
     */
    public int getCellY()
    {
        BoardStageView stageView = getStageView(BoardStageView.class);
        double cellH = stageView != null ? stageView.getCellHeight() : 1;
        return (int) Math.floor(getY() / cellH);
    }
}
