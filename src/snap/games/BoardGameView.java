package snap.games;

/**
 *
 */
public class BoardGameView extends GameView {

    /**
     * Constructor.
     */
    public BoardGameView()
    {
        super();
    }

    /**
     * Returns the number of cells available on X axis.
     */
    public int getCellsWide()
    {
        return (int) Math.round(getWidth());
    }

    /**
     * Returns the number of cells available on Y axis.
     */
    public int getCellsHigh()
    {
        return (int) Math.round(getHeight());
    }

    /**
     * Returns the cell width.
     */
    public double getCellWidth()
    {
        return getWidth() / getCellsWide();
    }

    /**
     * Returns the cell height.
     */
    public double getCellHeight()
    {
        return getHeight() / getCellsHigh();
    }

}
