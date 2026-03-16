package snap.view;

/**
 * A simple implementation of ViewController that has most functionality already implemented
 */
public abstract class SimpleViewController extends ViewController {

    /**
     * Creates the top level view for this controller by loading the UI from a .snp file in the 'resources' directory.
     * The .snp file loaded must share the same name as this class.
     * <br><br>
     * This method is called automatically by SnapKit, and does not need to be called inside an implementation.
     */
    @Override
    protected View createUI()  { return UILoader.loadViewForController(this); }

}
