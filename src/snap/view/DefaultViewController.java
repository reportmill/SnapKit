package snap.view;

/**
 * An implementation of ViewController that has all abstract methods implemented with a standard default. To use this
 * class properly, the class methods must be overridden methods to use this class.
 */
public class DefaultViewController extends SimpleViewController {

    /**
     * Initializes the UI panel. This method provides the ability to alter any settings or components of the View that
     * were not set by {@link #createUI()}.
     * <br><br>
     * This method is called automatically by SnapKit after the view has been initialized, and does not need to be
     * called inside of an implementation.
     * <br><br>
     * Implementation note: It is not always necessary to implement this method, especially if the {@code createUI()}
     * method was written by hand. It provides a way to add more initialization logic when the class has been loaded
     * from a .snp file. By default, this method has no implementation.
     */
    @Override
    protected void initUI() {

    }

    /**
     * Called automatically by SnapKit after a user reacts with a UI component, this method allows the resetting of
     * the UI. It will not cause accidental {@code respondUI(ViewEvent)} calls. It allows the user to reset or change
     * aspects of the UI after an interaction, such as might be required for an animation or image draw.
     * <br> <br>
     * This method is overridable with no default implementation.
     */
    @Override
    protected void resetUI() {

    }

    /**
     * Called automatically by SnapKit when it detects a ViewEvent. This method should be overridden to respond to UI
     * controls, and provide feedback to user interactions.
     * <br>
     * If you are coming from a Swing environment, this class serves the same purposes as the action listeners attached
     * to each individual component. In this case, all of the events are funnelled into the same method, making it
     * easier to keep track of interactions. Everything is managed from the same location.
     * <br> <br>
     * This method is overridable with no default implementation.
     *
     * @param anEvent
     */
    @Override
    protected void respondUI(ViewEvent anEvent) {

    }
}
