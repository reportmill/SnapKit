package snap.view;
import snap.geom.RectBase;
import snap.gfx.*;

/**
 * A class to manage painting an effect for a view.
 */
class ViewEffectPainter {
    
    // The View
    private View _view;
    
    // The Effect
    protected Effect _effect;

    // A PainterDVR to hold cached effect render from last pass
    private PainterDVR _pdvr;
    
    // A PainterDVR to hold plain render for compare
    private PainterDVR _pdvrX;

    // The view size when DVR was cached
    private double _viewW, _viewH;
    
    // The Focus Effect
    private static Effect _focusEffect;
    
    // The Focus ViewEffect
    private static ViewEffectPainter _focusEffectPainter;
    
    /**
     * Constructor for a given view and effect.
     */
    public ViewEffectPainter(View aView, Effect anEff)
    {
        _view = aView;
        _effect = anEff;
    }

    /**
     * Main paint method.
     */
    protected void paintAll(Painter aPntr)
    {
        // Make sure cache is up to date
        if (!isCacheValid())
            updateCache();

        // Execute PainterDVR to given painter
        _pdvr.exec(aPntr);

        // Shadow needs to draw front
        if (isShadow())
            paintAllView(aPntr);
    }

    /**
     * Returns whether cache is dirty.
     */
    private boolean isCacheValid()
    {
        // If no cache, return false
        if (_pdvr == null) return false;

        // If view size has changed, return false
        double viewW = _view.getWidth();
        double viewH = _view.getHeight();
        if (viewW != _viewW || viewH != _viewH)
            return false;

        // If simple shadow and size hasn't changed, return true
        if (_effect instanceof ShadowEffect shadowEffect && shadowEffect.isSimple())
            return true;

        // If there have been paint changes to view, return false
        if (_view.isNeedsRepaint() || _view instanceof ParentView parentView && parentView.isNeedsRepaintDeep())
            return false;

        // Return true since view hasn't changed size or needed repaint
        return true;
    }

    /**
     * Updates the PainterDVR that draws the effect and provides caching.
     */
    private void updateCache()
    {
        if (isShadow())
            updateCacheShadow();
        else updateCacheGeneric();
    }

    /**
     * Updates the PainterDVR that draws the effect and provides caching.
     */
    private void updateCacheGeneric()
    {
        // Do normal painting to new PainterDVR
        PainterDVR pdvr = new PainterDVR();
        paintAllView(pdvr);

        // If painting hasn't changed since last cache, just return
        double viewW = _view.getWidth();
        double viewH = _view.getHeight();
        if (viewW == _viewW && viewH == _viewH && pdvr.equals(_pdvrX))
            return;

        // Render and cache effect of painting to second PainterDVR
        _pdvr = new PainterDVR();
        _effect.applyEffect(pdvr, _pdvr, _view.getBoundsLocal());
        _viewW = viewW;
        _viewH = viewH;
        _pdvrX = pdvr;
    }

    /**
     * Updates the PainterDVR that draws the effect and provides caching.
     */
    private void updateCacheShadow()
    {
        // Get new PainterDVR and shadow and view size
        PainterDVR pdvr = new PainterDVR();
        ShadowEffect shadow = getShadow();
        double viewW = _view.getWidth();
        double viewH = _view.getHeight();

        // If simple, no shadow needed - otherwise if view covers bounds, just paint bounds, otherwise paint all
        if (!shadow.isSimple()) {
            if (_view.getFill() != null && _view.getBoundsShape() instanceof RectBase boundsRect)
                pdvr.fill(boundsRect);
            else paintAllView(pdvr);
        }

        // If painting hasn't changed since last cache, just return
        if (viewW == _viewW && viewH == _viewH && pdvr.equals(_pdvrX))
            return;

        // Render and cache effect of painting to second PainterDVR
        _pdvr = new PainterDVR();
        shadow.applyEffectShadowOnly(pdvr, _pdvr, _view.getBoundsLocal());
        _viewW = viewW;
        _viewH = viewH;
        _pdvrX = pdvr;
    }

    /**
     * Returns whether effect is shadow.
     */
    private boolean isShadow()  { return _effect instanceof ShadowEffect; }

    /**
     * Returns effect as shadow.
     */
    private ShadowEffect getShadow()  { return (ShadowEffect) _effect; }

    /**
     * Paints the view to given painter with standard paintAll.
     */
    private void paintAllView(Painter aPntr)
    {
        // Normal view paint
        _view.paintBack(aPntr);
        _view.paintFront(aPntr);

        // ParentView paint
        if (_view instanceof ParentView parentView) {
            parentView.paintChildren(aPntr);
            parentView.paintAbove(aPntr);
        }
    }

    /**
     * Returns the focus effect for given view.
     */
    public static Effect getFocusEffect()
    {
        if (_focusEffect != null) return _focusEffect;
        return _focusEffect = new ShadowEffect(5, ViewThemeUtils.getFocusColor(), 0, 0);
    }

    /**
     * Returns the focus effect painter for given view.
     */
    public static ViewEffectPainter getFocusEffectPainterForView(View aView)
    {
        if (_focusEffectPainter != null && _focusEffectPainter._view == aView)
            return _focusEffectPainter;
        return _focusEffectPainter = new ViewEffectPainter(aView, getFocusEffect());
    }
}