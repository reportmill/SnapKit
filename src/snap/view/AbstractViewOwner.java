package snap.view;

public abstract class AbstractViewOwner extends ViewOwner {

    /**
     * Creates the top level view for this owner.
     * <br> <br>
     * Implementation note: If you want the vanilla interaction of this class, return super.createUI();
     */
    @Override
    abstract protected View createUI();

    /**
     * Initializes the UI panel.
     * <br> <br>
     * Implementation note: If you want the vanilla interaction of this class, return super.initUI();
     */
    @Override
    abstract protected void initUI();

    /**
     * Reset UI controls.
     * <br> <br>
     * Implementation note: If you want the vanilla interaction of this class, return super.resetUI();
     */
    @Override
    abstract protected void resetUI();

    /**
     * Respond to UI controls.
     * <br> <br>
     * Implementation note: If you want the vanilla interaction of this class, return super.respondUI(ViewEvent);
     * @param anEvent Any event that has been caught by this ViewOwner.
     */
    @Override
    abstract protected void respondUI(ViewEvent anEvent);
}
