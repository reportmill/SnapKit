package snap.view;

/**
 * Helper class to create custom ViewOwners with novel interpretations.
 */
public abstract class CustomViewOwner extends ViewOwner {

    /**
     * Creates the top level view for this owner.
     * <br><br>
     * API Note: If you want the vanilla interaction of this class, return {@code default_createUI();}
     */
    @Override
    abstract protected View createUI();

    /**
     * Initializes the UI panel.
     * <br><br>
     * API Note: If you want the vanilla interaction of this class, return {@code default_initUI();}
     */
    @Override
    abstract protected void initUI();

    /**
     * Reset UI controls.
     * <br><br>
     * API Note: If you want the vanilla interaction of this class, return {@code default_resetUI();}
     */
    @Override
    abstract protected void resetUI();

    /**
     * Respond to UI controls.
     * <br><br>
     * API Note: If you want the vanilla interaction of this class, return {@code default_respondUI(ViewEvent);}
     * @param anEvent Any event that has been caught by this ViewOwner.
     */
    @Override
    abstract protected void respondUI(ViewEvent anEvent);

    /**
     * Default implementation of {@link ViewOwner#createUI()}
     * @return View loaded from snp file of the same name as the class.
     */
    final protected View default_createUI() {return super.createUI();}

    /**
     * Default implementation of {@link ViewOwner#initUI()}
     */
    final protected void default_initUI() {super.initUI();}

    /**
     * Default implementation of {@link ViewOwner#resetUI()}
     */
    final protected void default_resetUI() {super.resetUI();}

    /**
     * Default implementation of {@link ViewOwner#respondUI(ViewEvent)}
     * @param anEvent An event which has been captured by the view.
     */
    final protected void default_respondUI(ViewEvent anEvent) {super.respondUI(anEvent);}
}
