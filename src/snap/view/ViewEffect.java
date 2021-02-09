package snap.view;
import snap.geom.RectBase;
import snap.gfx.*;

/**
 * A class to manage an effect for a view.
 */
class ViewEffect {
    
    // The View
    private View  _view;
    
    // The Effect
    protected Effect  _eff;

    // A PainterDVR to hold cached effect render from last pass (and one to hold plain render for compare)
    private PainterDVR  _pdvr, _pdvrX;
    
    // The view size when DVR was cached
    private double  _viewW, _viewH;
    
    // The Focus Effect
    private static Effect  _focEff;
    
    // The Focus ViewEffect
    private static ViewEffect  _focVEff;
    
    // The Focused color
    public static Color  FOCUSED_COLOR = Color.get("#039ed3");
    
    /**
     * Creates a ViewEffect for a given view and effect.
     */
    public ViewEffect(View aView, Effect anEff)
    {
        _view = aView;
        _eff = anEff;
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
    boolean isCacheValid()
    {
        // If no cache, return false
        if (_pdvr==null) return false;

        // If view size has changed, return false
        double viewW = _view.getWidth();
        double viewH = _view.getHeight();
        if (viewW!= _viewW || viewH!= _viewH) return false;

        // If simple shadow and size hasn't changed, return true
        if (isShadow() && getShadow().isSimple())
            return true;

        // If there have been paint changes to view, return false
        if (_view.isNeedsRepaint() || isNeedsRepaintDeep(_view))
            return false;

        // Return true since view hasn't changed size or needed repaint
        return true;
    }

    /**
     * Updates the PainterDVR that draws the effect and provides caching.
     */
    void updateCache()
    {
        if (isShadow())
            updateCacheShadow();
        else updateCacheGeneric();
    }

    /**
     * Updates the PainterDVR that draws the effect and provides caching.
     */
    void updateCacheGeneric()
    {
        // Do normal painting to new PainterDVR
        PainterDVR pdvr = new PainterDVR();
        paintAllView(pdvr);

        // If painting hasn't changed since last cache, just return
        double viewW = _view.getWidth();
        double viewH = _view.getHeight();
        if (viewW== _viewW && viewH== _viewH && pdvr.equals(_pdvrX))
            return;

        // Render and cache effect of painting to second PainterDVR
        _pdvr = new PainterDVR();
        _eff.applyEffect(pdvr, _pdvr, _view.getBoundsLocal());
        _viewW = viewW;
        _viewH = viewH;
        _pdvrX = pdvr;
    }

    /**
     * Updates the PainterDVR that draws the effect and provides caching.
     */
    void updateCacheShadow()
    {
        // Get new PainterDVR and shadow and view size
        PainterDVR pdvr = new PainterDVR();
        ShadowEffect shadow = getShadow();
        double viewW = _view.getWidth();
        double viewH = _view.getHeight();

        // If simple, no shadow needed - otherwise if view covers bounds, just paint bounds, otherwise paint all
        if (!shadow.isSimple()) {
            if (_view.getFill()!=null && _view.getBoundsShape() instanceof RectBase)
                pdvr.fill(_view.getBoundsShape());
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
    public boolean isShadow()  { return _eff instanceof ShadowEffect; }

    /**
     * Returns effect as shadow.
     */
    public ShadowEffect getShadow()  { return (ShadowEffect)_eff; }

    /**
     * Returns whether view is parent.
     */
    public boolean isParentView()  { return _view instanceof ParentView; }

    /**
     * Returns view as parent.
     */
    public ParentView getParentView()  { return (ParentView)_view; }

    /**
     * Paints the view to given painter with standard paintAll.
     */
    protected void paintAllView(Painter aPntr)
    {
        // Normal view paint
        _view.paintBack(aPntr);
        _view.paintFront(aPntr);

        // ParentView paint
        if (isParentView()) { ParentView pv = getParentView();
            pv.paintChildren(aPntr);
            pv.paintAbove(aPntr);
        }
    }

    /**
     * Returns the focus effect for given view.
     */
    public static Effect getFocusEffect()
    {
        if (_focEff!=null) return _focEff;
        return _focEff = new ShadowEffect(5, FOCUSED_COLOR, 0, 0);
    }

    /**
     * Returns the focus effect for given view.
     */
    public static ViewEffect getFocusViewEffect(View aView)
    {
        if (_focVEff!=null && _focVEff._view==aView) return _focVEff;
        return _focVEff = new ViewEffect(aView, getFocusEffect());
    }

    /** Convenience. */
    private static final boolean isNeedsRepaintDeep(View aView)
    {
        return aView instanceof ParentView &&  ((ParentView)aView).isNeedsRepaintDeep();
    }
}