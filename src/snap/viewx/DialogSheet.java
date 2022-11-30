/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.HPos;
import snap.geom.Size;
import snap.gfx.Color;
import snap.view.*;

/**
 * A DialogBox subclass that shows as a sheet.
 */
public class DialogSheet extends DialogBox {

    // The parent view hosting the SheetDialogBox
    private ChildView  _hostView;

    // The BoxView to hold/clip the UI
    private BoxView  _clipBox;

    /**
     * Show Dialog in sheet.
     */
    @Override
    protected boolean showPanel(View aView)
    {
        // Get given view as HostView
        _hostView = aView instanceof ChildView ? (ChildView) aView : null;
        if (_hostView == null)
            return super.showPanel(aView);

        // Make Other views invisible to mouse clicks
        for (View child : _hostView.getChildren())
            child.setPickable(false);

        // Create/configure UI
        View ui = getUI();
        ui.setManaged(false);
        ui.setFill(ViewUtils.getBackFill());
        ui.setBorder(Color.DARKGRAY, 1);
        Size size = ui.getPrefSize();
        ui.setSize(size);

        // Create box to hold/clip UI
        _clipBox = new BoxView(ui);
        _clipBox.setSize(size);
        _clipBox.setManaged(false);
        _clipBox.setLeanX(HPos.CENTER);
        _clipBox.setClipToBounds(true);

        // Add UI box to HostView
        _hostView.addChild(_clipBox);

        // Configure UI to animate in and start
        ui.setTransY(-size.height);
        ui.getAnim(1000).setTransY(-1).play();

        // Make sure stage and Builder.FirstFocus are focused
        runLater(() -> notifyDidShow());
        setShowing(true);

        return true;
    }

    /**
     * Hide dialog.
     */
    @Override
    protected void hide()
    {
        // Configure UI to animate out and start
        View ui = getUI();
        ViewAnim anim = ui.getAnimCleared(1000);
        anim.setTransY(-ui.getHeight());
        anim.setOnFinish(() -> hideAnimDone()).needsFinish().play();
    }

    /**
     * Called when hide() animation finishes.
     */
    private void hideAnimDone()
    {
        // Remove UI, reset everything pickable and notify of close
        _hostView.removeChild(_clipBox);
        for (View child : _hostView.getChildren())
            child.setPickable(true);
        setShowing(false);
    }
}
