/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.HPos;
import snap.gfx.Color;
import snap.gfx.Effect;
import snap.gfx.ShadowEffect;
import snap.view.*;

/**
 * A DialogBox subclass that shows as a sheet.
 */
public class DialogSheet extends DialogBox {

    // The animation time
    private int _animTime = 1000;

    // The parent view hosting the SheetDialogBox
    private ParentView  _hostView;

    // The BoxView to hold/clip the UI
    private BoxView  _sheetView;

    // The HostView children when last shown
    private View[]  _hostChildren;

    // Constants
    private static final Effect SHADOW_EFFECT = new ShadowEffect(10, Color.GRAY, 0, 0);
    private static final int BORDER_RADIUS = 5;

    /**
     * Constructor.
     */
    public DialogSheet()
    {
        super();
    }

    /**
     * Returns the animation time.
     */
    public int getAnimTime()  { return _animTime; }

    /**
     * Sets the animation time.
     */
    public void setAnimTime(int aValue)
    {
        if (aValue == getAnimTime()) return;
        _animTime = aValue;
    }

    /**
     * Show Dialog in sheet.
     */
    @Override
    protected boolean showPanel(View aView)
    {
        // Get given view as HostView
        _hostView = aView instanceof ParentView ? (ParentView) aView : null;
        if (_hostView == null)
            return super.showPanel(aView);
        _hostView.setClipToBounds(true);

        // Make current HostView.Children invisible to mouse clicks
        _hostChildren = _hostView.getChildrenArray().clone();
        for (View child : _hostChildren)
            child.setPickable(false);

        // Get SheetView and add to Host
        View sheetView = getUI();
        ViewUtils.addChild(_hostView, sheetView);

        // Configure UI to animate in and start
        double transY = sheetView.getHeight();
        sheetView.setTransY(-transY);
        sheetView.getAnim(getAnimTime()).setTransY(-BORDER_RADIUS).play();

        // Make sure stage and Builder.FirstFocus are focused
        runLater(() -> notifyDidShow());

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
        ViewAnim anim = ui.getAnimCleared(getAnimTime());
        anim.setTransY(-ui.getHeight());
        anim.setOnFinish(() -> hideAnimDone()).needsFinish().play();
    }

    /**
     * Called when hide() animation finishes.
     */
    private void hideAnimDone()
    {
        // Remove UI, reset everything pickable and notify of close
        ViewUtils.removeChild(_hostView, _sheetView);

        // Reset HostView.Children pickable
        for (View child : _hostChildren)
            child.setPickable(true);
        _hostChildren = null;
    }

    /**
     * Override to create UI.
     */
    @Override
    protected View createUI()
    {
        // Do normal version
        View superUI = super.createUI();
        superUI.setBorderRadius(BORDER_RADIUS);

        // Create box to hold/clip UI
        _sheetView = new BoxView(superUI);
        _sheetView.setFill(ViewUtils.getBackFill());
        _sheetView.setBorder(Color.GRAY, 1);
        _sheetView.setBorderRadius(BORDER_RADIUS);
        _sheetView.setEffect(SHADOW_EFFECT);
        _sheetView.setLeanX(HPos.CENTER);
        _sheetView.setManaged(false);

        // Set size
        _sheetView.setSizeToBestSize();

        // Return
        return _sheetView;
    }
}
