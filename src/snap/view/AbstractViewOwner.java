package snap.view;

public abstract class AbstractViewOwner extends ViewOwner {

    /**
     * Creates the top level view for this owner.
     * @apiNote If you want the vanilla interaction of this class, return {@code super.createUI();}
     */
    @Override
    abstract protected View createUI();

    /**
     * Initializes the UI panel.
     * @apiNote If you want the vanilla interaction of this class, return {@code super.initUI();}
     */
    @Override
    abstract protected void initUI();

    /**
     * Reset UI controls.
     * @apiNote If you want the vanilla interaction of this class, return {@code super.resetUI();}
     */
    @Override
    abstract protected void resetUI();

    /**
     * Respond to UI controls.
     * @apiNote If you want the vanilla interaction of this class, return {@code super.respondUI(ViewEvent);}
     * @param anEvent Any event that has been caught by this ViewOwner.
     */
    @Override
    abstract protected void respondUI(ViewEvent anEvent);
}
