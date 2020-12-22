package snap.viewx;
import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snap.view.ViewUpdater;

/**
 * A DevPane tab for inspecting Graphics.
 */
public class DevPaneGraphics extends ViewOwner {

    // The DevPane
    private DevPane  _devPane;

    /**
     * Constructor.
     */
    public DevPaneGraphics(DevPane aDevPane)
    {
        super();
        _devPane = aDevPane;
    }

    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ShowFlashButton
        if (anEvent.equals("ShowFlashButton"))
            ViewUpdater.setPaintDebug(anEvent.getBoolValue());

        // Handle ShowFrameRateButton
        if (anEvent.equals("ShowFrameRateButton"))
            ViewUpdater.setShowFrameRate(anEvent.getBoolValue());
    }
}
