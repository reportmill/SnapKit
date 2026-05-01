package snap.view;

/**
 * A simple implementation of ViewController that has most functionality already implemented. This implementation will
 * load the UI from a .snp file named the same as this class, located in the resources dir of the jar package. The UI
 * can be managed by implementing the {@link #initUI()} method, and referencing the names of components inside of that
 * loaded View. To add events or feedback to elements of the UI, implement the {@link #respondUI(ViewEvent)} method and
 * add your logic there.
 * <br>
 * All abstract methods of this class can be implemented or left empty depending on your needs.
 */
public abstract class SimpleViewController extends ViewController {

    /**
     * Constructor.
     */
    public SimpleViewController()  {super();}
    /**
     * Constructor with given View for UI.
     */
    public SimpleViewController(View aView) {super(aView);}

    /**
     * Creates the top level view for this controller by loading the UI from a .snp file in the 'resources' directory.
     * The .snp file loaded must share the same name as this class.
     * <br><br>
     * This method is called automatically by SnapKit, and does not need to be called inside an implementation.
     */
    @Override
    protected View createUI()  { return UILoader.loadViewForController(this); }

}
